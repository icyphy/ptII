

package ptolemy.copernicus.java;

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
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining the values of parameters.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class FieldOptimizationTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldOptimizationTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static FieldOptimizationTransformer v(CompositeActor model) { 
        return new FieldOptimizationTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldOptimizationTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        SootClass stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");
        Type stringType = RefType.v(stringClass);
        SootClass objectClass = 
            Scene.v().loadClassAndSupport("java.lang.Object");
        SootMethod toStringMethod =
            objectClass.getMethod("java.lang.String toString()");
        SootClass namedObjClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootMethod attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");

        SootClass attributeClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);
        SootClass settableClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
        SootMethod getExpressionMethod = 
            settableClass.getMethod("java.lang.String getExpression()");
        SootMethod setExpressionMethod = 
            settableClass.getMethod("void setExpression(java.lang.String)");
        
        SootClass tokenClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
        Type tokenType = RefType.v(tokenClass);
        SootClass parameterClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        SootMethod getTokenMethod = 
            parameterClass.getMethod("ptolemy.data.Token getToken()");
        SootMethod setTokenMethod = 
            parameterClass.getMethod("void setToken(ptolemy.data.Token)");

        // Loop over all the actor instance classes.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = Options.getString(options, "targetPackage")
                + "." + entity.getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
                                              
            for(Iterator fields = entityClass.getFields().iterator();
                fields.hasNext();) {
                SootField field = (SootField)fields.next();
                // FIXME: static fields too.
                if(Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                boolean finalize = true;
                Value fieldValue = null;
                for(Iterator methods = entityClass.getMethods().iterator();
                    (methods.hasNext() && finalize);) {
                    SootMethod method = (SootMethod)methods.next();
                    if(method.getName().equals("<init>")) {
                        Chain units = method.retrieveActiveBody().getUnits();
                        Stmt stmt = (Stmt)units.getLast();
                        while(!stmt.equals(units.getFirst())) {
                            if(stmt instanceof DefinitionStmt &&
                                    ((DefinitionStmt)stmt).getLeftOp() instanceof InstanceFieldRef) {
                                InstanceFieldRef ref = (InstanceFieldRef) ((DefinitionStmt)stmt).getLeftOp();
                                if(ref.getField() == field && fieldValue == null) {
                                    fieldValue = ((DefinitionStmt)stmt).getRightOp();
                                    break;
                                } else if(fieldValue != null) {
                                    finalize = false;
                                }
                            }
                            stmt = (Stmt)units.getPredOf(stmt);
                        }
                    }
                }
                if(finalize && fieldValue != null) {
                    System.out.println("field " + field + " has final value = " + fieldValue);
                }
            }
        }
    }
    private CompositeActor _model;
}














