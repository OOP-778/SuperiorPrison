package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.api.SerializableObject;
import com.oop.datamodule.api.SerializedData;
import com.oop.orangeengine.main.util.data.set.OConcurrentSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode
@Getter
public class SShop implements MineShop, Attachable<SNormalMine>, SerializableObject, LinkableObject<SShop> {

    private final Set<SShopItem> items = new OConcurrentSet<>();
    private transient SNormalMine mine;

    public SShop() {
    }

    public static SShop from(SShop from) {
        SShop shop = new SShop();
        from.items
                .stream()
                .map(SShopItem::from)
                .forEach(item -> shop.items.add(item));

        return shop;
    }

    @Override
    public <T extends ShopItem> Set<T> getItems() {
        return new HashSet<>((Set<T>) items);
    }

    @Override
    public void addItem(ItemStack itemStack, BigDecimal price) {
        if (itemStack.getType() == Material.AIR) return;

        SShopItem shopItem = new SShopItem(itemStack, price);
        addItem(shopItem);
    }

    public void addItem(SShopItem item) {
        items.add(item);
        mine.getLinker().call(this);
    }

    @Override
    public void removeItem(ShopItem item) {
        items.remove(item);
        mine.getLinker().call(this);
    }

    @Override
    public BigDecimal getPrice(ItemStack itemStack) {
        return getItems()
                .stream()
                .filter(shopItem -> shopItem.getItem().isSimilar(itemStack))
                .findFirst().map(ShopItem::getPrice)
                .orElse(new BigDecimal(0));
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

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("items", items);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData.applyAsCollection("items")
                .map(sd -> sd.applyAs(SShopItem.class))
                .forEach(items::add);
    }

    @Override
    public void onChange(SShop from) {
        this.items.clear();
        this.items.addAll(from.items);
    }

    @Override
    public String getLinkId() {
        return "shop";
    }
}
