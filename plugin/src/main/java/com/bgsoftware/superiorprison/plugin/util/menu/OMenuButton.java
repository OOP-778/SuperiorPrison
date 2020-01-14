package com.bgsoftware.superiorprison.plugin.util.menu;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.google.common.collect.Maps;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.material.OMaterial;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static com.bgsoftware.superiorprison.plugin.util.ReplacerUtils.replaceList;
import static com.bgsoftware.superiorprison.plugin.util.ReplacerUtils.replaceText;

@Getter
public class OMenuButton implements Cloneable {

    private Map<String, ButtonItemBuilder> itemStates = Maps.newHashMap();

    @Setter
    private String requiredPermission = "";

    @Setter
    private String action;

    private char identifier;

    @Setter (value = AccessLevel.PROTECTED)
    private int slot = -1;

    public OMenuButton(char identifier) {
        this.identifier = identifier;
    }

    public static class ButtonItemBuilder extends OItem {

        protected ButtonItemBuilder() {}

        public ButtonItemBuilder(OMaterial material) {
            super(material);
        }

        public <T> ItemStack getItemStackWithPlaceholders(T object) {
            Optional<PapiHook> papiOptional = SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class);
            if (!papiOptional.isPresent()) return getItemStack();
            PapiHook papi = papiOptional.get();

            ItemStack clone = getItemStack().clone();
            setItemStack(clone.clone());

            setDisplayName(papi.parse(object, getDisplayName()));
            setLore(papi.parse(object, getLore()));

            ItemStack finalItem = getItemStack();
            setItemStack(clone);
            return finalItem;
        }

        public <T> ItemStack getItemStackWithPlaceholders(T object, Set<BiFunction<String, T, String>> placeholders) {
            ItemStack clone = getItemStack().clone();
            setItemStack(clone.clone());

            Optional<PapiHook> hook = SuperiorPrisonPlugin.getInstance().getHookController().findHook(PapiHook.class);
            setLore(replaceList(object, getLore(), placeholders, hook));
            setDisplayName(replaceText(object, getDisplayName(), placeholders, hook));

            ItemStack finalItem = getItemStack();
            setItemStack(clone);
            return finalItem;
        }

        @Override
        public ButtonItemBuilder clone() {
            return (ButtonItemBuilder) super.clone();
        }
    }

    public ButtonItemBuilder getDefaultStateItem() {
        return getStateItem("default");
    }

    public ButtonItemBuilder getStateItem(String state) {
        ButtonItemBuilder aDefault = itemStates.get(state);
        if (aDefault == null)
            return (ButtonItemBuilder) new ButtonItemBuilder(OMaterial.RED_STAINED_GLASS_PANE).setDisplayName("Failed to find " + state + " button state!");

        else
            return aDefault;
    }

    protected void addState(String state, ButtonItemBuilder itemBuilder) {
        itemStates.put(state, itemBuilder);
    }

    public OMenuButton clone() {
        try {
            OMenuButton button = (OMenuButton) super.clone();
            button.itemStates = Maps.newHashMap();
            itemStates.forEach((state, item) -> button.itemStates.put(state, item.clone()));

            return button;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
