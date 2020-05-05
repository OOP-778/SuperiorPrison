package com.bgsoftware.superiorprison.plugin.object.mine.messages;

import com.bgsoftware.superiorprison.api.data.mine.messages.MessageType;
import com.bgsoftware.superiorprison.api.data.mine.messages.MineTitleMessage;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.hook.impl.PapiHook;
import com.google.gson.JsonElement;
import com.oop.datamodule.SerializedData;
import com.oop.orangeengine.main.util.OTitle;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SMineTitleMessage extends SMineMessage implements MineTitleMessage {

    @Getter
    @Setter
    private int fadeIn;

    @Getter
    @Setter
    private int fadeOut;

    @Getter
    @Setter
    private int stay;

    @Getter
    private Optional<String> title, subTitle;

    public SMineTitleMessage() {
        super(0);
    }

    public SMineTitleMessage(long every, int fadeIn, int fadeOut, int stay, String title, String subTitle) {
        super(every);
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.stay = stay;
        this.title = Optional.ofNullable(title);
        this.subTitle = Optional.ofNullable(subTitle);
    }

    @Override
    public MessageType getType() {
        return MessageType.TITLE;
    }

    @Override
    public void serialize(SerializedData serializedData) {
        super.serialize(serializedData);
        serializedData.write("type", "title");

        title.ifPresent(s -> serializedData.write("title", s));
        subTitle.ifPresent(s -> serializedData.write("subtitle", s));

        serializedData.write("fadeIn", fadeIn);
        serializedData.write("stay", stay);
        serializedData.write("fadeOut", fadeOut);
    }

    @Override
    public void deserialize(SerializedData serializedData) {
        super.deserialize(serializedData);
        title = serializedData.getElement("title").map(JsonElement::getAsString);
        subTitle = serializedData.getElement("subtitle").map(JsonElement::getAsString);
        fadeIn = serializedData.applyAs("fadeIn", int.class);
        stay = serializedData.applyAs("stay", int.class);
        fadeOut = serializedData.applyAs("fadeOut", int.class);
    }

    @Override
    public void send(CommandSender sender) {
        if (!title.isPresent() && !subTitle.isPresent()) return;
        if (!(sender instanceof Player)) return;

        String[] title = new String[]{this.title.orElse("")};
        String[] subTitle = new String[]{this.subTitle.orElse("")};

        SuperiorPrisonPlugin.getInstance().getHookController().executeIfFound(() -> PapiHook.class, hook -> {
            title[0] = hook.parse(sender, title[0]);
            subTitle[0] = hook.parse(sender, subTitle[0]);
        });
        OTitle.sendTitle(fadeIn, stay, fadeOut, title[0].trim().length() == 0 ? null : title[0], subTitle[0].trim().length() == 0 ? null : subTitle[0], ((Player) sender));
    }

    @Override
    public void setSubTitle(String subTitle) {
        this.subTitle = Optional.ofNullable(subTitle);
    }

    public void setTitle(String title) {
        this.title = Optional.ofNullable(title);
    }
}
