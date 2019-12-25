package com.bgsoftware.superiorprison.plugin.constant;

public enum MenuNames {

    MINE_EDIT("mine edit menu"),
    MINE_EDIT_GENERATOR("edit generator"),
    MINE_EDIT_SHOP("edit shop"),
    MINE_EDIT_SHOP_ITEM_EDIT("item edit");

    private String id;

    MenuNames(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
