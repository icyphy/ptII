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

import java.util.*;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.Token;
import ptolemy.data.expr.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import soot.*;
import soot.util.Chain;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.scalar.LocalSplitter;
import soot.jimple.toolkits.typing.TypeAssigner;

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

        // createActorsIn(_model, phaseName, options);
    }

    public static void createActorsIn(CompositeActor model, HashSet createdSet,
            String phaseName, Map options) {
        // Create an instance class for every actor.
        for (Iterator i = model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();

 //            if(entity instanceof CompositeActor) {
//                 createActorsIn((CompositeActor) entity, phaseName, options);
//             }

            String className = entity.getClass().getName();

          //   if(className.equals("ptolemy.actor.lib.Expression")) {
//                 // Generate code for the expression 

//             } else {
 
            String newClassName = getInstanceClassName(entity, options);

            if(Scene.v().containsClass(newClassName)) {
                continue;
            }

            System.out.println("ActorTransformer: Creating actor class " + newClassName);
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
 
            if(entity instanceof CompositeActor) {
                _createCompositeActor((CompositeActor)entity, newClassName, options);
            } else if(entity instanceof Expression) {
                _createExpressionActor((Expression)entity, newClassName, options);
            } else {
                // Must be an atomicActor.
                _createAtomicActor(model, (AtomicActor)entity, newClassName, options);
            }
        }
    }

    public static String getInstanceClassName(Entity entity, Map options) {
        // Note that we use sanitizeName because entity names can have
        // spaces, and append leading characters because entity names
        // can start with numbers.
        return Options.getString(options, "targetPackage")
            + ".CG" + StringUtilities.sanitizeName(entity.getName(entity.toplevel()));
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
                    new LinkedList(), BooleanType.v(), Modifier.PUBLIC);
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

                    // FIXME: avoid inlining method calls 
                    // that don't have tokens in them 
                }
            }
        }
    }

    // Populate the given class with code to create the contents of
    // the given entity.
    private static EntitySootClass _createAtomicActor(
            CompositeActor model, AtomicActor entity, String newClassName, Map options) {

        String className = entity.getClass().getName();

        SootClass entityClass = Scene.v().loadClassAndSupport(className);
        entityClass.setLibraryClass();
        
        // create a class for the entity instance.
        EntitySootClass entityInstanceClass =
            new EntitySootClass(entityClass, newClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();
        
        // Record everything that the class creates.
        HashSet tempCreatedSet = new HashSet();

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
                
        ModelTransformer.updateCreatedSet(
                model.getFullName() + "." + entity.getName(),
                classEntity, classEntity, tempCreatedSet);
    
        // replace the previous dummy body
        // for the initialization method with a new one.
        body = Jimple.v().newBody(initMethod);
        initMethod.setActiveBody(body);
        body.insertIdentityStmts();
        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();
        
        // Initialize attributes that already exist in the class.
        _initializeAttributes(body, entity, thisLocal,
                entity, thisLocal, entityInstanceClass, tempCreatedSet);
        
        // Create and initialize ports
        _initializePorts(body, thisLocal, entity,
                thisLocal, entity, entityInstanceClass, tempCreatedSet);
        
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
        
        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(
                entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    // Populate the given class with code to create the contents of
    // the given entity.
    private static EntitySootClass _createCompositeActor(
            CompositeActor entity, String newClassName, Map options) {
      
        String className = entity.getClass().getName();

        SootClass entityClass = Scene.v().loadClassAndSupport(className);
        entityClass.setLibraryClass();

        // create a class for the entity instance.
        EntitySootClass entityInstanceClass =
            new EntitySootClass(entityClass, newClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();
        
        // Record everything that the class creates.
        HashSet tempCreatedSet = new HashSet();
        
        // create a new body for the initialization method.
        SootMethod initMethod = entityInstanceClass.getInitMethod();
        JimpleBody body = Jimple.v().newBody(initMethod);
        initMethod.setActiveBody(body);
        body.insertIdentityStmts();
        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();
        
        ModelTransformer._composite(body,
                thisLocal, entity, thisLocal, entity,
                entityInstanceClass, tempCreatedSet, options);
        // return void
        units.add(Jimple.v().newReturnVoidStmt());
        
        return entityInstanceClass;
    }

    // Populate the given class with code to create the contents of
    // the given entity.
    private static EntitySootClass _createExpressionActor(
            Expression entity, String newClassName, Map options) {
      
        SootClass entityClass = PtolemyUtilities.actorClass;
      
        // create a class for the entity instance.
        EntitySootClass entityInstanceClass =
            new EntitySootClass(entityClass, newClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();
 
        // Record everything that the class creates.
        HashSet tempCreatedSet = new HashSet();

        SootMethod initMethod = entityInstanceClass.getInitMethod();
        {
            // Populate the initialization method.
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();
            
            // Populate...
            // Initialize attributes that already exist in the class.
            _initializeAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);
            
            // Create and initialize ports
            _initializePorts(body, thisLocal, entity,
                    thisLocal, entity, entityInstanceClass, tempCreatedSet);
            
            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }
         
        // Add fields to contain the tokens for each port. 
        Map nameToField = new HashMap();
        Map nameToType = new HashMap();
        {
            Iterator inputPorts = entity.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort)(inputPorts.next());
                String name = port.getName(entity);
                Type type = PtolemyUtilities.tokenType;
                nameToType.put(name, port.getType());
                // PtolemyUtilities.getSootTypeForTokenType(
                       //  port.getType());
                SootField field = new SootField(
                        name + "Token",
                        type);
                entityInstanceClass.addField(field);
                nameToField.put(name, field);
            }
        }

        // populate the fire method.
        {
            SootMethod fireMethod = new SootMethod("fire",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            entityInstanceClass.addMethod(fireMethod);
            JimpleBody body = Jimple.v().newBody(fireMethod);
            fireMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local hasTokenLocal = Jimple.v().newLocal(
                    "hasTokenLocal", BooleanType.v());
            body.getLocals().add(hasTokenLocal);
            Local tokenLocal = Jimple.v().newLocal(
                    "tokenLocal", PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            Iterator inputPorts = entity.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort)(inputPorts.next());
                // FIXME: Handle multiports
                if (port.getWidth() > 0) {
                    String name = port.getName(entity);

                    // Create an if statement.
                    // 
                    Local portLocal = Jimple.v().newLocal("port",
                            PtolemyUtilities.portType);
                    body.getLocals().add(portLocal);
                    SootField portField = entityInstanceClass.getFieldByName(
                            StringUtilities.sanitizeName(name));
                    units.add(
                            Jimple.v().newAssignStmt(portLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            thisLocal, portField)));
                    units.add(
                            Jimple.v().newAssignStmt(hasTokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.hasTokenMethod,
                                            IntConstant.v(0))));
                 
                    Stmt target = Jimple.v().newNopStmt();
                    units.add(Jimple.v().newIfStmt(
                                      Jimple.v().newEqExpr(hasTokenLocal,
                                              IntConstant.v(0)),
                                      target));
                    units.add(Jimple.v().newAssignStmt(
                                      tokenLocal,
                                      Jimple.v().newVirtualInvokeExpr(
                                              portLocal,
                                              PtolemyUtilities.getMethod,
                                              IntConstant.v(0))));
                    // cast?
//                     RefType type = PtolemyUtilities.getSootTypeForTokenType(
//                             port.getType());
//                     units.add(Jimple.v().newAssignStmt(
//                                       tokenLocal,
//                                       Jimple.v().newCastExpr(
//                                               tokenLocal,
//                                               type)));
                  SootField tokenField = 
                        entityInstanceClass.getFieldByName(name + "Token");
                    units.add(Jimple.v().newAssignStmt(
                                      Jimple.v().newInstanceFieldRef(
                                              thisLocal,
                                              tokenField),
                                      tokenLocal));
                    units.add(target);
                    //                 if (port.hasToken(0)) {
                    //                     Token inputToken = port.get(0);
                    //                     _tokenMap.put(port.getName(), inputToken);
                    //                 }
                }
            }

            StringAttribute expressionAttribute = (StringAttribute)
                entity.getAttribute("expression");
            String expression = expressionAttribute.getExpression();

            Local local = null;
            try {
                PtParser parser = new PtParser();
                ASTPtRootNode parseTree = 
                    parser.generateParseTree(expression);
                ActorCodeGenerationScope scope = 
                    new ActorCodeGenerationScope(nameToField, nameToType, body);
                ParseTreeCodeGenerator generator = 
                    new ParseTreeCodeGenerator();
                local = generator.generateCode(parseTree, body, scope);
            } catch (IllegalActionException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.toString());
            }
           
            // send the computed token
            String name = "output";
            Local portLocal = Jimple.v().newLocal("port",
                    PtolemyUtilities.portType);
            body.getLocals().add(portLocal);

            SootField portField = entityInstanceClass.getFieldByName(name);

            units.add(
                    Jimple.v().newAssignStmt(portLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            thisLocal, portField)));
            units.add(
                    Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(
                                    portLocal,
                                    PtolemyUtilities.sendMethod,
                                    IntConstant.v(0), local)));
            
            
            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        // Remove super calls to the executable interface.
        // FIXME: This would be nice to do by inlining instead of
        // special casing.
        _implementExecutableInterface(entityInstanceClass);
        
        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        
        // Inline all methods in the class that are called from
        // within the class.
        _inlineLocalCalls(entityInstanceClass);
        
        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(
                entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    public static class ActorCodeGenerationScope
        implements CodeGenerationScope {
        public ActorCodeGenerationScope(Map nameToField, Map nameToType, JimpleBody body) {
            _nameToField = nameToField;
            _nameToType = nameToType;
            _body = body;
            _units = body.getUnits();
        }
        
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            throw new IllegalActionException("The ID " + name + 
                    " does not have a value");
        }

        public Local getLocal(String name)
                throws IllegalActionException {
            SootField portField = (SootField)_nameToField.get(name);

            if(portField != null) {
                Local thisLocal = _body.getThisLocal();
                
                Local portLocal = Jimple.v().newLocal("portToken",
                        PtolemyUtilities.tokenType);
                _body.getLocals().add(portLocal);
                
                _units.add(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, portField)));
                return portLocal;
            } else {
                throw new IllegalActionException(
                        "The ID " + name + " is undefined.");
            }
        }
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            if(_nameToType.containsKey(name)) {
                return (ptolemy.data.type.Type)_nameToType.get(name);
            } else {
                throw new IllegalActionException(
                        "The ID " + name + " is undefined.");
            }
        }
        public NamedList variableList() {
            return new NamedList();
        }
        
        private Map _nameToField;
        private Map _nameToType;
        private JimpleBody _body;
        private Chain _units;
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
                    if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.variableConstructorWithToken.getSubSignature())) {
                        SootClass variableClass =
                            r.getMethod().getDeclaringClass();
                        SootMethod constructorWithoutToken =
                            variableClass.getMethod(
                                    PtolemyUtilities.variableConstructorWithoutToken.getSubSignature());
                        // Replace the three-argument
                        // constructor with a two-argument
                        // constructor.  We do this for
                        // several reasons:
                        
                        // 1) The assignment is
                        // redundant...  all parameters
                        // are initialized with the
                        // appropriate value.
                        
                        // 2) The type of the token is
                        // often wrong for polymorphic
                        // actors.
                        
                        // 3) Later on, when we inline all
                        // token constructors, there is no
                        // longer a token to pass to the
                        // constructor.  It is easier to
                        // just deal with it now...
                        
                        // Create a new two-argument constructor.
                        InstanceInvokeExpr expr = (InstanceInvokeExpr)r;
                        stmt.getInvokeExprBox().setValue(
                                Jimple.v().newSpecialInvokeExpr(
                                        (Local)expr.getBase(), 
                                        constructorWithoutToken,
                                        r.getArg(0),
                                        r.getArg(1)));
                    }
                }
            }
        }
    }

    // This is similar to ModelTransformer.createFieldsForAttributes,
    // except that all attributes are initialized, even those that
    // have already been created.
    public static void _initializeAttributes(JimpleBody body,
            NamedObj context,
            Local contextLocal, NamedObj namedObj, Local namedObjLocal,
            SootClass theClass, HashSet createdSet) {

        Type variableType = RefType.v(PtolemyUtilities.variableClass);

        // A local that we will use to set the value of our
        // settable attributes.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                PtolemyUtilities.attributeType);
        body.getLocals().add(attributeLocal);
	Local settableLocal = Jimple.v().newLocal("settable",
                PtolemyUtilities.settableType);
	body.getLocals().add(settableLocal);
	Local variableLocal = Jimple.v().newLocal("variable",
                variableType);
	body.getLocals().add(variableLocal);
 
        /*    NamedObj classObject = _findDeferredInstance(namedObj);
              System.out.println("Class object for " + namedObj.getFullName());
              System.out.println(classObject.exportMoML());
        */

        for (Iterator attributes = namedObj.attributeList().iterator();
             attributes.hasNext();) {
	    Attribute attribute = (Attribute)attributes.next();

            // FIXME: This is horrible...  I guess we need an attribute for
            // persistence?
            if (attribute instanceof Variable &&
                    !(attribute instanceof Parameter)) {
                continue;
            }

            // Ignore frame sizes and locations.  They aren't really
            // necessary in the generated code, I don't think.
            if (attribute instanceof SizeAttribute ||
                attribute instanceof LocationAttribute) {
                continue;
            }

            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = ModelTransformer.getFieldNameForAttribute(attribute, context);

            Local local;
            if (createdSet.contains(attribute.getFullName())) {
                //    System.out.println("already has " + attributeName);
                // If the class for the object already creates the
                // attribute, then get a reference to the existing attribute.
                // Note that if the class creates the attribute, but
                // doesn't also create a field for it, that we will
                // fail later when we try to replace getAttribute
                // calls with references to fields.
                // if (theClass.declaresFieldByName(fieldName)) {
                    local = attributeLocal;
                    body.getUnits().add(Jimple.v().newAssignStmt(
                            attributeLocal,
                            Jimple.v().newVirtualInvokeExpr(contextLocal,
                                    PtolemyUtilities.getAttributeMethod,
                                    StringConstant.v(attributeName))));
              //   } else {
//                     System.out.println("Warning: " + theClass + " does " +
//                             "not declare a field " + fieldName);
//                     // FIXME: Try to analyze the constructor to set
//                     // the field.  This is nontrivial.
//                     // For the moment, we skip this case.
//                     continue;
//                 }
            } else {
                //   System.out.println("creating " + attributeName);
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        namedObjLocal, attribute.getName());
                Attribute classAttribute =
                    (Attribute)ModelTransformer._findDeferredInstance(attribute);
                ModelTransformer.updateCreatedSet(namedObj.getFullName() + "."
                        + attribute.getName(),
                        classAttribute, classAttribute, createdSet);
            }

            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
            SootUtilities.createAndSetFieldFromLocal(body, local,
                    theClass, attributeType, fieldName);

            if (attribute instanceof Variable) {
                // If the attribute is a parameter, then set its
                // token to the correct value.
		// cast to Variable.
                Stmt assignStmt = Jimple.v().newAssignStmt(
                        variableLocal,
                        Jimple.v().newCastExpr(
                                local,
                                variableType));
                               
              	body.getUnits().add(assignStmt);
                
                Token token = null;
                try {
                    token = ((Variable)attribute).getToken();
                } catch (IllegalActionException ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                if (token == null) {
                    throw new RuntimeException("Calling getToken() on '"
                            + attribute + "' returned null.  This may occur "
                            + "if an attribute has no value in the moml file");
                }

                Local tokenLocal = 
                    PtolemyUtilities.buildConstantTokenLocal(body,
                        assignStmt, token, "token");
                        
		// call setToken.
		body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(
                                variableLocal,
                                PtolemyUtilities.variableSetTokenMethod,
                                tokenLocal)));
                // call validate to ensure that attributeChanged is called.
                body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newInterfaceInvokeExpr(
                                variableLocal,
                                PtolemyUtilities.validateMethod)));
                
            } else if (attribute instanceof Settable) {
                // If the attribute is settable, then set its
                // expression.
                
		// cast to Settable.
		body.getUnits().add(Jimple.v().newAssignStmt(
                        settableLocal,
                        Jimple.v().newCastExpr(
                                local,
                                PtolemyUtilities.settableType)));
                String expression = ((Settable)attribute).getExpression();

		// call setExpression.
		body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newInterfaceInvokeExpr(
                                settableLocal,
                                PtolemyUtilities.setExpressionMethod,
                                StringConstant.v(expression))));
                // call validate to ensure that attributeChanged is called.
                body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newInterfaceInvokeExpr(
                                settableLocal,
                                PtolemyUtilities.validateMethod)));
	    }

            // FIXME: configurable??
            // recurse so that we get all parameters deeply.
            _initializeAttributes(body, context, contextLocal,
                    attribute, local, theClass, createdSet);
	}
    }

    // Initialize the ports of this actor.  This is similar to code in 
    // the ModelTransformer, except that here, all ports have their type set.
    private static void _initializePorts(JimpleBody body, Local containerLocal,
            Entity container, Local entityLocal,
            Entity entity, EntitySootClass modelClass, HashSet createdSet) {
        Entity classObject = (Entity)
            ModelTransformer._findDeferredInstance(entity);

        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v(PtolemyUtilities.portClass));
        body.getLocals().add(tempPortLocal);

	for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
	    Port port = (Port)ports.next();
            //   System.out.println("ModelTransformer: port: " + port);
	  
            String className = port.getClass().getName();
            String portName = port.getName(container);
            String fieldName = 
                ModelTransformer.getFieldNameForPort(port, container);
            RefType portType = RefType.v(className);
	    Local portLocal = Jimple.v().newLocal("port",
                    portType);
            body.getLocals().add(portLocal);
            
            if (createdSet.contains(port.getFullName())) {
                //       System.out.println("already created!");
                // If the class for the object already creates the
                // port, then get a reference to the existing port.
                // First assign to temp
                body.getUnits().add(
                        Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        entityLocal,
                                        PtolemyUtilities.getPortMethod,
                                        StringConstant.v(
                                                port.getName()))));
                // and then cast to portLocal
                body.getUnits().add(
                            Jimple.v().newAssignStmt(portLocal,
                                    Jimple.v().newCastExpr(tempPortLocal,
                                            portType)));
               //  } else {
