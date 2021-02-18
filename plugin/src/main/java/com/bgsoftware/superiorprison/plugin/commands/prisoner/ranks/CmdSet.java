package com.bgsoftware.superiorprison.plugin.commands.prisoner.ranks;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.RanksArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.oop.orangeengine.command.OCommand;

public class CmdSet extends OCommand {
  public CmdSet() {
    label("setladder");
    description("Set ladder rank of the prisoner");

    argument(new PrisonerArg(true).setRequired(true));
    argument(new RanksArg(true, false).setRequired(true));

    onCommand(
        command -> {
          SPrisoner prisoner = command.getArgAsReq("prisoner");
          SLadderRank ladderRank = command.getArgAsReq("rank");

          prisoner.setLadderRank(ladderRank, true);
          prisoner.save(true);

          messageBuilder(LocaleEnum.PRISONER_RANK_SET.getWithPrefix())
              .replace(prisoner)
              .replace(ladderRank)
              .send(command);
        });
  }
}
