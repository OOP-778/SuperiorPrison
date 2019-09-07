package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.OSimpleReflection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.oop.orangeengine.main.Engine.getEngine;

public class ReflectionUtils {

    // << Classes >>
    private static Class<?>

            // Player
            CRAFT_PLAYER,
            ENTITY_PLAYER,
            PLAYER_CONNECTION,
            PACKET,

    // Blocks
    CRAFT_WORLD,
            NMS_BLOCKS,
            NMS_WORLD,
            NMS_BLOCK_POS,
            NMS_BLOCK,
            IBLOCK_DATA;

    // << Fields >>
    private static Field
            PLAYER_CONNECTION_FIELD;

    // << Methods >>
    private static Method
            CRAFT_WORLD_GET_HANDLE,
            NMS_WORLD_SET_TYPE,
            NMS_BLOCK_GET_DATA,

    CONNECTION_SEND_PACKET,
            CRAFT_PLAYER_GET_HANDLE;

    // << Constructors >>
    private static Constructor<?>
            BLOCK_POSITION_CONSTRUCTOR;

    static {
        try {

            NMS_BLOCKS = OSimpleReflection.Package.NMS.getClass("Blocks");


            //Player Stuff
            CRAFT_PLAYER = OSimpleReflection.Package.CB_ENTITY.getClass("CraftPlayer");
            ENTITY_PLAYER = OSimpleReflection.Package.NMS.getClass("EntityPlayer");
            PLAYER_CONNECTION = OSimpleReflection.Package.NMS.getClass("PlayerConnection");
            PACKET = OSimpleReflection.Package.NMS.getClass("Packet");

            CRAFT_PLAYER_GET_HANDLE = OSimpleReflection.getMethod(CRAFT_PLAYER, "getHandle");
            PLAYER_CONNECTION_FIELD = OSimpleReflection.getField(ENTITY_PLAYER, false, "playerConnection");
            CONNECTION_SEND_PACKET = OSimpleReflection.getMethod(PLAYER_CONNECTION, "sendPacket", PACKET);

            //Block Stuff
            CRAFT_WORLD = OSimpleReflection.Package.CB.getClass("CraftWorld");
            NMS_WORLD = OSimpleReflection.Package.NMS.getClass("World");
            NMS_BLOCK_POS = OSimpleReflection.Package.NMS.getClass("BlockPosition");
            NMS_BLOCK = OSimpleReflection.Package.NMS.getClass("Block");
            IBLOCK_DATA = OSimpleReflection.Package.NMS.getClass("IBlockData");

            CRAFT_WORLD_GET_HANDLE = OSimpleReflection.getMethod(CRAFT_WORLD, "getHandle");
            BLOCK_POSITION_CONSTRUCTOR = OSimpleReflection.getConstructor(NMS_BLOCK_POS, double.class, double.class, double.class);

            NMS_WORLD_SET_TYPE = OSimpleReflection.getMethod(NMS_WORLD, "setTypeAndData", NMS_BLOCK_POS, IBLOCK_DATA, int.class);
            NMS_BLOCK_GET_DATA = OSimpleReflection.getMethod(NMS_BLOCK, "getBlockData");


        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Map<Material, Object> materialToDataMap = new HashMap<>();

    public static void setBlock(Location location, Material type) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ))
            return;

        try {

            // Convert material to IBlockData
            Object blockData = materialToDataMap.get(type);
            if (blockData == null) {
                blockData = NMS_BLOCK_GET_DATA.invoke(NMS_BLOCKS.getField(type.name()).get(null));
                materialToDataMap.put(type, blockData);
            }

            Object world = CRAFT_WORLD_GET_HANDLE.invoke(location.getWorld());
            Object blockPosition = BLOCK_POSITION_CONSTRUCTOR.newInstance(location.getX(), location.getY(), location.getZ());

            NMS_WORLD_SET_TYPE.invoke(world, blockPosition, blockData, 18);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void sendPacket(Player player, Object packet) {
        try {

            Object ENTITY_PLAYER = CRAFT_PLAYER_GET_HANDLE.invoke(player);
            Object PLAYER_CONNECTION = PLAYER_CONNECTION_FIELD.get(ENTITY_PLAYER);
            CONNECTION_SEND_PACKET.invoke(PLAYER_CONNECTION, packet);

        } catch (Exception ex) {
            getEngine().getLogger().error(new IllegalStateException(ex));
        }
    }

}
