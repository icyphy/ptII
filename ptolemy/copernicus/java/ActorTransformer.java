/* Transform Actors using Soot

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringUtilities;

import soot.Hierarchy;
import soot.IntType;
import soot.BooleanType;
import soot.Local;
import soot.Modifier;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.util.Chain;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.invoke.SiteInliner;


//////////////////////////////////////////////////////////////////////////
//// ActorTransformer
/**
Transform Actors using Soot.  This transformer creates a new class for
each actor in the model that is similar to the original class of the actor.
During code generation, this actor class will be transformed and eventually
written out as part of the generated code.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class ActorTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private ActorTransformer(CompositeActor model) {
        _model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ActorTransformer v(CompositeActor model) {
        // FIXME: This should use a map to return a singleton instance
	// for each model
        return new ActorTransformer(model);
    }

    /** Return the list of default options for this transformer.
     *  @return An empty string.
     */
    public String getDefaultOptions() {
        return "";
    }

    /** Return the list of declared options for this transformer.
     *  This is a list of space separated option names.
     *  @return The value of the superclass options,
     *  plus the option "targetPackage"..
     */
    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    /** Transform the Scene according to the information specified
     *  in the model for this transform.
     *  @param phaseName The phase this transform is operating under.
     *  @param options The options to apply.
     */
    protected void internalTransform(String phaseName, Map options) {
	System.out.println("ActorTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // Create an instance class for every actor.
        for (Iterator i = _model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = entity.getClass().getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            entityClass.setLibraryClass();

            String newClassName = getInstanceClassName(entity, options);

            System.out.println("Creating actor class " + newClassName);
            System.out.println("for actor " + entity.getFullName());
            System.out.println("based on " + className);

            // FIXME the code below should probably copy the class and then
            // add init stuff.  EntitySootClass handles this nicely, but
            // doesn't let us use copyClass.  Generally adding this init crap
            // is something we have to do a lot.  How do we handle it nicely?
            //
            //            SootClass newClass =
            //     SootUtilities.copyClass(entityClass, newClassName);
            //  newClass.setApplicationClass();


            // create a class for the entity instance.
            EntitySootClass entityInstanceClass =
                new EntitySootClass(entityClass, newClassName,
                        Modifier.PUBLIC);
            Scene.v().addClass(entityInstanceClass);
            entityInstanceClass.setApplicationClass();

            // populate the method to initialize this instance.
            // We need to put something here before folding so that
            // the folder can deal with it.
            SootMethod initMethod = entityInstanceClass.getInitMethod();
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            // return void
            body.getUnits().add(Jimple.v().newReturnVoidStmt());

            SootClass theClass = (SootClass)entityInstanceClass;
            SootClass superClass = theClass.getSuperclass();
            while (superClass != PtolemyUtilities.objectClass &&
                    superClass != PtolemyUtilities.actorClass &&
                    superClass != PtolemyUtilities.compositeActorClass) {
                superClass.setLibraryClass();
                SootUtilities.foldClass(theClass);
                superClass = theClass.getSuperclass();
            }

            // Go through all the initialization code and removing any
            // parameter initialization code.
            // FIXME: This needs to look at all code that is reachable
            // from a constructor.
            _removeAttributeInitialization(theClass);

            Entity classEntity;
            try {
                classEntity = (Entity)
                    ModelTransformer._findDeferredInstance(entity).clone();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
            }
            // Loop over all the constructors and add code to each one to
            // populate the actor with parameter values, etc...
            /*     for (Iterator methods = theClass.getMethods().iterator();
                   methods.hasNext();) {
                   SootMethod method = (SootMethod)methods.next();
                   if (!method.getName().equals("<init>")) {
                   continue;
                   }

                   JimpleBody initBody = (JimpleBody)method.retrieveActiveBody();
                   NamedObjConstructorAnalysis analysis = new
                   NamedObjConstructorAnalysis(initBody);
                   Local thisLocal = initBody.getThisLocal();
                   for (Iterator ports = entity.portList().iterator();
                   ports.hasNext();) {
                   TypedIOPort port = (TypedIOPort)ports.next();
                   //FIXME WRONG!
                   _createFieldsForExistingAttributes(
                   body, analysis, classEntity, thisLocal,
                   port, thisLocal, entityInstanceClass);

                   }
                   }*/

            // replace the previous dummy body
            // for the initialization method with a new one.
            body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Create a set to keep track of which parameters have been
            // created and which we haven't.
            HashSet createdSet = new HashSet();

            ModelTransformer.updateCreatedSet(
                    _model.getFullName() + "." + entity.getName(),
                    classEntity, classEntity, createdSet);

            // Insert code to initialize the settable
            // parameters of this instance and
            // create fields for attributes.
            ModelTransformer.createFieldsForAttributes(
                    body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, createdSet);

            // Initialize the parameters of the class entity.
            for (Iterator attributes =
                     entity.attributeList(Settable.class).iterator();
                 attributes.hasNext();) {
                Settable settable = (Settable)attributes.next();
                Settable classSettable = (Settable)classEntity.getAttribute(
                        settable.getName());
                if (classSettable != null) {
                    try {
                        if (settable instanceof Variable) {

                            ((Variable)classSettable).setToken(
                                    ((Variable)settable).getToken());
                        } else {
                            classSettable
                                .setExpression(settable.getExpression());
                            classSettable.validate();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // FIXME: this is very similar to other code in the
            // ModelTransformer.
            // Set the types of all the ports.
            Local portLocal = Jimple.v().newLocal("port",
                    RefType.v("ptolemy.kernel.Port"));
            body.getLocals().add(portLocal);
            Local ioportLocal =
                Jimple.v().newLocal("typedport",
                        PtolemyUtilities.ioportType);
            body.getLocals().add(ioportLocal);

            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                TypedIOPort port = (TypedIOPort)ports.next();
                if (classEntity.getPort(port.getName()) != null) {
                    // Call the getPort method to get a reference to the port.
                    body.getUnits().add(Jimple.v().newAssignStmt(
                            portLocal,
                            Jimple.v().newVirtualInvokeExpr(
                                    thisLocal,
                                    PtolemyUtilities.getPortMethod,
                                    StringConstant.v(port.getName()))));
                } else {
                    String portClassName = port.getClass().getName();
                    // If the class does not create the port,
                    // then create a new port with the right name.
                    Local local = PtolemyUtilities.createNamedObjAndLocal(
                            body, portClassName,
                            thisLocal, port.getName());
                    // and then cast to portLocal
                    body.getUnits().add(Jimple.v().newAssignStmt(portLocal,
                            Jimple.v().newCastExpr(local,
                                    PtolemyUtilities.portType)));

                    // Create a new field for the attribute, and initialize
                    // it to the the attribute above.
                    SootUtilities.createAndSetFieldFromLocal(body, local,
                            entityInstanceClass,
                            PtolemyUtilities.portType,
                            ModelTransformer.getFieldNameForPort(port,
                                    entity));


                    if (port instanceof TypedIOPort) {
                        TypedIOPort ioport = (TypedIOPort)port;
                        if (ioport.isInput()) {
                            body.getUnits().add(Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            local,
                                            PtolemyUtilities.setInputMethod,
                                            IntConstant.v(1))));
                        }
                        if (ioport.isOutput()) {
                            body.getUnits().add(Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            local,
                                            PtolemyUtilities.setOutputMethod,
                                            IntConstant.v(1))));
                        }
                        if (ioport.isMultiport()) {
                            body.getUnits().add(Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            local,
                                            PtolemyUtilities.setMultiportMethod,
                                            IntConstant.v(1))));
                        }
                    }
                }

                // Create attributes for the port
                ModelTransformer.createFieldsForAttributes(
                        body, entity, thisLocal,
                        port, portLocal, entityInstanceClass, createdSet);

                // Then cast to TypedIOPort
                body.getUnits().add(Jimple.v().newAssignStmt(
                        ioportLocal,
                        Jimple.v().newCastExpr(portLocal,
                                PtolemyUtilities.ioportType)));

                // Create a new type.
                Local typeLocal = PtolemyUtilities.buildConstantTypeLocal(
                        body,
                        body.getUnits().getLast(),
                        port.getType());

                // And call the setTypeEquals() method.
                body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(
                                ioportLocal,
                                PtolemyUtilities.portSetTypeMethod,
                                typeLocal)));

                // Lastly, call connectionsChanged()
                body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(
                                thisLocal,
                                PtolemyUtilities.connectionsChangedMethod,
                                portLocal)));
            }
            // return void
            units.add(Jimple.v().newReturnVoidStmt());

            // Remove super calls to the executable interface.
            // FIXME: This would be nice to do by inlining instead of
            // special casing.
            _implementExecutableInterface(entityInstanceClass);

            // Reinitialize the hierarchy, since we've added classes.
            Scene.v().setActiveHierarchy(new Hierarchy());

            // Inline all methods in the class that are called from
            // within the class.
            _inlineLocalCalls(entityInstanceClass);

            // Remove the __CGInit method.
            entityInstanceClass.removeMethod(
                    entityInstanceClass.getInitMethod());
        }
    }

    public static String getInstanceClassName(Entity entity, Map options) {
        // Note that we use sanitizeName because entity names can have
        // spaces, and append leading characters because entity names
        // can start with numbers.
        return Options.getString(options, "targetPackage")
            + ".CG" + StringUtilities.sanitizeName(entity.getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static void _implementExecutableInterface(SootClass theClass) {
        // Loop through all the methods and remove calls to super.
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Unit unit = (Unit)units.next();
                Iterator boxes = unit.getUseBoxes().iterator();
                while (boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if (value instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                        if (PtolemyUtilities.executableInterface.declaresMethod(
                                r.getMethod().getSubSignature())) {
                            boolean isNonVoidMethod =
                                r.getMethod().getName().equals("prefire") ||
                                r.getMethod().getName().equals("postfire");
                            if (isNonVoidMethod && unit instanceof AssignStmt) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                body.getUnits().remove(unit);
                            }
                        }
                    }
                }
            }
        }

        // The initialize method implemented in the actor package is weird,
        // because it calls getDirector.  Since we don't need it,
        // make sure that we never call the baseclass initialize method.
        // FIXME: When we get to the point where we no longer derive
        // from TypedAtomicActor, we need to implement all of these methods.
        /*  if (!theClass.declaresMethodByName("initialize")) {
            SootMethod method = new SootMethod("initialize",
            new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
            }
        */
        if (!theClass.declaresMethodByName("preinitialize")) {
            SootMethod method = new SootMethod("preinitialize",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("initialize")) {
            SootMethod method = new SootMethod("initialize",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("prefire")) {
            SootMethod method = new SootMethod("prefire",
                    new LinkedList(), IntType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnStmt(
                    IntConstant.v(1)));
        }
        if (!theClass.declaresMethodByName("fire")) {
            SootMethod method = new SootMethod("fire",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("postfire")) {
            SootMethod method = new SootMethod("postfire",
                    new LinkedList(), BooleanType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnStmt(
                    IntConstant.v(1)));
        }
        if (!theClass.declaresMethodByName("wrapup")) {
            SootMethod method = new SootMethod("wrapup",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
    }

    // Inline invocation sites from methods in the given class to
    // another method in the given class
    private static void _inlineLocalCalls(SootClass theClass) {
        // FIXME: what if the inlined code contains another call
        // to this class???
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                    // Avoid inlining recursive methods.
                    if (r.getMethod() != method && 
                         r.getMethod().getDeclaringClass().equals(theClass)) {
                        // FIXME: What if more than one method could be called?
                        SiteInliner.inlineSite(r.getMethod(), stmt, method);
                    }


                    // Inline other NamedObj methods here, too..

                }
            }
        }
    }

    private static void _removeAttributeInitialization(SootClass theClass) {
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                    // This is steve...
                    // This is steve gacking at the ugliest code
                    // he's written in a while.   See steve gack.
                    // gack steve, gack.
                    // This is Christopher.
                    // This is Christopher gacking on Steve's code
                    // gack Christopher, gack.
                    if (r.getMethod().getName().equals("attributeChanged") ||
                            r.getMethod().getName().equals("setExpression") ||
                            r.getMethod().getName().equals("setToken") ||
                            r.getMethod().getName()
                            .equals("setTokenConsumptionRate") ||
                            r.getMethod().getName()
                            .equals("setTokenProductionRate") ||
                            r.getMethod().getName()
                            .equals("setTokenInitProduction")) {
                        body.getUnits().remove(stmt);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CompositeActor _model;
}













