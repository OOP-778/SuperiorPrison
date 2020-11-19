package com.bgsoftware.superiorprison.plugin.ladder;

import com.bgsoftware.superiorprison.api.data.player.LadderObject;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParsedObject implements LadderObject {
    @Getter
    private BigInteger index;

    @Getter
    private String prefix;

    @Getter
    private List<String> commands = new ArrayList<>();

    @Getter
    private GlobalVariableMap variableMap;

    @Getter
    private Supplier<Boolean> meets;

    @Getter
    private LadderTemplate template;

    private Supplier<ParsedObject> next;
    private Supplier<ParsedObject> previous;

    @Getter
    private String name;

    private ParsedObject() {}

    public static ParsedObject of(String name, LadderTemplate template, GlobalVariableMap map, Supplier<ParsedObject> next, Supplier<ParsedObject> previous, BigInteger index) {
        ParsedObject parsedObject = new ParsedObject();
        parsedObject.name = name;
        parsedObject.variableMap = map;
        parsedObject.prefix = map.extractVariables(template.getPrefix());
        parsedObject.template = template;
        parsedObject.index = index;
        parsedObject.commands = template.getCommands()
                .stream()
                .map(map::extractVariables)
                .collect(Collectors.toList());

        parsedObject.meets = () -> template.getRequirements().meets(map).getFirst();
        parsedObject.next = next;
        parsedObject.previous = previous;
        return parsedObject;
    }

    @Override
    public String toString() {
        return "ParsedObject{" +
                "level=" + index +
                ", prefix='" + prefix + '\'' +
                ", commands=" + commands +
                ", variableMap=" + variableMap +
                ", template=" + template +
                '}';
    }

    @Override
    public Optional<LadderObject> getNext() {
        return Optional.ofNullable(next.get());
    }

    @Override
    public Optional<LadderObject> getPrevious() {
        return Optional.ofNullable(previous.get());
    }

    @Override
    public void take() {
        template.getRequirements().take(getVariableMap());
    }

    @Override
    public void executeCommands() {
        for (String command : getCommands())
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
