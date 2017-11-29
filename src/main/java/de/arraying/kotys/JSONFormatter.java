package de.arraying.kotys;

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
@SuppressWarnings("UnusedReturnValue")
final class JSONFormatter {

    private final StringBuilder builder = new StringBuilder();
    private final JSONUtil util = new JSONUtil();

    /**
     * Starts a new JSON object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter startObject() {
        builder.append("{");
        return this;
    }

    /**
     * Ends the JSON object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter endObject() {
        builder.append("}");
        return this;
    }

    /**
     * Starts a new JSON array.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter startArray() {
        builder.append("[");
        return this;
    }

    /**
     * Ends the JSON array.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter endArray() {
        builder.append("]");
        return this;
    }

    /**
     * Appends a comma.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter comma() {
        builder.append(",");
        return this;
    }

    /**
     * Appends an array entry.
     * @param value The value.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter array(Object value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends an object key.
     * @param key The key of the object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter objectKey(String key) {
        builder.append("\"")
                .append(key)
                .append("\"")
                .append(":");
        return this;
    }

    /**
     * Appends the object value.
     * @param value The value of the object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter objectValue(Object value) {
        if(value instanceof String) {
            builder.append("\"")
                    .append(value)
                    .append("\"");
        } else {
            builder.append(value);
        }
        return this;
    }

    /**
     * Gets the result of the formatting.
     * @return The resultant string.
     */
    String result() {
        return builder.toString();
    }

}
