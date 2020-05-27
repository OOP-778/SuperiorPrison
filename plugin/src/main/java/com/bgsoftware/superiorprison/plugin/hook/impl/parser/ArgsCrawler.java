package com.bgsoftware.superiorprison.plugin.hook.impl.parser;

import java.util.Arrays;

public class ArgsCrawler {

    private int index = -1;
    private final String[] args;

    public ArgsCrawler(String[] args) {
        this.args = args;
    }

    public boolean hasNext() {
        return args.length > index + 1;
    }

    public ArgsCrawler back() {
        index -= 1;
        return this;
    }

    public String next() {
        index += 1;
        return args[index];
    }

    public String current() {
        return args[index];
    }

    public ArgsCrawler cloneFromIndex() {
        return new ArgsCrawler(Arrays.copyOfRange(args, index + 1, args.length));
    }

    @Override
    public String toString() {
        return Arrays.toString(args);
    }
}
