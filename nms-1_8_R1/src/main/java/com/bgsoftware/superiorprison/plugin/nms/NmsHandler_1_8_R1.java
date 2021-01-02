package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NmsHandler_1_8_R1 implements SuperiorNms {
    private final Map<OMaterial, IBlockData> dataMap = new HashMap<>();
    private final Map<IBlockData, List<ItemStack>> dropsByData = new ConcurrentHashMap<>();

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, mat -> Block.getByCombinedId(material.getCombinedId()));
        net.minecraft.server.v1_8_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4, !nmsChunk.world.worldProvider.o());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    public Map<OMaterial, OPair<Integer, List<ItemStack>>> getTypeAndDrops(Set<Location> locations) {
        net.minecraft.server.v1_8_R1.World world = null;

        Map<OPair<Integer, Integer>, net.minecraft.server.v1_8_R1.Chunk> pairToChunk = new HashMap<>();
        Map<OMaterial, OPair<Integer, List<ItemStack>>> dropsByType = new HashMap<>();

        for (Location location : locations) {
            if (world == null)
                world = ((CraftWorld) location.getWorld()).getHandle();

            net.minecraft.server.v1_8_R1.World finalWorld = world;
            net.minecraft.server.v1_8_R1.Chunk chunk = pairToChunk.computeIfAbsent(new OPair<>(location.getBlockX() >> 4, location.getBlockZ() >> 4), key -> finalWorld.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4));

            int indexY = location.getBlockY() >> 4;
            ChunkSection section = chunk.getSections()[indexY];
            if (section == null) continue;

            IBlockData type = section.getType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15);
            if (type.getBlock() == Blocks.AIR) continue;

            List<ItemStack> itemStacks = dropsByData.get(type);
            if (itemStacks == null) {
                itemStacks = getDrops(type.getBlock(), chunk);
                dropsByData.put(type, itemStacks);
            }

            OMaterial material = OMaterial.byCombinedId(Block.getCombinedId(type));
            dropsByType.merge(material, new OPair<>(1, itemStacks), (first, second) -> {
                second.setFirst(second.getFirst() + 1);
                return second;
            });
        }

        return dropsByType;
    }

    private List<org.bukkit.inventory.ItemStack> getDrops(Block block, net.minecraft.server.v1_8_R1.Chunk chunk) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();
        if (block != Blocks.AIR) {
            byte data = (byte) block.toLegacyData(block.getBlockData());
            int count = block.getDropCount(0, chunk.getWorld().random);

            for (int i = 0; i < count; ++i) {
                Item item = block.getDropType(block.fromLegacyData(data), chunk.getWorld().random, 0);
                if (item != null) {
                    drops.add(new org.bukkit.inventory.ItemStack(CraftMagicNumbers.getMaterial(item), 1, (short) block.getDropData(block.fromLegacyData(data))));
                }
            }
        }

        return drops;
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> receivers) {
        List<Packet> packets = new LinkedList<>();

        boolean usePacketChunk = locations.size() > 15;
        locations.forEach((chunk, locs) -> {
            net.minecraft.server.v1_8_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.e();

            if (!usePacketChunk) {
                int locsSize = locs.size();
                short[] values = new short[locsSize];

                int counter = 0;
                for (Location location : locs) {
                    values[counter] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
                    counter++;
                }

                packets.add(new PacketPlayOutMultiBlockChange(locsSize, values, nmsChunk));

            } else {
                packets.add(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), false, 65280));
                packets.add(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), false, 255));
            }
        });

        for (Packet packet : packets) {
            for (Player receiver : receivers) {
                ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    @Override
    public void setBlockAndUpdate(Chunk chunk, Location location, OMaterial material, Collection<Player> players) {
        setBlock(chunk, location, material);

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(), new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        for (Player player : players)
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public OMaterial getBlockType(Chunk chunk, Location location) {
        int indexY = location.getBlockY() >> 4;
        net.minecraft.server.v1_8_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            return null;

        IBlockData type = chunkSection.getType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15);
        if (type == Blocks.AIR.getBlockData()) return null;

        return OMaterial.byCombinedId(Block.getCombinedId(type));
    }
}
