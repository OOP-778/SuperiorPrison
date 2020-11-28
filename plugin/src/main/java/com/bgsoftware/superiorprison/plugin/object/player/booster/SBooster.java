package com.bgsoftware.superiorprison.plugin.object.player.booster;

import com.bgsoftware.superiorprison.api.data.player.booster.Booster;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.SerializableObject;
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

    public static SBooster fromElement(SerializedData element) {
        SBooster booster = null;
        String type = element.getElement("type").get().getAsString();

        if (type.equalsIgnoreCase("drops"))
            booster = new SDropsBooster();
        else if (type.equalsIgnoreCase("money"))
            booster = new SMoneyBooster();
        else if (type.equalsIgnoreCase("xp"))
            booster = new SXPBooster();

        booster.deserialize(element);
        return booster;
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("id", id);
        data.write("validTill", validTill);
        data.write("rate", rate);
        data.write("type", getType());
    }

    @Override
    public void deserialize(SerializedData data) {
        this.id = data.applyAs("id", int.class);
        this.validTill = data.applyAs("validTill", long.class);
        this.rate = data.applyAs("rate", double.class);
    }
}
