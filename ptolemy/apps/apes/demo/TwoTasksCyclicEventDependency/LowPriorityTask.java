package ptolemy.apps.apes.demo.TwoTasksCyclicEventDependency;

import java.util.ArrayList;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class LowPriorityTask extends CTask {

    public LowPriorityTask() throws IllegalActionException, NameDuplicationException {
        super();
    }

    public LowPriorityTask(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
    }

    public LowPriorityTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    public static int ID = 1;
    public static int EVENT_ID = 1;
    
    protected void _callCMethod() {  
        System.out.println(this.getName() + "._callCMethod()");
        try {
            accessPointCallback(-1.0, 1.0); 
            eventManager.clearEvent(0);
            accessPointCallback(2.2, 0.0); 
            eventManager.setEvent(HighPriorityTask.ID, HighPriorityTask.EVENT_ID);
            
            accessPointCallback(1.0, 0.0); 
            eventManager.waitEvent(LowPriorityTask.EVENT_ID);
            
            accessPointCallback(1.0, 0.0);
            eventManager.clearEvent(0);
            cpuScheduler.terminateTask();
        } catch (NoRoomException e) { 
            e.printStackTrace();
        } catch (IllegalActionException e) { 
            e.printStackTrace();
        }   
    }

    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
}
