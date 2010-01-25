package ptolemy.domains.pthales.lib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class PthalesAddHeaderActor extends PthalesAtomicActor {

    public PthalesAddHeaderActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    public PthalesAddHeaderActor() throws IllegalActionException,
            NameDuplicationException {
    }

    public PthalesAddHeaderActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Header
        IOPort portOut = (IOPort) getPort("out");
        IOPort portIn = (IOPort) getPort("in");

        // One port in theory
        IOPort port = (IOPort) portIn.connectedPortList().get(0);

        // 1 tokens per dimension + 1 token for the number of dimensions
        String[] dims = PthalesIOPort.getDimensions(port);
        int[] sizes = new int[dims.length];
        Object[] sizesString = PthalesIOPort.getArraySizes(port).values()
                .toArray();
        for (int i = 0; i < sizes.length; i++)
            sizes[i] = (Integer) sizesString[i];

        // Ports modifications
        PthalesIOPort._modifyPattern(portIn, dims, sizes);
        PthalesIOPort._modifyPattern(portOut, "global", 1
                + PthalesIOPort.getDimensions(port).length
                + PthalesIOPort.getArraySize(port));

        PthalesIOPort.propagateHeader(portOut, dims, sizes, 1 + PthalesIOPort
                .getDimensions(port).length, PthalesIOPort.getArraySizes(portIn));
    }

    public void fire() throws IllegalActionException {

        // Variables
        IOPort portIn = (IOPort) getPort("in");
        IOPort portOut = (IOPort) getPort("out");

        // One port in theory
        IOPort previousPort = (IOPort) portIn.connectedPortList().get(0);
        int nbDims = PthalesIOPort.getDimensions(previousPort).length;

        // Token Arrays from simulation
        Token[] tokensIn = null;

        // Input ports created and filled before elementary task called 
        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);
        tokensIn = new FloatToken[dataSize];
        tokensIn = portIn.get(0, dataSize);

        // Header construction
        List<Token> header = new ArrayList<Token>();

        LinkedHashMap<String, Integer> sizes = PthalesIOPort
                .getArraySizes(previousPort);

        header.add(new IntToken(nbDims));
        for (String dim : PthalesIOPort.getDimensions(previousPort)) {
            header.add(new IntToken(sizes.get(dim)));
        }

        // then sent to output
        for (int i = 0; i < portOut.getWidth(); i++) {
            for (int j = 0; j < header.size(); j++) {
                portOut.send(i, header.get(j));
            }
            portOut.send(i, tokensIn, dataSize);
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
