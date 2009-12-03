/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties.token;

import ptolemy.actor.IOPort;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyAttribute;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

public class PropertyCombineParseTreeEvaluator extends ParseTreeEvaluator {

    public PropertyCombineParseTreeEvaluator(Object object,
            PropertySolver solver) {
        _solver = solver;
        _object = object;
    }

    /** Evaluate a numeric constant or an identifier. In the case of an
     *  identifier, its value is obtained from the scope or from the list
     *  of registered constants.
     *  @param node The specified node.
     *  @exception IllegalActionException If an evaluation error occurs.
     */
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        super.visitLeafNode(node);

        if (node.isEvaluated() && node.isConstant()
                && node.getToken().getType().equals(BaseType.STRING)) {

            String stringTokenValue = ((StringToken) node.getToken())
                    .stringValue();
            if (stringTokenValue.equalsIgnoreCase("token::combinedValueToken")) {
                // PropertyCombineSolver?

                // get attribue
                PropertyAttribute propertyAttribute = (PropertyAttribute) (((IOPort) _object)
                        .getAttribute(stringTokenValue));
                PropertyToken propertyToken = (PropertyToken) propertyAttribute
                        .getProperty();

                if ((propertyToken != null)) {
                    _evaluatedChildToken = propertyToken.getToken();
                } else {
                    _evaluatedChildToken = new StringToken(Token.NIL.toString());
                }
                // PropertyTokenSolver?
            } else if (stringTokenValue.startsWith("token::")) {
                stringTokenValue = stringTokenValue.replaceFirst("token::", "");

                // get solver
                PropertyTokenSolver portValueSolver = (PropertyTokenSolver) _solver
                        .findSolver(stringTokenValue);

                if (portValueSolver == null) {
                    // not found, treat as String
                    //TODO: treat as exception?
                    return;
                } else {
                    // get adapter and property
                    PropertyToken propertyToken = (PropertyToken) portValueSolver
                            .getProperty(_object);
                    if (propertyToken != null) {
                        _evaluatedChildToken = propertyToken.getToken();
                    } else {
                        _evaluatedChildToken = new StringToken(Token.NIL
                                .toString());
                    }
                }
                // PropertyConstraintSolver?
            } else if (stringTokenValue.startsWith("lattice::")) {
                stringTokenValue = stringTokenValue.replaceFirst("lattice::",
                        "");

                // get solver
                PropertyConstraintSolver propertyConstraintSolver = (PropertyConstraintSolver) _solver
                        .findSolver(stringTokenValue);

                if (propertyConstraintSolver == null) {
                    // not found, treat as String
                    //TODO: treat as exception?
                    return;
                } else {
                    // get adapter and property
                    Property property = propertyConstraintSolver
                            .getProperty(_object);
                    if (property != null) {
                        _evaluatedChildToken = new StringToken(property
                                .toString());
                    } else {
                        _evaluatedChildToken = new StringToken(Token.NIL
                                .toString());
                    }
                }
            }
        }
    }

    private Object _object = null;

    private PropertySolver _solver;

}
