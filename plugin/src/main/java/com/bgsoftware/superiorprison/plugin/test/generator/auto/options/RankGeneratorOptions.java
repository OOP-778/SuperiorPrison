package com.bgsoftware.superiorprison.plugin.test.generator.auto.options;

import com.bgsoftware.superiorprison.plugin.test.generator.auto.GeneratorOptions;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.bgsoftware.superiorprison.plugin.test.script.variable.GlobalVariableMap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class RankGeneratorOptions extends GeneratorOptions<String> {
    private static final HashFunction hashing = Hashing.crc32();

    private char start;
    private char end;
    private int repeat = -1;

    // Level to Index and other way around
    private HashBiMap<Integer, Integer> hashConverter = HashBiMap.create();

    // Rank name to index and other way around
    @Getter
    private HashBiMap<String, Integer> rankToIndex = HashBiMap.create();

    // Indexes of chars
    private List<Character> chars = new LinkedList<>();

    // Max level of ranks
    private int max = 1;

    public RankGeneratorOptions(ConfigSection section, GlobalVariableMap variableMap) {
        super(variableMap);
        section.ensureHasValues("range");

        String range = section.getAs("range");
        String[] split = range.split("-");
        start = split[0].toCharArray()[0];
        end = split[1].toCharArray()[0];

        section.ifValuePresent("repeat", int.class, v -> this.repeat = v);
        List<String> ranks = new LinkedList<>();
        IntStream
                .range(start, end + 1)
                .forEach(rankId -> {
                    chars.add((char) rankId);
                    ranks.add((char) rankId + "");
                    if (repeat != -1) {
                        for (int i = 2; i < repeat + 1; i++)
                            ranks.add((char) rankId + "" + i);
                    }
                });

        for (String rank : ranks) {
            int hash = getHash(rank);
            hashConverter.put(hash, max);
            rankToIndex.put(rank, max);
            max++;
        }
    }

    @Override
    public boolean hasNext(String key) {
        int index = getIndex(key);
        return (index + 1) < max;
    }

    @Override
    public boolean hasPrevious(String key) {
        int index = getIndex(key);
        return (index - 1) != 0;
    }

    @Override
    public boolean isValid(String key) {
        return rankToIndex.inverse().containsKey(getIndex(key));
    }

    @Override
    public int getIndex(Object in) {
        Integer level;

        if (in instanceof Number) {
            return ((Number) in).intValue();

        } else {
            String s = in.toString();
            if (Values.isNumber(s))
                level = getIndex(Values.parseAsInt(s));
            else {
                level = hashConverter.get(getHash(s));
            }
        }

        return Objects.requireNonNull(level, "Failed to find rank by " + in);
    }

    public int getHash(String key) {
        return hashing.hashString(key, StandardCharsets.UTF_8).asInt();
    }

    public String getRankByIndex(int index) {
        return Objects.requireNonNull(rankToIndex.inverse().get(index), "Failed to find rank by index: " + index);
    }

    public String getRankByHash(int hash) {
        return Objects.requireNonNull(rankToIndex.inverse().get(Objects.requireNonNull(hashConverter.get(hash), "Failed to find a valid index for hash: " + hash)), "Failed to find rank by hash: " + hash);
    }
}
