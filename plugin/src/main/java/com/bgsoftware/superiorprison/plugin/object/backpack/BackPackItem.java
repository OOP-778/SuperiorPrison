package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.oop.orangeengine.material.OMaterial;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.inventory.ItemStack;

@Getter
@ToString
public class BackPackItem {
    private final ItemStack itemStack;
    private final OMaterial material;

    private BackPackItem(@NonNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.material = OMaterial.matchMaterial(itemStack);
    }

    public static BackPackItem wrap(@NonNull ItemStack itemStack) {
        return new BackPackItem(itemStack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackPackItem that = (BackPackItem) o;
        return that.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        int meta = itemStack.hasItemMeta() ? itemStack.getItemMeta().hashCode() : 1;
        return new HashCodeBuilder(17, 37)
                .append(material)
                .append(meta)
                .toHashCode();
    }
}
