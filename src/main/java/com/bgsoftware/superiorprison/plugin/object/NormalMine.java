package com.bgsoftware.superiorprison.plugin.object;

import com.bgsoftware.superiorprison.api.data.IPrisoner;
import com.bgsoftware.superiorprison.api.data.mine.IMineGenerator;
import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.type.INormalMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.oop.orangeengine.database.OColumn;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;

import java.util.Set;

public class NormalMine extends DatabaseObject implements INormalMine {

    @DatabaseValue(columnName = "mineType")
    private MineEnum mineType;

    @DatabaseValue(columnName = "name", columnType = OColumn.VARCHAR)
    private String name;

    @DatabaseValue(columnName = "minPoint")
    private SPLocation minPoint;

    @DatabaseValue(columnName = "highPoint")
    private SPLocation highPoint;

    @DatabaseValue(columnName = "spawnPoint")
    private SPLocation spawnPoint;

    @DatabaseValue(columnName = "generator")
    private IMineGenerator generator;

    @Override
    public MineEnum getType() {
        return mineType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SPLocation getMinPoint() {
        return minPoint;
    }

    @Override
    public SPLocation getHighPoint() {
        return highPoint;
    }

    @Override
    public SPLocation getSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public IMineGenerator getGenerator() {
        return generator;
    }

    @Override
    public int getPlayerCount() {
        return 0;
    }

    @Override
    public Set<IPrisoner> getPrisoners() {
        return null;
    }
}
