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

import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;
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
            SootClass tokenClass = 
                Scene.v().loadClassAndSupport("ptolemy.data.Token");
            SootClass arrayTokenClass =
                Scene.v().loadClassAndSupport("ptolemy.data.ArrayToken");
            SootMethod arrayTokenConstructor =
                arrayTokenClass.getMethod("void <init>(ptolemy.data.Token[])");
            Type tokenArrayType = ArrayType.v(RefType.v(tokenClass), 1);
            Local tokenArrayLocal = Jimple.v().newLocal(localName + "Array",
                    tokenArrayType);
            body.getLocals().add(tokenArrayLocal);
            // Create the array of tokens.
            units.insertBefore(Jimple.v().newAssignStmt(tokenArrayLocal, 
                    Jimple.v().newNewArrayExpr(RefType.v(tokenClass),
                            IntConstant.v(((ArrayToken)token).length()))),
                    insertPoint);
            // recurse
            for(int i = 0; i < ((ArrayToken)token).length(); i++) {
                Local argLocal = buildConstantTokenLocal(body, insertPoint,
                       ((ArrayToken)token).getElement(i), localName + "_" + i);
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
    public static void callAttributeChanged(Local base, JimpleBody body, Object insertPoint) {
        Local attributeLocal = base;
        // Make sure we have a local of type attribute to pass 
        // to attributeChanged
        Type attributeType = RefType.v("ptolemy.kernel.util.Attribute");
        SootMethod attributeChangedMethod = Scene.v().getMethod(
                "<ptolemy.kernel.util.NamedObj: void attributeChanged(ptolemy.kernel.util.Attribute)>");
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
}
