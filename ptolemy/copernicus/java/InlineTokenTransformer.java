/* A transformer that inlines references to tokens.

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
A Transformer that is responsible for inlining the values of tokens.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class InlineTokenTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlineTokenTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static InlineTokenTransformer v(CompositeActor model) { 
        return new InlineTokenTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("InlineTokenTransformer.internalTransform("
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
                            //       System.out.println("invoking = " + r.getMethod());
                         
                            if(r.getBase().getType() instanceof RefType) {
                                RefType type = (RefType)r.getBase().getType();

                                //System.out.println("baseType = " + type);
                                // Statically evaluate constant arguments.
                                Value argValues[] = new Value[r.getArgCount()];
                                int argCount = 0;
                                for(Iterator args = r.getArgs().iterator();
                                    args.hasNext();) {
                                    Value arg = (Value)args.next();
                                    //      System.out.println("arg = " + arg);
                                    if(Evaluator.isValueConstantValued(arg)) {
                                        argValues[argCount++] = Evaluator.getConstantValueOf(arg);
                                        //       System.out.println("argument = " + argValues[argCount-1]);
                                    } else {
                                        break;
                                    }
                                }

                                if(SootUtilities.derivesFrom(type.getSootClass(), tokenClass)) {
                                    // if we are invoking a method on a token class, then
                                    // attempt to get the constant value of the token.
                                    Token token = getTokenValue(entity, (Local)r.getBase(), unit, localDefs);
                                    //  System.out.println("reference to Token with value = " + token);
                                   
                                    // If we have a token and all the args are constant valued, then
                                    if(token != null && argCount == r.getArgCount()) {
                                        // reflect and invoke the same method on our token
                                        Constant constant = 
                                            SootUtilities.reflectAndInvokeMethod(token, r.getMethod(), argValues);
                                        //      System.out.println("method result  = " + constant);
                                        
                                        // replace the method invocation.
                                        box.setValue(constant);
                                    }
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
            } else if(value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if(tag == null) {
                    return null;
                } else {
                    return (Token)tag.getObject();
                }
            } else {
                //       System.out.println("unknown value = " + value);
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














