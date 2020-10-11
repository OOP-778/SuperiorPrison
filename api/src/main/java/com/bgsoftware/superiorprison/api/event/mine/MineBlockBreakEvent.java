package com.bgsoftware.superiorprison.api.event.mine;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.api.event.MineEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;

@Getter
public class MineBlockBreakEvent extends MineEvent implements Cancellable {

    @Setter
    private boolean cancelled;

    private Prisoner prisoner;
    private Block block;

    public MineBlockBreakEvent(SuperiorMine mine, Prisoner prisoner, Block block) {
        super(mine);
        this.prisoner = prisoner;
        this.block = block;
    }
}
