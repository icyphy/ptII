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
        if (getStatus() == Status.START) {
            actor().fire();
            setStatus(Status.FINAL);
        } 
        else {
            // invalid state; 
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
