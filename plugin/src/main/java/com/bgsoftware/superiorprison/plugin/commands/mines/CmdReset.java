package com.bgsoftware.superiorprison.plugin.commands.mines;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SMineGenerator;
import com.oop.orangeengine.command.OCommand;
import java.util.Collection;

public class CmdReset extends OCommand {
  public CmdReset() {
    label("reset");
    description("Reset mines");
    argument(new MinesArg().setRequired(true));
    onCommand(
        command -> {
          Collection<SuperiorMine> mines = command.getArgAsReq("mines");
          for (SuperiorMine mine : mines) {
            if (mine.getGenerator().isResetting()
                || mine.getGenerator().isCaching()
                || ((SMineGenerator) mine.getGenerator()).isWorldLoadWait()) {
              return;
            }

            mine.getGenerator().reset();
          }

          messageBuilder(LocaleEnum.MINE_RESET_SUCCESSFUL.getWithPrefix()).send(command);
        });
  }
}
