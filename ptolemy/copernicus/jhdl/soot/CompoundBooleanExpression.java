/*

 Copyright (c) 2001-2003 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.soot;

import ptolemy.kernel.util.IllegalActionException;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;

import soot.UnitPrinter;

//////////////////////////////////////////////////////////////////////////
//// CompoundBooleanExpression
/**
 * This abstract class represents a CompoundBooleanExpression within Soot.
 * Note that this class is an extension to Soot by providing a
 * way to represent compound Boolean expressions within a single
 * class (there is no corresponding semantic object in Java).
 *
 * This class was creaed to represent compound Boolean expressions that
 * are represented in Soot (and correspondingly in Java Byte codes)
 * as a series of successive IfStmt objects. It is often difficult to
 * manipulate the compound expressions in the IfStmt form in which
 * they are traditionally represented in Java.
 *
 * This class provides a method for "inverting" a Value (specifically a
 * ConditionExpr) that is needed to perform the logic construction of
 * the cascading IfStmts.
 *
 * @see ptolemy.copernicus.jhdl.soot.ConditionalControlCompactor
 *
 * @author Mike Wirthlin
 * @version $Id$
 * @since Ptolemy II 2.0 */

public abstract class CompoundBooleanExpression implements Value, ConditionExpr {

    public abstract CompoundBooleanExpression invert() throws IllegalActionException;
    public abstract Object clone();
    public abstract Value getOp1();
    public abstract Value getOp2();
    public abstract void toString(UnitPrinter up);

    /**
     * This method will "invert" the Value passed as an argument. The
     * Value must be of type ConditionExpr (an IllegalActionException
     * will be thrown if a different type is passed). This method will
     * create a new ConditionExpr that is the inverse of the
     * ConditionExpr Value in the argument.
     *
     * The following mapping is used:
     *
     * <ul>
     * <li> EqExpr -> NeExpr
     * <li> GeExpr -> LtExpr
     * <li> GtExpr -> LeExpr
     * <li> LeExpr -> GtExpr
     * <li> LtExpr -> GeExpr
     * <li> NeExpr -> EqExpr
     * </ul>
     **/
    public static Value invertValue(Value inValue) throws IllegalActionException {
        Value newValue=null;
        if (inValue instanceof ConditionExpr) {
            Value op1 = ((ConditionExpr)inValue).getOp1();
            Value op2 = ((ConditionExpr)inValue).getOp2();
            if (inValue instanceof EqExpr) {
                newValue = new JNeExpr(op1,op2);
            } else if (inValue instanceof GeExpr) {
                newValue = new JLtExpr(op1,op2);
            } else if (inValue instanceof GtExpr) {
                newValue = new JLeExpr(op1,op2);
            } else if (inValue instanceof LeExpr) {
                newValue = new JGtExpr(op1,op2);
            } else if (inValue instanceof LtExpr) {
                newValue = new JGeExpr(op1,op2);
            } else if (inValue instanceof NeExpr) {
                newValue = new JEqExpr(op1,op2);
            } else if (inValue instanceof CompoundBooleanExpression) {
                newValue = ((CompoundBooleanExpression)inValue).invert();
            } else
                throw new IllegalActionException("Unknown ConditionExpr "+
                        inValue.getClass());
        } else
            throw new IllegalActionException("Can't Invert " +
                    inValue.getClass());
        return newValue;
    }

}
