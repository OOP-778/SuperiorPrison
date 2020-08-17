package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import lombok.NonNull;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSet;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.*;

public class NmsHandler_v1_16_R2 implements SuperiorNms {
    private Map<OMaterial, IBlockData> dataMap = new HashMap<>();

    @Override
    public void setBlock(@NonNull Chunk chunk, @NonNull Location location, @NonNull OMaterial material) {
        IBlockData data = dataMap.computeIfAbsent(material, key -> CraftMagicNumbers.getBlock(key.parseMaterial()).getBlockData());
        net.minecraft.server.v1_16_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();

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
        List<Packet> packets = new ArrayList<>();

        locations.forEach((chunk, locs) -> {
            Map<Integer, ShortSet> blocks = new HashMap<>();

            net.minecraft.server.v1_16_R2.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            nmsChunk.markDirty();

            for (Location location : locs)
                blocks.computeIfAbsent(location.getBlockY() >> 4, ShortArraySet::new)
                        .add((short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY()));

            blocks.forEach((y, b) -> packets.add(
                    new PacketPlayOutMultiBlockChange(
                            SectionPosition.a(nmsChunk.getPos(), y),
                            b,
                            nmsChunk.getSections()[y],
                            true
                    )
            ));
        });

        for (Packet packet : packets) {
            for (Player receiver : receivers) {
                ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }
}
