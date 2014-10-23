/* Event queue that's ordered by a linked list, which provides a total order among all events in this queue.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.ptides.kernel.PtidesEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

/**
 * Event queue that is a linked list. This provides a totally ordered sorted
 * event queue. It also allows all events to be accessed in the order they are
 * sorted.
 *
 * <p>
 * This is identical to PtidesListEventQueue except receivers are mapped to
 * MetroIIPtidesReceiver.
 * </p>
 *
 * @author Jia Zou, Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 *
 */
public class MetroIIPtidesListEventQueue implements DEEventQueue {

    /**
     * Constructs an empty event queue.
     */
    public MetroIIPtidesListEventQueue() {
        // Construct a calendar queue _cQueue with its default parameters:
        // minBinCount is 2, binCountFactor is 2, and isAdaptive is true.
        _listQueue = new LinkedList();
    }

    /**
     * Clears the event queue.
     */
    @Override
    public void clear() {
        _listQueue.clear();
    }

    /**
     * Gets the smallest event from the event queue.
     *
     * @return a PtidesEvent object.
     * @exception InvalidStateException
     *                if the getFirst() method of the queue throws it.
     */
    @Override
    public PtidesEvent get() throws InvalidStateException {
        PtidesEvent result = (PtidesEvent) _listQueue.getFirst();
        if (_debugging) {
            _debug("--- getting from queue: " + result);
        }
        return result;
    }

    /**
     * Gets the event from the event queue that is pointed by the index.
     *
     * @param index
     *            an int specifying the index.
     * @return a DEEvent object pointed to by the index.
     * @exception InvalidStateException
     *                if get() method of the queue throws it.
     */
    public PtidesEvent get(int index) throws InvalidStateException {
        PtidesEvent result = (PtidesEvent) _listQueue.get(index);
        if (_debugging) {
            _debug("--- getting " + index + "th element from queue: " + result);
        }
        return result;
    }

    /**
     * Checks if the event queue is empty.
     */
    @Override
    public boolean isEmpty() {
        return _listQueue.isEmpty();
    }

    /**
     * Puts the event queue into the event queue, and then sort it by timestamp
     * order.
     *
     * @param event
     *            a DEEvent object.
     * @exception IllegalActionException
     *                if the addFirst() method of the queue throws it.
     */
    @Override
    public void put(DEEvent event) throws IllegalActionException {
        if (_debugging) {
            _debug("+++ putting in queue: " + event);
        }
        _listQueue.addFirst(event);
        Collections.sort(_listQueue);
    }

    /** Throw an exception to indicate that this method is not supported.
     *  @param event The event to enqueue.
     *  @return In this class, this method never returns.
     *  @exception IllegalActionException Always thrown.
     */
    @Override
    public boolean remove(DEEvent event) throws IllegalActionException {
        throw new IllegalActionException(
                "remove() is not implemented by PtidesListEventQueue.");
    }

    /**
     * Returns the size of this event queue.
     */
    @Override
    public int size() {
        return _listQueue.size();
    }

    /**
     * Take this event and remove it from the event queue. If the event is a
     * DEEvent, then put the token of this event into the receiver.
     * <p>
     * NOTE: this method should only be called once for each event in the event
     * queue, unless the event is not a DEEvent. Because each time this method
     * is called, the token associated with this event is transferred to the
     * receiver. Also, the same event should not be taken out of the event queue
     * and then put into the event queue multiple times.
     *
     * @return The event associated with this index in the event queue.
     * @exception InvalidStateException
     */
    @Override
    public PtidesEvent take() throws InvalidStateException {
        PtidesEvent ptidesEvent = (PtidesEvent) _listQueue.remove();
        // put the token of this event into the destined receiver.
        if (ptidesEvent.receiver() != null) {
            ((MetroIIPtidesReceiver) ptidesEvent.receiver())
            .putToReceiver(ptidesEvent.token());
        }
        if (_debugging) {
            _debug("--- taking from queue: " + ptidesEvent);
        }
        return ptidesEvent;
    }

    /**
     * Takes this event and remove it from the event queue. If the event is a
     * DEEvent, then put the token of this event into the receiver.
     *
     * <p>
     * NOTE: this method should only be called once for each event in the event
     * queue, unless the event is not a DEEvent. Because each time this method
     * is called, the token associated with this event is transferred to the
     * receiver. Also, the same event should not be taken out of the event queue
     * and then put into the event queue multiple times.
     * </p>
     *
     * @param index
     *            The index of this event in the event queue.
     * @return The event associated with this index in the event queue.
     * @exception InvalidStateException
     */
    public PtidesEvent take(int index) throws InvalidStateException {
        PtidesEvent ptidesEvent = (PtidesEvent) _listQueue.remove(index);
        // put the token of this event into the destined receiver.
        if (ptidesEvent.receiver() != null) {
            if (ptidesEvent.receiver() instanceof MetroIIPtidesReceiver) {
                ((MetroIIPtidesReceiver) ptidesEvent.receiver())
                .putToReceiver(ptidesEvent.token());
            }
        }
        if (_debugging) {
            _debug("--- taking " + index + "th element from queue: "
                    + ptidesEvent);
        }
        return ptidesEvent;
    }

    /**
     * Returns an array representation of this event queue.
     *
     * @return an array of Objects in the list.
     */
    @Override
    public Object[] toArray() {
        return _listQueue.toArray();
    }

    /**
     * Adds a debugger listen for this event queue.
     *
     * @see #removeDebugListener
     */
    @Override
    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }

        _debugListeners.add(listener);
        _debugging = true;
    }

    /**
     * Removes the debugger listen for this event queue.
     *
     * @see #addDebugListener
     */
    @Override
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }

        _debugListeners.remove(listener);

        if (_debugListeners.size() == 0) {
            _debugListeners = null;
            _debugging = false;
        }

        return;
    }

    /**
     * Sends a debug message to all debug listeners that have registered. By
     * convention, messages should not include a newline at the end. The newline
     * will be added by the listener, if appropriate.
     *
     * @param message
     *            The message.
     */
    private final void _debug(String message) {
        if (_debugListeners == null || !_debugging) {
            return;
        } else {
            Iterator listeners = _debugListeners.iterator();

            while (listeners.hasNext()) {
                ((DebugListener) listeners.next()).message(message);
            }
        }
    }

    /** @serial The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** @serial A flag indicating whether there are debug listeners. */
    private boolean _debugging;

    /** The queue as represented by a linked list. */
    private LinkedList _listQueue;
}
