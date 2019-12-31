package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.oop.orangeengine.main.Engine.getEngine;

public class NmsHandler_v1_14_R1 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {
        org.bukkit.Material parsed = material.parseMaterial();
        if (parsed == null) {
            getEngine().getLogger().printError("Failed to find block data for material " + material.name());
            return;
        }

        IBlockData data = CraftMagicNumbers.getBlock(parsed, material.getData());
        net.minecraft.server.v1_14_R1.Chunk chunk = ((CraftChunk) location.getChunk()).getHandle();

        int indexY = location.getBlockY() >> 4;
        ChunkSection chunkSection = chunk.getSections()[indexY];
        if (chunkSection != null && chunkSection.getType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15) == data)
            return;

        if (chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4);

        chunkSection.setType(location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, data, false);

        // Updating light
        WorldServer world = (WorldServer) chunk.getWorld();

        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        world.getChunkProvider().getLightEngine().a(pos);
        world.getChunkProvider().flagDirty(pos);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        ChunkProviderServer cps = ((CraftWorld)world).getHandle().getChunkProvider();
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_14_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            Packet packet = new PacketPlayOutMapChunk(nmsChunk, 65535);

            cps.playerChunkMap.a(nmsChunk.getPos(), false).forEach(player -> player.playerConnection.sendPacket(packet));
        }
    }
}
