/* A TopologyChangeRequest is an an aggregation of topology change events.

 Copyright (c) 1997-1998 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

@ProposedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.event;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TopologyChangeRequest
/**
A TopologyChangeRequest contains a sequence of mutation events represented
by instances of TopologyEvent.  Generally, objects that
need to change the topology (<b>clients</b> or <b>sources</b>)
will create an anonymous inner class that extends this class and override
the method constructEventQueue().  In that method, the code will
create entities, relations, and so on, and call methods such as
queueEntityAddedEvent() to create and queue events.
<p>
A <b>server</b> is an object that can safely perform the mutations.
In specific classes of models of computation, there are certain
points where mutations of a graph can be safely performed. Thus, the
server would be an instance of Director.
The server performs the mutations by calling performRequest() on this
object.  It then calls notifyListeners() for any listeners that have
registered with the server to be notified of topology changes.

@author John Reekie
@version $Id$
@see TopologyListener, TopologyEvent */
public abstract class TopologyChangeRequest {

    /** Construct a new mutation request with the given client. The
     * client should be the object that is creating this object.
     */
    public TopologyChangeRequest (Object client) {
        this._client = client;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the mutation represented by this object. This is an
     * abstract method, so subclasses must override this method
     * and use it to construct the queue of pending topology events.
     */
    public abstract void constructEventQueue() throws Exception;

    /** Send the sequence of mutation events to the mutation listener
     * argument. This method must be called only after processEvents()
     * has successfully completed. In general, the caller will
     * pass an instance of TopologyMulticaster, so that a number
     * mutation listeners will be notified with the events.
     *
     * @param listener The mutation listener to pass the
     * sequence of events to
     * @exception IllegalActionException If any topology change in
     *  sequence of mutation events has not yet been implemented.
     */
    public void notifyListeners (TopologyListener listener)
            throws IllegalActionException {
        Enumeration elts = _events.elements();
        while (elts.hasMoreElements()) {
            TopologyEvent event = (TopologyEvent) elts.nextElement();
            event.notifyListeners(listener);
        }
    }

    /** Perform the actions contained by the mutation events
     * in this mutation request. (Note that no events are
     * passed to any listeners.) If any action throws an exception,
     * undo all the actions done so far, and then throw a
     * TopologyFailedException containing the original exception.
     * If any action undo fails, throw a TopologyFailedException
     * containing both the original exception and the exception
     * thrown on undo.
     *
     * @throws TopologyChangeFailedException if the mutation failed
     * @exception IllegalActionException If the change has already
     *   been implemented.
     */
    public void performRequest () throws TopologyChangeFailedException,
           IllegalActionException {
        Enumeration elts = _events.elements();
        LinkedList doneEvents = new LinkedList();
        TopologyChangeFailedException exception;

        while (elts.hasMoreElements()) {
            TopologyEvent event = (TopologyEvent) elts.nextElement();
            try {
                event.doTopologyChange();
            } catch (Exception doException) {
                /* If it failed, undo that last event and ignore errors
                 */
                try {
                    event.undoTopologyChange();
                }
                catch (Exception e) {}

                // Set up the exception to throw
                exception = new TopologyChangeFailedException(event, doException);

                // Now undo the events that were already done
                elts = doneEvents.elements();
                while (elts.hasMoreElements()) {
                    TopologyEvent undoEvent = (TopologyEvent) elts.nextElement();
                    try {
                        undoEvent.undoTopologyChange();
                    }
                    catch (Exception undoException) {
                        // Sheesh! That failed, so add the info into
                        // the exception and throw it
                        exception.failedEvent = undoEvent;
                        exception.thrownExceptionOnUndo = undoException;
                        throw exception;
                    }
                }
                // After undoing the events, throw the exception
                throw exception;
            }
            /* If the change didn't fail., remember it in case a later one does
             */
            doneEvents.insertFirst(event);
        }
    }

    /** Return an enumartion of the queued events.
     *
     * @returns An enumeration of the events in this request.
     */
    public Enumeration queuedEvents () {
        return _events.elements();
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * add <code>entity</code> to <code>compositeEntity</code>.
     *
     * @param compositeEntity The future container
     * @param entity The future containee
     */
    public final void queueEntityAddedEvent (
            CompositeEntity compositeEntity,
            ComponentEntity componentEntity) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.ENTITY_ADDED,
                _client,
                compositeEntity,
                componentEntity));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * remove <code>entity</code> from <code>compositeEntity</code>.
     *
     * @param compositeEntity The current container
     * @param entity The current containee
     */
    public final void queueEntityRemovedEvent(
            CompositeEntity compositeEntity,
            ComponentEntity componentEntity) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.ENTITY_REMOVED,
                _client,
                compositeEntity,
                componentEntity));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * add <code>port</code> to <code>entity</code>.
     *
     * @param entity The future container
     * @param port The future containee
     */
    public final void queuePortAddedEvent (Entity entity, Port port) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.PORT_ADDED,
                _client,
                entity,
                port));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * link <code>relation</code> to <code>port</code>.
     *
     * @param relation The relation to be linked
     * @param port The port to be linked
     */
    public final void queuePortLinkedEvent (Relation relation, Port port) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.PORT_LINKED,
                _client,
                relation,
                port));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * remove <code>port</code> from <code>entity</code>.
     *
     * @param entity The current container
     * @param port The current containee
     */
    public final void queuePortRemovedEvent (Entity entity, Port port) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.PORT_REMOVED,
                _client,
                entity,
                port));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * unlink <code>relation</code> from <code>port</code>.
     *
     * @param relation The relation to be unlinked
     * @param port The port to be unlinked
     */
    public final void queuePortUnlinkedEvent (Relation relation, Port port) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.PORT_UNLINKED,
                _client,
                relation,
                port));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * add <code>relation</code> to <code>compositeEntity</code>.
     *
     * @param compositeEntity The future container
     * @param relation The future containee
     */
    public final void queueRelationAddedEvent (
            CompositeEntity compositeEntity,
            ComponentRelation componentRelation) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.RELATION_ADDED,
                _client,
                compositeEntity,
                componentRelation));
    }

    /** Create a mutation event and add it to the internal
     * queue of pending events. When invoked, the event will
     * remove <code>relation</code> from <code>compositeEntity</code>.
     *
     * @param compositeEntity The current container
     * @param relation The current containee
     */
    public final void queueRelationRemovedEvent (
            CompositeEntity compositeEntity,
            ComponentRelation componentRelation) {
        _events.insertLast(new TopologyEvent(
                TopologyEvent.RELATION_REMOVED,
                _client,
                compositeEntity,
                componentRelation));
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // The queue of events
    private LinkedList _events = new LinkedList();

    // The object that created this request
    private Object _client;
}
