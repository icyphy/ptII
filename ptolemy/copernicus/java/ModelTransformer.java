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
import ptolemy.moml.LibraryAttribute;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLParser;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    public static String getFieldNameForEntity(Entity entity,
            NamedObj context) {
        return StringUtilities.sanitizeName(entity.getName(context));
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
    public static String getFieldNameForAttribute(Attribute attribute,
            NamedObj context) {
        return StringUtilities.sanitizeName(attribute.getName(context));
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForRelation(Relation relation,
            NamedObj context) {
        return StringUtilities.sanitizeName(relation.getName(context));
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

        _modelClass = ActorTransformer.createCompositeActor(
                _model, modelClassName, options);

    }
        /*
        EntitySootClass modelClass =
            new EntitySootClass(
                    PtolemyUtilities.compositeActorClass, modelClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(modelClass);
        modelClass.setApplicationClass();

        // Save the class so we can easily get it later.
        _modelClass = modelClass;

        populateModelClass(modelClass, _model, phaseName, options);
    }

    public static void populateModelClass(
            EntitySootClass modelClass, CompositeActor model,
            String phaseName, Map options) {
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
        _composite(body, thisLocal, model, thisLocal, model,
                modelClass, new HashSet(), options);

        units.add(Jimple.v().newReturnVoidStmt());

        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());

        _removeSuperExecutableMethods(modelClass);

        // Resolve name collisions.
        LocalSplitter.v().transform(body, phaseName + ".lns");
        LocalNameStandardizer.v().transform(body, phaseName + ".lns");

        // Since we've added classes and methods, update the hierarchy
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());
    }
         */
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Write the given composite.
    public static void _composite(JimpleBody body, Local containerLocal,
            CompositeActor container, Local thisLocal,
            CompositeActor composite, EntitySootClass modelClass,
            HashSet createdSet, Map options) {
        //  System.out.println("composite = " + composite.getFullName());
//         ActorTransformer.createActorsIn(composite, createdSet,
//                 "modelTransformer", _constAnalysis, options);

        // create fields for attributes.
   //      createFieldsForAttributes(body, container, containerLocal,
//                 composite, thisLocal, modelClass, createdSet);
        _ports(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet);
        _entities(body, containerLocal, container,
                thisLocal, composite, modelClass, createdSet, options);

        // handle the communication
        _relations(body, thisLocal, composite, modelClass);
        _links(body, composite);
        _linksOnPortsContainedByContainedEntities(body, composite);

        /*
          SootMethod clinitMethod;
          Body clinitBody;
          if (modelClass.declaresMethodByName("<clinit>")) {
          clinitMethod = modelClass.getMethodByName("<clinit>");
          clinitBody = clinitMethod.retrieveActiveBody();
          } else {
          clinitMethod = new SootMethod("<clinit>", Collections.EMPTY_LIST,
          VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
          modelClass.addMethod(clinitMethod);
          clinitBody = Jimple.v().newBody(clinitMethod);
          clinitMethod.setActiveBody(clinitBody);
          clinitBody.getUnits().add(Jimple.v().newReturnVoidStmt());
          }
          Chain clinitUnits = clinitBody.getUnits();

          // If we're doing deep (SDF) codegen, then create a
          // queue for every type of every channel of every relation.
          for (Iterator relations = composite.relationList().iterator();
          relations.hasNext();) {
          TypedIORelation relation = (TypedIORelation)relations.next();
          Parameter bufferSizeParameter =
          (Parameter)relation.getAttribute("bufferSize");
          int bufferSize;
          try {
          bufferSize =
          ((IntToken)bufferSizeParameter.getToken()).intValue();
          } catch (Exception ex) {
          System.out.println("No bufferSize parameter for " +
          relation);
          continue;
          }

          // Determine the types that the relation is connected to.
          Map typeMap = new HashMap();
          List destinationPortList =
          relation.linkedDestinationPortList();
          for (Iterator destinationPorts = destinationPortList.iterator();
          destinationPorts.hasNext();) {
          TypedIOPort port = (TypedIOPort)destinationPorts.next();
          ptolemy.data.type.Type type = port.getType();
          typeMap.put(type.toString(), type);
          }

          for (Iterator types = typeMap.keySet().iterator();
          types.hasNext();) {
          ptolemy.data.type.Type type =
          (ptolemy.data.type.Type)typeMap.get(types.next());
          BaseType tokenType =
          PtolemyUtilities.getSootTypeForTokenType(type);
          Type arrayType = ArrayType.v(tokenType, 1);
          String fieldName = relation.getName() + "_bufferLocal";
          Local arrayLocal =
          Jimple.v().newLocal(fieldName, arrayType);
          clinitBody.getLocals().add(arrayLocal);

          for (int i = 0; i < relation.getWidth(); i++) {
          SootField field = new SootField(
          getBufferFieldName(relation, i, type),
          arrayType,
          Modifier.PUBLIC | Modifier.STATIC);
          modelClass.addField(field);
          // System.out.println("creating field = " + field);

          // Tag the field with the type.
          field.addTag(new TypeTag(type));

          // Create the new buffer
          // Note: reverse order!
          clinitUnits.addFirst(Jimple.v().newAssignStmt(
          Jimple.v().newStaticFieldRef(field),
          arrayLocal));
          clinitUnits.addFirst(
          Jimple.v().newAssignStmt(arrayLocal,
          Jimple.v().newNewArrayExpr(tokenType,
          IntConstant.v(bufferSize))));

          }
          }
          }
        */
    }

    // Create and set attributes.
    public static void createFieldsForAttributes(JimpleBody body,
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

            if(ActorTransformer._isIgnorableAttribute(attribute)) {
                continue;
            }

            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = getFieldNameForAttribute(attribute, context);

            //     System.out.println("className = " + className);
            Local local;
            if (createdSet.contains(attribute.getFullName())) {
                //        System.out.println("already has " + attributeName);
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
                //       System.out.println("creating " + attributeName);
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        namedObjLocal, attribute.getName());
                Attribute classAttribute =
                    (Attribute)_findDeferredInstance(attribute);
                updateCreatedSet(namedObj.getFullName() + "."
                        + attribute.getName(),
                        classAttribute, classAttribute, createdSet);

                // Initialize the newly created variable.
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

                    // Some parameters (like _hideName) occasionally have no
                    // value.. this is stupid, but we have to deal with it.
                    if(token == null) {
                        token = new Token();
                    }

                    Local tokenLocal =
                        PtolemyUtilities.buildConstantTokenLocal(body,
                                assignStmt, token, "token");

                    // call setToken.
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
                                    Jimple.v().newVirtualInvokeExpr(
                                            variableLocal,
                                            PtolemyUtilities.variableSetTokenMethod,
                                            tokenLocal)));
                    // call validate to ensure that attributeChanged is called.
                    body.getUnits().add(
                            Jimple.v().newInvokeStmt(
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
            }

            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
            SootUtilities.createAndSetFieldFromLocal(body, local,
                    theClass, attributeType, fieldName);


            // FIXME: configurable??
            // recurse so that we get all parameters deeply.
            createFieldsForAttributes(body, context, contextLocal,
                    attribute, local, theClass, createdSet);
	}
}

    private static void _atomic(JimpleBody body, Local containerLocal,
            CompositeActor container, Local entityLocal,
            Entity entity, EntitySootClass entityClass, HashSet createdSet) {
        Entity classEntity;
        try {
            classEntity = (Entity)
                ModelTransformer._findDeferredInstance(entity).clone();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }

        // Create fields for attributes.
        ModelTransformer.createFieldsForAttributes(
                body, entity, entityLocal,
                entity, entityLocal, entityClass, createdSet);

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
            //	    System.out.println("ModelTransformer: entity: " + entity);

            // If we are doing deep codegen, then use the actor
            // classes we created earlier.
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
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
            //    if (entity instanceof CompositeActor) {
//                     _composite(body, containerLocal, container,
//                             local, (CompositeActor)entity,
//                             modelClass, createdSet, options);
//                 } else {
                    // Create fields for attributes.
                    //         ModelTransformer.createFieldsForAttributes(
                    //                        body, entity, entityLocal,
                    //           entity, entityLocal, entityClass, createdSet);

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
    public static void _ports(JimpleBody body, Local containerLocal,
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
            //  String className = port.getClass().getName();
            String className = "ptolemy.actor.TypedIOPort";
            RefType portType = RefType.v(PtolemyUtilities.ioportClass);//className);

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
               //  } else {
//                     System.out.println("Warning: " + modelClass + " does " +
//                             "not declare a field " + fieldName);
//                     // FIXME: Try to analyze the constructor to set
//                     // the field.  This is nontrivial.
//                     // For the moment, we skip this case.
//                     continue;
//                 }
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

    // FIXME: duplicate with Actor transformer.
    private static void _removeSuperExecutableMethods(SootClass theClass) {
        // Loop through all the methods

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
                            if (r.getMethod().getName().equals("prefire") ||
                                    r.getMethod().getName().equals("postfire")) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                body.getUnits().remove(stmt);
                            }
                        } else if (!r.getMethod().getName().equals("<init>")) {
                            System.out.println("superCall:" + r);
                        }
                }
            }
        }
    }

    /** Return an instance that represents the class that
     *  the given object defers to.
     */
    // FIXME: duplicate with MoMLWriter.
    public static NamedObj _findDeferredInstance(NamedObj object) {
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
    public static void updateCreatedSet(String prefix,
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map from Ports to Locals.
    public static Map _portLocalMap;

    // Map from Entitys to Locals.
    public static Map _entityLocalMap;

    // Map from Relations to Locals.
    public static Map _relationLocalMap;

    // The model we are generating code for.
    public CompositeActor _model;

    public static ConstVariableModelAnalysis _constAnalysis;

    private static SootClass _modelClass = null;
    private static Object[] _reflectionArguments = new Object[1];
    private static Workspace _reflectionWorkspace = new Workspace();
    private static MoMLParser _reflectionParser =
    new MoMLParser(_reflectionWorkspace);
}

