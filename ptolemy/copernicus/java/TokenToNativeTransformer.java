/* A transformer that unboxes tokens

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.type.Typeable;

import ptolemy.copernicus.kernel.*;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.DoubleType;
import soot.FloatType;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.NullType;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.NeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.UnusedLocalEliminator;


//////////////////////////////////////////////////////////////////////////
//// TokenToNativeTransformer
/**
A transformer that is responsible for unboxing tokens, i.e. replacing the
token with the value that is contained by that token.  This transformer
attempts to do this by replacing each token with the fields contained in
the appropriate token class and inlining the methods that are implemented
for that token class.  This is made more complex by the fact that tokens
may themselves be contained by other tokens.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class TokenToNativeTransformer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private TokenToNativeTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static TokenToNativeTransformer v(CompositeActor model) {
        return new TokenToNativeTransformer(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "level:0";
    }

    public String getDeclaredOptions() {
        return "debug level";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        _phaseName = phaseName;
        System.out.println("TokenToNativeTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // Only try unboxing tokens if the types are amenable.
        // Currently, arrays of arrays and records don't work.
        try {
            if(_hasBadTypes(_model)) {
                throw new RuntimeException("Token unboxing not possible because" +
                        " the model contains bad types.");
            }
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        // We need all classes as library:
        //      for (Iterator classes = Scene.v().getClasses().iterator();
        //              classes.hasNext();) {
        //              SootClass theClass = (SootClass)classes.next();
        //              if (theClass.isContextClass()) {
        //                  theClass.setLibraryClass();
        //              }
        //          }

        // Compute the set of locals that are tokens, but not safe to inline.
        // For now, this includes any token that originates
        // from a call to evaluateParseTree.
        Set unsafeLocalSet = new HashSet();

        boolean debug = PhaseOptions.getBoolean(options, "debug");
        int level = PhaseOptions.getInt(options, "level");

        entityFieldToTokenFieldToReplacementField = new HashMap();
        entityFieldToIsNotNullField = new HashMap();
        localToFieldToLocal = new HashMap();
        localToIsNotNullLocal = new HashMap();

        // FIXME: Compute max depth.
        int depth = 4;
        while (depth > level) {

            // Inline all methods on types that have the given depth.
            //    for (Iterator classes =
            //                      Scene.v().getApplicationClasses().iterator();
            //                  classes.hasNext();) {
            //                 SootClass entityClass = (SootClass)classes.next();

            //                 inlineTypeMethods(entityClass, depth, unsafeLocalSet, debug);
            //             }
            List classList = new LinkedList();
            classList.addAll(Scene.v().getApplicationClasses());

            updateTokenTypes(classList, depth, unsafeLocalSet, debug);

            // Inline all methods on tokens that have the given depth.
            for (Iterator classes =
                     Scene.v().getApplicationClasses().iterator();
                 classes.hasNext();) {
                SootClass entityClass = (SootClass)classes.next();

                inlineTokenAndTypeMethods(entityClass, depth,
                        unsafeLocalSet, debug);
            }

            updateTokenTypes(classList, depth, unsafeLocalSet, debug);

            // Create replacement fields for all token fields in the
            // given class with the given depth.
            for (Iterator classes =
                     Scene.v().getApplicationClasses().iterator();
                 classes.hasNext();) {
                SootClass entityClass = (SootClass)classes.next();

                createReplacementTokenFields(entityClass, depth,
                        unsafeLocalSet, debug);
            }

            // Replace the locals and fields of the given depth.
            for (Iterator classes =
                     Scene.v().getApplicationClasses().iterator();
                 classes.hasNext();) {
                SootClass entityClass = (SootClass)classes.next();

                replaceTokenFields(entityClass, depth,
                        unsafeLocalSet, debug);
            }

            for (Iterator classes = classList.iterator();
                 classes.hasNext();) {
                SootClass entityClass = (SootClass)classes.next();
                
                // This will allow us to get a better type inference below.
                for (Iterator methods = entityClass.getMethods().iterator();
                     methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();
                    if (debug) System.out.println("method = " + method);
                    
                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                    
                    LocalSplitter.v().transform(
                            body, _phaseName + ".ls");
                    // We may have locals with the same name.  Rename them.
                    LocalNameStandardizer.v().transform(
                            body, _phaseName + ".lns");
                }
            }
 
            //           updateTokenTypes(classList, depth, unsafeLocalSet, debug);
           
            depth--;
        }
    }

    public void updateTokenTypes(List classList, int depth,
            Set unsafeLocalSet, boolean debug) {
        System.out.println("updating token types for all classes");

        for (Iterator classes = classList.iterator();
             classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();

            // This will allow us to get a better type inference below.
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                if (debug) System.out.println("method = " + method);

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                // First split local variables that are used in
                // multiple places.
                LocalSplitter.v().transform(
                        body, _phaseName + ".ls");
                // We may have locals with the same name.  Rename them.
                LocalNameStandardizer.v().transform(
                        body, _phaseName + ".lns");
                // Assign types to local variables... This types
                // everything that isn't a token type.
                TypeAssigner.v().transform(
                        body, _phaseName + ".ta");

                // Run some cleanup...  this will speedup the rest
                // of the analysis.  And prevent typing errors.
                //     TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                //                         body, _phaseName + ".tie", unsafeLocalSet,
                //                         true);

                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");

                // Run some cleanup...  this will speedup the rest
                // of the analysis.  And prevent typing errors.
                TypeAssigner.v().transform(
                        body, _phaseName + ".ta");

                TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                        body, _phaseName + ".tie", unsafeLocalSet,
                        false);

                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");
                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");
                LocalSplitter.v().transform(
                        body, _phaseName + ".ls");
                TypeAssigner.v().transform(
                        body, _phaseName + ".ta");
                TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                        body, _phaseName + ".cie", 
                        Collections.EMPTY_SET, debug);
            }
        }

        TypeSpecializerAnalysis typeAnalysis =
            new TypeSpecializerAnalysis(classList, unsafeLocalSet);
        //         TokenTypeAnalysis typeAnalysis =
        //             new TokenTypeAnalysis(classList, unsafeLocalSet);

        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();

            // Specialize the code, according to the analyzed types.
            TypeSpecializer.specializeTypes(debug, entityClass,
                    unsafeLocalSet, typeAnalysis);
        }
    }

    public void inlineTokenAndTypeMethods(SootClass entityClass, int depth,
            Set unsafeLocalSet, boolean debug) {
        // Inline all token methods, until we run out of things to inline
        boolean doneSomething = true;
        int count = 0;
        while (doneSomething && count < 20) {
            doneSomething = false;
            count++;

            if (debug) {
                System.err.println("inlining token methods in " + entityClass + " iteration " +
                        count + " depth = " + depth);
                System.out.println("inlining token methods in " + entityClass + " iteration " +
                        count + " depth = " + depth);
            }

            // This will allow us to get a better type inference below.
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                // First split local variables that are used in
                // multiple places.
                LocalSplitter.v().transform(
                        body, _phaseName + ".ls");
                // We may have locals with the same name.  Rename them.
                LocalNameStandardizer.v().transform(
                        body, _phaseName + ".lns");
                // Assign types to local variables... This types
                // everything that isn't a token type.
                TypeAssigner.v().transform(
                        body, _phaseName + ".ta");

                // Run some cleanup...  this will speedup the rest
                // of the analysis.  And prevent typing errors.
                //     TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                //                         body, _phaseName + ".tie", unsafeLocalSet,
                //                         true);

                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");

                // Run some cleanup...  this will speedup the rest
                // of the analysis.  And prevent typing errors.
                if (debug) 
                    System.out.println("eliminating instanceof in " + method); 
                TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                        body, _phaseName + ".tie", unsafeLocalSet,
                        debug);

                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");
                UnreachableCodeEliminator.v().transform(
                        body, _phaseName + ".uce");
                CopyPropagator.v().transform(
                        body, _phaseName + ".cp");
                ConstantPropagatorAndFolder.v().transform(
                        body, _phaseName + ".cpf");
                ConditionalBranchFolder.v().transform(
                        body, _phaseName + ".cbf");
                DeadAssignmentEliminator.v().transform(
                        body, _phaseName + ".dae");
                UnusedLocalEliminator.v().transform(
                        body, _phaseName + ".ule");
                LocalSplitter.v().transform(
                        body, _phaseName + ".ls");
                TypeAssigner.v().transform(
                        body, _phaseName + ".ta");
                TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                        body, _phaseName + ".cie", 
                        Collections.EMPTY_SET, debug);
            }

            // InvokeGraph invokeGraph = ClassHierarchyAnalysis.newInvokeGraph();
            //Scene.v().setActiveInvokeGraph(invokeGraph);
            //VariableTypeAnalysis vta = new VariableTypeAnalysis(invokeGraph);

            // Now run the type specialization algorithm...  This
            // allows us to resolve the methods that we are inlining
            // with better precision.
            // Map objectToTokenType = TypeSpecializer.specializeTypes(
            //   debug, entityClass, unsafeLocalSet);
            TypeSpecializerAnalysis typeAnalysis =
                new TypeSpecializerAnalysis(entityClass, unsafeLocalSet);

            // Specialize the code, according to the analyzed types.
            //TypeSpecializer.specializeTypes(true, entityClass, unsafeLocalSet, typeAnalysis);

            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // Check to see if the method is safe to modify with:
                // It has no arguments or return values which are 
                // tokens.
                if(_methodWillBeInlined(method)) {
                    continue;
                }

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                CompleteUnitGraph unitGraph =
                    new CompleteUnitGraph(body);
                SimpleLocalDefs localDefs =
                    new SimpleLocalDefs(unitGraph);
                SimpleLocalUses localUses =
                    new SimpleLocalUses(unitGraph, localDefs);
                //  TokenTypeAnalysis typeAnalysis =
                //    new TokenTypeAnalysis(method, unitGraph);

                if (debug) System.out.println("method = " + method);

                // First inline all the methods that execute on Tokens.
                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt stmt = (Stmt)units.next();
                    if (debug) System.out.println("unit = " + stmt);
                    if (stmt.containsInvokeExpr()) {
                        ValueBox box = stmt.getInvokeExprBox();
                        Value value = box.getValue();
                        if (debug) System.out.println("value = " + value);
                        boolean flag = _inlineTokenMethodsIn(
                                method, body, stmt, box,
                                localDefs, localUses, typeAnalysis,
                                depth, unsafeLocalSet, debug);
                        doneSomething |= flag;
                        if (!flag) {
                            doneSomething |= _inlineTypeMethodsIn(
                                    method, body, stmt, box,
                                    localDefs, localUses,
                                    depth, unsafeLocalSet, debug);
                        }
                    }
                }
            }
        }
    }

    public boolean _inlineTokenMethodsIn(
            SootMethod method, JimpleBody body, Unit unit, ValueBox box,
            SimpleLocalDefs localDefs, SimpleLocalUses localUses,
            TypeSpecializerAnalysis typeAnalysis,
            int depth, Set unsafeLocalSet, boolean debug) {

        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        boolean doneSomething = false;
        Value value = box.getValue();

        // If we have a call to evaluateParseTree, then we can't
        // inline any of those locals.  This sucks, but I can't think
        // of a better way to statically determine the value of the
        // token.  In some cases, this doesn't work because things are
        // inlined on an unsafe local before we know it is unsafe!
        if (value instanceof InvokeExpr) {
            InvokeExpr r = (InvokeExpr)value;
            if (unit instanceof AssignStmt &&
                    r.getMethod().getName().equals("evaluateParseTree")) {
                AssignStmt stmt = (AssignStmt)unit;
                unsafeLocalSet.add(stmt.getLeftOp());
                unsafeLocalSet.addAll(_computeTokenLocalsDefinedFrom(
                        localUses, stmt));
                if (debug) System.out.println("unsafeLocalSet = " + unsafeLocalSet);
            }
        }

        if (value instanceof VirtualInvokeExpr ||
                value instanceof InterfaceInvokeExpr ||
                value instanceof SpecialInvokeExpr) {
            InstanceInvokeExpr r = (InstanceInvokeExpr)value;

            Local local = (Local)r.getBase();
            Type baseType = local.getType();

            if (baseType instanceof NullType) {
                // replace with NullPointerException..
                if (debug) {
                    System.out.println("Invoke base is null, replacing with " +
                            "NullPointerException");
                }
                Local exception = SootUtilities.createRuntimeException(
                        body, unit, "NullPointerException");
                body.getUnits().insertBefore(
                        Jimple.v().newThrowStmt(exception),
                        unit);
                body.getUnits().remove(unit);
            }

            boolean isInlineableTokenMethod = _isLocalTokenTypeWithDepth(
                local, typeAnalysis, unsafeLocalSet,
                depth, debug);

            // Check if token arguments are being used.  This makes
            // sure we get methods like Scale._scaleOnRight and
            // DB._doFunction inlined.  It would be better if the
            // token inliner modified the functions, but it doesn't.
            if (baseType instanceof RefType &&
                    Scene.v().getApplicationClasses().contains(
                            ((RefType)baseType).getSootClass())) {
                Type returnType = r.getMethod().getReturnType(); 
                isInlineableTokenMethod |= _isInlineableTokenType(returnType);
                        
                for (Iterator args = r.getArgs().iterator();
                     args.hasNext() && !isInlineableTokenMethod;) {
                    Object arg = args.next();
                    if (arg instanceof Local) {
                        Local argLocal = (Local)arg;
                        if (debug) System.out.println("argtype = " + argLocal.getType());
                        isInlineableTokenMethod = _isLocalTokenTypeWithDepth(
                                argLocal, typeAnalysis, unsafeLocalSet,
                                depth, debug);
                        if (debug) {
                            System.out.println("isInlineableTokenMethod = " +
                                    isInlineableTokenMethod);
                        }
                    }
                }
            }
        
            if (!isInlineableTokenMethod) {
                return false;
            }

            // System.out.println("baseType = " + baseType);
            RefType type = (RefType)typeAnalysis.getSpecializedSootType(local);
            // System.out.println("specializedType = " + type);

            // Then determine the method that was
            // actually invoked.
            List methodList;
            if (value instanceof SpecialInvokeExpr) {
                SootMethod targetMethod = hierarchy.resolveSpecialDispatch(
                        (SpecialInvokeExpr)r, method);
                methodList = new LinkedList();
                methodList.add(targetMethod);
            } else {
                methodList =
                    //      invokeGraph.getTargetsOf((Stmt)unit);
                    hierarchy.resolveAbstractDispatch(
                            type.getSootClass(),
                            r.getMethod());
            }

            // If there was only one possible method...
            if (methodList.size() == 1) {
                // Then inline its code
                SootMethod inlinee = (SootMethod)methodList.get(0);
                if (inlinee.getName().equals("getClass")) {
                    SootClass typeClass = type.getSootClass();
                    int subclasses =
                        hierarchy.getSubclassesOf(typeClass).size();
                    if(subclasses == 0) {
                        // FIXME: do something better here.
                        SootMethod newGetClassMethod = Scene.v().getMethod(
                                "<java.lang.Class: java.lang.Class "
                                + "forName(java.lang.String)>");
                        box.setValue(Jimple.v().newStaticInvokeExpr(
                                             newGetClassMethod,
                                             StringConstant.v(
                                                     typeClass.getName())));
                        doneSomething = true;
                    }
                } else {
                    SootClass declaringClass = inlinee.getDeclaringClass();
                    if(!declaringClass.isApplicationClass()) {
                        declaringClass.setLibraryClass();
                    }
                    if (!inlinee.isAbstract() &&
                            !inlinee.isNative()) {
                        // FIXME: only inline things where we are
                        // also inlining the constructor???
                        if (debug) System.out.println("inlining " + inlinee);
                        inlinee.retrieveActiveBody();
                        // Then we know exactly what method will
                        // be called, so inline it.
                        SiteInliner.inlineSite(
                                inlinee, (Stmt)unit, method);
                        doneSomething = true;
                    } else {
                        throw new RuntimeException(
                                "inlinee is not concrete!: " + inlinee);
                    }
                }
            } else {
                if (debug) {
                    System.out.println("uninlinable method invocation = " + r);
                    for (Iterator j = methodList.iterator();
                         j.hasNext();) {
                        System.out.println("method = " + j.next());
                    }
                }
            }
        } else if (value instanceof SpecialInvokeExpr) {
            SpecialInvokeExpr r = (SpecialInvokeExpr)value;
            if (debug) System.out.println("special invoking = " + r.getMethod());

            Type baseType = typeAnalysis.getSpecializedSootType((Local)r.getBase());

            if (baseType instanceof RefType) {
                RefType type = (RefType)baseType;

                boolean isInlineableTokenMethod =
                    SootUtilities.derivesFrom(type.getSootClass(),
                            PtolemyUtilities.tokenClass);

                // If it is a token, then check to
                // make sure that it has the
                // appropriate type
                if (isInlineableTokenMethod) {
                    type = (RefType)typeAnalysis.getSpecializedSootType((Local)r.getBase());
                    if (PtolemyUtilities.getTypeDepth(
                            typeAnalysis.getSpecializedType((Local)r.getBase())) != depth) {
                        if (debug) System.out.println("skipping, type depth = " +
                                PtolemyUtilities.getTypeDepth(
                                        typeAnalysis.getSpecializedType((Local)r.getBase())) +
                                ", but only inlining depth " + depth);
                        return false;
                        //continue;
                    }
                }

                if (isInlineableTokenMethod) {

                    SootMethod inlinee =
                        hierarchy.resolveSpecialDispatch(
                                r, method);
                    SootClass declaringClass = inlinee.getDeclaringClass();
                    if(!declaringClass.isApplicationClass()) {
                        declaringClass.setLibraryClass();
                    }
                    if (!inlinee.isAbstract() &&
                            !inlinee.isNative()) {
                        if (debug) System.out.println("inlining");
                        inlinee.retrieveActiveBody();
                        // Then we know exactly what method will
                        // be called, so inline it.
                        SiteInliner.inlineSite(
                                inlinee, (Stmt)unit, method);
                        doneSomething = true;
                    } else {
                        if (debug) System.out.println("removing");
                        // If we don't have a method,
                        // then remove the invocation.
                        body.getUnits().remove(unit);
                    }
                }
            }
        } else if (value instanceof StaticInvokeExpr) {
            StaticInvokeExpr r = (StaticInvokeExpr)value;
            // Inline typelattice methods.
            if (r.getMethod().getDeclaringClass().equals(
                    PtolemyUtilities.typeLatticeClass)) {
                try {
                    if (debug) {
                        System.out.println("inlining typelattice method = "
                                + unit);
                    }
                    typeAnalysis.inlineTypeLatticeMethods(method,
                            unit, box, r, localDefs, localUses);
                } catch (Exception ex) {
                    System.out.println("Exception occurred "
                            + ex.getMessage());
                }
            } else {
                if (debug) {
                    System.out.println("static invoking = " + r.getMethod());
                }
                SootMethod inlinee = (SootMethod)r.getMethod();
                SootClass declaringClass = inlinee.getDeclaringClass();
                Type returnType = inlinee.getReturnType();

                // These methods contain a large amount of
                // code, which greatly slows down further
                // inlining.  The code should also contain
                // little information, and is hard to get
                // rid of any other way.
                if (_mangleExceptionMessages && (
                        inlinee.getName().equals("notSupportedMessage") ||
                        inlinee.getName().equals("notSupportedConversionMessage") ||
                        inlinee.getName().equals("notSupportedIncomparableMessage") ||
                        inlinee.getName().equals("notSupportedIncomparableConversionMessage"))) {
                    box.setValue(StringConstant.v("Token Exception"));
                } else if (SootUtilities.derivesFrom(declaringClass,
                        PtolemyUtilities.tokenClass) ||
                           ((returnType instanceof RefType) &&
                                   SootUtilities.derivesFrom(
                                          ((RefType)returnType).getSootClass(),
                                          PtolemyUtilities.tokenClass))) {
                    // Note that we make sure to inline method like
                    // UtilityFunctions.gaussian, which returns a
                    // DoubleMatrixToken.
                    if(!declaringClass.isApplicationClass()) {
                        declaringClass.setLibraryClass();
                    }
                    if (!inlinee.isAbstract() &&
                            !inlinee.isNative()) {
                        if (debug) System.out.println("inlining");
                        inlinee.retrieveActiveBody();
                        // Then we know exactly what method will
                        // be called, so inline it.
                        SiteInliner.inlineSite(
                                inlinee, (Stmt)unit, method);
                        doneSomething = true;
                    }
                }
            }
        }
        return doneSomething;
    }

    public void inlineTypeMethods(SootClass entityClass, int depth, Set unsafeLocalSet, boolean debug) {
        // Inline all token methods, until we run out of things to inline
        boolean doneSomething = true;
        int count = 0;
        while (doneSomething && count < 20) {
            doneSomething = false;
            count++;
            if (debug) {
                System.err.println("inlining type methods in " + entityClass + " iteration " +
                        count + " depth = " + depth);
                System.out.println("inlining type methods in " + entityClass + " iteration " +
                        count + " depth = " + depth);
            }

            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                CompleteUnitGraph unitGraph =
                    new CompleteUnitGraph(body);
                SimpleLocalDefs localDefs =
                    new SimpleLocalDefs(unitGraph);
                SimpleLocalUses localUses =
                    new SimpleLocalUses(unitGraph, localDefs);
                //  TokenTypeAnalysis typeAnalysis =
                //    new TokenTypeAnalysis(method, unitGraph);

                if (debug) System.out.println("method = " + method);

                Hierarchy hierarchy = Scene.v().getActiveHierarchy();

                // First inline all the methods that execute on Tokens.
                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    if (debug) System.out.println("unit = " + unit);
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while (boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();

                        doneSomething |= _inlineTypeMethodsIn(
                                method, body, unit, box,
                                localDefs, localUses,
                                depth, unsafeLocalSet, debug);
                    }
                }
            }
        }
    }

    public boolean _inlineTypeMethodsIn(
            SootMethod method, JimpleBody body, Unit unit, ValueBox box,
            SimpleLocalDefs localDefs, SimpleLocalUses localUses,
            int depth, Set unsafeLocalSet, boolean debug) {
        boolean doneSomething = false;
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        Value value = box.getValue();
        if (value instanceof VirtualInvokeExpr ||
                value instanceof InterfaceInvokeExpr) {
            InstanceInvokeExpr r = (InstanceInvokeExpr)value;

            Type baseType = r.getBase().getType();

            // If we are invoking a method on a Token or Type, and
            // the token is not unsafe.
            if (baseType instanceof RefType &&
                    !unsafeLocalSet.contains(r.getBase())) {
                RefType type = (RefType)baseType;

                boolean isInlineableTypeMethod =
                    SootUtilities.derivesFrom(type.getSootClass(),
                            PtolemyUtilities.typeClass) &&
                    (r.getMethod().getName().equals("convert") ||
                            r.getMethod().getName().equals("equals") ||
                            r.getMethod().getName().equals("getTokenClass") ||
                            r.getMethod().getName().equals("getElementType"));

                if (debug && isInlineableTypeMethod) {
                    System.out.println("Inlineable type method = " + r.getMethod());
                }

                if (isInlineableTypeMethod) {

                    // Then determine the method that was
                    // actually invoked.
                    List methodList =
                        //      invokeGraph.getTargetsOf((Stmt)unit);
                        hierarchy.resolveAbstractDispatch(
                                type.getSootClass(),
                                r.getMethod());

                    // If there was only one possible method...
                    if (methodList.size() == 1) {
                        // Then inline its code
                        SootMethod inlinee = (SootMethod)methodList.get(0);
                        if (inlinee.getName().equals("getClass")) {
                            // FIXME: do something better here.
                            SootMethod newGetClassMethod = Scene.v().getMethod(
                                    "<java.lang.Class: java.lang.Class "
                                    + "forName(java.lang.String)>");
                            box.setValue(Jimple.v().newStaticInvokeExpr(
                                    newGetClassMethod,
                                    StringConstant.v("java.lang.Object")));

                        } else if (inlinee.getName().equals("getElementType")) {
                            if (debug) System.out.println("handling getElementType: " + unit);
                            // Handle ArrayType.getElementType specially.
                            ptolemy.data.type.Type baseTokenType =
                                PtolemyUtilities.getTypeValue(method,
                                        (Local)r.getBase(), unit, localDefs, localUses);
                            //typeAnalysis.getSpecializedType((Local)r.getBase());
                            if (baseTokenType instanceof ptolemy.data.type.ArrayType) {
                                Local local = PtolemyUtilities.buildConstantTypeLocal(
                                        body, unit,
                                        ((ptolemy.data.type.ArrayType)baseTokenType).getElementType());
                                box.setValue(local);
                                doneSomething = true;
                            }
                        } else if (inlinee.getName().equals("equals")) {
                            try {
                                if (debug) System.out.println("handling equals: " + unit);
                                // Handle Type.equals
                                ptolemy.data.type.Type baseTokenType =
                                    PtolemyUtilities.getTypeValue(method,
                                            (Local)r.getBase(), unit, localDefs, localUses);
                                //typeAnalysis.getSpecializedType((Local)r.getBase());
                                ptolemy.data.type.Type argumentTokenType =
                                    PtolemyUtilities.getTypeValue(method,
                                            (Local)r.getArg(0), unit, localDefs, localUses);
                                //      typeAnalysis.getSpecializedType((Local)r.getBase());
                                //    if (baseTokenType.isInstantiable()) {
                                //                                                     continue;
                                //                                                 }

                                if (baseTokenType.equals(argumentTokenType)) {
                                    if (debug) System.out.println("replacing with true: type = "
                                            + baseTokenType);
                                    box.setValue(IntConstant.v(1));
                                    doneSomething = true;
                                } else {
                                    if (debug) System.out.println("replacing with false: type1 = "
                                            + baseTokenType + ", type2 = " + argumentTokenType);
                                    box.setValue(IntConstant.v(0));
                                    doneSomething = true;
                                }
                            } catch(Exception ex) {
                            }
                        } else {
                            SootClass declaringClass = inlinee.getDeclaringClass();
                            if(!declaringClass.isApplicationClass()) {
                                declaringClass.setLibraryClass();
                            }
                            if (!inlinee.isAbstract() &&
                                    !inlinee.isNative()) {
                                // FIXME: only inline things where we are
                                // also inlining the constructor???
                                if (debug) System.out.println("inlining " + inlinee);
                                inlinee.retrieveActiveBody();
                                // Then we know exactly what method will
                                // be called, so inline it.
                                SiteInliner.inlineSite(
                                        inlinee, (Stmt)unit, method);
                                doneSomething = true;
                            } else {
                                System.out.println("inlinee is not concrete!: " + inlinee);
                            }
                        }
                    } else {
                        if (debug) {
                            System.out.println("uninlinable method invocation = " + r);
                            for (Iterator j = methodList.iterator();
                                 j.hasNext();) {
                                System.out.println("method = " + j.next());
                            }
                        }
                    }
                } else {
                    if (debug) System.out.println("Not a type method.");
                }
            } else {
                if (debug) System.out.println("baseType = " + baseType);
            }
        }
        return doneSomething;
    }

    public void createReplacementTokenFields(SootClass entityClass,
            int depth, Set unsafeLocalSet, boolean debug) {

        boolean doneSomething = false;

        if (debug) {
            System.err.println("Creating Replacement token fields in " + entityClass + " with depth " + depth);
            System.out.println("Creating Replacement token fields in " + entityClass + " with depth " + depth);
        }

        // For every Token field of the actor, create new fields
        // that represent the fields of the token class in this actor.
        TypeSpecializerAnalysis typeAnalysis =
            new TypeSpecializerAnalysis(entityClass, unsafeLocalSet);

        for (Iterator fields = entityClass.getFields().snapshotIterator();
             fields.hasNext();) {
            SootField field = (SootField)fields.next();
            Type fieldType = typeAnalysis.getSpecializedSootType(field);

            // If the type is not a token, then skip it.
            if (!PtolemyUtilities.isConcreteTokenType(fieldType)) {
                if (debug) System.out.println("Skipping field " + field + ": Is not a Token");
                continue;
            }
            ptolemy.data.type.Type fieldTokenType =
                typeAnalysis.getSpecializedType(field);
            if (debug) System.out.println("Specialized type = " + fieldTokenType);

            // Ignore fields that aren't of the right depth.
            if (PtolemyUtilities.getTypeDepth(fieldTokenType) != depth) {
                if (debug) System.out.println("Skipping field " + field + ": Wrong depth");
                continue;
            }

            // If the type is not instantiable, then skip it.
            // Hack: should this be an exception?
            //     if (!fieldTokenType.isInstantiable()) {
            //                 if (debug) System.out.println("Skipping field " + field + ": Not instantiable");
            //                 continue;
            //             }

            // If we've already created subfields for this
            // field, then don't do it again.
            if (entityFieldToTokenFieldToReplacementField.get(field) != null) {
                if (debug) System.out.println("Skipping field " + field + ": already done");
                continue;
            }

            RefType type = (RefType)PtolemyUtilities.getBaseTokenType(fieldType);
            SootClass fieldClass = type.getSootClass();

            if (!SootUtilities.derivesFrom(fieldClass, PtolemyUtilities.tokenClass)) {
                if (debug) System.out.println("Skipping field " + field + ": Not a token class");
                continue;
            }

            if (debug) System.out.println("Creating replacement fields for field = " + field);

            // We are going to make a modification
            doneSomething = true;

            // Create a boolean value that tells us whether or
            // not the token is null.  Initialize it to true.
            // FIXME: what about the elements of arrays?
            Type isNotNullType =
                SootUtilities.createIsomorphicType(field.getType(),
                        BooleanType.v());

            SootField isNotNullField = new SootField(
                    "_CG_" + field.getName() + "_isNotNull", isNotNullType,
                    field.getModifiers());
            entityClass.addField(isNotNullField);
            entityFieldToIsNotNullField.put(field, isNotNullField);
            // FIXME: initialize properly.
            //             body.getUnits().insertBefore(
            //                     Jimple.v().newAssignStmt(
            //                             isNotNullLocal,
            //                             IntConstant.v(1)),
            //                     body.getFirstNonIdentityStmt());


            // Hack to force the element type of an array.
            ptolemy.data.type.Type elementType = null;
            if (fieldTokenType instanceof ptolemy.data.type.ArrayType) {
                elementType = ((ptolemy.data.type.ArrayType)fieldTokenType).
                    getElementType();
            }
            Map tokenFieldToReplacementField = new HashMap();
            entityFieldToTokenFieldToReplacementField.put(field,
                    tokenFieldToReplacementField);
            for (Iterator tokenFields = _getTokenClassFields(fieldClass).iterator();
                 tokenFields.hasNext();) {
                SootField tokenField = (SootField)tokenFields.next();
                // We need a type that is the same shape as
                // the field, with the same type as the field
                // in the token.  This is complicated by the
                // fact that both may be arraytypes.
                Type replacementType =
                    SootUtilities.createIsomorphicType(field.getType(),
                            tokenField.getType());
                SootField replacementField =
                    new SootField("_CG_" + field.getName() + tokenField.getName(),
                            replacementType, field.getModifiers());
                tokenFieldToReplacementField.put(tokenField, replacementField);
                entityClass.addField(replacementField);

                // Hack for type of array type.
                if(elementType != null && 
                        tokenField.getName().equals("_value")) {
                    System.err.println("replacmentField = " + replacementField);
                    replacementField.addTag(new TypeTag(elementType));
                }
            }
        }
    }

    public void replaceTokenFields(SootClass entityClass, int depth, Set unsafeLocalSet, boolean debug) {

        boolean doneSomething = false;
        if (debug) {
            System.err.println("Replacing token fields in " + entityClass + " with depth " + depth);
            System.out.println("Replacing token fields in " + entityClass + " with depth " + depth);
        }

        TypeSpecializerAnalysis typeAnalysis =
            new TypeSpecializerAnalysis(entityClass, unsafeLocalSet);

        for (Iterator methods = entityClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();

            // Check to see if the method is safe to modify with:
            // It has no arguments or return values which are 
            // tokens.
            if(_methodWillBeInlined(method)) {
                continue;
            }

            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            //  UnitGraph graph = new CompleteUnitGraph(body);
            //  MustAliasAnalysis aliasAnalysis = new MustAliasAnalysis(graph);

            if (debug) System.out.println("creating replacement locals in method = " + method);

            // A map from a local variable that references a
            // token and a field of that token class to the
            // local variable that will replace a
            // fieldReference to that field based on the
            // local.

            for (Iterator locals = body.getLocals().snapshotIterator();
                 locals.hasNext();) {
                Local local = (Local)locals.next();
                Type localType = typeAnalysis.getSpecializedSootType(local);

                // If the type is not a token, then skip it.
                if (!PtolemyUtilities.isConcreteTokenType(localType) ||
                        unsafeLocalSet.contains(local)) {
                    continue;
                }
                ptolemy.data.type.Type localTokenType =
                    typeAnalysis.getSpecializedType(local);

                // Ignore fields that aren't of the right depth.
                if (PtolemyUtilities.getTypeDepth(localTokenType) != depth) {
                    continue;
                }

                // If the type is not instantiable, then skip it.
                //    if (!localTokenType.isInstantiable()) {
                //    continue;
                // }

                // If we've already created subfields for this
                // field, then don't do it again.
                if (localToFieldToLocal.get(local) != null) {
                    continue;
                }

                RefType type = PtolemyUtilities.getBaseTokenType(localType);
                SootClass localClass = type.getSootClass();

                if (!SootUtilities.derivesFrom(localClass,
                        PtolemyUtilities.tokenClass)) {
                    continue;
                }

                if (debug) System.out.println("Creating replacement fields for local = " + local);
                if (debug) System.out.println("localClass = " + localClass);

                // We are going to make a modification
                doneSomething = true;

                Type isNotNullType =
                    SootUtilities.createIsomorphicType(localType,
                            BooleanType.v());
                // Create a boolean value that tells us whether or
                // not the token is null.  Initialize it to true.
                Local isNotNullLocal = Jimple.v().newLocal(
                        local.getName() + "_isNotNull", isNotNullType);
                body.getLocals().add(isNotNullLocal);
                localToIsNotNullLocal.put(local, isNotNullLocal);
                // Note: default initialization is to false..
                //                 body.getUnits().insertBefore(
                //                         Jimple.v().newAssignStmt(
                //                                 isNotNullLocal,
                //                                 IntConstant.v(1)),
                //                         body.getFirstNonIdentityStmt());

                Map tokenFieldToReplacementLocal = new HashMap();
                localToFieldToLocal.put(local,
                        tokenFieldToReplacementLocal);
                for (Iterator tokenFields = _getTokenClassFields(localClass).iterator();
                     tokenFields.hasNext();) {
                    SootField tokenField = (SootField)tokenFields.next();
                    if (debug) System.out.println("tokenField = " + tokenField);
                    Type replacementType = SootUtilities.createIsomorphicType(localType,
                            tokenField.getType());
                    Local replacementLocal = Jimple.v().newLocal(
                            local.getName() + "_" + tokenField.getName(),
                            replacementType);
                    body.getLocals().add(replacementLocal);
                    tokenFieldToReplacementLocal.put(tokenField, replacementLocal);
                }
            }

            // Go back again and replace references to fields
            // in the token with references to local
            // variables.
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Unit unit = (Unit)units.next();
                if (debug) System.out.println("unit = " + unit);
                if (unit instanceof InvokeStmt) {
                    // Handle java.lang.arraycopy
                    InvokeExpr r = (InvokeExpr)((InvokeStmt)unit).getInvokeExpr();
                    if (r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                        if (debug) System.out.println("handling as array copy");
                        Local toLocal = (Local)r.getArg(0);
                        Local fromLocal = (Local)r.getArg(2);
                        Map toFieldToReplacementLocal =
                            (Map) localToFieldToLocal.get(toLocal);
                        Map fromFieldToReplacementLocal =
                            (Map) localToFieldToLocal.get(fromLocal);
                        if (toFieldToReplacementLocal != null &&
                                fromFieldToReplacementLocal != null) {
                            if(debug) System.out.println("toFieldToReplacementLocal = " + toFieldToReplacementLocal);
                            if(debug) System.out.println("fromFieldToReplacementLocal = " + fromFieldToReplacementLocal);
                            {
                                List argumentList = new LinkedList();
                                argumentList.add((Local)localToIsNotNullLocal.get(toLocal));
                                argumentList.add(r.getArg(1));
                                argumentList.add((Local)localToIsNotNullLocal.get(fromLocal));
                                argumentList.add(r.getArg(3));
                                argumentList.add(r.getArg(4));
                                
                                body.getUnits().insertBefore(
                                        Jimple.v().newInvokeStmt(
                                                Jimple.v().newStaticInvokeExpr(
                                                        PtolemyUtilities.arraycopyMethod,
                                                        argumentList)),
                                        unit);
                            }
                            
                            for (Iterator tokenFields = toFieldToReplacementLocal.keySet().iterator();
                                 tokenFields.hasNext();) {
                                SootField tokenField = (SootField)tokenFields.next();

                                Local toReplacementLocal = (Local)
                                    toFieldToReplacementLocal.get(tokenField);
                                Local fromReplacementLocal = (Local)
                                    fromFieldToReplacementLocal.get(tokenField);
                                List argumentList = new LinkedList();
                                argumentList.add(toReplacementLocal);
                                argumentList.add(r.getArg(1));
                                argumentList.add(fromReplacementLocal);
                                argumentList.add(r.getArg(3));
                                argumentList.add(r.getArg(4));

                                body.getUnits().insertBefore(
                                        Jimple.v().newInvokeStmt(
                                                Jimple.v().newStaticInvokeExpr(
                                                        PtolemyUtilities.arraycopyMethod,
                                                        argumentList)),
                                        unit);
                            }
                            body.getUnits().remove(unit);
                            doneSomething = true;
                        }
                    }
                } else if (unit instanceof AssignStmt) {
                    AssignStmt stmt = (AssignStmt)unit;
                    Type assignmentType = stmt.getLeftOp().getType();
                    if (stmt.getLeftOp() instanceof Local &&
                            stmt.getRightOp() instanceof LengthExpr) {
                        if (debug) System.out.println("handling as length expr");
                        LengthExpr lengthExpr = (LengthExpr)stmt.getRightOp();
                        Local baseLocal = (Local)lengthExpr.getOp();
                        if (debug) System.out.println("operating on " + baseLocal);

                        Map fieldToReplacementArrayLocal =
                            (Map) localToFieldToLocal.get(baseLocal);

                        if (fieldToReplacementArrayLocal != null) {
                            doneSomething = true;
                            // Get the length of a random one of the replacement fields.
                            List replacementList = new ArrayList(
                                    fieldToReplacementArrayLocal.keySet());
                            Collections.sort(replacementList,
                                    new Comparator() {
                                            public int compare(Object o1, Object o2) {
                                                SootField f1 = (SootField)o1;
                                                SootField f2 = (SootField)o2;
                                                return f1.getName().compareTo(f2.getName());
                                            }
                                        });
                            SootField field = (SootField)
                                replacementList.get(replacementList.size() - 1);
                            if (debug) System.out.println("replace with  " +
                                    fieldToReplacementArrayLocal.get(field));
                            lengthExpr.setOp((Local)
                                    fieldToReplacementArrayLocal.get(field));
                            if (debug) System.out.println("unit now = " + unit);
                            //    body.getUnits().remove(unit);
                        }
                    } else if (stmt.getLeftOp() instanceof InstanceFieldRef) {
                        // Replace references to fields of tokens.
                        // FIXME: assign to all aliases as well.
                        if (debug) System.out.println("is assignment to Instance FieldRef");
                        InstanceFieldRef r = (InstanceFieldRef)stmt.getLeftOp();
                        SootField field = r.getField();
                        if (r.getBase().getType() instanceof RefType) {
                            RefType type = (RefType)r.getBase().getType();
                            //System.out.println("BaseType = " + type);
                            if (SootUtilities.derivesFrom(type.getSootClass(),
                                    PtolemyUtilities.tokenClass)) {
                                if (debug) System.out.println("handling " +
                                        unit + " token operation");

                                // We have a reference to a field of a token class.
                                Local baseLocal = (Local)r.getBase();
                                Local instanceLocal = _getInstanceLocal(body, baseLocal,
                                        field, localToFieldToLocal, debug);
                                if (instanceLocal != null) {
                                    stmt.getLeftOpBox().setValue(instanceLocal);
                                    doneSomething = true;
                                }
                            }
                        }
                    } else if (stmt.getRightOp() instanceof InstanceFieldRef) {
                        // Replace references to fields of tokens.
                        if (debug) System.out.println("is assignment from Instance FieldRef");
                        InstanceFieldRef r = (InstanceFieldRef)stmt.getRightOp();
                        SootField field = r.getField();
                        if (r.getBase().getType() instanceof RefType) {
                            RefType type = (RefType)r.getBase().getType();
                            //System.out.println("BaseType = " + type);
                            if (SootUtilities.derivesFrom(type.getSootClass(),
                                    PtolemyUtilities.tokenClass)) {
                                if (debug) System.out.println("handling " +
                                        unit + " token operation");

                                // We have a reference to a field of a token class.
                                Local baseLocal = (Local)r.getBase();
                                Local instanceLocal = _getInstanceLocal(body, baseLocal,
                                        field, localToFieldToLocal, debug);
                                if (debug) System.out.println("instanceLocal = " + instanceLocal);
                                if (instanceLocal != null) {
                                    stmt.getRightOpBox().setValue(instanceLocal);
                                    doneSomething = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (debug) System.out.println("Specializing types for " + entityClass);

        // Specialize the token types.  Any field we created above
        // should now have its correct concrete type.
        // TypeSpecializer.specializeTypes(debug, entityClass, unsafeLocalSet);



        // Now go through the methods again and handle all token assignments,
        // replacing them with assignments on the native replacements.
        for (Iterator methods = entityClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();

            if (debug) System.out.println("Replacing token assignments in method " + method);

            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            //   InstanceEqualityEliminator.removeInstanceEqualities(body, null, true);

            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Unit unit = (Unit)units.next();
                if (debug) System.out.println("unit = " + unit);
                if (unit instanceof AssignStmt) {
                    AssignStmt stmt = (AssignStmt)unit;
                    Type assignmentType = stmt.getLeftOp().getType();
                    if (PtolemyUtilities.isTokenType(assignmentType)) {
                        if (stmt.getLeftOp() instanceof Local &&
                                (stmt.getRightOp() instanceof Local ||
                                        stmt.getRightOp() instanceof Constant)) {
                            if (debug) System.out.println("handling as local-immediate assign");
                            doneSomething |= _handleImmediateAssignment(body, stmt,
                                    localToFieldToLocal, localToIsNotNullLocal,
                                    stmt.getLeftOp(), stmt.getRightOp(), debug);

                        } else if (stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof CastExpr) {
                            if (debug) System.out.println("handling as local cast");
                            Value rightLocal = ((CastExpr)stmt.getRightOp()).getOp();

                            doneSomething |= _handleImmediateAssignment(body, stmt,
                                    localToFieldToLocal, localToIsNotNullLocal,
                                    stmt.getLeftOp(), rightLocal, debug);

                        } else if (stmt.getLeftOp() instanceof FieldRef &&
                                stmt.getRightOp() instanceof Local) {
                            if (debug) System.out.println("handling as assignment to Field");
                            FieldRef oldFieldRef = (FieldRef)stmt.getLeftOp();
                            SootField field = oldFieldRef.getField();
                            Map fieldToReplacementField =
                                (Map) entityFieldToTokenFieldToReplacementField.get(field);
                            Map fieldToReplacementLocal =
                                (Map) localToFieldToLocal.get(stmt.getRightOp());

                            //        System.out.println("fieldToReplacementField = " + fieldToReplacementField);
                            //                             System.out.println("fieldToReplacementLocal = " + fieldToReplacementLocal);
                            if (fieldToReplacementLocal != null &&
                                    fieldToReplacementField != null) {
                                doneSomething = true;
                                // Replace references to fields with token types.
                                {
                                    SootField replacementField = (SootField)entityFieldToIsNotNullField.get(field);
                                    //System.out.println("replacementField = " + replacementField);
                                    FieldRef isNotNullFieldRef;
                                    if (oldFieldRef instanceof InstanceFieldRef) {
                                        isNotNullFieldRef = Jimple.v().newInstanceFieldRef(
                                                ((InstanceFieldRef)oldFieldRef).getBase(),
                                                replacementField);
                                    } else {
                                        isNotNullFieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    isNotNullFieldRef,
                                                    (Local)localToIsNotNullLocal.get(stmt.getRightOp())),
                                            unit);
                                }
                                if (debug) System.out.println("local = " + stmt.getLeftOp());
                                for (Iterator tokenFields = fieldToReplacementField.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    if (debug) System.out.println("tokenField = " + tokenField);

                                    SootField replacementField = (SootField)
                                        fieldToReplacementField.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    if (debug) System.out.println("replacementLocal = " + replacementLocal);
                                    FieldRef fieldRef;
                                    if (stmt.getLeftOp() instanceof InstanceFieldRef) {
                                        Local base = (Local)((InstanceFieldRef)stmt.getLeftOp()).getBase();
                                        fieldRef = Jimple.v().newInstanceFieldRef(base,
                                                replacementField);
                                    } else {
                                        fieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }

                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    fieldRef,
                                                    replacementLocal),
                                            unit);
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());

                                // body.getUnits().remove(unit);
                            }
                        } else if (stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof FieldRef) {
                            if (debug) System.out.println("handling as assignment from Field");
                            FieldRef oldFieldRef = (FieldRef)stmt.getRightOp();
                            Map fieldToReplacementLocal =
                                (Map) localToFieldToLocal.get(stmt.getLeftOp());
                            SootField field = oldFieldRef.getField();
                            Map fieldToReplacementField =
                                (Map) entityFieldToTokenFieldToReplacementField.get(field);

                            // There are some fields that
                            // represent singleton tokens.
                            // Deal with them specially.
                            boolean isBooleanTokenTrueSingleton = false;
                            boolean isBooleanTokenFalseSingleton = false;
                            if (field.getSignature().equals(
                                    "<ptolemy.data.BooleanToken: ptolemy.data.BooleanToken TRUE>")) {
                                isBooleanTokenTrueSingleton = true;
                            } else if (field.getSignature().equals(
                                    "<ptolemy.data.BooleanToken: ptolemy.data.BooleanToken FALSE>")) {
                                isBooleanTokenFalseSingleton = true;
                            }
                            if ((isBooleanTokenFalseSingleton || isBooleanTokenTrueSingleton) &&
                                    fieldToReplacementLocal != null) {
                                doneSomething = true;
                                // Replace references to fields with token types.
                                // The special fields should never be null
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNotNullLocal.get(stmt.getLeftOp()),
                                                IntConstant.v(1)),
                                        unit);
                                if (debug) System.out.println("local = " + stmt.getLeftOp());
                                for (Iterator localFields = fieldToReplacementLocal.keySet().iterator();
                                     localFields.hasNext();) {
                                    SootField localField = (SootField)localFields.next();
                                    if (!localField.getName().equals("_value")) {
                                        throw new RuntimeException("Unknown Field in BooleanToken: " +
                                                localField.getName());
                                    }
                                    if (debug) System.out.println("localField = " + localField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(localField);
                                    if (isBooleanTokenTrueSingleton) {
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        replacementLocal,
                                                        IntConstant.v(1)),
                                                unit);
                                    } else {
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        replacementLocal,
                                                        IntConstant.v(0)),
                                                unit);
                                    }
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());

                                //body.getUnits().remove(unit);
                            } else if (fieldToReplacementLocal != null &&
                                    fieldToReplacementField != null) {
                                doneSomething = true;
                                // Replace references to fields with token types.
                                // FIXME properly handle isNotNull field?
                                {
                                    SootField replacementField = (SootField)entityFieldToIsNotNullField.get(field);
                                    //   System.out.println("replacementField = " + replacementField);
                                    FieldRef isNotNullFieldRef;
                                    if (oldFieldRef instanceof InstanceFieldRef) {
                                        isNotNullFieldRef = Jimple.v().newInstanceFieldRef(
                                                ((InstanceFieldRef)oldFieldRef).getBase(),
                                                replacementField);
                                    } else {
                                        isNotNullFieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    (Local)localToIsNotNullLocal.get(stmt.getLeftOp()),
                                                    isNotNullFieldRef),
                                            unit);
                                }
                                if (debug) System.out.println("local = " + stmt.getLeftOp());
                                for (Iterator tokenFields = fieldToReplacementField.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    if (debug) System.out.println("tokenField = " + tokenField);
                                    SootField replacementField = (SootField)
                                        fieldToReplacementField.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    FieldRef fieldRef;
                                    if (stmt.getRightOp() instanceof InstanceFieldRef) {
                                        Local base = (Local)((InstanceFieldRef)stmt.getRightOp()).getBase();
                                        fieldRef = Jimple.v().newInstanceFieldRef(base,
                                                replacementField);
                                    } else {
                                        fieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }

                                    if (debug) System.out.println("replacementLocal = " + replacementLocal);

                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal,
                                                    fieldRef),
                                            unit);
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());
                                //      body.getUnits().remove(unit);
                            }
                        } else if (stmt.getLeftOp() instanceof ArrayRef &&
                                stmt.getRightOp() instanceof Local) {
                            if (debug) System.out.println("handling as assignment to Array");
                            ArrayRef arrayRef = (ArrayRef)stmt.getLeftOp();
                            Local baseLocal = (Local) arrayRef.getBase();
                            Map fieldToReplacementArrayLocal =
                                (Map) localToFieldToLocal.get(baseLocal);
                            Map fieldToReplacementLocal =
                                (Map) localToFieldToLocal.get(stmt.getRightOp());

                            if(debug) System.out.println("fieldToReplacementArrayLocal = " + fieldToReplacementArrayLocal);
                            if(debug) System.out.println("fieldToReplacementLocal = " + fieldToReplacementLocal);
                            if (fieldToReplacementLocal != null &&
                                    fieldToReplacementArrayLocal != null) {
                                doneSomething = true;
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                Jimple.v().newArrayRef(
                                                        (Local)localToIsNotNullLocal.get(baseLocal),
                                                        arrayRef.getIndex()),
                                                (Local)localToIsNotNullLocal.get(stmt.getRightOp())),
                                        unit);
                                if (debug) System.out.println("local = " + stmt.getLeftOp());
                                for (Iterator tokenFields = fieldToReplacementLocal.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    if (debug)  System.out.println("tokenField = " + tokenField);
                                    Local replacementArrayLocal = (Local)
                                        fieldToReplacementArrayLocal.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);

                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    Jimple.v().newArrayRef(
                                                            replacementArrayLocal,
                                                            arrayRef.getIndex()),
                                                    replacementLocal),
                                            unit);
                                }
                                // Have to remove here, because otherwise we'll try to
                                // index into a null array.
                                //stmt.getRightOpBox().setValue(NullConstant.v());
                                body.getUnits().remove(unit);
                            }
                        } else if (stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof ArrayRef) {
                            if (debug)  System.out.println("handling as assignment from Array");
                            ArrayRef arrayRef = (ArrayRef)stmt.getRightOp();
                            Map fieldToReplacementLocal =
                                (Map) localToFieldToLocal.get(stmt.getLeftOp());
                            Local baseLocal = (Local)arrayRef.getBase();
                            Map fieldToReplacementArrayLocal =
                                (Map) localToFieldToLocal.get(baseLocal);

                            if (fieldToReplacementLocal != null &&
                                    fieldToReplacementArrayLocal != null) {
                                doneSomething = true;

                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNotNullLocal.get(stmt.getLeftOp()),
                                                Jimple.v().newArrayRef(
                                                        (Local)localToIsNotNullLocal.get(baseLocal),
                                                        arrayRef.getIndex())),
                                        unit);
                                if (debug) System.out.println("local = " + stmt.getLeftOp());
                                for (Iterator tokenFields = fieldToReplacementLocal.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    if (debug)  System.out.println("tokenField = " + tokenField);
                                    Local replacementArrayLocal = (Local)
                                        fieldToReplacementArrayLocal.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    if (debug) System.out.println("replacementLocal = " + replacementLocal);
                                    if (debug) System.out.println("replacementArrayLocal = " + replacementArrayLocal);

                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal,
                                                    Jimple.v().newArrayRef(
                                                            replacementArrayLocal,
                                                            arrayRef.getIndex())),
                                            unit);
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());
                                //body.getUnits().remove(unit);
                            }
                        } else if (stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof NewArrayExpr) {
                            if (debug) System.out.println("handling as new array object");
                            NewArrayExpr newExpr = (NewArrayExpr)stmt.getRightOp();
                            // We have an assignment to a local from a new array.
                            Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                            if (map != null) {
                                doneSomething = true;

                                Type isNotNullType =
                                    SootUtilities.createIsomorphicType(newExpr.getBaseType(),
                                            BooleanType.v());
                                Local isNotNullLocal = (Local)
                                    localToIsNotNullLocal.get(stmt.getLeftOp());
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                isNotNullLocal,
                                                Jimple.v().newNewArrayExpr(
                                                        isNotNullType,
                                                        newExpr.getSize())),
                                        unit);
                                for (Iterator tokenFields = map.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    if (debug) System.out.println("tokenField = " + tokenField);
                                    Local replacementLocal = (Local)
                                        map.get(tokenField);
                                    
                                    Type replacementType =
                                        SootUtilities.createIsomorphicType(newExpr.getBaseType(),
                                                tokenField.getType());

                                    // Initialize fields?
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal,
                                                    Jimple.v().newNewArrayExpr(
                                                            replacementType,
                                                            newExpr.getSize())),
                                            unit);
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());
                                //body.getUnits().remove(unit);
                            }

                        } else if (stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof NewExpr) {
                            if (debug) System.out.println("handling as new object");
                            NewExpr newExpr = (NewExpr)stmt.getRightOp();
                            // We have an assignment from one local token to another.
                            Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                            if (map != null) {
                                doneSomething = true;

                                Local isNotNullLocal = (Local)
                                    localToIsNotNullLocal.get(stmt.getLeftOp());
                                if (debug) System.out.println("Stmt = " + stmt);

                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                isNotNullLocal,
                                                IntConstant.v(1)),
                                        unit);
                                for (Iterator tokenFields = map.keySet().iterator();
                                     tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    Local replacementLocal = (Local)
                                        map.get(tokenField);
                                    // Initialize fields?
                                    if (debug) System.out.println("tokenField = " + tokenField);
                                    // FIXME: ??
                                    Value replacementValue = _getNullValueForType(replacementLocal.getType());
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal,
                                                    replacementValue),
                                            unit);
                                }
                                stmt.getRightOpBox().setValue(NullConstant.v());
                                // body.getUnits().remove(unit);
                            }

                        }
                        //  else if (stmt.getLeftOp() instanceof Local &&
                        //                                        stmt.getRightOp() instanceof InvokeExpr) {
                        //                                  System.out.println("handling as method call.");
                        //                                  // We have an assignment from one local token to another.
                        //                                  Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                        //                                  if (map != null) {
                        //                                      Local isNotNullLocal = (Local)
                        //                                          localToIsNotNullLocal.get(stmt.getLeftOp());
                        //                                      body.getUnits().insertAfter(
                        //                                              Jimple.v().newAssignStmt(
                        //                                                      isNotNullLocal,
                        //                                                      IntConstant.v(0)),
                        //                                              unit);
                        //                                      for (Iterator tokenFields = map.keySet().iterator();
                        //                                          tokenFields.hasNext();) {
                        //                                          SootField tokenField = (SootField)tokenFields.next();
                        //                                          Local replacementLocal = (Local)
                        //                                              map.get(tokenField);
                        //                                          // Initialize fields?
                        //                                          body.getUnits().insertAfter(
                        //                                                  Jimple.v().newAssignStmt(
                        //                                                          replacementLocal,
                        //                                                          ),
                        //                                                  unit);
                        //                                      }
                        //                                  }
                        //                                  }
                    }
                }
                // FIXME!  This does not handle null values in fields well...
                for (Iterator boxes = unit.getUseAndDefBoxes().iterator();
                     boxes.hasNext();) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if (value instanceof BinopExpr) {
                        BinopExpr expr = (BinopExpr) value;
                        boolean op1IsToken = PtolemyUtilities.isTokenType(expr.getOp1().getType());
                        boolean op2IsToken = PtolemyUtilities.isTokenType(expr.getOp2().getType());
                        if (op1IsToken && op2IsToken) {
                            throw new RuntimeException("Unable to handle expression" +
                                    " of two token types: " + unit);
                        } else if (op1IsToken && expr.getOp2().getType().equals(NullType.v())) {
                            doneSomething = true;

                            Local isNotNullLocal = (Local)
                                localToIsNotNullLocal.get(expr.getOp1());
                            if (isNotNullLocal != null) {
                                if (debug) System.out.println("replacing binary expression " + expr);
                                Value nullValue;
                                if (isNotNullLocal.getType().equals(BooleanType.v())) {
                                    // If the isNotNullLocal is for a regular Object.
                                    nullValue = IntConstant.v(0);
                                } else {
                                    // If the isNotNullLocal is for an Array Object.
                                    nullValue = NullConstant.v();
                                }

                                if (expr instanceof EqExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNotNullLocal,
                                            nullValue));
                                } else if (expr instanceof NeExpr) {
                                    box.setValue(Jimple.v().newNeExpr(
                                            isNotNullLocal,
                                            nullValue));
                                }
                            }
                        } else if (op2IsToken && expr.getOp1().getType().equals(NullType.v())) {
                            doneSomething = true;

                            Local isNotNullLocal = (Local)
                                localToIsNotNullLocal.get(expr.getOp2());
                            if (isNotNullLocal != null) {
                                Value nullValue;
                                if (isNotNullLocal.getType().equals(BooleanType.v())) {
                                    // If the isNotNullLocal is for a regular Object.
                                    nullValue = IntConstant.v(0);
                                } else {
                                    // If the isNotNullLocal is for an Array Object.
                                    nullValue = NullConstant.v();
                                }

                                if (expr instanceof EqExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNotNullLocal,
                                            nullValue));
                                } else if (expr instanceof NeExpr) {
                                    box.setValue(Jimple.v().newNeExpr(
                                            isNotNullLocal,
                                            nullValue));
                                }
                            }
                        }
                    }
                }
                //       if (debug) System.out.println("New unit = " + unit);
            }
        }
    }

    // Compute all the locals with value type that depend on
    // the local defined at the given stmt.
    private static Set _computeTokenLocalsDefinedFrom(
            SimpleLocalUses localUses, Stmt stmt) {
        Set set = new HashSet();
        List useList = localUses.getUsesOf(stmt);
        for (Iterator uses = useList.iterator();
             uses.hasNext();) {
            UnitValueBoxPair pair = (UnitValueBoxPair)uses.next();
            Stmt defStmt = (Stmt)pair.getUnit();
            if (defStmt instanceof DefinitionStmt) {
                Value value = ((DefinitionStmt)defStmt).getLeftOp();
                if (PtolemyUtilities.isTokenType(value.getType())) {
                    set.add(value);
                    set.addAll(_computeTokenLocalsDefinedFrom(
                            localUses, defStmt));
                }
            }
        }
        return set;
    }

    private static Local _getInstanceLocal(Body body, Local baseLocal,
            SootField field, Map localToFieldToLocal, boolean debug) {
        // Get the map for that local from a field of the local's
        // class to the local variable that replaces it.
        Map fieldToLocal = (Map)localToFieldToLocal.get(baseLocal);
        if (fieldToLocal == null) {
            //             if (debug) System.out.println("creating new fieldToLocal for " + baseLocal);
            //             // If we have not seen this local get, then
            //             // create a new map.
            //             fieldToLocal = new HashMap();
            //             localToFieldToLocal.put(baseLocal, fieldToLocal);
            return null;
        }
        Local instanceLocal = (Local)fieldToLocal.get(field);
        if (instanceLocal == null) {
            //             if (debug) System.out.println("creating new instanceLocal for " + baseLocal);
            //             // If we have not referenced this particular field
            //             // then create a new local.
            //             instanceLocal = Jimple.v().newLocal(
            //                     baseLocal.getName() + "_" + field.getName(),
            //                     field.getType());
            //             body.getLocals().add(instanceLocal);
            //             fieldToLocal.put(field, instanceLocal);
            return null;
        }
        return instanceLocal;
    }

    /*
      public static Value _getSubFieldValue(SootClass entityClass, Body body,
      Value baseValue, SootField tokenField, Map localToFieldToLocal,
      boolean debug) {
      Value returnValue;
      if (baseValue instanceof Local) {
      returnValue = _getInstanceLocal(body, (Local)baseValue,
      tokenField, localToFieldToLocal, debug);
      } else if (baseValue instanceof FieldRef) {
      SootField field = ((FieldRef)baseValue).getField();
      SootField replacementField =
      entityClass.getFieldByName("_CG_" + field.getName()
      + tokenField.getName());
      if (baseValue instanceof InstanceFieldRef) {
      returnValue = Jimple.v().newInstanceFieldRef(
      ((InstanceFieldRef)baseValue).getBase(), replacementField);
      } else {
      returnValue = Jimple.v().newStaticFieldRef(
      replacementField);
      }
      } else {
      throw new RuntimeException("Unknown value type for subfield: " + baseValue);
      }
      return returnValue;
      }
    */

    public static Value _getNullValueForType(Type type) {
        if (type instanceof ArrayType) {
            return NullConstant.v();
        } else if (type instanceof RefType) {
            return NullConstant.v();
        } else if (type instanceof DoubleType) {
            return DoubleConstant.v(77.0);
        } else if (type instanceof FloatType) {
            return FloatConstant.v((float)7.0);
        } else if (type instanceof LongType) {
            return LongConstant.v(77);
        } else if (type instanceof IntType ||
                type instanceof ShortType ||
                type instanceof ByteType) {
            return IntConstant.v(7);
        } else if (type instanceof BooleanType) {
            return IntConstant.v(0);
        } else {
            throw new RuntimeException("Unknown type = " + type);
        }
    }

    public static List _getTokenClassFields(SootClass tokenClass) {
        List list;
        if (tokenClass.equals(PtolemyUtilities.tokenClass)) {
            list = new LinkedList();
        } else {
            list =
                _getTokenClassFields(tokenClass.getSuperclass());
        }
        for (Iterator fields = tokenClass.getFields().iterator();
             fields.hasNext();) {
            SootField field = (SootField)fields.next();
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                list.add(field);
            }
        }
        return list;
    }

    public static boolean _handleImmediateAssignment(
            JimpleBody body, AssignStmt stmt,
            Map localToFieldToLocal, Map localToIsNotNullLocal,
            Value leftValue, Value rightValue, boolean debug) {
        boolean doneSomething = false;

        if (debug) System.out.println("leftValue = " + leftValue);
        if (debug) System.out.println("rightValue = " + rightValue);

        Map fieldToReplacementLeftLocal =
            (Map)localToFieldToLocal.get(leftValue);
        Map fieldToReplacementRightLocal =
            (Map)localToFieldToLocal.get(rightValue);

        if (debug) System.out.println("leftValue isValid = " + (fieldToReplacementLeftLocal != null));
        if (debug) System.out.println("rightValue isValid = " + (fieldToReplacementRightLocal != null));

        if (rightValue instanceof NullConstant) {
            if (fieldToReplacementLeftLocal != null) {
                doneSomething = true;
                Local replacementIsNotNullLocal = (Local)localToIsNotNullLocal.get(leftValue);
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                replacementIsNotNullLocal,
                                _getNullValueForType(replacementIsNotNullLocal.getType())),
                        stmt);
                for (Iterator tokenFields = fieldToReplacementLeftLocal.keySet().iterator();
                     tokenFields.hasNext();) {
                    SootField tokenField = (SootField)tokenFields.next();
                    if (debug) System.out.println("tokenField = " + tokenField);
                    Local replacementLocal = (Local)
                        fieldToReplacementLeftLocal.get(tokenField);
                    // FIXME: ??
                    Value replacementValue = _getNullValueForType(replacementLocal.getType());
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    replacementLocal,
                                    replacementValue),
                            stmt);
                }
                //body.getUnits().remove(stmt);
            }
        } else {
            // We have an assignment from one local token to another.
            if (fieldToReplacementLeftLocal != null &&
                    fieldToReplacementRightLocal != null) {
                doneSomething = true;
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                (Local)localToIsNotNullLocal.get(leftValue),
                                (Local)localToIsNotNullLocal.get(rightValue)),
                        stmt);
                if (debug) System.out.println("local = " + leftValue);
                for (Iterator tokenFields = fieldToReplacementLeftLocal.keySet().iterator();
                     tokenFields.hasNext();) {
                    SootField tokenField = (SootField)tokenFields.next();
                    if (debug) System.out.println("tokenField = " + tokenField);
                    Local replacementLeftLocal = (Local)
                        fieldToReplacementLeftLocal.get(tokenField);
                    Local replacementRightLocal = (Local)
                        fieldToReplacementRightLocal.get(tokenField);
                    if (debug) System.out.println("replacementLeftLocal = " + replacementLeftLocal);
                    if (debug) System.out.println("replacementRightLocal = " + replacementRightLocal);
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    replacementLeftLocal,
                                    replacementRightLocal),
                            stmt);
                }
                stmt.getRightOpBox().setValue(NullConstant.v());
                body.getUnits().remove(stmt);
            }

        }
        return doneSomething;
    }

    // Return true if the method is one we will inline because it has
    // token arguments.
    private static boolean _methodWillBeInlined(
            SootMethod method) {
        boolean isInlineableTokenMethod = _isInlineableTokenType(
                method.getReturnType());
        
        for (Iterator args = method.getParameterTypes().iterator();
             args.hasNext() && !isInlineableTokenMethod;) {
            Type argType = (Type)args.next();
            isInlineableTokenMethod = _isInlineableTokenType(argType);
        }
        
        return isInlineableTokenMethod;
    }
    
    // Return true if the type is one we will inline because it is a
    // token type.
    private static boolean _isInlineableTokenType(Type type) {
        if (type instanceof RefType) {
            RefType refType = (RefType)type;
            
            if (SootUtilities.derivesFrom(refType.getSootClass(),
                        PtolemyUtilities.tokenClass)) {
                return true;
            }
        }
        return false;
    }
    // Return true if the givne local is a token, and its specialized type
    // according to the given analysis is a type with the given depth.
    private static boolean _isLocalTokenTypeWithDepth(
            Local local, TypeSpecializerAnalysis typeAnalysis,
            Set unsafeLocalSet, int depth, boolean debug) {
        Type baseType = local.getType();

        // If we are invoking a method on a Token
        // or Type, and the token is not unsafe.
        if (_isInlineableTokenType(baseType) &&
                !unsafeLocalSet.contains(local)) {
            
            // If it is a token, then check to
            // make sure that it has the
            // appropriate type
            if (PtolemyUtilities.getTypeDepth(
                    typeAnalysis.getSpecializedType(local)) != depth) {
                if (debug) System.out.println("skipping, type depth = " +
                        PtolemyUtilities.getTypeDepth(
                                typeAnalysis.getSpecializedType(local)) +
                        ", but only inlining depth " + depth);
                return false;
                //  continue;
            }
            return true;
        }
        return false;
    }

    // Return true if the given object contains a typeable object with
    // a type that is not supported by token unboxing.
    private boolean _hasBadTypes(NamedObj object) 
            throws IllegalActionException {
        if(object instanceof Attribute &&
                ModelTransformer._isIgnorableAttribute((Attribute)object)) {
            return false;
        }
        if(object instanceof Typeable) {
            Typeable typeable = (Typeable)object;
           
            ptolemy.data.type.Type type = typeable.getType();
            if(type instanceof ptolemy.data.type.RecordType) {
                System.out.println("badTypeObject = " + object);
                return true;
            }
            if(type instanceof ptolemy.data.type.ArrayType) {
                type = ((ptolemy.data.type.ArrayType)type).getElementType();
                if(type instanceof ptolemy.data.type.ArrayType) {
                    System.out.println("badTypeObject = " + object);
                    return true;
                }
            }
        }
        if(object instanceof CompositeEntity) {
            for(Iterator i = ((CompositeEntity)object).entityList().iterator();
                i.hasNext();) {
                if(_hasBadTypes((NamedObj)i.next())) {
                    return true;
                }
            }
            for(Iterator i = ((CompositeEntity)object).relationList().iterator();
                i.hasNext();) {
                if(_hasBadTypes((NamedObj)i.next())) {
                    return true;
                }
            }
        }
        if(object instanceof Entity) {
            for(Iterator i = ((Entity)object).portList().iterator();
                i.hasNext();) {
                if(_hasBadTypes((NamedObj)i.next())) {
                    return true;
                }
            }
           
        }
        for(Iterator i = object.attributeList().iterator();
            i.hasNext();) {
            if(_hasBadTypes((NamedObj)i.next())) {
                return true;
            }
        }
        return false;
    }

    private CompositeActor _model;
    private String _phaseName;
    private Map entityFieldToTokenFieldToReplacementField;
    private Map entityFieldToIsNotNullField;
    private Map localToFieldToLocal;
    private Map localToIsNotNullLocal;
    private boolean _mangleExceptionMessages = true;

}














