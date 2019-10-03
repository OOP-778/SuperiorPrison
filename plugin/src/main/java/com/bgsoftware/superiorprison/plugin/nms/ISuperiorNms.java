package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public interface ISuperiorNms {

    void setBlock(Location location, OMaterial material);

    void refreshChunks(World world, List<Chunk> chunkList);
}
