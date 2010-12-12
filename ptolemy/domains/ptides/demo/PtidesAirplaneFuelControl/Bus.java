package ptolemy.domains.ptides.demo.PtidesAirplaneFuelControl;

import java.util.Iterator;

//import com.sun.jmx.mbeanserver.NamedObject;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.wireless.kernel.AtomicWirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessDirector;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.domains.wireless.kernel.WirelessReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

public class Bus extends AtomicWirelessChannel {

    public Bus(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * only send to receivers of actors specified in the port property "receiver"
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties) throws IllegalActionException {
        try {
            workspace().getReadAccess();

            // The following check will ensure that receivers are of type
            // WirelessReceiver.
            if (!(getDirector() instanceof WirelessDirector)) {
                throw new IllegalActionException(this,
                        "AtomicWirelessChannel can only work "
                                + "with a WirelessDirector.");
            }

            Parameter parameter = (Parameter) port.getAttribute("receiver"); 
            Object obj = getContainer().getAttribute(parameter.getDefaultExpression());
            if (obj instanceof PortParameter) {
                ((PortParameter)obj).setCurrentValue(token); 
                //((PortParameter)obj).getContainer().
            } else { 
                Port receiverPort = (Port)((ObjectToken) parameter.getToken()).getValue(); 
                _transmitTo(token, port, (Receiver)((IOPort)receiverPort).getReceivers()[0][0], properties); 
                
            } 
        } catch (Exception e) {
        	System.out.println("error transmitting");
        } finally {
            workspace().doneReading();
        }
    }
    
    protected void _transmitTo(Token token, WirelessIOPort sender,
            Receiver receiver, RecordToken properties)
            throws IllegalActionException {
        if (_debugging) {
            _debug(" * transmitting to: "
                    + receiver.getContainer().getFullName());
        }

        if (token != null) {
            if (receiver.hasRoom()) {
                WirelessIOPort destination = (WirelessIOPort) receiver
                        .getContainer();
                Token newToken = destination.convert(token);
                receiver.put(newToken);
                // Notify any channel listeners after the transmission occurs.
                channelNotify(properties, token, sender, destination);
            }
        } else {
            receiver.clear();
        }
    }
    


}




