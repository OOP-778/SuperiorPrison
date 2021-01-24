package com.bgsoftware.superiorprison.plugin.object.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.flags.Flag;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.google.common.collect.Maps;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

@EqualsAndHashCode
public class SArea implements Area, Attachable<SNormalMine>, SerializableObject {

    @Getter
    private final Map<Flag, Boolean> flags = Maps.newConcurrentMap();
    @Getter
    private transient SNormalMine mine;
    private SPLocation minPoint;
    private SPLocation highPoint;
    private AreaEnum type;

    private SArea() {
    }

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

    public SPLocation getMinPointSP() {
        return minPoint;
    }

    public SPLocation getHighPointSP() {
        return highPoint;
    }

    @Override
    public Location getHighPoint() {
        return highPoint.toBukkit();
    }

    @Override
    public World getWorld() {
        return minPoint.getWorld();
    }

    public boolean isInsideWithoutY(SPLocation location) {
        if (!getWorld().getName().contentEquals(location.getWorld().getName())) return false;

        double x1 = Math.min(getMinPoint().getX(), getHighPoint().getX());
        double z1 = Math.min(getMinPoint().getZ(), getHighPoint().getZ());
        double x2 = Math.max(getMinPoint().getX(), getHighPoint().getX());
        double z2 = Math.max(getMinPoint().getZ(), getHighPoint().getZ());
        return location.xBlock() >= x1 && location.xBlock() <= x2 && location.zBlock() >= z1 && location.zBlock() <= z2;
    }

    public boolean isInsideWithY(SPLocation location, boolean yDownwards) {
        double x1 = Math.min(getMinPoint().getX(), getHighPoint().getX());
        double z1 = Math.min(getMinPoint().getZ(), getHighPoint().getZ());
        double x2 = Math.max(getMinPoint().getX(), getHighPoint().getX());
        double z2 = Math.max(getMinPoint().getZ(), getHighPoint().getZ());
        double y1 = Math.min(getMinPoint().getY(), getHighPoint().getY());
        double y2 = Math.max(getMinPoint().getY(), getHighPoint().getY());
        return location.xBlock() >= x1 && location.xBlock() <= x2 && location.zBlock() >= z1 && location.zBlock() <= z2 && (!yDownwards ? location.y() >= y1 && location.y() <= y2 : location.y() >= y1);
    }

    public boolean isInsideWithoutY(Location location) {
        return isInsideWithoutY(new SPLocation(location));
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
                    SerializedData key = pair.getKey();
                    SerializedData value = pair.getValue();

                    try {
                        flags.put(Flag.valueOf(key.applyAs()), value.applyAs());
                    } catch (Exception ex) {
                        SuperiorPrisonPlugin.getInstance().getOLogger().printWarning("Failed to find a flag by {}, ignoring it...", key.applyAs(String.class));
                    }
                });
    }
}
