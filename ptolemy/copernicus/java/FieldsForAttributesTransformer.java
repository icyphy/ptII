

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
import ptolemy.copernicus.kernel.ActorTransformer;
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining references to attributes.
*/
public class FieldsForAttributesTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldsForAttributesTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static FieldsForAttributesTransformer v(CompositeActor model) { 
        return new FieldsForAttributesTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForAttributesTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        Map attributeToFieldMap = new HashMap();
        Map classToObjectMap = new HashMap();
      
        // This won't actually create any fields, but will pick up
        // the fields that already exist.
        _getAttributeFields(Scene.v().getMainClass(), _model, 
                _model, attributeToFieldMap);
        classToObjectMap.put(Scene.v().getMainClass(), _model);
       
        // Loop over all the actor instance classes and get the
        // attribute fields.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = 
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass = 
                Scene.v().loadClassAndSupport(className);
            _getAttributeFields(entityClass, entity, entity,
                    attributeToFieldMap);
            classToObjectMap.put(entityClass, entity);
        }

        // Loop over all the entity classes and replace getAttribute calls.
       for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = 
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass theClass = 
                Scene.v().loadClassAndSupport(className);
       
            // replace calls to getAttribute with field references.
            // inline calls to parameter.getToken and getExpression
            for(Iterator methods = theClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                CompleteUnitGraph unitGraph = 
                    new CompleteUnitGraph(body);
                // this will help us figure out where locals are defined.
                SimpleLocalDefs localDefs =
                    new SimpleLocalDefs(unitGraph);

                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while(boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                            if(r.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.getDirectorMethod.getSubSignature())) {
                                // Replace calls to getDirector with null.
                                // FIXME: we should be able to do better than this?
                                box.setValue(NullConstant.v());
                            } else if(r.getMethod().equals(PtolemyUtilities.getAttributeMethod)) {
                                // inline calls to getAttribute(arg) when arg is a string
                                // that can be statically evaluated.
                                Value nameValue = r.getArg(0);
                                //System.out.println("attribute name =" + nameValue);
                                if(Evaluator.isValueConstantValued(nameValue)) {
                                    StringConstant nameConstant = 
                                        (StringConstant)
                                        Evaluator.getConstantValueOf(nameValue);
                                    String name = nameConstant.value;
                                    // perform type analysis to determine what the 
                                    // type of the base is.
                                    
                                    // FIXME: This is not enough.
                                    Local baseLocal = (Local)r.getBase();
                                    RefType type = (RefType)baseLocal.getType();
                                    NamedObj object = (NamedObj)classToObjectMap.get(type.getSootClass());
                                    SootField attributeField;
                                    if(object != null) {
                                        // Then we are dealing with a getAttribute call on one of the
                                        // classes we are generating.
                                        Attribute attribute = object.getAttribute(name);
                                        attributeField = (SootField)
                                            attributeToFieldMap.get(attribute);
                                    } else {
                                        // Walk back and get the definition of the field.
                                        SootField baseField = _getFieldDef(baseLocal, unit, localDefs);
                                        attributeField = baseField.getDeclaringClass().getFieldByName(
                                                baseField.getName() + "_" + name);
                                    }
                                    if(attributeField != null) {
                                        box.setValue(Jimple.v().newInstanceFieldRef(
                                                r.getBase(), attributeField));
                                    }                                
                                } else {
                                    String string = "Attribute cannot be statically determined";
                                    throw new RuntimeException(string);
                                }
                            }
                        }
                    }
                }
            }            
        }
    }

    /** Attempt to determine the constant value of the 
     *  given local, which is assumed to have a variable
     *  type.  Walk backwards through all the possible
     *   places that the local may have been defined and
     *  try to symbolically evaluate the value of the 
     *  variable. If the value can be determined, 
     *  then return it, otherwise return null.
     */ 
    private static SootField _getFieldDef(Local local, 
            Unit location, LocalDefs localDefs) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof CastExpr) {
                return _getFieldDef((Local)((CastExpr)value).getOp(), stmt, localDefs);
            } else if(value instanceof FieldRef) {
                return ((FieldRef)value).getField();
            } else {
                throw new RuntimeException("unknown value = " + value);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for(Iterator i = definitionList.iterator();
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
       
        for(Iterator attributes =
                object.attributeList().iterator();
            attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
                    
            String fieldName =
                SootUtilities.sanitizeName(attribute.getName(container));
            SootField field;
            if(!theClass.declaresFieldByName(fieldName)) {
                throw new RuntimeException("Class " + theClass 
                        + " does not declare field for attribute "
                        + attribute.getFullName());
            } else {
                // retrieve the existing field.
                field = theClass.getFieldByName(fieldName);   
                // Make the field final.
                field.setModifiers(field.getModifiers() | Modifier.FINAL);
            }
            field.addTag(new ValueTag(
                    attribute));
            attributeToFieldMap.put(attribute, field);
            // call recursively
            _getAttributeFields(theClass, container, 
                    attribute, attributeToFieldMap);
        }
    }

    private CompositeActor _model;
}














