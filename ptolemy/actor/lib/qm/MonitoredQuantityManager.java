/* This actor implements a MonitoredQuantityManager.

@Copyright (c) 2010-2013 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.qm.Bus.BusAttributes;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
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

/** This abstract class implements functionality to monitor the activity of a
 *  quantity manager as well as assign a color attribute to a quantity manager.
 *  This color is used to perform highlighting on the ports that use this
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
public abstract class MonitoredQuantityManager extends TypedAtomicActor
        implements QuantityManager, Decorator {

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public MonitoredQuantityManager(Workspace workspace) {
        super(workspace);
    }

    /** Construct a MonitoredQuantityManager with a name and a container.
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
    public MonitoredQuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        color = new ColorAttribute(this, "_color");
        color.setExpression("{1.0,0.0,0.0,1.0}");
        _listeners = new ArrayList<QuantityManagerListener>();
        _parameters = new HashMap<IOPort, List<Attribute>>();
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
                return new QMAttributes(target, this);
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
    
    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    public boolean isGlobalDecorator() {
        return true;
    }
    
    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MonitoredQuantityManager newObject = (MonitoredQuantityManager) super
                .clone(workspace);
        newObject._listeners = null;
        newObject._parameters = null;
        return newObject;
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

    /** List of parameters per port.
     */
    protected HashMap<IOPort, List<Attribute>> _parameters;  

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

    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;

    /** Amount of tokens currently being processed by the switch. */
    protected int _tokenCount;
    
    public class QMAttributes extends DecoratorAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public QMAttributes(NamedObj target, MonitoredQuantityManager decorator)
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
        public QMAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** The enable parameter specifies whether the decorated actor uses
         *  the decorator.
         *  This is a boolean that defaults to false.
         */
        public Parameter enable;  

        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                enable = new Parameter(this, "enable");
                enable.setExpression("false");
                enable.setTypeEquals(BaseType.BOOLEAN);  
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }
    }

}
