package de.arraying.kotys;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.*;

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
public class JSONArray implements Iterator<Object> {

    private static final JSONUtil util = new JSONUtil();
    private final Object writeLock = new Object();
    private final List<Object> rawContent = new ArrayList<>();
    private JSONFormatter formatter = new JSONFormatter.DefaultImplementation();

    /**
     * Creates an empty JSON array.
     */
    public JSONArray() {}

    /**
     * Creates a new JSON array from a string.
     * @param rawJSONArray A string representation of a JSON array.
     * @throws IllegalArgumentException If the provided string is null or empty.
     * @throws IllegalStateException If the provided JSON string is invalid.
     */
    public JSONArray(String rawJSONArray)
            throws IllegalArgumentException, IllegalStateException {
        if(rawJSONArray == null) {
            throw new IllegalArgumentException("Provided string is null");
        }
        if(rawJSONArray.isEmpty()) {
            throw new IllegalArgumentException("Provided string is empty");
        }
        JSONTokenizer.Token[] tokens = new JSONTokenizer(new StringReader(rawJSONArray)).getTokens();
        if(tokens.length < 2) {
            throw new IllegalStateException("Too little tokens");
        }
        if(tokens[0].getType() != JSONTokenizer.Type.ARRAY_OPEN
                || tokens[tokens.length-1].getType() != JSONTokenizer.Type.ARRAY_CLOSE) {
            throw new IllegalArgumentException("A JSON array must start with [ and end with ]");
        }
        if(tokens.length == 2) {
            return;
        }
        new JSONParser().parse(this, Arrays.copyOfRange(tokens, 1, tokens.length-1));
    }

    /**
     * Creates a new JSON array from an array of existing objects.
     * Please note that the array has to be a non primitive array.
     * For more information on how custom object parsing works, see {@link de.arraying.kotys.JSON(Object)}
     * @param objects An array of objects.
     * @throws IllegalArgumentException If the provided objects are null.
     */
    public JSONArray(Object[] objects)
            throws IllegalArgumentException {
        if(objects == null) {
            throw new IllegalArgumentException("Provided array is null");
        }
        for(Object object : objects) {
            rawContent.add(util.getFinalValue(object));
        }
    }

    /**
     * Parses the contents of the specified file, and then calls {@link #JSONArray(String)} with it.
     * If the file is null or does not exist, an empty object will be created.
     * @param file The file.
     * @throws IOException If there was an error reading the file.
     * @throws IllegalStateException If the provided JSON string is invalid.
     */
    public JSONArray(File file)
            throws IOException, IllegalStateException {
        this(util.getFileContent(file, false));
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
     * Appends a value to the array.
     * This entry can either be a raw JSON data type, or a custom object.
     * For more information on how custom object parsing works, see {@link de.arraying.kotys.JSON(Object)}
     * @param values The values to append.
     * @return The current JSONArray, for chaining purposes.
     */
    public JSONArray append(Object... values)
            throws IllegalArgumentException {
        synchronized(writeLock) {
            for(Object object : values) {
                rawContent.add(util.getFinalValue(object));
            }
        }
        return this;
    }

    /**
     * Deletes the element at the specified index.
     * @param index The index.
     * @return The object that was removed from the Array.
     */
    public Object delete(int index) {
        synchronized(writeLock) {
            return rawContent.remove(index);
        }
    }

    /**
     * Gets the Object at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as an Object, or null if the specified index is out of range or the value is null.
     * @throws IndexOutOfBoundsException If the provided index is out of range.
     * @throws ClassCastException If the entry is not an Object.
     */
    public Object object(int index)
            throws IndexOutOfBoundsException, ClassCastException {
        if(index < 0
                || index >= rawContent.size()) {
            return null;
        }
        return rawContent.get(index);
    }

    /**
     * Gets the String at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a String, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a String.
     */
    public String string(int index)
            throws ClassCastException {
        return (String) object(index);
    }

    /**
     * Gets the Integer at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as an Integer, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not an Integer.
     */
    public Integer integer(int index)
            throws ClassCastException {
        return (Integer) object(index);
    }

    /**
     * Gets the Long at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a Long, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a Long.
     */
    public Long large(int index)
            throws ClassCastException {
        return (Long) object(index);
    }

    /**
     * Gets the Double at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a Double, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a Double.
     */
    public Double decimal(int index)
            throws ClassCastException {
        return (Double) object(index);
    }

    /**
     * Gets the Boolean at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a Boolean, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a Boolean.
     */
    public Boolean bool(int index)
            throws ClassCastException {
        return (Boolean) object(index);
    }

    /**
     * Gets the JSON at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a JSON, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a JSON.
     */
    public JSON json(int index)
            throws ClassCastException {
        return (JSON) object(index);
    }

    /**
     * Gets the JSONArray at the specified index.
     * @param index The index, 0 based indexing.
     * @return The entry as a JSONArray, or null if the specified index is out of range or the value is null.
     * @throws ClassCastException If the entry is not a JSONArray.
     */
    public JSONArray array(int index)
            throws ClassCastException {
        return (JSONArray) object(index);
    }

    /**
     * Gets the raw implementation of the JSON array.
     * @return A list.
     */
    public final List<Object> raw() {
        return Collections.unmodifiableList(rawContent);
    }

    /**
     * Gets the length of the array.
     * @return The length.
     */
    public final int length() {
        return rawContent.size();
    }

    /**
     * Checks whether the array is empty.
     * @return True if it is, false otherwise.
     */
    public final boolean isEmpty() {
        return rawContent.isEmpty();
    }

    /**
     * Turns the JSON array into a Java array.
     * @return An array of objects.
     */
    public final Object[] toArray() {
        return rawContent.toArray();
    }

    /**
     * Marshals the JSON array into a string.
     * @return A string representation of the JSON array.
     */
    public final String marshal() {
        formatter.startArray();
        for(int i = 0; i < length(); i++) {
                Object valueRaw = rawContent.get(i);
                util.format(formatter, valueRaw);
            if(i + 1 < length()) {
                formatter.comma();
            }
        }
        formatter.endArray();
        return formatter.result();
    }

    /**
     * Marshals the JSON array to a JVM array.
     * If an element is not of type T, then it will be skipped.
     * @param clazz The class of T to use when marshaling.
     * @param <T> The type (object) to marshal to.
     * @return A marshaled array.
     */
    @SuppressWarnings("unchecked")
    public final <T> T[] marshal(Class<T> clazz) {
        List<Object> objects = new ArrayList<>();
        for(Object raw : rawContent) {
            if(raw instanceof JSON) {
                try {
                    objects.add(((JSON) raw).marshal(clazz));
                } catch(Exception ignored) {}
            } else {
                if(raw.getClass().equals(clazz)) {
                    objects.add(raw);
                }
            }
        }
        Object[] array = (Object[]) Array.newInstance(clazz, objects.size());
        for(int i = 0; i < array.length; i++) {
            array[i] = objects.get(i);
        }
        return (T[]) array;
    }

    /**
     * Converts the JSON array to a string.
     * This method invokes the {@link #marshal()} method.
     * @return A string representation of the JSON array.
     */
    @Override
    public final String toString() {
        return marshal();
    }

    /**
     * Whether or not the array has a next entry.
     * @return True if it does, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return rawContent.iterator().hasNext();
    }

    /**
     * Gets the next value.
     * @return The next value.
     */
    @Override
    public Object next() {
        return rawContent.iterator().next();
    }

    /**
     * Removes the value.
     */
    @Override
    public void remove() {
        rawContent.iterator().remove();
    }

}
