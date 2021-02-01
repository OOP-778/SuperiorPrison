package com.bgsoftware.superiorprison.plugin.menu.reward;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.mergeText;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.reward.SMineReward;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.chatcmds.ChatCommands;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

public class MineRewardsMenu extends OPagedMenu<SMineReward> implements OMenu.Templateable {
  private SNormalMine mine;

  public MineRewardsMenu(SPrisoner viewer, SNormalMine mine) {
    super("mineRewards", viewer);
    this.mine = mine;

    clickHandler("reward")
        .handle(
            event -> {
              if (event.getClick().isLeftClick()) {
                editReward(requestObject(event.getRawSlot()));
              } else {
                mine.getRewards().removeReward(requestObject(event.getRawSlot()));
                refresh();
              }
            });

    clickHandler("create")
        .handle(
            event -> {
              mine.getRewards().addReward(new SMineReward(0.5, new ArrayList<>()));
              refresh();
            });
  }

  private void editReward(SMineReward reward) {
    // Force close menus
    forceClose();

    // Handle with chat commands
    ChatCommands chatCommands = new ChatCommands(getViewer().getPlayer());

    // Send message runnable
    Runnable sendMessage =
        () ->
            messageBuilder(LocaleEnum.EDIT_REWARD.getMessage())
                .replace(reward)
                .send(getViewer().getPlayer());

    // Add command chat cmd
    chatCommands.appendCommand(
        "addCmd",
        (player, args) -> {
          String text = mergeText(args);
          reward.getCommands().add(text);
        });

    // Add chat command for removing cmds
    chatCommands.appendCommand(
        "remCmd",
        (player, args) -> {
          int index = Integer.parseInt(args[0]);
          reward.getCommands().remove(index);

          messageBuilder(LocaleEnum.EDIT_REWARD_REMOVED_COMMAND.getWithPrefix())
              .replace("{index}", index)
              .send(player);
        });

    // Add chat command for setting chance
    chatCommands.appendCommand(
        "setChance",
        (player, args) -> {
          double chance = Double.parseDouble(args[0]);
          if (chance > 1) chance = chance / 10;
          reward.setChance(chance);

          messageBuilder(LocaleEnum.EDIT_REWARD_CHANCE.getWithPrefix())
              .replace("{chance}", chance)
              .send(player);
        });

    chatCommands.appendCommand(
        "done",
        (player, canceller, args) -> {
          refresh();
          canceller.set(true);
        });

    chatCommands.listen();
    chatCommands.onFinish(this::refresh);
    chatCommands.afterInput(sendMessage);
    sendMessage.run();
  }

  @Override
  public List<SMineReward> requestObjects() {
    return mine.getRewards().getRewards().stream()
        .map(reward -> (SMineReward) reward)
        .collect(Collectors.toList());
  }

  @Override
  public OMenuButton toButton(SMineReward obj) {
    Optional<OMenuButton> rewardTemplateOpt = getTemplateButtonFromTemplate("reward");
    if (!rewardTemplateOpt.isPresent()) return null;

    OMenuButton buttonTemplate = rewardTemplateOpt.get().clone();
    OMenuButton.ButtonItemBuilder defaultStateItem = buttonTemplate.getDefaultStateItem();

    List<String> newLore = new ArrayList<>();

    for (Object o : defaultStateItem.itemBuilder().getLore()) {
      String line = o.toString();

      // If the line is template
      if (ChatColor.stripColor(line).startsWith("{template}")) {
        line = line.replace("{template}", "");

        for (String command : obj.getCommands())
          newLore.add(line.replace("{reward_command}", command));
      } else newLore.add(line);
    }

    defaultStateItem.itemBuilder().setLore(newLore);
    defaultStateItem.itemBuilder().replace("{reward_index}", requestObjects().indexOf(obj) + 1);
    return buttonTemplate.currentItem(defaultStateItem.getItemStackWithPlaceholders(obj));
  }

  @Override
  public Object[] getBuildPlaceholders() {
    return new Object[] {mine};
  }
}
