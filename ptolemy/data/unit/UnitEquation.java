/*

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

import java.util.Vector;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// UnitEquation
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/

public class UnitEquation implements UnitPresentation {

    /**
     * @param lhs
     * @param op
     * @param rhs
     */
    public UnitEquation(UnitExpr lhs, String op, UnitExpr rhs) {
        _lhs = lhs;
        _rhs = rhs;
        _operator = op;
    }

    /**
     *
     */
    public UnitEquation() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * @param equations
     * @param bindings
     * @return
     */
    public static boolean areSatisfied(Vector equations, Bindings bindings) {
        for (int i = 0; i < equations.size(); i++) {
            if (((UnitEquation) (equations.elementAt(i))).isSatisfied(bindings)
                != true) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    public void canonicalize() {
        UnitExpr lhsUExpr = getLhs();
        UnitExpr rhsUExpr = getRhs();
        UnitExpr newLeftUExpr = new UnitExpr();
        UnitExpr newRightUExpr = new UnitExpr();
        Vector leftUTerms = lhsUExpr.getUTerms();

        for (int i = 0; i < leftUTerms.size(); i++) {
            UnitTerm uTerm = ((UnitTerm) (leftUTerms.elementAt(i)));
            if (uTerm.isUnit()) {
                newRightUExpr.add(uTerm.invert());
            } else if (uTerm.isVariable()) {
                newLeftUExpr.add(uTerm);
            }
        }
        Vector rightUTerms = rhsUExpr.getUTerms();
        for (int i = 0; i < rightUTerms.size(); i++) {
            UnitTerm uTerm = ((UnitTerm) (rightUTerms.elementAt(i)));
            if (uTerm.isUnit()) {
                newRightUExpr.add(uTerm);
            } else if (uTerm.isVariable()) {
                newLeftUExpr.add(uTerm.invert());
            }
        }
        if (newRightUExpr.getUTerms().isEmpty()) {
            UnitTerm x = new UnitTerm();
            x.setUnit(UnitLibrary.Identity);
            newRightUExpr.add(x);
        }

        newRightUExpr.reduce();
        setLhs(newLeftUExpr);
        setRhs(newRightUExpr);
    }

    /* (non-Javadoc)
     * @see ptolemy.data.unit.UnitPresentation#commonDesc()
     */
    public String commonDesc() {
        return _lhs.commonDesc() + _operator + _rhs.commonDesc();
    }

    /**
     * @return
     */
    public UnitEquation copy() {
        UnitEquation uC = new UnitEquation();
        uC.setLhs(getLhs().copy());
        uC.setOperator(getOperator());
        uC.setRhs(getRhs().copy());
        return uC;
    }

    /**
     * @param bindings
     * @return
     */
    public String getEvaledExpression(Bindings bindings) {
        return _lhs.getEvaledExpression(bindings)
            + _operator
            + _rhs.getEvaledExpression(bindings);
    }

    /**
     * @return
     */
    public UnitExpr getLhs() {
        return _lhs;
    }

    /**
     * @return
     */
    public String getOperator() {
        return _operator;
    }

    /**
     * @return
     */
    public UnitExpr getRhs() {
        return _rhs;
    }

    /**
     * @return
     */
    public NamedObj getSource() {
        return _source;
    }

    /**
     * @param modelBindings
     * @return
     */
    public String humanReadableForm(Bindings modelBindings) {
        String retv =
            commonDesc() + " satisfied = " + isSatisfied(modelBindings);
        return retv;
    }

    /**
     * @param bindings
     * @return
     */
    public boolean isSatisfied(Bindings bindings) {
        Boolean retv = null;
        Unit lhsUnit = getLhs().eval(bindings);
        Unit rhsUnit = getRhs().eval(bindings);

        if (lhsUnit == null || rhsUnit == null) {
            return false;
        }
        if (lhsUnit.hasSameType(rhsUnit)) {
            return true;
        } else {
            return false;
        }
    }

    public void setLhs(UnitExpr expr) {
        _lhs = expr;
    }

    public void setOperator(String string) {
        _operator = string;
    }

    public void setRhs(UnitExpr expr) {
        _rhs = expr;
    }

    public void setSource(NamedObj source) {
        _source = source;
    }

    public String toString() {
        return _lhs.toString() + _operator + _rhs.toString();
    }

    public Object visit(EquationVisitor visitor)
        throws IllegalActionException {
        return visitor._visitUnitEquation(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    UnitExpr _lhs, _rhs;
    String _operator;
    NamedObj _source = null;
}
