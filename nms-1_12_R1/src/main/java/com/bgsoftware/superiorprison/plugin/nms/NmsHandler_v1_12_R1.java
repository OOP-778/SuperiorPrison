package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.main.util.data.cache.OCache;
import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class NmsHandler_v1_12_R1 implements SuperiorNms {

    private Map<OMaterial, IBlockData> dataMap = new HashMap<>();

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, mat -> Block.getByCombinedId(material.getCombinedData()));
        net.minecraft.server.v1_12_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4, nmsChunk.world.worldProvider.m());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations, Collection<Player> receivers) {
        List<Packet> packets = new ArrayList<>();

        locations.forEach((chunk, locs) -> {
            int locsSize = locs.size();
            short[] values = new short[locsSize];
            net.minecraft.server.v1_12_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.markDirty();

            Location firstLocation = null;

            int counter = 0;
            for (Location location : locs) {
                if (firstLocation == null)
                    firstLocation = location;

                values[counter] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
                counter++;
            }

            assert firstLocation != null;
            packets.add(new PacketPlayOutMultiBlockChange(locsSize, values, nmsChunk));
        });

        for (Packet packet : packets) {
            for (Player receiver : receivers) {
                ((CraftPlayer)receiver).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }
}
