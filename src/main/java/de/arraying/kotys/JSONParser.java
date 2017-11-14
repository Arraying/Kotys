package de.arraying.kotys;

import java.util.Arrays;

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
final class JSONParser {

    /**
     * A dual return consisting of the actual return object as well as the index to continue at.
     * @param <T> The return type.
     */
    private class BiReturn<T> {

        private T returned;
        private int index;

        private BiReturn(T returned, int index) {
            this.returned = returned;
            this.index = index;
        }

    }

    /**
     * Parses the JSON.
     * When first invoking this function (not recursively) the beginning { or [ and trailing ] or } should be omitted.
     * @param destination The destination where the parsed code goes.
     * @param tokens An array of tokens.
     */
    void parse(Object destination, JSONTokenizer.Token[] tokens) {
        boolean expectingComma = false;
        for(int i = 0; i < tokens.length; i++) {
            JSONTokenizer.Token token = tokens[i];
            if(expectingComma
                    && i != tokens.length - 1) {
                if(token.getType() == JSONTokenizer.Type.CHAR_COMMA) {
                    expectingComma = false;
                    continue;
                } else {
                    throw new IllegalStateException("Malformed JSON. Expecting comma, found " + token.getToken());
                }
            }
            if(destination instanceof JSONArray) {
                JSONArray array = (JSONArray) destination;
                if(isDataType(token)) {
                    array.append(asDataType(token));
                } else if(token.getType() == JSONTokenizer.Type.ARRAY_OPEN) {
                    BiReturn<JSONArray> result = parseArray(tokens, i);
                    array.append(result.returned);
                    i = result.index;
                } else if(token.getType() == JSONTokenizer.Type.OBJECT_OPEN) {
                    BiReturn<JSON> result = parseObject(tokens, i);
                    array.append(result.returned);
                    i = result.index;
                } else {
                    throw new IllegalStateException("Malformed JSON. Illegal token at current position " + token.getToken());
                }
                expectingComma = true;
            } else if(destination instanceof JSON) {
                JSON json = (JSON) destination;
                if(token.getType() != JSONTokenizer.Type.TYPE_STRING) {
                    throw new IllegalStateException("Malformed JSON. Expected string key, found " + token.getType());
                }
                String key = (String) asDataType(token);
                if(i + 1 >= tokens.length) {
                    throw new IllegalStateException("Malformed JSON. Expected colon, but found end of object");
                }
                if(i + 2 >= tokens.length) {
                    throw new IllegalStateException("Malformed JSON. Expected value, but found end of object");
                }
                JSONTokenizer.Token valueToken = tokens[i + 2];
                if(isDataType(valueToken)) {
                    json.put(key, asDataType(valueToken));
                    i += 2;
                } else if(valueToken.getType() == JSONTokenizer.Type.ARRAY_OPEN) {
                    BiReturn<JSONArray> result = parseArray(tokens, i + 2);
                    json.put(key, result.returned);
                    i = result.index;
                } else if(valueToken.getType() == JSONTokenizer.Type.OBJECT_OPEN) {
                    BiReturn<JSON> result = parseObject(tokens, i + 2);
                    json.put(key, result.returned);
                    i = result.index;
                } else {
                    throw new IllegalStateException("Malformed JSON. Illegal token at current position " + token.getToken());
                }
                expectingComma = true;
            } else {
                throw new IllegalStateException("Internal error. Found a non object and array entity to parse. If you see this please report it to the developer");
            }
        }
    }

    /**
     * Parses an array during tokenization.
     * @param tokens An array of tokens.
     * @param i The current iteration index.
     * @return A parsed array and the index.
     */
    private BiReturn<JSONArray> parseArray(JSONTokenizer.Token[] tokens, int i) {
        int next = nextBracket(i, tokens, true);
        if(i + 1 < tokens.length
                && next != -1) {
            JSONTokenizer.Token[] subArray = Arrays.copyOfRange(tokens, i + 1, next);
            JSONArray newArray = new JSONArray();
            parse(newArray, subArray);
            return new BiReturn<>(newArray, next);
        } else {
            throw new IllegalStateException("Malformed JSON. ] not found");
        }
    }

    /**
     * Parses an object during tokenization.
     * @param tokens An array of tokens.
     * @param i The current iteration index.
     * @return A parsed object and the index.
     */
    private BiReturn<JSON> parseObject(JSONTokenizer.Token[] tokens, int i) {
        int next = nextBracket(i, tokens, false);
        if(i + 1 < tokens.length
                && next != -1) {
            JSONTokenizer.Token[] subObject = Arrays.copyOfRange(tokens, i + 1, next);
            JSON newObject = new JSON();
            parse(newObject, subObject);
            return new BiReturn<>(newObject, next);
        } else {
            throw new IllegalStateException("Malformed JSON. } not found");
        }
    }

    /**
     * Whether or not the specified token is a valid data type.
     * @param token The token.
     * @return True if it is, false otherwise.
     */
    private boolean isDataType(JSONTokenizer.Token token) {
        return token.getType().name().startsWith("TYPE");
    }

    /**
     * Gets the specified token as a data type.
     * The token MUST be a valid data type. It is not checked.
     * Should the value exceed the minimal/maximal limit, then it will be set to its maximum value.
     * @param token The token.
     * @return A data type as an object.
     */
    private Object asDataType(JSONTokenizer.Token token) {
        JSONTokenizer.Type type = token.getType();
        if(!type.name().startsWith("TYPE")) {
            return null;
        }
        String value = token.getToken();
        switch(type) {
            case TYPE_NULL:
                return null;
            case TYPE_BOOL:
                return Boolean.valueOf(value);
            case TYPE_DOUBLE:
                return asDouble(value);
            case TYPE_INTEGER:
                return asNonDecimal(value);
            case TYPE_STRING:
                return value.substring(1, value.length()-1);
        }
        return null;
    }

    /**
     * Gets a string as a double.
     * @param value The string.
     * @return A double.
     */
    private Double asDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch(NumberFormatException exception) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Gets the value as non decimal.
     * @param value The value.
     * @return A long or integer depending on the size.
     */
    private Number asNonDecimal(String value) {
        try {
            long large = Long.valueOf(value);
            if(large <= Integer.MAX_VALUE
                    && large >= Integer.MIN_VALUE) {
                return (int) large;
            } else {
                return large;
            }
        } catch(NumberFormatException exception) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Gets the next closed brace/bracket.
     * @param current The current index concerning the token iteration during method processing.
     * @param tokens An array of tokens (sub array).
     * @param array True: handling with array, false: handling with object.
     * @return The index of the next bracket, or -1 if it does not exist.
     */
    private int nextBracket(int current, JSONTokenizer.Token[] tokens, boolean array) {
        int open = 0;
        int closed = 0;
        for(int i = current; i < tokens.length; i++) {
            JSONTokenizer.Token token = tokens[i];
            if(token.getToken().equals(array ? "[" : "{")) {
                open++;
            }
            if(token.getToken().equals(array ? "]" : "}")
                    && ++closed >= open) {
                return i;
            }
        }
        return -1;
    }

}
