package com.bgsoftware.superiorprison.plugin.protocol;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.PlayerChatFilterController;
import com.oop.orangeengine.main.util.OSimpleReflection;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.bgsoftware.superiorprison.plugin.protocol.PrisonProtocol.Constant.*;

public class PrisonProtocol extends ProtocolWrapper {
    public PrisonProtocol() {
        super(SuperiorPrisonPlugin.getInstance());
        SuperiorPrisonPlugin.getInstance().onDisable(this::close);
    }

    @Override
    @SneakyThrows
    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
        if (!packetClass.isAssignableFrom(packet.getClass())) return packet;

        StringBuilder plainMessage = new StringBuilder();
        BaseComponent[] ichatComponents = (BaseComponent[]) baseComponentsField.get(packet);
        if (ichatComponents == null) {
            Object ichatComp = iChatComponentField.get(packet);
            if (ichatComp != null)
                plainMessage = new StringBuilder((String) chatCompToString.invoke(ichatComp));

        } else {
            for (BaseComponent ichatComponent : ichatComponents)
                plainMessage.append(ichatComponent.toPlainText());
        }

        PlayerChatFilterController playerChatFilterController = SuperiorPrisonPlugin.getInstance().getPlayerChatFilterController();
        if (playerChatFilterController == null || !playerChatFilterController.isFiltered(receiver.getUniqueId()))
            return packet;

        if (!playerChatFilterController.validate(plainMessage.toString()))
            return null;

        return packet;
    }

    public static class Constant {
        public static Class<?>
                packetClass,
                iChatComponentClass;

        public static Field
                baseComponentsField,
                iChatComponentField;

        public static Method
                chatCompToString;

        static {
            try {
                packetClass = OSimpleReflection.findClass("{nms}.PacketPlayOutChat");
                iChatComponentClass = OSimpleReflection.findClass("{nms}.IChatBaseComponent");

                baseComponentsField = OSimpleReflection.getField(packetClass, "components");
                iChatComponentField = OSimpleReflection.getField(packetClass, "a");

                chatCompToString = OSimpleReflection.getMethod(iChatComponentClass, "c");

            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to initialize Prison Protocol!");
            }
        }
    }

}
