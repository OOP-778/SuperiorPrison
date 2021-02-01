package com.bgsoftware.superiorprison.plugin.util.menu;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.ClickType;

@Accessors(fluent = true, chain = true)
public class ClickHandler {

  private final Set<ClickType> acceptsTypes = Sets.newHashSet(ClickType.values());
  private String action;
  private Consumer<ButtonClickEvent> consumer;

  public static ClickHandler of(OMenuButton button) {
    ClickHandler clickHandler = new ClickHandler();
    clickHandler.action = button.action();

    return clickHandler;
  }

  public static ClickHandler of(String action) {
    ClickHandler clickHandler = new ClickHandler();
    clickHandler.action = action;

    return clickHandler;
  }

  public ClickHandler apply(OMenu menu) {
    menu.getClickHandlers()
        .put(Objects.requireNonNull(action.toLowerCase(), "Cannot put action as null"), this);
    return this;
  }

  public ClickHandler clearClickTypes() {
    acceptsTypes.clear();
    return this;
  }

  public ClickHandler acceptsClickType(ClickType... type) {
    acceptsTypes.addAll(Arrays.asList(type));
    return this;
  }

  public boolean doesAcceptEvent(ButtonClickEvent event) {
    if (!acceptsTypes.contains(event.getClick())) return false;

    return action == null || event.getButton().action().equalsIgnoreCase(action);
  }

  public ClickHandler handle(Consumer<ButtonClickEvent> consumer) {
    this.consumer = consumer;
    return this;
  }

  public void handle(ButtonClickEvent event) {
    if (consumer != null) consumer.accept(event);
  }
}
