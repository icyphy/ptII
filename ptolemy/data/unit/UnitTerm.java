/* UnitTerm

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
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/
package ptolemy.data.unit;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// UnitTerm
/** UnitTerm represents a term in a Unit Expression.
A UnitTerm has 1) an exponent and 2) an element.
The element
can be either 1) a Unit, 2) a variable, or 3) a
Unit Expression. These choices for element are mutually exclusive.
UnitTerms are usually constructed as a result of parsing a Unit Expression.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitTerm implements UnitPresentation {

    /**
     * Construct a UnitTerm with no element.
     *
     */
    public UnitTerm() {
    }

    /** Construct an instance where the contained element is a Unit.
     * @param unit The Unit that will be the element of this instance.
     */
    public UnitTerm(Unit unit) {
        _type = _UNIT;
        _unit = unit;
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////// public methods //////////////////////////

    /** Make a shallow copy of this UnitTerm. That is, the underlying Unit or
     *  UnitExpr is not copied.
     * @return The copy of this UnitTerm
     */
    public UnitTerm copy() {
        UnitTerm retv = new UnitTerm();
        retv.setVariable(_variable);
        retv.setExponent(getExponent());
        retv.setUnit(_unit);
        retv.setUnitExpr(_unitExpr);
        retv._setType(_type);
        return retv;
    }

    /**
     * Create a String that is understandableable by a human.
     * @see ptolemy.data.unit.UnitPresentation#descriptiveForm()
     */
    public String descriptiveForm() {
        String retv = null;
        switch (_type) {
            case _VARIABLE :
                {
                    retv = "$" + _variable;
                    break;
                }
            case _UNIT :
                {
                    retv = _unit.descriptiveForm();
                    break;
                }
            case _UNITEXPR :
                {
                    retv = "(" + _unitExpr.descriptiveForm() + ")";
                    break;
                }
        }
        if (getExponent() != 1) {
            retv += "^" + getExponent();
        }
        return retv;
    }

    /**
     * @return The exponent.
     */
    public int getExponent() {
        return _exponent;
    }

    /** Get the element if it is a Unit.
     * @return The Unit if the element is a Unit, otherwise null.
     */
    public Unit getUnit() {
        if (_type == _UNIT)
            return _unit;
        return null;
    }

    /** Get the element if it is a UnitExpr.
     * @return The UnitExpr if the element is a UnitExpr, otherwise null.
     */
    public UnitExpr getUnitExpr() {
        if (_type == _UNITEXPR)
            return _unitExpr;
        return null;
    }

    /** Get the element if it is a variable.
     * @return The variable if the element is a variable, null otherwise.
     */
    public String getVariable() {
        if (_type == _VARIABLE)
            return _variable;
        return null;
    }

    /** Invert this UnitTerm.
     * @return The inverse of this UnitTerm.
     */
    public UnitTerm invert() {
        UnitTerm retv = copy();
        switch (_type) {
            case _VARIABLE :
            case _UNIT :
                {
                    retv.setExponent(-getExponent());
                    break;
                }
            case _UNITEXPR :
                {
                    retv.setUnitExpr(_unitExpr.invert());
                    break;
                }
        }
        return retv;
    }

    /** True if this is a Unit.
     * @return True if this is a Unit.
     */
    public boolean isUnit() {
        return (_type == _UNIT);
    }

    /** True is this is a UnitExpr.
                 * @return True is this is a UnitExpr.
                 */
    public boolean isUnitExpr() {
        return (_type == _UNITEXPR);
    }

    /** True if this a variable.
     * @return True if this a variable.
     */
    public boolean isVariable() {
        return (_type == _VARIABLE);
    }

    /** Set the exponent.
     * @param exponent The exponent.
     */
    public void setExponent(int exponent) {
        _exponent = exponent;
    }

    /** Set the element to be a Unit.
     * @param unit The Unit.
     */
    public void setUnit(Unit unit) {
        _type = _UNIT;
        _unit = unit;
    }

    /** Set the element to be a UnitExpr.
     * @param expr The Unit Expression.
     */
    public void setUnitExpr(UnitExpr expr) {
        _type = _UNITEXPR;
        _unitExpr = expr;
    }

    /** Set the element to be a variable
     * @param v The variable.
     */
    public void setVariable(String v) {
        _type = _VARIABLE;
        _variable = v;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String retv = null;
        switch (_type) {
            case _VARIABLE :
                {
                    retv = _variable;
                    break;
                }
            case _UNIT :
                {
                    retv = _unit.toString();
                    break;
                }
            case _UNITEXPR :
                {
                    retv = "(" + _unitExpr.toString() + ")";
                    break;
                }
        }
        if (getExponent() != 1) {
            retv += "^" + getExponent();
        }
        return retv;
    }

    /** Visit an instance of UnitTerm.
     * @param visitor The visitor.
     * @return The result of visiting the UnitTerm.
     */
    public Object visit(EquationVisitor visitor)
        throws IllegalActionException {
        return visitor._visitUnitTerm(this);
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////// protected methods //////////////////////////

    /**
     * @param type The Unit type.
     */
    protected void _setType(int type) {
        _type = type;
    } ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    private static final int _UNIT = 1;
    private static final int _UNITEXPR = 2;
    private static final int _VARIABLE = 3;
    private int _exponent = 1;
    private int _type = -1;
    private Unit _unit = null;
    private UnitExpr _unitExpr = null;
    private String _variable = null;
}
