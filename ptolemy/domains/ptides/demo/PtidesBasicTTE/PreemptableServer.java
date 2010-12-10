package ptolemy.domains.ptides.demo.PtidesBasicTTE;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PreemptableServer extends Server {

    public PreemptableServer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    
    
    public boolean postfire() throws IllegalActionException {
        serviceTime.update();
        double serviceTimeValue = ((DoubleToken) serviceTime.getToken())
                .doubleValue();
        Time currentTime = getDirector().getModelTime();

        if (_nextTimeFree.equals(Time.NEGATIVE_INFINITY) && _queue.size() > 0) {
            _nextTimeFree = currentTime.add(serviceTimeValue);
            _fireAt(_nextTimeFree);
        } 
        
        if (_nextTimeFree.equals(Time.POSITIVE_INFINITY) && !Double.isInfinite(serviceTimeValue)) {
            _nextTimeFree = currentTime.add(serviceTimeValue);
            _fireAt(_nextTimeFree);
        }
        
        return super.postfire();
    }

}
