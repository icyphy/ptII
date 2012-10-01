package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
A adapter class for ptolemy.domains.de.lib.VariableDelay.

@deprecated Use ptolemy.actor.lib.TimeDelay
@author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
*/
public class VariableDelay extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the TimedDelay adapter.
     * @param actor the associated actor
     */
    public VariableDelay(ptolemy.domains.de.lib.VariableDelay actor) {
        super(actor);
    }

    @Override
    public String generateFireCode() throws IllegalActionException {

        String s = super.generateFireCode();
        return s;
    }

    /** Return the name of the port that is the time source.
     *  @return The string "input".
     */
    public String getTimeSourcePortName() {
        return "input";
    }
}
