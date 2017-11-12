package de.arraying.kotys;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright 2017 Arraying
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class JSON {

    private final Map<String, Object> rawContent = new HashMap<>();
    private final JSONUtil util = new JSONUtil();

    /**
     * Creates an empty JSON object.
     */
    public JSON() {}

    /**
     * Creates a new JSON object from a string.
     * @param rawJSON The JSON represented as a string.
     *                This can be, for example, {"hello":"world"}
     *                The string can contain whitespaces, all of these will be removed during processing.
     * @throws IllegalArgumentException If the provided string is null or empty.
     * @throws IllegalStateException If the provided JSON string is invalid.
     */
    public JSON(String rawJSON)
            throws IllegalArgumentException, IllegalStateException {
        if(rawJSON == null) {
            throw new IllegalArgumentException("Provided string is null");
        }
        if(rawJSON.isEmpty()) {
            throw new IllegalArgumentException("Provided string is empty");
        }
        JSONTokenizer.Token[] tokens = new JSONTokenizer(rawJSON).getTokens();
        if(tokens.length < 2) {
            throw new IllegalStateException("Too little tokens");
        }
        if(tokens[0].getType() != JSONTokenizer.Type.OBJECT_OPEN
                || tokens[tokens.length-1].getType() != JSONTokenizer.Type.OBJECT_CLOSE) {
            throw new IllegalArgumentException("A JSON object must start with { and end with }");
        }
        if(tokens.length == 2) {
            return;
        }
        new JSONParser().parse(this, Arrays.copyOfRange(tokens, 1, tokens.length-1));
    }

    /**
     * Creates a new JSON object from a Map.
     * The map content will be iterated over, and added to the local cache.
     * If at any time an error should occur throughout the iteration then the existing objects will still remain.
     * The contents of this map will be parsed, and each non-JSON datatype object will be attempted to turn into a sub-JSON object.
     * Non-string keys are not allowed.
     * @param map A map of existing key-value objects.
     * @throws IllegalArgumentException If the provided map is null, or the keys are not strings/null.
     */
    public JSON(Map<?, ?> map)
            throws IllegalArgumentException {
        if(map == null) {
            throw new IllegalArgumentException("Provided map is null");
        }
        for(Map.Entry<?, ?> entry : map.entrySet()) {
            if(entry.getKey() == null) {
                throw new IllegalArgumentException("One of the map entry keys is null");
            }
            if(!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("One of the map entry keys is not a string");
            }
            String key = (String) entry.getKey();
            rawContent.put(key, util.getFinalValue(entry.getValue()));
        }
    }

    /**
     * Creates a new JSON object from a container.
     * This will look for all getter methods (public), and then use the result of these methods.
     * If a getter returns an object, it will attempt to parse this this object too, etc.
     * If an error occurs invoking the getter, it will be ignored silently, and the object won't be added.
     * @param container A container.
     * @throws IllegalArgumentException If the provided container is null, or not a container.
     */
    public JSON(Object container)
            throws IllegalArgumentException {
        if(container == null) {
            throw new IllegalArgumentException("Provided container is null");
        }
        Object result = util.getFinalValue(container);
        if(!(result instanceof JSON)) {
            throw new IllegalArgumentException("Provided object is not a container");
        }
        rawContent.putAll(((JSON) result).rawContent);
    }

    /**
     * Adds an entry to the current JSON object.
     * This entry can either be a raw JSON data type, or a custom object.
     * For more information on how custom object parsing works, see {@link #JSON(Object)}
     * @param key The key.
     * @param entry The entry.
     * @throws IllegalArgumentException If the key is null.
     */
    public void put(String key, Object entry)
            throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        rawContent.put(key, util.getFinalValue(entry));
    }

    /**
     * Whether or not a specified key exists in the object.
     * @param key The key.
     * @return True if is exists, false otherwise,
     * @throws IllegalArgumentException If the key is null.
     */
    public boolean has(String key)
            throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return rawContent.containsKey(key);
    }

    /**
     * Gets an Object object from all entries.
     * @param key The key.
     * @return The entry as an Object, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not an Object.
     */
    public Object object(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return rawContent.get(key);
    }

    /**
     * Gets a String object from all entries.
     * @param key The key.
     * @return The entry as a String, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a String.
     */
    public String string(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (String) rawContent.get(key);
    }

    /**
     * Gets an Integer object from all entries.
     * @param key The key.
     * @return The entry as an Integer, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not an Integer.
     */
    public Integer integer(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (Integer) rawContent.get(key);
    }

    /**
     * Gets a Double object from all entries.
     * @param key The key.
     * @return The entry as a Double, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a Double.
     */
    public Double decimal(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (Double) rawContent.get(key);
    }

    /**
     * Gets a Boolean object from all entries.
     * @param key The key.
     * @return The entry as a Boolean, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a Boolean.
     */
    public Boolean bool(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (Boolean) rawContent.get(key);
    }

    /**
     * Gets a JSON object from all entries.
     * @param key The key.
     * @return The entry as a JSON, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a JSON.
     */
    public JSON json(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (JSON) rawContent.get(key);
    }

    /**
     * Gets a JSONArray object from all entries.
     * @param key The key.
     * @return The entry as a JSONArray, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a JSONArray.
     */
    public JSONArray array(String key)
            throws IllegalArgumentException, ClassCastException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        return (JSONArray) rawContent.get(key);
    }

    /**
     * Gets a raw map implementation of the JSON object.
     * This can be used instead of the provided methods, however it requires a lot of casting.
     * @return A local map of JSON keys and values.
     */
    public final Map<String, Object> raw() {
        return Collections.unmodifiableMap(rawContent);
    }

    /**
     * Gets the number of keys in the JSON object.
     * @return The number of keys.
     */
    public final int length() {
        return rawContent.size();
    }

    /**
     * Marshals the JSON object into a string.
     * @return A string representation of the JSON object.
     */
    public final String marshal() {
        JSONFormatter formatter = new JSONFormatter()
                .startObject();
        Iterator<Map.Entry<String, Object>> iterator = rawContent.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            formatter.objectKey(entry.getKey());
            Object valueRaw = entry.getValue();
            Object value;
            if(valueRaw instanceof JSON) {
                value = ((JSON) valueRaw).marshal();
            } else if(valueRaw instanceof JSONArray) {
                value = ((JSONArray) valueRaw).marshal();
            } else {
                value = util.toString(valueRaw);
            }
            formatter.objectValue(value);
            if(iterator.hasNext()) {
                formatter.comma();
            }
        }
        formatter.endObject();
        return formatter.result();
    }

    /**
     * Marshals the JSON object to the specified object.
     * This requires the object to have an open and public constructor.
     * The fields of the object will then be to the corresponding value (e.g. String name -> gets the String with the "name" key).
     * If the object corresponding to the field is a JSON object then it will attempt to marshal the field.
     * @param clazz The class of T to use when marshalling.
     * @param ignoredKeys A vararg of ignored JSON keys. These will not be mapped.
     * @param <T> The type (object) to marshal to.
     * @return A marshaled object.
     * @throws IllegalArgumentException If the class is null or has no empty constructor.
     */
    public final <T> T marshal(Class<? extends T> clazz, String... ignoredKeys)
            throws IllegalArgumentException {
        if(clazz == null) {
            throw new IllegalArgumentException("Provided class is null");
        }
        Constructor<?> constructor = null;
        for(Constructor<?> tConstructor : clazz.getConstructors()) {
            if(tConstructor.getParameterCount() == 0) {
                constructor = tConstructor;
            }
        }
        if(constructor == null) {
            throw new IllegalArgumentException("Provided class has no empty constructor");
        }
        T t;
        try {
            //noinspection unchecked
            t = (T) constructor.newInstance();
        } catch(InvocationTargetException | InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException("An internal error occurred, could not instantiate " + clazz);
        }
        boolean classBasedAnnotation = clazz.isAnnotationPresent(JSONContainer.class);
        keyIteration:
        for(Map.Entry<String, Object> entry : rawContent.entrySet()) {
            String key = entry.getKey();
            for(String ignored : ignoredKeys) {
                if(entry.getKey().equals(ignored)) {
                    continue keyIteration;
                }
            }
            Field field = null;
            for(Field classField : t.getClass().getDeclaredFields()) {
                if(classBasedAnnotation) {
                    if(classField.getName().equals(key)) {
                        field = classField;
                        break;
                    }
                } else {
                    if(classField.isAnnotationPresent(JSONField.class)
                            && classField.getAnnotation(JSONField.class).jsonKey().equals(key)) {
                        field = classField;
                        break;
                    }
                }
            }
            if(field == null) {
                continue;
            }
            field.setAccessible(true);
            Object rawValue = entry.getValue();
            Object value;
            if(rawValue instanceof JSON) {
                value = ((JSON) rawValue).marshal(field.getType());
            } else if(rawValue instanceof JSONArray) {
                value = ((JSONArray) rawValue).toArray();
            } else {
                value = rawValue;
            }
            try {
                field.set(t, value);
            } catch(Exception ignored) {}
        }
        return t;
    }

    /**
     * Converts the JSON object to a string.
     * This method invokes the {@link #marshal()} method.
     * @return A string representation of the JSON object.
     */
    @Override
    public final String toString() {
        return marshal();
    }

}
