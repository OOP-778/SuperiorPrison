package com.bgsoftware.superiorprison.api.event;

import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/*
Called on each item when it's being picked up!
*/

public class AutoPickupEvent extends PrisonerEvent implements Cancellable {

    @Getter @Setter
    private ItemStack item;

    @Getter @Setter
    private Prisoner prisoner;

    private boolean cancelled = false;

    public AutoPickupEvent(Prisoner prisoner, ItemStack itemStack) {
        super(prisoner);
        this.item = itemStack;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
