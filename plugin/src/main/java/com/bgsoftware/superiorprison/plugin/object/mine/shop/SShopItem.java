package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.google.gson.annotations.SerializedName;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Setter
@EqualsAndHashCode
public class SShopItem implements ShopItem, SerializableObject {
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
    private ItemStack item;

    @Getter
    private double price;

    private SShopItem() {}

    protected SShopItem(@NonNull ItemStack item, double price) {
        this.item = item;
        this.price = price;
    }

    public static SShopItem from(SShopItem from) {
        SShopItem item = new SShopItem();
        item.setItem(from.getItem().clone());
        item.setPrice(from.getPrice());
        return item;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("item", item);
        serializedData.write("price", price);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.item = serializedData.applyAs("item", ItemStack.class);
        this.price = serializedData.applyAs("price", double.class);
    }
}
