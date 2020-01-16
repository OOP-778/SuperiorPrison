package com.bgsoftware.superiorprison.plugin.util.menu;

import com.google.common.collect.Maps;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;

public class MenuLoader {

    public static void loadMenu(OMenu menu, OConfiguration configuration) {
        menu.setTitle(ChatColor.translateAlternateColorCodes('&', configuration.getValueAsReq("title")));

        List<String> layout = configuration.getValueAsReq("layout", List.class);
        menu.setMenuRows(layout.size());

        Map<Character, String> charToActionMap = Maps.newHashMap();
        if (configuration.hasValue("actions")) {
            for (String action : (List<String>) configuration.getValueAsReq("actions")) {
                String[] split = action.split(":");
                charToActionMap.put(split[1].charAt(0), split[0]);
            }
        }

        if (menu instanceof OMenu.Placeholderable)
            ((OMenu.Placeholderable) menu).initPlaceholderable(configuration);

        if (menu instanceof OMenu.Templateable)
            ((OMenu.Templateable) menu).initTemplateable(configuration);

        ConfigurationSection buttonsSection = configuration.getSection("buttons");
        for (int a = 0; a < layout.size(); a++) {
            String row = layout.get(a);
            int slot = a * 9;

            for (int b = 0; b < row.length(); b++) {
                char ch = row.charAt(b);
                if (ch == ' ') continue;

                ConfigurationSection section = buttonsSection.getSection(ch + "");
                if (section == null) {
                    slot++;
                    continue;
                }

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
        for (ConfigurationSection buttonSection : buttonsSection.getSections().values()) {
            Character ch = buttonSection.getKey().charAt(0);
            if (menu instanceof OMenu.Templateable && ((OMenu.Templateable) menu).containsTemplate(ch + "")) {
                OMenuButton button = initButton(buttonSection);
                String action = charToActionMap.get(ch);
                if (action != null)
                    button.action(action);
                ((OMenu.Templateable) menu).getTemplateButtonMap().put(ch + "", button);
            }
        }

        ConfigurationSection actions = configuration.getSection("actions");
        if (actions == null) return;

        actions.getValues().forEach((k, v) -> menu.buttonOfChar(v.getValueAsReq(String.class).charAt(0)).ifPresent(button -> button.action(v.getKey())));
    }

    private static OMenuButton initButton(ConfigurationSection section) {
        OMenuButton button = new OMenuButton(section.getKey().charAt(0));
        section.ifValuePresent("permission", String.class, button::requiredPermission);

        if (section.isPresentValue("material"))
            button.addState("default", new OMenuButton.ButtonItemBuilder(ItemBuilder.fromConfiguration(section)));

        for (ConfigurationSection stateSection : section.getSections().values())
            button.addState(stateSection.getKey(), new OMenuButton.ButtonItemBuilder(ItemBuilder.fromConfiguration(stateSection)));

        return button;
    }

}
