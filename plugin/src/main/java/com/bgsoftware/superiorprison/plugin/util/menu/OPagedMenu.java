package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import org.bukkit.inventory.Inventory;

import java.util.*;

public abstract class OPagedMenu<T> extends OMenu {

    private Map<Integer, T> items = Maps.newConcurrentMap();
    private Map<Integer, OMenuButton> buttons = Maps.newConcurrentMap();
    private List<Integer> emptySlots = new ArrayList<>();

    private int currentPage = 1;

    public OPagedMenu(String identifier, SPrisoner viewer) {
        super(identifier, viewer);

        ClickHandler
                .of("next page")
                .handle(event -> {
                    if (getPages() == currentPage)
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
        Inventory inventory = buildInventory(getTitle().replace("{current_page}", currentPage + "").replace("{pages_available}", getPages() + ""), getViewer());

        List<T> allItems = requestObjects();
        if (allItems.isEmpty()) return inventory;

        items.clear();
        emptySlots = getEmptySlots();
        for (int i = 0; i < emptySlots.size(); i++) {
            int objectIndex = i + (emptySlots.size() * (currentPage - 1));
            if (objectIndex >= allItems.size()) break;

            Optional<OMenuButton> pagedButton = Optional.ofNullable(toButton(allItems.get(objectIndex)));
            if (pagedButton.isPresent()) {
                pagedButton.get().slot(emptySlots.get(i));
                inventory.setItem(pagedButton.get().slot(), pagedButton.get().currentItem());
                items.put(pagedButton.get().slot(), allItems.get(objectIndex));
                buttons.put(pagedButton.get().slot(), pagedButton.get());

            } else
                inventory.setItem(emptySlots.get(i), new OItem(OMaterial.RED_STAINED_GLASS_PANE).setDisplayName("&cFailed to convert object into paged button").getItemStack());
        }

        Optional<OMenuButton> nextPageButton = buttonOf(button -> button.action().contentEquals("next page"));
        Optional<OMenuButton> previousPageButton = buttonOf(button -> button.action().contentEquals("previous page"));

        if (nextPageButton.isPresent()) {
            OMenuButton button = nextPageButton.get();
            if (currentPage == getPages()) {
                if (button.containsState("hidden"))
                    inventory.setItem(button.slot(), button.currentItem(button.getStateItem("hidden").getItemStackWithPlaceholdersMulti(getViewer())).currentItem());

                else
                    inventory.setItem(button.slot(), OMaterial.AIR.parseItem());
            } else
                inventory.setItem(button.slot(), button.currentItem(button.getStateItem("shown").getItemStackWithPlaceholders(getViewer())).currentItem());
        }

        if (previousPageButton.isPresent()) {
            OMenuButton button = previousPageButton.get();
            if (currentPage == 1)
                if (button.containsState("hidden"))
                    inventory.setItem(button.slot(), button.currentItem(button.getStateItem("hidden").getItemStackWithPlaceholdersMulti(getViewer())).currentItem());

                else
                    inventory.setItem(previousPageButton.get().slot(), button.currentItem(OMaterial.AIR.parseItem()).currentItem());
            else
                inventory.setItem(button.slot(), button.currentItem(button.getStateItem("shown").getItemStackWithPlaceholders(getViewer())).currentItem());
        }

        return inventory;
    }

    private int getPages() {
        int pages = Math.round(requestObjects().size() / getEmptySlots().size());
        if (pages == 0)
            return 1;

        return pages;
    }

    public abstract List<T> requestObjects();

    public abstract OMenuButton toButton(T obj);

    @Override
    public List<OMenuButton> getButtons() {
        List<OMenuButton> buttons = super.getButtons();
        buttons.addAll(this.buttons.values());
        return buttons;
    }

    public List<Integer> getEmptySlots() {
        List<Integer> emptySlots = Lists.newArrayList();
        Set<Integer> usedSlots = Sets.newHashSet();
        if (this instanceof Placeholderable)
            getFillerItems().forEach((slot, button) -> {
                if (((Placeholderable) this).containsPlaceholder(button.identifier() + ""))
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

    public T requestObject(int slot) {
        return items.get(slot);
    }
}
