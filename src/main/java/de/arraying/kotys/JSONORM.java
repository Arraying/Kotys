package de.arraying.kotys;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
final class JSONORM<T> {

    /**
     * The field container containing useful information.
     */
    class FieldContainer {

        private final Field field;
        private final String jsonKey;

        /**
         * Creates a new field container.
         * @param field The field.
         * @param jsonKey The JSON key.
         *                If there is no annotation to specify, use the field name.
         */
        FieldContainer(Field field, String jsonKey) {
            this.field = field;
            this.jsonKey = jsonKey;
        }

    }

    private final T container;
    private final JSONUtil util = new JSONUtil();

    /**
     * Creates a new JSON ORM by using the object.
     * @param container The container that will be worked with.
     */
    JSONORM(T container) {
        this.container = container;
    }

    /**
     * Creates a new JSON ORM by using the class.
     * @param clazz The class of the container that will be worked with.
     * @throws IllegalArgumentException If the provided class has no empty constructor.
     * @throws IllegalStateException If the provided class could not be instantiated.
     */
    JSONORM(Class<T> clazz)
            throws IllegalArgumentException {
        Constructor<?> constructor = null;
        for(Constructor<?> tConstructor : clazz.getDeclaredConstructors()) {
            if(tConstructor.getParameterCount() == 0) {
                constructor = tConstructor;
            }
        }
        if(constructor == null) {
            throw new IllegalArgumentException("Provided class " + clazz + " has no empty constructor");
        }
        constructor.setAccessible(true);
        try {
            //noinspection unchecked
            container = (T) constructor.newInstance();
        } catch(InvocationTargetException | InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException("An internal error occurred, could not instantiate " + clazz);
        }
    }

    /**
     * Gets all values from the object.
     * @param ignoredFieldNames Fields that will be ignored.
     * @return An immutable map of keys and values.
     */
    Map<String, Object> getValues(String... ignoredFieldNames) {
        Map<String, Object> values = new HashMap<>();
        containerLoop:
        for(FieldContainer container : getAllFields()) {
            String key = container.jsonKey;
            for(String ignoredJSONKey : ignoredFieldNames) {
                if(ignoredJSONKey == null
                        || key.equals(ignoredJSONKey)) {
                    continue containerLoop;
                }
            }
            Field field = container.field;
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(this.container);
                Object value = util.getFinalValue(fieldValue);
                values.put(key, value);
            } catch(IllegalAccessException ignored) {}
        }
        return Collections.unmodifiableMap(values);
    }

    /**
     * Maps the specified JSON object to the
     * @param json The JSON object.
     * @param ignoredJSONKeys JSON keys that will be ignored.
     * @return A mapped object.
     * @throws UnsupportedOperationException If a field is a multidimensional array.
     */
    T mapTo(JSON json, String... ignoredJSONKeys)
            throws UnsupportedOperationException {
        containerLoop:
        for(FieldContainer container : getAllFields()) {
            String key = container.jsonKey;
            for(String ignoredJSONKey : ignoredJSONKeys) {
                if(ignoredJSONKey == null
                    || key.equals(ignoredJSONKey)) {
                    continue containerLoop;
                }
            }
            Field field = container.field;
            field.setAccessible(true);
            if(!json.has(key)) {
                continue;
            }
            Object rawValue = json.object(key);
            Object value;
            if(rawValue instanceof JSON) {
                value = ((JSON) rawValue).marshal(field.getType());
            } else if(rawValue instanceof JSONArray) {
                Class<?> fieldType = field.getType().getComponentType();
                if(fieldType.isPrimitive()) {
                    throw new IllegalArgumentException("Array type " + fieldType + " is primitive");
                }
                value = ((JSONArray) rawValue).marshal(fieldType);
            } else {
                value = rawValue;
            }
            try {
                field.set(this.container, value);
            } catch(IllegalAccessException ignored) {}
        }
        return container;
    }

    /**
     * Gets all fields eligible for JSON parsing.
     * @return An immutable list of field containers.
     */
    private List<FieldContainer> getAllFields() {
        List<FieldContainer> fieldContainers = new ArrayList<>();
        for(Field classField : container.getClass().getDeclaredFields()) {
            if(classField.isAnnotationPresent(JSONField.class)) {
                String key = classField.getAnnotation(JSONField.class).key();
                fieldContainers.add(new FieldContainer(classField, key));
            } else {
                fieldContainers.add(new FieldContainer(classField, classField.getName()));
            }
        }
        return Collections.unmodifiableList(fieldContainers);
    }

}
