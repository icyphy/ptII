package ptolemy.data.properties.token;

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.properties.PropertyAttribute;
import ptolemy.data.properties.PropertySolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyTokenAttribute extends PropertyAttribute {

    public PropertyTokenAttribute(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }    

    /** Set the expression. This method takes the descriptive form and
     * determines the internal form (by parsing the descriptive form) and stores
     * it.
     * @param expression A String that is the descriptive form of either a Unit
     * or a UnitEquation.
     * @see ptolemy.kernel.util.Settable#setExpression(java.lang.String)
     */

    public void setExpression(String expression) throws IllegalActionException {
        if (expression.length() > 0) {

            // Get the shared parser.
            PtParser parser = PropertySolver.getParser();
            ASTPtRootNode root = parser.generateParseTree(expression);
            
            ParseTreeEvaluator evaluator = new ParseTreeEvaluator();

            // FIXME: we may need scoping for evaluating variables
            // in the expression.
            Token token = evaluator.evaluateParseTree(root);
            
            _property = (PropertyToken) new PropertyToken(token);
        }
        super.setExpression(expression);
    }

}
