package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.api.data.top.TopSystem;
import com.bgsoftware.superiorprison.plugin.object.top.balance.SBalanceTopSystem;
import com.bgsoftware.superiorprison.plugin.object.top.blocks.SBlocksTopSystem;
import com.bgsoftware.superiorprison.plugin.object.top.prestige.SPrestigeTopSystem;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TopTypeArg extends CommandArgument<TopTypeArg.TopType> {
    public TopTypeArg() {
        setIdentity("type");
        setDescription("Top type");
        setMapper(in -> {
            Optional<TopType> typeOptional = Optional.ofNullable(TopType.match(in));
            return new OPair<>(typeOptional.orElse(null), "Failed to find top system by " + in);
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete((args, e) -> Arrays.stream(TopType.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList()));
    }

    @AllArgsConstructor
    @Getter
    public enum TopType {
        BLOCKS(SBlocksTopSystem.class),
        PRESTIGE(SPrestigeTopSystem.class),
        BALANCE(SBalanceTopSystem.class);

        private final Class<? extends TopSystem> clazz;

        public static TopType match(String in) {
            return Arrays.stream(values())
                    .filter(tt -> tt.name().equalsIgnoreCase(in))
                    .findFirst()
                    .orElse(null);
        }
    }
}
