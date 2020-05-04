package com.bgsoftware.superiorprison.api.data.mine.messages;

public interface MineMessage {

    MessageType getType();

    long getEvery();

    int getId();

    void setInterval(long every);
}
