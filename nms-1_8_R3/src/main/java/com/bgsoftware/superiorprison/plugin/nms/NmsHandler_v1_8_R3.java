package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class NmsHandler_v1_8_R3 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ))
            return;

        int id = material.getId();
        if (material.getData() > 0)
            id = id + (material.getData() << 12);

        IBlockData data = Block.getByCombinedId(id);
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_8_R3.Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        chunk.a(pos, data);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_8_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            for (Player player : world.getPlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, false, 65535));
            }
        }
    }
}
