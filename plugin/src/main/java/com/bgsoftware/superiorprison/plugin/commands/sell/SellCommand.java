package com.bgsoftware.superiorprison.plugin.commands.sell;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.hook.impl.VaultHook;
import com.bgsoftware.superiorprison.plugin.menu.SellMenu;
import com.bgsoftware.superiorprison.plugin.object.backpack.BackPackItem;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.inventory.PatchedInventory;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.PlayerArg;
import com.oop.orangeengine.main.util.data.pair.OTriplePair;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
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
            .permission("prison.sell.hand")
            .ableToExecute(Player.class)
            .description("Sell your inventory matched by hand")
            .onCommand(
                command -> {
                  Player player = command.getSenderAsPlayer();
                  ItemStack hand = player.getItemInHand();
                  if (hand == null || hand.getType() == Material.AIR) {
                    LocaleEnum.SELL_HAND_MUST_HOLD_SOMETHING
                        .getWithErrorPrefix()
                        .send(command.getSender());
                    return;
                  }

                  SPrisoner prisoner =
                      SuperiorPrisonPlugin.getInstance()
                          .getPrisonerController()
                          .getInsertIfAbsent(player);

                  // Backpack check
                  if (SuperiorPrisonPlugin.getInstance().getBackPackController().isBackPack(hand)) {
                    SBackPack backPack =
                        (SBackPack)
                            SuperiorPrisonPlugin.getInstance()
                                .getBackPackController()
                                .getBackPack(hand, player);
                    if (!backPack.getData().isSell()) return;

                    handleSell(
                        backPack.getData().getItems().entrySet().stream()
                            .map(
                                pair ->
                                    new OTriplePair<ItemStack, BigInteger, Runnable>(
                                        pair.getKey().getItemStack(),
                                        pair.getValue(),
                                        () -> {
                                          ItemStack itemStack =
                                              pair.getKey().getItemStack().clone();
                                          itemStack.setAmount(pair.getValue().intValue());

                                          backPack.remove(itemStack);
                                        }))
                            .collect(Collectors.toSet()),
                        prisoner);

                    if (backPack.isModified()) {
                      backPack.save();
                      backPack.update();
                    }
                    return;
                  }

                  handleSell(
                      Sets.newHashSet(
                          new OTriplePair<>(
                              hand,
                              BigInteger.valueOf(hand.getAmount()),
                              () -> player.getInventory().remove(hand))),
                      prisoner);
                }));

    subCommand(
        new OCommand()
            .label("inventory")
            .permission("prison.sell.inventory")
            .ableToExecute(Player.class)
            .description("Sell your whole inventory")
            .onCommand(
                command -> {
                  Player player = command.getSenderAsPlayer();
                  SPrisoner prisoner =
                      SuperiorPrisonPlugin.getInstance()
                          .getPrisonerController()
                          .getInsertIfAbsent(player);

                  Set<OTriplePair<ItemStack, BigInteger, Runnable>> toSell = new LinkedHashSet<>();
                  ItemStack[] contents = player.getInventory().getContents();

                  Collection<SBackPack> backpacks =
                      ((PatchedInventory) player.getInventory())
                          .getOwner()
                          .getBackPackMap()
                          .values();

                  backpacks.stream()
                      .filter(backpack -> backpack.getData().isSell())
                      .forEach(
                          backpack ->
                              backpack
                                  .getData()
                                  .getItems()
                                  .forEach(
                                      (item, amount) ->
                                          toSell.add(
                                              new OTriplePair<>(
                                                  item.getItemStack(),
                                                  amount,
                                                  () -> {
                                                    ItemStack itemStack =
                                                        item.getItemStack().clone();
                                                    itemStack.setAmount(amount.intValue());

                                                    backpack.remove(itemStack);
                                                  }))));

                  Map<BackPackItem, BigInteger> items = new HashMap<>();
                  for (ItemStack itemStack : contents) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) continue;

                    items.merge(
                        BackPackItem.wrap(itemStack),
                        BigInteger.valueOf(itemStack.getAmount()),
                        BigInteger::add);
                  }

                  for (Map.Entry<BackPackItem, BigInteger> itemEntry : items.entrySet()) {
                    toSell.add(
                        new OTriplePair<>(
                            itemEntry.getKey().getItemStack(),
                            itemEntry.getValue(),
                            () -> {
                              ItemStack itemStack = itemEntry.getKey().getItemStack();
                              player
                                  .getInventory()
                                  .removeItem(
                                      createItems(itemStack, itemEntry.getValue())
                                          .toArray(new ItemStack[0]));
                            }));
                  }

                  handleSell(toSell, prisoner);

                  backpacks.forEach(
                      backpack -> {
                        if (backpack.isModified()) {
                          backpack.save();
                          backpack.update();
                        }
                      });
                }));

    subCommand(
        new OCommand()
            .label("gui")
            .permission("prison.sell.gui")
            .description("Drop items into a gui to sell them")
            .ableToExecute(Player.class)
            .onCommand(
                command ->
                    new SellMenu(
                            SuperiorPrisonPlugin.getInstance()
                                .getPrisonerController()
                                .getInsertIfAbsent(command.getSenderAsPlayer()))
                        .open()));

    createSubCommandByConsole("inventory");
    createSubCommandByConsole("hand");
    createSubCommandByConsole("gui");

    getSubCommands().values().forEach(PermissionsInitializer::registerPrisonerCommand);
  }

  public void handleSell(
      Set<OTriplePair<ItemStack, BigInteger, Runnable>> items, SPrisoner prisoner) {
    BigDecimal total = new BigDecimal(0);
    for (OTriplePair<ItemStack, BigInteger, Runnable> content : items) {
      if (content.getFirst() == null || content.getFirst().getType() == Material.AIR) continue;

      BigDecimal price = prisoner.getPrice(content.getFirst());
      if (price.doubleValue() == 0) continue;

      total = total.add(price.multiply(new BigDecimal(content.getSecond())));
      content.getThird().run();
    }

    if (total.doubleValue() > 0) {
      messageBuilder(LocaleEnum.SOLD_EVERYTHING.getWithPrefix())
          .replace("{total}", TextUtil.beautifyNumber(total))
          .replace(prisoner)
          .send(prisoner.getPlayer());

      final BigDecimal finalTotal = total;
      SuperiorPrisonPlugin.getInstance()
          .getHookController()
          .executeIfFound(() -> VaultHook.class, hook -> hook.depositPlayer(prisoner, finalTotal));

    } else LocaleEnum.SELL_INVENTORY_WORTHLESS.getWithPrefix().send(prisoner.getPlayer());
  }

  private void createSubCommandByConsole(String subCommand) {
    OCommand consoleSubCommand =
        getSubCommands()
            .computeIfAbsent(
                "console",
                cmd -> new OCommand().label("console").ableToExecute(ConsoleCommandSender.class));
    OCommand oCommand = subCommand(subCommand);

    consoleSubCommand.subCommand(
        new OCommand()
            .label(subCommand)
            .argument(new PlayerArg())
            .onCommand(
                command ->
                    oCommand
                        .getListener()
                        .accept(
                            new WrappedCommand(command.getArgAsReq("player"), new HashMap<>()))));
  }

  private List<ItemStack> createItems(ItemStack item, BigInteger amount) {
    BigInteger NUM_64 = BigInteger.valueOf(64);
    List<ItemStack> converted = new ArrayList<>();

    while (!NumberUtil.equals(amount, BigInteger.ZERO)) {
      ItemStack clone = item.clone();
      if (NumberUtil.isMoreThan(amount, NUM_64)) {
        clone.setAmount(64);
        amount = amount.subtract(NUM_64);
      } else {
          clone.setAmount(amount.intValue());
          amount = BigInteger.ZERO;
      }
      converted.add(clone);
    }

    return converted;
  }
}
