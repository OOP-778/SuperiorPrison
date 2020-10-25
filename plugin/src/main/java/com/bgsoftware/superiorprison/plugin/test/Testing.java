package com.bgsoftware.superiorprison.plugin.test;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.test.generator.ParsedObject;
import com.bgsoftware.superiorprison.plugin.test.generator.auto.generator.RankGenerator;
import com.bgsoftware.superiorprison.plugin.test.requirement.RequirementController;
import com.bgsoftware.superiorprison.plugin.test.script.util.Values;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.Config;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;

public class Testing {
    public static RequirementController controller;

    public static void main(String[] args) {
        controller = new RequirementController();
        OFile file = new OFile(new File("test/ranks.yml")).createIfNotExists(true);
        Config config = new Config(file);

        RankGenerator generator = new RankGenerator(config);
        OChatMessage message = new OChatMessage(
                "&cSummary of rank {1}",
                "&7Rank Index: &4{2}",
                "&7Rank Hash: &4{3}",
                "&7Took: &4{4}"
        );

        SyncEvents.listen(AsyncPlayerChatEvent.class, event -> {
            if (Values.isNumber(event.getMessage())) {
                long start = System.currentTimeMillis();
                int i = Values.parseAsInt(event.getMessage());
                ParsedObject parsed = generator.getParsed(SuperiorPrisonPlugin.getInstance().getPrisonerController().getInsertIfAbsent(event.getPlayer()), i);
                message.clone()
                        .replace("{1}", parsed.getPrefix())
                        .replace("{2}", parsed.getLevel())
                        .replace("{3}", generator.getOptions().getHash(event.getMessage()))
                        .replace("{4}", (System.currentTimeMillis() - start))
                        .send(event.getPlayer());
            }
        });

//        long start = System.currentTimeMillis();
//        System.out.println("Output rank: " + generator.getOptions().getRankByIndex(generator.getOptions().getIndex(e3)));
//        System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");

//        ImmutableMap map = new ImmutableMap();
//
//        List<String> ranks = new LinkedList<>();
//        int repeat = 400000;
//        IntStream
//                .range('A', 'Z' + 1)
//                .forEach(rankId -> {
//                    ranks.add((char) rankId + "");
//                    for (int i = 2; i < repeat + 1; i++)
//                        ranks.add((char) rankId + "" + i);
//                });
//
//        System.out.println("ranks: " + ranks.size());
//        int a = 0;
//
//        List<Long> speeds = new ArrayList<>();
//
//        long start1 = System.currentTimeMillis();
//        long start = 0;
//        for (String rank : ranks) {
//            try {
//                start = System.currentTimeMillis();
//                map.put(Hashing.crc32().hashString(rank, StandardCharsets.UTF_8), rank);
//                speeds.add(System.currentTimeMillis() - start);
//            } catch (Exception ex) {
//                System.out.println("Max ranks generated: " + a);
//                break;
//            }
//            a++;
//        }
//
//        System.out.println("Took: " + (System.currentTimeMillis() - start1) + "ms");
//        System.out.println("Average: " + speeds.stream().mapToLong(Long::longValue).average().orElse(-1));
    }
}
