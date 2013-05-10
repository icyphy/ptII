/* This actor implements a quantity manager that is a composite actor.

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

package ptolemy.actor.lib.qm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.ResourceAttributes;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
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

/** This class implements functionality of a composite quantity manager.
*
*  <p>
*  When an intermediate receiver sends a token to an input port of this
*  quantity manager, the original receiver and the token are encoded in a
*  RecordToken. When such a token arrives at an output port, the original token
*  is extracted and sent to the original receiver.
*  <p>
*  A color parameter is used to perform highlighting on the ports that use this
*  quantity manager.
*
*  @author Patricia Derler
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating Yellow (derler)
*  @Pt.AcceptedRating Red (derler)
*/
public class CompositeQM extends TypedCompositeActor implements QuantityManager, Decorator {

    /** Construct a CompositeQM in the specified workspace with
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
    public CompositeQM(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /** Construct a CompositeQM with a name and a container.
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
    public CompositeQM(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      parameters                           ////

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

    /** Clone the actor into the specified workspace.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown here.
     *  @return A new CompositeQM.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CompositeQM newObject = (CompositeQM) super.clone(workspace);
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
                return new CQMAttributes(target, this);
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
                for (Object port : ((Actor)object).inputPortList()) {
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
        if (_debugging) {
            _debug("Calling fire() at " + getDirector().getModelTime());
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            // No input ports.

            if (_stopRequested) {
                return;
            }

            for (Const mappedConst : _tokens.keySet()) {
                mappedConst.value.setToken(_tokens.get(mappedConst));
                mappedConst.fire();
                System.out.println(mappedConst);
            }
            _tokens.clear();

            getDirector().fire();

            if (_stopRequested) {
                return;
            } 
            for (Object entity : entityList()) {
                if (entity instanceof CQMOutputPort) {
                    CQMOutputPort outputPort = ((CQMOutputPort)entity);
                    while (outputPort.hasToken()) {
                        RecordToken recordToken = (RecordToken) outputPort.takeToken();
                        Receiver receiver = (Receiver) ((ObjectToken) recordToken.get("receiver")).getValue();
                        Token token = recordToken.get("token");
                        receiver.put(token);
                    }
                }
            } 
        } finally {
            _workspace.doneReading();
        }
    }
    
    public ColorAttribute getColor() {
        return color;
    }

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    public boolean isGlobalDecorator() {
        return true;
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
    
    /** Reset.
     */
    public void reset() {
        // FIXME what to do here?
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
    public void setContainer(CompositeEntity container) throws IllegalActionException,
            NameDuplicationException {
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

    /** Set an attribute for a given port. In case the attribute is the name of the
     *  input port in the CQM that this actor port is mapped to, store the mapping
     *  and, if necessary, create the CQMInputPort.
     *  @param port The port. 
     *  @param attribute The new attribute or the attribute containing a new value.
     *  @exception IllegalActionException Thrown if attribute could not be updated.
     */
    public void setInputPortName(Port port, String inputPortName) throws IllegalActionException { 
        if (_mappedConsts == null) {
            _mappedConsts = new HashMap<Port, String>();
        }
        _mappedConsts.put(port, inputPortName); 
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
        CQMInputPort port = (CQMInputPort) getEntity(_mappedConsts.get(receiver.getContainer())); 
        if (port == null) {
            throw new IllegalActionException(this, "No mapping constant in "
                    + this.getName() + " for "
                    + receiver.getContainer().getContainer().getName() + "_"
                    + receiver.getContainer().getName());
        }
        if (_tokens == null) {
            _tokens = new HashMap<Const, Token>();
        }
        RecordToken recordToken = new RecordToken(
                new String[]{"receiver", "token"}, 
                new Token[]{new ObjectToken(receiver), token});
        _tokens.put(port, recordToken);

        ((CompositeActor) getContainer()).getDirector().fireAtCurrentTime(this);

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
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
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
        
        _parameters = new HashMap<IOPort, List<Attribute>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap<Const, Token> _tokens;
    
    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;

    private HashMap<Port, String> _mappedConsts;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class CQMAttributes extends ResourceAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public CQMAttributes(NamedObj target, CompositeQM decorator)
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
        public CQMAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** 
         */
        public Parameter inputPort; 
        
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (attribute == inputPort) {
                _inputPort = ((StringToken)((Parameter)attribute).getToken()).stringValue();
                IOPort port = (IOPort) getContainer();
                CompositeQM compositeQM = (CompositeQM) getDecorator();
                if (compositeQM != null) {
                    compositeQM.setInputPortName(port, _inputPort);
                }
            } else {
                super.attributeChanged(attribute);
            } 
        } 

        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                inputPort = new Parameter(this, "inputPort", new StringToken("")); 
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }
        
        private String _inputPort; 
    }
}
