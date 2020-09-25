package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.OQueue;
import com.oop.orangeengine.material.OMaterial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ChunkResetData {

    private final World world;
    private final int x, z;
    private final OQueue<ListenablePair<Location, OMaterial>> data = new OQueue<>();
    @Setter
    private boolean ready = false;

    public void add(Location location, OMaterial material, Runnable onComplete) {
        data.add(new ListenablePair<>(location, material).onComplete(onComplete));
    }
}
