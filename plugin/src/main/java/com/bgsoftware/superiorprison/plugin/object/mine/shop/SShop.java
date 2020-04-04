package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@EqualsAndHashCode
@Getter
public class SShop implements MineShop, Attachable<SNormalMine> {

    private transient SNormalMine mine;

    @SerializedName(value = "items")
    private Set<SShopItem> items = Sets.newConcurrentHashSet();

    public SShop() {}

    @Override
    public <T extends ShopItem> Set<T> getItems() {
        return (Set<T>) items;
    }

    @Override
    public void addItem(ItemStack itemStack, double price) {
        SShopItem shopItem = new SShopItem(itemStack, price);
        if (!items.contains(shopItem))
            items.add(shopItem);
    }

    @Override
    public void removeItem(ShopItem item) {
        items.remove(item);
    }

    @Override
    public double getPrice(ItemStack itemStack) {
        return getItems()
                .stream()
                .filter(shopItem -> shopItem.getItem().isSimilar(itemStack))
                .findFirst().map(ShopItem::getPrice)
                .orElse(0.0);
    }

    @Override
    public boolean hasItem(ItemStack itemStack) {
        return getItems()
                .stream()
                .anyMatch(shopItem -> shopItem.getItem().isSimilar(itemStack));
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }
}
