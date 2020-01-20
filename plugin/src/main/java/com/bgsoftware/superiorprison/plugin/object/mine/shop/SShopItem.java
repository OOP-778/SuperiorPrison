package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Setter
public class SShopItem implements ShopItem, GsonUpdateable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SShopItem shopItem = (SShopItem) o;
        return item.equals(shopItem.item);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Getter
    @SerializedName(value = "item")
    private ItemStack item;

    @SerializedName(value = "sellPrice")
    private double price;

    private SShopItem() {}

    protected SShopItem(@NonNull ItemStack item, double price) {
        this.item = item;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }
}
