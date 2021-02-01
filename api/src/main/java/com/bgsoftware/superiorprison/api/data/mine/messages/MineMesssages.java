package com.bgsoftware.superiorprison.api.data.mine.messages;

import java.util.Optional;
import java.util.Set;

public interface MineMesssages {
  // Get an message by id if available
  Optional<MineMessage> get(int id);

  // Get set of messages
  Set<MineMessage> get();

  // Remove an message by id
  void remove(int id);

  // Remove an message
  default void remove(MineMessage mineMessage) {
    remove(mineMessage.getId());
  }

  // Add an chat message (interval in seconds)
  MineChatMessage addChatMessage(String content, long interval);

  // Add an title message (fadeIn, stay, FadeOut in ticks, subTitle nullable, interval in seconds)
  MineTitleMessage addTitleMessage(
      int fadeIn, int stay, int fadeOut, String title, String subTitle, long interval);

  // Add an action bar message (interval in seconds)
  MineActionBarMessage addActionBarMessage(String content, long interval);
}
