package com.bgsoftware.superiorprison.plugin.enums;

public enum MenuNames {

    MINE_EDIT("mine edit menu");

    private String id;
    MenuNames(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}