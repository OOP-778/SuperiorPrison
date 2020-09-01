package com.bgsoftware.superiorprison.plugin.menu.backpack;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.AdvancedBackPackConfig;
import com.bgsoftware.superiorprison.plugin.constant.LocaleEnum;
import com.bgsoftware.superiorprison.plugin.controller.ConfigController;
import com.bgsoftware.superiorprison.plugin.object.backpack.SBackPack;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.menu.*;
import com.google.common.base.Preconditions;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;

@Getter
public class BackPackViewMenu extends OPagedMenu<ItemStack> {

    private SBackPack backPack;
    private final OMenuButton[] top = new OMenuButton[9];
    private final OMenuButton[] bottom = new OMenuButton[9];

    public BackPackViewMenu(SPrisoner viewer, SBackPack backPack) {
        super("backpackview", viewer);
        Preconditions.checkArgument(backPack.getConfig() instanceof AdvancedBackPackConfig, "This menu only works on AdvancedBackPack!");

        this.backPack = backPack;
        backPack.setCurrentView(this);

        getStateRequester()
                .registerRequest("sellContentsFlag", button -> getToggleableState(button, backPack.getData().isSell()));

        clickHandler("upgrade")
                .handle(event -> move(new BackPackUpgradeMenu(getViewer(), backPack)));

        clickHandler("sellContentsFlag")
                .handle(event -> {
                    backPack.getData().setSell(!backPack.getData().isSell());
                    refresh();
                });

        ConfigController cc = SuperiorPrisonPlugin.getInstance().getConfigController();
        Config configuration = cc.getMenus().get(getIdentifier().toLowerCase());

        MenuLoader.loadBackPackMenu(Objects.requireNonNull(configuration, "Failed to find configuration for menu " + getIdentifier()), this);

        initializeBackpack();
    }

    void initializeBackpack() {
        int plus = (isEmpty(top) ? 0 : 1) + (isEmpty(bottom) ? 0 : 1) + ((AdvancedBackPackConfig) backPack.getConfig()).getRows();
        setMenuRows(Math.min(plus, 6));

        getItems().clear();
        getFillerItems().clear();
        getEmptySlots().clear();

        for (int i = 0; i < 2; i++) {
            int startSlot = i == 0 ? 0 : (getMenuRows() * 9) - 9;
            int endSlot = i == 0 ? 9 : getMenuRows() * 9;

            int finalI = i;
            Function<Integer, OMenuButton> getter = index ->
                    (finalI == 0 ? top : bottom)[index];

            int index = 0;
            for (int slot = startSlot; i < endSlot; slot++) {
                if (index == 9) break;
                getFillerItems().put(slot, getter.apply(index).slot(slot));
                index++;
            }
        }
    }

    public void onUpgrade() {
        initializeBackpack();
    }

    void updatePage(int page, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();

        if (!isEmpty(top))
            contents = Arrays.copyOfRange(contents, 9, contents.length);

        if (!isEmpty(bottom))
            contents = Arrays.copyOfRange(contents, 0, contents.length - 9);

        int startingIndex = page == 1 ? 0 : page * 9;
        System.out.println("starting slot: " + startingIndex);
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null)
                System.out.println("Setting item at index " + startingIndex);
            backPack.getData().setItem(startingIndex, contents[i]);
            startingIndex++;
        }
    }

    @Override
    public void closeInventory(InventoryCloseEvent event) {
        super.closeInventory(event);

        // If action is null, save the inventory page
        if (getCurrentAction() != null) {
            if (getSwitchAction() != null) {
                int updatingPage = getSwitchAction() == SwitchEnum.NEXT ? getCurrentPage() - 1 : getCurrentPage() + 1;
                updatePage(updatingPage, event.getInventory());

            } else
                updatePage(getCurrentPage(), event.getInventory());
            return;
        }

        updatePage(getCurrentPage(), event.getInventory());

        backPack.save();
        backPack.setCurrentView(null);

        if (!event.getPlayer().getInventory().addItem(backPack.getItem()).isEmpty()) {
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), backPack.getItem());
            LocaleEnum.BACKPACK_DROPPED_INVENTORY_FULL.getWithPrefix().send(event.getPlayer());
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            Optional<OMenuButton> menuButton = getButtons()
                    .stream()
                    .filter(button -> button.slot() == event.getRawSlot())
                    .findFirst();
            if (!menuButton.isPresent()) return;

            ButtonClickEvent buttonClickEvent = new ButtonClickEvent(event, menuButton.get());
            Optional<ClickHandler> clickHandler = clickHandlerFor(buttonClickEvent);
            if (!clickHandler.isPresent()) {
                if (!buttonClickEvent.getButton().action().contentEquals("backpack-item"))
                    event.setCancelled(true);

                return;
            }

            event.setCancelled(true);
            clickHandler.get().handle(buttonClickEvent);
        }
    }

    @Override
    public void handleBottomClick(InventoryClickEvent event) {
        event.setCancelled(false);
    }

    @Override
    public List<ItemStack> requestObjects() {
        return backPack.getStored();
    }

    @Override
    public OMenuButton toButton(ItemStack obj) {
        return new OMenuButton('+')
                .currentItem(obj == null ? new ItemStack(Material.AIR) : obj)
                .action("backpack-item");
    }

    @Override
    protected void _init() {
    }

    private boolean isEmpty(OMenuButton[] array) {
        for (OMenuButton oMenuButton : array) {
            if (oMenuButton != null) return false;
        }
        return true;
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {}

    private OMenuButton.ButtonItemBuilder getToggleableState(OMenuButton button, boolean state) {
        if (state)
            return button.getStateItem("enabled");

        else
            return button.getStateItem("disabled");
    }
}
