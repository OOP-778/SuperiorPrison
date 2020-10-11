package com.bgsoftware.superiorprison.api.event;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class MineEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    private SuperiorMine mine;

    public MineEvent(SuperiorMine mine) {
        super(!Bukkit.isPrimaryThread());
        this.mine = mine;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
