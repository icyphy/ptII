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
    
    public PropertyCombineParseTreeEvaluator(Object object, PropertySolver solver) {
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

        if (node.isEvaluated() && 
            node.isConstant() && 
            node.getToken().getType().equals(BaseType.STRING)) {
            
            String stringTokenValue = ((StringToken)node.getToken()).stringValue();
            if (stringTokenValue.equalsIgnoreCase("token::combinedValueToken")) {
                // PropertyCombineSolver?

                // get attribue
                PropertyAttribute propertyAttribute = (PropertyAttribute)(((IOPort)_object).getAttribute(stringTokenValue));
                PropertyToken propertyToken = (PropertyToken)propertyAttribute.getProperty();

                if ((propertyAttribute != null) && (propertyToken != null)) {
                    _evaluatedChildToken = propertyToken.getToken();
                } else {
                    _evaluatedChildToken = new StringToken(Token.NIL.toString());
                }
                // PropertyTokenSolver?
            } else if (stringTokenValue.startsWith("token::")) {
                stringTokenValue = stringTokenValue.replaceFirst("token::", "");
                
                // get solver
                PropertyTokenSolver portValueSolver = (PropertyTokenSolver) _solver.findSolver(stringTokenValue);
                
                if (portValueSolver == null) {
                    // not found, treat as String
                    //TODO: treat as exception?
                    return;
                } else {
                    // get helper and property
                    PropertyToken propertyToken = (PropertyToken)portValueSolver.getProperty(_object);
                    if (propertyToken != null) {
                        _evaluatedChildToken = propertyToken.getToken();
                    } else {
                        _evaluatedChildToken = new StringToken(Token.NIL.toString());
                    }
                }
            // PropertyConstraintSolver?
            } else if (stringTokenValue.startsWith("lattice::")) {
                stringTokenValue = stringTokenValue.replaceFirst("lattice::", "");
            
                // get solver
                PropertyConstraintSolver propertyConstraintSolver = (PropertyConstraintSolver) _solver.findSolver(stringTokenValue);

                if (propertyConstraintSolver == null) {
                    // not found, treat as String
                    //TODO: treat as exception?
                    return;
                } else {
                    // get helper and property
                    Property property = (Property)propertyConstraintSolver.getProperty(_object);
                    if (property != null) {
                        _evaluatedChildToken = new StringToken(property.toString());                            
                    } else {
                        _evaluatedChildToken = new StringToken(Token.NIL.toString());
                    }
                }
            }
        }
    }
    
    private Object _object = null;

    private PropertySolver _solver;

}
