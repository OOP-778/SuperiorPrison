package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.object.chat.ChatFormat;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.yaml.Config;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatController implements OComponent<SuperiorPrisonPlugin> {

    private Map<String, ChatFormat> formatMap = new ConcurrentHashMap<>();

    @Getter
    private boolean enabled;

    @Override
    public boolean load() {
        formatMap.clear();

        Config chatConfig = getPlugin().getConfigController().getChatConfig();
        this.enabled = chatConfig.getAs("enabled");

        chatConfig.ifSectionPresent("formats", parent -> parent.getSections().values().forEach(section -> formatMap.put(section.getKey(), ChatFormat.of(section))));
        return true;
    }

    public List<ChatFormat> findAllMatchingFor(SPrisoner prisoner) {
        return formatMap.values()
                .stream()
                .filter(format -> format.getPermission() == null || prisoner.getPlayer().hasPermission(format.getPermission()))
                .collect(Collectors.toList());
    }

    public ChatFormat findHighest(SPrisoner prisoner) {
        List<ChatFormat> allMatching = findAllMatchingFor(prisoner);
        if (allMatching.isEmpty()) return null;

        return allMatching
                .stream()
                .max(Comparator.comparingInt(ChatFormat::getOrder))
                .get();
    }
}
