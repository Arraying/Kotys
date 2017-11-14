package de.arraying.kotys;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
public class JSONArray {

    private final List<Object> rawContent = new LinkedList<>();
    private final JSONUtil util = new JSONUtil();

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
        JSONTokenizer.Token[] tokens = new JSONTokenizer(rawJSONArray).getTokens();
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
     * Appends a value to the array.
     * This entry can either be a raw JSON data type, or a custom object.
     * For more information on how custom object parsing works, see {@link de.arraying.kotys.JSON(Object)}
     * @param values The values to append.
     */
    public void append(Object... values)
            throws IllegalArgumentException {
        for(Object object : values) {
            rawContent.add(util.getFinalValue(object));
        }
    }

    /**
     * Deletes the element at the specified index.
     * @param index The index.
     * @return The object that was removed from the Array.
     */
    public Object delete(int index) {
        return rawContent.remove(index);
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
        JSONFormatter formatter = new JSONFormatter()
                .startArray();
        for(int i = 0; i < length(); i++) {
                Object valueRaw = rawContent.get(i);
                Object value;
                if(valueRaw instanceof JSON) {
                    value = ((JSON) valueRaw).marshal();
                } else if(valueRaw instanceof JSONArray) {
                    value = ((JSONArray) valueRaw).marshal();
                } else {
                    value = util.toString(valueRaw);
                }
                formatter.array(value);
            if(i + 1 < length()) {
                formatter.comma();
            }
        }
        formatter.endArray();
        return formatter.result();
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

}
