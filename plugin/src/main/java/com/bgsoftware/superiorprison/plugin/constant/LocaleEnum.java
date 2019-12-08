package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {

    PREFIX("&eS&6P "),
    NO_PERMISSION("&cYou don't have permission!");

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

}
