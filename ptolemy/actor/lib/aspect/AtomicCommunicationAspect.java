/* This abstract class implements functionality of an atomic
    communication aspect.

@Copyright (c) 2010-2014 The Regents of the University of California.
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
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectAttributes;
import ptolemy.actor.CommunicationAspectListener;
import ptolemy.actor.CommunicationAspectListener.EventType;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** This abstract class implements functionality of an atomic
 *  communication aspect.
 *  Listeners can register with this actor for events happening in this quantity
 *  manager. Events are
 *  created when, for instance, tokens are received or tokens are sent. These
 *  events are implemented in derived classes.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public abstract class AtomicCommunicationAspect extends TypedAtomicActor
implements CommunicationAspect, Decorator {

    /** Construct an AtomicQuantityManager in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public AtomicCommunicationAspect(Workspace workspace) {
        super(workspace);
    }

    /** Construct an AtomicQuantityManager with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AtomicCommunicationAspect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ColorAttribute color = new ColorAttribute(this,
                decoratorHighlightColorName);
        color.setExpression("{1.0,0.6,0.0,1.0}");
        _listeners = new ArrayList<CommunicationAspectListener>();
        _parameters = new HashMap<IOPort, List<Attribute>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort) {
            try {
                return new CommunicationAspectAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    @Override
    public List<NamedObj> decoratedObjects() {
        if (workspace().getVersion() == _decoratedObjectsVersion) {
            return _decoratedObjects;
        }
        _decoratedObjectsVersion = workspace().getVersion();
        List<NamedObj> list = new ArrayList();
        CompositeEntity container = (CompositeEntity) getContainer();
        for (Object object : container.deepEntityList()) {
            if (object instanceof Actor) {
                for (Object port : ((Actor) object).inputPortList()) {
                    list.add((NamedObj) port);
                }
            }
        }
        _decoratedObjects = list;
        return list;
    }

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    @Override
    public boolean isGlobalDecorator() {
        return true;
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AtomicCommunicationAspect newObject = (AtomicCommunicationAspect) super
                .clone(workspace);
        newObject._listeners = null;
        newObject._parameters = null;

        newObject._decoratedObjects = null;
        newObject._decoratedObjectsVersion = -1L;
        return newObject;
    }

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException Not thrown in this class but may be thrown in derived classes.
     */
    @Override
    public Receiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Add a communication aspect monitor to the list of listeners.
     *  @param monitor The communication aspect monitor.
     */
    @Override
    public void registerListener(CommunicationAspectListener monitor) {
        if (_listeners == null) {
            _listeners = new ArrayList<CommunicationAspectListener>();
        }
        _listeners.add(monitor);
    }

    /** Initialize the actor.
     *  @exception IllegalActionException Thrown by super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tokenCount = 0;
    }

    /** Notify the monitor that an event happened.
     *  @param source The source actor that caused the event in the
     *      communication aspect.
     *  @param messageId The ID of the message that caused the event in
     *      the communication aspect.
     *  @param messageCnt The amount of messages currently being processed
     *      by the communication aspect.
     *  @param eventType Type of event.
     */
    public void sendCommunicationEvent(Actor source, int messageId,
            int messageCnt, EventType eventType) {
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                ((CommunicationAspectListener) listeners.next()).event(this,
                        source, messageId, messageCnt, getDirector()
                        .getModelTime().getDoubleValue(), eventType);
            }
        }
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
    @Override
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

    ///////////////////////////////////////////////////////////////////
    ////                      protected fields                     ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Send token to receiver.
     *  @param receiver The receiver.
     *  @param token The token.
     *  @exception NoRoomException If the receiver has no room for the token.
     *  @exception IllegalActionException If the receiver cannot receive the token.
     */
    protected void _sendToReceiver(Receiver receiver, Token token)
            throws NoRoomException, IllegalActionException {
        if (receiver instanceof IntermediateReceiver) {
            ((IntermediateReceiver) receiver).source = this;
        }
        receiver.put(token);
    }

    /** List of parameters per port.
     */
    protected HashMap<IOPort, List<Attribute>> _parameters;

    /** Amount of tokens currently being processed by the switch. */
    protected int _tokenCount;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Cached list of decorated objects. */
    private List<NamedObj> _decoratedObjects;

    /** Version for _decoratedObjects. */
    private long _decoratedObjectsVersion = -1L;

    /** Listeners registered to receive events from this object. */
    private ArrayList<CommunicationAspectListener> _listeners;

}
