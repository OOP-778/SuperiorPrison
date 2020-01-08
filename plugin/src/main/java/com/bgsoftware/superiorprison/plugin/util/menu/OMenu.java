package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.ConfigController;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ReplacerUtils;
import com.google.common.collect.Maps;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Getter
public abstract class OMenu implements InventoryHolder {

    private OMenu previousMenu;
    private final SPrisoner viewer;
    private final String identifier;

    protected boolean previousMove = true;
    private boolean refreshing = false;

    @Getter
    private Map<Integer, OMenuButton> fillerItems = Maps.newHashMap();

    private Map<String, ClickHandler> clickHandlers = Maps.newHashMap();

    @Setter
    private String title;

    @Setter
    private int menuRows;

    public OMenu(String identifier, SPrisoner viewer) {
        this.identifier = identifier;
        this.viewer = viewer;
        init();
    }

    private void init() {
        ConfigController cc = SuperiorPrisonPlugin.getInstance().getConfigController();
        OConfiguration configuration = cc.getMenus().get(identifier.toLowerCase());

        MenuLoader.loadMenu(this, Objects.requireNonNull(configuration, "Failed to find configuration for menu " + identifier));
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(viewer);
    }

    protected static <T extends OMenu> void refreshMenus(Class<T> menuClazz) {
        runActionOnMenus(menuClazz, menu -> true, ((player, menu) -> {
            menu.previousMove = false;
            menu.open(menu.previousMenu);
        }));
    }

    protected static <T extends OMenu> void destroyMenus(Class<T> menuClazz) {
        destroyMenus(menuClazz, OMenu -> true);
    }

    protected Inventory buildInventory(Object object) {
        Set<BiFunction<String, Object, String>> placeholders = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
        String title = ReplacerUtils.replaceText(object, getTitle(), placeholders, SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class));

        Inventory inventory = Bukkit.createInventory(this, menuRows * 9, title);
        fillerItems.forEach((slot, button) -> inventory.setItem(slot, button.getDefaultStateItem().getItemStackWithPlaceholders(object, placeholders)));

        return inventory;
    }

    protected static <T extends OMenu> void destroyMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, OMenu) -> player.closeInventory()));
    }

    private static <T extends OMenu> void runActionOnMenus(Class<T> menuClazz, Predicate<T> predicate, MenuCallback callback) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();

            if (menuClazz.isInstance(inventoryHolder) && predicate.test((T) inventoryHolder)) {
                OMenu OMenu = (OMenu) inventoryHolder;
                callback.run(player, OMenu);
            }
        }
    }

    private interface MenuCallback {
        void run(Player player, OMenu OMenu);
    }

    public void open(OMenu previousMenu) {
        if (Bukkit.isPrimaryThread()) {
            StaticTask.getInstance().async(() -> open(previousMenu));
            return;
        }

        Inventory inventory = getInventory();

        StaticTask.getInstance().sync(() -> {
            Player player = viewer.getPlayer();
            if (player == null)
                return;

            OMenu currentMenu = null;
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
            if (inventoryHolder instanceof OMenu)
                currentMenu = (OMenu) inventoryHolder;

            if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
                return;

            player.openInventory(inventory);

            refreshing = false;
            this.previousMenu = previousMenu != null ? previousMenu : previousMove ? currentMenu : null;
        });
    }

    public void closeInventory() {
        if (previousMenu != null) {
            StaticTask.getInstance().sync(() -> {
                if (previousMove)
                    previousMenu.open(previousMenu.previousMenu);
                else
                    previousMove = true;
            });
        }
    }

    public void handleDragItem(InventoryMoveItemEvent event) {}

    public void handleClick() {}

    public int slotOf(Predicate<OMenuButton> predicate) {
        return fillerItems.values()
                .stream()
                .filter(predicate)
                .mapToInt(OMenuButton::getSlot)
                .findFirst()
                .orElse(-1);
    }

    public int slotOfAction(String action) {
        return slotOf(button -> button.getAction() != null && button.getAction().equalsIgnoreCase(action));
    }

}
