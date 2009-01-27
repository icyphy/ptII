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
    
    
    public int activateTask(int taskId) throws NoRoomException, IllegalActionException {
        return cpuScheduler.activateTask(taskId);
    }

    public void terminateTask() throws NoRoomException, IllegalActionException {
        cpuScheduler.terminateTask();
    }
    
    public StatusType setEvent(int taskId, byte newEvents) throws NoRoomException, IllegalActionException {
        return eventManager.setEvent(taskId, newEvents);
    }
    
    public StatusType clearEvent() { 
        return eventManager.clearEvent();
    }
    
    public StatusType waitEvent(byte events) throws NoRoomException, IllegalActionException {
        return eventManager.waitEvent(events);
    }
    
}
