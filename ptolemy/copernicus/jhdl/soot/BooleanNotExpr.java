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

import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.internal.*;

//////////////////////////////////////////////////////////////////////////
//// BooleanNotExpr
/**
 *
 * A Not expression in Java (i.e. the ! operator) is implemented as
 * an IfStmt in which constant Boolean values are assigned to the
 * corresponding value. This method will scan through the chain
 * of units and convert such Not expressions into BooleanNotExpr
 * objects. This simplifies the control-flow analysis by removing
 * this control flow construct with a dataflow expression.
 *
 * This class looks a lot like soot.jimple.internal.AbstractNegExpr
 *
 * @see ptolemy.copernicus.jhdl.soot.BooleanNotCompactor
 *
 * @author Mike Wirthlin
 * @version $Id$
 * @since Ptolemy II 2.0
*/
public class BooleanNotExpr implements UnopExpr {

    /*
      public BooleanNotExpr(CompoundBooleanExpression op) {
      super(Jimple.v().newConditionExprBox(op));
      }

      public BooleanNotExpr(Value op) {
      super(Jimple.v().newImmediateBox(op));
      }
    */
    public BooleanNotExpr(Value op) {
        this.op = op;
    }

    public Value getOp() { return op; }
    public void setOp(Value op) { this.op = op; }
    public ValueBox getOpBox() { return null; }
    public java.util.List getUseBoxes() { return null; }

    public Object clone() {
        return new BooleanNotExpr(Jimple.cloneIfNecessary(getOp()));
    }
    /** Compares the specified object with this one for structural equality. */
    public boolean equivTo(Object o)
    {
        if (o instanceof BooleanNotExpr)
            {
                return getOpBox().getValue().equivTo(((BooleanNotExpr)o).getOpBox().getValue());
            }
        return false;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode()
    {
        return op.equivHashCode();
    }

    public String toString()
    {
        return "!" + " " + op.toString();
    }

    public void toString( UnitPrinter up ) {
        up.literal("!" + " ");
        op.toString(up);
        /*
        Value val1 = op1Box.getValue();
        Value val2 = op2Box.getValue();

        up.literal("(");
        op1Box.toString(up);
        up.literal(")");

        up.literal(getSymbol());
        
        up.literal("(");
        op2Box.toString(up);        
        up.literal(")");
        */
    }

    /*
    public String toBriefString()
    {
        return "!" + " " + ((ToBriefString) op).toBriefString();
    }
    */

    public Type getType()
    {
        Value op = getOpBox().getValue();

        if (op.getType().equals(IntType.v()) || op.getType().equals(ByteType.v()) ||
                op.getType().equals(ShortType.v()) || op.getType().equals(BooleanType.v()) ||
                op.getType().equals(CharType.v()))
            return IntType.v();
        else if (op.getType().equals(LongType.v()))
            return LongType.v();
        else if (op.getType().equals(DoubleType.v()))
            return DoubleType.v();
        else if (op.getType().equals(FloatType.v()))
            return FloatType.v();
        else
            return UnknownType.v();
    }

    public void apply(Switch sw)
    {
        //((ExprSwitch) sw).caseNegExpr(this);
    }
    ValueBox opBox;
    Value op;
}

