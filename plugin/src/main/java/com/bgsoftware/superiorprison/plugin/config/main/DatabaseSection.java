package com.bgsoftware.superiorprison.plugin.config.main;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.types.MySqlDatabase;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import com.oop.orangeengine.yaml.ConfigurationSection;
import lombok.Getter;

@Getter
public class DatabaseSection {

    private String type;
    private String database = "data";
    private String hostname = "127.0.0.1";
    private int port = 3306;
    private String username = "root";
    private String password;

    protected DatabaseSection(ConfigurationSection section) {
        this.type = section.getValueAsReq("type");

        section.ifValuePresent("database", String.class, database -> this.database = database);
        section.ifValuePresent("hostname", String.class, hostname -> this.hostname = hostname);
        section.ifValuePresent("port", int.class, port -> this.port = port);
        section.ifValuePresent("username", String.class, username -> this.username = username);
        section.ifValuePresent("password", String.class, password -> this.password = password);
    }

    public boolean isMySql() {
        return type.equalsIgnoreCase("mysql");
    }

    public ODatabase getDatabase() {
        return isMySql() ? new MySqlDatabase(
                new MySqlDatabase.MySqlProperties()
                        .database(database)
                        .password(password)
                        .port(port)
                        .user(username)
                        .url(hostname)
        ) : new SqlLiteDatabase(SuperiorPrisonPlugin.getInstance().getDataFolder(), database);
    }
}
