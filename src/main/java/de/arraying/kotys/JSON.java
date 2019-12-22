package de.arraying.kotys;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class JSON {

    private static final JSONUtil util = new JSONUtil();
    private final Object writeLock = new Object();
    private Map<String, Object> rawContent = new HashMap<>();
    private JSONFormatter formatter = new JSONFormatter.DefaultImplementation();

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
        JSONTokenizer.Token[] tokens = new JSONTokenizer(new StringReader(rawJSON)).getTokens();
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
            final String key = (String) entry.getKey();
            rawContent.put(key, util.getFinalValue(entry.getValue()));
        }
    }

    /**
     * Creates a new JSON object from a container.
     * The JSON key will correspond to the field name, unless the field is annotated with {@link de.arraying.kotys.JSONField}.
     * All selected fields will then be attempted to be converted into JSON.
     * If a field is not a valid JSON datatype the object will then be taken, and this method will be called recursively.
     * @param container A container.
     * @param ignoredFields An array of ignored field names that will not get converted, regardless of annotation.
     * @throws IllegalArgumentException If the provided container is null, or not a container.
     */
    public JSON(Object container, String... ignoredFields)
            throws IllegalArgumentException {
        if(container == null) {
            throw new IllegalArgumentException("Provided container is null");
        }
        rawContent.putAll(new JSONORM<>(container).getValues(ignoredFields));
    }

    /**
     * Parses the contents of the specified file, and then calls {@link #JSON(String)} with it.
     * If the file is null or does not exist, an empty object will be created.
     * @param file The file.
     * @throws IOException If there was an error reading the file.
     * @throws IllegalStateException If the provided JSON string is invalid.
     */
    public JSON(File file)
            throws IOException, IllegalStateException {
        this(util.getFileContent(file, true));
    }

    /**
     * Specifies the map type to use for internal storage.
     * This is so that when marshalling some specific ordering is retained.
     * This map will get cleared.
     * Note that a {@link java.util.concurrent.ConcurrentHashMap} is not allowed.
     * @param map The map.
     */
    public void use(Map<String, Object> map) {
        if(map == null) {
            throw new IllegalArgumentException("Provided map is null");
        }
        if(map instanceof ConcurrentHashMap) {
            throw new IllegalArgumentException("ConcurrentHashMap not allowed");
        }
        Map<String, Object> cache = new HashMap<>(rawContent);
        map.clear();
        synchronized(writeLock) {
            rawContent = map;
            rawContent.putAll(cache);
        }
    }

    /**
     * Specifies the formatter to use when marshalling to a string.
     * @param formatter A formatter instance.
     */
    public void use(JSONFormatter formatter) {
        if(formatter == null) {
            throw new IllegalArgumentException("Formatter is null");
        }
        this.formatter = formatter;
    }

    /**
     * Adds an entry to the current JSON object.
     * This entry can either be a raw JSON data type, or a custom object.
     * For more information on how custom object parsing works, see {@link #JSON(Object, String...)} )}
     * @param key The key.
     * @param entry The entry.
     * @return The current JSON object, for chaining purposes.
     * @throws IllegalArgumentException If the key is null.
     */
    public JSON put(String key, Object entry)
            throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        synchronized(writeLock) {
            rawContent.put(key, util.getFinalValue(entry));
        }
        return this;
    }

    /**
     * Removes an entry from the JSON object.
     * @param key The key.
     * @return The current JSON object, for chaining purposes.
     * @throws IllegalArgumentException If the key is null.
     */
    public JSON remove(String key)
            throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("Provided key is null");
        }
        synchronized(writeLock) {
            rawContent.remove(key);
        }
        return this;
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
        return (String) object(key);
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
        return (Integer) object(key);
    }

    /**
     * Gets a Long object from all entries.
     * @param key The key.
     * @return The entry as a Long, or null if the specified value does not exist or the value is null.
     * @throws IllegalArgumentException If the key is null.
     * @throws ClassCastException If the entry is not a Long.
     */
    public Long large(String key)
            throws IllegalArgumentException, ClassCastException {
        return (Long) object(key);
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
        return (Double) object(key);
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
        return (Boolean) object(key);
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
        return (JSON) object(key);
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
        return (JSONArray) object(key);
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
        formatter.startObject();
        Iterator<Map.Entry<String, Object>> iterator = rawContent.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            formatter.objectKey(entry.getKey());
            Object valueRaw = entry.getValue();
            util.format(formatter, valueRaw);
            if(iterator.hasNext()) {
                formatter.comma();
            }
        }
        formatter.endObject();
        return formatter.result();
    }

    /**
     * Marshals (maps) the JSON object to the specified object.
     * An instance of this object will be created.
     * The JSON entry will be mapped to a field with the same name as the key, unless the field is anntoated with {@link de.arraying.kotys.JSONField}.
     * If the field is annotated with {@link de.arraying.kotys.JSONField}, then the field will receive the value of the JSON key specified in the annotation.
     * In the case that the JSON object does not contain the key specified in the annotation, the field will be initialized as null instead.
     * When using arrays, then the array in T MUST be non primitive.
     * The data type of an array is always the type the field uses.
     * If elements in the JSON array are not the same type as the array, then null will be used.
     * If a field is not a JSON datatype, then a JSON sub object will be fetched from the current JSON object, and this object will be marshaled recursively.
     * Multidimensional arrays are not supported, i.e. a JSONArray in a JSONArray is not permitted.
     * @param clazz The class of T to use when marshalling.
     * @param ignoredKeys A vararg of ignored JSON keys. These will not be marshaled, regardless of annotation.
     * @param <T> The type (object) to marshal to.
     * @return A marshaled object.
     * @throws IllegalArgumentException If the class is null, has no empty constructor or arrays are not primitive.
     * @throws IllegalStateException If the class could not be instantiated.
     * @throws UnsupportedOperationException If a field is a multidimensional array.
     */
    public final <T> T marshal(Class<T> clazz, String... ignoredKeys)
            throws IllegalArgumentException {
        if(clazz == null) {
            throw new IllegalArgumentException("Provided class is null");
        }
        return new JSONORM<>(clazz).mapTo(this, ignoredKeys);
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
