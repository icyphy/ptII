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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;


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
public class FieldsForEntitiesTransformer extends SceneTransformer {
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

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage debug";
    }

    /** Given an object in the model, return the first object above it
     *  in the hierarchy that is an entity.  If the object is itself
     *  an entity, then simply return its container.
     */
    public static Entity getEntityContainerOfObject(Nameable object) {
        Nameable container = object.getContainer();
        if(container instanceof Entity) {
            return (Entity)container;
        } else {
            return getEntityContainerOfObject(container);
        }
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

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForEntitiesTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _debug = Options.getBoolean(options, "debug");
        _entityToFieldMap = new HashMap();
        _fieldToEntityMap = new HashMap();
        _classToObjectMap = new HashMap();

        _createEntityInstanceFields(ModelTransformer.getModelClass(),
                _model);

        _replaceEntityCalls(ModelTransformer.getModelClass(),
                _model);
    }

    private void _replaceEntityCalls(SootClass actorClass,
            ComponentEntity actor) {

        // Replace calls to entity method with field references.
        for (Iterator methods = actorClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            if(_debug)
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
                                PtolemyUtilities
                                .getContainerMethod.getSubSignature())) {

                        Value newFieldRef =
                            _getContainerMethodReplacementFieldRef(
                                    (Local)r.getBase(),
                                    unit, localDefs);
                        box.setValue(newFieldRef);
                        if(_debug) System.out.println("replacing " + unit);
                    } else if (r.getMethod().equals(
                                       PtolemyUtilities.toplevelMethod)) {
                        // Replace with reference to the toplevel
                        Value newFieldRef =
                            Jimple.v().newStaticFieldRef((SootField)
                                    _entityToFieldMap.get(_model));
                        box.setValue(newFieldRef);
                        if(_debug) System.out.println("replacing " + unit);
                   } else if (r.getMethod().getSubSignature().equals(
                                       PtolemyUtilities.getEntityMethod.getSubSignature())) {
                        Value nameValue = r.getArg(0);
                        if (Evaluator.isValueConstantValued(nameValue)) {
                            StringConstant nameConstant =
                                (StringConstant)
                                Evaluator.getConstantValueOf(nameValue);
                            String name = nameConstant.value;

                            Value newFieldRef =
                                _getEntityMethodReplacementFieldRef(
                                        (Local)r.getBase(), name,
                                        unit, localDefs);
                            box.setValue(newFieldRef);
                            if(_debug) System.out.println("replacing " + unit);
                        } else {
                            String string = "Entity cannot be " +
                                "statically determined";
                            throw new RuntimeException(string);
                        }

                    }
                }
            }
            TypeAssigner.v().transform(body, "ta", "");
        }

        if(actor instanceof CompositeEntity && !(actor instanceof FSMActor)) {
            CompositeEntity model = (CompositeEntity)actor;
            // Loop over all the entity classes and replace getAttribute calls.
            for (Iterator i = model.deepEntityList().iterator();
                 i.hasNext();) {
                ComponentEntity entity = (ComponentEntity)i.next();
                String className =
                    ActorTransformer.getInstanceClassName(entity, _options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);
                _replaceEntityCalls(entityClass, entity);

            }
        }
    }

    private Value _getContainerMethodReplacementFieldRef(Local baseLocal,
            Unit unit, LocalDefs localDefs) {

        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        NamedObj object = (NamedObj)_classToObjectMap.get(type.getSootClass());
        if (object != null) {
            // Then we are dealing with a getContainer call on one of the
            // classes we are generating.
            Entity container = (Entity)object.getContainer();
            return getFieldRefForEntity(container);
        } else {
            DefinitionStmt stmt = _getFieldDef(baseLocal, unit, localDefs);
            System.out.println("stmt = " + stmt);
            FieldRef ref = (FieldRef) stmt.getRightOp();
            SootField field = ref.getField();
            ValueTag tag = (ValueTag) field.getTag("_CGValue");
            if(tag == null) {
                return NullConstant.v();
            }
            object = (NamedObj)tag.getObject();
            CompositeEntity container = (CompositeEntity)object.getContainer();
            return getFieldRefForEntity(container);

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
    }

    private FieldRef _getEntityMethodReplacementFieldRef(Local baseLocal,
            String name, Unit unit, LocalDefs localDefs) {

        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        NamedObj object = (NamedObj)_classToObjectMap.get(type.getSootClass());
        if (object != null) {
            // Then we are dealing with a getEntity call on one of the
            // classes we are generating.
            Entity entity = (Entity)((CompositeEntity)object).getEntity(name);
            return getFieldRefForEntity(entity);
        } else {
            DefinitionStmt stmt = _getFieldDef(baseLocal, unit, localDefs);
            FieldRef ref = (FieldRef) stmt.getRightOp();
            SootField field = ref.getField();
            CompositeEntity container = (CompositeEntity) _fieldToEntityMap.get(field);
            return getFieldRefForEntity(container.getEntity(name));
        }
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

    private void _createEntityInstanceFields(SootClass actorClass,
            ComponentEntity actor) {

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
        if(actor instanceof CompositeEntity && !(actor instanceof FSMActor)) {
            // Then recurse
            CompositeEntity model = (CompositeEntity)actor;
            for (Iterator i = model.deepEntityList().iterator();
                 i.hasNext();) {
                ComponentEntity entity = (ComponentEntity)i.next();
                String className =
                    ActorTransformer.getInstanceClassName(entity, _options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);
                _createEntityInstanceFields(entityClass, entity);
            }
        }
    }

    private CompositeActor _model;
    private Map _options;
    private boolean _debug;
    private static Map _entityToFieldMap;
    private static Map _fieldToEntityMap;
    private Map _classToObjectMap;
}














