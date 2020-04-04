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
import java.util.Objects;
import java.util.Optional;

public class RanksEditMenu extends RanksMenu {

    private SNormalMine mine;

    public RanksEditMenu(SPrisoner viewer, SNormalMine mine) {
        super("mineRanks", viewer);
        this.mine = mine;

        ClickHandler
                .of("rank click")
                .handle(event -> {
                    SRank rank = requestObject(event.getRawSlot());

                    if (rank instanceof SLadderRank)
                        ((SLadderRank) rank).getAllNext().forEach(rank2 -> mine.getRanks().remove(rank2.getName()));

                    mine.getRanks().remove(rank.getName());

                    refreshMenus(RanksEditMenu.class, menu -> menu.mine.equals(mine));
                    mine.save(true);
                })
                .apply(this);

        ClickHandler
                .of("find ranks")
                .handle(event -> {
                    previousMove = false;
                    new FindRanksMenu(getViewer(), mine).open(this);
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
    public List<SRank> requestObjects() {
        return sorted(mine.getRanks()
                .stream()
                .map(rankName -> SuperiorPrisonPlugin.getInstance().getRankController().getRank(rankName).orElse(null))
                .filter(Objects::nonNull)
                .map(rank -> (SRank) rank));
    }

    @Override
    public OMenuButton toButton(SRank obj) {
        Optional<OMenuButton> rankTemplate = getTemplateButtonFromTemplate("rank template");
        if (!rankTemplate.isPresent()) return null;

        OMenuButton button = rankTemplate.get().clone();
        return button.currentItem(button.getDefaultStateItem().getItemStackWithPlaceholdersMulti(getViewer(), mine, obj));
    }
}
