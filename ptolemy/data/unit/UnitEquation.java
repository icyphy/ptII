/* A Unit equation which is a prticular type of Unit Constraint.

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

//////////////////////////////////////////////////////////////////////////
//// UnitEquation
/** A Unit equation is a particlar type of Unit constraint, another type being
a Unit inequality.
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the equations are all satisfied.
     * @param equations The equations.
     * @return True if the equations are all satisfied.
     */
    public static boolean areSatisfied(Vector equations) {
        for (int i = 0; i < equations.size(); i++) {
            if (((UnitEquation) (equations.elementAt(i))).isSatisfied()
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
     * @return unitEquation The UnitEquation in canonical form.
     */
    public UnitEquation canonicalize() {
        UnitExpr lhsUExpr = getLhs();
        UnitExpr rhsUExpr = getRhs();
        UnitExpr newLeftUExpr = new UnitExpr();
        UnitExpr newRightUExpr = new UnitExpr();
        Vector leftUTerms = lhsUExpr.getUTerms();

        for (int i = 0; i < leftUTerms.size(); i++) {
            UnitTerm uTerm = ((UnitTerm) (leftUTerms.elementAt(i)));
            if (uTerm.isUnit()) {
                newRightUExpr.addUnitTerm(uTerm.invert());
            } else if (uTerm.isVariable()) {
                newLeftUExpr.addUnitTerm(uTerm);
            }
        }
        Vector rightUTerms = rhsUExpr.getUTerms();
        for (int i = 0; i < rightUTerms.size(); i++) {
            UnitTerm uTerm = ((UnitTerm) (rightUTerms.elementAt(i)));
            if (uTerm.isUnit()) {
                newRightUExpr.addUnitTerm(uTerm);
            } else if (uTerm.isVariable()) {
                newLeftUExpr.addUnitTerm(uTerm.invert());
            }
        }
        if (newRightUExpr.getUTerms().isEmpty()) {
            UnitTerm x = new UnitTerm();
            x.setUnit(UnitLibrary.Identity);
            newRightUExpr.addUnitTerm(x);
        }

        newRightUExpr = newRightUExpr.reduce();
        return new UnitEquation(newLeftUExpr, newRightUExpr);
    }

    /** Make a copy where the left and right sides are alos copied.
     * @return The copy.
     */
    public UnitEquation copy() {
        UnitEquation uE = new UnitEquation(getLhs().copy(), getRhs().copy());
        return uE;
    }

    /** Return true if this equation is satisfied.
     * @return True if this equation is satisfied.
     */
    public boolean isSatisfied() {
        Boolean retv = null;
        UnitExpr lhsReduced = getLhs().reduce();
        UnitExpr rhsReduced = getRhs().reduce();
        Unit lhsUnit = lhsReduced.getSingleUnit();
        Unit rhsUnit = rhsReduced.getSingleUnit();

        if (lhsUnit == null || rhsUnit == null) {
            return false;
        }
        if (lhsUnit.hasSameType(rhsUnit)) {
            return true;
        } else {
            return false;
        }
    }

    /** Visit this Unit equation (on the way to visiting the leaves)
     * @param visitor The visitor.
     * @return Whatever may be returned by the visitor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Object visit(EquationVisitor visitor)
        throws IllegalActionException {
        return visitor._visitUnitEquation(this);
    }
}
