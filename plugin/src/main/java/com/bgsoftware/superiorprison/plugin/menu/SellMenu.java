package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import org.bukkit.inventory.ItemStack;

public class SellMenu extends OMenu {
    public SellMenu(SPrisoner viewer) {
        super("sellGui", viewer);

        ClickHandler
                .of("sell")
                .handle(event -> {
                    for (SPair<Integer, ItemStack> bukkitItem : getBukkitItems(event.getClickedInventory())) {
                        viewer.getPrice(bukkitItem.getValue());
                    }
                })
                .apply(this);

    }
}
