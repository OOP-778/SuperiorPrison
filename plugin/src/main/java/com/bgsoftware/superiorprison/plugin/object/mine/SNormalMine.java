package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShop;
import com.oop.orangeengine.database.OColumn;
import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@DatabaseTable(tableName = "mines")
public class SNormalMine extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.mine.type.NormalMine, Serializable {

    private Set<Prisoner> prisoners = ConcurrentHashMap.newKeySet();

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

    @Getter
    @DatabaseValue(columnName = "settings")
    private MineSettings settings;

    @Setter
    @DatabaseValue(columnName = "icon")
    private ItemStack icon;

    protected SNormalMine() {
        super();
        MineDefaultsSection defaults = SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults();

        registerFieldSupplier("options", MineSettings.class, MineSettings::new);
        registerFieldSupplier("icon", ItemStack.class, () -> icon = new OItem(OMaterial.STONE)
                .setDisplayName("&c" + name)
                .replaceDisplayName("{mine_name}", name)
                .appendLore("&cYikes Yupppie!")
                .getItemStack());

        setWhenLoaded(() -> {
            generator.attach(this);
            settings.attach(this);
        });
    }

    public SNormalMine(String name, Location pos1, Location pos2) {
        this.name = name;
        this.minPoint = new SPLocation(pos1);
        this.highPoint = new SPLocation(pos2);
        this.shop = new SShop();
        this.settings = new MineSettings();
        shop.attach(this);
        settings.attach(this);

        MineDefaultsSection defaults = SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults();
        this.icon = ItemBuilder.fromItem(defaults.getIcon().getItemStack().clone())
                .replaceDisplayName("{mine_name}", name)
                .getItemStack();

        generator = new SMineGenerator();
        defaults.getMaterials().forEach(material -> generator.getGeneratorMaterials().add(material));
        generator.setMine(this);
        generator.initBlockChanger();

        defaults.getShopPrices().forEach(item -> shop.addItem(item.getFirst().parseItem(), item.getSecond()));
        StaticTask.getInstance().async(() -> generator.initCache(() -> generator.generate()));

        this.permission = "superiorprison." + name;

        settings.setPlayerLimit(defaults.getLimit());

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

    public void checkForReset() {
    }

}
