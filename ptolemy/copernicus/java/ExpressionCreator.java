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
import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.StringUtilities;
import soot.BooleanType;
import soot.FastHierarchy;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
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
public class ExpressionCreator implements AtomicActorCreator {
    /** Generate a new class with the given name that can take the
     *  place of the given actor.  Use the given options when
     *  necessary.  The given entity is assumed to be an expression actor.
     */
    public SootClass createAtomicActor(Entity actor, String newClassName,
            ConstVariableModelAnalysis constAnalysis, Map options) {
        Expression entity = (Expression) actor;
        SootClass entityClass = PtolemyUtilities.actorClass;

        // Create a class for the entity instance.
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
            // Populate the initialization method.
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();

            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Populate...
            // Initialize attributes that already exist in the class.
            ModelTransformer.createAttributes(body, entity, thisLocal, entity,
                    thisLocal, entityInstanceClass, tempCreatedMap);

            // Create and initialize ports
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

                SootField field = new SootField(StringUtilities.sanitizeName(
                                                        name) + "Token", type);
                entityInstanceClass.addField(field);
                nameToField.put(name, field);
            }
        }
        // Populate the fire method.
        {
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

                    SootField portField = entityInstanceClass.getFieldByName(StringUtilities
                            .sanitizeName(name));
                    units.add(Jimple.v().newAssignStmt(portLocal,
                                      Jimple.v().newInstanceFieldRef(thisLocal, portField)));
                    units.add(Jimple.v().newAssignStmt(hasTokenLocal,
                                      Jimple.v().newVirtualInvokeExpr(portLocal,
                                              PtolemyUtilities.hasTokenMethod,
                                              IntConstant.v(0))));

                    Stmt target = Jimple.v().newNopStmt();
                    units.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(hasTokenLocal,
                                                           IntConstant.v(0)), target));
                    units.add(Jimple.v().newAssignStmt(tokenLocal,
                                      Jimple.v().newVirtualInvokeExpr(portLocal,
                                              PtolemyUtilities.getMethod, IntConstant.v(0))));

                    SootField tokenField = entityInstanceClass.getFieldByName(name
                            + "Token");
                    units.add(Jimple.v().newAssignStmt(Jimple.v()
                                      .newInstanceFieldRef(thisLocal,
                                              tokenField), tokenLocal));
                    units.add(target);
                }
            }

            StringAttribute expressionAttribute = (StringAttribute) entity
                .getAttribute("expression");
            String expression = expressionAttribute.getExpression();

            Local local = DataUtilities.generateExpressionCode(entity,
                    entityInstanceClass, expression, nameToField, nameToType,
                    body);

            // send the computed token
            String name = "output";
            Local portLocal = Jimple.v().newLocal("port",
                    PtolemyUtilities.componentPortType);
            body.getLocals().add(portLocal);

            SootField portField = entityInstanceClass.getFieldByName(name);

            units.add(Jimple.v().newAssignStmt(portLocal,
                              Jimple.v().newInstanceFieldRef(thisLocal, portField)));
            units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(portLocal,
                                                       PtolemyUtilities.sendMethod, IntConstant.v(0), local)));

            // return void
            units.add(Jimple.v().newReturnVoidStmt());

            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
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
}
