package com.bgsoftware.superiorprison.plugin.commands;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.args.ConflictEnumArg;
import com.bgsoftware.superiorprison.plugin.commands.args.FileArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.oop.datamodule.api.converter.importer.StorageImporter;
import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.api.storage.Storage;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.task.StaticTask;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CmdImport extends OCommand {
  public CmdImport() {
    label("import");
    description("Import data from datapack");
    argument(
        new FileArg(
                SuperiorPrisonPlugin.getInstance().getDataFolder(),
                name -> name.endsWith(".datapack"))
            .setRequired(true));
    argument(new ConflictEnumArg().setRequired(true));
    onCommand(
        command -> {
          File file = command.getArgAsReq("file");
          ConflictEnum conflictEnum = command.getArgAsReq("conflict");

          DatabaseController databaseController =
              SuperiorPrisonPlugin.getInstance().getDatabaseController();
          StorageImporter importer = new StorageImporter(databaseController);
          StaticTask.getInstance()
              .async(
                  () -> {
                    try {
                      Map<String, List<ModelBody>> stringListMap = importer.importData(file);
                      long imported = 0;

                      for (Map.Entry<String, List<ModelBody>> typesEntry :
                          stringListMap.entrySet()) {
                        Optional<Storage<? extends ModelBody>> first =
                            databaseController.getStorages().stream()
                                .filter(
                                    storage ->
                                        storage.getVariants().containsKey(typesEntry.getKey()))
                                .findFirst();

                        if (!first.isPresent()) continue;

                        Storage<ModelBody> storage = (Storage<ModelBody>) first.get();
                        for (ModelBody body : typesEntry.getValue()) {
                          boolean found =
                              storage.stream()
                                  .anyMatch(ob -> ob.getKey().equalsIgnoreCase(body.getKey()));
                          if (found) {
                            switch (conflictEnum) {
                              case IGNORE:
                                continue;
                              case OVERRIDE:
                                storage.remove(body);
                            }
                          }

                          storage.add(body);
                          imported += 1;
                        }
                      }

                      messageBuilder(LocaleEnum.IMPORTED_DATA.getWithPrefix())
                          .replace("{amount}", imported)
                          .replace("{file}", file.getPath())
                          .send(command.getSender());
                      databaseController.save(true);
                    } catch (Throwable throwable) {
                      throw new IllegalStateException("Failed to import data", throwable);
                    }
                  });
        });
  }

  public static enum ConflictEnum {
    OVERRIDE,
    // ASK,
    IGNORE;

    public static ConflictEnum from(String string) {
      for (ConflictEnum type : values()) if (type.name().equalsIgnoreCase(string)) return type;
      return null;
    }
  }
}
