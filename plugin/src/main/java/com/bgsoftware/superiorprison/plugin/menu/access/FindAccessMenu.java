package com.bgsoftware.superiorprison.plugin.menu.access;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FindAccessMenu extends AccessMenu {
    private SNormalMine mine;

    public FindAccessMenu(SPrisoner viewer, SNormalMine mine) {
        super("findAccess", viewer);
        this.mine = mine;

        ClickHandler
                .of("access click")
                .handle(event -> {
                    AccessObject accessObject = requestObject(event.getRawSlot());
                    if (accessObject.isRank()) {
                        SRank rank = accessObject.getAs();
                        if (event.isShiftClick() && rank instanceof SLadderRank)
                            ((SLadderRank) rank).getAllPrevious().forEach(mine::addRank);
                        mine.addRank(rank);

                    } else
                        mine.addPrestige(accessObject.getAs(Prestige.class));

                    mine.save(true);
                    refreshMenus(FindAccessMenu.class, menu -> menu.mine.equals(mine));
                })
                .apply(this);
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public List<AccessObject> requestObjects() {
        List<AccessObject> accessObjects = new ArrayList<>();
        SuperiorPrisonPlugin.getInstance().getRankController().getRanks()
                .stream()
                .filter(rank -> !mine.getRanks().contains(rank.getName()))
                .map(AccessObject::new)
                .forEach(accessObjects::add);

        SuperiorPrisonPlugin.getInstance().getPrestigeController().getPrestiges()
                .stream()
                .filter(prestige -> !mine.getPrestiges().contains(prestige.getName()))
                .map(AccessObject::new)
                .forEach(accessObjects::add);


        return sorted(accessObjects
                .stream()
                .sorted(Comparator.comparing(AccessObject::getName))
        );
    }

    @Override
    public OMenuButton toButton(AccessObject obj) {
        Optional<OMenuButton> accessTemplate = getTemplateButtonFromTemplate("access template");
        if (!accessTemplate.isPresent()) return null;

        OMenuButton button = accessTemplate.get().clone();
        OMenuButton.ButtonItemBuilder defaultStateItem = button.getDefaultStateItem();
        return button.currentItem(defaultStateItem.getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine, getViewer(), getSortMethod()};
    }
}
