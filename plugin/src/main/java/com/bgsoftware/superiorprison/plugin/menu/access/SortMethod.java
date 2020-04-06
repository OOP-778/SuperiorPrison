package com.bgsoftware.superiorprison.plugin.menu.access;

public enum SortMethod {
    ABC(0),
    LADDER_FIRST(1),
    SPECIAL_FIRST(2),
    PRESTIGE_FIRST(3),
    INPUT(4);

    private int order;

    SortMethod(int order) {
        this.order = order;
    }

    public SortMethod getNext() {
        SortMethod method = getByOrder(order + 1);
        if (method == null)
            method = getByOrder(1);

        return method;
    }

    public SortMethod getPrevious() {
        SortMethod method = getByOrder(order - 1);
        if (method == null)
            method = getByOrder(3);

        return method;
    }

    public static SortMethod getByOrder(int order) {
        for (SortMethod method : values())
            if (method.order == order) return method;

        return null;
    }
}
