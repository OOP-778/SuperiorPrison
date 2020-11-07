package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSet;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.*;

public class NmsHandler_v1_16_R3 implements SuperiorNms {
    private Map<OMaterial, IBlockData> dataMap = new HashMap<>();

    private static Class<?> SHORT_ARRAY_SET_CLASS = null;
    private static Constructor<?> MULTI_BLOCK_CHANGE_CONSTRUCTOR = null;

    static {
        try {
            SHORT_ARRAY_SET_CLASS = Class.forName("it.unimi.dsi.fastutil.shorts.ShortArraySet");
            for (Constructor<?> constructor : PacketPlayOutMultiBlockChange.class.getConstructors()) {
                if (constructor.getParameterCount() > 0)
                    MULTI_BLOCK_CHANGE_CONSTRUCTOR = constructor;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, key -> CraftMagicNumbers.getBlock(key.parseMaterial()).getBlockData());
        net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4);

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);

        // Updating light
        WorldServer world = ((CraftWorld) chunk.getWorld()).getHandle();

        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        world.getChunkProvider().getLightEngine().a(pos);
        world.getChunkProvider().flagDirty(pos);
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> receivers) {
        List<Packet> packets = new ArrayList<>();

        boolean usePacketChunk = locations.size() > 15;
        locations.forEach((chunk, locs) -> {
            Map<Integer, Set<Short>> blocks = new HashMap<>();

            net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.markDirty();

            if (!usePacketChunk) {
                for (Location location : locs)
                    blocks.computeIfAbsent(location.getBlockY() >> 4, key -> createShortSet())
                            .add((short) ((location.getBlockX() & 15) << 8 | (location.getBlockZ() & 15) << 4 | (location.getBlockY() & 15)));

                blocks.forEach((y, b) -> packets.add(
                        createMultiBlockChangePacket(
                                SectionPosition.a(nmsChunk.getPos(), y),
                                b,
                                nmsChunk.getSections()[y]
                        )
                ));
            } else {
                packets.add(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65280));
                packets.add(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 255));
            }
        });

        for (Packet packet : packets) {
            for (Player receiver : receivers) {
                ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    private static Set<Short> createShortSet() {
        if (SHORT_ARRAY_SET_CLASS == null)
            return new ShortArraySet();

        try {
            return (Set<Short>) SHORT_ARRAY_SET_CLASS.newInstance();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static PacketPlayOutMultiBlockChange createMultiBlockChangePacket(SectionPosition sectionPosition, Set<Short> shortSet, ChunkSection chunkSection) {
        if (MULTI_BLOCK_CHANGE_CONSTRUCTOR == null) {
            return new PacketPlayOutMultiBlockChange(
                    sectionPosition,
                    (ShortSet) shortSet,
                    chunkSection,
                    true
            );
        }

        try {
            return (PacketPlayOutMultiBlockChange) MULTI_BLOCK_CHANGE_CONSTRUCTOR.newInstance(sectionPosition, shortSet, chunkSection, true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void setBlockAndUpdate(Chunk chunk, Location location, OMaterial material, Collection<Player> players) {
        setBlock(chunk, location, material);

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(), new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        for (Player player : players)
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
