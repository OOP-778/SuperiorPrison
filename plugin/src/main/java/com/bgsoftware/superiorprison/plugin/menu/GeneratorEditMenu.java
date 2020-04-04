package com.bgsoftware.superiorprison.plugin.menu;

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
import lombok.Getter;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautify;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautifyDouble;

public class GeneratorEditMenu extends OPagedMenu<OPair<Double, OMaterial>> implements OMenu.Templateable {

    private List<OPair<Double, OMaterial>> materials;

    @Getter
    private SNormalMine mine;

    public GeneratorEditMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineGenerator", viewer);
        this.mine = mine;
        this.materials = mine.getGenerator().getGeneratorMaterials();

        ClickHandler
                .of("reset")
                .handle(event -> {
                    materials.addAll(SuperiorPrisonPlugin.getInstance().getMainConfig().getMineDefaults().getMaterials());
                    refresh();
                })
                .apply(this);

        ClickHandler
                .of("save")
                .handle(event -> {
                    double percentage = materials
                            .stream()
                            .map(OPair::getFirst)
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    if (percentage < 100 || percentage > 100) {
                        LocaleEnum.EDIT_GENERATOR_SAVE_FAILED_WRONG_PERCENTAGE.getWithErrorPrefix().send((Player) event.getWhoClicked());
                        return;
                    }

                    mine.getGenerator().setGeneratorMaterials(materials);
                    mine.getGenerator().setMaterialsChanged(true);
                    mine.save(true);
                    LocaleEnum.EDIT_GENERATOR_SAVE.getWithPrefix().send((Player) event.getWhoClicked());
                })
                .apply(this);

        ClickHandler
                .of("material")
                .handle(event -> {
                    OPair<Double, OMaterial> materialPair = requestObject(event.getRawSlot());
                    if (event.getClick() == ClickType.RIGHT) {
                        materials.remove(materialPair);
                        refresh();

                    } else if (event.getClick() == ClickType.LEFT) {
                        previousMove = false;
                        event.getWhoClicked().closeInventory();
                        LocaleEnum.EDIT_GENERATOR_WRITE_RATE.getWithPrefix().send((Player) event.getWhoClicked());

                        SubscriptionFactory.getInstance().subscribeTo(
                                AsyncPlayerChatEvent.class,
                                chatEvent -> {
                                    double rate = Double.parseDouble(chatEvent.getMessage());
                                    chatEvent.setCancelled(true);

                                    Optional<OPair<Double, OMaterial>> first = materials.stream()
                                            .filter(pair -> pair.getSecond() == materialPair.getValue())
                                            .findFirst();
                                    if (first.isPresent()) {
                                        first.get().setFirst(rate);
                                        LocaleEnum.EDIT_GENERATOR_RATE_SET.getWithPrefix().send(chatEvent.getPlayer(), ImmutableMap.of("%material%", beautify(materialPair.getSecond().name()), "%rate%", beautifyDouble(rate)));

                                        // Update
                                        refresh();
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
        return materials;
    }

    @Override
    public OMenuButton toButton(OPair<Double, OMaterial> obj) {
        Optional<OMenuButton> material = getTemplateButtonFromTemplate("material");
        if (!material.isPresent()) return null;

        OMenuButton button = material.get().clone();
        OMenuButton.ButtonItemBuilder clone = button.getDefaultStateItem().clone();
        clone.itemBuilder()
                .setMaterial(obj.getSecond().parseMaterial())
                .setDurability(obj.getSecond().getData())
                .replaceDisplayName("{material_name}", beautify(obj.getValue().name()))
                .replaceInLore("{material_rate}", beautifyDouble(obj.getFirst()));
        return button.currentItem(clone.getItemStackWithPlaceholdersMulti(getViewer(), mine));
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public void handleBottomClick(InventoryClickEvent event) {
        ItemStack clone = event.getCurrentItem().clone();
        OMaterial material = OMaterial.matchMaterial(clone);
        event.setCancelled(true);

        //TODO: Add messages to locale
        if (!clone.getType().isBlock()) {
            LocaleEnum.EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK.getWithErrorPrefix().send((Player) event.getWhoClicked());
            return;
        }

        if (materials.stream().anyMatch(pair -> pair.getSecond() == material)) {
            LocaleEnum.EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS.getWithErrorPrefix().send((Player) event.getWhoClicked());
            return;
        }

        materials.add(new OPair<>(0.0, OMaterial.matchMaterial(clone)));
        refreshMenus(GeneratorEditMenu.class, menu -> menu.mine.getName().contentEquals(mine.getName()));
        mine.save(true);
        mine.getGenerator().setMaterialsChanged(true);
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine};
    }
}
