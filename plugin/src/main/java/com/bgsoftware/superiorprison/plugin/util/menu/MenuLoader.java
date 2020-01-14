package com.bgsoftware.superiorprison.plugin.util.menu;

import com.google.common.collect.Maps;
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

        if (menu instanceof OMenu.Placeholderable)
            ((OMenu.Placeholderable) menu).initPlaceholderable(configuration);

        if (menu instanceof OMenu.Templateable)
            ((OMenu.Templateable) menu).initTemplateable(configuration);

        for (int a = 0; a < layout.size(); a++) {
            String row = layout.get(a);
            int slot = a * 9;

            for (int b = 0; b < row.length(); b++) {
                char ch = row.charAt(b);
                if (ch == ' ') continue;

                ConfigurationSection section = configuration.getSection("buttons." + ch);
                OMenuButton button = new OMenuButton(ch);

                section.ifValuePresent("permission", String.class, button::setRequiredPermission);

                if (section.getSections().size() == 0)
                    button.addState("default", (OMenuButton.ButtonItemBuilder) new OMenuButton.ButtonItemBuilder().load(section));

                else
                    for (ConfigurationSection stateSection : section.getSections().values())
                        button.addState(stateSection.getKey(), (OMenuButton.ButtonItemBuilder) new OMenuButton.ButtonItemBuilder().load(stateSection));

                if (menu instanceof OMenu.Templateable && ((OMenu.Templateable) menu).containsTemplate(ch + "")) {
                    ((OMenu.Templateable) menu).getTemplateButtonMap().put(((OMenu.Templateable) menu).getTemplateFromIdentifier(ch + "").orElse("-"), button);
                    continue;
                }

                menu.getFillerItems().put(slot, new OMenuButton(ch));
                button.setSlot(slot);
                slot++;
            }
        }

        ConfigurationSection actions = configuration.getSection("actions");
        if (actions == null) return;

        actions.getValues().forEach((k, v) -> menu.buttonOfChar(v.getValueAsReq(String.class).charAt(0)).ifPresent(button -> button.setAction(v.getKey())));
    }

}
