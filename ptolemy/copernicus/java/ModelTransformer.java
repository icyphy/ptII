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
import ptolemy.actor.IORelation;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;

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

        // create a class for the model
        String modelClassName = Options.getString(options, "targetPackage")
            + "." + _model.getName();

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

        // Now instantiate all the stuff inside the model.
        _composite(body, thisLocal, _model, modelClass, options);

        units.add(Jimple.v().newReturnVoidStmt());
        
        _removeSuperExecutableMethods(modelClass);

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

        _externalPorts(body, thisLocal, composite, modelClass);
        _entities(body, thisLocal, composite, modelClass, options);
        // create fields for attributes.
        createFieldsForAttributes(body, composite, thisLocal, 
                composite, modelClass);
       
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
            // queue for every channel of every relation.
            for(Iterator relations = composite.relationList().iterator();
                relations.hasNext();) {
                IORelation relation = (IORelation)relations.next();
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

                // Create a new local variable.
                // FIXME: This is probably not the right type for a relation...
                ptolemy.data.type.Type type = ((TypedIOPort)relation.linkedPortList().get(0)).getType();
                BaseType tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
                Type arrayType = ArrayType.v(tokenType, 1);
                Local arrayLocal = 
                    Jimple.v().newLocal("bufferArray", arrayType);
                clinitBody.getLocals().add(arrayLocal);

                for(int i=0; i < relation.getWidth(); i++) {
                    SootField field = new SootField("_" + 
                            relation.getName() + "_" + i, arrayType,
                            Modifier.PUBLIC | Modifier.STATIC);
                    modelClass.addField(field);
                    
                    // Tag the field with the type.
                    field.addTag(new TypeTag(type));

                    // Create the new buffer
                    clinitUnits.addFirst(Jimple.v().newAssignStmt(
                            Jimple.v().newStaticFieldRef(field),
                            arrayLocal));
                    clinitUnits.addFirst(Jimple.v().newAssignStmt(arrayLocal, 
                            Jimple.v().newNewArrayExpr(tokenType, 
                                    IntConstant.v(bufferSize))));
                    
                }
            }
        } else {
            _relations(body, thisLocal, composite);
            _links(body, composite);
            _linksOnPortsContainedByContainedEntities(body, composite);
        }
    }

    // Create and set attributes.
    public static void createFieldsForAttributes(JimpleBody body, NamedObj container,
            Local containerLocal, NamedObj namedObj, SootClass theClass) {
	Local settableLocal = Jimple.v().newLocal("settable",
						  PtolemyUtilities.settableType);
	body.getLocals().add(settableLocal);

	Chain units = body.getUnits();
       	for(Iterator attributes = namedObj.attributeList().iterator();
	    attributes.hasNext();) {
	    Attribute attribute = (Attribute)attributes.next();
	    String className = attribute.getClass().getName();
            Type attributeType = RefType.v(className);
            String fieldName = 
                SootUtilities.sanitizeName(attribute.getName(container));
            // FIXME: This is a hack.  We should be able to statically determine this
            // like the MoMLWriter does.
            Local local = PtolemyUtilities.createNamedObjAndLocal(body, className,
                    containerLocal, fieldName);
	    SootUtilities.createAndSetFieldFromLocal(body, local, 
                    theClass, attributeType, fieldName);
	    if(attribute instanceof Settable) {
		// cast to Settable.
		units.add(Jimple.v().newAssignStmt(
                        settableLocal,
                        Jimple.v().newCastExpr(
                                local,
                                PtolemyUtilities.settableType)));
		// call setExpression.
		units.add(Jimple.v().newInvokeStmt(
                        Jimple.v().newInterfaceInvokeExpr(
                                settableLocal,
                                PtolemyUtilities.setExpressionMethod,
                                StringConstant.v(
                                        ((Settable)attribute).getExpression()))));
	    }
            // FIXME: configurable??
            // recurse so that we get all parameters deeply.
            createFieldsForAttributes(body, container, local, attribute, theClass);
	}
    }

    // Create and set entities.
    private void _entities(JimpleBody body, Local thisLocal, 
            CompositeEntity composite, EntitySootClass modelClass,
            Map options) {
	_entityLocalMap = new HashMap();
	for(Iterator entities = composite.entityList().iterator();
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
	    Local local = 
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, entity.getName());
	    _entityLocalMap.put(entity, local);

            if(entity instanceof CompositeEntity) {
                _composite(body, local, (CompositeEntity)entity, modelClass, options);
            }

	    if(Options.getBoolean(options, "deep")) {
		// If we are doing deep codegen, then we
		// include a field for each actor.
		SootUtilities.createAndSetFieldFromLocal(body, local, modelClass,
                        PtolemyUtilities.actorType, entity.getName());
	    } else {
		// If we are doing shallow code generation, then
		// include code to initialize the parameters of this
		// entity.
		_initializeParameters(body, composite,
				      entity, thisLocal);
	    }
	}
    }

    // Create and set external ports.
    private void _externalPorts(JimpleBody body, Local thisLocal,
            CompositeEntity composite, EntitySootClass modelClass) {
	_portLocalMap = new HashMap();

	for(Iterator ports = composite.portList().iterator();
	    ports.hasNext();) {
	    Port port = (Port)ports.next();
	    String className = port.getClass().getName();
            String fieldName = SootUtilities.sanitizeName(port.getName());
	    Local local = 
                PtolemyUtilities.createNamedObjAndLocal(body, className,
                        thisLocal, fieldName);
	    _portLocalMap.put(port, local);
	    SootUtilities.createAndSetFieldFromLocal(body, 
                    local, modelClass, PtolemyUtilities.portType,
                    fieldName);
	}
    }

    // Generate code in the given body to initialize all of the attributes
    // in the given container.
    private void _initializeParameters(JimpleBody body,
            NamedObj context, NamedObj container, Local contextLocal) {
        Chain units = body.getUnits();
        // First create a local variable.
        Local attributeLocal = Jimple.v().newLocal("attribute",
                PtolemyUtilities.attributeType);
        body.getLocals().add(attributeLocal);

        Local settableLocal = Jimple.v().newLocal("settable",
                PtolemyUtilities.settableType);
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
                            PtolemyUtilities.getAttributeMethod,
                            StringConstant.v(attribute.getName(context)))));
            // cast to Settable.
            units.add(Jimple.v().newAssignStmt(settableLocal,
                    Jimple.v().newCastExpr(attributeLocal,
                            PtolemyUtilities.settableType)));
            // call setExpression.
            units.add(Jimple.v().newInvokeStmt(
                    Jimple.v().newInterfaceInvokeExpr(settableLocal,
                            PtolemyUtilities.setExpressionMethod,
                            StringConstant.v(((Settable)attribute)
                                    .getExpression()))));

        }
    }

    // Create and set links.
    private void _links(JimpleBody body, CompositeEntity composite) {
	// To get the ordering right,
	// we read the links from the ports, not from the relations.
	// First, produce the inside links on contained ports.
	Chain units = body.getUnits();
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
		units.add(Jimple.v()
			  .newInvokeStmt(Jimple.v()
					 .newVirtualInvokeExpr(portLocal,
							       PtolemyUtilities.insertLinkMethod,
							       IntConstant.v(index),
							       relationLocal)));

	    }
	}
    }

    // Produce the links on ports contained by contained entities.
    private void _linksOnPortsContainedByContainedEntities(
            JimpleBody body, CompositeEntity composite) {
        // This local is used to store the return from the getPort
        // method, before it is stored in a type-specific local variable.
        Local tempPortLocal = Jimple.v().newLocal("tempPort",
                RefType.v("ptolemy.kernel.Port"));
        body.getLocals().add(tempPortLocal);


	Chain units = body.getUnits();

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
                    // Otherwise, create a new local for the given port.
                    Local entityLocal = (Local)_entityLocalMap.get(entity);
                    portLocal = Jimple.v().newLocal(port.getName(),
                            PtolemyUtilities.portType);
                    body.getLocals().add(portLocal);
                    _portLocalMap.put(port, portLocal);
                    // Reference the port.
                    SootClass entityClass = Scene.v().getSootClass(
                            entity.getClass().getName());
                    SootMethod method =
                        SootUtilities.searchForMethodByName(entityClass,
                                "getPort");

                    // First assign to temp
                    units.add(Jimple.v().newAssignStmt(tempPortLocal,
                            Jimple.v().newVirtualInvokeExpr(entityLocal,
                                    method,
                                    StringConstant.v(port.getName()))));
                    // and then cast to portLocal
                    units.add(Jimple.v().newAssignStmt(portLocal,
                            Jimple.v().newCastExpr(tempPortLocal,
                                    PtolemyUtilities.portType)));
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

                    // Call the _insertLink method with the current index.
                    units.add(Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(portLocal,
                                    PtolemyUtilities.insertLinkMethod, IntConstant.v(index),
                                    relationLocal)));
                }
            }
        }
    }

    // Create and set relations.
    private void _relations(JimpleBody body, Local thisLocal, CompositeEntity composite) {
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
}

