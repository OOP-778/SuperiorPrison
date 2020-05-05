package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MineMessage;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

public abstract class SMineMessage implements MineMessage, SerializableObject {

    private long interval;
    private int id;

    @Setter @Getter
    private ZonedDateTime timeToRun;

    public SMineMessage(long interval) {
        this.interval = interval;
    }

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setInterval(long every) {
        this.interval = every;
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        this.id = serializedData.applyAs("id", int.class);
        this.interval = serializedData.applyAs("interval", long.class);
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("id", id);
        serializedData.write("interval", interval);
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
