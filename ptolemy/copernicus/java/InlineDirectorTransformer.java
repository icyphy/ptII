/* A transformer that inlines method calls on an SDF director.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.*;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.actor.IOPort;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.scalar.LocalSplitter;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeResolver;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// InlineDirectorTransformer
/**
A transformer that inlines an SDF director.  This transformer synthesizes
methods that properly implement the executable interface inside the class
representing the model.  The resulting class includes code to properly
initialize the instance classes for the actors and fire them in the
order of the SDF schedule.

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher
Hylands, Vishal Khandelwal
@version $Id$
@since Ptolemy II 2.0
*/
public class InlineDirectorTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlineDirectorTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlineDirectorTransformer v(CompositeActor model) {
        return new InlineDirectorTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlineDirectorTransformer.internalTransform("
                + phaseName + ", " + options + ")");
       
        SootClass modelClass = ModelTransformer.getModelClass();
        _inlineDirectorsIn(_model, modelClass, phaseName, options);        
    }

    private void _inlineDirectorsIn(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) {

        for (Iterator i = model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            if(entity instanceof CompositeActor) {
                String className = 
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass compositeClass = Scene.v().getSootClass(className);
                _inlineDirectorsIn((CompositeActor)entity, compositeClass,
                        phaseName, options);
            }
        }

        if(model.getDirector() instanceof SDFDirector) {
            _inlineSDFDirector(model, modelClass, phaseName, options);
        } else if(model.getDirector() instanceof HSDirector ||
                  model.getDirector() instanceof FSMDirector) {
            _inlineHSDirector(model, modelClass, phaseName, options);
        } else {
            throw new RuntimeException("Inlining a director can not "
                    + "be performed on a director of class " 
                    + model.getDirector().getClass().getName());
        }
        // First remove methods that are called on the director.
        // Loop over all the entity classes...
        for (Iterator i = model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass theClass =
                Scene.v().loadClassAndSupport(className);

            // Loop over all the methods...
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                // Loop over all the statements.
                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    if (!unit.containsInvokeExpr()) {
                        continue;
                    }
                    ValueBox box = (ValueBox)unit.getInvokeExprBox();
                    InvokeExpr r = (InvokeExpr)box.getValue();
                    if (r.getMethod().getSubSignature().equals(
                            PtolemyUtilities.invalidateResolvedTypesMethod
                            .getSubSignature())) {
                        // Remove calls to invalidateResolvedTypes()
                        body.getUnits().remove(unit);
                    }
                }
            }
        }
    }

    private void _inlineHSDirector(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) {
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
            SootMethod classMethod =
                modelClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod preinitializeMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "preinitialize");
                Local actorLocal = Jimple.v().newLocal("actor",
                        RefType.v(theClass));
                body.getLocals().add(actorLocal);
                // Get the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(actorLocal,
                                        preinitializeMethod)),
                        insertPoint);
            }
            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the initialize method
            SootMethod classMethod =
                modelClass.getMethodByName("initialize");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod initializeMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "initialize");
                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                initializeMethod)),
                        insertPoint);
            }
            //           units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the prefire method
            SootMethod classMethod =
                modelClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local prefireReturnsLocal = Jimple.v().newLocal("preReturns", BooleanType.v());
            body.getLocals().add(prefireReturnsLocal);

            // Prefire the controller.
            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            String fieldName = ModelTransformer.getFieldNameForEntity(
                    controller, model);
            SootField field = modelClass.getFieldByName(fieldName);
            String className =
                ActorTransformer.getInstanceClassName(controller, options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);
            SootMethod actorPrefireMethod =
                SootUtilities.searchForMethodByName(
                        theClass, "prefire");
            
            units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                    Jimple.v().newInstanceFieldRef(thisLocal, field)),
                    insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
                              Jimple.v().newVirtualInvokeExpr(actorLocal,
                                      actorPrefireMethod)),
                    insertPoint);
            
            units.insertBefore(Jimple.v().newReturnStmt(prefireReturnsLocal),
                    insertPoint);

            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the fire method
            SootMethod classMethod =
                modelClass.getMethodByName("fire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local indexLocal = Jimple.v().newLocal("index", IntType.v());
            body.getLocals().add(indexLocal);
            Local tokenLocal = Jimple.v().newLocal("token", 
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);
            
            // Transfer Inputs from input ports.
            for(Iterator ports = model.inputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                int rate = 1;
 
                String fieldName = ModelTransformer.getFieldNameForPort(
                        port, model);
                SootField field = modelClass.getFieldByName(fieldName);
                
                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);
                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(
                        Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tempPortLocal,
                                        PtolemyUtilities.ioportType)),
                        insertPoint);
            

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                    // Read
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    tokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.getMethod,
                                            IntConstant.v(i))));
                    // Write
                    bodyList.add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendInsideMethod,
                                            IntConstant.v(i),
                                            tokenLocal)));
                    // Increment the index.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    
                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(
                                indexLocal,
                                IntConstant.v(rate));
                    
                    SootUtilities.createForLoopBefore(body,
                            insertPoint,
                            initializerList,
                            bodyList,
                            conditionalExpr);
                }
            }

            {
                // Fire the controller.
                Local actorLocal = Jimple.v().newLocal("actor", actorType);
                body.getLocals().add(actorLocal);
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        controller, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(controller, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod actorFireMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "fire");
                
                units.insertBefore(
                        Jimple.v().newAssignStmt(actorLocal,
                                Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(actorLocal,
                                        actorFireMethod)),
                        insertPoint);
            }

            // Transfer outputs from output ports
            for(Iterator ports = model.outputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                int rate;
                try {
                    rate = SDFScheduler.getTokenProductionRate(port);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                String fieldName = ModelTransformer.getFieldNameForPort(
                        port, model);
                SootField field = modelClass.getFieldByName(fieldName);
                
                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);
                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(
                        Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tempPortLocal,
                                        PtolemyUtilities.ioportType)),
                        insertPoint);
            

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                 
                    // Read
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    tokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.getInsideMethod,
                                            IntConstant.v(i))));
                    // Write
                    bodyList.add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendMethod,
                                            IntConstant.v(i),
                                            tokenLocal)));
                    // Increment the index.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    
                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(
                                indexLocal,
                                IntConstant.v(rate));
                    
                    SootUtilities.createForLoopBefore(body,
                            insertPoint,
                            initializerList,
                            bodyList,
                            conditionalExpr);
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
            SootMethod classMethod =
                modelClass.getMethodByName("postfire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = 
                Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);
           
            // Postfire the controller.
            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            String fieldName = ModelTransformer.getFieldNameForEntity(
                    controller, model);
            SootField field = modelClass.getFieldByName(fieldName);
            String className =
                ActorTransformer.getInstanceClassName(controller, options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);
            SootMethod actorPostfireMethod =
                SootUtilities.searchForMethodByName(
                        theClass, "postfire");
            
            units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                    Jimple.v().newInstanceFieldRef(thisLocal, field)),
                    insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                              Jimple.v().newVirtualInvokeExpr(actorLocal,
                                      actorPostfireMethod)),
                    insertPoint);

            units.insertBefore(Jimple.v().newReturnStmt(postfireReturnsLocal),
                    insertPoint);
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the wrapup method
            SootMethod classMethod =
                modelClass.getMethodByName("wrapup");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod wrapupMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "wrapup");
                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                wrapupMethod)),
                        insertPoint);
            }
            //           units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //                   insertPoint);
        }
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());
    }

    private void _inlineSDFDirector(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) {
        System.out.println("Inlining director for " + model.getFullName());
        Type actorType = RefType.v(PtolemyUtilities.actorClass);

        SootField postfireReturnsField = new SootField("_postfireReturns", 
                BooleanType.v(), Modifier.PRIVATE);
        modelClass.addField(postfireReturnsField);

        // Inline the director
        {
            // populate the preinitialize method
            SootMethod classMethod =
                modelClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);
            
            // Initialize the postfire flag.
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       IntConstant.v(1)),
                    insertPoint);
            units.insertBefore(Jimple.v().newAssignStmt(
                              Jimple.v().newInstanceFieldRef(thisLocal, postfireReturnsField),
                              postfireReturnsLocal),
                    insertPoint);

            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod preinitializeMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "preinitialize");
                Local actorLocal = Jimple.v().newLocal("actor",
                        RefType.v(theClass));
                body.getLocals().add(actorLocal);
                // Get the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                preinitializeMethod)),
                        insertPoint);
            }
            //            units.insertBefore(Jimple.v().newReturnVoidStmt(),
                    //                   insertPoint);
        }

        {
            // populate the initialize method
            SootMethod classMethod =
                modelClass.getMethodByName("initialize");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod initializeMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "initialize");
                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                initializeMethod)),
                        insertPoint);
            }
            //            units.insertBefore(Jimple.v().newReturnVoidStmt(),insertPoint);
        }

        {
            // populate the postfire method
            SootMethod classMethod =
                modelClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local prefireReturnsLocal = Jimple.v().newLocal("preReturns", BooleanType.v());
            body.getLocals().add(prefireReturnsLocal);
            units.insertBefore(Jimple.v().newAssignStmt(prefireReturnsLocal,
                              IntConstant.v(1)),
                    insertPoint);
            units.insertBefore(Jimple.v().newReturnStmt(prefireReturnsLocal),
                    insertPoint);

            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the fire method
            SootMethod classMethod =
                modelClass.getMethodByName("fire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            
            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);

            Local indexLocal = Jimple.v().newLocal("index", IntType.v());
            body.getLocals().add(indexLocal);
            Local tokenLocal = Jimple.v().newLocal("token", 
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);
            
            // Transfer Inputs from input ports.
            for(Iterator ports = model.inputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                int rate;
                try {
                    rate = SDFScheduler.getTokenConsumptionRate(port);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                String fieldName = ModelTransformer.getFieldNameForPort(
                        port, model);
                SootField field = modelClass.getFieldByName(fieldName);
                
                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);
                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(
                        Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tempPortLocal,
                                        PtolemyUtilities.ioportType)),
                        insertPoint);
            

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                    // Read
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    tokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.getMethod,
                                            IntConstant.v(i))));
                    // Write
                    bodyList.add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendInsideMethod,
                                            IntConstant.v(i),
                                            tokenLocal)));
                    // Increment the index.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    
                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(
                                indexLocal,
                                IntConstant.v(rate));
                    
                    SootUtilities.createForLoopBefore(body,
                            insertPoint,
                            initializerList,
                            bodyList,
                            conditionalExpr);
                }
            }
  

            Local localPostfireReturnsLocal = 
                Jimple.v().newLocal("localPostfireReturns", BooleanType.v());
            body.getLocals().add(localPostfireReturnsLocal);

            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                              Jimple.v().newInstanceFieldRef(thisLocal, 
                                      postfireReturnsField)),
                    insertPoint);
         
            // Execute the schedule
            // Get the Schedule from the Scheduler             
            SDFDirector director = (SDFDirector)model.getDirector();
            Schedule ptLoopSchedule = null;
            try {
            ptLoopSchedule = director.getScheduler().getSchedule();
            }
            catch (Exception ex) {
                throw new KernelRuntimeException(ex,
                        "Failed to get schedule");
            }

            //Calling the loophandler function to execute the schedule
            
            _nestedLoopHandler(ptLoopSchedule, body, body, model, modelClass,
                    phaseName, options, actorLocal, postfireReturnsLocal,
                    indexLocal, localPostfireReturnsLocal, thisLocal,
                    insertPoint);

            // Transfer outputs from output ports
            for(Iterator ports = model.outputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                int rate;
                try {
                    rate = SDFScheduler.getTokenProductionRate(port);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                String fieldName = ModelTransformer.getFieldNameForPort(
                        port, model);
                SootField field = modelClass.getFieldByName(fieldName);
                
                // Get a reference to the port.
                Local portLocal = Jimple.v().newLocal("port",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(portLocal);
                Local tempPortLocal = Jimple.v().newLocal("tempPort",
                        PtolemyUtilities.ioportType);
                body.getLocals().add(tempPortLocal);
                units.insertBefore(
                        Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, field)),
                        insertPoint);
                units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tempPortLocal,
                                        PtolemyUtilities.ioportType)),
                        insertPoint);
            

                for (int i = 0; i < port.getWidth(); i++) {
                    // The list of initializer instructions.
                    List initializerList = new LinkedList();
                    initializerList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    IntConstant.v(0)));

                    // The list of body instructions.
                    List bodyList = new LinkedList();
                 
                    // Read
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    tokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.getInsideMethod,
                                            IntConstant.v(i))));
                    // Write
                    bodyList.add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendMethod,
                                            IntConstant.v(i),
                                            tokenLocal)));
                    // Increment the index.
                    bodyList.add(
                            Jimple.v().newAssignStmt(
                                    indexLocal,
                                    Jimple.v().newAddExpr(
                                            indexLocal,
                                            IntConstant.v(1))));
                    
                    Expr conditionalExpr =
                        Jimple.v().newLtExpr(
                                indexLocal,
                                IntConstant.v(rate));
                    
                    SootUtilities.createForLoopBefore(body,
                            insertPoint,
                            initializerList,
                            bodyList,
                            conditionalExpr);
                }
            }

            // Return.
            units.insertBefore(Jimple.v().newAssignStmt(
                              Jimple.v().newInstanceFieldRef(thisLocal, 
                                      postfireReturnsField),
                              postfireReturnsLocal),
                    insertPoint);
            //       units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //              insertPoint);
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the postfire method
            SootMethod classMethod =
                modelClass.getMethodByName("postfire");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal =
                Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);
            units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                              Jimple.v().newInstanceFieldRef(thisLocal, 
                                      postfireReturnsField)),
                    insertPoint);
            units.insertBefore(Jimple.v().newReturnStmt(postfireReturnsLocal),
                    insertPoint);
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the wrapup method
            SootMethod classMethod =
                modelClass.getMethodByName("wrapup");
            JimpleBody body = (JimpleBody)classMethod.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for (Iterator entities = model.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
                SootMethod wrapupMethod =
                    SootUtilities.searchForMethodByName(
                            theClass, "wrapup");
                // Set the field.
                units.insertBefore(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)),
                        insertPoint);
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                wrapupMethod)),
                        insertPoint);
            }
            //       units.insertBefore(Jimple.v().newReturnVoidStmt(),
            //               insertPoint);
        }
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());
    }
    /* Returns the sootbody of the fire method after executing the entire
     * schedule for the SDF model. 
     * The SDF Model may now have any schedule (flat or looped) which is
     * passed to this method for execution. The schedule can now be viewed
     * as a tree where each node can be either a schedule instance or a
     * firing instance. By falling through the schedule tree recursively,
     * we create local JimpleBody at each schedule level and in a bottom-up
     * fashion we add this body to the local body of the upper schedule
     * level in the tree. This way once we execute the full schedule tree
     * we have the localbody at the root being the body of the fire method
     * of the model with the schedule executed with the appropriate
     * looping.
     */
    private JimpleBody _nestedLoopHandler(Schedule ptSchedule, JimpleBody body,
            JimpleBody mainBody, CompositeActor model,
            SootClass modelClass, String phaseName, Map options,
            Local actorLocal, Local postfireReturnsLocal,
            Local indexLocal, Local localPostfireReturnsLocal,
            Local thisLocal, Stmt insertPoint) {

            Iterator schedule = null;
            int count = ptSchedule.getIterationCount();
            //set up a local body for every level of the nested schedule
            JimpleBody localBody = Jimple.v().newBody();
            //Keep a track of the current level in the schedule tree
            scheduleLevel = scheduleLevel + 1;

            try {
                schedule =  ptSchedule.iterator();
            } catch (Exception ex) {
                throw new KernelRuntimeException(ex,
                        "Failed to get schedule");
            }
            while (schedule.hasNext()) {
                ScheduleElement ptScheduleElement = (ScheduleElement)schedule
                        .next();
                // recursive call to _nestedLoopHandler() if we have
                // schedelement being a schedule itself
                if (ptScheduleElement instanceof Schedule) {
                    localBody = _nestedLoopHandler((Schedule)ptScheduleElement,
                            body, localBody, model, modelClass,
                            phaseName, options, actorLocal,
                            postfireReturnsLocal, indexLocal,
                            localPostfireReturnsLocal,
                            thisLocal, insertPoint);
                    scheduleLevel = scheduleLevel - 1;
                } else {
                 // Incase schedelement is a firing instance
                    Chain units = localBody.getUnits();
                    Firing firing = (Firing)ptScheduleElement;
                    Entity entity = (Entity)firing.getActor();

                    int firingCount = firing.getIterationCount();
                    System.out.println("firingcount"+firingCount);
                    String fieldName = ModelTransformer
                            .getFieldNameForEntity(entity, model);
                    SootField field = modelClass.getFieldByName(fieldName);
                    String className = ActorTransformer
                            .getInstanceClassName(entity, options);
                    SootClass theClass = Scene.v()
                            .loadClassAndSupport(className);
                    SootMethod actorPrefireMethod =
                            SootUtilities.searchForMethodByName(
                                    theClass, "prefire");
                    SootMethod actorFireMethod =
                            SootUtilities.searchForMethodByName(
                                    theClass, "fire");
                    SootMethod actorPostfireMethod =
                            SootUtilities.searchForMethodByName(
                                    theClass, "postfire");

                     // Set the field.
                    units.add(Jimple.v().newAssignStmt(actorLocal,
                            Jimple.v().newInstanceFieldRef(thisLocal, field)));
                    System.out.println("fieldvalue assign:");
                    System.out.println(Jimple.v().newAssignStmt(actorLocal,
                            Jimple.v().newInstanceFieldRef(thisLocal, field)));

                    // The threshold at which it is better to generate loops,
                    // than to inline code.  A threshold of 2 means that loops
                    // willalways be used.
                    // FIXME: This should be a command line option.
                    int threshold = 2;

                    if(firingCount < threshold) {
                        for(int i = 0; i < firingCount; i++) {
                            units.add(Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(actorLocal,
                                            actorPrefireMethod)));
                            units.add(Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(actorLocal,
                                            actorFireMethod)));
                            units.add(Jimple.v().newAssignStmt(
                                    localPostfireReturnsLocal,
                                    Jimple.v().newVirtualInvokeExpr(actorLocal,
                                            actorPostfireMethod)));
                            units.add(Jimple.v().newAssignStmt(
                                    postfireReturnsLocal,
                                    Jimple.v().newAndExpr(postfireReturnsLocal,
                                            localPostfireReturnsLocal)));
                        }
                    } else {
                        // The list of initializer instructions.
                        List initializerList = new LinkedList();

                        initializerList.add(
                                Jimple.v().newAssignStmt(
                                        indexLocal,
                                        IntConstant.v(0)));

                        // The list of body instructions.
                        List bodyList = new LinkedList();
                        bodyList.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(actorLocal,
                                        actorPrefireMethod)));
                        bodyList.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(actorLocal,
                                        actorFireMethod)));
                        bodyList.add(Jimple.v().newAssignStmt(
                                localPostfireReturnsLocal,
                                Jimple.v().newVirtualInvokeExpr(actorLocal,
                                        actorPostfireMethod)));
                        bodyList.add(Jimple.v().newAssignStmt(
                                postfireReturnsLocal,
                                Jimple.v().newAndExpr(postfireReturnsLocal,
                                        localPostfireReturnsLocal)));
                        // Increment the index.
                        bodyList.add(Jimple.v().newAssignStmt(
                                indexLocal, Jimple.v().newAddExpr(
                                        indexLocal, IntConstant.v(1))));

                        Expr conditionalExpr = Jimple.v().newLtExpr(
                                indexLocal, IntConstant.v(firingCount));

                        Stmt stmt = Jimple.v().newNopStmt();
                        units.add(stmt);
                        SootUtilities.createForLoopBefore(localBody,
                                stmt, initializerList,
                                bodyList, conditionalExpr);
                    }
                }
            }
            //Add the localBody to the mainBody once a level of the
            //schedule tree has been completed
            if (count > 1) {
                //in this case a loop needs to be added at this level of
                //the schedule tree
                Stmt stmtNop = Jimple.v().newNopStmt();
                Chain mainUnits = mainBody.getUnits();
                mainUnits.add(stmtNop);
                // Create a new index using indexCount
                indexCount = indexCount + 1;
                String newIndex = "index" + Integer.toString(indexCount);
                Local indexCountLocal =
                            Jimple.v().newLocal(newIndex, IntType.v());
                body.getLocals().add(indexCountLocal);
                //Create a List representation of the localBody for this level
                List localBodyList = new LinkedList(
                        localBody.getUnits());
                // The list of initializer instructions.
                List newInitializerList = new LinkedList();
                newInitializerList.add(
                        Jimple.v().newAssignStmt(
                                indexCountLocal,
                                IntConstant.v(0)));
                // Increment the index.
                localBodyList.add(Jimple.v().newAssignStmt(
                        indexCountLocal,
                        Jimple.v().newAddExpr(
                                indexCountLocal,
                                IntConstant.v(1))));

                Expr newConditionalExpr =
                        Jimple.v().newLtExpr(
                                indexCountLocal,
                                IntConstant.v(count));

                //Create the for loop using the newconditional Expr, stmtnop,
                //newInitializerList, localBodyList
                Stmt bodyStart = (Stmt)localBodyList.get(0);

                Stmt conditionalStmt = Jimple.v().newIfStmt(
                        newConditionalExpr, bodyStart);

                mainBody.getUnits().insertAfter(
                        conditionalStmt, stmtNop);

                mainBody.getUnits().insertAfter(
                        localBodyList, stmtNop);

                mainBody.getUnits().insertAfter(
                        Jimple.v().newGotoStmt(conditionalStmt),
                                stmtNop);

                mainBody.getUnits().insertAfter(
                        newInitializerList, stmtNop);
                
            } else if (scheduleLevel > 1) {
                // We are not yet at the root of the schedule tree 
                Stmt stmtNop = Jimple.v().newNopStmt();
                Chain mainUnits = mainBody.getUnits();
                mainUnits.add(stmtNop);
                List localBodyList = new LinkedList(localBody.getUnits());
                mainUnits.insertAfter(localBodyList, stmtNop);
            } else {
                // We are now at the root of the schedule tree and this is
                // the final merging of the localbody of the level to the 
                // body of the fire method of the SDF model. The entire
                // schedule has now been executed
                Chain mainUnits = mainBody.getUnits();
                Stmt stmentNop = Jimple.v().newNopStmt();
                mainUnits.insertBefore(stmentNop, insertPoint);
                List localBodyList = new LinkedList(localBody.getUnits());
                mainUnits.insertAfter(localBodyList, stmentNop);
            }
            return mainBody;
    }

    private CompositeActor _model;
    private static int indexCount = 0;
    private int scheduleLevel = 0;
}














