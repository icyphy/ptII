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

import soot.BooleanType;
import soot.FastHierarchy;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.Modifier;
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.util.DFUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// SDFDirectorInliner

/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class SDFDirectorInliner implements DirectorInliner {
    public void inlineDirector(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) throws IllegalActionException {
        InlinePortTransformer.setPortInliner(model,
                new SDFPortInliner(modelClass, model, options));
        System.out.println("Inlining director for " + model.getFullName());

        Type actorType = RefType.v(PtolemyUtilities.actorClass);

        SootField postfireReturnsField = new SootField("_postfireReturns",
                BooleanType.v(), Modifier.PRIVATE);
        modelClass.addField(postfireReturnsField);

        int iterationLimit = 0;
        SDFDirector director = (SDFDirector) model.getDirector();

        if (director != null) {
            Attribute attribute = director.getAttribute("iterations");

            if (attribute instanceof Variable) {
                IntToken token = (IntToken) ((Variable) attribute).getToken();
                iterationLimit = token.intValue();
            }
        }
        // Inline the director
        {
            // populate the preinitialize method
            SootMethod classMethod = modelClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns",
                    BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);

            // Initialize the postfire flag.
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       IntConstant.v(1)), insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(Jimple.v()
                                       .newInstanceFieldRef(thisLocal,
                                               postfireReturnsField), postfireReturnsLocal),
                    insertPoint);

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

            //            units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //                   insertPoint);
        }

        SootField iterationField = new SootField("_iteration", IntType.v());
        modelClass.addField(iterationField);

        {
            // populate the initialize method
            SootMethod classMethod = modelClass.getMethodByName("initialize");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            if (iterationLimit > 0) {
                units.insertBefore(Jimple.v().newAssignStmt(Jimple.v()
                                           .newInstanceFieldRef(thisLocal,
                                                   iterationField), IntConstant.v(0)),
                        insertPoint);
            }

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

            //            units.insertBefore(Jimple.v().newReturnVoidStmt(),insertPoint);
        }
        // ModelTransformer does this.
        //         {
        //             // populate the prefire method
        //             SootMethod classMethod =
        //                 modelClass.getMethodByName("prefire");
        //             JimpleBody body = (JimpleBody)classMethod.getActiveBody();
        //             Stmt insertPoint = body.getFirstNonIdentityStmt();
        //             Chain units = body.getUnits();
        //             Local thisLocal = body.getThisLocal();
        //             Local prefireReturnsLocal =
        //                 Jimple.v().newLocal("preReturns", BooleanType.v());
        //             body.getLocals().add(prefireReturnsLocal);
        //             units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
        //                     IntConstant.v(1)),
        //                     insertPoint);
        //             units.insertBefore(Jimple.v().newReturnStmt(prefireReturnsLocal),
        //                     insertPoint);
        //             LocalSplitter.v().transform(body, phaseName + ".lns");
        //             LocalNameStandardizer.v().transform(body, phaseName + ".lns");
        //             TypeResolver.resolve(body, Scene.v());
        //         }
        {
            // populate the fire method
            SootMethod classMethod = modelClass.getMethodByName("fire");
            JimpleBody body = (JimpleBody) classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns",
                    BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);

            Local indexLocal = Jimple.v().newLocal("index", IntType.v());
            body.getLocals().add(indexLocal);

            Local tokenLocal = Jimple.v().newLocal("token",
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            // Update PortParameters.
            for (Iterator parameters = model.attributeList(PortParameter.class)
                     .iterator();
                 parameters.hasNext();) {
                PortParameter parameter = (PortParameter) parameters.next();
                String fieldName = ModelTransformer.getFieldNameForAttribute(parameter,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                RefType fieldType = (RefType) field.getType();
                Local parameterLocal = Jimple.v().newLocal("parameter",
                        fieldType);
                SootClass fieldClass = fieldType.getSootClass();

                body.getLocals().add(parameterLocal);

                // Get a reference to the port parameter.
                units.insertBefore(Jimple.v().newAssignStmt(parameterLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);

                // Invoke the update() method.
                units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                           .newVirtualInvokeExpr(parameterLocal,
                                                   fieldClass.getMethod(
                                                           PtolemyUtilities.portParameterUpdateMethod
                                                           .getSubSignature()))), insertPoint);
            }

            // FIXME: This is the quiescent point where parameters
            // reconfigured as a result of port parameters should be
            // evaluated.
            // Transfer Inputs from input ports.
            for (Iterator ports = model.inputPortList().iterator();
                 ports.hasNext();) {
                IOPort port = (IOPort) ports.next();

                if (port instanceof ParameterPort) {
                    continue;
                }

                int rate;
                rate = DFUtilities.getTokenConsumptionRate(port);

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

            Local localPostfireReturnsLocal = Jimple.v().newLocal("localPostfireReturns",
                    BooleanType.v());
            body.getLocals().add(localPostfireReturnsLocal);

            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       Jimple.v().newInstanceFieldRef(thisLocal,
                                               postfireReturnsField)), insertPoint);

            // Execute the schedule
            Iterator schedule = null;

            try {
                schedule = director.getScheduler().getSchedule().firingIterator();
            } catch (Exception ex) {
                throw new KernelRuntimeException(ex, "Failed to get schedule");
            }

            while (schedule.hasNext()) {
                Firing firing = (Firing) schedule.next();

                Entity entity = (Entity) firing.getActor();
                int firingCount = firing.getIterationCount();
                String fieldName = ModelTransformer.getFieldNameForEntity(entity,
                        model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className = ModelTransformer.getInstanceClassName(entity,
                        options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod actorPrefireMethod = SootUtilities
                    .searchForMethodByName(theClass, "prefire");
                SootMethod actorFireMethod = SootUtilities
                    .searchForMethodByName(theClass, "fire");
                SootMethod actorPostfireMethod = SootUtilities
                    .searchForMethodByName(theClass, "postfire");

                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                                           Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);

                // The threshold at which it is better to generate loops,
                // than to inline code.  A threshold of 2 means that loops will
                // always be used.
                // FIXME: This should be a command line option.
                int threshold = 2;

                if (firingCount < threshold) {
                    for (int i = 0; i < firingCount; i++) {
                        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                                   .newVirtualInvokeExpr(actorLocal,
                                                           actorPrefireMethod)),
                                insertPoint);
                        units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v()
                                                   .newVirtualInvokeExpr(actorLocal,
                                                           actorFireMethod)), insertPoint);
                        units.insertBefore(Jimple.v().newAssignStmt(localPostfireReturnsLocal,
                                                   Jimple.v().newVirtualInvokeExpr(actorLocal,
                                                           actorPostfireMethod)), insertPoint);
                        units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                                   Jimple.v().newAndExpr(postfireReturnsLocal,
                                                           localPostfireReturnsLocal)), insertPoint);
                    }
                } else {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(Jimple.v().newAssignStmt(indexLocal,
                                                IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                    bodyList.add(Jimple.v().newInvokeStmt(Jimple.v()
                                         .newVirtualInvokeExpr(actorLocal,
                                                 actorPrefireMethod)));
                    bodyList.add(Jimple.v().newInvokeStmt(Jimple.v()
                                         .newVirtualInvokeExpr(actorLocal,
                                                 actorFireMethod)));
                    bodyList.add(Jimple.v().newAssignStmt(localPostfireReturnsLocal,
                                         Jimple.v().newVirtualInvokeExpr(actorLocal,
                                                 actorPostfireMethod)));
                    bodyList.add(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                         Jimple.v().newAndExpr(postfireReturnsLocal,
                                                 localPostfireReturnsLocal)));

                    // Increment the index.
                    bodyList.add(Jimple.v().newAssignStmt(indexLocal,
                                         Jimple.v().newAddExpr(indexLocal, IntConstant.v(1))));

                    Expr conditionalExpr = Jimple.v().newLtExpr(indexLocal,
                            IntConstant.v(firingCount));

                    SootUtilities.createForLoopBefore(body, insertPoint,
                            initializerList, bodyList, conditionalExpr);
                }
            }

            // Transfer outputs from output ports
            for (Iterator ports = model.outputPortList().iterator();
                 ports.hasNext();) {
                IOPort port = (IOPort) ports.next();
                int rate;
                rate = DFUtilities.getTokenProductionRate(port);

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

                for (int i = 0; i < port.getWidthInside(); i++) {
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
            units.insertBefore(Jimple.v().newAssignStmt(Jimple.v()
                                       .newInstanceFieldRef(thisLocal,
                                               postfireReturnsField), postfireReturnsLocal),
                    insertPoint);

            //       units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //              insertPoint);
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
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       Jimple.v().newInstanceFieldRef(thisLocal,
                                               postfireReturnsField)), insertPoint);

            // If we need to keep track of the number of iterations, then...
            if (iterationLimit > 0) {
                Local iterationLocal = null;
                iterationLocal = Jimple.v().newLocal("iteration", IntType.v());
                body.getLocals().add(iterationLocal);

                // Get the current number of iterations
                units.insertBefore(Jimple.v().newAssignStmt(iterationLocal,
                                           Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                                                   iterationField)), insertPoint);

                // Increment the number of iterations.
                units.insertBefore(Jimple.v().newAssignStmt(iterationLocal,
                                           Jimple.v().newAddExpr(iterationLocal, IntConstant.v(1))),
                        insertPoint);

                // Save the current number of iterations
                units.insertBefore(Jimple.v().newAssignStmt(Jimple.v()
                                           .newInstanceFieldRef(body
                                                   .getThisLocal(), iterationField),
                                           iterationLocal), insertPoint);

                Stmt endStmt = Jimple.v().newNopStmt();

                // If the number of iterations is less than then
                // limit, then don't force postfire return to be
                // false.
                units.insertBefore(Jimple.v().newIfStmt(Jimple.v().newLtExpr(iterationLocal,
                                                                IntConstant.v(iterationLimit)), endStmt),
                        insertPoint);
                units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                           IntConstant.v(0)), // FALSE
                        insertPoint);
                units.insertBefore(endStmt, insertPoint);
            }

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

            //       units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //               insertPoint);
        }

        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());
    }
}
