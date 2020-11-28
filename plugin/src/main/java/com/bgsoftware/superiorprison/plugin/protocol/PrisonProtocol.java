package com.bgsoftware.superiorprison.plugin.protocol;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.bgsoftware.superiorprison.plugin.controller.PlayerChatFilterController;
import com.oop.orangeengine.main.events.SyncEvents;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.OSimpleReflection;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorprison.plugin.protocol.PrisonProtocol.Constant.*;

public class PrisonProtocol extends ConnectionInjector {
    private final Map<UUID, Registration> registrationMap = new HashMap<>();

    public PrisonProtocol() {
        SyncEvents.listen(PlayerJoinEvent.class, event -> {
            new OTask()
                    .sync(true)
                    .delay(TimeUnit.MILLISECONDS, 200)
                    .runnable(() -> {
                        if (!event.getPlayer().isOnline()) return;
                        registrationMap.put(event.getPlayer().getUniqueId(), inject(event.getPlayer()));
                    })
                    .execute();
        });

        SyncEvents.listen(PlayerQuitEvent.class, event -> {
            Optional
                    .ofNullable(registrationMap.remove(event.getPlayer().getUniqueId()))
                    .ifPresent(Registration::close);
        });

        on(packetClass, (player, packet) -> {
            try {
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
                if (playerChatFilterController == null || !playerChatFilterController.isFiltered(player.getUniqueId()))
                    return true;

                if (!playerChatFilterController.validate(plainMessage.toString()))
                    return false;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to handle chat packet!", throwable);
            }

            return true;
        });
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
