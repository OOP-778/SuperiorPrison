package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NmsHandler_v1_14_R1 implements SuperiorNms {
    private final Map<OMaterial, IBlockData> dataMap = new HashMap<>();

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, key -> CraftMagicNumbers.getBlock(key.parseMaterial()).getBlockData());
        net.minecraft.server.v1_14_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

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
        List<Packet> packets = new LinkedList<>();

        locations.forEach((chunk, locs) -> {
            net.minecraft.server.v1_14_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.markDirty();
            boolean usePacketChunk = locs.size() > 100;

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
        net.minecraft.server.v1_14_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            return null;

        IBlockData type = chunkSection.getType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15);
        if (type == Blocks.AIR.getBlockData()) return null;

        return OMaterial.matchMaterial(CraftMagicNumbers.getMaterial(type.getBlock()));
    }
}
