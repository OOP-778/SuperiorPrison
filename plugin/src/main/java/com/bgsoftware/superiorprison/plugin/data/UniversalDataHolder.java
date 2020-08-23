package com.bgsoftware.superiorprison.plugin.data;

import com.bgsoftware.superiorprison.plugin.controller.DatabaseController;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.body.MultiTypeBody;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.datamodule.database.types.MySqlDatabase;
import com.oop.datamodule.database.types.SqlLiteDatabase;
import com.oop.datamodule.storage.MultiFileStorage;
import com.oop.datamodule.storage.SqlStorage;
import com.oop.datamodule.storage.Storage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

@Getter(value = AccessLevel.PROTECTED)
public abstract class UniversalDataHolder<I, T extends MultiTypeBody> extends Storage<T> {
    public Map<I, T> dataMap = new ConcurrentHashMap<>();
    public Storage<T> currentHolder;
    private Function<T, I> keyExtractor;
    private Set<Class<? extends T>> classCache = new HashSet<>();

    public UniversalDataHolder(DatabaseController controller, Function<T, I> keyExtractor) {
        super(controller);
        this.keyExtractor = keyExtractor;
    }

    protected void currentHolder(DataSettings<T> settings) {
        currentHolder = settings.toStorage(this);
    }

    @Override
    public void add(T object) {
        classCache.add((Class<? extends T>) object.getClass());
        dataMap.put(keyExtractor.apply(object), object);
        save(object, true, null);
        onAdd(object);
    }

    @Override
    public void remove(T t) {
        dataMap.remove(keyExtractor.apply(t));
        onRemove(t);
    }

    @Override
    public void save(T t, boolean b, Runnable runnable) {
        currentHolder.save(t, b, runnable);
    }

    @Override
    public Stream<T> stream() {
        return dataMap.values().stream();
    }

    @Override
    public boolean accepts(Class aClass) {
        return classCache.contains(aClass);
    }

    @Override
    public void load(boolean b, Runnable runnable) {
        currentHolder.load(b, runnable);
    }

    @Override
    public void save(boolean b, Runnable runnable) {
        currentHolder.save(b, runnable);
    }

    public void save(T object, boolean async) {
        save(object, async, null);
    }

    @Override
    public Iterator<T> iterator() {
        return dataMap.values().iterator();
    }

    @Override
    protected void onAdd(T t) {

    }

    @Override
    protected void onRemove(T t) {

    }

    @Getter()
    public abstract static class DataSettings<T extends MultiTypeBody> {
        // Sql settings

        @SneakyThrows
        public static <T extends DataSettings<E>, E extends MultiTypeBody> T builder(Class<T> settingsClass, Class<E> typeClass) {
            return settingsClass.newInstance();
        }

        abstract Storage<T> toStorage(UniversalDataHolder<?, T> parent);

        @Getter
        @Setter
        @Accessors(chain = true, fluent = true)
        public static class SQlSettings<T extends MultiTypeBody> extends DataSettings<T> {
            private boolean isFile;
            private File directory;
            private String database;
            private String hostname;
            private int port;
            private String username;
            private String password;
            private DatabaseWrapper databaseWrapper;
            private Class<? extends T>[] variants;

            @Override
            public SqlStorageImpl<T> toStorage(UniversalDataHolder<?, T> parent) {
                DatabaseWrapper database;
                if (databaseWrapper == null)
                    if (isFile)
                        database = new SqlLiteDatabase(directory, this.database + ".db");
                    else
                        database = new MySqlDatabase(new MySqlDatabase.MySqlProperties().database(this.database).password(password).port(port).url(hostname).user(username));
                else
                    database = databaseWrapper;

                return new SqlStorageImpl<>(parent, variants, database);
            }
        }

        @Getter
        @Setter
        @Accessors(chain = true, fluent = true)
        public static class FlatStorageSettings<T extends MultiTypeBody> extends DataSettings<T> {
            private File directory;
            private Map<String, Class<? extends T>> variants;

            @Override
            Storage<T> toStorage(UniversalDataHolder<?, T> parent) {
                Objects.requireNonNull(variants, "Variants cannot be null!");
                Objects.requireNonNull(directory, "Directory cannot be null!");

                if (!directory.exists())
                    directory.mkdirs();

                return new FlatStorageImpl<>(parent, variants, directory);
            }
        }
    }

    protected static class FlatStorageImpl<T extends MultiTypeBody> extends MultiFileStorage<T> {
        private Map<String, Class<? extends T>> variants;
        private UniversalDataHolder<?, T> parent;

        public FlatStorageImpl(UniversalDataHolder<?, T> parent, Map<String, Class<? extends T>> variants, File directory) {
            super(parent.getStorageHolder(), directory);
            this.variants = variants;
            this.parent = parent;
        }

        @Override
        public Map<String, Class<? extends T>> getVariants() {
            return variants;
        }

        @Override
        protected void onAdd(T t) {
            parent.add(t);
        }

        @Override
        protected void onRemove(T t) {
            parent.remove(t);
        }

        @Override
        public Stream<T> stream() {
            return parent.stream();
        }

        @Override
        public boolean accepts(Class aClass) {
            return parent.accepts(aClass);
        }

        @Override
        public Iterator<T> iterator() {
            return parent.iterator();
        }
    }

    protected static class SqlStorageImpl<T extends MultiTypeBody> extends SqlStorage<T> {
        private UniversalDataHolder<?, T> parent;
        private Class<? extends T>[] variants;

        public SqlStorageImpl(UniversalDataHolder<?, T> parent, Class<? extends T>[] variants, DatabaseWrapper database) {
            super(parent.getStorageHolder(), database);
            this.parent = parent;
            this.variants = variants;
        }

        @Override
        public Class<? extends T>[] getVariants() {
            return variants;
        }

        @Override
        protected void onAdd(T t) {
            parent.add(t);
        }

        @Override
        protected void onRemove(T t) {
            parent.remove(t);
        }

        @Override
        public Stream<T> stream() {
            return parent.stream();
        }

        @Override
        public Iterator<T> iterator() {
            return parent.iterator();
        }
    }
}
