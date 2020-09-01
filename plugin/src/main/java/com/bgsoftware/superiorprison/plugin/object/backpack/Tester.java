package com.bgsoftware.superiorprison.plugin.object.backpack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Tester {
    public static void main(String[] args) {

        int rowsSize = 3;
        Object[] objects = new Object[9 * 3 * 4];
        int newRowSize = 4;

        objects[0] = "slot 1";
        objects[9 * 3] = "slot 1 page 2";

        System.out.println("Before modification");

        int slot = 0;
        int page = 1;

        Map<Integer, Map<Integer, Object>> pagesData = new HashMap<>();
        for (int i = 0; i < objects.length; i++) {
            if (slot == rowsSize * 9) {
                page++;
                slot = 0;
            }
            if (objects[i] != null) {
                pagesData.computeIfAbsent(page, key -> new HashMap<>()).put(slot, objects[i]);
                System.out.println("page: " + page +", slot: " + slot + ", obj: " + objects[i]);
            }
            slot++;
        }

        System.out.println("Migrating");

        page = 1;
        slot = 0;
        objects = new Object[newRowSize * 9 * 4];
        for (int i = 0; i < objects.length; i++) {
            if (slot == newRowSize * 9) {
                page++;
                slot = 0;
            }

            int finalSlot = slot;
            Object oldData = Optional
                    .ofNullable(pagesData.get(page))
                    .flatMap(map -> Optional.ofNullable(map.get(finalSlot)))
                    .orElse(null);
            if (oldData != null) {
                objects[i] = oldData;
                System.out.println("found old data at page: " + page +", slot: " + slot + ", obj: " + objects[i]);
            }
            slot++;
        }

        System.out.println("migrated data");
        page = 1;
        slot = 0;
        for (int i = 0; i < objects.length; i++) {
            if (slot == newRowSize * 9) {
                page++;
                slot = 0;
            }
            System.out.println("page: " + page +", slot: " + slot + ", obj: " + objects[i]);
            slot++;
        }
    }

    public static void perform(String id, Runnable runnable) {
        System.out.println("=== Start " + id + " ===");
        long startTime = System.currentTimeMillis();
        runnable.run();
        System.out.println("=== End " + id + " Took " + (System.currentTimeMillis() - startTime) + "ms ===");
    }
}
