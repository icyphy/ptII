/* A Java AST visitor that finds the most specific Token type allowable for
   variables declared as abstract Tokens (Token, ScalarToken, and MatrixToken).

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.codegen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.graph.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.lang.*;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A Java AST visitor that finds the most specific Token type allowable for
variables declared as abstract Tokens (Token, ScalarToken, and MatrixToken).

@author Jeff Tsay
@version $Id$
*/
public class SpecializeTokenVisitor extends ResolveVisitorBase {

    public SpecializeTokenVisitor(ActorCodeGeneratorInfo actorInfo,
            PtolemyTypeVisitor typeVisitor) {
        this(actorInfo, typeVisitor,
                new InequalitySolver(_cpo), new HashMap());
    }

    public SpecializeTokenVisitor(ActorCodeGeneratorInfo actorInfo,
            PtolemyTypeVisitor typeVisitor,
            InequalitySolver solver,
            Map declToTermMap) {
        super(TM_CUSTOM);
        _actorInfo = actorInfo;

        _typeID =
            (PtolemyTypeIdentifier) typeVisitor.typePolicy().typeIdentifier();
        _typeVisitor = typeVisitor;

        _solver = solver;
        _declToTermMap = declToTermMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Use the CompileUnitNodes (which presumably contain classes in a
     *  hierarchy), to return a Map from all Token declarations that appear
     *  in the compile units to TypeNameNodes representing the most specific
     *  type possible that the declaration could have.
     */
    public static Map specializeTokens(List nodeList,
            ActorCodeGeneratorInfo actorInfo, PtolemyTypeVisitor typeVisitor) {

	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.specializeTokens(): "
			       + "Start:" 
			       + "\n nodeList.size(): " + nodeList.size()
			       + "\n actorInfo: " + actorInfo
			       + "\n typeVisitor: " + typeVisitor
			       );
	}
        Iterator nodeItr = nodeList.iterator();

        InequalitySolver solver = new InequalitySolver(_cpo);
        HashMap declToTermMap = new HashMap();

        // use the same visitor, so that the same inequality solver and decl to
        // inequality term map is used in each invocation

        SpecializeTokenVisitor specializeVisitor =
            new SpecializeTokenVisitor(actorInfo, typeVisitor,
                    solver, declToTermMap);

