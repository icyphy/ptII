/* UnitAttribute used to specify either a Unit Expression or Unit Equations.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
import java.util.Vector;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////////
//// UnitAttribute
/**
This class is used to implement the Unit Attribute. A UnitsAttribute is either
a UnitExpr, or a vector of UnitEquations.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitAttribute extends Attribute implements Settable {
    /** Construct a UnitsAttribute with no specific name, or container.
     *  @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public UnitAttribute()
        throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a UnitsAttribute with the specified name, and container.
     * @param container Container
     * @param name Name
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public UnitAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
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

    /** True if this UnitAttribute are Unit Equations.
     * @return True if this UnitAttribute are Unit Equations.
     */
    public boolean areEquations() {
        return (_type == EQUATION);
    }

    /** Write a MoML description of the UnitsAttribute.
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
                + "<"
                + _elementName
                + " name=\"_units\" class=\""
                + getMoMLInfo().className
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</" + _elementName + ">\n");
    }

    /** Get the Common Description of the Unit Equations.
     * @return The Common Description of the UnitEquations.
     */
    public String getCommonDescOfUnitEquations() {
        String retv = "";
        if (_unitEquations.size() > 0) {
            retv = ((UnitEquation) (_unitEquations.elementAt(0))).commonDesc();
        }
        for (int i = 1; i < _unitEquations.size(); i++) {
            retv += ";"
                + ((UnitEquation) (_unitEquations.elementAt(i))).commonDesc();
        }
        return retv;
    }

    /** Get the expression for this attribute.
     * @return a String that represents the expression.
     * @see ptolemy.kernel.util.Settable#getExpression()
     */
    public String getExpression() {
        if (isExpression()) {
            return getUnitExpr().commonDesc();
        } else if (areEquations()) {
            return getCommonDescOfUnitEquations();
        }
        return null;
    }

    /** Get the UnitEquations.
     * @return The UnitEquations.
     */
    public Vector getUnitEquations() {
        return _unitEquations;
    }

    /** Get the Unit Expression.
     * @return The UnitExpr.
     */
    public UnitExpr getUnitExpr() {
        return _unitExpr;
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     * The visibility is set by default to NONE.
     * @return The visibility of this attribute.
     * @see ptolemy.kernel.util.Settable#getVisibility()
     */
    public Visibility getVisibility() {
        return _visibility;
    }

    /** True if this UnitsAttribute is a Unit Expression.
     * @return True if this UnitsAttribute is a UnitExpr.
     */
    public boolean isExpression() {
        return (_type == EXPRESSION);
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

    /** Set the expression.
     * @param expression
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */
    public void setExpression(String expression)
        throws IllegalActionException {
        try {
            if (getName().equals("_unitConstraints")) {
                Vector uEquations =
                    UnitLibrary.getParser().parseEquations(expression);
                setUnitEquations(uEquations);
            }
            if (getName().equals("_units")) {
                UnitExpr uExpr;
                uExpr = UnitLibrary.getParser().parseUnitExpr(expression);
                setUnitExpr(uExpr);
            }
        } catch (ParseException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Set the Unit Equations.
     * @param equations A Vector of UnitEquations.
     */
    public void setUnitEquations(Vector equations) {
        _unitEquations = equations;
        _type = EQUATION;
    }

    /** Set the Unit Expression.
     * @param expr A UnitExpr.
     */
    public void setUnitExpr(UnitExpr expr) {
        _unitExpr = expr;
        _type = EXPRESSION;
    }

    /** Set the visibility of this attribute.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this attribute.
     *  @see ptolemy.kernel.util.Settable#setVisibility(ptolemy.kernel.util.Settable.Visibility)
     */
    public void setVisibility(Visibility visibility) {
        _visibility = visibility;
    }

    /* Not really relevant to current capability.
     * But has to be included this class implements the Settable interface.
     * @see ptolemy.kernel.util.Settable#validate()
     */
    public void validate() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Visibility _visibility = Settable.NONE;
    // Listeners for changes in value.
    private List _valueListeners;
    private UnitExpr _unitExpr = null;
    private Vector _unitEquations = null;
    private int _type = -1;
    private static final int EXPRESSION = 0;
    private static final int EQUATION = 1;
}
