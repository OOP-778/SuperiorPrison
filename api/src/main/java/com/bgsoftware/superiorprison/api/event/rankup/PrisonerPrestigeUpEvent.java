package com.bgsoftware.superiorprison.api.event.rankup;

import com.bgsoftware.superiorprison.api.data.player.Prestige;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.PrisonerEvent;
import lombok.Getter;

@Getter
public class PrisonerPrestigeUpEvent extends PrisonerEvent {

    private Prestige previousPrestige;
    private Prestige newPrestige;

    public PrisonerPrestigeUpEvent(Prisoner prisoner, Prestige previousPrestige, Prestige newPrestige) {
        super(prisoner);
        this.previousPrestige = previousPrestige;
        this.newPrestige = newPrestige;
    }
}
