/* Transform Actors using Soot

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
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.kernel.ActorTransformer;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLParser;

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

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
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
        return super.getDeclaredOptions() + " deep targetPackage";
    }

    /** Return the name of the field that is created for the
     *  given entity.
     */
    public static String getFieldNameForEntity(Entity entity) {
        return SootUtilities.sanitizeName(entity.getName());
    }

    /** Return the name of the field that is created to 
     *  represent the given channel of the given type of the
     *  given relation.
     */
    public static String getBufferFieldName(TypedIORelation relation, 
            int channel, ptolemy.data.type.Type type) {
        return "_" + SootUtilities.sanitizeName(relation.getName())
            + "_" + channel
            + "_" + SootUtilities.sanitizeName(type.toString());           
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ModelTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // create a class for the model
        String modelClassName = ActorTransformer.getInstanceClassName(_model, options);

        EntitySootClass modelClass =
            new EntitySootClass(PtolemyUtilities.compositeActorClass, modelClassName,
                    Modifier.PUBLIC);
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
        _composite(body, thisLocal, _model, modelClass, options);

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
        if(type instanceof ptolemy.data.type.BaseType) {
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
        } else if(type instanceof ptolemy.data.type.ArrayType) {
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
    private void _composite(JimpleBody body, Local thisLocal, 
            CompositeEntity composite, EntitySootClass modelClass,
            Map options) {

        _ports(body, thisLocal, composite, thisLocal, composite, modelClass);
        _entities(body, thisLocal, composite, modelClass, options);
        // create fields for attributes.
        createFieldsForAttributes(body, composite, thisLocal, 
                composite, thisLocal, modelClass);
       
        // handle the communication
        if(Options.getBoolean(options, "deep")) {
            SootMethod clinitMethod;
            Body clinitBody;
            if(modelClass.declaresMethodByName("<clinit>")) {
                clinitMethod = modelClass.getMethodByName("<clinit>");
                clinitBody = clinitMethod.retrieveActiveBody();
            } else {
                clinitMethod = new SootMethod("<clinit>", new LinkedList(),
                        VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
                modelClass.addMethod(clinitMethod);
                clinitBody = Jimple.v().newBody(clinitMethod);
                clinitMethod.setActiveBody(clinitBody);
                clinitBody.getUnits().add(Jimple.v().newReturnVoidStmt());
            }
            Chain clinitUnits = clinitBody.getUnits();
            
            // If we're doing deep (SDF) codegen, then create a
            // queue for every type of every channel of every relation.
            for(Iterator relations = composite.relationList().iterator();
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
                List destinationPortList = relation.linkedDestinationPortList();
                for(Iterator destinationPorts = destinationPortList.iterator();
                    destinationPorts.hasNext();) {
                    TypedIOPort port = (TypedIOPort)destinationPorts.next();
                    ptolemy.data.type.Type type = port.getType();
                    typeMap.put(type.toString(), type);
                }
                
                for(Iterator types = typeMap.keySet().iterator();
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
                    
                    for(int i = 0; i < relation.getWidth(); i++) {
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
                        clinitUnits.addFirst(Jimple.v().newAssignStmt(arrayLocal, 
                                Jimple.v().newNewArrayExpr(tokenType, 
                                        IntConstant.v(bufferSize))));
                        
                    }
                }
            }
        } else {
            _relations(body, thisLocal, composite);
            _links(body, composite);
            _linksOnPortsContainedByContainedEntities(body, composite);
        }
    }

    // Create and set attributes.
    public static void createFieldsForAttributes(JimpleBody body, 
            NamedObj context,
            Local contextLocal, NamedObj namedObj, Local namedObjLocal, 
            SootClass theClass) {
        // A local that we will use to set the value of our
        // settable attributes.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                PtolemyUtilities.attributeType);
        body.getLocals().add(attributeLocal);
	Local settableLocal = Jimple.v().newLocal("settable",
                PtolemyUtilities.settableType);
	body.getLocals().add(settableLocal);

        NamedObj classObject = _findDeferredInstance(namedObj);
        for(Iterator attributes = namedObj.attributeList().iterator();
	    attributes.hasNext();) {
	    Attribute attribute = (Attribute)attributes.next();
            String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String attributeName = attribute.getName(context);
            String fieldName = SootUtilities.sanitizeName(attributeName);
           
            Local local;
            if(classObject.getAttribute(attribute.getName()) != null) {
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
            }

            // Create a new field for the attribute, and initialize
            // it to the the attribute above.
	    SootUtilities.createAndSetFieldFromLocal(body, local, 
                    theClass, attributeType, fieldName);
            // If the attribute is settable, then set its
            // expression.
	    if(attribute instanceof Settable) {
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
	    }
            // FIXME: configurable??
            // recurse so that we get all parameters deeply.
            createFieldsForAttributes(body, context, contextLocal, 
                    attribute, local, theClass);
	}
    }

    // Create and set entities.
    private void _entities(JimpleBody body, Local thisLocal, 
            CompositeEntity composite, EntitySootClass modelClass,
            Map options) {
	for(Iterator entities = composite.entityList().iterator();
	    entities.hasNext();) {
	    Entity entity = (Entity)entities.next();
	    System.out.println("ModelTransformer: entity: " + entity);
	    String className;
            if(Options.getBoolean(options, "deep")) {
		// If we are doing deep codegen, then use the actor
		// classes we created earlier.
		className = ActorTransformer.getInstanceClassName(entity, options);
	    } else {
            
		// If we are doing shallow, then use the base actor
		// classes.
		className = entity.getClass().getName();
            }
            
            String entityName = getFieldNameForEntity(entity);

	    // Create a new local variable.
	    Local local = 
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, entityName);
	    _entityLocalMap.put(entity, local);

            if(entity instanceof CompositeEntity) {
                _composite(body, local, (CompositeEntity)entity,
                        modelClass, options);
            } else {
                _ports(body, thisLocal, composite, local, entity, modelClass);
            }

            if(Options.getBoolean(options, "deep")) {
		// If we are doing deep codegen, then we
		// include a field for each actor.
		SootUtilities.createAndSetFieldFromLocal(
                        body, local, modelClass,
                        PtolemyUtilities.actorType, entityName);
            } else {
		// If we are doing shallow code generation, then
		// include code to initialize the parameters of this
		// entity.
                // FIXME: flag to not create fields?
                createFieldsForAttributes(body, composite, thisLocal, 
                        entity, local, modelClass);
            }
	}
    }

    // Create and set external ports.
    private void _ports(JimpleBody body, Local containerLocal,
            CompositeEntity container, Local entityLocal,
            Entity entity, EntitySootClass modelClass) {
        Entity classObject = (Entity)_findDeferredInstance(entity);

        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v("ptolemy.kernel.Port"));
        body.getLocals().add(tempPortLocal);

	for(Iterator ports = entity.portList().iterator();
	    ports.hasNext();) {
	    Port port = (Port)ports.next();
	    String className = port.getClass().getName();
            String fieldName =
                SootUtilities.sanitizeName(port.getName(container));
	    Local portLocal;

            portLocal = Jimple.v().newLocal("port",
                    PtolemyUtilities.portType);
            body.getLocals().add(portLocal);

            if(classObject.getPort(port.getName()) != null) {
                // If the class for the object already creates the
                // attribute, then get a reference to the existing attribute.

                // First assign to temp
                body.getUnits().add(Jimple.v().newAssignStmt(tempPortLocal,
                        Jimple.v().newVirtualInvokeExpr(entityLocal,
                                PtolemyUtilities.getPortMethod,
                                StringConstant.v(port.getName()))));
                // and then cast to portLocal
                body.getUnits().add(Jimple.v().newAssignStmt(portLocal,
                        Jimple.v().newCastExpr(tempPortLocal,
                                PtolemyUtilities.portType)));
            } else {
                // If the class does not create the attribute,
                // then create a new attribute with the right name.
                Local local = PtolemyUtilities.createNamedObjAndLocal(
                        body, className,
                        entityLocal, port.getName());
                if(port instanceof TypedIOPort) {
                    TypedIOPort ioport = (TypedIOPort)port;
                    if(ioport.isInput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setInputMethod,
                                        IntConstant.v(1))));
                    }
                    if(ioport.isOutput()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setOutputMethod,
                                        IntConstant.v(1))));
                    }
                    if(ioport.isMultiport()) {
                        body.getUnits().add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(local,
                                        PtolemyUtilities.setMultiportMethod,
                                        IntConstant.v(1))));
                    }
                }
                // and then cast to portLocal
                body.getUnits().add(Jimple.v().newAssignStmt(portLocal,
                        Jimple.v().newCastExpr(local,
                                PtolemyUtilities.portType)));

            }

            
           //   // Set the type of the port if we need to.
