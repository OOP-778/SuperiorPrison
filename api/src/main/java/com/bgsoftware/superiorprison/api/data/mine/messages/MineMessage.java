package com.bgsoftware.superiorprison.api.data.mine.messages;

import org.bukkit.command.CommandSender;

public interface MineMessage {

    MessageType getType();

    long getInterval();

    int getId();

    void setInterval(long every);

    void send(CommandSender sender);
}
