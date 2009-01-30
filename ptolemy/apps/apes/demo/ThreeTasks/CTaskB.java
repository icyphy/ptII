package ptolemy.apps.apes.demo.ThreeTasks;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskB extends CTask {

    public CTaskB() throws IllegalActionException, NameDuplicationException { 
        super();
    }

    public CTaskB(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
    }

    public CTaskB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    @Override
    protected void _callCMethod() {
        long period;

        System.out.println(this.getName() + "._callCMethod()");
        try {
            accessPointCallback(-1.0, 1.0);
            period = System.currentTimeMillis();
            for (int i=0;i<Integer.MAX_VALUE/4;i++){
                double a = Math.PI*Math.PI;
            }
            period = System.currentTimeMillis() - period;
            System.out.println("duration of CMethod of " + this.getName() + ": " + Long.toString(period) + " ms.");            
            accessPointCallback(1.7, 0.0); 
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
