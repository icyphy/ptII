/* A visitor for parse trees of the expression language to evaluate
function definitions.

 Copyright (c) 2002 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.matlab.Engine;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeFunctionDefinitionEvaluator
/**
This class visits part of a parse tree under a function definition node
and evaluates it into a function token value.

@author Xiaojun Liu
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeFunctionDefinitionEvaluator implements ParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public ptolemy.data.Token evaluateFunctionDefinition(
	        ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        return evaluateFunctionDefinition(node, null);
    }
    
    public ptolemy.data.Token evaluateFunctionDefinition(
            ASTPtFunctionDefinitionNode node, ParserScope scope) 
            throws IllegalActionException {
		_definitionRoot = node;
		_clonedParseTreeNodeStack.clear();
		_functionArgumentListStack.clear();
		_functionArgumentListStack.push(node.getArgumentNameList());
        _scope = scope;
        // Evaluate the value of the root node.
        ((ASTPtRootNode)node.jjtGetChild(0)).visit(this);
        // and return it.
        _scope = null;
		ExprFunction definedFunction =
				new ExprFunction(_definitionRoot.getArgumentNameList(),
				(ASTPtRootNode)_clonedParseTreeNodeStack.pop());
		FunctionToken result = new FunctionToken(definedFunction);
		node.setToken(result);
		_functionArgumentListStack.pop();
		// assert stack is empty
		_definitionRoot = null;
        return result;
    }
    
    public void visitArrayConstructNode(ASTPtArrayConstructNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
		_cloneSubtree(node);
	}
    public void visitFunctionNode(ASTPtFunctionNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
	public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
			throws IllegalActionException {
		_functionArgumentListStack.push(node.getArgumentNameList());
		_cloneSubtree(node);
		_functionArgumentListStack.pop();
	}
	public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitLeafNode(ASTPtLeafNode node) 
            throws IllegalActionException {
		ASTPtLeafNode newNode = null;
		try {
			newNode = (ASTPtLeafNode)node.clone();
		} catch (CloneNotSupportedException ex) {
			//FIXME
			throw new IllegalActionException("Unable to clone parse tree "
					+ "when evaluating function definition: "
					+ ex.getMessage());
		}
		_clonedParseTreeNodeStack.push(newNode);
        if (node.isConstant()) {
            return;
        }

        // The node refers to a variable, or something else that is in
        // scope.
		String name = node.getName();

		Iterator nameLists = _functionArgumentListStack.iterator();
		while (nameLists.hasNext()) {
			List nameList = (List)nameLists.next();
			if (nameList.contains(name)) {
				// this leaf node refers to an argument of a defined
				// function
				return;
			}
		}
		
        if (_scope != null) {
            ptolemy.data.Token value = _scope.get(name);
            if(value != null) {
                newNode.setToken(value);
				//FIXME add a setName() method to ASTPtLeafNode
				newNode._name = null;
				newNode._isConstant = true;
                return;
            }
        }
        throw new IllegalActionException(
                "The ID " + node.getName() + " is undefined.");
    }
    public void visitLogicalNode(ASTPtLogicalNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node) 
        	throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitPowerNode(ASTPtPowerNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitRecordConstructNode(ASTPtRecordConstructNode node) 
        	throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitRelationalNode(ASTPtRelationalNode node)
        	throws IllegalActionException {
		_cloneSubtree(node);
    }
   	public void visitShiftNode(ASTPtShiftNode node)
        	throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitSumNode(ASTPtSumNode node) 
            throws IllegalActionException {
		_cloneSubtree(node);
    }
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
		_cloneSubtree(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    protected void _cloneSubtree(ASTPtRootNode node) 
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
			child.visit(this);
        }
		ASTPtRootNode newNode = null;
		try {
			newNode = (ASTPtRootNode)node.clone();
		} catch (CloneNotSupportedException ex) {
			//FIXME
			throw new IllegalActionException("Unable to clone parse tree "
					+ "when evaluating function definition: "
					+ ex.getMessage());
		}
		newNode._children = (ArrayList)node._children.clone();
		for (int i = node.jjtGetNumChildren() - 1; i >= 0; --i) {
			ASTPtRootNode child =
					(ASTPtRootNode)_clonedParseTreeNodeStack.pop();
			child.jjtSetParent(newNode);
			newNode.jjtAddChild(child, i);
		}
		_clonedParseTreeNodeStack.push(newNode);
    }
	
	private Stack _clonedParseTreeNodeStack = new Stack();
	private Stack _functionArgumentListStack = new Stack();
	private ASTPtFunctionDefinitionNode _definitionRoot;
    private ParserScope _scope;
    private ParseTreeTypeInference _typeInference;
	
    ///////////////////////////////////////////////////////////////////
    ////                       inner classes                       ////

	private class ExprFunction implements FunctionToken.Function {
		
		public ExprFunction(List argumentNames, ASTPtRootNode exprRoot) {
			_argumentNames = new ArrayList(argumentNames);
			_exprRoot = exprRoot;
		}
		
		public ptolemy.data.Token apply(List args)
				throws IllegalActionException {
			if (_parseTreeEvaluator == null) {
				_parseTreeEvaluator = new ParseTreeEvaluator();
			}
			if (_variableList == null) {
				_variableList = new NamedList();
				Iterator names = _argumentNames.iterator();
				while (names.hasNext()) {
					String name = (String)names.next();
					Variable variable = new Variable();
					try {
						variable.setName(name);
						_variableList.append(variable);
					} catch (NameDuplicationException ex) {
						throw new InternalErrorException("Argument name "
								+ "duplication in defined function: "
								+ ex.getMessage());
					}
				}
				_scope = new ExplicitScope(_variableList);
			}
			for (int i = 0; i < args.size(); ++i) {
				String name = (String)_argumentNames.get(i);
				ptolemy.data.Token arg = (ptolemy.data.Token)args.get(i);
				Variable variable = (Variable)_variableList.get(name);
				variable.setToken(arg);
			}
			return _parseTreeEvaluator.evaluateParseTree(_exprRoot, _scope);
		}
		
		public int getNumberOfArguments() {
			return _argumentNames.size();
		}
		
		private ASTPtRootNode _exprRoot;
		private List _argumentNames;
		private NamedList _variableList;
		private ParserScope _scope;
		private ParseTreeEvaluator _parseTreeEvaluator;
	}

}
