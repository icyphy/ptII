

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
public class FieldsForPortsTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldsForPortsTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static FieldsForPortsTransformer v(CompositeActor model) { 
        return new FieldsForPortsTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForPortsTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        Map portToFieldMap = new HashMap();
        Map classToObjectMap = new HashMap();
      
        // This won't actually create any fields, but will pick up
        // the fields that already exist.
        _getPortFields(Scene.v().getMainClass(), _model, 
                _model, portToFieldMap);
        classToObjectMap.put(Scene.v().getMainClass(), _model);
       
        // Loop over all the actor instance classes and get
        // fields for ports.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            _getPortFields(entityClass, entity, entity,
                    portToFieldMap);
            classToObjectMap.put(entityClass, entity);
        }

        // Loop over all the classes and replace getAttribute calls.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);
                  
            // Loop through all the methods in the class.
            for(Iterator methods = theClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                CompleteUnitGraph unitGraph = 
                    new CompleteUnitGraph(body);
                // This will help us figure out where locals are defined.
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
                            // FIXME: string matching is probably not good enough.
                            if(r.getMethod().getName().equals("getPort")) {
                                // Inline calls to getPort(arg) when arg is a string
                                // that can be statically evaluated.
                                Value nameValue = r.getArg(0);
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
                                    Entity object = (Entity)classToObjectMap.get(type.getSootClass());
                                    SootField portField;
                                    if(object != null) {
                                        // Then we are dealing with a getPort call on one of the
                                        // classes we are generating.
                                        Port port = object.getPort(name);
                                        portField = (SootField)
                                            portToFieldMap.get(port);
                                    } else {
                                        // Walk back and get the definition of the field.
                                        SootField baseField = _getFieldDef(baseLocal, unit, localDefs);
                                        portField = baseField.getDeclaringClass().getFieldByName(
                                                baseField.getName() + "_" + name);
                                    }
                                    if(portField != null) {
                                        box.setValue(Jimple.v().newInstanceFieldRef(
                                                r.getBase(), portField));
                                    }                                
                                } else {
                                    System.out.println("Port cannot be statically determined for " + 
                                            unit + " in method " + method + ".");
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
    // FIXME: This is actually a backwards DataFlow problem.
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
    // ports of the given object that are expected 
    // to exist in the given class
    private void _getPortFields(SootClass theClass, Entity container,
            Entity object, Map portToFieldMap) {
      
        for(Iterator ports =
                object.portList().iterator();
            ports.hasNext();) {
            Port port = (Port)ports.next();
                    
            String fieldName =
                SootUtilities.sanitizeName(port.getName(container));
            SootField field;
            if(!theClass.declaresFieldByName(fieldName)) {
                throw new RuntimeException("Class " + theClass 
                        + " does not declare field for port "
                        + port.getFullName());
            } else {
                // retrieve the existing field.
                field = theClass.getFieldByName(fieldName);  
                // Make the field final.
                field.setModifiers(field.getModifiers() | Modifier.FINAL);
            }
            field.addTag(new ValueTag(
                    port));
            portToFieldMap.put(port, field);
            // FIXME: call recursively
            // _getAttributeFields(theClass, container, 
            //        attribute, attributeToFieldMap);
        }
    }

    private CompositeActor _model;
}














