/* Ptolemy-specific utilities to use with Soot

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

package ptolemy.copernicus.kernel;

import soot.ArrayType;
import soot.BaseType;
import soot.Body;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.NullType;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;

import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.StringConstant;
import soot.jimple.DoubleConstant;
import soot.jimple.NullConstant;
import soot.jimple.Constant;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.MonitorStmt;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.SynchronizerManager;
import soot.jimple.toolkits.scalar.Evaluator;

import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;

import soot.util.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.lang.reflect.Method;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.expr.Variable;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.copernicus.kernel.SootUtilities;

/*
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
*/
//////////////////////////////////////////////////////////////////////////
//// PtolemyUtilities
/**
This class consists of ptolemy-specific static utility methods for use
with Soot.

@author Stephen Neuendorffer
@version $Id$
*/
public class PtolemyUtilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Create a new Local variable in the given body with the given name.
     *  Insert statements into the given body before the given unit to
     *  initialize the value of that local to a token that has
     *  the same value as the given token.
     *  @return The new local.
     */
    public static Local buildConstantTokenLocal(Body body,
            Unit insertPoint, Token token, String localName) {
        Chain units = body.getUnits();
        if(token instanceof ptolemy.data.ArrayToken) {
            ArrayToken arrayToken = (ArrayToken)token;
            RefType tokenType =
                getSootTypeForTokenType(arrayToken.getElementType());
            Type tokenArrayType = ArrayType.v(tokenType, 1);
            Local tokenArrayLocal = Jimple.v().newLocal(localName + "Array",
                    tokenArrayType);
            body.getLocals().add(tokenArrayLocal);
            // Create the array of tokens.
            units.insertBefore(Jimple.v().newAssignStmt(tokenArrayLocal,
                    Jimple.v().newNewArrayExpr(tokenType,
                            IntConstant.v(arrayToken.length()))),
                    insertPoint);
            // recurse
            for(int i = 0; i < arrayToken.length(); i++) {
                Local argLocal = buildConstantTokenLocal(body, insertPoint,
                       arrayToken.getElement(i), localName + "_" + i);
                units.insertBefore(
                        Jimple.v().newAssignStmt(
                                Jimple.v().newArrayRef(tokenArrayLocal,
                                        IntConstant.v(i)),
                                argLocal),
                        insertPoint);
            }
            Local tokenLocal = Jimple.v().newLocal(localName,
                    RefType.v(arrayTokenClass));
            body.getLocals().add(tokenLocal);
            units.insertBefore(Jimple.v().newAssignStmt(tokenLocal,
                    Jimple.v().newNewExpr(RefType.v(arrayTokenClass))),
                    insertPoint);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(tokenLocal,
                            arrayTokenConstructor, tokenArrayLocal)),
                    insertPoint);
            return tokenLocal;
        } else {
            SootClass tokenClass =
                Scene.v().loadClassAndSupport(token.getClass().getName());
            SootMethod tokenConstructor =
                tokenClass.getMethod("void <init>(java.lang.String)");

            Local tokenLocal = Jimple.v().newLocal(localName,
                    RefType.v(tokenClass));
            body.getLocals().add(tokenLocal);
            units.insertBefore(Jimple.v().newAssignStmt(tokenLocal,
                    Jimple.v().newNewExpr(RefType.v(tokenClass))),
                    insertPoint);
            // Ugh...  otherwise we get some stupid quotes.
            if(token instanceof StringToken) {
                StringToken stringToken = (StringToken)token;
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                tokenConstructor,
                                StringConstant.v(stringToken.stringValue()))),
                        insertPoint);
            } else {
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                tokenConstructor,
                                StringConstant.v(token.toString()))),
                        insertPoint);
            }
            return tokenLocal;
        }
    }

    /** Insert a local into the given body and code before the given insertion
     *  point to initialize that local to an instance that is equal to the
     *  given type.
     */
     // FIXME Records!
   public static Local buildConstantTypeLocal(Body body, Object insertPoint,
            ptolemy.data.type.Type type) {
        Chain units = body.getUnits();
        if(type instanceof ptolemy.data.type.BaseType) {
            Local typeLocal = Jimple.v().newLocal("type_" + type.toString(),
                    RefType.v(baseTypeClass));
            body.getLocals().add(typeLocal);
            // This may look ugly, but wherever we insert type casts
            // it is more efficient and also much easier to optimize
            // during the translation process if we want to inline code.
            if(type.equals(ptolemy.data.type.BaseType.UNKNOWN)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(unknownTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.GENERAL)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(generalTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.BOOLEAN)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(booleanTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.BOOLEAN_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(booleanMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.COMPLEX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(complexTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.COMPLEX_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(complexMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.DOUBLE)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(doubleTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.DOUBLE_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(doubleMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.FIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(fixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.FIX_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(fixMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.INT)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(intTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.INT_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(intMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.LONG)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(longTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.LONG_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(longMatrixTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.OBJECT)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(objectTypeField)),
                        insertPoint);
            } else if(type.equals(ptolemy.data.type.BaseType.STRING)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(stringTypeField)),
                        insertPoint);
            } else {
                // Some base type that we didn't special case above.
                SootMethod typeConstructor =
                    SootUtilities.searchForMethodByName(
                            baseTypeClass, "forName");
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticInvokeExpr(typeConstructor,
                                StringConstant.v(type.toString()))),
                        insertPoint);
            }
            return typeLocal;
        } else if(type instanceof ptolemy.data.type.ArrayType) {
            // recurse
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(arrayTypeClass, "<init>");
            Local elementTypeLocal = buildConstantTypeLocal(body, insertPoint,
                    ((ptolemy.data.type.ArrayType)type).getElementType());
            Local typeLocal = Jimple.v().newLocal("type_arrayOf" +
                    elementTypeLocal.getName(),
                    RefType.v(arrayTypeClass));
            body.getLocals().add(typeLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newNewExpr(RefType.v(arrayTypeClass))),
                    insertPoint);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(typeLocal,
                            typeConstructor, elementTypeLocal)),
                    insertPoint);
            return typeLocal;
        } else if(type instanceof ptolemy.data.type.RecordType) {
            ptolemy.data.type.RecordType recordType =
                (ptolemy.data.type.RecordType)type;
            // recurse
            String typeName = "type_recordOf";

            // Create the new array of labels.
            Local labelArrayLocal = Jimple.v().newLocal("labelArray",
                    ArrayType.v(RefType.v("java.lang.String"), 1));
            body.getLocals().add(labelArrayLocal);
            units.insertBefore(Jimple.v().newAssignStmt(labelArrayLocal,
                    Jimple.v().newNewArrayExpr(RefType.v("java.lang.String"),
                            IntConstant.v(recordType.labelSet().size()))),
                    insertPoint);
            // Create the new array of types.
            Local typeArrayLocal = Jimple.v().newLocal("typeArray",
                    ArrayType.v(RefType.v(typeClass), 1));
            body.getLocals().add(typeArrayLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeArrayLocal,
                    Jimple.v().newNewArrayExpr(RefType.v(baseTypeClass),
                            IntConstant.v(recordType.labelSet().size()))),
                    insertPoint);

            int count = 0;
            for(Iterator labels = recordType.labelSet().iterator();
                labels.hasNext(); count++) {
                String label = (String)labels.next();
                ptolemy.data.type.Type elementType = recordType.get(label);
                Local elementTypeLocal = buildConstantTypeLocal(body,
                        insertPoint, elementType);
                typeName += "_" + label + "_" + elementTypeLocal.getName();
                // Store into the array of labels.
                units.insertBefore(Jimple.v().newAssignStmt(
                        Jimple.v().newArrayRef(labelArrayLocal,
                                IntConstant.v(count)),
                        StringConstant.v(label)),
                        insertPoint);

                // Store into the array of types.
                units.insertBefore(Jimple.v().newAssignStmt(
                        Jimple.v().newArrayRef(typeArrayLocal,
                                IntConstant.v(count)),
                        elementTypeLocal),
                        insertPoint);
             }

            // Create the new local and assign to local variable.
            Local typeLocal = Jimple.v().newLocal(typeName,
                    RefType.v(recordTypeClass));
            body.getLocals().add(typeLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newNewExpr(RefType.v(recordTypeClass))),
                    insertPoint);

            // invoke the initializer.
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(recordTypeClass, "<init>");
            System.out.println("typeConstructor = " + typeConstructor);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(typeLocal,
                            typeConstructor, labelArrayLocal, typeArrayLocal)),
                    insertPoint);
            return typeLocal;
        }
        throw new RuntimeException("Unidentified type class = " +
                type.getClass().getName());
    }

    /** Insert code into the given body before the
     *  given insertion point to call the
     *  attribute changed method on the object stored in the given local.
     *  @param base A local that is assumed to have an attribute type.
     */
    public static void callAttributeChanged(
            Local base, SootClass theClass, SootMethod method,
            JimpleBody body, Object insertPoint) {
        Local attributeLocal = base;
        // Make sure we have a local of type attribute to pass
        // to attributeChanged
        if(base.getType() != attributeType) {
            attributeLocal = Jimple.v().newLocal("attributeLocal",
                    attributeType);
            body.getLocals().add(attributeLocal);
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(attributeLocal,
                            Jimple.v().newCastExpr(base,
                                    attributeType)),
                    insertPoint);
        }

        Stmt stmt = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(body.getThisLocal(),
                        attributeChangedMethod, attributeLocal));
        body.getUnits().insertBefore(stmt, insertPoint);
    }

    /** In the given body, create a new local with the given name.
     *  The local will refer to an object of type className.
     *  Add instructions to the end of the chain of the body
     *  to create and initialize a new
     *  named object with the given container and name.  Assign the value
     *  of the local to the created instance.
     *  @return The local that was created.
     */
    public static Local createNamedObjAndLocal(Body body, String className,
            Local container, String name) {
        Chain units = body.getUnits();
        SootClass objectClass;
        if(Scene.v().containsClass(className)) {
            objectClass = Scene.v().getSootClass(className);
        } else {
            objectClass = Scene.v().loadClassAndSupport(className);
        }

        RefType objectType = RefType.v(objectClass);

        // Create the new local with the given name.
        Local local = Jimple.v().newLocal(name, objectType);

        // Add the local to the body.
        body.getLocals().add(local);

        // Create the new local with the given name.
        Local attributeLocal = Jimple.v().newLocal(name, attributeType);

        // Add the local to the body.
        body.getLocals().add(attributeLocal);

        // Create the object.
        units.add(Jimple.v().newAssignStmt(local,
                Jimple.v().newNewExpr(objectType)));

        // The constructor arguments.
        List args = new LinkedList();
        args.add(container);
        args.add(StringConstant.v(name));

        // Call the constructor on the object.
        SootMethod constructor =
            SootUtilities.getMatchingMethod(objectClass, "<init>", args);
        if(constructor == null) {
            throw new RuntimeException("Could not find 2 argument constructor"
                    + " for class " + objectClass);
        }

        units.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(local,
                        constructor, args)));
        return local;
    }

    /** If the given type is a reference type to a class that
     *  derives from ptolemy.data.Token, or array whose element type
     *  derives from ptolemy.data.Token, then return that
     *  token type.  Otherwise return null.
     */
    // FIXME: should throw exception.
    public static RefType getBaseTokenType(Type type) {
        RefType returnType;
        if(type instanceof RefType) {
            returnType = (RefType)type;
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if(arrayType.baseType instanceof RefType) {
                returnType = (RefType)arrayType.baseType;
            } else {
                return null;
            }
        } else {
            // If we have a native type, then ignore because it can't
            // be a token type.
            return null;
        }
        SootClass objectClass = returnType.getSootClass();
        if(SootUtilities.derivesFrom(objectClass,
                PtolemyUtilities.tokenClass)) {
            return returnType;
        }
        return null;
    }

    /** Given a ptolemy token type, return the soot type that can reference
     *  tokens of the ptolemy type.
     */
    // FIXME Records!
    public static RefType getSootTypeForTokenType(
            ptolemy.data.type.Type type) {
        if(type instanceof ptolemy.data.type.ArrayType) {
            return RefType.v("ptolemy.data.ArrayToken");
        } else if(type instanceof ptolemy.data.type.RecordType) {
            return RefType.v("ptolemy.data.RecordToken");
        } else if(!type.isInstantiable()) {
            // We should be able to do something better here...
            // This means that the port
            // has no data.
            return RefType.v("ptolemy.data.Token");
        } else if(type instanceof ptolemy.data.type.BaseType) {
            ptolemy.data.type.BaseType baseType =
                (ptolemy.data.type.BaseType)type;
            return RefType.v(baseType.getTokenClass().getName());
        }
        else throw new RuntimeException("unknown type = " + type);
    }

    /** Given a soot type that references a
     *  token class, return the ptolemy token type
     *  associated with the token class.  If the type is an array token, then
     *  the returned type will have an indeterminate element type.
     */
    // FIXME Records!
    // FIXME: this is hacked to return unknown types for token types that
    // are abstract.
    public static ptolemy.data.type.Type getTokenTypeForSootType(
            RefType type) {
        String className = type.getSootClass().getName();
        //  System.out.println("className = " + className);
        if(className.equals("ptolemy.data.ArrayToken")) {
            return new ptolemy.data.type.ArrayType(
                    ptolemy.data.type.BaseType.UNKNOWN);
        } else if(className.equals("ptolemy.data.RecordToken")) {
            return new ptolemy.data.type.RecordType(
                    new String[0], new ptolemy.data.type.Type[0]);
        } else if(className.equals("ptolemy.data.Token")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if(className.equals("ptolemy.data.ScalarToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if(className.equals("ptolemy.data.MatrixToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else {
            ptolemy.data.type.Type tokenType =
                ptolemy.data.type.BaseType.forClassName(className);
            if(tokenType == null) {
                throw new RuntimeException("unknown type = " + type + ".");
            }
            return tokenType;
        }
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a variable type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise throw an exception
     */
    public static ptolemy.data.type.Type getTypeValue(SootMethod method,
            Local local, Unit location, LocalDefs localDefs) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof Local) {
                return getTypeValue(method, (Local)value,
                        stmt, localDefs);
            } else if(value instanceof CastExpr) {
                return getTypeValue(method, (Local)((CastExpr)value).getOp(),
                        stmt, localDefs);
            } else if(value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                if(field.equals(unknownTypeField)) {
                    return ptolemy.data.type.BaseType.UNKNOWN;
                } else if(field.equals(booleanTypeField)) {
                    return ptolemy.data.type.BaseType.BOOLEAN;
                } else if(field.equals(booleanMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.BOOLEAN_MATRIX;
                } else if(field.equals(byteTypeField)) {
                    return ptolemy.data.type.BaseType.BYTE;
                } else if(field.equals(complexTypeField)) {
                    return ptolemy.data.type.BaseType.COMPLEX;
                } else if(field.equals(complexMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.COMPLEX_MATRIX;
                } else if(field.equals(doubleTypeField)) {
                    return ptolemy.data.type.BaseType.DOUBLE;
                } else if(field.equals(doubleMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.DOUBLE_MATRIX;
                } else if(field.equals(fixTypeField)) {
                    return ptolemy.data.type.BaseType.FIX;
                } else if(field.equals(fixMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.FIX_MATRIX;
                } else if(field.equals(intTypeField)) {
                    return ptolemy.data.type.BaseType.INT;
                } else if(field.equals(intMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.INT_MATRIX;
                } else if(field.equals(longTypeField)) {
                    return ptolemy.data.type.BaseType.LONG;
                } else if(field.equals(longMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.LONG_MATRIX;
                } else if(field.equals(objectTypeField)) {
                    return ptolemy.data.type.BaseType.OBJECT;
                } else if(field.equals(stringTypeField)) {
                    return ptolemy.data.type.BaseType.STRING;
                } else {
                    throw new RuntimeException("Unknown type field: " + field);
                }
            } else if(value instanceof NullConstant) {
                // If we get to an assignment from null, then the
                // attribute statically evaluates to null.
                return null;
            } else {
                throw new RuntimeException("Unknown type of value: " +
                        value + " in " + method);
            }
        } else {
            String string = "More than one definition of = " + local + "\n";
            for(Iterator i = definitionList.iterator();
                i.hasNext();) {
                string += "Definition = " + i.next().toString();
            }
            throw new RuntimeException(string);
        }
    }

    /** Inline the given invocation expression, given knowledge that the
     *  method was invoked on the given named object.  In general,
     *  methods that return information that can be determined from the
     *  given object return that value, methods that set information
     *  are removed and methods that are non-sensical in generated
     *  code throw an exception.
     *  @param body The body in which the invoke occured.
     *  @param unit The unit in which the invoke occured.
     *  @param box The value box containing the invoke expression.
     *  @param typeable A reference to an object that has the same
     *  resolve type as the typeable object that the invoke would have
     *  occured on, has the code actually been executed.
     */
    public static void inlineNamedObjMethods(JimpleBody body,
            Unit unit, ValueBox box, InstanceInvokeExpr expr,
            NamedObj typeable) {
        String name = expr.getMethod().getName();
        // FIXME name matching here is rather imprecise.
        if(name.equals("getType")) {
        }
    }

    /** Inline the given invocation expression, given knowledge that the
     *  method was invoked on the given Typeable object, and that the
     *  typeable object has a final resolved type.  The getType method
     *  will be replace with code that returns the resolved type as a
     *  constant, methods that add type constraints are removed, and
     *  other methods that return a value are changed to throw exceptions.
     *  @param body The body in which the invoke occured.
     *  @param unit The unit in which the invoke occured.
     *  @param box The value box containing the invoke expression.
     *  @param typeable A reference to an object that has the same
     *  resolve type as the typeable object that the invoke would have
     *  occured on, has the code actually been executed.
     */
    public static void inlineTypeableMethods(JimpleBody body,
            Unit unit, ValueBox box, InstanceInvokeExpr expr,
            Typeable typeable) {
        String name = expr.getMethod().getName();
        // FIXME name matching here is rather imprecise.
        if(name.equals("getType")) {
            // Replace method calls to getType with the constant type
            // of the typeable.
            try {
                Local typeLocal = PtolemyUtilities.buildConstantTypeLocal(
                        body, unit, typeable.getType());
                box.setValue(typeLocal);
            } catch (Exception ex) {
                throw new RuntimeException("Type of " + typeable +
                        " could not be determined: " + ex.getMessage());
            }
        } else if(name.equals("getTypeTerm")) {
            // FIXME: This method should be removed.
            //Local exceptionLocal =
            // SootUtilities.createRuntimeException(body, unit,
            //        "Illegal Method Call: getTypeTerm()");
            //body.getUnits().swapWith(unit,
            //        Jimple.v().newThrowStmt(exceptionLocal));
        } else if(name.equals("setTypeEquals")) {
            // Remove call.
            body.getUnits().remove(unit);
        } else if(name.equals("setTypeAtLeast")) {
            // Remove call.
            body.getUnits().remove(unit);
        } else if(name.equals("setTypeAtMost")) {
            // Remove call.
            body.getUnits().remove(unit);
        } else if(name.equals("setTypeSameAs")) {
            // Remove call.
            body.getUnits().remove(unit);
        } else if(name.equals("typeConstraintList")) {
            //FIXME This method should be removed.
            // Local exceptionLocal =
            //    SootUtilities.createRuntimeException(body, unit,
            //      "Illegal Method Call: typeConstraintList()");
            //body.getUnits().swapWith(unit,
            //        Jimple.v().newThrowStmt(exceptionLocal));
        }
    }

    /** Return true if the given type references a concrete
     *  ptolemy token type.  In other words It is either a direct
     *  reference to a token, or an array of tokens.
     *  This method only returns true if the token is
     *  an instantiable token.
     */
    public static boolean isConcreteTokenType(Type type) {
        RefType refType;
        if(type instanceof RefType) {
            refType = (RefType)type;
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if(arrayType.baseType instanceof RefType) {
                refType = (RefType)arrayType.baseType;
            } else return false;
        } else return false;
        SootClass tokenClass = refType.getSootClass();
        if(tokenClass.equals(PtolemyUtilities.tokenClass) ||
                tokenClass.equals(PtolemyUtilities.scalarTokenClass)) {
            return false;
        }
        return SootUtilities.derivesFrom(refType.getSootClass(),
                PtolemyUtilities.tokenClass);
    }

    /** Return true if the given type references a
     *  ptolemy token type.  In other words It is either a direct
     *  reference to a token, or an array of tokens.
     */
    public static boolean isArrayTokenType(Type type) {
        RefType refType;
        if(type instanceof RefType) {
            refType = (RefType)type;
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if(arrayType.baseType instanceof RefType) {
                refType = (RefType)arrayType.baseType;
            } else return false;
        } else return false;
        return SootUtilities.derivesFrom(refType.getSootClass(),
                PtolemyUtilities.arrayTokenClass);
    }

    /** Return true if the given type references a
     *  ptolemy token type.  In other words It is either a direct
     *  reference to a token, or an array of tokens.
     */
    public static boolean isTokenType(Type type) {
        RefType refType;
        if(type instanceof RefType) {
            refType = (RefType)type;
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if(arrayType.baseType instanceof RefType) {
                refType = (RefType)arrayType.baseType;
            } else return false;
        } else return false;
        return SootUtilities.derivesFrom(refType.getSootClass(),
                PtolemyUtilities.tokenClass);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Soot class representing the ptolemy.actor.TypedAtomicActor class.
    public static SootClass actorClass;

    // Soot Type representing the ptolemy.actor.TypedAtomicActor class.
    public static Type actorType;

    // The soot method representing the java.lang.System.arraycopy method.
    public static SootMethod arraycopyMethod;

    // Soot class representing the ptolemy.data.ArrayToken class.
    public static SootClass arrayTokenClass;

    // Soot Method representing the ArrayToken(Token[]) constructor.
    public static SootMethod arrayTokenConstructor;

    // Soot class representing the ptolemy.data.type.ArrayType class.
    public static SootClass arrayTypeClass;

    // Soot Method representing NamedObj.attachText()
    public static SootMethod attachTextMethod;

    // Soot Method representing NamedObj.attributeChanged().
    public static SootMethod attributeChangedMethod;

    // Soot class representing the ptolemy.kernel.util.Attribute class.
    public static SootClass attributeClass;

    // Soot Type representing the ptolemy.kernel.util.Settable class.
    public static Type attributeType;

    // Soot Class representing the ptolemy.data.type.BaseType class.
    public static SootClass baseTypeClass;

    public static SootField booleanTypeField;
    public static SootField booleanMatrixTypeField;
    public static SootField byteTypeField;
    public static SootField complexTypeField;
    public static SootField complexMatrixTypeField;

    // Soot class representing the ptolemy.actor.TypedCompositeActor class.
    public static SootClass compositeActorClass;

    // Soot class representing the ptolemy.kernel.CompositeEntity class.
    public static SootClass compositeEntityClass;

    // Soot Method representing Entity.connectionsChanged().
    public static SootMethod connectionsChangedMethod;

    public static SootField doubleTypeField;
    public static SootField doubleMatrixTypeField;

    // Soot class representing the ptolemy.kernel.Entity class.
    public static SootClass entityClass;

    // Soot class representing the ptolemy.actor.Executable interface.
    public static SootClass executableInterface;

    public static SootField fixTypeField;
    public static SootField fixMatrixTypeField;

    public static SootField generalTypeField;

    // SootMethod representing
    // ptolemy.kernel.util.Attribute.getAttribute();
    public static SootMethod getAttributeMethod;

    // SootMethod representing
    // ptolemy.actor.Actor.getDirector
    public static SootMethod getDirectorMethod;

    // SootMethod representing
    // ptolemy.kernel.CompositeEntity.getEntity
    public static SootMethod getEntityMethod;

    // SootMethod representing
    // ptolemy.kernel.util.Settable.getExpression();
    public static SootMethod getExpressionMethod;

    // ptolemy.kernel.util.NamedObj.getFullName
    public static SootMethod getFullNameMethod;

    // ptolemy.kernel.util.NamedObj.getName
    public static SootMethod getNameMethod;

    // SootMethod representing
    // ptolemy.kernel.Entity.getPort
    public static SootMethod getPortMethod;

    // SootMethod representing ptolemy.kernel.ComponentPort.insertLink().
    public static SootMethod insertLinkMethod;

    public static SootField intTypeField;
    public static SootField intMatrixTypeField;

    // SootMethod representing ptolemy.actor.Director.invalidateResolvedTypes()
    public static SootMethod invalidateResolvedTypesMethod;

    // Soot Class representing the ptolemy.actor.TypedIOPort class.
    public static SootClass ioportClass;

    // Soot Type representing the ptolemy.actor.TypedIOPort class.
    public static Type ioportType;

    public static SootClass kernelExceptionClass;
    public static SootClass kernelRuntimeExceptionClass;

    public static SootField longTypeField;
    public static SootField longMatrixTypeField;

    // SootClass representing ptolemy.actor.Manager
    public static SootClass managerClass;

    // SootClass representing ptolemy.kernel.util.NamedObj.
    public static SootClass namedObjClass;

    // SootClass representing java.lang.Object.
    public static SootClass objectClass;

    public static SootField objectTypeField;

    // Soot Class representing the ptolemy.kernel.ComponentPort class.
    public static SootClass portClass;

    // Soot Method representing the ptolemy.actor.TypedIOPort.setTypeEquals method.
    public static SootMethod portSetTypeMethod;

    // Soot Type representing the ptolemy.kernel.ComponentPort class.
    public static Type portType;

    // Soot Class representing the ptolemy.kernel.ComponentRelation class.
    public static SootClass relationClass;

    // Soot Class representing the ptolemy.data.type.RecordType class.
    public static SootClass recordTypeClass;

    // Soot Type representing the ptolemy.kernel.ComponentRelation class.
    public static Type relationType;

    public static SootClass runtimeExceptionClass;

    // Soot class representing the ptolemy.data.ScalarToken class.
    public static SootClass scalarTokenClass;

    // SootMethod representing ptolemy.kernel.util.Settable.setExpression().
    public static SootMethod setExpressionMethod;

    // SootMethod representing ptolemy.actor.IOPort.setInput().
    public static SootMethod setInputMethod;
    // SootMethod representing ptolemy.actor.IOPort.setOutput().
    public static SootMethod setOutputMethod;
    // SootMethod representing ptolemy.actor.IOPort.setMultiport().
    public static SootMethod setMultiportMethod;

    // Soot Class representing the ptolemy.kernel.util.Settable class.
    public static SootClass settableClass;

    // Soot Type representing the ptolemy.kernel.util.Settable class.
    public static Type settableType;

    //The soot class representing java.lang.system
    public static SootClass stringClass;

    public static SootField stringTypeField;

    //The soot class representing java.lang.system
    public static SootClass systemClass;

    // Soot class representing the ptolemy.data.Token class.
    public static SootClass tokenClass;

    // Soot Type representing the ptolemy.data.Token class.
    public static BaseType tokenType;

    public static SootMethod toStringMethod;

    public static SootClass typeClass;

    public static SootMethod typeConvertMethod;

    public static SootClass typeLatticeClass;

    public static SootField unknownTypeField;

    // ptolemy.kernel.util.Settable.validate()
    public static SootMethod validateMethod;

    public static SootClass variableClass;

    public static SootMethod variableConstructorWithoutToken;

    public static SootMethod variableConstructorWithToken;

    public static SootMethod variableSetTokenMethod;

    static {
        loadSootReferences();
    }

    /**
     *  Initialize the fields of this class to point to classes in the
     *  current soot tree.
     */
    public static void loadSootReferences() {
        objectClass =
            Scene.v().loadClassAndSupport("java.lang.Object");
        toStringMethod = objectClass.getMethod("java.lang.String toString()");

        stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");

        systemClass = Scene.v().loadClassAndSupport("java.lang.System");
        arraycopyMethod = systemClass.getMethodByName("arraycopy");

        namedObjClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute "
                + "getAttribute(java.lang.String)");
        attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");
        attachTextMethod = namedObjClass.getMethod(
                "void _attachText(java.lang.String,java.lang.String)");
        getFullNameMethod =
            namedObjClass.getMethod("java.lang.String getFullName()");
        getNameMethod =
            namedObjClass.getMethod("java.lang.String getName()");


        attributeClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        attributeType = RefType.v(attributeClass);

        settableClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        settableType = RefType.v(settableClass);
        setExpressionMethod =
            settableClass.getMethodByName("setExpression");
        getExpressionMethod =
            settableClass.getMethod("java.lang.String getExpression()");
        validateMethod =
            settableClass.getMethod("void validate()");

        variableClass =
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        variableConstructorWithoutToken = variableClass.getMethod(
                "void <init>(ptolemy.kernel.util.NamedObj,java.lang.String)");
        variableConstructorWithToken = variableClass.getMethod(
                "void <init>(ptolemy.kernel.util.NamedObj,java.lang.String,ptolemy.data.Token)");
        variableSetTokenMethod = variableClass.getMethod(
                "void setToken(ptolemy.data.Token)");

        executableInterface =
            Scene.v().loadClassAndSupport("ptolemy.actor.Executable");

        entityClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.Entity");
        connectionsChangedMethod = entityClass.getMethod(
                "void connectionsChanged(ptolemy.kernel.Port)");
        getPortMethod =
            entityClass.getMethod("ptolemy.kernel.Port getPort(java.lang.String)");

        compositeEntityClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.CompositeEntity");
        getEntityMethod =
            compositeEntityClass.getMethod("ptolemy.kernel.ComponentEntity getEntity(java.lang.String)");

        actorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedAtomicActor");
        actorType = RefType.v(actorClass);
        getDirectorMethod =
            Scene.v().getMethod("<ptolemy.actor.Actor: ptolemy.actor.Director getDirector()>");

        invalidateResolvedTypesMethod =
            Scene.v().getMethod("<ptolemy.actor.Director: void invalidateResolvedTypes()>");

        compositeActorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");

        portClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentPort");
        portType = RefType.v(portClass);

        relationClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentRelation");
        relationType = RefType.v(relationClass);

        ioportClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedIOPort");
        ioportType = RefType.v(ioportClass);
        portSetTypeMethod =
            Scene.v().getMethod("<ptolemy.actor.TypedIOPort: void setTypeEquals(ptolemy.data.type.Type)>");

        insertLinkMethod = SootUtilities.searchForMethodByName(portClass,
                "insertLink");
        setInputMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setInput(boolean)>");
        setOutputMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setOutput(boolean)>");
        setMultiportMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setMultiport(boolean)>");

        tokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
        tokenType = RefType.v(tokenClass);

        arrayTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.ArrayToken");
        arrayTokenConstructor =
            arrayTokenClass.getMethod("void <init>(ptolemy.data.Token[])");

        typeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.Type");
        typeConvertMethod =
            typeClass.getMethod("ptolemy.data.Token convert(ptolemy.data.Token)");

        arrayTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.ArrayType");
        recordTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.RecordType");
        baseTypeClass =
                Scene.v().loadClassAndSupport("ptolemy.data.type.BaseType");
        unknownTypeField = baseTypeClass.getFieldByName("UNKNOWN");
        generalTypeField = baseTypeClass.getFieldByName("GENERAL");
        booleanTypeField = baseTypeClass.getFieldByName("BOOLEAN");
        booleanMatrixTypeField =
            baseTypeClass.getFieldByName("BOOLEAN_MATRIX");
        byteTypeField = baseTypeClass.getFieldByName("BYTE");
        complexTypeField = baseTypeClass.getFieldByName("COMPLEX");
        complexMatrixTypeField =
            baseTypeClass.getFieldByName("COMPLEX_MATRIX");
        doubleTypeField = baseTypeClass.getFieldByName("DOUBLE");
        doubleMatrixTypeField = baseTypeClass.getFieldByName("DOUBLE_MATRIX");
        fixTypeField = baseTypeClass.getFieldByName("FIX");
        fixMatrixTypeField = baseTypeClass.getFieldByName("FIX_MATRIX");
        intTypeField = baseTypeClass.getFieldByName("INT");
        intMatrixTypeField = baseTypeClass.getFieldByName("INT_MATRIX");
        longTypeField = baseTypeClass.getFieldByName("LONG");
        longMatrixTypeField = baseTypeClass.getFieldByName("LONG_MATRIX");
        objectTypeField = baseTypeClass.getFieldByName("OBJECT");
        stringTypeField = baseTypeClass.getFieldByName("STRING");

        typeLatticeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.TypeLattice");

        kernelExceptionClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.KernelException");
        kernelRuntimeExceptionClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.KernelRuntimeException");
        runtimeExceptionClass =
            Scene.v().loadClassAndSupport("java.lang.RuntimeException");
        managerClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.Manager");
    }
}
