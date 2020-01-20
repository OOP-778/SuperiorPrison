package com.bgsoftware.superiorprison.plugin.util.chatCmds;

import com.google.common.collect.Maps;
import com.oop.orangeengine.main.Helper;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatCommands {

    // command label = command
    private Map<String, BiConsumer<Player, String[]>> commandMap = Maps.newHashMap();

    private Map<ErrorType, Consumer<Player>> errorHandlers = Maps.newHashMap();

    public ChatCommands appendCommand(String label, BiConsumer<Player, String[]> handler) {
        this.commandMap.put(label, handler);
        return this;
    }

    public ChatCommands setHandler(ErrorType type, Consumer<Player> handler) {
        errorHandlers.put(type, handler);
        return this;
    }

    public void handle(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (commandMap.isEmpty()) return;

        String message = event.getMessage();

        // Try to find labels
        Optional<String> first = commandMap.keySet().stream()
                .filter(label -> message.toLowerCase().startsWith(label.toLowerCase()))
                .findFirst();
        if (!first.isPresent()) {
            Optional.ofNullable(errorHandlers.get(ErrorType.COMMAND_NOT_FOUND)).ifPresent(c -> c.accept(event.getPlayer()));
            return;
        }

        BiConsumer<Player, String[]> cmd = commandMap.get(first.get());
        String[] noLabelMessage = message.split(first.get());
        String[] args = new String[0];

        if (noLabelMessage.length > 0) {
            String cutMessage = noLabelMessage[1].substring(1);
            args = cutMessage.split("\\s+");
            if (args.length == 0)
                args = new String[]{cutMessage};
        }

        try {
            cmd.accept(event.getPlayer(), args);
        } catch (Throwable th) {
            if (th instanceof NullPointerException)
                th.printStackTrace();

            event.getPlayer().sendMessage(Helper.color("&c&lERROR: &4" + th.getMessage()));
        }
    }

    public static enum ErrorType {
        COMMAND_NOT_FOUND
    }

}
