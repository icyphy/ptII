/* A transformer that adds the command-line interface.

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import soot.Body;
import soot.BooleanType;
import soot.FastHierarchy;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeResolver;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// CommandLineTransformer
/**
A transformer that adds the command-line interface.  This create a new
class that is similar to the ptolemy.copernicus.java.CommandLineTemplate
class that creates and executes the model being generated.

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class CommandLineTransformer extends SceneTransformer implements HasPhaseOptions {
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

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "iterations:" + Integer.MAX_VALUE
            + " template:" + _commandLineTemplateDefault;
    }

    public String getDeclaredOptions() {
        return "iterations targetPackage template";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CommandLineTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        /* SootClass actorClass =  Scene.v().loadClassAndSupport(
           "ptolemy.actor.TypedAtomicActor");
           Type actorType = RefType.v(actorClass);
           SootClass compositeActorClass =
           Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");
        */
        // SootClass applicationClass = Scene.v().loadClassAndSupport(
        //        "ptolemy.actor.gui.CompositeActorApplication");
        String commandLineTemplate = PhaseOptions.getString(options, "template");
        if (commandLineTemplate.equals("")) {
            commandLineTemplate = _commandLineTemplateDefault;
        }
        SootClass applicationClass = Scene.v().loadClassAndSupport(
                commandLineTemplate);
        applicationClass.setLibraryClass();

        SootClass modelClass = ModelTransformer.getModelClass();

        SootClass mainClass = SootUtilities.copyClass(applicationClass,
                PhaseOptions.getString(options, "targetPackage") + ".Main");

        mainClass.setApplicationClass();

        // Tell the rest of soot that this is the interesting main method.
        Scene.v().setMainClass(mainClass);

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        // Optimizations.
        // We know that we will never parse classes, so throw away that code.
        SootUtilities.assertFinalField(mainClass,
                mainClass.getFieldByName("_expectingClass"),
                IntConstant.v(0));

        // We know that we will never be testing, so throw away that code.
        SootUtilities.assertFinalField(mainClass,
                mainClass.getFieldByName("_test"),
                IntConstant.v(0));

        // We know that we have exactly one model, so create it.
        // The final field for the model.
        SootField modelField = new SootField("_CGmodel",
                RefType.v(modelClass),
                Modifier.PRIVATE);// | Modifier.FINAL);
        mainClass.addField(modelField);


        // initialize the field by creating a model
        // in all the <init> methods.
        for (Iterator methods = mainClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            // ignore things that aren't initializers.
            if (!method.getName().equals("<init>"))
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

            // Set the name.
            units.insertBefore(
                    Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(modelLocal,
                                    PtolemyUtilities.setNameMethod,
                                    StringConstant.v(_model.getName()))),
                    insertPoint);

            // Set the hardcoded iteration limit, if necessary.
            int iterationLimit = PhaseOptions.getInt(options, "iterations");
            if(iterationLimit != Integer.MAX_VALUE) {
                units.insertBefore(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                                        mainClass.getFieldByName("_iterationLimit")),
                                IntConstant.v(iterationLimit)),
                        insertPoint);
            }
        }


        try {
            // unroll places where the list of models is used.

            // We put this in a try block so that we can exclude it
            // if necessary
            LinkedList modelList = new LinkedList();
            modelList.add(modelField);
            SootField modelsField = mainClass.getFieldByName("_models");

            if (modelsField != null) {
                SootUtilities.unrollIteratorInstances(mainClass,
                        modelsField, modelList);
            }
        } catch (RuntimeException ex) {
            System.out.println("Warning: did not find _models field: " + ex);
            for (Iterator methods = mainClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                System.out.println("clt: " + method + " " + body);
            }
            /*
              SootUtilities.createAndSetFieldFromLocal(
              body,
              modelField,
              mainClass,
              modelField.getType(),
              "_model");
            */
        }

        // Find calls to Manager.startRun() and replace it with
        // iteration code.
        // Note: It would be nice if we could inline the manager
        // code and optimize it, but in this case, the amount of code
        // we would want to throw away is fairly large.  This
        // just seems simpler here.
        SootClass managerClass =
            Scene.v().getSootClass("ptolemy.actor.Manager");
        SootMethod managerStartRunMethod =
            managerClass.getMethodByName("startRun");
        SootMethod mainStartRunMethod =
            mainClass.getMethodByName("startRun");
        for (Iterator methods = mainClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (!stmt.containsInvokeExpr()) {
                    continue;
                }
                ValueBox box = stmt.getInvokeExprBox();
                Value value = box.getValue();
                if (value instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr expr =
                        (InstanceInvokeExpr)value;
                    if (expr.getMethod().equals(mainStartRunMethod)) {
                        // Replace the start run method call
                        // with code to iterate the model.
                        // First create a local that refers to the model.
                        // FIXME This is redundant, since the local
                        // already exists somewhere...
                        Local modelLocal = Jimple.v().newLocal(
                                "_CGTemp" +
                                modelField.getName(),
                                modelField.getType());

                        body.getLocals().add(modelLocal);
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        modelLocal,
                                        Jimple.v().newInstanceFieldRef(
                                                body.getThisLocal(),
                                                modelField)),
                                stmt);

                        _insertIterateCalls(body,
                                stmt,
                                mainClass,
                                modelClass,
                                modelLocal,
                                options);
                        body.getUnits().remove(stmt);
                    }
                }
            }
        }

        // inline calls to the startRun and stopRun method.
        SootMethod startRunMethod = mainClass.getMethodByName("startRun");
        SootUtilities.inlineCallsToMethod(startRunMethod, mainClass);
        mainClass.removeMethod(startRunMethod);
        SootUtilities.inlineCallsToMethod(
                mainClass.getMethodByName("stopRun"), mainClass);

        for (Iterator methods = mainClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            LocalSplitter.v().transform(body, phaseName + ".lns");
            LocalNameStandardizer.v().transform(body, phaseName + ".lns");
            TypeResolver.resolve(body, Scene.v());
        }

        // unroll places where the model itself is looked at.
        // SootField modelsField = mainClass.getFieldByName("_models");
        // SootUtilities.unrollIteratorInstances(mainClass,
        //        modelsField, modelList);

        // Take the instance of main, and convert it to be a static class.
        /*
          // FIXME this is currently broken.
          {
          // First find the constructor statement.
          SootMethod mainMethod = mainClass.getMethodByName("main");
          JimpleBody body = (JimpleBody)mainMethod.retrieveActiveBody();
          Chain units = body.getUnits();
          for (Iterator stmts = units.iterator(); stmts.hasNext();) {
          Stmt stmt = (Stmt)stmts.next();
          // filter out anything that is not a definition.
          if (!(stmt instanceof DefinitionStmt)) {
          continue;
          }
          DefinitionStmt newStmt = (DefinitionStmt)stmt;
          Value value = (newStmt).getRightOp();
          if (!(value instanceof NewExpr)) {
          continue;
          }
          RefType type = ((NewExpr)value).getBaseType();
          if (type.getSootClass() != mainClass) {
          continue;
          }
          InvokeStmt constructorStmt = null;
          // Now walk forward and find the constructor.
          while (stmts.hasNext()) {
          stmt = (Stmt)stmts.next();
          if (stmt instanceof InvokeStmt &&
          ((InvokeStmt)stmt).getInvokeExpr()
          instanceof SpecialInvokeExpr) {
          constructorStmt = (InvokeStmt)stmt;
          }
          break;
          }

          // Now we actually have a creation of the main object,
          // so create a class just for that instance.
          SootClass staticMainClass =
          SootUtilities.createStaticClassForInstance(
          mainClass, body, newStmt, constructorStmt,
          PhaseOptions.getString(options, "targetPackage")
          + ".StaticMain");

          // Remove the extra Main method that we created in
          // doing this.
          SootMethod staticMainMethod =
          staticMainClass.getMethodByName("main");
          staticMainClass.removeMethod(staticMainMethod);

          break;
          }
          }
        */
        for (Iterator methods = mainClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            System.out.println("method = " + method.toString());
            SootMethod method2 = Scene.v().getMethod(method.toString());
        }
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());
    }

    /** Default value for the class name that is used as the command
     *  line template.  The initial default value is
     *  "ptolemy.copernicus.java.CommandLineTemplate");
     */
    protected String _commandLineTemplateDefault =
    "ptolemy.copernicus.java.CommandLineTemplate";

    private String _getFinalName(String dottedName) {
        // Take the entity and it's class name and munge them into a
        // unique name for the generated class
        StringTokenizer tokenizer = new StringTokenizer(dottedName, ".");
        String endName = "error";
        while (tokenizer.hasMoreElements()) {
            endName = tokenizer.nextToken();
        }
        return endName;
    }

    /** Insert into the given body before the given unit, calls to
     *  iterate the model that is referred to by the given local
     *  variable of the body that refers to an object of the given
     *  class.
     */
    private void _insertIterateCalls(Body body, Unit unit, SootClass mainClass,
            SootClass modelClass, Local modelLocal, Map options) {
        System.out.println("modelClass = " + modelClass);
        Chain units = body.getUnits();

        int iterationLimit = PhaseOptions.getInt(options, "iterations");

        Local postfireReturnsLocal =
            Jimple.v().newLocal("postfireReturns", BooleanType.v());
        body.getLocals().add(postfireReturnsLocal);

        Local iterationLocal = null;
        Local iterationLimitLocal = null;
        if (iterationLimit > 1) {
            iterationLocal = Jimple.v().newLocal("iteration",
                    IntType.v());
            body.getLocals().add(iterationLocal);
            units.insertBefore(
                    Jimple.v().newAssignStmt(iterationLocal,
                            IntConstant.v(0)),
                    unit);
            
            iterationLimitLocal = Jimple.v().newLocal("iterationLimit",
                    IntType.v());
            body.getLocals().add(iterationLimitLocal);
            units.insertBefore(
                    Jimple.v().newAssignStmt(iterationLimitLocal,
                            Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                                    mainClass.getFieldByName("_iterationLimit"))),
                    unit);
        }

        // call preinitialize
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "preinitialize"))),
                unit);

        // call initialize on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "initialize"))),
                unit);

        // A jump point for the start of the iteration.
        Stmt iterationStartStmt = Jimple.v().newNopStmt();
        // A jump point for the end of the iteration.
        // we don't actually insertBefore this until later in the sequence.
        Stmt iterationEndStmt = Jimple.v().newNopStmt();

        units.insertBefore(iterationStartStmt,
                unit);

        // call fire on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "fire"))),
                unit);

        // call postfire on the model.
        units.insertBefore(Jimple.v().newAssignStmt(postfireReturnsLocal,
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "postfire"))),
                unit);

        // If postfire returned false,
        // then we're done.
        units.insertBefore(Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(postfireReturnsLocal,
                        IntConstant.v(0)),
                iterationEndStmt),
                unit);

        // If we need to keep track of the number of iterations, then...
        if (iterationLimit > 1) {
            // Increment the number of iterations.
            units.insertBefore(Jimple.v().newAssignStmt(iterationLocal,
                    Jimple.v().newAddExpr(iterationLocal,
                            IntConstant.v(1))),
                    unit);
            
            // If the number of iterations is greater than, or equal
            // to the limit, then we're done.
            units.insertBefore(Jimple.v().newIfStmt(
                    Jimple.v().newGeExpr(iterationLocal,
                            iterationLimitLocal),
                    iterationEndStmt),
                    unit);
        }
        if (iterationLimit != 1) {
            units.insertBefore(Jimple.v().newGotoStmt(iterationStartStmt),
                    unit);
        }

        // insertBefore the jump point for the end of the iteration
        units.insertBefore(iterationEndStmt,
                unit);

        // call wrapup on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "wrapup"))),
                unit);
    }

    private CompositeActor _model;
}














