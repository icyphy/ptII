/* A transformer that adds the command-line interface.

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

package ptolemy.copernicus.java;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Entity;

import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
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
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.util.Chain;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/*
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;
*/


/**
A transformer that adds the command-line interface.
@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
*/
public class CommandLineTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private CommandLineTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static CommandLineTransformer v(CompositeActor model) {
        return new CommandLineTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " deep targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        SootClass actorClass =  Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedAtomicActor");
        Type actorType = RefType.v(actorClass);
        SootClass compositeActorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");
        SootClass applicationClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.gui.CompositeActorApplication");
        applicationClass.setLibraryClass();

        SootClass modelClass = Scene.v().getMainClass();

        SootClass mainClass = SootUtilities.copyClass(applicationClass,
                Options.getString(options, "targetPackage") + ".Main");
        mainClass.setApplicationClass();
        /*
          {
          LinkedList mainArgumentTypes = new LinkedList();
          mainArgumentTypes.add(ArrayType.v(RefType.v("java.lang.String"),
          1));
          SootMethod mainMethod = new SootMethod("main",
          mainArgumentTypes, VoidType.v(),
          Modifier.PUBLIC | Modifier.STATIC);
          modelClass.addMethod(mainMethod);
          JimpleBody body = Jimple.v().newBody(mainMethod);
          mainMethod.setActiveBody(body);
          body.insertIdentityStmts();
          Chain units = body.getUnits();

          Local modelLocal = Jimple.v().newLocal("model",
          RefType.v(modelClass));
          body.getLocals().add(modelLocal);


          // create the model
          units.add(Jimple.v().newAssignStmt(modelLocal,
          Jimple.v().newNewExpr(RefType.v(modelClass))));

          // the arguments
          List args = new LinkedList();

          // call the constructor on the object.
          SootMethod constructor =
          SootUtilities.getMatchingMethod(modelClass, "<init>", args);
          units.add(Jimple.v().newInvokeStmt(
          Jimple.v().newSpecialInvokeExpr(modelLocal,
          constructor, args)));

          //FIXME make parameter.
          int iterationLimit = 50;

          Local iterationLocal = null;
          if(iterationLimit > 1) {
          iterationLocal = Jimple.v().newLocal("iteration",
          IntType.v());
          body.getLocals().add(iterationLocal);
          units.add(Jimple.v().newAssignStmt(iterationLocal,
          IntConstant.v(0)));
          }
          // call preinitialize
          units.add(Jimple.v().newInvokeStmt(
          Jimple.v().newVirtualInvokeExpr(modelLocal,
          SootUtilities.searchForMethodByName(modelClass,
          "preinitialize"))));

          // call initialize on the model
          units.add(Jimple.v().newInvokeStmt(
          Jimple.v().newVirtualInvokeExpr(modelLocal,
          SootUtilities.searchForMethodByName(modelClass,
          "initialize"))));

          // A jump point for the start of the iteration.
          Stmt iterationStartStmt = Jimple.v().newNopStmt();
          // A jump point for the end of the iteration.
          // we don't actually add this until later in the sequence.
          Stmt iterationEndStmt = Jimple.v().newNopStmt();

          units.add(iterationStartStmt);

          // call fire on the model
          units.add(Jimple.v().newInvokeStmt(
          Jimple.v().newVirtualInvokeExpr(modelLocal,
          SootUtilities.searchForMethodByName(modelClass,
          "fire"))));

          // If we need to keep track of the number of iterations, then...
          if(iterationLimit > 1) {
          // Increment the number of iterations.
          units.add(Jimple.v().newAssignStmt(iterationLocal,
          Jimple.v().newAddExpr(iterationLocal,
          IntConstant.v(1))));
          // If the number of iterations is greater than the limit,
          // then we're done.
          units.add(Jimple.v().newIfStmt(
          Jimple.v().newGtExpr(iterationLocal,
          IntConstant.v(iterationLimit)),
          iterationEndStmt));
          }
          if(iterationLimit != 1) {
          units.add(Jimple.v().newGotoStmt(iterationStartStmt));
          }

          // add the jump point for the end of the iteration
          units.add(iterationEndStmt);

          // call wrapup on the model
          units.add(Jimple.v().newInvokeStmt(
          Jimple.v().newVirtualInvokeExpr(modelLocal,
          SootUtilities.searchForMethodByName(modelClass,
          "wrapup"))));
          // Return from the main method.
          units.add(Jimple.v().newReturnVoidStmt());
          }*/

        // Inline the director
        {
            // populate the preinitialize method
            SootMethod classMethod = new SootMethod("preinitialize",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            SootMethod actorMethod =
                SootUtilities.searchForMethodByName(actorClass,
                        classMethod.getName());
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            //DavaBody body = Dava.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor",
                    actorType);
            body.getLocals().add(actorLocal);
            for(Iterator entities = _model.entityList().iterator();
                entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                SootField field = modelClass.getFieldByName(entity.getName());
                // Get the field.
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorMethod)));
            }
            /*
              for(Iterator entities = _model.entityList().iterator();
              entities.hasNext();) {
              Entity entity = (Entity)entities.next();
              for(Iterator ports = entity.portList().iterator();
              ports.hasNext();) {
              Port port = (Port)ports.hasNext();
              if(port instanceof TypedIOPort) {
              TypedIOPort typedPort = (TypedIOPort)port;
              // set the type of the port.
              SootMethod method =
              SootUtilities.searchForMethodByName(portClass,
              "setTypeEquals");
              Local typeLocal =
              // build a constant type.
              _buildConstantTypeLocal(body, typedPort.getType());

              units.add(Jimple.v().newInvokeStmt(
              Jimple.v().newVirtualInvokeExpr(portLocal,
              method, typeLocal)));
              }
              }

              }*/
            units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the initialize method
            SootMethod classMethod = new SootMethod("initialize",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            SootMethod actorMethod =
                SootUtilities.searchForMethodByName(actorClass,
                        classMethod.getName());
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for(Iterator entities = _model.entityList().iterator();
                entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                SootField field = modelClass.getFieldByName(entity.getName());
                // Set the field.
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorMethod)));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the fire method
            SootMethod classMethod = new SootMethod("fire",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            SootMethod actorPrefireMethod =
                SootUtilities.searchForMethodByName(actorClass, "prefire");
            SootMethod actorFireMethod =
                SootUtilities.searchForMethodByName(actorClass, "fire");
            SootMethod actorPostfireMethod =
                SootUtilities.searchForMethodByName(actorClass, "postfire");
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            // Execute the schedule
            SDFDirector director = (SDFDirector)_model.getDirector();
            Iterator schedule = null;
            try {
                schedule =
                    director.getScheduler().getSchedule().actorIterator();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException();
            }
            while(schedule.hasNext()) {
                Entity entity = (Entity)schedule.next();
                SootField field = modelClass.getFieldByName(entity.getName());
                // Set the field.
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorPrefireMethod)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorFireMethod)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorPostfireMethod)));

            }
            units.add(Jimple.v().newReturnVoidStmt());
        }

        {
            // populate the wrapup method
            SootMethod classMethod = new SootMethod("wrapup",
                    new LinkedList(), VoidType.v(),
                    Modifier.PUBLIC);
            SootMethod actorMethod =
                SootUtilities.searchForMethodByName(actorClass,
                        classMethod.getName());
            modelClass.addMethod(classMethod);
            JimpleBody body = Jimple.v().newBody(classMethod);
            classMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local actorLocal = Jimple.v().newLocal("actor", actorType);
            body.getLocals().add(actorLocal);
            for(Iterator entities = _model.entityList().iterator();
                entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                SootField field = modelClass.getFieldByName(entity.getName());
                // Set the field.
                units.add(Jimple.v().newAssignStmt(actorLocal,
                        Jimple.v().newInstanceFieldRef(thisLocal, field)));
                units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(actorLocal,
                                actorMethod)));
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }
        Scene.v().setActiveHierarchy(new Hierarchy());

        // Optimizations.
        // We know that we will never parse classes, so throw away that code.
        SootField field = mainClass.getFieldByName("_expectingClass");
        assertFinalField(mainClass, field, IntConstant.v(0));

        // We know that we have exactly one model, so create it.
        // The final field for the model.
        SootField modelField =
            new SootField("_CGmodel", RefType.v(compositeActorClass),
                    Modifier.PRIVATE | Modifier.FINAL);
        mainClass.addField(modelField);


        // initialize the field by creating a model in all the <init> methods.
        for(Iterator methods = mainClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            // ignore things that aren't initializers.
            if(!method.getName().equals("<init>"))
                continue;

            System.out.println("method = " + method);
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Chain units = body.getUnits();
            Stmt insertPoint = (Stmt)units.getLast();
            Local modelLocal = Jimple.v().newLocal("_CGTemp" +
                    modelField.getName(), modelField.getType());

            body.getLocals().add(modelLocal);
            units.insertBefore(Jimple.v().newAssignStmt(modelLocal,
                    Jimple.v().newNewExpr(RefType.v(modelClass))),
                    insertPoint);

            // the arguments
            List args = new LinkedList();
            SootMethod constructor =
                SootUtilities.getMatchingMethod(modelClass, "<init>", args);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(modelLocal,
                            constructor, args)), insertPoint);

            FieldRef fieldRef =
                Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                        modelField);
            units.insertBefore(Jimple.v().newAssignStmt(fieldRef, modelLocal),
                    insertPoint);
        }

        LinkedList modelList = new LinkedList();
        modelList.add(modelField);
        SootField modelsField = mainClass.getFieldByName("_models");
        unrollIteratorInstances(mainClass, modelsField, modelList);

    }

    /** Anywhere where the iterator of the given field is referenced
     *  in the given class, unroll the iterator as if it contained the
     *  objects referenced by the given fields.
     */
    public void unrollIteratorInstances(SootClass theClass, SootField field,
            List fieldList) {
        SootClass iteratorClass = Scene.v().getSootClass("java.util.Iterator");
        SootMethod iteratorNextMethod =
            iteratorClass.getMethod("java.lang.Object next()");
        SootMethod iteratorHasNextMethod =
            iteratorClass.getMethod("boolean hasNext()");
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            BlockGraph graph = new CompleteBlockGraph(body);
            for(Iterator blocks = graph.iterator();
                blocks.hasNext();) {
                Block block = (Block)blocks.next();
                System.out.println("body = " + block);
                // filter out anything that doesn't look like a loop body.
                if ((block.getPreds().size() != 1) ||
                        (block.getSuccs().size() != 1)) {
                    continue;
                }
                // filter out anything that isn't attached to something
                // that looks like a conditional jump.
                Block whileCond = (Block)block.getSuccs().get(0);
                System.out.println("cond = " + whileCond);
                if(whileCond != block.getPreds().get(0) ||
                        whileCond.getPreds().size() != 2 ||
                        whileCond.getSuccs().size() != 2) {
                    continue;
                }

                // filter out anything that doesn't start with a call
                // to hasNext().
                if(!(whileCond.getHead() instanceof DefinitionStmt)) {
                    continue;
                }
                DefinitionStmt stmt =
                    (DefinitionStmt)whileCond.getHead();
                if(!(stmt.getRightOp() instanceof InterfaceInvokeExpr)) {
                    continue;
                }
                InterfaceInvokeExpr expr =
                    (InterfaceInvokeExpr)stmt.getRightOp();
                if(expr.getMethod() != iteratorHasNextMethod) {
                    continue;
                }
                // At this point we know we have a while(hasNext()) loop.
                // Now go check for iterator is defined...  it should be just
                // above

                Local iteratorLocal = (Local)expr.getBase();
                Block whilePredecessor = (Block)whileCond.getPreds().get(0);
                if(whilePredecessor == block) {
                    whilePredecessor = (Block)whileCond.getPreds().get(1);
                }

                System.out.println("whilePredecessor = " + whilePredecessor);
                Unit unit = whilePredecessor.getTail();
                boolean found = false;
                // walk backwards until we find a definition of the iterator.
                while(unit != whilePredecessor.getHead() && !found) {
                    if(unit instanceof DefinitionStmt &&
                            ((DefinitionStmt)unit).getLeftOp()
                            .equals(iteratorLocal)) {
                        found = true;
                    } else {
                        unit = whilePredecessor.getPredOf(unit);
                    }
                }

                System.out.println("iterator def = " + unit);
                DefinitionStmt iteratorDefinition = ((DefinitionStmt)unit);

                if(!(iteratorDefinition.getRightOp()
                        instanceof InterfaceInvokeExpr) ||
                        !((InterfaceInvokeExpr)iteratorDefinition
                                .getRightOp()).getMethod().getName()
                        .equals("iterator")) {
                    continue;
                }
                Local collectionLocal =
                    (Local) ((InterfaceInvokeExpr)iteratorDefinition
                            .getRightOp()).getBase();
                System.out.println("collection Local = " + collectionLocal);
                found = false;

                // Walk backward again until we reach the definition
                // of the collection.
                while(unit != whilePredecessor.getHead() && !found) {
                    if(unit instanceof DefinitionStmt &&
                            ((DefinitionStmt)unit).getLeftOp()
                            .equals(collectionLocal)) {
                        found = true;
                    } else {
                        unit = whilePredecessor.getPredOf(unit);
                    }
                }
                System.out.println("collection def = " + unit);
                System.out.println("field = " + field);
                DefinitionStmt collectionDefinition = ((DefinitionStmt)unit);
                if(!(collectionDefinition.getRightOp() instanceof FieldRef) ||
                        ((FieldRef)collectionDefinition.getRightOp())
                        .getField() != field) {
                    continue;
                }
                // FINALLY we know we've found something we can unroll... :)
                System.out.println("is unrollable...");

                // There should be a jump from the predecessor to the
                // condition.  Redirect this jump to the body.

                whileCond.getHead().redirectJumpsToThisTo(block.getHead());

                Local thisLocal = body.getThisLocal();
                Chain units = body.getUnits();
                List blockStmtList = new LinkedList();
                // insert after the main block
                Unit insertPoint = (Unit)units.getSuccOf(block.getTail());
                for(Iterator blockStmts = block.iterator();
                    blockStmts.hasNext();) {
                    Stmt original = (Stmt)blockStmts.next();
                    blockStmtList.add(original);
                    blockStmts.remove();
                }

                // Loop through and unroll the loop body.
                for(Iterator fields = fieldList.iterator();
                    fields.hasNext();) {
                    SootField insertField = (SootField)fields.next();
                    for(Iterator blockStmts = blockStmtList.iterator();
                        blockStmts.hasNext();) {
                        Stmt original = (Stmt)blockStmts.next();
                        Stmt clone = (Stmt)original.clone();
                        for(Iterator boxes = clone.getUseBoxes().iterator();
                            boxes.hasNext();) {
                            ValueBox box = (ValueBox)boxes.next();
                            Value value = box.getValue();
                            if(value instanceof InvokeExpr) {
                                InvokeExpr r = (InvokeExpr)value;
                                if(r.getMethod() == iteratorNextMethod) {
                                    box.setValue(Jimple.v()
                                            .newInstanceFieldRef(thisLocal,
                                                    insertField));
                                }
                            }
                        }
                        units.insertBefore(clone, insertPoint);
                    }
                }

                // remove the conditional
                for(Iterator blockStmts = whileCond.iterator();
                    blockStmts.hasNext();) {
                    Stmt original = (Stmt)blockStmts.next();
                    blockStmts.remove();
                }


                // Find while loops.
                // This code modified from WhileMatcher.

                /*
                  List successorList = block.getSuccs();

                  if(successorList.size() == 2) {
                  Block whileBody, whileSucc;
                  boolean found = false;

                  whileBody = whileSucc = block;
                  whileBody = (Block) successorList.get(0);
                  whileSucc = (Block) successorList.get(1);

                  if ((whileBody.getPreds().size() == 1) &&
                  (whileBody.getSuccs().size() == 1) &&
                  (whileBody.getSuccs().get(0) == block))
                  found = true;
                  if(!found) {
                  Block bt;
                  bt = whileSucc;
                  whileSucc = whileBody;
                  whileBody = bt;

                  if ((whileBody.getPreds().size() == 1) &&
                  (whileBody.getSuccs().size() == 1) &&
                  (whileBody.getSuccs().get(0) == block))
                  found = true;
                  }

                  if(found) {
                  if(con
                  System.out.println("found while Loop:");
                  System.out.println("body = " + whileBody);
                  System.out.println("cond = " + block);
                  System.out.println("successor = " + whileSucc);
                  }
                  }      */
            }
        }
    }

    /** Make the given field final.  Anywhere where the the given
     *  field is used in the given class, inline the reference with
     *  the given value.  Anywhere where the given field is illegally
     *  defined in the given class, inline the definition to throw a
     *  new exception.  This happens unless the given class is the
     *  defining class for the given field and the definition occurs
     *  within an initializer (for instance fields) or a static
     *  initializer (for static fields).  Note that this is rather
     *  limited, since it is only really useful for constant values.
     *  In would be nice to specify a more complex expression to
     *  inline, but I'm not sure how to do it.
     */
    public void assertFinalField(SootClass theClass,
            SootField theField, Value newValue) {
        // First make the field final.
        theField.setModifiers(theField.getModifiers() | Modifier.FINAL);

        // Find any assignment to the field in the class and convert
        // them to Exceptions, unless they are in constructors,
        // in which case remove them.
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Chain units = body.getUnits();

            for(Iterator stmts = units.snapshotIterator();
                stmts.hasNext();) {
                Stmt stmt = (Stmt)stmts.next();
                // Remove all the definitions.
                for(Iterator boxes = stmt.getDefBoxes().iterator();
                    boxes.hasNext();) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if(value instanceof FieldRef) {
                        FieldRef ref = (FieldRef)value;
                        if(ref.getField() == theField) {
                            System.out.println("removing stmt = " + stmt);
                            units.remove(stmt);
                        }
                    }
                }
                // Inline all the uses.
                if(Evaluator.isValueConstantValued(newValue)) {
                    for(Iterator boxes = stmt.getUseBoxes().iterator();
                        boxes.hasNext();) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof FieldRef) {
                            FieldRef ref = (FieldRef)value;
                            if(ref.getField() == theField) {
                                System.out.println("inlining stmt = " + stmt);

                                box.setValue(Evaluator
                                        .getConstantValueOf(newValue));
                            }
                        }

                    }
                }
            }
        }
        if(Modifier.isStatic(theField.getModifiers())) {
            SootMethod method;
            // create a class initializer if one does not already exist.
            if(theClass.declaresMethodByName("<clinit>")) {
                method = theClass.getMethodByName("<clinit>");
            } else {
                method = new SootMethod("<clinit>", new LinkedList(),
                        NullType.v(), Modifier.PUBLIC);
                theClass.getMethods().add(method);
            }
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Chain units = body.getUnits();
            Stmt insertPoint = (Stmt)units.getLast();
            Local local = Jimple.v().newLocal("_CGTemp" + theField.getName(),
                    theField.getType());
            body.getLocals().add(local);
            units.insertBefore(Jimple.v().newAssignStmt(local, newValue),
                    insertPoint);
            FieldRef fieldRef = Jimple.v().newStaticFieldRef(theField);
            units.insertBefore(Jimple.v().newAssignStmt(fieldRef, local),
                    insertPoint);
        } else {
            for(Iterator methods = theClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                // ignore things that aren't initializers.
                if(!method.getName().equals("<init>"))
                    continue;

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                Chain units = body.getUnits();
                Stmt insertPoint = (Stmt)units.getLast();
                Local local = Jimple.v().newLocal("_CGTemp" +
                        theField.getName(),
                        theField.getType());
                body.getLocals().add(local);
                units.insertBefore(Jimple.v().newAssignStmt(local, newValue),
                        insertPoint);
                FieldRef fieldRef =
                    Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                            theField);
                units.insertBefore(Jimple.v().newAssignStmt(fieldRef, local),
                        insertPoint);
            }
        }
    }

    private String _getFinalName(String dottedName) {
        // Take the entity and it's class name and munge them into a
        // unique name for the generated class
        StringTokenizer tokenizer = new StringTokenizer(dottedName, ".");
        String endName = "error";
        while(tokenizer.hasMoreElements()) {
            endName = tokenizer.nextToken();
        }
        return endName;
    }

    private CompositeActor _model;
}














