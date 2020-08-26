package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.commands.PermissionsInitializer;
import com.bgsoftware.superiorprison.plugin.commands.args.MinesArg;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.MinesListMenu;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.frameworks.Framework;
import com.oop.orangeengine.command.OCommand;

import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdMines extends OCommand {
    public CmdMines() {
        label("mines");
        argument(new MinesArg());
        onCommand(command -> {
            Optional<SNormalMine> optionalMine = command.getArg("mine", SNormalMine.class);
            if (optionalMine.isPresent()) {
                SNormalMine mine = optionalMine.get();
                if (!mine.getSettings().isTeleporation()) {
                    messageBuilder(LocaleEnum.MINE_TELEPORTATION_DISABLED.getWithErrorPrefix())
                            .replace(mine)
                            .send(command);
                    return;
                }

                int prisoners = mine.getPlayerCount();
                if (mine.getSettings().getPlayerLimit() != -1 && prisoners == mine.getSettings().getPlayerLimit()) {
                    messageBuilder(LocaleEnum.MINE_IS_FULL.getWithErrorPrefix())
                            .replace(mine)
                            .send(command);
                    return;
                }

                Framework.FRAMEWORK.teleport(command.getSenderAsPlayer(), mine.getSpawnPoint());
            } else {
                MinesListMenu minesListMenu = new MinesListMenu(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(command.getSenderAsPlayer()));
                command.getSenderAsPlayer().openInventory(minesListMenu.getInventory());
            }
        });

        subCommand(new CmdCreate());
        subCommand(new CmdDelete());
        subCommand(new CmdReset());
        subCommand(new CmdReload());
        subCommand(new CmdCopy());
        subCommand(new CmdSetSpawn());
        subCommand(new CmdMigrate());

        PermissionsInitializer.registerPrisonerCommand(this);
    }
}
