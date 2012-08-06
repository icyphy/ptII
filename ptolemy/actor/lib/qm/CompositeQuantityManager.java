/* This actor implements a quantity manager that is a composite actor.

@Copyright (c) 2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
    
    /** Construct a TypedCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeQuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        _initialize();
    }
    
    /** The color associated with this actor used to highlight other
     *  actors or connections that use this quantity manager. The default value
     *  is the color red described by the expression {1.0,0.0,0.0,1.0}.
     */
    public ColorAttribute color;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
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


    /** Create a receiver to mediate a communication via the specified receiver.
     *  @param receiver Target receiver.
     *  @return never returned
     *  @throws IllegalActionException Always thrown because this
     *  method cannot be used because a receiver cannot be created
     *  without specifying a port of a CompositeQuantityManager.
     */
    public Receiver getReceiver(Receiver receiver)
            throws IllegalActionException { 
        throw new IllegalActionException(receiver.getContainer(),
					 "Cannot create receiver" +
					 "without specifying a port of a CompositeQuantityManager.");
    }    
    
    
    
    private boolean _receiversInvalid = true;
    
    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        if (_receiversInvalid) {
            _outputMappings.clear();
            _receiversInvalid = false;
        }
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver, port); 
        
        if (((IOPort)(receiver.getContainer())).isInput()) {
            Receiver[][] result = new Receiver[1][1]; 
            List<Receiver[][]> occurrences = new LinkedList<Receiver[][]>();
            occurrences.add(result);
            HashMap<IORelation, List<Receiver[][]>> map = new HashMap<IORelation, List<Receiver[][]>>();
            map.put(new IORelation(), occurrences);
            ((IOPort)receiver.getContainer()).setLocalReceiversTable(map);
            ArrayList<Receiver> list = new ArrayList();
            list.add(receiver);
            _outputMappings.put(port, list);
            return receiver;
        } else {
            List<Receiver> list = _outputMappings.get(port);
            if (list == null) {
                list = new ArrayList();
            } else {
                // Some receivers are removed in the IOPort class; remove 'dead'
                // receivers here too. 
                List<Receiver> copy = new ArrayList<Receiver>(list);
                for (Receiver listReceiver : copy) {
                    if (listReceiver.getContainer() == null) {
                        list.remove(listReceiver);
                    }
                }
            }
            if (!list.contains(receiver)) {
                list.add(receiver);
            }
            _outputMappings.put(port, list);
        } 
        return intermediateReceiver;
    }
    
    /** Initialize the actor.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        _tokenCount = 0;
    }


   
    /** Override the fire and change the transferring of outputs
     *  to transfer data from output ports to target receivers. 
     */
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
                        sendQMTokenEvent((Actor) receiver.getContainer().getContainer(), 0,
                                _tokenCount, EventType.SENT);
                    } 
                    
                }
            } 
        } finally {
            _workspace.doneReading();
        }
 
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
    
    @Override
    public void preinitialize() throws IllegalActionException { 
        _receiversInvalid = true;
        super.preinitialize();
    }

    /** Reset.
     */
    public void reset() {  
        // FIXME what to do here?
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
    

    /** Use other sendToken method.
     *  @param source Receiver that sent the token.  Ignored in this method.
     *  @param receiver The receiver for which this quantity manager is mediating
     *   communication.  Ignored in this method.
     *  @param token The token for the communication to mediate.
     *  Ignored in this method.
     *  @exception IllegalActionException Always thrown because a port must be specified
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
	throws IllegalActionException {
        throw new IllegalActionException(this, "Port must be specified");
    }
    
    /** Intermediate Receiver sends token to this quantity manager which puts 
     *  the token to the right port. 
     *  @param source The source that sent the token.
     *  @param receiver The target receiver of this token.
     *  @param token Token that is sent.
     *  @param port Input port that should get the token.
     *  @throws IllegalActionException
     */
    public void sendToken(Receiver source, Receiver receiver, Token token, IOPort port)
            throws IllegalActionException {
        if (port.isInput()) {
            prefire(); // has to be done such that the director gets the current time
            for (int i = 0; i < port.insidePortList().size(); i++) { 
                ((IOPort)port.insidePortList().get(i)).getReceivers()[0][0].put(token);
                ((CompositeActor)getContainer()).getDirector().fireAtCurrentTime(this);
            } 
            sendQMTokenEvent((Actor) source.getContainer().getContainer(),
                    0, _tokenCount, EventType.RECEIVED); 
        } else {
            throw new IllegalActionException(this, 
                    "Outputs should be sent to target receivers in the fire, not in this method!");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** Amount of tokens currently being processed by the qm. */
    protected int _tokenCount;
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Initialize color and private lists. 
     * @throws IllegalActionException If color attribute cannot be initialized.
     * @throws NameDuplicationException If color attribute cannot be initialized.
     */
    private void _initialize() throws IllegalActionException, NameDuplicationException { 
        _listeners = new ArrayList();
        _outputMappings = new HashMap();
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Store mapping of output port to target receivers. */
    private Map<IOPort, List<Receiver>> _outputMappings;
    
    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;
    

}
