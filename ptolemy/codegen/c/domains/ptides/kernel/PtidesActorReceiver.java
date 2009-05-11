package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

public class PtidesActorReceiver extends ptolemy.codegen.c.actor.Receiver {

    public PtidesActorReceiver(ptolemy.domains.ptides.kernel.PtidesActorReceiver receiver) {
        super(receiver);
        // TODO Auto-generated constructor stub
    }

    public String generateCodeForGet(int channel) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort)getReceiver().getContainer();
        return "Event_Head_" + generateName(port) + "[" + channel + "]->Val." + port.getType().toString() + "_Value";
    }

    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        IOPort port = getReceiver().getContainer();
        return "Event_Head_" + generateName(port) + "[" + channel + "] != NULL";
    }

    public String generateCodeForPut(String token) throws IllegalActionException{
        TypedIOPort sinkPort = (TypedIOPort)getReceiver().getContainer();
        Type sinkType = sinkPort.getType();
        
        // Getting deadline.
        Parameter remainingDeadline = (Parameter)sinkPort.getAttribute("relativeDeadline");
        String deadlineString = null;
        if (remainingDeadline != null) {
            deadlineString = remainingDeadline.toString();
        } else {
            deadlineString = new String("ZERO_TIME");
        }
        
        // Getting offsetTime.
        Parameter offsetTime = (Parameter)sinkPort.getAttribute("minDelay");
        String offsetString = null;
        if (offsetTime != null) {
            offsetString = offsetTime.toString();
        } else {
            offsetString = new String("ZERO_TIME");
        }
        
        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        List args = new ArrayList();
        args.add(sinkType);
        args.add(token);
        args.add(generateName(sinkPort.getContainer()));
        args.add("Event_Head_" + generateName(sinkPort) + "[" + 
                sinkPort.getChannelForReceiver(getReceiver()) + "]");
        args.add("");//timestamp
        args.add("");//microstep
        args.add(deadlineString);//deadline
        args.add(offsetString);//offsetTime
        return _generateBlockCode("createEvent", args);
    }

}
