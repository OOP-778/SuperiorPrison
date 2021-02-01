package com.bgsoftware.superiorprison.api.data.mine.messages;

public interface MineChatMessage extends MineMessage {
  // Get content of chat message
  String getContent();

  // Set content of chat message
  void setContent(String content);
}
