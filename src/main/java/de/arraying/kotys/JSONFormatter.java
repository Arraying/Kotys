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
public interface JSONFormatter {

    /**
     * Starts a new JSON object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter startObject();

    /**
     * Ends the JSON object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter endObject();

    /**
     * Starts a new JSON array.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter startArray();

    /**
     * Ends the JSON array.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter endArray();

    /**
     * Appends a comma.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter comma();

    /**
     * Appends an object.
     * @param object The object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter object(String object);

    /**
     * Appends an array.
     * @param array The array.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter array(String array);

    /**
     * Appends an object key.
     * @param key The key of the object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter objectKey(String key);

    /**
     * Appends the value.
     * @param value The value of the object.
     * @return The formatter for chaining purposes.
     */
    JSONFormatter value(Object value);

    /**
     * Gets the result of the formatting.
     * @return The resultant string.
     */
    String result();

    /**
     * Default implementation, minimal JSON formatter.
     */
    final class DefaultImplementation implements JSONFormatter {

        private final StringBuilder builder = new StringBuilder();

        /**
         * Adds an opening brace.
         * @return This.
         */
        @Override
        public JSONFormatter startObject() {
            builder.append("{");
            return this;
        }

        /**
         * Adds a closing brace.
         * @return This.
         */
        @Override
        public JSONFormatter endObject() {
            builder.append("}");
            return this;
        }

        /**
         * Adds an opening bracket.
         * @return This.
         */
        @Override
        public JSONFormatter startArray() {
            builder.append("[");
            return this;
        }

        /**
         * Adds a closing bracket.
         * @return This.
         */
        @Override
        public JSONFormatter endArray() {
            builder.append("]");
            return this;
        }

        /**
         * Adds a comma.
         * @return This.
         */
        @Override
        public JSONFormatter comma() {
            builder.append(",");
            return this;
        }

        /**
         * Adds a JSON object.
         * @return This.
         */
        @Override
        public JSONFormatter object(String object) {
            builder.append(object);
            return this;
        }

        /**
         * Adds a JSON array.
         * @return This.
         */
        @Override
        public JSONFormatter array(String array) {
            builder.append(array);
            return this;
        }

        /**
         * Adds an object key, a string enclosed in quotation marks followed by a colon.
         * @return This.
         */
        @Override
        public JSONFormatter objectKey(String key) {
            builder.append("\"")
                    .append(key)
                    .append("\"")
                    .append(":");
            return this;
        }

        /**
         * Adds a numerical of textual value.
         * @return This.
         */
        @Override
        public JSONFormatter value(Object value) {
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
         * @return The resultant string.
         */
        @Override
        public String result() {
            return builder.toString();
        }

    }

}
