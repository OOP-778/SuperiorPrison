package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;

public class CmdList extends OCommand {
    public CmdList() {
        label("list");
        description("Show a list of boosters");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            OMessage message = LocaleEnum.PRISONER_BOOSTER_LIST.getWithPrefix().clone();

            OPair<MessageLine, LineContent> line1 = message.findLine(line -> line.getText().startsWith("{BOOSTER_TEMPLATE}"));
            if (line1.getFirst() == null) return;

            MessageLine messageLine = line1.getFirst().clone();
            messageLine.removeContentIf(lineContent -> lineContent.getText().contentEquals(line1.getSecond().getText()));

            LineContent lineContent = line1.getSecond().clone();
            lineContent.replace("{BOOSTER_TEMPLATE}", "");

            int count = 0;
            for (Booster booster : prisoner.getBoosters().getBoosters()) {
                LineContent content = lineContent.clone();
                content.replace(
                        ImmutableMap.of("{rate}", booster.getRate(), "{time}", TimeUtil.toString(TimeUtil.getDate(booster.getValidTill())), "{type}", booster instanceof MoneyBooster ? "money" : "drops")
                );
                messageLine.append(content);
                count++;
                if (count != prisoner.getBoosters().getBoosters().size())
                    messageLine.append(", ");
            }

            messageLine.send(command.getSenderAsPlayer(), ImmutableMap.of("{prisoner}", prisoner.getOfflinePlayer().getName()));
        });
    }
}
