/* Transform Actors using Soot

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

package ptolemy.copernicus.shallow;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import soot.Body;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;


//////////////////////////////////////////////////////////////////////////
//// ShallowModelTransformer
/**

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/

public class ShallowModelTransformer extends SceneTransformer  implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private ShallowModelTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     */

    public static ShallowModelTransformer v(CompositeActor model) {
        return new ShallowModelTransformer(model);
    }

    // Create and set attributes.
    public static void createFieldsForAttributes(JimpleBody body,
            NamedObj context,
            Local contextLocal, NamedObj namedObj, Local namedObjLocal,
            SootClass theClass, HashSet createdSet) {
        // A local that we will use to set the value of our
        // settable attributes.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                PtolemyUtilities.attributeType);
        body.getLocals().add(attributeLocal);
        Local settableLocal = Jimple.v().newLocal("settable",
                PtolemyUtilities.settableType);
        body.getLocals().add(settableLocal);

        for (Iterator attributes = namedObj.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();

            // Ignore things like Variable.
            if (!attribute.isPersistent() || 
                    attribute instanceof GeneratorAttribute) {
                continue;
            }

            String className = attribute.getClass().getName();
            Scene.v().loadClassAndSupport(className);
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = getFieldNameForAttribute(attribute, context);

            Local local;
            if (createdSet.contains(attribute.getFullName())) {
                // If the class for the object already creates the
                // attribute, then get a reference to the existing attribute.
                local = attributeLocal;
                body.getUnits().add(Jimple.v().newAssignStmt(attributeLocal,
                        Jimple.v().newVirtualInvokeExpr(contextLocal,
                                PtolemyUtilities.getAttributeMethod,
                                StringConstant.v(attributeName))));
            } else {
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        namedObjLocal, attribute.getName());
                Attribute classAttribute =
                    (Attribute)_findDeferredInstance(attribute);
                _updateCreatedSet(namedObj.getFullName()
                        + "." + attribute.getName(),
                        classAttribute, classAttribute, createdSet);
            }

            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
            SootUtilities.createAndSetFieldFromLocal(body, local,
                    theClass, attributeType, fieldName);
            // If the attribute is settable, then set its
            // expression.
            if (attribute instanceof Settable) {
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
            createFieldsForAttributes(body, context, contextLocal,
                    attribute, local, theClass, createdSet);
        }
    }

    /** Return the name of the field that is created to
     *  represent the given channel of the given type of the
     *  given relation.
     */
    public static String getBufferFieldName(TypedIORelation relation,
            int channel, ptolemy.data.type.Type type) {
        return "_" + StringUtilities.sanitizeName(relation.getName())
            + "_" + channel
            + "_" + StringUtilities.sanitizeName(type.toString());
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
    public static String getFieldNameForEntity(Entity entity,
            NamedObj context) {
        return "E" + StringUtilities.sanitizeName(entity.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForPort(
            Port port, NamedObj context) {
        return "P" + StringUtilities.sanitizeName(port.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForAttribute(Attribute attribute,
            NamedObj context) {
        return "A" + StringUtilities.sanitizeName(attribute.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForRelation(Relation relation,
            NamedObj context) {
        return "R" + StringUtilities.sanitizeName(relation.getName(context));
    }

    public String getPhaseName() {
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ShallowModelTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // create a class for the model
        String modelClassName =
            PhaseOptions.getString(options, "targetPackage")
            + ".CG" + StringUtilities.sanitizeName(_model.getName());

        EntitySootClass modelClass =
            new EntitySootClass(PtolemyUtilities.compositeActorClass,
                    modelClassName, Modifier.PUBLIC);
        Scene.v().addClass(modelClass);
        modelClass.setApplicationClass();

        // not really sure what this does..
        Scene.v().setMainClass(modelClass);

        // Initialize the model.
        SootMethod initMethod = modelClass.getInitMethod();
        JimpleBody body = Jimple.v().newBody(initMethod);
        initMethod.setActiveBody(body);
        body.insertIdentityStmts();
        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();

        _entityLocalMap = new HashMap();
        _portLocalMap = new HashMap();

        // Now instantiate all the stuff inside the model.
        _composite(body, thisLocal, _model, thisLocal, _model, modelClass,
                new HashSet());

        units.add(Jimple.v().newReturnVoidStmt());

        _removeSuperExecutableMethods(modelClass);

        // Resolve name collisions.
        LocalSplitter.v().transform(body, phaseName + ".lns");
        LocalNameStandardizer.v().transform(body, phaseName + ".lns");

        Scene.v().setActiveHierarchy(new Hierarchy());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Local _buildConstantTypeLocal(Body body,
            ptolemy.data.type.Type type) {
        Chain units = body.getUnits();
        if (type instanceof ptolemy.data.type.BaseType) {
            SootClass typeClass =
                Scene.v().loadClassAndSupport("ptolemy.data.type.BaseType");
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(typeClass, "forName");
            Local typeLocal = Jimple.v().newLocal("type_" + type.toString(),
                    RefType.v(typeClass));
            body.getLocals().add(typeLocal);
            units.add(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newStaticInvokeExpr(typeConstructor,
                            StringConstant.v(type.toString()))));
            return typeLocal;
        } else if (type instanceof ptolemy.data.type.ArrayType) {
            // recurse
            SootClass typeClass =
                Scene.v().loadClassAndSupport("ptolemy.data.type.ArrayType");
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(typeClass, "<init>");
            Local elementTypeLocal = _buildConstantTypeLocal(body,
                    ((ptolemy.data.type.ArrayType)type).getElementType());
            Local typeLocal = Jimple.v().newLocal("type_arrayOf" +
                    elementTypeLocal.getName(),
                    RefType.v(typeClass));
            body.getLocals().add(typeLocal);
            units.add(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newNewExpr(RefType.v(typeClass))));
            units.add(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(typeLocal,
                            typeConstructor, elementTypeLocal)));
            return typeLocal;
        }
        throw new RuntimeException("Unidentified type class = " +
                type.getClass().getName());
    }

    // Write the given composite.
    private void _composite(JimpleBody body, Local containerLocal,
            CompositeEntity container, Local thisLocal,
            CompositeEntity composite, EntitySootClass modelClass,
            HashSet createdSet) {

        // create fields for attributes.
        createFieldsForAttributes(body, container, containerLocal,
                composite, thisLocal, modelClass, createdSet);
        _ports(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet);
        _entities(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet);

        // handle the communication
        _relations(body, thisLocal, composite, modelClass, createdSet);
        _links(body, composite);
        _linksOnPortsContainedByContainedEntities(body, composite);
    }

    // Create and set entities.
    private void _entities(JimpleBody body, Local containerLocal,
            CompositeEntity container, Local thisLocal,
            CompositeEntity composite, EntitySootClass modelClass,
            HashSet createdSet) {
        // A local that we will use to get existing entities
        Local entityLocal = Jimple.v().newLocal("entity",
                RefType.v(PtolemyUtilities.entityClass));
        body.getLocals().add(entityLocal);

        for (Iterator entities = composite.entityList().iterator();
             entities.hasNext();) {
            Entity entity = (Entity)entities.next();
            System.out.println("ShallowModelTransformer: entity: " + entity);
            Local local;
            // If we are doing shallow, then use the base actor
            // classes.  Note that the entity might actually be
            // a MoML class (like Sinewave).
            String className = entity.getClass().getName();
            Scene.v().loadClassAndSupport(className);

            if (createdSet.contains(entity.getFullName())) {
                // Get a reference to the previously created entity.
                local = Jimple.v().newLocal("entity",
                        PtolemyUtilities.componentEntityType);
                body.getLocals().add(local);
                body.getUnits().add(Jimple.v().newAssignStmt(entityLocal,
                        Jimple.v().newVirtualInvokeExpr(containerLocal,
                                PtolemyUtilities.getEntityMethod,
                                StringConstant.v(entity.getName(container)))));
                // and then cast
                body.getUnits().add(
                        Jimple.v().newAssignStmt(local,
                                Jimple.v().newCastExpr(entityLocal,
                                        RefType.v(className))));    
            } else {
               // Create a new local variable.
                // The name of the local is determined automatically.
                // The name of the NamedObj is the same as in the model.
                // (Note that this might not be a valid Java identifier.)
                local = PtolemyUtilities.createNamedObjAndLocal(body,
                        className, thisLocal, entity.getName());

                Entity classEntity = (Entity)_findDeferredInstance(entity);

                if (!(entity instanceof CompositeEntity) ||
                        className.equals(entity.getMoMLInfo().className)) {
                    // If the entity is NOT a moml class....
                    // Then record the things inside the master as things
                    // that automagically get created when we construct the
                    // object.  Otherwise, we'll go through and create
                    // them manually later.
                    _updateCreatedSet(
                            composite.getFullName() + "." + entity.getName(),
                            classEntity, classEntity, createdSet);
                }
            }

            _entityLocalMap.put(entity, local);

            if (entity instanceof CompositeEntity) {
                _composite(body, containerLocal, container, local,
                        (CompositeEntity)entity, modelClass, createdSet);
            } else {
                _ports(body, thisLocal, composite, local,
                        entity, modelClass, createdSet);
                // If we are doing shallow code generation, then
                // include code to initialize the parameters of this
                // entity.
                createFieldsForAttributes(body, composite, thisLocal,
                        entity, local, modelClass, createdSet);
            }
        }
    }

    // Create and set external ports.
    private void _ports(JimpleBody body, Local containerLocal,
            CompositeEntity container, Local entityLocal,
            Entity entity, EntitySootClass modelClass, HashSet createdSet) {
        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v("ptolemy.kernel.Port"));
        body.getLocals().add(tempPortLocal);

        for (Iterator ports = entity.portList().iterator();
             ports.hasNext();) {
            Port port = (Port)ports.next();
            String className = port.getClass().getName();
            String fieldName = getFieldNameForPort(port, container);
            Local portLocal;

            Scene.v().loadClassAndSupport(className);
            portLocal = Jimple.v().newLocal("port",
                    PtolemyUtilities.componentPortType);
            body.getLocals().add(portLocal);

            if (createdSet.contains(port.getFullName())) {
                // If the class for the object already creates the
                // attribute, then get a reference to the existing attribute.

                // First assign to temp
                body.getUnits().add(Jimple.v().newAssignStmt(tempPortLocal,
                        Jimple.v().newVirtualInvokeExpr(entityLocal,
                                PtolemyUtilities.getPortMethod,
                                StringConstant.v(port.getName()))));
                if (port instanceof TypedIOPort) {
                    TypedIOPort ioPort = (TypedIOPort)port;
                    if (ioPort.isInput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(tempPortLocal,
                                        PtolemyUtilities.setInputMethod,
                                        IntConstant.v(1))));
                    }
                    if (ioPort.isOutput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(tempPortLocal,
                                        PtolemyUtilities.setOutputMethod,
                                        IntConstant.v(1))));
                    }
                    if (ioPort.isMultiport()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(tempPortLocal,
                                        PtolemyUtilities.setMultiportMethod,
                                        IntConstant.v(1))));
                    }
                }
                // and then cast to portLocal
                body.getUnits().add(Jimple.v().newAssignStmt(portLocal,
                        Jimple.v().newCastExpr(tempPortLocal,
                                RefType.v(className))));
            } else {
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                Local local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        entityLocal, port.getName());

                Port classPort = (Port)_findDeferredInstance(port);
                _updateCreatedSet(entity.getFullName() + "." + port.getName(),
                        classPort, classPort, createdSet);
                if (port instanceof TypedIOPort) {
                    TypedIOPort ioPort = (TypedIOPort)port;
                    if (ioPort.isInput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setInputMethod,
                                        IntConstant.v(1))));
                    }
                    if (ioPort.isOutput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setOutputMethod,
                                        IntConstant.v(1))));
                    }
                    if (ioPort.isMultiport()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setMultiportMethod,
                                        IntConstant.v(1))));
                    }
                }
                // and then cast to portLocal
                body.getUnits().add(Jimple.v().newAssignStmt(portLocal,
                        Jimple.v().newCastExpr(local,
                                RefType.v(className))));

            }

            _portLocalMap.put(port, portLocal);
            SootUtilities.createAndSetFieldFromLocal(body,
                    portLocal, modelClass, PtolemyUtilities.componentPortType,
                    fieldName);
            createFieldsForAttributes(body, container, containerLocal,
                    port, portLocal, modelClass, createdSet);
        }
    }

    // Create and set links.
    private void _links(JimpleBody body, CompositeEntity composite) {
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
    private void _linksOnPortsContainedByContainedEntities(
            JimpleBody body, CompositeEntity composite) {

        for (Iterator entities = composite.entityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            Iterator ports = entity.portList().iterator();
            while (ports.hasNext()) {
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
    private void _relations(JimpleBody body, Local thisLocal,
            CompositeEntity composite,
            EntitySootClass modelClass, HashSet createdSet) {
        _relationLocalMap = new HashMap();
        for (Iterator relations = composite.relationList().iterator();
             relations.hasNext();) {
            Relation relation = (Relation)relations.next();
            String className = relation.getClass().getName();
            // Create a new local variable.
            Local local =
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, relation.getName());
            _relationLocalMap.put(relation, local);
          
            Relation classRelation = (Relation)_findDeferredInstance(relation);
            
            _updateCreatedSet(
                    composite.getFullName() + "." + relation.getName(),
                    classRelation, classRelation, createdSet);
            
            createFieldsForAttributes(body, composite, thisLocal,
                    relation, local, modelClass, createdSet);
        }
    }

    // FIXME: duplicate with Actor transformer.
    private static void _removeSuperExecutableMethods(SootClass theClass) {
        // Loop through all the methods

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
                            if (r.getMethod().getName().equals("prefire") ||
                                    r.getMethod().getName().equals("postfire")) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                body.getUnits().remove(unit);
                            }
                        } else if (!r.getMethod().getName().equals("<init>")) {
                            System.out.println("superCall:" + r);
                        }
                    }
                }
            }
        }
    }

    /** Return an instance that represents the class that
     *  the given object defers to.
     */
    // FIXME: duplicate with MoMLWriter.
    private static NamedObj _findDeferredInstance(NamedObj object) {
        // System.out.println("findDeferred = " + object.getFullName());
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
                String deferredClass;
                if (isClass) {
                    deferredClass = info.superclass;
                } else {
                    deferredClass = info.className;
                }

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
                // System.out.println("reflecting " + theClass);
                // OK..  try reflecting using a workspace constructor
                _reflectionArguments[0] = _reflectionWorkspace;
                Constructor[] constructors =
                    theClass.getConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Constructor constructor = constructors[i];
                    Class[] parameterTypes =
                        constructor.getParameterTypes();
                    if (parameterTypes.length !=
                            _reflectionArguments.length)
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
                        System.out.println("constructor = " + constructor);
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
                //System.out.println("class with workspace = " +
                //        deferredClass);
                if (deferredObject == null) {
                    // Damn, no workspace constructor.  Let's
                    // try a container, name constructor.
                    // It really would be nice if all of
                    // our actors had workspace constructors,
                    // but version 1.0 only specified the
                    // (container, name) constructor, and
                    // now we're stuck with it.
                    String source = "<entity name=\"parsedClone\""
                        + "class=\"ptolemy.kernel.CompositeEntity\">\n"
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
                        throw new InternalErrorException(null, ex, "Attempt "
                                + "to create an instance of "
                                + deferredClass + " failed because "
                                + "it does not have a Workspace "
                                + "constructor.");
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
                    //  System.out.println("class without workspace = " +
                    //   deferredClass);
                }
            }
            catch (Exception ex) {
                System.err.println("Exception occurred during parsing:\n");
                ex.printStackTrace();
                deferredObject = null;
            }
        }
        return deferredObject;
    }

    // Add the full names of all named objects contained in the given object
    // to the given set, assuming that the object is contained within the
    // given context.
    private static void _updateCreatedSet(String prefix,
            NamedObj context, NamedObj object, HashSet set) {
        if (object == context) {
            System.out.println("creating " + prefix);
            set.add(prefix);
        } else {
            String name = prefix + "." + object.getName(context);
            System.out.println("creating " + name);
            set.add(name);
        }
        if (object instanceof CompositeEntity) {
            CompositeEntity composite = (CompositeEntity) object;
            for (Iterator entities = composite.entityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                _updateCreatedSet(prefix, context, entity, set);
            }
            for (Iterator relations = composite.relationList().iterator();
                 relations.hasNext();) {
                Relation relation = (Relation) relations.next();
                _updateCreatedSet(prefix, context, relation, set);
            }
        }
        if (object instanceof Entity) {
            Entity entity = (Entity) object;
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                Port port = (Port)ports.next();
                _updateCreatedSet(prefix, context, port, set);
            }
        }

        // actor/lib/test/auto/ElectronicUnitBase1.xml and others were failing
        // here, so now we check for null.
        if (object == null || object.attributeList() == null) { 
            System.out.println("ShallowModelTransformer: Warning: "
                    + "object == null, or object.attributeList() == null?, "
                    + "object: " + object);
        } else { 
            for (Iterator attributes = object.attributeList().iterator();
                 attributes.hasNext();) {
                Attribute attribute = (Attribute)attributes.next();
                _updateCreatedSet(prefix, context, attribute, set);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map from Ports to Locals.
    private Map _portLocalMap;

    // Map from Entitys to Locals.
    private Map _entityLocalMap;

    // Map from Relations to Locals.
    private Map _relationLocalMap;

    // The model we are generating code for.
    private CompositeActor _model;

    private static Object[] _reflectionArguments = new Object[1];
    private static Workspace _reflectionWorkspace = new Workspace();
    private static MoMLParser _reflectionParser =
    new MoMLParser(_reflectionWorkspace);
}

