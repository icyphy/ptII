package ptolemy.apps.apes.demo;

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
        CMethod();
    }
    
    @Override
    public void accessPointCallback() throws NoRoomException,
            IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback();
    }
}
