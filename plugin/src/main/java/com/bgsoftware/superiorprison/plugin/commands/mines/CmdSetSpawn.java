package com.bgsoftware.superiorprison.plugin.commands.mines;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import org.bukkit.entity.Player;

public class CmdSetSpawn extends OCommand {
  public CmdSetSpawn() {
    label("setspawn");
    description("Set spawn location of a mine");
    argument(new MinesArg().setRequired(true));
    ableToExecute(Player.class);

    onCommand(
        command -> {
          SNormalMine mine = command.getArgAsReq("mine");

          mine.setSpawnPointOf(command.getSenderAsPlayer());
          mine.save(true);

          messageBuilder(LocaleEnum.MINE_SET_SPAWN_POS.getWithPrefix()).replace(mine).send(command);
        });
  }
}
