package com.bgsoftware.superiorprison.api.event.rankup;

import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;

@Getter
@RequiredArgsConstructor
public class PrisonerLadderRankupEvent implements Cancellable {

    private final Prisoner prisoner;
    private final LadderRank currentRank;
    private final LadderRank nextRank;
    private final RankupMethod method;
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
