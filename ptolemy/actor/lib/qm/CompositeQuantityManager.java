package ptolemy.actor.lib.qm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.parameters.ParameterPort; 
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
        _initialize();
    }

    public CompositeQuantityManager(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace); 
        _initialize();
    }
    
    public CompositeQuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _initialize();
    }

    private void _initialize() throws IllegalActionException, NameDuplicationException { 
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
        _listeners = new ArrayList();
        _outputMappings = new HashMap();
    }

    
    
    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public IntermediateReceiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver, port);
        //intermediateReceiver.setContainer(port);
        
        if (((IOPort)receiver.getContainer()).isOutput()) {
            Receiver[][] result = new Receiver[1][1];
            Receiver r = new DEReceiver(((IOPort)receiver.getContainer()));
            List<Receiver[][]> occurrences = new LinkedList<Receiver[][]>();
            occurrences.add(result);
            ((IOPort)receiver.getContainer())._localReceiversTable = new HashMap<IORelation, List<Receiver[][]>>();
            ((IOPort)receiver.getContainer())._localReceiversTable.put(new IORelation(), occurrences);
        } else {
            List<Receiver> list = _outputMappings.get(port);
            if (list == null) {
                list = new ArrayList();
            }
            if (!list.contains(receiver)) {
                list.add(receiver);
            }
            _outputMappings.put(port, list);
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
                    List<Receiver> receivers = _outputMappings.get(p);
                    Token token = p.getInsideReceivers()[0][0].get();
                    for (Receiver receiver : receivers) {
                        receiver.put(token);
                    } 
                }
            } 
        } finally {
            _workspace.doneReading();
        }
 
    }

    private Map<IOPort, List<Receiver>> _outputMappings;
    
    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;

    /** Amount of tokens currently being processed by the switch. */
    protected int _tokenCount;

    /** Reset.
     */
    public void reset() { 
        // FIXME what to do here?
    }

    /** Use other sendToken method.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token) throws IllegalActionException {
        throw new IllegalActionException(this, "Port must be specified");
    }
    
    /**
     * 
     * @param source
     * @param receiver
     * @param token
     * @param port
     * @throws IllegalActionException
     */
    public void sendToken(Receiver source, Receiver receiver, Token token, IOPort port)
            throws IllegalActionException {
        if (port.isInput()) {
            prefire(); // has to be done such that the director gets the current time
            for (int i = 0; i < port.insidePortList().size(); i++) { 
                ((IOPort)port.insidePortList().get(i)).getReceivers()[0][0].put(token);
                ((CompositeActor)getContainer()).getDirector().fireAtCurrentTime(this);
            } 
        } else {
            throw new IllegalActionException(this, 
                    "Outputs should be sent to target receivers in the fire, not in this method!");
        }
    }
    

    /** Other getReceiver method has to be used. 
     *  @param receiver Target receiver.
     *  @throws IllegalActionException Thrown because this method
     *  cannot be used. 
     */
    public Receiver getReceiver(Receiver receiver)
            throws IllegalActionException { 
        throw new IllegalActionException(receiver.getContainer(), "Cannot create receiver" +
        		"without specifying port of CompositeQM.");
    }
    
}
