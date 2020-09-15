package com.bgsoftware.superiorprison.plugin.util;

import org.bukkit.block.BlockFace;

public class Directional {
    public static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace fromDirection(float yaw) {
        return fromDirection(yaw, true);
    }

    public static BlockFace fromDirection(float yaw, boolean subCardinalDirections) {
        yaw = yaw > 0 ? yaw - 180 : yaw + 180;

        if (subCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }
}
