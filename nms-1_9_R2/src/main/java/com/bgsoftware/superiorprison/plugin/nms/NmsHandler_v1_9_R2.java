package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class NmsHandler_v1_9_R2 implements SuperiorNms {
    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = Block.getByCombinedId(material.getCombinedData());
        net.minecraft.server.v1_9_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4, !nmsChunk.world.worldProvider.m());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations) {
        locations.forEach((chunk, locs) -> {
            int locsSize = locs.size();
            short[] values = new short[locsSize];
            net.minecraft.server.v1_9_R2.Chunk nmsChunk = ((CraftChunk)chunk).getHandle();
            nmsChunk.initLighting();

            Location firstLocation = null;

            AtomicInteger counter = new AtomicInteger(0);
            for (Location location : locs) {
                if (firstLocation == null)
                    firstLocation = location;

                values[counter.incrementAndGet()] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
            }

            assert firstLocation != null;
            AxisAlignedBB bb = new AxisAlignedBB(firstLocation.getX() - 60, firstLocation.getY() - 200, firstLocation.getZ() - 60,
                    firstLocation.getX() + 60, firstLocation.getY() + 200, firstLocation.getZ() + 60);

            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(locsSize, values, nmsChunk);

            for (Object entity : nmsChunk.getWorld().getEntities(null, bb)) {
                if (entity instanceof EntityPlayer)
                    ((EntityPlayer) entity).playerConnection.sendPacket(packet);
            }
        });
    }
}
