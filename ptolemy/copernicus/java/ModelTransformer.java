/* A transformer that creates a class for the toplevel of a model

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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.copernicus.gui.GeneratorTableauAttribute;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.Documentation;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import soot.BooleanType;
import soot.FastHierarchy;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;


//////////////////////////////////////////////////////////////////////////
//// ModelTransformer
/**
A transformer that creates a class to represent the model specified in
the constructor.  This transformer creates instance classes for each
actor in the model, and generates code for composite actors that
instantiates singleton instances of those classes.  Additionally code
is generated in composite actor classes to create links and relations
between actors.

<p> The class generates code for composite actors itself.  For atomic
actors, it defers to various implementations of the AtomicActorCreator
class.  This allows customized code to be generated for various atomic
actors.  By default, this class defers to a GenericAtomicActorCreator.
That creator simply copies the existing actor specification code and
specializes it.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class ModelTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private ModelTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     */
    public static ModelTransformer v(CompositeActor model) {
        return new ModelTransformer(model);
    }

    /** Generate code in the given body of the given class before the
     *  given statement to compute the values of the given list of
     *  attributes.  The value of the variable will be computed using
     *  generated code, and then set into the variable.  The given
     *  class is assumed to be associated with the given context.
     *  @param body The body to generate code in.
     *  @param insertPoint A statement in the given body.
     *  @param context The named object corresponding to the class in which
     *  code is being generated.
     *  @param contextLocal A local in the given body that points to
     *  an instance of the given class.
     *  @param namedObj The named object that contains attributes.
     *  @param namedObjLocal A local in the given body.  Attributes will be
     *  created using this local as the container.
     *  @param theClass The soot class being modified.
     *  @param attributeList The list of attributes.
     */
    public static void computeAttributesBefore(
            JimpleBody body, Stmt insertPoint,
            NamedObj context, Local contextLocal,
            NamedObj namedObj, Local namedObjLocal,
            SootClass theClass, List attributeList) {

        // Check to see if we have anything to do.
        if (namedObj.attributeList().size() == 0) return;

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

        for (Iterator attributes = namedObj.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();

            if (_isIgnorableAttribute(attribute) ||
                    !attributeList.contains(attribute)) {
                continue;
            }

            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = getFieldNameForAttribute(
                    attribute, context);

            Local local = attributeLocal;
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            attributeLocal,
                            Jimple.v().newVirtualInvokeExpr(contextLocal,
                                    PtolemyUtilities.getAttributeMethod,
                                    StringConstant.v(attributeName))),
                    insertPoint);

            if (attribute instanceof Variable) {
                // If the attribute is a parameter, then generateCode...
                Local tokenLocal =
                    DataUtilities.generateExpressionCodeBefore(
                            (Entity)namedObj, theClass,
                            ((Variable)attribute).getExpression(),
                            new HashMap(), new HashMap(), body, insertPoint);

                // cast to Variable.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                variableLocal,
                                Jimple.v().newCastExpr(
                                        local,
                                        variableType)),
                        insertPoint);

                // call setToken.
                body.getUnits().insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(
                                        variableLocal,
                                        PtolemyUtilities.variableSetTokenMethod,
                                        tokenLocal)),
                        insertPoint);
                // call validate to ensure that attributeChanged is called.
                body.getUnits().insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newInterfaceInvokeExpr(
                                        variableLocal,
                                        PtolemyUtilities.validateMethod)),
                        insertPoint);

            }

            // recurse so that we get all parameters deeply.
            computeAttributesBefore(
                    body, insertPoint,
                    context, contextLocal,
                    attribute, local, theClass, attributeList);
        }
    }

    /** Generate code in the given body of the given class to create
     *  attributes contained by the given named object.  The given
     *  class is assumed to be associated with the given context.
     *  Attributes whose full names are already in createdSet are not
     *  created.  The given createdSet is update with the full names
     *  of all the created attributes.
     *  @param body The body to generate code in.
     *  @param context The named object corresponding to the class in which
     *  code is being generated.
     *  @param contextLocal A local in the given body that points to
     *  an instance of the given class.
     *  @param namedObj The named object that contains attributes.
     *  @param namedObjLocal A local in the given body.  Attributes will be
     *  created using this local as the container.
     *  @param theClass The soot class being modified.
     *  @param createdSet A set of the full names of ptolemy objects.
     */
    public static void createAttributes(JimpleBody body,
            NamedObj context, Local contextLocal,
            NamedObj namedObj, Local namedObjLocal,
            SootClass theClass, HashSet createdSet) {

        //   System.out.println("initializing attributes in " + namedObj);

        // Check to see if we have anything to do.
        if (namedObj.attributeList().size() == 0) return;


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

        for (Iterator attributes = namedObj.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();

            if (_isIgnorableAttribute(attribute)) {
                continue;
            }

            SootClass attributeClass = (SootClass)
                _objectToClassMap.get(attribute);
            if(attributeClass == null) {
                attributeClass = Scene.v().loadClassAndSupport(attribute.getClass().getName());
            }
            String className = attributeClass.getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = getFieldNameForAttribute(
                    attribute, context);

            Local local;
            if (createdSet.contains(attribute.getFullName())) {
                //     System.out.println("already has " + attributeName);
                // If the class for the object already creates the
                // attribute, then get a reference to the existing attribute.
                // Note that if the class creates the attribute, but
                // doesn't also create a field for it, that we will
                // fail later when we try to replace getAttribute
                // calls with references to fields.
                local = attributeLocal;
                body.getUnits().add(
                        Jimple.v().newAssignStmt(attributeLocal,
                                Jimple.v().newVirtualInvokeExpr(contextLocal,
                                        PtolemyUtilities.getAttributeMethod,
                                        StringConstant.v(attributeName))));
            } else {
                //System.out.println("creating " + attribute.getFullName());
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        namedObjLocal, attribute.getName());
                // System.out.println("created local");
                
                // NOTE: Assume that attributes that contain other
                // attributes must implement a workspace constructor
                try {
                    Attribute classAttribute =
                        (Attribute)_findDeferredInstance(attribute);
                    updateCreatedSet(namedObj.getFullName() + "."
                            + attribute.getName(),
                            classAttribute, classAttribute, createdSet);
                } catch (Exception ex) {
                    createdSet.add(namedObj.getFullName() + "."
                            + attribute.getName());
                }
            }

            // System.out.println("creating new field");
            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
            SootField field = 
                SootUtilities.createAndSetFieldFromLocal(body, local,
                        theClass, attributeType, fieldName);
            field.addTag(new ValueTag(attribute));
                    
            createAttributes(body, context, contextLocal,
                    attribute, local, theClass, createdSet);
        }
    }

    /** Generate code in the given body of the given class to create
     *  ports contained by the given entity.  The given
     *  class is assumed to be associated with the given context.
     *  Ports whose full names are already in createdSet are not
     *  created.  The given createdSet is updated with the full names
     *  of all the created ports.
     *  @param body The body to generate code in.
     *  @param context The named object corresponding to the class in which
     *  code is being generated.
     *  @param contextLocal A local in the given body that points to
     *  an instance of the given class.
     *  @param entity The entity that contains ports.
     *  @param entityLocal A local in the given body.  Ports will be
     *  created using this local as the container.
     *  @param theClass The soot class being modified.
     *  @param createdSet A set of the full names of ptolemy objects.
     */
    public static void createPorts(JimpleBody body, Local contextLocal,
            Entity context, Local entityLocal,
            Entity entity, EntitySootClass theClass, HashSet createdSet) {
        Entity classObject = (Entity)_findDeferredInstance(entity);

        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v(PtolemyUtilities.componentPortClass));
        body.getLocals().add(tempPortLocal);

        for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
            Port port = (Port)ports.next();
            //   System.out.println("ModelTransformer: port: " + port);
                   
            String className = port.getClass().getName();
            String portName = port.getName(context);
            String fieldName = getFieldNameForPort(port, context);
            RefType portType = RefType.v(className);
            Local portLocal = Jimple.v().newLocal("port",
                    portType);
            body.getLocals().add(portLocal);

            // Deal with ParameterPorts specially, since they are
            // created by the PortParameter.  Just use the
            // portParameter to get a reference to the ParameterPort.
            if(port instanceof ParameterPort) {
                updateCreatedSet(entity.getFullName() + "."
                        + port.getName(),
                        port, port, createdSet);
                PortParameter parameter = ((ParameterPort)port).getParameter();
                Local parameterLocal = Jimple.v().newLocal("parameter",
                        RefType.v(PtolemyUtilities.portParameterClass));
                body.getLocals().add(parameterLocal);
                body.getUnits().add(
                        Jimple.v().newAssignStmt(parameterLocal,
                                Jimple.v().newVirtualInvokeExpr(contextLocal,
                                        PtolemyUtilities.getAttributeMethod,
                                        StringConstant.v(parameter.getName(context)))));
              
                // If the class for the object already creates the
                // port, then get a reference to the existing port.
                // First assign to temp
                body.getUnits().add(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        parameterLocal,
                                        PtolemyUtilities.portParameterGetPortMethod)));
            } else if (createdSet.contains(port.getFullName())) {
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
            } else {
                //     System.out.println("Creating new!");
                // If the class does not create the port
                // then create a new port with the right name.
                Local local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        entityLocal, port.getName());
                // Record the name of the created port.
                // NOTE: we assume that this is only a TypedIOPort, which
                // contains no other objects!
             //    Port classPort =
