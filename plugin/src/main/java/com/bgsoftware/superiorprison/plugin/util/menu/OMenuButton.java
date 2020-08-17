package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.bgsoftware.superiorprison.plugin.util.ClassDebugger;
import com.google.common.collect.Maps;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;

import static com.bgsoftware.superiorprison.plugin.util.TextUtil.replaceList;
import static com.bgsoftware.superiorprison.plugin.util.TextUtil.replaceText;

@Getter
@Accessors(fluent = true, chain = true)
public class OMenuButton implements Cloneable {

    private Map<String, ButtonItemBuilder> itemStates = Maps.newHashMap();

    @Setter
    private ItemStack currentItem;

    @Setter
    private String requiredPermission = "";

    @Setter
    private String action = "";

    private final char identifier;

    @Setter
    private int slot = -1;

    @Getter
    @Setter
    private boolean locked;

    public OMenuButton(char identifier) {
        this.identifier = identifier;
    }

    public ButtonItemBuilder getDefaultStateItem() {
        return getStateItem("default");
    }

    public boolean containsState(String state) {
        return itemStates.containsKey(state);
    }

    public ButtonItemBuilder getStateItem(String state) {
        ButtonItemBuilder aDefault = itemStates.get(state);
        if (aDefault == null)
            return new ButtonItemBuilder(new OItem(OMaterial.RED_STAINED_GLASS_PANE).setDisplayName("&cFailed to find a button state by id '" + state + "'"));

        else
            return aDefault;
    }

    protected void addState(String state, ButtonItemBuilder itemBuilder) {
        itemStates.remove(state);
        itemStates.put(state, itemBuilder);
    }

    public OMenuButton clone() {
        try {
            OMenuButton button = (OMenuButton) super.clone();
            button.itemStates = Maps.newHashMap();
            itemStates.forEach((state, item) -> button.itemStates.put(state, item.clone()));
            button.currentItem = currentItem != null ? currentItem.clone() : null;

            return button;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ButtonItemBuilder {

        @Getter
        private ItemBuilder itemBuilder;

        public ButtonItemBuilder(@NonNull ItemBuilder itemBuilder) {
            this.itemBuilder = itemBuilder;
        }

        protected ButtonItemBuilder() {
        }

        public <T> ItemStack getItemStackWithPlaceholders(T object) {
            if (getItemStack().getType() == Material.AIR) return getItemStack();
            Optional<PapiHook> papiOptional = SuperiorPrisonPlugin.getInstance().getHookController().findHook(() -> PapiHook.class);
            if (!papiOptional.isPresent()) return itemBuilder.getItemStack();
            PapiHook papi = papiOptional.get();

            Set<OPair<String, Function<Object, String>>> placeholdersFor = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
            ItemBuilder clone = itemBuilder.clone();

            clone.setLore(replaceList(object, clone.getLore(), placeholdersFor));
            clone.setDisplayName(replaceText(object, clone.getDisplayName(), placeholdersFor));

            clone.setDisplayName(papi.parse(object, clone.getDisplayName()));
            clone.setLore(papi.parse(object, finalizeLore(clone.getLore(), getPlayer(object))));

            return clone.getItemStack();
        }

        public <T> ItemStack getItemStackWithPlaceholders(T object, Set<OPair<String, Function<T, String>>> placeholders) {
            if (getItemStack().getType() == Material.AIR) return getItemStack();
            ItemBuilder clone = itemBuilder.clone();

            clone.setLore(replaceList(object, clone.getLore(), placeholders));
            clone.setDisplayName(replaceText(object, clone.getDisplayName(), placeholders));

            return clone.getItemStack();
        }

        public <T> ItemStack getItemStackWithPlaceholdersMulti(Object... objs) {
            if (getItemStack().getType() == Material.AIR) return getItemStack();

            ItemBuilder clone = itemBuilder.clone();
            for (Object object : objs) {
                if (object instanceof Player || object instanceof SPrisoner)
                    clone.setLore(finalizeLore(clone.getLore(), getPlayer(object)));

                Set<OPair<String, Function<Object, String>>> placeholdersFor = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
                clone.setLore(replaceList(object, clone.getLore(), placeholdersFor));
                clone.setDisplayName(replaceText(object, clone.getDisplayName(), placeholdersFor));
            }

            return clone.getItemStack();
        }

        public ItemStack getItemStackWithPlaceholders(Map<String, Object> placeholders) {
            if (getItemStack().getType() == Material.AIR) return getItemStack();

            ItemBuilder clone = itemBuilder.clone();
            placeholders.forEach((key, value) -> {
                clone.replaceDisplayName(key, value.toString());
                clone.replaceInLore(key, value.toString());
            });

            return clone.getItemStack();
        }

        @Override
        public ButtonItemBuilder clone() {
            return new ButtonItemBuilder(ItemBuilder.fromItem(itemBuilder.getItemStack().clone()));
        }

        private List<String> finalizeLore(List<String> lore, Player player) {
            if (player == null) return lore;

            List<String> finalizedLore = new ArrayList<>();

            boolean hasPerm = false;
            boolean inTags = false;

            for (String line : lore) {
                if (line.startsWith("<hp=")) {
                    inTags = true;
                    hasPerm = player.hasPermission(extractPermission(line));
                    ClassDebugger.debug(player.getName() + " Has Permission " + extractPermission(line) + ": " + hasPerm);
                    continue;

                } else if (inTags && line.equalsIgnoreCase("</hp>")) {
                    inTags = false;
                    continue;
                }

                if (inTags && hasPerm)
                    finalizedLore.add(line);

                else if (!inTags)
                    finalizedLore.add(line);
            }

            return finalizedLore;
        }

        private String extractPermission(String line) {
            String[] split = line.split("=");
            return split[1].replace(">", "");
        }

        public ItemStack getItemStack() {
            return itemBuilder.getItemStack();
        }

        private Player getPlayer(Object object) {
            if (object instanceof Player) return (Player) object;
            if (object instanceof SPrisoner && ((SPrisoner) object).isOnline()) return ((SPrisoner) object).getPlayer();
            return null;
        }
    }
}
