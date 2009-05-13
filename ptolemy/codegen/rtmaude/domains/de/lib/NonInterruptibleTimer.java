package ptolemy.codegen.rtmaude.domains.de.lib;

import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

public class NonInterruptibleTimer extends Timer {

    public NonInterruptibleTimer(ptolemy.domains.de.lib.NonInterruptibleTimer component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("processing", "false");
        atts.put("wait-queue", "emptyList");
        return atts;
    }
}
