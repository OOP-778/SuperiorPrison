package com.bgsoftware.superiorprison.api.data.mine.messages;

public interface MineActionBarMessage extends MineMessage {
  // Get content of action bar
  String getContent();

  // Set content of action bar
  void setContent(String content);
}
