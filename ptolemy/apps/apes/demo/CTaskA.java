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
        //       CMethod();
        try {
            synchronized(this){
            this.wait(100L);
            }
            accessPointCallback(3.0,"");
//            synchronized(this){
//                wait(100);            
//            }
//            accessPointCallback(-1.0,"");            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void accessPointCallback(double extime, String syscall) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, syscall);
    }
}
