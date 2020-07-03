package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import org.bukkit.inventory.Inventory;

import java.util.*;

import static com.oop.orangeengine.main.Engine.getEngine;

public abstract class OPagedMenu<T> extends OMenu {

    @Getter
    private final Map<Integer, T> items = new HashMap<>();
    private final Map<Integer, OMenuButton> buttons = new HashMap<>();

    @Getter
    private int currentPage = 1;

    @Getter
    private SwitchEnum switchAction = null;

    private int pages;

    public OPagedMenu(String identifier, SPrisoner viewer) {
        super(identifier, viewer);

        ClickHandler
                .of("next page")
                .handle(event -> {
                    if (pages == currentPage)
                        return;

                    currentPage += 1;
                    switchAction = SwitchEnum.NEXT;
                    refresh(() -> switchAction = null);
                })
                .apply(this);

        ClickHandler
                .of("previous page")
                .handle(event -> {
                    if (currentPage == 1)
                        return;

                    currentPage -= 1;
                    switchAction = SwitchEnum.PREVIOUS;
                    refresh(() -> switchAction = null);
                })
                .apply(this);
    }

    @Override
    public Inventory getInventory() {
        items.clear();
        buttons.clear();

        List<T> allItems = requestObjects();
        pages = getPages();
        Inventory inventory = Objects
                .requireNonNull(buildInventory(getTitle().replace("{current_page}", currentPage + "").replace("{pages_available}", pages + ""), getViewer()), "Invalid Inventory");

        if (allItems.isEmpty()) {
            buttonOf(button -> button.action().contentEquals("next page")).ifPresent(button -> {
                if (!button.containsState("hidden")) return;

                inventory.setItem(button.slot(), button.currentItem(button.getStateItem("hidden").getItemStackWithPlaceholdersMulti(getViewer())).currentItem());
            });

            buttonOf(button -> button.action().contentEquals("previous page")).ifPresent(button -> {
                if (!button.containsState("hidden")) return;

                inventory.setItem(button.slot(), button.currentItem(button.getStateItem("hidden").getItemStackWithPlaceholdersMulti(getViewer())).currentItem());
            });
            return inventory;
        }

        List<Integer> emptySlots = getEmptySlots();
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
            if (currentPage == pages) {
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
        double pagesNotRounded = (float) requestObjects().size() / getEmptySlots().size();
        String[] split = String.valueOf(pagesNotRounded).split("\\.");
        int pages = (int) pagesNotRounded;

        if (split.length == 2 && Integer.parseInt(String.valueOf(split[1].toCharArray()[0])) > 0)
            pages = pages + 1;

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

        usedSlots.addAll(buttons.keySet());
        for (int slot = 0; slot < (getMenuRows() * 9); slot++) {
            if (!usedSlots.contains(slot))
                emptySlots.add(slot);
        }

        return emptySlots;
    }

    public T requestObject(int slot) {
        return items.get(slot);
    }

    public static enum SwitchEnum {
        NEXT,
        PREVIOUS
    }
}
