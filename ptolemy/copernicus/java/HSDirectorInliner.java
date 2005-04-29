/* An interface for classes that replaces port methods.

Copyright (c) 2001-2005 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Expr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeResolver;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;


//////////////////////////////////////////////////////////////////////////
//// HSDirectorInliner

/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class HSDirectorInliner implements DirectorInliner {
    public void inlineDirector(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) throws IllegalActionException {
        InlinePortTransformer.setPortInliner(model,
                new HSPortInliner(modelClass, model, options));

        FSMDirector director = (FSMDirector) model.getDirector();
        FSMActor controller;

        try {
            controller = director.getController();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        System.out.println("Inlining director for " + model.getFullName());

        Type actorType = RefType.v(PtolemyUtilities.actorClass);
        //         SootField postfireReturnsField = new SootField("_postfireReturns",
        //                 BooleanType.v(), Modifier.PRIVATE);
        //         modelClass.addField(postfireReturnsField);
        // Inline the director
        {
            // populate the preinitialize method
            SootMethod classMethod = modelClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Add code to the beginning of the preinitialize method that
            // initializes the attributes.
            //             ModelTransformer.initializeAttributesBefore(body, insertPoint,
            //                     model, body.getThisLocal(),
            //                     model, body.getThisLocal(),
            //                     modelClass);
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(entity,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(entity,
                        options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod preinitializeMethod = SootUtilities
                    .searchForMethodByName(theClass, "preinitialize");
                Local actorLocal = Jimple.v().newLocal("actor",
                        RefType.v(theClass));
                body.getLocals().add(actorLocal);

                // Get the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                           .newVirtualInvokeExpr(actorLocal,
                                                   preinitializeMethod)), insertPoint);
            }

            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the initialize method
            SootMethod classMethod = modelClass.getMethodByName("initialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(entity,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(entity,
                        options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod initializeMethod = SootUtilities
                    .searchForMethodByName(theClass, "initialize");

                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                           .newVirtualInvokeExpr(actorLocal,
                                                   initializeMethod)), insertPoint);
            }

            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the prefire method
            SootMethod classMethod = modelClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Chain units = body.getUnits();
            Stmt insertPoint = (Stmt) units.getLast();

            Local thisLocal = body.getThisLocal();

            Local prefireReturnsLocal = Jimple.v().newLocal("preReturns",
                    BooleanType.v());
            body.getLocals().add(prefireReturnsLocal);

            // Prefire the controller.
            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            String fieldName = ModelTransformer.getFieldNameForEntity(controller,
                    model);
            SootField field = modelClass.getFieldByName(fieldName);
            String className = ModelTransformer.getInstanceClassName(controller,
                    options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);
            SootMethod actorPrefireMethod = SootUtilities.searchForMethodByName(theClass,
                    "prefire");

            units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                       Jimple.v().newInstanceFieldRef(thisLocal, field)),
                    insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
                                       Jimple.v().newVirtualInvokeExpr(actorLocal,
                                               actorPrefireMethod)), insertPoint);

            units.insertBefore(Jimple.v().newReturnStmt(prefireReturnsLocal),
                    insertPoint);

            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the fire method
            SootMethod classMethod = modelClass.getMethodByName("fire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local indexLocal = Jimple.v().newLocal("index", IntType.v());
            body.getLocals().add(indexLocal);

            Local tokenLocal = Jimple.v().newLocal("token",
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            // Transfer Inputs from input ports.
            for (Iterator ports = model.inputPortList().iterator();
                 ports.hasNext();) {
                IOPort port = (IOPort) ports.next();
                int rate = 1;

                String fieldName = ModelTransformer.getFieldNameForPort(port,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);

                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);

                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(Jimple.v().newAssignStmt(tempPortLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(portLocal,
                                           Jimple.v().newCastExpr(tempPortLocal,
                                                   PtolemyUtilities.ioportType)), insertPoint);

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(Jimple.v().newAssignStmt(indexLocal,
                                                IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();

                    // Read
                    bodyList.add(Jimple.v().newAssignStmt(tokenLocal,
                                         Jimple.v().newVirtualInvokeExpr(portLocal,
                                                 PtolemyUtilities.getMethod, IntConstant.v(i))));

                    // Write
                    bodyList.add(Jimple.v().newInvokeStmt(Jimple.v()
                                         .newVirtualInvokeExpr(portLocal,
                                                 PtolemyUtilities.sendInsideMethod,
                                                 IntConstant.v(i), tokenLocal)));

                    // Increment the index.
                    bodyList.add(Jimple.v().newAssignStmt(indexLocal,
                                         Jimple.v().newAddExpr(indexLocal, IntConstant.v(1))));

                    Expr conditionalExpr = Jimple.v().newLtExpr(indexLocal,
                            IntConstant.v(rate));

                    SootUtilities.createForLoopBefore(body, insertPoint,
                            initializerList, bodyList, conditionalExpr);
                }
            }

            {
                // Fire the controller.
                Local actorLocal = Jimple.v().newLocal("actor", actorType);
                body.getLocals().add(actorLocal);

                String fieldName = ModelTransformer.getFieldNameForEntity(controller,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(controller,
                        options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod actorFireMethod = SootUtilities
                    .searchForMethodByName(theClass, "fire");

                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                           .newVirtualInvokeExpr(actorLocal,
                                                   actorFireMethod)), insertPoint);
            }

            // Transfer outputs from output ports
            for (Iterator ports = model.outputPortList().iterator();
                 ports.hasNext();) {
                IOPort port = (IOPort) ports.next();
                int rate = DFUtilities.getTokenProductionRate(port);

                String fieldName = ModelTransformer.getFieldNameForPort(port,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);

                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);

                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(Jimple.v().newAssignStmt(tempPortLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(portLocal,
                                           Jimple.v().newCastExpr(tempPortLocal,
                                                   PtolemyUtilities.ioportType)), insertPoint);

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(Jimple.v().newAssignStmt(indexLocal,
                                                IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();

                    // Read
                    bodyList.add(Jimple.v().newAssignStmt(tokenLocal,
                                         Jimple.v().newVirtualInvokeExpr(portLocal,
                                                 PtolemyUtilities.getInsideMethod,
                                                 IntConstant.v(i))));

                    // Write
                    bodyList.add(Jimple.v().newInvokeStmt(Jimple.v()
                                         .newVirtualInvokeExpr(portLocal,
                                                 PtolemyUtilities.sendMethod,
                                                 IntConstant.v(i), tokenLocal)));

                    // Increment the index.
                    bodyList.add(Jimple.v().newAssignStmt(indexLocal,
                                         Jimple.v().newAddExpr(indexLocal, IntConstant.v(1))));

                    Expr conditionalExpr = Jimple.v().newLtExpr(indexLocal,
                            IntConstant.v(rate));

                    SootUtilities.createForLoopBefore(body, insertPoint,
                            initializerList, bodyList, conditionalExpr);
                }
            }

            // Return.
            //            units.add(Jimple.v().newReturnVoidStmt());
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the postfire method
            SootMethod classMethod = modelClass.getMethodByName("postfire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns",
                    BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);

            // Postfire the controller.
            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            String fieldName = ModelTransformer.getFieldNameForEntity(controller,
                    model);
            SootField field = modelClass.getFieldByName(fieldName);
            String className = ModelTransformer.getInstanceClassName(controller,
                    options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);
            SootMethod actorPostfireMethod = SootUtilities
                .searchForMethodByName(theClass, "postfire");

            units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                       Jimple.v().newInstanceFieldRef(thisLocal, field)),
                    insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       Jimple.v().newVirtualInvokeExpr(actorLocal,
                                               actorPostfireMethod)), insertPoint);

            units.insertBefore(Jimple.v().newReturnStmt(postfireReturnsLocal),
                    insertPoint);
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the wrapup method
            SootMethod classMethod = modelClass.getMethodByName("wrapup");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity) entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(entity,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(entity,
                        options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod wrapupMethod = SootUtilities.searchForMethodByName(theClass,
                        "wrapup");

                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                           .newVirtualInvokeExpr(actorLocal,
                                                   wrapupMethod)), insertPoint);
            }

            //           units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //                   insertPoint);
        }
    }
}
