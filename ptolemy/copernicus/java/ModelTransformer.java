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
import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ModelTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        SootClass objectClass =
            Scene.v().loadClassAndSupport("java.lang.Object");
        SootClass namedObjClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootClass attributeClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);
        SootClass settableClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
        SootMethod setExpressionMethod =
            settableClass.getMethodByName("setExpression");
        SootClass actorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedAtomicActor");
        Type actorType = RefType.v(actorClass);
        SootClass compositeActorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");
        SootClass portClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentPort");
        Type portType = RefType.v(portClass);
        SootMethod insertLinkMethod =
            SootUtilities.searchForMethodByName(portClass,
                    "insertLink");

        SootClass ioportClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedIOPort");
        Type ioportType = RefType.v(ioportClass);
        SootMethod portSetTypeMethod =
            SootUtilities.searchForMethodByName(ioportClass,
                    "setTypeEquals");

        SootClass parameterClass =
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Parameter");
        SootClass directorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.Director");

        // Don't generate code for TypedAtomicActor
        actorClass.setLibraryClass();

        // create a class for the model
        String modelClassName = Options.getString(options, "targetPackage")
            + "." + _model.getName();

        EntitySootClass modelClass =
            new EntitySootClass(compositeActorClass, modelClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(modelClass);
        modelClass.setApplicationClass();

        // not really sure what this does..
        Scene.v().setMainClass(modelClass);

        SootMethod preinitializeMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "preinitialize");
        SootMethod initializeMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "initialize");
        SootMethod prefireMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "prefire");
        SootMethod fireMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "fire");
        SootMethod postfireMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "postfire");
        SootMethod wrapupMethod =
            SootUtilities.searchForMethodByName(compositeActorClass,
                    "wrapup");

        // Initialize the model.
        {
            SootMethod initMethod = modelClass.getInitMethod();
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Now instantiate all the stuff inside the model.

	    _externalPorts(body, thisLocal, modelClass, portType);

	    _entities(body, thisLocal, modelClass, actorType, options);

            // now Attributes.
	    _createAndSetAttributesFromLocal(_model,
					     modelClass, 
					     attributeType, settableType,
					     setExpressionMethod,
					     body);
	    _relations(body, thisLocal);

	    _links(body, insertLinkMethod);

            // This local is used to store the return from the getPort
            // method, before it is stored in a type-specific local variable.
            Local tempPortLocal = Jimple.v().newLocal("tempPort",
                    RefType.v("ptolemy.kernel.Port"));
            body.getLocals().add(tempPortLocal);

            // Next, produce the links on ports contained
            // by contained entities.
            for(Iterator entities = _model.entityList().iterator();
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
                        // otherwise, create a new local for the given port.
                        Local entityLocal = (Local)_entityLocalMap.get(entity);
                        portLocal = Jimple.v().newLocal(port.getName(),
                                portType);
                        body.getLocals().add(portLocal);
                        _portLocalMap.put(port, portLocal);
                        // reference the port.
                        SootClass entityClass = Scene.v().getSootClass(
                                entity.getClass().getName());
                        SootMethod method =
                            SootUtilities.searchForMethodByName(entityClass,
                                    "getPort");

                        // first assign to temp
                        units.add(Jimple.v().newAssignStmt(tempPortLocal,
                                Jimple.v().newVirtualInvokeExpr(entityLocal,
                                        method,
                                        StringConstant.v(port.getName()))));
                        // and then cast to portLocal
                        units.add(Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(tempPortLocal,
                                        portType)));
                    }
                    /*
                    // Set the type of the port if we need to.
                    if(Options.getBoolean(options, "deep") &&
                            (port instanceof TypedIOPort)) {
                        TypedIOPort typedPort = (TypedIOPort)port;

                        // Build a type expression.
                        Local typeLocal =
                            _buildConstantTypeLocal(body, typedPort.getType());
                        Local ioportLocal =
                            Jimple.v().newLocal("typed_" + port.getName(),
                                    ioportType);
                        body.getLocals().add(ioportLocal);
                        units.add(Jimple.v().newAssignStmt(ioportLocal,
                                Jimple.v().newCastExpr(portLocal,
                                        ioportType)));
                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(ioportLocal,
                                        portSetTypeMethod, typeLocal)));
                    }
                    */
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

                        // call the insertLink method with the current index.
                        units.add(Jimple.v().newInvokeStmt(
                                Jimple.v().newVirtualInvokeExpr(portLocal,
                                        insertLinkMethod, IntConstant.v(index),
                                        relationLocal)));
                    }
                }
            }
            units.add(Jimple.v().newReturnVoidStmt());
        }
        Scene.v().setActiveHierarchy(new Hierarchy());
    }

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

    // Create a new instance field with the given name
    // and type and add it to the
    // given class.  Add statements to the given body to initialize the
    // field from the given local.
    private void _createAndSetFieldFromLocal(SootClass theClass, Type type,
            String name, JimpleBody body, Local local) {
        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();

        Local castLocal;
        if(local.getType().equals(type)) {
            castLocal = local;
        } else {
            castLocal = Jimple.v().newLocal("local_" + name, type);
            body.getLocals().add(castLocal);
            // Cast the local to the type of the field.
            units.add(Jimple.v().newAssignStmt(castLocal,
                    Jimple.v().newCastExpr(local, type)));
        }

        // Create the new field
        SootField field = new SootField(name,
                type, Modifier.PUBLIC);
        theClass.addField(field);
        // Set the field.
        units.add(Jimple.v().newAssignStmt(
                Jimple.v().newInstanceFieldRef(thisLocal, field),
                castLocal));
    }

    // In the given body, create a new local with the given name.
    // The local will refer to an object of type className.
    // Add instructions to the end of the chain of the body
    // to create and initialize a new
    // named object with the given container and name.  Assign the value
    // of the local to the created instance.
    // @return The local that was created.
    private Local _createNamedObjAndLocal(Body body, String className,
            Local container, String name) {
        Chain units = body.getUnits();
        SootClass objectClass;
        if(Scene.v().containsClass(className)) {
            objectClass = Scene.v().getSootClass(className);
        } else {
            objectClass = Scene.v().loadClassAndSupport(className);
        }

        RefType objectType = RefType.v(objectClass);

        // create the new local with the given name.
        Local local = Jimple.v().newLocal(name, objectType);

        // add the local to the body.
        body.getLocals().add(local);

        // create the object
        units.add(Jimple.v().newAssignStmt(local,
                Jimple.v().newNewExpr(objectType)));

        // the objects
        List args = new LinkedList();
        args.add(container);
        args.add(StringConstant.v(name));

        // call the constructor on the object.
        SootMethod constructor =
            SootUtilities.getMatchingMethod(objectClass, "<init>", args);
        units.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(local,
                        constructor, args)));
        return local;
    }

    // Create and set attributes
    private void
	_createAndSetAttributesFromLocal(NamedObj namedObj,
					 SootClass theClass,
					 Type attributeType,
					 Type settableType,
					 SootMethod setExpressionMethod,
					 JimpleBody body) {
	Local settableLocal = Jimple.v().newLocal("settable",
						  settableType);
	body.getLocals().add(settableLocal);


	Chain units = body.getUnits();
	Local thisLocal = body.getThisLocal();
	for(Iterator attributes = namedObj.attributeList().iterator();
	    attributes.hasNext();) {
	    Attribute attribute = (Attribute)attributes.next();
	    String className = attribute.getClass().getName();
	    Local local = _createNamedObjAndLocal(body, className,
						  thisLocal,
						  attribute.getName());
	    _createAndSetFieldFromLocal(theClass, attributeType,
					attribute.getName(), body, local);
	    if(attribute instanceof Settable) {
		// cast to Settable.
		units.add(Jimple.v().newAssignStmt(settableLocal,
						   Jimple.v()
						   .newCastExpr(local,
								settableType)
						   ));
		// call setExpression.
		units.add(Jimple.v()
			  .newInvokeStmt(Jimple.v()
					 .newInterfaceInvokeExpr(settableLocal,
								 setExpressionMethod,
								 StringConstant.v(((Settable)attribute).getExpression()))));
	    }
	    // Set the attributes of attributes.  This handles setting
	    // the iterations of the SDFDirector.
	    _initializeParameters(body, namedObj,
				  attribute, thisLocal);
	}
    }

    // Then Entities
    private void _entities(JimpleBody body, Local thisLocal, 
			   EntitySootClass modelClass, Type actorType,
			   Map options) {
	Map _entityLocalMap = new HashMap();
	for(Iterator entities = _model.entityList().iterator();
	    entities.hasNext();) {
	    Entity entity = (Entity)entities.next();
	    System.out.println("ModelTransformer: entity: " + entity);
	    String className;
	    if(Options.getBoolean(options, "deep")) {
		// If we are doing deep codegen, then use the actor
		// classes we created earlier.
		className = Options.getString(options, "targetPackage") +
		    "." + entity.getName();
	    } else {
		// If we are doing shallow, then use the base actor
		// classes.
		className = entity.getClass().getName();
	    }

	    // Create a new local variable.
	    Local local = _createNamedObjAndLocal(body, className,
						  thisLocal, entity.getName());
	    _entityLocalMap.put(entity, local);

	    if(Options.getBoolean(options, "deep")) {
		// If we are doing deep codegen, then we
		// include a field for each actor.
		_createAndSetFieldFromLocal(modelClass, actorType,
					    entity.getName(), body, local);
	    } else {
		// If we are doing shallow code generation, then
		// include code to initialize the parameters of this
		// entity.
		_initializeParameters(body, _model,
				      entity, thisLocal);
	    }
	}
    }

    // Instantiate external ports
    private void _externalPorts(JimpleBody body, Local thisLocal,
				EntitySootClass modelClass, Type portType) {
	_portLocalMap = new HashMap();

	for(Iterator ports = _model.portList().iterator();
	    ports.hasNext();) {
	    Port port = (Port)ports.next();
	    String className = port.getClass().getName();
	    Local local = _createNamedObjAndLocal(body, className,
						  thisLocal, port.getName());
	    _portLocalMap.put(port, local);
	    _createAndSetFieldFromLocal(modelClass, portType,
					port.getName(), body, local);
	}
    }

    // Generate code in the given body to initialize all of the attributes
    // in the given entity.
    private static void _initializeParameters(JimpleBody body,
            NamedObj context, NamedObj container, Local contextLocal) {
        SootClass namedObjClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootClass attributeClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);

        SootClass settableClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
        SootMethod setExpressionMethod =
            settableClass.getMethodByName("setExpression");

        Chain units = body.getUnits();
        // First create a local variable.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                attributeType);
        body.getLocals().add(attributeLocal);

        Local settableLocal = Jimple.v().newLocal("settable",
                settableType);
        body.getLocals().add(settableLocal);

        // now initialize each settable.
        for(Iterator attributes =
                container.attributeList(Settable.class).iterator();
            attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
            if(attribute instanceof ptolemy.moml.Location) {
                // ignore locations.
                // FIXME: this is a bit of a hack.
                continue;
            }
            Settable settable = (Settable)attribute;

            // first assign to temp
            units.add(Jimple.v().newAssignStmt(attributeLocal,
                    Jimple.v().newVirtualInvokeExpr(contextLocal,
                            getAttributeMethod,
                            StringConstant.v(attribute.getName(context)))));
            // cast to Settable.
            units.add(Jimple.v().newAssignStmt(settableLocal,
                    Jimple.v().newCastExpr(attributeLocal,
                            settableType)));
            // call setExpression.
            units.add(Jimple.v().newInvokeStmt(
                    Jimple.v().newInterfaceInvokeExpr(settableLocal,
                            setExpressionMethod,
                            StringConstant.v(((Settable)attribute)
                                    .getExpression()))));

        }
    }

    // initialize links
    private void _links(JimpleBody body, SootMethod insertLinkMethod) {
	// To get the ordering right,
	// we read the links from the ports, not from the relations.
	// First, produce the inside links on contained ports.
	Chain units = body.getUnits();
	for(Iterator ports = _model.portList().iterator();
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
		// call the insertLink method with the current index.
		units.add(Jimple.v()
			  .newInvokeStmt(Jimple.v()
					 .newVirtualInvokeExpr(portLocal,
							       insertLinkMethod,
							       IntConstant.v(index),
							       relationLocal)));

	    }
	}
    }

    private void _relations(JimpleBody body, Local thisLocal) {
	// next relations.
	Map _relationLocalMap = new HashMap();
	for(Iterator relations = _model.relationList().iterator();
	    relations.hasNext();) {
	    Relation relation = (Relation)relations.next();
	    String className = relation.getClass().getName();
	    // Create a new local variable.
	    Local local = _createNamedObjAndLocal(body, className,
						  thisLocal,
						  relation.getName());
	    _relationLocalMap.put(relation, local);
	}
    }
    private CompositeActor _model;

    // Map from Entitys to Locals.
    Map _entityLocalMap;

    // Map from Ports to Locals.
    Map _portLocalMap;

    // Map from Relations to Locals.
    Map _relationLocalMap;

}

