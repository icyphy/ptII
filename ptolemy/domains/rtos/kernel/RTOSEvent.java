/* A real-time operating system event in the RTOS domain.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.actor.Receiver;
import ptolemy.actor.Actor;
import ptolemy.data.Token;

import java.lang.Comparable;

//////////////////////////////////////////////////////////////////////////
//// RTOSEvent

/** A RTOS event in an event that triggers the execution of a RTOS actor
 *  (task). It contains destination receiver, a token, 
 *  a priority for the destination receiver, a flag <i>hasBeenPreempted</i>
 *  indicating whether the processing of it has been started but has not
 *  finished (due to preemption), and an <i>processingTime</i> for the time 
 *  needed to finish processing this event. Note that for an event
 *  that has been preempted, the <i>processingTime</i> is smalled than
 *  the execution time of the destination actor. 
 *  <p>
 *  A event queue is used to sort these event, based on 
 *  <pre>
 *     - priority
 *     - and whether it has been started
 *  </pre>
 *  in that order.
 *  <p>
 *  Notice that a pure event is not a treated as a RTOS event. 
 *  They are external events that carries time stamps, implemented
 *  use the DEEvent class.
 *  @author  Jie Liu
 *  @version $Id$
 *  @see ptolemy.domains.de.kernel.DEEvent
 */
public final class RTOSEvent implements Comparable {

    /** Construct an event with the specified destination receiver,
     *  token, priority, and executionTime. Upon creation,
     *  the processing of an event has not beed preempted.
     *  @param receiver The destination receiver.
     *  @param token The transferred token.
     *  @param priority The priority of the port that contains the 
     *         destination receiver.
     *  @param processingTime The time needed to finish processing the event.
     *  @exception NullPointerException If the receiver is null or is
     *   not contained by a port contained by an actor.
     */
    public RTOSEvent(RTOSReceiver receiver, Token token, int priority,
            double processingTime) {
        _receiver = receiver;
        if (receiver != null) {
            _actor = (Actor)receiver.getContainer().getContainer();
        }
        _token = token;
        _priority = priority;
        _hasBeenPreempted = false;
        _processingTime = processingTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the destination actor for this event.
     *  @return The destination actor.
     */
    public final Actor actor() {
        return _actor;
    }

    /** Compare the tag of this event with the specified event for order.
     *  See compareTo(RTOSEvent event) for the comparison rules.
     *  The argument has to be an instance of RTOSEvent or a
     *  ClassCastException will be thrown.
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     * @exception ClassCastException If the argument is not an instance
     *  of RTOSEvent.
     */
    public final int compareTo(Object event) {
        return compareTo((RTOSEvent)event);
    }
    
    /** Compare the tag of this event with the specified event for order.
     *  Return -1, zero, or +1 if this
     *  event is less than, equal to, or greater than the specified event.
     *  The priority is checked first. Return 1 if the priority of this
     *  event is strictly smaller than that of the argument. 
     *  Return -1 if the priority of this event is strictly greater than
     *  the arguement. If the two priorities are
     *  identical, then the hasBeenPreempted field is checked.  
     *  return 1 if hasBeenPreempted of this event is true.
     *  Return -1 if this event has not been preempted, but the argument
     *  has. Return 0 otherwise, i.e. none has been preempted.
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     */
    public final int compareTo(RTOSEvent event) {

        if ( _priority > event._priority)  {
            return 1;
        } else if ( _priority < event._priority) {
            return -1;
        } else if ( _hasBeenPreempted ) {
            return -1;
        } else if ( !_hasBeenPreempted && event._hasBeenPreempted ) {
            return 1;
        } else {
            return 0;
        }
    }

    /** Return the destination receiver of this event. If the event is pure,
     *  then return null.
     *  @return The destination receiver
     */
    public final RTOSReceiver receiver() {
        return _receiver;
    }

    /** Compare the tag of this event with the specified and return true
     *  if they are equal and false otherwise.  This is provided along
     *  with compareTo() because it is slightly faster when all you need
     *  to know is whether the events are simultaneous.
     *  @param event The event to compare against.
     */
    public final boolean isEquallyPriorTo(RTOSEvent event) {
        return (_priority == event._priority) &&
            (!_hasBeenPreempted) && (!event._hasBeenPreempted);
    }

    /** Return the token contained by this event.
     *  @return The token in this event.
     */
    public final Token token() {
        return _token;
    }

    /** Return the priority.
     *  @return The priority.
     */
    public final int priority() {
        return _priority;
    }

    /** Return true if the processing of this event has been preempted.
     *  @return The hasBeenPreempted.
     */
    public final boolean hasBeenPreempted() {
        return _hasBeenPreempted;
    }

    /** Mark that the processing of this event has been preempted.
     *  @param time The time when the processing of this event is preempted
     */
    public final void preemptAfter(double time) {
        _hasBeenPreempted = true;
        _processingTime = _processingTime - time;
    }
    
    /** Return the time needed to finish processing this event.
     *  @return The time needed to finish processing this event.
     */
    public final double processingTime() {
        return _processingTime;
    }

    /** Return a description of the event, including the contained token
     *  (or "null" if there is none) and the time stamp.
     *  @return The token as a string with the time stamp.
     */
    public String toString() {
        return "RTOSEvent(token=" + _token + ", priority=" + _priority 
            + ", dest=" 
            + _actor
            + ", hasBeenPreempted=" + _hasBeenPreempted
            + ", processingTime=" + _processingTime + ")";
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The destination receiver.
    private RTOSReceiver _receiver;

    // The destination actor.
    private Actor _actor;

    // The token contained by this event.
    private Token _token;

    // The priority.
    private int _priority;

    // Indicate whether the processing of the event has been preempted.
    private boolean _hasBeenPreempted;

    // The time needed to process the event.
    private double _processingTime;

}


    


    




