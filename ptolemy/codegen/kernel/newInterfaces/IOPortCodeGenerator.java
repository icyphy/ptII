package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.codegen.util.PartialResult;

public interface IOPortCodeGenerator{

    public PartialResult get(PartialResult PARAM);

    public PartialResult get(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult setInput(PartialResult PARAM);

    public PartialResult setOutput(PartialResult PARAM);

    public PartialResult isInput();

    public PartialResult isOutput();

    public PartialResult broadcast(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult broadcast(PartialResult PARAM);

    public PartialResult getRemoteReceivers(PartialResult PARAM);

    public PartialResult getRemoteReceivers();

    public PartialResult broadcastClear();

    public PartialResult convert(PartialResult PARAM);

    public PartialResult createReceivers();

    public PartialResult getWidth();

    public PartialResult deepConnectedInPortList();

    public PartialResult deepConnectedInPorts();

    public PartialResult deepConnectedOutPortList();

    public PartialResult deepConnectedOutPorts();

    public PartialResult deepGetReceivers();

    public PartialResult getWidthInside();

    public PartialResult getReceivers();

    public PartialResult getReceivers(PartialResult PARAM);

    public PartialResult getReceivers(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult getChannelForReceiver(PartialResult PARAM);

    public PartialResult getCurrentTime(PartialResult PARAM);

    public PartialResult getModelTime(PartialResult PARAM);

    public PartialResult getInside(PartialResult PARAM);

    public PartialResult getInsideReceivers();

    //static public PartialResult getRelationIndex(PartialResult PARAM, PartialResult PARAM2, PartialResult PARAM3);

    public PartialResult isMultiport();

    public PartialResult hasRoom(PartialResult PARAM);

    public PartialResult hasRoomInside(PartialResult PARAM);

    public PartialResult hasToken(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult hasToken(PartialResult PARAM);

    public PartialResult hasTokenInside(PartialResult PARAM);

    public PartialResult insertLink(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult insideSinkPortList();

    public PartialResult insideSourcePortList();

    public PartialResult isInsideConnected();

    public PartialResult isKnown(PartialResult PARAM);

    public PartialResult isKnown();

    public PartialResult isKnownInside(PartialResult PARAM);

    public PartialResult isOutsideConnected();

    public PartialResult liberalLink(PartialResult PARAM);

    public PartialResult link(PartialResult PARAM);

    public PartialResult numberOfSinks();

    public PartialResult numberOfSources();

    public PartialResult removeIOPortEventListener(PartialResult PARAM);

    public PartialResult send(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult send(PartialResult PARAM, PartialResult PARAM2, PartialResult PARAM3);

    public PartialResult sendClear(PartialResult PARAM);

    public PartialResult sendClearInside(PartialResult PARAM);

    public PartialResult sendInside(PartialResult PARAM, PartialResult PARAM2);

    public PartialResult setContainer(PartialResult PARAM);

    public PartialResult setMultiport(PartialResult PARAM);

    public PartialResult sinkPortList();

    public PartialResult sourcePortList();

    public PartialResult transferInputs();

    public PartialResult transferOutputs();

    public PartialResult unlink(PartialResult PARAM);

    //public PartialResult unlink(PartialResult PARAM);

    public PartialResult unlinkAll();

    public PartialResult unlinkAllInside();

    public PartialResult unlinkInside(PartialResult PARAM);

    // FIXME: how to handle parameter type overloading?
    //public PartialResult unlinkInside(PartialResult PARAM);

}

