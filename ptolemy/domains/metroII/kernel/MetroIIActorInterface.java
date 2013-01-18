package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.kernel.util.IllegalActionException;

public interface MetroIIActorInterface {
    public void resume(LinkedList<Event.Builder> metroIIEventList) throws IllegalActionException;
    
    public void close(); 
}
