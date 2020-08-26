package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.bgsoftware.superiorprison.plugin.data.UniversalDataHolder;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.statistic.SStatisticsContainer;
import com.bgsoftware.superiorprison.plugin.util.input.PlayerInput;
import com.bgsoftware.superiorprison.plugin.util.input.multi.MultiPlayerInput;
import com.google.common.collect.ImmutableMap;
import com.oop.datamodule.body.MultiTypeBody;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.datamodule.storage.SqlStorage;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdMigrate extends OCommand {
    public CmdMigrate() {
        label("migrate");
        argument(new EnumArg<>(MigrationSource::values, MigrationSource::match).setIdentity("source").setRequired(true));
        argument(new EnumArg<>(MigrationType::values, MigrationType::match).setIdentity("type").setRequired(true));
        ableToExecute(Player.class);
        onCommand(command -> {
            MigrationSource source = command.getArgAsReq("source");
            MigrationType type = command.getArgAsReq("type");

            switch (type) {
                case MINES:
                    migrateMines(
                            source,
                            command.getSenderAsPlayer(),
                            null,
                            null
                    );
                    break;

                case PRISONERS:
                    migratePrisoners(
                            source,
                            command.getSenderAsPlayer(),
                            null,
                            null
                    );
                    break;

                case STATISTICS:
                    migrateStatistics(
                            source,
                            command.getSenderAsPlayer(),
                            null,
                            null
                    );
                    break;

                case ALL:
                    AtomicReference<DatabaseWrapper> databaseReference = new AtomicReference<>();
                    DatabaseWrapper wrapper = migrateMines(source, command.getSenderAsPlayer(), () -> {
                        migratePrisoners(source, command.getSenderAsPlayer(), () -> {
                            migrateStatistics(source, command.getSenderAsPlayer(), () -> {}, databaseReference.get());
                        }, databaseReference.get());
                    }, null);
                    databaseReference.set(wrapper);
                    break;
            }
        });
    }

    public DatabaseWrapper migrateMines(MigrationSource source, Player player, Runnable callback, DatabaseWrapper databaseWrapper) {
        return migrate(
                source,
                MigrationType.MINES,
                player,
                String.class,
                SNormalMine.class,
                () -> new SMineHolder(SuperiorPrisonPlugin.getInstance().getDatabaseController()),
                SNormalMine::getKey,
                SuperiorPrisonPlugin.getInstance().getDatabaseController().getMineHolder(),
                SNormalMine::getKey,
                callback,
                databaseWrapper
        );
    }

    public void migratePrisoners(MigrationSource source, Player player, Runnable callback, DatabaseWrapper databaseWrapper) {
        migrate(
                source,
                MigrationType.PRISONERS,
                player,
                UUID.class,
                SPrisoner.class,
                () -> new SPrisonerHolder(SuperiorPrisonPlugin.getInstance().getDatabaseController()),
                SPrisoner::getUUID,
                SuperiorPrisonPlugin.getInstance().getDatabaseController().getPrisonerHolder(),
                prisoner -> prisoner.getOfflinePlayer().getName(),
                callback,
                databaseWrapper
        );
    }

    public void migrateStatistics(MigrationSource source, Player player, Runnable callback, DatabaseWrapper databaseWrapper) {
        migrate(
                source,
                MigrationType.STATISTICS,
                player,
                UUID.class,
                SStatisticsContainer.class,
                () -> new SStatisticHolder(SuperiorPrisonPlugin.getInstance().getDatabaseController()),
                SStatisticsContainer::getUuid,
                SuperiorPrisonPlugin.getInstance().getDatabaseController().getStatisticHolder(),
                SStatisticsContainer::getKey,
                callback,
                databaseWrapper
        );
    }

    @SneakyThrows
    private <T extends MultiTypeBody, K extends Object> DatabaseWrapper migrate(
            @NonNull MigrationSource source,
            @NonNull MigrationType type,
            @NonNull Player player,
            @NonNull Class<K> keyClass,
            @NonNull Class<T> bodyClass,
            @NonNull Supplier<UniversalDataHolder<K, T>> holderSupplier,
            @NonNull Function<T, K> keyGatherer,
            @NonNull UniversalDataHolder<K, T> currentStorage,
            @NonNull Function<T, String> identityGatherer,
            Runnable callback,
            DatabaseWrapper databaseWrapperCache
    ) {

        // Initialize Reference for DatabaseWrapper if existent
        AtomicReference<DatabaseWrapper> databaseWrapper = new AtomicReference<>();

        Constructor<T> templateConstruct = bodyClass.getDeclaredConstructor();
        templateConstruct.setAccessible(true);

        T templateObject = templateConstruct.newInstance();
        String serializedType = templateObject.getSerializedType();

        UniversalDataHolder<K, T> tempHolder = holderSupplier.get();

        // Initialize migration runnable
        Runnable migrationStart = () -> {
            messageBuilder(LocaleEnum.MIGRATION_START.getWithPrefix())
                    .replace("{type}", type.name().toLowerCase())
                    .send(player);

            tempHolder.load(true, () -> {
                LocaleEnum.MIGRATION_SOURCE_LOADED.getWithPrefix().send(player);

                // Create queue of migration objects
                Queue<T> migrationQueue = new ConcurrentLinkedDeque<>();
                for (T object : tempHolder)
                    migrationQueue.add(object);

                // Check if queue is empty
                if (migrationQueue.isEmpty()) {
                    messageBuilder(LocaleEnum.MIGRATION_FAILED_CAUSE.getWithErrorPrefix())
                            .replace("{cause}", "Source is empty")
                            .send(player);
                    return;
                }

                migrateObject(
                        player,
                        migrationQueue,
                        currentStorage,
                        keyGatherer,
                        identityGatherer,
                        () -> {
                            messageBuilder(LocaleEnum.MIGRATION_SUCCESSFUL.getWithPrefix())
                                    .replace("{type}", type.name().toLowerCase())
                                    .send(player);

                            if (callback != null)
                                callback.run();
                        }
                );
            });
        };

        // Initialize storage
        switch (source) {
            case FLAT:
                tempHolder.currentHolder = new UniversalDataHolder.DataSettings.FlatStorageSettings<T>()
                        .directory(new File(SuperiorPrisonPlugin.getInstance().getDataFolder() + "/statistics"))
                        .variants(ImmutableMap.of(serializedType, bodyClass))
                        .toStorage(tempHolder);
                LocaleEnum.MIGRATION_SOURCE_TEST_SUCCESSFUL.getWithPrefix().send(player);
                migrationStart.run();
                break;

            case SQLITE:
                if (!new File(SuperiorPrisonPlugin.getInstance().getDataFolder(), "data.db").exists()) {
                    messageBuilder(LocaleEnum.MIGRATION_FAILED_CAUSE.getWithErrorPrefix())
                            .replace("{cause}", "Source was not found.")
                            .send(player);
                    return databaseWrapper.get();
                }

                tempHolder.currentHolder = new UniversalDataHolder.DataSettings.SQlSettings<T>()
                        .directory(SuperiorPrisonPlugin.getInstance().getDataFolder())
                        .database("data")
                        .databaseWrapper(databaseWrapperCache)
                        .variants(new Class[]{bodyClass})
                        .toStorage(tempHolder);
                LocaleEnum.MIGRATION_SOURCE_TEST_SUCCESSFUL.getWithPrefix().send(player);
                databaseWrapper.set(((SqlStorage) tempHolder.currentHolder).getDatabase());
                migrationStart.run();
                break;

            case MYSQL:
                Runnable onStorageSet = () -> {
                    try {
                        ((SqlStorage) tempHolder.currentHolder).getDatabase().getConnection();
                    } catch (Throwable ex) {
                        messageBuilder(LocaleEnum.MIGRATION_FAILED_CAUSE.getWithErrorPrefix())
                                .replace("{cause}", ex.getMessage())
                                .send(player);
                        return;
                    }

                    LocaleEnum.MIGRATION_SOURCE_TEST_SUCCESSFUL.getWithPrefix().send(player);
                    migrationStart.run();

                    databaseWrapper.set(((SqlStorage) tempHolder.currentHolder).getDatabase());
                };

                if (databaseWrapperCache != null) {
                    tempHolder.currentHolder = new UniversalDataHolder.DataSettings.SQlSettings<T>()
                            .databaseWrapper(databaseWrapperCache)
                            .toStorage(tempHolder);
                    onStorageSet.run();

                } else {
                    new MultiPlayerInput(player)
                            .add(MultiPlayerInput.InputData.string().id("hostname").requestMessage((OMessage) LocaleEnum.MIGRATION_CREDENTIALS_ASK.getWithPrefix().clone().replace("{credential}", "hostname")))
                            .add(MultiPlayerInput.InputData.integer().id("port").requestMessage((OMessage) LocaleEnum.MIGRATION_CREDENTIALS_ASK.getWithPrefix().clone().replace("{credential}", "port")))
                            .add(MultiPlayerInput.InputData.string().id("username").requestMessage((OMessage) LocaleEnum.MIGRATION_CREDENTIALS_ASK.getWithPrefix().clone().replace("{credential}", "username")))
                            .add(MultiPlayerInput.InputData.string().id("password").requestMessage((OMessage) LocaleEnum.MIGRATION_CREDENTIALS_ASK.getWithPrefix().clone().replace("{credential}", "password")))
                            .add(MultiPlayerInput.InputData.string().id("database").requestMessage((OMessage) LocaleEnum.MIGRATION_CREDENTIALS_ASK.getWithPrefix().clone().replace("{credential}", "database name")))
                            .onInput(($, input) -> {
                                tempHolder.currentHolder = new UniversalDataHolder.DataSettings.SQlSettings<T>()
                                        .database(input.getAsReq("database"))
                                        .hostname(input.getAsReq("hostname"))
                                        .port(input.getAsReq("port"))
                                        .username(input.getAsReq("username"))
                                        .password(input.getAsReq("password"))
                                        .variants(new Class[]{bodyClass})
                                        .toStorage(tempHolder);
                                onStorageSet.run();
                            })
                            .listen();
                }
                break;
        }
        return databaseWrapper.get();
    }

    private <T extends MultiTypeBody, K> void migrateObject(
            @NonNull Player player,
            @NonNull Queue<T> queue,
            @NonNull UniversalDataHolder<K, T> currentStorage,
            @NonNull Function<T, K> keyGatherer,
            @NonNull Function<T, String> identityGatherer,
            @NonNull Runnable callback
    ) {
        if (queue.isEmpty()) {
            callback.run();
            return;
        }

        T migratedObject = queue.poll();
        if (migratedObject == null) {
            migrateObject(player, queue, currentStorage, keyGatherer, identityGatherer, callback);
            return;
        }

        if (currentStorage.dataMap.containsKey(keyGatherer.apply(migratedObject))) {
            messageBuilder(LocaleEnum.MIGRATION_DUPLICATE.getMessage())
                    .replace("{identity}", identityGatherer.apply(migratedObject))
                    .send(player);
            new PlayerInput<String>(player)
                    .parser(in -> in)
                    .onInput((obj, in) -> {
                        if (in.equalsIgnoreCase("source")) {
                            T currentObject = currentStorage.dataMap.get(keyGatherer.apply(migratedObject));
                            currentStorage.remove(currentObject);

                            currentStorage.add(migratedObject);
                        }
                        obj.cancel();
                        migrateObject(player, queue, currentStorage, keyGatherer, identityGatherer, callback);
                    })
                    .listen();
        } else
            currentStorage.add(migratedObject);
    }

    private static class EnumArg<T extends Enum> extends CommandArgument<T> {
        private Supplier<T[]> valuesSupplier;

        public EnumArg(Supplier<T[]> valuesSupplier, Function<String, T> mapper) {
            this.valuesSupplier = valuesSupplier;
            setMapper(in -> {
                T value = mapper.apply(in);
                return new OPair<>(value, "Failed to find " + getIdentity() + " by " + in);
            });
        }

        @Override
        public void onAdd(OCommand command) {
            command.nextTabComplete(((completionResult, strings) -> Arrays.stream(valuesSupplier.get()).map(Enum::name).collect(Collectors.toList())));
        }
    }

    private static enum MigrationType {
        ALL,
        PRISONERS,
        MINES,
        STATISTICS;

        public static MigrationType match(String in) {
            for (MigrationType value : values())
                if (value.name().equalsIgnoreCase(in)) return value;

            return null;
        }
    }

    private static enum MigrationSource {
        MYSQL,
        FLAT,
        SQLITE;

        public static MigrationSource match(String in) {
            for (MigrationSource value : values())
                if (value.name().equalsIgnoreCase(in)) return value;

            return null;
        }
    }
}
