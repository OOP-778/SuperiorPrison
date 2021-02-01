package com.bgsoftware.superiorprison.plugin.commands;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class CmdMine extends OCommand {

  public CmdMine() {
    label("mine");
    description("Teleport to a mine by their rank!");
    ableToExecute(Player.class);
    onCommand(
        command -> {
          SPrisoner prisoner =
              SuperiorPrisonPlugin.getInstance()
                  .getPrisonerController()
                  .getInsertIfAbsent(command.getSenderAsPlayer());
          if (SuperiorPrisonPlugin.getInstance().getMineController().getMines().isEmpty()) {
            LocaleEnum.MINES_NOT_FOUND.getWithErrorPrefix().send(command.getSender());
            return;
          }

          SNormalMine mine = (SNormalMine) prisoner.getHighestMine();
          if (mine == null) {
            LocaleEnum.PRISONER_HIGHEST_MINE_SEARCH_FAILED
                .getWithErrorPrefix()
                .send(command.getSender());
            return;
          }

          if (!mine.getSettings().isTeleportation()) {
            messageBuilder(LocaleEnum.MINE_TELEPORTATION_DISABLED.getWithErrorPrefix())
                .replace(mine)
                .send(command);
            return;
          }

          int prisoners = mine.getPlayerCount();
          if (mine.getSettings().getPlayerLimit() != -1
              && prisoners == mine.getSettings().getPlayerLimit()) {
            messageBuilder(LocaleEnum.MINE_IS_FULL.getWithErrorPrefix())
                .replace(mine)
                .send(command);
            return;
          }

          Framework.FRAMEWORK.teleport(command.getSenderAsPlayer(), mine.getSpawnPoint());
        });

    PermissionsInitializer.registerPrisonerCommand(this);
  }
}
