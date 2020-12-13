package com.bgsoftware.superiorprison.plugin.util.chatCmds;

import com.bgsoftware.superiorprison.plugin.util.input.PlayerInput;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class ChatCommands {
    // command label = command
    private final Map<String, CommandHandler> commandMap = Maps.newHashMap();

    @Setter
    private BiConsumer<Player, Throwable> exceptionHandler;
    private final Player player;
    private Runnable finish;

    private Runnable afterInput;

    private final AtomicBoolean canceller = new AtomicBoolean(false);

    public ChatCommands(Player player) {
        this.player = player;
    }

    public void appendCommand(String label, BiConsumer<Player, String[]> handler) {
        this.commandMap.put(label, (player, cancelled, args) -> handler.accept(player, args));
    }

    public void appendCommand(String label, CommandHandler handler) {
        this.commandMap.put(label, handler);
    }

    public void onFinish(Runnable finish) {
        this.finish = finish;
    }

    public void afterInput(Runnable runnable) {
        this.afterInput = runnable;
    }

    private PlayerInput<String> input;

    public void listen() {
        input = new PlayerInput<String>(player)
                .timeOut(TimeUnit.MINUTES, 4)
                .commandsEnabled(false)
                .parser(in -> in)
                .onError((b, t) -> {
                    if (exceptionHandler == null)
                        player.sendMessage(Helper.color("&c&lERROR: &4" + t.getMessage()));

                    else
                        exceptionHandler.accept(player, t);
                })
                .onInput((o, in) -> handle(in));
        input.listen();
    }

    private void handle(String message) {
        if (commandMap.isEmpty()) return;

        message = ChatColor.stripColor(message);

        // Try to find labels
        String finalMessage = message;
        Optional<String> first = commandMap.keySet()
                .stream()
                .filter(label -> finalMessage.toLowerCase().startsWith(label.toLowerCase()))
                .findFirst();

        if (!first.isPresent()) {
            try {
                throw new IllegalStateException("Failed to find command by " + message);
            } catch (Throwable th) {
                if (exceptionHandler == null)
                    player.sendMessage(Helper.color("&c&lERROR: &4" + th.getMessage()));

                else
                    exceptionHandler.accept(player, th);
            }
            return;
        }

        CommandHandler cmd = commandMap.get(first.get());
        String messageNoLabel = removeLabel(first.get(), message);
        String[] args = messageNoLabel.split("\\s+");

        try {
            cmd.handle(player, canceller, args);
        } catch (Throwable th) {
            if (th instanceof NullPointerException)
                th.printStackTrace();

            if (exceptionHandler == null)
                player.sendMessage(Helper.color("&c&lERROR: &4" + th.getMessage()));

            else
                exceptionHandler.accept(player, th);
        }

        if (canceller.get()) {
            input.cancel();
            if (finish != null)
                finish.run();
        } else if (afterInput != null)
            afterInput.run();
    }

    private String removeLabel(String label, String message) {
        return message.length() == label.length() ? "" : message.substring(label.length() + 1);
    }

    @FunctionalInterface
    public interface CommandHandler {
        void handle(Player player, AtomicBoolean canceller, String[] args);
    }
}
