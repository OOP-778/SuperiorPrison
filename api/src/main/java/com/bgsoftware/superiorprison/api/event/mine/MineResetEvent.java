package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.event.MineEvent;

public class MineResetEvent extends MineEvent {
    public MineResetEvent(SuperiorMine mine) {
        super(mine);
    }
}
