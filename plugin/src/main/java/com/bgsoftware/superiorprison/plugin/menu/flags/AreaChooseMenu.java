package com.bgsoftware.superiorprison.plugin.menu.flags;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.area.SArea;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AreaChooseMenu extends OPagedMenu<SArea> implements OMenu.Templateable {
  private final SNormalMine mine;

  public AreaChooseMenu(SPrisoner viewer, SNormalMine mine) {
    super("mineAreaChoose", viewer);
    this.mine = mine;

    ClickHandler.of("area click")
        .handle(
            event -> {
              SArea sArea = requestObject(event.getRawSlot());
              move(new FlagsEditMenu(getViewer(), sArea));
            })
        .apply(this);
  }

  @Override
  public List<SArea> requestObjects() {
    return new ArrayList<>(mine.getAreas().values());
  }

  @Override
  public OMenuButton toButton(SArea obj) {
    Optional<OMenuButton> areaTemplate = getTemplateButtonFromTemplate("area template");
    if (!areaTemplate.isPresent()) return null;

    OMenuButton button = areaTemplate.get().clone();
    return button.currentItem(
        button.getDefaultStateItem().getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
  }

  @Override
  public OMenu getMenu() {
    return this;
  }
}
