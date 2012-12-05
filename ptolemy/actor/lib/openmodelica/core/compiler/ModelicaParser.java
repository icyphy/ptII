/*
 * This file is part of Modelica Development Tooling.
 *
 * Copyright (c) 2005, Linköpings universitet, Department of
 * Computer and Information Science, PELAB
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 * http://www.opensource.org/licenses/bsd-license.php)
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Linköpings universitet nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.actor.lib.openmodelica.core.compiler;

import ptolemy.actor.lib.openmodelica.core.Element;
import ptolemy.actor.lib.openmodelica.core.List;
import ptolemy.actor.lib.openmodelica.core.ListElement;

/**
 * This class prvides some code to parse simple modelica primitives,
 * for now it can parse lists in modelica syntax.
 * 
 * @author Andreas Remar
 */
public class ModelicaParser {
    /**
     * This function parses Modelica lists, any nesting possible.
     * @param str the Modelica list to parse
     * @return a Vector containing Vector:s and String:s. The Vector:s contain
     * further Vector:s and String:s. Nesting and stuff.
     * @throws ModelicaParserException 
     */
    public static List parseList(String str) throws ModelicaParserException {
        List elements = new List();

        /* Remove whitespace before and after */
        str = str.trim();

        /* Make sure this string is not empty */
        if (str == "" || str.length() < 2) {
            throw new ModelicaParserException("Empty list: [" + str + "]");
        }

        if (str.startsWith("{ rec(")) {
            str = "{" + str.substring(6, str.length() - 3) + "}";
        }

        /* Make sure this is a list */
        if (str.charAt(0) != '{' || str.charAt(str.length() - 1) != '}') {
            throw new ModelicaParserException("Not a list: [" + str + "]");
        }
        /* Remove { and } */
        str = str.substring(1, str.length() - 1);

        str = str.trim();

        if (str.startsWith("rec(")) {
            str = str.substring(4, str.length() - 1);
        }

        if (str.trim().equals("")) {
            /* This is an empty list, so return an empty list! */
            return new List();
        }

        /*
         * { { hej, pÃ¥ } , dig } => [[hej,pÃ¥],dig]
         */

        /*
         * Go through the string character by character, looking for commas (,)
         * and start ({) and end (}) of lists. Take special note of " as they
         * start and end strings. Inside a string, there can be , { and }
         * characters and also escaped characters (for example \").
         */

        // TODO Rewrite this using a better way of acumulating the characters.
        // Right now, it uses string += otherString, which generates alot of
        // strings that later are thrown away. Slowness, the slowness!

        int depth = 0;
        boolean insideString = false;
        boolean listFound = false;
        String subString = "";
        int tupleDepth = 0;

        for (int characterPosition = 0; characterPosition < str.length(); characterPosition++) {
            if (str.charAt(characterPosition) == '\\' && insideString == true) {
                /* Read this \ and the escaped character */
                characterPosition++;

                if (characterPosition >= str.length()) {
                    /* This is some kind of error*/
                    throw new ModelicaParserException("String ends in \\: "
                            + str);
                }

                subString += "\\" + str.charAt(characterPosition);
            }

            else if (str.charAt(characterPosition) == '"') {
                /* If we're not inside a string, enter string mode*/
                if (insideString == false) {
                    insideString = true;
                }
                /* else exit it */
                else {
                    insideString = false;
                }

                subString += '"';
            } else if (str.charAt(characterPosition) == '<') {
                if (!insideString)
                    tupleDepth++;
                subString += '<';
            } else if (str.charAt(characterPosition) == '>') {
                if (!insideString)
                    tupleDepth--;
                subString += '>';
            } else if (str.charAt(characterPosition) == '{'
                    && insideString == false) {
                listFound = true;
                depth++;
                subString += '{';
            } else if (str.charAt(characterPosition) == ',' && depth == 0
                    && insideString == false && tupleDepth == 0) {
                /*
                 * If we're at depth 0, then we've found a list (or element)
                 * at the bottom level.
                 */
                ListElement element = null;

                if (listFound) {
                    try {
                        element = parseList(subString);
                    } catch (ModelicaParserException e) {
                        /* If there was an error, it might have been because
                         * subString isn't a list. It might be an element.
                         * But if it's not 'Not a list', just pass this on. */
                        if (e.getMessage().startsWith("Not a list: [") == false) {
                            throw e;
                        }
                    }
                    /*
                     * If subString really wasn't a list (it contains {} but
                     * still isn't a list), then just trim it and say it's an
                     * element.
                     */
                    if (element == null) {
                        element = new Element(subString.trim());
                    }
                } else {
                    element = new Element(subString.trim());
                }

                listFound = false;
                if (element instanceof Element
                        && ((Element) element).toString().equals("")) {
                    /* An empty string denotes an empty 
                     * list element, which is an error. 
                     */
                    throw new ModelicaParserException("Element is empty");
                }

                elements.append(element);
                subString = "";
            } else if (str.charAt(characterPosition) == '}'
                    && insideString == false) {
                depth--;
                subString += '}';

                /* Unmatched } */
                if (depth < 0) {
                    throw new ModelicaParserException("Unmatched }: [" + str
                            + "]");
                }
            } else {
                subString += str.charAt(characterPosition);
            }
        }

        /* This happens at the end of the list. */
        if (depth == 0) {
            ListElement element = null;
            if (listFound) {
                try {
                    element = parseList(subString);
                } catch (ModelicaParserException e) {
                    /* If there was an error, it might have been because
                     * subString isn't a list. It might be an element.
                     * But if it's not 'Not a list', just pass this on. */
                    if (e.getMessage().startsWith("Not a list: [") == false) {
                        throw e;
                    }
                }
                /*
                 * If subString really wasn't a list (it contains {} but
                 * still isn't a list), then just trim it and say it's an
                 * element.
                 */
                if (element == null) {
                    element = new Element(subString.trim());
                }
            } else {
                element = new Element(subString.trim());
            }

            if (element instanceof Element
                    && ((Element) element).toString().equals("")) {
                /* An empty string denotes an empty list element, which
                 * is an error. */
                throw new ModelicaParserException("Element is empty");
            } else {
                elements.append(element);
            }
        }

        if (insideString == true) {
            /* We should not be inside a string at the end of the list */
            throw new ModelicaParserException("Unterminated string: [" + str
                    + "]");
        }

        return elements;
    }
}
