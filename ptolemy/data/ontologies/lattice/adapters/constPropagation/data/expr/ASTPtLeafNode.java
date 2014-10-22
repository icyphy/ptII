/* The adapter class for ptolemy.data.expr.ASTPtLeafNode for the constPropagation ontology.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

 */

package ptolemy.data.ontologies.lattice.adapters.constPropagation.data.expr;

import java.util.List;

import ptolemy.data.Token;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.ontologies.lattice.LatticeOntologyASTNodeAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 The adapter class for ptolemy.data.expr.ASTPtLeafNode for the constPropagation ontology.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class ASTPtLeafNode extends LatticeOntologyASTNodeAdapter {

    /** Construct an property constraint adapter for the given ASTPtLeafNode.
     *  @param solver The given solver to get the lattice from.
     *  @param node The given ASTPtArrayConstructNode.
     *  @exception IllegalActionException Thrown if the parent construct
     *   throws it.
     */
    public ASTPtLeafNode(LatticeOntologySolver solver,
            ptolemy.data.expr.ASTPtLeafNode node) throws IllegalActionException {
        super(solver, node, false);
        _constantRepresentative = _getConstantRepresentative();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraint list for the adapter.
     *  @exception IllegalActionException If there is an error building the constraint list.
     *  @return The list of constraints for this adapter.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.data.expr.ASTPtLeafNode node = (ptolemy.data.expr.ASTPtLeafNode) _getNode();
        Token nodeToken = node.getToken();

        if (node.isConstant()) {
            if (nodeToken != null) {
                setAtLeast(node,
                        _constantRepresentative
                                .getFlatTokenInfiniteConceptByToken(nodeToken));
            } else {
                throw new IllegalActionException("A constant expression "
                        + "leaf node should not have a null token value.");
            }
        }

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the constant representative contained in the constant propagation
     *  ontology lattice. If none is found or there are more than one, throw
     *  an exception.
     *  @return The constant value representative.
     *  @exception IllegalActionException Thrown if there is no constant value
     *   FlatTokenInfiniteRepresentativeConcept, or there is more than one.
     */
    private FlatTokenRepresentativeConcept _getConstantRepresentative()
            throws IllegalActionException {
        List<FlatTokenRepresentativeConcept> _representatives = _solver
                .getOntology().entityList(FlatTokenRepresentativeConcept.class);
        if (_representatives == null || _representatives.isEmpty()) {
            throw new IllegalActionException("Constant propagation ontology "
                    + "does not have a constant representative concept.");
        } else if (_representatives.size() == 1) {
            return _representatives.get(0);
        } else {
            throw new IllegalActionException("There should only be one flat "
                    + "token representative concept in the constant "
                    + "propagation ontology.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The constant representative for the const propagation ontology lattice. */
    private FlatTokenRepresentativeConcept _constantRepresentative;
}
