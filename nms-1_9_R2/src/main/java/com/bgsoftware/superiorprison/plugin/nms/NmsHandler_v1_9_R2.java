package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NmsHandler_v1_9_R2 implements SuperiorNms {
    private final Map<OMaterial, IBlockData> dataMap = new HashMap<>();

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, mat -> Block.getByCombinedId(material.getCombinedId()));
        net.minecraft.server.v1_9_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4, !nmsChunk.world.worldProvider.m());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> receivers) {
        List<Packet> packets = new LinkedList<>();

        boolean usePacketChunk = locations.size() > 15;
        locations.forEach((chunk, locs) -> {
            net.minecraft.server.v1_9_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
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
        net.minecraft.server.v1_9_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            return null;

        IBlockData type = chunkSection.getType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15);
        if (type == Blocks.AIR.getBlockData()) return null;

        return OMaterial.byCombinedId(Block.getCombinedId(type));
    }
}
