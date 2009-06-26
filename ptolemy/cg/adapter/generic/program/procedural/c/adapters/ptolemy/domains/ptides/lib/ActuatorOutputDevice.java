package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib;

/**
 * A code generation helper class for ptolemy.domains.ptides.lib.ActuatorOutputDevice
 * @author Jia Zou, Isaac Liu
 * @version $Id: ActuatorOutputDevice.java 53821 2009-05-18 15:49:29Z cxh $
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
    public ActuatorOutputDevice(
            ptolemy.domains.ptides.lib.ActuatorOutputDevice actor) {
        super(actor);
    }
}
