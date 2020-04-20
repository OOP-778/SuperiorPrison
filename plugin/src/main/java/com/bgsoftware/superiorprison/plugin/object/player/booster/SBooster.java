package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import io.netty.bootstrap.ServerBootstrap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public abstract class SBooster implements Booster, SerializableObject {

    @Getter
    @Setter
    private int id;

    @Getter
    private long validTill;

    @Getter
    private double rate;

    public SBooster() {}

    @Override
    public void serialize(SerializedData data) {
        data.write("id", id);
        data.write("validTill", validTill);
        data.write("rate", rate);
        data.write("type", this instanceof SDropsBooster ? "drops" : "money");
    }

    @Override
    public void deserialize(SerializedData data) {
        this.id = data.applyAs("id", int.class);
        this.validTill = data.applyAs("validTill", long.class);
        this.rate = data.applyAs("rate", double.class);
    }

    public static SBooster fromElement(JsonElement element) {
        JsonObject object = element.getAsJsonObject();

        SBooster booster;
        if (object.get("type").getAsString().contentEquals("drops"))
            booster = new SDropsBooster();
        else
            booster = new SMoneyBooster();

        booster.deserialize(new SerializedData(object));
        return booster;
    }

}
