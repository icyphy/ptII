package ptolemy.codegen.rtmaude.actor.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class Clock extends Entity {

    public Clock(ptolemy.actor.lib.Clock component) {
        super(component);
    }
    
    @Override
    public String generateFireCode() throws IllegalActionException {
        ptolemy.actor.lib.Clock component = (ptolemy.actor.lib.Clock) getComponent();
        String evt = _generateBlockCode("eventBlock",
                component.getName(),
                component.output.getName(),
                _generateBlockCode("firstValueBlock"),
                _generateBlockCode("firstOffsetBlock")
                );
        return evt + super.generateFireCode();
    }

    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("period", "'period");
        atts.put("offsets", "'offsets");
        atts.put("values", "'values");
        atts.put("index", "0");
        return atts;
    }
}
