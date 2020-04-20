package com.bgsoftware.superiorprison.plugin.object.mine.sign;

import com.bgsoftware.superiorprison.api.data.mine.sign.Sign;
import com.bgsoftware.superiorprison.api.data.mine.sign.SignType;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class SSign implements Sign {

    private Location location;
    private SignType type;

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void update() {
    }
}
