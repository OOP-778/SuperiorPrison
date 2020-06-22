package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.ConfigController;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.bgsoftware.superiorprison.plugin.util.SPair;
import com.bgsoftware.superiorprison.plugin.util.TextUtil;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.CancelledPacketHandleException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Getter
public abstract class OMenu implements InventoryHolder {

    private final SPrisoner viewer;
    private final String identifier;
    protected boolean previousMove = true;
    private final Map<String, Object> data = Maps.newConcurrentMap();
    private OMenu previousMenu;

    @Getter
    private final Map<Integer, OMenuButton> fillerItems = Maps.newHashMap();

    private final Set<OMenuButton> miscButtons = Sets.newHashSet();

    private final Map<String, ClickHandler> clickHandlers = Maps.newHashMap();

    @Setter
    private String title;

    @Setter
    private int menuRows;

    private final StateRequester stateRequester = new StateRequester();

    private OMenu moving;
    private MenuAction currentAction;

    public OMenu(String identifier, SPrisoner viewer) {
        this.identifier = identifier;
        this.viewer = viewer;
        _init();

        ClickHandler
                .of("return")
                .handle(event -> executeAction(MenuAction.RETURN))
                .apply(this);
    }

    public static <T extends OMenu> void refreshMenus(Class<T> menuClazz) {
        runActionOnMenus(menuClazz, null, ((player, menu) -> menu.open()));
    }

    public static <T extends OMenu> void refreshMenus(Class<T> menuClazz, Predicate<T> filter) {
        runActionOnMenus(menuClazz, filter, ((player, menu) -> menu.refresh()));
    }

    public static <T extends OMenu> void closeMenus(Class<T> menuClazz) {
        closeMenus(menuClazz, null);
    }

