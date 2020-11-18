package com.bgsoftware.superiorprison.plugin.util.script.function;

import com.bgsoftware.superiorprison.plugin.util.script.variable.GlobalVariableMap;

/*
Function executes some kind of method based of input
*/
public interface Function<T> {

    /*
    Initialize function by string
    Regex check is already done
    */
    void initialize(String string, GlobalVariableMap variableMap);

    /*
    Returns what it returns
    */
    Class<T> getType();

    /*
    Determines if the returned value should be cached
    */
    boolean isCacheable();

    /*
    Generates value based of previously initialized data
    */
    T execute(GlobalVariableMap globalVariables);

    default T executeWithTiming(GlobalVariableMap globalVariableMap) {
        long start = System.currentTimeMillis();
        T t = execute(globalVariableMap);
        System.out.println("Function done took (" + (System.currentTimeMillis() - start) + "ms) value: " + t);
        return t;
    }

    String getId();
}
