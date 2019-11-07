package com.bgsoftware.superiorprison.api.events;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class MineCreateEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private final Location pos1;
    private final Location pos2;
    private final String name;
    private final Player creator;

    @Setter
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }


    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
