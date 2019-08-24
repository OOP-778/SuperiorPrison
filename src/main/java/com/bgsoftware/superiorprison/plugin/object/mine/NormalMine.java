package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.type.INormalMine;
import com.bgsoftware.superiorprison.api.data.player.IPrisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.oop.orangeengine.database.OColumn;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NormalMine extends DatabaseObject implements INormalMine {

    private Set<IPrisoner> prisoners = ConcurrentHashMap.newKeySet();

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
    private MineGenerator generator;

    public NormalMine() {
        setWhenLoaded(() -> generator.attach(this));
    }

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
    public MineGenerator getGenerator() {
        return generator;
    }

    @Override
    public int getPlayerCount() {
        return prisoners.size();
    }

    @Override
    public Set<IPrisoner> getPrisoners() {
        return prisoners;
    }
}
