package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.flags.FlagEnum;
import com.bgsoftware.superiorprison.api.data.mine.type.INormalMine;
import com.bgsoftware.superiorprison.api.data.player.IPrisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.oop.orangeengine.database.OColumn;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NormalMine extends DatabaseObject implements INormalMine {

    private Set<IPrisoner> prisoners = ConcurrentHashMap.newKeySet();

    private Map<FlagEnum, Boolean> flags = new HashMap<>();

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

    private Cuboid cuboid;

    protected NormalMine() {
        setWhenLoaded(() -> {
            generator.attach(this);

            // Find missing flags and set them to false

        });
    }

    public NormalMine(String name, Location pos1, Location pos2) {
        this.name = name;
        this.minPoint = new SPLocation(pos1);
        this.highPoint = new SPLocation(pos2);

        MineGenerator mineGenerator = new MineGenerator();
        mineGenerator.getGeneratorMaterials().add(new OPair<>(50d, OMaterial.STONE));
        mineGenerator.getGeneratorMaterials().add(new OPair<>(20d, OMaterial.CYAN_TERRACOTTA));
        mineGenerator.getGeneratorMaterials().add(new OPair<>(30d, OMaterial.DIAMOND_ORE));

        mineGenerator.setMine(this);
        StaticTask.getInstance().async(() -> mineGenerator.initCache(() -> {
            mineGenerator.clearMine();
            mineGenerator.generate();
        }));

        // Preset all the flags to false
        for (FlagEnum flagEnum : FlagEnum.values())
            flags.put(flagEnum, false);

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

    @Override
    public boolean isInside(Location location) {
        return (location.getX() > getMinPoint().getX()) &&
                (location.getY() > getMinPoint().getY()) &&
                (location.getZ() > getMinPoint().getZ()) &&
                (location.getX() < getHighPoint().getX()) &&
                (location.getY() < getHighPoint().getY()) &&
                (location.getZ() < getHighPoint().getZ());
    }

    @Override
    public boolean isFlag(FlagEnum flag) {
        return flags.get(flag);
    }
}
