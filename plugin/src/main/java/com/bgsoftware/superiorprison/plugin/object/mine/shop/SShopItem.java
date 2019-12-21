package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.menu.config.button.AConfigButton;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@Setter
@Getter
public class SShopItem implements ShopItem, GsonUpdateable {

    @SerializedName(value = "item")
    private ItemStack item;

    @SerializedName(value = "sellPrice")
    private double sellPrice;

    @SerializedName(value = "buyPrice")
    private double buyPrice;

    @SerializedName(value = "command")
    private String command;

    private SShopItem() {
        registerFieldSupplier("sellPrice", int.class, () -> 0);
        registerFieldSupplier("buyPrice", int.class, () -> 0);
    }

    public SShopItem(ItemStack item, double sellPrice, double buyPrice) {
        this.item = item;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }

    @Override
    public Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }
}