        while (nodeItr.hasNext()) {
            CompileUnitNode unitNode = (CompileUnitNode) nodeItr.next();
            unitNode.accept(specializeVisitor, null);
        }

	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.specializeTokens(): "
			       + "\n solver:" + solver
			       + "\n _cpo:" + _cpo.description()
			       + "\n declToTermMap:\n"
			       + _declToTermMapDescription(declToTermMap)
			       + "\n solver:\n"
			       + solver.description()
			       );
	}
        boolean ok = solver.solveLeast();

        if (!ok) {
            System.err.println("Warning: "
			       + "SpecializedTokenVisitor.specializeTokens(): "
			       + "unable to solve for Token types.");

            Iterator unsatisfiedItr = solver.unsatisfiedInequalities();

            System.err.println("SpecializedTokenVisitor.specializeTokens(): "
			       + "Warning: unsatisfied inequalities:");

            while (unsatisfiedItr.hasNext()) {
                System.err.println("Warning: " 
				   + unsatisfiedItr.next().toString());
            }
            System.err.println("Warning: end of unsatisfied inequalities");
        }

        HashMap declToTokenTypeMap = new HashMap();

        Iterator termItr = declToTermMap.values().iterator();

        while (termItr.hasNext()) {
            InequalityTerm term = (InequalityTerm) termItr.next();

	    if (_debug) {
		System.out.println("SpecializedTokenVisitor.specializeTokens(): "
				   + "term = " + term.toString());
	    }

            ClassDecl value = (ClassDecl) term.getValue();

            TypedDecl typedDecl = (TypedDecl) term.getAssociatedObject();

            if ((value == PtolemyTypeIdentifier.DUMMY_LOWER_BOUND) ||
                    (value == PtolemyTypeIdentifier.TOKEN_DECL) ||
                    (value == PtolemyTypeIdentifier.SCALAR_TOKEN_DECL) ||
                    (value == PtolemyTypeIdentifier.ARRAY_TOKEN_DECL) ||
                    (value == PtolemyTypeIdentifier.MATRIX_TOKEN_DECL)) {
                System.err.println("Warning: "
				   + "SpecializedTokenVisitor"
				   + ".specializeTokens(): "
				   + "could not solve for specific "
				   + "token type for declaration '"
				   + typedDecl.getName()
				   + "' in "
				   + actorInfo.actor.getName()
				   + ".\n term = " + value
				   + " which is unsupported. \n"
				   + "Try setting the type with something like"
				   + "\n'fileWriter.input.setTypeEquals"
				   + "(BaseType.INT);'");

                // Replace the declaration type with "Token" as an indication
                // for later passes.
                declToTokenTypeMap.put(typedDecl,
                        PtolemyTypeIdentifier.TOKEN_TYPE.clone());

		// FIXME: This is totally wrong
                //System.err.println("Warning: SpecializeTokenVisitor"
		//		   + ".specializeTokens(): defaulting to "
		//		   + "integer");
                //declToTokenTypeMap.put(typedDecl,
                //        PtolemyTypeIdentifier.INT_TOKEN_TYPE.clone());
            } else {
                TypeNameNode typeNode =
                    (TypeNameNode) value.getDefType().clone();

                declToTokenTypeMap.put(typedDecl, typeNode);
            }
        }

	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.specializeTokens(): "
                    + "End . . .");
        }
        return declToTokenTypeMap;
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return null;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.visitArrayTypeNode()"
			       + node);
	}
        return null;
    }

    /** Return the inequality solver and the decl to inequality term map
     *  in an array of length 2.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        TNLManip.traverseList(this, null, node.getDefTypes());

        return new Object[] { _solver, _declToTermMap };
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        // visit the fields first to initialize the decl -> inequality term map

        List memberList = node.getMembers();

        Iterator memberItr = memberList.iterator();

        while (memberItr.hasNext()) {
            TreeNode member = (TreeNode) memberItr.next();

            if (member.classID() == FIELDDECLNODE_ID) {
                visitFieldDeclNode((FieldDeclNode) member, null);
            }
        }

        // visit the rest of the members

        memberItr = memberList.iterator();

        while (memberItr.hasNext()) {
            TreeNode member = (TreeNode) memberItr.next();

            if (member.classID() != FIELDDECLNODE_ID) {
                member.accept(this, null);
            }
        }
        return null;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node,
            LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        String methodName = node.getName().getIdent();
	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.visitMethodDeclNode():" +
			       methodName);
        }
        // get rid of the following methods
        if ( methodName.equals("attributeChanged") ||
                methodName.equals("attributeTypeChanged") ||
		methodName.equals("clone") ||
		methodName.equals("iterate")
             ) {
	    System.out.println(
                    "SpecializedTokenVisitor.visitMethodDeclNode(): " +
                    "removing " + methodName);

            return NullValue.instance;
        }

        // make sure we process the parameters first
        TNLManip.traverseList(this, null, node.getParams());

        node.getBody().accept(this, null);

        return null;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        // make sure we process the parameters first
        TNLManip.traverseList(this, null, node.getParams());

        try {
	    node.getBody().accept(this, null);
	} catch (Exception e) {
	    throw new RuntimeException("SpecializedTokenVisitor.visitConstructorDeclNode()" + node + " ");
	}
        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node,
            LinkedList args) {
        // do not look into interfaces
        return null;
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        TypeNode type = node.getDefType();
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);

        InequalityTerm term = _makeVariableTerm(type, typedDecl);

        if (term != null) {
            // add to the map from decls to inequality terms
            _declToTermMap.put(typedDecl, term);

            // constrain the type (it must be added the inequalities)
            _solver.addInequality(new Inequality(term,
                    _makeConstantTerm(type, null)));
        }
        return null;
    }


    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.visitArrayAccessNode():" +
			       node);
	}
        return _visitExprNode(node);
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
	if (_debug) {
	    System.out.println("SpecializedTokenVisitor.visitObjectFieldAccessNode:");
	}
        return _visitVariableNode(node);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        return _visitVariableNode(node);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {

        TypeNode returnType = _typeVisitor.type(node);

        InequalityTerm retval =
            (InequalityTerm) _makeConstantTerm(returnType, null);

	if (_debug) {
	    System.err.println("SpecializeTokenVisitor.visitMethodCallNode(): "
			       + " returnType: " + returnType + "\n  node: "
			       + node + "\n node.getArgs(): " + node.getArgs());
	}

        List argTerms = TNLManip.traverseList(this, null, node.getArgs());

        FieldAccessNode fieldAccessNode = (FieldAccessNode) node.getMethod();

        ExprNode accessedObj =
            (ExprNode) ExprUtility.accessedObject(fieldAccessNode);

        // if this is a static method call, we can't do any more.
        if (accessedObj == null) {
            return null;
        }

        Object accessedObjTermObj = accessedObj.accept(this, null);

        int accessedObjKind = _typeID.kind(_typeVisitor.type(accessedObj));

        MethodDecl methodDecl =
            (MethodDecl) JavaDecl.getDecl((NamedNode) fieldAccessNode);

        String methodName = methodDecl.getName();

        if (_typeID.isSupportedTokenKind(accessedObjKind)) {

            // If the return type is not a subclass of Token, we can't do
            // any more.
            if (retval == null) {
                return null;
            }

            InequalityTerm accessedObjTerm =
                (InequalityTerm) accessedObjTermObj;

            boolean variableAccessedTerm =
                (accessedObjTerm instanceof _VariableTerm);

            if (variableAccessedTerm) {
                retval = _makeVariableTerm(returnType, null);
            }

            int numArgs = argTerms.size();

            switch (numArgs) {

            case 0:
                if (methodName.equals("one") || methodName.equals("zero")) {
                    retval = accessedObjTerm;
                }
                break;

            case 1:
                {
                    InequalityTerm firstArgTerm = null;
		    if (argTerms.get(0) == ptolemy.lang.NullValue.instance) {
			System.err.println("SpecializeTokenVisitor."
					   + "visitMethodCallNode(): "
					   + " argTerms.get(0) == NullValue"
                                + "\n methodName = " + methodName
                                + "\n this = " + this
                                + "\n node = " + node
                                + "\n node.getArgs() = "
                                + node.getArgs()
                                + "\n args = " + args
                                + "\n argTerms = " + argTerms
                                + "\n argTerms.get(0) = "
                                + (( argTerms == null) ?
                                        "null" : argTerms.get(0))
					   );
			return retval;
		    } 
		    try {
			firstArgTerm = (InequalityTerm) argTerms.get(0);
		    } catch (ClassCastException e) {
			System.err.println("SpecializeTokenVisitor."
                                + "visitMethodCallNode: " + e
                                + "\n methodName = " + methodName
                                + "\n this = " + this
                                + "\n node = " + node
                                + "\n node.getArgs() = "
                                + node.getArgs()
                                + "\n args = " + args
                                + "\n argTerms = " + argTerms
                                + "\n argTerms.get(0) = "
                                + (( argTerms == null) ?
                                        "null" : argTerms.get(0))
					   );
			throw e;
		    }
                    if (!variableAccessedTerm &&
                            (firstArgTerm instanceof _VariableTerm)) {
                        retval = _makeVariableTerm(returnType, null);
			if (retval == null ) {
			    System.err.println("SpecializeTokenVisitor."
					       + "visitMethodCallNode(): "
					       + "2 mvt->null");
			}
			return retval;
                    }

                    if (methodName.equals("add") ||
                            methodName.equals("addReverse") ||
                            methodName.equals("subtract") ||
                            methodName.equals("subtractReverse") ||
                            methodName.equals("multiply") ||
                            methodName.equals("multiplyReverse") ||
                            methodName.equals("divide") ||
                            methodName.equals("divideReverse") ||
                            methodName.equals("modulo") ||
                            methodName.equals("moduloReverse")) {
                        // Constrain the return value to be >= both
                        // the accessedObject and the first argument

                        _solver.addInequality(new Inequality(accessedObjTerm,
                                retval));
                        _solver.addInequality(new Inequality(firstArgTerm,
                                retval));

                    } else if (methodName.equals("convert")) {
                        // constrain the return value to be equal the
                        // accessedObject also constain the first argument to
                        // be <= the accessedObject

                        retval = accessedObjTerm;

                        _solver.addInequality(new Inequality(firstArgTerm,
                                accessedObjTerm));
                    }
                } // numArgs == 1
                break;
            }

        } else if (_typeID.isSupportedPortKind(accessedObjKind)) {
            // use the resolved type of ports to do type inference

            TypedDecl typedDecl =
                (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
            String varName = typedDecl.getName();

            TypedIOPort port =
                (TypedIOPort) _actorInfo.portNameToPortMap.get(varName);

            if (port == null) {
                System.err.println("Warning: method called on port " +
                        " that is not a field of the actor");
                return null;
            }

            TypeNameNode portTypeNode;
	    try {
		portTypeNode =
		    _typeID.typeNodeForTokenType(port.getType());
	    } catch (Exception e) {
		throw new RuntimeException("SpecializedTokenVisitor.visitMethodCallNode(): methodName: '" + methodName + "' port.getType(): '" + port.getType() + "' varName: '" + varName + "' accessedObj: '" + accessedObj + "' typeDecl:"
 + typedDecl);
	    }
            if (methodName.equals("broadcast")) {
                // first argument is a token, constrain it
                InequalityTerm firstArgTerm = (InequalityTerm) argTerms.get(0);
                _solver.addInequality(new Inequality(firstArgTerm,
                        _makeConstantTerm(portTypeNode, null)));
                return null; // return type is void
            } else if (methodName.equals("get")) {
                return _makeConstantTerm(portTypeNode, null);
            } else if (methodName.equals("send")) {
                if (argTerms.size() == 3) {
                    // FIXME: iterate() uses a three arg send(), we should
                    // consider dealing with that.
                    System.err.println("Warning: "
				       + "SpecializedTokenVisitor"
				       + ".specializeTokens(): "
				       + "found 3 argument send call. "
                            /*
				       + " We are ignoring the 3rd arg (count)"
				       + " Node was: "
				       + node
				       + "\n node.getArgs() = "
				       + node.getArgs()
				       + "\n args = " + args
				       + "\n argTerms = " + argTerms
				       + "\n argTerms.get(0) = "
				       + (( argTerms == null) ?
					  "null" : argTerms.get(0))
				       + "\n argTerms.get(1) = "
				       + (( argTerms == null) ?
					  "null" : argTerms.get(1))
                            */
				       );
                    // Second argument is a token, constrain it.
		    if (argTerms.get(1) == ptolemy.lang.NullValue.instance) {
			System.err.println("Warning"
					   + "SpecializeTokenVisitor."
					   + "visitMethodCallNode(): "
					   + " argTerms.get(1) == NullValue"
					   + "\n methodName = " + methodName
                                /*
					   + "\n this = " + this
					   + "\n node = " + node
                                */
					   );
		    } else {
			// Second arg is a Token[]
			// Really, we want to get the BaseType?
			// There is a implicit dereference here
			InequalityTerm secondArgTerm =
			    (InequalityTerm) argTerms.get(1);
			
			_solver.addInequality(new Inequality(secondArgTerm,
				     _makeVariableTerm(portTypeNode, null)));
		    }
		    return null; // return type is void
                } else {
		    if (_debug) {
			System.out.println("SpecializeTokenVisitor."
					   + "visitMethodCallNode(): "
					   + "2 arg send: " 
					   + "argTerms.get(1): "
					   + argTerms.get(1)
                                + "\n node = " + node
                                + "\n node.getArgs() = "
                                + node.getArgs()
                                + "\n args = " + args
                                + "\n argTerms = " + argTerms
					   );
		    }
                    // Second argument is a token, constrain it.
                    InequalityTerm secondArgTerm =
                        (InequalityTerm) argTerms.get(1);
                    _solver.addInequality(new Inequality(secondArgTerm,
                            _makeConstantTerm(portTypeNode, null)));

		    return null; // return type is void
                }
            } // support getArray ...

        } else if (accessedObjKind ==
                PtolemyTypeIdentifier.TYPE_KIND_PARAMETER) {
            // use the tokens returned by parameters to do type inference

            if (methodName.equals("getToken")){
                TypedDecl typedDecl =
                    (TypedDecl) JavaDecl.getDecl((NamedNode) accessedObj);
                String varName = typedDecl.getName();

                Token token =
                    (Token) _actorInfo.parameterNameToTokenMap.get(varName);

                if (token == null) {
                    System.err.println("Warning: "
				       + "SpecializedTokenVisitor"
				       + ".specializeTokens(): "
				       + "getToken() called on "
				       + "parameter that is not a field "
				       + "of the actor");
                    return null;
                }

                TypeNameNode tokenTypeNode =
                    _typeID.typeNodeForTokenType(token.getType());

		return _makeConstantTerm(tokenTypeNode, null);
            }
        }

        return retval;
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _visitExprNode(node);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        // Assume that all casts succeed.

        InequalityTerm term = null;
        try {
            term = (InequalityTerm) node.getExpr().accept(this, null);
            if (term != null) {
                _solver.addInequality(new Inequality(term,
                        _makeConstantTerm(node.getDtype(), null)));
            }
        } catch (RuntimeException exception) {
            System.err.println("SpecializedTokenVisitor.CastNode(): "
                    + "RuntimeException was thrown on: \n"
                    + node);
            throw exception;
        }
        return term;
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        InequalityTerm leftTerm =
            (InequalityTerm) node.getExpr1().accept(this, null);
        InequalityTerm rightTerm =
            (InequalityTerm) node.getExpr2().accept(this, null);

        if ((leftTerm != null) && (rightTerm != null)) {
            _solver.addInequality(new Inequality(rightTerm, leftTerm));
        }

        return leftTerm;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a variable inequality term corresponding to the given type,
     *  with the associated decl, which may be null. If type is not a
     *  supported token type, return null.
     */
    private _VariableTerm _makeVariableTerm(TypeNode type, TypedDecl decl) {
        int kind = _typeID.kind(type);
	if (_debug) {
	    System.err.println("SpecializedTokenVisitor._makeVariableTerm():"
			       + "Entering . . . " 
			       + "\n type: " + type
			       + "\n decl: " + decl);
	}
        if (!_typeID.isSupportedTokenKind(kind)) {
            if (! _useArrays) { 
                return null;
            }
	    // FIXME: What about Matrices?
	    // type.classID() will return an integer from nodetypes.NodeClassID.
	    if (type.classID() != ARRAYTYPENODE_ID || 
		!((NameNode)(((ArrayTypeNode)type).getBaseType().getChild(0))).getIdent().equals("Token")) {
		System.err.println("SpecializedTokenVisitor._makeVariableTerm:"
			       + " Token " + kind + " not supported");
		return null;
	    } else {
		if (_debug) {

		    System.err.println("SpecializedTokenVisitor._makeVariableTerm:"
				       + " we have a Token");
		}
		System.out.println("Warning: "
				   + "SpecializedTokenVisitor._makeVariableTerm:"
				   + " defaulting to int array");
		TypeNode tmpType = PtolemyTypeIdentifier.INT_ARRAY_TOKEN_TYPE;
		if (_debug) {
		    System.err.println("SpecializedTokenVisitor._makeVariableTerm:"
				       + "\n type: " + type
				       + "\n tmpType: " + tmpType
				       + "\n baseType: "
				       + ((ArrayTypeNode)type).getBaseType()
				       );
		}
		type = tmpType;
	    }
	}

	if (_debug) {
	    System.err.println("SpecializedTokenVisitor._makeVariableTerm:"
			       + "\n JavaDecl.getDecl((NamedNode) type): "
			       + JavaDecl.getDecl((NamedNode) type)
			       + "Leaving . . .");
	}
        return new _VariableTerm((ClassDecl)
                JavaDecl.getDecl((NamedNode) type), decl);
    }

    /** Return a constant inequality term corresponding to the given type,
     *  with the associated decl, which may be null. If type is not a
     *  supported token type, return null.
     */
    private _ConstantTerm _makeConstantTerm(TypeNode type, TypedDecl decl) {
        int kind = _typeID.kind(type);
        if (!_typeID.isSupportedTokenKind(kind)) {
	    // FIXME: What about Matrices?
	    // type.classID() will return an integer from nodetypes.NodeClassID.
            if (! _useArrays) { 
                return null;
            }
	    if (type.classID() != ARRAYTYPENODE_ID ||


		!((NameNode)(((ArrayTypeNode)type).getBaseType().getChild(0))).getIdent().equals("Token")) {
		System.err.println("SpecializedTokenVisitor._makeConstantTerm("
			       + "type= " + type
			       + ", decl = " + decl + ") _typeId: "
			       + _typeID + " is not a supported Token kind, kind = "
			       + kind + " "
			       + ((kind > TypeIdentifier.TYPE_KINDS)
			       ?  _typeID.typeNodeForKind(kind)
			       .getName().getIdent()
			       : "unknown"));
		return null;
	    } else {
		if (_debug) {
		    System.err.println("SpecializedTokenVisitor._makeConstantTerm:"
				       + " we have a Token");
		}
		System.out.println("SpecializedTokenVisitor._makeConstantTerm:"
				   + "Warning: defaulting to int array");
		TypeNode tmpType = PtolemyTypeIdentifier.INT_ARRAY_TOKEN_TYPE;

		if (_debug) {
		    System.err.println("SpecializedTokenVisitor._makeConstantTerm:"
				       + "\n type: " + type
				       + "\n tmpType: " + tmpType
				       + "\n baseType: " + ((ArrayTypeNode)type).getBaseType());
		}
		type = tmpType;
	    }
        }


        return new _ConstantTerm((ClassDecl)
                JavaDecl.getDecl((NamedNode) type), decl);
    }

    private InequalityTerm _visitExprNode(ExprNode node) {
        _defaultVisit(node, null);
        return _makeConstantTerm(_typeVisitor.type(node), null);
    }

    private InequalityTerm _visitVariableNode(NamedNode node) {
        _defaultVisit((TreeNode) node, null);

        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl(node);

        InequalityTerm term =
            (InequalityTerm) _declToTermMap.get(typedDecl); // may be null

        return term;
    }

    private Object _visitVarInitDeclNode(VarInitDeclNode node) {
        TypeNode type = node.getDefType();
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);

        InequalityTerm term = _makeVariableTerm(type, typedDecl);
        InequalityTerm initExprTerm =
            (InequalityTerm) node.getInitExpr().accept(this, null);

	if (_debug) {
	    System.out.println("SpecializedTokenVisitor._visitVarInitDeclNode: "
			       + "\n node" + node
			       + "\n term: " + term
			       + "\n type: " + type
			       + "\n typedDecl: " + typedDecl
			       + "\n initExprTerm: " + initExprTerm
			       );
	}
        if (term != null) {
            // add to the map from decls to inequality terms
            _declToTermMap.put(typedDecl, term);

            // constrain the type (it must be added the inequalities)
            _solver.addInequality(new Inequality(term,
                    _makeConstantTerm(type, null)));

            if (initExprTerm != null) {
                _solver.addInequality(new Inequality(initExprTerm, term));
            }
        }
        return null;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ActorCodeGeneratorInfo _actorInfo;

    private PtolemyTypeIdentifier _typeID;

    private PtolemyTypeVisitor _typeVisitor;

    /** A Map from TypeDecls to InequalityTerms. */
    private Map _declToTermMap;

    private static final DirectedAcyclicGraph _cpo;
    private InequalitySolver _solver;

    /** Return a description of declToTermMap. */
    private static String _declToTermMapDescription(HashMap declToTermMap) {
        Iterator termItr = declToTermMap.values().iterator();
	StringBuffer results = new StringBuffer() ;
        while (termItr.hasNext()) {
            InequalityTerm term = (InequalityTerm) termItr.next();
	    results.append(term.toString() + "\n");
	}
	return results.toString();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private static class _ConstantTerm implements InequalityTerm {
        public _ConstantTerm(ClassDecl classDecl, TypedDecl decl) {
            _classDecl = classDecl;
            _decl = decl;
        }

        public void fixValue() {}

        public Object getValue() {
            return _classDecl;
        }

        public Object getAssociatedObject() {
            return _decl;
        }

        // Constant terms do not contain any variables
        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        public void initialize(Object e) throws IllegalActionException {
            setValue(e);
        }

        // Constant terms are not settable
        public boolean isSettable() { return false; }

        public boolean isValueAcceptable() { return true; }

        public void setValue(Object e) throws IllegalActionException {
            throw new IllegalActionException(
                    "_ConstantTerm.setValue(): This term is a constant");
        }

        public String toString() {
            return "{_ConstantTerm: _decl = " + _decl + ", _classDecl = " +
		((_classDecl == null) ?
		 "null" : _classDecl.getName()
		 + "}");
        }

        public void unfixValue() {}

        private ClassDecl _classDecl;
        private TypedDecl _decl;
    }

    private static class _VariableTerm implements InequalityTerm {
        public _VariableTerm(ClassDecl classDecl, TypedDecl decl) {
            _classDecl = classDecl;
            _decl = decl;
        }

        public void fixValue() { _fixed = true; }

        public Object getValue() { return _classDecl; }

        public Object getAssociatedObject() { return _decl; }

        public InequalityTerm[] getVariables() {
            return new InequalityTerm[] { this };
        }

        public void initialize(Object e) {
            setValue(e);
        }

        // Variable terms are settable
        public boolean isSettable() { return true; }

        public boolean isValueAcceptable() {
            return true;
        }

        public void setValue(Object e) {
            if (!_fixed) {
                _classDecl = (ClassDecl) e;
            }
        }

        public String toString() {
            return "{_VariableTerm: _decl = " + _decl + ", _classDecl = " +
		((_classDecl == null) ?
		 "null" : _classDecl.getName()
		 + "}");
        }

        public void unfixValue() { _fixed = false; }

        private ClassDecl _classDecl;
        private TypedDecl _decl;
        private boolean _fixed = false;
    }

    static {
        System.out.println("SpecializedTokenVisitor<static>");
        // construct the type lattice
        _cpo = new DirectedAcyclicGraph(21);
        _cpo.add(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND);
        _cpo.add(PtolemyTypeIdentifier.BOOLEAN_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.BOOLEAN_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.COMPLEX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.COMPLEX_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.DOUBLE_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.DOUBLE_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.FIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.FIX_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.INT_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.INT_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.LONG_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.LONG_MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.MATRIX_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.OBJECT_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.SCALAR_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.STRING_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.TOKEN_DECL);

	// We need the rest of the array tokens
        _cpo.add(PtolemyTypeIdentifier.ARRAY_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.DOUBLE_ARRAY_TOKEN_DECL);
        _cpo.add(PtolemyTypeIdentifier.INT_ARRAY_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.OBJECT_TOKEN_DECL,
                PtolemyTypeIdentifier.TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.STRING_TOKEN_DECL,
                PtolemyTypeIdentifier.TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.BOOLEAN_TOKEN_DECL,
                PtolemyTypeIdentifier.STRING_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.SCALAR_TOKEN_DECL,
                PtolemyTypeIdentifier.STRING_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.STRING_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.LONG_TOKEN_DECL,
                PtolemyTypeIdentifier.SCALAR_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.COMPLEX_TOKEN_DECL,
                PtolemyTypeIdentifier.SCALAR_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.FIX_TOKEN_DECL,
                PtolemyTypeIdentifier.SCALAR_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DOUBLE_TOKEN_DECL,
                PtolemyTypeIdentifier.COMPLEX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.INT_TOKEN_DECL,
                PtolemyTypeIdentifier.LONG_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.INT_TOKEN_DECL,
                PtolemyTypeIdentifier.DOUBLE_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.BOOLEAN_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.MATRIX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.LONG_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.MATRIX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.COMPLEX_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.MATRIX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.FIX_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.MATRIX_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.DOUBLE_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.COMPLEX_MATRIX_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.INT_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.DOUBLE_MATRIX_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.INT_MATRIX_TOKEN_DECL,
                PtolemyTypeIdentifier.LONG_MATRIX_TOKEN_DECL);

	// FIXME: is this right?
        _cpo.addEdge(PtolemyTypeIdentifier.INT_TOKEN_DECL,
		     PtolemyTypeIdentifier.INT_ARRAY_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DOUBLE_TOKEN_DECL,
		     PtolemyTypeIdentifier.DOUBLE_ARRAY_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.INT_ARRAY_TOKEN_DECL,
                PtolemyTypeIdentifier.DOUBLE_ARRAY_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DOUBLE_ARRAY_TOKEN_DECL,
                PtolemyTypeIdentifier.ARRAY_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.ARRAY_TOKEN_DECL,
               PtolemyTypeIdentifier.STRING_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.INT_ARRAY_TOKEN_DECL);


        //_cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
        //        PtolemyTypeIdentifier.ARRAY_TOKEN_DECL);
        //_cpo.addEdge(PtolemyTypeIdentifier.ARRAY_TOKEN_DECL,
        //        PtolemyTypeIdentifier.INT_ARRAY_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.OBJECT_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.STRING_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.BOOLEAN_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.INT_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.FIX_TOKEN_DECL);

        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.BOOLEAN_MATRIX_TOKEN_DECL);
	// Shouldn't this be INT, INT_MATRIX like data.type.TypeLattice?
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.INT_MATRIX_TOKEN_DECL);
        _cpo.addEdge(PtolemyTypeIdentifier.DUMMY_LOWER_BOUND,
                PtolemyTypeIdentifier.FIX_MATRIX_TOKEN_DECL);

        if (!_cpo.isLattice()) {
            throw new RuntimeException("SpecializeTokenVisitor: The " +
                    "type hierarchy is not a lattice.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Set to true to turn on debugging messages.
    private static final boolean _debug = false;

    // Set to true if we want to try out INT_ARRAY etc.
    private static final boolean _useArrays = false;
}
