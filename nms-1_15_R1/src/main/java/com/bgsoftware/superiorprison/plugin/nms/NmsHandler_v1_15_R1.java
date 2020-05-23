package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class NmsHandler_v1_15_R1 implements SuperiorNms {
    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        org.bukkit.Material parsed = material.parseMaterial();
        IBlockData data = CraftMagicNumbers.getBlock(parsed).getBlockData();
        net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = nmsChunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = nmsChunk.getSections()[indexY] = new ChunkSection(indexY << 4);

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);

        // Updating light
        WorldServer world = (WorldServer) chunk.getWorld();

        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        world.getChunkProvider().getLightEngine().a(pos);
        world.getChunkProvider().flagDirty(pos);
    }

    @Override
    public void refreshChunks(World world, Map<Chunk, Set<Location>> locations) {
        ChunkProviderServer cps = ((CraftWorld) world).getHandle().getChunkProvider();
        locations.forEach((chunk, locs) -> {
            int locsSize = locs.size();
            short[] values = new short[locsSize];
            net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

            AtomicInteger counter = new AtomicInteger(0);
            for (Location location : locs) {
                values[counter.incrementAndGet()] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
            }

            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(locsSize, values, nmsChunk);
            cps.playerChunkMap.a(nmsChunk.getPos(), false).forEach(player -> player.playerConnection.sendPacket(packet));
        });
    }
}
