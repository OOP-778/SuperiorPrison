package com.bgsoftware.superiorprison.plugin.config;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.util.configwrapper.SectionWrapper;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.datamodule.database.types.MySqlDatabase;
import com.oop.datamodule.database.types.SqlLiteDatabase;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public class DatabaseSection extends SectionWrapper {

    private String type;
    private String database = "data";
    private String hostname = "127.0.0.1";
    private int port = 3306;
    private String username = "root";
    private String password;

    public boolean isMySql() {
        return type.equalsIgnoreCase("mysql");
    }

    public DatabaseWrapper getDatabase() {
        return isMySql() ? new MySqlDatabase(
                new MySqlDatabase.MySqlProperties()
                        .database(database)
                        .password(password)
                        .port(port)
                        .user(username)
                        .url(hostname)
        ) : new SqlLiteDatabase(SuperiorPrisonPlugin.getInstance().getDataFolder(), database);
    }

    @Override
    protected void initialize() {
        ConfigSection section = getSection();
        this.type = section.getAs("type");

        section.ifValuePresent("database", String.class, database -> this.database = database);
        section.ifValuePresent("hostname", String.class, hostname -> this.hostname = hostname);
        section.ifValuePresent("port", int.class, port -> this.port = port);
        section.ifValuePresent("username", String.class, username -> this.username = username);
        section.ifValuePresent("password", String.class, password -> this.password = password);
    }
}
