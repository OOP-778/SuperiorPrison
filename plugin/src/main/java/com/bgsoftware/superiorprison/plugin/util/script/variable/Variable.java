package com.bgsoftware.superiorprison.plugin.util.script.variable;

/*
Variable is used to get data
It can be used multiple times, hence it's cached
It has a getType() which returns a class of what it returns
*/
public interface Variable<T> {
    /*
    Get type of the object
    */
    Class<T> getType();

    /*
    Returns the data based of what it got previously
    */
    T get(GlobalVariableMap globalVariableMap);

    default T getWithTiming(GlobalVariableMap globalVariableMap) {
        long start = System.currentTimeMillis();
        T t = get(globalVariableMap);
        System.out.println("Variable GET done took (" + (System.currentTimeMillis() - start) + "ms) value: " + t);
        return t;
    }
}
