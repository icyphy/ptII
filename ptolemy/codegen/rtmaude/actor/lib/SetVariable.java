package ptolemy.codegen.rtmaude.actor.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class SetVariable extends Entity {
    
    public SetVariable(ptolemy.actor.lib.SetVariable component) {
        super(component);
    }

    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("variableName", 
                "'"+((ptolemy.actor.lib.SetVariable)getComponent()).variableName.getExpression());                
        return atts;
    }
}
