package ptolemy.caltrop.ddi;

import ptolemy.actor.Executable;

/**
 * An interface for domain dependent interpretation. Each instance of
 * {@link ptolemy.caltrop.actors.CalInterpreter CalInterpreter} is associated with its
 * own <tt>DDI</tt>, which performs tasks needed to interpret the actor in a specific domain.
 * <p>
 * <b>Note: this interface is likely to grow larger as more domains are implemented.</b>
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public interface DDI extends Executable {
    /**
     * Perform static checking on the actor, ensuring its validity in a given domain.
     * @return True, if the actor is legal.
     */
    boolean isLegalActor();
    /**
     * Perform any domain dependent setup. This can include hanging various attributes off of the actor, for example,
     * the rate of the input and output ports.
     */
    void setupActor();
    /**
     * Get the name of the domain that this DDI implements.
     * @return The name of the domain that this DDI implements.
     */
    String getName();
}
