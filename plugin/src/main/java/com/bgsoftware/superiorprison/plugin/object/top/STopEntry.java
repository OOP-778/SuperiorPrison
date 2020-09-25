package com.bgsoftware.superiorprison.plugin.object.top;

import com.bgsoftware.superiorprison.api.data.top.TopEntry;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class STopEntry<T> implements TopEntry<T> {

    private final SPrisoner prisoner;
    private final T object;
    private final int position;

}
