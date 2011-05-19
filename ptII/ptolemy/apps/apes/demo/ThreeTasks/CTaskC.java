package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskC extends CTask {

    public CTaskC() throws IllegalActionException, NameDuplicationException { 
        super();
    }

    public CTaskC(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
    }

    public CTaskC(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    @Override
    protected void _callCMethod() {
 //       CMethod();
        long period;

        System.out.println(this.getName() + "._callCMethod()");
        try {
            accessPointCallback(-1.0, 0.5); 
            System.out.println("!!!!!!!!");
            accessPointCallback(1.0, 0.0);         
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
