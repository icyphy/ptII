package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.List;
import java.util.ArrayList;

import ptolemy.actor.IOPort;
import ptolemy.data.type.Type;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.IllegalActionException;

public class PtidesActorReceiver extends ptolemy.codegen.c.actor.Receiver {

    public PtidesActorReceiver(ptolemy.domains.ptides.kernel.PtidesActorReceiver receiver) {
        super(receiver);
        // TODO Auto-generated constructor stub
    }

    public String generateCodeForGet(int channel) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort)getReceiver().getContainer();
        return "Event_Head_" + generateSimpleName(port.getContainer()) + "_" + generateSimpleName(port)
            + "[" + channel + "]->" + port.getType().toString() + "_Value";
    }

    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        IOPort port = getReceiver().getContainer();
        return "Event_Head_" + generateSimpleName(port.getContainer()) + "_" + generateSimpleName(port) 
            + "[" + channel + "] != NULL";
    }

    public String generateCodeForPut(String token) throws IllegalActionException{
        Type sinkType = ((TypedIOPort)getReceiver().getContainer()).getType();
        
        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        List args = new ArrayList();
        args.add(sinkType);
        args.add(token);
        //args.add(token);
        args.add("");//timestamp
        args.add("");//microstep
        return _generateBlockCode("createEvent", args);
    }

}
