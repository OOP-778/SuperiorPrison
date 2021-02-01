package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import java.util.Optional;

public class BombsArg extends CommandArgument<BombConfig> {
  public BombsArg() {
    setIdentity("bomb");
    setDescription("Name of the bomb");
    setMapper(
        in -> {
          Optional<BombConfig> bombOf =
              SuperiorPrisonPlugin.getInstance().getBombController().getBombOf(in);
          return new OPair<>(bombOf.orElse(null), "Bomb by name " + in + " was not found!");
        });
  }

  @Override
  public void onAdd(OCommand command) {
    command.nextTabComplete(
        (res, old) -> SuperiorPrisonPlugin.getInstance().getBombController().getBombs());
  }
}
