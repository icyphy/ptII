/* Make all references to attributes point to attribute fields

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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

import soot.HasPhaseOptions;
import soot.Local;
import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;


//////////////////////////////////////////////////////////////////////////
//// FieldsForEntitiesTransformer
/**
A transformer that is responsible for replacing references to
entities.  Any calls to the getContainer(), and getEntity() methods
are replaced with a field reference to the field of the appropriate
class.

FIXME: deal with this also?

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FieldsForEntitiesTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private FieldsForEntitiesTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static FieldsForEntitiesTransformer v(CompositeActor model) {
        return new FieldsForEntitiesTransformer(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage debug";
    }

    /** Given an object in the model, return the first object above it
     *  in the hierarchy that is an entity.  If the object is itself
     *  an entity, then simply return its container.
     */
    public static Entity getEntityContainerOfObject(Nameable object) {
        Nameable container = object.getContainer();
        if (container instanceof Entity) {
            return (Entity)container;
        } else {
            return getEntityContainerOfObject(container);
        }
    }

    /** Return true if the first object is contained in the second.
     */
    public static boolean isContained(Nameable object1, Nameable object2) {
        while(object1 != null && object1 != object2) {
            object1 = object1.getContainer();
        }
        return (object1 == object2);
    }

    /** Insert code into the given body of a method in the given class
     *  before the given unit to get a reference to the object
     *  generated for the given entity.
     *  @exception RuntimeException If no field was created for the
     *  given entity.
     */
    public static Local getLocalReferenceForEntity(
            Entity entity, SootClass sourceClass, Local local, 
            JimpleBody body, Unit unit, Map options) {
        //  System.out.println("Get reference to " + entity + " from " + sourceClass);
        if(ModelTransformer.isActorClass(sourceClass)) {
            Entity sourceEntity = 
                ModelTransformer.getActorForClass(sourceClass);
            if(entity.equals(sourceEntity)) {
                return local;
            }
            if(isContained(entity, sourceEntity)) {
                Entity entityContainer = getEntityContainerOfObject(entity);
                SootClass entityContainerClass = 
                ModelTransformer.getClassForActor(entityContainer);
                Local entityContainerLocal = getLocalReferenceForEntity(
                        entityContainer, sourceClass, local, body, unit, options);
                String fieldName = ModelTransformer.getFieldNameForEntity(
                        entity, sourceEntity);
                SootField field = 
                    entityContainerClass.getFieldByName(fieldName);
                Local newLocal = Jimple.v().newLocal("container",
                        RefType.v(PtolemyUtilities.entityClass));
                body.getLocals().add(newLocal);
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(newLocal,
                                Jimple.v().newInstanceFieldRef(
                                        entityContainerLocal,
                                        field)),
                        unit);
                return newLocal;
            }
            // Then the source class must be something up the hierarchy.
            CompositeEntity container = 
                (CompositeEntity)sourceEntity.getContainer();
            SootClass containerClass = 
                ModelTransformer.getClassForActor(container);
            RefType type = RefType.v(containerClass);
            Local containerLocal = Jimple.v().newLocal("container",
                    type);
            SootField field = sourceClass.getFieldByName(
                    ModelTransformer.getContainerFieldName());
            body.getLocals().add(containerLocal);
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            containerLocal,
                            Jimple.v().newInstanceFieldRef(
                                    local, field)),
                    unit);
            return getLocalReferenceForEntity(entity, containerClass, 
                    containerLocal, body, unit, options); // FIXME!
        } else {
            // Then the source class must be a class for a settable attribute.
            NamedObj sourceObject = 
                ModelTransformer.getObjectForClass(sourceClass);
            Entity container = (Entity)sourceObject.getContainer();
//             System.out.println("sourceObject = " + sourceObject);
//             System.out.println("container = " + container);
            SootClass containerClass = 
                ModelTransformer.getClassForActor(container);
            RefType type = RefType.v(containerClass);
            Local containerLocal = Jimple.v().newLocal("container",
                    type);
            SootField field = sourceClass.getFieldByName(
                    ModelTransformer.getContainerFieldName());
            body.getLocals().add(containerLocal);
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(
                            containerLocal,
                            Jimple.v().newInstanceFieldRef(
                                    local, field)),
                    unit);
