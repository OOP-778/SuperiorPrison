package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.shop.MineShop;
import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.orangeengine.main.util.data.set.OSet;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SMineShop implements MineShop, Attachable<SNormalMine>, Serializable {

    private OSet<ShopItem> items = new OSet<>();
    private transient SNormalMine mine;

    @Override
    public OSet<ShopItem> getItems() {
        return items;
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }
}
