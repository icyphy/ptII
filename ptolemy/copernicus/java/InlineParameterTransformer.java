

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
import ptolemy.data.type.Typeable;
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining the values of parameters.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class InlineParameterTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlineParameterTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static InlineParameterTransformer v(CompositeActor model) { 
        return new InlineParameterTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("InlineParameterTransformer.internalTransform("
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
            
            // Create a field for every settable attribute,
            // in case we haven't been
            // following coding convention.
            // now initialize each settable.
            for(Iterator attributes =
                    entity.attributeList(Settable.class).iterator();
                attributes.hasNext();) {
                Attribute attribute = (Attribute)attributes.next();
                if(attribute instanceof ptolemy.moml.Location) {
                    // ignore locations.
                    // FIXME: this is a bit of a hack.
                    continue;
                }
                Settable settable = (Settable)attribute;
                
                if(!entityClass.declaresFieldByName(attribute.getName())) {
                    // Create the new field
                    SootField field = new SootField(attribute.getName(), 
                            settableType, Modifier.PUBLIC);
                    entityClass.addField(field);
                }
                
                // Create a field to contain the value of the attribute.
                if(settable instanceof Variable) {
                    SootField field = new SootField("_CGToken_" + attribute.getName(), 
                            tokenType, Modifier.PUBLIC | Modifier.FINAL);
                    entityClass.addField(field);
                    try {
                        field.addTag(new ValueTag(((Variable)settable).getToken()));
                    } catch (Exception ex) {
                    }
                } else {
                    SootField field = new SootField("_CGExpression_" + attribute.getName(), 
                            stringType, Modifier.PUBLIC | Modifier.FINAL);
                    entityClass.addField(field);
                    field.addTag(new ValueTag(settable.getExpression()));
                }
            }

            // replace calls to getAttribute with field references.
            // inline calls to parameter.getToken and getExpression
            for(Iterator methods = entityClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // What about static methods?
                if(method.isStatic()) {
                    continue;
                }
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                // Add a this local...  note that we might not have one.
                Local thisLocal;
                try {
                    thisLocal = body.getThisLocal();
                } catch (Exception ex) {
                    //FIXME: what if no thisLocal?
                    continue;
                }
                /*Jimple.v().newLocal("this", 
                        RefType.v(entityClass));
                body.getLocals().add(thisLocal);
                body.getUnits().addFirst(Jimple.v().newIdentityStmt(thisLocal, 
                        Jimple.v().newThisRef((RefType)thisLocal.getType())));
                */

                // System.out.println("method = " + method);

                CompleteUnitGraph unitGraph = 
                    new CompleteUnitGraph(body);
                // this will help us figure out where locals are defined.
                SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
                SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while(boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                            //      System.out.println("invoking = " + r.getMethod());
                          
                                                        
                            if(r.getMethod().equals(getAttributeMethod)) {
                                // inline calls to getAttribute(arg) when arg is a string
                                // that can be statically evaluated.
                                Value nameValue = r.getArg(0);
                                //System.out.println("attribute name =" + nameValue);
                                if(Evaluator.isValueConstantValued(nameValue)) {
                                    StringConstant nameConstant = 
                                        (StringConstant)
                                        Evaluator.getConstantValueOf(nameValue);
                                    String name = nameConstant.value;
                                    box.setValue(Jimple.v().newInstanceFieldRef(
                                            r.getBase(),
                                            entityClass.getFieldByName(name)));
                                }
                            } else if(r.getBase().getType() instanceof RefType) {
                                RefType type = (RefType)r.getBase().getType();

                                //System.out.println("baseType = " + type);
                                // Statically evaluate constant arguments.
                                Value argValues[] = new Value[r.getArgCount()];
                                int argCount = 0;
                                for(Iterator args = r.getArgs().iterator();
                                    args.hasNext();) {
                                    Value arg = (Value)args.next();
                                //    System.out.println("arg = " + arg);
                                    if(Evaluator.isValueConstantValued(arg)) {
                                        argValues[argCount++] = Evaluator.getConstantValueOf(arg);
                                //        System.out.println("argument = " + argValues[argCount-1]);
                                    } else {
                                        break;
                                    }
                                }

                                if(SootUtilities.derivesFrom(type.getSootClass(), settableClass)) {
                                    // if we are invoking a method on a variable class, then
                                    // attempt to get the constant value of the variable.
                                    Attribute attribute =
                                        getAttributeValue(entity, (Local)r.getBase(), unit, localDefs);
                                                                      
                                    // If the base is not constant, then obviously there is nothing we can do.
                                    if(attribute == null) {
                                        continue;
                                    }
                                    
                                    if(attribute instanceof Typeable) {
                                        PtolemyUtilities.inlineTypeableMethods(body, 
                                                unit, box, r, (Typeable)attribute);
                                       
                                    }

                                    if(attribute instanceof Variable) {
                                        // Deal with tricky methods separately.
                                        if(r.getMethod().getName().equals("getToken")) {
                                            // replace the method call with a field ref.
                                            box.setValue(Jimple.v().newInstanceFieldRef(thisLocal,
                                                    entityClass.getFieldByName("_CGToken_" + 
                                                            attribute.getName())));
                                        } else if(r.getMethod().getName().equals("setToken")) {
                                            // Call attribute changed AFTER we set the token.
                                            PtolemyUtilities.callAttributeChanged(
                                                    (Local)r.getBase(), body, 
                                                    body.getUnits().getSuccOf(unit));
                                            
                                            // replace the entire statement (which must be an invokeStmt anyway)
                                            // with an assignment to the field of the first argument.
                                            body.getUnits().swapWith(unit, 
                                                    Jimple.v().newAssignStmt(
                                                            Jimple.v().newInstanceFieldRef(thisLocal,
                                                                    entityClass.getFieldByName("_CGToken_" + 
                                                                            attribute.getName())),
                                                            r.getArg(0)));
                                        } else if(r.getMethod().getName().equals("getExpression")) {
                                            // First get the token out of the field, and then insert a call
                                            // to its toString method to get the expression.
                                            SootField tokenField = 
                                                entityClass.getFieldByName("_CGToken_" + 
                                                        attribute.getName());
                                            String localName = "_CGTokenLocal" + localCount++;
                                            Local tokenLocal = Jimple.v().newLocal(localName,
                                                        tokenField.getType());
                                            body.getLocals().add(tokenLocal);
                                            
                                            body.getUnits().insertBefore(
                                                    Jimple.v().newAssignStmt(tokenLocal,
                                                            Jimple.v().newInstanceFieldRef(thisLocal,
                                                                   tokenField)),
                                                    unit);
                                            box.setValue(Jimple.v().newVirtualInvokeExpr(tokenLocal, 
                                                    toStringMethod));
                                            // FIXME null result => ""
                                        } else if(r.getMethod().getName().equals("setExpression")) {
                                            // Call attribute changed AFTER we set the token.
                                            PtolemyUtilities.callAttributeChanged(
                                                    (Local)r.getBase(), body, 
                                                    body.getUnits().getSuccOf(unit));
                                            
                                            Token token;
                                            // First create a token with the given expression and then set the
                                            // token to that value.
                                            try {
                                                // FIXME: This is rather tricky..
                                                // is there a better way to do it?
                                                Variable temp = new Variable();
                                                temp.setTypeEquals(((Variable)attribute).getType());
                                                temp.setExpression(((StringConstant)argValues[0]).value);
                                                token = temp.getToken();
                                        } catch (Exception ex) {
                                                throw new RuntimeException("Illegal parameter value = " 
                                                        + argValues[0]);
                                            }
                                            // Create code to instantiate the token
                                            SootField tokenField = 
                                                entityClass.getFieldByName("_CGToken_" + 
                                                        attribute.getName());
                                            String localName = "_CGTokenLocal" + localCount++;
                                            Local tokenLocal = 
                                                PtolemyUtilities.buildConstantTokenLocal(
                                                        body, unit, token, localName);
                                                                                         
                                            body.getUnits().swapWith(unit, 
                                                    Jimple.v().newAssignStmt(
                                                            Jimple.v().newInstanceFieldRef(thisLocal,
                                                                    tokenField), tokenLocal));
                                        } 
                                    } else {
                                        if(r.getMethod().equals(getExpressionMethod)) {
                                            // Call attribute changed AFTER we set the expression
                                            PtolemyUtilities.callAttributeChanged(
                                                    (Local)r.getBase(), body, 
                                                    body.getUnits().getSuccOf(unit));
                                            
                                            box.setValue(Jimple.v().newInstanceFieldRef(thisLocal,
                                                    entityClass.getFieldByName("_CGExpression_" + 
                                                            attribute.getName())));
                                        } else if(r.getMethod().equals(setExpressionMethod)) {
                                            // Call attribute changed AFTER we set the token.
                                            PtolemyUtilities.callAttributeChanged(
                                                    (Local)r.getBase(), body, 
                                                    body.getUnits().getSuccOf(unit));
                                            // replace the entire statement (which must be an invokeStmt anyway)
                                            // with an assignment to the field of the first argument.
                                            body.getUnits().swapWith(unit, 
                                                    Jimple.v().newAssignStmt(
                                                            Jimple.v().newInstanceFieldRef(thisLocal,
                                                                entityClass.getFieldByName("_CGExpression_" + 
                                                                        attribute.getName())),
                                                            r.getArg(0)));
                                        }
                                    }
                                   

                                    /*
                                    // FIXME what about all the other methods???
                                    // If we have a attribute and all the args are constant valued, then
                                    if(argCount == r.getArgCount()) {
                                        // reflect and invoke the same method on our token
                                        Constant constant = SootUtilities.reflectAndInvokeMethod(attribute,
                                                r.getMethod(), argValues);
                                        System.out.println("method result  = " + constant);
                                        
                                        // replace the method invocation.
                                        box.setValue(constant);
                                    }
                                    */
                                } 
                            }                              
                        }
                    }
                }
            }            
        }
    }

    /** Attempt to determine the constant value of the given local, which is assumed to have a token 
     *  type.  Walk backwards through all the possible places that the local may have been defined and
     *  try to symbolically evaluate the token.  If the value can be determined, then return it, otherwise
     *  return null.
     */ 
    public static Token getTokenValue(Entity entity, Local local, 
            Unit location, LocalDefs localDefs) {
        SootClass parameterClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        SootMethod getTokenMethod = 
            parameterClass.getMethod("ptolemy.data.Token getToken()");

        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof CastExpr) {
                // If the local was defined by a cast, then recurse on the value we are
                // casting from.  Note that we assume the type is acceptable.
                return getTokenValue(entity, (Local)((CastExpr)value).getOp(), stmt, localDefs);
            } else if(value instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                if(r.getMethod().equals(getTokenMethod)) {
                    // If the token was defined by getting the token of a variable, then try to symbolically
                    // evaluate the variable and then get it's value.
                    // FIXME: can this be generalized?
                    Variable variable = (Variable) getAttributeValue(entity, (Local)r.getBase(), stmt, localDefs);
                    try {
                        return variable.getToken();
                    } catch (Exception ex) {
                        // System.out.println("getToken on parameter =" + variable);
                    }
                }
            } else {
                // System.out.println("unknown value = " + value);
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

    /** Attempt to determine the constant value of the given local, which is assumed to have a variable
     *  type.  Walk backwards through all the possible places that the local may have been defined and
     *  try to symbolically evaluate the value of the variable. If the value can be determined, 
     *  then return it, otherwise return null.
     */ 
    public static Attribute getAttributeValue(Entity entity, Local local, Unit location, LocalDefs localDefs) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof CastExpr) {
                return getAttributeValue(entity, (Local)((CastExpr)value).getOp(), stmt, localDefs);
            } else if(value instanceof FieldRef) {
                String name = ((FieldRef)value).getField().getName();
                return (Variable)entity.getAttribute(name);
            } else {
                System.out.println("unknown value = " + value);
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

    private CompositeActor _model;
}














