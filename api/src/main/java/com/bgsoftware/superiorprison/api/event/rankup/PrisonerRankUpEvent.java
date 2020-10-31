package com.bgsoftware.superiorprison.api.event.rankup;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.PrisonerEvent;
import lombok.Getter;

@Getter
public class PrisonerRankUpEvent extends PrisonerEvent {

    private LadderObject previousRank;
    private LadderObject newRank;

    public PrisonerRankUpEvent(Prisoner prisoner, LadderObject previousRank, LadderObject newRank) {
        super(prisoner);
        this.previousRank = previousRank;
        this.newRank = newRank;
    }
}
