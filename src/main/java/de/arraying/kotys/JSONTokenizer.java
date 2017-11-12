package de.arraying.kotys;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
final class JSONTokenizer {

    /**
     * All the types of tokens.
     */
    enum Type {

        /**
         * The regular expression for an opening object.
         */
        OBJECT_OPEN(Pattern.compile("^\\{")),

        /**
         * The regular expression for a closing object.
         */
        OBJECT_CLOSE(Pattern.compile("^}")),

        /**
         * The regular expression for an opening array.
         */
        ARRAY_OPEN(Pattern.compile("^\\[")),

        /**
         * The regular expression for a closing array.
         */
        ARRAY_CLOSE(Pattern.compile("^]")),

        /**
         * The regular expression for a comma.
         */
        CHAR_COMMA(Pattern.compile("^,")),

        /**
         * The regular expression for a colon.
         */
        CHAR_COLON(Pattern.compile("^:")),

        /**
         * The regular expression for null.
         */
        TYPE_NULL(Pattern.compile("^null")),

        /**
         * The regular expression for a boolean.
         */
        TYPE_BOOL(Pattern.compile("^(true|false)")),

        /**
         * The regular expression for a double.
         */
        TYPE_DOUBLE(Pattern.compile("^-?[0-9]*\\.[0-9]+")),

        /**
         * The regular expression for an integer.
         */
        TYPE_INTEGER(Pattern.compile("^-?[0-9]+")),

        /**
         * The regular expression for a string.
         */
        TYPE_STRING(Pattern.compile("^\"((?:\\\\\"|[^\"])*)\""));

        private final Pattern pattern;

        /**
         * Sets the pattern.
         * @param pattern The pattern.
         */
        Type(Pattern pattern) {
            this.pattern = pattern;
        }

    }

    /**
     * The token class.
     */
    class Token {

        private final String token;
        private final Type type;

        /**
         * Creates a new token.
         * @param token The token, as a string.
         * @param type The type of token.
         */
        private Token(String token, Type type) {
            this.token = token;
            this.type = type;
        }

        /**
         * Gets the token.
         * @return The token.
         */
        String getToken() {
            return token;
        }

        /**
         * Gets the token type.
         * @return The type.
         */
        Type getType() {
            return type;
        }

    }

    private String input;
    private Token[] cached;

    /**
     * Creates a new JSON tokenizer.
     * @param input The input.
     */
    JSONTokenizer(String input) {
        this.input = input.trim();
    }

    /**
     * Gets all tokens.
     * @return An array of tokens.
     * @throws IllegalStateException If an unknown entry was found.
     */
    Token[] getTokens()
            throws IllegalStateException {
        if(cached != null) {
            return cached;
        }
        LinkedList<Token> tokens = new LinkedList<>();
        whileLoop:
        while(!input.isEmpty()) {
            input = input.trim();
            for(Type type : Type.values()) {
                Matcher matcher = type.pattern.matcher(input);
                if(!matcher.find()) {
                    continue;
                }
                String tokenString = matcher.group().trim();
                tokens.add(new Token(tokenString, type));
                input = matcher.replaceFirst("");
                continue whileLoop;
            }
            throw new IllegalStateException("Unknown token \"" + input.replace("\"", "\\\"") + "\"");
        }
        cached = tokens.toArray(new Token[tokens.size()]);
        return cached;
    }

}
