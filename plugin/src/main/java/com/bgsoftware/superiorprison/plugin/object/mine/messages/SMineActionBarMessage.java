package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineActionBarMessage;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.util.OActionBar;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SMineActionBarMessage extends SMineMessage implements MineActionBarMessage {

    @Getter
    @Setter
    private String content;

    public SMineActionBarMessage() {
        super(0);
    }

    public SMineActionBarMessage(long every, String content) {
        super(every);
        this.content = content;
    }

    @Override
    public MessageType getType() {
        return MessageType.ACTION_BAR;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "actionbar");
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

        String[] content = new String[]{this.content};
        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, hook -> content[0] = hook.parse(sender, content[0]));
        OActionBar.sendActionBar(content[0], (Player) sender);
    }
}
