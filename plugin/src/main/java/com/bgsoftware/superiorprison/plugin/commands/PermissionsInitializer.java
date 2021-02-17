package com.bgsoftware.superiorprison.plugin.commands;

import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.yaml.ConfigSection;
import com.oop.orangeengine.yaml.interfaces.Valuable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsInitializer {

  private static final List<String> prisonerCommands = new ArrayList<>();
  private static final String prisonerBase = "prison.prisoner.cmds";
  private static final String adminBase = "prison.admin.cmds";

  public static void registerPrisonerCommand(OCommand command) {
    prisonerCommands.add(command.getLabelWithParents());
  }

  // The start
  private void dumpPluginYml(Valuable valuable, OCommand value) {
    if (value.getSubCommands().isEmpty()) {
      ConfigSection section = valuable.createSection(value.getPermission(), false);
      section.set("description", value.getDescription() == null ? "none" : value.getDescription());

    } else {
      ConfigSection section = valuable.createSection(value.getPermission() + ".*", false);
      section.set("description", value.getDescription() == null ? "none" : value.getDescription());
      ConfigSection children = section.createSection("children");
      for (OCommand oCommand : value.getSubCommands().values()) {
        dumpPluginYml(children, oCommand);
      }
    }
  }

  public static void initPermissions(String base, OCommand command) {
    base = base + "." + command.getLabel();
    if (prisonerCommands.contains(command.getLabelWithParents())) {
      command.permission(prisonerBase + base);

    } else command.permission(adminBase + base);

    for (OCommand value : command.getSubCommands().values()) {
      initPermissions(base, value);
    }
  }

  // Out = superiorprison.commands.mines.copy
  // If current command has sub commands then it must have a *
  public void dump(Valuable valuable, OCommand command) {
    valuable.set(reverseLabel(command.getLabelWithParents()), command.getPermission());
    for (OCommand value : command.getSubCommands().values()) {
      dump(valuable, value);
    }
  }

  private String reverseLabel(String labelWithParents) {
    List<String> strings;
    if (labelWithParents.contains(" "))
      strings = new ArrayList<>(Arrays.asList(labelWithParents.split(" ")));
    else return labelWithParents;

    StringBuilder builder = new StringBuilder();
    for (String append : strings) {
      builder.append(append).append(" ");
    }

    return builder.toString();
  }
}
