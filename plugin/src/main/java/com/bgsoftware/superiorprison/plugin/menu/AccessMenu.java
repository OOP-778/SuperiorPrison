package com.bgsoftware.superiorprison.plugin.menu;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.access.MineCondition;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;
import com.bgsoftware.superiorprison.plugin.util.menu.OPagedMenu;
import com.oop.orangeengine.item.custom.OItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccessMenu extends OPagedMenu<MineCondition> implements OMenu.Templateable {
    
    private SNormalMine mine;
    public AccessMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineAccessList", viewer);
        this.mine = mine;
    }

    @Override
    public List<MineCondition> requestObjects() {
        return new ArrayList<>(mine.getAccess().getConditions());
    }

    @Override
    public OMenuButton toButton(MineCondition obj) {
        Optional<OMenuButton> condition = getTemplateButtonFromTemplate("condition");
        if (!condition.isPresent()) return null;

        OMenuButton conditionButton = condition.get().clone();
        OMenuButton.ButtonItemBuilder defaultStateItem = conditionButton.getDefaultStateItem();

        return conditionButton.currentItem(new OItem(defaultStateItem.getItemStackWithPlaceholders(obj)).getItemStack());
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine};
    }
}
