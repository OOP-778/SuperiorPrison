package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.additions.action.CommandAddition;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import lombok.Getter;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {

    PREFIX("&8[&e&lS&6&lP&8]&7 "),
    PREFIX_ERROR("&8[&c&lSP&r&8]&7 "),
    NO_PERMISSION("&cYou don't have permission!"),

    MINE_CREATE_FAIL_ALREADY_EXISTS("Mine already exists!"),
    MINE_CREATE_SELECT_REGION_POS("Select two corners for the region of the mine"),
    MINE_CREATE_SELECT_MINE_POS("Select two corners for the mine area"),
    MINE_SELECT_SPAWN_POS("Select spawn position"),
    MINE_CREATE_SUCCESSFUL("Successfully created a new mine! (%mine_name%)"),

    MINE_SELECT_POS("Selected position #%pos%"),

    EDIT_GENERATOR_WRITE_RATE("Please write new rate! It must be higher than 0. Example: 5.0, 1.5, 0.1"),
    EDIT_GENERATOR_RATE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_GENERATOR_RATE_SET("Rate of %material% was set to %rate%!"),
    EDIT_GENERATOR_REMOVED("Successfully removed %material% from mine generator!"),
    EDIT_GENERATOR_RATE_LIMIT_EXCEED("Failed to set rate of %material% cause combined it exceeds the 100 rate limit!"),
    EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK("Failed to add material cause it's not a block!"),
    EDIT_GENERATOR_SAVE("Mine materials were successfully saved!"),
    EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS("Failed to add material cause it's already exists!"),
    EDIT_GENERATOR_SAVE_FAILED_WRONG_PERCENTAGE("Failed to save, percentage must be equal to 100, no less or more!"),

    EDIT_SHOP_WRITE_PRICE("Please write new price!"),
    EDIT_SHOP_PRICE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_SHOP_PRICE_SET("Price {item_name} was set to ${item_price}"),

    MINE_RESET_SUCCESSFUL("Mine was successfully reset!"),
    MINE_TELEPORT_FAILED_SPAWN_NOT_SET("Failed to teleport to the mine, cause spawn point is not set!"),

    EDIT_ICON_MAIN_MESSAGE(
            new OMessage()
                    .appendLine("&7&l-----------------")
                    .appendLine("")
                    .appendLine("   &e&lICON EDITOR")
                    .appendLine("   &7Current: &e%item%")
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
    EDIT_ICON_DISPLAY_NAME_SET("Set the display name of the mine icon to %display_name%"),
    EDIT_ICON_SAVE("Successfully saved the icon!"),
    EDIT_ICON_SET_LORE_LINE("Set lore line %line% of mine icon to %text%"),
    EDIT_ICON_ADD_LORE("Added to lore %text%"),
    EDIT_ICON_CLEAR_LORE("The lore was cleared!"),
    EDIT_ICON_REMOVE_LORE_LINE("Removed lore at line %line%"),
    EDIT_ICON_SET_MATERIAL("The material of mine icon was set to %material%"),

    RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS("Failed to rank up to (%rank%) cause requirements aren't met: "),
    RANKUP_REQUIREMENT_FORMAT("&e&l-&7 %requirement% (%current%/%required%)"),
    RANKUP_SUCCESSFUL("Successfully ranked up from (%previous_rank%) to (%current_rank%)"),
    RANKUP_MAX("You're already the highest rank!"),
    RANKUP_AVAILABLE(
            new OMessage().appendLine(
                    new MessageLine("You have an rankup to (&e{rank}&7) available! ")
                    .append(
                            new LineContent("&e&lRANKUP")
                            .hoverText("&eClick me to rankup!")
                            .addAddition(new CommandAddition("rankup"))
                    )
            )
    ),

    PRESTIGE_FAILED_DOES_NOT_MEET_REQUIREMENTS("Failed to prestige to (%prestige%) cause requirements aren't met: "),
    PRESTIGE_SUCCESSFUL("Successfully prestiged up to (%prestige%)"),
    MINE_RANK_FIND_INPUT("Please write input for rank!"),

    PLUGIN_RELOADED("SuperiorPrison reloaded successfully!"),
    PLUGIN_FAILED_RELOAD("SuperiorPrison failed to reload, check console for more information!"),
    PRISONER_ALREADY_HAVE_RANK("&c{prisoner}&7 already has &c{rank}&7 rank!"),

    SUCCESSFULLY_ADDED_RANK("Successfully added &e{rank}&7 for &e{prisoner}"),
    SUCCESSFULLY_CLEARED_RANKS("Successfully cleared ranks for &e{prisoner}"),
    SUCCESSFULLY_REMOVED_RANK("Successfully removed &e{rank}&7 for &e{prisoner}"),

    SUCCESSFULLY_RESET_PRISONER("{prisoner} was successfully reset!"),
    PRISONER_RANKS_LIST("&e{prisoner}&7 ranks list:", "&e&l* &7Ladder Ranks: &e{ladder_ranks}", "&e&l* &7Special Ranks: &e{special_ranks}"),
    PRISONER_ADD_BOOSTER("Added booster (&e{type}, {rate}, {time}&7) to {prisoner}"),
    PRISONER_BOOSTER_LIST(
            new OMessage(
                    new MessageLine("{prisoner} active boosters: ")
                    .append(
                            new LineContent("{BOOSTER_TEMPLATE}&e{type}")
                            .appendHover("&e&l* &7Rate: &e{rate}")
                            .appendHover("&e&l* &7Time left: &e{time}")
                    )
            )
    );


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

    LocaleEnum(String ...message) {
        this.message = new OMessage();
        for (String s : message)
            this.message.appendLine(s);
    }

    public static void load() {
        for (LocaleEnum localeEnum : values())
            getLocale().getMessage(localeEnum.name(), () -> localeEnum.message, true);
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
