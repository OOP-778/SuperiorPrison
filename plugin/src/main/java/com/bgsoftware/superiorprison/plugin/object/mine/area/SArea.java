package com.bgsoftware.superiorprison.plugin.object.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import com.oop.datamodule.util.DataUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

@EqualsAndHashCode
public class SArea implements Area, Attachable<SNormalMine>, SerializableObject {

    @Getter
    private transient SNormalMine mine;

    private SPLocation minPoint;
    private SPLocation highPoint;

    @Getter
    private Map<Flag, Boolean> flags = Maps.newConcurrentMap();
    private AreaEnum type;

    private SArea() {}

    public SArea(SPLocation pos1, SPLocation pos2, AreaEnum type) {
        if (pos1.y() > pos2.y()) {
            this.highPoint = pos1;
            this.minPoint = pos2;

        } else {
            this.highPoint = pos2;
            this.minPoint = pos1;
        }
        this.type = type;
    }

    @Override
    public Location getMinPoint() {
        return minPoint.toBukkit();
    }

    @Override
    public Location getHighPoint() {
        return highPoint.toBukkit();
    }

    @Override
    public World getWorld() {
        return minPoint.getWorld();
    }

    public boolean isInside(SPLocation location) {
        if (!getWorld().getName().contentEquals(location.getWorld().getName())) return false;

        int x1 = Math.min(getMinPoint().getBlockX(), getHighPoint().getBlockX());
        int z1 = Math.min(getMinPoint().getBlockZ(), getHighPoint().getBlockZ());
        int x2 = Math.max(getMinPoint().getBlockX(), getHighPoint().getBlockX());
        int z2 = Math.max(getMinPoint().getBlockZ(), getHighPoint().getBlockZ());
        return location.x() >= x1 && location.x() <= x2 && location.z() >= z1 && location.z() <= z2;
    }

    public boolean isInside(Location location) {
        return isInside(new SPLocation(location));
    }

    @Override
    public boolean getFlagState(Flag flag) {
        return flags.get(flag);
    }

    @Override
    public void setFlagState(Flag flag, boolean state) {
        flags.remove(flag);
        flags.put(flag, state);
    }

    @Override
    public AreaEnum getType() {
        return type;
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;

        for (Flag flag : Flag.values())
            if (!flags.containsKey(flag))
                flags.put(flag, flag.getDefaultValue());
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("min", minPoint);
        data.write("high", highPoint);
        data.write("flags", flags);
        data.write("type", type.name());
    }

    @Override
    public void deserialize(SerializedData data) {
        this.minPoint = data.applyAs("min", SPLocation.class);
        this.highPoint = data.applyAs("high", SPLocation.class);
        this.type = AreaEnum.valueOf(data.applyAs("type", String.class));

        data.applyAsMap("flags")
                .forEach(pair -> {
                    JsonElement key = pair.getKey();
                    JsonElement value = pair.getValue();

                    flags.put(Flag.valueOf(key.getAsString()), DataUtil.fromElement(value, boolean.class));
                });
    }
}
