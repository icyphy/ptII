/* This actor implements a communication aspect that is a composite actor.

@Copyright (c) 2011-2013 The Regents of the University of California.
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

package ptolemy.actor.lib.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspectListener;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.CommunicationAspectAttributes;
import ptolemy.actor.CommunicationAspect; 
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.Const;
import ptolemy.actor.ResourceAttributes;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** This class implements functionality of a composite communication aspect.
*
*  <p>
*  When an intermediate receiver sends a token to an input port of this
*  communication aspect, the original receiver and the token are encoded in a
*  RecordToken. When such a token arrives at an output port, the original token
*  is extracted and sent to the original receiver. 
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class CompositeCommunicationAspect extends TypedCompositeActor implements
        CommunicationAspect, Decorator {

    /** Construct a CompositeQuantityManager in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeCommunicationAspect(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /** Construct a CompositeQuantityManager with a name and a container.
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
    public CompositeCommunicationAspect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    /** This parameter indicates whether the tokens received via the 
     *  ImmediateReceivers are immediately forwarded to the wrapped 
     *  receivers or whether they are delayed by this communication aspect
     *  and only forwarded through a CommunicationResponsePort. 
     *  This parameter is a boolean that defaults to false.
     */
    public Parameter justMonitor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the change of the <i>justMonitor</i> attribute by
     *  updating internal variables.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If token in attribute cannot
     *    be accessed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == justMonitor) {
            _justMonitor = ((BooleanToken) justMonitor.getToken())
                    .booleanValue();
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown here.
     *  @return A new CompositeQM.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CompositeCommunicationAspect newObject = (CompositeCommunicationAspect) super
                .clone(workspace);
        newObject._parameters = new HashMap<IOPort, List<Attribute>>();
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort) {
            try {
                return new CompositeCommunicationAspectAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException Not thrown in this class but may be thrown in derived classes.
     */
    public Receiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    public List<NamedObj> decoratedObjects() {
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        for (Object object : container.deepEntityList()) {
            if (object instanceof Actor) {
                for (Object port : ((Actor) object).inputPortList()) {
                    list.add((NamedObj) port);
                }
            }
        }
        return list;
    }

    /** Override the fire and change the transferring tokens
     * from and to input/output placeholders.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("Calling fire() at " + getDirector().getModelTime());
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            _transferPortParameterInputs();

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

            // Use the local director to transfer outputs.
            getDirector().transferOutputs();

            if (_tokens != null) {
                for (Const mappedConst : _tokens.keySet()) {
                    mappedConst.value.setToken(_tokens.get(mappedConst));
                    mappedConst.fire();
                }

                _tokens.clear();
            }
            getDirector().fire();

            if (_stopRequested) {
                return;
            }
            if (!_justMonitor) {
                for (Object entity : entityList()) {
                    if (entity instanceof CommunicationResponsePort) {
                        CommunicationResponsePort outputPort = ((CommunicationResponsePort) entity);
                        while (outputPort.hasToken()) {
                            RecordToken recordToken = (RecordToken) outputPort
                                    .takeToken();
                            Receiver receiver = (Receiver) ((ObjectToken) recordToken
                                    .get("receiver")).getValue();
                            Token token = recordToken.get("token");
                            receiver.put(token);
                        }
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    public boolean isGlobalDecorator() {
        return true;
    }

    /** Add a communication aspect monitor to the list of listeners.
     *  @param monitor The communication aspect monitor.
     */
    public void registerListener(CommunicationAspectListener monitor) {
        if (_listeners == null) {
            _listeners = new ArrayList<CommunicationAspectListener>();
        }
        _listeners.add(monitor);
    }

    /** Reset - nothing to do here.
     */
    public void reset() {
        // nothing to do here.
    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
    }

    /** Set the name of the CommunicationRequestPort that will be receiving tokens from 
     *  this actor port.
     *  @param port The actorport.  
     *  @param inputPortName The name of the CommunicationRePort. 
     */
    public void setInputPortName(Port port, String inputPortName) {
        if (_communicationRequestPortNames == null) {
            _communicationRequestPortNames = new HashMap<Port, String>();
        }
        _communicationRequestPortNames.put(port, inputPortName);
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        String name = _communicationRequestPortNames.get(receiver.getContainer());
        CommunicationRequestPort port = (CommunicationRequestPort) getEntity(name);
        if (port == null) {
            throw new IllegalActionException(this, "CommunicationRequestPort with name "
                    + name + " specified by " + receiver.getContainer()
                    + " missing");
        }
        if (_tokens == null) {
            _tokens = new HashMap<CommunicationRequestPort, Token>();
        }
        if (token != null) {
            RecordToken recordToken = new RecordToken(new String[] {
                    "receiver", "token" }, new Token[] {
                    new ObjectToken(receiver), token });
            _tokens.put(port, recordToken);
            if (_justMonitor) {
                receiver.put(token);
            }

            ((CompositeActor) getContainer()).getDirector().fireAtCurrentTime(
                    this);

            if (_debugging) {
                _debug("At time " + getDirector().getModelTime()
                        + ", initiating send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** List of parameters per port.
     */
    protected HashMap<IOPort, List<Attribute>> _parameters;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize color and private lists.
     * @exception IllegalActionException If color attribute cannot be initialized.
     * @exception NameDuplicationException If color attribute cannot be initialized.
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        ColorAttribute color = new ColorAttribute(this,
                decoratorHighlightColorName);
        color.setExpression("{1.0,0.6,0.0,1.0}");

        justMonitor = new Parameter(this, "justMonitor");
        justMonitor.setTypeEquals(BaseType.BOOLEAN);
        justMonitor.setExpression("false");
        _justMonitor = false;

        _parameters = new HashMap<IOPort, List<Attribute>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap<CommunicationRequestPort, Token> _tokens;

    /** Listeners registered to receive events from this object. */
    private ArrayList<CommunicationAspectListener> _listeners;

    private HashMap<Port, String> _communicationRequestPortNames;

    private boolean _justMonitor;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Attributes for ports decorated by this composite communication aspect.
     *  A port on an actor decorated by a composite communication aspect must
     *  specify the port in the CompositeQuantityManager 
     *  that input tokens are routed to.
     * 
     *  @author Patricia Derler
     */
    public static class CompositeCommunicationAspectAttributes extends CommunicationAspectAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public CompositeCommunicationAspectAttributes(NamedObj target, CompositeCommunicationAspect decorator)
                throws IllegalActionException, NameDuplicationException {
            super(target, decorator);
            _init();
        }

        /** Constructor to use when parsing a MoML file.
         *  @param target The object being decorated.
         *  @param name The name of this attribute.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public CompositeCommunicationAspectAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** Input port in the composite communication aspect that receives
         *  tokens from decorated actor ports.
         */
        public Parameter inputPort;

        /** React to a change in the input port attribute.
         *  @param attribute The attribute that changed.
         *  @exception IllegalActionException If the change is not acceptable
         *   to this container (not thrown in this base class).
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            IOPort port = (IOPort) getContainer();
            if (attribute == inputPort) {
                _inputPort = ((StringToken) ((Parameter) attribute).getToken())
                        .stringValue();
                CompositeCommunicationAspect compositeQM = (CompositeCommunicationAspect) getDecorator();
                if (compositeQM != null) {
                    compositeQM.setInputPortName(port, _inputPort);
                }
            }
            super.attributeChanged(attribute);
        }

        /** Add names of available CommunicationRequestPort in CompositeQM as
         *  choices to inputPort.
         *  @exception InteralErrorException Thrown if CompositeQM
         *    cannot be accessed.  
         */
        @Override
        public void updateContent() throws InternalErrorException {
            super.updateContent();
            try {
                if (getDecorator() != null) {
                    inputPort.removeAllChoices();

                    List communicationRequestPorts = ((CompositeCommunicationAspect) getDecorator())
                            .entityList(CommunicationRequestPort.class);
                    for (Object communicationRequestPort : communicationRequestPorts) {
                        String name = ((CommunicationRequestPort) communicationRequestPort).getName();
                        inputPort.addChoice(name);
                    }
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                inputPort = new StringParameter(this, "inputPort");
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }

        private String _inputPort;
    }
}
