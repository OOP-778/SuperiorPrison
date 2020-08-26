package com.bgsoftware.superiorprison.plugin.util.input.multi;

import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.eventssubscription.subscription.SubscribedEvent;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Accessors(chain = true, fluent = true)
public class MultiPlayerInput {

    @Setter
    @Getter
    private boolean commandsEnabled = false;

    @Getter
    private @NonNull
    final Player player;

    @Getter
    @Setter
    private @NonNull BiConsumer<MultiPlayerInput, Throwable> onError;

    @Getter
    @Setter
    private @NonNull MultiInputCompletion onInput;

    @Setter
    private Runnable onCancel;

    @Getter
    private long timeOut;

    @Getter
    private boolean cancelled;

    private final Set<SubscribedEvent> events = new HashSet<>();
    private Queue<InputData> queue = new ConcurrentLinkedDeque<>();
    private Map<String, Object> parsedData = new HashMap<>();

    public MultiPlayerInput(Player player) {
        this.player = player;
    }

    public MultiPlayerInput timeOut(TimeUnit unit, long time) {
        return timeOut(unit.toMillis(time));
    }

    public MultiPlayerInput timeOut(long time) {
        this.timeOut = time;
        return this;
    }

    public <T> MultiPlayerInput add(InputData<T> data) {
        queue.add(data);
        return this;
    }

    public void listen() {
        InputData peek = queue.peek();
        if (peek != null && peek.requestMessage != null)
            peek.requestMessage.send(player);

        events.add(
                SubscriptionFactory.getInstance().subscribeTo(AsyncPlayerChatEvent.class, event -> {
                    event.setCancelled(true);
                    if (event.getMessage().equalsIgnoreCase("cancel")) {
                        cancel();
                        return;
                    }

                    try {
                        if (queue.isEmpty()) return;

                        InputData poll = queue.poll();
                        Object apply = poll.parser.apply(event.getMessage());
                        parsedData.put(poll.id, apply);

                        if (queue.isEmpty()) {
                            onInput.complete(player, new MultiInputData(parsedData));
                            cancel();

                        } else {
                            InputData peek1 = queue.peek();
                            if (peek1 != null && peek1.requestMessage != null)
                                peek1.requestMessage.send(player);
                        }
                    } catch (Throwable ex) {
                        if (onError != null)
                            onError.accept(this, ex);
                        else {
                            event.getPlayer().sendMessage(Helper.color("&cError: &7" + ex.getMessage()));
                            if (ex instanceof NullPointerException)
                                ex.printStackTrace();
                        }
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
        for (SubscribedEvent event : events)
            event.end();

        if (onCancel != null)
            onCancel.run();
    }

    @Accessors(fluent = true, chain = true)
    @Setter
    public static class InputData<T> {
        private String id;
        private Function<String, T> parser;
        private OMessage requestMessage;

        public static InputData<Integer> integer() {
            return new InputData<Integer>()
                    .parser(string -> {
                        try {
                            return Integer.parseInt(string);
                        } catch (Throwable throwable) {
                            throw new IllegalStateException("Invalid number: " + string);
                        }
                    });
        }

        public static InputData<String> string() {
            return new InputData<String>()
                    .parser(in -> in);
        }
    }
}
