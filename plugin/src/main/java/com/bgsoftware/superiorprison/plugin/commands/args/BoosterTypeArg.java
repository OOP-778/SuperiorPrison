package com.bgsoftware.superiorprison.plugin.commands.args;

import com.google.common.collect.Lists;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class BoosterTypeArg extends CommandArgument<String> {
  public BoosterTypeArg() {
    setIdentity("type");
    setDescription("Booster type either drops or money");
    setMapper(
        in ->
            new OPair<>(
                (in.equalsIgnoreCase("drops") || in.equalsIgnoreCase("money"))
                        || in.equalsIgnoreCase("xp")
                    ? in
                    : null,
                "Cannot find booster type by " + in));
  }

  @Override
  public void onAdd(OCommand command) {
    command.nextTabComplete((previous, args) -> Lists.newArrayList("drops", "money", "xp"));
  }
}
