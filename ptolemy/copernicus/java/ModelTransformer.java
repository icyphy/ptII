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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.copernicus.gui.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.attributes.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.moml.*;
import ptolemy.util.StringUtilities;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;

import soot.dava.*;
import soot.toolkits.graph.*;
import soot.util.*;

import java.lang.reflect.Constructor;
import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// ModelTransformer
/**
A transformer that creates a class to represent the model specified in
the constructor.  This transformer creates new instances of the classes
created by the ActorTransformer, along with the relations and links that
are present in the model.  It also creates attributes and appropriate
fields for any toplevel attributes of the model.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class ModelTransformer extends SceneTransformer {
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

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
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

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForPort(Port port, NamedObj context) {
        return StringUtilities.sanitizeName(port.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForRelation(Relation relation,
            NamedObj context) {
        return StringUtilities.sanitizeName(relation.getName(context));
    }

    /** Given an entity that we are generating code for, return a
     *  reference to the instance field created for that entity.
     *  @exception RuntimeException If no field was created for the
     *  given entity.
     */
    public static StaticFieldRef getFieldRefForEntity(Entity entity) {
        SootField entityField = (SootField)
            _entityToFieldMap.get(entity);
        if (entityField != null) {
            return Jimple.v().newStaticFieldRef(entityField);
        } else {
            throw new RuntimeException(
                    "Failed to find field for entity " + entity);
        }
    }

    /**
     *  @exception RuntimeException If no field was created for the
     *  given entity.
     */
    public static Entity getActorForClass(SootClass theClass) {
        Entity entity = (Entity) _classToObjectMap.get(theClass);
        if (entity != null) {
            return entity;
        } else {
            throw new RuntimeException(
                    "Failed to find entity for class " + theClass);
        }
    }

    public static String getInstanceClassName(Entity entity, Map options) {
        // Note that we use sanitizeName because entity names can have
        // spaces, and append leading characters because entity names
        // can start with numbers.
        return Options.getString(options, "targetPackage")
            + ".CG"
            + StringUtilities.sanitizeName(entity.getName(entity.toplevel()));
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
        return Options.getString(options, "targetPackage")
            + ".CGModel" + StringUtilities.sanitizeName(model.getName());
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

        // Create a class for the model
        String modelClassName = getModelClassName(_model, options);

        _modelClass = createCompositeActor(
                _model, modelClassName, options);

        // Create static instance fields for each actor.
        _entityToFieldMap = new HashMap();
        _fieldToEntityMap = new HashMap();
        _classToObjectMap = new HashMap();
        _createEntityInstanceFields(_modelClass, _model, options);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Write the given composite.
    private static void _composite(JimpleBody body, Local containerLocal,
            CompositeActor container, Local thisLocal,
            CompositeActor composite, EntitySootClass modelClass,
            HashSet createdSet, Map options) {

        _ports(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet);
        _entities(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet, options);

        // handle the communication
        _relations(body, thisLocal, composite, modelClass);
        _links(body, composite);
        _linksOnPortsContainedByContainedEntities(body, composite);
    }

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
                SootUtilities.createAndSetFieldFromLocal(
                        body, local, modelClass,
                        RefType.v(className), entityFieldName);

                _ports(body, thisLocal, composite,
                        local, entity, modelClass, createdSet);
            } else {
                //  System.out.println("Creating new!");

                // Create a new local variable.  The name of the local is
                // determined automatically.  The name of the NamedObj is
                // the same as in the model.  (Note that this might not be
                // a valid Java identifier.)
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        thisLocal, entity.getName());

                updateCreatedSet(
                        composite.getFullName() + "." + entity.getName(),
                        entity, entity, createdSet);

                SootUtilities.createAndSetFieldFromLocal(
                        body, local, modelClass,
                        RefType.v(className), entityFieldName);

                _ports(body, containerLocal, container,
                        local, entity, modelClass, createdSet);
                //   }
            }

            _entityLocalMap.put(entity, local);
        }
    }

    // Create and set external ports.
    private static void _ports(JimpleBody body, Local containerLocal,
            Entity container, Local entityLocal,
            Entity entity, EntitySootClass modelClass, HashSet createdSet) {
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

            // FIXME: what about subclasses?
            String className = "ptolemy.actor.TypedIOPort";
            RefType portType = RefType.v(PtolemyUtilities.ioportClass);

            String portName = port.getName(container);
            String fieldName = getFieldNameForPort(port, container);
            Local portLocal;

            if (createdSet.contains(port.getFullName())) {
                //    System.out.println("already created!");
                portLocal = Jimple.v().newLocal("port",
                        portType);
                body.getLocals().add(portLocal);
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
                updateCreatedSet(entity.getFullName() + "."
                        + port.getName(),
                        port, port, createdSet);
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
            }

            _portLocalMap.put(port, portLocal);
            if (!modelClass.declaresFieldByName(fieldName)) {
                SootUtilities.createAndSetFieldFromLocal(body,
                        portLocal, modelClass, portType, fieldName);
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

            SootUtilities.createAndSetFieldFromLocal(body,
                    local, modelClass, PtolemyUtilities.relationType,
                    fieldName);
        }
    }

    /** Return an instance that represents the class that
     *  the given object defers to.
     */
    // FIXME: duplicate with MoMLWriter.
    private static NamedObj _findDeferredInstance(NamedObj object) {
        //  System.out.println("findDeferred =" + object.getFullName());
        NamedObj deferredObject = null;
        NamedObj.MoMLInfo info = object.getMoMLInfo();
        if (info.deferTo != null) {
            deferredObject = info.deferTo;
            // System.out.println("object = " + object.getFullName());
            // System.out.println("deferredDirectly = " + deferredObject);
            //(new Exception()).printStackTrace(System.out);
        } else if (info.className != null) {
            try {
                // First try to find the local moml class that
                // we extend
                String deferredClass;
                if (info.elementName.equals("class")) {
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

    // Add the full names of all named objects contained in the given object
    // to the given set, assuming that the object is contained within the
    // given context.
    private static void updateCreatedSet(String prefix,
            NamedObj context, NamedObj object, HashSet set) {
        if (object == context) {
            //  System.out.println("creating " + prefix);
            set.add(prefix);
        } else {
            String name = prefix + "." + object.getName(context);
            //  System.out.println("creating " + name);
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
        for(Iterator methods = actorClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();
            if(method.getName().equals("<init>")) {
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
        if(actor instanceof CompositeActor) {
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

    private static void createActorsIn(CompositeActor model, HashSet createdSet,
            String phaseName,
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

            if (entity instanceof CompositeActor) {
                CompositeActor composite = (CompositeActor)entity;
                createCompositeActor(composite, newClassName, options);
            } else if (entity instanceof Expression) {
                AtomicActorCreator creator = new ExpressionCreator();
                creator.createAtomicActor((Expression)entity,
                        newClassName, options);
            } else if (entity instanceof FSMActor) {
                _createFSMActor((FSMActor)entity, newClassName, options);
            } else {
                // Must be an atomicActor.
                _createAtomicActor(model, (AtomicActor)entity,
                        newClassName, constAnalysis, options);
            }
        }
    }

    public static void _implementExecutableInterface(SootClass theClass) {
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

    // Inline invocation sites from methods in the given class to
    // another method in the given class
    public static void _inlineLocalCalls(SootClass theClass) {
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

    // Populate the given class with code to create the contents of
    // the given entity.
    private static EntitySootClass _createAtomicActor(
            CompositeActor model, AtomicActor entity, String newClassName,
            ConstVariableModelAnalysis constAnalysis, Map options) {

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
        {
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            // return void
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        SootClass theClass = (SootClass)entityInstanceClass;
        SootClass superClass = theClass.getSuperclass();
        while (superClass != PtolemyUtilities.objectClass &&
                superClass != PtolemyUtilities.actorClass &&
                superClass != PtolemyUtilities.compositeActorClass) {
            superClass.setLibraryClass();
            SootUtilities.foldClass(theClass);
            superClass = theClass.getSuperclass();
        }

        // Go through all the initialization code and remove any old
        // parameter initialization code.  This has to happen after
        // class folding so that all of the parameter initialization
        // is available, but before we add the correct initialization.
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

        {
            // replace the previous dummy body
            // for the initialization method with a new one.
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // create attributes for those in the class
            _createAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);

            // Create and initialize ports
            _initializePorts(body, thisLocal, entity,
                    thisLocal, entity, entityInstanceClass, tempCreatedSet);

            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }

        // Remove super calls to the executable interface.
        // FIXME: This would be nice to do by inlining instead of
        // special casing.
        _implementExecutableInterface(entityInstanceClass);

        {
            // Add code to the beginning of the preinitialize method that
            // initializes the attributes.

            SootMethod method = theClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody)method.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();
            _initializeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass);
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        {
            LinkedList notConstantAttributeList = new LinkedList(
                    entity.attributeList(Variable.class));
            notConstantAttributeList.removeAll(
                    constAnalysis.getConstVariables(entity));
            // Sort according to dependancies.

            // Add code to the beginning of the prefire method that
            // computes the attribute values of anything that is not a
            // constant.

            SootMethod method = theClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody)method.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();
            _computeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass,
                    notConstantAttributeList);
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());

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
    private static EntitySootClass createCompositeActor(
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

            createActorsIn(entity, tempCreatedSet,
                    "modelTransformer",
                    ModelTransformer._constAnalysis, options);

            _createAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);

            ModelTransformer._composite(body,
                    thisLocal, entity, thisLocal, entity,
                    entityInstanceClass, tempCreatedSet, options);
            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }

        _implementExecutableInterface(entityInstanceClass);

        {
            // Add code to the beginning of the preinitialize method that
            // initializes the attributes.

            SootMethod method =
                entityInstanceClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody)method.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();
            _initializeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass);
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());

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
    private static EntitySootClass _createFSMActor(
            FSMActor entity, String newClassName, Map options) {

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
            _createAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);

            // Create and initialize ports
            // System.out.println("initializing ports");
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
                        StringUtilities.sanitizeName(name) + "Token",
                        type);
                entityInstanceClass.addField(field);
                nameToField.put(name, field);

                field = new SootField(
                        StringUtilities.sanitizeName(name) + "IsPresent",
                        type);
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
            _initializeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass);
        }

        // Add a field to keep track of the current state.
        SootField currentStateField = new SootField(
                "_currentState", IntType.v());
        entityInstanceClass.addField(currentStateField);
        SootField nextTransitionField = new SootField(
                "_nextTransition", IntType.v());
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
            String initialStateName = ((StringAttribute)
                    entity.getAttribute("initialStateName")).getExpression();
            int initialStateIndex = entity.entityList().indexOf(
                    entity.getEntity(initialStateName));
            units.add(Jimple.v().newAssignStmt(
                              Jimple.v().newInstanceFieldRef(
                                      thisLocal, currentStateField),
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
                            PtolemyUtilities.componentPortType);
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
                    Local hasTokenToken =
                        PtolemyUtilities.addTokenLocal(body, "token",
                                PtolemyUtilities.booleanTokenClass,
                                PtolemyUtilities.booleanTokenConstructor,
                                hasTokenLocal);

                    // store the isPresent
                    SootField tokenIsPresentField = (SootField)
                        nameToField.get(name + "_isPresent");
                    units.add(Jimple.v().newAssignStmt(
                                      Jimple.v().newInstanceFieldRef(
                                              thisLocal,
                                              tokenIsPresentField),
                                      hasTokenToken));

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
                    SootField tokenField = (SootField)
                        nameToField.get(name);
                    units.add(Jimple.v().newAssignStmt(
                                      Jimple.v().newInstanceFieldRef(
                                              thisLocal,
                                              tokenField),
                                      tokenLocal));
                    units.add(target);
                }
            }

            Map stateToStartStmt = new HashMap();
            List stateStmtList = new LinkedList();
            int numberOfStates = entity.entityList().size();
            // Figure out what state we are in.
            for (Iterator states = entity.entityList().iterator();
                states.hasNext();) {
                State state = (State)states.next();
                Stmt startStmt = Jimple.v().newNopStmt();

                stateToStartStmt.put(state, startStmt);
                stateStmtList.add(startStmt);
            }

            Local currentStateLocal = Jimple.v().newLocal(
                    "currentStateLocal", IntType.v());
            body.getLocals().add(currentStateLocal);

            Local nextStateLocal = Jimple.v().newLocal(
                    "nextStateLocal", IntType.v());
            body.getLocals().add(nextStateLocal);

            Local flagLocal = Jimple.v().newLocal(
                    "flagLocal", BooleanType.v());
            body.getLocals().add(flagLocal);
            Local transitionTakenLocal = Jimple.v().newLocal(
                    "transitionTakenLocal", BooleanType.v());
            body.getLocals().add(transitionTakenLocal);
            Local nextTransitionLocal = Jimple.v().newLocal(
                    "nextTransitionLocal", IntType.v());
            body.getLocals().add(nextTransitionLocal);

            units.add(
                    Jimple.v().newAssignStmt(
                            currentStateLocal,
                            Jimple.v().newInstanceFieldRef(
                                    thisLocal,
                                    currentStateField)));
            // Start by doing nothing.
            units.add(
                    Jimple.v().newAssignStmt(
                            transitionTakenLocal,
                            IntConstant.v(0)));
            units.add(
                    Jimple.v().newAssignStmt(
                            nextTransitionLocal,
                            IntConstant.v(-1)));

            // If no transition is taken, then stay in this state.
            units.add(
                    Jimple.v().newAssignStmt(
                            nextStateLocal,
                            currentStateLocal));


            Stmt finishedStmt = Jimple.v().newNopStmt();
            Stmt errorStmt = Jimple.v().newNopStmt();

            // Get the current state.
            units.add(
                    Jimple.v().newTableSwitchStmt(currentStateLocal,
                            0, numberOfStates - 1,
                            stateStmtList,
                            errorStmt));

            // Generate code for each state.
            for (Iterator states = entity.entityList().iterator();
                states.hasNext();) {
                State state = (State)states.next();
                System.out.println("state " + state.getName());

                Stmt startStmt = (Stmt)stateToStartStmt.get(state);
                units.add(startStmt);

                // Fire the refinement actor.
                TypedActor[] refinements;
                try {
                    refinements = state.getRefinement();
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                if (refinements != null)
                    for (int i = 0; i < refinements.length; i++) {
                        TypedActor refinement = refinements[i];

                        Local containerLocal = Jimple.v().newLocal("container",
                                RefType.v(PtolemyUtilities.namedObjClass));
                        body.getLocals().add(containerLocal);
                        Local entityLocal = Jimple.v().newLocal("entity",
                                RefType.v(PtolemyUtilities.entityClass));
                        body.getLocals().add(entityLocal);

                        NamedObj toplevel = entity.toplevel();
                        String deepName
                            = ((NamedObj)refinement).getName(toplevel);

                        units.add(
                                Jimple.v().newAssignStmt(containerLocal,
                                        Jimple.v().newVirtualInvokeExpr(
                                                thisLocal,
                                                PtolemyUtilities
                                                .toplevelMethod)));
                        units.add(
                                Jimple.v().newAssignStmt(containerLocal,
                                        Jimple.v().newCastExpr(
                                                containerLocal,
                                                RefType.v(PtolemyUtilities.compositeActorClass))));
                        units.add(
                                Jimple.v().newAssignStmt(entityLocal,
                                        Jimple.v().newVirtualInvokeExpr(
                                                containerLocal,
                                                PtolemyUtilities.getEntityMethod,
                                                StringConstant.v(deepName))));

                        units.add(
                                Jimple.v().newAssignStmt(entityLocal,
                                        Jimple.v().newCastExpr(
                                                entityLocal,
                                                RefType.v(PtolemyUtilities.compositeActorClass))));
                        SootMethod rprefireMethod,
                            rfireMethod, rpostfireMethod;
                        if (refinement instanceof CompositeActor) {
                            rprefireMethod =
                                SootUtilities.searchForMethodByName(
                                        PtolemyUtilities.compositeActorClass,
                                        "prefire");
                            rfireMethod =
                                SootUtilities.searchForMethodByName(
                                        PtolemyUtilities.compositeActorClass,
                                        "fire");
                            rpostfireMethod =
                                SootUtilities.searchForMethodByName(
                                        PtolemyUtilities.compositeActorClass,
                                        "postfire");
                        } else {
                            throw new RuntimeException();
                        }
                        units.add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(
                                                entityLocal,
                                                rprefireMethod)));
                        units.add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(
                                                entityLocal,
                                                rfireMethod)));
                        units.add(
                                Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(
                                                entityLocal,
                                                rpostfireMethod)));

                    }

                // Determine the next state in this state.
                for (Iterator transitions =
                        state.outgoingPort.linkedRelationList().iterator();
                    transitions.hasNext();) {
                    Transition transition = (Transition)transitions.next();
                    String guardExpression = transition.getGuardExpression();

                    Local guardLocal = _generateExpressionCode(
                            entity, entityInstanceClass, guardExpression,
                            nameToField, nameToType, body);

                    // Test the guard.
                    units.add(Jimple.v().newAssignStmt(
                                      tokenLocal,
                                      Jimple.v().newCastExpr(
                                              guardLocal,
                                              RefType.v(PtolemyUtilities.booleanTokenClass))));
                    units.add(
                            Jimple.v().newAssignStmt(
                                    flagLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            tokenLocal,
                                            PtolemyUtilities.booleanValueMethod)));
                    Stmt skipStmt = Jimple.v().newNopStmt();

                    units.add(
                            Jimple.v().newIfStmt(
                                    Jimple.v().newEqExpr(
                                            flagLocal,
                                            IntConstant.v(0)),
                                    skipStmt));
                    units.add(
                            Jimple.v().newIfStmt(
                                    Jimple.v().newEqExpr(
                                            transitionTakenLocal,
                                            IntConstant.v(1)),
                                    errorStmt));

                    // If transition taken, then store the next state
                    units.add(
                            Jimple.v().newAssignStmt(
                                    transitionTakenLocal,
                                    IntConstant.v(1)));
                    units.add(
                            Jimple.v().newAssignStmt(
                                    nextTransitionLocal,
                                    IntConstant.v(entity.relationList().indexOf(transition))));
                    int nextStateIndex = entity.entityList().indexOf(
                            transition.destinationState());
                    units.add(
                            Jimple.v().newAssignStmt(
                                    nextStateLocal,
                                    IntConstant.v(nextStateIndex)));

                    // Generate code for the outputExpression of the guard.
                    for (Iterator actions = transition.choiceActionList().iterator();
                        actions.hasNext();) {
                        AbstractActionsAttribute action =
                            (AbstractActionsAttribute)actions.next();
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

            Local exceptionLocal = SootUtilities.createRuntimeException(
                    body, errorStmt, "state error");
            units.insertBefore(
                    Jimple.v().newThrowStmt(exceptionLocal),
                    errorStmt);

            // Store the next state.
            units.add(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(
                                    thisLocal,
                                    currentStateField),
                            nextStateLocal));
            // And the next Transition.
            units.add(
                    Jimple.v().newAssignStmt(
                            Jimple.v().newInstanceFieldRef(
                                    thisLocal,
                                    nextTransitionField),
                            nextTransitionLocal));


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
            for (Iterator transitions = entity.relationList().iterator();
                transitions.hasNext();) {
                Transition transition = (Transition)transitions.next();
                Stmt startStmt = Jimple.v().newNopStmt();

                transitionToStartStmt.put(transition, startStmt);
                transitionStmtList.add(startStmt);
            }

            Local nextTransitionLocal = Jimple.v().newLocal(
                    "nextTransitionLocal", IntType.v());
            body.getLocals().add(nextTransitionLocal);

            units.add(
                    Jimple.v().newAssignStmt(
                            nextTransitionLocal,
                            Jimple.v().newInstanceFieldRef(
                                    thisLocal,
                                    nextTransitionField)));

            Stmt finishedStmt = Jimple.v().newNopStmt();
            Stmt errorStmt = Jimple.v().newNopStmt();

            // Get the current transition..
            units.add(
                    Jimple.v().newTableSwitchStmt(nextTransitionLocal,
                            0, numberOfTransitions - 1,
                            transitionStmtList,
                            errorStmt));

            // Generate code for each transition
            for (Iterator transitions = entity.relationList().iterator();
                transitions.hasNext();) {
                Transition transition = (Transition)transitions.next();
                Stmt startStmt = (Stmt)
                    transitionToStartStmt.get(transition);
                units.add(startStmt);

                // Generate code for the commitExpression of the guard.
                for (Iterator actions =
                        transition.commitActionList().iterator();
                    actions.hasNext();) {
                    AbstractActionsAttribute action =
                        (AbstractActionsAttribute)actions.next();
                    _generateActionCode(entity, entityInstanceClass,
                            nameToField, nameToType, body, action);
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
        _implementExecutableInterface(entityInstanceClass);

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());

        // Inline all methods in the class that are called from
        // within the class.
        _inlineLocalCalls(entityInstanceClass);

        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(
                entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    public static void _generateActionCode(
            Entity entity, SootClass entityClass,
            Map nameToField, Map nameToType, JimpleBody body,
            AbstractActionsAttribute action) {
        for (Iterator names = action.getDestinationNameList().iterator();
            names.hasNext();) {
            String name = (String)names.next();
            String actionExpression = action.getExpression(name);
            Local outputTokenLocal = _generateExpressionCode(
                    entity, entityClass, actionExpression,
                    nameToField, nameToType, body);
            try {
                NamedObj destination = action.getDestination(name);
                if (destination instanceof TypedIOPort) {
                    // send the computed token
                    Local portLocal = Jimple.v().newLocal("port",
                            PtolemyUtilities.componentPortType);
                    body.getLocals().add(portLocal);

                    SootField portField =
                        entityClass.getFieldByName(name);

                    body.getUnits().add(
                            Jimple.v().newAssignStmt(portLocal,
                                    Jimple.v().newInstanceFieldRef(
                                            body.getThisLocal(), portField)));
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            portLocal,
                                            PtolemyUtilities.sendMethod,
                                            IntConstant.v(0),
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

                    // Get a ref to the parameter through the toplevel,
                    // since the parameter we are assigning to may be
                    // above us in the hierarchy.

                    NamedObj toplevel = entity.toplevel();
                    String deepName
                        = ((NamedObj)destination).getName(toplevel);

                    body.getUnits().add(
                            Jimple.v().newAssignStmt(containerLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            body.getThisLocal(),
                                            PtolemyUtilities.toplevelMethod)));
                    body.getUnits().add(
                            Jimple.v().newAssignStmt(attributeLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            containerLocal,
                                            PtolemyUtilities.getAttributeMethod,
                                            StringConstant.v(deepName))));

                    body.getUnits().add(
                            Jimple.v().newAssignStmt(paramLocal,
                                    Jimple.v().newCastExpr(
                                            attributeLocal,
                                            RefType.v(PtolemyUtilities.variableClass))));
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
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

    public static Local _generateExpressionCodeBefore(
            Entity entity, SootClass entityClass, String expression,
            Map nameToField, Map nameToType,
            JimpleBody body, Stmt insertPoint) {

        Local local;
        try {
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree =
                parser.generateParseTree(expression);
            ActorCodeGenerationScope scope =
                new ActorCodeGenerationScope(
                        entity, entityClass, nameToField,
                        nameToType, body, insertPoint);
            ParseTreeCodeGenerator generator =
                new ParseTreeCodeGenerator();
            local = generator.generateCode(
                    parseTree, body, insertPoint, scope);
        } catch (IllegalActionException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
        return local;
    }

    public static Local _generateExpressionCode(
            Entity entity, SootClass entityClass, String expression,
            Map nameToField, Map nameToType, JimpleBody body) {
        Stmt insertPoint = Jimple.v().newNopStmt();
        body.getUnits().add(insertPoint);
        return _generateExpressionCodeBefore(entity, entityClass, expression,
                nameToField, nameToType, body, insertPoint);
    }

    public static class ActorCodeGenerationScope
        implements CodeGenerationScope {
        public ActorCodeGenerationScope(
                Entity entity, SootClass entityClass, Map nameToField,
                Map nameToType, JimpleBody body, Stmt insertPoint) {
            _nameToField = nameToField;
            _nameToType = nameToType;
            _body = body;
            _insertPoint = insertPoint;
            _units = body.getUnits();
            _entity = entity;
            _entityClass = entityClass;
        }

        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            throw new IllegalActionException("The ID " + name +
                    " does not have a value");
        }

        public Local getLocal(String name)
                throws IllegalActionException {
            Local thisLocal = _body.getThisLocal();

            if (name.equals("time")) {
                throw new RuntimeException("time not supported");
            } else if (name.equals("iteration")) {
                throw new RuntimeException("iteration not supported");
            }
            //                 Local intLocal = Jimple.v().newLocal("intLocal",
            //                         IntType.v());
            //                 _body.getLocals().add(intLocal);
            //                 _units.add(
            //                         Jimple.v().newAssignStmt(intLocal,
            //                                 Jimple.v().newInstanceFieldRef(
            //                                         thisLocal,
            //                                         entityClass.getFieldByName("_iteration"))));
            //                 Local tokenLocal =
            //                     PtolemyUtilities.addTokenLocal(_body, "iterationLocal",
            //                         PtolemyUtilities.intTokenClass,
            //                         PtolemyUtilities.intTokenConstructor,
            //                         intLocal);
            //                 return tokenLocal;
            //             }

            SootField portField = (SootField)_nameToField.get(name);

            if (portField != null) {

                Local portLocal = Jimple.v().newLocal("portToken",
                        PtolemyUtilities.getSootTypeForTokenType(
                                getType(name)));
                _body.getLocals().add(portLocal);

                Local tokenLocal = Jimple.v().newLocal("portToken",
                        PtolemyUtilities.tokenType);
                _body.getLocals().add(tokenLocal);

                _units.insertBefore(
                        Jimple.v().newAssignStmt(tokenLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, portField)),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        PtolemyUtilities
                                        .getSootTypeForTokenType(
                                                getType(name)))),
                        _insertPoint);

                return portLocal;
            }

            // Look for parameter in actor.
            NamedObj container = _entity;
            Variable result = null;
            while (container != null) {
                result = _searchIn(container, name);
                if (result != null) {
                    // Insert code to get a ref to the variable,
                    // and to get the token of that variable.
                    Local containerLocal = Jimple.v().newLocal("container",
                            RefType.v(PtolemyUtilities.namedObjClass));
                    _body.getLocals().add(containerLocal);
                    Local attributeLocal = Jimple.v().newLocal("attribute",
                            PtolemyUtilities.attributeType);
                    _body.getLocals().add(attributeLocal);
                    Local tokenLocal = Jimple.v().newLocal("token",
                            PtolemyUtilities.tokenType);
                    _body.getLocals().add(tokenLocal);

                    NamedObj toplevel = _entity.toplevel();
                    String deepName = result.getName(toplevel);


                    _units.insertBefore(
                            Jimple.v().newAssignStmt(containerLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            thisLocal,
                                            PtolemyUtilities.toplevelMethod)),
                            _insertPoint);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(attributeLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            containerLocal,
                                            PtolemyUtilities.getAttributeMethod,
                                            StringConstant.v(deepName))),
                            _insertPoint);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(attributeLocal,
                                    Jimple.v().newCastExpr(attributeLocal,
                                            RefType.v(
                                                    PtolemyUtilities.variableClass))),
                            _insertPoint);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(tokenLocal,
                                    Jimple.v().newVirtualInvokeExpr(
                                            attributeLocal,
                                            PtolemyUtilities.variableGetTokenMethod)),
                            _insertPoint);

                    return tokenLocal;
                } else {
                    container = (NamedObj)container.getContainer();
                }
            }

            throw new IllegalActionException(
                    "The ID " + name + " is undefined.");
        }
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            if (_nameToType.containsKey(name)) {
                return (ptolemy.data.type.Type)_nameToType.get(name);
            }

            NamedObj container = _entity;
            while (container != null) {
                Variable result = _searchIn(container, name);
                if (result != null) {
                    return result.getType();
                } else {
                    container = (NamedObj)container.getContainer();
                }
            }

            throw new IllegalActionException(
                    "The ID " + name + " is undefined.");
        }
        public NamedList variableList() {
            return new NamedList();
        }

        // Search in the container for an attribute with the given name.
        // Search recursively in any instance of ScopeExtender in the
        // container.
        private Variable _searchIn(NamedObj container, String name) {
            Attribute result = container.getAttribute(name);
            if (result != null && result instanceof Variable)
                return (Variable)result;
            Iterator extenders =
                container.attributeList(ScopeExtender.class).iterator();
            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender)extenders.next();
                result = extender.getAttribute(name);
                if (result != null && result instanceof Variable)
                    return (Variable)result;
            }
            return null;
        }

        private Map _nameToField;
        private Map _nameToType;
        private JimpleBody _body;
        private Stmt _insertPoint;
        private Chain _units;
        private Entity _entity;
        private SootClass _entityClass;
    }

    private static void _removeAttributeInitialization(SootClass theClass) {
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
                            PtolemyUtilities
                            .variableConstructorWithToken.getSubSignature())) {
                    SootClass variableClass =
                        r.getMethod().getDeclaringClass();
                    SootMethod constructorWithoutToken =
                        variableClass.getMethod(
                                PtolemyUtilities
                                .variableConstructorWithoutToken.getSubSignature());
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

    // This is similar to ModelTransformer.createFieldsForAttributes,
    // except that all attributes are created, even those that
    // have already been created.
    public static void _createAttributes(JimpleBody body,
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

            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = ModelTransformer.getFieldNameForAttribute(
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
                body.getUnits().add(Jimple.v().newAssignStmt(
                                            attributeLocal,
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
                Attribute classAttribute =
                    (Attribute)ModelTransformer._findDeferredInstance(attribute);
                ModelTransformer.updateCreatedSet(namedObj.getFullName() + "."
                        + attribute.getName(),
                        classAttribute, classAttribute, createdSet);
            }

            // System.out.println("creating new field");
            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
            SootUtilities.createAndSetFieldFromLocal(body, local,
                    theClass, attributeType, fieldName);

            _createAttributes(body, context, contextLocal,
                    attribute, local, theClass, createdSet);
        }
    }

    // This is similar to ModelTransformer.createFieldsForAttributes,
    // except that all attributes are initialized, even those that
    // have already been created.
    public static void _initializeAttributesBefore(
            JimpleBody body, Stmt insertPoint,
            NamedObj context, Local contextLocal,
            NamedObj namedObj, Local namedObjLocal,
            SootClass theClass) {

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
            String fieldName = ModelTransformer.getFieldNameForAttribute(
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
            _initializeAttributesBefore(body, insertPoint,
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
    }

    private static void _computeAttributesBefore(
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
            String fieldName = ModelTransformer.getFieldNameForAttribute(
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
                Local tokenLocal = _generateExpressionCodeBefore(
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
            _computeAttributesBefore(
                    body, insertPoint,
                    context, contextLocal,
                    attribute, local, theClass, attributeList);
        }
    }

    // Initialize the ports of this actor.  This is similar to code in
    // the ModelTransformer, except that here, all ports have their type set.
    public static void _initializePorts(JimpleBody body, Local containerLocal,
            Entity container, Local entityLocal,
            Entity entity, EntitySootClass modelClass, HashSet createdSet) {
        Entity classObject = (Entity)
            ModelTransformer._findDeferredInstance(entity);

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

            if (!modelClass.declaresFieldByName(fieldName)) {
                SootUtilities.createAndSetFieldFromLocal(body,
                        portLocal, modelClass, RefType.v(className),
                        fieldName);
            }
        }
    }

    // Return true if the given attribute is one that can be ignored
    // during code generation...
    private static boolean _isIgnorableAttribute(Attribute attribute) {
        // FIXME: This is horrible...  I guess we need an attribute for
        // persistence?
        if (attribute instanceof Variable &&
                !(attribute instanceof Parameter)) {
            return true;
        }

        // Ignore frame sizes and locations.  They aren't really
        // necessary in the generated code, I don't think.
        if (attribute instanceof SizeAttribute ||
                attribute instanceof LocationAttribute ||
                attribute instanceof LibraryAttribute ||
                attribute instanceof VersionAttribute ||
                attribute instanceof TableauFactory ||
                attribute instanceof EditorFactory ||
                attribute instanceof Location ||
                attribute instanceof WindowPropertiesAttribute ||
                attribute instanceof GeneratorTableauAttribute) {
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CompositeActor _model;


    private static Map _entityToFieldMap;
    private static Map _fieldToEntityMap;
    private static Map _classToObjectMap;

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

