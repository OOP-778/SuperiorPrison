package com.bgsoftware.superiorprison.plugin.condition;

import com.bgsoftware.superiorprison.plugin.condition.parser.TemplateParser;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true)
@Getter
public class MineConditionTemplate {

    @NonNull
    @Setter
    private String name;

    @NonNull
    @Setter
    private List<String> description;

    @NonNull
    private TemplateParser parser;

    public void description(String... lines) {
        description = new ArrayList<>(Arrays.asList(lines));
    }

    public List<String> description() {
        return description;
    }

    public void parser(Consumer<TemplateParser> parserConsumer) {
        this.parser = new TemplateParser();
        parserConsumer.accept(parser);
    }
}
