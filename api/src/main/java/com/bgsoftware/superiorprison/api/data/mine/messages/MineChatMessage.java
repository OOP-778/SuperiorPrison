package com.bgsoftware.superiorprison.api.data.mine.messages;

public interface MineChatMessage extends MineMessage {
    String getContent();

    void setContent(String content);
}
