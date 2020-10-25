package com.bgsoftware.superiorprison.plugin.test.script.util;

import com.oop.orangeengine.main.util.OSimpleReflection;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtil {
    private static Map<Class<?>, Map<String, Method>> cache = new HashMap<>();

    @SneakyThrows
    public static Object invoke(Object where, String methodName) {
        Method method = getMethod(where.getClass(), methodName);
        return method.invoke(where);
    }

    public static Method getMethod(Class<?> where, String methodName) {
        Map<String, Method> nameToMethod = cache.computeIfAbsent(where.getClass(), clazz -> new HashMap<>());
        Method method = nameToMethod.get(methodName);

        if (method == null) {
            // First try to find method with default name
            method = OSimpleReflection.getMethod(where, methodName);

            // If null try to find by adding get
            if (method == null)
                method = OSimpleReflection.getMethod(where, "get" + StringUtils.capitalize(methodName));

            if (method == null)
                throw new IllegalStateException("Failed to find method for " + where.getSimpleName() + " by name " + methodName);

            nameToMethod.put(methodName, method);
        }

        return method;
    }

    public static Class getMethodReturnClass(Class where, String methodName) {
        Method method = getMethod(where, methodName);
        return method.getReturnType();
    }
}
