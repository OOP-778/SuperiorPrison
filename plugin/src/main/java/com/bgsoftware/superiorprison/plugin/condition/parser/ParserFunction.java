package com.bgsoftware.superiorprison.plugin.condition.parser;

import com.bgsoftware.superiorprison.api.util.Pair;

@FunctionalInterface
public interface ParserFunction {
    String parse(String input);
}
