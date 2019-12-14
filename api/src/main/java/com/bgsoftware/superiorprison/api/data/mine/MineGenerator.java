package com.bgsoftware.superiorprison.api.data.mine;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;

import java.time.Instant;
import java.util.List;

public interface MineGenerator {

    List<OPair<Double, OMaterial>> getGeneratorMaterials();

    void clearMine();

    void generate();

    void reset();
}