//              if(Options.getBoolean(options, "deep") &&
//                      (port instanceof TypedIOPort)) {
//                  TypedIOPort typedPort = (TypedIOPort)port;

//                          // Build a type expression.
//                  Local typeLocal =
//                      _buildConstantTypeLocal(body, typedPort.getType());
//                  Local ioportLocal =
//                      Jimple.v().newLocal("typed_" + port.getName(),
//                              ioportType);
//                  body.getLocals().add(ioportLocal);
//                  body.getUnits().add(Jimple.v().newAssignStmt(ioportLocal,
//                          Jimple.v().newCastExpr(portLocal,
//                                  ioportType)));
//                  body.getUnits().add(Jimple.v().newInvokeStmt(
//                          Jimple.v().newVirtualInvokeExpr(ioportLocal,
//                                  portSetTypeMethod, typeLocal)));
//              }
                    
            _portLocalMap.put(port, portLocal);
	    SootUtilities.createAndSetFieldFromLocal(body, 
                    portLocal, modelClass, PtolemyUtilities.portType,
                    fieldName);
	}
    }

    // Create and set links.
    private void _links(JimpleBody body, CompositeEntity composite) {
	// To get the ordering right,
	// we read the links from the ports, not from the relations.
	// First, produce the inside links on contained ports.
        for(Iterator ports = composite.portList().iterator();
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
   
        for(Iterator entities = composite.entityList().iterator();
            entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            Iterator ports = entity.portList().iterator();
            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort)ports.next();
                
                Local portLocal;
                // If we already have a local reference to the port
                if(_portLocalMap.keySet().contains(port)) {
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
            CompositeEntity composite) {
	_relationLocalMap = new HashMap();
	for(Iterator relations = composite.relationList().iterator();
	    relations.hasNext();) {
	    Relation relation = (Relation)relations.next();
	    String className = relation.getClass().getName();
	    // Create a new local variable.
	    Local local = 
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, relation.getName());
	    _relationLocalMap.put(relation, local);
	}
    }

    // FIXME: duplicate with Actor transformer.
    private static void _removeSuperExecutableMethods(SootClass theClass) {
        // Loop through all the methods 
                        
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
             for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Unit unit = (Unit)units.next();
                Iterator boxes = unit.getUseBoxes().iterator();
                while(boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if(value instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                        if(PtolemyUtilities.executableInterface.declaresMethod(
                                r.getMethod().getSubSignature())) {
                            if(r.getMethod().getName().equals("prefire") ||
                               r.getMethod().getName().equals("postfire")) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                body.getUnits().remove(unit);
                            }
                        } else if(!r.getMethod().getName().equals("<init>")) {
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
    public static NamedObj _findDeferredInstance(NamedObj object) {
        // System.out.println("findDeferred =" + object.getFullName());
        NamedObj deferredObject = null;
        NamedObj.MoMLInfo info = object.getMoMLInfo();
        if(info.deferTo != null) {
            deferredObject = info.deferTo;
            // System.out.println("object = " + object.getFullName());
            //System.out.println("deferredDirectly = " + deferredObject);
            //(new Exception()).printStackTrace(System.out);
        } else if(info.className != null) {
            try {
                // First try to find the local moml class that
                // we extend
                String deferredClass;
                if(info.elementName.equals("class")) {
                    deferredClass = info.superclass;
                } else {
                    deferredClass = info.className;
                }
                
                // No moml class..  must have been a java class.
                // FIXME: This sucks.  We should integrate with 
                // the classloader mechanism.
                String objectType;
                if(object instanceof Attribute) {
                    objectType = "property";
                } else if(object instanceof Port) {
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
                if(deferredObject == null) {
                    // Damn, no workspace constructor.  Let's
                    // try a container, name constructor.
                    // It really would be nice if all of 
                    // our actors had workspace constructors,
                    // but version 1.0 only specified the
                    // (container,name) constructor, and 
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
                        throw new InternalErrorException("Attempt "
                                + "to create an instance of "
                                + deferredClass + " failed because "
                                + "it does not have a Workspace "
                                + "constructor.  Original error:\n"
                                + ex.getMessage());
                    }
                    if(object instanceof Attribute) {
                        deferredObject = 
                            toplevel.getAttribute(object.getName());
                    } else if(object instanceof Port) {
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
                System.err.println("Exception occured during parsing:\n");
                ex.printStackTrace();
                deferredObject = null;
            }
        }
        return deferredObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map from Ports to Locals.
    public Map _portLocalMap;

    // Map from Entitys to Locals.
    public Map _entityLocalMap;

    // Map from Relations to Locals.
    public Map _relationLocalMap;

    // The model we are generating code for.
    public CompositeActor _model;

    private static Object[] _reflectionArguments = new Object[1];
    private static Workspace _reflectionWorkspace = new Workspace();
    private static MoMLParser _reflectionParser =
    new MoMLParser(_reflectionWorkspace);
}

