/* A transformer that unboxes tokens

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
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeAssigner;
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
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.MustAliasAnalysis;
import ptolemy.copernicus.kernel.InstanceEqualityEliminator;


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
public class TokenToNativeTransformer extends SceneTransformer {
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

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions();
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("TokenToNativeTransformer.internalTransform("
                + phaseName + ", " + options + ")");

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

        boolean debug = Options.getBoolean(options, "debug");
        // Loop over all the actor instance classes.
        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();

            System.out.println("Inlining token methods in " + entityClass);

            // Inline all token methods, until we run out of things to inline
            boolean doneSomething = true;
            int count = 0;
            while (doneSomething && (count < 20)) {
                doneSomething = false;
                System.out.println("inlining methods iteration " + count++);
                // This will allow us to get a better type inference below.
                for (Iterator methods = entityClass.getMethods().iterator();
                     methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();

                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                    // First split local variables that are used in
                    // multiple places.
                    LocalSplitter.v().transform(
                            body, phaseName + ".ls", "");
                    // We may have locals with the same name.  Rename them.
                    LocalNameStandardizer.v().transform(
                            body, phaseName + ".lns", "");
                    // Assign types to local variables... This types
                    // everything that isn't a token type.
                    TypeAssigner.v().transform(body, phaseName + ".ta", "");

                    // Run some cleanup...  this will speedup the rest
                    // of the analysis.  And prevent typing errors.
                    TokenInstanceofEliminator.eliminateCastsAndInstanceOf(
                            body, phaseName + ".tie", unsafeLocalSet,
                            false);
                    CopyPropagator.v().transform(
                            body, phaseName + ".cp", "");
                    ConstantPropagatorAndFolder.v().transform(
                            body, phaseName + ".cpf", "");
                    ConditionalBranchFolder.v().transform(
                            body, phaseName + ".cbf", "");
                    UnreachableCodeEliminator.v().transform(
                            body, phaseName + ".uce", "");
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
                        System.out.println("unit = " + unit);
                        Iterator boxes = unit.getUseBoxes().iterator();
                        while (boxes.hasNext()) {
                            ValueBox box = (ValueBox)boxes.next();
                            Value value = box.getValue();

                            // If we have a call to evaluateParseTree, then
                            // we can't inline any of those locals.
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
                                    value instanceof InterfaceInvokeExpr) {
                                InstanceInvokeExpr r = (InstanceInvokeExpr)value;

                                Type baseType = r.getBase().getType();

                                // If we are invoking a method on a Token, and
                                // the token is not unsafe.
                                if (baseType instanceof RefType &&
                                        !unsafeLocalSet.contains(r.getBase())) {
                                    RefType type = (RefType)baseType;
                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.arrayTokenClass)) {
                                        type = (RefType)typeAnalysis.getSpecializedSootType((Local)r.getBase());
                                    }

                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.arrayTokenClass)
                                            ||  (SootUtilities.derivesFrom(type.getSootClass(),
                                                    PtolemyUtilities.typeClass) && r.getMethod().getName().equals("convert"))) {

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

                                            } else {
                                                SootClass declaringClass = inlinee.getDeclaringClass();
                                                declaringClass.setLibraryClass();
                                                if (!inlinee.isAbstract() &&
                                                        !inlinee.isNative()) {
                                                    // FIXME: only inline things where we are
                                                    // also inlining the constructor???
                                                    System.out.println("inlining " + inlinee);
                                                    inlinee.retrieveActiveBody();
                                                    // Then we know exactly what method will
                                                    // be called, so inline it.
                                                    SiteInliner.inlineSite(
                                                            inlinee, (Stmt)unit, method);
                                                    doneSomething = true;
                                                }
                                            }
                                        } else {
                                            System.out.println("uninlinable method invocation = " + r);
                                            for (Iterator j = methodList.iterator();
                                                 j.hasNext();) {
                                                System.out.println("method = " + j.next());
                                            }
                                        }
                                    } else {
                                        System.out.println("Not a token or type method.");
                                    }
                                } else {
                                    System.out.println("baseType = " + baseType);
                                }
                            } else if (value instanceof SpecialInvokeExpr) {
                                SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                                System.out.println("special invoking = " + r.getMethod());

                                Type baseType = typeAnalysis.getSpecializedSootType((Local)r.getBase());
                                if (baseType instanceof RefType) {
                                    RefType type = (RefType)baseType;

                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.arrayTokenClass)) {
                                        SootMethod inlinee =
                                            hierarchy.resolveSpecialDispatch(
                                                    r, method);
                                        SootClass declaringClass = inlinee.getDeclaringClass();
                                        declaringClass.setLibraryClass();
                                        if (!inlinee.isAbstract() &&
                                                !inlinee.isNative()) {
                                            System.out.println("inlining");
                                            inlinee.retrieveActiveBody();
                                            // Then we know exactly what method will
                                            // be called, so inline it.
                                            SiteInliner.inlineSite(
                                                    inlinee, (Stmt)unit, method);
                                            doneSomething = true;
                                        } else {
                                            System.out.println("removing");
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
                                    System.out.println("inlining typelattice method = " + unit);
                                    typeAnalysis.inlineTypeLatticeMethods(method,
                                            unit, box, r, localDefs);
                                }

                                // System.out.println("static invoking = " + r.getMethod());
                                SootMethod inlinee = (SootMethod)r.getMethod();
                                SootClass declaringClass = inlinee.getDeclaringClass();
                                if (SootUtilities.derivesFrom(declaringClass,
                                        PtolemyUtilities.arrayTokenClass)) {
                                    declaringClass.setLibraryClass();
                                    if (!inlinee.isAbstract() &&
                                            !inlinee.isNative()) {
                                        //System.out.println("inlining");
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
                    }
                }
            }
        }

        Map entityFieldToTokenFieldToReplacementField = new HashMap();
        Map localToFieldToLocal = new HashMap();
        Map localToIsNullLocal = new HashMap();

        boolean doneSomething = true;
        int count = 0;
        while (doneSomething && (count < 20)) {
            doneSomething = false;
            System.out.println("inlining field iteration " + count++);

            // Loop over all the classes.
            for (Iterator classes = Scene.v().getApplicationClasses().iterator();
                 classes.hasNext();) {
                SootClass entityClass = (SootClass)classes.next();

                if (debug) System.out.println("creating replacement fields in Class = " + entityClass);
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
                        continue;
                    }
                    ptolemy.data.type.Type fieldTokenType =
                        typeAnalysis.getSpecializedType(field);

                    // If the type is not instantiable, then skip it.
                    if (!fieldTokenType.isInstantiable()) {
                        continue;
                    }

                    // If we've already created subfields for this
                    // field, then don't do it again.
                    if (entityFieldToTokenFieldToReplacementField.get(field) != null) {
                        continue;
                    }

                    // We are going to make a modification
                    doneSomething = true;

                    RefType type = (RefType)PtolemyUtilities.getBaseTokenType(fieldType);
                    SootClass fieldClass = type.getSootClass();

                    if (!SootUtilities.derivesFrom(fieldClass, PtolemyUtilities.arrayTokenClass)) {
                        continue;
                    }

                    if (debug) System.out.println("field = " + field);

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
                    }
                }

                if (debug) System.out.println("creating replacement locals in Class = " + entityClass);

                for (Iterator methods = entityClass.getMethods().iterator();
                     methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();

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

                        // If the type is not instantiable, then skip it.
                        //    if (!localTokenType.isInstantiable()) {
                        //    continue;
                        // }

                        // If we've already created subfields for this
                        // field, then don't do it again.
                        if (localToFieldToLocal.get(local) != null) {
                            continue;
                        }

                        // We are going to make a modification
                        doneSomething = true;

                        RefType type = PtolemyUtilities.getBaseTokenType(localType);
                        SootClass localClass = type.getSootClass();

                        if (debug) System.out.println("local = " + local);
                        if (debug) System.out.println("localClass = " + localClass);

                        if (!SootUtilities.derivesFrom(localClass,
                                PtolemyUtilities.arrayTokenClass)) {
                            continue;
                        }

                        // Create a boolean value that tells us whether or
                        // not the token is null.  Initialize it to true.
                        Local isNullLocal = Jimple.v().newLocal(
                                local.getName() + "_isNull", BooleanType.v());
                        body.getLocals().add(isNullLocal);
                        localToIsNullLocal.put(local, isNullLocal);
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        isNullLocal,
                                        IntConstant.v(1)),
                                body.getFirstNonIdentityStmt());

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
                                Local toLocal = (Local)r.getArg(0);
                                Local fromLocal = (Local)r.getArg(2);
                                Map toFieldToReplacementLocal =
                                    (Map) localToFieldToLocal.get(toLocal);
                                Map fromFieldToReplacementLocal =
                                    (Map) localToFieldToLocal.get(fromLocal);
                                if (toFieldToReplacementLocal != null &&
                                        fromFieldToReplacementLocal != null) {
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
                                    SootField field = (SootField)
                                        fieldToReplacementArrayLocal.keySet().iterator().next();
                                    if (debug) System.out.println("replace with  " +
                                            fieldToReplacementArrayLocal.get(field));
                                    lengthExpr.setOp((Local)
                                            fieldToReplacementArrayLocal.get(field));
                                    if (debug) System.out.println("unit now = " + unit);
                                }
                                //body.getUnits().remove(unit);
                            } else if (stmt.getLeftOp() instanceof InstanceFieldRef) {
                                // Replace references to fields of tokens.
                                // FIXME: assign to all aliases as well.
                                if (debug) System.out.println("is Instance FieldRef");
                                InstanceFieldRef r = (InstanceFieldRef)stmt.getLeftOp();
                                SootField field = r.getField();
                                if (r.getBase().getType() instanceof RefType) {
                                    RefType type = (RefType)r.getBase().getType();
                                    //System.out.println("BaseType = " + type);
                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.arrayTokenClass)) {
                                        if (debug) System.out.println("handling " +
                                                unit + " token operation");

                                        // We have a reference to a field of a token class.
                                        Local baseLocal = (Local)r.getBase();
                                        Local instanceLocal = _getInstanceLocal(body, baseLocal,
                                                field, localToFieldToLocal, debug);
                                        stmt.getLeftOpBox().setValue(instanceLocal);
                                        doneSomething = true;
                                    }
                                }
                            } else if (stmt.getRightOp() instanceof InstanceFieldRef) {
                                // Replace references to fields of tokens.
                                if (debug) System.out.println("is Instance FieldRef");
                                InstanceFieldRef r = (InstanceFieldRef)stmt.getRightOp();
                                SootField field = r.getField();
                                if (r.getBase().getType() instanceof RefType) {
                                    RefType type = (RefType)r.getBase().getType();
                                //System.out.println("BaseType = " + type);
                                    if (SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.arrayTokenClass)) {
                                        if (debug) System.out.println("handling " +
                                                unit + " token operation");

                                        // We have a reference to a field of a token class.
                                        Local baseLocal = (Local)r.getBase();
                                        Local instanceLocal = _getInstanceLocal(body, baseLocal,
                                                field, localToFieldToLocal, debug);
                                        stmt.getRightOpBox().setValue(instanceLocal);
                                        doneSomething = true;

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
                    for (Iterator units = body.getUnits().snapshotIterator();
                         units.hasNext();) {
                        Unit unit = (Unit)units.next();
                        if (debug) System.out.println("unit = " + unit);
                        if (unit instanceof AssignStmt) {
                            AssignStmt stmt = (AssignStmt)unit;
                            Type assignmentType = stmt.getLeftOp().getType();
                            if (PtolemyUtilities.isArrayTokenType(assignmentType)) {
                                if (stmt.getLeftOp() instanceof Local &&
                                        (stmt.getRightOp() instanceof Local ||
                                                stmt.getRightOp() instanceof Constant)) {
                                    if (debug) System.out.println("handling as local-immediate assign");
                                    doneSomething |= _handleImmediateAssignment(body, unit,
                                            localToFieldToLocal, localToIsNullLocal,
                                            stmt.getLeftOp(), stmt.getRightOp(), debug);

                                } else if (stmt.getLeftOp() instanceof Local &&
                                        stmt.getRightOp() instanceof CastExpr) {
                                    if (debug) System.out.println("handling as local cast");
                                    Value rightLocal = ((CastExpr)stmt.getRightOp()).getOp();

                                    doneSomething |= _handleImmediateAssignment(body, unit,
                                            localToFieldToLocal, localToIsNullLocal,
                                            stmt.getLeftOp(), rightLocal, debug);

                                } else if (stmt.getLeftOp() instanceof FieldRef &&
                                        stmt.getRightOp() instanceof Local) {
                                    if (debug) System.out.println("handling as assignment to Field");
                                    SootField field = ((FieldRef)stmt.getLeftOp()).getField();
                                    Map fieldToReplacementField =
                                        (Map) entityFieldToTokenFieldToReplacementField.get(field);
                                    Map fieldToReplacementLocal =
                                        (Map) localToFieldToLocal.get(stmt.getRightOp());

                                    if (fieldToReplacementLocal != null &&
                                            fieldToReplacementField != null) {
                                        doneSomething = true;
                                        // FIXME isNull field?
                                        for (Iterator tokenFields = fieldToReplacementField.keySet().iterator();
                                             tokenFields.hasNext();) {
                                            SootField tokenField = (SootField)tokenFields.next();

                                            SootField replacementField = (SootField)
                                                fieldToReplacementField.get(tokenField);
                                            Local replacementLocal = (Local)
                                                fieldToReplacementLocal.get(tokenField);
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
                                        body.getUnits().remove(unit);
                                    }
                                } else if (stmt.getLeftOp() instanceof Local &&
                                        stmt.getRightOp() instanceof FieldRef) {
                                    if (debug) System.out.println("handling as assignment from Field");
                                    Map fieldToReplacementLocal =
                                        (Map) localToFieldToLocal.get(stmt.getLeftOp());
                                    SootField field = ((FieldRef)stmt.getRightOp()).getField();
                                    Map fieldToReplacementField =
                                        (Map) entityFieldToTokenFieldToReplacementField.get(field);

                                    if (fieldToReplacementLocal != null &&
                                            fieldToReplacementField != null) {
                                        doneSomething = true;
                                        // Replace references to fields with token types.
                                        // FIXME properly handle isNull field?
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                        IntConstant.v(0)),
                                                unit);
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
                                        body.getUnits().remove(unit);
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

                                    if (fieldToReplacementLocal != null &&
                                            fieldToReplacementArrayLocal != null) {
                                        doneSomething = true;
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        (Local)localToIsNullLocal.get(baseLocal),
                                                        (Local)localToIsNullLocal.get(stmt.getRightOp())),
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
                                                        (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                        (Local)localToIsNullLocal.get(baseLocal)),
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
                                        body.getUnits().remove(unit);
                                    }
                                } else if (stmt.getLeftOp() instanceof Local &&
                                        stmt.getRightOp() instanceof NewArrayExpr) {
                                    if (debug) System.out.println("handling as new array object");
                                    NewArrayExpr newExpr = (NewArrayExpr)stmt.getRightOp();
                                // We have an assignment from one local token to another.
                                    Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                    if (map != null) {
                                        doneSomething = true;

                                        Local isNullLocal = (Local)
                                            localToIsNullLocal.get(stmt.getLeftOp());
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        isNullLocal,
                                                        IntConstant.v(0)),
                                                unit);
                                        for (Iterator tokenFields = map.keySet().iterator();
                                             tokenFields.hasNext();) {
                                            SootField tokenField = (SootField)tokenFields.next();
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
                                        body.getUnits().remove(unit);
                                    }

                                } else if (stmt.getLeftOp() instanceof Local &&
                                        stmt.getRightOp() instanceof NewExpr) {
                                    if (debug) System.out.println("handling as new object");
                                    NewExpr newExpr = (NewExpr)stmt.getRightOp();
                                    // We have an assignment from one local token to another.
                                    Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                    if (map != null) {
                                        doneSomething = true;

                                        Local isNullLocal = (Local)
                                            localToIsNullLocal.get(stmt.getLeftOp());
                                        System.out.println("Stmt = " + stmt);

                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        isNullLocal,
                                                        IntConstant.v(0)),
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
                                        body.getUnits().remove(unit);
                                    }

                                }
                                //  else if (stmt.getLeftOp() instanceof Local &&
                                //                                        stmt.getRightOp() instanceof InvokeExpr) {
                                //                                  System.out.println("handling as method call.");
                                //                                  // We have an assignment from one local token to another.
                                //                                  Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                //                                  if (map != null) {
                                //                                      Local isNullLocal = (Local)
                                //                                          localToIsNullLocal.get(stmt.getLeftOp());
                                //                                      body.getUnits().insertAfter(
                                //                                              Jimple.v().newAssignStmt(
                                //                                                      isNullLocal,
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
                        // FIXME!
                        if (false)
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

                                        Local isNullLocal = (Local)
                                            localToIsNullLocal.get(expr.getOp1());
                                        if (expr instanceof EqExpr) {
                                            box.setValue(Jimple.v().newEqExpr(
                                                    isNullLocal,
                                                    IntConstant.v(1)));
                                        } else if (expr instanceof NeExpr) {
                                            box.setValue(Jimple.v().newEqExpr(
                                                    isNullLocal,
                                                    IntConstant.v(0)));
                                        }
                                    } else if (op2IsToken && expr.getOp1().getType().equals(NullType.v())) {
                                        doneSomething = true;

                                        Local isNullLocal = (Local)
                                            localToIsNullLocal.get(expr.getOp2());
                                        if (expr instanceof EqExpr) {
                                            box.setValue(Jimple.v().newEqExpr(
                                                    isNullLocal,
                                                    IntConstant.v(1)));
                                        } else if (expr instanceof NeExpr) {
                                            box.setValue(Jimple.v().newEqExpr(
                                                    isNullLocal,
                                                    IntConstant.v(0)));
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }

        // Loop over all the classes.
        if (false)  for (Iterator classes = Scene.v().getApplicationClasses().iterator();
                         classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();

            // Now go through all the methods one last time, and replace
            // any remaining uses of tokens with Null...
            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while (boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if (value instanceof Local &&
                                PtolemyUtilities.isTokenType(value.getType()) &&
                                !unsafeLocalSet.contains(value)) {
                            System.out.println("Replacing " +
                                    value + " in " + method + " with null.");
                            body.getUnits().insertBefore(
                                    Jimple.v().newAssignStmt(
                                            value,
                                            NullConstant.v()),
                                    unit);
                        }
                    }
                }
                //                CastAndInstanceofEliminator.eliminateCastsAndInstanceOf(
                //        body, phaseName + ".cie", unsafeLocalSet);
                //CopyPropagator.v().transform(body, phaseName + ".cp", "");
                //ConstantPropagatorAndFolder.v().transform(body, phaseName + ".cpf", "");
                //ConditionalBranchFolder.v().transform(body, phaseName + ".cbf", "");
                //UnreachableCodeEliminator.v().transform(body, phaseName + ".uce", "");
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
            List defList = stmt.getDefBoxes();
            if (defList.size() == 1) {
                Value value = ((ValueBox)defList.get(0)).getValue();
                if (PtolemyUtilities.isTokenType(value.getType())) {
                    set.add(value);
                    set.addAll(_computeTokenLocalsDefinedFrom(localUses, defStmt));
                }
            }
        }
        return set;
    }

    public static Local _getInstanceLocal(Body body, Local baseLocal,
            SootField field, Map localToFieldToLocal, boolean debug) {
        // Get the map for that local from a field of the local's
        // class to the local variable that replaces it.
        Map fieldToLocal = (Map)localToFieldToLocal.get(baseLocal);
        if (fieldToLocal == null) {
            if (debug) System.out.println("creating new fieldToLocal for " + baseLocal);
            // If we have not seen this local get, then
            // create a new map.
            fieldToLocal = new HashMap();
            localToFieldToLocal.put(baseLocal, fieldToLocal);
        }
        Local instanceLocal = (Local)fieldToLocal.get(field);
        if (instanceLocal == null) {
            if (debug) System.out.println("creating new instanceLocal for " + baseLocal);
            // If we have not referenced this particular field
            // then create a new local.
            instanceLocal = Jimple.v().newLocal(
                    baseLocal.getName() + "_" + field.getName(),
                    field.getType());
            body.getLocals().add(instanceLocal);
            fieldToLocal.put(field, instanceLocal);
        }
        return instanceLocal;
    }

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
                type instanceof ByteType ||
                type instanceof BooleanType) {
            return IntConstant.v(7);
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
            JimpleBody body, Unit unit,
            Map localToFieldToLocal, Map localToIsNullLocal,
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
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                (Local)localToIsNullLocal.get(leftValue),
                                IntConstant.v(1)),
                        unit);
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
                            unit);
                }
                body.getUnits().remove(unit);
            }
        } else {
            // We have an assignment from one local token to another.
            if (fieldToReplacementLeftLocal != null &&
                    fieldToReplacementRightLocal != null) {
                doneSomething = true;
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                (Local)localToIsNullLocal.get(leftValue),
                                (Local)localToIsNullLocal.get(rightValue)),
                        unit);
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
                            unit);
                }
                body.getUnits().remove(unit);
            }

        }
        return doneSomething;
    }
    private CompositeActor _model;
}














