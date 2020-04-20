package com.bgsoftware.superiorprison.plugin.commands;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.WrappedCommand;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.Contentable;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

public class CommandHelper {
    public static void sendMessage(WrappedCommand command, OMessage message, Object... objects) {
        message = message.clone();

        for (Object object : objects)
            message.replace(object, SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object));

        if (command.getSender() instanceof Player)
            message.send(command.getSenderAsPlayer());

        else
            message.getRaw().forEach(line -> Bukkit.getConsoleSender().sendMessage(Helper.color(line)));
    }

    public static void send(CommandSender sender, Contentable contentable) {
        if (contentable instanceof OMessage)
            sendMessage(sender, contentable);

        else
            sendLine(sender, contentable);
    }

    private static void sendMessage(CommandSender sender, Contentable contentable) {
        if (sender instanceof Player)
            ((OMessage)contentable).send((Player) sender);

        else
            ((OMessage)contentable).getRaw().forEach(line -> Bukkit.getConsoleSender().sendMessage(Helper.color(line)));
    }

    private static void sendLine(CommandSender sender, Contentable contentable) {
        if (sender instanceof Player)
            ((MessageLine)contentable).send((Player) sender);

        else
            Bukkit.getConsoleSender().sendMessage(Helper.color(((MessageLine)contentable).getRaw()));
    }

    public static MessageBuilder messageBuilder(Contentable message, boolean clone) {
        return new MessageBuilder(clone ? message.clone() : message);
    }

    public static MessageBuilder messageBuilder(Contentable message) {
        return messageBuilder(message, true);
    }

    public static <T extends Object> ListedBuilder<T> listedBuilder(Class<T> clazz) { return new ListedBuilder<>(); }

    public static class MessageBuilder {
        private Contentable contentable;

        private MessageBuilder(Contentable contentable) {
            this.contentable = contentable;
        }

        public MessageBuilder replace(Object ...objects) {
            for (Object object : objects)
                contentable.replace(object, SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(object));
            return this;
        }

        public MessageBuilder replace(String key, Object value) {
            return replace(ImmutableMap.of(key, value));
        }

        public MessageBuilder replace(Map<String, Object> placeholders) {
            contentable.replace(placeholders);
            return this;
        }

        public <T extends Contentable> T getAs() {
            return (T) contentable;
        }

        public void send(WrappedCommand command) {
            sendMessage(command.getSender(), contentable);
        }

        public void send(CommandSender sender) {
            sendMessage(sender, contentable);
        }
    }

    @Accessors(fluent = true, chain = true)
    public static class ListedBuilder<T> {

        private @NonNull OMessage message;

        @Setter
        private @NonNull String identifier;

        @Setter
        private Set<T> objects = Sets.newHashSet();

        @Setter
        private Set<Object> placeholderObjects = Sets.newHashSet();

        private Map<Class, Set<OPair<String, Function<Object, String>>>> placeholders = Maps.newHashMap();

        public ListedBuilder<T> addObject(T ...objects) {
            this.objects.addAll(Arrays.asList(objects));
            return this;
        }

        public ListedBuilder<T> addPlaceholderObject(Object ...objects) {
            this.placeholderObjects.addAll(Arrays.asList(objects));
            return this;
        }

        public <E extends Object> ListedBuilder<T> addPlaceholder(E object, String placeholder, Function<E, String> function) {
            Set<OPair<String, Function<Object, String>>> pairs = placeholders.computeIfAbsent(object.getClass(), clazz -> new HashSet<>());
            pairs.add(new OPair<>(placeholder, (Function<Object, String>) function));
            return this;
        }

        public ListedBuilder<T> message(OMessage message) {
            this.message = message.clone();
            return this;
        }

        private Set<OPair<String, Function<Object, String>>> findFor(Class clazz, Map<Class, Set<OPair<String, Function<Object, String>>>> allPlaceholders) {
            Optional<Class> first = allPlaceholders.keySet().stream().filter(clazz2 -> clazz == clazz2 || clazz.isAssignableFrom(clazz2) || clazz2.isAssignableFrom(clazz)).findFirst();
            if (!first.isPresent())
                return new HashSet<>();

            return allPlaceholders.getOrDefault(first.get(), new HashSet<>());
        }

        private boolean compareClasses(Class n1, Class n2) {
            List<Class> n1Classes = findAllParents(n1);
            List<Class> n2Classes = findAllParents(n2);
            return n1Classes.stream().anyMatch(c1 -> n2Classes.stream().anyMatch(c2 -> c1 == c2));
        }

        private List<Class> findAllParents(Class clazz) {
            List<Class> classes = new ArrayList<>();
            classes.add(clazz);

            if (clazz.getSuperclass() == null || clazz.getSuperclass() == Object.class)
                return classes;

            clazz = clazz.getSuperclass();
            while (clazz != Object.class) {
                classes.add(clazz);
                clazz = clazz.getSuperclass();
            }

            return classes;
        }

        private Class findParent(Class clazz) {
            Class found = null;

            Class temp = clazz;
            while (found == null) {
                Class newTemp = temp.getSuperclass();
                if (newTemp == null)
                    return temp;

                if (newTemp.isInterface() || newTemp == Object.class)
                    return temp;

                else
                    temp = newTemp;
            }
            return temp;
        }

        public OMessage build() {
            Map<Class, Set<OPair<String, Function<Object, String>>>> allPlaceholders = Maps.newHashMap();
            // Initialize placeholders for objects
            Set<Object> allObjects = new HashSet<>(placeholderObjects);

            if (!objects.isEmpty()) allObjects.add(objects.toArray()[0]);
            for (Object placeholderObject : allObjects) {
                Set<OPair<String, Function<Object, String>>> set = Sets.newHashSet();
                set.addAll(placeholders.getOrDefault(placeholderObject.getClass(), new HashSet<>()));
                set.addAll(SuperiorPrisonPlugin.getInstance().getPlaceholderController().findPlaceholdersFor(placeholderObject));
                allPlaceholders.put(findParent(placeholderObject.getClass()), set);
            }

            // Handle message stuff
            OPair<MessageLine, LineContent> line1 = message.findLine(line -> line.getText().contains(identifier));
            if (line1.getFirst() == null) return null;

            MessageLine messageLine = line1.getFirst().clone();
            messageLine.removeContentIf(lineContent -> lineContent.getText().contentEquals(line1.getSecond().getText()));

            // Replace placeholders for placeholder objects
            for (Object placeholderObject : placeholderObjects)
                messageLine.replace(placeholderObject, findFor(placeholderObject.getClass(), allPlaceholders));

            LineContent lineContent = line1.getSecond().clone();
            lineContent.replace(identifier, "");

            if (!objects.isEmpty()) {
                int count = 0;
                for (T object : objects) {
                    LineContent objectContent = lineContent.clone();
                    objectContent.replace(object, findFor(object.getClass(), allPlaceholders));

                    messageLine.append(objectContent);

                    count++;
                    if (count != objects.size())
                        messageLine.append(", ");
                }
            } else messageLine.append("None");

            return new OMessage(messageLine);
        }

        public void send(WrappedCommand command) {
            sendMessage(command.getSender(), build());
        }
    }
}
