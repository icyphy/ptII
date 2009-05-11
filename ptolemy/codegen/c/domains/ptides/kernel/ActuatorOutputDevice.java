package ptolemy.codegen.c.domains.ptides.kernel;

import ptolemy.codegen.c.domains.ptides.lib.OutputDevice;

/**
 * A code generation helper class for ptolemy.domains.ptides.lib.ActuatorOutputDevice
 * @author Jia Zou, Isaac Liu
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 */
public class ActuatorOutputDevice extends OutputDevice {
    /**
     * Construct a ActuatorOutputDevice helper.
     * @param actor The associated actor.
     * 
     */
    public ActuatorOutputDevice(ptolemy.domains.ptides.lib.ActuatorOutputDevice actor) {
        super(actor);
    }
}
