package ptolemy.actor.lib.qm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This class implements functionality of a composite quantity manager. 
 * 
 *  This quantity manager has input ports and for every input port, there is
 *  an output port with the same name and the post fix "Out". When a relation
 *  should be interfered by this quantity manager, an input port of this
 *  quantity manager is specified. 
 *  <p>
 *  When an intermediate receiver sends a token to an input port of this
 *  quantity manager, the original receiver and the token are encoded in a 
 *  RecordToken. When such a token arrives at an output port, the original token
 *  is extracted and sent to the original receiver. 
 *  <p>
 *  A color parameter is used to perform highlighting on the ports that use this
 *  quantity manager.
 *  <p>
 *  Listeners can register for events happening in this quantity manager. Events are
 *  created when, for instance, tokens are received or tokens are sent. These
 *  events are implemented in derived classes.
 *  
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class CompositeQuantityManager extends TypedCompositeActor implements QuantityManager {

    
    public CompositeQuantityManager() throws IllegalActionException, NameDuplicationException {
        super();
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
    }

    public CompositeQuantityManager(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
    }
    
    public CompositeQuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
        _listeners = new ArrayList();
        

    }


    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @param port The port that will receive tokens from the receiver
     *  being wrapped. 
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException Not thrown in this class but may be thrown in derived classes.
     */
    public IntermediateReceiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver, port);
        IOPort outPort = null;
        for (int i = 0; i < outputPortList().size(); i++) {
            if (((IOPort)this.outputPortList().get(i)).getName().equals(port.getName() + "Out")) {
                outPort = (IOPort)this.outputPortList().get(i);
            }
        }
        if (outPort == null) {
            throw new IllegalActionException(this, "No matching outPort for " + 
                    port.getName() + " available.");
        }
        if (outPort._localReceiversTable == null) {
            Receiver[][] result = new Receiver[1][1];
            Receiver r = new DEReceiver(outPort);
            List<Receiver[][]> occurrences = new LinkedList<Receiver[][]>();
            occurrences.add(result);
            outPort._localReceiversTable = new HashMap<IORelation, List<Receiver[][]>>();
            outPort._localReceiversTable.put(new IORelation(), occurrences);
        } 
        return intermediateReceiver;
    }
    

    /** Add a quantity manager monitor to the list of listeners.
     *  @param monitor The quantity manager monitor.
     */
    public void registerListener(QuantityManagerMonitor monitor) {
        if (_listeners == null) {
            _listeners = new ArrayList<QuantityManagerListener>();
        }
        _listeners.add(monitor);
    }

    /** Initialize the actor.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenCount = 0;
    }

    /** Notify the monitor that an event happened.
     *  @param source The source actor that caused the event in the
     *      quantity manager.
     *  @param messageId The ID of the message that caused the event in
     *      the quantity manager.
     *  @param messageCnt The amount of messages currently being processed
     *      by the quantity manager.
     *  @param eventType Type of event.
     */
    public void sendQMTokenEvent(Actor source, int messageId, int messageCnt,
            EventType eventType) {
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                ((QuantityManagerListener) listeners.next()).event(this,
                        source, messageId, messageCnt, getDirector()
                                .getModelTime().getDoubleValue(), eventType);
            }
        }
    }

    /** The color associated with this actor used to highlight other
     *  actors or connections that use this quantity manager. The default value
     *  is the color red described by the expression {1.0,0.0,0.0,1.0}.
     */
    public ColorAttribute color;

    /** If the attribute is <i>color</i>, then update the highlighting colors
     *  in the model.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == color) {
            // FIXME not implemented yet.
        }
        super.attributeChanged(attribute);
    }
   
    
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            // Need to read from port parameters
            // first because in some domains (e.g. SDF)
            // the behavior of the schedule might depend on rate variables
            // set from ParameterPorts.
            for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort p = (IOPort) inputPorts.next();

                if (p instanceof ParameterPort) {
                    ((ParameterPort) p).getParameter().update();
                }
            }

            // Use the local director to transfer inputs from
            // everything that is not a port parameter.
            // The director will also update the schedule in
            // the process, if necessary.
            for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort p = (IOPort) inputPorts.next();

                if (!(p instanceof ParameterPort)) {
                    getDirector().transferInputs(p);
                }
            }

            if (_stopRequested) {
                return;
            }

            getDirector().fire();

            if (_stopRequested) {
                return;
            }

            Iterator<?> outports = outputPortList().iterator();
            while (outports.hasNext() && !_stopRequested) {
                IOPort p = (IOPort) outports.next();
                if (p.getInsideReceivers()[0][0].hasToken()) {
                    RecordToken token = (RecordToken) p.getInsideReceivers()[0][0].get();
                    ObjectToken receiverToken = (ObjectToken) token.get("receiver");
                    Receiver receiver = (Receiver) receiverToken.getValue();
                    Token payload = (Token) token.get("payload");
                    receiver.put(payload);
                }
            } 
        } finally {
            _workspace.doneReading();
        }
 
    }

    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;

    /** Amount of tokens currently being processed by the switch. */
    protected int _tokenCount;

    @Override
    public void reset() { 
    }

    public void sendToken(Receiver source, Receiver receiver, Token token) throws IllegalActionException {
        throw new IllegalActionException(this, "Port must be specified");
    }
    
    
    
    public void sendToken(Receiver source, Receiver receiver, Token token, IOPort port)
            throws IllegalActionException {
        prefire(); // has to be done such that the director gets the current time
        String[] labels = {"receiver", "payload"};
        ObjectToken receiverToken = new ObjectToken(receiver);
        Token[] values = {receiverToken, token};
        RecordToken recordToken = new RecordToken(labels, values);
        for (int i = 0; i < port.insidePortList().size(); i++) { 
            ((IOPort)port.insidePortList().get(i)).getReceivers()[0][0].put(recordToken);
            ((CompositeActor)getContainer()).getDirector().fireAtCurrentTime(this);
        } 
    }
    

    @Override
    public Receiver getReceiver(Receiver receiver)
            throws IllegalActionException { 
        return null;
    }
    
}
