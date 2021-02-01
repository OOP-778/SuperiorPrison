package com.bgsoftware.superiorprison.api.data.mine.messages;

import java.util.Optional;

public interface MineTitleMessage extends MineMessage {
  // Get fade in of title message
  int getFadeIn();

  // Set fade in (in ticks)
  void setFadeIn(int fadeIn);

  // Get stay of title message
  int getStay();

  // Set stay (in ticks)
  void setStay(int stay);

  // Get fade out of title message
  int getFadeOut();

  // Set fade out (in ticks)
  void setFadeOut(int fadeOut);

  // Get title of the title message
  String getTitle();

  // Set title
  void setTitle(String title);

  // Get sub title of the title message
  Optional<String> getSubTitle();

  // Set sub title
  void setSubTitle(String subTitle);
}
