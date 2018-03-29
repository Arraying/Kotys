package de.arraying.kotys;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Copyright 2018 Arraying
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
        OBJECT_OPEN,

        /**
         * The regular expression for a closing object.
         */
        OBJECT_CLOSE,

        /**
         * The regular expression for an opening array.
         */
        ARRAY_OPEN,

        /**
         * The regular expression for a closing array.
         */
        ARRAY_CLOSE,

        /**
         * The regular expression for a comma.
         */
        CHAR_COMMA,

        /**
         * The regular expression for a colon.
         */
        CHAR_COLON,

        /**
         * The regular expression for null.
         */
        TYPE_NULL,

        /**
         * The regular expression for a boolean.
         */
        TYPE_BOOL,

        /**
         * The regular expression for a double.
         */
        TYPE_DOUBLE,

        /**
         * The regular expression for an integer.
         */
        TYPE_INTEGER,

        /**
         * The regular expression for a string.
         */
        TYPE_STRING

    }

    /**
     * The token class.
     */
    class Token {

        private final Object token;
        private final Type type;

        /**
         * Creates a new token.
         * @param token The token, as an object.
         * @param type The type of token.
         */
        private Token(Object token, Type type) {
            this.token = token;
            this.type = type;
        }

        /**
         * Gets the token.
         * @return The token.
         */
        Object getToken() {
            return token;
        }

        /**
         * Gets the token type.
         * @return The type.
         */
        Type getType() {
            return type;
        }

        /**
         * Custom string representation of the token.
         * @return A string representation.
         */
        @Override
        public String toString() {
            return type.name() + ":" + token;
        }

    }

    private final Reader reader;
    private final ArrayList<Token> tokens = new ArrayList<>();
    private boolean eos;
    private char character;
    private Character pushBack;

    /**
     * Creates a new JSON tokenizer.
     * @param reader The reader to get data from.
     * @throws IllegalStateException If the input could not be tokenized.
     */
    JSONTokenizer(Reader reader)
            throws IllegalStateException {
        this.reader = reader;
        tokenize();
    }

    /**
     * Gets all the tokens.
     * @return A list of tokens.
     */
    Token[] getTokens() {
        return tokens.toArray(new Token[tokens.size()]);
    }

    /**
     * Begins the tokenization process.
     * @throws IllegalStateException If an error should occur.
     */
    private void tokenize()
            throws IllegalStateException {
        while(!eos) {
            read();
        }
    }

    /**
     * Reads an individual token.
     * @throws IllegalStateException If an unexpected character was found.
     */
    private void read()
            throws IllegalStateException {
        next();
        while(Character.isWhitespace(character)
                || character == '\t') {
            next();
        }
        if(eos) {
            return;
        }
        switch(character) {
            case '{':
                push(Type.OBJECT_OPEN);
                break;
            case '}':
                push(Type.OBJECT_CLOSE);
                break;
            case '[':
                push(Type.ARRAY_OPEN);
                break;
            case ']':
                push(Type.ARRAY_CLOSE);
                break;
            case ',':
                push(Type.CHAR_COMMA);
                break;
            case ':':
                push(Type.CHAR_COLON);
                break;
            case '"':
                readString();
                break;
            default:
                if(Character.isDigit(character)
                        || character == '-') {
                    readNumber();
                } else if(Character.isLetter(character)) {
                    readLiteral();
                } else {
                    throw new IllegalStateException("Unexpected character " + character);
                }
        }
    }

    /**
     * Gets the next character to tokenize.
     * @throws IllegalStateException If there was an internal error.
     */
    private void next()
            throws IllegalStateException {
        if(eos) {
            return;
        }
        if(pushBack != null) {
            character = pushBack;
            pushBack = null;
            return;
        }
        int out;
        try {
            out = reader.read();
        } catch(IOException exception) {
            throw new IllegalStateException(exception);
        }
        if(out <= 0) {
            eos = true;
            character = 0;
            return;
        }
        character = (char) out;
    }

    /**
     * Appends a token to the token list.
     * @param type The token type.
     */
    private void push(Type type) {
        push(String.valueOf(character), type);
    }

    /**
     * Appends a token to the token list.
     * @param value The value of the token.
     * @param type The token type.
     */
    private void push(Object value, Type type) {
        tokens.add(new Token(value, type));
    }

    /**
     * Reads a number, both with or without decimal.
     * @throws IllegalStateException If a second decimal was provided.
     */
    private void readNumber()
            throws IllegalStateException {
        boolean decimal = false;
        StringBuilder builder = new StringBuilder()
                .append(character);
        while(true) {
            next();
            if(eos) {
                break;
            }
            if(Character.isDigit(character)) {
                builder.append(character);
            } else if(character == '.') {
                if(decimal) {
                    throw new IllegalStateException("Found second decimal point in decimal");
                }
                builder.append(character);
                decimal = true;
            } else {
                pushBack = character;
                break;
            }
        }
        String number = builder.toString();
        if(number.endsWith(".")) {
            throw new IllegalStateException("Numbers cannot end with a decimal point");
        } else if(number.endsWith("-")) {
            throw new IllegalStateException("Numbers need a value after negative sign");
        }
        Object value;
        if(decimal) {
            value = Double.valueOf(number);
        } else {
            value = Long.valueOf(number);
        }
        push(value, decimal ? Type.TYPE_DOUBLE : Type.TYPE_INTEGER);
    }

    /**
     * Reads a literal, such as a boolean value or null.
     * @throws IllegalStateException If the literal provided is not one of the above.
     */
    private void readLiteral()
            throws IllegalStateException {
        StringBuilder builder = new StringBuilder()
                .append(character);
        while(true) {
            next();
            if(eos) {
                break;
            }
            if(Character.isLetter(character)) {
                builder.append(character);
            } else {
                pushBack = character;
                break;
            }
        }
        String literal = builder.toString();
        switch(literal) {
            case "null":
                push(null, Type.TYPE_NULL);
                break;
            case "true":
            case "false":
                push(Boolean.valueOf(literal), Type.TYPE_BOOL);
                break;
            default:
                throw new IllegalStateException("Invalid literal " + literal);
        }
    }

    /**
     * Parses a string.
     * @throws IllegalStateException If the string provided was illegal.
     */
    private void readString()
            throws IllegalStateException {
        StringBuilder builder = new StringBuilder();
        boolean ended = false;
        loop:while(true) {
            next();
            if(eos) {
                break;
            }
            switch(character) {
                case '\r':
                case '\n':
                    throw new IllegalStateException("String not terminated");
                case '\\':
                    next();
                    if(eos) {
                        break loop;
                    }
                    switch(character) {
                        case 'b':
                            builder.append('\b');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'f':
                            builder.append('\f');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 'u':
                            StringBuilder unicodeBuilder = new StringBuilder();
                            for(int i = 0; i < 4; i++) {
                                next();
                                if(eos) {
                                    break loop;
                                }
                                unicodeBuilder.append(character);
                            }
                            char value = (char) Integer.parseInt(unicodeBuilder.toString(), 16);
                            builder.append(value);
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            builder.append(character);
                            break;
                        default:
                            throw new IllegalStateException("Invalid escape character");
                    }
                    break;
                default:
                    if(character == '"') {
                        ended = true;
                        break loop;
                    }
                    builder.append(character);
            }
        }
        String string = builder.toString();
        if(!ended) {
            throw new IllegalStateException("String remained unterminated");
        }
        push(string, Type.TYPE_STRING);
    }

}
