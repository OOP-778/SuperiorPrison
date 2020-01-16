package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.item.ItemBuilder;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorprison.plugin.util.ReplacerUtils.replaceList;
import static com.bgsoftware.superiorprison.plugin.util.ReplacerUtils.replaceText;

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

    private char identifier;

    @Setter(value = AccessLevel.PROTECTED)
    private int slot = -1;

    public OMenuButton(char identifier) {
        this.identifier = identifier;
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
            Optional<PapiHook> papiOptional = SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class);
            if (!papiOptional.isPresent()) return itemBuilder.getItemStack();
            PapiHook papi = papiOptional.get();

            ItemBuilder clone = itemBuilder.clone();
            clone.setDisplayName(papi.parse(object, clone.getDisplayName()));
            clone.setLore(papi.parse(object, finalizeLore(clone.getLore(), object instanceof SPrisoner ? ((SPrisoner) object).getPlayer() : null)));

            return clone.getItemStack();
        }

        public <T> ItemStack getItemStackWithPlaceholders(T object, Set<BiFunction<String, T, String>> placeholders) {
            ItemBuilder clone = itemBuilder.clone();

            Optional<PapiHook> hook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class);
            clone.setLore(replaceList(object, clone.getLore(), placeholders, hook));
            clone.setDisplayName(replaceText(object, clone.getDisplayName(), placeholders, hook));

            return clone.getItemStack();
        }

        public <T> ItemStack getItemStackWithPlaceholdersMulti(Object... objs) {
            ItemBuilder clone = itemBuilder.clone();

            Optional<PapiHook> papi = SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class);
            for (Object object : objs) {
                if (object instanceof Player || object instanceof SPrisoner)
                    clone.setLore(finalizeLore(clone.getLore(), object instanceof Player ? (Player) object : ((SPrisoner) object).getPlayer()));

                Set<BiFunction<String, Object, String>> placeholdersFor = SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object);
                clone.setLore(replaceList(object, clone.getLore(), placeholdersFor, papi));
                clone.setDisplayName(replaceText(object, clone.getDisplayName(), placeholdersFor, papi));
            }

            return clone.getItemStack();
        }

        @Override
        public ButtonItemBuilder clone() {
            return new ButtonItemBuilder(itemBuilder.clone());
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

}
