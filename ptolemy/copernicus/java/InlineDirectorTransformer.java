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
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelRuntimeException;

import soot.Body;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.BooleanType;
import soot.NullType;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.scalar.LocalSplitter;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeResolver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// InlineDirectorTransformer
/**
A transformer that inlines an SDF director.  This transformer synthesizes
methods that properly implement the executable interface inside the class
representing the model.  The resulting class includes code to properly
initialize the instance classes for the actors and fire them in the
order of the SDF schedule.

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
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

       
        // First remove methods that are called on the director.
        // Loop over all the entity classes...
        for (Iterator i = _model.deepEntityList().iterator();
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
        } else {
            throw new RuntimeException("Inlining a director can not "
                    + "be performed on a director of class " 
                    + _model.getDirector().getClass().getName());
        }
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
            SootMethod classMethod = new SootMethod("preinitialize",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            //DavaBody body = Dava.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);
            
            // Initialize the postfire flag.
            units.add(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                       IntConstant.v(1)));
            units.add(Jimple.v().newAssignStmt(
                              Jimple.v().newInstanceFieldRef(thisLocal, postfireReturnsField),
                              postfireReturnsLocal));

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
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                preinitializeMethod)));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the initialize method
            SootMethod classMethod = new SootMethod("initialize",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
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
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                initializeMethod)));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the fire method
            SootMethod classMethod = new SootMethod("fire",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            
            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);


            // FIXME: Transfer Inputs!
            

            Local localPostfireReturnsLocal = Jimple.v().newLocal("localPostfireReturns", BooleanType.v());
            body.getLocals().add(localPostfireReturnsLocal);

            units.add(Jimple.v().newAssignStmt(postfireReturnsLocal,
                              Jimple.v().newInstanceFieldRef(thisLocal, 
                                      postfireReturnsField)));
         
            // Execute the schedule
            SDFDirector director = (SDFDirector)model.getDirector();
            Iterator schedule = null;
            try {
                schedule =
                    director.getScheduler().getSchedule().actorIterator();
            } catch (Exception ex) {
                throw new KernelRuntimeException(ex,
                        "Failed to get schedule");
            }
            while (schedule.hasNext()) {
                Entity entity = (Entity)schedule.next();
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, model);
                SootField field = modelClass.getFieldByName(fieldName);
                String className =
                    ActorTransformer.getInstanceClassName(entity, options);
                SootClass theClass = Scene.v().loadClassAndSupport(className);
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
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorPrefireMethod)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorFireMethod)));
                units.add(Jimple.v().newAssignStmt(localPostfireReturnsLocal,
                                  Jimple.v().newVirtualInvokeExpr(actorLocal,
                                          actorPostfireMethod)));
                units.add(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                  Jimple.v().newAndExpr(postfireReturnsLocal,
                                          localPostfireReturnsLocal)));
            }

            // FIXME: Transfer Outputs!

            units.add(Jimple.v().newAssignStmt(
                              Jimple.v().newInstanceFieldRef(thisLocal, 
                                      postfireReturnsField),
                              postfireReturnsLocal));
            units.add(Jimple.v().newReturnVoidStmt());
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the postfire method
            SootMethod classMethod = new SootMethod("postfire",
                    new LinkedList(), BooleanType.v(),
                    Modifier.PUBLIC);
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local postfireReturnsLocal = Jimple.v().newLocal("postfireReturns", BooleanType.v());
            body.getLocals().add(postfireReturnsLocal);
                units.add(Jimple.v().newAssignStmt(postfireReturnsLocal,
                                  Jimple.v().newInstanceFieldRef(thisLocal, 
                                          postfireReturnsField)));
            units.add(Jimple.v().newReturnStmt(postfireReturnsLocal));
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        {
            // populate the wrapup method
            SootMethod classMethod = new SootMethod("wrapup",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
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
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                wrapupMethod)));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }
        Scene.v().setActiveHierarchy(new Hierarchy());
    }
    private CompositeActor _model;
}














