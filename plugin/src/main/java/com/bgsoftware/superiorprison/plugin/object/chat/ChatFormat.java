package com.bgsoftware.superiorprison.plugin.object.chat;

import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

@Getter
public class ChatFormat {

    private OMessage format;
    private int order = -1;
    private String permission = null;

    private ChatFormat() {
    }

    public static ChatFormat of(ConfigSection section) {
        ChatFormat format = new ChatFormat();
        format.format = YamlMessage.load(section.getSection("format").get());

        section.ifValuePresent("order", int.class, order -> format.order = order);
        section.ifValuePresent("permission", String.class, permission -> format.permission = permission);
        return format;
    }
}
