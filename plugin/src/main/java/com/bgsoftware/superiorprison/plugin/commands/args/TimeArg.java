package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.util.TimeUtil;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class TimeArg extends CommandArgument<Long> {
  public TimeArg() {
    setIdentity("time");
    setDescription("Time like so xD:xH:xM:xS");
    setMapper(
        in -> {
          long seconds = TimeUtil.toSeconds(in);
          return new OPair<>(seconds == 0 ? null : seconds, "Failed to parse time from " + in);
        });
  }
}
