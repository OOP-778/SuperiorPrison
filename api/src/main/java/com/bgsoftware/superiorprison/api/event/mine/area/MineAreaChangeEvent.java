package com.bgsoftware.superiorprison.api.event.mine.area;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

@Getter
public class MineAreaChangeEvent extends MineEvent implements Cancellable {

  private Prisoner prisoner;
  private AreaEnum from;
  private AreaEnum to;

  @Setter private boolean cancelled = false;

  public MineAreaChangeEvent(Prisoner prisoner, AreaEnum from, AreaEnum to, SuperiorMine mine) {
    super(mine);

    this.prisoner = prisoner;
    this.from = from;
    this.to = to;
  }
}
