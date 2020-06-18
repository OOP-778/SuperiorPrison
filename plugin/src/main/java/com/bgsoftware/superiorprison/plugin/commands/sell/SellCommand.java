package com.bgsoftware.superiorprison.plugin.commands.sell;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.menu.SellMenu;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.oop.orangeengine.command.OCommand;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class SellCommand extends OCommand {
    public SellCommand() {
        label("sell");
        permission("superiorprison.sell");
        ableToExecute(Player.class);

        PermissionsInitializer.registerPrisonerCommand(this);

        subCommand(
                new OCommand()
                        .label("hand")
                        .permission("superiorprison.sell.hand")
                        .ableToExecute(Player.class)
                        .description("Sell your inventory matched by hand")
                        .onCommand(command -> {
                            Player player = command.getSenderAsPlayer();
                            ItemStack hand = player.getItemInHand();
                            if (hand == null || hand.getType() == Material.AIR) {
                                LocaleEnum.SELL_HAND_MUST_HOLD_SOMETHING.getWithErrorPrefix().send(command.getSender());
                                return;
                            }

                            BigDecimal total = new BigDecimal(0);
                            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
                            for (ItemStack content : player.getInventory().getContents()) {
                                if (content == null || content.getType() == Material.AIR || !content.isSimilar(hand)) continue;

                                BigDecimal price = prisoner.getPrice(content);
                                if (price.doubleValue() == 0) continue;

                                total = total.add(price.multiply(new BigDecimal(content.getAmount())));
                                player.getInventory().remove(content);
                            }

                            if (total.doubleValue() > 0) {
                                messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
                                        .replace("{total}", total.toString())
                                        .replace(prisoner)
                                        .send(command);

                                final BigDecimal finalTotal = total;
                                SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer(prisoner, finalTotal));

                            } else
                                LocaleEnum.SELL_INVENTORY_WORTHLESS.getWithPrefix().send(command.getSender());
                        })
        );

        subCommand(
                new OCommand()
                        .label("inventory")
                        .permission("superiorprison.sell.inventory")
                        .ableToExecute(Player.class)
                        .description("Sell your whole inventory")
                        .onCommand(command -> {
                            Player player = command.getSenderAsPlayer();

                            BigDecimal total = new BigDecimal(0);
                            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);
                            for (ItemStack content : player.getInventory().getContents()) {
                                BigDecimal price = prisoner.getPrice(content);
                                if (price.doubleValue() == 0) continue;

                                total = total.add(price.multiply(new BigDecimal(content.getAmount())));
                                player.getInventory().remove(content);
                            }

                            if (total.doubleValue() > 0) {
                                messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
                                        .replace("{total}", total.toString())
                                        .replace(prisoner)
                                        .send(command);

                                final BigDecimal finalTotal = total;
                                SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer(prisoner, finalTotal));

                            } else
                                LocaleEnum.SELL_INVENTORY_WORTHLESS.getWithPrefix().send(command.getSender());
                        })
        );

        subCommand(
                new OCommand()
                        .label("gui")
                        .permission("superiorprison.sell.gui")
                        .description("Drop items into a gui to sell them")
                        .ableToExecute(Player.class)
                        .onCommand(command -> new SellMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer())).open())
        );

        getSubCommands().values().forEach(PermissionsInitializer::registerPrisonerCommand);
    }
}
