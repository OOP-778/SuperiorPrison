package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.google.common.collect.Sets;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.button.AMenuButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public abstract class EditMenuHelper {

    public abstract AMenu build(SNormalMine mine);

    public ItemStack parsePlaceholders(ItemStack itemStack, SNormalMine mine) {
        OItem item = new OItem(itemStack.clone());

        // Parse display name
        item.setDisplayName(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getDisplayName(),  mine));

        // Parse lore
        item.setLore(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getLore(), mine));

        return item.getItemStack();
    }

    public void updateButton(AMenuButton button, SNormalMine mine) {
        if (!button.containsData("placeholder"))
            button.saveCurrentItem("placeholder");

        ItemStack parsed = parsePlaceholders(button.grab("placeholder", ItemStack.class).get(), mine);
        button.currentItem(parsed);
    }

    public void parseButtons(AMenu menu, SNormalMine mine) {
        Set<AMenuButton> buttons = Sets.newHashSet();
        buttons.addAll(menu.buttons());
        if (menu.designer() != null)
            buttons.addAll(menu.designer().getButtons());

        buttons.stream()
                .filter(button -> !button.template() && button.currentItem().hasItemMeta())
                .forEach(button -> updateButton(button, mine));
    }

}
