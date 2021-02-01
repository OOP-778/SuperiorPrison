package com.bgsoftware.superiorprison.plugin.commands;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.oop.datamodule.api.converter.exporter.StorageExporter;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.main.task.StaticTask;
import java.io.File;

public class CmdExport extends OCommand {
  public CmdExport() {
    label("export");
    description("Export data to datapack");
    argument(
        new StringArg().setIdentity("name").setDescription("Name of the file").setRequired(true));

    onCommand(
        command -> {
          String name = command.getArgAsReq("name", String.class);
          File dataFolder = SuperiorPrisonPlugin.getInstance().getDataFolder();

          StaticTask.getInstance()
              .async(
                  () -> {
                    long export =
                        new StorageExporter(
                                SuperiorPrisonPlugin.getInstance().getDatabaseController())
                            .export(dataFolder, name);

                    messageBuilder(LocaleEnum.EXPORTED_DATA.getWithPrefix())
                        .replace("{amount}", export)
                        .replace("{file}", dataFolder.toPath().resolve(name + ".datapack"))
                        .send(command.getSender());
                  });
        });
  }
}
