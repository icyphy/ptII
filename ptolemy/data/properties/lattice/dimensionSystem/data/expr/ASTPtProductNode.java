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

package ptolemy.data.properties.lattice.dimensionSystem.data.expr;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 A helper class for ptolemy.data.expr.ASTPtArrayConstructNode.

 @author Thomas Mandl
 @version $Id: ASTPtArrayConstructNode.java 53211 2009-04-24 02:59:19Z mankit $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtProductNode extends PropertyConstraintASTNodeHelper {

    /**
     * Construct an property constraint helper for the given ASTPtArrayConstructNode.
     * @param solver The given solver to get the lattice from.
     * @param node The given ASTPtArrayConstructNode.
     * @exception IllegalActionException Thrown if the parent construct
     *  throws it.
     */
    public ASTPtProductNode(PropertyConstraintSolver solver,
            ptolemy.data.expr.ASTPtProductNode node)
    throws IllegalActionException {
        super(solver, node, false);
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
     */
    private class FunctionTerm extends MonotonicFunction {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            ptolemy.data.expr.ASTPtProductNode node = 
                (ptolemy.data.expr.ASTPtProductNode) _getNode();

            List tokenList = node.getLexicalTokenList();

            int numChildren = node.jjtGetNumChildren();

            if (numChildren != 2 && tokenList.size() != 1 && 
                    ((Token) tokenList.get(0)).kind != PtParserConstants.MULTIPLY) {
                throw new IllegalActionException(ASTPtProductNode.this.getSolver(), 
                        "The property analysis " +
                "currently supports only binary multiplication.");
            }

            Property time = _lattice.getElement("TIME");
            Property position = _lattice.getElement("POSITION");
            Property speed = _lattice.getElement("SPEED");
            Property acceleration = _lattice.getElement("ACCELERATION");
            Property unitless = _lattice.getElement("UNITLESS");
            Property unknown = _lattice.getElement("UNKNOWN");

            Property property1 = (Property) getSolver().getProperty(node.jjtGetChild(0));
            Property property2 = (Property) getSolver().getProperty(node.jjtGetChild(1));
            
            if ((property1 == speed && property2 == time) ||
                (property2 == speed && property1 == time)) {
                return position;
            }

            if ((property1 == acceleration && property2 == time) ||
                (property2 == acceleration && property1 == time)) {
                return speed;
            }

            if (property1 == unitless) {
                return property2;
            }

            if (property2 == unitless) {
                return property1;
            }

            if (property1 == unknown || property2 == unknown) {
                return unknown;
            } 
            return _lattice.getElement("TOP");
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
