/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2014 The Regents of the University of California.
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


 */
package ptolemy.data.ontologies;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ParseTreePropertyInference

/**
 FIXME: What is this??? Copy and pasted comment below.

 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeASTNodeAdapterCollector extends AbstractParseTreeVisitor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Infer the property of the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param solver The given solver.
     *  @return The list of property adapters.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public List<OntologyAdapter> collectAdapters(ASTPtRootNode node,
            OntologySolver solver) throws IllegalActionException {

        _adapters = new LinkedList<OntologyAdapter>();
        _solver = solver;

        _visitAllChildren(node);
        _solver = null;
        return _adapters;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Loop through all of the children of this node,
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     *
     *  @param node The root node whose children will be visited
     *  @exception IllegalActionException If an exception is thrown while
     *  visiting child nodes
     */
    @Override
    protected void _visitAllChildren(ASTPtRootNode node)
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();

        _adapters.add(_solver.getAdapter(node));

        for (int i = 0; i < numChildren; i++) {
            _visitChild(node, i);
        }
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     *
     *  @param node The root node whose child will be visited
     *  @param i The index (starting from 0) of the child node to be visited
     *  @exception IllegalActionException If an exception is thrown while
     *  visiting the child node
     */
    @Override
    protected void _visitChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(i);
        _visitAllChildren(child);
    }

    /** The list of ontology adapters for each node in the AST. */
    protected List<OntologyAdapter> _adapters;

    /** The given ontology solver for which the AST will be evaluated. */
    protected OntologySolver _solver;

}
