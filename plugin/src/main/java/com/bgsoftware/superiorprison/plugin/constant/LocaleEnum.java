package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.OActionBarMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.Getter;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {
    PREFIX("&8[&e&lS&6&lP&8]&7 "),
    PREFIX_ERROR("&8[&c&lSP&r&8]&7 "),
    NO_PERMISSION("&cYou don't have permission!"),

    MINE_CREATE_ALREADY_CREATING("You're already creating a mine"),
    MINE_CREATE_FAIL_ALREADY_EXISTS("Mine already exists!"),
    MINE_CREATE_SELECT_REGION_POS("Select two corners for the region of the mine"),
    MINE_CREATE_SELECT_MINE_POS("Select two corners for the mine area"),
    MINE_SELECT_SPAWN_POS("Select spawn position"),
    MINE_CREATE_POSITION_MUST_BE_WITHIN_REGION("Position should be inside region"),
    MINE_CREATE_SUCCESSFUL("Successfully created a new mine! ({mine_name})"),
    MINE_DELETE_SUCCESSFUL("Successfully deleted mine! (&e{mine_name}&7)"),

    MINE_SELECT_POS("Selected position #{pos}"),

    EDIT_GENERATOR_WRITE_RATE("Please write new rate! It must be higher than 0. Example: 5.0, 1.5, 0.1"),
    EDIT_GENERATOR_RATE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_GENERATOR_RATE_SET("Rate of {material} was set to {rate}!"),
    EDIT_GENERATOR_REMOVED("Successfully removed {material} from mine generator!"),
    EDIT_GENERATOR_RATE_LIMIT_EXCEED("Failed to set rate of {material} cause combined it exceeds the 100 rate limit!"),
    EDIT_GENERATOR_MATERIAL_IS_NOT_BLOCK("Failed to add material cause it's not a block!"),
    EDIT_GENERATOR_SAVE("Mine materials were successfully saved!"),
    EDIT_GENERATOR_MATERIAL_ALREADY_EXISTS("Failed to add material cause it's already exists!"),
    EDIT_GENERATOR_SAVE_FAILED_WRONG_PERCENTAGE("Failed to save, percentage must be equal to 100, no less or more!"),

    EDIT_SHOP_WRITE_PRICE("Please write new price!"),
    EDIT_SHOP_PRICE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_SHOP_PRICE_SET("Price {item_name} was set to ${item_price}"),

    EDIT_SETTINGS_LIMIT("Please write new limit for the mine"),
    EDIT_SETTINGS_LIMIT_SUCCESS("Set the new player limit to {setting_value}"),

    EDIT_SETTING_RESET_TYPE("Please write a reset type from given ones: Timed, Percentage"),
    EDIT_SETTINGS_RESET_TYPE_SUCCESS("Set the new reset type to {setting_value}"),

    EDIT_SETTINGS_RESET_VALUE("Please write new {setting_name} for resetting"),
    EDIT_SETTINGS_RESET_VALUE_SUCCESS("Set the {setting_name} to {setting_value}"),

    EDIT_SETTINGS_ERROR("Error while editing setting {setting_name}: &c{error}"),

    EDIT_MESSAGE_INTERVAL("Please write new interval! Example: 1h2m2s, 1m, 5m6s, 10s"),
    EDIT_MESSAGE_INTERVAL_SET("New interval for message {message_id} set to {message_interval}"),
    EDIT_MESSAGE_CHAT_CONTENT(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &e&lCHAT MESSAGE EDITOR")
                    .append("   &7Content: &e{message_content}")
                    .append("")
                    .append("   &6Available functions")
                    .append("   &e&l* &7set (text) &6=>&f set the content of message")
                    .append("   &e&l(!) &7When you're done write '&esave&7'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_CONTENT_SUCCESS("Set the content of the message to {message_content}"),
    EDIT_MESSAGE_ACTION_BAR_CONTENT(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &e&lACTION BAR MESSAGE EDITOR")
                    .append("   &7Content: &e{message_content}")
                    .append("")
                    .append("   &6Available functions")
                    .append("   &e&l* &7set (text) &6=>&f set the content of message")
                    .append("   &e&l(!) &7When you're done write '&esave&7'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_TITLE_CONTENT(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &e&lTITLE MESSAGE EDITOR")
                    .append("   &7Title: &e{message_title}")
                    .append("   &7Sub title: &e{message_subTitle}")
                    .append("   &7Fade in, stay, fade out: &e{message_fadeIn}, {message_stay}, {message_fadeOut}")
                    .append("")
                    .append("   &6Available functions")
                    .append("   &e&l* &7setTitle (text) &6=>&f set title of message")
                    .append("   &e&l* &7setSubTitle (text) &6=>&f set subtitle of message")
                    .append("   &e&l* &7setFadeIn (int) &6=>&f set fade in of message")
                    .append("   &e&l* &7setStay (int) &6=>&f set stay of message")
                    .append("   &e&l* &7setFadeOut (int) &6=>&f set fade out of message")
                    .append("   &e&l(!) &7When you're done write '&esave&7'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_TITLE_SUCCESS("Set title of the message to: {message_title}"),
    EDIT_MESSAGE_TITLE_SUBTITLE_SUCCESS("Set sub title of the message to: {message_subTitle}"),
    EDIT_MESSAGE_TITLE_FADEIN_SUCCESS("Set fade in of the message to: {message_fadeIn}"),
    EDIT_MESSAGE_TITLE_STAY_SUCCESS("Set stay of the message to: {message_stay}"),
    EDIT_MESSAGE_TITLE_FADEOUT_SUCCESS("Set fade out of the message to: {message_fadeOut}"),
    EDIT_MESSAGE_SAVE("Saved message of id {message_id}"),

    EDIT_FLAGS_TOGGLE("You've &e{state} {flag} &7for &e{mine}&7"),

    MINE_RESET_SUCCESSFUL("Mine was successfully reset!"),
    MINE_TELEPORT_FAILED_SPAWN_NOT_SET("Failed to teleport to the mine, cause spawn point is not set!"),

    EDIT_ICON_MAIN_MESSAGE(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &e&lICON EDITOR")
                    .append("   &7Current: &e%item%")
                    .append("")
                    .append("   &6Available functions")
                    .append("   &e&l* &7material (material) &6=>&f set material of the icon!")
                    .append("   &e&l* &7display name (text) &6=>&f set display name of the icon")
                    .append("   &e&l* &7add lore (text) &6=>&f add lore to the icon!")
                    .append("   &e&l* &7set lore (line) (text) &6=>&f set specific line of the lore of the icon!")
                    .append("   &e&l* &7remove lore (line) &6=>&f removes specific line of the lore of the icon!")
                    .append("   &e&l* &7clear lore &6=>&f clears the lore of the icon!")
                    .append("   &e&l* &7cancel &6=>&f quit icon editor without saving!")
                    .append("   &e&l* &7save &6=>&f quit icon editor with saving!")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_ICON_DISPLAY_NAME_SET("Set the display name of the mine icon to &e{display_name}"),
    EDIT_ICON_SAVE("Successfully saved the icon!"),
    EDIT_ICON_SET_LORE_LINE("Set lore line &e{line}&7 of mine icon to &e{text}"),
    EDIT_ICON_ADD_LORE("Added to lore &e{text}"),
    EDIT_ICON_CLEAR_LORE("The lore was cleared!"),
    EDIT_ICON_REMOVE_LORE_LINE("Removed lore at line &e{line}"),
    EDIT_ICON_SET_MATERIAL("The material of mine icon was set to &e{material}"),

    RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OChatMessage(
                    new ChatLine("Failed to rank up to (&e{rank_name}&7) cause requirements aren't met: ")
                            .append(
                                    new LineContent("{TEMPLATE}&e{requirement_type}")
                                            .hover()
                                            .add("&e&l* &7Expected Value: &e{requirement_expected}", "&e&l* &7Current value: &e{requirement_current}")
                                            .parent()
                            )
            )
    ),
    RANKUP_SUCCESSFUL("Successfully ranked up from ({previous_rank}) to ({current_rank})"),
    RANKUP_MAX("You're already the highest rank!"),
    RANKUP_AVAILABLE(
            new OChatMessage().append(
                    new ChatLine("You have an rankup to (&e{rank_name}&7) available! ")
                            .append(
                                    new LineContent("&e&lRANKUP")
                                            .hover()
                                            .add("&eClick me to rankup!")
                                            .parent()
                                            .command()
                                            .command("rankup")
                                            .parent()
                            )
            )
    ),

    PRESTIGE_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OChatMessage(
                    new ChatLine("Failed to prestige up to (&e{prestige_name}&7) cause requirements aren't met: ")
                            .append(
                                    new LineContent("{TEMPLATE}&e{requirement_type}")
                                            .hover()
                                            .add("&e&l* &7Expected Value: &e{requirement_expected}", "&e&l* &7Current value: &e{requirement_current}")
                                            .parent()
                            )
            )
    ),
    PRESTIGE_SUCCESSFUL("Successfully prestiged up to ({prestige_name})"),
    MINE_RANK_FIND_INPUT("Please write input for rank!"),

    PLUGIN_RELOADED("SuperiorPrison reloaded successfully!"),
    PLUGIN_FAILED_RELOAD("SuperiorPrison failed to reload, check console for more information!"),
    PRISONER_ALREADY_HAVE_RANK("&c{prisoner_name}&7 already has &c{rank_name}&7 rank!"),
    PRISONER_ALREADY_HAVE_PRESTIGE("&c{prisoner_name}&7 already has &c{prestige_name}&7 prestige!"),

    SUCCESSFULLY_ADDED_PRESTIGE("Successfully added &e{prestige_name}&7 for &e{prisoner_name}"),
    SUCCESSFULLY_ADDED_RANK("Successfully added &e{rank_name}&7 for &e{prisoner_name}"),
    SUCCESSFULLY_CLEARED_RANKS("Successfully cleared ranks for &e{prisoner_name}"),
    SUCCESSFULLY_CLEARED_PRESTIGES("Successfully cleared prestiges for &e{prisoner_name}"),

    SUCCESSFULLY_REMOVED_RANK("Successfully removed &e{rank_name}&7 for &e{prisoner_name}"),
    SUCCESSFULLY_REMOVED_PRESTIGE("Successfully removed &e{prestige_name}&7 for &e{prisoner_name}"),

    SUCCESSFULLY_RESET_PRISONER("{prisoner_name} was successfully reset!"),
    PRISONER_RANKS_LIST("&e{prisoner_name}&7 ranks list:", "&e&l* &7Ladder Ranks: &e{prisoner_ladder_ranks}", "&e&l* &7Special Ranks: &e{prisoner_special_ranks}"),
    PRISONER_PRESTIGES_LIST("&e{prisoner_name}&7 prestiges list: &e{prisoner_prestiges}"),

    PRISONER_BOOSTER_ADD("Added booster (&e{booster_type}, {booster_rate}, {booster_time}&7) to {prisoner_name}"),
    PRISONER_BOOSTER_CLEAR("Cleared boosters for {prisoner_name}"),
    PRISONER_BOOSTER_REMOVE_DOESNT_HAVE("{prisoner_name} doesn't have booster by id {id}"),
    PRISONER_BOOSTER_REMOVE("Removed {booster_id} booster from {prisoner_name}"),
    PRISONER_BOOSTER_LIST(
            new OChatMessage(
                    new ChatLine("{prisoner_name}'s active boosters: ")
                            .append(
                                    new LineContent("{TEMPLATE}&e{booster_id}")
                                            .hover()
                                            .add("&e&l* &7Type: &e{booster_type}")
                                            .add("&e&l* &7Rate: &e{booster_rate}")
                                            .add("&e&l* &7Time left: &e{booster_time}")
                                            .parent()
                            )
            )
    ),
    PRISONER_MAX_PRESTIGE("You have the highest prestige!"),
    PRISONER_OPTION_TOGGLE("You have {state} {option_name}"),

    SHOP_EDIT_ALREADY_HAS_ITEM("Shop already has this item"),
    COPIED_OPTION("Copied {option_name} from {from_name} to {to_name}"),

    SOLD_EVERYTHING("Sold everything for {total}$"),
    MINE_IS_FULL("Mine is currently full."),

    MINE_MESSAGES_CREATE_TYPE("What type of message you want to create? Available: actionbar, title, chat"),
    MINE_MESSAGES_CREATE_INTERVAL("Now write interval of message. Example: 1h2m2s, 1m, 3s, 10m10s"),
    MINE_MESSAGES_CREATE_SUCCESS("Successfully create mine message of type {message_type}"),

    AUTO_PICKUP_PRISONER_INVENTORY_FULL("Your inventory is full!"),
    CANNOT_ENTER_MINE_MISSING_RANK("You cannot enter the mine! You're missing at least {rank}"),
    CANNOT_ENTER_MINE_MINE_NOT_READY("Cannot enter the mine! It's either resetting or initializing..."),
    MINE_RESETTING("You've been teleported out! Because mine is resetting!"),

    SOLD_BLOCKS_MESSAGE(new OActionBarMessage().text("&eYou've sold &6{blocks} &eblocks in the past &6{time}&e and earned &6{money}"));

    private final OMessage[] cache = new OMessage[2];

    @Getter
    private final OMessage message;

    LocaleEnum(String text) {
        message = new OChatMessage(text);
    }

    LocaleEnum(OMessage message) {
        this.message = message;
    }

    LocaleEnum(String... message) {
        this.message = new OChatMessage(message);
    }

    public static void load() {
        for (LocaleEnum localeEnum : values())
            getLocale().getMessage(localeEnum.name(), () -> localeEnum.message, true);
    }

    public OMessage getWithPrefix() {
        if (message instanceof OChatMessage) {
            if (cache[0] == null) {
                OChatMessage chatMessage = ((OChatMessage) message).clone();
                chatMessage.lineList().get(0).insert(new LineContent(PREFIX.message.asChat().lineList().get(0).raw()), 0);
                cache[0] = chatMessage;
                return chatMessage;

            } else return cache[0];
        }
        return message;
    }

    public OMessage getWithErrorPrefix() {
        if (message instanceof OChatMessage) {
            if (cache[0] == null) {
                OChatMessage chatMessage = ((OChatMessage) message).clone();
                chatMessage.lineList().get(0).insert(new LineContent(PREFIX_ERROR.message.asChat().lineList().get(0).raw()), 0);
                cache[0] = chatMessage;
                return chatMessage;

            } else return cache[0];
        }
        return message;
    }
}
