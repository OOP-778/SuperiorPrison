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

    MINE_CREATE_ALREADY_CREATING("You're already creating a mine"),
    MINE_CREATE_FAIL_ALREADY_EXISTS("Mine already exists!"),
    MINE_CREATE_SELECT_REGION_POS("Select two corners for the region of the mine"),
    MINE_CREATE_SELECT_MINE_POS("Select two corners for the mine area"),
    MINE_SELECT_SPAWN_POS("Select spawn position"),
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

    EDIT_FLAGS_TOGGLE("You've &e{state} {flag} &7for &e{mine}&7"),

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
    EDIT_ICON_DISPLAY_NAME_SET("Set the display name of the mine icon to &e{display_name}"),
    EDIT_ICON_SAVE("Successfully saved the icon!"),
    EDIT_ICON_SET_LORE_LINE("Set lore line &e{line}&7 of mine icon to &e{text}"),
    EDIT_ICON_ADD_LORE("Added to lore &e{text}"),
    EDIT_ICON_CLEAR_LORE("The lore was cleared!"),
    EDIT_ICON_REMOVE_LORE_LINE("Removed lore at line &e{line}"),
    EDIT_ICON_SET_MATERIAL("The material of mine icon was set to &e{material}"),

    RANKUP_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OMessage(
                    new MessageLine("Failed to rank up to (&e{rank_name}&7) cause requirements aren't met: ")
                    .append(
                            new LineContent("{TEMPLATE}&e{requirement_type}")
                            .appendHover("&e&l* &7Expected Value: &e{requirement_expected}")
                            .appendHover("&e&l* &7Current value: &e{requirement_current}")
                    )
            )
    ),
    RANKUP_SUCCESSFUL("Successfully ranked up from ({previous_rank}) to ({current_rank})"),
    RANKUP_MAX("You're already the highest rank!"),
    RANKUP_AVAILABLE(
            new OMessage().appendLine(
                    new MessageLine("You have an rankup to (&e{rank_name}&7) available! ")
                    .append(
                            new LineContent("&e&lRANKUP")
                            .hoverText("&eClick me to rankup!")
                            .addAddition(new CommandAddition("rankup"))
                    )
            )
    ),

    PRESTIGE_FAILED_DOES_NOT_MEET_REQUIREMENTS(
            new OMessage(
                    new MessageLine("Failed to prestige up to (&e{prestige_name}&7) cause requirements aren't met: ")
                            .append(
                                    new LineContent("{TEMPLATE}&e{requirement_type}")
                                            .appendHover("&e&l* &7Expected Value: &e{requirement_expected}")
                                            .appendHover("&e&l* &7Current value: &e{requirement_current}")
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
            new OMessage(
                    new MessageLine("{prisoner_name}'s active boosters: ")
                    .append(
                            new LineContent("{TEMPLATE}&e{booster_id}")
                                    .appendHover("&e&l* &7Type: &e{booster_type}")
                                    .appendHover("&e&l* &7Rate: &e{booster_rate}")
                                    .appendHover("&e&l* &7Time left: &e{booster_time}")
                    )
            )
    ),
    PRISONER_MAX_PRESTIGE("You have the highest prestige!"),
    PRISONER_OPTION_TOGGLE("You have {state} {option_name}"),

    SHOP_EDIT_ALREADY_HAS_ITEM("Shop already has this item"),
    COPIED_OPTION("Copied {option_name} from {from_name} to {to_name}"),

    SOLD_EVERYTHING("Sold everything for {total}");

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
