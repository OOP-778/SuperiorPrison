package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineTitleMessage;
import com.google.gson.JsonElement;
import com.oop.datamodule.SerializedData;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class SMineTitleMessage extends SMineMessage implements MineTitleMessage {

    @Getter @Setter
    private int fadeIn;

    @Getter @Setter
    private int fadeOut;

    @Getter @Setter
    private int stay;

    @Getter
    private Optional<String> title, subTitle;

    public SMineTitleMessage() {
        super(0);
    }

    public SMineTitleMessage(long every, int fadeIn, int fadeOut, int stay, String title, String subTitle) {
        super(every);
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.stay = stay;
        this.title = Optional.ofNullable(title);
        this.subTitle = Optional.ofNullable(subTitle);
    }

    @Override
    public MessageType getType() {
        return MessageType.TITLE;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "actionbar");
        if (title.isPresent())
            serializedData.write("title", title);

        if (subTitle.isPresent())
            serializedData.write("subtitle", subTitle);

        serializedData.write("fadeIn", fadeIn);
        serializedData.write("stay", stay);
        serializedData.write("fadeOut", fadeOut);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        super.deserialize(serializedData);
        title = serializedData.getElement("title").map(JsonElement::getAsString);
        subTitle = serializedData.getElement("subtitle").map(JsonElement::getAsString);
        fadeIn = serializedData.applyAs("fadeIn", int.class);
        stay = serializedData.applyAs("stay", int.class);
        fadeOut = serializedData.applyAs("fadeOut", int.class);
    }

    @Override
    public void setSubTitle(String subTitle) {
        this.title = Optional.ofNullable(subTitle);
    }

    public void setTitle(String title) {
        this.title = Optional.ofNullable(title);
    }
}
