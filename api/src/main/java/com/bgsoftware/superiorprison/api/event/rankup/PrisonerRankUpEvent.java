package com.bgsoftware.superiorprison.api.event.rankup;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.data.player.rank.LadderRank;
import com.bgsoftware.superiorprison.api.event.PrisonerEvent;
import lombok.Getter;

@Getter
public class PrisonerRankUpEvent extends PrisonerEvent {

    private LadderRank previousRank;
    private LadderRank newRank;

    public PrisonerRankUpEvent(Prisoner prisoner, LadderRank previousRank, LadderRank newRank) {
        super(prisoner);
        this.previousRank = previousRank;
        this.newRank = newRank;
    }
}
