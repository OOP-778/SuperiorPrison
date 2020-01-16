package com.bgsoftware.superiorprison.plugin.newMenu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautify;

public class GeneratorEditMenu extends OPagedMenu<OPair<Double, OMaterial>> implements OMenu.Templateable {

    private SNormalMine mine;

    public GeneratorEditMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineGenerator", viewer);
        this.mine = mine;

        ClickHandler
                .of("material")
                .handle(event -> {
                    OPair<Double, OMaterial> materialPair = requestObject(event.getRawSlot());
                    if (event.getClick() == ClickType.RIGHT) {
                        mine.getGenerator().getGeneratorMaterials().remove(materialPair);
                        refreshMenus(GeneratorEditMenu.class, menu -> menu.mine.getName().contentEquals(mine.getName()));

                    } else if (event.getClick() == ClickType.LEFT) {
                        SubscriptionFactory.getInstance().subscribeTo(
                                AsyncPlayerChatEvent.class,
                                chatEvent -> {
                                    double rate = Double.parseDouble(chatEvent.getMessage());

                                    chatEvent.setCancelled(true);
                                    double currentRate = mine.getGenerator().getCurrentUsedRate(materialPair.getSecond());
                                    if ((currentRate + rate) > 100) {
                                        LocaleEnum.EDIT_GENERATOR_RATE_LIMIT_EXCEED.getWithErrorPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%material%", beautify(materialPair.getSecond().name())));
                                        open(null);
                                        return;
                                    }

                                    Optional<OPair<Double, OMaterial>> first = mine.getGenerator().getGeneratorMaterials().stream()
                                            .filter(pair -> pair.getSecond() == materialPair.getValue())
                                            .findFirst();
                                    if (first.isPresent()) {
                                        first.get().setFirst(rate);
                                        LocaleEnum.EDIT_GENERATOR_RATE_SET.getWithPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%material%", beautify(materialPair.getSecond().name()), "%rate%", rate + ""));

                                        // Update
                                        SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
                                        refreshMenus(GeneratorEditMenu.class, menu -> menu.mine.getName().contentEquals(mine.getName()));
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
                    }
                })
                .apply(this);
    }

    @Override
    public List<OPair<Double, OMaterial>> requestObjects() {
        return mine.getGenerator().getGeneratorMaterials();
    }

    @Override
    public OMenuButton toButton(OPair<Double, OMaterial> obj) {
        Optional<OMenuButton> material = getTemplateButtonFromTemplate("material");
        if (!material.isPresent()) return null;

        OMenuButton button = material.get();
        OMenuButton.ButtonItemBuilder clone = button.getDefaultStateItem().clone();
        clone.itemBuilder()
                .setMaterial(obj.getSecond().parseMaterial())
                .replaceDisplayName("{material_name}", beautify(obj.getValue().name()))
                .replaceInLore("{material_rate}", obj.getFirst() + "");
        return button.currentItem(clone.getItemStackWithPlaceholdersMulti(getViewer(), mine));
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {
        ItemStack clone = event.getCursor().clone();
        OMaterial material = OMaterial.matchMaterial(clone);
        event.setCancelled(true);

        //TODO: Add messages to locale
        if (!clone.getType().isBlock()) {
            LocaleEnum.EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK.getWithErrorPrefix().send((Player) event.getWhoClicked());
            return;
        }

        if (mine.getGenerator().getGeneratorMaterials().stream().anyMatch(pair -> pair.getSecond() == material)) {
            LocaleEnum.EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS.getWithErrorPrefix().send((Player) event.getWhoClicked());
            return;
        }

        mine.getGenerator().getGeneratorMaterials().add(new OPair<>(0.0, OMaterial.matchMaterial(clone)));
        refreshMenus(GeneratorEditMenu.class, menu -> menu.mine.getName().contentEquals(mine.getName()));
        SuperiorPrisonPlugin.getInstance().getDataController().save(mine, true);
    }
}
