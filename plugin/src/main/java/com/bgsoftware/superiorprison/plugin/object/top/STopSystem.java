package com.bgsoftware.superiorprison.plugin.object.top;

import com.bgsoftware.superiorprison.api.data.top.TopSystem;
import lombok.SneakyThrows;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class STopSystem<T, E extends STopEntry> implements TopSystem<E> {

    private final ReentrantLock entriesLock = new ReentrantLock();
    private final List<E> entries = new LinkedList<>();

    protected abstract Comparator<T> comparator();

    protected abstract Stream<T> stream();

    protected abstract Predicate<T> filter();

    protected abstract BiFunction<T, Integer, E> constructor();

    @SneakyThrows
    public List<E> getEntries() {
        entriesLock.lock();
        List<E> entriesCopy = new LinkedList<>(entries);
        entriesLock.unlock();
        return entriesCopy;
    }

    @Override
    public void update(int entriesLimit) {
        try {
            List<T> newEntries = stream()
                    .filter(filter())
                    .sorted(comparator().reversed())
                    .limit(entriesLimit)
                    .collect(Collectors.toCollection(LinkedList::new));

            entriesLock.lock();
            entries.clear();
            int pos = 1;
            for (T newEntry : newEntries) {
                entries.add(constructor().apply(newEntry, pos));
                pos++;
            }
            entriesLock.unlock();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (entriesLock.isLocked())
                entriesLock.unlock();
        }
    }
}
