package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.menu.backpack.BackPackViewMenu;
import com.google.common.collect.Maps;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class MenuLoader {

    public static void loadMenu(OMenu menu, Config config) {
        menu.setTitle(ChatColor.translateAlternateColorCodes('&', config.getAs("title")));

        List<String> layout = config.getAs("layout", List.class);
        menu.setMenuRows(layout.size());

        Map<Character, String> charToActionMap = Maps.newHashMap();
        if (config.isValuePresent("actions")) {
            for (String action : (List<String>) config.getAs("actions")) {
                String[] split = action.split(":");
                charToActionMap.put(split[1].charAt(0), split[0]);
            }
        }

        if (menu instanceof OMenu.Placeholderable)
            ((OMenu.Placeholderable) menu).initPlaceholderable(config);

        if (menu instanceof OMenu.Templateable)
            ((OMenu.Templateable) menu).initTemplateable(config);

        ConfigSection buttonsSection = config.getSection("buttons").get();
        for (int a = 0; a < layout.size(); a++) {
            String row = layout.get(a);
            int slot = a * 9;

            for (int b = 0; b < row.length(); b++) {
                char ch = row.charAt(b);
                if (ch == ' ') continue;

                Optional<ConfigSection> optSection = buttonsSection.getSection(ch + "");
                if (!optSection.isPresent()) {
                    slot++;
                    continue;
                }

                ConfigSection section = optSection.get();

                OMenuButton button = initButton(section);
                String action = charToActionMap.get(ch);
                if (action != null)
                    button.action(action);

                if (menu instanceof OMenu.Templateable && ((OMenu.Templateable) menu).containsTemplate(ch + "")) {
                    ((OMenu.Templateable) menu).getTemplateButtonMap().put(((OMenu.Templateable) menu).getTemplateFromIdentifier(ch + "").orElse("-"), button);
                    slot++;
                    continue;
                }

                button.slot(slot);
                menu.getFillerItems().put(slot, button);
                slot++;
            }
        }

        // For templates
        for (ConfigSection buttonSection : buttonsSection.getSections().values()) {
            Character ch = buttonSection.getKey().charAt(0);
            if (menu instanceof OMenu.Templateable && ((OMenu.Templateable) menu).containsTemplate(ch + "")) {
                OMenuButton button = initButton(buttonSection);
                String action = charToActionMap.get(ch);
                if (action != null)
                    button.action(action);

                ((OMenu.Templateable) menu).getTemplateButtonMap().put(ch + "", button);
            }
        }

        Optional<ConfigSection> actions = config.getSection("actions");
        if (!actions.isPresent()) return;

        actions.get().getValues().forEach((k, v) -> menu.buttonOfChar(v.getAs(String.class).charAt(0)).ifPresent(button -> button.action(v.getKey())));
    }

    private static OMenuButton initButton(ConfigSection section) {
        OMenuButton button = new OMenuButton(section.getKey().charAt(0));
        section.ifValuePresent("permission", String.class, button::requiredPermission);
        section.ifValuePresent("required permission", String.class, button::requiredPermission);

        if (section.isValuePresent("material"))
            button.addState("default", new OMenuButton.ButtonItemBuilder(ItemBuilder.fromConfiguration(section)));

        for (ConfigSection stateSection : section.getSections().values()) {
            button.addState(stateSection.getKey(), new OMenuButton.ButtonItemBuilder(ItemBuilder.fromConfiguration(stateSection)));
        }

        return button;
    }

    public static void loadBackPackMenu(Config config, BackPackViewMenu menu) {
        menu.setTitle(ChatColor.translateAlternateColorCodes('&', config.getAs("title")));

        List<String> layout = config.getAs("layout", List.class);
        menu.setMenuRows(layout.size());

        Map<Character, String> charToActionMap = Maps.newHashMap();
        if (config.isValuePresent("actions")) {
            for (String action : (List<String>) config.getAs("actions")) {
                String[] split = action.split(":");
                charToActionMap.put(split[1].charAt(0), split[0]);
            }
        }

        if (menu instanceof OMenu.Placeholderable)
            ((OMenu.Placeholderable) menu).initPlaceholderable(config);

        if (menu instanceof OMenu.Templateable)
            ((OMenu.Templateable) menu).initTemplateable(config);

        ConfigSection buttonsSection = config.getSection("buttons").get();
        for (String layoutRow : layout) {
            boolean isBottom = false;
            if (layoutRow.toLowerCase().startsWith("bottom:")) {
                isBottom = true;
                layoutRow = layoutRow.substring(7);
            } else
                layoutRow = layoutRow.substring(4);

            boolean finalIsBottom = isBottom;
            BiConsumer<Integer, OMenuButton> setter = (index, button) ->
                    Objects.requireNonNull((finalIsBottom ? menu.getBottom() : menu.getTop()), "Array is null")[index] = button == null ? new OMenuButton('+').currentItem(new ItemStack(Material.AIR)) : button;

            int index = 0;
            for (char c : layoutRow.toCharArray()) {
                if (c == ' ') continue;

                Optional<ConfigSection> optSection = buttonsSection.getSection(c + "");
                if (!optSection.isPresent()) {
                    setter.accept(index, null);
                    index++;
                    continue;
                }

                ConfigSection section = optSection.get();

                OMenuButton button = initButton(section);
                String action = charToActionMap.get(c);
                if (action != null)
                    button.action(action);

                setter.accept(index, button);
                index++;
            }
        }

        // For templates
        for (ConfigSection buttonSection : buttonsSection.getSections().values()) {
            Character ch = buttonSection.getKey().charAt(0);
            if (menu instanceof OMenu.Templateable && ((OMenu.Templateable) menu).containsTemplate(ch + "")) {
                OMenuButton button = initButton(buttonSection);
                String action = charToActionMap.get(ch);
                if (action != null)
                    button.action(action);

                ((OMenu.Templateable) menu).getTemplateButtonMap().put(ch + "", button);
            }
        }

        Optional<ConfigSection> actions = config.getSection("actions");
        if (!actions.isPresent()) return;

        actions.get().getValues().forEach((k, v) -> menu.buttonOfChar(v.getAs(String.class).charAt(0)).ifPresent(button -> button.action(v.getKey())));
    }
}
