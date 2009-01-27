package ptolemy.apps.apes.demo.TwoTasksCyclicEventDependency;

import java.util.ArrayList;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class LowPriorityTask extends CTask {

    public LowPriorityTask() { 
    }

    public LowPriorityTask(Workspace workspace) {
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
            eventManager.clearEvent();
            accessPointCallback(2.2, 0.0);
            ArrayList<Integer> list = new ArrayList();
            list.add(HighPriorityTask.EVENT_ID); 
            eventManager.setEvent(HighPriorityTask.ID, list);
            
            accessPointCallback(1.0, 0.0);
            list.clear();
            list.add(LowPriorityTask.EVENT_ID); 
            eventManager.waitEvent(list);
            
            accessPointCallback(1.0, 0.0);
            eventManager.clearEvent();
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
