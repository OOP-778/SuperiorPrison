package com.bgsoftware.superiorprison.plugin.util.placeholders.parser;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Parser<P, T> implements Cloneable {

    private final Map<String, Parser> children = new HashMap<>();
    private final Map<String, PlaceholderFunction<P, T, Object>> parsers = new HashMap<>();
    private int index;
    private @NonNull Parser<P, Object> parent;
    private String id;
    private T object;
    private Class<T> clazz;
    private PlaceholderFunction<P, T, T> mapper;

    public Parser(Parser parent, Class<T> clazz, String id) {
        this.parent = parent;
        this.id = id;
        this.clazz = clazz;
    }

    public Parser() {
    }

    public <E> Parser<T, E> add(String id, Class<E> clazz) {
        Parser<T, E> parser = new Parser<>(this, clazz, id.toLowerCase());
        children.put(id.toLowerCase(), parser);
        return parser;
    }

    public Parser<P, T> parse(String id, BiFunction<T, ArgsCrawler, Object> func) {
        return parse(id, (object, parent, crawler) -> func.apply(object, crawler));
    }

    public Parser<P, T> parse(String id, Function<T, Object> func) {
        return parse(id, (object, parent, crawler) -> func.apply(object));
    }

    public Parser<P, T> parse(String id, PlaceholderFunction<P, T, Object> func) {
        String[] split = id.toLowerCase().split("/");
        for (String s : split) {
            parsers.put(s, func);
        }
        return this;
    }

    public Parser<P, T> mapper(PlaceholderFunction<P, T, T> mapper) {
        this.mapper = mapper;
        return this;
    }

    public <E, V> Parser<V, E> parent(Class<E> parentClazz, Class<V> parentValueClazz) {
        return (Parser<V, E>) parent;
    }

    public String parse(String[] args, ObjectCache cache) {
        return parse(null, null, new ArgsCrawler(args), cache);
    }

    public String parse(Object current, Object parent, ArgsCrawler crawler, ObjectCache cache) {
        if (crawler.hasNext()) {
            try {
                String identifier = crawler.next().toLowerCase();

                if (mapper != null && current == null) {
                    crawler.back();
                    current = mapper.get(null, (P) parent, crawler);
                    identifier = current == null ? crawler.current() : crawler.hasNext() ? crawler.next() : "";
                }

                if (current == null)
                    current = cache.getAs(clazz);

                Parser placeholderParser = children.get(identifier.toLowerCase());
                if (placeholderParser != null)
                    return placeholderParser.parse(null, current, crawler.cloneFromIndex(), cache);

                PlaceholderFunction parser = parsers.get(identifier.toLowerCase());
                if (parser != null) {
                    Object parsed = parser.get(current, parent, crawler);
                    return parsed == null ? "none" : parsed.toString();
                }
            } catch (Throwable throwable) {
                if (throwable instanceof NullPointerException) {
                    return "none";

                } else
                    throwable.printStackTrace();
            }
        }
        return "none";
    }
}
