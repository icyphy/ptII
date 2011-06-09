package ptolemy.apps.apes;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.EventManager.StatusType;
import ptolemy.kernel.util.IllegalActionException;

public class OSEKEntryPoint {
    
    public OSEKEntryPoint(CPUScheduler cpuScheduler, EventManager eventManager) {
        this.cpuScheduler = cpuScheduler;
        this.eventManager = eventManager;
    }

    public CPUScheduler cpuScheduler;
    public EventManager eventManager;
    
    public native void InitializeC();
    public native int appStartup();
    
    
    public int activateTask(int taskId) throws NoRoomException, IllegalActionException {
        return cpuScheduler.activateTask(taskId);
    }

    public void terminateTask() throws NoRoomException, IllegalActionException {
        cpuScheduler.terminateTask();
    }
    
    public int setEvent(int taskId, int newEvents) throws NoRoomException, IllegalActionException {
        return eventManager.setEvent(taskId, newEvents);
    }
    
    public int clearEvent(int events) { 
        return eventManager.clearEvent(events);
    }
    
    public int waitEvent(int events) throws NoRoomException, IllegalActionException {
        return eventManager.waitEvent(events);
    }
    
}
