package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIActorIterationWrapper implements MetroIIActorInterface {

    /** Construct a Actor-Thread pair.
     * 
     * @param actor The actor
     * @param type The type of actor
     * @param state The initial thread state
     * @param thread The thread
     */
    public MetroIIActorIterationWrapper(Actor actor) {
        this.actor = actor;
    }
    
    public void close() {
    }
    
    /** Actor which is being fired 
     * 
     */
    public Actor actor;

    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (actor.prefire()) {
            actor.fire();
            if (!actor.postfire()) {
                // FIXME: handle the request that the actor wants to halt
                //                if (_debugging) {
                //                    _debug("Actor requests halt: "
                //                            + ((Nameable) actorThread.actor).getFullName());
                //                }
            }
        }
    }

}
