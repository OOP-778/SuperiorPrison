package com.bgsoftware.superiorprison.api.data.mine;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;

import java.util.List;

public interface MineGenerator {

    /*
    Get Generator Materials
    Pair contains Percentage and material
    */
    List<OPair<Double, OMaterial>> getGeneratorMaterials();

    /*
    Generates AIR between pos1 and pos2
    */
    void generateAir();

    /*
    Generates shuffled materials between pos1 and pos2
    */
    void generate();

    /*
    Resets the mine
    */
    void reset();
}
