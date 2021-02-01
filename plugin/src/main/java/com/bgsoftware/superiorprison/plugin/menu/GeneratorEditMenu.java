package com.bgsoftware.superiorprison.plugin.menu;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautify;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.beautifyNumber;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.input.Input;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.google.common.collect.ImmutableMap;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GeneratorEditMenu extends OPagedMenu<OPair<Double, OMaterial>>
    implements OMenu.Templateable {

  private final List<OPair<Double, OMaterial>> materials;

  @Getter private final SNormalMine mine;

  public GeneratorEditMenu(SPrisoner viewer, SNormalMine mine) {
    super("mineGenerator", viewer);
    this.mine = mine;
    this.materials = new ArrayList<>(mine.getGenerator().getGeneratorMaterials());

    ClickHandler.of("reset")
        .handle(
            event -> {
              materials.clear();
              materials.addAll(
                  SuperiorPrisonPlugin.getInstance()
                      .getMainConfig()
                      .getMineDefaults()
                      .getMaterials());
              refresh();
            })
        .apply(this);

    ClickHandler.of("save")
        .handle(
            event -> {
              mine.getGenerator().setGeneratorMaterials(materials);
              mine.getGenerator().setMaterialsChanged(true);
              mine.save(true);
              mine.getLinker().call(mine.getGenerator());
              LocaleEnum.EDIT_GENERATOR_SAVE.getWithPrefix().send(event.getWhoClicked());
            })
        .apply(this);

    ClickHandler.of("material")
        .handle(
            event -> {
              OPair<Double, OMaterial> materialPair = requestObject(event.getRawSlot());
              if (event.getClick() == ClickType.RIGHT) {
                materials.remove(materialPair);
                refresh();

              } else if (event.getClick() == ClickType.LEFT) {
                forceClose();
                LocaleEnum.EDIT_GENERATOR_WRITE_RATE.getWithPrefix().send(event.getWhoClicked());

                Runnable onCancel = this::open;
                Input.doubleInput(event.getWhoClicked())
                    .timeOut(TimeUnit.MINUTES, 2)
                    .onCancel(onCancel)
                    .onInput(
                        (obj, input) -> {
                          Optional<OPair<Double, OMaterial>> first =
                              materials.stream()
                                  .filter(pair -> pair.getSecond() == materialPair.getValue())
                                  .findFirst();

                          if (first.isPresent()) {
                            first.get().setFirst(input);
                            LocaleEnum.EDIT_GENERATOR_RATE_SET
                                .getWithPrefix()
                                .send(
                                    ImmutableMap.of(
                                        "{material}",
                                        beautify(materialPair.getSecond().name()),
                                        "{rate}",
                                        beautifyNumber(input)),
                                    obj.player());
                          }
                          obj.cancel();
                        })
                    .listen();
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
    clone
        .itemBuilder()
        .setMaterial(obj.getSecond().parseMaterial())
        .setDurability(obj.getSecond().getData())
        .replaceDisplayName("{material_name}", beautify(obj.getValue().name()))
        .replaceInLore("{material_rate}", beautifyNumber(obj.getFirst()));
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

    if (!clone.getType().isBlock()) {
      LocaleEnum.EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK
          .getWithErrorPrefix()
          .send(event.getWhoClicked());
      return;
    }

    if (materials.stream().anyMatch(pair -> pair.getSecond() == material)) {
      LocaleEnum.EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS
          .getWithErrorPrefix()
          .send(event.getWhoClicked());
      return;
    }

    materials.add(new OPair<>(0.0, OMaterial.matchMaterial(clone)));
    refreshMenus(
        GeneratorEditMenu.class, menu -> menu.mine.getName().contentEquals(mine.getName()));
    mine.save(true);
    mine.getGenerator().setMaterialsChanged(true);
  }

  @Override
  public Object[] getBuildPlaceholders() {
    return new Object[] {mine};
  }
}
