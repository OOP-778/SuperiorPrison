package com.bgsoftware.superiorprison.plugin.menu.control;

import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.menu.GeneratorEditMenu;
import com.bgsoftware.superiorprison.plugin.menu.MineEffectsMenu;
import com.bgsoftware.superiorprison.plugin.menu.ShopEditMenu;
import com.bgsoftware.superiorprison.plugin.menu.access.AccessEditMenu;
import com.bgsoftware.superiorprison.plugin.menu.flags.AreaChooseMenu;
import com.bgsoftware.superiorprison.plugin.menu.messages.MessagesListMenu;
import com.bgsoftware.superiorprison.plugin.menu.reward.MineRewardsMenu;
import com.bgsoftware.superiorprison.plugin.menu.settings.SettingsMenu;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.chatcmds.ChatCommands;
import com.bgsoftware.superiorprison.plugin.util.menu.ButtonClickEvent;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.item.custom.OSkull;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.commands.CommandHelper.messageBuilder;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.mergeText;

@Getter
public class MineControlPanel extends OPagedMenu<OptionEnum> implements OMenu.Templateable {

    private final SNormalMine mine;

    public MineControlPanel(SPrisoner viewer, SNormalMine mine) {
        super("mineControlPanel", viewer);
        this.mine = mine;

        clickHandler("option click")
                .handle(event -> {
                    OptionEnum optionEnum = requestObject(event.getSlot());
                    switch (optionEnum) {
                        case ACCESS:
                            move(new AccessEditMenu(getViewer(), mine));
                            break;

                        case ICON:
                            handleIconClick(event);
                            break;

                        case GENERATOR:
                            move(new GeneratorEditMenu(getViewer(), getMine()));
                            break;

                        case FLAGS:
                            move(new AreaChooseMenu(getViewer(), getMine()));
                            break;

                        case SHOP:
                            move(new ShopEditMenu(getViewer(), getMine()));
                            break;

                        case EFFECTS:
                            move(new MineEffectsMenu(getViewer(), getMine().getEffects()));
                            break;

                        case MESSAGES:
                            move(new MessagesListMenu(getViewer(), getMine()));
                            break;

                        case SETTINGS:
                            move(new SettingsMenu(getViewer(), getMine()));
                            break;

                        case REWARDS:
                            move(new MineRewardsMenu(getViewer(), getMine()));
                            break;
                    }
                });
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        Optional<OMenuButton.ButtonItemBuilder> soon = getTemplateButtonFromTemplate("option").map(button -> button.getStateItem("soon"));
        if (!soon.isPresent()) return inventory;

        for (int slot : getEmptySlots()) {
            inventory.setItem(slot, soon.get().getItemStack());
        }

        return inventory;
    }

