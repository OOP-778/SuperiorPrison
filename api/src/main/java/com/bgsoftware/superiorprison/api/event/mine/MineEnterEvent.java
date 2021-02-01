package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

@Getter
public class MineEnterEvent extends MineEvent implements Cancellable {

  private Prisoner prisoner;

  private Area area;

  @Setter private boolean cancelled;

  public MineEnterEvent(SuperiorMine mine, Prisoner prisoner, Area area) {
    super(mine);
    this.prisoner = prisoner;
    this.area = area;
  }
}
