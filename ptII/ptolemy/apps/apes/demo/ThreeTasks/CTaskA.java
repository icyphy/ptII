package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskA extends CTask {

    public CTaskA() throws IllegalActionException, NameDuplicationException { 
        super();
    }

    public CTaskA(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
    }

    public CTaskA(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    
    protected void _callCMethod() { 
        long period;
        System.out.println(this.getName() + "._callCMethod()");
        try {
            accessPointCallback(-1.0, 1.0);
            period = System.currentTimeMillis();
            for (int i=0;i<Integer.MAX_VALUE/6;i++){
                double a = Math.PI*Math.PI;
            }
            period = System.currentTimeMillis() - period;
            System.out.println("duration of CMethod of " + this.getName() + ": " + Long.toString(period) + " ms.");            
            accessPointCallback(2.2, 0.0);
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