    private void handleIconClick(ButtonClickEvent event) {
        forceClose();
        final ItemBuilder[] itemBuilder = {new OItem(mine.getIcon().clone())};

        ChatCommands chatCommands = new ChatCommands((Player) event.getWhoClicked());
        chatCommands.appendCommand("display name", (player, args) -> {
            Preconditions.checkArgument(args.length >= 1, "Failed to find display name!");
            String displayName = mergeText(args);
            itemBuilder[0].setDisplayName(displayName);

            messageBuilder(LocaleEnum.EDIT_ICON_DISPLAY_NAME_SET.getWithPrefix())
                    .replace("{display_name}", displayName)
                    .send(player);
        });

        chatCommands.appendCommand("cancel", (player, canceller, args) -> {
            refresh();
            canceller.set(true);
        });

        chatCommands.appendCommand("save", (player, canceller, args) -> {
            mine.setIcon(itemBuilder[0].getItemStack());
            mine.save(true);
            refreshMenus(getClass(), menu -> menu.getMine().getName().contentEquals(getMine().getName()));
            refresh();
            canceller.set(true);

            LocaleEnum.EDIT_ICON_SAVE.getWithPrefix().send(player);
        });

        chatCommands.appendCommand("set lore", (player, args) -> {
            Preconditions.checkArgument(args.length >= 2, "Lore line is required!");
            int line = Integer.parseInt(args[0]);
            String text = mergeText(Arrays.stream(args).skip(1).toArray(String[]::new));

            itemBuilder[0].setLoreLine(line, text);
            messageBuilder(LocaleEnum.EDIT_ICON_SET_LORE_LINE.getWithPrefix())
                    .replace("{line}", line)
                    .replace("{text}", text)
                    .send(player);
        });

        chatCommands.appendCommand("add lore", (player, args) -> {
            Preconditions.checkArgument(args.length >= 1, "Lore text is required!");
            String text = mergeText(args);
            itemBuilder[0].appendLore(text);

            messageBuilder(LocaleEnum.EDIT_ICON_ADD_LORE.getWithPrefix())
                    .replace("{text}", text)
                    .send(player);
        });

        chatCommands.appendCommand("clear lore", (player, args) -> {
            itemBuilder[0].setLore(new ArrayList<>());
            LocaleEnum.EDIT_ICON_CLEAR_LORE.getWithPrefix().send(player);
        });

        chatCommands.appendCommand("remove lore", (player, args) -> {
            Preconditions.checkArgument(args.length == 1, "Lore line is required!");
            int line = Integer.parseInt(args[0]);

            itemBuilder[0].removeLoreLine(line);
            messageBuilder(LocaleEnum.EDIT_ICON_REMOVE_LORE_LINE.getWithPrefix())
                    .replace("{line}", line)
                    .send(player);
        });

        chatCommands.appendCommand("material", (player, args) -> {
            Preconditions.checkArgument(args.length == 1, "Material is required!");

            OMaterial material = OMaterial.matchMaterial(args[0].toUpperCase());
            Preconditions.checkArgument(material != null, "Failed to find material by name: " + args[0]);

            itemBuilder[0].setMaterial(material);
            messageBuilder(LocaleEnum.EDIT_ICON_SET_MATERIAL.getWithPrefix())
                    .replace("{material}", Helper.beautify(material.name()))
                    .send(player);
        });

        chatCommands.appendCommand("texture", (player, args) -> {
            Preconditions.checkArgument(args.length == 1, "Texture is required!");
            Preconditions.checkArgument(itemBuilder[0].getOMaterial() == OMaterial.PLAYER_HEAD, "This command only works for heads!");

            itemBuilder[0] = new OSkull(itemBuilder[0].getItemStack()).texture(args[0]);
            messageBuilder(LocaleEnum.EDIT_ICON_SET_TEXTURE.getWithPrefix())
                    .send(player);
        });

        chatCommands.listen();
        chatCommands.onFinish(this::refresh);
        chatCommands.afterInput(() -> {
            OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
            clone.replace("%item%", new LineContent(Helper.beautify(OMaterial.matchMaterial(itemBuilder[0].getItemStack()))).hoverItem().item(itemBuilder[0].getItemStack()).parent());
            clone.send(event.getWhoClicked());
        });

        chatCommands.setExceptionHandler(((player, throwable) -> {
            OMessage clone = LocaleEnum.PREFIX_ERROR.getMessage().clone();
            if (clone instanceof OChatMessage) {
                ((OChatMessage) clone).lineList().get(0).append(throwable.getMessage());
            }
            clone.send(player);
        }));

        OMessage clone = LocaleEnum.EDIT_ICON_MAIN_MESSAGE.getMessage().clone();
        clone.replace("%item%", new LineContent(Helper.beautify(OMaterial.matchMaterial(itemBuilder[0].getItemStack()))).hoverItem().item(itemBuilder[0].getItemStack()).parent());
        clone.send(event.getWhoClicked());
    }

    @Override
    public List<OptionEnum> requestObjects() {
        return Arrays.asList(OptionEnum.values());
    }

    @Override
    public OMenuButton toButton(OptionEnum obj) {
        Optional<OMenuButton> templateButtonFromTemplate = getTemplateButtonFromTemplate("option");
        Preconditions.checkArgument(templateButtonFromTemplate.isPresent(), "Failed to find option template in Control Panel");

        OMenuButton optionButton = templateButtonFromTemplate.get().clone();
        if (mine.getLinker().isLinked(obj.name().toLowerCase())) {
            optionButton.locked(true);
            OMenuButton.ButtonItemBuilder stateItem = optionButton.getStateItem("option-linked");
            if (stateItem != null) {
                stateItem
                        .itemBuilder()
                        .replace("{linked_mine_name}", mine.getLinker().getLinkedTo().get(obj.name().toLowerCase()))
                        .replace("{option_name}", obj.name().toLowerCase());
                return optionButton.currentItem(stateItem.getItemStack());
            }
        }

        OMenuButton.ButtonItemBuilder optionItem = optionButton.getStateItem(obj.name().toLowerCase() + "-option");
        return optionButton.currentItem(optionItem.getItemStack());
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{getViewer(), mine};
    }
}
