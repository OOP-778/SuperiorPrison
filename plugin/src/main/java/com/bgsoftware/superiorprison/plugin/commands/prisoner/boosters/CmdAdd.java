package com.bgsoftware.superiorprison.plugin.commands.prisoner.boosters;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.bgsoftware.superiorprison.plugin.commands.args.BoosterTypeArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.commands.args.TimeArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SDropsBooster;
import com.bgsoftware.superiorprison.plugin.object.player.booster.SMoneyBooster;
import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.NumberArg;

import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

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

            messageBuilder(LocaleEnum.PRISONER_BOOSTER_ADD.getWithPrefix())
                    .replace(prisoner, booster)
                    .send(command);

            prisoner.save(true);
        });
    }
}
