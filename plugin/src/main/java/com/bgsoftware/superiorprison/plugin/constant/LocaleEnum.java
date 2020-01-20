package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import lombok.Getter;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {

    PREFIX("&8[&e&lS&6&lP&8]&7 "),
    PREFIX_ERROR("&8[&c&lSP&r&8]&7 "),
    NO_PERMISSION("&cYou don't have permission!"),

    EDIT_ICON_MAIN_MESSAGE(
            new OMessage()
                    .appendLine("&7&l-----------------")
                    .appendLine("")
                    .appendLine("   &e&lICON EDITOR")
                    .appendLine("   &7Current %item% (hover)")
                    .appendLine("")
                    .appendLine("   &6Available functions")
                    .appendLine("   &e&l* &7material (material) &6=>&f set material of the icon!")
                    .appendLine("   &e&l* &7display name (text) &6=>&f set display name of the icon")
                    .appendLine("   &e&l* &7add lore (text) &6=>&f add lore to the icon!")
                    .appendLine("   &e&l* &7set lore (line) (text) &6=>&f set specific line of the lore of the icon!")
                    .appendLine("   &e&l* &7remove lore (line) &6=>&f removes specific line of the lore of the icon!")
                    .appendLine("   &e&l* &7clear lore &6=>&f clears the lore of the icon!")
                    .appendLine("   &e&l* &7cancel &6=>&f quit icon editor without saving!")
                    .appendLine("   &e&l* &7save &6=>&f quit icon editor with saving!")
                    .appendLine("")
                    .appendLine("&7&l-----------------")
    ),

    EDIT_GENERATOR_WRITE_RATE("Please write new rate! It must be higher than 0. Example: 5.0, 1.5, 0.1"),
    EDIT_GENERATOR_RATE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_GENERATOR_RATE_SET("Rate of %material% was set to %rate%!"),
    EDIT_GENERATOR_REMOVED("Successfully removed %material% from mine generator!"),
    EDIT_GENERATOR_RATE_LIMIT_EXCEED("Failed to set rate of %material% cause combined it exceeds the 100 rate limit!"),
    EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK("Failed to add material cause it's not a block!"),
    EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS("Failed to add material cause it's already exists!"),

    EDIT_SHOP_WRITE_PRICE("Please write new price!"),
    EDIT_SHOP_PRICE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_SHOP_PRICE_SET("Price {item_name} was set to ${item_price}"),

    MINE_RESET_SUCCESSFUL("Mine was successfully reset!"),
    MINE_TELEPORT_FAILED_SPAWN_NOT_SET("Failed to teleport to the mine, cause spawn point is not set!");

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
