/**
 * 
 */
package ptolemy.domains.sdf.optimize;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.Firing;

/**
 * @author mgeilen
 *
 */
public class BufferingProfileFiring extends Firing {

    public boolean fireExclusive;
    
    /**
     * 
     */
    public BufferingProfileFiring(boolean exclusive) {
        this.fireExclusive = exclusive;
    }

    /**
     * @param actor
     */
    public BufferingProfileFiring(Actor actor, boolean exclusive) {
        super(actor);
        this.fireExclusive = exclusive;
    }

}
