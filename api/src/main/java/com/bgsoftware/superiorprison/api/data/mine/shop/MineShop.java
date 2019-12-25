package com.bgsoftware.superiorprison.api.data.mine.shop;

import java.util.LinkedList;

public interface MineShop {

    <T extends ShopItem> LinkedList<T> getItems();

}
