package com.bgsoftware.superiorprison.plugin.condition.parser;

import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TemplateParser {
    @Getter
    private Map<OPair<String, String>, ParserFunction> values = new HashMap<>();

    @NonNull
    @Getter
    private Function<Map<String, String>, String> templateParser;

    public void addField(String valueName, String requestMessage, ParserFunction parser) {
        values.put(new OPair<>(valueName, requestMessage), parser);
    }

    public void conditionProvider(Function<Map<String, String>, String> templateParser) {
        this.templateParser = templateParser;
    }
}
