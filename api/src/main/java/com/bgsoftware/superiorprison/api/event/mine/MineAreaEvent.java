package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import lombok.Getter;

@Getter
public class MineAreaEvent extends MineEvent {

  private AreaEventType type;
  private Area area;

  public MineAreaEvent(AreaEventType type, Area area, SuperiorMine mine) {
    super(mine);
    this.type = type;
    this.area = area;
  }

  public enum AreaEventType {
    // Called when player switches area for example from mine -> region and from region -> mine
    SWITCH,

    // Called when player enters region of mine by moving
    ENTER,

    // Called when player leaves area
    LEAVE
  }
}
