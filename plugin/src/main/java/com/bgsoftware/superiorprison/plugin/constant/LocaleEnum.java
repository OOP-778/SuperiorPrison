package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import lombok.Getter;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {

    PREFIX("&8[&e&lS&6&lP&8]&7 "),
    PREFIX_ERROR("&8[&c&lSP&r&8]&7 "),
    NO_PERMISSION("&cYou don't have permission!"),

    GENERATOR_RATE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    GENERATOR_WRITE_RATE("Please write new rate! It must be higher than 0. Example: 5.0, 1.5, 0.1"),
    GENERATOR_RATE_SET("Rate of %material% set to %rate%!"),
    GENERATOR_REMOVED("Successfully removed %material% from mine generator!"),
    GENERATOR_RATE_LIMIT_EXCEED("Failed to set rate of %material% cause combined it exceeds the 100 rate limit!"),
    GENERATOR_MATERIAL_IS_NOT_BLOCK("Failed to add material cause it's not a block!"),
    GENERATOR_MATERIAL_ALREADY_EXISTS("Failed to add material cause it's already exists!");

    private OMessage[] cache = new OMessage[2];

    @Getter
    private OMessage message;
    LocaleEnum(String text) {
        message = new OMessage();
        message.appendLine(text);
    }

    LocaleEnum(OMessage message) {
        this.message = message;
    }

    public static void load() {
        for (LocaleEnum localeEnum : values())
            getLocale().getMessage(localeEnum.name(), () -> localeEnum.message);
    }

    public OMessage getWithPrefix() {
        if (cache[0] == null) {
            OMessage messageClone = message.clone();
            messageClone.getLineList().get(0).insert(new LineContent(PREFIX.message.getLineList().get(0).getRaw()), 0);
            cache[0] = messageClone;
            return messageClone;

        } else
            return cache[0];
    }

    public OMessage getWithErrorPrefix() {
        if (cache[1] == null) {
            OMessage messageClone = message.clone();
            messageClone.getLineList().get(0).insert(new LineContent(PREFIX_ERROR.message.getLineList().get(0).getRaw()), 0);
            cache[1] = messageClone;
            return messageClone;

        } else
            return cache[1];
    }

}
