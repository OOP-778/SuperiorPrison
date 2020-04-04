package com.bgsoftware.superiorprison.api.data.mine.area;

import java.util.ArrayList;
import java.util.Arrays;

public enum AreaEnum {

    REGION,
    MINE;

    public int getOrder() {
        return new ArrayList<>(Arrays.asList(values())).indexOf(this);
    }
}
