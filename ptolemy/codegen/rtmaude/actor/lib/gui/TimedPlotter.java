package ptolemy.codegen.rtmaude.actor.lib.gui;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class TimedPlotter extends Entity {

    public TimedPlotter(ptolemy.actor.lib.gui.TimedPlotter component) {
        super(component);
    }

    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("current-time", "0");
        atts.put("event-history", "emptyHistory");
        return atts;
    }
    
}
