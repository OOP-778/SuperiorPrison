package com.bgsoftware.superiorprison.api.event.rankup;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.PrisonerEvent;
import lombok.Getter;

@Getter
public class PrisonerPrestigeUpEvent extends PrisonerEvent {

    private LadderObject previousPrestige;
    private LadderObject newPrestige;

    public PrisonerPrestigeUpEvent(Prisoner prisoner, LadderObject previousPrestige, LadderObject newPrestige) {
        super(prisoner);
        this.previousPrestige = previousPrestige;
        this.newPrestige = newPrestige;
    }
}
