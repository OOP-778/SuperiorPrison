package com.bgsoftware.superiorprison.plugin.protocol;

import com.oop.orangeengine.main.util.OSimpleReflection;
import io.netty.channel.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import static com.oop.orangeengine.main.util.OSimpleReflection.findClass;
import static java.util.Collections.emptyList;

public abstract class ConnectionInjector {
    private static final String HANDLER = "packet_handler", INJECTOR = "SP_PACKET_INJECTOR";
    private final Map<Type, Collection<BiPredicate<Player, Object>>> packetListeners = new IdentityHashMap<>();

    private static final Method getPlayerHandle = OSimpleReflection.getMethod(findClass("{cb}.entity.CraftPlayer"), "getHandle");
    private static final Field getConnection = OSimpleReflection.getField(findClass("{nms}.EntityPlayer"), "playerConnection");
    private static final Field getManager = OSimpleReflection.getField(findClass("{nms}.PlayerConnection"), "networkManager");
    private static final Field getChannel = OSimpleReflection.getField(findClass("{nms}.NetworkManager"), Channel.class);

    private boolean handle(Player player, Object packet) {
        return packetListeners.getOrDefault(packet.getClass(), emptyList()).stream().allMatch(listener ->
                listener.test(player, packet)
        );
    }

    public Registration inject(Player player) {
        final ChannelHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
                if (handle(player, packet)) super.channelRead(context, packet);
            }

            @Override
            public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
                if (handle(player, packet)) super.write(context, packet, promise);
            }
        };

        try {
            Object invoke = getPlayerHandle.invoke(player);
            Object connection = getConnection.get(invoke);
            Object networkManager = getManager.get(connection);
            Channel channel = (Channel) getChannel.get(networkManager);

            channel.pipeline().addBefore(HANDLER, INJECTOR, handler);

            return () -> {
                if (channel.isOpen())
                    channel.pipeline().remove(INJECTOR);
            };
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to inject Channel Handler for " + player.getName(), throwable);
        }
    }

    public Registration on(Class type, BiPredicate<Player, Object> listener) {
        final Collection listeners = packetListeners.computeIfAbsent(type, $ -> new ArrayList<>());
        return () -> listeners.remove(listener);
    }

    public Registration on(Class type, BiConsumer<Player, Object> listener) {
        return on(type, (player, packet) -> { listener.accept(player, packet); return true; });
    }

    public interface Registration extends Closeable {
        @Override void close();
        default void unregister(){ close(); }
    }
}
