package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.ConfigController;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ReplacerUtils;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.yaml.OConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Getter
public abstract class OMenu implements InventoryHolder {

    private Map<String, Object> data = Maps.newConcurrentMap();

    private OMenu previousMenu;
    private final SPrisoner viewer;
    private final String identifier;

    protected boolean previousMove = true;
    private boolean refreshing = false;

    @Getter
    private Map<Integer, OMenuButton> fillerItems = Maps.newHashMap();

    private Set<OMenuButton> miscButtons = Sets.newHashSet();

    private Map<String, ClickHandler> clickHandlers = Maps.newHashMap();

    @Setter
    private String title;

    @Setter
    private int menuRows;

    public OMenu(String identifier, SPrisoner viewer) {
        this.identifier = identifier;
        this.viewer = viewer;
        _init();

        ClickHandler
                .of("return")
                .handle(event -> {
                    if (previousMenu == null)
                        return;

                    previousMove = false;
                    open(previousMenu);
                })
                .apply(this);
    }

    private void _init() {
        ConfigController cc = SuperiorPrisonPlugin.getInstance().getConfigController();
        OConfiguration configuration = cc.getMenus().get(identifier.toLowerCase());

        MenuLoader.loadMenu(this, Objects.requireNonNull(configuration, "Failed to find configuration for menu " + identifier));
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(viewer);
    }

    public static <T extends OMenu> void refreshMenus(Class<T> menuClazz) {
        runActionOnMenus(menuClazz, menu -> true, ((player, menu) -> {
            menu.previousMove = false;
            menu.open(menu);
        }));
    }

    public static <T extends OMenu> void refreshMenus(Class<T> menuClazz, Predicate<T> filter) {
        runActionOnMenus(menuClazz, menu -> true, ((player, menu) -> {
            if (!filter.test((T) menu)) return;
            menu.previousMove = false;
            menu.open(menu);
        }));
    }

    public static <T extends OMenu> void closeMenus(Class<T> menuClazz) {
        closeMenus(menuClazz, OMenu -> true);
    }

    protected Inventory buildInventory(Object object) {
        Set<BiFunction<String, Object, String>> placeholders = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
        String title = ReplacerUtils.replaceText(object, getTitle(), placeholders, SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class));

        Inventory inventory = Bukkit.createInventory(this, menuRows * 9, title);
        fillerItems.forEach((slot, button) -> inventory.setItem(slot, button.getDefaultStateItem().getItemStackWithPlaceholders(object, placeholders)));

        return inventory;
    }

    public static <T extends OMenu> void closeMenus(Class<T> menuClazz, Predicate<T> predicate) {
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

    public void open(OMenu menu) {
        if (Bukkit.isPrimaryThread()) {
            StaticTask.getInstance().async(() -> open(menu));
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
            this.previousMenu = menu != null ? menu : previousMove ? currentMenu : null;
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

    public void handleDrag(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            OMenuButton button = getButtons().get(event.getRawSlot());
            if (button == null) return;

            ClickHandler clickHandler = clickHandlerFor(button);
            if (clickHandler == null || !clickHandler.doesAcceptEvent(event)) return;

            clickHandler.handle(event);
        }
    }

    public int slotOf(Predicate<OMenuButton> predicate) {
        return getButtons()
                .stream()
                .filter(predicate)
                .mapToInt(OMenuButton::getSlot)
                .findFirst()
                .orElse(-1);
    }

    public int slotOfAction(String action) {
        return slotOf(button -> button.getAction() != null && button.getAction().equalsIgnoreCase(action));
    }

    public Optional<OMenuButton> buttonOf(Predicate<OMenuButton> predicate) {
        return getButtons()
                .stream()
                .filter(predicate)
                .findFirst();
    }

    public Optional<OMenuButton> buttonOfChar(char ch) {
        return buttonOf(button -> button.getIdentifier() == ch);
    }

    public List<OMenuButton> getButtons() {
        return new ArrayList<>(fillerItems.values());
    }

    private ClickHandler clickHandlerFor(OMenuButton button) {
        return clickHandlers.get(button.getAction().toLowerCase());
    }

    /*
    For different types menu loadings
    */
    public static interface Mappable {
        Map<String, Object> getMap();
    }

    public static interface Placeholderable extends Mappable {

        // Placeholders are stored like so: placeholder=button char / action
        default HashBiMap<String, String> getPlaceholderMap() {
            return (HashBiMap<String, String>) getMap().computeIfAbsent("placeholders", (key) -> HashBiMap.create(new HashMap<String, String>()));
        }

        default boolean containsPlaceholder(String buttonId) {
            return getPlaceholderMap().containsKey(buttonId);
        }

        default Optional<String> getPlaceholderFromIdentifier(String identifier) {
            return Optional.ofNullable(getPlaceholderMap().inverse().get(identifier));
        }

        default Optional<String> getIdentifierFromPlaceholder(String placeholder) {
            return Optional.ofNullable(getPlaceholderMap().get(placeholder));
        }

        default void initPlaceholderable(OConfiguration configuration) {
            List<String> stringPlaceholders = (List<String>) configuration.getValueAsReq("placeholders", List.class);
            HashBiMap<String, String> placeholders = HashBiMap.create();

            for (String placeholder : stringPlaceholders) {
                String[] split = placeholder.split(":");
                placeholders.put(split[0], split[1]);
            }

            getPlaceholderMap().putAll(placeholders);
        }
    }

    public static interface Templateable extends Mappable {

        // Placeholders are stored like so: template=button char / action
        default HashBiMap<String, String> getTemplateMap() {
            return (HashBiMap<String, String>) getMap().computeIfAbsent("templates", (key) -> HashBiMap.create(new HashMap<String, String>()));
        }

        default HashMap<String, OMenuButton> getTemplateButtonMap() {
            return (HashMap<String, OMenuButton>) getMap().computeIfAbsent("templateButtons", (key) -> new HashMap<String, OMenuButton>());
        }

        default boolean containsTemplate(String buttonId) {
            return getTemplateMap().containsKey(buttonId);
        }

        default Optional<String> getTemplateFromIdentifier(String identifier) {
            return Optional.ofNullable(getTemplateMap().inverse().get(identifier));
        }

        default Optional<String> getIdentifierFromTemplate(String template) {
            return Optional.ofNullable(getTemplateMap().get(template));
        }

        default Optional<OMenuButton> getTemplateButtonFromIdentifier(String identifier) {
            return Optional.ofNullable(getTemplateButtonMap().get(identifier));
        }

        default Optional<OMenuButton> getTemplateButtonFromTemplate(String template) {
            return getTemplateButtonFromIdentifier(getIdentifierFromTemplate(template).orElse("nothing"));
        }

        default void initTemplateable(OConfiguration configuration) {
            List<String> stringPlaceholders = (List<String>) configuration.getValueAsReq("templates", List.class);
            HashBiMap<String, String> placeholders = HashBiMap.create();

            for (String placeholder : stringPlaceholders) {
                String[] split = placeholder.split(":");
                placeholders.put(split[0], split[1]);
            }

            getTemplateMap().putAll(placeholders);
        }
    }
}
