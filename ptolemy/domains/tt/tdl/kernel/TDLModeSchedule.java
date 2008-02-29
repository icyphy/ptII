package ptolemy.domains.tt.tdl.kernel;

import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.actor.util.Time;

/**
 * hold runtime information for every schedule for a mode
 * 
 * @author Patricia Derler
 *
 */
public class TDLModeSchedule {

    public TDLModeSchedule(long modePeriod, HashMap modeSchedule) {
        this.modePeriod = modePeriod;
        this.modeSchedule = modeSchedule;
    }
    
    /** period of the mode */
    public long modePeriod;
    
    /** schedule containing tasks, actuators and ports, calculated by TDLModeScheduler */
    public HashMap modeSchedule;
    
    /** actual time in schedule */
    public long currentScheduleTime = 0;
    
    /** last time the mode was fired */
    public long lastFiredAt = -1;
    
    /** position in current slot in the schedule */
    public int currentPositionInSlot = 0;
    
    /** true if it is called for the very first time or after a mode switch */
    public boolean firstSlot = true;
    
    /** scheduled time for next execution */
	public long nextFireTime;
    
}
