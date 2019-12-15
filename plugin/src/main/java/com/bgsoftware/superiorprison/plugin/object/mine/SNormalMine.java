package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.flags.FlagEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShop;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShopItem;
import com.oop.orangeengine.database.OColumn;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@DatabaseTable(tableName = "mines")
public class SNormalMine extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.mine.type.NormalMine, Serializable {

    private Set<Prisoner> prisoners = ConcurrentHashMap.newKeySet();
    private Map<FlagEnum, Boolean> flags = new HashMap<>();

    @DatabaseValue(columnName = "mineType")
    private MineEnum type = MineEnum.NORMAL_MINE;

    @DatabaseValue(columnName = "name", columnType = OColumn.VARCHAR)
    @Setter
    private String name;

    @Setter
    @DatabaseValue(columnName = "minPoint")
    private SPLocation minPoint;

    @Setter
    @DatabaseValue(columnName = "highPoint")
    private SPLocation highPoint;

    @Setter
    @DatabaseValue(columnName = "spawnPoint")
    private SPLocation spawnPoint = null;

    @DatabaseValue(columnName = "generator")
    private SMineGenerator generator;

    @DatabaseValue(columnName = "shop")
    private SShop shop;

    @Setter
    @DatabaseValue(columnName = "permission")
    private String permission;

    @Setter
    @DatabaseValue(columnName = "icon")
    private ItemStack icon;

    protected SNormalMine() {
        setWhenLoaded(() -> {
            generator.attach(this);

            // Find missing flags and set them to default value
            for (FlagEnum flagEnum : FlagEnum.values())
                if (!flags.containsKey(flagEnum))
                    flags.put(flagEnum, flagEnum.getDefaultValue());

        });
    }

    public SNormalMine(String name, Location pos1, Location pos2) {
        this.name = name;
        this.minPoint = new SPLocation(pos1);
        this.highPoint = new SPLocation(pos2);
        this.shop = new SShop();
        shop.attach(this);

        generator = new SMineGenerator();
        generator.getGeneratorMaterials().add(new OPair<>(50d, OMaterial.STONE));
        generator.getGeneratorMaterials().add(new OPair<>(20d, OMaterial.CYAN_TERRACOTTA));
        generator.getGeneratorMaterials().add(new OPair<>(30d, OMaterial.DIAMOND_ORE));
        generator.setMine(this);
        generator.initBlockChanger();

        shop.getItems().add(new SShopItem(new OItem(Material.REDSTONE).getItemStack(), 20));

        StaticTask.getInstance().async(() -> generator.initCache(() -> generator.generate()));

        this.permission = "superiorprison." + name;

        // Preset all the flags to default values
        for (FlagEnum flagEnum : FlagEnum.values())
            flags.put(flagEnum, flagEnum.getDefaultValue());

        // TODO make configurable
        this.icon = new OItem(OMaterial.IRON_BARS)
                .setDisplayName("&e" + name)
                .getItemStack();
    }

    @Override
    public MineEnum getType() {
        return type;
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
    public Optional<SPLocation> getSpawnPoint() {
        return Optional.ofNullable(spawnPoint);
    }

    @Override
    public SMineGenerator getGenerator() {
        return generator;
    }

    @Override
    public int getPlayerCount() {
        return prisoners.size();
    }

    @Override
    public Set<Prisoner> getPrisoners() {
        return prisoners;
    }

    @Override
    public boolean isInside(Location location) {
        return location.getWorld().getName().contentEquals(getMinPoint().worldName()) &&
                (location.getBlockX() > getMinPoint().x()) &&
                (location.getBlockZ() > getMinPoint().z()) &&
                (location.getBlockX() < getHighPoint().x()) &&
                (location.getBlockZ() < getHighPoint().z());
    }

    @Override
    public boolean isFlagEnabled(FlagEnum flag) {
        return flags.get(flag);
    }

    @Override
    public SShop getShop() {
        return shop;
    }

    @Override
    public Optional<String> getPermission() {
        return Optional.ofNullable(permission);
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    public void preDelete() {
        // TO DO
    }
}
