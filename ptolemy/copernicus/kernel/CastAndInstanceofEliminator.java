/* A transformer that tried to statically evaluate object == object

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
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;
import java.util.*;

/** 
An attempt to remove unnecessary casts and instanceof checks.

*/

public class CastAndInstanceofEliminator extends BodyTransformer
{
    private static CastAndInstanceofEliminator instance = new CastAndInstanceofEliminator();
    private CastAndInstanceofEliminator() {}

    public static CastAndInstanceofEliminator v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions(); }
    
    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;
        if(Main.isVerbose)
            System.out.println("[" + body.getMethod().getName() +
                "] Eliminating unnecessary casts and instanceof...");
        
        eliminateCastsAndInstanceOf(body, phaseName, new HashSet());
    }
 
    public static void eliminateCastsAndInstanceOf(Body body, String phaseName, 
            Set unsafeLocalSet) {
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            for(Iterator boxes = unit.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
              
                if(value instanceof CastExpr) {
                    // If the cast is to the same type as the 
                    // operand already is, then replace with 
                    // simple assignment.
                    CastExpr expr = (CastExpr)value;
                    Type castType = expr.getCastType();
                    Value op = expr.getOp();
                    Type opType = op.getType();
 
                    // Skip locals that are unsafe.
                    if(castType.equals(opType) &&
                       !unsafeLocalSet.contains(op)) {
                        box.setValue(op);
                    }
                } else if(value instanceof InstanceOfExpr) {
                    // If the operand of the expression is 
                    // declared to be of a type that implies
                    // the instanceof is true, then replace
                    // with true.
                    InstanceOfExpr expr = (InstanceOfExpr)value;
                    Type checkType = expr.getCheckType();
                    Value op = expr.getOp();
                    Type opType = op.getType();

                    // Skip locals that are unsafe.
                    if(unsafeLocalSet.contains(op)) {
                        continue;
                    }

                    RefType checkRef, opRef;
                    if(checkType instanceof RefType && 
                            opType instanceof RefType) {
                        checkRef = (RefType)checkType;
                        opRef = (RefType)opType;
                      
                    } else if(checkType instanceof ArrayType &&
                              opType instanceof ArrayType) {
                        if(((ArrayType)checkType).numDimensions != 
                                ((ArrayType)opType).numDimensions) {
                            // We know the answer is false.
                            box.setValue(IntConstant.v(0));
                            continue;
                        }
                        Type checkBase = ((ArrayType)checkType).baseType;
                        Type opBase = ((ArrayType)opType).baseType;
                        if(checkBase instanceof RefType &&
                               opBase instanceof RefType) {
                            checkRef = (RefType)checkBase;
                            opRef = (RefType)opBase;
                        } else {
                            // Can't say anything?
                            continue;
                        }
                    } else {
                        // Can't say anything?
                        continue;
                    }
                    SootClass checkClass = ((RefType)checkRef).getSootClass();
                    SootClass opClass = ((RefType)checkRef).getSootClass();
                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();
                    if(checkClass.isInterface()) {
                        if(opClass.getInterfaces().contains(checkClass)) {
                            // Then we know the instanceof will be true.
                            box.setValue(IntConstant.v(1));
                        }
                    } else if(hierarchy.isClassSuperclassOfIncluding(checkClass, opClass)) {
                        // Then we know the instanceof will be true.
                        box.setValue(IntConstant.v(1));
                    }
                }
            }
        }
    }
}
