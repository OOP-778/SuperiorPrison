package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.google.common.collect.Sets;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class SellMenu extends OMenu {
    public SellMenu(SPrisoner viewer) {
        super("sellGui", viewer);

        ClickHandler
                .of("sell")
                .handle(event -> {
                    //TODO: Implement get items of inventory that aren't buttons
                    Set<ItemStack> itemStacks = Sets.newHashSet();

                    for (ItemStack itemStack : itemStacks) {

                    }
                })
                .apply(this);

    }
}
