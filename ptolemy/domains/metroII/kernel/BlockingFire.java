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

        if (getStatus() == Status.START) {
            _currentStateEvent = _createMetroIIEvent("FIRE_BEGIN");
            metroIIEventList.add(_currentStateEvent);
            setStatus(Status.BEGIN);
        } else if (getStatus() == Status.BEGIN) {
            assert _currentStateEvent.getName().contains("FIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                actor().fire();
                setStatus(Status.END);
                _currentStateEvent = _createMetroIIEvent("FIRE_END");
                metroIIEventList.add(_currentStateEvent);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (getStatus() == Status.END) {
            assert _currentStateEvent.getName().contains("FIRE_END");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                _currentStateEvent = null; 
                setStatus(Status.FINAL);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (getStatus() == Status.FINAL) {
            // do nothing
        } else {
            // unknown state; 
            assert false; 
        }
        
    }
    

    @Override
    public State getState() {
        // TODO Auto-generated method stub
        assert false; 
        return null;
    }


}