//                     System.out.println("Warning: " + modelClass + " does " +
//                             "not declare a field " + fieldName);
//                     // FIXME: Try to analyze the constructor to set
//                     // the field.  This is nontrivial.
//                     // For the moment, we skip this case.
//                     continue;
//                 }
            } else {
                //     System.out.println("Creating new!");
                // If the class does not create the port
                // then create a new port with the right name.
                Local local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        entityLocal, port.getName());
                ModelTransformer.updateCreatedSet(entity.getFullName() + "."
                        + port.getName(),
                        port, port, createdSet);
                // and then assign to portLocal
                // (It should already have the correct type.
                body.getUnits().add(
                        Jimple.v().newAssignStmt(portLocal,
                                local));
                             //    Jimple.v().newCastExpr(local,
//                                         portType)));
                
            }
            if (port instanceof TypedIOPort) {
                TypedIOPort ioport = (TypedIOPort)port;
                Local ioportLocal =
                    Jimple.v().newLocal("typed_" + port.getName(),
                            PtolemyUtilities.ioportType);
                body.getLocals().add(ioportLocal);
                Stmt castStmt = Jimple.v().newAssignStmt(ioportLocal,
                        Jimple.v().newCastExpr(portLocal,
                                PtolemyUtilities.ioportType));
                body.getUnits().add(castStmt);
                if (ioport.isInput()) {
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(ioportLocal,
                                            PtolemyUtilities.setInputMethod,
                                            IntConstant.v(1))));
                }
                if (ioport.isOutput()) {
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(ioportLocal,
                                            PtolemyUtilities.setOutputMethod,
                                            IntConstant.v(1))));
                }
                if (ioport.isMultiport()) {
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(ioportLocal,
                                            PtolemyUtilities.setMultiportMethod,
                                            IntConstant.v(1))));
                }
                // Set the port's type.
                Local typeLocal =
                    PtolemyUtilities.buildConstantTypeLocal(
                            body, castStmt, ioport.getType());
                body.getUnits().add(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(
                                        ioportLocal,
                                        PtolemyUtilities.portSetTypeMethod,
                                        typeLocal)));
            }

            if (!modelClass.declaresFieldByName(fieldName)) {
                SootUtilities.createAndSetFieldFromLocal(body,
                        portLocal, modelClass, RefType.v(className),
                        fieldName);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CompositeActor _model;
}













