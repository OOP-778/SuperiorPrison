package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.MineEnum;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.sign.Sign;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.main.MineDefaultsSection;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.mine.settings.SMineSettings;
import com.bgsoftware.superiorprison.plugin.object.mine.shop.SShop;
import com.bgsoftware.superiorprison.plugin.object.mine.sign.SSign;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.database.DatabaseObject;
import com.oop.orangeengine.database.annotation.Column;
import com.oop.orangeengine.database.annotation.PrimaryKey;
import com.oop.orangeengine.database.annotation.Table;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Table(name = "mines")
public class SNormalMine extends DatabaseObject implements com.bgsoftware.superiorprison.api.data.mine.type.NormalMine, Serializable {

    private Set<Prisoner> prisoners = ConcurrentHashMap.newKeySet();

    @PrimaryKey(name = "name")
    @Setter
    private String name;

    @Column(name = "type")
    private MineEnum type = MineEnum.NORMAL_MINE;

    @Setter
    @Column(name = "spawnPoint")
    private SPLocation spawnPoint = null;

    @Column(name = "generator")
    private SMineGenerator generator;

    @Column(name = "shop")
    private SShop shop;

    @Getter
    @Column(name = "ranks")
    private Set<String> ranks = Sets.newConcurrentHashSet();

    @Getter
    @Column(name = "settings")
    private SMineSettings settings;

    @Setter
    @Column(name = "icon")
    private ItemStack icon;

    @Column(name = "areas")
    @Getter
    private Map<AreaEnum, SArea> areas = Maps.newConcurrentMap();

    @Column(name = "signs")
    private Set<SSign> signs = Sets.newConcurrentHashSet();

    protected SNormalMine() {
        super();
        runWhenLoaded(() -> {
            generator.attach(this);
            settings.attach(this);

            Map<AreaEnum, SArea> newAreas = Maps.newConcurrentMap();
            areas.keySet()
                    .stream()
                    .sorted(Comparator.comparingInt(AreaEnum::getOrder))
                    .sorted(Comparator.reverseOrder())
                    .forEachOrdered(areaEnum -> newAreas.put(areaEnum, areas.get(areaEnum)));

            this.areas = newAreas;
            areas.values().forEach(area -> area.attach(this));
        });
    }

    public SNormalMine(@NonNull String name, @NonNull SPLocation regionPos1, @NonNull SPLocation regionPos2, @NonNull SPLocation minePos1, @NonNull SPLocation minePos2) {
        this.name = name;
        this.areas.put(AreaEnum.MINE, new SArea(minePos1, minePos2, AreaEnum.MINE));
        this.areas.put(AreaEnum.REGION, new SArea(regionPos1.y(0), regionPos2.y(255), AreaEnum.REGION));
        areas.values().forEach(area -> area.attach(this));
        this.shop = new SShop();
        shop.attach(this);

        MineDefaultsSection defaults = SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults();
        this.icon = ItemBuilder.fromItem(defaults.getIcon().getItemStack().clone())
                .replaceDisplayName("{mine_name}", name)
                .getItemStack();

        this.settings = new SMineSettings(defaults);

        generator = new SMineGenerator();
        defaults.getMaterials().forEach(material -> generator.getGeneratorMaterials().add(material));
        generator.setMine(this);
        generator.setMineArea((SArea) getArea(AreaEnum.MINE));
        generator.initBlockChanger();

        defaults.getShopPrices().forEach(item -> shop.addItem(item.getFirst().parseItem(), item.getSecond()));
        StaticTask.getInstance().async(() -> generator.initCache(() -> generator.generate()));
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
    public Area getArea(AreaEnum type) {
        return areas.get(type);
    }

    @Override
    public Area getArea(Location location) {
        Area mine = getArea(AreaEnum.MINE);
        Area region = getArea(AreaEnum.REGION);

        if (mine.isInside(location))
            return mine;

        if (region.isInside(location))
            return region;

        return null;
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
        return areas.get(AreaEnum.REGION).isInside(location);
    }

    @Nullable
    @Override
    public AreaEnum getAreaTypeAt(Location location) {
        if (areas.get(AreaEnum.MINE).isInside(location))
            return AreaEnum.MINE;

        if (areas.get(AreaEnum.REGION).isInside(location))
            return AreaEnum.REGION;

        return null;
    }

    @Override
    public boolean isInsideArea(AreaEnum areaEnum, Location location) {
        return getArea(areaEnum).isInside(location);
    }

    @Override
    public World getWorld() {
        return getArea(AreaEnum.MINE).getHighPoint().getWorld();
    }

    @Override
    public SShop getShop() {
        return shop;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public boolean canEnter(Prisoner prisoner) {
        return prisoner.getPlayer().isOp() || getRanks().stream().anyMatch(name -> prisoner.getRanks().stream().anyMatch(name2 -> name.contentEquals(name)));
    }

    @Override
    @Nullable
    public Sign getSignAt(Location location) {
        return signs
                .stream()
                .filter(sign -> sign.getLocation() == location)
                .findFirst()
                .orElse(null);
    }

    public Set<Sign> getSigns() {
        return new HashSet<>(signs);
    }

    @Override
    public Set<Sign> getSigns(Predicate<Sign> sign) {
        return signs
                .stream()
                .filter(sign)
                .collect(Collectors.toSet());
    }

    @Override
    public void removeSign(Sign sign) {
        signs.remove(sign);
    }

    @Override
    public void removeSign(Location location) {
        signs.removeIf(sign -> sign.getLocation() == location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SNormalMine that = (SNormalMine) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
