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
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
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

public class UnitEquation extends UnitConstraint implements UnitPresentation {

    /** Construct a UnitEquation from the left and right hand sides.
     * @param lhs The left hand side.
     * @param rhs The right hand side.
     */
    public UnitEquation(UnitExpr lhs, UnitExpr rhs) {
        super(lhs, "=", rhs);
    }

    /**
     *
     */
    public UnitEquation() {
        super("=");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the equations are all satisfied.
     * @param equations
     * @param bindings
     * @return True if the equations are all satisfied.
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

    /** Transform to the canonical form of the equation.
     * The canonical form of an equation is
     * <b>
     * Ex1, Ex2, ..., Exn = A
     * <b>
     * where each Exi is a Unit term containing only one variable, and A is a
     * Unit term containing one Unit and no variables.
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

    public UnitEquation copy() {
        UnitEquation uE = new UnitEquation(getLhs().copy(), getRhs().copy());
        return uE;
    }

    /**
     * @param modelBindings
     * @return A human readable form.
     */
    public String humanReadableForm(Bindings modelBindings) {
        String retv =
            commonExpression() + " satisfied = " + isSatisfied(modelBindings);
        return retv;
    }

    /** Return true if this equation is satisfied.
     * @param bindings
     * @return True if this equation is satisfied.
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

    public Object visit(EquationVisitor visitor)
        throws IllegalActionException {
        return visitor._visitUnitEquation(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
