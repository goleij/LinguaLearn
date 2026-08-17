package com.germanlearning.service.activity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.germanlearning.model.LessonActivity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed access to the JSON blob on {@link LessonActivity}.
 *
 * Keeping the type specific data here means adding an activity type does not
 * touch the database schema.
 */
public final class ActivityPayload {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> values;

    private ActivityPayload(Map<String, Object> values) {
        this.values = values;
    }

    public static ActivityPayload of(LessonActivity activity) {
        return parse(activity == null ? null : activity.getPayload());
    }

    public static ActivityPayload parse(String json) {
        if (json == null || json.isBlank()) {
            return new ActivityPayload(new LinkedHashMap<>());
        }
        try {
            return new ActivityPayload(MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            }));
        } catch (Exception e) {
            throw new IllegalStateException("Malformed activity payload: " + json, e);
        }
    }

    /** Builds a payload JSON string from alternating key/value pairs. */
    public static String write(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating keys and values");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write activity payload", e);
        }
    }

    public boolean has(String key) {
        return values.get(key) != null;
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public Integer getInteger(String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<String> getStringList(String key) {
        Object value = values.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            result.add(String.valueOf(item));
        }
        return result;
    }

    /** Ordered map, e.g. the pairs of a matching activity. */
    public Map<String, String> getStringMap(String key) {
        Object value = values.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            return new LinkedHashMap<>();
        }
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
        return result;
    }

    /** List of objects, e.g. dialogue lines or learn card examples. */
    public List<Map<String, String>> getObjectList(String key) {
        Object value = values.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, String> entry = new LinkedHashMap<>();
                map.forEach((k, v) -> entry.put(String.valueOf(k), String.valueOf(v)));
                result.add(entry);
            }
        }
        return result;
    }
}