//   Local containerLocal = Jimple.v().newLocal("container",
//                                 RefType.v(PtolemyUtilities.entityClass));
//             body.getLocals().add(containerLocal);
//             body.getUnits().insertBefore(
//                     Jimple.v().newAssignStmt(
//                             containerLocal,
//                             Jimple.v().newInterfaceInvokeExpr(
//                                     local, PtolemyUtilities.getContainerMethod)),
//                     unit);
            return containerLocal;
        }
//         throw new RuntimeException(
//                 "Failed to find field for entity " + entity);
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForEntitiesTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _debug = PhaseOptions.getBoolean(options, "debug");

        for(Iterator classes =
                ModelTransformer.actorClassList().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            _createContainerField(theClass);
        }
        for(Iterator classes = 
                ModelTransformer.attributeClassList().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            _createContainerField(theClass);
        }
        for(Iterator classes = 
                ModelTransformer.actorClassList().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            _replaceEntityCalls(theClass);
        }
        for(Iterator classes = 
                ModelTransformer.attributeClassList().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            _replaceEntityCalls(theClass);
        }
    }

    public void _createContainerField(SootClass theClass) {
        NamedObj correspondingObject = 
            ModelTransformer.getObjectForClass(theClass);
        // Create a field referencing the container, for all but the top level.
        if(!correspondingObject.equals(_model)) {
            Entity container = (Entity)correspondingObject.getContainer();
            SootClass containerClass = 
                ModelTransformer.getClassForActor(container);
            SootField field = new SootField(
                    ModelTransformer.getContainerFieldName(),
                    RefType.v(containerClass),
                    Modifier.PUBLIC);
           
            theClass.addField(field);

            ModelTransformer.addFieldForEntity(field, container);
            
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                if(method.getName().equals("<init>")) {
                    if(method.getParameterCount() == 2) {
                        // Assign to the container field.  Assume this
                        // is a container, name constructor.  Note
                        // that classes might have strange types for
                        // the first argument, so it is hard to just
                        // grab the right constructor.
                        JimpleBody body = (JimpleBody)method.getActiveBody();
                        Stmt insertPoint = (Stmt)body.getUnits().getLast();
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newInstanceFieldRef(
                                                body.getThisLocal(),
                                                field),
                                        body.getParameterLocal(0)),
                                insertPoint);
                    } else {
                        // Assign null to the container field.
                        JimpleBody body = (JimpleBody)method.getActiveBody();
                        Stmt insertPoint = (Stmt)body.getUnits().getLast();
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        Jimple.v().newInstanceFieldRef(
                                                body.getThisLocal(),
                                                field),
                                        NullConstant.v()),
                                insertPoint);
                    }
                }
            }
        }
    }

    private void _replaceEntityCalls(SootClass theClass) {
        System.out.println("replacing entity calls in " + theClass);
        NamedObj correspondingObject = ModelTransformer.getObjectForClass(theClass);

        // Replace calls to entity method with field references.
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            if (_debug)
                System.out.println("Replacing entity calls in " + method);

            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
            // this will help us figure out where locals are defined.
            SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);

            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt unit = (Stmt)units.next();
                if (!unit.containsInvokeExpr()) {
                    continue;
                }
                ValueBox box = (ValueBox)unit.getInvokeExprBox();
                Value value = box.getValue();
                if (value instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                    if (r.getMethod().getSubSignature().equals(
                                PtolemyUtilities.getContainerMethod.getSubSignature())) {
                        
                        Value newFieldRef =
                            _getContainerMethodReplacementFieldRef(
                                    theClass, (Local)r.getBase(),
                                    body, unit, localDefs);
                        box.setValue(newFieldRef);
                        if (_debug) System.out.println("replacing " + unit);
                    } else if (r.getMethod().equals(
                            PtolemyUtilities.toplevelMethod)) {
                        // Replace with reference to the toplevel
                        Value newFieldRef = getLocalReferenceForEntity(
                                _model, theClass, 
                                body.getThisLocal(), body, unit, _options);
                        box.setValue(newFieldRef);
                        if (_debug) System.out.println("replacing " + unit);
                    } else if (r.getMethod().getSubSignature().equals(
                            PtolemyUtilities.getEntityMethod.getSubSignature())) {
                        Value nameValue = r.getArg(0);
                        if (Evaluator.isValueConstantValued(nameValue)) {
                            StringConstant nameConstant =
                                (StringConstant)
                                Evaluator.getConstantValueOf(nameValue);
                            String name = nameConstant.value;
                            if (_debug) System.out.println("replacing " + unit);
                            Value newFieldRef =
                                _getEntityMethodReplacementValue(theClass,
                                        (Local)r.getBase(), name,
                                        body, unit, localDefs);
                            box.setValue(newFieldRef);

                            if (_debug) System.out.println("replacing " + unit);
                       
                        } else {
                            String string = "Entity cannot be " +
                                "statically determined";
                            throw new RuntimeException(string);
                        }

                    }
                }
            }
            TypeAssigner.v().transform(body, "ta");
        }
    }

    private Value _getContainerMethodReplacementFieldRef(
            SootClass sourceClass, Local baseLocal,
            JimpleBody body, Unit unit, LocalDefs localDefs) {

        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        SootClass theClass = type.getSootClass();
        NamedObj object = (NamedObj)ModelTransformer.getObjectForClass(
                type.getSootClass());
        if (object != null) {
            // Then we are dealing with a getContainer call on one of the
            // classes we are generating.
            //            Entity container = (Entity)object.getContainer();
            //    return ModelTransformer.getFieldRefForEntity(container);
            return Jimple.v().newInstanceFieldRef(baseLocal,
                    theClass.getFieldByName(ModelTransformer.getContainerFieldName()));
        } else {
            DefinitionStmt stmt = _getFieldDef(baseLocal, unit, localDefs);
            FieldRef ref = (FieldRef) stmt.getRightOp();
            SootField field = ref.getField();
            Entity entity = (Entity)
                ModelTransformer.getEntityForField(field);
            Entity container = (Entity)entity.getContainer();
            return getLocalReferenceForEntity(container, sourceClass, 
                    body.getThisLocal(), body, unit, _options);
        }
  
    //       DefinitionStmt stmt = _getFieldDef(baseLocal, unit, localDefs);
//             System.out.println("stmt = " + stmt);
//             FieldRef ref = (FieldRef) stmt.getRightOp();
//             SootField field = ref.getField();
//             ValueTag tag = (ValueTag) field.getTag("_CGValue");
//             if (tag == null) {
//                 return NullConstant.v();
//             }
//             object = (NamedObj)tag.getObject();
//             CompositeEntity container = (CompositeEntity)object.getContainer();
//             return ModelTransformer.getFieldRefForEntity(container);

            //            throw new RuntimeException("unimplemented case");
            //   // Walk back and get the definition of the field.
            //             DefinitionStmt definition =
            //                 _getFieldDef(baseLocal, unit, localDefs);
            //             InstanceFieldRef fieldRef = (InstanceFieldRef)
            //                 definition.getRightOp();
            //             SootField baseField = fieldRef.getField();
            //             System.out.println("baseField = " + baseField);
            //             return _getContainerMethodReplacementFieldRef(
            //                     (Local)fieldRef.getBase(),
            //                     baseField.getName() + "." + name, definition,
            //                     localDefs);
            //             //baseField.getDeclaringClass().getFieldByName(
            //             //    baseField.getName() + "_" + name);
    }

    private Value _getEntityMethodReplacementValue(
            SootClass sourceClass, Local baseLocal,
            String name, JimpleBody body, Unit unit, LocalDefs localDefs) {
        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        NamedObj object = (NamedObj)ModelTransformer.getActorForClass(
                type.getSootClass());
        Entity entity;
        if (object != null) {
            // Then we are dealing with a getEntity call on one of the
            // classes we are generating.
            entity = (Entity)((CompositeEntity)object).getEntity(name);
        } else {
            DefinitionStmt stmt = _getFieldDef(baseLocal, unit, localDefs);
            FieldRef ref = (FieldRef) stmt.getRightOp();
            SootField field = ref.getField();
            CompositeEntity container = (CompositeEntity)
                ModelTransformer.getEntityForField(field);
            entity = container.getEntity(name);
        }
        if(_debug) System.out.println("found entity = " + entity.getFullName());
        return getLocalReferenceForEntity(entity, sourceClass, 
                body.getThisLocal(), body, unit, _options);
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a variable type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise return null.
     */
    private static DefinitionStmt _getFieldDef(Local local,
            Unit location, LocalDefs localDefs) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof CastExpr) {
                return _getFieldDef((Local)((CastExpr)value).getOp(),
                        stmt, localDefs);
            } else if (value instanceof FieldRef) {
                return stmt;
            } else {
                throw new RuntimeException("unknown value = " + value);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                System.out.println(i.next().toString());
            }
        }
        return null;
    }

    private CompositeActor _model;
    private Map _options;
    private boolean _debug;
}














