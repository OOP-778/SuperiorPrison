package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.BlockController;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

public class SBlockController implements BlockController {
    @Override
    public void syncHandleBlockBreak(Player who, Location... locations) {

    }

    @Override
    public void asyncHandleBlockBreak(Player who, Map<Location, Map<Material, Integer>> data) {

    }
}
