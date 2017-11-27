package de.arraying.kotys;

import java.util.ArrayList;
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
final class JSONUtil {

    /**
     * Gets the object as a JSON string.
     * @param object The object.
     * @return A string version of the object.
     */
    String toString(Object object) {
        return object == null ? "null" : object.toString();
    }

    /**
     * Gets the final value from an object.
     * @param value The raw object.
     * @param ignoredFields All ignored fields.
     * @return A final value.
     */
    Object getFinalValue(Object value, String... ignoredFields) {
        if(isValidJSONDataType(value)) {
            return value;
        } else if(value.getClass().isArray()) {
            List<Object> objects = new ArrayList<>();
            for(Object entry : (Object[]) value) {
                objects.add(getFinalValue(entry));
            }
            return new JSONArray(objects.toArray());
        } else if(!(value instanceof JSON)
                && !(value instanceof JSONArray)){
            return new JSON(new JSONORM<>(value).getValues(ignoredFields));
        } else {
            return value;
        }
    }


    /**
     * Checks if the specified object is a valid JSON data type.
     * This presumes that all arrays have already been parsed.
     * @param object The object.
     * @return True if it is, false otherwise.
     */
    private boolean isValidJSONDataType(Object object) {
        return object == null
                || object instanceof String
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Double
                || object instanceof Float
                || object instanceof Boolean;
    }

}
