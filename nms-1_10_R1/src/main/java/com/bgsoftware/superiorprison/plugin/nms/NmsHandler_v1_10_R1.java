package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.ChunkSection;
import net.minecraft.server.v1_10_R1.IBlockData;
import net.minecraft.server.v1_10_R1.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class NmsHandler_v1_10_R1 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {
        int id = material.getId();
        if (material.getData() > 0)
            id = id + (material.getData() << 12);

        IBlockData data = Block.getByCombinedId(id);
        net.minecraft.server.v1_10_R1.Chunk chunk = ((CraftChunk) location.getChunk()).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = chunk.getSections()[indexY];

        if (chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.m());

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_10_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.initLighting();

            for (Player player : world.getPlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, 65535));
            }
        }
    }
}
