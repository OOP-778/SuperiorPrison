package com.bgsoftware.superiorprison.plugin.util;

import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DebuggableList<T> implements List<T> {
    private final List<T> collection;
    private final String name;

    public DebuggableList(String name, List<T> collection) {
        this.name = name;
        this.collection = collection;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return collection.toArray(a);
    }

    @Override
    public boolean add(T t) {
        SuperiorPrisonPlugin.getInstance().getOLogger().print("<{}> On Add {}", name, t);
        return collection.add(t);
    }

    @Override
    public boolean remove(Object o) {
        SuperiorPrisonPlugin.getInstance().getOLogger().print("<{}> On Remove {}", name, o);
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return collection.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return collection.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return collection.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public T get(int index) {
        return collection.get(index);
    }

    @Override
    public T set(int index, T element) {
        return collection.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        collection.add(index, element);
    }

    @Override
    public T remove(int index) {
        return collection.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return collection.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return collection.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return collection.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return collection.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return collection.subList(fromIndex, toIndex);
    }
}
