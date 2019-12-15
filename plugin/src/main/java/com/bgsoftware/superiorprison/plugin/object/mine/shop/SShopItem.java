package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Setter
@Getter
public class SShopItem implements ShopItem, GsonUpdateable {

    private ItemStack item;
    private double price;

    private String command;

    private SShopItem() {}

    public SShopItem(ItemStack item, double price) {
        this.item = item;
        this.price = price;
    }

    @Override
    public Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }
}
