package ptolemy.codegen.rtmaude.domains.de.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class TimedDelay extends Entity {
    
    public TimedDelay(ptolemy.domains.de.lib.TimedDelay component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("delay", "'delay");
        return atts;
    }
    
}