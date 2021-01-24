package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.datamodule.api.converter.exporter.StorageExporter;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.main.task.StaticTask;

import java.io.File;

public class CmdExport extends OCommand {
    public CmdExport() {
        label("export");
        description("Export data to datapack");
        argument(new StringArg().setIdentity("name").setDescription("Name of the file").setRequired(true));

        onCommand(command -> {
            String name = command.getArgAsReq("name", String.class);
            File dataFolder = SuperiorPrisonPlugin.getInstance().getDataFolder();

            command.getSender().sendMessage("Starting export...");
            StaticTask.getInstance().async(() -> {

                new StorageExporter(SuperiorPrisonPlugin.getInstance().getDatabaseController())
                        .export(dataFolder, name);
                command.getSender().sendMessage("Export finished.");
            });
        });
    }
}
