package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.bgsoftware.superiorprison.api.data.backpack.BackPack;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.config.backpack.BackPackConfig;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.oop.datamodule.DataHelper;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.bgsoftware.superiorprison.plugin.controller.SBackPackController.NBT_KEY;

@AllArgsConstructor
public class SBackPack implements BackPack {

    private ItemStack itemStack;
    private BackPackConfig config;
    private BackPackData data;

    @SneakyThrows
    public SBackPack(@Nonnull ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        Preconditions.checkArgument(nbtItem.hasKey(NBT_KEY), "The given item is not an backpack");

        String serialized = nbtItem.getString(NBT_KEY);
        JsonObject jsonObject = DataHelper.gson().fromJson(serialized, JsonObject.class);

        data = new BackPackData();
        data.deserialize(new SerializedData(jsonObject));

        config = SuperiorPrisonPlugin.getInstance().getBackPackController().getConfig(data.configId).orElseThrow(() -> new IllegalStateException("Failed to find backPack by id " + data.configId + " level " + data.level));
    }

    @Override
    public int getCapacity() {
        return config.getPages() * config.getRows();
    }

    @Override
    public int getCurrentLevel() {
        return data.getLevel();
    }

    @Override
    public int getUsed() {
        return 0;
    }

    @Override
    public List<ItemStack> getStored() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItem() {
        return itemStack;
    }
}
