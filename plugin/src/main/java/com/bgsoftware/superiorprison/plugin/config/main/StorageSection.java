package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.lib.mongodb.client.MongoDatabase;
import com.oop.datamodule.mongodb.MongoCredential;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.mysql.MySQLDatabase;
import com.oop.datamodule.sqlite.SQLiteCredential;
import com.oop.datamodule.sqlite.SQLiteDatabase;
import com.oop.datamodule.universal.StorageProviders;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.ConfigValue;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class StorageSection {
    private Map<String, Function<UniversalStorage, Storage>> providers = new HashMap<>();
    private Map<String, String> objectsTypes = new HashMap<>();

    public StorageSection(Config config) {
        ConfigSection storageSection = config.createSection("storages");
        if (storageSection.getComments().isEmpty())
            storageSection.getComments()
                    .addAll(Arrays.asList(
                            "Setup of databases",
                            "You can define what database",
                            "Will be used per object type",
                            "You can set default",
                            "And then you can set per object",
                            "Available objects: prisoners, mines, statistics"
                    ));

        config.getSection("database").ifPresent(databaseSection -> {
            String type = databaseSection.getAs("type");
            String database = databaseSection.getAs("database");
            String hostname = databaseSection.getAs("hostname");
            String username = databaseSection.getAs("username");
            String password = databaseSection.getAs("password");
            int port = databaseSection.getAs("port");
            ConfigSection configSection = Objects.requireNonNull(initDefaultTypes(storageSection));

            ConfigSection defaultType = configSection.getSection(type.toLowerCase(Locale.ROOT)).get();
            defaultType.set("enabled", true);
            if (type.equalsIgnoreCase("sqlite"))
                defaultType.set("database", database);

            else if (type.equalsIgnoreCase("mysql")) {
                defaultType.set("database", database);
                defaultType.set("hostname", hostname);
                defaultType.set("username", username);
                defaultType.set("password", password);
                defaultType.set("port", port);
            }

            storageSection.set("default", type.toLowerCase(Locale.ROOT));
            config.getSections().remove(databaseSection.getKey());
        });

        String defaultType = storageSection.getAs("default");
        for (ConfigSection type : storageSection.getSection("types").get().getSections().values())
            registerProvider(type);

        objectsTypes.put("default", defaultType);
        for (ConfigValue value : storageSection.getValues().values())
            objectsTypes.put(value.getKey(), value.getAs(String.class));
    }

    private void registerProvider(ConfigSection section) {
        boolean enabled = section.getAs("enabled");
        if (enabled) {
            String type = section.getKey().toLowerCase(Locale.ROOT);
            try {
                if (type.contentEquals("mysql")) {
                    MySQLCredential credential = new MySQLCredential();
                    credential.database(section.getAs("database"));
                    credential.username(section.getAs("username"));
                    credential.hostname(section.getAs("hostname"));
                    credential.password(section.getAs("password"));
                    section.ifValuePresent("port", int.class, credential::port);

                    credential.test();
                    MySQLDatabase build = credential.build();
                    credential.mySQLDatabase(build);

                    providers.put(type, storage -> StorageProviders.MYSQL.provide(storage.getLinker(), credential));
                    return;
                }

                if (type.equalsIgnoreCase("sqlite")) {
                    SQLiteCredential credential = new SQLiteCredential();
                    credential.database(section.getAs("database"));
                    credential.folder(SuperiorPrisonPlugin.getInstance().getDataFolder());

                    credential.test();
                    SQLiteDatabase build = credential.build();
                    credential.sqlLiteDatabase(build);

                    providers.put(type, storage -> StorageProviders.SQLITE.provide(storage.getLinker(), credential));
                    return;
                }

                if (type.equalsIgnoreCase("mongodb")) {
                    MongoCredential credential = new MongoCredential();
                    if (!section.isValuePresent("connection uri")) {
                        credential.database(section.getAs("database"));
                        credential.username(section.getAs("username"));
                        credential.hostname(section.getAs("hostname"));
                        credential.password(section.getAs("password"));
                        section.ifValuePresent("port", int.class, credential::port);
                    } else {
                        credential.connectionUri(section.getAs("connection uri"));
                        credential.database(section.getAs("database"));
                    }

                    credential.test();
                    MongoDatabase build = credential.build();
                    credential.mongoDatabase(build);

                    providers.put(type, storage -> StorageProviders.MONGO_DB.provide(storage.getLinker(), credential.build()));
                }

                if (type.equalsIgnoreCase("flat"))
                    providers.put(type, storage -> StorageProviders.JSON.provide(storage.getLinker(), SuperiorPrisonPlugin.getInstance().getDataFolder()));
            } catch (Throwable throwable) {
                SuperiorPrisonPlugin.getInstance().getOLogger().error(throwable, "Failed to register storage provider by " + type);
            }
        }
    }

    public <T extends UniversalBodyModel, F extends UniversalStorage<T>> Storage<T> provideFor(F storage, String objName) {
        String databaseType = objectsTypes.get(objName);
        if (databaseType == null)
            databaseType = objectsTypes.get("default");

        Function<UniversalStorage, Storage> storageProvider = Objects.requireNonNull(providers.get(databaseType), "Failed to find default storage provider!");
        return storageProvider.apply(storage);
    }

    public ConfigSection initDefaultTypes(ConfigSection section) {
        if (section.isSectionPresent("types")) return null;
        ConfigSection types = section.createSection("types");

        // create sqlite
        ConfigSection sqlite = types.createSection("sqlite");
        sqlite.getComments().addAll(Arrays.asList(
                "The credentials for sqlite",
                "Only database name is required :)"
        ));
        sqlite.set("enabled", false);
        sqlite.set("database", "data");

        ConfigSection mongodb = types.createSection("mongodb");
        mongodb.getComments().addAll(Arrays.asList(
                "The credentials for mongodb",
                "You can pass connection uri",
                "Or fill everything yourself",
                "Required values: username, hostname, password, database",
                "Optional: port"
        ));
        mongodb.set("enabled", false);
        mongodb.set("connection uri", "");
        mongodb.set("database", "admin");

        ConfigSection mysql = types.createSection("mysql");
        mysql.getComments().addAll(Arrays.asList(
                "The credentials for mysql",
                "Required values: username, hostname, password, database",
                "Optional: port"
        ));
        mysql.set("enabled", false);
        mysql.set("hostname", "localhost");
        mysql.set("username", "admin");
        mysql.set("password", "password");
        mysql.set("database", "test");

        ConfigSection flat = types.createSection("flat");
        flat.getComments().addAll(Arrays.asList(
                "The credentials for mysql",
                "Required values: username, hostname, password, database",
                "Optional: port"
        ));
        flat.set("enabled", false);

        return types;
    }
}
