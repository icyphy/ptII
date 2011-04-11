/* This actor implements a Network Bus.

@Copyright (c) 2010 The Regents of the University of California.
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
import java.util.Iterator;

import ptolemy.actor.Actor; 
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/** This Plotter shows the time quantity managers in the model receive messages
 *  and send messages. Additionally, it shows how many messages are currently 
 *  being processed by the quantity manager
 * 
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public abstract class MonitoredQuantityManager extends TypedAtomicActor implements QuantityManager {
 
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
        _listeners = new ArrayList();
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
     *  @param time The time when the event happened.
     *  @param event The type of the event. e.g. message received, message sent, ... 
     */
    public void sendQMTokenEvent(Actor source, int messageId,
            int messageCnt, EventType eventType) {
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator(); 
            while (listeners.hasNext()) {
                ((QuantityManagerListener) listeners.next()).event(this,
                        source, messageId, messageCnt, getDirector().getModelTime()
                                .getDoubleValue(), eventType);
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
    
    protected void _putToReceiver(Receiver receiver, Token token) throws NoRoomException, IllegalActionException { 
        if (receiver instanceof IntermediateReceiver) {
            ((IntermediateReceiver) receiver).source = this;
        }
        receiver.put(token); 
        _tokenCount--;
        sendQMTokenEvent((Actor) receiver.getContainer()
                .getContainer(), 0, _tokenCount, EventType.SENT);
    }
    
    /** Listeners registered to receive events from this object. */
    private ArrayList<QuantityManagerListener> _listeners;
    
    /** Amount of tokens currently being processed by the switch. */
    protected int _tokenCount;

    
}
