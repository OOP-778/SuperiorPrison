package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineChatMessage;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OActionBar;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMineChatMessage extends SMineMessage implements MineChatMessage {

    @Getter @Setter
    private String content;

    public SMineChatMessage() {
        super(0);
    }

    public SMineChatMessage(long every, String content) {
        super(every);
        this.content = content;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAT;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "chat");
        serializedData.write("content", content);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        super.deserialize(serializedData);
        this.content = serializedData.applyAs("content", String.class);
    }

    @Override
    public void send(CommandSender sender) {
        if (content == null) return;
        if (!(sender instanceof Player)) return;

        String content[] = new String[]{this.content};
        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, hook -> content[0] = hook.parse(sender, content[0]));
        sender.sendMessage(Helper.color(content[0]));
    }
}
