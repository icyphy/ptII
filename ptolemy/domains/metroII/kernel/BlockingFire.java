package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class BlockingFire extends FireMachine {

    /** Construct a basic wrapper.
    *
    * @param actor The actor
    */
    public BlockingFire(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
    * The functions prefire(), fire() and postfire()
    * are wrapped in startOrResume() as follows:
    * <ol>
    * <li> Propose MetroII event POSTFIRE_END_PREFIRE_BEGIN and wait for
    * the event being notified</li>
    * <li> prefire() </li>
    * <li> Propose MetroII event PREFIRE_END_FIRE_BEGIN and wait for the
    * event being notified</li>
    * <li> fire() </li>
    * <li> Propose MetroII event FIRE_END_POSTFIRE_BEGIN and wait for the
    * the event being notified</li>
    * <li> postfire() </li>
    * </ol>
    * where 'wait' means checking the status of MetroII event. If notified,
    * continue execution, otherwise proposing the same event again.
    *
    * @param metroIIEventList A list of MetroII events.
    */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        assert metroIIEventList != null;        
        if (getCurrentState() == State.START) {
            setState(State.BEGIN);
            metroIIEventList.add(proposeCurrentStateEvent());
        } else if (getCurrentState() == State.BEGIN) {
            assert getCurrentStateEvent().getName().contains("FIRE_BEGIN");
            if (getCurrentStateEvent().getStatus() == Event.Status.NOTIFIED) {
                actor().fire();
                setState(State.END);
                metroIIEventList.add(proposeCurrentStateEvent());
            } else {
                metroIIEventList.add(getCurrentStateEvent());
            }
        } else if (getCurrentState() == State.END) {
            assert getCurrentStateEvent().getName().contains("FIRE_END");
            if (getCurrentStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.FINAL);
            } else {
                metroIIEventList.add(getCurrentStateEvent());
            }
        } else if (getCurrentState() == State.FINAL) {
            // do nothing
        } else {
            // unknown state; 
            assert false; 
        }
        
    }
    

    @Override
    public ProcessState getProcessState() {
        // TODO Auto-generated method stub
        assert false; 
        return null;
    }
    
}