//                     (Port)_findDeferredInstance(port);
           //      updateCreatedSet(entity.getFullName() + "."
//                         + port.getName(),
//                         classPort, classPort, createdSet);
                createdSet.add(entity.getFullName() + "."
                        + port.getName());
                // Then assign to portLocal.
                body.getUnits().add(
                        Jimple.v().newAssignStmt(portLocal,
                                local));
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

            if (!theClass.declaresFieldByName(fieldName)) {
                SootField field = 
                    SootUtilities.createAndSetFieldFromLocal(body,
                            portLocal, theClass, RefType.v(className),
                            fieldName);
                field.addTag(new ValueTag(port));
            }
            createAttributes(body, context, contextLocal,
                    port, portLocal, theClass, createdSet);
        }
    }

    /** Generate code in the given body of the given class before the
     *  given statement to set the values of variables and settable
     *  attributes contained by the given named object.  The given
     *  class is assumed to be associated with the given context.
     *  @param body The body to generate code in.
     *  @param insertPoint A statement in the given body.
     *  @param context The named object corresponding to the class in which
     *  code is being generated.
     *  @param contextLocal A local in the given body that points to
     *  an instance of the given class.
     *  @param namedObj The named object that contains attributes.
     *  @param namedObjLocal A local in the given body.  Attributes will be
     *  created using this local as the container.
     *  @param theClass The soot class being modified.
     */
    public static void initializeAttributesBefore(
            JimpleBody body, Stmt insertPoint,
            NamedObj context, Local contextLocal,
            NamedObj namedObj, Local namedObjLocal,
            SootClass theClass) {

        System.out.println("initializing attributes in " + namedObj);

        // Check to see if we have anything to do.
        if (namedObj.attributeList().size() == 0) return;

        Type variableType = RefType.v(PtolemyUtilities.variableClass);

        // A local that we will use to set the value of our
        // settable attributes.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                PtolemyUtilities.attributeType);
        body.getLocals().add(attributeLocal);
        Local settableLocal = Jimple.v().newLocal("settable",
                PtolemyUtilities.settableType);
        body.getLocals().add(settableLocal);

        // A list of locals that we will validate.
        List validateLocalsList = new LinkedList();

        for (Iterator attributes = namedObj.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();

            if (_isIgnorableAttribute(attribute)) {
                continue;
            }

            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = getFieldNameForAttribute(
                    attribute, context);

            Local local = attributeLocal;
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            attributeLocal,
                            Jimple.v().newVirtualInvokeExpr(contextLocal,
                                    PtolemyUtilities.getAttributeMethod,
                                    StringConstant.v(attributeName))),
                    insertPoint);

            if (attribute instanceof Variable) {
                // If the attribute is a parameter, then set its
                // token to the correct value.

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
                            insertPoint, token, "token");

                Local variableLocal = Jimple.v().newLocal("variable",
                        variableType);
                body.getLocals().add(variableLocal);

                // cast to Variable.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                variableLocal,
                                Jimple.v().newCastExpr(
                                        local,
                                        variableType)),
                        insertPoint);

                // call setToken.
                body.getUnits().insertBefore(

                        Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(
                                        variableLocal,
                                        PtolemyUtilities.variableSetTokenMethod,
                                        tokenLocal)),
                        insertPoint);

                // Store that we will call validate to ensure that
                // attributeChanged is called.
                validateLocalsList.add(variableLocal);

            } else if (attribute instanceof Settable) {
                // If the attribute is settable, then set its
                // expression.

                // cast to Settable.
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                settableLocal,
                                local),
                        insertPoint);

                String expression = ((Settable)attribute).getExpression();

                // call setExpression.
                body.getUnits().insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newInterfaceInvokeExpr(
                                        settableLocal,
                                        PtolemyUtilities.setExpressionMethod,
                                        StringConstant.v(expression))),
                        insertPoint);
                // call validate to ensure that attributeChanged is called.
                body.getUnits().insertBefore(
                        Jimple.v().newInvokeStmt(
                                Jimple.v().newInterfaceInvokeExpr(
                                        settableLocal,
                                        PtolemyUtilities.validateMethod)),
                        insertPoint);
            }
           
            // recurse so that we get all parameters deeply.
            initializeAttributesBefore(body, insertPoint,
                    context, contextLocal,
                    attribute, local, theClass);
        }

        for (Iterator validateLocals = validateLocalsList.iterator();
             validateLocals.hasNext();) {
            Local validateLocal = (Local)validateLocals.next();
            // Validate local params
            body.getUnits().insertBefore(
                    Jimple.v().newInvokeStmt(
                            Jimple.v().newInterfaceInvokeExpr(
                                    validateLocal,
                                    PtolemyUtilities.validateMethod)),
                    insertPoint);
        }
       
        if(namedObj instanceof Entity) {
            Entity entity = (Entity)namedObj;
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                Port port = (Port)ports.next();
                Local portLocal = Jimple.v().newLocal("port",
                        RefType.v(PtolemyUtilities.portClass));
                body.getLocals().add(portLocal);
                String portName = port.getName(context);
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                portLocal,
                                Jimple.v().newVirtualInvokeExpr(contextLocal,
                                        PtolemyUtilities.getPortMethod,
                                        StringConstant.v(portName))),
                        insertPoint);
                // recurse so that we get all parameters deeply.
                initializeAttributesBefore(body, insertPoint,
                        context, contextLocal,
                        port, portLocal, theClass);
            }
        }
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage";
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForAttribute(Attribute attribute,
            NamedObj context) {
        return StringUtilities.sanitizeName(attribute.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForEntity(Entity entity,
            NamedObj context) {
        return StringUtilities.sanitizeName(entity.getName(context));
    }

    /** Given an entity that we are generating code for, return a
     *  reference to the instance field created for that entity.
     *  @exception RuntimeException If no field was created for the
     *  given entity.
     */
    public static Entity getEntityForField(SootField field) {
        Entity entity = (Entity) _fieldToEntityMap.get(field);
        if (entity != null) {
            return entity;
        } else {
            throw new RuntimeException(
                    "Failed to find entity for field " + field);
        }
    }

    /** Assert that the given field always points to the given entity.
     */
    public static void addFieldForEntity(SootField field, Entity entity) {
        _fieldToEntityMap.put(field, entity);
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForPort(Port port, NamedObj context) {
        if(port instanceof ParameterPort) {
            return StringUtilities.sanitizeName(port.getName(context) + "Port");
        } else {
            return StringUtilities.sanitizeName(port.getName(context));
        }
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForRelation(Relation relation,
            NamedObj context) {
        return StringUtilities.sanitizeName(relation.getName(context));
    }

    /** Return the entity for the given class if the given class is a
     *  class being generated, or null if not.
     */
    public static Entity getActorForClass(SootClass theClass) {
        Entity entity = (Entity) _classToObjectMap.get(theClass);
        return entity;
    }

    /** Return the entity for the given class if the given class is a
     *  class being generated, or null if not.
     */
    public static SootClass getClassForActor(Entity entity) {
        return (SootClass) _objectToClassMap.get(entity);
    }

    /** Return true if the given class was generated for an actor in the model
     */
    public static boolean isActorClass(SootClass theClass) {
        return _actorClasses.contains(theClass);
    }

    /**
     *  Associate the given class, which has been created, with the
     *  given attribute.  This allows references to the given class to
     *  be resolved as references to the given attribute.
     */
    public static void addAttributeForClass(
            SootClass theClass, Attribute attribute) {
        _classToObjectMap.put(theClass, attribute);
        _objectToClassMap.put(attribute, theClass);
        _attributeClasses.add(theClass);
    }

    /**
     *  Associate the given class, which has been created, with the
     *  given attribute.  This allows references to the given class to
     *  be resolved as references to the given attribute.
     */
    public static void addActorForClass(
            SootClass theClass, Entity entity) {
        _classToObjectMap.put(theClass, entity);
        _objectToClassMap.put(entity, theClass);
        _actorClasses.add(theClass);
    }
    
    /**
     *  Return the list of classes that correspond to actors.
     */
    public static List actorClassList() {
        return Collections.unmodifiableList(_actorClasses);
    }

    /**
     *  Return the list of classes that correspond to attributes.
     */
    public static List attributeClassList() {
        return Collections.unmodifiableList(_attributeClasses);
    }

    /** Return the object that the given class was generated for.
     */
    public static NamedObj getObjectForClass(
            SootClass theClass) {
        return (NamedObj)_classToObjectMap.get(theClass);
    }

    /**
     *  @exception RuntimeException If no field was created for the
     *  given attribute.
     */
    public static Attribute getAttributeForClass(SootClass theClass) {
        Attribute attribute = (Attribute) _classToObjectMap.get(theClass);
        if (attribute != null) {
            return attribute;
        } else {
            throw new RuntimeException(
                    "Failed to find attribute for class " + theClass);
        }
    }

    /** Return the name of the class that is generated for the given
     *  named object.  This name is guaraunteed to be unique among the
     *  classes being generated.
     */
    public static String getInstanceClassName(NamedObj object, Map options) {
        // Note that we use sanitizeName because entity names can have
        // spaces, and append leading characters because entity names
        // can start with numbers.
        NamedObj toplevel = object.toplevel();
        return PhaseOptions.getString(options, "targetPackage")
            + "." + StringUtilities.sanitizeName(toplevel.getName()) + "_"
            + StringUtilities.sanitizeName(object.getName(object.toplevel()));
    }

    /** Return the model class created during the most recent
     *  execution of this transformer.
     */
    public static SootClass getModelClass() {
        return _modelClass;
    }

    /** Return the name of the class that will be created for the
     *  given model.
     */
    public static String getModelClassName(CompositeActor model, Map options) {
        // Note that we use sanitizeName because entity names can have
        // spaces, and append leading characters because entity names
        // can start with numbers.
        return PhaseOptions.getString(options, "targetPackage")
            + "." + StringUtilities.sanitizeName(model.getName());
    }

    /** Return the name of the field that references the container of
     * a generated class.
     */
    public static String getContainerFieldName() {
        return "_CGContainer";
    }

    /** Transform the given class so that it properly implements the
     *  ptolemy Executable interface.  If any of those methods not
     *  implemented directly by the given class, then they are created
     *  and given minimal bodies.  The generated prefire() and
     *  postfire() methods return true.
     *  @param theClass The class to transform.
     */
    public static void implementExecutableInterface(SootClass theClass) {
        // Loop through all the methods and remove calls to super.
        for (Iterator methods = theClass.getMethods().iterator();
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
                if (value instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                    if (PtolemyUtilities.executableInterface.declaresMethod(
                            r.getMethod().getSubSignature())) {
                        boolean isNonVoidMethod =
                            r.getMethod().getName().equals("prefire") ||
                            r.getMethod().getName().equals("postfire");
                        if (isNonVoidMethod && stmt instanceof AssignStmt) {
                            box.setValue(IntConstant.v(1));
                        } else {
                            body.getUnits().remove(stmt);
                        }
                    }
                }
            }
        }

        // The initialize method implemented in the actor package is weird,
        // because it calls getDirector.  Since we don't need it,
        // make sure that we never call the baseclass initialize method.
        if (!theClass.declaresMethodByName("preinitialize")) {
            SootMethod method = new SootMethod("preinitialize",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("initialize")) {
            SootMethod method = new SootMethod("initialize",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("prefire")) {
            SootMethod method = new SootMethod("prefire",
                    Collections.EMPTY_LIST, BooleanType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnStmt(
                    IntConstant.v(1)));
        }
        if (!theClass.declaresMethodByName("fire")) {
            SootMethod method = new SootMethod("fire",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        if (!theClass.declaresMethodByName("postfire")) {
            SootMethod method = new SootMethod("postfire",
                    Collections.EMPTY_LIST, BooleanType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnStmt(
                    IntConstant.v(1)));
        }
        if (!theClass.declaresMethodByName("wrapup")) {
            SootMethod method = new SootMethod("wrapup",
                    Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
    }

    /** Inline invocation sites from methods in the given class to
     *  another method in the given class that are problematic.  This
     *  primarily includes any method that takes or returns a
     *  NamedObj.
     *  @param theClass The class to transform.
     */
    public static void inlineLocalCalls(SootClass theClass) {//Dangerous
        // FIXME: what if the inlined code contains another call
        // to this class???
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (!stmt.containsInvokeExpr()) {
                    continue;
                }
                InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                SootMethod targetMethod = r.getMethod();
                // Don't inline obviously recursive methods.
                if (targetMethod == method) {
                    continue;
                } 
                // Don't inline methods invoked on the super
                // class. (These get taken care of later explicitly)
                if (!targetMethod.getDeclaringClass().equals(theClass)) {
                    continue;
                }
                boolean isCGInitMethod = 
                    targetMethod.getName().equals("__CGInit");       
                // Avoid inlining methods that don't take or return
                // named objects, except for the CGInit method, which
                // is always inlined.
                boolean hasDangerousType = false;
                
                {
                    Type type = targetMethod.getReturnType();
                    if (type instanceof RefType) {
                        SootClass typeClass = ((RefType)type).getSootClass();
                        if(SootUtilities.derivesFrom(typeClass,
                                   PtolemyUtilities.namedObjClass)) {
                            hasDangerousType = true;
                        }
                    }
                }
                for (Iterator argTypes = 
                         targetMethod.getParameterTypes().iterator();
                     argTypes.hasNext();) {
                    Type type = (Type)argTypes.next();
                    if (type instanceof RefType) {
                        SootClass typeClass = ((RefType)type).getSootClass();
                        if(SootUtilities.derivesFrom(typeClass,
                                   PtolemyUtilities.namedObjClass)) {
                            hasDangerousType = true;
                        }
                    }
                }                         
                if (!isCGInitMethod && !hasDangerousType) {
                    continue;
                }

                // System.err.println("inlining method " + r.getMethod());
                // FIXME: What if more than one method could be
                // called?
                SiteInliner.inlineSite(r.getMethod(), stmt, method);
                // Inline other NamedObj methods here, too..

                // FIXME: avoid inlining method calls
                // that don't have tokens in them
            }
        }
    }

    /** Add the full names of all named objects contained in the given object
     *  to the given set, assuming that the object is contained within the
     *  given context.
     *  @param context The context.
     *  @param object The object being recorded.
     *  @param set A set of full names of ptolemy objects.
     */
    public static void updateCreatedSet(String prefix,
            NamedObj context, NamedObj object, HashSet set) {
        if (object == context) {
            System.out.println("creating " + prefix);
            set.add(prefix);
        } else {
            String name = prefix + "." + object.getName(context);
            System.out.println("creating " + name);
            set.add(name);
        }
        if (object instanceof CompositeActor) {
            CompositeActor composite = (CompositeActor) object;
            for (Iterator entities = composite.deepEntityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                updateCreatedSet(prefix, context, entity, set);
            }
            for (Iterator relations = composite.relationList().iterator();
                 relations.hasNext();) {
                Relation relation = (Relation) relations.next();
                updateCreatedSet(prefix, context, relation, set);
            }
        }
        if (object instanceof Entity) {
            Entity entity= (Entity) object;
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                Port port = (Port)ports.next();
                updateCreatedSet(prefix, context, port, set);
            }
        }
        for (Iterator attributes = object.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
            updateCreatedSet(prefix, context, attribute, set);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ModelTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _entityLocalMap = new HashMap();
        _portLocalMap = new HashMap();
        try {
            _constAnalysis = new ConstVariableModelAnalysis(_model);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to calculate constant vars: " +
                    ex.getMessage());
        }

        // Some indexes, to make it easier to resolve an actor object,
        // given the objects class, and vice versa.
        _entityToFieldMap = new HashMap();
        _fieldToEntityMap = new HashMap();
     
        _classToObjectMap = new HashMap();
        _objectToClassMap = new HashMap();

        _actorClasses = new LinkedList();
        _attributeClasses = new LinkedList();

        // Create a class for the model
        String modelClassName = getModelClassName(_model, options);

        _modelClass = _createCompositeActor(
                _model, modelClassName, options);
        addActorForClass(_modelClass, _model);

        // Create static instance fields for each actor.
        //        _createEntityInstanceFields(_modelClass, _model, options);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create and set entities.
    private static void _entities(JimpleBody body,
            Local containerLocal, CompositeActor container,
            Local thisLocal, CompositeActor composite,
            EntitySootClass modelClass,
            HashSet createdSet, Map options) {
        CompositeActor classObject = (CompositeActor)
            _findDeferredInstance(composite);
        // A local that we will use to get existing entities
        Local entityLocal = Jimple.v().newLocal("entity",
                PtolemyUtilities.componentEntityType);
        body.getLocals().add(entityLocal);

        for (Iterator entities = composite.deepEntityList().iterator();
             entities.hasNext();) {
            Entity entity = (Entity)entities.next();
            //            System.out.println("ModelTransformer: entity: " + entity);

            // If we are doing deep codegen, then use the actor
            // classes we created earlier.
            String className = getInstanceClassName(entity, options);
            String entityName =
                entity.getName(container);
            String entityFieldName =
                getFieldNameForEntity(entity, container);
            Local local;

            if (createdSet.contains(entity.getFullName())) {
                //     System.out.println("already created!");
                local = Jimple.v().newLocal("entity",
                        PtolemyUtilities.componentEntityType);
                body.getLocals().add(local);
                body.getUnits().add(
                        Jimple.v().newAssignStmt(entityLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        thisLocal,
                                        PtolemyUtilities.getEntityMethod,
                                        StringConstant.v(
                                                entity.getName(composite)))));
                // and then cast
                body.getUnits().add(
                        Jimple.v().newAssignStmt(local,
                                Jimple.v().newCastExpr(entityLocal,
                                        PtolemyUtilities.componentEntityType)));
                SootField field = SootUtilities.createAndSetFieldFromLocal(
                        body, local, modelClass,
                        RefType.v(className), entityFieldName);
                field.addTag(new ValueTag(entity));
                
                _ports(body, thisLocal, composite,
                        local, entity, modelClass, createdSet, false);
            } else {
                //  System.out.println("Creating new!");

                // Create a new local variable.  The name of the local is
                // determined automatically.  The name of the NamedObj is
                // the same as in the model.  (Note that this might not be
                // a valid Java identifier.)
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        thisLocal, entity.getName());

                Entity classEntity =
                    (Entity)_findDeferredInstance(entity);
                // This class assumes that any entity that is created creates
                // all stuff inside it.
                updateCreatedSet(
                        composite.getFullName() + "." + entity.getName(),
                        entity, entity, createdSet);

                SootField field = SootUtilities.createAndSetFieldFromLocal(
                        body, local, modelClass,
                        RefType.v(className), entityFieldName);
                field.addTag(new ValueTag(entity));

                _ports(body, containerLocal, container,
                        local, entity, modelClass, createdSet, false);
                //   }
            }

            _entityLocalMap.put(entity, local);
        }
    }

    // Create and set external ports.
    private static void _ports(JimpleBody body, Local contextLocal,
            Entity context, Local entityLocal,
            Entity entity, EntitySootClass modelClass, HashSet createdSet,
            boolean createFieldsInClass) {
        Entity classObject = (Entity)_findDeferredInstance(entity);

        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v(PtolemyUtilities.componentPortClass));
        body.getLocals().add(tempPortLocal);

        for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
            Port port = (Port)ports.next();
            //System.out.println("ModelTransformer: port: " + port);

                 
            String className = port.getClass().getName();
            String portName = port.getName(context);
            String fieldName = getFieldNameForPort(port, context);
            RefType portType = RefType.v(className);
            Local portLocal = Jimple.v().newLocal("port",
                    portType);
            body.getLocals().add(portLocal);

            // Ignore ParameterPorts, since they are created by the
            // PortParameter.  Just use the portParameter to get a
            // reference to the ParameterPort.
            if(port instanceof ParameterPort) {
                updateCreatedSet(entity.getFullName() + "."
                        + port.getName(),
                        port, port, createdSet);
                PortParameter parameter = ((ParameterPort)port).getParameter();
                Local parameterLocal = Jimple.v().newLocal("parameter",
                        RefType.v(PtolemyUtilities.portParameterClass));
                body.getLocals().add(parameterLocal);
                body.getUnits().add(
                        Jimple.v().newAssignStmt(parameterLocal,
                                Jimple.v().newVirtualInvokeExpr(contextLocal,
                                        PtolemyUtilities.getAttributeMethod,
                                        StringConstant.v(parameter.getName(context)))));
              
                // If the class for the object already creates the
                // port, then get a reference to the existing port.
                // First assign to temp
                body.getUnits().add(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        parameterLocal,
                                        PtolemyUtilities.portParameterGetPortMethod)));
            } else if (createdSet.contains(port.getFullName())) {
                //    System.out.println("already created!");
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
            } else {
                //    System.out.println("Creating new!");
                // If the class does not create the port
                // then create a new port with the right name.
                Local local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        entityLocal, port.getName());
//                 updateCreatedSet(entity.getFullName() + "."
//                         + port.getName(),
//                         port, port, createdSet);
                createdSet.add(entity.getFullName() + "."
                        + port.getName());
                // Then assign to portLocal.
                body.getUnits().add(
                        Jimple.v().newAssignStmt(portLocal,
                                local));
                if (port instanceof TypedIOPort) {
                    TypedIOPort ioport = (TypedIOPort)port;
                    if (ioport.isInput()) {
                        body.getUnits().add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(local,
                                                PtolemyUtilities.setInputMethod,
                                                IntConstant.v(1))));
                    }
                    if (ioport.isOutput()) {
                        body.getUnits().add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(local,
                                                PtolemyUtilities.setOutputMethod,
                                                IntConstant.v(1))));
                    }
                    if (ioport.isMultiport()) {
                        body.getUnits().add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(local,
                                                PtolemyUtilities.setMultiportMethod,
                                                IntConstant.v(1))));
                    }
                    // Set the port's type.
                    Local ioportLocal =
                        Jimple.v().newLocal("typed_" + port.getName(),
                                PtolemyUtilities.ioportType);
                    body.getLocals().add(ioportLocal);
                    Stmt castStmt = Jimple.v().newAssignStmt(ioportLocal,
                            Jimple.v().newCastExpr(local,
                                    PtolemyUtilities.ioportType));
                    body.getUnits().add(castStmt);
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
                portLocal = local;
                // Create attributes for the port.
                createAttributes(body, context, contextLocal,
                        port, portLocal, modelClass, createdSet);
            }

            _portLocalMap.put(port, portLocal);
            if (createFieldsInClass &&
                    !modelClass.declaresFieldByName(fieldName)) {
                SootField field = 
                    SootUtilities.createAndSetFieldFromLocal(body,
                            portLocal, modelClass, portType, fieldName);
                field.addTag(new ValueTag(port));
            }
        }
    }

    // Create and set links.
    private static void _links(JimpleBody body, CompositeActor composite) {
        // To get the ordering right,
        // we read the links from the ports, not from the relations.
        // First, produce the inside links on contained ports.
        for (Iterator ports = composite.portList().iterator();
             ports.hasNext();) {
            ComponentPort port = (ComponentPort)ports.next();
            Iterator relations = port.insideRelationList().iterator();
            int index = -1;
            while (relations.hasNext()) {
                index++;
                ComponentRelation relation
                    = (ComponentRelation)relations.next();
                if (relation == null) {
                    // Gap in the links.  The next link has to use an
                    // explicit index.
                    continue;
                }
                Local portLocal = (Local)_portLocalMap.get(port);
                Local relationLocal = (Local)_relationLocalMap.get(relation);
                // call the _insertLink method with the current index.
                body.getUnits().add(Jimple.v().newInvokeStmt(
                        Jimple.v().newVirtualInvokeExpr(portLocal,
                                PtolemyUtilities.insertLinkMethod,
                                IntConstant.v(index),
                                relationLocal)));

            }
        }
    }

    // Produce the links on ports contained by contained entities.
    // Note that we have to carefully handle transparent hierarchy.
    private static void _linksOnPortsContainedByContainedEntities(
            JimpleBody body, CompositeActor composite) {

        for (Iterator entities = composite.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity =(ComponentEntity)entities.next();
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                ComponentPort port = (ComponentPort)ports.next();

                Local portLocal;
                // If we already have a local reference to the port
                if (_portLocalMap.keySet().contains(port)) {
                    // then just get the reference.
                    portLocal = (Local)_portLocalMap.get(port);
                } else {
                    throw new RuntimeException("Found a port: " + port +
                            " that does not have a local variable!");
                }

                List connectedPortList = port.connectedPortList();
                Iterator relations = port.linkedRelationList().iterator();
                int index = -1;
                while (relations.hasNext()) {
                    index++;
                    ComponentRelation relation
                        = (ComponentRelation)relations.next();
                    if (relation == null) {
                        // Gap in the links.  The next link has to use an
                        // explicit index.
                        continue;
                    }
                    Local relationLocal = (Local)
                        _relationLocalMap.get(relation);

                    // FIXME: Special case the transparent
                    // hierarchy...  find the right relation that IS
                    // in the relationLocalMap.  I have no idea how to
                    // do this without reimplementing a bunch of stuff
                    // that I don't really understand about how the
                    // kernel handles receivers.  Basically, there is
                    // no way to traverse the hierarchy without
                    // getting receivers, and in this case I don't
                    // want to do that because I actually want to find
                    // a relation!
                    if (relationLocal == null) {
                        throw new RuntimeException("Transparent hierarchy is" +
                                " not supported...");
                    }

                    // Call the _insertLink method with the current index.
                    body.getUnits().add(Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.insertLinkMethod,
                                    IntConstant.v(index),
                                    relationLocal)));
                }

            }
        }
    }

    // Create and set relations.
    private static void _relations(JimpleBody body, Local thisLocal,
            CompositeActor composite, EntitySootClass modelClass) {
        _relationLocalMap = new HashMap();
        for (Iterator relations = composite.relationList().iterator();
             relations.hasNext();) {
            Relation relation = (Relation)relations.next();
            String className = relation.getClass().getName();
            String fieldName = getFieldNameForRelation(relation, composite);
            // Create a new local variable.
            Local local =
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, relation.getName());
            _relationLocalMap.put(relation, local);

//             SootUtilities.createAndSetFieldFromLocal(body,
//                     local, modelClass, PtolemyUtilities.relationType,
//                     fieldName);
        }
    }

    /** Return an instance that represents the class that
     *  the given object defers to.
     */
    // FIXME: duplicate with MoMLWriter.
    public static NamedObj _findDeferredInstance(NamedObj object) {
        //  System.out.println("findDeferred =" + object.getFullName());
        NamedObj deferredObject = null;
        boolean isClass = false;
        if (object instanceof Prototype) {
            deferredObject = ((Prototype)object).getDeferTo();
            isClass = ((Prototype)object).isClassDefinition();
        }
        NamedObj.MoMLInfo info = object.getMoMLInfo();
        if (deferredObject == null && info.className != null) {
            try {
                // First try to find the local moml class that
                // we extend
                String deferredClass = info.className;

                // No moml class..  must have been a java class.
                // FIXME: This sucks.  We should integrate with
                // the classloader mechanism.
                String objectType;
                if (object instanceof Attribute) {
                    objectType = "property";
                } else if (object instanceof Port) {
                    objectType = "port";
                } else {
                    objectType = "entity";
                }
                Class theClass = Class.forName(deferredClass,
                        true, ClassLoader.getSystemClassLoader());
                //   System.out.println("reflecting " + theClass);
                // OK..  try reflecting using a workspace constructor
                _reflectionArguments[0] = _reflectionWorkspace;
                Constructor[] constructors =
                    theClass.getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Constructor constructor = constructors[i];
                    Class[] parameterTypes =
                        constructor.getParameterTypes();
                    if (parameterTypes.length != _reflectionArguments.length)
                        continue;
                    boolean match = true;
                    for (int j = 0; j < parameterTypes.length; j++) {
                        if (!(parameterTypes[j].isInstance(
                                _reflectionArguments[j]))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        deferredObject = (NamedObj)
                            constructor.newInstance(
                                    _reflectionArguments);
                        break;
                    }
                }


                //String source = "<" + objectType + " name=\""
                //    + object.getName() + "\" class=\""
                //    + deferredClass + "\"/>";
                //deferredObject = parser.parse(source);
                //   System.out.println("class with workspace = " +
                //         deferredClass);
                if (deferredObject == null) {
                    // Damn, no workspace constructor.  Let's
                    // try a container, name constructor.
                    // It really would be nice if all of
                    // our actors had workspace constructors,
                    // but version 1.0 only specified the
                    // (container,name) constructor, and
                    // now we're stuck with it.
                    String source = "<entity name=\"parsedClone\""
                        + " class=\"ptolemy.kernel.CompositeEntity\">\n"
                        + "<" + objectType + " name=\""
                        + object.getName() + "\" class=\""
                        + deferredClass + "\"/>\n"
                        + "</entity>";
                    _reflectionParser.reset();
                    CompositeEntity toplevel;
                    try {
                        toplevel = (CompositeEntity)
                            _reflectionParser.parse(source);
                    } catch (Exception ex) {
                        throw new InternalErrorException("Attempt "
                                + "to create an instance of "
                                + deferredClass + " failed because "
                                + "it does not have a Workspace "
                                + "constructor.  Original error:\n"
                                + ex.getMessage());
                    }
                    if (object instanceof Attribute) {
                        deferredObject =
                            toplevel.getAttribute(object.getName());
                    } else if (object instanceof Port) {
                        deferredObject =
                            toplevel.getPort(object.getName());
                    } else {
                        deferredObject =
                            toplevel.getEntity(object.getName());
                    }
                    //       System.out.println("class without workspace = " +
                    //         deferredClass);
                }
            } catch (InternalErrorException ex) {
                throw ex;
            } catch (Exception ex) {
                System.out.println("Exception occurred during parsing:\n"
                        + ex);
                ex.printStackTrace();
                System.out.println("done parsing:\n");
                deferredObject = null;
            }
        }
        return deferredObject;
    }

    private static void _createEntityInstanceFields(SootClass actorClass,
            ComponentEntity actor, Map options) {

        // Create a static field in the actor class.  This field
        // will reference the singleton instance of the actor class.
        SootField field = new SootField(
                "_CGInstance",
                RefType.v(actorClass),
                Modifier.PUBLIC | Modifier.STATIC);
        actorClass.addField(field);

        field.addTag(new ValueTag(actor));
        _entityToFieldMap.put(actor, field);
        _fieldToEntityMap.put(field, actor);

        // Add code to the end of each class initializer to set the
        // instance field.
        for (Iterator methods = actorClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();
            if (method.getName().equals("<init>")) {
                JimpleBody body = (JimpleBody)method.getActiveBody();
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newStaticFieldRef(field),
                                body.getThisLocal()),
                        body.getUnits().getLast());
            }
        }

        _classToObjectMap.put(actorClass, actor);

        // Loop over all the actor instance classes and get
        // fields for ports.
        if (actor instanceof CompositeActor) {
            // Then recurse
            CompositeEntity model = (CompositeEntity)actor;
            for (Iterator i = model.deepEntityList().iterator();
                 i.hasNext();) {
                ComponentEntity entity = (ComponentEntity)i.next();
                String className = getInstanceClassName(entity, options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);
                _createEntityInstanceFields(entityClass, entity, options);
            }
        }
    }

    private static void _createActorsIn(
            CompositeActor model, HashSet createdSet, String phaseName,
            ConstVariableModelAnalysis constAnalysis, Map options) {
        // Create an instance class for every actor.
        for (Iterator i = model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();

            String className = entity.getClass().getName();

            String newClassName = getInstanceClassName(entity, options);
                
            if (Scene.v().containsClass(newClassName)) {
                continue;
            }

            System.out.println("ModelTransformer: Creating actor class "
                    + newClassName);
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

            if (entity.getClass().getName().equals(
                        "ptolemy.actor.lib.MathFunction")) {
                throw new RuntimeException("Code Generation for " + 
                        "ptolemy.actor.lib.MathFunction not supported, since " 
                        + "it dynamically creates ports.");
            } else if (entity.getClass().getName().equals(
                               "ptolemy.actor.lib.TypeTest")) {
                throw new RuntimeException("Code Generation for " + 
                        "ptolemy.actor.lib.TypeTest not supported, since " 
                        + "it walks around the model.");
            } else if (entity.getClass().getName().equals(
                               "ptolemy.actor.lib.RecordAssembler") ||
                    entity.getClass().getName().equals(
                            "ptolemy.actor.lib.RecordDisassembler")) {
                throw new RuntimeException("Code Generation for " + 
                        "some record actors not supported, since " 
                        + "they iterate over ports.");
            } else if (entity.getClass().getName().equals(
                               "ptolemy.actor.lib.ExpressionToToken") ||
                    entity.getClass().getName().equals(
                            "ptolemy.actor.lib.io.ExpressionReader")) {
                throw new RuntimeException("Code Generation for " + 
                        "some actors not supported, since " 
                        + "they have dynamic expressions.");
            } else if (entity instanceof CompositeActor) {
                CompositeActor composite = (CompositeActor)entity;
                _createCompositeActor(composite, newClassName, options);
            } else if (entity instanceof Expression) {
                AtomicActorCreator creator = new ExpressionCreator();
                creator.createAtomicActor((Expression)entity,
                        newClassName, constAnalysis, options);
            } else if (entity instanceof FSMActor) {
                FSMCreator creator = new FSMCreator();
                creator.createAtomicActor((FSMActor)entity,
                        newClassName, constAnalysis, options);
            } else {
                // Must be an atomicActor.
                GenericAtomicActorCreator creator =
                    new GenericAtomicActorCreator();
                creator.createAtomicActor((AtomicActor)entity,
                        newClassName, constAnalysis, options);
            }
            SootClass entityClass = 
                Scene.v().loadClassAndSupport(newClassName);
            addActorForClass(entityClass, entity);
        }
    }

    // Populate the given class with code to create the contents of
    // the given entity.
    private static EntitySootClass _createCompositeActor(
            CompositeActor entity, String newClassName, Map options) {
        // FIXME: what about subclasses of CompositeActor?
        SootClass entityClass = PtolemyUtilities.compositeActorClass;
        // create a class for the entity instance.
        EntitySootClass entityInstanceClass =
            new EntitySootClass(entityClass, newClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();

        // Record everything that the class creates.
        HashSet tempCreatedSet = new HashSet();

        {
            // create a new body for the initialization method.
            SootMethod initMethod = entityInstanceClass.getInitMethod();
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Create a new class for each actor in the model.
            _createActorsIn(entity, tempCreatedSet,
                    "modelTransformer", _constAnalysis, options);

            // Create code in the model class to instantiate the ports
            // and parameters of the model.
            createAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);

            _ports(body, thisLocal, entity, thisLocal, entity,
                    entityInstanceClass, tempCreatedSet, true);

            // Excess initialization, but necessary for -actor???
            Stmt insertPoint = Jimple.v().newNopStmt();
            body.getUnits().add(insertPoint);

            // InitializeAttributes of the ports and parameters.
            initializeAttributesBefore(body, insertPoint, 
                    entity, thisLocal,
                    entity, thisLocal, entityInstanceClass);

            // Create code in the model class to instantiate all
            // actors and relations, and connect the relations
            // to the ports.
            _entities(body, thisLocal, entity, thisLocal, entity, 
                    entityInstanceClass, tempCreatedSet, options);
            _relations(body, thisLocal, entity, entityInstanceClass);
            _links(body, entity);
            _linksOnPortsContainedByContainedEntities(body, entity);
         
            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }

        implementExecutableInterface(entityInstanceClass);

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        // Inline all methods in the class that are called from
        // within the class.
        inlineLocalCalls(entityInstanceClass);

        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(
                entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    // Return true if the given attribute is one that can be ignored
    // during code generation...
    public static boolean _isIgnorableAttribute(Attribute attribute) {
        // Ignore frame sizes and locations.  They aren't really
        // necessary in the generated code, I don't think.
        if (attribute instanceof SizeAttribute ||
                attribute instanceof LocationAttribute ||
                attribute instanceof LibraryAttribute ||
                attribute instanceof VersionAttribute ||
                attribute instanceof TableauFactory ||
                attribute instanceof Documentation ||
                attribute instanceof EditorFactory ||
                attribute instanceof Location ||
                attribute instanceof WindowPropertiesAttribute ||
                attribute instanceof GeneratorAttribute ||
                attribute instanceof GeneratorTableauAttribute) {
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static CompositeActor _model;

    private static Map _entityToFieldMap;
    private static Map _fieldToEntityMap;

    private static Map _classToObjectMap;
    private static Map _objectToClassMap;
    
    private static List _actorClasses;
    private static List _attributeClasses;

    // Map from Ports to Locals.
    private static Map _portLocalMap;

    // Map from Entitys to Locals.
    private static Map _entityLocalMap;

    // Map from Relations to Locals.
    private static Map _relationLocalMap;

    private static ConstVariableModelAnalysis _constAnalysis;

    private static SootClass _modelClass = null;
    private static Object[] _reflectionArguments = new Object[1];
    private static Workspace _reflectionWorkspace = new Workspace();
    private static MoMLParser _reflectionParser =
    new MoMLParser(_reflectionWorkspace);
}

