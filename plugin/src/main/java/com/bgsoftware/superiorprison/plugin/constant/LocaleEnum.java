package com.bgsoftware.superiorprison.plugin.constant;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.OActionBarMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.Getter;

import static com.oop.orangeengine.message.locale.Locale.getLocale;

public enum LocaleEnum {
    PREFIX("&d&lPrison | &7"),
    PREFIX_ERROR("&c&lError | &7"),
    NO_PERMISSION("You don't have permission!"),

    MINE_CREATE_ALREADY_CREATING("You're already creating a mine"),
    MINE_CREATE_FAIL_ALREADY_EXISTS("Mine already exists!"),
    MINE_CREATE_SELECT_REGION_POS("Select two corners for the region of the mine"),
    MINE_CREATE_SELECT_MINE_POS("Select two corners for the mine area"),
    MINE_SELECT_SPAWN_POS("Select spawn position"),
    MINE_CREATE_POSITION_MUST_BE_WITHIN_REGION("Position should be inside region"),
    MINE_CREATE_SUCCESSFUL("Successfully created a new mine! ({mine_name})"),
    MINE_DELETE_SUCCESSFUL("Successfully deleted mine! (&d{mine_name}&7)"),
    MINE_SET_SPAWN_POS("You've set &d{mine_name} &7spawn position to your current location!"),

    MINE_SELECT_POS("Selected position #{pos}"),

