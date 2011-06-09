package ptolemy.apps.apes.demo.ThreeCTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.InterruptServiceRoutine;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class IRSC extends InterruptServiceRoutine {

    public IRSC() throws IllegalActionException, NameDuplicationException {  
        super();
    }

    public IRSC(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);  
    }

    public IRSC(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
     
    private native void CMethod(); 
    
    protected void _callCMethod() {  
        System.out.println(this.getName() + "._callCMethod()");
        CMethod();   
    }

    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
    
    public int activateTask(int taskId) throws NoRoomException, IllegalActionException {
        return cpuScheduler.activateTask(taskId);
    }
    public void terminateTask() throws NoRoomException, IllegalActionException {
        cpuScheduler.terminateTask();
    }
    

 
}
