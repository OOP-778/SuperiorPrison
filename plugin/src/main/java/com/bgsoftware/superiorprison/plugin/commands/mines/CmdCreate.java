package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.MutliVerUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CmdCreate extends OCommand {

    public CmdCreate() {
        label("create")
                .ableToExecute(Player.class)
                .argument(
                        new StringArg()
                                .setIdentity("name")
                                .setRequired(true)
                ).listen(onCommand());
    }

    private Consumer<WrappedCommand> onCommand() {
        return command -> {
            Player player = (Player) command.getSender();
            String mineName = (String) command.getArg("name").get();

            if (SuperiorPrisonPlugin.getInstance().getMineController().getMine(mineName).isPresent()) {
                LocaleEnum.MINE_CREATE_FAIL_ALREADY_EXISTS.getWithErrorPrefix().send(player);
                return;
            }

            LocaleEnum.MINE_CREATE_SELECT_REGION_POS.getWithPrefix().send(player);
            addIfDoesntHave(SuperiorPrisonPlugin.getInstance().getMainConfig().getAreaSelectionTool().getItemStack(), player);

            SubscriptionFactory sf = SubscriptionFactory.getInstance();
            AtomicReference<SPLocation> regionPos1 = new AtomicReference<>();
            AtomicReference<SPLocation> regionPos2 = new AtomicReference<>();
            AtomicReference<SPLocation> minePos1 = new AtomicReference<>();
            AtomicReference<SPLocation> minePos2 = new AtomicReference<>();
            AtomicReference<SPLocation> spawnPos = new AtomicReference<>();

            Set<AtomicReference<SPLocation>> posses = Sets.newHashSet(regionPos1, regionPos2, minePos1, minePos2);

            SubscribedEvent<PlayerInteractEvent> subscribedEvent = sf.subscribeTo(PlayerInteractEvent.class, posEvent -> {
                if (!MutliVerUtil.isPrimaryHand(posEvent) || posEvent.getClickedBlock() == null) return;
                posEvent.setCancelled(true);

                if (regionPos1.get() == null) {
                    regionPos1.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    LocaleEnum.MINE_SELECT_POS.getWithPrefix().send(player, ImmutableMap.of("%pos%", 1 + ""));

                } else if (regionPos2.get() == null) {
                    regionPos2.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    LocaleEnum.MINE_SELECT_POS.getWithPrefix().send(player, ImmutableMap.of("%pos%", 2 + ""));

                } else if (minePos1.get() == null) {
                    minePos1.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    LocaleEnum.MINE_SELECT_POS.getWithPrefix().send(player, ImmutableMap.of("%pos%", 1 + ""));

                } else if (minePos2.get() == null) {
                    minePos2.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    LocaleEnum.MINE_SELECT_SPAWN_POS.getWithPrefix().send(player);

                } else if (spawnPos.get() == null) {
                    spawnPos.set(new SPLocation(posEvent.getClickedBlock().getLocation()));

                    LocaleEnum.MINE_CREATE_SUCCESSFUL.getWithPrefix().send(player, ImmutableMap.of("%mine_name%", mineName));
                    SNormalMine sNormalMine = new SNormalMine(mineName, regionPos1.get(), regionPos2.get(), minePos1.get(), minePos2.get());
                    sNormalMine.setSpawnPoint(spawnPos.get());

                    SuperiorPrisonPlugin.getInstance().getMineController().add(sNormalMine);
                }
            }, new SubscriptionProperties<PlayerInteractEvent>().timeOut(TimeUnit.MINUTES, 2).timesToRun(5).filter(posEvent -> MutliVerUtil.isPrimaryHand(posEvent) && SuperiorPrisonPlugin.getInstance().getMainConfig().getAreaSelectionTool().getItemStack().equals(posEvent.getItem()) && posEvent.getClickedBlock() != null));
        };
    }

    public void addIfDoesntHave(ItemStack itemStack, Player player) {
        for (ItemStack itemStack2 : player.getInventory().getContents())
            if (itemStack2.equals(itemStack)) return;
        player.getInventory().addItem(itemStack);
    }
}
