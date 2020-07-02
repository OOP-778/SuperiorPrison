package com.bgsoftware.superiorprison.plugin.commands.backpacks;

import com.bgsoftware.superiorprison.plugin.commands.args.BackPackArg;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.IntArg;
import com.oop.orangeengine.command.arg.arguments.PlayerArg;
import com.oop.orangeengine.main.task.StaticTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CmdGive extends OCommand {
    public CmdGive() {
        label("give");
        description("Give an backpack");

        argument(new PlayerArg().setRequired(true));
        argument(new BackPackArg().setRequired(true));
        argument(new IntArg().setIdentity("amount").setDescription("Amount of backpacks you want to give"));
        argument(new IntArg().setIdentity("level").setDescription("What level the backpacks should be"));

        onCommand(command -> {
            Player receiver = command.getArgAsReq("player");
            BackPackConfig backPackConfig = command.getArgAsReq("backpack");
            int amount = command.getArg("amount").map(object -> (Integer) object).orElse(1);
            int level = command.getArg("level").map(object -> (Integer) object).orElse(1);

            backPackConfig = backPackConfig.getByLevel(level);
            BackPackConfig finalBackPackConfig = backPackConfig;
            StaticTask.getInstance().ensureSync(() -> {
                for (int i = 0; i < amount; i++) {
                    SBackPack backpack = finalBackPackConfig.build(receiver);
                    backpack.add(new ItemStack(Material.DIAMOND, 32), new ItemStack(Material.DIAMOND, 32));
                    backpack.save();
                    if (receiver.getInventory().firstEmpty() == -1)
                        receiver.getWorld().dropItem(receiver.getLocation(), backpack.getItem());
                    else
                        receiver.getInventory().addItem(backpack.getItem());
                }
            });
        });
    }
}
