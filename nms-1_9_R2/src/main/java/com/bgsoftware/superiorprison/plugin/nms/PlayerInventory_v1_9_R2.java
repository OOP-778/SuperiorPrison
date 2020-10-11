package com.bgsoftware.superiorprison.plugin.nms;

import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.inventory.SPlayerInventory;
import lombok.Getter;
import net.minecraft.server.v1_9_R2.PlayerInventory;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerInventory_v1_9_R2 extends CraftInventoryPlayer implements PatchedInventory {
    @Getter
    private SPlayerInventory owner;

    private AtomicBoolean calling = new AtomicBoolean(false);

    public PlayerInventory_v1_9_R2(PlayerInventory inventory, SPlayerInventory owner) {
        super(inventory);
        this.owner = owner;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        if (!calling.get())
            items = owner.addItem(items);
        else
            calling.set(false);

        if (Arrays.stream(items).noneMatch(Objects::nonNull)) return new HashMap<>();
        return super.addItem(items);
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
        if (!calling.get())
            items = owner.removeItem(items);
        else
            calling.set(false);

        if (Arrays.stream(items).noneMatch(Objects::nonNull)) return new HashMap<>();
        return super.removeItem(items);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (!calling.get())
            item = owner.setItem(index, item);
        else
            calling.set(false);

        super.setItem(index, item);
    }

    @Override
    public void setOwnerCalling() {
        calling.set(true);
    }

    @Override
    public boolean isOwnerCalling() {
        return calling.get();
    }
}
