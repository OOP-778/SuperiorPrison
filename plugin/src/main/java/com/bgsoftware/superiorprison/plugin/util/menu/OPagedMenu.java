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
    private List<Integer> emptySlots = new ArrayList<>();

    private int currentPage = 1;

    public OPagedMenu(String identifier, SPrisoner viewer) {
        super(identifier, viewer);

        ClickHandler
                .of("next page")
                .handle(event -> {
                    boolean nextPage = emptySlots.size() * currentPage < items.size();
                    if (!nextPage)
                        return;

                    currentPage += 1;
                    previousMove = false;
                    open(getPreviousMenu());
                })
                .apply(this);

        ClickHandler
                .of("previous page")
                .handle(event -> {
                    if (currentPage == 1)
                        return;

                    currentPage -= 1;
                    previousMove = false;
                    open(getPreviousMenu());
                })
                .apply(this);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = getInventory();

        items = requestObjects();
        emptySlots = getEmptySlots();

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
        Set<Integer> usedSlots = Sets.newHashSet();
        if (this instanceof Placeholderable)
            getFillerItems().forEach((slot, button) -> {
                if (((Placeholderable)this).containsPlaceholder(button.getIdentifier() + ""))
                    return;

                usedSlots.add(slot);
            });
        else
            usedSlots.addAll(getFillerItems().keySet());

        for (int slot = 0; slot < (getMenuRows() * 9); slot++)
            if (!usedSlots.contains(slot))
                emptySlots.add(slot);

        return emptySlots;
    }
}
