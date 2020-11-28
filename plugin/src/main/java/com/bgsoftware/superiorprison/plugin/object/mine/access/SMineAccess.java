package com.bgsoftware.superiorprison.plugin.object.mine.access;

import com.bgsoftware.superiorprison.api.data.mine.access.MineAccess;
import com.bgsoftware.superiorprison.api.data.player.Prisoner;
import com.bgsoftware.superiorprison.plugin.object.mine.SNormalMine;
import com.bgsoftware.superiorprison.plugin.object.mine.linkable.LinkableObject;
import com.bgsoftware.superiorprison.plugin.util.script.util.PasteHelper;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.bgsoftware.superiorprison.plugin.util.script.variable.VariableHelper;
import com.bgsoftware.superiorprison.plugin.util.Attachable;
import com.oop.datamodule.api.SerializedData;
import com.oop.datamodule.api.SerializableObject;
import com.oop.orangeengine.main.util.data.cache.OCache;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SMineAccess implements MineAccess, SerializableObject, LinkableObject<SMineAccess>, Attachable<SNormalMine> {

    @Getter
    private final Set<MineCondition> conditions = new HashSet<>();
    private @NonNull SNormalMine mine;

    private final OCache<UUID, Boolean> resultCache = OCache
            .builder()
            .concurrencyLevel(1)
            .expireAfter(3, TimeUnit.SECONDS)
            .build();

    @Override
    public boolean canEnter(Prisoner prisoner) {
        return resultCache.getIfAbsent(prisoner.getUUID(), () -> {
            boolean result = true;
            for (MineCondition condition : conditions) {
                if (!result) return false;

                GlobalVariableMap map = condition.getVariableMap().clone();
                map.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
                map.newOrReplace("mine", VariableHelper.createVariable(mine));

                result = condition.test(map);
            }

            return result;
        });
    }

    public boolean canEnterDebug(Prisoner prisoner) {
        return resultCache.getIfAbsent(prisoner.getUUID(), () -> {
            boolean result = true;
            for (MineCondition condition : conditions) {
                if (!result) return false;

                GlobalVariableMap map = condition.getVariableMap().clone();
                map.newOrReplace("prisoner", VariableHelper.createVariable(prisoner));
                map.newOrReplace("mine", VariableHelper.createVariable(mine));

                System.out.println("can enter: " + PasteHelper.paste(map));

                result = condition.test(map);
            }

            return result;
        });
    }

    @Override
    public void addScript(String name, String script) {
        conditions.add(new MineCondition(name, script));
    }

    @Override
    public void serialize(SerializedData serializedData) {
        serializedData.write("list", conditions);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        serializedData
                .getChildren("list")
                .get()
                .applyAsCollection()
                .forEach(sd -> conditions.add(sd.applyAs(MineCondition.class)));
    }

    @Override
    public void onChange(SMineAccess from) {
        this.conditions.clear();
        this.conditions.addAll(from.conditions);
    }

    @Override
    public String getLinkId() {
        return "access";
    }

    @Override
    public void attach(SNormalMine obj) {
        this.mine = obj;
    }
}
