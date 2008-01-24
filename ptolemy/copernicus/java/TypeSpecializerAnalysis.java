/* A transformer that specializes token types in an actor.

 Copyright (c) 2001-2008 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalitySolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
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
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.LengthExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.UnopExpr;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;

//////////////////////////////////////////////////////////////////////////
//// TypeSpecializerAnalysis

/**
 A transformer that modifies each class using the token types from
 ports.  In particular, this class creates constraints in the same
 fashion as the Ptolemy II type system and solves those constraints.
 The resulting solution should correspond to valid Java types which are
 more specific (in the Ptolemy II sense) than the original Java types.
 The code is then transformed to use these more specific types.

 <p> This transformer is necessary because there are some token types
 that we want to make more specific, but that don't directly depend on
 the the types of a port.  This transformation enables the token unboxing
 performed by the TokenToNativeTransformer

 <p> Note that this analysis assumes very little flow of control...  Or
 at least that the types of each variable along each path are the
 same...  as such, it works very badly if there are any instanceof
 checks, along with some other program constructs.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TypeSpecializerAnalysis {
    /** Specialize all token types that appear in the given class.
     *  Return a map from locals and fields in the class to their new
     *  specific Ptolemy type.  Exclude locals in the given set from
     *  the typing algorithm.
     */
    public TypeSpecializerAnalysis(SootClass theClass, Set unsafeLocals) {
        if (_debug) {
            System.out.println("Starting type specialization");
        }

        _unsafeLocals = unsafeLocals;

        _solver = new InequalitySolver(new JavaTypeLattice());

        //TypeLattice.lattice());
        _objectToInequalityTerm = new HashMap();

        // Get the variables.
        //  _collectVariables(theClass, _debug);
        for (Iterator classes = Scene.v().getApplicationClasses().iterator(); classes
                .hasNext();) {
            SootClass applicationClass = (SootClass) classes.next();
            _collectVariables(applicationClass, _debug);
        }

        _collectConstraints(theClass, _debug);

        boolean succeeded;

        try {
            succeeded = _solver.solveLeast();
        } catch (Exception ex) {
            _printSolverVariables();
            throw new RuntimeException(ex);
        }

        if (_debug) {
            _printSolverVariables();
        }

        if (succeeded) {
            if (_debug) {
                System.out.println("solution FOUND!");
            }
        } else {
            System.out.println("Unsatisfied Inequalities:");

            try {
                Iterator inequalities = _solver.unsatisfiedInequalities();

                while (inequalities.hasNext()) {
                    System.out.println("Inequality: "
                            + inequalities.next().toString());
                }

                System.err.println("Unsatisfied Inequalities:");
                inequalities = _solver.unsatisfiedInequalities();

                while (inequalities.hasNext()) {
                    System.err.println("Inequality: "
                            + inequalities.next().toString());
                }
            } catch (IllegalActionException ex) {
                throw new RuntimeException("Error in type resolution");
            }
        }

        //        System.out.println("Done");
    }

    /** Specialize all token types that appear in the given list of
     *  classes.  Return a map from locals and fields in the class to
     *  their new specific Ptolemy type.  Exclude locals in the given
     *  set from the typing algorithm.
     *  @param list A list of SootClass.
     */
    public TypeSpecializerAnalysis(List list, Set unsafeLocals) {
        if (_debug) {
            System.out.println("Starting type specialization list");
        }

        _unsafeLocals = unsafeLocals;

        _solver = new InequalitySolver(new JavaTypeLattice()); //TypeLattice.lattice());
        _objectToInequalityTerm = new HashMap();

        for (Iterator classes = list.iterator(); classes.hasNext();) {
            SootClass theClass = (SootClass) classes.next();
            _collectVariables(theClass, _debug);
        }

        for (Iterator classes = list.iterator(); classes.hasNext();) {
            SootClass theClass = (SootClass) classes.next();
            _collectConstraints(theClass, _debug);
        }

        boolean succeeded;

        try {
            succeeded = _solver.solveLeast();
        } catch (Exception ex) {
            _printSolverVariables();
            throw new RuntimeException(ex.getMessage());
        }

        if (_debug) {
            _printSolverVariables();
        }

        if (succeeded) {
            if (_debug) {
                System.out.println("solution FOUND!");
            }
        } else {
            System.out.println("Unsatisfied Inequalities:");

            try {
                Iterator inequalities = _solver.unsatisfiedInequalities();

                while (inequalities.hasNext()) {
                    System.out.println("Inequality: "
                            + inequalities.next().toString());
                }

                System.err.println("Unsatisfied Inequalities:");
                inequalities = _solver.unsatisfiedInequalities();

                while (inequalities.hasNext()) {
                    System.err.println("Inequality: "
                            + inequalities.next().toString());
                }
            } catch (IllegalActionException ex) {
                throw new RuntimeException("Error in type resolution");
            }
        }

        //        System.out.println("Done");
    }

    public Iterator getSolverVariables() {
        return _solver.variables();
    }

    public Type getSpecializedSootType(Local local) {
        try {
            Type type = local.getType();
            Type type2 = _getUpdateType(local, type);

            if (type2 == null) {
                return type;
            } else {
                return type2;
            }
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Type getSpecializedSootType(NewArrayExpr expr) {
        try {
            Type type = expr.getBaseType();
            Type type2 = _getUpdateType(expr, type);

            if (type2 == null) {
                return type;
            } else {
                return type2;
            }
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Type getSpecializedSootType(SootField field) {
        try {
            Type type = field.getType();
            Type type2 = _getUpdateType(field, type);

            if (type2 == null) {
                return type;
            } else {
                return type2;
            }
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ptolemy.data.type.Type getSpecializedType(Local local) {
        try {
            return _getTokenType(local);
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ptolemy.data.type.Type getSpecializedType(NewArrayExpr expr) {
        try {
            return _getTokenType(expr);
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public ptolemy.data.type.Type getSpecializedType(SootField field) {
        try {
            return _getTokenType(field);
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /** Inline the given invocation point in the given box, unit, and method.
     *  Use the given local definition information to
     *  perform the inlining.
     */
    public void inlineTypeLatticeMethods(SootMethod method, Unit unit,
            ValueBox box, StaticInvokeExpr expr, LocalDefs localDefs,
            LocalUses localUses) throws IllegalActionException {
        if (_debug) {
            System.out.println("inlineTypeLatticeMethods:\n\t" + method
                    + "\n\t" + unit + "\n\t" + expr);
        }
        SootMethod tokenTokenCompareMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("int compare(ptolemy.data.Token,ptolemy.data.Token)");
        SootMethod tokenTypeCompareMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("int compare(ptolemy.data.Token,ptolemy.data.type.Type)");
        SootMethod typeTokenCompareMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("int compare(ptolemy.data.type.Type,ptolemy.data.Token)");
        SootMethod typeTypeCompareMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("int compare(ptolemy.data.type.Type,ptolemy.data.type.Type)");
        SootMethod leastUpperBoundMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("ptolemy.data.type.Type leastUpperBound(ptolemy.data.type.Type,ptolemy.data.type.Type)");
        SootMethod latticeMethod = PtolemyUtilities.typeLatticeClass
                .getMethod("ptolemy.graph.CPO lattice()");

        ptolemy.data.type.Type type1;
        ptolemy.data.type.Type type2;

        String nullTypeMessage = "One or both of the arguments to TypeLattice.compare is/are "
                + "null or unknown.  This means we are likely to throw an exception at "
                + "runtime.  Explanation: \n"
                + "Getting 'null' out of the TypeAnalysis "
                + "means 'I can't figure out where this came from and hence, "
                + "I can't tell you what the type is'  The type might be "
                + "incomparable, or maybe not! The type analysis is rather "
                + "hairy to debug because it happens multiple "
                + "times and is self dependent...  A bug in one place tends "
                + "to propagate to a method inline, which propagates to another "
                + "type analysis....";

        if (expr.getMethod().equals(tokenTokenCompareMethod)) {
            Local tokenLocal1 = (Local) expr.getArg(0);
            Local tokenLocal2 = (Local) expr.getArg(1);
            type1 = getSpecializedType(tokenLocal1);
            type2 = getSpecializedType(tokenLocal2);
            if (type1 == null || type2 == null) {
                System.out.println(nullTypeMessage
                        + "\n\t tokentokenCompare: type1 = " + type1
                        + " type2 = " + type2);
            }
        } else if (expr.getMethod().equals(typeTokenCompareMethod)) {
            Local typeLocal = (Local) expr.getArg(0);
            Local tokenLocal = (Local) expr.getArg(1);
            //             type1 = PtolemyUtilities.getTypeValue(method, typeLocal, unit,
            //                     localDefs, localUses);
            type1 = getSpecializedType(typeLocal);
            type2 = getSpecializedType(tokenLocal);
            if (type1 == null || type2 == null || type1 == BaseType.UNKNOWN) {
                System.out.println("Contents of _objectToInequalityTerm");
                Iterator terms = _objectToInequalityTerm.entrySet().iterator();
                while (terms.hasNext()) {
                    Map.Entry pairs = (Map.Entry) terms.next();
                    System.out.println(pairs.getKey() + " = "
                            + (InequalityTerm) (pairs.getValue()));
                }
                System.out.println(nullTypeMessage
                        + "\n\t typetokenCompare: type1 = " + type1
                        + " type2 = " + type2);
                //System.exit(2);
            }
        } else if (expr.getMethod().equals(tokenTypeCompareMethod)) {
            Local tokenLocal = (Local) expr.getArg(0);
            Local typeLocal = (Local) expr.getArg(1);
            type1 = getSpecializedType(tokenLocal);
            type2 = getSpecializedType(typeLocal);
            if (type1 == null || type2 == null || type2 == BaseType.UNKNOWN) {
                System.out.println(nullTypeMessage
                        + "\n\t tokentypeCompare: type1 = " + type1
                        + "type2 = " + type2);
            }
            //             type2 = PtolemyUtilities.getTypeValue(method, typeLocal, unit,
            //                     localDefs, localUses);
        } else if (expr.getMethod().equals(typeTypeCompareMethod)) {
            Local typeLocal1 = (Local) expr.getArg(0);
            Local typeLocal2 = (Local) expr.getArg(1);
            type1 = getSpecializedType(typeLocal1);
            type2 = getSpecializedType(typeLocal2);
            if (type1 == null || type2 == null || type1 == BaseType.UNKNOWN
                    || type2 == BaseType.UNKNOWN) {
                System.out
                        .println(nullTypeMessage
                                + "\n\t typetypeCompare: type1 = " + "type2 = "
                                + type2);
            }
            //             type1 = PtolemyUtilities.getTypeValue(method, typeLocal1, unit,
            //                     localDefs, localUses);
            //             type2 = PtolemyUtilities.getTypeValue(method, typeLocal2, unit,
            //                     localDefs, localUses);
        } else if (expr.getMethod().equals(leastUpperBoundMethod)) {
            if (_debug) {
                System.out.println("Found LUB method!");
            }

            Local typeLocal1 = (Local) expr.getArg(0);
            Local typeLocal2 = (Local) expr.getArg(1);
            type1 = getSpecializedType(typeLocal1);
            type2 = getSpecializedType(typeLocal2);
            //             type1 = PtolemyUtilities.getTypeValue(method, typeLocal1, unit,
            //                     localDefs, localUses);
            //             type2 = PtolemyUtilities.getTypeValue(method, typeLocal2, unit,
            //                     localDefs, localUses);

            Local newTypeLocal = PtolemyUtilities.buildConstantTypeLocal(method
                    .getActiveBody(), unit, TypeLattice.leastUpperBound(type1,
                    type2));
            box.setValue(newTypeLocal);

            if (_debug) {
                System.out.println("inlineTypeLatticeMethods: LUB Method: "
                        + typeLocal1 + " " + typeLocal2 + " " + type1 + type2);
            }

            return;
        } else if (expr.getMethod().equals(latticeMethod)) {
            if (_debug) {
                System.out
                        .println("inlineTypeLatticeMethods: latticeMethod: Do nothing.");
            }
            // Do nothing...
            return;
        } else {
            throw new RuntimeException(
                    "attempt to inline unhandled typeLattice method: " + unit);
        }

        if (_debug) {
            System.out.println("specializer");
            System.out.println("type1 = " + type1);
            System.out.println("type2 = " + type2);
            System.out.println("result = " + TypeLattice.compare(type1, type2));
        }
        // Only inline if both are concrete types.
        if (type1.isInstantiable() && type2.isInstantiable()) {
            if (_debug) {
                System.out.println("inlineTypeLatticeMethods: is Instantiable");
            }
            box.setValue(IntConstant.v(TypeLattice.compare(type1, type2)));
        }
    }

    private void _collectVariables(SootClass entityClass, boolean debug) {
        // Loop through all the fields.
        for (Iterator fields = entityClass.getFields().iterator(); fields
                .hasNext();) {
            SootField field = (SootField) fields.next();

            // Ignore things that aren't reference types.
            Type type = field.getType();
            _createInequalityTerm(debug, field, type, _objectToInequalityTerm);

            // If the field has been tagged with a more specific type, then
            // constrain the type more.
            TypeTag tag = (TypeTag) field.getTag("_CGType");

            if (tag != null) {
                if (debug) {
                    System.out.println("tagged with type = " + tag.getType());
                }

                _addInequality(debug, _solver, new ConstantTerm(tag.getType(),
                        field), (InequalityTerm) _objectToInequalityTerm
                        .get(field));
            }
        }
    }

    private void _collectConstraints(SootClass entityClass, boolean debug) {
        if (debug) {
            System.out.println("collecting constraints for " + entityClass);
        }

        // Loop through all the methods.
        for (Iterator methods = entityClass.getMethods().iterator(); methods
                .hasNext();) {
            SootMethod method = (SootMethod) methods.next();
            Body body = method.retrieveActiveBody();

            if (debug) {
                System.out.println("collecting constraints for " + method);
            }

            CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

            // this will help us figure out where locals are defined.
            SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
            SimpleLocalUses localUses = new SimpleLocalUses(unitGraph,
                    localDefs);

            //   System.out.println("done computing aliases for " + method);
            for (Iterator locals = body.getLocals().iterator(); locals
                    .hasNext();) {
                Local local = (Local) locals.next();

                if (_unsafeLocals.contains(local)) {
                    continue;
                }

                // Ignore things that aren't reference types.
                Type type = local.getType();
                _createInequalityTerm(debug, local, type,
                        _objectToInequalityTerm);
            }

            for (Iterator units = body.getUnits().iterator(); units.hasNext();) {
                Stmt stmt = (Stmt) units.next();

                if (debug) {
                    System.out.println("stmt = " + stmt);
                }

                if (stmt instanceof AssignStmt) {
                    Value leftOp = ((AssignStmt) stmt).getLeftOp();
                    Value rightOp = ((AssignStmt) stmt).getRightOp();

                    // Note that the only real possibilities on the
                    // left side are a local or a fieldRef.
                    InequalityTerm leftOpTerm = _getInequalityTerm(method,
                            debug, leftOp, _solver, _objectToInequalityTerm,
                            stmt, localDefs, localUses);

                    InequalityTerm rightOpTerm = _getInequalityTerm(method,
                            debug, rightOp, _solver, _objectToInequalityTerm,
                            stmt, localDefs, localUses);

                    // The type of all aliases of the left hand side
                    // must always be greater than
                    // the type of all aliases of the right hand side.
                    _addInequality(debug, _solver, rightOpTerm, leftOpTerm);

                    // If an alias is created by this instruction,
                    // then the left and right hand sides must
                    // actually be equal.  NOTE: Alternatively, we
                    // could create individual constraints for all of
                    // the different aliases.  This might be better
                    // given that we actually have alias information.
                    if (SootUtilities.isAliasableValue(leftOp)
                            && SootUtilities.isAliasableValue(rightOp)) {
                        _addInequality(debug, _solver, leftOpTerm, rightOpTerm);
                    }
                } else if (stmt instanceof InvokeStmt) {
                    // Still call getInequalityTerm because there may
                    // be side effects that cause type constraints.
                    _getInequalityTerm(method, debug, ((InvokeStmt) stmt)
                            .getInvokeExpr(), _solver, _objectToInequalityTerm,
                            stmt, localDefs, localUses);
                }
            }
        }

        // System.out.println("done collecting constraints for " + entityClass);
    }

    // Given an object (which must be either a local, or a field) of
    // the given type, look into the given map and retrieve the
    // inequality term for the object.  retrieve the resolved type,
    // and return it.
    //    private static Type _getUpdateType(boolean debug, Object object, Type type,
    //            Map objectToInequalityTerm) throws IllegalActionException {
    //        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
    //
    //        if (tokenType != null) {
    //            if (debug) {
    //                System.out.println("type of value " + object + " = " + type);
    //            }
    //
    //            InequalityTerm term = (InequalityTerm) objectToInequalityTerm
    //                    .get(object);
    //
    //            if (term == null) {
    //                return null;
    //            }
    //
    //            ptolemy.data.type.Type newTokenType = (ptolemy.data.type.Type) term
    //                    .getValue();
    //            RefType newType = PtolemyUtilities
    //                    .getSootTypeForTokenType(newTokenType);
    //
    //            if (debug) {
    //                System.out.println("newType = " + newType);
    //            }
    //
    //            if (!SootUtilities.derivesFrom(newType.getSootClass(), tokenType
    //                    .getSootClass())) {
    //                // If the new Type is less specific, in Java terms,
    //                // than what we had before, then the resulting code is
    //                // likely not correct.  FIXME: hack to get around the
    //                // bogus type lattice.  This should be an exception.
    //                System.out.println("Warning! Resolved type of " + object
    //                        + " to " + newType
    //                        + " which is more general than the old type " + type);
    //                newType = tokenType;
    //            }
    //
    //            // create a new type isomorphic with the old type.
    //            return SootUtilities.createIsomorphicType(type, newType);
    //        }
    //
    //        // If this is not a token class, then we don't change it.
    //        return null;
    //    }

    // Given an object (which must be either a local, or a field) of
    // the given type, look into the given map and retrieve the
    // inequality term for the object.  retrieve the resolved type,
    // and return it.
    private Type _getUpdateType(Object object, Type type)
            throws IllegalActionException {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);

        if (tokenType != null) {
            if (_debug) {
                System.out.println("type of value " + object + " = " + type);
            }

            InequalityTerm term = (InequalityTerm) _objectToInequalityTerm
                    .get(object);

            if (term == null) {
                return null;
            }

            ptolemy.data.type.Type newTokenType = (ptolemy.data.type.Type) term
                    .getValue();
            RefType newType = PtolemyUtilities
                    .getSootTypeForTokenType(newTokenType);

            if (_debug) {
                System.out.println("newType = " + newType);
            }

            if (!SootUtilities.derivesFrom(newType.getSootClass(), tokenType
                    .getSootClass())) {
                // If the new Type is less specific, in Java terms,
                // than what we had before, then the resulting code is
                // likely not correct.  FIXME: hack to get around the
                // bogus type lattice.  This should be an exception.
                System.out.println("Warning! Resolved type of " + object
                        + " to " + newType
                        + " which is more general than the old type " + type);
                newType = tokenType;
            }

            // create a new type isomorphic with the old type.
            return SootUtilities.createIsomorphicType(type, newType);
        }

        // If this is not a token class, then we don't change it.
        return null;
    }

    public ptolemy.data.type.Type _getTokenType(Object object)
            throws IllegalActionException {
        InequalityTerm term = (InequalityTerm) _objectToInequalityTerm
                .get(object);

        if (term == null) {
            throw new RuntimeException("Attempt to get type for object "
                    + object + " with no inequality term!");
        }

        return (ptolemy.data.type.Type) term.getValue();
    }

    public static InequalityTerm _getInequalityTerm(SootMethod method,
            boolean debug, Value value, InequalitySolver solver,
            Map objectToInequalityTerm, Unit unit, LocalDefs localDefs,
            LocalUses localUses) {
        if (debug) {
            System.out.println("_getInequalityTerm(): " + method + " " + value);
        }
        if (value instanceof StaticInvokeExpr) {
            StaticInvokeExpr r = (StaticInvokeExpr) value;

            if (r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                // If we are copying one array to another, then the
                // types must be equal.
                InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                        .get(r.getArg(0));
                InequalityTerm thirdArgTerm = (InequalityTerm) objectToInequalityTerm
                        .get(r.getArg(2));
                _addInequality(debug, solver, firstArgTerm, thirdArgTerm);
                _addInequality(debug, solver, thirdArgTerm, firstArgTerm);
                return null;
            }
        } else if (value instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr r = (InstanceInvokeExpr) value;
            String methodName = r.getMethod().getName();

            // If we are invoking on something that is not a reference type,
            // then ignore it.
            if (!(r.getBase().getType() instanceof RefType)) {
                return null;
            }

            //    System.out.println("invokeExpr = " + r);
            SootClass baseClass = ((RefType) r.getBase().getType())
                    .getSootClass();
            InequalityTerm baseTerm = (InequalityTerm) objectToInequalityTerm
                    .get(r.getBase());

            // FIXME: match better.
            // If we are invoking a method on a token, then...
            if (SootUtilities
                    .derivesFrom(baseClass, PtolemyUtilities.typeClass)) {
                if (methodName.equals("convert")) {
                    return baseTerm;
                    //                     try {
                    //                         ptolemy.data.type.Type baseType = PtolemyUtilities
                    //                                 .getTypeValue(method, (Local) r.getBase(),
                    //                                         unit, localDefs, localUses);
                    //                         InequalityTerm baseTypeTerm = new ConstantTerm(
                    //                                 baseType, r);
                    //                         return baseTypeTerm;
                    //                     } catch (RuntimeException ex) {
                    //                         // Ignore..
                    //                     }
                } else if (r
                        .getMethod()
                        .getSignature()
                        .equals(
                                "<ptolemy.data.type.ArrayType: void <init>(ptolemy.data.type.Type)>")) {
                    InequalityTerm elementTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));
                    ptolemy.data.type.ArrayType arrayType = new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                    InequalityTerm variableTerm = new VariableTerm(arrayType, r);
                    _addInequality(debug, solver, elementTerm, arrayType
                            .getElementTypeTerm());
                    _addInequality(debug, solver, arrayType
                            .getElementTypeTerm(), elementTerm);
                    _addInequality(debug, solver, variableTerm, baseTerm);
                    return variableTerm;
                }
            }

            if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.tokenClass)) {
                if (r.getMethod()
                        .equals(PtolemyUtilities.arrayTokenConstructor)) {
                    InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));
                    ptolemy.data.type.ArrayType arrayType = new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                    VariableTerm newTerm = new VariableTerm(arrayType, r);
                    _addInequality(debug, solver, baseTerm, newTerm);
                    _addInequality(debug, solver, newTerm, baseTerm);

                    InequalityTerm elementTerm = arrayType.getElementTypeTerm();
                    _addInequality(debug, solver, firstArgTerm, elementTerm);
                    _addInequality(debug, solver, elementTerm, firstArgTerm);
                    return baseTerm;
                } else if (r.getMethod().equals(
                        PtolemyUtilities.arrayTokenWithTypeConstructor)) {
                    InequalityTerm elementTypeTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));
                    ptolemy.data.type.ArrayType arrayType = new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                    VariableTerm newTerm = new VariableTerm(arrayType, r);
                    _addInequality(debug, solver, baseTerm, newTerm);
                    _addInequality(debug, solver, newTerm, baseTerm);

                    InequalityTerm elementTerm = arrayType.getElementTypeTerm();
                    _addInequality(debug, solver, elementTypeTerm, elementTerm);
                    _addInequality(debug, solver, elementTerm, elementTypeTerm);
                    return baseTerm;
                } else if (methodName.equals("one")
                        || methodName.equals("zero")
                        || methodName.equals("bitwiseNot")
                        || methodName.equals("pow")
                        || methodName.equals("logicalRightShift")
                        || methodName.equals("leftShift")
                        || methodName.equals("rightShit")) {
                    // The returned type must be equal to the type
                    // we are calling the method on.
                    return baseTerm;
                } else if (methodName.equals("add")
                        || methodName.equals("addReverse")
                        || methodName.equals("subtract")
                        || methodName.equals("subtractReverse")
                        || methodName.equals("multiply")
                        || methodName.equals("multiplyReverse")
                        || methodName.equals("divide")
                        || methodName.equals("divideReverse")
                        || methodName.equals("modulo")
                        || methodName.equals("moduloReverse")
                        || methodName.equals("bitwiseAnd")
                        || methodName.equals("bitwiseOr")
                        || methodName.equals("bitwiseXor")) {
                    // The return value is greater than the base and
                    // the argument.
                    //  InequalityTerm returnValueTerm = new VariableTerm(
                    //                             PtolemyUtilities.getTokenTypeForSootType(
                    //                                     (RefType)r.getMethod().getReturnType()),
                    //                             r.getMethod());
                    final InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));
                    final InequalityTerm finalBaseTerm = baseTerm;
                    final InstanceInvokeExpr finalExpression = r;

                    //                     _addInequality(debug, solver, firstArgTerm,
                    //                             returnValueTerm);
                    //                     _addInequality(debug, solver, baseTerm,
                    //                             returnValueTerm);
                    InequalityTerm returnValueTerm = new MonotonicFunction() {
                        public Object getValue() throws IllegalActionException {
                            if (firstArgTerm.getValue().equals(
                                    TypeLattice.lattice().bottom())
                                    || finalBaseTerm.getValue().equals(
                                            TypeLattice.lattice().bottom())) {
                                return TypeLattice.lattice().bottom();
                            }

                            return TypeLattice.lattice().leastUpperBound(
                                    firstArgTerm.getValue(),
                                    finalBaseTerm.getValue());
                        }

                        public InequalityTerm[] getVariables() {
                            ArrayList list = new ArrayList();

                            if (firstArgTerm.isSettable()) {
                                list.add(firstArgTerm);
                            }

                            if (finalBaseTerm.isSettable()) {
                                list.add(finalBaseTerm);
                            }

                            InequalityTerm[] terms = (InequalityTerm[]) list
                                    .toArray(new InequalityTerm[list.size()]);
                            return terms;
                        }

                        public Object getAssociatedObject() {
                            return finalExpression;
                        }
                    };

                    return returnValueTerm;
                } else if (methodName.equals("convert")) {
                    System.out.println("convert method!");

                    // The return value type is equal to the base
                    // type.  The first argument type is less than or
                    // equal to the base type.
                    InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));
                    _addInequality(debug, solver, firstArgTerm, baseTerm);
                    return baseTerm;
                } else if (methodName.equals("getElement")
                        || methodName.equals("arrayValue")
                        || methodName.equals("getElementType")) {
                    // If we call getElement or arrayValue on an array
                    // token, then the returned type is the element
                    // type of the array.
                    ptolemy.data.type.ArrayType arrayType = new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                    _addInequality(debug, solver, baseTerm, new VariableTerm(
                            arrayType, r));

                    InequalityTerm returnTypeTerm = arrayType
                            .getElementTypeTerm();
                    return returnTypeTerm;
                } else if (methodName.equals("getElementAsToken")) {
                    final InequalityTerm matrixTerm = baseTerm;
                    InequalityTerm returnTypeTerm = new MonotonicFunction() {
                        public Object getValue() throws IllegalActionException {
                            if (matrixTerm.getValue() instanceof MatrixType) {
                                MatrixType type = (MatrixType) matrixTerm
                                        .getValue();
                                return type.getElementType();
                            } else {
                                return BaseType.UNKNOWN;
                            }
                        }

                        public InequalityTerm[] getVariables() {
                            if (matrixTerm.isSettable()) {
                                InequalityTerm[] terms = new InequalityTerm[1];
                                terms[0] = matrixTerm;
                                return terms;
                            } else {
                                return new InequalityTerm[0];
                            }
                        }
                    };

                    return returnTypeTerm;
                } else if (methodName.equals("absolute")) {
                    // Return the same as the input type, unless
                    // complex, in which case, return double.
                    final InequalityTerm finalBaseTerm = baseTerm;
                    final InstanceInvokeExpr finalExpression = r;
                    InequalityTerm returnValueTerm = new MonotonicFunction() {
                        public Object getValue() throws IllegalActionException {
                            if (finalBaseTerm.getValue().equals(
                                    BaseType.COMPLEX)) {
                                return BaseType.DOUBLE;
                            }
                            return finalBaseTerm.getValue();
                        }

                        public InequalityTerm[] getVariables() {
                            ArrayList list = new ArrayList();

                            if (finalBaseTerm.isSettable()) {
                                list.add(finalBaseTerm);
                            }

                            InequalityTerm[] terms = (InequalityTerm[]) list
                                    .toArray(new InequalityTerm[list.size()]);
                            return terms;
                        }

                        public Object getAssociatedObject() {
                            return finalExpression;
                        }
                    };

                    return returnValueTerm;
                }
            } else if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.componentPortClass)) {
                // If we are invoking a method on a port.
                TypedIOPort port = InlinePortTransformer.getPortValue(method,
                        (Local) r.getBase(), unit, localDefs, localUses);

                if (port == null) {
                    throw new RuntimeException("Failed to find port for "
                            + unit);
                }

                // Don't create constant terms for
                // ports where we don't already know the type.
                if (!port.getType().isInstantiable()) {
                    return null;
                }

                InequalityTerm portTypeTerm = new ConstantTerm(port.getType(),
                        port);

                if (methodName.equals("broadcast")) {
                    // The type of the argument must be less than the
                    // type of the port.
                    InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                            .get(r.getArg(0));

                    _addInequality(debug, solver, firstArgTerm, portTypeTerm);

                    // Return type is void.
                    return null;
                } else if (methodName.equals("get")) {
                    if (r.getArgCount() == 2) {
                        // FIXME: array of portTypeTerm?
                        return portTypeTerm;
                    } else if (r.getArgCount() == 1) {
                        return portTypeTerm;
                    }
                } else if (methodName.equals("send")) {
                    if (r.getArgCount() == 3) {
                        // The type of the argument must be less than the
                        // type of the port.
                        InequalityTerm secondArgTerm = (InequalityTerm) objectToInequalityTerm
                                .get(r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);

                        // Return type is void.
                        return null;
                    } else if (r.getArgCount() == 2) {
                        // The type of the argument must be less than the
                        // type of the port.
                        InequalityTerm secondArgTerm = (InequalityTerm) objectToInequalityTerm
                                .get(r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);

                        // Return type is void.
                        return null;
                    }
                }
            } else if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.attributeClass)) {
                // If we are invoking a method on a port.
                Attribute attribute = InlineParameterTransformer
                        .getAttributeValue(method, (Local) r.getBase(), unit,
                                localDefs, localUses);

                if (attribute == null) {
                    // A method invocation with a null base is bogus,
                    // so don't create a type constraint.
                    return null;
                }

                if (attribute instanceof Variable) {
                    Variable parameter = (Variable) attribute;
                    InequalityTerm parameterTypeTerm = new ConstantTerm(
                            parameter.getType(), parameter);

                    if (methodName.equals("setToken")) {
                        // The type of the argument must be less than the
                        // type of the parameter.
                        InequalityTerm firstArgTerm = (InequalityTerm) objectToInequalityTerm
                                .get(r.getArg(0));

                        _addInequality(debug, solver, firstArgTerm,
                                parameterTypeTerm);

                        // Return type is void.
                        return null;
                    } else if (methodName.equals("getToken")) {
                        // Return the type of the parameter.
                        return parameterTypeTerm;
                    }
                }
            }
        } else if (value instanceof ArrayRef) {
            // The type must be the same as the type of the
            // base of the array.
            return (InequalityTerm) objectToInequalityTerm
                    .get(((ArrayRef) value).getBase());

            // If we call getElement or arrayValue on an array
            // token, then the returned type is the element
            // type of the array.
            //             InequalityTerm baseTerm =
            //                 (InequalityTerm)objectToInequalityTerm.get(
            //                         ((ArrayRef)value).getBase());
            //             ptolemy.data.type.ArrayType arrayType =
            //                 new ptolemy.data.type.ArrayType(
            //                         ptolemy.data.type.BaseType.UNKNOWN);
            //             _addInequality(debug, solver, baseTerm,
            //                     new VariableTerm(arrayType, value));
            //             InequalityTerm returnTypeTerm = (InequalityTerm)
            //                 arrayType.getElementTypeTerm();
            //             return returnTypeTerm;
        } else if (value instanceof CastExpr) {
            CastExpr castExpr = (CastExpr) value;

            // The return type will be the type
            // of the cast.
            InequalityTerm baseTerm = (InequalityTerm) objectToInequalityTerm
                    .get(castExpr.getOp());
            return baseTerm;
        } else if (value instanceof NewExpr) {
            NewExpr newExpr = (NewExpr) value;
            RefType type = newExpr.getBaseType();
            SootClass castClass = type.getSootClass();

            // If we are creating a Token type...
            if (SootUtilities.derivesFrom(castClass,
                    PtolemyUtilities.tokenClass)) {
                InequalityTerm typeTerm = new ConstantTerm(PtolemyUtilities
                        .getTokenTypeForSootType(type), newExpr);

                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            } else {
                // Otherwise there is nothing to be done.
                return null;
            }
        } else if (value instanceof NewArrayExpr) {
            // Since arrays are aliasable, we must update their types.
            NewArrayExpr newExpr = (NewArrayExpr) value;
            Type type = newExpr.getBaseType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);

            if ((tokenType != null)
                    && (objectToInequalityTerm.get(newExpr) == null)) {
                InequalityTerm typeTerm = new VariableTerm(PtolemyUtilities
                        .getTokenTypeForSootType(tokenType), newExpr);

                // This is something we update, so put an entry
                // in the map used for updating
                objectToInequalityTerm.put(newExpr, typeTerm);

                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            }

            // Otherwise there is nothing to be done.
            return null;
        } else if (value instanceof NewMultiArrayExpr) {
            // Since arrays are aliasable, we must update their types.
            NewMultiArrayExpr newExpr = (NewMultiArrayExpr) value;
            Type type = newExpr.getBaseType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);

            if ((tokenType != null)
                    && (objectToInequalityTerm.get(newExpr) == null)) {
                InequalityTerm typeTerm = new VariableTerm(PtolemyUtilities
                        .getTokenTypeForSootType(tokenType), newExpr);

                // This is something we update, so put an entry
                // in the map used for updating
                objectToInequalityTerm.put(newExpr, typeTerm);

                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            }

            // Otherwise there is nothing to be done.
            return null;
        } else if (value instanceof FieldRef) {
            FieldRef r = (FieldRef) value;

            // Field references have the type of the field.
            SootField field = r.getField();

            // FIXME: UGH: This is the same as elementType...
            if (field.getSignature().equals(
                    "<ptolemy.data.ArrayToken: ptolemy.data.Token[] _value>")
                    || field
                            .getSignature()
                            .equals(
                                    "<ptolemy.data.ArrayToken: ptolemy.data.type.Type _elementType>")) {
                InequalityTerm baseTerm = (InequalityTerm) objectToInequalityTerm
                        .get(((InstanceFieldRef) r).getBase());
                ptolemy.data.type.ArrayType arrayType = new ptolemy.data.type.ArrayType(
                        ptolemy.data.type.BaseType.UNKNOWN);
                InequalityTerm variableTerm = new VariableTerm(arrayType, r);
                _addInequality(debug, solver, baseTerm, variableTerm);
                _addInequality(debug, solver, variableTerm, baseTerm);

                InequalityTerm returnTypeTerm = arrayType.getElementTypeTerm();
                return returnTypeTerm;
            } else if (field.equals(PtolemyUtilities.unknownTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.UNKNOWN, r);
            } else if (field.equals(PtolemyUtilities.booleanTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.BOOLEAN, r);
            } else if (field.equals(PtolemyUtilities.booleanMatrixTypeField)) {
                return new ConstantTerm(
                        ptolemy.data.type.BaseType.BOOLEAN_MATRIX, r);
            } else if (field.equals(PtolemyUtilities.byteTypeField)) {
                return new ConstantTerm(
                        ptolemy.data.type.BaseType.UNSIGNED_BYTE, r);
            } else if (field.equals(PtolemyUtilities.complexTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.COMPLEX, r);
            } else if (field.equals(PtolemyUtilities.complexMatrixTypeField)) {
                return new ConstantTerm(
                        ptolemy.data.type.BaseType.COMPLEX_MATRIX, r);
            } else if (field.equals(PtolemyUtilities.doubleTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.DOUBLE, r);
            } else if (field.equals(PtolemyUtilities.doubleMatrixTypeField)) {
                return new ConstantTerm(
                        ptolemy.data.type.BaseType.DOUBLE_MATRIX, r);
            } else if (field.equals(PtolemyUtilities.fixTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.FIX, r);
            } else if (field.equals(PtolemyUtilities.fixMatrixTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.FIX_MATRIX,
                        r);
            } else if (field.equals(PtolemyUtilities.floatTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.FLOAT, r);
            } else if (field.equals(PtolemyUtilities.intTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.INT, r);
            } else if (field.equals(PtolemyUtilities.intMatrixTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.INT_MATRIX,
                        r);
            } else if (field.equals(PtolemyUtilities.longTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.LONG, r);
            } else if (field.equals(PtolemyUtilities.longMatrixTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.LONG_MATRIX,
                        r);
            } else if (field.equals(PtolemyUtilities.objectTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.OBJECT, r);
            } else if (field.equals(PtolemyUtilities.shortTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.SHORT, r);
            } else if (field.equals(PtolemyUtilities.stringTypeField)) {
                return new ConstantTerm(ptolemy.data.type.BaseType.STRING, r);
            }

            return (InequalityTerm) objectToInequalityTerm.get(field);
        } else if (value instanceof Local) {
            // Local references have the type of the local.
            return (InequalityTerm) objectToInequalityTerm.get(value);
        } else if (value instanceof Constant) {
        } else if (value instanceof BinopExpr) {
        } else if (value instanceof UnopExpr) {
        } else if (value instanceof LengthExpr) {
        } else if (value instanceof InstanceOfExpr) {
        } else {
            throw new RuntimeException("found unknown value = " + value);
        }

        // do nothing.
        return null;
    }

    private static void _addInequality(boolean debug, InequalitySolver solver,
            InequalityTerm lesser, InequalityTerm greater) {
        if (debug) {
            System.out.println("lesser = " + lesser);
        }

        if (debug) {
            System.out.println("greater = " + greater);
        }

        if ((lesser != null) && (greater != null) && greater.isSettable()) {
            Inequality inequality = new Inequality(lesser, greater);

            if (debug) {
                System.out.println("adding inequality = " + inequality);
            }

            solver.addInequality(inequality);
        }
    }

    private static void _createInequalityTerm(boolean debug, Object object,
            Type type, Map objectToInequalityTerm) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if (debug) {
            System.out.println("_createInequalityTerm(): " + object + " type: "
                    + type + " tokenType: " + tokenType);
        }
        if (objectToInequalityTerm.get(object) != null) {
            return;
        }
        if (tokenType != null) {
            if (debug) {
                System.out.println("creating inequality term for " + object);
            }

            if (debug) {
                System.out.println("type " + type);
            }

            InequalityTerm term = new VariableTerm(PtolemyUtilities
                    .getTokenTypeForSootType(tokenType), object);
            objectToInequalityTerm.put(object, term);
            return;
        }

        RefType typeType = PtolemyUtilities.getBaseTokenTypeType(type);

        if (typeType != null) {
            if (debug) {
                System.out.println("creating inequality term for " + object);
            }

            if (debug) {
                System.out.println("type " + type);
            }

            InequalityTerm term = new VariableTerm(PtolemyUtilities
                    .getTokenTypeTypeForSootType(typeType), object);
            if (debug) {
                System.out.println("objectToInequality: typeType: "
                        + typeType
                        + " object: "
                        + object
                        + " term:"
                        + term
                        + " gttfst: "
                        + PtolemyUtilities
                                .getTokenTypeTypeForSootType(typeType));
            }
            objectToInequalityTerm.put(object, term);
            return;
        }

    }

    private void _printSolverVariables() {
        System.out.println("Type Assignment:");

        Iterator variables = _solver.variables();

        while (variables.hasNext()) {
            System.out
                    .println("InequalityTerm: " + variables.next().toString());
        }
    }

    private static class ConstantTerm implements InequalityTerm {
        public ConstantTerm(ptolemy.data.type.Type type, Object object) {
            _type = type;
            _object = object;
        }

        public void fixValue() {
        }

        public Object getValue() {
            return _type;
        }

        public Object getAssociatedObject() {
            return _object;
        }

        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        public void initialize(Object e) {
            setValue(e);
        }

        // Constant terms are not settable
        public boolean isSettable() {
            return false;
        }

        public boolean isValueAcceptable() {
            return true;
        }

        public void setValue(Object e) {
            _type = (ptolemy.data.type.Type) e;
        }

        public String toString() {
            return "{ConstantTerm: value = " + _type + ", associated object = "
                    + _object + "}";
        }

        public void unfixValue() {
        }

        private ptolemy.data.type.Type _type;

        private Object _object;
    }

    private static class VariableTerm implements InequalityTerm {
        public VariableTerm(ptolemy.data.type.Type type, Object object) {
            _declaredType = type;
            _currentType = type;
            _object = object;
        }

        public void fixValue() {
            //_fixed = true;
        }

        public Object getValue() {
            return _currentType;
        }

        public Object getAssociatedObject() {
            return _object;
        }

        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] result = new InequalityTerm[1];
                result[0] = this;
                return result;
            }

            return (new InequalityTerm[0]);
        }

        public void initialize(Object e) throws IllegalActionException {
            if (_declaredType == ptolemy.data.type.BaseType.UNKNOWN) {
                _currentType = (ptolemy.data.type.Type) e;
            } else {
                // _declaredType is a StructuredType
                ((ptolemy.data.type.StructuredType) _currentType)
                        .initialize((ptolemy.data.type.Type) e);
            }
        }

        // Variable terms are settable
        public boolean isSettable() {
            return (!_declaredType.isConstant());
        }

        public boolean isValueAcceptable() {
            return _currentType.isInstantiable();
        }

        public void setValue(Object e) throws IllegalActionException {
            //   System.out.println("setting value of " + toString() + " to " + e);
            ptolemy.data.type.Type newType = (ptolemy.data.type.Type) e;

            if (!_declaredType.isSubstitutionInstance(newType)) {
                _currentType = newType;
                return;

                //                 throw new RuntimeException("VariableTerm.setValue: "
                //                         + "Cannot update the type of " + this + " to the "
                //                         + "new type."
                //                         + ", Variable type: " + _declaredType.toString()
                //                         + ", New type: " + e.toString());
            }

            if (_declaredType == ptolemy.data.type.BaseType.UNKNOWN) {
                _currentType = newType; //((ptolemy.data.type.Type)e).clone();
            } else {
                // _declaredType is a StructuredType
                ((ptolemy.data.type.StructuredType) _currentType)
                        .updateType((ptolemy.data.type.StructuredType) e);
            }
        }

        public String toString() {
            return "{VariableTerm: value = " + _currentType + ", depth = "
                    + PtolemyUtilities.getTypeDepth(_currentType)
                    + ", associated object = " + _object + "}";
        }

        public void unfixValue() {
            //_fixed = false;
        }

        private ptolemy.data.type.Type _declaredType;

        private ptolemy.data.type.Type _currentType;

        private Object _object;

        //private boolean _fixed = false;
    }

    public static class JavaTypeLattice implements ptolemy.graph.CPO {
        /** Return the bottom element of this CPO.  The bottom element
         *  is the element in the CPO that is lower than all the other
         *  elements.
         *  @return An Object representing the bottom element, or
         *   <code>null</code> if the bottom does not exist.
         */
        public Object bottom() {
            return BaseType.UNKNOWN;
        }

        /** Compare two elements in this CPO.
         *  @param e1 An Object representing a CPO element.
         *  @param e2 An Object representing a CPO element.
         *  @return One of <code>CPO.LOWER, CPO.SAME,
         *   CPO.HIGHER, CPO.INCOMPARABLE</code>.
         *  @exception IllegalArgumentException If at least one of the
         *   specified Objects is not an element of this CPO.
         */
        public int compare(Object e1, Object e2) {
            if (e1.equals(e2)) {
                return SAME;
            } else {
                ptolemy.data.type.Type t1 = (ptolemy.data.type.Type) e1;
                ptolemy.data.type.Type t2 = (ptolemy.data.type.Type) e2;
                Class c1 = t1.getTokenClass();
                Class c2 = t2.getTokenClass();
                if (t1.equals(top()) || t2.equals(bottom())) {
                    return HIGHER;
                } else if (t2.equals(top()) || t1.equals(bottom())) {
                    return LOWER;
                } else if (c1.equals(c2)) {
                    return TypeLattice.lattice().compare(e1, e2);
                } else {
                    return INCOMPARABLE;
                }
            }
        }

        /** Compute the down-set of an element in this CPO.
         *  The down-set of an element is the subset consisting of
         *  all the elements lower than or the same as the specified element.
         *  @param e An Object representing an element in this CPO.
         *  @return An array of Objects representing the elements in the
         *   down-set of the specified element.
         *  @exception IllegalArgumentException If the specified Object is not
         *   an element in this CPO, or the resulting set is infinite.
         */
        public Object[] downSet(Object e) {
            throw new RuntimeException("not supported");
        }

        /** Compute the greatest element of a subset.
         *  The greatest element of a subset is an element in the
         *  subset that is higher than all the other elements in the
         *  subset.
         *  @param subset An array of Objects representing the subset.
         *  @return An Object representing the greatest element of the subset,
         *   or <code>null</code> if the greatest element does not exist.
         *  @exception IllegalArgumentException If at least one Object in the
         *   specified array is not an element of this CPO.
         */
        public Object greatestElement(Object[] subset) {
            // Compare each element with all of the other elements to search
            // for the greatest one. This is a simple, brute force algorithm,
            // but may be inefficient. A more efficient one is used in
            // the graph package, but more complex.
            for (int i = 0; i < subset.length; i++) {
                boolean isGreatest = true;

                for (int j = 0; j < subset.length; j++) {
                    int result = compare(subset[i], subset[j]);

                    if ((result == LOWER) || (result == INCOMPARABLE)) {
                        isGreatest = false;
                        break;
                    }
                }

                if (isGreatest == true) {
                    return subset[i];
                }
            }

            return null;
        }

        /** Compute the greatest lower bound (GLB) of two elements.
         *  The GLB of two elements is the greatest element in the CPO
         *  that is lower than or the same as both of the two elements.
         *  @param e1 An Object representing an element in this CPO.
         *  @param e2 An Object representing an element in this CPO.
         *  @return An Object representing the GLB of the two specified
         *   elements, or <code>null</code> if the GLB does not exist.
         *  @exception IllegalArgumentException If at least one of the
         *   specified Objects is not an element of this CPO.
         */
        public Object greatestLowerBound(Object e1, Object e2) {
            if (e1.equals(e2)) {
                return e1;
            } else {
                ptolemy.data.type.Type t1 = (ptolemy.data.type.Type) e1;
                ptolemy.data.type.Type t2 = (ptolemy.data.type.Type) e2;
                Class c1 = t1.getTokenClass();
                Class c2 = t2.getTokenClass();
                if (t1.equals(bottom()) || t2.equals(bottom())) {
                    return bottom();
                } else if (t1.equals(top())) {
                    return t2;
                } else if (t2.equals(top())) {
                    return t1;
                } else if (c1.equals(c2)) {
                    return TypeLattice.lattice().greatestLowerBound(e1, e2);
                } else {
                    return bottom();
                }
            }
        }

        /** Compute the greatest lower bound (GLB) of a subset.
         *  The GLB of a subset is the greatest element in the CPO that
         *  is lower than or the same as all the elements in the
         *  subset.
         *  @param subset An array of Objects representing the subset.
         *  @return An Object representing the GLB of the subset, or
         *   <code>null</code> if the GLB does not exist.
         *  @exception IllegalArgumentException If at least one Object
         *   in the specified array is not an element of this CPO.
         */
        public Object greatestLowerBound(Object[] subset) {
            Object returnValue = null;

            for (int i = 0; i < subset.length; i++) {
                if (returnValue == null) {
                    returnValue = subset[i];
                } else {
                    returnValue = greatestLowerBound(returnValue, subset[i]);
                }
            }

            return returnValue;
        }

        /** Return true.
         *  @return true.
         */
        public boolean isLattice() {
            return true;
        }

        /** Return the least type of a set of types, or null if the
         *  least one does not exist.
         *  @param subset an array of Types.
         *  @return A Type or null.
         */
        public Object leastElement(Object[] subset) {
            // Compare each element with all of the other elements to search
            // for the least one. This is a simple, brute force algorithm,
            // but may be inefficient. A more efficient one is used in
            // the graph package, but more complex.
            for (int i = 0; i < subset.length; i++) {
                boolean isLeast = true;

                for (int j = 0; j < subset.length; j++) {
                    int result = compare(subset[i], subset[j]);

                    if ((result == HIGHER) || (result == INCOMPARABLE)) {
                        isLeast = false;
                        break;
                    }
                }

                if (isLeast == true) {
                    return subset[i];
                }
            }

            return null;
        }

        /** Compute the least upper bound (LUB) of two elements.
         *  The LUB of two elements is the least element in the CPO
         *  that is greater than or the same as both of the two elements.
         *  @param e1 An Object representing an element in this CPO.
         *  @param e2 An Object representing an element in this CPO.
         *  @return An Object representing the LUB of the two specified
         *   elements, or <code>null</code> if the LUB does not exist.
         *  @exception IllegalArgumentException If at least one of the
         *   specified Objects is not an element of this CPO.
         */
        public Object leastUpperBound(Object e1, Object e2) {
            // System.out.println("e1 = " + e1 + " e2 = " + e2);
            Object retVal = null;
            if (e1.equals(e2)) {
                retVal = e1;
            } else {
                ptolemy.data.type.Type t1 = (ptolemy.data.type.Type) e1;
                ptolemy.data.type.Type t2 = (ptolemy.data.type.Type) e2;
                Class c1 = t1.getTokenClass();
                Class c2 = t2.getTokenClass();
                if (t1.equals(top()) || t2.equals(top())) {
                    return top();
                } else if (t1.equals(bottom())) {
                    return t2;
                } else if (t2.equals(bottom())) {
                    return t1;
                } else if (c1.equals(c2)) {
                    retVal = TypeLattice.lattice().leastUpperBound(e1, e2);
                } else {
                    retVal = top();
                }
            }
            // System.out.println("return = " + retVal);
            return retVal;
        }

        /** Compute the least upper bound (LUB) of a subset.
         *  The LUB of a subset is the least element in the CPO that
         *  is greater than or the same as all the elements in the
         *  subset.
         *  @param subset An array of Objects representing the subset.
         *  @return An Object representing the LUB of the subset, or
         *   <code>null</code> if the LUB does not exist.
         *  @exception IllegalArgumentException If at least one Object
         *   in the specified array is not an element of this CPO.
         */
        public Object leastUpperBound(Object[] subset) {
            Object returnValue = null;

            for (int i = 0; i < subset.length; i++) {
                if (returnValue == null) {
                    returnValue = subset[i];
                } else {
                    returnValue = leastUpperBound(returnValue, subset[i]);
                }
            }

            return returnValue;
        }

        /** Return the top element of this CPO.
         *  The top element is the element in the CPO that is higher than
         *  all the other elements.
         *  @return An Object representing the top element, or
         *   <code>null</code> if the top does not exist.
         */
        public Object top() {
            return TypeLattice.lattice().top();
        }

        /** Compute the up-set of an element in this CPO.
         *  The up-set of an element is the subset consisting of
         *  all the elements higher than or the same as the specified element.
         *  @param e An Object representing an element in this CPO.
         *  @return An array of Objects representing the elements in the
         *   up-set of the specified element.
         *  @exception IllegalArgumentException If the specified Object is not
         *   an element of this CPO, or the resulting set is infinite.
         */
        public Object[] upSet(Object e) {
            throw new RuntimeException("not supported");
        }
    }

    private InequalitySolver _solver;

    private Map _objectToInequalityTerm;

    private Set _unsafeLocals;

    private boolean _debug = false;
}
