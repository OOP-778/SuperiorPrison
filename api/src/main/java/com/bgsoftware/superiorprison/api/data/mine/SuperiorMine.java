package com.bgsoftware.superiorprison.api.data.mine;

import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffect;
import com.bgsoftware.superiorprison.api.data.mine.effects.MineEffects;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineMessage;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineMesssages;
import com.bgsoftware.superiorprison.api.data.mine.settings.MineSettings;
import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.Rank;
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
    Location getSpawnPoint();

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
    Get all the ranks that can access the mine mapped
    */
    Set<Rank> getRanksMapped();

    /*
    Get all the prestiges that can access the mine!
    */
    Set<String> getPrestiges();

    /*
    Get all the prestiges that can access the mine mapped
    */
    Set<Prestige> getPrestigesMapped();

    /*
    Get icon of the mine
    */
    ItemStack getIcon();

    /*
    Get settings of the mine
    */
    MineSettings getSettings();

    // Get messages of the mine
    MineMesssages getMessages();

    /*
    Get Mine Effects
    */
    MineEffects getEffects();

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

    /*
    Check if prisoner can enter the mine
    Checks for ranks & prestiges & admin permissions
    */
    boolean canEnter(Prisoner prisoner);

    // Save the mine
    void save(boolean async);

    // Remove an rank from the mine
    void removeRank(String... rank);

    // Remove an rank from the mine
    void removeRank(Rank... rank);

    // Remove an prestige from the mine
    void removePrestige(String... prestige);

    // Remove an prestige from the mine
    void removePrestige(Prestige... prestige);

    // Add an rank to the mine
    void addRank(String... rank);

    // Add an rank to the mine
    void addRank(Rank... rank);

    // Add an prestige to the mine
    void addPrestige(String... prestige);

    // Add an prestige to the mine
    void addPrestige(Prestige... prestige);
}
