package ptolemy.domains.de.lib.resource;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.util.Time;
import ptolemy.data.ResourceToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PeriodicTaskTrigger extends TypedAtomicActor {

    
    
    public PeriodicTaskTrigger(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _initialize();
        // TODO Auto-generated constructor stub
    }
    
    public TypedIOPort output;
    
    public TypedIOPort trigger;
    
    /** Output the current value of the clock if the time is right
     *  and, if connected, a trigger has been received.
     *  @exception IllegalActionException If
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Called fire() at time " + currentTime);
        }

        ResourceToken token = new ResourceToken(_task, 0);
        output.send(0, token);
        getDirector().fireAt(this, _period);
    }
    
    @Override
    public void initialize() throws IllegalActionException {
        _period = new Time(getDirector(), 2.0);
        _task = (Task) ((IOPort)trigger.sourcePortList().get(0)).getContainer();
        getDirector().fireAt(this, _period);
        
    }
    
    private void _initialize() {
        try {
            output = new TypedIOPort(this, "output", false, true); 
            trigger = new TypedIOPort(this, "trigger", false, true);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    


    private Time _period;
    private Task _task;
    
}
