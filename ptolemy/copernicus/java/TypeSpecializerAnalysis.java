/* A transformer that specializes token types in an actor.

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
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.UnsizedMatrixType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalitySolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
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
*/
public class TypeSpecializerAnalysis {

    /** Specialize all token types that appear in the given class.
     *  Return a map from locals and fields in the class to their new
     *  specific Ptolemy type.  Exclude locals in the given set from
     *  the typing algorithm.
     */
    public TypeSpecializerAnalysis(SootClass theClass, Set unsafeLocals) {
        // System.out.println("Starting type specialization");
        _unsafeLocals = unsafeLocals;

        _solver = new InequalitySolver(TypeLattice.lattice());
        _objectToInequalityTerm = new HashMap();

        _collectConstraints(theClass, _debug);

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
            if (_debug) System.out.println("solution FOUND!");
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
     *  class.  Return a map from locals and fields in the class to
     *  their new specific Ptolemy type.  Exclude locals in the given
     *  set from the typing algorithm.
     *  @param list A list of SootClass.
     */
    public TypeSpecializerAnalysis(List list, Set unsafeLocals) {
        //  _debug = true;
        //        System.out.println("Starting type specialization list");
        _unsafeLocals = unsafeLocals;

        _solver = new InequalitySolver(TypeLattice.lattice());
        _objectToInequalityTerm = new HashMap();

        for (Iterator classes = list.iterator();
             classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
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
            if (_debug) System.out.println("solution FOUND!");
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

    public Type getSpecializedSootType(Local local) {
        try {
            Type type = local.getType();
            Type type2 = _getUpdateType(local, type);
            if(type2 == null) {
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
            if(type2 == null) {
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
            if(type2 == null) {
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
    public void inlineTypeLatticeMethods(SootMethod method,
            Unit unit, ValueBox box, StaticInvokeExpr expr,
            LocalDefs localDefs, LocalUses localUses)
            throws IllegalActionException {
        SootMethod tokenTokenCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.Token,ptolemy.data.Token)");
        SootMethod tokenTypeCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.Token,ptolemy.data.type.Type)");
        SootMethod typeTokenCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.type.Type,ptolemy.data.Token)");
        SootMethod typeTypeCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.type.Type,ptolemy.data.type.Type)");
        SootMethod leastUpperBoundMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "ptolemy.data.type.Type leastUpperBound(ptolemy.data.type.Type,ptolemy.data.type.Type)");

        ptolemy.data.type.Type type1;
        ptolemy.data.type.Type type2;
        if (expr.getMethod().equals(tokenTokenCompareMethod)) {
            Local tokenLocal1 = (Local)expr.getArg(0);
            Local tokenLocal2 = (Local)expr.getArg(1);
            type1 = getSpecializedType(tokenLocal1);
            type2 = getSpecializedType(tokenLocal2);
        } else if (expr.getMethod().equals(typeTokenCompareMethod)) {
            Local typeLocal = (Local)expr.getArg(0);
            Local tokenLocal = (Local)expr.getArg(1);
            type1 = PtolemyUtilities.getTypeValue(
                    method, typeLocal, unit, localDefs, localUses);
            type2 = getSpecializedType(tokenLocal);
        } else if (expr.getMethod().equals(tokenTypeCompareMethod)) {
            Local tokenLocal = (Local)expr.getArg(0);
            Local typeLocal = (Local)expr.getArg(1);
            type1 = getSpecializedType(tokenLocal);
            type2 = PtolemyUtilities.getTypeValue(
                    method, typeLocal, unit, localDefs, localUses);
        } else if (expr.getMethod().equals(typeTypeCompareMethod)) {
            Local typeLocal1 = (Local)expr.getArg(0);
            Local typeLocal2 = (Local)expr.getArg(1);
            type1 = PtolemyUtilities.getTypeValue(
                    method, typeLocal1, unit, localDefs, localUses);
            type2 = PtolemyUtilities.getTypeValue(
                    method, typeLocal2, unit, localDefs, localUses);
        } else if (expr.getMethod().equals(leastUpperBoundMethod)) {
            System.out.println("Found LUB method!");
            Local typeLocal1 = (Local)expr.getArg(0);
            Local typeLocal2 = (Local)expr.getArg(1);
            type1 = PtolemyUtilities.getTypeValue(
                    method, typeLocal1, unit, localDefs, localUses);
            type2 = PtolemyUtilities.getTypeValue(
                    method, typeLocal2, unit, localDefs, localUses);
            Local newTypeLocal = PtolemyUtilities.buildConstantTypeLocal(
                    method.getActiveBody(), unit,
                    TypeLattice.leastUpperBound(type1, type2));
            box.setValue(newTypeLocal);
            return;
        } else {
            throw new RuntimeException(
                    "attempt to inline unhandled typeLattice method: " + unit);
        }
      //   System.out.println("specializer");
//         System.out.println("type1 = " + type1);
//         System.out.println("type2 = " + type2);
//         System.out.println("result = " + TypeLattice.compare(type1, type2));
        // Only inline if both are concrete types.
        if(type1.isInstantiable() && type2.isInstantiable()) {
            box.setValue(IntConstant.v(TypeLattice.compare(type1, type2)));
        }
    }

    private void _collectConstraints(SootClass entityClass, boolean debug) {
        if (debug) System.out.println("collecting constraints for " + entityClass);
        // System.out.println("collecting constraints for " + entityClass);
        // Loop through all the fields.
        for (Iterator fields = entityClass.getFields().iterator();
             fields.hasNext();) {
            SootField field = (SootField)fields.next();
            // Ignore things that aren't reference types.
            Type type = field.getType();
            _createInequalityTerm(debug, field, type, _objectToInequalityTerm);

            // If the field has been tagged with a more specific type, then
            // constrain the type more.
            TypeTag tag = (TypeTag)field.getTag("_CGType");
            if (tag != null) {
                _addInequality(debug, _solver,
                        new ConstantTerm(tag.getType(), field),
                        (InequalityTerm)_objectToInequalityTerm.get(field));
            }
        }

        // FIXME: we also need the fields that we represent from
        //
        for (Iterator fields = ModelTransformer.getModelClass().getFields().iterator();
             fields.hasNext();) {
            SootField field = (SootField)fields.next();
            // Ignore things that aren't reference types.
            Type type = field.getType();
            _createInequalityTerm(debug, field, type, _objectToInequalityTerm);

            // If the field has been tagged with a more specific type, then
            // constrain the type more.
            TypeTag tag = (TypeTag)field.getTag("_CGType");
            if (tag != null) {
                _addInequality(debug, _solver,
                        new ConstantTerm(tag.getType(), field),
                        (InequalityTerm)_objectToInequalityTerm.get(field));
            }
        }

        // Loop through all the methods.
        for (Iterator methods = entityClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            Body body = method.retrieveActiveBody();
            if (debug) System.out.println("collecting constraints for " + method);

            CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
            // this will help us figure out where locals are defined.
            SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
            SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
            //   System.out.println("done computing aliases for " + method);

            for (Iterator locals = body.getLocals().iterator();
                 locals.hasNext();) {
                Local local = (Local)locals.next();
                if (_unsafeLocals.contains(local)) {
                    continue;
                }
                // Ignore things that aren't reference types.
                Type type = local.getType();
                _createInequalityTerm(debug, local,
                        type, _objectToInequalityTerm);
            }
            for (Iterator units = body.getUnits().iterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (debug) System.out.println("stmt = " + stmt);
                if (stmt instanceof AssignStmt) {
                    Value leftOp = ((AssignStmt)stmt).getLeftOp();
                    Value rightOp = ((AssignStmt)stmt).getRightOp();

                    // Note that the only real possibilities on the
                    // left side are a local or a fieldRef.
                    InequalityTerm leftOpTerm =
                        _getInequalityTerm(method, debug, leftOp,
                                _solver, _objectToInequalityTerm, stmt,
                                localDefs, localUses);

                    InequalityTerm rightOpTerm =
                        _getInequalityTerm(method, debug, rightOp,
                                _solver, _objectToInequalityTerm, stmt,
                                localDefs, localUses);

                    // The type of all aliases of the left hand side
                    // must always be greater than
                    // the type of all aliases of the right hand side.
                    _addInequality(debug, _solver, rightOpTerm,
                            leftOpTerm);

                    // If an alias is created by this instruction, then the
                    // left and right hand sides must actually be equal.
                    // FIXME: Alternatively, we could create individual constraints for
                    // all of the different aliases.  This might be better
                    // given that we actually have alias information.
                    if (SootUtilities.isAliasableValue(leftOp) &&
                            (SootUtilities.isAliasableValue(rightOp) ||
                                    rightOp instanceof NewArrayExpr)) {
                        _addInequality(debug, _solver, leftOpTerm, rightOpTerm);
                    }
                } else if (stmt instanceof InvokeStmt) {
                    // Still call getInequalityTerm because there may
                    // be side effects that cause type constraints.
                    _getInequalityTerm(method, debug,
                            ((InvokeStmt)stmt).getInvokeExpr(),
                            _solver, _objectToInequalityTerm, stmt,
                            localDefs, localUses);
                }
            }
        }
        // System.out.println("done collecting constraints for " + entityClass);
    }

    // Given an object (which must be either a local, or a field) of
    // the given type, look into the given map and retrieve the
    // inequality term for the object.  retrieve the resolved type,
    // and return it.
    private static Type _getUpdateType(boolean debug,
            Object object, Type type, Map objectToInequalityTerm) throws IllegalActionException {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if (tokenType != null) {
            if (debug) System.out.println("type of value " + object + " = " + type);
            InequalityTerm term = (InequalityTerm)objectToInequalityTerm.get(object);
            if (term == null) {
                return null;
            }
            ptolemy.data.type.Type newTokenType = (ptolemy.data.type.Type)term.getValue();
            RefType newType = PtolemyUtilities.getSootTypeForTokenType(newTokenType);
            if (debug) System.out.println("newType = " + newType);
            if (!SootUtilities.derivesFrom(newType.getSootClass(), tokenType.getSootClass())) {
                // If the new Type is less specific, in Java terms, than what we
                // had before, then the resulting code is likely not correct.
                // FIXME: hack to get around the bogus type lattice.  This should be an exception.
                System.out.println("Warning! Resolved type of " + object +
                        " to " + newType + " which is more general than the old type " + type);
                newType = tokenType;
            }

            // create a new type isomorphic with the old type.
            return SootUtilities.createIsomorphicType(type, newType);
        }
        // If this is not a token class, then we don't change it.
        return null;
    }

    // Given an object (which must be either a local, or a field) of
    // the given type, look into the given map and retrieve the
    // inequality term for the object.  retrieve the resolved type,
    // and return it.
    private Type _getUpdateType(Object object, Type type) throws IllegalActionException {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if (tokenType != null) {
            if (_debug) System.out.println("type of value " +
                    object + " = " + type);
            InequalityTerm term = (InequalityTerm)
                _objectToInequalityTerm.get(object);
            if (term == null) {
                return null;
            }
            ptolemy.data.type.Type newTokenType =
                (ptolemy.data.type.Type)term.getValue();
            RefType newType = PtolemyUtilities.getSootTypeForTokenType(
                    newTokenType);
            if (_debug) System.out.println("newType = " + newType);
            if (!SootUtilities.derivesFrom(newType.getSootClass(),
                    tokenType.getSootClass())) {
                // If the new Type is less specific, in Java terms,
                // than what we had before, then the resulting code is
                // likely not correct.  FIXME: hack to get around the
                // bogus type lattice.  This should be an exception.
                System.out.println("Warning! Resolved type of " + object +
                        " to " + newType +
                        " which is more general than the old type " + type);
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
        InequalityTerm term = (InequalityTerm)
            _objectToInequalityTerm.get(object);
        if (term == null) {
            throw new RuntimeException("Attempt to get type for object "
                    + object + " with no inequality term!");
        }
        return (ptolemy.data.type.Type)term.getValue();
    }

    public static InequalityTerm _getInequalityTerm(
            SootMethod method, boolean debug,
            Value value, InequalitySolver solver,
            Map objectToInequalityTerm,
            Unit unit, LocalDefs localDefs, LocalUses localUses) {
        if (value instanceof StaticInvokeExpr) {
            StaticInvokeExpr r = (StaticInvokeExpr)value;
            if (r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                // If we are copying one array to another, then the
                // types must be equal.
                InequalityTerm firstArgTerm = (InequalityTerm)
                    objectToInequalityTerm.get(r.getArg(0));
                InequalityTerm thirdArgTerm = (InequalityTerm)
                    objectToInequalityTerm.get(r.getArg(2));
                _addInequality(debug, solver, firstArgTerm,
                        thirdArgTerm);
                _addInequality(debug, solver, thirdArgTerm,
                        firstArgTerm);
                return null;
            }
        } else if (value instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
            String methodName = r.getMethod().getName();
            // If we are invoking on something that is not a reference type,
            // then ignore it.
            if (!(r.getBase().getType() instanceof RefType)) {
                return null;
            }
            //     System.out.println("invokeExpr = " + r);
            SootClass baseClass = ((RefType)r.getBase().getType()).getSootClass();
            InequalityTerm baseTerm =
                (InequalityTerm)objectToInequalityTerm.get(r.getBase());
            // FIXME: match better.
            // If we are invoking a method on a token, then...
            if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.tokenClass)) {
                if (methodName.equals("one") ||
                        methodName.equals("zero") ||
                        methodName.equals("bitwiseNot") ||
                        methodName.equals("pow") ||
                        methodName.equals("logicalRightShift") ||
                        methodName.equals("leftShift") ||
                        methodName.equals("rightShit")) {
                    // The returned type must be equal to the type
                    // we are calling the method on.
                    return baseTerm;
                } else if (methodName.equals("add") ||
                        methodName.equals("addReverse") ||
                        methodName.equals("subtract") ||
                        methodName.equals("subtractReverse") ||
                        methodName.equals("multiply") ||
                        methodName.equals("multiplyReverse") ||
                        methodName.equals("divide") ||
                        methodName.equals("divideReverse") ||
                        methodName.equals("modulo") ||
                        methodName.equals("moduloReverse") ||
                        methodName.equals("bitwiseAnd") ||
                        methodName.equals("bitwiseOr") ||
                        methodName.equals("bitwiseXor")) {
                    // The return value is greater than the base and
                    // the argument.
                    InequalityTerm returnValueTerm = new VariableTerm(
                            PtolemyUtilities.getTokenTypeForSootType(
                                    (RefType)r.getMethod().getReturnType()),
                            r.getMethod());
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));
                    _addInequality(debug, solver, firstArgTerm,
                            returnValueTerm);
                    _addInequality(debug, solver, baseTerm,
                            returnValueTerm);
                    return returnValueTerm;
                } else if (methodName.equals("convert")) {
                    // The return value type is equal to the base type.
                    // The first argument type is less than or equal to the base type.
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));
                    _addInequality(debug, solver, firstArgTerm,
                            baseTerm);
                    return baseTerm;
                } else if (methodName.equals("getElement") ||
                        methodName.equals("arrayValue")) {
                    // If we call getElement or arrayValue on an array
                    // token, then the returned type is the element
                    // type of the array.
                    ptolemy.data.type.ArrayType arrayType =
                        new ptolemy.data.type.ArrayType(
                                ptolemy.data.type.BaseType.UNKNOWN);
                    _addInequality(debug, solver, baseTerm,
                            new VariableTerm(arrayType, r));
                    InequalityTerm returnTypeTerm = (InequalityTerm)
                        arrayType.getElementTypeTerm();
                    return returnTypeTerm;
                } else if (methodName.equals("getElementAsToken")) {
                    final InequalityTerm matrixTerm = baseTerm;
                    InequalityTerm returnTypeTerm = (InequalityTerm)
                        new MonotonicFunction() {
                            public Object getValue() throws IllegalActionException {
                                if(matrixTerm.getValue() instanceof UnsizedMatrixType) {
                                    UnsizedMatrixType type = (UnsizedMatrixType) matrixTerm.getValue();
                                    return type.getElementType();
                                } else {
                                    return BaseType.UNKNOWN;
                                }
                            }
                            public InequalityTerm[] getVariables() {
                                if(matrixTerm.isSettable()) {
                                    InequalityTerm[] terms = 
                                        new InequalityTerm[1];
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
                    // complex, in which case, return double.  FIXME:
                    // This is WRONG for complex.  need a monotonic
                    // function.
                    return baseTerm;
                }
            } else if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.componentPortClass)) {
                // If we are invoking a method on a port.
                TypedIOPort port = (TypedIOPort)
                    InlinePortTransformer.getPortValue(
                            method,
                            (Local)r.getBase(),
                            unit,
                            localDefs,
                            localUses);
                if (port == null) {
                    throw new RuntimeException("Failed to find port for " +
                            unit);
                }
                // Don't create constant terms for
                // ports where we don't already know the type.
                if (!port.getType().isInstantiable()) {
                    return null;
                }
                InequalityTerm portTypeTerm =
                    new ConstantTerm(port.getType(),
                            port);
                if (methodName.equals("broadcast")) {
                    // The type of the argument must be less than the
                    // type of the port.
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));

                    _addInequality(debug, solver,firstArgTerm,
                            portTypeTerm);
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
                        InequalityTerm secondArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);
                        // Return type is void.
                        return null;
                    } else if (r.getArgCount() == 2) {
                        // The type of the argument must be less than the
                        // type of the port.
                        InequalityTerm secondArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);
                        // Return type is void.
                        return null;
                    }
                }
            } else if (SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.attributeClass)) {
                // If we are invoking a method on a port.
                Attribute attribute = (Attribute)
                    InlineParameterTransformer.getAttributeValue(
                            method,
                            (Local)r.getBase(),
                            unit,
                            localDefs,
                            localUses);
                if (attribute == null) {
                    // A method invocation with a null base is bogus,
                    // so don't create a type constraint.
                    return null;
                }
                if (attribute instanceof Variable) {
                    Variable parameter = (Variable)attribute;
                    InequalityTerm parameterTypeTerm =
                        new ConstantTerm(parameter.getType(),
                                parameter);
                    if (methodName.equals("setToken")) {
                        // The type of the argument must be less than the
                        // type of the parameter.
                        InequalityTerm firstArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(0));

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
            return (InequalityTerm)objectToInequalityTerm.get(
                    ((ArrayRef)value).getBase());
        } else if (value instanceof CastExpr) {
            /* CastExpr castExpr = (CastExpr)value;
            Type type = castExpr.getType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
            if (tokenType != null) {
                // The type of the argument must be greater than the
                // type of the cast.  The return type will be the type
                // of the cast.
                InequalityTerm baseTerm = (InequalityTerm)objectToInequalityTerm.get(
                        castExpr.getOp());
                InequalityTerm typeTerm = new ConstantTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        tokenType);
                //System.out.println("baseTerm = " + baseTerm);
                //System.out.println("typeTerm = " + typeTerm);
                // _addInequality(debug, solver, typeTerm, baseTerm);
                return baseTerm;
            } else {
                // Otherwise there is nothing to be done.
                return null;
               }*/
            return null; // Since this is not aware of flow, casts can
                         // have no information.
        } else if (value instanceof NewExpr) {
            NewExpr newExpr = (NewExpr)value;
            RefType type = newExpr.getBaseType();
            SootClass castClass = type.getSootClass();
            // If we are creating a Token type...
            if (SootUtilities.derivesFrom(castClass,
                    PtolemyUtilities.tokenClass)) {
                InequalityTerm typeTerm = new ConstantTerm(
                        PtolemyUtilities.getTokenTypeForSootType(type),
                        newExpr);
                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            } else {
                // Otherwise there is nothing to be done.
                return null;
            }
        } else if (value instanceof NewArrayExpr) {
            // Since arrays are aliasable, we must update their types.
            NewArrayExpr newExpr = (NewArrayExpr)value;
            Type type = newExpr.getBaseType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
            if (tokenType != null && objectToInequalityTerm.get(newExpr) == null) {
                InequalityTerm typeTerm = new VariableTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        newExpr);
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
            FieldRef r = (FieldRef)value;
            // Field references have the type of the field.
            SootField field = r.getField();

            // FIXME: UGH: This is the same as elementType...
            if (field.getSignature().equals("<ptolemy.data.ArrayToken: ptolemy.data.Token[] _value>")) {
                InequalityTerm baseTerm =
                    (InequalityTerm)objectToInequalityTerm.get(((InstanceFieldRef)r).getBase());
                ptolemy.data.type.ArrayType arrayType =
                    new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                _addInequality(debug, solver, baseTerm,
                        new VariableTerm(arrayType, r));
                InequalityTerm returnTypeTerm = (InequalityTerm)
                    arrayType.getElementTypeTerm();
                return returnTypeTerm;
            }
            return (InequalityTerm)objectToInequalityTerm.get(field);
        } else if (value instanceof Local) {
            // Local references have the type of the local.
            return (InequalityTerm)objectToInequalityTerm.get(value);
        }
        // do nothing.
        return null;
    }

    private static void _addInequality(boolean debug,
            InequalitySolver solver, InequalityTerm lesser, InequalityTerm greater) {
        if (lesser != null && greater != null && greater.isSettable()) {
            Inequality inequality = new Inequality(lesser, greater);
            if (debug) System.out.println("adding inequality = " + inequality);
            solver.addInequality(inequality);
        }
    }

    private static void _createInequalityTerm(boolean debug, Object object, Type type,
            Map objectToInequalityTerm) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if (tokenType != null && objectToInequalityTerm.get(object) == null) {
            if (debug) System.out.println("creating inequality term for " + object);
            if (debug) System.out.println("type " + type);

            InequalityTerm term = new VariableTerm(
                    PtolemyUtilities.getTokenTypeForSootType(tokenType),
                    object);
            objectToInequalityTerm.put(object, term);
        }
    }

    private void _printSolverVariables() {
        System.out.println("Type Assignment:");
        Iterator variables = _solver.variables();
        while (variables.hasNext()) {
            System.out.println("InequalityTerm: "
                    + variables.next().toString());
        }
    }

    private static class ConstantTerm implements InequalityTerm {
        public ConstantTerm(ptolemy.data.type.Type type, Object object) {
            _type = type;
            _object = object;
        }

        public void fixValue() { }

        public Object getValue() { return _type; }

        public Object getAssociatedObject() { return _object; }

        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        public void initialize(Object e) {
            setValue(e);
        }

        // Constant terms are not settable
        public boolean isSettable() { return false; }

        public boolean isValueAcceptable() {
            return true;
        }

        public void setValue(Object e) {
            _type = (ptolemy.data.type.Type)e;
        }

        public String toString() {
            return "{ConstantTerm: value = " + _type + ", associated object = " +
                _object + "}";
        }

        public void unfixValue() { }

        private ptolemy.data.type.Type _type;
        private Object _object;
    }

    private static class VariableTerm implements InequalityTerm {
        public VariableTerm(ptolemy.data.type.Type type, Object object) {
            _declaredType = type;
            _currentType = type;
            _object = object;
        }

        public void fixValue() { _fixed = true; }

        public Object getValue() { return _currentType; }

        public Object getAssociatedObject() { return _object; }

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
                _currentType = (ptolemy.data.type.Type)e;
            } else {
                // _declaredType is a StructuredType
                ((ptolemy.data.type.StructuredType)_currentType).initialize((ptolemy.data.type.Type) e);
            }
        }

        // Variable terms are settable
        public boolean isSettable() { return ( !_declaredType.isConstant()); }

        public boolean isValueAcceptable() {
            return  _currentType.isInstantiable();
        }

        public void setValue(Object e) throws IllegalActionException {
            //   System.out.println("setting value of " + toString() + " to " + e);
            if (!_declaredType.isSubstitutionInstance((ptolemy.data.type.Type)e)) {
                throw new RuntimeException("VariableTerm.setValue: "
                        + "Cannot update the type of " + this + " to the "
                        + "new type."
                        + ", Variable type: " + _declaredType.toString()
                        + ", New type: " + e.toString());
            }

            if (_declaredType == ptolemy.data.type.BaseType.UNKNOWN) {
                _currentType = (ptolemy.data.type.Type)e;//((ptolemy.data.type.Type)e).clone();
            } else {
                // _declaredType is a StructuredType
                ((ptolemy.data.type.StructuredType)_currentType).updateType((ptolemy.data.type.StructuredType)e);
            }
        }

        public String toString() {
            return "{VariableTerm: value = " + _currentType + ", depth = " +
                PtolemyUtilities.getTypeDepth(_currentType) + ", associated object = " +
                _object + "}";
        }

        public void unfixValue() { _fixed = false; }

        private ptolemy.data.type.Type _declaredType;
        private ptolemy.data.type.Type _currentType;
        private Object _object;
        private boolean _fixed = false;
    }

    private InequalitySolver _solver;
    private Map _objectToInequalityTerm;
    private Set _unsafeLocals;
    private boolean _debug = false;
}














