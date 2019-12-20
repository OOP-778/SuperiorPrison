package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.constant.MenuNames;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.button.AMenuButton;
import com.oop.orangeengine.menu.config.ConfigMenuTemplate;
import com.oop.orangeengine.menu.config.action.ActionListenerController;
import com.oop.orangeengine.menu.config.action.ActionProperties;
import com.oop.orangeengine.menu.events.ButtonClickEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class GeneratorMenu extends EditMenuHelper {

    private ConfigMenuTemplate template;

    public GeneratorMenu(ConfigMenuTemplate menuTemplate) {
        this.template = menuTemplate;

        String menuId = MenuNames.MINE_EDIT_GENERATOR.getId();
        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                        .menuId(menuId)
                        .actionId("edit")
                        .buttonAction(clickEvent -> {
                            clickEvent.getPlayer().closeInventory();
                            LocaleEnum.EDIT_GENERATOR_WRITE_RATE.getWithPrefix().send(clickEvent.getPlayer());

                            SubscriptionFactory.getInstance().subscribeTo(
                                    AsyncPlayerChatEvent.class,
                                    chatEvent -> {
                                        double rate = Double.parseDouble(chatEvent.getMessage());
                                        OMaterial material = clickEvent.getClickedButton().grab("mineMaterial", OMaterial.class).get();
                                        SNormalMine mine = clickEvent.getMenu().grab("mine", SNormalMine.class).get();

                                        chatEvent.setCancelled(true);
                                        double currentRate = mine.getGenerator().getCurrentUsedRate(material);
                                        if ((currentRate + rate) > 100) {
                                            LocaleEnum.EDIT_GENERATOR_RATE_LIMIT_EXCEED.getWithErrorPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%material%", beautify(material.name())));
                                            clickEvent.getWrappedInventory().open(chatEvent.getPlayer());
                                            return;
                                        }

                                        Optional<OPair<Double, OMaterial>> first = mine.getGenerator().getGeneratorMaterials().stream()
                                                .filter(pair -> pair.getSecond() == material)
                                                .findFirst();
                                        if (first.isPresent()) {
                                            first.get().setFirst(rate);
                                            LocaleEnum.EDIT_GENERATOR_RATE_SET.getWithPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%material%", beautify(material.name()), "%rate%", rate + ""));

                                            // Update
                                            fillMaterials(clickEvent.getMenu(), mine);
                                            clickEvent.getMenu().update();
                                            clickEvent.getWrappedInventory().open(chatEvent.getPlayer());
                                            SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
                                        }
                                    },
                                    new SubscriptionProperties<AsyncPlayerChatEvent>()
                                            .filter(chatEvent -> {
                                                double value = NumberUtils.toDouble(chatEvent.getMessage(), -0.0);
                                                chatEvent.setCancelled(true);
                                                if (value == -0.0) {
                                                    LocaleEnum.EDIT_GENERATOR_RATE_NOT_NUMBER.getWithErrorPrefix().send(chatEvent.getPlayer());
                                                }

                                                return value > -0.0;
                                            }).timesToRun(1)
                            );
                        })
        );
        ActionListenerController.getInstance().listen(
                new ActionProperties<>(ButtonClickEvent.class)
                        .menuId(menuId)
                        .actionId("remove")
                        .buttonAction(clickEvent -> {
                            OMaterial material = clickEvent.getClickedButton().grab("mineMaterial", OMaterial.class).get();
                            SNormalMine mine = clickEvent.getMenu().grab("mine", SNormalMine.class).get();

                            Optional<OPair<Double, OMaterial>> first = mine.getGenerator().getGeneratorMaterials().stream()
                                    .filter(pair -> pair.getSecond() == material)
                                    .findFirst();
                            if (first.isPresent()) {
                                mine.getGenerator().getGeneratorMaterials().remove(first.get());
                                LocaleEnum.EDIT_GENERATOR_REMOVED.getWithPrefix().send(clickEvent.getPlayer(), ImmutableMap.of("%material%", beautify(material.name())));
                            }

                            fillMaterials(clickEvent.getMenu(), mine);
                            clickEvent.getMenu().update();
                            SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
                        })
        );
    }

    @Override
    public AMenu build(SNormalMine mine) {
        AMenu menu = template.build();
        menu.title(menu.title().replace("%mine_name%", mine.getName()));
        parseButtons(menu, mine);
        fillMaterials(menu, mine);

        menu.bottomInvClickHandler(event -> {
            if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
            ItemStack clone = event.getCurrentItem().clone();
            OMaterial material = OMaterial.matchMaterial(clone);

            //TODO: Add messages to locale
            if (!clone.getType().isBlock()) {
                LocaleEnum.EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK.getWithErrorPrefix().send((Player) event.getWhoClicked());
                return;
            }

            if (material == null) {
                event.getWhoClicked().sendMessage(Helper.color("&cFailed to match material from itemStack, Please Report to Developer!"));
                return;
            }

            if (mine.getGenerator().getGeneratorMaterials().stream().anyMatch(pair -> pair.getSecond() == material)) {
                LocaleEnum.EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS.getWithErrorPrefix().send((Player) event.getWhoClicked());
                return;
            }

            mine.getGenerator().getGeneratorMaterials().add(new OPair<>(0.0, OMaterial.matchMaterial(clone)));
            fillMaterials(menu, mine);
            menu.update();

            SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
        });

        menu.store("mine", mine);
        menu.getAllChildren().forEach(children -> children.store("mine", mine));
        return menu;
    }

    public void fillMaterials(AMenu menu, SNormalMine mine) {
        AMenuButton templateButton = menu.buttons().stream()
                .filter(button -> button.containsData("template") && button.grab("template", String.class).get().contentEquals("materialTemplate"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find material template for Generator Edit menu"));

        // Remove old buttons with mats
        menu.removeButtonIfMatched(button -> button.containsData("mineMaterial"));

        String displayName = templateButton.currentItem().getItemMeta().getDisplayName();
        List<String> lore = templateButton.currentItem().getItemMeta().getLore();

        // Fill new materials
        mine.getGenerator().getGeneratorMaterials().forEach(pair -> {
            AMenuButton button = templateButton.clone();

            Helper.debug("Setting " + pair.getSecond());
            OItem buttonItem = new OItem(pair.getSecond());

            buttonItem.setLore(lore);
            buttonItem.setDisplayName(displayName);

            buttonItem.replaceDisplayName("%material_name%", beautify(pair.getSecond().name()));
            buttonItem.replaceInLore("%material_rate%", pair.getFirst() + "");

            button.currentItem(buttonItem.getItemStack());
            button.store("mineMaterial", pair.getSecond());

            menu.addButton(button.paged(true));
        });
    }
}
