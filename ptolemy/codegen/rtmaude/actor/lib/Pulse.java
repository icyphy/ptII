package ptolemy.codegen.rtmaude.actor.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.data.ArrayToken;
import ptolemy.kernel.util.IllegalActionException;

public class Pulse extends Entity {

    public Pulse(ptolemy.actor.lib.Pulse component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("current-index", "0");
        atts.put("indexes", "'indexes");
        atts.put("values", "'values");
        return atts;
    }

}