    EDIT_GENERATOR_WRITE_RATE("Please write new rate! It must be higher than 0. Example: 5.0, 1.5, 0.1"),
    EDIT_GENERATOR_RATE_NOT_NUMBER("Given value is not an number! ex: 5.5"),
    EDIT_GENERATOR_RATE_SET("Rate of {material} was set to {rate}!"),
    EDIT_GENERATOR_REMOVED("Successfully removed {material} from mine generator!"),
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
                    .append("   &d&lCHAT MESSAGE EDITOR")
                    .append("   &7Content: &d{message_content}")
                    .append("")
                    .append("   &5Available functions")
                    .append("   &d&l* &7set (text) &5=>&f set the content of message")
                    .append("   &d&l(!) &7When you're done write '&dsave&7'")
                    .append("   &c&l(!) &7To cancel write 'cancel'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_CONTENT_SUCCESS("Set the content of the message to {message_content}"),
    EDIT_MESSAGE_ACTION_BAR_CONTENT(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &d&lACTION BAR MESSAGE EDITOR")
                    .append("   &7Content: &d{message_content}")
                    .append("")
                    .append("   &5Available functions")
                    .append("   &d&l* &7set (text) &5=>&f set the content of message")
                    .append("   &d&l(!) &7When you're done write '&dsave&7'")
                    .append("   &c&l(!) &7To cancel write 'cancel'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_TITLE_CONTENT(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &d&lTITLE MESSAGE EDITOR")
                    .append("   &7Title: &d{message_title}")
                    .append("   &7Sub title: &d{message_subTitle}")
                    .append("   &7Fade in, stay, fade out: &d{message_fadeIn}, {message_stay}, {message_fadeOut}")
                    .append("")
                    .append("   &5Available functions")
                    .append("   &d&l* &7setTitle (text) &5=>&f set title of message")
                    .append("   &d&l* &7setSubTitle (text) &5=>&f set subtitle of message")
                    .append("   &d&l* &7setFadeIn (int) &5=>&f set fade in of message")
                    .append("   &d&l* &7setStay (int) &5=>&f set stay of message")
                    .append("   &d&l* &7setFadeOut (int) &5=>&f set fade out of message")
                    .append("   &d&l(!) &7When you're done write '&dsave&7'")
                    .append("   &c&l(!) &7To cancel write 'cancel'")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_MESSAGE_TITLE_SUCCESS("Set title of the message to: {message_title}"),
    EDIT_MESSAGE_TITLE_SUBTITLE_SUCCESS("Set sub title of the message to: {message_subTitle}"),
    EDIT_MESSAGE_TITLE_FADEIN_SUCCESS("Set fade in of the message to: {message_fadeIn}"),
    EDIT_MESSAGE_TITLE_STAY_SUCCESS("Set stay of the message to: {message_stay}"),
    EDIT_MESSAGE_TITLE_FADEOUT_SUCCESS("Set fade out of the message to: {message_fadeOut}"),
    EDIT_MESSAGE_SAVE("Saved message of id {message_id}"),

    EDIT_FLAGS_TOGGLE("You've &d{state} {flag} &7for &d{mine}&7"),

    MINE_RESET_SUCCESSFUL("Mine was successfully reset!"),
    MINE_TELEPORT_FAILED_SPAWN_NOT_SET("Failed to teleport to the mine, cause spawn point is not set!"),

    EDIT_ICON_MAIN_MESSAGE(
            new OChatMessage()
                    .append("&7&l-----------------")
                    .append("")
                    .append("   &d&lICON EDITOR")
                    .append("   &7Current: &d%item%")
                    .append("")
                    .append("   &5Available functions")
                    .append("   &d&l* &7material (material) &5=>&f set material of the icon!")
                    .append("   &d&l* &7display name (text) &5=>&f set display name of the icon")
                    .append("   &d&l* &7add lore (text) &5=>&f add lore to the icon!")
                    .append("   &d&l* &7set lore (line) (text) &5=>&f set specific line of the lore of the icon!")
                    .append("   &d&l* &7remove lore (line) &5=>&f removes specific line of the lore of the icon!")
                    .append("   &d&l* &7clear lore &5=>&f clears the lore of the icon!")
                    .append("   &d&l* &7texture (texture) &5=>&f sets texture of the skull!")
                    .append("   &d&l* &7cancel &5=>&f quit icon editor without saving!")
                    .append("   &d&l* &7save &5=>&f quit icon editor with saving!")
                    .append("")
                    .append("&7&l-----------------")
    ),
    EDIT_ICON_DISPLAY_NAME_SET("Set the display name of the mine icon to &d{display_name}"),
    EDIT_ICON_SAVE("Successfully saved the icon!"),
    EDIT_ICON_SET_LORE_LINE("Set lore line &d{line}&7 of mine icon to &d{text}"),
    EDIT_ICON_ADD_LORE("Added to lore &d{text}"),
    EDIT_ICON_CLEAR_LORE("The lore was cleared!"),
    EDIT_ICON_REMOVE_LORE_LINE("Removed lore at line &d{line}"),
    EDIT_ICON_SET_MATERIAL("The material of mine icon was set to &d{material}"),
    EDIT_ICON_SET_TEXTURE("The texture of head was set."),

    RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OChatMessage(
                    new ChatLine("Failed to rank up to (&d{rank_name}&7) cause requirements aren't met: ")
                            .append(
                                    new LineContent("{TEMPLATE}&d{requirement_type}")
                                            .hover()
                                            .add("&d&l* &7Expected Value: &d{requirement_expected}", "&d&l* &7Current value: &d{requirement_current}")
                                            .parent()
                            )
            )
    ),
    RANKUP_SUCCESSFUL("Successfully ranked up from ({previous_rank}) to ({current_rank})"),
    RANKUP_MAX("You're already the highest rank!"),
    RANKUP_AVAILABLE(
            new OChatMessage().append(
                    new ChatLine("You have an rankup to (&d{rank_name}&7) available! ")
                            .append(
                                    new LineContent("&d&lRANKUP")
                                            .hover()
                                            .add("&dClick me to rankup!")
                                            .parent()
                                            .command()
                                            .command("rankup")
                                            .parent()
                            )
            )
    ),
    MAX_RANKUP_PRESTIGE_CHANGES("You've ranked up from &d{starting_prestige} &7to &d{current_prestige}&7 prestige"),
    MAX_RANKUP_RANK_CHANGES("You've ranked up from &d{starting_rank} &7to &d{current_rank}&7 rank"),

    PRESTIGE_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OChatMessage(
                    new ChatLine("Failed to prestige up to (&d{prestige_name}&7) cause requirements aren't met: ")
                            .append(
                                    new LineContent("{TEMPLATE}&d{requirement_type}")
                                            .hover()
                                            .add("&d&l* &7Expected Value: &d{requirement_expected}", "&d&l* &7Current value: &d{requirement_current}")
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

    SUCCESSFULLY_ADDED_PRESTIGE("Successfully added &d{prestige_name}&7 for &d{prisoner_name}"),
    SUCCESSFULLY_ADDED_RANK("Successfully added &d{rank_name}&7 for &d{prisoner_name}"),
    SUCCESSFULLY_CLEARED_RANKS("Successfully cleared ranks for &d{prisoner_name}"),
    SUCCESSFULLY_CLEARED_PRESTIGES("Successfully cleared prestiges for &d{prisoner_name}"),

    SUCCESSFULLY_REMOVED_RANK("Successfully removed &d{rank_name}&7 for &d{prisoner_name}"),
    SUCCESSFULLY_REMOVED_PRESTIGE("Successfully removed &d{prestige_name}&7 for &d{prisoner_name}"),

    SUCCESSFULLY_RESET_PRISONER("{prisoner_name} was successfully reset!"),
    PRISONER_RANKS_VIEW("&d{prisoner_name}&7 ranks information:", "&d&l* &7Ladder rank: &d{prisoner_ladderrank}", "&d&l* &7Special Ranks: &d{prisoner_specialranks}"),
    PRISONER_PRESTIGE_VIEW("&d{prisoner_name}&7 Current Prestige: &d{prisoner_prestige}"),
    PRISONER_PRESTIGE_SET("&d{prisoner_name} &7prestige was set to &d{prestige_name}"),
    PRISONER_RANK_SET("&d{prisoner_name} &7ladder rank was set to &d{rank_name}"),
    PRISONER_RANKS_ADD_CANNOT_LADDER("Cannot add ladder rank. Use set command to set a ladder rank!"),

    PRISONER_BOOSTER_ADD("Added booster (&d{booster_type}, {booster_rate}, {booster_time}&7) to {prisoner_name}"),
    PRISONER_BOOSTER_CLEAR("Cleared boosters for {prisoner_name}"),
    PRISONER_BOOSTER_REMOVE_DOESNT_HAVE("{prisoner_name} doesn't have booster by id {id}"),
    PRISONER_BOOSTER_REMOVE("Removed {booster_id} booster from {prisoner_name}"),
    PRISONER_BOOSTER_LIST(
            new OChatMessage(
                    new ChatLine("{prisoner_name}'s active boosters: ")
                            .append(
                                    new LineContent("{TEMPLATE}&d{booster_id}")
                                            .hover()
                                            .add("&d&l* &7Type: &d{booster_type}")
                                            .add("&d&l* &7Rate: &d{booster_rate}")
                                            .add("&d&l* &7Time left: &d{booster_time}")
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

    SOLD_BLOCKS_MESSAGE(new OActionBarMessage().text("&dYou've sold &5{blocks} &dblocks in the past &5{time}&d and earned &5{money}")),

    SELL_HAND_MUST_HOLD_SOMETHING("You must hold something to execute this command!"),
    SELL_INVENTORY_WORTHLESS("Your inventory is worthless..."),

    PRISON_SHUTDOWN("&cPrison is shutting down."),
    BACKPACK_DROPPED_INVENTORY_FULL("Your backpack was dropped on the ground, because your inventory is full!"),
    BACKPACK_UPGRADE_DONT_MEET_REQUIREMENTS("You cannot upgrade this backpack, because you don't meet the requirements!"),

    PRESTIGE_TOP_MESSAGE(
            new OChatMessage(
                    "&7&l&m------- &d&lPrestige Top",
                    "{entries}"
            )
            .append(
                    new ChatLine("{TEMPLATE}&d&l{position} &d{player_name} &d{prestige}")
            )
    ),
    PRISONER_RESET("&cYou've been reset.");

    private final OMessage[] cache = new OMessage[2];

    @Getter
    private OMessage message;

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
        for (LocaleEnum localeEnum : values()) {
            localeEnum.cache[0] = null;
            localeEnum.cache[1] = null;
            localeEnum.message = getLocale().getMessage(localeEnum.name(), () -> localeEnum.message, true);
        }
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
            if (cache[1] == null) {
                OChatMessage chatMessage = ((OChatMessage) message).clone();
                chatMessage.lineList().get(0).insert(new LineContent(PREFIX_ERROR.message.asChat().lineList().get(0).raw()), 0);
                cache[1] = chatMessage;
                return chatMessage;

            } else return cache[1];
        }
        return message;
    }
}
