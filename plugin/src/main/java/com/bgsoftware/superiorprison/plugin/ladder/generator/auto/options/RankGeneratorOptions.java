package com.bgsoftware.superiorprison.plugin.ladder.generator.auto.options;

import com.bgsoftware.superiorprison.plugin.ladder.generator.auto.GeneratorOptions;
import com.bgsoftware.superiorprison.plugin.util.script.util.Values;
import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class RankGeneratorOptions extends GeneratorOptions<String> {
    private static final HashFunction hashing = Hashing.crc32();

    private int repeat = -1;

    // Level to Index and other way around
    private final HashBiMap<Integer, Integer> hashConverter = HashBiMap.create();

    // Rank name to index and other way around
    @Getter
    private final HashBiMap<String, Integer> rankToIndex = HashBiMap.create();

    // Indexes of chars
    private final List<Character> chars = new LinkedList<>();

    // Max level of ranks
    @Getter
    private BigInteger max = BigInteger.ONE;

    public RankGeneratorOptions(ConfigSection section, GlobalVariableMap variableMap) {
        super(variableMap);
        section.ensureHasValues("range");

        String range = section.getAs("range");
        String[] split = range.split("-");
        char start = split[0].toCharArray()[0];
        char end = split[1].toCharArray()[0];

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
            hashConverter.put(hash, max.intValue());
            rankToIndex.put(rank, max.intValue());
            max = max.add(BigInteger.ONE);
        }
    }

    @Override
    public boolean hasNext(String key) {
        int index = getIndex(key).intValueExact();
        return (index + 1) < max.intValue();
    }

    @Override
    public boolean hasPrevious(String key) {
        int index = getIndex(key).intValueExact();
        return (index - 1) != 0;
    }

    @Override
    public boolean isValid(String key) {
        return rankToIndex.inverse().containsKey(getIndex(key).intValueExact());
    }

    @Override
    public BigInteger getIndex(Object in) {
        BigInteger level;

        if (in instanceof Number) {
            return BigInteger.valueOf(((Number) in).longValue());

        } else {
            String s = in.toString();
            if (Values.isNumber(s))
                level = getIndex(Values.parseAsInt(s));
            else {
                level = BigInteger.valueOf(hashConverter.get(getHash(s)).longValue());
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
