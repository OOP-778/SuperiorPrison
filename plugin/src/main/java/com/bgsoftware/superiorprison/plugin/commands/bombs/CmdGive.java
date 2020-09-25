package com.bgsoftware.superiorprison.plugin.commands.bombs;

import com.bgsoftware.superiorprison.plugin.commands.args.BombsArg;
import com.bgsoftware.superiorprison.plugin.config.bomb.BombConfig;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.IntArg;
import com.oop.orangeengine.command.arg.arguments.PlayerArg;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.entity.Player;

public class CmdGive extends OCommand {

    public CmdGive() {
        label("give");
        description("Give a bomb");
        argument(new PlayerArg().setRequired(true));
        argument(new BombsArg().setRequired(true));
        argument(new IntArg().setIdentity("amount").setDescription("Amount of backpacks you want to give"));

        onCommand(command -> {
            Player receiver = command.getArgAsReq("player");
            BombConfig bombConfig = command.getArgAsReq("bomb");
            int amount = command.getArg("amount").map(object -> (Integer) object).orElse(1);

            StaticTask.getInstance().ensureSync(() -> {
                for (int i = 0; i < amount; i++) {
                    if (receiver.getInventory().firstEmpty() == -1)
                        receiver.getWorld().dropItem(receiver.getLocation(), bombConfig.getItem());
                    else
                        receiver.getInventory().addItem(bombConfig.getItem());
                }
            });
        });
    }

}
