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

import soot.Value;
import soot.UnitPrinter;
import soot.jimple.internal.AbstractIntBinopExpr;
import soot.jimple.Jimple;
import soot.util.Switch;
import soot.jimple.ConditionExpr;


// imports I need for copy
import soot.IntType;
//import soot.ToBriefString;
import java.util.List;
import java.util.ArrayList;
import soot.jimple.Expr;
import soot.Type;
import soot.ValueBox;

//////////////////////////////////////////////////////////////////////////
//// AbstractCompoundExpression
/**
 * This class extends the CompoundBooleanExpression class and implements
 * a number of interfaces to be compatible with soot (i.e. Expr and
 * ToBriefString).
 *
 * This class is very similar to the soot.jimple.internal.AbstractBinopExpr
 * class.
 *
 * TODO: figure out how to combine this class with CompoundBooleanExpression
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public abstract class AbstractCompoundExpression extends CompoundBooleanExpression
            //implements Expr, ToBriefString {
    implements Expr {

    public AbstractCompoundExpression(Value op1, Value op2) {
        if (op1 instanceof ConditionExpr)
            this.op1Box = Jimple.v().newConditionExprBox(op1);
        else
            this.op1Box = Jimple.v().newArgBox(op1);
        if (op2 instanceof ConditionExpr)
            this.op2Box = Jimple.v().newConditionExprBox(op2);
        else
            this.op2Box = Jimple.v().newArgBox(op2);
    }
    public void apply(Switch sw) {}; //?

    // This is a copy of AbstractIntBinopExpr
    public Type getType()
    {
        return IntType.v();
    }

    // This is all a copy of AbstractBinopExpr. Except for:
    // -  access method on getsymbol()
    // -  comment out abstract clone method
    // -  types in equivTo method
    protected ValueBox op1Box;
    protected ValueBox op2Box;

    public Value getOp1()
    {
        return op1Box.getValue();
    }

    public Value getOp2()
    {
        return op2Box.getValue();
    }

    public ValueBox getOp1Box()
    {
        return op1Box;
    }

    public ValueBox getOp2Box()
    {
        return op2Box;
    }

    public void setOp1(Value op1)
    {
        op1Box.setValue(op1);
    }

    public void setOp2(Value op2)
    {
        op2Box.setValue(op2);
    }

    public List getUseBoxes()
    {
        List list = new ArrayList();

        list.addAll(op1Box.getValue().getUseBoxes());
        list.add(op1Box);
        list.addAll(op2Box.getValue().getUseBoxes());
        list.add(op2Box);

        return list;
    }

    public boolean equivTo(Object o)
    {
        if (o instanceof AbstractCompoundExpression)
            {
                AbstractCompoundExpression abe = (AbstractCompoundExpression)o;
                return op1Box.getValue().equivTo(abe.op1Box.getValue()) &&
                    op2Box.getValue().equivTo(abe.op2Box.getValue()) &&
                    getSymbol().equals(abe.getSymbol());
            }
        return false;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode()
    {
        return op1Box.getValue().equivHashCode() * 101 + op2Box.getValue().equivHashCode() + 17
            ^ getSymbol().hashCode();
    }

    /** Returns the unique symbol for an operator. */
    abstract public String getSymbol();
    //abstract public Object clone();

    public String toString()
    {
        Value op1 = op1Box.getValue(), op2 = op2Box.getValue();
        String leftOp = op1.toString(), rightOp = op2.toString();

        return "(" + leftOp + getSymbol() + rightOp +")";
    }

    public void toString( UnitPrinter up ) {
        Value val1 = op1Box.getValue();
        Value val2 = op2Box.getValue();

        up.literal("(");
        op1Box.toString(up);
        up.literal(")");

        up.literal(getSymbol());
        
        up.literal("(");
        op2Box.toString(up);        
        up.literal(")");
    }
    
    /*
    public String toBriefString()
    {
        Value op1 = op1Box.getValue(), op2 = op2Box.getValue();
        String leftOp = ((ToBriefString)op1).toBriefString(),
            rightOp = ((ToBriefString)op2).toBriefString();

        return "(" + leftOp + getSymbol() + rightOp + ")";
    }
    */
}
