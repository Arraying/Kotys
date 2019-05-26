package de.arraying.kotys;

/**
 * Copyright 2019 ipr0james
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
public class JSONDefaultMarshalFormat implements JSONMarshalFormat {

    @Override
    public String format(String json) {
        StringBuilder builder = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;

        for(char type : json.toCharArray()) {
            switch(type) {
                case '"':
                    // switch the quoting status
                    inQuote = !inQuote;
                    builder.append(type);
                    break;
                case ' ':
                    // For space: ignore the space if it is not being quoted.
                    if(inQuote) {
                        builder.append(type);
                    }
                    break;
                case '{':
                case '[':
                    // Starting a new block: increase the indent level
                    builder.append(type);
                    if(!inQuote) {
                        indentLevel++;
                        appendIndentedNewLine(indentLevel, builder);
                    }
                    break;
                case '}':
                case ']':
                    // Ending a new block; decrese the indent level
                    if(!inQuote) {
                        indentLevel--;
                        appendIndentedNewLine(indentLevel, builder);
                    }
                    builder.append(type);
                    break;
                case ',':
                    // Ending a json item; create a new line after
                    builder.append(type);
                    if(!inQuote) {
                        appendIndentedNewLine(indentLevel, builder);
                    }
                    break;
                case ':':
                    if(!inQuote) {
                        builder.append(": ");
                    }
                    break;
                default:
                    builder.append(type);
            }
        }
        return builder.toString();
    }

    /**
     * Print a new line with indention at the beginning of the new line.
     */
    private void appendIndentedNewLine(int indentLevel, StringBuilder builder) {
        builder.append("\n");
        for(int i = 0; i < indentLevel; i++) {
            builder.append("  ");
        }
    }
}
