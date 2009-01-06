package ptolemy.apps.apes.demo;

import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskA extends CTask {

    public CTaskA() { 
    }

    public CTaskA(Workspace workspace) {
        super(workspace); 
    }

    public CTaskA(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    
    @Override
    protected void _callCMethod() { 
        long period;
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime());
        try {
            accessPointCallback(-1.0, 1.0, "");
        } catch (NoRoomException e1) { 
            e1.printStackTrace();
        } catch (IllegalActionException e1) { 
            e1.printStackTrace();
        }
        period = System.currentTimeMillis();
        for (int i=0;i<Integer.MAX_VALUE/6;i++){
            double a = Math.PI*Math.PI;
        }
        period = System.currentTimeMillis() - period;
        System.out.println("duration of CMethod of " + this.getName() + ": " + Long.toString(period) + " ms.");            
        try {
            accessPointCallback(3.0, -1.0,"");
        } catch (NoRoomException e) { 
            e.printStackTrace();
        } catch (IllegalActionException e) { 
            e.printStackTrace();
        }          
    }

    @Override
    public void accessPointCallback(double extime, double minNextTime, String syscall) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime, syscall);
    }
}
