package ptolemy.apps.apes.demo.TwoTasksCyclicEventDependency;

import java.util.ArrayList;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class HighPriorityTask extends CTask {

    public HighPriorityTask() { 
    }

    public HighPriorityTask(Workspace workspace) {
        super(workspace); 
    }

    public HighPriorityTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    public static int ID = 2;
    public static int EVENT_ID = 2;

    protected void _callCMethod() { 

        System.out.println(this.getName() + "._callCMethod()");
        try {
            accessPointCallback(-1.0, 0.5); 
            
            accessPointCallback(0.7, 0.0); 
            ArrayList<Integer> list = new ArrayList();
            list.add(LowPriorityTask.EVENT_ID); 
            eventManager.setEvent(LowPriorityTask.ID, list);
            accessPointCallback(1.0, 0.0);
            // wait for event 1
            list.clear();
            list.add(HighPriorityTask.EVENT_ID); 
            eventManager.waitEvent(list);
            
            accessPointCallback(1.0, 0.0); 
            eventManager.clearEvent();
            cpuScheduler.terminateTask();
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    @Override
    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
}
