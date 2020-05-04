package com.bgsoftware.superiorprison.api.data.mine.messages;

public enum MessageType {
    CHAT,
    TITLE,
    ACTION_BAR;

    public static MessageType match(String message) {
        message = message.toLowerCase().replace("_", "");
        for (MessageType type : values()) {
            if (type.name().toLowerCase().replace("_", "").equalsIgnoreCase(message))
                return type;
        }
        return null;
    }
}
