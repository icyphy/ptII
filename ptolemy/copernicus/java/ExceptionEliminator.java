/* Eliminate all references to named objects

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

package ptolemy.copernicus.java;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import soot.HasPhaseOptions;
import soot.Local;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;


//////////////////////////////////////////////////////////////////////////
//// ExceptionEliminator
/**
Replace instances of Ptolemy exceptions with instances of plain old
RuntimeException.  This transformation is primarily useful from a memory
standpoint, as it prevents the ptolemy kernel from being required in the
generated code.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ExceptionEliminator extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private ExceptionEliminator(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ExceptionEliminator v(CompositeActor model) {
        return new ExceptionEliminator(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage obfuscate";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("ExceptionEliminator.internalTransform("
                + phaseName + ", " + options + ")");

        _obfuscate = PhaseOptions.getBoolean(options, "obfuscate");
        // Loop over all the classes

        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                for (Iterator traps = body.getTraps().iterator();
                     traps.hasNext();) {
                    Trap trap = (Trap)traps.next();
                    SootClass exception = trap.getException();
                    if (_isPtolemyException(exception)) {
                        trap.setException(PtolemyUtilities.exceptionClass);
                    }
                }
                _replaceExceptions(body);
            }
        }
    }

    private boolean _isPtolemyException(SootClass exceptionClass) {
        if (SootUtilities.derivesFrom(
                exceptionClass,
                PtolemyUtilities.kernelExceptionClass)) {
            return true;
        }
        if (SootUtilities.derivesFrom(
                exceptionClass,
                PtolemyUtilities.kernelRuntimeExceptionClass)) {
            return true;
        }
        return false;
    }

    // Replace any Ptolemy exception constructor
    // or initializer with a plain old RuntimeException.
    private void _replaceExceptions(JimpleBody body) {
        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Stmt unit = (Stmt)units.next();

            // If any box is removable, then remove the statement.
            for (Iterator boxes = unit.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                // FIXME: This is currently way too simple.
                Value value = box.getValue();
                Type type = value.getType();
                if (value instanceof NewExpr) {
                    // Fix kernel exceptions to be runtime exceptions.
                    NewExpr expr = (NewExpr)value;
                    SootClass exceptionClass = expr.getBaseType().getSootClass();
                    if (_isPtolemyException(exceptionClass)) {
                        expr.setBaseType(
                                RefType.v(PtolemyUtilities.runtimeExceptionClass));

                    }
                } else if(value instanceof CastExpr) {
                    CastExpr expr = (CastExpr) value;
                    Type castType = expr.getCastType();
                    if(castType instanceof RefType &&
                            _isPtolemyException(((RefType)castType).getSootClass())) {
                        expr.setCastType(RefType.v(PtolemyUtilities.runtimeExceptionClass));
                    }
                } else if(value instanceof SpecialInvokeExpr) {
                    // Fix the exception constructors.
                    SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                    SootClass exceptionClass =
                        ((RefType)expr.getBase().getType()).getSootClass();
                    if (_isPtolemyException(exceptionClass)) {
                        Value foundArg = null;
                        for (Iterator args = expr.getArgs().iterator();
                             args.hasNext();) {
                            Value arg = (Value)args.next();
                            if (arg.getType().equals(RefType.v(PtolemyUtilities.stringClass))) {
                                foundArg = arg;
                                break;
                            }
                        }
                        if (foundArg == null || _obfuscate) {
                            box.setValue(Jimple.v().newSpecialInvokeExpr(
                                    (Local)expr.getBase(),
                                    PtolemyUtilities.runtimeExceptionConstructor,
                                    Collections.EMPTY_LIST));
                        } else {
                            box.setValue(Jimple.v().newSpecialInvokeExpr(
                                    (Local)expr.getBase(),
                                    PtolemyUtilities.runtimeExceptionStringConstructor,
                                    foundArg));
                        }
                    }
                } else if (value instanceof VirtualInvokeExpr) {
                    VirtualInvokeExpr expr = (VirtualInvokeExpr)value;
                    Type exceptionType = expr.getBase().getType();
                    if(exceptionType instanceof RefType) {
                        SootClass exceptionClass =
                            ((RefType)exceptionType).getSootClass();
                        if (_isPtolemyException(exceptionClass)) {
                            SootMethod method = expr.getMethod();
                            if (method.getName().equals("getMessage")) {
                                if (unit instanceof InvokeStmt) {
                                    body.getUnits().remove(unit);
                                } else {
                                    box.setValue(StringConstant.v("PtolemyException"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private CompositeActor _model;
    private boolean _obfuscate;
}














