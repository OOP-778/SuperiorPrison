package com.bgsoftware.superiorprison.plugin.menu.ranks;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SLadderRank;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.object.player.rank.SRank;
import com.bgsoftware.superiorprison.plugin.util.menu.ClickHandler;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenu;
import com.bgsoftware.superiorprison.plugin.util.menu.OMenuButton;

import java.util.List;
import java.util.Optional;

public class FindRanksMenu extends RanksMenu {
    private SNormalMine mine;

    public FindRanksMenu(SPrisoner viewer, SNormalMine mine) {
        super("findRanks", viewer);
        this.mine = mine;

        ClickHandler
                .of("rank click")
                .handle(event -> {
                    SRank rank = requestObject(event.getRawSlot());
                    if (rank instanceof SLadderRank)
                        ((SLadderRank) rank).getAllPrevious().forEach(rank2 -> mine.getRanks().add(rank2.getName()));

                    mine.getRanks().add(rank.getName());
                    mine.save(true);
                    refreshMenus(FindRanksMenu.class, menu -> menu.mine.equals(mine));
                })
                .apply(this);
    }

    @Override
    public OMenu getMenu() {
        return this;
    }

    @Override
    public List<SRank> requestObjects() {
        return sorted(SuperiorPrisonPlugin.getInstance().getRankController().getRanks()
                .stream()
                .filter(rank -> !mine.getRanks().contains(rank.getName()))
                .map(rank -> (SRank) rank));
    }

    @Override
    public OMenuButton toButton(SRank obj) {
        Optional<OMenuButton> rankTemplate = getTemplateButtonFromTemplate("rank template");
        if (!rankTemplate.isPresent()) return null;

        OMenuButton button = rankTemplate.get().clone();
        OMenuButton.ButtonItemBuilder defaultStateItem = button.getDefaultStateItem();
        return button.currentItem(defaultStateItem.getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
    }

    @Override
    public Object[] getBuildPlaceholders() {
        return new Object[]{mine, getViewer(), getSortMethod()};
    }
}
