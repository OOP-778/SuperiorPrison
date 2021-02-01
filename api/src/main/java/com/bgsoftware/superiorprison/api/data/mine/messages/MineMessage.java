package com.bgsoftware.superiorprison.api.data.mine.messages;

import org.bukkit.command.CommandSender;

public interface MineMessage {

  // Get message type
  MessageType getType();

  // Get interval in seconds
  long getInterval();

  // Set interval (seconds)
  void setInterval(long every);

  // Get id of the message
  int getId();

  // Send the message to someone
  void send(CommandSender sender);
}
