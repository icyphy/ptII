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
/**
UnitTerm is a construct that represents a term in a Unit Expression.
UnitTerms are construct as a result of parsing a Unit Expression. There are
three types of UnitTerms; 1) a Unit, 2) a variable, or 3) a Unit Expression.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitTerm implements UnitPresentation {
    /**
     * Construct a UnitTerm
     *
     */
    public UnitTerm() {
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////// public methods //////////////////////////
    /**
     * Create a String that is readable by a human. Said String can also be to
     * yield a UnitExpr that contains a UnitExpr equal to this one.
     *
     * @see ptolemy.data.unit.UnitPresentation#commonDesc()
     */
    public String commonDesc() {
        String retv = null;
        switch (getType()) {
            case VARIABLE :
                {
                    retv = "$" + _variable;
                    break;
                }
            case UNIT :
                {
                    retv = _unit.commonDesc();
                    break;
                }
            case UNITEXPR :
                {
                    retv = "(" + _unitExpr.commonDesc() + ")";
                    break;
                }
        }
        if (getExponent() != 1) {
            retv += "^" + getExponent();
        }
        return retv;
    }

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
        retv.setType(getType());
        return retv;
    }

    public Unit eval(Bindings bindings) {
        switch (_type) {
            case UNIT :
                {
                    return _unit;
                }
            case VARIABLE :
                {
                    if (bindings == null) {
                        return null;
                    }
                    return bindings.get(_variable);

                }
            case UnitTerm.UNITEXPR :
                {
                    return _unitExpr.eval(bindings);
                }
        }
        return null;
    }

    /**
     * @param bindings
     * @return The evaled expression.
     */
    public String getEvaledExpression(Bindings bindings) {
        if (isUnitExpr()) {
            return "(" + _unitExpr.getEvaledExpression(bindings) + ")";
        } else if (isVariable()) {
            Unit unit = bindings.get(_variable);
            if (unit != null) {
                return unit.toString();
            }
        } else if (isUnit()) {
            if (_unit != null) {
                return _unit.toString();
            }
        }
        return "null";
    }

    /**
     * @return The exponent.
     */
    public int getExponent() {
        return _exponent;
    }

    /**
     * @return The type.
     */
    public int getType() {
        return _type;
    }

    /**
     * @return The Unit.
     * @exception IllegalActionException If this is not a Unit.
     */
    public Unit getUnit() throws IllegalActionException {
        if (!isUnit()) {
            throw new IllegalActionException(this +" is not a Unit");
        }
        return _unit;
    }

    public UnitExpr getUnitExpr() throws IllegalActionException {
        if (!isUnitExpr()) {
            throw new IllegalActionException(this +" is not a UnitExpr");
        }
        return _unitExpr;
    }

    public String getVariable() throws IllegalActionException {
        if (!isVariable()) {
            throw new IllegalActionException(this +" is not a variable");
        }
        return _variable;
    }

    /** Invert this UnitTerm.
     * @return The inverse of this UnitTerm.
     */
    public UnitTerm invert() {
        UnitTerm retv = copy();
        switch (getType()) {
            case VARIABLE :
            case UNIT :
                {
                    retv.setExponent(-getExponent());
                    break;
                }
            case UNITEXPR :
                {
                    retv.setUnitExpr(_unitExpr.invert());
                    break;
                }
        }
        return retv;
    }

    public boolean isUnit() {
        return (_type == UNIT);
    }

    public boolean isUnitExpr() {
        return (_type == UNITEXPR);
    }

    public boolean isVariable() {
        return (_type == VARIABLE);
    }

    /**
     * @param multiplicand
     * @return The product of this Unit and the argument.
     * @exception IllegalActionException If either this UnitTerm or the
     * multiplicand is not a Unit.
     */
    public UnitTerm multiplyBy(UnitTerm multiplicand)
        throws IllegalActionException {
        if (!isUnit() || !multiplicand.isUnit()) {
            throw new IllegalActionException("Attempt to multiply non-Unit UnitTerm");
        }
        UnitTerm retv = new UnitTerm();
        retv.setUnit(getUnit().multiplyBy(multiplicand.getUnit()));
        return retv;
    }

    /**
     * @return The reduced Unit.
     */
    public UnitTerm reduce() {
        if (_exponent != 1 && isUnit()) {
            Unit unit = null;
            unit = _unit.copy();
            unit.setScale(Math.pow(unit.getScale(), _exponent));
            int exponents[] = unit.getType();
            for (int i = 0; i < exponents.length; i++) {
                exponents[i] *= _exponent;
            }
            unit.setType(exponents);
            UnitTerm retv = new UnitTerm();
            retv.setUnit(unit);
            return retv;
        }
        return this;
    }

    /**
     * @param exponent The exponent.
     */
    public void setExponent(int exponent) {
        _exponent = exponent;
    }

    /**
     * @param type The Unit type.
     */
    public void setType(int type) {
        _type = type;
    }

    /**
     * @param unit The Unit.
     */
    public void setUnit(Unit unit) {
        _type = UNIT;
        _unit = unit;
    }

    /**
     * @param expr The Unit Expression.
     */
    public void setUnitExpr(UnitExpr expr) {
        _type = UNITEXPR;
        _unitExpr = expr;
    }

    /**
     * @param v
     */
    public void setVariable(String v) {
        _type = VARIABLE;
        _variable = v;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String retv = null;
        switch (getType()) {
            case VARIABLE :
                {
                    retv = _variable;
                    break;
                }
            case UNIT :
                {
                    retv = _unit.toString();
                    break;
                }
            case UNITEXPR :
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

    /**
     * @param visitor
     * @return The result of visiting the UnitTerm.
     */
    public Object visit(EquationVisitor visitor)
        throws IllegalActionException {
        return visitor._visitUnitTerm(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private static final int UNIT = 1;
    private static final int UNITEXPR = 2;
    private static final int VARIABLE = 3;

    private int _exponent = 1;
    private int _type = -1;
    private Unit _unit = null;
    private UnitExpr _unitExpr = null;
    private String _variable = null;

}
