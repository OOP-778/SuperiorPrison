package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

@Setter
public class SShopItem implements ShopItem, SerializableObject {
    @Getter
    private ItemStack item;

    @Getter
    private BigDecimal price;

    private SShopItem() {
    }

    protected SShopItem(@NonNull ItemStack item, BigDecimal price) {
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

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("item", item);
        serializedData.write("price", price.toString());
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.item = serializedData.applyAs("item", ItemStack.class);
        this.price = new BigDecimal(serializedData.applyAs("price", String.class));
    }
}
