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


import java.util.*;
import ptolemy.graph.InequalityTerm;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;

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
     *  @param nameToFieldOrLocal A map from an identifier to a SootField in
     *  entityClass.
     *  @param nameToType A map from an identifier to a ptolemy data type.
     *  @param body The body to add code to.
     *  @param insertPoint The insertion point.
     */
    public static Local generateExpressionCodeBefore(
            Entity entity, SootClass entityClass, String expression,
            Map nameToFieldOrLocal, Map nameToType,
            JimpleBody body, Unit insertPoint) {
        Local local;
        try {
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree =
                parser.generateParseTree(expression);
            ActorCodeGenerationScope scope =
                new ActorCodeGenerationScope(
                        entity, entityClass, nameToFieldOrLocal,
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
     *  @param nameToFieldOrLocal A map from an identifier to a SootField in
     *  entityClass.
     *  @param nameToType A map from an identifier to a ptolemy data type.
     *  @param body The body to add code to.
     */
    public static Local generateExpressionCode(
            Entity entity, SootClass entityClass, String expression,
            Map nameToFieldOrLocal, Map nameToType, JimpleBody body) {
        Stmt insertPoint = Jimple.v().newNopStmt();
        body.getUnits().add(insertPoint);
        return generateExpressionCodeBefore(entity, entityClass, expression,
                nameToFieldOrLocal, nameToType, body, insertPoint);
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
                Entity entity, SootClass entityClass, Map nameToFieldOrLocal,
                Map nameToType, JimpleBody body, Unit insertPoint) {
            _nameToFieldOrLocal = nameToFieldOrLocal;
            _nameToType = nameToType;
            _body = body;
            _insertPoint = insertPoint;
            _units = body.getUnits();
            _entity = entity;
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

            Object identifierReference = _nameToFieldOrLocal.get(name);
            if (identifierReference instanceof Local) {
                return (Local) identifierReference;
            }
            if (identifierReference instanceof SootField) {
                SootField portField = (SootField)identifierReference;

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
            Variable result = null;
            if(_entity != null) {
                result = getScopedVariable(
                        null, _entity, name);
            }
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

                Entity entityContainer =
                    FieldsForEntitiesTransformer.getEntityContainerOfObject(
                            result);
                String deepName = result.getName(entityContainer);

                _units.insertBefore(
                        Jimple.v().newAssignStmt(containerLocal,
                                thisLocal),
                        _insertPoint);
                NamedObj container = _entity;
                while(container != entityContainer) {
                    Local containerLocal2 = Jimple.v().newLocal("container",
                            RefType.v(PtolemyUtilities.namedObjClass));
                    _body.getLocals().add(containerLocal2);
                    _units.insertBefore(
                            Jimple.v().newAssignStmt(containerLocal2,
                                    Jimple.v().newInterfaceInvokeExpr(
                                            containerLocal,
                                            PtolemyUtilities.getContainerMethod)),
                            _insertPoint);
                    container = (NamedObj)container.getContainer();
                    containerLocal = containerLocal2;
                }
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
                return null;
                //  throw new IllegalActionException(
                //         "The ID " + name + " is undefined.");
            }
        }

        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            if (name.equals("time")) {
                return new TypeConstant(BaseType.DOUBLE);
            } else if (name.equals("iteration")) {
                return new TypeConstant(BaseType.INT);
            }

            if (_nameToType.containsKey(name)) {
                return new TypeConstant(
                        (ptolemy.data.type.Type)_nameToType.get(name));
            }

            Variable result = getScopedVariable(
                    null, _entity, name);
            if (result != null) {
                return result.getTypeTerm();
            } else {
                return null;
                //  throw new IllegalActionException(
                //         "The ID " + name + " is undefined.");
            }
        }
        
        public Set identifierSet() {
            if(_entity == null) {
                return Collections.EMPTY_SET;
            } else {
                return getAllScopedVariableNames(null, _entity);
            }
        }

        private Map _nameToFieldOrLocal;
        private Map _nameToType;
        private JimpleBody _body;
        private Unit _insertPoint;
        private Chain _units;
        private Entity _entity;
    }
}
