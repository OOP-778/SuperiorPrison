package com.bgsoftware.superiorprison.plugin.commands.mines;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.bgsoftware.superiorprison.plugin.util.MutliVerUtil;
import com.bgsoftware.superiorprison.plugin.util.SPLocation;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.command.arg.arguments.StringArg;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.task.OTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;

public class CmdCreate extends OCommand {

    private final Cache<UUID, Boolean> creating = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public CmdCreate() {
        description("Create a mine");
        label("create")
                .ableToExecute(Player.class)
                .permission("superiorprison.admin")
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

            if (creating.getIfPresent(player.getUniqueId()) != null) {
                LocaleEnum.MINE_CREATE_ALREADY_CREATING.getWithErrorPrefix().send(player);
                return;
            }

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

            AtomicReference<Cuboid> region = new AtomicReference<>();
            creating.put(player.getUniqueId(), true);

            AtomicReference<SubscribedEvent<PlayerInteractEvent>> posSelectEvent = new AtomicReference<>();
            posSelectEvent.set(sf.subscribeTo(PlayerInteractEvent.class, posEvent -> {
                if (!MutliVerUtil.isPrimaryHand(posEvent) || posEvent.getClickedBlock() == null) return;
                posEvent.setCancelled(true);

                if (regionPos1.get() == null) {
                    regionPos1.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    messageBuilder(LocaleEnum.MINE_SELECT_POS.getWithPrefix())
                            .replace("{pos}", 1)
                            .send(command);

                } else if (regionPos2.get() == null) {
                    regionPos2.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    messageBuilder(LocaleEnum.MINE_SELECT_POS.getWithPrefix())
                            .replace("{pos}", 2)
                            .send(command);
                    LocaleEnum.MINE_CREATE_SELECT_MINE_POS.getWithPrefix().send(player);
                    region.set(new Cuboid(regionPos1.get().toBukkit(), regionPos2.get().toBukkit()));

                } else if (minePos1.get() == null) {
                    if (!region.get().containsLocation(posEvent.getClickedBlock().getLocation(), false)) {
                        LocaleEnum.MINE_CREATE_POSITION_MUST_BE_WITHIN_REGION.getWithErrorPrefix().send(player);
                        return;
                    }

                    minePos1.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    messageBuilder(LocaleEnum.MINE_SELECT_POS.getWithPrefix())
                            .replace("{pos}", 1)
                            .send(command);

                } else if (minePos2.get() == null) {
                    if (!region.get().containsLocation(posEvent.getClickedBlock().getLocation(), false)) {
                        LocaleEnum.MINE_CREATE_POSITION_MUST_BE_WITHIN_REGION.getWithErrorPrefix().send(player);
                        return;
                    }

                    minePos2.set(new SPLocation(posEvent.getClickedBlock().getLocation()));
                    messageBuilder(LocaleEnum.MINE_SELECT_POS.getWithPrefix())
                            .replace("{pos}", 2)
                            .send(command);

                    messageBuilder(LocaleEnum.MINE_SELECT_SPAWN_POS.getWithPrefix())
                            .send(command);
                    posSelectEvent.get().end();

                    sf.subscribeTo(PlayerInteractEvent.class, spawnEvent -> {
                                if (!region.get().containsLocation(posEvent.getClickedBlock().getLocation(), false)) {
                                    LocaleEnum.MINE_CREATE_POSITION_MUST_BE_WITHIN_REGION.getWithErrorPrefix().send(player);
                                    return;
                                }
                                spawnPos.set(new SPLocation(spawnEvent.getPlayer().getEyeLocation().add(0.5, 1.3, 0.5)));

                                new OTask()
                                        .delay(400)
                                        .runnable(() -> player.getInventory().setItemInHand(null))
                                        .execute();

                                SNormalMine sNormalMine = new SNormalMine(mineName, regionPos1.get(), regionPos2.get(), minePos1.get(), minePos2.get());
                                sNormalMine.setSpawnPoint(spawnPos.get());

                                messageBuilder(LocaleEnum.MINE_CREATE_SUCCESSFUL.getWithPrefix())
                                        .replace(sNormalMine)
                                        .send(command);

                                SuperiorPrisonPlugin.getInstance().getMineController().add(sNormalMine);
                                creating.invalidate(player.getUniqueId());

                            },
                            new SubscriptionProperties<PlayerInteractEvent>()
                                    .timeOut(TimeUnit.MINUTES, 5)
                                    .priority(EventPriority.HIGHEST)
                                    .timesToRun(1)
                                    .filter(spawnEvent -> spawnEvent.getPlayer().equals(player) && MutliVerUtil.isPrimaryHand(spawnEvent) && SuperiorPrisonPlugin.getInstance().getMainConfig().getAreaSelectionTool().getItemStack().equals(spawnEvent.getItem()))
                    );
                }
            }, new SubscriptionProperties<PlayerInteractEvent>()
                    .timeOut(TimeUnit.MINUTES, 5)
                    .priority(EventPriority.HIGHEST)
                    .timesToRun(-1)
                    .filter(posEvent -> posEvent.getPlayer().equals(player) && MutliVerUtil.isPrimaryHand(posEvent) && SuperiorPrisonPlugin.getInstance().getMainConfig().getAreaSelectionTool().getItemStack().equals(posEvent.getItem()) && posEvent.getClickedBlock() != null)));

            sf.subscribeTo(AsyncPlayerChatEvent.class, chatEvent -> {
                chatEvent.setCancelled(true);
                creating.invalidate(player.getUniqueId());
                posSelectEvent.get().end();
                player.getInventory().setItemInHand(null);
            }, new SubscriptionProperties<AsyncPlayerChatEvent>()
                    .runTill(e -> posSelectEvent.get().cancelled())
                    .filter(event -> event.getMessage().equalsIgnoreCase("cancel") && event.getPlayer().equals(player)));
        };
    }

    private void addIfDoesntHave(ItemStack itemStack, Player player) {
        for (ItemStack itemStack2 : player.getInventory().getContents()) {
            if (itemStack2 == null || itemStack2.getType() == Material.AIR) continue;
            if (itemStack2.equals(itemStack)) return;
        }
        player.getInventory().addItem(itemStack);
    }
}
