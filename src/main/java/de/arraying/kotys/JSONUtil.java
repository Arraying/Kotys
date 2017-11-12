package de.arraying.kotys;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
final class JSONUtil {

    private final String[] parsableMethodPrefixes = {"is", "get"};

    /**
     * Gets the object as a JSON string.
     * @param object The object.
     * @return A string version of the object.
     */
    String toString(Object object) {
        String string = object.toString();
        if(object instanceof String) {
            string = string.replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\f", "\\f")
                    .replace("\'", "\\'")
                    .replace("\"", "\\\"");
            return "\"" + string + "\"";
        }
        return string;
    }

    /**
     * Gets the final value from an object.
     * @param value The raw object.
     * @return A final value.
     */
    Object getFinalValue(Object value) {
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
            return getFromObject(value);
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
                || object instanceof Double
                || object instanceof Float
                || object instanceof Boolean;
    }

    /**
     * Gets a JSON object from a specified Java object.
     * @param object The object.
     * @return A JSON object.
     */
    private JSON getFromObject(Object object) {
        Map<String, Object> local = new ConcurrentHashMap<>();
        for(Method method : object.getClass().getMethods()) {
            if(method.getParameterCount() != 0
                    || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            String methodName = method.getName();
            if(methodName.equals("getClass")) {
                continue;
            }
            String key = null;
            for(String prefix : parsableMethodPrefixes) {
                if(methodName.startsWith(prefix)) {
                    key = methodName.substring(prefix.length()).toLowerCase();
                    break;
                }
            }
            if(key == null
                    || key.isEmpty()) {
                continue;
            }
            try {
                local.put(key, getFinalValue(method.invoke(object)));
            } catch(IllegalAccessException | InvocationTargetException ignored) {}
        }
        return new JSON(local);
    }

}
