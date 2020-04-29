package com.bgsoftware.superiorprison.api.data.mine.messages;

import java.util.Optional;

public interface MineTitleMessage extends MineMessage {
    int getFadeIn();

    int getStay();

    int getFadeOut();

    Optional<String> getTitle();

    Optional<String> getSubTitle();

    void setFadeIn(int fadeIn);

    void setStay(int stay);

    void setFadeOut(int fadeOut);

    void setTitle(String title);

    void setSubTitle(String subTitle);
}
