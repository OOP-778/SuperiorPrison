package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.flags.AreaChooseMenu;
import com.bgsoftware.superiorprison.plugin.menu.access.AccessEditMenu;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.chatCmds.ChatCommands;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.item.message.ItemLineContent;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.mergeText;

@Getter
public class MineControlPanel extends OMenu {

    private SNormalMine mine;

    public MineControlPanel(SPrisoner viewer, SNormalMine mine) {
        super("mineControlPanel", viewer);
        this.mine = mine;

        ClickHandler
                .of("edit flags")
                .handle(event -> {
                    previousMove = false;
                    new AreaChooseMenu(getViewer(), getMine()).open(this);
                })
                .apply(this);

        ClickHandler
                .of("edit access")
                .handle(event -> {
                    previousMove = false;
                    new AccessEditMenu(getViewer(), mine).open(this);
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

                        messageBuilder(LocaleEnum.EDIT_ICON_DISPLAY_NAME_SET.getWithPrefix())
                                .replace("{display_name}", displayName)
                                .send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
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

                        LocaleEnum.EDIT_ICON_SAVE.getWithPrefix().send(player);
                    });

                    chatCommands.appendCommand("set lore", (player, args) -> {
                        Preconditions.checkArgument(args.length >= 2, "Lore line is required!");
                        int line = Integer.parseInt(args[0]);
                        String text = mergeText(Arrays.stream(args).skip(1).toArray(String[]::new));

                        itemBuilder.setLoreLine(line, text);
                        messageBuilder(LocaleEnum.EDIT_ICON_SET_LORE_LINE.getWithPrefix())
                                .replace("{line}", line)
                                .replace("{text}", text)
                                .send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
                    });

                    chatCommands.appendCommand("add lore", (player, args) -> {
                        Preconditions.checkArgument(args.length >= 1, "Lore text is required!");
                        String text = mergeText(args);
                        itemBuilder.appendLore(text);

                        messageBuilder(LocaleEnum.EDIT_ICON_ADD_LORE.getWithPrefix())
                                .replace("{text}", text)
                                .send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
                    });

                    chatCommands.appendCommand("clear lore", (player, args) -> {
                        itemBuilder.setLore(new ArrayList<>());
                        LocaleEnum.EDIT_ICON_CLEAR_LORE.getWithPrefix().send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
                    });

                    chatCommands.appendCommand("remove lore", (player, args) -> {
                        Preconditions.checkArgument(args.length == 1, "Lore line is required!");
                        int line = Integer.parseInt(args[0]);

                        itemBuilder.removeLoreLine(line);
                        messageBuilder(LocaleEnum.EDIT_ICON_REMOVE_LORE_LINE.getWithPrefix())
                                .replace("{line}", line)
                                .send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
                    });

                    chatCommands.appendCommand("material", (player, args) -> {
                        Preconditions.checkArgument(args.length == 1, "Material is required!");

                        OMaterial material = OMaterial.matchMaterial(args[0].toUpperCase());
                        Preconditions.checkArgument(material != null, "Failed to find material by name: " + args[0]);

                        itemBuilder.setMaterial(material);
                        messageBuilder(LocaleEnum.EDIT_ICON_SET_MATERIAL.getWithPrefix())
                                .replace("{material}", Helper.beautify(material.name()))
                                .send(player);

                        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                        clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                        clone.send((Player) event.getWhoClicked());
                    });

                    SubscriptionFactory.getInstance().subscribeTo(
                            AsyncPlayerChatEvent.class,
                            chatCommands::handle,
                            new SubscriptionProperties<AsyncPlayerChatEvent>()
                                    .runTill(e -> cancel.get())
                                    .priority(EventPriority.HIGHEST)
                                    .async(false)
                                    .timeOut(TimeUnit.MINUTES, 4)
                    );

                    chatCommands.setExceptionHandler(((player, throwable) -> {
                        OMessage clone = LocaleEnum.PREFIX_ERROR.getMessage().clone();
                        clone.getLineList().get(0).append(throwable.getMessage());
                        clone.send(player);
                    }));

                    OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
                    clone.replace("%item%", new ItemLineContent(itemBuilder.getItemStack()).text(Helper.beautify(OMaterial.matchMaterial(itemBuilder.getItemStack()).name())));
                    clone.send((Player) event.getWhoClicked());
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
