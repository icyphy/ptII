/* A visitor for parse trees of the expression language that infers types.

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
//// ParseTreeSpecializer
/**


@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeSpecializer extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Replace every identifier reference by the given parse tree
     *  with constants according to the given scope.  Exclude the
     *  given set of names from being replaced.
     */
    public void specialize(ASTPtRootNode node, List excludedNames,
            ParserScope scope)
            throws IllegalActionException {
        _excludedNames = excludedNames;
        _scope = scope;
        node.visit(this);
        _scope = null;
        _excludedNames = null;
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
        _visitAllChildren(node);
    }
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        List excludedNames = new LinkedList(_excludedNames);
        // Don't substitute any names in the parse tree that are
        // bound in the definition.
        excludedNames.addAll(node.getArgumentNameList());
        List oldExcludedNames = _excludedNames;
        _excludedNames = excludedNames;
        node.getExpressionTree().visit(this);
        _excludedNames = oldExcludedNames;

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
        if(!_excludedNames.contains(node.getName())) {
            if(_scope != null) {
                ptolemy.data.Token token = _scope.get(node.getName());
                node.setToken(token);
                node.setConstant(true);
                // Reset the name, since it no longer makes sense.
                node._name = null;
                return;
            }
            throw new IllegalActionException("Expression contains identifier "
                    + node.getName() + " which is not bound " +
                    "in the given scope");
        }
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

    protected List _excludedNames;
    protected ParserScope _scope;
}
