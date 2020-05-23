package com.bgsoftware.superiorprison.plugin.util.input;

import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.Helper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Accessors(chain = true, fluent = true)
public class PlayerInput<T> {

    @Setter
    @Getter
    private boolean commandsEnabled = false;

    @Getter
    private @NonNull
    final Player player;

    @Getter
    @Setter
    private @NonNull BiConsumer<PlayerInput<T>, Throwable> onError;

    @Getter
    @Setter
    private @NonNull Function<String, T> parser;

    @Getter
    @Setter
    private @NonNull BiConsumer<PlayerInput<T>, T> onInput;

    @Setter
    private Runnable onCancel;

    @Getter
    private long timeOut;

    @Getter
    private boolean cancelled;

    private final Set<SubscribedEvent> events = new HashSet<>();

    public PlayerInput(Player player) {
        this.player = player;
    }

    public PlayerInput<T> timeOut(TimeUnit unit, long time) {
        return timeOut(unit.toMillis(time));
    }

    public PlayerInput<T> timeOut(long time) {
        this.timeOut = time;
        return this;
    }

    public void listen() {
        events.add(
                SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, event -> {
                    event.setCancelled(true);
                    if (event.getMessage().equalsIgnoreCase("cancel")) {
                        cancel();
                        if (onCancel != null)
                            onCancel.run();
                        return;
                    }

                    try {
                        T parsed = parser.apply(event.getMessage());
                        onInput.accept(this, parsed);
                    } catch (Throwable ex) {
                        if (onError != null)
                            onError.accept(this, ex);
                        else
                            event.getPlayer().sendMessage(Helper.color("&cError: &7" + ex.getMessage()));
                    }
                }, new SubscriptionProperties<AsyncPlayerChatEvent>().runTill(event -> cancelled).filter(event -> event.getPlayer().equals(player)))
        );

        if (!commandsEnabled)
            events.add(
                    SubscriptionFactory.getInstance().subscribeTo(PlayerCommandPreprocessEvent.class, event -> event.setCancelled(true), new SubscriptionProperties<PlayerCommandPreprocessEvent>().filter(event -> event.getPlayer().equals(player)).runTill(event -> cancelled))
            );
    }

    public void cancel() {
        cancelled = true;
        for (SubscribedEvent event : events) {
            event.end();
        }
    }
}
