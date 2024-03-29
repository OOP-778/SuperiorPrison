package com.bgsoftware.superiorprison.plugin.commands.mines;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.commands.args.MineArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;

public class CmdDelete extends OCommand {
  public CmdDelete() {
    label("delete");
    description("Delete a mine");
    argument(new MineArg().setRequired(true));
    onCommand(
        command -> {
          SNormalMine mine = (SNormalMine) command.getArg("mine").get();
          mine.remove();

          messageBuilder(LocaleEnum.MINE_DELETE_SUCCESSFUL.getWithPrefix())
              .replace(mine)
              .send(command);
        });
  }
}
