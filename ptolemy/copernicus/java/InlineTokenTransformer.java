/* A transformer that inlines references to tokens.

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

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;


//////////////////////////////////////////////////////////////////////////
//// InlineTokenTransformer
/**
A Transformer that is responsible for inlining the values of tokens.
Whenever a method is invoked on a token, this transformer attempts to
compile-time evaluate that method call.  Usually this will involve
inserting the constant value of the token, if this transformer can
determine what that value is.  Information about the values of tokens
comes from two places: Analysis of token constructors (using the
TokenConstructorAnalysis class) and value information that is
annotated into the model by previous transformation steps using a
ValueTag.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class InlineTokenTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlineTokenTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlineTokenTransformer v(CompositeActor model) {
        return new InlineTokenTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        boolean debug = Options.getBoolean(options, "debug");
        System.out.println("InlineTokenTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // Loop over all the actor instance classes.
        for (Iterator i = _model.deepEntityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass =
                Scene.v().loadClassAndSupport(className);

            if (debug) System.out.println("class = " + entityClass);

            // Inline calls to token methods that can be statically
            // evaluated.
            for (Iterator methods = entityClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // What about static methods?
                if (method.isStatic()) {
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

                if (debug) System.out.println("method = " + method);

                int count = 0;
                boolean doneSomething = true;
                while (doneSomething) {
                    doneSomething = false;
                    // System.out.println("Inlining tokens iteration " + count++);

                    CompleteUnitGraph unitGraph =
                        new CompleteUnitGraph(body);
                    // This will help us figure out where locals are defined.
                    SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);

                    // Analyze and tokens that are constructed in this
                    // method...  these will likely not have fields
                    // for them.
                    TokenConstructorAnalysis tokenAnalysis =
                        new TokenConstructorAnalysis(body, localDefs);

                    for (Iterator units = body.getUnits().snapshotIterator();
                        units.hasNext();) {
                        Unit unit = (Unit)units.next();
                        Iterator boxes = unit.getUseBoxes().iterator();
                        while (boxes.hasNext()) {
                            ValueBox box = (ValueBox)boxes.next();
                            Value value = box.getValue();
                            if (value instanceof InstanceInvokeExpr) {
                                InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                                if (debug) System.out.println("invoking = " + r.getMethod());

                                // Skip initializers.
                                if (r.getMethod().getName().equals("<init>")) {
                                    continue;
                                }

                                if (r.getBase().getType() instanceof RefType) {
                                    RefType type = (RefType)r.getBase().getType();

                                //System.out.println("baseType = " + type);
                                // Statically evaluate constant arguments.
                                    Value argValues[] = new Value[r.getArgCount()];
                                    int argCount = 0;
                                    for (Iterator args = r.getArgs().iterator();
                                        args.hasNext();) {
                                        Value arg = (Value)args.next();
                                        //      System.out.println("arg = " + arg);
                                        if (Evaluator.isValueConstantValued(arg)) {
                                            argValues[argCount++] = Evaluator.getConstantValueOf(arg);
                                            //       System.out.println("argument = " + argValues[argCount-1]);
                                        } else {
                                            break;
                                        }
                                    }

                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.tokenClass)) {

                                        // if we are invoking a method on a token class, then
                                        // attempt to get the constant value of the token.
                                        Token token = getTokenValue(entity, (Local)r.getBase(), unit, localDefs,
                                                tokenAnalysis);
                                        if (debug) System.out.println("reference to Token with value = " + token);

                                        // If we have a token and all the args are constant valued,
                                        // and the method returns a Constant, or a token, then
                                        if (token != null && argCount == r.getArgCount()) {
                                            if (debug) {
                                                System.out.println("statically invoking " + r);
                                                for (int j = 0; j < r.getArgCount(); j++) {
                                                    System.out.println("argument " + j + " = " + argValues[j]);
                                                }
                                            }
                                            // reflect and invoke the same method on our token
                                            Object object =
                                                SootUtilities.reflectAndInvokeMethod(token, r.getMethod(), argValues);
                                            if (debug) System.out.println("method result  = " + object);

                                            Type returnType = r.getMethod().getReturnType();
                                            if (returnType instanceof ArrayType) {
                                            } else if (returnType instanceof RefType) {
                                                SootClass returnClass = ((RefType)returnType).getSootClass();
                                                if (SootUtilities.derivesFrom(returnClass,
                                                        PtolemyUtilities.tokenClass)) {
                                                    if (debug) System.out.println("handling as token type");
                                                    Local local = PtolemyUtilities.buildConstantTokenLocal(body,
                                                            unit, (Token)object, "token");
                                                    box.setValue(local);
                                                    doneSomething = true;
                                                } else if (returnClass.getName().equals("java.lang.String")) {
                                                    if (debug) System.out.println("handling as string type");
                                                    Constant constant = StringConstant.v((String)object);
                                                box.setValue(constant);
                                                doneSomething = true;
                                                }
                                            } else if (returnType instanceof BaseType &&
                                                    !(returnType instanceof VoidType)) {
                                                if (debug) System.out.println("handling as base type");
                                            // Must be a primitive type...
                                                Constant constant =
                                                    SootUtilities.convertArgumentToConstantValue(object);
                                                box.setValue(constant);
                                                doneSomething = true;
                                            } else {
                                                throw new RuntimeException("unknown return type = " + returnType);
                                            }
                                            // Reanalyze the locals... since this may have changed.
                                            unitGraph = new CompleteUnitGraph(body);
                                            localDefs = new SimpleLocalDefs(unitGraph);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a token type.  Walk backwards through
     *  all the possible places that the local may have been defined
     *  and try to symbolically evaluate the token.  If the value can
     *  be determined, then return it, otherwise return null.
     */
    public static Token getTokenValue(Entity entity, Local local,
            Unit location, LocalDefs localDefs,
            TokenConstructorAnalysis tokenAnalysis) {

        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
             if (value instanceof Local) {
                return getTokenValue(entity, (Local)value,
                        stmt, localDefs, tokenAnalysis);
            } else if (value instanceof CastExpr) {
                // If the local was defined by a cast, then recurse on
                // the value we are casting from.  Note that we assume
                // the type is acceptable.
                return getTokenValue(entity,
                        (Local)((CastExpr)value).getOp(), stmt,
                        localDefs, tokenAnalysis);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if (tag == null) {
                    return null;
                } else {
                    return (Token)tag.getObject();
                }
            } else if (value instanceof NewExpr) {
                // Find the value of the constructed token.
                return tokenAnalysis.getConstructedTokenValue(stmt);
            } else {
                //       System.out.println("unknown value = " + value);
            }
        } else {
            /*System.out.println("more than one definition of = " + local);
            for (Iterator i = definitionList.iterator();
                i.hasNext();) {
                System.out.println(i.next().toString());
                }*/
        }
        return null;
    }

    private CompositeActor _model;
}














