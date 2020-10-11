package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.inventory.SPlayerInventory;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryUpdateTask extends OTask {

    @Setter
    private long updateEvery = 1000;

    public PlayerInventoryUpdateTask() {
        delay(150);
        repeat(true);
        sync(false);
        runnable(() -> {
            for (Player onlinePlayer : Helper.getOnlinePlayers()) {
                if (!(onlinePlayer.getInventory() instanceof PatchedInventory)) continue;

                SPlayerInventory patchedInventory = ((PatchedInventory) onlinePlayer.getInventory()).getOwner();
                patchedInventory.getBackPackMap().forEach((slot, backPack) -> {
                    long lastUpdated = backPack.getLastUpdated();

                    // If last updated is not set, set it to now
                    if (lastUpdated == -1) {
                        backPack.setLastUpdated(System.currentTimeMillis());
                        return;
                    }

                    long currentMillis = System.currentTimeMillis();
                    long difference = currentMillis - lastUpdated;

                    if (difference >= updateEvery) {
                        if (!backPack.isModified()) return;
                        if (!patchedInventory.getBackPackMap().containsKey(slot)) return;

                        backPack.save();
                        StaticTask.getInstance().sync(() -> {
                            if (!onlinePlayer.isOnline()) return;
                            if (!patchedInventory.getBackPackMap().containsKey(slot)) return;

                            ItemStack itemStack = backPack.updateManually();
                            onlinePlayer.getInventory().setItem(slot, itemStack);
                        });
                        backPack.setLastUpdated(currentMillis);
                    }
                });
            }
        });
    }
}
