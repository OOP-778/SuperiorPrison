package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.google.gson.annotations.SerializedName;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.gson.GsonUpdateable;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class MineOptions implements Attachable<SNormalMine>, GsonUpdateable {

    @SerializedName(value = "icon")
    private ItemStack icon;

    @SerializedName(value = "playerLimit")
    private int playerLimit;

    @SerializedName(value = "resetting")
    private OPair<String, String> resetting;

    private transient SNormalMine owner;

    MineOptions() {}

    @Override
    public void attach(SNormalMine obj) {
        this.owner = obj;

        if (icon == null)
            icon = new OItem(OMaterial.STONE)
                    .setDisplayName("&cDefault Name")
                    .getItemStack();
    }
}
