package com.bgsoftware.superiorprison.plugin.menu.access;

import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccessEditMenu extends AccessMenu {

    private SNormalMine mine;

    public AccessEditMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineAccess", viewer);
        this.mine = mine;

        ClickHandler
                .of("access click")
                .handle(event -> {
                    AccessObject accessObject = requestObject(event.getRawSlot());
                    if (accessObject.isRank()) {
                        SRank rank = accessObject.getAs();
                        List<String> ranks = new ArrayList<>();

                        if (event.isShiftClick() && rank instanceof SLadderRank)
                            ((SLadderRank) rank).getAllPrevious().forEach(rank2 -> ranks.add(rank2.getName()));

                        ranks.add(rank.getName());
                        mine.removeRank(ranks.toArray(new String[0]));

                    } else
                        mine.removePrestige(accessObject.getName());

                    refreshMenus(AccessEditMenu.class, menu -> menu.mine.equals(mine));
                    mine.save(true);
                })
                .apply(this);

        ClickHandler
                .of("find access")
                .handle(event -> {
                    previousMove = false;
                    new FindAccessMenu(getViewer(), mine).open(this);
                })
                .apply(this);
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine, getViewer(), getSortMethod()};
    }

    @Override
    public List<AccessObject> requestObjects() {
        List<AccessObject> accessObjects = new ArrayList<>();
        mine.getRanksMapped().stream().map(AccessObject::new).forEach(accessObjects::add);
        mine.getPrestigesMapped().stream().map(AccessObject::new).forEach(accessObjects::add);
        return sorted(accessObjects.stream());
    }

    @Override
    public OMenuButton toButton(AccessObject obj) {
        Optional<OMenuButton> accessTemplate = getTemplateButtonFromTemplate("access template");
        if (!accessTemplate.isPresent()) return null;

        OMenuButton button = accessTemplate.get().clone();
        return button.currentItem(button.getDefaultStateItem().getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
    }
}
