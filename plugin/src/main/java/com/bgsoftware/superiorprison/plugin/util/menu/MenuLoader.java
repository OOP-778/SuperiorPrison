package com.bgsoftware.superiorprison.plugin.util.menu;

import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.yaml.ConfigurationSection;
import com.oop.orangeengine.yaml.OConfiguration;
import org.bukkit.ChatColor;

import java.util.List;

public class MenuLoader {

    public static void loadMenu(OMenu menu, OConfiguration configuration) {
        menu.setTitle(ChatColor.translateAlternateColorCodes('&', configuration.getValueAsReq("title")));

        List<String> layout = configuration.getValueAsReq("layout", List.class);
        menu.setMenuRows(layout.size());

        for (int a = 0; a < layout.size(); a++) {
            String row = layout.get(a);
            int slot = a * 9;

            for (int b = 0; b < row.length(); b++) {
                char ch = row.charAt(b);
                if (ch == ' ') continue;

                ConfigurationSection section = configuration.getSection("buttons." + ch);
                OMenuButton button = new OMenuButton(ch);
                if (section.getSections().size() == 0)
                    button.addState("default", (OMenuButton.ButtonItemBuilder) new OMenuButton.ButtonItemBuilder().load(section));

                else
                    for (ConfigurationSection stateSection : section.getSections().values())
                        button.addState(stateSection.getKey(), (OMenuButton.ButtonItemBuilder) new OMenuButton.ButtonItemBuilder().load(stateSection));

                menu.getFillerItems().put(slot, new OMenuButton(ch));
                slot++;
            }
        }
    }

}
