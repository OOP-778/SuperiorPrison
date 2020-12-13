package com.bgsoftware.superiorprison.plugin.object.backpack;

import com.google.gson.JsonElement;
import com.oop.orangeengine.material.OMaterial;

import java.util.ArrayList;
import java.util.List;

public class ItemStack {
    // Item Material
    private OMaterial itemMaterial;

    // Amount of current Item
    private long amount;

    // NBT tags
    private final List<JsonElement> nbtTags = new ArrayList<>();
}
