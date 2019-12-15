package com.bgsoftware.superiorprison.api.data.mine.shop;

import com.oop.orangeengine.main.util.data.set.OSet;

import java.util.LinkedList;

public interface MineShop {

    <T extends ShopItem> LinkedList<T> getItems();

}
