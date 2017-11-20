package de.arraying.kotys;

import java.util.HashSet;
import java.util.Set;

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
class JSONStorage {

    private static JSONStorage instance;
    private static final Object mutex = new Object();
    private final Set<Class<?>> classes = new HashSet<>();

    /**
     * Gets the storage singleton instance.
     * @return The instance.
     */
    static JSONStorage getInstance() {
        if(instance == null) {
            synchronized(mutex) {
                if(instance == null) {
                    instance = new JSONStorage();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a class.
     * @param clazz The class. Not null.
     */
    synchronized void register(Class<?> clazz) {
        classes.add(clazz);
    }

    /**
     * Whether or not the class is registered.
     * @param clazz The class.
     * @return True if it is, false otherwise.
     */
    synchronized boolean has(Class<?> clazz) {
        return classes.contains(clazz);
    }

}
