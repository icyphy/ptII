/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
*/
package ptolemy.data.properties.lattice;

import java.util.LinkedList;

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


    /** Set the expression. This method takes the descriptive form and
     * determines the internal form (by parsing the descriptive form) and stores
     * it.
     * @param expression A String that is the descriptive form of either a Unit
     * or a UnitEquation.
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */

    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        if (expression.length() > 0) {
            String latticeName =
                getName().substring(getName().indexOf("::") + 2);

            PropertyLattice lattice =
                PropertyLattice.getPropertyLattice(latticeName);

            try {
                if (lattice instanceof PropertySetLattice) {
                    _property = _parseSetExpression(lattice, expression);
                } else {
                    _property = _parseElementExpression(lattice, expression);
                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot resolve the property expression: \""
                        + expression + "\"");
            }
        }
    }


    private static Property _parseElementExpression(PropertyLattice lattice,
            String expression) throws IllegalActionException {
        expression = expression.trim();
        String fieldName = expression.toUpperCase();

        if (!fieldName.equalsIgnoreCase("NIL")) {
            return lattice.getElement(fieldName);
        }
        return null;
    }


    private static PropertySet _parseSetExpression(PropertyLattice lattice, String setExpression) throws IllegalActionException {
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

    public static void main (String[] args) throws IllegalActionException {
        Lattice lattice = new Lattice();

        System.out.println(_parseSetExpression(lattice, "{A, B, C}"));
        System.out.println(_parseSetExpression(lattice, "{A, B, }C"));
        System.out.println(_parseSetExpression(lattice, "A{, B, C}"));
        System.out.println(_parseSetExpression(lattice, "A{, B}, C"));
        System.out.println(_parseSetExpression(lattice, "A, {B}, C"));
    }
}
