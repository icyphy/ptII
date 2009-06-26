package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

public class PtidesBasicReceiver extends ptolemy.codegen.c.actor.Receiver {

    public PtidesBasicReceiver(ptolemy.domains.ptides.kernel.PtidesBasicReceiver receiver) {
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
        Parameter relativeDeadline = (Parameter)sinkPort.getAttribute("relativeDeadline");
        String deadlineSecsString = null;
        String deadlineNsecsString = null;
        if (relativeDeadline != null) {
            double value = ((DoubleToken)relativeDeadline.getToken()).doubleValue();
            int intPart = (int)value;
            int fracPart = (int)((value - (double)intPart)*1000000000.0);
            deadlineSecsString = Integer.toString(intPart);
            deadlineNsecsString = Integer.toString(fracPart);
        } else {
            deadlineSecsString = new String("0");
            deadlineNsecsString = new String("0");
        }

        // Getting offsetTime.
        Parameter offsetTime = (Parameter)sinkPort.getAttribute("minDelay");
        String offsetSecsString = null;
        String offsetNsecsString = null;
        if (offsetTime != null) {
            double value = ((DoubleToken)offsetTime.getToken()).doubleValue();
            int intPart = (int)value;
            int fracPart = (int)((value - (double)intPart)*1000000000.0);
            offsetSecsString = Integer.toString(intPart);
            offsetNsecsString = Integer.toString(fracPart);
        } else {
            offsetSecsString = new String("0");
            offsetNsecsString = new String("0");
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
        args.add(deadlineSecsString);//deadline
        args.add(deadlineNsecsString);
        args.add(offsetSecsString);//offsetTime
        args.add(offsetNsecsString);
        return _generateBlockCode("createEvent", args);
    }

}
