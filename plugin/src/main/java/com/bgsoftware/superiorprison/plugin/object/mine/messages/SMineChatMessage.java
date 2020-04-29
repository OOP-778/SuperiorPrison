package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineChatMessage;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import lombok.Setter;

public class SMineChatMessage extends SMineMessage implements MineChatMessage {

    @Getter @Setter
    private String content;

    public SMineChatMessage() {
        super(0);
    }

    public SMineChatMessage(long every, String content) {
        super(every);
        this.content = content;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAT;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "chat");
        serializedData.write("content", content);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        super.deserialize(serializedData);
        this.content = serializedData.applyAs("content", String.class);
    }
}
