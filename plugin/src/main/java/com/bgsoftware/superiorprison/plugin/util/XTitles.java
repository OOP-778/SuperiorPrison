package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.util.OSimpleReflection;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class XTitles {
    /**
     * Used for the "stay" feature of titles.
     */
    private static final Object TIMES;
    private static final Object TITLE;
    private static final Object SUBTITLE;
    private static final Object CLEAR;

    /**
     * PacketPlayOutTitle Types: TITLE, SUBTITLE, ACTIONBAR, TIMES, CLEAR, RESET;
     */
    private static final MethodHandle PACKET;
    /**
     * ChatComponentText JSON message builder.
     */
    private static final MethodHandle CHAT_COMPONENT_TEXT;

    static {
        try {
            Class<?> chatComponentText = OSimpleReflection.Package.NMS.getClass("ChatComponentText");
            Class<?> packet = OSimpleReflection.Package.NMS.getClass("PacketPlayOutTitle");
            Class<?> titleTypes = packet.getDeclaredClasses()[0];
            MethodHandle packetCtor = null;
            MethodHandle chatComp = null;

            Object times = null;
            Object title = null;
            Object subtitle = null;
            Object clear = null;

            for (Object type : titleTypes.getEnumConstants()) {
                switch (type.toString()) {
                    case "TIMES":
                        times = type;
                        break;
                    case "TITLE":
                        title = type;
                        break;
                    case "SUBTITLE":
                        subtitle = type;
                        break;
                    case "CLEAR":
                        clear = type;
                }
            }

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                chatComp = lookup.findConstructor(chatComponentText, MethodType.methodType(void.class, String.class));

                packetCtor = lookup.findConstructor(packet,
                        MethodType.methodType(void.class, titleTypes,
                                OSimpleReflection.Package.NMS.getClass("IChatBaseComponent"), int.class, int.class, int.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            TITLE = title;
            SUBTITLE = subtitle;
            TIMES = times;
            CLEAR = clear;

            PACKET = packetCtor;
            CHAT_COMPONENT_TEXT = chatComp;
        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to initialize XTitles!", thrw);
        }
    }

    public static void sendTitle(@Nonnull Player player,
                                 int fadeIn, int stay, int fadeOut,
                                 @Nullable String title, @Nullable String subtitle) {
        Objects.requireNonNull(player, "Cannot send title to null player");
        if (title == null && subtitle == null) return;

        try {
            Object timesPacket = PACKET.invoke(TIMES, CHAT_COMPONENT_TEXT.invoke(title), fadeIn, stay, fadeOut);
            OSimpleReflection.Player.sendPacket(player, timesPacket);

            if (title != null) {
                Object titlePacket = PACKET.invoke(TITLE, CHAT_COMPONENT_TEXT.invoke(ChatColor.translateAlternateColorCodes('&', title)), fadeIn, stay, fadeOut);
                OSimpleReflection.Player.sendPacket(player, titlePacket);
            }
            if (subtitle != null) {
                Object subtitlePacket = PACKET.invoke(SUBTITLE, CHAT_COMPONENT_TEXT.invoke(ChatColor.translateAlternateColorCodes('&', subtitle)), fadeIn, stay, fadeOut);
                OSimpleReflection.Player.sendPacket(player, subtitlePacket);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Sends a title message with title and subtitle with normal
     * fade in, stay and fade out time to a player.
     *
     * @param player   the player to send the title to.
     * @param title    the title message.
     * @param subtitle the subtitle message.
     * @see #sendTitle(Player, int, int, int, String, String)
     * @since 1.0.0
     */
    public static void sendTitle(@Nonnull Player player, @Nonnull String title, @Nonnull String subtitle) {
        sendTitle(player, 10, 20, 10, title, subtitle);
    }

    /**
     * Parses and sends a title from the config.
     * The configuration section must at least
     * contain {@code title} or {@code subtitle}
     *
     * <p>
     * <b>Example:</b>
     * <blockquote><pre>
     *     ConfigurationSection titleSection = plugin.getConfig().getConfigurationSection("restart-title");
     *     Titles.sendTitle(player, titleSection);
     * </pre></blockquote>
     *
     * @param player the player to send the title to.
     * @param config the configuration section to parse the title properties from.
     * @since 1.0.0
     */
    public static void sendTitle(@Nonnull Player player, @Nonnull ConfigurationSection config) {
        String title = config.getString("title");
        String subtitle = config.getString("subtitle");

        int fadeIn = config.getInt("fade-in");
        int stay = config.getInt("stay");
        int fadeOut = config.getInt("fade-out");

        if (fadeIn < 1) fadeIn = 10;
        if (stay < 1) stay = 20;
        if (fadeOut < 1) fadeOut = 10;

        sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }
}
