package com.bgsoftware.superiorprison.plugin.object.mine.linkable;

public interface LinkableObject<T extends LinkableObject<T>> {
    void onChange(T from);

    String getLinkId();
}
