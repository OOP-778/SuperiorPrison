package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings;
import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.mine.sign.Sign;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface SuperiorMine {

    /*
    Get Mine Type
    */
    MineEnum getType();

    /*
    Get mine name
    */
    String getName();

    /*
    Get different areas of the mine region or mine
    */
    Area getArea(AreaEnum type);

    Area getArea(Location location);

    /*
    Get spawn point of mine
    Can return null inside Optional
    */
    Optional<SPLocation> getSpawnPoint();

    /*
    Get generator of mine
    */
    MineGenerator getGenerator();

    /*
    Get how many players are in the mine
    */
    int getPlayerCount();

    /*
    Get all prisoners inside the mine
    */
    Set<Prisoner> getPrisoners();

    /*
    Check if specific location is inside mine area
    */
    boolean isInside(Location location);

    /*
    Get mine shop
    */
    MineShop getShop();

    /*
    Get rank names that can access the mine!
    */
    Set<String> getRanks();

    /*
    Get icon of the mine
    */
    ItemStack getIcon();

    /*
    Get settings of the mine
    */
    MineSettings getSettings();

    /*
    Get area type at an location
    */
    @Nullable
    AreaEnum getAreaTypeAt(Location location);

    /*
    Get a boolean if the location is inside the area
    */
    boolean isInsideArea(AreaEnum areaEnum, Location location);

    /*
    Get world of the mine
    */
    World getWorld();

    boolean canEnter(Prisoner prisoner);

    void save(boolean async);

    @Nullable
    Sign getSignAt(Location location);

    Set<Sign> getSigns();

    Set<Sign> getSigns(Predicate<Sign> sign);

    void removeSign(Location location);

    void removeSign(Sign sign);
}
