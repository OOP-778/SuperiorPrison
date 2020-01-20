package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;

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
    Get min point of mine area
    */
    SPLocation getMinPoint();

    /*
    Get min point of mine area
    */
    SPLocation getHighPoint();

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
    Get permission of the mine
    */
    Optional<String> getPermission();

    /*
    Get icon of the mine
    */
    ItemStack getIcon();


}
