package com.bgsoftware.superiorprison.plugin.menu.edit;

import com.bgsoftware.superiorprison.api.data.mine.shop.ShopItem;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.google.common.collect.Sets;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.menu.AMenu;
import com.oop.orangeengine.menu.WrappedInventory;
import com.oop.orangeengine.menu.button.AMenuButton;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;

import static com.oop.orangeengine.main.Helper.capitalizeAll;

public abstract class EditMenuHelper {

    public abstract AMenu build(SNormalMine mine);

    public ItemStack parsePlaceholders(@NonNull ItemStack itemStack, SNormalMine mine) {
        OItem item = new OItem(itemStack.clone());

        // Parse display name
        item.setDisplayName(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getDisplayName(), mine));

        // Parse lore
        item.setLore(SuperiorPrisonPlugin.getInstance().getPlaceholderController().parse(item.getLore(), mine));

        return item.getItemStack();
    }

    public ItemStack parsePlaceholders(ItemStack itemStack, ShopItem shopItem) {
        OItem item = new OItem(itemStack.clone());

        // Parse display name
        item.setDisplayName(item.getDisplayName().replaceAll("%item_name%", shopItem.getItem().getItemMeta().hasDisplayName() ? shopItem.getItem().getItemMeta().getDisplayName() : beautify(shopItem.getItem().getType().name())));

        // Parse lore
        item.replaceInLore("%item_name%", shopItem.getItem().getItemMeta().hasDisplayName() ? shopItem.getItem().getItemMeta().getDisplayName() : beautify(shopItem.getItem().getType().name()));
        item.replaceInLore("%item_buy_price%", shopItem.getBuyPrice() + "");
        item.replaceInLore("%item_sell_price%", shopItem.getSellPrice() + "");

        return item.getItemStack();
    }

    public void updateButton(AMenuButton button, SNormalMine mine) {
        if (!button.containsData("placeholder_itemstack"))
            button.saveCurrentItem("placeholder_itemstack");

        ItemStack parsed = parsePlaceholders(button.grab("placeholder_itemstack", ItemStack.class).get(), mine);
        button.currentItem(parsed);
    }

    public void updateButton(AMenuButton button, ShopItem item) {
        if (!button.containsData("placeholder_itemstack"))
            button.saveCurrentItem("placeholder_itemstack");

        ItemStack parsed = parsePlaceholders(button.grab("placeholder_itemstack", ItemStack.class).get(), item);
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

    public void parseButtons(WrappedInventory wrappedInventory, SNormalMine mine) {
        Arrays.stream(wrappedInventory.getArrayButtons())
                .filter(button -> !button.template() && button.currentItem().hasItemMeta())
                .forEach(button -> updateButton(button, mine));
    }

    public String beautify(String text) {
        return capitalizeAll(text.toLowerCase().replace("_", " "));
    }

}
