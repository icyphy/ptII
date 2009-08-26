/*
 * PropertyConstraintAttribute.java 53044 2009-04-10 21:47:49Z cxh $
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyAttribute;
import ptolemy.data.properties.lattice.exampleSetLattice.Lattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyConstraintAttribute extends PropertyAttribute {

    /**
     * 
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PropertyConstraintAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Set the expression. This method takes the descriptive form and determines
     * the internal form (by parsing the descriptive form) and stores it.
     * @param expression A String that is the descriptive form of either a Unit
     * or a UnitEquation.
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */

    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        if (expression.length() > 0) {
            String latticeName = getName().substring(
                    getName().indexOf("::") + 2);

            PropertyLattice lattice = PropertyLattice
                    .getPropertyLattice(latticeName);

            try {
                if (lattice instanceof PropertySetLattice) {
                    _property = _parseSetExpression(lattice, expression);
                } else {
                    _property = _parsePropertyExpression(lattice, expression);
                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot resolve the property expression: \""
                                + expression + "\"");
            }
        }
    }

    /**
     * Return the index of a specific character in the string starting from the
     * given index. It find the first occurence of the character that is not
     * embedded inside parentheses "()".
     * @param ch The character to search for.
     * @param string The given string to search from.
     * @param fromIndex The index to start the search.
     * @return The first occurence of the character in the string that is not
     * embedded in parentheses.
     */
    public static int _indexOf(String ch, String string, int fromIndex) {

        int parenIndex = fromIndex;
        int result = -1;
        int closedParenIndex = parenIndex;

        do {
            result = string.indexOf(ch, closedParenIndex);

            parenIndex = string.indexOf('{', closedParenIndex);

            if (parenIndex >= 0) {
                try {
                    closedParenIndex = _findClosedBracket(string, parenIndex);
                } catch (IllegalActionException e) {
                    closedParenIndex = -1;
                }
            }
        } while (result > parenIndex && result < closedParenIndex);

        return result;
    }

    public static List<String> _parseList(String parameters) {
        List<String> result = new ArrayList<String>();
        int previousCommaIndex = 0;
        int commaIndex = _indexOf(",", parameters, 0);

        while (commaIndex >= 0) {
            String item = parameters.substring(previousCommaIndex, commaIndex)
                    .trim();

            result.add(item);

            previousCommaIndex = commaIndex + 1;
            commaIndex = _indexOf(",", parameters, previousCommaIndex);
        }

        String item = parameters.substring(previousCommaIndex,
                parameters.length()).trim();

        if (item.trim().length() > 0) {
            result.add(item);
        }

        return result;
    }

    public static void main(String[] args) throws IllegalActionException {
        Lattice lattice = new Lattice();

        System.out.println(_parseSetExpression(lattice, "{A, B, C}"));
        System.out.println(_parseSetExpression(lattice, "{A, B, }C"));
        System.out.println(_parseSetExpression(lattice, "A{, B, C}"));
        System.out.println(_parseSetExpression(lattice, "A{, B}, C"));
        System.out.println(_parseSetExpression(lattice, "A, {B}, C"));
    }

    /**
     * Find the paired close parenthesis given a string and an index which is
     * the position of an open parenthesis. Return -1 if no paired close
     * parenthesis is found.
     * @param string The given string.
     * @param pos The given index.
     * @return The index which indicates the position of the paired close
     * parenthesis of the string.
     * @exception IllegalActionException If the character at the given position
     * of the string is not an open parenthesis or if the index is less than 0
     * or past the end of the string.
     */
    private static int _findClosedBracket(String string, int pos)
            throws IllegalActionException {
        if (pos < 0 || pos >= string.length()) {
            throw new IllegalActionException("The character index " + pos
                    + " is past the end of string \"" + string
                    + "\", which has a length of " + string.length() + ".");
        }

        if (string.charAt(pos) != '(') {
            throw new IllegalActionException("The character at index " + pos
                    + " of string: " + string + " is not a open parenthesis.");
        }

        int nextOpenParen = string.indexOf("(", pos + 1);

        if (nextOpenParen < 0) {
            nextOpenParen = string.length();
        }

        int nextCloseParen = string.indexOf(")", pos);

        if (nextCloseParen < 0) {
            return -1;
        }

        int count = 1;
        int beginIndex = pos + 1;

        while (beginIndex > 0) {
            if (nextCloseParen < nextOpenParen) {
                count--;

                if (count == 0) {
                    return nextCloseParen;
                }

                beginIndex = nextCloseParen + 1;
                nextCloseParen = string.indexOf(")", beginIndex);

                if (nextCloseParen < 0) {
                    return -1;
                }
            }

            if (nextOpenParen < nextCloseParen) {
                count++;
                beginIndex = nextOpenParen + 1;
                nextOpenParen = string.indexOf("(", beginIndex);

                if (nextOpenParen < 0) {
                    nextOpenParen = string.length();
                }
            }
        }

        return -1;
    }

    /**
     * Return a property in the specified lattice that is identified by the
     * given expression. Return null if the expression is case-insensitively
     * equivalent to "nil".
     * @param lattice The specified lattice.
     * @param expression The expression string.
     * @return A property in the specified lattice, or null if the expression is
     * equal "nil".
     * @exception IllegalActionException If the lattice does not contain such
     * element and the expression is not "nil".
     * 
     */
    private static LatticeProperty _parseElementExpression(
            PropertyLattice lattice, String expression)
            throws IllegalActionException {
        expression = expression.trim();
        String fieldName = expression.toUpperCase();

        if (!fieldName.equalsIgnoreCase("NIL")) {
            return lattice.getElement(fieldName);
        }
        return null;
    }

    /**
     * Parse the given expression for an arbitrary Property.
     * @param lattice The lattice.
     * @param expression The expression
     * @return The LatticeProperty that corresponds with
     * the lattice and expression.
     * @exception IllegalActionException If thrown by
     * {@link #_parseElementExpression(PropertyLattice, String)}
     */
    private static LatticeProperty _parsePropertyExpression(
            PropertyLattice lattice, String expression)
            throws IllegalActionException {

        if (expression.startsWith("{") && expression.endsWith("}")) {
            // Parsing RecordProperty.
            List<String> fieldExpressions = _parseList(expression.substring(1,
                    expression.length() - 1));

            int size = fieldExpressions.size();
            String[] labels = new String[size];
            LatticeProperty[] fieldProperties = new LatticeProperty[size];

            for (int i = 0; i < size; i++) {
                String fieldExpression = fieldExpressions.get(i);
                String[] labelAndProperty = fieldExpression.split("=", 2);

                labels[i] = labelAndProperty[0];
                fieldProperties[i] = _parsePropertyExpression(lattice,
                        labelAndProperty[1]);
            }
            return new RecordProperty(lattice, labels, fieldProperties);
        }
        return _parseElementExpression(lattice, expression);
    }

    private static PropertySet _parseSetExpression(PropertyLattice lattice,
            String setExpression) throws IllegalActionException {
        LinkedList result = new LinkedList();
        int start = 0;
        int openBrackets = 0;

        int i;

        setExpression = setExpression.trim();
        if (!setExpression.startsWith("{") && !setExpression.endsWith("}")) {
            result.add(_parseElementExpression(lattice, setExpression));
            return new PropertySet(lattice, result);
        }

        setExpression = setExpression.substring(1);

        for (i = 0; i < setExpression.length(); i++) {
            if (setExpression.charAt(i) == ',' && openBrackets == 0) {
                String element = setExpression.substring(start, i);
                if (element.trim().length() != 0) {
                    result.addAll(_parseSetExpression(lattice, element));
                    start = i + 1;
                } else {
                    throw new IllegalActionException(
                            "Cannot resolve the property expression: \""
                                    + element + "\"");
                }

            } else if (setExpression.charAt(i) == '{') {
                //start++;
                openBrackets++;
            } else if (setExpression.charAt(i) == '}') {
                openBrackets--;

                if (openBrackets == -1) {
                    String element = setExpression.substring(start, i);
                    if (element.trim().length() != 0) {
                        result.addAll(_parseSetExpression(lattice, element));
                        start = i + 1;
                    } else {
                        // Return the empty set.
                        if (result.isEmpty()) {
                            return new PropertySet(lattice, new Property[0]);
                        }
                        throw new IllegalActionException(
                                "Cannot resolve the property expression: \""
                                        + element + "\"");
                    }
                }
            }
        }

        //        String element = setExpression.substring(start, i - start);
        //        if (!element.trim().isEmpty()) {
        //        result.add(parseSetExpression(lattice, element));
        //        }
        return new PropertySet(lattice, result);
    }
}
