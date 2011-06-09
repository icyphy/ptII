package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.InterruptServiceRoutine;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class IRS_C extends InterruptServiceRoutine {

    public IRS_C() throws IllegalActionException, NameDuplicationException {  
        super();
    }

    public IRS_C(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);  
    }

    public IRS_C(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
     
    
    
    protected void _callCMethod() {  
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime());
        try {
            accessPointCallback(-1.0, 0.0);
            cpuScheduler.activateTask(3);
            accessPointCallback(0.2, 0.0);
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
