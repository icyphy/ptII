package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class NonBlockingFire extends FireMachine {

    public NonBlockingFire(Actor actor) {
        super(actor);
    }

    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (getCurrentState() == State.START) {
            actor().fire();
            setState(State.FINAL);
        } 
        else {
            // invalid state; 
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
