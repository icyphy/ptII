package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIActorBasicWrapper implements MetroIIActorInterface {

    /** Construct a Actor-Thread pair.
     * 
     * @param actor The actor
     * @param type The type of actor
     * @param state The initial thread state
     * @param thread The thread
     */
    public MetroIIActorBasicWrapper(Actor actor) {
        this.actor = actor;
        this.state = State.POSTFIRE_END_PREFIRE_BEGIN;
        currentStateEvent = createMetroIIEvent("PREFIRE_BEGIN");
    }

    public void close() {
    }

    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (state == State.POSTFIRE_END_PREFIRE_BEGIN) {
            assert (currentStateEvent.getName().contains("PREFIRE_BEGIN"));
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (actor.prefire()) {
                    state = State.PREFIRE_END_FIRE_BEGIN;
                    currentStateEvent = createMetroIIEvent("FIRE_BEGIN");
                }
            }
            metroIIEventList.add(currentStateEvent);
        } else if (state == State.PREFIRE_END_FIRE_BEGIN) {
            assert (currentStateEvent.getName().contains("FIRE_BEGIN"));
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                actor.fire();
                state = State.FIRE_END_POSTFIRE_BEGIN;
                currentStateEvent = createMetroIIEvent("POSTFIRE_BEGIN");
            }
            metroIIEventList.add(currentStateEvent);
        } else if (state == State.FIRE_END_POSTFIRE_BEGIN) {
            assert (currentStateEvent.getName().contains("POSTFIRE_BEGIN"));
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (actor.postfire()) {
                    state = State.POSTFIRE_END_PREFIRE_BEGIN;
                    currentStateEvent = createMetroIIEvent("PREFIRE_BEGIN");
                    metroIIEventList.add(currentStateEvent);
                }
                else {
                    // FIXME: handle the request that the actor wants to halt
                    //                if (_debugging) {
                    //                    _debug("Actor requests halt: "
                    //                            + ((Nameable) actorThread.actor).getFullName());
                    //                }
                }
            }
            metroIIEventList.add(currentStateEvent);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected fields                       ////

    /** Create MetroII event
     * 
     */
    protected Builder createMetroIIEvent(String name) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(actor.getFullName() + "." + name);
        meb.setOwner(actor.getFullName());
        meb.setStatus(Event.Status.PROPOSED);
        meb.setType(Event.Type.GENERIC);
        return meb;
    }

    /** Current state event
     * 
     */
    protected Builder currentStateEvent;

    /** Actor state
     * 
     */
    protected State state;
    
    /** Actor state 
     */
    protected enum State {
        POSTFIRE_END_PREFIRE_BEGIN, PREFIRE_END_FIRE_BEGIN, FIRING, FIRE_END_POSTFIRE_BEGIN
    }

    /** Actor which is being fired 
     * 
     */
    protected Actor actor;

}
