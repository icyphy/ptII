/* Make all references to ports point to port fields

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;

import soot.HasPhaseOptions;
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
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;

//////////////////////////////////////////////////////////////////////////
//// FieldsForPortsTransformer
/**
A transformer that is responsible for replacing references to ports
Any calls to the getPort() method are replaced with a field reference to
the field of the appropriate class that points to the correct port.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FieldsForPortsTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private FieldsForPortsTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static FieldsForPortsTransformer v(CompositeActor model) {
        return new FieldsForPortsTransformer(model);
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

    /** Return the field created that canonically points to the given port.
     */
    public static SootField getPortField(Port port) {
        return (SootField)_portToFieldMap.get(port);
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForPortsTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _debug = PhaseOptions.getBoolean(_options, "debug");
        _portToFieldMap = new HashMap();
   
        _indexExistingFields(ModelTransformer.getModelClass(),
                _model);

        _replacePortCalls(ModelTransformer.getModelClass(),
                _model);

    }

    private void _replacePortCalls(SootClass actorClass,
            ComponentEntity actor) {

        // Loop through all the methods in the class.
        for (Iterator methods = actorClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();

            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            CompleteUnitGraph unitGraph =
                new CompleteUnitGraph(body);
            // This will help us figure out where locals are defined.
            SimpleLocalDefs localDefs =
                new SimpleLocalDefs(unitGraph);

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
                                PtolemyUtilities.getPortMethod.getSubSignature())) {
                        if (_debug) {
                            System.out.println("replacing getPort in " + unit);
                        }
                        // Inline calls to getPort(arg) when
                        // arg is a string that can be
                        // statically evaluated.
                        Value nameValue = r.getArg(0);
                        if (Evaluator.isValueConstantValued(nameValue)) {
                            StringConstant nameConstant =
                                (StringConstant)
                                Evaluator.getConstantValueOf(nameValue);
                            String name = nameConstant.value;
                            // perform type analysis to determine what the
                            // type of the base is.

                            Local baseLocal = (Local)r.getBase();
                            Value newFieldRef = _createPortField(
                                    baseLocal, name, unit, localDefs);
                            if(unit instanceof AssignStmt) {
                                box.setValue(newFieldRef);
                            } else {
                                body.getUnits().remove(unit);
                            }
                        } else {
                            String string = "Port cannot be " +
                                "statically determined";
                            throw new RuntimeException(string);
                        }
                    } 
                }
            }
        }
        if (actor instanceof CompositeEntity && !(actor instanceof FSMActor)) {
            CompositeEntity model = (CompositeEntity)actor;
            // Loop over all the entity classes and replace getPort calls.
            for (Iterator i = model.deepEntityList().iterator();
                 i.hasNext();) {
                ComponentEntity entity = (ComponentEntity)i.next();
                String className =
                    ModelTransformer.getInstanceClassName(entity, _options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);
                _replacePortCalls(entityClass, entity);

            }
        }
    }

    // Given a local variable that refers to an entity, and the name
    // of an port in that object, return a new field ref that
    // refers to that port.  If no reference is found, then return null.
    private Value _createPortField(Local baseLocal,
            String name, Unit unit, LocalDefs localDefs) {

        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        Entity entity = (Entity)
            ModelTransformer.getObjectForClass(type.getSootClass());
        if (entity != null) {
            // Then we are dealing with a getPort call on one of the
            // classes we are generating.
            Port port = entity.getPort(name);
            SootField portField = (SootField)
                _portToFieldMap.get(port);
            if (portField != null) {
                return Jimple.v().newInstanceFieldRef(
                        baseLocal, portField);
            } else {
                return NullConstant.v();
            }
        } else {
            // Walk back and get the definition of the field.
            DefinitionStmt definition =
                _getFieldDef(baseLocal, unit, localDefs);
            InstanceFieldRef fieldRef = (InstanceFieldRef)
                definition.getRightOp();
            SootField baseField = fieldRef.getField();
            System.out.println("baseField = " + baseField);
            SootField portField =
                baseField.getDeclaringClass().getFieldByName(
                        baseField.getName() + "_" + name);
            return Jimple.v().newInstanceFieldRef(
                    baseLocal, portField);
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

    // Populate the given map according to the fields representing the
    // ports of the given object that are expected
    // to exist in the given class
    private void _getPortFields(SootClass theClass, Entity container,
            Entity object) {

        for (Iterator ports = object.portList().iterator();
             ports.hasNext();) {
            Port port = (Port)ports.next();
            
            String fieldName =
                ModelTransformer.getFieldNameForPort(port, container);
            SootField field;
            if (!theClass.declaresFieldByName(fieldName)) {
//                 throw new RuntimeException("Class " + theClass
//                         + " does not declare field "
//                         + fieldName + " for port "
//                         + port.getFullName());
            }

            // retrieve the existing field.
            field = theClass.getFieldByName(fieldName);

            Type type = field.getType();
            if (!(type instanceof RefType)) {
                System.out.println("Class " + theClass
                        + " declares field for port "
                        + port.getFullName() + " but it has type "
                        + type);
                continue;
            } else {
                SootClass fieldClass = ((RefType)type).getSootClass();
                if (!SootUtilities.derivesFrom(fieldClass,
                        PtolemyUtilities.componentPortClass)) {
                    System.out.println("Class " + theClass
                            + " declares field for port "
                            + port.getFullName() + " but it has type "
                            + fieldClass.getName());
                    continue;
                }
            }

            // Make the field final and private.
            field.setModifiers((field.getModifiers() & Modifier.STATIC) |
                    Modifier.FINAL | Modifier.PUBLIC);// | Modifier.PRIVATE);

            field.addTag(new ValueTag(port));
            _portToFieldMap.put(port, field);
            // FIXME: call recursively
            // _getAttributeFields(theClass, container,
            //        attribute, attributeToFieldMap);
        }
    }

    private void _indexExistingFields(SootClass actorClass,
            ComponentEntity actor) {
        // This won't actually create any fields, but will pick up
        // the fields that already exist.
        _getPortFields(actorClass, actor,
                actor);
   
        // Loop over all the actor instance classes and get
        // fields for ports.
        if (actor instanceof CompositeEntity && !(actor instanceof FSMActor)) {
            // Then recurse
            CompositeEntity model = (CompositeEntity)actor;
            for (Iterator i = model.deepEntityList().iterator();
                 i.hasNext();) {
                ComponentEntity entity = (ComponentEntity)i.next();
                String className =
                    ModelTransformer.getInstanceClassName(entity, _options);
                SootClass entityClass =
                    Scene.v().loadClassAndSupport(className);
                _indexExistingFields(entityClass, entity);
            }
        }
    }

    private CompositeActor _model;
    private Map _options;
    private boolean _debug;
    private static Map _portToFieldMap;
}














