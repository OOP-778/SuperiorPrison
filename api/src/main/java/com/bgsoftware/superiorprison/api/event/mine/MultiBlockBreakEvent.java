package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MultiBlockBreakEvent extends MineEvent {
    private Map<Location, Pair<Material, List<ItemStack>>> blockData = new HashMap<>();
    private Prisoner prisoner;
    private ItemStack tool;

    public MultiBlockBreakEvent(SuperiorMine mine, Prisoner prisoner, ItemStack tool, Map<Location, Pair<Material, List<ItemStack>>> blockData) {
        super(mine);
        this.prisoner = prisoner;
        this.tool = tool;
    }
}
