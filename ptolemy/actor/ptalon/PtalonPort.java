package ptolemy.actor.ptalon;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This class extends TypedIOPort and identifies the port
 * as a valid PtalonObject. 
 * @author acataldo
 *
 */
public class PtalonPort extends TypedIOPort implements PtalonObject {

    public PtalonPort() {
        super();
        // TODO Auto-generated constructor stub
    }

    public PtalonPort(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public PtalonPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    public PtalonPort(ComponentEntity container, String name, boolean isInput,
            boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);
        // TODO Auto-generated constructor stub
    }

}
