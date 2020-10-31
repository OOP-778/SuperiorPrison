package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.api.controller.PrisonerHolder;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.oop.datamodule.database.TableEditor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SPrisonerHolder extends UniversalDataHolder<UUID, SPrisoner> implements PrisonerHolder {

    @Getter
    private final Map<String, UUID> usernameToUuidMap = Maps.newConcurrentMap();

    public SPrisonerHolder(DatabaseController controller) {
        super(controller, SPrisoner::getUUID);

        String type = SuperiorPrisonPlugin.getInstance().getMainConfig().getDatabase().getType();
        if (type.equalsIgnoreCase("flat")) {
            currentHolder(
                    DataSettings.builder(DataSettings.FlatStorageSettings.class, SPrisoner.class)
                            .directory(new File(SuperiorPrisonPlugin.getInstance().getDataFolder() + "/prisoners"))
                            .variants(ImmutableMap.of("prisoner", SPrisoner.class))
            );
        } else if (type.equalsIgnoreCase("sqlite") || type.equalsIgnoreCase("mysql")) {
            currentHolder(
                    DataSettings.builder(DataSettings.SQlSettings.class, SPrisoner.class)
                            .databaseWrapper(controller.getDatabase())
                            .variants(new Class[]{SPrisoner.class})
            );
        }

        if (controller.getDatabase() != null) {
            new TableEditor("prisoners")
                    .renameColumn("prestiges", "currentPrestige")
                    .addColumn("currentLadderRank", "TEXT")
                    .addDropColumn("ranks")
                    .edit(controller.getDatabase());
        }
    }

    public Stream<SPrisoner> streamOnline() {
        return stream().filter(SPrisoner::isOnline);
    }

    @Override
    public Optional<Prisoner> getPrisoner(UUID uuid) {
        return Optional.ofNullable(getDataMap().get(uuid));
    }

    @Override
    public Stream<SPrisoner> stream() {
        return super
                .stream()
                .filter(prisoner -> !prisoner.isRemoved());
    }

    @Override
    public Optional<Prisoner> getPrisoner(String username) {
        UUID uuid = usernameToUuidMap.get(username);
        if (uuid == null) {
            if (username.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
                return getPrisoner(UUID.fromString(username));

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            if (offlinePlayer == null) return Optional.empty();
            uuid = offlinePlayer.getUniqueId();
            usernameToUuidMap.put(username, uuid);
        }

        return getPrisoner(uuid);
    }

    public SPrisoner getInsertIfAbsent(Player player) {
        return getInsertIfAbsent(player.getUniqueId());
    }

    private SPrisoner newPrisoner(SPrisoner prisoner) {
        add(prisoner);
        return prisoner;
    }


    public SPrisoner getInsertIfAbsent(UUID uuid) {
        Optional<Prisoner> optionalPrisoner = getPrisoner(uuid);
        return optionalPrisoner
                .map(prisoner -> (SPrisoner) prisoner)
                .orElseGet(() -> newPrisoner(new SPrisoner(uuid)));
    }

    public void initializeCache() {
        for (SPrisoner prisoner : this) {
            usernameToUuidMap.put(prisoner.getOfflinePlayer().getName(), prisoner.getUUID());
        }
    }

    public Map<UUID, SPrisoner> getPrisonerMap() {
        return dataMap;
    }

    public void cleanInvalids() {
        long start = System.currentTimeMillis();
        long count = stream()
                .filter(prisoner -> prisoner.getOfflinePlayer() == null || prisoner.getOfflinePlayer().getName() == null)
                .peek(this::remove)
                .count();

        SuperiorPrisonPlugin.getInstance().getOLogger().print("Prisoner Invalidation DONE ({}) Took {}ms", count, (System.currentTimeMillis() - start));
    }
}
