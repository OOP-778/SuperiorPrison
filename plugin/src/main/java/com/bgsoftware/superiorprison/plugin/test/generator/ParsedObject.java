package com.bgsoftware.superiorprison.plugin.test.generator;

import com.bgsoftware.superiorprison.plugin.test.generator.auto.ObjectGenerator;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class ParsedObject {
    private int level;
    private String prefix;
    private List<String> commands = new ArrayList<>();
    private OMessage<?> message;
    private GlobalVariableMap variableMap;

    private Supplier<Boolean> meets;
    private ObjectTemplate template;

    private Supplier<ParsedObject> next;
    private Supplier<ParsedObject> previous;

    private ParsedObject() {}

    public static ParsedObject of(ObjectTemplate template, GlobalVariableMap map, Supplier<ParsedObject> next, Supplier<ParsedObject> previous) {
        ParsedObject parsedObject = new ParsedObject();
        parsedObject.variableMap = map;
        parsedObject.prefix = map.extractVariables(template.getPrefix());
        parsedObject.template = template;
        parsedObject.level = map.getRequiredVariableByInput("level", Number.class).get(map).intValue();
        parsedObject.commands = template.getCommands()
                .stream()
                .map(map::extractVariables)
                .collect(Collectors.toList());
        parsedObject.message = template.getMessage() == null ? null :
                (OMessage<?>) template.getMessage().clone()
                        .replace(in -> map.extractVariables(in.toString()));

        parsedObject.meets = () -> template.getRequirements().meets(map);
        parsedObject.next = next;
        parsedObject.previous = previous;
        return parsedObject;
    }
}
