/* Ptolemy-specific utilities to use with Soot

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

package ptolemy.copernicus.kernel;
import ptolemy.math.Complex;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.Typeable;
import ptolemy.copernicus.java.DataUtilities;

import soot.ArrayType;
import soot.PrimType;
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
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.Chain;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyUtilities
/**
This class consists of ptolemy-specific static utility methods for use
with Soot.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
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
    public static Local buildConstantTokenLocal(JimpleBody body,
            Unit insertPoint, Token token, String localName) {
        Chain units = body.getUnits();
        if (token instanceof ptolemy.data.ArrayToken) {
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
            for (int i = 0; i < arrayToken.length(); i++) {
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
            //     } else if (token instanceof ptolemy.data.RecordToken) {
            //             RecordToken recordToken = (RecordToken)token;
            //             int size = recordToken.labelSet().size();
            //             Type stringArrayType =
            //                 ArrayType.v(RefType.v(PtolemyUtilities.stringClass), 1);
            //             Local stringArrayLocal = Jimple.v().newLocal(localName + "SArray",
            //                     stringArrayType);
            //             body.getLocals().add(stringArrayLocal);
            //             // Create the array of strings.
            //             units.insertBefore(
            //                     Jimple.v().newAssignStmt(stringArrayLocal,
            //                             Jimple.v().newNewArrayExpr(
            //                                     RefType.v(PtolemyUtilities.stringClass),
            //                                     IntConstant.v(size))),
            //                     insertPoint);

            //             Type tokenArrayType = ArrayType.v(tokenType, 1);
            //             Local tokenArrayLocal = Jimple.v().newLocal(localName + "TArray",
            //                     tokenArrayType);
            //             body.getLocals().add(tokenArrayLocal);
            //             // Create the array of tokens.
            //             units.insertBefore(Jimple.v().newAssignStmt(tokenArrayLocal,
            //                     Jimple.v().newNewArrayExpr(tokenType,
            //                             IntConstant.v(size))),
            //                     insertPoint);
            //             // recurse
            //             int i = 0;
            //             for (Iterator labels = recordToken.labelSet().iterator();
            //                  labels.hasNext(); i++) {
            //                 String label = (String)labels.next();
            //                 Local argLocal = buildConstantTokenLocal(body, insertPoint,
            //                         recordToken.get(label), localName + "_" + label);
            //                 units.insertBefore(
            //                         Jimple.v().newAssignStmt(
            //                                 Jimple.v().newArrayRef(stringArrayLocal,
            //                                         IntConstant.v(i)),
            //                                 StringConstant.v(label)),
            //                         insertPoint);
            //                 units.insertBefore(
            //                         Jimple.v().newAssignStmt(
            //                                 Jimple.v().newArrayRef(tokenArrayLocal,
            //                                         IntConstant.v(i)),
            //                                 argLocal),
            //                         insertPoint);
            //             }
            //             Local tokenLocal = Jimple.v().newLocal(localName,
            //                     RefType.v(recordTokenClass));
            //             body.getLocals().add(tokenLocal);
            //             units.insertBefore(Jimple.v().newAssignStmt(tokenLocal,
            //                     Jimple.v().newNewExpr(RefType.v(recordTokenClass))),
            //                     insertPoint);
            //             units.insertBefore(Jimple.v().newInvokeStmt(
            //                     Jimple.v().newSpecialInvokeExpr(tokenLocal,
            //                             recordTokenConstructor, stringArrayLocal,
            //                             tokenArrayLocal)),
            //                     insertPoint);
            //             return tokenLocal;
        } else if (token.getClass().equals(Token.class)) {
            // Token has no string constructor.
            SootClass tokenClass =
                Scene.v().loadClassAndSupport(token.getClass().getName());
            SootMethod tokenConstructor =
                tokenClass.getMethod("void <init>()");

            Local tokenLocal = Jimple.v().newLocal(localName,
                    RefType.v(tokenClass));
            body.getLocals().add(tokenLocal);
            units.insertBefore(Jimple.v().newAssignStmt(tokenLocal,
                    Jimple.v().newNewExpr(RefType.v(tokenClass))),
                    insertPoint);
            units.insertBefore(
                    Jimple.v().newInvokeStmt(
                            Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                    tokenConstructor)),
                    insertPoint);
            return tokenLocal;
        } else if (token instanceof IntToken) {
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, intTokenClass, intTokenConstructor,
                    IntConstant.v(((IntToken)token).intValue()));
            return tokenLocal;
        }  else if (token instanceof UnsignedByteToken) {
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, unsignedByteTokenClass,
                    unsignedByteTokenConstructor,
                    IntConstant.v(((UnsignedByteToken)token).byteValue()));
            return tokenLocal;
        } else if (token instanceof BooleanToken) {
            Value value;
            if (((BooleanToken)token).booleanValue()) {
                value = IntConstant.v(1);
            } else {
                value = IntConstant.v(0);
            }
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, booleanTokenClass, booleanTokenConstructor,
                    value);
            return tokenLocal;
        } else if (token instanceof DoubleToken) {
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, doubleTokenClass, doubleTokenConstructor,
                    DoubleConstant.v(((DoubleToken)token).doubleValue()));
            return tokenLocal;
        } else if (token instanceof ComplexToken) {
            Complex complex = ((ComplexToken)token).complexValue();
            // ComplexToken takes a Complex as a constructor.
            SootClass complexClass =
                Scene.v().loadClassAndSupport("ptolemy.math.Complex");
            SootMethod complexConstructor =
                complexClass.getMethod("void <init>(double,double)");

            Local complexLocal = Jimple.v().newLocal(localName + "Arg",
                    RefType.v(complexClass));
            body.getLocals().add(complexLocal);
            units.insertBefore(Jimple.v().newAssignStmt(complexLocal,
                    Jimple.v().newNewExpr(RefType.v(complexClass))),
                    insertPoint);
            units.insertBefore(
                    Jimple.v().newInvokeStmt(
                            Jimple.v().newSpecialInvokeExpr(complexLocal,
                                    complexConstructor,
                                    DoubleConstant.v(complex.real),
                                    DoubleConstant.v(complex.imag))),
                    insertPoint);
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, complexTokenClass, complexTokenConstructor,
                    complexLocal);
            return tokenLocal;
        } else if (token instanceof StringToken) {
            Local tokenLocal = _buildConstantTokenLocal(body, insertPoint,
                    localName, stringTokenClass, stringTokenConstructor,
                    StringConstant.v(((StringToken)token).stringValue()));
            return tokenLocal;
        } else {
            String expression = token.toString();
            Local tokenLocal = DataUtilities.generateExpressionCodeBefore(
                    null, null, expression,
                    new HashMap(), new HashMap(), body, insertPoint);
            return tokenLocal;
        }
    }

    public static Local addTokenLocal(Body body, String localName,
            SootClass tokenClass, SootMethod tokenConstructor,
            Value constructorArg) {
        Stmt stmt = Jimple.v().newNopStmt();
        body.getUnits().add(stmt);
        return _buildConstantTokenLocal(body, stmt,
                localName, tokenClass,
                tokenConstructor, constructorArg);
    }

    public static Local addTokenLocalBefore(Body body, Unit insertPoint,
            String localName,
            SootClass tokenClass, SootMethod tokenConstructor,
            Value constructorArg) {
        return _buildConstantTokenLocal(body, insertPoint,
                localName, tokenClass,
                tokenConstructor, constructorArg);
    }

    private static Local _buildConstantTokenLocal(Body body,
            Unit insertPoint, String localName,
            SootClass tokenClass, SootMethod tokenConstructor,
            Value constructorArg) {
        RefType tokenType = RefType.v(tokenClass);
        Local tokenLocal = Jimple.v().newLocal(localName, tokenType);
        body.getLocals().add(tokenLocal);
        body.getUnits().insertBefore(
                Jimple.v().newAssignStmt(tokenLocal,
                        Jimple.v().newNewExpr(tokenType)),
                insertPoint);
        body.getUnits().insertBefore(
                Jimple.v().newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                tokenConstructor,
                                constructorArg)),
                insertPoint);
        return tokenLocal;
    }

    /** Insert a local into the given body and code before the given insertion
     *  point to initialize that local to an instance that is equal to the
     *  given type.
     *  @return The created local.
     */
    // FIXME Records!
    public static Local buildConstantTypeLocal(Body body, Object insertPoint,
            ptolemy.data.type.Type type) {
        Chain units = body.getUnits();
        if (type instanceof ptolemy.data.type.BaseType ||
                type instanceof ptolemy.data.type.UnsizedMatrixType) {
            Local typeLocal = Jimple.v().newLocal("type_" + type.toString(),
                    RefType.v(baseTypeClass));
            body.getLocals().add(typeLocal);
            // This may look ugly, but wherever we insert type casts
            // it is more efficient and also much easier to optimize
            // during the translation process if we want to inline code.
            if (type.equals(ptolemy.data.type.BaseType.UNKNOWN)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(unknownTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.GENERAL)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(generalTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.BOOLEAN)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(booleanTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.BOOLEAN_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(booleanMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.COMPLEX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(complexTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.COMPLEX_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(complexMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.DOUBLE)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(doubleTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.DOUBLE_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(doubleMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.FIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(fixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.FIX_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(fixMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.UNSIGNED_BYTE)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(byteTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.INT)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(intTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.INT_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(intMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.LONG)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(longTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.LONG_MATRIX)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(longMatrixTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.OBJECT)) {
                units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                        Jimple.v().newStaticFieldRef(objectTypeField)),
                        insertPoint);
            } else if (type.equals(ptolemy.data.type.BaseType.STRING)) {
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
        } else if (type instanceof ptolemy.data.type.ArrayType) {
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
        } else if (type instanceof ptolemy.data.type.RecordType) {
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
                    Jimple.v().newNewArrayExpr(RefType.v(typeClass),
                            IntConstant.v(recordType.labelSet().size()))),
                    insertPoint);

            int count = 0;
            for (Iterator labels = recordType.labelSet().iterator();
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
        } else if (type instanceof ptolemy.data.type.FixType) {
            Local typeLocal = Jimple.v().newLocal("type_fix",
                    RefType.v(fixTypeClass));
            body.getLocals().add(typeLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newStaticFieldRef(fixTypeField)),
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
            Local base, Local attributeLocal, SootClass theClass, SootMethod method,
            JimpleBody body, Object insertPoint) {
        // Make sure we have a local of type attribute to pass
        // to attributeChanged
        if (attributeLocal.getType() != attributeType) {
            Local oldAttributeLocal = attributeLocal;
            attributeLocal = Jimple.v().newLocal("attributeLocal",
                    attributeType);
            body.getLocals().add(attributeLocal);
            body.getUnits().insertBefore(
                    Jimple.v().newAssignStmt(attributeLocal,
                            Jimple.v().newCastExpr(oldAttributeLocal,
                                    attributeType)),
                    insertPoint);
        }

        Stmt stmt = Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(base,
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
        if (Scene.v().containsClass(className)) {
            objectClass = Scene.v().getSootClass(className);
        } else {
            objectClass = Scene.v().loadClassAndSupport(className);
        }
        // System.out.println("done loading support of " + className);

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
        if (constructor == null) {
            throw new RuntimeException("Could not find 2 argument constructor"
                    + " for class " + objectClass);
        }

        units.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(local,
                        constructor, args)));
        return local;
    }

    /** Create a constructor in theClass that has the same signature
     * as the given method.  Add instructions to the body of the
     * constructor that call the given method with the same arguments.
     */
    public static SootMethod createSuperConstructor(SootClass theClass,
            SootMethod superConstructor) {
        // Create the constructor.
        SootMethod constructor = new SootMethod("<init>",
                superConstructor.getParameterTypes(),
                superConstructor.getReturnType(),
                superConstructor.getModifiers());

        theClass.addMethod(constructor);
        // System.out.println("creating constructor = " +
        //        constructor.getSignature());

        // create empty body
        JimpleBody body = Jimple.v().newBody(constructor);
        // Add this and read the parameters into locals
        body.insertIdentityStmts();
        constructor.setActiveBody(body);

        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();

        // get a list of the locals that reference the parameters of the
        // constructor.  What a nice hack.
        List parameterList = new ArrayList();
        parameterList.addAll(body.getLocals());
        parameterList.remove(thisLocal);

        // Call the super constructor.
        units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(
                thisLocal, superConstructor, parameterList)));

        units.add(Jimple.v().newReturnVoidStmt());
        return constructor;
    }

    /** If the given type is a reference type to a class that
     *  derives from ptolemy.data.Token, or array whose element type
     *  derives from ptolemy.data.Token, then return that
     *  token type.  Otherwise return null.
     */
    // FIXME: should throw exception.
    public static RefType getBaseTokenType(Type type) {
        RefType returnType;
        if (type instanceof RefType) {
            returnType = (RefType)type;
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.baseType instanceof RefType) {
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
        if (SootUtilities.derivesFrom(objectClass,
                PtolemyUtilities.tokenClass) ||
                objectClass.getName().equals("ptolemy.data.BitwiseOperationToken")) {
            return returnType;
        }
        return null;
    }

    /** Return the depth of the given type.  Most simple types have
     *  depth one, while structured types have depth greater than one.
     */
    public static int getTypeDepth(ptolemy.data.type.Type type) {
        if (type instanceof ptolemy.data.type.ArrayType) {
            return 1 + getTypeDepth(((ptolemy.data.type.ArrayType)type).getElementType());
        } else if (type instanceof ptolemy.data.type.RecordType) {
            ptolemy.data.type.RecordType recordType =
                (ptolemy.data.type.RecordType)type;
            Iterator labels = recordType.labelSet().iterator();
            int maxDepth = 0;
            while (labels.hasNext()) {
                ptolemy.data.type.Type elementType =
                    recordType.get((String)labels.next());
                int depth = getTypeDepth(elementType);
                if (depth > maxDepth) maxDepth = depth;
            }
            return maxDepth + 1;
        } else {
            return 1;
        }
    }

    /** Given a ptolemy token type, return the soot type that can reference
     *  tokens of the ptolemy type.
     */
    // FIXME Records!
    public static RefType getSootTypeForTokenType(
            ptolemy.data.type.Type type) {
        if (type instanceof ptolemy.data.type.ArrayType) {
            return RefType.v("ptolemy.data.ArrayToken");
        } else if (type instanceof ptolemy.data.type.RecordType) {
            return RefType.v("ptolemy.data.RecordToken");
        } else if (!type.isInstantiable()) {
            // We should be able to do something better here...
            // This means that the port
            // has no data.
            return RefType.v("ptolemy.data.Token");
        } else if (type instanceof ptolemy.data.type.BaseType) {
            ptolemy.data.type.BaseType baseType =
                (ptolemy.data.type.BaseType)type;
            return RefType.v(baseType.getTokenClass().getName());
        } else if (type instanceof ptolemy.data.type.FixType) {
            ptolemy.data.type.FixType fixType =
                (ptolemy.data.type.FixType)type;
            return RefType.v(fixType.getTokenClass().getName());
        } else if (type instanceof ptolemy.data.type.UnsizedMatrixType) {
            ptolemy.data.type.UnsizedMatrixType matrixType =
                (ptolemy.data.type.UnsizedMatrixType)type;
            return RefType.v(matrixType.getTokenClass().getName());
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
        if (className.equals("ptolemy.data.ArrayToken")) {
            return new ptolemy.data.type.ArrayType(
                    ptolemy.data.type.BaseType.UNKNOWN);
        } else if (className.equals("ptolemy.data.RecordToken")) {
            return new ptolemy.data.type.RecordType(
                    new String[0], new ptolemy.data.type.Type[0]);
        } else if (className.equals("ptolemy.data.Token")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.ScalarToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.AbstractNotConvertibleToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.AbstractConvertibleToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.MatrixToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.BitwiseOperationToken")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if (className.equals("ptolemy.data.FixToken")) {
            return ptolemy.data.type.BaseType.FIX;
        } else {
            ptolemy.data.type.Type tokenType =
                ptolemy.data.type.BaseType.forClassName(className);
            if (tokenType == null) {
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
            Local local, Unit location, LocalDefs localDefs,
            LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof Local) {
                return getTypeValue(method, (Local)value,
                        stmt, localDefs, localUses);
            } else if (value instanceof CastExpr) {
                return getTypeValue(method, (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                if (field.equals(unknownTypeField)) {
                    return ptolemy.data.type.BaseType.UNKNOWN;
                } else if (field.equals(booleanTypeField)) {
                    return ptolemy.data.type.BaseType.BOOLEAN;
                } else if (field.equals(booleanMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.BOOLEAN_MATRIX;
                } else if (field.equals(byteTypeField)) {
                    return ptolemy.data.type.BaseType.UNSIGNED_BYTE;
                } else if (field.equals(complexTypeField)) {
                    return ptolemy.data.type.BaseType.COMPLEX;
                } else if (field.equals(complexMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.COMPLEX_MATRIX;
                } else if (field.equals(doubleTypeField)) {
                    return ptolemy.data.type.BaseType.DOUBLE;
                } else if (field.equals(doubleMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.DOUBLE_MATRIX;
                } else if (field.equals(fixTypeField)) {
                    return ptolemy.data.type.BaseType.FIX;
                } else if (field.equals(fixMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.FIX_MATRIX;
                } else if (field.equals(intTypeField)) {
                    return ptolemy.data.type.BaseType.INT;
                } else if (field.equals(intMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.INT_MATRIX;
                } else if (field.equals(longTypeField)) {
                    return ptolemy.data.type.BaseType.LONG;
                } else if (field.equals(longMatrixTypeField)) {
                    return ptolemy.data.type.BaseType.LONG_MATRIX;
                } else if (field.equals(objectTypeField)) {
                    return ptolemy.data.type.BaseType.OBJECT;
                } else if (field.equals(stringTypeField)) {
                    return ptolemy.data.type.BaseType.STRING;
                } else {
                    throw new RuntimeException("Unknown type field: " + field);
                }
            } else if (value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the object is initialized
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while (pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if (pair.getUnit() instanceof InvokeStmt) {
                        InvokeStmt useStmt = (InvokeStmt)pair.getUnit();
                        if (useStmt.getInvokeExpr() instanceof SpecialInvokeExpr) {
                            SpecialInvokeExpr constructorExpr = (SpecialInvokeExpr) useStmt.getInvokeExpr();
                            if (constructorExpr.getMethod().getSignature().equals(
                                    "<ptolemy.data.type.ArrayType: void <init>(ptolemy.data.type.Type)>")) {
                                Local arg1Local = (Local)constructorExpr.getArg(0);
                                ptolemy.data.type.Type elementType =
                                    getTypeValue(method, arg1Local,
                                            useStmt, localDefs, localUses);
                                return new ptolemy.data.type.ArrayType(elementType);
                            } else {
                                throw new RuntimeException("ArrayType(unknown): " + stmt);
                            }
                        }
                    }
                }
                throw new RuntimeException("unknown constructor");
            } else if (value instanceof NullConstant) {
                // If we get to an assignment from null, then the
                // attribute statically evaluates to null.
                return null;
            } else {
                throw new RuntimeException("Unknown type of value: " +
                        value + " in " + method);
            }
        } else {
            String string = "More than one definition of = " + local + "\n";
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                string += "Definition = " + i.next().toString();
            }
            throw new RuntimeException(string);
        }
    }

    /** Inline the given invocation expression, given knowledge that the
     *  method was invoked on the given Typeable object, and that the
     *  typeable object has a final resolved type.  The getType method
     *  will be replace with code that returns the resolved type as a
     *  constant, methods that add type constraints are removed, and
     *  other methods that return a value are changed to throw exceptions.
     *  @param body The body in which the invoke occurred.
     *  @param unit The unit in which the invoke occurred.
     *  @param box The value box containing the invoke expression.
     *  @param typeable A reference to an object that has the same
     *  resolve type as the typeable object that the invoke would have
     *  occurred on, has the code actually been executed.
     */
    public static boolean inlineTypeableMethods(JimpleBody body,
            Unit unit, ValueBox box, InstanceInvokeExpr expr,
            Typeable typeable) {
        String name = expr.getMethod().getName();
        boolean doneSomething = false;
        // FIXME name matching here is rather imprecise.
        if (name.equals("getType")) {
            // Replace method calls to getType with the constant type
            // of the typeable.
            try {
                Local typeLocal = PtolemyUtilities.buildConstantTypeLocal(
                        body, unit, typeable.getType());
                box.setValue(typeLocal);
                doneSomething = true;
            } catch (Exception ex) {
                throw new RuntimeException("Type of " + typeable +
                        " could not be determined: " + ex.getMessage());
            }
        } else if (name.equals("getTypeTerm")) {
            // FIXME: This method should be removed.
            //Local exceptionLocal =
            // SootUtilities.createRuntimeException(body, unit,
            //        "Illegal Method Call: getTypeTerm()");
            //body.getUnits().swapWith(unit,
            //        Jimple.v().newThrowStmt(exceptionLocal));
            // doneSomething = true;
        } else if (name.equals("setTypeEquals")) {
            // Remove call.
            body.getUnits().remove(unit);
            doneSomething = true;
        } else if (name.equals("setTypeAtLeast")) {
            // Remove call.
            body.getUnits().remove(unit);
            doneSomething = true;
        } else if (name.equals("setTypeAtMost")) {
            // Remove call.
            body.getUnits().remove(unit);
            doneSomething = true;
        } else if (name.equals("setTypeSameAs")) {
            // Remove call.
            body.getUnits().remove(unit);
            doneSomething = true;
        } else if (name.equals("typeConstraintList")) {
            //FIXME This method should be removed.
            // Local exceptionLocal =
            //    SootUtilities.createRuntimeException(body, unit,
            //      "Illegal Method Call: typeConstraintList()");
            //body.getUnits().swapWith(unit,
            //        Jimple.v().newThrowStmt(exceptionLocal));
            // doneSomething = true;
        }
        return doneSomething;
    }

    /** Return true if the given type references a concrete
     *  ptolemy token type.  In other words It is either a direct
     *  reference to a token, or an array of tokens.
     *  This method only returns true if the token is
     *  an instantiable token.
     */
    public static boolean isConcreteTokenType(Type type) {
        RefType refType;
        if (type instanceof RefType) {
            refType = (RefType)type;
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.baseType instanceof RefType) {
                refType = (RefType)arrayType.baseType;
            } else return false;
        } else return false;
        SootClass tokenClass = refType.getSootClass();
        if (tokenClass.equals(PtolemyUtilities.tokenClass) ||
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
        if (type instanceof RefType) {
            refType = (RefType)type;
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.baseType instanceof RefType) {
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
        if (type instanceof RefType) {
            refType = (RefType)type;
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if (arrayType.baseType instanceof RefType) {
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
    public static SootMethod arrayGetElementMethod;
    public static SootMethod arrayValueMethod;

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

    // Soot class representing the ptolemy.data.BooleanToken class.
    public static SootClass booleanTokenClass;
    // Soot Method representing the BooleanToken(bool) constructor.
    public static SootMethod booleanTokenConstructor;
    public static SootMethod booleanValueMethod;

    public static SootClass booleanMatrixTokenClass;
    public static SootMethod booleanMatrixTokenConstructor;
    public static SootMethod booleanMatrixTokenArrayConstructor;
    public static SootMethod booleanMatrixMethod;

    public static SootField booleanTypeField;
    public static SootField booleanMatrixTypeField;
    public static SootField byteTypeField;

    // Soot class representing java.lang.Class.
    public static SootClass classClass;

    // Soot class representing the ptolemy.data.ComplexToken class.
    public static SootClass complexTokenClass;
    public static SootMethod complexTokenConstructor;
    public static SootMethod complexValueMethod;
    public static SootClass complexMatrixTokenClass;
    public static SootMethod complexMatrixTokenConstructor;
    public static SootMethod complexMatrixMethod;

    public static SootField complexTypeField;
    public static SootField complexMatrixTypeField;

    // Soot class representing the ptolemy.actor.TypedCompositeActor class.
    public static SootClass compositeActorClass;

    // Soot class representing the ptolemy.kernel.CompositeEntity class.
    public static SootClass compositeEntityClass;

    // Soot class representing the ptolemy.kernel.CompositeEntity class.
    public static SootClass componentEntityClass;
    public static RefType componentEntityType;

    // Soot Class representing the ptolemy.kernel.ComponentPort class.
    public static SootClass componentPortClass;
    // Soot Type representing the ptolemy.kernel.ComponentPort class.
    public static Type componentPortType;

    // Soot Method representing Entity.connectionsChanged().
    public static SootMethod connectionsChangedMethod;

    // Soot field corresponding to the debugging flag for named objects.
    public static SootField debuggingField;

    // Soot class representing the ptolemy.data.DoubleToken class.
    public static SootClass doubleTokenClass;
    // Soot Method representing the DoubleToken(int) constructor.
    public static SootMethod doubleTokenConstructor;
    public static SootMethod doubleValueMethod;

    public static SootClass doubleMatrixTokenClass;
    public static SootMethod doubleMatrixTokenConstructor;
    public static SootMethod doubleMatrixMethod;

    public static SootField doubleTypeField;
    public static SootField doubleMatrixTypeField;

    // Soot class representing the ptolemy.kernel.Entity class.
    public static SootClass entityClass;
    public static RefType entityType;

    public static SootClass exceptionClass;
    // Soot class representing the ptolemy.actor.Executable interface.
    public static SootClass executableInterface;
    public static SootMethod executablePrefireMethod;
    public static SootMethod executableFireMethod;
    public static SootMethod executablePostfireMethod;

    // Soot class representing the ptolemy.data.BooleanToken class.
    public static SootClass fixTokenClass;
    public static SootMethod fixTokenConstructor;
    public static SootMethod fixValueMethod;
    public static SootClass fixMatrixTokenClass;
    public static SootMethod fixMatrixTokenConstructor;
    public static SootMethod fixMatrixMethod;

    // Soot class representing the ptolemy.data.type.FixType class.
    public static SootClass fixTypeClass;

    public static SootField fixTypeField;
    public static SootField fixMatrixTypeField;

    public static SootField generalTypeField;

    // SootMethod representing java.lang.Object.getClass()
    public static SootMethod getClassMethod;

    // SootMethod representing ptolemy.actor.IOPort.get().
    public static SootMethod getMethod;
    // SootMethod representing ptolemy.actor.IOPort.getInside().
    public static SootMethod getInsideMethod;

    // SootMethod representing
    // ptolemy.kernel.util.Attribute.getAttribute();
    public static SootMethod getAttributeMethod;

    public static SootMethod getContainerMethod;

    // SootMethod representing
    // ptolemy.actor.Actor.getDirector
    public static SootMethod getDirectorMethod;

    // SootMethod representing
    // ptolemy.kernel.CompositeEntity.getEntity
    public static SootMethod getEntityMethod;

    // SootMethod representing
    // ptolemy.actor.gui.Configuration findEffigy
    public static SootMethod findEffigyMethod;

    // SootMethod representing
    // ptolemy.kernel.util.Settable.getExpression();
    public static SootMethod getExpressionMethod;

    // ptolemy.kernel.util.NamedObj.getFullName
    public static SootMethod getFullNameMethod;

    // ptolemy.kernel.util.NamedObj.getName
    public static SootMethod getNameMethod;

    // ptolemy.kernel.attributes.URIAttribute.getModelURI(NamedObj) method.
    public static SootMethod getModelURIMethod;

    // SootMethod representing
    // ptolemy.kernel.Entity.getPort
    public static SootMethod getPortMethod;

    // SootMethod representing ptolemy.actor.IOPort.hasToken(int).
    public static SootMethod hasTokenMethod;

    // SootClass representing ptolemy.graph.InequalityTerm.
    public static SootClass inequalityTermClass;

    // SootMethod representing ptolemy.kernel.ComponentPort.insertLink().
    public static SootMethod insertLinkMethod;

    // Soot class representing the ptolemy.data.IntToken class.
    public static SootClass intTokenClass;
    // Soot Method representing the IntToken(int) constructor.
    public static SootMethod intTokenConstructor;
    public static SootMethod intValueMethod;

    public static SootClass intMatrixTokenClass;
    public static SootMethod intMatrixTokenConstructor;
    public static SootMethod intMatrixTokenArrayConstructor;
    public static SootMethod intMatrixMethod;

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

    // Soot class representing the ptolemy.data.LongToken class.
    public static SootClass longTokenClass;
    // Soot Method representing the LongToken(long) constructor.
    public static SootMethod longTokenConstructor;
    public static SootMethod longValueMethod;

    public static SootClass longMatrixTokenClass;
    public static SootMethod longMatrixTokenConstructor;
    public static SootMethod longMatrixMethod;

    public static SootField longTypeField;
    public static SootField longMatrixTypeField;

    // SootClass representing ptolemy.actor.Manager
    public static SootClass managerClass;

    // Soot class representing the ptolemy.data.MatrixToken class.
    public static SootClass matrixTokenClass;
    public static SootMethod matrixTokenCreateMethod;
    public static SootMethod matrixGetElementAsTokenMethod;

    // Soot Class representing the ptolemy.data.type.UnsizedMatrixType
    // class.
    public static SootClass matrixTypeClass;

    // SootClass representing ptolemy.kernel.util.NamedObj.
    public static SootClass namedObjClass;

    // SootClass representing java.lang.Object.
    public static SootClass objectClass;
    public static SootMethod objectConstructor;

    public static SootField objectTypeField;

    public static SootClass parameterClass;

    // Soot Class representing the ptolemy.kernel.Port class.
    public static SootClass portClass;

    // Soot Method representing the
    // ptolemy.actor.TypedIOPort.setTypeEquals method.
    public static SootMethod portSetTypeMethod;

    // Soot Class representing the ptolemy.kernel.ComponentRelation class.
    public static SootClass relationClass;

    // Soot class representing the ptolemy.data.RecordToken class.
    public static SootClass recordTokenClass;
    // Soot Method representing the RecordToken(String[], Token[]) constructor.
    public static SootMethod recordTokenConstructor;
    public static SootMethod recordGetMethod;

    // Soot Class representing the ptolemy.data.type.RecordType class.
    public static SootClass recordTypeClass;

    // Soot Type representing the ptolemy.kernel.ComponentRelation class.
    public static Type relationType;

    public static SootClass runtimeExceptionClass;
    public static SootMethod runtimeExceptionConstructor;
    public static SootMethod runtimeExceptionStringConstructor;

    // Soot class representing the ptolemy.data.ScalarToken class.
    public static SootClass scalarTokenClass;

    // SootMethod representing ptolemy.actor.IOPort.send().
    public static SootMethod sendMethod;
    // SootMethod representing ptolemy.actor.IOPort.sendInside().
    public static SootMethod sendInsideMethod;

    // SootMethod representing ptolemy.kernel.util.Settable.setExpression().
    public static SootMethod setExpressionMethod;

    // SootMethod representing ptolemy.actor.IOPort.setInput().
    public static SootMethod setInputMethod;
    // SootMethod representing ptolemy.actor.IOPort.setOutput().
    public static SootMethod setOutputMethod;
    // SootMethod representing ptolemy.actor.IOPort.setMultiport().
    public static SootMethod setMultiportMethod;

    public static SootMethod setNameMethod;
    // Soot Class representing the ptolemy.kernel.util.Settable class.
    public static SootClass settableClass;

    // Soot Type representing the ptolemy.kernel.util.Settable class.
    public static Type settableType;

    // Soot class representing the ptolemy.kernel.util.StringAttribute class.
    public static SootClass stringAttributeClass;

    // The soot class representing java.lang.system
    public static SootClass stringClass;

    public static SootField stringTypeField;

    // Soot class representing the ptolemy.data.StringToken class.
    public static SootClass stringTokenClass;
    public static SootMethod stringTokenConstructor;
    public static SootMethod stringValueMethod;

    //The soot class representing java.lang.system
    public static SootClass systemClass;

    // Soot class representing the ptolemy.data.Token class.
    public static SootClass tokenClass;
    public static SootMethod tokenGetTypeMethod;
    public static SootMethod tokenAddMethod;
    public static SootMethod tokenSubtractMethod;
    public static SootMethod tokenModuloMethod;
    public static SootMethod tokenMultiplyMethod;
    public static SootMethod tokenDivideMethod;
    public static SootMethod tokenEqualsMethod;
    public static SootMethod tokenIsLessThanMethod;
    public static SootMethod tokenNotMethod;
    public static SootMethod tokenBitwiseAndMethod;
    public static SootMethod tokenBitwiseOrMethod;
    public static SootMethod tokenBitwiseXorMethod;
    public static SootMethod tokenBitwiseNotMethod;
    public static SootMethod tokenLeftShiftMethod;
    public static SootMethod tokenRightShiftMethod;
    public static SootMethod tokenLogicalRightShiftMethod;
    public static SootMethod tokenIntValueMethod;
    public static SootMethod tokenZeroMethod;
    public static SootMethod tokenPowMethod;

    // Soot Type representing the ptolemy.data.Token class.
    public static RefType tokenType;

    public static SootMethod toplevelMethod;

    public static SootMethod toStringMethod;

    public static SootClass typeClass;

    public static SootMethod typeConvertMethod;

    public static SootClass typeLatticeClass;

    public static SootField unknownTypeField;

    // Soot class representing the ptolemy.data.IntToken class.
    public static SootClass unsignedByteTokenClass;
    // Soot Method representing the IntToken(unsignedByte) constructor.
    public static SootMethod unsignedByteTokenConstructor;
    public static SootMethod unsignedByteValueMethod;

    // ptolemy.kernel.util.Settable.validate()
    public static SootMethod validateMethod;

    public static SootClass variableClass;
    public static SootMethod variableConstructorWithoutToken;
    public static SootMethod variableConstructorWithToken;
    public static SootMethod variableGetTokenMethod;
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
        classClass =
            Scene.v().loadClassAndSupport("java.lang.Class");
        toStringMethod = objectClass.getMethod("java.lang.String toString()");
        getClassMethod = objectClass.getMethod("java.lang.Class getClass()");
        objectConstructor = objectClass.getMethod("void <init>()");

        stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");

        systemClass = Scene.v().loadClassAndSupport("java.lang.System");
        arraycopyMethod = systemClass.getMethodByName("arraycopy");

        namedObjClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        debuggingField = namedObjClass.getField("boolean _debugging");
        getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute "
                + "getAttribute(java.lang.String)");
        attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");
        attachTextMethod = namedObjClass.getMethod(
                "void _attachText(java.lang.String,java.lang.String)");
        getFullNameMethod =
            namedObjClass.getMethod("java.lang.String getFullName()");
        setNameMethod =
            namedObjClass.getMethod("void setName(java.lang.String)");
        getNameMethod =
            namedObjClass.getMethod("java.lang.String getName()");
        toplevelMethod =
            namedObjClass.getMethod("ptolemy.kernel.util.NamedObj toplevel()");
        getContainerMethod =
            Scene.v().getMethod("<ptolemy.kernel.util.Nameable: ptolemy.kernel.util.Nameable getContainer()>");
        getModelURIMethod =
            Scene.v().loadClassAndSupport("ptolemy.kernel.attributes.URIAttribute")
            .getMethod("java.net.URI getModelURI(ptolemy.kernel.util.NamedObj)");
     

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

        stringAttributeClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.StringAttribute");

        parameterClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Parameter");
        variableClass =
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        variableConstructorWithoutToken = variableClass.getMethod(
                "void <init>(ptolemy.kernel.util.NamedObj,java.lang.String)");
        variableConstructorWithToken = variableClass.getMethod(
                "void <init>(ptolemy.kernel.util.NamedObj,java.lang.String,ptolemy.data.Token)");
        variableSetTokenMethod = variableClass.getMethod(
                "void setToken(ptolemy.data.Token)");
        variableGetTokenMethod = variableClass.getMethod(
                "ptolemy.data.Token getToken()");

        entityClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.Entity");
        entityType = RefType.v(PtolemyUtilities.entityClass);

        connectionsChangedMethod = entityClass.getMethod(
                "void connectionsChanged(ptolemy.kernel.Port)");
        getPortMethod =
            entityClass.getMethod("ptolemy.kernel.Port getPort(java.lang.String)");

        componentEntityClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentEntity");
        componentEntityType = RefType.v(PtolemyUtilities.componentEntityClass);
        compositeEntityClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.CompositeEntity");
        getEntityMethod =
            compositeEntityClass.getMethod("ptolemy.kernel.ComponentEntity getEntity(java.lang.String)");

        executableInterface =
            Scene.v().loadClassAndSupport("ptolemy.actor.Executable");
        executablePrefireMethod =
            executableInterface.getMethodByName("prefire");
        executableFireMethod =
            executableInterface.getMethodByName("fire");
        executablePostfireMethod =
            executableInterface.getMethodByName("postfire");


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
            Scene.v().loadClassAndSupport("ptolemy.kernel.Port");
        componentPortClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentPort");
        componentPortType = RefType.v(componentPortClass);

        relationClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentRelation");
        relationType = RefType.v(relationClass);

        ioportClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedIOPort");
        ioportType = RefType.v(ioportClass);
        portSetTypeMethod =
            Scene.v().getMethod("<ptolemy.actor.TypedIOPort: void setTypeEquals(ptolemy.data.type.Type)>");

        insertLinkMethod = SootUtilities.searchForMethodByName(
                componentPortClass, "insertLink");
        setInputMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setInput(boolean)>");
        setOutputMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setOutput(boolean)>");
        setMultiportMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void setMultiport(boolean)>");
        getMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: ptolemy.data.Token get(int)>");
        getInsideMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: ptolemy.data.Token getInside(int)>");
        hasTokenMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: boolean hasToken(int)>");
        sendMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void send(int,ptolemy.data.Token)>");
        sendInsideMethod = Scene.v().getMethod("<ptolemy.actor.IOPort: void sendInside(int,ptolemy.data.Token)>");

        tokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
        tokenType = RefType.v(tokenClass);
        tokenGetTypeMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.type.Type getType()>");
        tokenZeroMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token zero()>");
        tokenAddMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token add(ptolemy.data.Token)>");
        tokenSubtractMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token subtract(ptolemy.data.Token)>");
        tokenModuloMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token modulo(ptolemy.data.Token)>");
        tokenMultiplyMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token multiply(ptolemy.data.Token)>");
        tokenDivideMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token divide(ptolemy.data.Token)>");
        tokenEqualsMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.BooleanToken isEqualTo(ptolemy.data.Token)>");
        tokenIsLessThanMethod = Scene.v().getMethod("<ptolemy.data.ScalarToken: ptolemy.data.BooleanToken isLessThan(ptolemy.data.ScalarToken)>");
        tokenNotMethod = Scene.v().getMethod("<ptolemy.data.BooleanToken: ptolemy.data.BooleanToken not()>");
        tokenBitwiseAndMethod = Scene.v().getMethod("<ptolemy.data.BitwiseOperationToken: ptolemy.data.BitwiseOperationToken bitwiseAnd(ptolemy.data.Token)>");
        tokenBitwiseOrMethod = Scene.v().getMethod("<ptolemy.data.BitwiseOperationToken: ptolemy.data.BitwiseOperationToken bitwiseOr(ptolemy.data.Token)>");
        tokenBitwiseXorMethod = Scene.v().getMethod("<ptolemy.data.BitwiseOperationToken: ptolemy.data.BitwiseOperationToken bitwiseXor(ptolemy.data.Token)>");
        tokenBitwiseNotMethod = Scene.v().getMethod("<ptolemy.data.BitwiseOperationToken: ptolemy.data.BitwiseOperationToken bitwiseNot()>");
        tokenLeftShiftMethod = Scene.v().getMethod("<ptolemy.data.ScalarToken: ptolemy.data.ScalarToken leftShift(int)>");
        tokenRightShiftMethod = Scene.v().getMethod("<ptolemy.data.ScalarToken: ptolemy.data.ScalarToken rightShift(int)>");
        tokenLogicalRightShiftMethod = Scene.v().getMethod("<ptolemy.data.ScalarToken: ptolemy.data.ScalarToken logicalRightShift(int)>");
        tokenIntValueMethod = Scene.v().getMethod("<ptolemy.data.ScalarToken: int intValue()>");
        tokenPowMethod = Scene.v().getMethod("<ptolemy.data.Token: ptolemy.data.Token pow(int)>");

        arrayTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.ArrayToken");
        arrayTokenConstructor =
            arrayTokenClass.getMethod("void <init>(ptolemy.data.Token[])");
        arrayValueMethod =
            arrayTokenClass.getMethod("ptolemy.data.Token[] arrayValue()");
        arrayGetElementMethod =
            arrayTokenClass.getMethod("ptolemy.data.Token getElement(int)");

        recordTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.RecordToken");
        recordTokenConstructor =
            recordTokenClass.getMethod("void <init>(java.lang.String[],ptolemy.data.Token[])");
        recordGetMethod =
            recordTokenClass.getMethod("ptolemy.data.Token get(java.lang.String)");

        scalarTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.ScalarToken");
        matrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.MatrixToken");
        matrixTokenCreateMethod =
            matrixTokenClass.getMethod("ptolemy.data.MatrixToken create(ptolemy.data.Token[],int,int)");
        matrixGetElementAsTokenMethod =
            matrixTokenClass.getMethod("ptolemy.data.Token getElementAsToken(int,int)");

        doubleTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.DoubleToken");
        doubleTokenConstructor =
            doubleTokenClass.getMethod("void <init>(double)");
        doubleValueMethod =
            scalarTokenClass.getMethod("double doubleValue()");
        doubleMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.DoubleMatrixToken");
        doubleMatrixTokenConstructor =
            doubleMatrixTokenClass.getMethod("void <init>(double[][])");
        doubleMatrixMethod =
            doubleMatrixTokenClass.getMethod("double[][] doubleMatrix()");


        booleanTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.BooleanToken");
        booleanTokenConstructor =
            booleanTokenClass.getMethod("void <init>(boolean)");
        booleanValueMethod =
            booleanTokenClass.getMethod("boolean booleanValue()");
        booleanMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.BooleanMatrixToken");
        booleanMatrixTokenConstructor =
            booleanMatrixTokenClass.getMethod("void <init>(boolean[][])");
        booleanMatrixTokenArrayConstructor =
            booleanMatrixTokenClass.getMethod("void <init>(ptolemy.data.Token[],int,int)");
        booleanMatrixMethod =
            booleanMatrixTokenClass.getMethod("boolean[][] booleanMatrix()");

        unsignedByteTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.UnsignedByteToken");
        unsignedByteTokenConstructor =
            unsignedByteTokenClass.getMethod("void <init>(int)");
        unsignedByteValueMethod =
            scalarTokenClass.getMethod("byte byteValue()");

        intTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.IntToken");
        intTokenConstructor =
            intTokenClass.getMethod("void <init>(int)");
        intValueMethod =
            scalarTokenClass.getMethod("int intValue()");
        intMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.IntMatrixToken");
        intMatrixTokenConstructor =
            intMatrixTokenClass.getMethod("void <init>(int[][])");
        intMatrixTokenArrayConstructor =
            intMatrixTokenClass.getMethod("void <init>(ptolemy.data.Token[],int,int)");
        intMatrixMethod =
            intMatrixTokenClass.getMethod("int[][] intMatrix()");

        fixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.FixToken");
        fixTokenConstructor =
            fixTokenClass.getMethod("void <init>(ptolemy.math.FixPoint)");
        fixValueMethod =
            scalarTokenClass.getMethod("ptolemy.math.FixPoint fixValue()");
        fixMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.FixMatrixToken");
        fixMatrixTokenConstructor =
            fixMatrixTokenClass.getMethod("void <init>(ptolemy.math.FixPoint[][])");
        fixMatrixMethod =
            fixMatrixTokenClass.getMethod("ptolemy.math.FixPoint[][] fixMatrix()");

        complexTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.ComplexToken");
        complexTokenConstructor =
            complexTokenClass.getMethod("void <init>(ptolemy.math.Complex)");
        complexValueMethod =
            scalarTokenClass.getMethod("ptolemy.math.Complex complexValue()");
        complexMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.ComplexMatrixToken");
        complexMatrixMethod =
            matrixTokenClass.getMethod("ptolemy.math.Complex[][] complexMatrix()");

        longTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.LongToken");
        longTokenConstructor =
            longTokenClass.getMethod("void <init>(long)");
        longValueMethod =
            scalarTokenClass.getMethod("long longValue()");
        longMatrixTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.LongMatrixToken");
        longMatrixTokenConstructor =
            longMatrixTokenClass.getMethod("void <init>(long[][])");
        longMatrixMethod =
            longMatrixTokenClass.getMethod("long[][] longMatrix()");


        stringTokenClass =
            Scene.v().loadClassAndSupport("ptolemy.data.StringToken");
        stringTokenConstructor =
            stringTokenClass.getMethod("void <init>(java.lang.String)");
        stringValueMethod =
            stringTokenClass.getMethod("java.lang.String stringValue()");

        typeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.Type");
        typeConvertMethod =
            typeClass.getMethod("ptolemy.data.Token convert(ptolemy.data.Token)");

        arrayTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.ArrayType");
        fixTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.FixType");
        recordTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.RecordType");
        baseTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.BaseType");
        matrixTypeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.UnsizedMatrixType");
        unknownTypeField = baseTypeClass.getFieldByName("UNKNOWN");
        generalTypeField = baseTypeClass.getFieldByName("GENERAL");
        booleanTypeField = baseTypeClass.getFieldByName("BOOLEAN");
        booleanMatrixTypeField =
            baseTypeClass.getFieldByName("BOOLEAN_MATRIX");
        byteTypeField = baseTypeClass.getFieldByName("UNSIGNED_BYTE");
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
        exceptionClass =
            Scene.v().loadClassAndSupport("java.lang.Exception");
        runtimeExceptionConstructor =
            runtimeExceptionClass.getMethod("void <init>()");
        runtimeExceptionStringConstructor =
            runtimeExceptionClass.getMethod("void <init>(java.lang.String)");

        managerClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.Manager");

        Scene.v().loadClassAndSupport("ptolemy.actor.gui.Configuration");
        findEffigyMethod = Scene.v().getMethod("<ptolemy.actor.gui.Configuration: ptolemy.actor.gui.Effigy findEffigy(ptolemy.kernel.util.NamedObj)>");

        inequalityTermClass =
            Scene.v().loadClassAndSupport("ptolemy.graph.InequalityTerm");
    }
}
