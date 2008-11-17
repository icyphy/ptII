package ptolemy.domains.de.lib.apes.demo;

import ptolemy.domains.de.lib.apes.CTask;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTaskB extends CTask {

    public CTaskB() { 
    }

    public CTaskB(Workspace workspace) {
        super(workspace); 
    }

    public CTaskB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
    }
    
    private native void CMethod();
    
    @Override
    protected void _callCMethod() {
        CMethod();
    }
}
