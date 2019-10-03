package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.List;

public class NmsHandler_v1_13_R2 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ))
            return;


        net.minecraft.server.v1_13_R2.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_13_R2.Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        chunk.setType(pos, CraftMagicNumbers.getBlock(material.parseMaterial(), material.getData()), false);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_13_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            for (Player player : world.getPlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, 65535));
            }
        }
    }
}
