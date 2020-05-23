package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class SellMenu extends OMenu {
    public SellMenu(SPrisoner viewer) {
        super("sell", viewer);

        ClickHandler
                .of("sell")
                .handle(event -> {
                    BigDecimal total = new BigDecimal(0);
                    for (SPair<Integer, ItemStack> bukkitItem : getBukkitItems(event.getClickedInventory())) {
                        BigDecimal price = viewer.getPrice(bukkitItem.getValue());
                        if (price.doubleValue() == 0) continue;

                        total = total.add(price);
                        event.getClickedInventory().setItem(bukkitItem.getKey(), new ItemStack(Material.AIR));
                    }

                    messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
                            .replace("{total}", total)
                            .replace(viewer)
                            .send(event.getWhoClicked());
                    final BigDecimal finalTotal = total;
                    SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer(viewer, finalTotal));
                })
                .apply(this);

    }
}
