package com.bgsoftware.superiorprison.api.event.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import lombok.Getter;

@Getter
public class MineAreaEnterEvent extends MineEvent {

    private final Prisoner prisoner;
    private final Area area;
    private final AreaEnum areaEnum;

    public MineAreaEnterEvent(Prisoner prisoner, Area area, AreaEnum areaEnum, SuperiorMine mine) {
        super(mine);

        this.prisoner = prisoner;
        this.area = area;
        this.areaEnum = areaEnum;
    }
}