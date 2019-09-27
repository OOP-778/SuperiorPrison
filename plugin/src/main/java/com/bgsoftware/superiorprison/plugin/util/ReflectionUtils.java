package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.item.ItemStackUtil;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.version.OVersion;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.IBlockData;
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

    private static Map<String, Object> materialToDataMap = new HashMap<>();

    // << Classes >>
    private static Class<?>

            // Player
            CRAFT_PLAYER,
            ENTITY_PLAYER,
            PLAYER_CONNECTION,
            PACKET,

    // Blocks
    CRAFT_WORLD,
            NMS_WORLD,
            NMS_BLOCK_POS,
            NMS_BLOCK,
            NMS_BLOCKS,
            NMS_CHUNK,
            IBLOCK_DATA,
            NMS_ITEM_BLOCK,

    NMS_ITEM,
            NMS_ITEM_STACK;

    // << Fields >>
    private static Field
            PLAYER_CONNECTION_FIELD;

    // << Methods >>
    private static Method
            CRAFT_WORLD_GET_HANDLE,
            NMS_BLOCK_GET_DATA,
            NMS_ITEM_BLOCK_GET_BLOCK,
            NMS_ITEM_STACK_GET_ITEM,
            NMS_CHUNK_SET_TYPE,
            NMS_WORLD_GET_CHUNK_AT,

    CONNECTION_SEND_PACKET,
            CRAFT_PLAYER_GET_HANDLE;

    // << Constructors >>
    private static Constructor<?>
            BLOCK_POSITION_CONSTRUCTOR;

    static {
        try {

            // Player Stuff
            CRAFT_PLAYER = OSimpleReflection.Package.CB_ENTITY.getClass("CraftPlayer");
            ENTITY_PLAYER = OSimpleReflection.Package.NMS.getClass("EntityPlayer");
            PLAYER_CONNECTION = OSimpleReflection.Package.NMS.getClass("PlayerConnection");
            PACKET = OSimpleReflection.Package.NMS.getClass("Packet");
            NMS_ITEM_BLOCK = OSimpleReflection.Package.NMS.getClass("ItemBlock");

            CRAFT_PLAYER_GET_HANDLE = OSimpleReflection.getMethod(CRAFT_PLAYER, "getHandle");
            PLAYER_CONNECTION_FIELD = OSimpleReflection.getField(ENTITY_PLAYER, false, "playerConnection");
            CONNECTION_SEND_PACKET = OSimpleReflection.getMethod(PLAYER_CONNECTION, "sendPacket", PACKET);

            // Block Stuff
            CRAFT_WORLD = OSimpleReflection.Package.CB.getClass("CraftWorld");
            NMS_WORLD = OSimpleReflection.Package.NMS.getClass("World");
            NMS_BLOCK_POS = OSimpleReflection.Package.NMS.getClass("BlockPosition");
            NMS_BLOCK = OSimpleReflection.Package.NMS.getClass("Block");
            NMS_BLOCKS = OSimpleReflection.Package.NMS.getClass("Blocks");
            IBLOCK_DATA = OSimpleReflection.Package.NMS.getClass("IBlockData");
            NMS_ITEM = OSimpleReflection.Package.NMS.getClass("Item");
            NMS_ITEM_STACK = OSimpleReflection.Package.NMS.getClass("ItemStack");

            CRAFT_WORLD_GET_HANDLE = OSimpleReflection.getMethod(CRAFT_WORLD, "getHandle");
            BLOCK_POSITION_CONSTRUCTOR = OSimpleReflection.getConstructor(NMS_BLOCK_POS, double.class, double.class, double.class);

            NMS_BLOCK_GET_DATA = OSimpleReflection.getMethod(NMS_BLOCK, "getBlockData");
            NMS_ITEM_BLOCK_GET_BLOCK = OSimpleReflection.getMethod(NMS_ITEM_BLOCK, "getBlock");
            NMS_ITEM_STACK_GET_ITEM = OSimpleReflection.getMethod(NMS_ITEM_STACK, "getItem");
            NMS_CHUNK = OSimpleReflection.Package.NMS.getClass("Chunk");
            NMS_WORLD_GET_CHUNK_AT = OSimpleReflection.getMethod(NMS_WORLD, "getChunkAt", int.class, int.class);

            if (OVersion.isBefore(13))
                NMS_CHUNK_SET_TYPE = OSimpleReflection.getMethod(NMS_CHUNK, "a", NMS_BLOCK_POS, int.class);

            else
                NMS_CHUNK_SET_TYPE = OSimpleReflection.getMethod(NMS_CHUNK, "setType", NMS_BLOCK_POS, IBLOCK_DATA);

            Field AIR_FIELD = OSimpleReflection.getField(NMS_BLOCKS, true, "AIR");
            materialToDataMap.put("AIR:0", NMS_BLOCK_GET_DATA.invoke(AIR_FIELD.get(null)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setBlock(Location location, Material type, int data) {
        StaticTask.getInstance().sync(() -> {

            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            if (!location.getWorld().isChunkLoaded(chunkX, chunkZ))
                return;

            try {

                // Convert material to IBlockData
                Object blockData = materialToDataMap.get(type.name() + ":" + data);
                if (blockData == null && type != Material.AIR) {
                    Object itemStack = ItemStackUtil.itemFromBukkit(new OItem(type).setDurability(data).getItemStack());
                    Object item = NMS_ITEM_STACK_GET_ITEM.invoke(itemStack);
                    Object block = null;

                    try {
                        block = NMS_ITEM_BLOCK_GET_BLOCK.invoke(item);
                    } catch (Exception ignored) {}

                    blockData = NMS_BLOCK_GET_DATA.invoke(block);
                    materialToDataMap.put(type + ":" + data, blockData);
                }

                Object world = CRAFT_WORLD_GET_HANDLE.invoke(location.getWorld());
                Object blockPosition = BLOCK_POSITION_CONSTRUCTOR.newInstance(location.getX(), location.getY(), location.getZ());
                Object chunk = NMS_WORLD_GET_CHUNK_AT.invoke(world, chunkX, chunkZ);

                Chunk c;
                c.a()

                NMS_CHUNK_SET_TYPE.invoke(chunk, blockPosition, Block.getCombinedId((IBlockData) blockData));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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
