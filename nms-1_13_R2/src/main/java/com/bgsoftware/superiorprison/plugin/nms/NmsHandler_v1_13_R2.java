package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;

import java.util.List;

import static com.oop.orangeengine.main.Engine.getEngine;

public class NmsHandler_v1_13_R2 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {
        org.bukkit.Material parsed = material.parseMaterial();
        if (parsed == null) {
            getEngine().getLogger().printError("Failed to find block data for material " + material.name());
            return;
        }

        IBlockData data = CraftMagicNumbers.getBlock(parsed).getBlockData();
        net.minecraft.server.v1_13_R2.Chunk chunk = ((CraftChunk) location.getChunk()).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = chunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.g());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        ChunkProviderServer cps = ((CraftWorld) world).getHandle().getChunkProvider();
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_13_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.initLighting();

            Packet packet = new PacketPlayOutMapChunk(nmsChunk, 65535);
            //cps.playerChunkMap.a(nmsChunk.getPos(), false).forEach(player -> player.playerConnection.sendPacket(packet));
        }
    }
}
