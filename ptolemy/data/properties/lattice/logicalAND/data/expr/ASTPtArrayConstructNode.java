/* A helper class for ptolemy.data.expr.ASTPtArrayConstructNode.

 Copyright (c) 2006-2009 The Regents of the University of California.
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

package ptolemy.data.properties.lattice.logicalAND.data.expr;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.Token;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.logicalAND.Lattice;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 A helper class for ptolemy.data.expr.ASTPtArrayConstructNode.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtArrayConstructNode extends ASTPtRootNode {

    /**
     * Construct an property constraint helper for the given ASTPtArrayConstructNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtArrayConstructNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtArrayConstructNode(PropertyConstraintSolver solver,
            ptolemy.data.expr.ASTPtArrayConstructNode node)
            throws IllegalActionException {

        super(solver, node, true);
        _lattice = (Lattice) getSolver().getLattice();
        _node = node;
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_node, new FunctionTerm());
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.data.expr.ASTPtArrayConstructNode _node;
    private Lattice _lattice;

    /**
     *
     * @author Thomas Mandl
     *
     */
    private class FunctionTerm
    extends MonotonicFunction {

        /**
         * @exception IllegalActionException
         *
         */
        public Object getValue() throws IllegalActionException {
            boolean isAllSameTokenValue = true;
            Token token = null;

            for (int i = 0; i < _node.jjtGetNumChildren(); i++) {
                ptolemy.data.expr.ASTPtRootNode childNode =
                    (ptolemy.data.expr.ASTPtRootNode) _node.jjtGetChild(i);

                Property childProperty =
                    getSolver().getProperty(childNode);

                if ((childProperty == null) || (childProperty == _lattice.UNKNOWN) || (childProperty == _lattice.FALSE)) {
                    return childProperty;
                } else if (!(childNode.isConstant() && childNode.isEvaluated())) {
                    return _lattice.FALSE;
                } else {
                    if (token == null) {
                        token = childNode.getToken();
                    } else {
                        if (!(childNode.getToken().isEqualTo(token).booleanValue())) {
                            isAllSameTokenValue = false;
                        }
                    }
                }
            }

            return (isAllSameTokenValue) ? _lattice.TRUE : _lattice.FALSE;
        }


        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

            try {
                for (int i = 0; i < _node.jjtGetNumChildren(); i++) {
                    Object child = _node.jjtGetChild(i);

                    PropertyConstraintASTNodeHelper helper;

                    helper = (PropertyConstraintASTNodeHelper) getSolver().getHelper(child);
                    InequalityTerm term = helper.getPropertyTerm(child);

                    terms.add(term);
                }
            } catch (IllegalActionException e) {
                throw new AssertionError(
                "Unable to get the children property term(s).");
            }

            return terms.toArray(new InequalityTerm[0]);
        }
    }

}
