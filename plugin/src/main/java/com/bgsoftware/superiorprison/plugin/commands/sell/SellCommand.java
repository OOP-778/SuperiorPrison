package com.bgsoftware.superiorprison.plugin.commands.sell;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.menu.SellMenu;
import com.bgsoftware.superiorprison.plugin.object.backpack.OldSBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.PlayerArg;
import com.oop.orangeengine.main.util.data.pair.OPair;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

                            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

                            // Backpack check
                            if (SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(hand)) {
                                OldSBackPack backPack = (OldSBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(hand, player);
                                if (!backPack.getData().isSell()) return;

                                handleSell(backPack.getStored().stream().map(item -> new OPair<ItemStack, Runnable>(item, () -> backPack.remove(item))).collect(Collectors.toSet()), prisoner);
                                if (backPack.isModified()) {
                                    backPack.save();
                                    backPack.update();
                                }
                                return;
                            }

                            handleSell(Sets.newHashSet(new OPair<>(hand, () -> player.getInventory().remove(hand))), prisoner);
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
                            SPrisoner prisoner = SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(player);

                            Set<OPair<ItemStack, Runnable>> items = new HashSet<>();
                            ItemStack[] contents = player.getInventory().getContents();

                            List<BackPack> backpacks = new ArrayList<>();

                            for (int i = 0; i < contents.length; i++) {
                                ItemStack itemStack = contents[i];
                                if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                                // Backpack check
                                if (SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(itemStack)) {
                                    OldSBackPack backPack = (OldSBackPack) SuperiorPrisonPlugin.getInstance().getBackPackController().getBackPack(itemStack, player);
                                    if (!backPack.getData().isSell()) continue;
                                    items.addAll(backPack.getStored().stream().map(item -> new OPair<ItemStack, Runnable>(item, () -> backPack.remove(item))).collect(Collectors.toSet()));
                                    backpacks.add(backPack);
                                }

                                int finalI = i;
                                items.add(new OPair<>(itemStack, () -> player.getInventory().setItem(finalI, null)));
                            }
                            handleSell(
                                    items,
                                    prisoner
                            );

                            backpacks.forEach(backpack -> {
                                backpack.save();
                                backpack.update();
                            });
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

        createSubCommandByConsole("inventory");
        createSubCommandByConsole("hand");
        createSubCommandByConsole("gui");

        getSubCommands().values().forEach(PermissionsInitializer::registerPrisonerCommand);
    }

    public void handleSell(Set<OPair<ItemStack, Runnable>> items, SPrisoner prisoner) {
        BigDecimal total = new BigDecimal(0);
        for (OPair<ItemStack, Runnable> content : items) {
            if (content.getFirst() == null || content.getFirst().getType() == Material.AIR) continue;

            BigDecimal price = prisoner.getPrice(content.getFirst());
            if (price.doubleValue() == 0) continue;

            total = total.add(price.multiply(new BigDecimal(content.getFirst().getAmount())));
            content.getSecond().run();
        }

        if (total.doubleValue() > 0) {
            messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
                    .replace("{total}", total.toString())
                    .replace(prisoner)
                    .send(prisoner.getPlayer());

            final BigDecimal finalTotal = total;
            SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer(prisoner, finalTotal));

        } else
            LocaleEnum.SELL_INVENTORY_WORTHLESS.getWithPrefix().send(prisoner.getPlayer());
    }

    private void createSubCommandByConsole(String subCommand) {
        OCommand consoleSubCommand = getSubCommands().computeIfAbsent("console", cmd -> new OCommand().label("console").ableToExecute(ConsoleCommandSender.class));
        OCommand oCommand = subCommand(subCommand);

        consoleSubCommand
                .subCommand(
                        new OCommand()
                                .label(subCommand)
                                .argument(new PlayerArg())
                                .onCommand(command -> oCommand.getListener().accept(new WrappedCommand(command.getArgAsReq("player"), new HashMap<>())))
                );
    }
}
