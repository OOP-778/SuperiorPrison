package com.bgsoftware.superiorprison.api.events;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import lombok.Getter;

@Getter
public class MineEnterEvent extends MineEvent {

    private Prisoner prisoner;

    public MineEnterEvent(SuperiorMine mine, Prisoner prisoner) {
        super(mine);
        this.prisoner = prisoner;
    }
}
