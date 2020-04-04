package com.bgsoftware.superiorprison.plugin.commands.prisoners.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.commands.args.BoosterTypeArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.TimeArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SDropsBooster;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SMoneyBooster;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.NumberArg;

import java.util.Optional;

public class CmdAdd extends OCommand {
    public CmdAdd() {
        label("add");
        description("Add a booster");
        argument(new PrisonerArg(true).setRequired(true));
        argument(new BoosterTypeArg().setRequired(true));
        argument(new NumberArg().setRequired(true).setIdentity("rate").setDescription("Boost rate"));
        argument(new TimeArg());
        onCommand(command -> {
            SPrisoner prisoner = command.getArgAsReq("prisoner");
            String type = command.getArgAsReq("type");
            Number rate = command.getArgAsReq("rate");
            Optional<Long> time = command.getArg("time");

            Booster booster = prisoner.getBoosters().addBooster(
                    type.equalsIgnoreCase("drops") ? SDropsBooster.class : SMoneyBooster.class,
                    time.map(aLong -> TimeUtil.getDate().plusSeconds(aLong).toEpochSecond()).orElse(-1L),
                    rate.doubleValue()
            );

            LocaleEnum.PRISONER_ADD_BOOSTER.getWithPrefix().send(
                    command.getSenderAsPlayer(),
                    ImmutableMap.of(
                            "{type}", type.toLowerCase(),
                            "{rate}", rate.toString(),
                            "{time}", TimeUtil.toString(TimeUtil.getDate(booster.getValidTill())),
                            "{prisoner}", prisoner.getOfflinePlayer().getName()
                    )
            );
        });
    }
}
