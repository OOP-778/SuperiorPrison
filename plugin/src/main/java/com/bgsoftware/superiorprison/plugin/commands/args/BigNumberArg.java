package com.bgsoftware.superiorprison.plugin.commands.args;

import com.bgsoftware.superiorprison.plugin.util.NumberUtil;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.math.BigDecimal;

public class BigNumberArg extends CommandArgument<BigDecimal> {
    public BigNumberArg() {
        setIdentity("number");
        setDescription("A plain number or a formatted one. ex. 22.2m");
        setMapper(in -> {
            try {
                return new OPair<>(NumberUtil.formattedToBigDecimal(in), "");
            } catch (Throwable throwable) {
                return new OPair<>(null, throwable.getMessage());
            }
        });
    }
}
