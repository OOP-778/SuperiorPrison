package com.bgsoftware.superiorprison.plugin.util.menu;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class ClickHandler {

    private String action;
    private Set<ClickType> acceptsTypes = Sets.newHashSet(ClickType.values());

    private Consumer<InventoryClickEvent> consumer;

    private ClickHandler() {}

    public static ClickHandler of(OMenuButton button) {
        ClickHandler clickHandler = new ClickHandler();
        clickHandler.action = button.getAction();

        return clickHandler;
    }

    public static ClickHandler of(String action) {
        ClickHandler clickHandler = new ClickHandler();
        clickHandler.action = action;

        return clickHandler;
    }

    public void apply(OMenu menu) {
        menu.getClickHandlers().put(Objects.requireNonNull(action, "Cannot put action as null"), this);
    }

    public ClickHandler clearClickTypes() {
        acceptsTypes.clear();
        return this;
    }

    public ClickHandler acceptsClickType(ClickType ...type) {
        acceptsTypes.addAll(Arrays.asList(type));
        return this;
    }

    public boolean doesAcceptEvent(InventoryClickEvent event) {
        if (!acceptsTypes.contains(event.getClick()))
            return false;

        return true;
    }

    public ClickHandler handle(Consumer<InventoryClickEvent> consumer) {
        this.consumer = consumer;
        return this;
    }

}
