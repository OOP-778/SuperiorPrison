package com.bgsoftware.superiorprison.api.event;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItemWrapped;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/*
Called on every item that's being auto sold!
*/
public class AutoSellEvent extends PrisonerEvent implements Cancellable {

    @Getter
    private ItemStack itemStack;

    @Getter
    private ShopItemWrapped shopItem;

    private boolean cancelled = false;

    public AutoSellEvent(Prisoner prisoner, ItemStack itemStack, ShopItemWrapped shopItem) {
        super(prisoner);
        this.itemStack = itemStack;
        this.shopItem = shopItem;
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
