package com.bgsoftware.superiorprison.plugin.util;

public class Test {
    public static void main(String[] args) {
        System.out.println(SNumberWrapper.of("2522525")
                .get()
                .getType());
    }
}
