/* A visitor for parse trees of the expression language that collects the set of free variables.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeFreeVariableCollector
/**
This class visits parse trees and collects the set of free variables
in the expression.  Generally speaking, free variables are any lone
identifiers, and any function applications where the name of the
function is valid in the scope of the expression.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeFreeVariableCollector extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the set of names of free variables in the given parse
     *  tree.
     *  @return A set of strings.
     */
    public Set collectFreeVariables(ASTPtRootNode node)
            throws IllegalActionException {
        //Set set = new HashSet();
        //_set = set;
        //node.visit(this);
        //_set = null;
        //return set;
        return collectFreeVariables(node, null);
    }

    public Set collectFreeVariables(ASTPtRootNode node, ParserScope scope)
            throws IllegalActionException {
        Set set = new HashSet();
        _set = set;
        _scope = scope;
        //        _functionArgumentListStack.clear();
        node.visit(this);
        _scope = null;
        _set = null;
        return set;
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitFunctionNode(ASTPtFunctionNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            _visitChild(node, i);
        }

        // FIXME: the function name should just be an open variable.
        //      if(_isValidName(node.getFunctionName())) {
        _set.add(node.getFunctionName());
            //}
    }
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        //        _functionArgumentListStack.push(node.getArgumentNameList());
        _visitAllChildren(node);
        _set.removeAll(node.getArgumentNameList());
        //  _functionArgumentListStack.pop();
    }
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    public void visitLeafNode(ASTPtLeafNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        /*       Iterator nameLists = _functionArgumentListStack.iterator();
        while (nameLists.hasNext()) {
            List nameList = (List)nameLists.next();
            if (nameList.contains(node.getName())) {
                // this leaf node refers to an argument of a defined
                // function
                return;
            }
        }
         */
        _set.add(node.getName());
    }

    public void visitLogicalNode(ASTPtLogicalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitPowerNode(ASTPtPowerNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitRecordConstructNode(ASTPtRecordConstructNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitRelationalNode(ASTPtRelationalNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitShiftNode(ASTPtShiftNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitSumNode(ASTPtSumNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }
    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        _visitAllChildren(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
        child.visit(this);
    }

    /** Test if the given identifier is valid.
     */
     protected boolean _isValidName(String name)
            throws IllegalActionException {
        if(_scope != null) {
            try {
                return (_scope.getType(name) != null);
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected ParserScope _scope;
    protected Set _set;
    //    private Stack _functionArgumentListStack = new Stack();
}
