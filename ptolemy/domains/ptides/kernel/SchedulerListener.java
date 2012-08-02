package ptolemy.domains.ptides.kernel;

import ptolemy.actor.util.Time;

public interface SchedulerListener {
    
    public Time scheduleEvent(PtidesEvent event);

}
