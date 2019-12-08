package com.bgsoftware.superiorprison.plugin.constant;

public enum MenuNames {

    MINE_EDIT("mine edit menu"),
    MINE_EDIT_GENERATOR("edit generator");

    private String id;
    MenuNames(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
