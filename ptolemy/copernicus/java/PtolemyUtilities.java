/* Ptolemy-specific utilities to use with Soot

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
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

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

import soot.util.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.lang.reflect.Method;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.Typeable;
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
This class consists of ptolemy-specific static utility methods for use with Soot. 

@author Stephen Neuendorffer
@version $Id$
*/
public class PtolemyUtilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

   public static Local buildConstantTokenLocal(Body body, Unit insertPoint, Token token, String localName) {
        Chain units = body.getUnits();
        if(token instanceof ptolemy.data.ArrayToken) {
            ArrayToken arrayToken = (ArrayToken)token;
            RefType tokenType = getSootTypeForTokenType(arrayToken.getElementType());
                // SootClass tokenClass =Scene.v().loadClassAndSupport("ptolemy.data.Token");
            SootClass arrayTokenClass =
                Scene.v().loadClassAndSupport("ptolemy.data.ArrayToken");
            SootMethod arrayTokenConstructor =
                arrayTokenClass.getMethod("void <init>(ptolemy.data.Token[])");
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
                units.insertBefore(Jimple.v().newAssignStmt(
                        Jimple.v().newArrayRef(tokenArrayLocal, IntConstant.v(i)),
                        argLocal), insertPoint);
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
                    insertPoint);;
            // Ugh...  otherwise we get some stupid quotes.
            if(token instanceof StringToken) {
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                tokenConstructor, StringConstant.v(((StringToken)token).stringValue()))),
                        insertPoint);
            } else {
                units.insertBefore(Jimple.v().newInvokeStmt(
                        Jimple.v().newSpecialInvokeExpr(tokenLocal,
                                tokenConstructor, StringConstant.v(token.toString()))),
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
            SootClass typeClass =
                Scene.v().loadClassAndSupport("ptolemy.data.type.BaseType");
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(typeClass, "forName");
            Local typeLocal = Jimple.v().newLocal("type_" + type.toString(),
                    RefType.v(typeClass));
            body.getLocals().add(typeLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newStaticInvokeExpr(typeConstructor,
                            StringConstant.v(type.toString()))),
                    insertPoint);
            return typeLocal;
        } else if(type instanceof ptolemy.data.type.ArrayType) {
            // recurse
            SootClass typeClass =
                Scene.v().loadClassAndSupport("ptolemy.data.type.ArrayType");
            SootMethod typeConstructor =
                SootUtilities.searchForMethodByName(typeClass, "<init>");
            Local elementTypeLocal = buildConstantTypeLocal(body, insertPoint,
                    ((ptolemy.data.type.ArrayType)type).getElementType());
            Local typeLocal = Jimple.v().newLocal("type_arrayOf" +
                    elementTypeLocal.getName(),
                    RefType.v(typeClass));
            body.getLocals().add(typeLocal);
            units.insertBefore(Jimple.v().newAssignStmt(typeLocal,
                    Jimple.v().newNewExpr(RefType.v(typeClass))),
                    insertPoint);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(typeLocal,
                            typeConstructor, elementTypeLocal)),
                    insertPoint);
            return typeLocal;
        }
        throw new RuntimeException("Unidentified type class = " +
                type.getClass().getName());
    }

    /** Insert code into the given body before the given insertion point to call the
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
        units.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(local,
                        constructor, args)));
        return local;
    }

    /** Given a ptolemy token type, return the soot type that can reference
     *  tokens of the ptolemy type.
     */
    // FIXME Records!
    public static RefType getSootTypeForTokenType(ptolemy.data.type.Type type) {
        if(type instanceof ptolemy.data.type.ArrayType) {
            return RefType.v("ptolemy.data.ArrayToken");
        } else if(!type.isInstantiable()) {
            // We should be able to do something better here...  This means that the port
            // has no data.
            return RefType.v("ptolemy.data.Token");
        } else if(type instanceof ptolemy.data.type.BaseType) {
            //   System.out.println("className = " + ((ptolemy.data.type.BaseType)type).getTokenClass().getName());
            return RefType.v(((ptolemy.data.type.BaseType)type).getTokenClass().getName());
        }
        else throw new RuntimeException("unknown type = " + type);
    }

    /** Given a soot type that references a token class, return the ptolemy token type
     *  associated with the token class.  If the type is an array token, then 
     *  the returned type will have an indeterminate element type.
     */
    // FIXME Records!
    public static ptolemy.data.type.Type getTokenTypeForSootType(RefType type) {
        String className = type.getSootClass().getName();
        //  System.out.println("className = " + className);
        if(className.equals("ptolemy.data.ArrayToken")) {
            return new ptolemy.data.type.ArrayType(ptolemy.data.type.BaseType.UNKNOWN);
        } else if(className.equals("ptolemy.data.Token")) {
            return ptolemy.data.type.BaseType.UNKNOWN;
        } else if(className.equals("ptolemy.data.ScalarToken")) {
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
            //Local exceptionLocal = SootUtilities.createRuntimeException(body, unit,
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
            //            Local exceptionLocal = SootUtilities.createRuntimeException(body, unit,
            //      "Illegal Method Call: typeConstraintList()");
            //body.getUnits().swapWith(unit, 
            //        Jimple.v().newThrowStmt(exceptionLocal));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Soot class representing the ptolemy.actor.TypedAtomicActor class.
    public static SootClass actorClass;

    // Soot Type representing the ptolemy.actor.TypedAtomicActor class.
    public static Type actorType;

    // Soot Method representing NamedObj.attributeChanged().
    public static SootMethod attributeChangedMethod;

    // Soot class representing the ptolemy.kernel.util.Attribute class.
    public static SootClass attributeClass;

    // Soot Type representing the ptolemy.kernel.util.Settable class.
    public static Type attributeType;

    // Soot class representing the ptolemy.actor.TypedCompositeActor class.
    public static SootClass compositeActorClass;

    // Soot class representing the ptolemy.actor.Executable interface.
    public static SootClass executableInterface;

    // SootMethod representing
    // ptolemy.kernel.util.Attribute.getAttribute();
    public static SootMethod getAttributeMethod;

    // SootMethod representing
    // ptolemy.kernel.util.Settable.getExpression();
    public static SootMethod getExpressionMethod;

    // SootMethod representing ptolemy.kernel.ComponentPort.insertLink().
    public static SootMethod insertLinkMethod;

    // SootClass representing ptolemy.kernel.util.NamedObj.
    public static SootClass namedObjClass;

    // Soot Class representing the ptolemy.kernel.ComponentPort class.
    public static SootClass portClass;

    // Soot Type representing the ptolemy.kernel.ComponentPort class.
    public static Type portType;

    // SootMethod representing ptolemy.kernel.util.Settable.setExpression().
    public static SootMethod setExpressionMethod;

    // Soot Class representing the ptolemy.kernel.util.Settable class.
    public static SootClass settableClass;

    // Soot Type representing the ptolemy.kernel.util.Settable class.
    public static Type settableType;

    public static SootClass tokenClass;

    public static BaseType tokenType;
    
    public static SootMethod toStringMethod;

    public static SootClass typeClass;
 
    static {
        SootClass objectClass =
            Scene.v().loadClassAndSupport("java.lang.Object");
        toStringMethod = objectClass.getMethod("java.lang.String toString()");
       
        namedObjClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute "
                + "getAttribute(java.lang.String)");
        attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");

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

        executableInterface = 
            Scene.v().loadClassAndSupport("ptolemy.actor.Executable");
        
        actorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedAtomicActor");
        actorType = RefType.v(actorClass);

        compositeActorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");
    
        portClass =
            Scene.v().loadClassAndSupport("ptolemy.kernel.ComponentPort");
        portType = RefType.v(portClass);
        insertLinkMethod = SootUtilities.searchForMethodByName(portClass,
                "insertLink");

        tokenClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
        tokenType = RefType.v(tokenClass);

        typeClass =
            Scene.v().loadClassAndSupport("ptolemy.data.type.Type");
    }
}
