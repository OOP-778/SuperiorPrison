package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CmdCreate extends OCommand {

    public CmdCreate() {
        label("create")
                .ableToExecute(Player.class)
                .argument(
                        new StringArg()
                                .setIdentity("name")
                                .setIsRequired(true)
                ).listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand() {
        return command -> {
            Player player = (Player) command.getSender();
            String mineName = (String) command.getArg("name").get();

            //TODO: OOP, create that freaking database
            if (/*SuperiorPrisonPlugin.getInstance().getMineController().getMineByName(mineName).isPresent()*/ false) {
                //TODO: Configurable
                player.sendMessage(ChatColor.RED + "Mine already exists.");
                return;
            }

            player.sendMessage(ChatColor.GREEN + "Select two corners for the mine.");

            SubscriptionFactory sf = SubscriptionFactory.getInstance();
            sf.subscribeTo(PlayerInteractEvent.class, pos1Event -> {
                Location pos1 = pos1Event.getClickedBlock().getLocation();
                player.sendMessage(ChatColor.GREEN + "Selected position #1.");

                pos1Event.setCancelled(true);

                sf.subscribeTo(PlayerInteractEvent.class, pos2Event -> {
                    Location pos2 = pos2Event.getClickedBlock().getLocation();
                    player.sendMessage(ChatColor.GREEN + "Successfully created a new mine! (" + mineName + ")");
                    new SNormalMine(mineName, pos1, pos2);
                }, new SubscriptionProperties<PlayerInteractEvent>().timeOut(TimeUnit.SECONDS, 30).timesToRun(1).filter(filterEvent -> filterEvent.getClickedBlock() != null && filterEvent.hasItem() && filterEvent.getItem().getType() == OMaterial.GOLDEN_AXE.parseMaterial()).onTimeOut(event -> {
                }));
            }, new SubscriptionProperties<PlayerInteractEvent>().timeOut(TimeUnit.SECONDS, 30).timesToRun(1).filter(filterEvent -> filterEvent.getClickedBlock() != null && filterEvent.hasItem() && filterEvent.getItem().getType() == OMaterial.GOLDEN_AXE.parseMaterial()).onTimeOut(event -> {
            }));
        };
    }

}
