package com.bgsoftware.superiorprison.api.data.mine.messages;

import org.bukkit.command.CommandSender;

public interface MineMessage {

    // Get message type
    MessageType getType();

    // Get interval in seconds
    long getInterval();

    // Get id of the message
    int getId();

    // Set interval (seconds)
    void setInterval(long every);

    // Send the message to someone
    void send(CommandSender sender);
}
