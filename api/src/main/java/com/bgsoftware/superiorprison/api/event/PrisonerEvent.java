package com.bgsoftware.superiorprison.api.event;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public abstract class PrisonerEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Prisoner prisoner;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
