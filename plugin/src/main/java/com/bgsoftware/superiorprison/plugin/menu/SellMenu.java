package com.bgsoftware.superiorprison.plugin.menu;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.menu.ButtonClickEvent;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class SellMenu extends OMenu {
  public SellMenu(SPrisoner viewer) {
    super("sell", viewer);

    ClickHandler.of("sell")
        .handle(
            event -> {
              BigDecimal total = new BigDecimal(0);
              for (SPair<Integer, ItemStack> bukkitItem :
                  getBukkitItems(event.getClickedInventory())) {
                BigDecimal price = viewer.getPrice(bukkitItem.getValue());
                if (price.doubleValue() == 0) continue;

                total =
                    total.add(price.multiply(new BigDecimal(bukkitItem.getValue().getAmount())));
                event.getClickedInventory().setItem(bukkitItem.getKey(), null);
              }

              messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
                  .replace("{total}", total)
                  .replace(viewer)
                  .send(event.getWhoClicked());
              final BigDecimal finalTotal = total;
              SuperiorPrisonPlugin.getInstance()
                  .getHookController()
                  .executeIfFound(
                      () -> VaultHook.class, hook -> hook.depositPlayer(viewer, finalTotal));
            })
        .apply(this);
  }

  @Override
  public void handleClick(InventoryClickEvent event) {
    OMenuButton menuButton = getFillerItems().get(event.getSlot());
    if (menuButton == null) return;

    event.setCancelled(true);
    ButtonClickEvent buttonClickEvent = new ButtonClickEvent(event, menuButton);
    Optional<ClickHandler> clickHandler = clickHandlerFor(buttonClickEvent);
    if (!clickHandler.isPresent()) return;

    clickHandler.get().handle(buttonClickEvent);
  }

  @Override
  public void handleBottomClick(InventoryClickEvent event) {}

  @Override
  public void closeInventory(InventoryCloseEvent event) {
    super.closeInventory(event);

    for (SPair<Integer, ItemStack> bukkitItem : getBukkitItems(event.getInventory()))
      event.getView().getPlayer().getInventory().addItem(bukkitItem.getValue());
  }
}
