/* Make all references to attributes point to attribute fields

 Copyright (c) 2001-2002 The Regents of the University of California.
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
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;


//////////////////////////////////////////////////////////////////////////
//// FieldsForAttributesTransformer
/**
A transformer that is responsible for replacing references to attributes.
Any calls to the getAttribute() method are replaced with a field reference to
the field of the appropriate class that points to the correct attribute.
Any calls to the getDirector() method are replaced with null.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FieldsForAttributesTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldsForAttributesTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static FieldsForAttributesTransformer v(CompositeActor model) {
        return new FieldsForAttributesTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForAttributesTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        Map attributeToFieldMap = new HashMap();
        Map classToObjectMap = new HashMap();

        // This won't actually create any fields, but will pick up
        // the fields that already exist.
        //   _getAttributeFields(ModelTransformer.getModelClass(), _model,
        //        _model, attributeToFieldMap);
        classToObjectMap.put(ModelTransformer.getModelClass(), _model);

        // Loop over all the actor instance classes and get the
        // attribute fields.
        for (Iterator i = _model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass =
                Scene.v().loadClassAndSupport(className);
            _getAttributeFields(entityClass, entity, entity,
                    attributeToFieldMap);
            // And get the attributes of ports too...
            for (Iterator ports = entity.portList().iterator();
                 ports.hasNext();) {
                Port port = (Port)ports.next();
                _getAttributeFields(entityClass, entity, port,
                        attributeToFieldMap);
            }
            classToObjectMap.put(entityClass, entity);
        }

        // Loop over all the entity classes and replace getAttribute calls.
        for (Iterator i = _model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass theClass =
                Scene.v().loadClassAndSupport(className);

            // Replace calls to getAttribute with field references.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

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
                                .getDirectorMethod.getSubSignature())) {
                                // Replace calls to getDirector with
                                // null.  FIXME: we should be able to
                                // do better than this?
                            box.setValue(NullConstant.v());
                        } else if (r.getMethod().equals(
                                PtolemyUtilities.getAttributeMethod)) {
                                // inline calls to getAttribute(arg)
                                // when arg is a string that can be
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
                                Value newFieldRef = _createAttributeField(
                                        baseLocal, name, unit, localDefs,
                                        classToObjectMap, attributeToFieldMap);
                                box.setValue(newFieldRef);
                            } else {
                                String string = "Attribute cannot be " +
                                    "statically determined";
                                throw new RuntimeException(string);
                            }
                        }
                    }
                }
            }
        }
    }

    // Given a local variable that refers to a namedObj, and the name
    // of an attribute in that object, return a new field ref that
    // refers to that attribute.
    private static FieldRef _createAttributeField(Local baseLocal,
            String name, Unit unit, LocalDefs localDefs,
            Map classToObjectMap, Map attributeToFieldMap) {
        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        NamedObj object = (NamedObj)classToObjectMap.get(type.getSootClass());
        System.out.println("name = " + name);
        System.out.println("object = " + object);
        if (object != null) {
            // Then we are dealing with a getAttribute call on one of the
            // classes we are generating.
            Attribute attribute = object.getAttribute(name);
            SootField attributeField = (SootField)
                attributeToFieldMap.get(attribute);
            if (attributeField != null) {
                return Jimple.v().newInstanceFieldRef(
                        baseLocal, attributeField);
            } else {
                return null;
            }
        } else {
            // Walk back and get the definition of the field.
            DefinitionStmt definition =
                _getFieldDef(baseLocal, unit, localDefs);
            InstanceFieldRef fieldRef = (InstanceFieldRef)
                definition.getRightOp();
            SootField baseField = fieldRef.getField();
            System.out.println("baseField = " + baseField);
            return _createAttributeField((Local)fieldRef.getBase(),
                    baseField.getName() + "." + name, definition,
                    localDefs, classToObjectMap, attributeToFieldMap);
            //baseField.getDeclaringClass().getFieldByName(
            //    baseField.getName() + "_" + name);
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
    // attributes of the given object that are expected
    // to exist in the given class
    private void _getAttributeFields(SootClass theClass, NamedObj container,
            NamedObj object, Map attributeToFieldMap) {

        for (Iterator attributes =
                 object.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();

            String fieldName = ModelTransformer.getFieldNameForAttribute(
                    attribute, container);

            if (!theClass.declaresFieldByName(fieldName)) {
                // FIXME: This should be an exception.
                System.out.println("Class " + theClass
                        + " does not declare field for attribute "
                        + attribute.getFullName());
                continue;
            }

            // retrieve the existing field.
            SootField field = theClass.getFieldByName(fieldName);
            // Make the field final and private.
            field.setModifiers((field.getModifiers() & Modifier.STATIC) |
                    Modifier.FINAL | Modifier.PRIVATE);

            field.addTag(new ValueTag(attribute));
            attributeToFieldMap.put(attribute, field);
            // call recursively
            _getAttributeFields(theClass, container,
                    attribute, attributeToFieldMap);
        }
    }

    private CompositeActor _model;
}














