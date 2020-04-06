package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.api.data.player.booster.MoneyBooster;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.CommandHelper;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SBooster;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.listedBuilder;
import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdList extends OCommand {
    public CmdList() {
        label("list");
        description("Show a list of boosters");
        argument(new PrisonerArg(true).setRequired(true));
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");

            listedBuilder(Booster.class)
                    .message(LocaleEnum.PRISONER_BOOSTER_LIST.getWithPrefix())
                    .identifier("{TEMPLATE}")
                    .addObject(prisoner.getBoosters().set().toArray(new Booster[0]))
                    .addPlaceholderObject(prisoner)
                    .send(command);
        });
    }
}
