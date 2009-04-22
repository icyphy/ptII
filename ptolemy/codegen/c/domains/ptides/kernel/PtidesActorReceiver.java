package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.List;
import java.util.ArrayList;

import ptolemy.actor.IOPort;
import ptolemy.data.type.Type;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class PtidesActorReceiver extends ptolemy.codegen.c.actor.Receiver {
    public PtidesActorReceiver() {
        // TODO Auto-generated constructor stub
    }

    public PtidesActorReceiver(ptolemy.domains.ptides.kernel.PtidesActorReceiver receiver) {
        _receiver = receiver;
        // TODO Auto-generated constructor stub
    }

    public PtidesActorReceiver(NamedObj component, String name) {
        super(component, name);
        // TODO Auto-generated constructor stub
    }

    public String generateCodeForGet(int channel) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort)_receiver.getContainer();
        return "Event_Head_" + port.getContainer().getName() + "_" + port.getName()
            + "[" + channel + "]->" + port.getType().toString() + "_Value";
    }

    public String generateCodeForHasToken(int channel) throws IllegalActionException{
        IOPort port = _receiver.getContainer();
        return "Event_Head_" + port.getContainer().getName() + "_" + port.getName() 
            + "[" + channel + "] != NULL";
    }

    public String generateCodeForPut(ptolemy.codegen.c.actor.IOPort sourcePort, 
            String token) throws IllegalActionException{
        Type sourceType = ((TypedIOPort)sourcePort.getComponent()).getType();
        Type sinkType = ((TypedIOPort)_receiver.getContainer()).getType();
        
        
        // FIXME: not sure whether we should check if we are putting into an input port or
        // output port.
        // Generate a new event.
        List args = new ArrayList();
        args.add(sinkType);

        // The next line does not compile:
        //args.add(generateTypeConvertCode(sourceType, sinkType, token));
        if (1 == 1) {
            throw new RuntimeException("This code does not compile");
        }

        //args.add(token);
        args.add("");//timestamp
        args.add("");//microstep
        return _generateBlockCode("createEvent", args);
    }

}
