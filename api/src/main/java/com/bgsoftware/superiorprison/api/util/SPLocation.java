package com.bgsoftware.superiorprison.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor(staticName = "of")
public class SPLocation implements Serializable {

    private int x;
    private int y;
    private int z;
    private String world;

}
