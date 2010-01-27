/* An adapter class for ptolemy.data.expr.ASTPtArrayConstructNode.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 An adapter class for ptolemy.data.expr.ASTPtArrayConstructNode.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtArrayConstructNode extends ASTPtRootNode {

    /**
     * Construct an property constraint adapter for the given ASTPtArrayConstructNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtArrayConstructNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtArrayConstructNode(PropertyConstraintSolver solver,
            ptolemy.data.expr.ASTPtArrayConstructNode node)
            throws IllegalActionException {

        super(solver, node, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_getNode(), new FunctionTerm());
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                    ////

    /**
     *
     * @author Thomas Mandl
     *
     */
    private class FunctionTerm extends MonotonicFunction {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * @exception IllegalActionException
         *
         */
        public Object getValue() throws IllegalActionException {
            boolean isAllSameTokenValue = true;
            Token token = null;

            for (int i = 0; i < _getNode().jjtGetNumChildren(); i++) {
                ptolemy.data.expr.ASTPtRootNode childNode = (ptolemy.data.expr.ASTPtRootNode) _getNode()
                        .jjtGetChild(i);

                Property childProperty = getSolver().getProperty(childNode);

                if ((childProperty == null)
                        || (childProperty == _lattice.getElement("UNKNOWN"))
                        || (childProperty == _lattice.getElement("FALSE"))) {
                    return childProperty;
                } else if (!(childNode.isConstant() && childNode.isEvaluated())) {
                    return _lattice.getElement("FALSE");
                } else {
                    if (token == null) {
                        token = childNode.getToken();
                    } else {
                        if (!(childNode.getToken().isEqualTo(token)
                                .booleanValue())) {
                            isAllSameTokenValue = false;
                        }
                    }
                }
            }

            return (isAllSameTokenValue) ? _lattice.getElement("TRUE")
                    : _lattice.getElement("FALSE");
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        ///////////////////////////////////////////////////////////////////
        ////                      protected methods                    ////

        protected InequalityTerm[] _getDependentTerms() {
            List<InequalityTerm> terms = new ArrayList<InequalityTerm>();

            try {
                for (int i = 0; i < _getNode().jjtGetNumChildren(); i++) {
                    Object child = _getNode().jjtGetChild(i);

                    PropertyConstraintASTNodeHelper adapter;

                    adapter = (PropertyConstraintASTNodeHelper) getSolver()
                            .getHelper(child);
                    InequalityTerm term = adapter.getPropertyTerm(child);

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
