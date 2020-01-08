package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.oop.orangeengine.menu.button.AMenuButton;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class OPagedMenu<T> extends OMenu {

    private List<T> items = new ArrayList<>();
    private int currentPage = 1;

    public OPagedMenu(String identifier, SPrisoner viewer) {
        super(identifier, viewer);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = getInventory();

        items = requestObjects();
        List<Integer> emptySlots = getEmptySlots();

        for (int slot : emptySlots) {
            int objectIndex = slot + (emptySlots.size() * currentPage -1);
            if (objectIndex < items.size())
                inventory.setItem(emptySlots.get(slot), toButton(items.get(objectIndex)).getDefaultStateItem().getItemStackWithPlaceholders(getViewer()));
        }

        return inventory;
    }

    public abstract List<T> requestObjects();

    public abstract OMenuButton toButton(T obj);

    public List<Integer> getEmptySlots() {
        List<Integer> emptySlots = Lists.newArrayList();
        Set<Integer> usedSlots = Sets.newHashSet(getFillerItems().keySet());
        for (int slot = 0; slot < (getMenuRows() * 9); slot++)
            if (!usedSlots.contains(slot))
                emptySlots.add(slot);

        return emptySlots;
    }
}
