package ptolemy.codegen.rtmaude.data.expr;

import ptolemy.codegen.rtmaude.kernel.RTMaudeAdaptor;
import ptolemy.kernel.util.IllegalActionException;

public class Variable extends RTMaudeAdaptor {

    public Variable(ptolemy.data.expr.Variable component) {
        super(component);
    }
    
    @Override
    public String generateTermCode() throws IllegalActionException {
        ptolemy.data.expr.Variable v = (ptolemy.data.expr.Variable) getComponent();
        String ret;
        
        if(v.getElementName() != null && v.getExpression().trim().length() > 0)
            ret = _generateBlockCode("valBlock", v.getName());
        else
            ret = "nil";
        return _generateBlockCode(this.defaultTermBlock, v.getName(), ret);
    }
}
