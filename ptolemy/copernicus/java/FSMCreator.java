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
 */
package ptolemy.copernicus.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.StringUtilities;
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
import soot.VoidType;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// AtomicActorCreator

/**

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FSMCreator implements AtomicActorCreator {
    /** Generate a new class with the given name that can take the
     *  place of the given actor.  Use the given options when
     *  necessary.  The given entity is assumed to be an expression actor.
     */
    public SootClass createAtomicActor(Entity actor, String newClassName,
            ConstVariableModelAnalysis constAnalysis, Map options) {
        FSMActor entity = (FSMActor) actor;

        SootClass entityClass = PtolemyUtilities.actorClass;

        // create a class for the entity instance.
        EntitySootClass entityInstanceClass = new EntitySootClass(entityClass,
                newClassName, Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();

        // Create methods that will compute and set the values of the
        // parameters of this actor.
        ModelTransformer.createAttributeComputationFunctions(entity, entity,
                entityInstanceClass, constAnalysis);

        // Record everything that the class creates.
        HashMap tempCreatedMap = new HashMap();

        SootMethod initMethod = entityInstanceClass.getInitMethod();

        {
            System.out.println("creating <init>");

            // Populate the initialization method.
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Populate...
            // Initialize attributes that already exist in the class.
            //  System.out.println("initializing attributes");
            ModelTransformer.createAttributes(body, entity, thisLocal, entity,
                    thisLocal, entityInstanceClass, tempCreatedMap);

            // Create and initialize ports
            // System.out.println("initializing ports");
            ModelTransformer.createPorts(body, thisLocal, entity, thisLocal,
                    entity, entityInstanceClass, tempCreatedMap);

            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }

        // Add fields to contain the tokens for each port.
        Map nameToField = new HashMap();
        Map nameToType = new HashMap();

        {
            Iterator inputPorts = entity.inputPortList().iterator();

            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) (inputPorts.next());
                String name = port.getName(entity);
                Type type = PtolemyUtilities.tokenType;
                nameToType.put(name, port.getType());

                // PtolemyUtilities.getSootTypeForTokenType(
                //  port.getType());
                SootField field = new SootField(StringUtilities
                        .sanitizeName(name)
                        + "Token", type);
                entityInstanceClass.addField(field);
                nameToField.put(name, field);

                field = new SootField(StringUtilities.sanitizeName(name)
                        + "IsPresent", type);
                entityInstanceClass.addField(field);
                nameToField.put(name + "_isPresent", field);
            }
        }

        {
            SootMethod preinitializeMethod = new SootMethod("preinitialize",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            entityInstanceClass.addMethod(preinitializeMethod);

            JimpleBody body = Jimple.v().newBody(preinitializeMethod);
            preinitializeMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Stmt insertPoint = Jimple.v().newReturnVoidStmt();
            body.getUnits().add(insertPoint);
            ModelTransformer.initializeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(), entity, body.getThisLocal(),
                    entityInstanceClass);
        }

        // Add a field to keep track of the current state.
        SootField currentStateField = new SootField("_currentState", IntType
                .v());
        entityInstanceClass.addField(currentStateField);

        SootField nextTransitionField = new SootField("_nextTransition",
                IntType.v());
        entityInstanceClass.addField(nextTransitionField);
        // populate the initialize method.
        {
            System.out.println("create initialize()");

            SootMethod initializeMethod = new SootMethod("initialize",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            entityInstanceClass.addMethod(initializeMethod);

            JimpleBody body = Jimple.v().newBody(initializeMethod);
            initializeMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Set the initial state.
            String initialStateName = ((StringAttribute) entity
                    .getAttribute("initialStateName")).getExpression();
            int initialStateIndex = entity.entityList().indexOf(
                    entity.getEntity(initialStateName));
            units.add(Jimple.v().newAssignStmt(
                    Jimple.v()
                            .newInstanceFieldRef(thisLocal, currentStateField),
                    IntConstant.v(initialStateIndex)));

            // return void
            units.add(Jimple.v().newReturnVoidStmt());

            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }
        // populate the fire method.
        {
            System.out.println("create fire()");

            SootMethod fireMethod = new SootMethod("fire",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            entityInstanceClass.addMethod(fireMethod);

            JimpleBody body = Jimple.v().newBody(fireMethod);
            fireMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Local hasTokenLocal = Jimple.v().newLocal("hasTokenLocal",
                    BooleanType.v());
            body.getLocals().add(hasTokenLocal);

            Local tokenLocal = Jimple.v().newLocal("tokenLocal",
                    PtolemyUtilities.tokenType);
            body.getLocals().add(tokenLocal);

            Iterator inputPorts = entity.inputPortList().iterator();

            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) (inputPorts.next());

                // FIXME: Handle multiports
                if (port.getWidth() > 0) {
                    String name = port.getName(entity);

                    // Create an if statement.
                    //
                    Local portLocal = Jimple.v().newLocal("port",
                            PtolemyUtilities.componentPortType);
                    body.getLocals().add(portLocal);

                    SootField portField = entityInstanceClass
                            .getFieldByName(StringUtilities.sanitizeName(name));
                    units
                            .add(Jimple.v().newAssignStmt(
                                    portLocal,
                                    Jimple.v().newInstanceFieldRef(thisLocal,
                                            portField)));
                    units.add(Jimple.v().newAssignStmt(
                            hasTokenLocal,
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.hasTokenMethod,
                                    IntConstant.v(0))));

                    Local hasTokenToken = PtolemyUtilities.addTokenLocal(body,
                            "token", PtolemyUtilities.booleanTokenClass,
                            PtolemyUtilities.booleanTokenConstructor,
                            hasTokenLocal);

                    // store the isPresent
                    SootField tokenIsPresentField = (SootField) nameToField
                            .get(name + "_isPresent");
                    units.add(Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(thisLocal,
                                    tokenIsPresentField), hasTokenToken));

                    Stmt target = Jimple.v().newNopStmt();
                    units.add(Jimple.v().newIfStmt(
                            Jimple.v().newEqExpr(hasTokenLocal,
                                    IntConstant.v(0)), target));
                    units.add(Jimple.v().newAssignStmt(
                            tokenLocal,
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.getMethod,
                                    IntConstant.v(0))));

                    SootField tokenField = (SootField) nameToField.get(name);
                    units.add(Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(thisLocal,
                                    tokenField), tokenLocal));
                    units.add(target);
                }
            }

            Map stateToStartStmt = new HashMap();
            List stateStmtList = new LinkedList();
            int numberOfStates = entity.entityList().size();

            // Figure out what state we are in.
            for (Iterator states = entity.entityList().iterator(); states
                    .hasNext();) {
                State state = (State) states.next();
                Stmt startStmt = Jimple.v().newNopStmt();

                stateToStartStmt.put(state, startStmt);
                stateStmtList.add(startStmt);
            }

            Local currentStateLocal = Jimple.v().newLocal("currentStateLocal",
                    IntType.v());
            body.getLocals().add(currentStateLocal);

            Local nextStateLocal = Jimple.v().newLocal("nextStateLocal",
                    IntType.v());
            body.getLocals().add(nextStateLocal);

            Local flagLocal = Jimple.v().newLocal("flagLocal", BooleanType.v());
            body.getLocals().add(flagLocal);

            Local transitionTakenLocal = Jimple.v().newLocal(
                    "transitionTakenLocal", BooleanType.v());
            body.getLocals().add(transitionTakenLocal);

            Local nextTransitionLocal = Jimple.v().newLocal(
                    "nextTransitionLocal", IntType.v());
            body.getLocals().add(nextTransitionLocal);

            units
                    .add(Jimple.v().newAssignStmt(
                            currentStateLocal,
                            Jimple.v().newInstanceFieldRef(thisLocal,
                                    currentStateField)));

            // Start by doing nothing.
            units.add(Jimple.v().newAssignStmt(transitionTakenLocal,
                    IntConstant.v(0)));
            units.add(Jimple.v().newAssignStmt(nextTransitionLocal,
                    IntConstant.v(-1)));

            // If no transition is taken, then stay in this state.
            units.add(Jimple.v().newAssignStmt(nextStateLocal,
                    currentStateLocal));

            Stmt finishedStmt = Jimple.v().newNopStmt();
            Stmt errorStmt = Jimple.v().newNopStmt();

            // Get the current state.
            units.add(Jimple.v().newTableSwitchStmt(currentStateLocal, 0,
                    numberOfStates - 1, stateStmtList, errorStmt));

            // Generate code for each state.
            for (Iterator states = entity.entityList().iterator(); states
                    .hasNext();) {
                State state = (State) states.next();
                System.out.println("state " + state.getName());

                Stmt startStmt = (Stmt) stateToStartStmt.get(state);
                units.add(startStmt);

                // Fire the refinement actor.
                TypedActor[] refinements = null;

                try {
                    refinements = state.getRefinement();
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                if (refinements != null) {
                    for (int i = 0; i < refinements.length; i++) {
                        TypedActor refinement = refinements[i];

                        Local containerLocal = Jimple.v().newLocal("container",
                                RefType.v(PtolemyUtilities.namedObjClass));
                        body.getLocals().add(containerLocal);

                        Local entityLocal = Jimple.v().newLocal("entity",
                                RefType.v(PtolemyUtilities.entityClass));
                        body.getLocals().add(entityLocal);

                        NamedObj containerModel = (NamedObj) entity
                                .getContainer();
                        String deepName = ((NamedObj) refinement)
                                .getName(containerModel);

                        units.add(Jimple.v().newAssignStmt(
                                containerLocal,
                                Jimple.v().newInterfaceInvokeExpr(thisLocal,
                                        PtolemyUtilities.getContainerMethod)));
                        units
                                .add(Jimple
                                        .v()
                                        .newAssignStmt(
                                                containerLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                containerLocal,
                                                                RefType
                                                                        .v(PtolemyUtilities.compositeActorClass))));
                        units.add(Jimple.v().newAssignStmt(
                                entityLocal,
                                Jimple.v().newVirtualInvokeExpr(containerLocal,
                                        PtolemyUtilities.getEntityMethod,
                                        StringConstant.v(deepName))));

                        units
                                .add(Jimple
                                        .v()
                                        .newAssignStmt(
                                                entityLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                entityLocal,
                                                                RefType
                                                                        .v(PtolemyUtilities.compositeActorClass))));

                        SootMethod rprefireMethod;
                        SootMethod rfireMethod;
                        SootMethod rpostfireMethod;

                        if (refinement instanceof CompositeActor) {
                            rprefireMethod = SootUtilities
                                    .searchForMethodByName(
                                            PtolemyUtilities.compositeActorClass,
                                            "prefire");
                            rfireMethod = SootUtilities.searchForMethodByName(
                                    PtolemyUtilities.compositeActorClass,
                                    "fire");
                            rpostfireMethod = SootUtilities
                                    .searchForMethodByName(
                                            PtolemyUtilities.compositeActorClass,
                                            "postfire");
                        } else {
                            throw new RuntimeException();
                        }

                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(entityLocal,
                                        rprefireMethod)));
                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(entityLocal,
                                        rfireMethod)));
                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(entityLocal,
                                        rpostfireMethod)));
                    }
                }

                // Determine the next state in this state.
                for (Iterator transitions = state.outgoingPort
                        .linkedRelationList().iterator(); transitions.hasNext();) {
                    Transition transition = (Transition) transitions.next();
                    System.out.println("transition = " + transition);

                    String guardExpression = transition.getGuardExpression();

                    Local guardLocal = DataUtilities.generateExpressionCode(
                            entity, entityInstanceClass, guardExpression,
                            nameToField, nameToType, body);

                    // Test the guard.
                    units
                            .add(Jimple
                                    .v()
                                    .newAssignStmt(
                                            tokenLocal,
                                            Jimple
                                                    .v()
                                                    .newCastExpr(
                                                            guardLocal,
                                                            RefType
                                                                    .v(PtolemyUtilities.booleanTokenClass))));
                    units.add(Jimple.v().newAssignStmt(
                            flagLocal,
                            Jimple.v().newVirtualInvokeExpr(tokenLocal,
                                    PtolemyUtilities.booleanValueMethod)));

                    Stmt skipStmt = Jimple.v().newNopStmt();

                    units.add(Jimple.v().newIfStmt(
                            Jimple.v().newEqExpr(flagLocal, IntConstant.v(0)),
                            skipStmt));
                    units.add(Jimple.v().newIfStmt(
                            Jimple.v().newEqExpr(transitionTakenLocal,
                                    IntConstant.v(1)), errorStmt));

                    // If transition taken, then store the next state
                    units.add(Jimple.v().newAssignStmt(transitionTakenLocal,
                            IntConstant.v(1)));
                    units.add(Jimple.v().newAssignStmt(
                            nextTransitionLocal,
                            IntConstant.v(entity.relationList().indexOf(
                                    transition))));

                    int nextStateIndex = entity.entityList().indexOf(
                            transition.destinationState());
                    units.add(Jimple.v().newAssignStmt(nextStateLocal,
                            IntConstant.v(nextStateIndex)));

                    // Generate code for the outputExpression of the guard.
                    for (Iterator actions = transition.choiceActionList()
                            .iterator(); actions.hasNext();) {
                        AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                                .next();
                        System.out.println("action = " + action);
                        _generateActionCode(entity, entityInstanceClass,
                                nameToField, nameToType, body, action);
                    }

                    units.add(skipStmt);
                }

                units.add(Jimple.v().newGotoStmt(finishedStmt));
            }

            units.add(errorStmt);

            // throw an exception.
            units.add(finishedStmt);

            Local exceptionLocal = SootUtilities.createRuntimeException(body,
                    errorStmt, "state error");
            units.insertBefore(Jimple.v().newThrowStmt(exceptionLocal),
                    errorStmt);

            // Store the next state.
            units.add(Jimple.v().newAssignStmt(
                    Jimple.v()
                            .newInstanceFieldRef(thisLocal, currentStateField),
                    nextStateLocal));

            // And the next Transition.
            units.add(Jimple.v().newAssignStmt(
                    Jimple.v().newInstanceFieldRef(thisLocal,
                            nextTransitionField), nextTransitionLocal));

            // return void
            units.add(Jimple.v().newReturnVoidStmt());

            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }
        // populate the postfire method.
        {
            System.out.println("create postfire()");

            SootMethod postfireMethod = new SootMethod("postfire",
                    Collections.EMPTY_LIST, BooleanType.v(), Modifier.PUBLIC);
            entityInstanceClass.addMethod(postfireMethod);

            JimpleBody body = Jimple.v().newBody(postfireMethod);
            postfireMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            Map transitionToStartStmt = new HashMap();
            List transitionStmtList = new LinkedList();
            int numberOfTransitions = entity.relationList().size();

            // Figure out what transition we are in.
            for (Iterator transitions = entity.relationList().iterator(); transitions
                    .hasNext();) {
                Transition transition = (Transition) transitions.next();
                Stmt startStmt = Jimple.v().newNopStmt();

                transitionToStartStmt.put(transition, startStmt);
                transitionStmtList.add(startStmt);
            }

            Local nextTransitionLocal = Jimple.v().newLocal(
                    "nextTransitionLocal", IntType.v());
            body.getLocals().add(nextTransitionLocal);

            units.add(Jimple.v().newAssignStmt(
                    nextTransitionLocal,
                    Jimple.v().newInstanceFieldRef(thisLocal,
                            nextTransitionField)));

            Stmt finishedStmt = Jimple.v().newNopStmt();
            Stmt errorStmt = Jimple.v().newNopStmt();

            // Get the current transition..
            units.add(Jimple.v().newTableSwitchStmt(nextTransitionLocal, 0,
                    numberOfTransitions - 1, transitionStmtList, errorStmt));

            // Generate code for each transition
            for (Iterator transitions = entity.relationList().iterator(); transitions
                    .hasNext();) {
                Transition transition = (Transition) transitions.next();
                Stmt startStmt = (Stmt) transitionToStartStmt.get(transition);
                units.add(startStmt);

                // Generate code for the commitExpression of the guard.
                for (Iterator actions = transition.commitActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();
                    _generateActionCode(entity, entityInstanceClass,
                            nameToField, nameToType, body, action);
                }

                // Generate code to reinitialize the target state, if
                // reset is true.
                TypedActor[] refinements = null;

                try {
                    BooleanToken resetToken = (BooleanToken) transition.reset
                            .getToken();

                    if (resetToken.booleanValue()) {
                        refinements = ((State) transition.destinationState())
                                .getRefinement();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }

                if (refinements != null) {
                    for (int i = 0; i < refinements.length; i++) {
                        TypedActor refinement = refinements[i];

                        Local containerLocal = Jimple.v().newLocal("container",
                                RefType.v(PtolemyUtilities.namedObjClass));
                        body.getLocals().add(containerLocal);

                        Local entityLocal = Jimple.v().newLocal("entity",
                                RefType.v(PtolemyUtilities.entityClass));
                        body.getLocals().add(entityLocal);

                        NamedObj containerModel = (NamedObj) entity
                                .getContainer();
                        String deepName = ((NamedObj) refinement)
                                .getName(containerModel);

                        units.add(Jimple.v().newAssignStmt(
                                containerLocal,
                                Jimple.v().newInterfaceInvokeExpr(thisLocal,
                                        PtolemyUtilities.getContainerMethod)));
                        units
                                .add(Jimple
                                        .v()
                                        .newAssignStmt(
                                                containerLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                containerLocal,
                                                                RefType
                                                                        .v(PtolemyUtilities.compositeActorClass))));
                        units.add(Jimple.v().newAssignStmt(
                                entityLocal,
                                Jimple.v().newVirtualInvokeExpr(containerLocal,
                                        PtolemyUtilities.getEntityMethod,
                                        StringConstant.v(deepName))));

                        units
                                .add(Jimple
                                        .v()
                                        .newAssignStmt(
                                                entityLocal,
                                                Jimple
                                                        .v()
                                                        .newCastExpr(
                                                                entityLocal,
                                                                RefType
                                                                        .v(PtolemyUtilities.compositeActorClass))));

                        SootMethod rinitializeMethod = SootUtilities
                                .searchForMethodByName(
                                        PtolemyUtilities.compositeActorClass,
                                        "initialize");
                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(entityLocal,
                                        rinitializeMethod)));
                    }
                }

                units.add(Jimple.v().newGotoStmt(finishedStmt));
            }

            units.add(errorStmt);
            units.add(finishedStmt);

            // return true
            units.add(Jimple.v().newReturnStmt(IntConstant.v(1)));

            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        // Remove super calls to the executable interface.
        // FIXME: This would be nice to do by inlining instead of
        // special casing.
        ModelTransformer.implementExecutableInterface(entityInstanceClass);

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        // Inline all methods in the class that are called from
        // within the class.
        ModelTransformer.inlineLocalCalls(entityInstanceClass);

        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    private static void _generateActionCode(Entity entity,
            SootClass entityClass, Map nameToField, Map nameToType,
            JimpleBody body, AbstractActionsAttribute action) {
        for (Iterator names = action.getDestinationNameList().iterator(); names
                .hasNext();) {
            String name = (String) names.next();
            String actionExpression = action.getExpression(name);
            Local outputTokenLocal = DataUtilities.generateExpressionCode(
                    entity, entityClass, actionExpression, nameToField,
                    nameToType, body);

            try {
                NamedObj destination = action.getDestination(name);

                if (destination instanceof TypedIOPort) {
                    // send the computed token
                    Local portLocal = Jimple.v().newLocal("port",
                            PtolemyUtilities.componentPortType);
                    body.getLocals().add(portLocal);

                    SootField portField = entityClass.getFieldByName(name);

                    body.getUnits().add(
                            Jimple.v().newAssignStmt(
                                    portLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            body.getThisLocal(), portField)));
                    body
                            .getUnits()
                            .add(
                                    Jimple
                                            .v()
                                            .newInvokeStmt(
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    portLocal,
                                                                    PtolemyUtilities.sendMethod,
                                                                    IntConstant
                                                                            .v(0),
                                                                    outputTokenLocal)));
                } else if (destination instanceof Parameter) {
                    // set the computed token
                    Local paramLocal = Jimple.v().newLocal("param",
                            RefType.v(PtolemyUtilities.variableClass));
                    body.getLocals().add(paramLocal);

                    Local containerLocal = Jimple.v().newLocal("container",
                            RefType.v(PtolemyUtilities.namedObjClass));
                    body.getLocals().add(containerLocal);

                    Local attributeLocal = Jimple.v().newLocal("attribute",
                            RefType.v(PtolemyUtilities.attributeClass));
                    body.getLocals().add(attributeLocal);

                    // Get a ref to the parameter through the container,
                    // since the parameter we are assigning to may be
                    // above us in the hierarchy.
                    NamedObj containerModel = (NamedObj) entity.getContainer();
                    String deepName = ((NamedObj) destination)
                            .getName(containerModel);

                    body
                            .getUnits()
                            .add(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    containerLocal,
                                                    Jimple
                                                            .v()
                                                            .newInterfaceInvokeExpr(
                                                                    body
                                                                            .getThisLocal(),
                                                                    PtolemyUtilities.getContainerMethod)));
                    body
                            .getUnits()
                            .add(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    attributeLocal,
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    containerLocal,
                                                                    PtolemyUtilities.getAttributeMethod,
                                                                    StringConstant
                                                                            .v(deepName))));

                    body
                            .getUnits()
                            .add(
                                    Jimple
                                            .v()
                                            .newAssignStmt(
                                                    paramLocal,
                                                    Jimple
                                                            .v()
                                                            .newCastExpr(
                                                                    attributeLocal,
                                                                    RefType
                                                                            .v(PtolemyUtilities.variableClass))));
                    body
                            .getUnits()
                            .add(
                                    Jimple
                                            .v()
                                            .newInvokeStmt(
                                                    Jimple
                                                            .v()
                                                            .newVirtualInvokeExpr(
                                                                    paramLocal,
                                                                    PtolemyUtilities.variableSetTokenMethod,
                                                                    outputTokenLocal)));
                } else {
                    throw new RuntimeException("unknown object");
                }
            } catch (IllegalActionException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }
}
