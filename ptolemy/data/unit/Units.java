/* Units

 Copyright (c) 1998-2003 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_3
                                        COPYRIGHTENDKEY
@ProposedRating Red (rowland@eecs.berkeley.edu)
@AcceptedRating Red (rowland@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////////
//// Units
/**
This class is used to implement the Units Attribute.
<p>
The code herein is experimental. It implements a Units Attribute and is used in
such a way that there shouldn't be any impact on the rest of the system.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/

public class Units extends Attribute implements Settable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Units(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Add a listener to be notified when the value of this attribute changes.
    *  If the listener is already on the list of listeners, then do nothing.
    *  @param listener The listener to add.
    */
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }
        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /** Write a MoML description of the Units.
      *  @param output The output stream to write to.
      *  @param depth The depth in the hierarchy, to determine indenting.
      *  @param name The name to use instead of the current name.
      *  @exception IOException If an I/O error occurs.
      *  @see ptolemy.kernel.util.NamedObj#exportMoML(java.io.Writer, int, java.lang.String)
      */
    public void exportMoML(Writer output, int depth, String name)
        throws IOException {
        String value = getExpression();
        String valueTerm = "";
        if (value != null && !value.equals("")) {
            valueTerm =
                " value=\"" + StringUtilities.escapeForXML(value) + "\"";
        }

        output.write(
            _getIndentPrefix(depth)
                + "<property name=\"_units\" class=\""
                + getMoMLInfo().className
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(
            _getIndentPrefix(depth) + "</" + getMoMLInfo().elementName + ">\n");
    }

    /** Get the expression for this attribute.
     * @return a String that represents the expression.
     * @see ptolemy.kernel.util.Settable#getExpression()
     */
    public String getExpression() {
        String retv = "[";
        for (int i = 0; i < _unitCategoryExponents.length; i++) {
            if (i > 0)
                retv += ", ";
            retv += "" + _unitCategoryExponents[i];
        }
        return retv + "]";
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     * The visibility is set by default to FULL.
     * @return The visibility of this attribute.
     * @see ptolemy.kernel.util.Settable#getVisibility()
     */
    public Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that is
    *  notified when the value of this attribute changes.  If no such listener
    *  exists, do nothing.
    *  @param listener The listener to remove.
    */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Set the expression
     * @param expression
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */
    public void setExpression(String expression)
        throws IllegalActionException {

        if (expression.equals("")) {
            int numCategories = UnitUtilities.getNumCategories();
            for (int i = 0; i < numCategories; i++) {
                _unitCategoryExponents[i] = 0;
            }
        } else {

            Vector exponents = new Vector();
            int lastIndex = expression.length() - 1;
            if (expression.charAt(0) != '['
                || expression.charAt(lastIndex) != ']') {
                // TODO Figure out which exception to throw
                System.out.println("Units expression incorrect " + expression);
            }
            int categoryIndex = 0;
            StringTokenizer st =
                new StringTokenizer(expression.substring(1, lastIndex), " ,");
            while (st.hasMoreTokens()) {
                String token = (st.nextToken());
                _unitCategoryExponents[categoryIndex++] =
                    Integer.parseInt(token);
            }
        }
    }

    /** Set the visibility of this attribute.  The argument should be one
      *  of the public static instances in Settable.
      *  @param visibility The visibility of this attribute.
      *  @see ptolemy.kernel.util.Settable#setVisibility(ptolemy.kernel.util.Settable.Visibility)
      */
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /** Return the string representation of the units of this Units.
     *  This just invokes UnitUtilities.unitsString
     *  The general format of the returned string is
     *  "(l_1 * l_2 * ... * l_m) / (s_1 * s_2 * ... * s_n)".
     *  For example: "(meter * kilogram) / (second * second)".
     *  If m or n is 1, then the parenthesis above or below "/" is
     *  omited. For example: "meter / second".
     *  If there is no term above "/", the format becomes
     *  "1 / (s_1 * s_2 * ... * s_n)". For example: "1 / meter".
     *  @return A string representation of the units.
     *  @see ptolemy.data.unit.UnitUtilities
     */
    public String toString() {
        return UnitUtilities.unitsString(_unitCategoryExponents);
    }

    // Not really relevant to current capability.
    public void validate() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create a UnitsSystem by loading in the BasicUnits.xml
    static {
        MoMLParser momlParser = new MoMLParser();
        try {
            NamedObj container = new NamedObj();
            momlParser.setContext(container);
            momlParser.parseFile("ptolemy/data/unit/BasicUnits.xml");
            _unitSystem = (UnitSystem) (container.getAttribute("BasicUnits"));
        } catch (Exception e) {
            // TODO figure out which exception to throw
            System.out.println("Exception " + e);
        } catch (Throwable t) {
            // TODO figure out which exception to throw
            System.out.println("Throwable " + t);
        }
    }

    private int[] _unitCategoryExponents() {
        int numCategories = UnitUtilities.getNumCategories();
        int[] uce = new int[numCategories];
        for (int i = 0; i < numCategories; i++) {
            uce[i] = 0;
        }
        return uce;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**  The unit category exponents.
      *  This array records the exponents of the base categories.
      */
    private int[] _unitCategoryExponents = _unitCategoryExponents();

    Visibility _visibility = Settable.FULL;

    // Listeners for changes in value.
    private List _valueListeners;

    /** The static UnitSystem
     *
     */
    private static UnitSystem _unitSystem;
}
