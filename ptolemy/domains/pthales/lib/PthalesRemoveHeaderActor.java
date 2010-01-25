package ptolemy.domains.pthales.lib;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PthalesRemoveHeaderActor extends PthalesAtomicActor {

    public PthalesRemoveHeaderActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    public PthalesRemoveHeaderActor() throws IllegalActionException,
            NameDuplicationException {
    }

    public PthalesRemoveHeaderActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public void fire() throws IllegalActionException {

        // Variables
        IOPort portIn = (IOPort) getPort("in");
        IOPort portOut = (IOPort) getPort("out");
        
 
        // Token Arrays from simulation
        Token[] tokensIn = null;

        // Input ports created and filled before elementary task called 
        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);
        tokensIn = new FloatToken[dataSize];

        // Header
        int nbToken = ((IntToken)portIn.get(0)).intValue();
        Token[] headerIn = portIn.get(0, nbToken);
        
        // Token Arrays from simulation
        tokensIn = portIn.get(0, dataSize - headerIn.length - 1);

        // then input sent to output
        for (int i = 0; i < portOut.getWidth(); i++) {
            portOut.send(i, tokensIn, dataSize - headerIn.length - 1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {
        super._initialize();

        // input port
        new TypedIOPort(this, "in", true, false);

        // output port
        new TypedIOPort(this, "out", false, true);
    }

}
