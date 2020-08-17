package com.bgsoftware.superiorprison.plugin.util.chatCmds;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ChatCommands {

    // command label = command
    private final Map<String, BiConsumer<Player, String[]>> commandMap = Maps.newHashMap();

    @Setter
    private BiConsumer<Player, Throwable> exceptionHandler;

    public ChatCommands appendCommand(String label, BiConsumer<Player, String[]> handler) {
        this.commandMap.put(label, handler);
        return this;
    }

    public void handle(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (commandMap.isEmpty()) return;

        String message = event.getMessage();

        // Try to find labels
        Optional<String> first = commandMap.keySet()
                .stream()
                .filter(label -> message.toLowerCase().startsWith(label.toLowerCase()))
                .findFirst();

        if (!first.isPresent()) {
            try {
                throw new IllegalStateException("Failed to find command by " + message);
            } catch (Throwable th) {
                if (exceptionHandler == null)
                    event.getPlayer().sendMessage(Helper.color("&c&lERROR: &4" + th.getMessage()));

                else
                    exceptionHandler.accept(event.getPlayer(), th);
            }
            return;
        }

        BiConsumer<Player, String[]> cmd = commandMap.get(first.get());
        String messageNoLabel = removeLabel(first.get(), message);
        String[] args = messageNoLabel.split("\\s+");

        try {
            cmd.accept(event.getPlayer(), args);
        } catch (Throwable th) {
            if (th instanceof NullPointerException)
                th.printStackTrace();

            if (exceptionHandler == null)
                event.getPlayer().sendMessage(Helper.color("&c&lERROR: &4" + th.getMessage()));

            else
                exceptionHandler.accept(event.getPlayer(), th);
        }
    }

    private String removeLabel(String label, String message) {
        return message.length() == label.length() ? "" : message.substring(label.length() + 1);
    }
}
