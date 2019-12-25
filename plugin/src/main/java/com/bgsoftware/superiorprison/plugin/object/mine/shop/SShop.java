package com.bgsoftware.superiorprison.plugin.object.mine.shop;

import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.main.util.data.list.OLinkedList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

@EqualsAndHashCode
@Getter
public class SShop implements MineShop, Attachable<SNormalMine>, GsonUpdateable {

    private transient SNormalMine mine;
    private LinkedList<SShopItem> items = new OLinkedList<>();
    @Setter
    private String title = "Undefined Title";

    public SShop() {
    }

    @Override
    public <T extends ShopItem> LinkedList<T> getItems() {
        return (LinkedList<T>) items;
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }
}
