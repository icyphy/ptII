package ptolemy.codegen.rtmaude.domains.de.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class Timer extends Entity {

    public Timer(ptolemy.domains.de.lib.Timer component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("output-value", "'value");
        return atts;
    }
}
