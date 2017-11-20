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
@SuppressWarnings({"unused", "WeakerAccess"})
public class JSONForger {

    /**
     * "Forges" the {@link de.arraying.kotys.JSONContainer} annotation, treating instances as if they have this annotation.
     * @param container The class of the container that should be forged.
     * @return The current JSONForger object, for chaining purposes.
     * @throws IllegalArgumentException If the container is null.
     */
    public JSONForger treatAsClassAnnotation(Class<?> container)
            throws IllegalArgumentException {
        if(container == null) {
            throw new IllegalArgumentException("Container is null");
        }
        JSONStorage.getInstance().register(container);
        return this;
    }

}
