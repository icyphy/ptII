/* A transformer that inlines references to tokens.

Copyright (c) 2001-2005 The Regents of the University of California.
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
*/
package ptolemy.copernicus.java;

import soot.ArrayType;
import soot.HasPhaseOptions;
import soot.Local;
import soot.PhaseOptions;
import soot.PrimType;
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
import soot.VoidType;

import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import soot.jimple.toolkits.scalar.Evaluator;

import soot.toolkits.graph.CompleteUnitGraph;

import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class InlineTokenTransformer extends SceneTransformer
    implements HasPhaseOptions {
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

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        _debug = PhaseOptions.getBoolean(options, "debug");
        _options = options;
        System.out.println("InlineTokenTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext();) {
            SootClass theClass = (SootClass) classes.next();
            _inlineTokenCalls(theClass);
        }
    }

    private void _inlineTokenCalls(SootClass actorClass) {
        if (_debug) {
            System.out.println("InlineTokenTransformer in class = "
                    + actorClass);
        }

        // Inline calls to token methods that can be statically
        // evaluated.
        for (Iterator methods = actorClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            // What about static methods?
            if (method.isStatic()) {
                continue;
            }

            JimpleBody body = (JimpleBody) method.retrieveActiveBody();

            // Add a this local...  note that we might not have one.
            Local thisLocal;

            try {
                thisLocal = body.getThisLocal();
            } catch (Exception ex) {
                //FIXME: what if no thisLocal?
                continue;
            }

            if (_debug) {
                System.out.println("method = " + method);
            }

            int count = 0;
            boolean doneSomething = true;

            while (doneSomething) {
                doneSomething = false;

                // System.out.println("Inlining tokens iteration " + count++);
                CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

                // This will help us figure out where locals are defined.
                _localDefs = new SimpleLocalDefs(unitGraph);

                // Analyze and tokens that are constructed in this
                // method...  these will likely not have fields
                // for them.
                _tokenAnalysis = new TokenConstructorAnalysis(body, _localDefs);

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt stmt = (Stmt) units.next();

                    if (!stmt.containsInvokeExpr()) {
                        continue;
                    }

                    ValueBox box = stmt.getInvokeExprBox();
                    Value value = box.getValue();

                    if (value instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr r = (InstanceInvokeExpr) value;

                        if (_debug) {
                            System.out.println("invoking = " + r.getMethod());
                        }

                        // Skip initializers.
                        if (r.getMethod().getName().equals("<init>")) {
                            continue;
                        }

                        doneSomething |= _replaceTokenInvocation(body, stmt,
                                box, r);
                    }
                }
            }
        }
    }

    private boolean _replaceTokenInvocation(JimpleBody body, Unit unit,
            ValueBox box, InstanceInvokeExpr r) {
        boolean doneSomething = false;

        if (r.getBase().getType() instanceof RefType) {
            RefType type = (RefType) r.getBase().getType();

            //System.out.println("baseType = " + type);
            // Statically evaluate constant arguments.
            Value[] argValues = new Value[r.getArgCount()];
            int argCount = 0;

            for (Iterator args = r.getArgs().iterator(); args.hasNext();) {
                Value arg = (Value) args.next();

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
                Token token = getTokenValue((Local) r.getBase(), unit,
                        _localDefs, _tokenAnalysis);

                if (_debug) {
                    System.out.println("reference to Token with value = "
                            + token);
                }

                // If we have a token and all the args are constant valued,
                // and the method returns a Constant, or a token, then
                if ((token != null) && (argCount == r.getArgCount())) {
                    if (_debug) {
                        System.out.println("statically invoking " + r);

                        for (int j = 0; j < r.getArgCount(); j++) {
                            System.out.println("argument " + j + " = "
                                    + argValues[j]);
                        }
                    }

                    // reflect and invoke the same method on our token
                    Object object = SootUtilities.reflectAndInvokeMethod(token,
                            r.getMethod(), argValues);

                    if (_debug) {
                        System.out.println("method result  = " + object);
                    }

                    Type returnType = r.getMethod().getReturnType();

                    if (returnType instanceof ArrayType) {
                    } else if (returnType instanceof RefType) {
                        SootClass returnClass = ((RefType) returnType)
                            .getSootClass();

                        if (SootUtilities.derivesFrom(returnClass,
                                    PtolemyUtilities.tokenClass)) {
                            if (_debug) {
                                System.out.println("handling as token type");
                            }

                            Local local = PtolemyUtilities
                                .buildConstantTokenLocal(body,
                                        unit, (Token) object, "token");
                            box.setValue(local);
                            doneSomething = true;
                        } else if (returnClass.getName().equals("java.lang.String")) {
                            if (_debug) {
                                System.out.println("handling as string type");
                            }

                            Constant constant = StringConstant.v((String) object);
                            box.setValue(constant);
                            doneSomething = true;
                        }
                    } else if (returnType instanceof PrimType
                            && !(returnType instanceof VoidType)) {
                        if (_debug) {
                            System.out.println("handling as base type");
                        }

                        // Must be a primitive type...
                        Constant constant = SootUtilities
                            .convertArgumentToConstantValue(object);
                        box.setValue(constant);
                        doneSomething = true;
                    } else {
                        throw new RuntimeException("unknown return type = "
                                + returnType);
                    }

                    // Reanalyze the locals... since this may have changed.
                    CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
                    _localDefs = new SimpleLocalDefs(unitGraph);
                }
            }
        }

        return doneSomething;
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a token type.  Walk backwards through
     *  all the possible places that the local may have been defined
     *  and try to symbolically evaluate the token.  If the value can
     *  be determined, then return it, otherwise return null.
     */
    public static Token getTokenValue(Local local, Unit location,
            LocalDefs localDefs, TokenConstructorAnalysis tokenAnalysis) {
        List definitionList = localDefs.getDefsOfAt(local, location);

        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt) definitionList.get(0);
            Value value = (Value) stmt.getRightOp();

            if (value instanceof Local) {
                return getTokenValue((Local) value, stmt, localDefs,
                        tokenAnalysis);
            } else if (value instanceof CastExpr) {
                // If the local was defined by a cast, then recurse on
                // the value we are casting from.  Note that we assume
                // the type is acceptable.
                return getTokenValue((Local) ((CastExpr) value).getOp(), stmt,
                        localDefs, tokenAnalysis);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef) value).getField();
                ValueTag tag = (ValueTag) field.getTag("_CGValue");

                if (tag == null) {
                    return null;
                } else {
                    return (Token) tag.getObject();
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
    private boolean _debug;
    private Map _options;
    private LocalDefs _localDefs;
    private TokenConstructorAnalysis _tokenAnalysis;
}
