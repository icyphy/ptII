/* A transformer that tried to statically evaluate object == object

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


package ptolemy.copernicus.kernel;

import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.Hierarchy;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
A transformer that remove unnecessary casts and instanceof checks.
Note that this relies on properly inferred Java types to operate properly.
If you create code that has types which are too specific (relative to the
inferred types) then this transformer will likely create code that is no
longer verifiable.

*/

public class CastAndInstanceofEliminator extends BodyTransformer {
    private static CastAndInstanceofEliminator instance =
    new CastAndInstanceofEliminator();
    private CastAndInstanceofEliminator() {}

    public static CastAndInstanceofEliminator v() {
        return instance;
    }

    public String getDeclaredOptions() {
        return "targetPackage debug";
    }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;

        //         System.out.println("CastAndInstanceofEliminator.internalTransform("
        //                 + b.getMethod() + phaseName + ")");

        boolean debug = PhaseOptions.getBoolean(options, "debug");

        eliminateCastsAndInstanceOf(body, phaseName, new HashSet(), debug);
    }

    public static void eliminateCastsAndInstanceOf(Body body,
            String phaseName, Set unsafeLocalSet, boolean debug) {

        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Unit unit = (Unit)units.next();
            for (Iterator boxes = unit.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();

                // This assumes that the types of local
                // variables are verifiable types.
                // This is not ensured by Soot, unless
                // you run the TypeAssigner before this
                // transformation.
                if (value instanceof CastExpr) {
                    // If the cast is to the same type as the
                    // operand already is, then replace with
                    // simple assignment.
                    CastExpr expr = (CastExpr)value;
                    Type castType = expr.getCastType();
                    Value op = expr.getOp();
                    Type opType = op.getType();

                    //                     // Skip locals that are unsafe.
                    //                     if (castType.equals(opType) &&
                    //                             !unsafeLocalSet.contains(op)) {
                    //                         box.setValue(op);
                    //                     }
                    if (unsafeLocalSet.contains(op)) {
                        continue;
                    }

                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();

                  //   if(debug) System.out.println("checking cast in " + unit);
//                     if(debug) System.out.println("op = " + op);
//                     if(debug) System.out.println("opType = " + opType);
                    replaceCast(box, hierarchy,
                            castType, op, opType, debug);

                } else if (value instanceof InstanceOfExpr) {
                    // If the operand of the expression is
                    // declared to be of a type that implies
                    // the instanceof is true, then replace
                    // with true.
                    InstanceOfExpr expr = (InstanceOfExpr)value;
                    Type checkType = expr.getCheckType();
                    Value op = expr.getOp();
                    Type opType = op.getType();

                    // Skip locals that are unsafe.
                    if (unsafeLocalSet.contains(op)) {
                        continue;
                    }

                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();

                    replaceInstanceofCheck(box, hierarchy,
                            checkType, opType, debug);
                }
            }
        }
    }

    /** Statically evaluate the instance of Check in the given box,
     *  if possible.  If <i>opType</i> is always an instance of
     *  <i>checkType</i>, based on the given hierarchy,
     *  then replace with a true constant.  If <i>opType</i> is
     *  never an instance of <i>checkType</i>, then replace
     *  with a false constant.
     */
    public static void replaceInstanceofCheck(ValueBox box,
            Hierarchy hierarchy, Type checkType, Type opType,
            boolean debug) {

        RefType checkRef, opRef;
        if (checkType instanceof RefType &&
                opType instanceof RefType) {
            checkRef = (RefType)checkType;
            opRef = (RefType)opType;

        } else if (checkType instanceof ArrayType &&
                opType instanceof ArrayType) {
            if (((ArrayType)checkType).numDimensions !=
                    ((ArrayType)opType).numDimensions) {
                // We know the answer is false.
                box.setValue(IntConstant.v(0));
                if (debug) System.out.println("Replacing " +
                        box.getValue() + " with false.");
                return;
            }
            Type checkBase = ((ArrayType)checkType).baseType;
            Type opBase = ((ArrayType)opType).baseType;
            if (checkBase instanceof RefType &&
                    opBase instanceof RefType) {
                checkRef = (RefType)checkBase;
                opRef = (RefType)opBase;
            } else {
                // Can't say anything?
                return;
            }
        } else {
            // Can't say anything?
            return;
        }
        SootClass checkClass = ((RefType)checkRef).getSootClass();
        SootClass opClass = ((RefType)opRef).getSootClass();
        if (debug) System.out.println("checkClass = " + checkClass);
        if (debug) System.out.println("opClass = " + opClass);
        if (checkClass.isInterface()) {
            if (opClass.isInterface()) {
                if (hierarchy.isInterfaceSubinterfaceOf(
                        opClass, checkClass) ||
                        opClass.equals(checkClass)) {
                                // Then we know the instanceof will be true.
                    if (debug) System.out.println("Replacing " +
                            box.getValue() + " with true.");
                    box.setValue(IntConstant.v(1));
                }
            } else {
                // opClass is a class, not an interface.
                if (hierarchy.getImplementersOf(checkClass).contains(opClass)) {
                    // Then we know the instanceof will be true.
                    if (debug) System.out.println("Replacing " +
                            box.getValue() + " with true.");
                    box.setValue(IntConstant.v(1));
                } else {
                    // We need to ensure that no subclass
                    // of opclass implements the
                    // interface.  This will mean we
                    // replace with false.
                }
            }
        } else {
            if (opClass.isInterface()) {
                //???
            } else {
                if (hierarchy.isClassSuperclassOfIncluding(
                        checkClass, opClass)) {
                    // Then we know the instanceof will be true.
                    if (debug) System.out.println("Replacing " +
                            box.getValue() + " with true.");
                    box.setValue(IntConstant.v(1));
                } else if (!hierarchy.isClassSuperclassOfIncluding(
                                   opClass, checkClass)) {
                    // Then we know the instanceof will be false,
                    // because no subclass of opClass can suddenly
                    // become a subclass of checkClass.
                    if (debug) System.out.println("Replacing " +
                            box.getValue() + " with false.");
                    box.setValue(IntConstant.v(0));
                }
            }
        }
    }

    /** Remove the case in the given box,
     *  if possible.  If <i>opType</i> is always an instance of
     *  <i>checkType</i>, based on the given hierarchy,
     *  then replace with an assignment.
     */
    public static void replaceCast(ValueBox box,
            Hierarchy hierarchy, Type checkType,
            Value op, Type opType, boolean debug) {

        RefType checkRef, opRef;
        if (checkType instanceof RefType &&
                opType instanceof RefType) {
            checkRef = (RefType)checkType;
            opRef = (RefType)opType;

        } else if (checkType instanceof ArrayType &&
                opType instanceof ArrayType) {
            if (((ArrayType)checkType).numDimensions !=
                    ((ArrayType)opType).numDimensions) {
                // We know the cast is necessary
                return;
            }
            Type checkBase = ((ArrayType)checkType).baseType;
            Type opBase = ((ArrayType)opType).baseType;
            if (checkBase instanceof RefType &&
                    opBase instanceof RefType) {
                checkRef = (RefType)checkBase;
                opRef = (RefType)opBase;
            } else {
                // Can't say anything?
                return;
            }
        } else {
            // Can't say anything?
            return;
        }
        SootClass checkClass = ((RefType)checkRef).getSootClass();
        SootClass opClass = ((RefType)opRef).getSootClass();
        if (debug) System.out.println("castClass = " + checkClass);
        if (debug) System.out.println("opClass = " + opClass);
        if (checkClass.isInterface()) {
            if (opClass.isInterface()) {
                if (hierarchy.isInterfaceSubinterfaceOf(
                        opClass, checkClass) ||
                        opClass.equals(checkClass)) {
                    // Then we know the instanceof will be true.
                    if (debug) System.out.println("Replacing " +
                            "with assignment.");
                    box.setValue(op);
                }
            } else {
                // opClass is a class, not an interface.
                if (hierarchy.getImplementersOf(checkClass).contains(opClass)) {
                    // Then we know the instanceof will be true.
                    //if (debug) System.out.println("Replacing " +
                    //       box.getValue() + " with true.");
                    //   box.setValue(IntConstant.v(1));
                } else {
                    // We need to ensure that no subclass
                    // of opclass implements the
                    // interface.  This will mean we
                    // replace with false.
                }
            }
        } else {
            if (opClass.isInterface()) {
                //???
            } else {
                if (hierarchy.isClassSuperclassOfIncluding(
                        checkClass, opClass)) {
                    // Then we know the instanceof will be true.
                    if (debug) System.out.println("Replacing " +
                            "with assignment.");
                    box.setValue(op);
                } else if (!hierarchy.isClassSuperclassOfIncluding(
                        opClass, checkClass)) {
                    // Then we know the instanceof will be false,
                    // because no subclass of opClass can suddenly
                    // become a subclass of checkClass.
                    //if (debug) System.out.println("Replacing " +
                    //        box.getValue() + " with false.");
                    //                    box.setValue(IntConstant.v(0));
                }
            }
        }
    }
}
