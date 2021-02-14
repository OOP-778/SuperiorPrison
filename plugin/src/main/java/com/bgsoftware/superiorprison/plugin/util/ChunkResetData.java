package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.data.OQueue;
import com.oop.orangeengine.main.util.data.pair.OPair;
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

  private final String world;
  private final int x, z;
  private final OQueue<OPair<Location, OMaterial>> data = new OQueue<>();

  public void add(Location location, OMaterial material) {
    data.add(new OPair<>(location, material));
  }
}
