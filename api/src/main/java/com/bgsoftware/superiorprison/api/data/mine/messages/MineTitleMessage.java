package com.bgsoftware.superiorprison.api.data.mine.messages;

import java.util.Optional;

public interface MineTitleMessage extends MineMessage {
    // Get fade in of title message
    int getFadeIn();

    // Get stay of title message
    int getStay();

    // Get fade out of title message
    int getFadeOut();

    // Get title of the title message
    String getTitle();

    // Get sub title of the title message
    Optional<String> getSubTitle();

    // Set fade in (in ticks)
    void setFadeIn(int fadeIn);

    // Set stay (in ticks)
    void setStay(int stay);

    // Set fade out (in ticks)
    void setFadeOut(int fadeOut);

    // Set title
    void setTitle(String title);

    // Set sub title
    void setSubTitle(String subTitle);
}
