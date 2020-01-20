package com.bgsoftware.superiorprison.plugin.newMenu;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.chatCmds.ChatCommands;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.mergeText;

@Getter
public class MineControlPanel extends OMenu {

    private SNormalMine mine;

    public MineControlPanel(SPrisoner viewer, SNormalMine mine) {
        super("mineControlPanel", viewer);
        this.mine = mine;

        // Action handler for set permission
        ClickHandler
                .of("set permission")
                .handle(event -> {
                    previousMove = false;
                    event.getWhoClicked().closeInventory();

                    //TODO: Configurable
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Write new permission!");

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            chatEvent -> {
                                mine.setPermission(chatEvent.getMessage());
                                chatEvent.setCancelled(true);
                                chatEvent.getPlayer().sendMessage(ChatColor.RED + "Mine permission has been set to " + chatEvent.getMessage());

                                // Update button
                                refreshMenus(getClass(), menu -> menu.getMine().getName().contentEquals(getMine().getName()));
                                refresh();
                                mine.save(true);
                            },
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                                    .timesToRun(1)
                                    .timeOut(TimeUnit.SECONDS, 30)
                    );
                })
                .apply(this);

        ClickHandler
                .of("edit generator")
                .handle(event -> {
                    previousMove = false;
                    new GeneratorEditMenu(getViewer(), getMine()).open(this);
                })
                .apply(this);

        ClickHandler
                .of("edit icon")
                .handle(event -> {
                    previousMove = false;
                    event.getWhoClicked().closeInventory();
                    OItem itemBuilder = new OItem(mine.getIcon().clone());
                    AtomicBoolean cancel = new AtomicBoolean(false);

                    ChatCommands chatCommands = new ChatCommands();
                    chatCommands.appendCommand("display name", (player, args) -> {
                        Preconditions.checkArgument(args.length >= 1, "Failed to find display name!");
                        String displayName = mergeText(args);
                        itemBuilder.setDisplayName(displayName);

                        player.sendMessage(Helper.color("Set the display name to " + displayName));
                    });

                    chatCommands.appendCommand("cancel", (player, args) -> {
                        refresh();
                        cancel.set(true);
                    });
                    chatCommands.appendCommand("save", (player, args) -> {
                        mine.setIcon(itemBuilder.getItemStack());
                        mine.save(true);
                        refreshMenus(getClass(), menu -> menu.getMine().getName().contentEquals(getMine().getName()));
                        refresh();
                        cancel.set(true);
                    });

                    chatCommands.appendCommand("set lore", (player, args) -> {
                        Preconditions.checkArgument(args.length >= 2, "Lore line is required!");
                        int line = Integer.parseInt(args[0]);
                        String message = mergeText(Arrays.stream(args).skip(1).toArray(String[]::new));

                        itemBuilder.setLoreLine(line, message);
                        player.sendMessage(Helper.color("Set the lore line " + line + " to " + message));
                    });

                    chatCommands.appendCommand("add lore", (player, args) -> {
                        Preconditions.checkArgument(args.length >= 1, "Lore text is required!");
                        itemBuilder.appendLore(mergeText(args));

                        player.sendMessage(Helper.color("Added to lore: " + mergeText(args)));
                    });

                    chatCommands.appendCommand("clear lore", (player, args) -> {
                        itemBuilder.setLore(new ArrayList<>());

                        player.sendMessage("Cleared the lore!");
                    });

                    chatCommands.appendCommand("remove lore", (player, args) -> {
                        Preconditions.checkArgument(args.length == 1, "Lore line is required!");

                        itemBuilder.removeLoreLine(Integer.parseInt(args[0]));
                        player.sendMessage("Removed lore line " + Integer.parseInt(args[0]));
                    });

                    chatCommands.appendCommand("material", (player, args) -> {
                        Preconditions.checkArgument(args.length == 1, "Material is required!");

                        OMaterial material = OMaterial.matchMaterial(args[0].toUpperCase());
                        Preconditions.checkArgument(material != null, "Failed to find material by name: " + args[0]);

                        itemBuilder.setMaterial(material);
                    });

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            event3 -> {
                                event3.setCancelled(true);
                                chatCommands.handle(event3);
                            },
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                                    .runTill(e -> cancel.get())
                                    .timeOut(TimeUnit.MINUTES, 4)
                    );

                    LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().send((Player) event.getWhoClicked());
                })
                .apply(this);

        ClickHandler
                .of("edit shop")
                .handle(event -> {
                    previousMove = false;
                    new ShopEditMenu(getViewer(), getMine()).open(this);
                })
                .apply(this);
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{getMine()};
    }
}
