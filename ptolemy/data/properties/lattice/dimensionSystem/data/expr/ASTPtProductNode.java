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

package ptolemy.data.properties.lattice.dimensionSystem.data.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.MultiplyMonotonicFunction;
import ptolemy.data.properties.lattice.dimensionSystem.DivideMonotonicFunction;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtLeafNode

/**
 An adapter class for ptolemy.data.expr.ASTPtArrayConstructNode.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtProductNode extends PropertyConstraintASTNodeHelper {

    /**
     * Construct an property constraint adapter for the given ASTPtArrayConstructNode.
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
        setAtLeast(_getNode(), new ASTPtProductNodeMonotonicFunction());
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private inner classes                    ////

    /**
     *
     */
    private class ASTPtProductNodeMonotonicFunction extends MonotonicFunction {

        ///////////////////////////////////////////////////////////////////
        ////                         public inner methods                    ////

        /**
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {
            // Updated by Charles Shelton 12/15/09:
            // Created a general function that covers any combination of multiplication
            // and division operators.  Modulo operators are not supported.

            ptolemy.data.expr.ASTPtProductNode node = (ptolemy.data.expr.ASTPtProductNode) _getNode();
            List tokenList = node.getLexicalTokenList();
            
            // Throw an exception if there is a modulo (%) operation in the product node expression.
            for (Object lexicalToken : tokenList) {
                if (((Token) lexicalToken).kind == PtParserConstants.MODULO) {
                    throw new IllegalActionException(
                            ASTPtProductNode.this.getSolver(),
                            "The Dimension System property analysis "
                                + "supports only multiplication and division, not modulo.");
                }
            }
            
            // Loop through all the child nodes in the product node
            // and get the correct result property by calling MultiplyMonotonicFunction
            // or DivideMonotonicFunction depending on the operator used.
            
            // Initialize the result to the first node in the product node
            Property result = getSolver().getProperty(node.jjtGetChild(0));
            
            // Iterate through the operator tokens
            Iterator lexicalTokenIterator = tokenList.iterator();
            
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                if (lexicalTokenIterator.hasNext()) {
                    Token lexicalToken = (Token) lexicalTokenIterator.next();
                    MonotonicFunction propertyFunction = null;
                    
                    // If operator token is '*' call the MultiplyMonotonicFunction
                    if (lexicalToken.kind == PtParserConstants.MULTIPLY) {
                        propertyFunction = new MultiplyMonotonicFunction(result,
                                node.jjtGetChild(i), _lattice, ASTPtProductNode.this);
                        result = (Property) propertyFunction.getValue();
                        
                    // If operator token is '/' call the DivideMonotonicFunction
                    } else {
                        propertyFunction = new DivideMonotonicFunction(result,
                                node.jjtGetChild(i), _lattice, ASTPtProductNode.this);
                        result = (Property) propertyFunction.getValue(); 
                    }
                    
                } else {
                    throw new IllegalActionException(
                            ASTPtProductNode.this.getSolver(),
                            "Error in the product expression; "
                                    + "the number of operators don't match the number of operands.");
                }
            }
            
            return result;
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
