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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.attributes.URIAttribute;
import soot.FastHierarchy;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalSplitter;


//////////////////////////////////////////////////////////////////////////
//// ConstructorSpecializer
/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ConstructorSpecializer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private ConstructorSpecializer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ConstructorSpecializer v(CompositeActor model) {
        return new ConstructorSpecializer(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("ConstructorSpecializer.internalTransform("
                + phaseName + ", " + options + ")");
 
        List modifiedConstructorClassList = new LinkedList();

        // Loop over all the classes
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            if (SootUtilities.derivesFrom(theClass,
                    PtolemyUtilities.actorClass) ||
                    SootUtilities.derivesFrom(theClass,
                            PtolemyUtilities.compositeActorClass) ||
                    SootUtilities.derivesFrom(theClass,
                            PtolemyUtilities.attributeClass)) {
                if(theClass.declaresFieldByName(
                           ModelTransformer.getContainerFieldName())) {
                    for (Iterator methods = theClass.getMethods().iterator();
                         methods.hasNext();) {
                        SootMethod method = (SootMethod)methods.next();
                        if(method.getName().equals("<init>") && 
                                method.getParameterCount() == 2) {
                            // Change the constructor so that it takes an
                            // appropriate container type.
                            SootField containerField =
                                theClass.getFieldByName(
                                        ModelTransformer.getContainerFieldName());
                            RefType containerType = 
                                (RefType)containerField.getType();
                            List typeList = new LinkedList();
                            typeList.add(containerType);
                            typeList.add(RefType.v("java.lang.String"));
                            method.setParameterTypes(typeList);
                            
                            // Dance so that indexes in the Scene are properly
                            // updated.
                            theClass.removeMethod(method);
                            theClass.addMethod(method);      
                            
                            // Replace the parameter refs so THEY have
                            // the right type, too..
                            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                            
                            for (Iterator units = body.getUnits().snapshotIterator();
                                 units.hasNext();) {
                                Stmt unit = (Stmt)units.next();
                                
                                if (unit instanceof IdentityStmt) {
                                    IdentityStmt identityStmt = (IdentityStmt)unit;
                                    Value value = identityStmt.getRightOp();
                                    if (value instanceof ParameterRef) {
                                        ParameterRef parameterRef = (ParameterRef)value;
                                        if(parameterRef.getIndex() == 0) {
                                            ValueBox box = identityStmt.getRightOpBox();
                                            box.setValue(Jimple.v().newParameterRef(
                                                                 method.getParameterType(0), 0));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Keep track of the modification, so we know to
                    // modify invocations of that constructor.
                    modifiedConstructorClassList.add(theClass);
                    
                }
            }
        }

        // Reset the hierarchy, since we've changed superclasses and such.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        // Fix the specialInvokes.
        /* for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    if (unit.containsInvokeExpr()) {
                        ValueBox box = unit.getInvokeExprBox();
                        Value value = box.getValue();

                        if (value instanceof SpecialInvokeExpr) {
                            // If we're constructing one of our actor classes,
                            // then switch to the modified constructor.
                            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                            SootClass declaringClass =
                                expr.getMethod().getDeclaringClass();
                            if (expr.getMethod().getName().equals("<init>") &&
                                    modifiedConstructorClassList.contains(
                                            declaringClass)) {
                                // System.out.println(
//                                         "replacing constructor invocation = "
//                                         + unit + " in method " + method);
                                SootMethod newConstructor = 
                                    declaringClass.getMethodByName("<init>");
                                if(newConstructor.getParameterCount() == 1) {
                                    // Replace with just container arg constructor.
                                    List args = new LinkedList();
                                    args.add(expr.getArg(0));
                                    box.setValue(
                                            Jimple.v().newSpecialInvokeExpr(
                                                    (Local)expr.getBase(),
                                                    newConstructor,
                                                    args));
                                } else {
                                    // Replace with zero arg constructor.
                                    box.setValue(
                                            Jimple.v().newSpecialInvokeExpr(
                                                    (Local)expr.getBase(),
                                                    newConstructor,
                                                    Collections.EMPTY_LIST));
                                }
                            }
                        }
                    }
                }
            }
        }
         */
    }
    private CompositeActor _model;
}














