package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineActionBarMessage;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import lombok.Setter;

public class SMineActionBarMessage extends SMineMessage implements MineActionBarMessage {

    @Getter @Setter
    private String content;

    public SMineActionBarMessage() {
        super(0);
    }

    public SMineActionBarMessage(long every, String content) {
        super(every);
        this.content = content;
    }

    @Override
    public MessageType getType() {
        return MessageType.ACTION_BAR;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "actionbar");
        serializedData.write("content", content);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        super.deserialize(serializedData);
        this.content = serializedData.applyAs("content", String.class);
    }
}
