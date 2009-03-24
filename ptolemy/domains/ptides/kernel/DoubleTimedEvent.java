package ptolemy.domains.ptides.kernel;

import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;

public class DoubleTimedEvent extends TimedEvent {

    public DoubleTimedEvent(Time time1, Object obj, Time time2) {
        super(time1, obj);
        modelTime = time2;
    }

    public Time modelTime;
    
    public String toString() {
        return super.toString() + ", modelTime = " + modelTime;
    }
}