    public static <T extends OMenu> void closeMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, menu) -> menu.forceClose()));
    }

    private static <T extends OMenu> void runActionOnMenus(Class<T> menuClazz, Predicate<T> predicate, MenuCallback callback) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();

            if (menuClazz.isInstance(inventoryHolder) && (predicate == null || predicate.test((T) inventoryHolder))) {
                OMenu OMenu = (OMenu) inventoryHolder;
                callback.run(player, OMenu);
            }
        }
    }

    public static <T> Object[] mergeArray(T[] arr1, T... arr2) {
        return Stream.of(arr1, arr2).flatMap(Stream::of).toArray();
    }

    private void _init() {
        ConfigController cc = SuperiorPrisonPlugin.getInstance().getConfigController();
        Config configuration = cc.getMenus().get(identifier.toLowerCase());

        MenuLoader.loadMenu(this, Objects.requireNonNull(configuration, "Failed to find configuration for menu " + identifier));
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(getTitle(), viewer);
    }

    protected Inventory buildInventory(String title, Object... objects) {
        objects = mergeArray(objects, getBuildPlaceholders());

        for (Object object : objects) {
            Set<OPair<String, Function<Object, String>>> placeholders = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
            title = TextUtil.replaceText(object, title, placeholders);
        }

        Inventory inventory = Bukkit.createInventory(this, menuRows * 9, title);
        Object[] finalObjects = objects;
        fillerItems.forEach((slot, button) -> {
            if (button.requiredPermission().trim().length() == 0) {
                ItemStack item = button.currentItem(stateRequester.request(button).getItemStackWithPlaceholdersMulti(finalObjects)).currentItem();
                inventory.setItem(slot, item);
            } else if (getViewer().getPlayer().hasPermission(button.requiredPermission()))
                inventory.setItem(slot, button.currentItem(stateRequester.request(button).getItemStackWithPlaceholdersMulti(finalObjects)).currentItem());

            else {
                OMenuButton.ButtonItemBuilder no_permission = button.getStateItem("no permission");
                if (no_permission == null) return;

                no_permission.itemBuilder().replaceInLore("{permission}", button.requiredPermission());
                inventory.setItem(slot, button.currentItem(no_permission.getItemStackWithPlaceholders(getViewer())).currentItem());
            }
        });

        return inventory;
    }

    public void handleBottomClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public void forceClose() {
        executeAction(MenuAction.CLOSE);
    }

    public void move(OMenu menu) {
        moving = menu;
        menu.previousMenu = this;
        executeAction(MenuAction.MOVE);
    }

    protected void executeAction(MenuAction action) {
        executeAction(action, null);
    }

    protected void executeAction(MenuAction action, Runnable callback) {
        if (action == MenuAction.REFRESH) {
            currentAction = action;
            open(this, () -> {
                currentAction = null;
                if (callback != null)
                    callback.run();
            });
        } else if (action == MenuAction.MOVE && moving != null) {
            currentAction = action;
            open(moving, () -> {
                moving = null;
                currentAction = null;
                if (callback != null)
                    callback.run();
            });

        } else if (action == MenuAction.RETURN && previousMenu != null) {
            currentAction = action;
            open(previousMenu, () -> {
                currentAction = null;
                if (callback != null)
                    callback.run();
            });

        } else if (action == MenuAction.CLOSE) {
            currentAction = action;
            StaticTask.getInstance().ensureSync(() -> getViewer().getPlayer().closeInventory(), () -> {
                currentAction = null;
                if (callback != null)
                    callback.run();
            });
        }
    }

    public void open() {
        executeAction(MenuAction.REFRESH);
    }

    public void open(OMenu menu) {
        if (this == menu)
            executeAction(MenuAction.REFRESH);

        else
            move(menu);
    }

    public void open(@NonNull OMenu menu, Runnable callback) {
        if (Bukkit.isPrimaryThread()) {
            StaticTask.getInstance().async(() -> open(menu, callback));
            return;
        }

        Inventory inventory = menu.getInventory();
        Player player = viewer.getPlayer();
        if (player == null)
            return;

        StaticTask.getInstance().ensureSync(() -> {
            player.openInventory(inventory);

            if (callback != null)
                callback.run();
        });
    }

    public void closeInventory(InventoryCloseEvent event) {
        if (previousMenu != null && currentAction == null) {
            if (previousMove)
                executeAction(MenuAction.RETURN);

            else
                previousMove = true;
        }
    }

    public void handleDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            event.setCancelled(true);
            Optional<OMenuButton> menuButton = getButtons()
                    .stream()
                    .filter(button -> button.slot() == event.getRawSlot())
                    .findFirst();
            if (!menuButton.isPresent()) return;

            ButtonClickEvent buttonClickEvent = new ButtonClickEvent(event, menuButton.get());
            Optional<ClickHandler> clickHandler = clickHandlerFor(buttonClickEvent);
            if (!clickHandler.isPresent()) return;

            clickHandler.get().handle(buttonClickEvent);
        }
    }

    public int slotOf(Predicate<OMenuButton> predicate) {
        return getButtons()
                .stream()
                .filter(predicate)
                .mapToInt(OMenuButton::slot)
                .findFirst()
                .orElse(-1);
    }

    public int slotOfAction(String action) {
        return slotOf(button -> button.action() != null && button.action().equalsIgnoreCase(action));
    }

    public Optional<OMenuButton> buttonOf(Predicate<OMenuButton> predicate) {
        return getButtons()
                .stream()
                .filter(predicate)
                .findFirst();
    }

    public Optional<OMenuButton> buttonOfChar(char ch) {
        return buttonOf(button -> button.identifier() == ch);
    }

    public List<OMenuButton> getButtons() {
        return new ArrayList<>(fillerItems.values());
    }

    protected Optional<ClickHandler> clickHandlerFor(ButtonClickEvent event) {
        return clickHandlers.values()
                .stream()
                .filter(ch -> ch.doesAcceptEvent(event))
                .findFirst();
    }

    public Object[] getBuildPlaceholders() {
        return new Object[0];
    }

    public void refresh() {
        executeAction(MenuAction.REFRESH);
    }

    public List<SPair<Integer, ItemStack>> getBukkitItems(@NonNull Inventory inventory) {
        List<Integer> occupiedSlots = getButtons()
                .stream()
                .map(OMenuButton::slot)
                .sorted()
                .collect(toList());

        List<SPair<Integer, ItemStack>> bukkitItems = new ArrayList<>();
        for (int slot = 0; slot < (menuRows * 9); slot++) {
            ItemStack atSlot = inventory.getItem(slot);
            if (atSlot == null || atSlot.getType() == Material.AIR) continue;

            int finalSlot = slot;
            if (occupiedSlots.stream().anyMatch(slot2 -> slot2 == finalSlot)) continue;

            bukkitItems.add(new SPair<>(slot, atSlot));
        }

        return bukkitItems;
    }

    public ClickHandler clickHandler(String action) {
        return ClickHandler.of(action).apply(this);
    }

    private interface MenuCallback {
        void run(Player player, OMenu OMenu);
    }

    /*
    For different types menu loadings
    */
    public interface Mappable {

        OMenu getMenu();

        default Map<String, Object> getMap() {
            return getMenu().getData();
        }
    }

    public interface Placeholderable extends Mappable {

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

        default Optional<OMenuButton> getTemplatePlaceholderFromIdentifier(String identifier) {
            return getMenu().buttonOfChar(Optional.ofNullable(getPlaceholderMap().get(identifier)).orElse("%").charAt(0));
        }

        default Optional<OMenuButton> getPlaceholderButtonFromTemplate(String template) {
            return getTemplatePlaceholderFromIdentifier(getIdentifierFromPlaceholder(template).orElse("nothing"));
        }

        default OMenuButton parsePlaceholders(OMenuButton button, Object... objects) {
            ItemStack itemStackWithPlaceholdersMulti = button.getDefaultStateItem().getItemStackWithPlaceholdersMulti(objects);
            button = button.clone();
            button.currentItem(itemStackWithPlaceholdersMulti);
            return button;
        }

        default void initPlaceholderable(Config configuration) {
            List<String> stringPlaceholders = (List<String>) configuration.getAs("placeholders", List.class);
            HashBiMap<String, String> placeholders = HashBiMap.create();

            for (String placeholder : stringPlaceholders) {
                String[] split = placeholder.split(":");
                placeholders.put(split[0], split[1]);
            }

            getPlaceholderMap().putAll(placeholders);
        }
    }

    public interface Templateable extends Mappable {

        // Placeholders are stored like so: template=button char / action
        default HashBiMap<String, String> getTemplateMap() {
            return (HashBiMap<String, String>) getMap().computeIfAbsent("templates", (key) -> HashBiMap.create(new HashMap<String, String>()));
        }

        default HashMap<String, OMenuButton> getTemplateButtonMap() {
            return (HashMap<String, OMenuButton>) getMap().computeIfAbsent("templateButtons", (key) -> new HashMap<String, OMenuButton>());
        }

        default boolean containsTemplate(String buttonId) {
            return getTemplateMap().inverse().containsKey(buttonId);
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

        default void initTemplateable(Config configuration) {
            List<String> stringPlaceholders = (List<String>) configuration.getAs("templates", List.class);
            HashBiMap<String, String> placeholders = HashBiMap.create();

            for (String placeholder : stringPlaceholders) {
                String[] split = placeholder.split(":");
                placeholders.put(split[0], split[1]);
            }

            getTemplateMap().putAll(placeholders);
        }
    }

    public final static class StateRequester {

        private final Map<String, StateRequest> requestMap = Maps.newConcurrentMap();

        public StateRequester registerRequest(String identifier, StateRequest request) {
            requestMap.put(identifier, request);
            return this;
        }

        public OMenuButton.ButtonItemBuilder request(OMenuButton button) {
            StateRequest request = requestMap.get(button.action());
            if (request == null)
                request = requestMap.get(button.identifier() + "");

            if (request == null) {
                return button.getDefaultStateItem();

            } else
                return request.request(button);
        }

        public interface StateRequest {
            OMenuButton.ButtonItemBuilder request(OMenuButton button);
        }
    }

}
