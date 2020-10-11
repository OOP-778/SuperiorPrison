package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.locks.Lock;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import com.bgsoftware.superiorprison.api.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@Getter
public class MultiBlockBreakEvent extends MineEvent implements Cancellable {
    private Map<Location, Pair<Material, List<ItemStack>>> blockData;
    private Prisoner prisoner;
    private ItemStack tool;
    private Lock lock;

    @Setter
    private int experience;

    @Setter
    private boolean cancelled;

    public MultiBlockBreakEvent(SuperiorMine mine, Prisoner prisoner, ItemStack tool, Map<Location, Pair<Material, List<ItemStack>>> blockData, Lock lock, int experience) {
        super(mine);
        this.prisoner = prisoner;
        this.tool = tool;
        this.blockData = blockData;
        this.experience = experience;
        this.lock = lock;
    }
}
