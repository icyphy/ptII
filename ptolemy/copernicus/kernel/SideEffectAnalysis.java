/* An analysis for detecting objects that must be aliased to eachother.

 Copyright (c) 2001 The Regents of the University of California.
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

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.*;

/**
An analysis that determines which methods in a given invoke graph
have no side effects.
*/
public class SideEffectAnalysis extends BackwardFlowAnalysis {
    public SideEffectAnalysis(MethodCallGraph g) {
        super(g);
        doAnalysis();
    }
    
    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public boolean hasSideEffects(SootMethod method) {
        BooleanFlow flow = (BooleanFlow)getFlowBefore(method);
        if(flow == null) {
            if(_debug) System.out.println(
                    "SideEffectAnalysis: Method not found: " + method);
            return true;
        }
        return flow.getValue();
    }

    // Formulation:
    protected Object newInitialFlow() {
        return new BooleanFlow();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue) {
        BooleanFlow in = (BooleanFlow)inValue;
        BooleanFlow out = (BooleanFlow)outValue;
        SootMethod method = (SootMethod)d;
 
        if(_debug) System.out.println("SideEffectAnalysis: method = " + method);
        // A method has side effects if any method it uses has side effects.
        if(in.getValue() == true) {
            if(_debug) System.out.println(
                    "SideEffectAnalysis: uses a side-effect Method");
            out.setValue(true);
            return;
        }

        // A method that is a context class is assumed to have side effects,
        // since we can't get it's method body.  Note that we could do better
        // by handling each method specifically.  (For Example, Thread.currentThread()
        // has no body, but also has no side effects).
        SootClass declaringClass = method.getDeclaringClass();
        if(declaringClass.isContextClass()) {
            declaringClass.setLibraryClass();
        }

        if(!method.isConcrete()) {
            if(_debug) System.out.println("SideEffectAnalysis: has no body.");
            out.setValue(true);
            return;
        }
        
        // A method has side effects if it sets the values of any fields.
        Body body = method.retrieveActiveBody();
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            if(_debug) System.out.println("unit = " + unit);
            for(Iterator boxes = unit.getDefBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
                if(value instanceof FieldRef) {
                    if(_debug) System.out.println(
                            "SideEffectAnalysis: assigns to field");
                    out.setValue(true);
                    return;
                }
            }

            // Method calls that are in the invokeGraph have already been checked.
            // However, it turns out that context classes are not included in the
            // invokeGraph!  This checks to see if there are any invocations of
            // methods that are not in the invoke graph.  Conservatively
            // assume that they have side effects.
            for(Iterator boxes = unit.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
                if(value instanceof InvokeExpr) {
                    SootMethod invokedMethod = ((InvokeExpr)value).getMethod();
                    if(!((MethodCallGraph)graph).isReachable(
                            invokedMethod.getSignature())) {
                        if(_debug) System.out.println(
                                "SideEffectAnalysis: Calls a method that is not in the graph");
                        out.setValue(true);
                        return;
                    }
                }
            }
        }

        // Otherwise, we have no side effects.
        out.setValue(false);
    }

    protected void copy(Object inValue, Object outValue) {
        BooleanFlow in = (BooleanFlow)inValue;
        BooleanFlow out = (BooleanFlow)outValue;
        out.setValue(in.getValue());
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        BooleanFlow in1 = (BooleanFlow)in1Value;
        BooleanFlow in2 = (BooleanFlow)in2Value;
        BooleanFlow out = (BooleanFlow)outValue;
        
        // A method has side effects if any method it uses has side effects.
        out.setValue(in1.getValue() || in2.getValue());
    }
 
    private static class BooleanFlow {
        public BooleanFlow() {
            _value = false;
        }
        public boolean equals(Object o) {
            if(o instanceof BooleanFlow) {
                return _value == ((BooleanFlow)o).getValue();
            } else {
                return false;
            }
        }
        public void setValue(boolean flag) {
            _value = flag;
        }
        public boolean getValue() {
            return _value;
        }
        private boolean _value;
    }
    private boolean _debug = false;
}
