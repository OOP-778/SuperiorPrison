package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MineMessage;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;

public abstract class SMineMessage implements MineMessage, SerializableObject {

    private long every;
    private int id;

    public SMineMessage(long every) {
        this.every = every;
    }

    @Override
    public long getEvery() {
        return every;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setEvery(long every) {
        this.every = every;
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.id = serializedData.applyAs("id", int.class);
        this.every = serializedData.applyAs("every", long.class);
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("id", id);
        serializedData.write("every", every);
    }

    public static SMineMessage from(SerializedData data) {
        String type = data.applyAs("type", String.class);

        SMineMessage message = null;
        if (type.equalsIgnoreCase("chat"))
            message = new SMineChatMessage();

        else if (type.equalsIgnoreCase("title"))
            message = new SMineTitleMessage();

        else if (type.equalsIgnoreCase("actionbar"))
            message = new SMineActionBarMessage();

        message.deserialize(data);
        return message;
    }

    void assignId(int id) {
        if (this.id != 0) return;

        this.id = id;
    }
}
