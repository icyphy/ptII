package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskC extends CTask {

    public CTaskC() { 
    }

    public CTaskC(Workspace workspace) {
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
            period = System.currentTimeMillis();
            for (int i=0;i<Integer.MAX_VALUE/4;i++){
                double a = Math.PI*Math.PI;
            }
            period = System.currentTimeMillis() - period;
            System.out.println("duration of CMethod of " + this.getName() + ": " + Long.toString(period) + " ms.");            
            accessPointCallback(1.0, 0.0);         
            cpuScheduler.TerminateTask();
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
