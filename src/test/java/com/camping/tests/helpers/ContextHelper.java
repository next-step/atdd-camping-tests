package com.camping.tests.helpers;

import java.util.HashMap;
import java.util.Map;

public class ContextHelper {
    private static final ThreadLocal<ContextHelper> THREAD_LOCAL = ThreadLocal.withInitial(ContextHelper::new);

    private final Map<String, Object> data = new HashMap<>();

    private static ContextHelper getInstance() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }

    public static void clearContext() {
        clear();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> type) {
        return (T) getInstance().data.get(key);
    }

    public static void set(String key, Object value) {
        getInstance().data.put(key, value);
    }

}
