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
import java.util.Set;

import java.lang.reflect.Method;

import ptolemy.copernicus.kernel.*;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.ScopeExtender;
import ptolemy.data.expr.ScopeExtendingAttribute;

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
//// DataUtilities
/**
This class consists of ptolemy-specific static utility methods for use
with Soot.  This class particularly contains code relating to the
ptolemy.data package.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class DataUtilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Generate code before the given insert point in the given body
     *  in a method of the given entityClass, representing the given
     *  entity that will evaluate the given expression.  Use the given
     *  maps to resolve the types and values of identifiers.
     *  @param entity The entity containing the expression.
     *  @param entityClass The class corresponding to the given entity.
     *  @param expression The expression.
     *  @param nameToField A map from an identifier to a SootField in
     *  entityClass.
     *  @param nameToType A map from an identifier to a ptolemy data type.
     *  @param body The body to add code to.
     *  @param insertPoint The insertion point.
     */
    public static Local generateExpressionCodeBefore(
            Entity entity, SootClass entityClass, String expression,
            Map nameToField, Map nameToType,
            JimpleBody body, Stmt insertPoint) {

        Local local;
        try {
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree =
                parser.generateParseTree(expression);
            ActorCodeGenerationScope scope =
                new ActorCodeGenerationScope(
                        entity, entityClass, nameToField,
                        nameToType, body, insertPoint);
            ParseTreeCodeGenerator generator =
                new ParseTreeCodeGenerator();
            local = generator.generateCode(
                    parseTree, body, insertPoint, scope);
        } catch (IllegalActionException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
        return local;
    }

    /** Add code to the given body in a method of the given
     *  entityClass, representing the given entity that will evaluate
     *  the given expression.  Use the given maps to resolve the types
     *  and values of identifiers.
     *  @param entity The entity containing the expression.
     *  @param entityClass The class corresponding to the given entity.
     *  @param expression The expression.
     *  @param nameToField A map from an identifier to a SootField in
     *  entityClass.
     *  @param nameToType A map from an identifier to a ptolemy data type.
     *  @param body The body to add code to.
     */
    public static Local generateExpressionCode(
            Entity entity, SootClass entityClass, String expression,
            Map nameToField, Map nameToType, JimpleBody body) {
        Stmt insertPoint = Jimple.v().newNopStmt();
        body.getUnits().add(insertPoint);
        return generateExpressionCodeBefore(entity, entityClass, expression,
                nameToField, nameToType, body, insertPoint);
    }

    /** An inner class used by the <i>generateExpressionCode()</I>
     *  method.  This class creates the correct code to resolve
     *  identifier values when they are discovered in traversing the
     *  parse tree.
     */
    public static class ActorCodeGenerationScope 
        extends ptolemy.data.expr.ModelScope
        implements CodeGenerationScope {
        public ActorCodeGenerationScope(
                Entity entity, SootClass entityClass, Map nameToField,
                Map nameToType, JimpleBody body, Stmt insertPoint) {
            _nameToField = nameToField;
            _nameToType = nameToType;
            _body = body;
            _insertPoint = insertPoint;
            _units = body.getUnits();
            _entity = entity;
            _entityClass = entityClass;
        }

        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            throw new IllegalActionException("The ID " + name +
                    " does not have a value");
        }

        public Local getLocal(String name)
                throws IllegalActionException {
            Local thisLocal = _body.getThisLocal();

            if (name.equals("time")) {
                throw new RuntimeException("time not supported");
            } else if (name.equals("iteration")) {
                throw new RuntimeException("iteration not supported");
            }
            //                 Local intLocal = Jimple.v().newLocal("intLocal",
            //                         IntType.v());
            //                 _body.getLocals().add(intLocal);
            //                 _units.add(
            //                         Jimple.v().newAssignStmt(intLocal,
            //                                 Jimple.v().newInstanceFieldRef(
            //                                         thisLocal,
            //                                         entityClass.getFieldByName("_iteration"))));
            //                 Local tokenLocal =
            //                     PtolemyUtilities.addTokenLocal(_body, "iterationLocal",
            //                         PtolemyUtilities.intTokenClass,
            //                         PtolemyUtilities.intTokenConstructor,
            //                         intLocal);
            //                 return tokenLocal;
            //             }

            SootField portField = (SootField)_nameToField.get(name);

            if (portField != null) {

                Local portLocal = Jimple.v().newLocal("portToken",
                        PtolemyUtilities.getSootTypeForTokenType(
                                getType(name)));
                _body.getLocals().add(portLocal);

                Local tokenLocal = Jimple.v().newLocal("portToken",
                        PtolemyUtilities.tokenType);
                _body.getLocals().add(tokenLocal);

                _units.insertBefore(
                        Jimple.v().newAssignStmt(tokenLocal,
                                Jimple.v().newInstanceFieldRef(
                                        thisLocal, portField)),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(portLocal,
                                Jimple.v().newCastExpr(
                                        tokenLocal,
                                        PtolemyUtilities
                                        .getSootTypeForTokenType(
                                                getType(name)))),
                        _insertPoint);

                return portLocal;
            }

            // Look for parameter in actor.
            NamedObj container = _entity;
            Variable result = getScopedVariable(
                    null, _entity, name);
            if (result != null) {
                // Insert code to get a ref to the variable,
                // and to get the token of that variable.
                Local containerLocal = Jimple.v().newLocal("container",
                        RefType.v(PtolemyUtilities.namedObjClass));
                _body.getLocals().add(containerLocal);
                Local attributeLocal = Jimple.v().newLocal("attribute",
                        PtolemyUtilities.attributeType);
                _body.getLocals().add(attributeLocal);
                Local tokenLocal = Jimple.v().newLocal("token",
                        PtolemyUtilities.tokenType);
                _body.getLocals().add(tokenLocal);
                
                NamedObj toplevel = _entity.toplevel();
                String deepName = result.getName(toplevel);
                
                
                _units.insertBefore(
                        Jimple.v().newAssignStmt(containerLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                            thisLocal,
                                            PtolemyUtilities.toplevelMethod)),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(attributeLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                        containerLocal,
                                        PtolemyUtilities.getAttributeMethod,
                                        StringConstant.v(deepName))),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(attributeLocal,
                                Jimple.v().newCastExpr(attributeLocal,
                                        RefType.v(
                                                PtolemyUtilities.variableClass))),
                        _insertPoint);
                _units.insertBefore(
                        Jimple.v().newAssignStmt(tokenLocal,
                                Jimple.v().newVirtualInvokeExpr(
                                            attributeLocal,
                                            PtolemyUtilities.variableGetTokenMethod)),
                        _insertPoint);
                
                return tokenLocal;
            } else {
                throw new IllegalActionException(
                        "The ID " + name + " is undefined.");

            }
        }
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            if (_nameToType.containsKey(name)) {
                return (ptolemy.data.type.Type)_nameToType.get(name);
            }

            Variable result = getScopedVariable(
                    null, _entity, name);
            if (result != null) {
                return result.getType();
            } else {
                throw new IllegalActionException(
                        "The ID " + name + " is undefined.");
            }
        }
        public Set identifierSet() {
            return getAllScopedVariableNames(null, _entity);
        }
        
        private Map _nameToField;
        private Map _nameToType;
        private JimpleBody _body;
        private Stmt _insertPoint;
        private Chain _units;
        private Entity _entity;
        private SootClass _entityClass;
    }
}
