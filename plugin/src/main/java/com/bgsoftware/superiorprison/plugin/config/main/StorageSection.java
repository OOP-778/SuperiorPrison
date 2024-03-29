package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.datamodule.api.storage.Storage;
import com.oop.datamodule.commonsql.database.SQLDatabase;
import com.oop.datamodule.commonsql.database.SqlCredential;
import com.oop.datamodule.h2.H2Credential;
import com.oop.datamodule.h2.H2Database;
import com.oop.datamodule.mongodb.MongoCredential;
import com.oop.datamodule.mysql.MySQLCredential;
import com.oop.datamodule.mysql.MySQLDatabase;
import com.oop.datamodule.postgresql.PostgreDatabase;
import com.oop.datamodule.postgresql.PostgreSQLCredential;
import com.oop.datamodule.sqlite.SQLiteCredential;
import com.oop.datamodule.sqlite.SQLiteDatabase;
import com.oop.datamodule.universal.StorageProviders;
import com.oop.datamodule.universal.UniversalStorage;
import com.oop.datamodule.universal.model.UniversalBodyModel;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class StorageSection {
  private Function<
          UniversalStorage<? extends UniversalBodyModel>, Storage<? extends UniversalBodyModel>>
      storageProvider;

  public StorageSection(Config config) {
    ConfigSection storageSection = config.createSection("storage");
    if (storageSection.getComments().isEmpty()) storageSection.getComments().add("Storage options");

    ConfigSection credentialsSection = storageSection.createSection("credentials");
    if (credentialsSection.getComments().isEmpty())
      credentialsSection
          .getComments()
          .addAll(
              Arrays.asList(
                  "The values for sqlite or h2 is: database aka the name of the file",
                  "The values for mongodb is",
                  "Either connection uri & database",
                  "Or host, database, username, password",
                  "For mysql & postgresql the values are: host, database, username, password",
                  "For json it's path"));

    // Migrate old db
    config.ifSectionPresent(
        "database",
        databaseSection -> {
          String type = databaseSection.getAs("type", String.class);
          if (type.equalsIgnoreCase("sqlite")) {
            storageSection.set("type", "sqlite");
            credentialsSection.set("database", databaseSection.getAs("database", String.class));

          } else if (type.equalsIgnoreCase("mysql")) {
            storageSection.set("type", "mysql");
            credentialsSection.set("host", databaseSection.getAs("hostname", String.class));
            credentialsSection.set("port", databaseSection.getAs("port", int.class));
            credentialsSection.set("database", databaseSection.getAs("database", String.class));
            credentialsSection.set("username", databaseSection.getAs("username", String.class));
            credentialsSection.set("password", databaseSection.getAs("password", String.class));

          } else if (type.equalsIgnoreCase("flat")) {
            storageSection.set("type", "json");
            credentialsSection.set("path", "/");
          }

          config.getSections().remove(databaseSection.getKey());
        });

    // Initialize db
    String type = storageSection.getAs("type");

    // For mysql & postgresql
    if (type.equalsIgnoreCase("mysql") || type.equalsIgnoreCase("postgresql")) {
      String host = credentialsSection.getAs("host");
      String database = credentialsSection.getAs("database", String.class);
      String username = credentialsSection.getAs("username", String.class);
      String password = credentialsSection.getAs("password", String.class);

      SQLDatabase sqlDatabase;
      SqlCredential credential;
      if (type.equalsIgnoreCase("mysql")) {
        credential =
            new MySQLCredential()
                .database(database)
                .hostname(host)
                .username(username)
                .password(password);
        credentialsSection.ifValuePresent("port", int.class, ((MySQLCredential) credential)::port);

      } else {
        credential =
            new PostgreSQLCredential()
                .database(database)
                .hostname(host)
                .username(username)
                .password(password);
        credentialsSection.ifValuePresent(
            "port", int.class, ((PostgreSQLCredential) credential)::port);
      }

      sqlDatabase = credential.build();

      SuperiorPrisonPlugin.getInstance()
          .getOLogger()
          .printWarning("Testing database {} connection...", type);
      try {
        sqlDatabase.getConnection().use(conn -> {});
        SuperiorPrisonPlugin.getInstance()
            .getOLogger()
            .print("Database connection test successful!");
      } catch (Throwable throwable) {
        throw new IllegalStateException("Database connection test failed...", throwable);
      }

      SQLDatabase finalSqlDatabase = sqlDatabase;
      storageProvider =
          (storage) -> {
            if (credential instanceof MySQLCredential)
              return StorageProviders.MYSQL.provide(
                  storage.getLinker(), (MySQLDatabase) finalSqlDatabase);
            else
              return StorageProviders.POSTGRE.provide(
                  storage.getLinker(), (PostgreDatabase) finalSqlDatabase);
          };
    }

    // For sqlite
    if (type.equalsIgnoreCase("sqlite")) {
      String database = credentialsSection.getAs("database", String.class);
      SQLiteCredential credential =
          new SQLiteCredential()
              .database(database)
              .folder(SuperiorPrisonPlugin.getInstance().getDataFolder());

      SQLiteDatabase sqLiteDatabase = credential.build();
      storageProvider =
          (storage) -> StorageProviders.SQLITE.provide(storage.getLinker(), sqLiteDatabase);
    }

    // For h2
    if (type.equalsIgnoreCase("h2")) {
      String database = credentialsSection.getAs("database", String.class);
      H2Credential credential =
          new H2Credential()
              .database(database)
              .folder(SuperiorPrisonPlugin.getInstance().getDataFolder());

      H2Database h2Database = credential.build();
      storageProvider =
          (storage) -> StorageProviders.H2.provide(storage.getLinker(), h2Database);
    }

    // Json
    if (type.equalsIgnoreCase("json")) {
      String path = credentialsSection.getAs("path", String.class);
      storageProvider =
          (storage) -> {
            return StorageProviders.JSON.provide(
                storage.getLinker(),
                new File(SuperiorPrisonPlugin.getInstance().getDataFolder() + path));
          };
    }

    // For mongodb
    if (type.equalsIgnoreCase("mongodb")) {
      MongoCredential credential;
      if (credentialsSection.isValuePresent("connection uri")) {
        String database = credentialsSection.getAs("database", String.class);
        credential =
            new MongoCredential()
                .connectionUri(credentialsSection.getAs("connection uri"))
                .database(database);

      } else {
        String host = credentialsSection.getAs("host");
        String database = credentialsSection.getAs("database", String.class);
        String username = credentialsSection.getAs("username", String.class);
        String password = credentialsSection.getAs("password", String.class);
        credential =
            new MongoCredential()
                .database(database)
                .password(password)
                .username(username)
                .hostname(host);

        credentialsSection.ifValuePresent("port", int.class, credential::port);
      }

      SuperiorPrisonPlugin.getInstance()
          .getOLogger()
          .printWarning("Testing database {} connection...", type);
      try {
        credential.test();
        SuperiorPrisonPlugin.getInstance()
            .getOLogger()
            .print("Database connection test successful!");
      } catch (Throwable throwable) {
        throw new IllegalStateException("Database connection test failed...", throwable);
      }

      storageProvider =
          (storage) -> StorageProviders.MONGO_DB.provide(storage.getLinker(), credential);
    }
  }
}
