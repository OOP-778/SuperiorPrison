package com.bgsoftware.superiorprison.plugin.commands.prisoner;

import com.bgsoftware.superiorprison.plugin.commands.args.FeatureArg;
import com.bgsoftware.superiorprison.plugin.commands.args.PrisonerArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.BoolArg;

import java.util.function.Supplier;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdToggleFeature extends OCommand {
  public CmdToggleFeature() {
    label("toggleFeature");
    description("Toggle an prisoner feature");
    argument(new PrisonerArg(true).setRequired(true));
    argument(new FeatureArg().setRequired(true));
    argument(new BoolArg().setIdentity("silent"));

    onCommand(
        command -> {
          SPrisoner prisoner = command.getArgAsReq("prisoner");
          String feature = command.getArgAsReq("feature");

          boolean silent = command.getArg("silent", boolean.class).orElse(false);

          Supplier<Boolean> currentState =
              () -> {
                if (feature.equalsIgnoreCase("autosell")) return prisoner.isAutoSell();

                if (feature.equalsIgnoreCase("autopickup")) return prisoner.isAutoPickup();

                if (feature.equalsIgnoreCase("autoburn")) return prisoner.isAutoBurn();

                if (feature.equalsIgnoreCase("fortuneblocks")) return prisoner.isFortuneBlocks();

                return null;
              };

          Runnable switchState =
              () -> {
                if (feature.equalsIgnoreCase("autosell"))
                  prisoner.setAutoSell(!prisoner.isAutoSell());
                else if (feature.equalsIgnoreCase("autopickup"))
                  prisoner.setAutoPickup(!prisoner.isAutoPickup());
                else if (feature.equalsIgnoreCase("autoburn"))
                  prisoner.setAutoBurn(!prisoner.isAutoBurn());
                else if (feature.equalsIgnoreCase("fortuneblocks"))
                  prisoner.setFortuneBlocks(!prisoner.isFortuneBlocks());
              };

          boolean oldState = currentState.get();
          switchState.run();

          if (!silent) {
            messageBuilder(LocaleEnum.TOGGLED_PRISONER_FEATURE.getWithPrefix())
                .replace("{state}", oldState ? "disabled" : "enabled")
                    .replace("{feature}", feature)
                    .replace("{prisoner}", prisoner.getOfflinePlayer().getName())
                    .send(command.getSender());
          }
        });
  }
}
