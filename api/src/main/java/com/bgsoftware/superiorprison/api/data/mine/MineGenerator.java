package com.bgsoftware.superiorprison.api.data.mine;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;

import java.time.Instant;
import java.util.List;

public interface MineGenerator {

    /*
    Get Generator Materials
    Pair contains Percentage and material
    */
    List<OPair<Double, OMaterial>> getGeneratorMaterials();

    /*
    Generates shuffled materials between pos1 and pos2
    */
    void generate();

    /*
    Resets the mine
    */
    void reset();

    MineBlockData getBlockData();

    /*
    When did last reset happen?
    */
    Instant getLastReset();

    /*
    When will next reset occur
    Returns current time if mine reset mode is at PERCENTAGE
    */
    Instant getWhenNextReset();

    /*
    Check if mine is caching
    When mine is caching, it's not interactable
    */
    boolean isCaching();

    /*
    Check if mine is resetting
    */
    boolean isResetting();
}
