/* A TimeKeeper manages an actor's local value of time in the DDE domain.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (davisj@eecs.berkeley.edu)
@AcceptedRating Green (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TimeKeeper
/**
A TimeKeeper manages an actor's local value of time in the DDE domain.
A TimeKeeper is instantiated by a DDEThread and is used by the thread
to manage time for the thread's actor. A TimeKeeper has a list of
DDEReceivers that are contained by the actor that the thread controls.
As tokens flow through the DDEReceivers, the TimeKeeper keeps track
of the advancement of time local to the actor.
<P>
DDEReceivers each have three important variables: <I>receiver time</I>,
<I>last time</I> and <I>priority</I>. The receiver time of a DDEReceiver
is equal to the time of the oldest event that resides in the receiver.
The last time is equal to the time of the newest event residing on the
receiver.
<P>
A TimeKeeper manages the DDEReceivers of its actor by keeping track of
the receiver with the minimum receiver time. The actor is allowed to
consume a token from a receiver if that receiver has the unique, minimum
receiver time of all receivers managed by the TimeKeeper. The TimeKeeper
keeps track of its receivers' priorities as well. The receiver with the
highest priority is enabled to have its token consumed if the receiver
shares a common minimum receive time with one or more additional receivers.
<P>
The receiver priorities are set using the method _setReceiverPriorities() in
the following manner. All of the input receivers associated with a given
TimeKeeper are prioritized according to the inverse order in which they
were connected in the model topology. I.e., if two input receivers (rA
and rB) of an actor are connected such that receiver rA is connected
before receiver rB, then rB will have a higher priority than rA.
<P>
The above approach provides each receiver associated with a given
TimeKeeper with a unique priority, such that the set of receiver
priorities of the associated TimeKeeper is totally ordered.
<P>
A TimeKeeper manages the ordering of receivers by keeping track of
its receivers and their corresponding receiver times and priorities.
As tokens are placed in and taken out of the receivers of an actor,
the TimeKeeper's receiver list is updated. The receiver list is sorted
by ReceiverComparator. This same information allows the TimeKeeper to
determine what the current time is local to the actor.

@author John S. Davis II
@version $Id$
@since Ptolemy II 0.3
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class TimeKeeper {

    /** Construct a time keeper to manage the local time of an actor
     *  in the DDE domain. Set the receiver priorities of all receivers
     *  contained by the actor of this time keeper.
     * @param actor The DDEActor for which time will be managed.
     * @exception IllegalActionException If there is an error
     *  while setting the receiver priorities.
     */
    public TimeKeeper(Actor actor) throws IllegalActionException {
        _actor = actor;
        _receiverList = new LinkedList();
        _receiverComparator = new ReceiverComparator(this);

        _setReceiverPriorities();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this TimeKeeper. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers managed by this TimeKeeper.
     * @return The current time of this TimeKeeper.
     */
    public double getCurrentTime() {
        return _currentTime;
    }

    /** Return the active PrioritizedTimedQueue with the oldest receiver time
     *  of all receivers contained in the actor that this TimeKeeper
     *  controls. A PrioritizedTimedQueue is considered active if its receiver
     *  time is nonnegative. If all receivers of the managed actor are no
     *  longer active, then return the first receiver to become inactive.
     * @return PrioritizedTimedQueue The oldest active PrioritizedTimedQueue
     *  managed by this TimeKeeper.
     */
    public synchronized PrioritizedTimedQueue getFirstReceiver() {
        if ( _receiverList.size() == 0 ) {
            return null;
        }
        return (PrioritizedTimedQueue)_receiverList.getFirst();
    }

    /** Return the earliest possible time stamp of the next token to be
     *  consumed by the actor managed by this time keeper. Consider
     *  this returned time value as a greatest lower bound. The next
     *  time is equal to the oldest (smallest valued) receiver time of
     *  all receivers managed by this thread.
     * @return The next earliest possible time stamp to be produced by
     *  this actor.
     */
    public double getNextTime() {
        if ( _receiverList.size() == 0 ) {
            return _currentTime;
        }
        return ((PrioritizedTimedQueue)_receiverList.getFirst()).getReceiverTime();
    }

    /** Return the current value of the output time associated with
     *  this time keeper and, after so doing, set the output time to
     *  a new value that is equivalent to this time keeper's current time.
     * @return double The output time of this time keeper.
     */
    public synchronized double getOutputTime() {

        if ( !((ComponentEntity)_actor).isAtomic() ) {
            return _outputTime;
        }


        if ( _outputTime < _currentTime ) {
            _outputTime = _currentTime;
        }
        return _outputTime;
    }

    /** Update receivers controlled by this time keeper that have
     *  a receiver time equal to PrioritizedTimedQueue.IGNORE. For
     *  each such receiver, call DDEReceiver.removeIgnoredToken().
     */
    public synchronized void removeAllIgnoreTokens() {
        if ( _receiverList == null ) {
            return;
        }
        if ( _ignoredReceivers ) {
            PrioritizedTimedQueue receiver;
            for ( int i = 0; i < _receiverList.size(); i++ ) {
                receiver = (PrioritizedTimedQueue)_receiverList.get(i);
                if ( receiver.getReceiverTime() ==
                        PrioritizedTimedQueue.IGNORE ) {
                    receiver.removeIgnoredToken();
                }
            }
            _ignoredReceivers = false;
        }
    }

    /** Send a NullToken to all output channels that have a receiver
     *  time less than or equal to the current time of this time keeper.
     *  In this case, set the time stamp of the NullTokens to be equal
     *  to the current time of this time keeper. This method assumes
     *  that the actor controlled by this time keeper is atomic.
     *  <P>
     *  This method is not synchronized so the calling method should be.
     *  @param receiver The receiver that is causing this method to be invoked.
     */
    public void sendOutNullTokens(DDEReceiver receiver) {
        Iterator ports = _actor.outputPortList().iterator();
        double time = getCurrentTime();
        while ( ports.hasNext() ) {
            IOPort port = (IOPort)ports.next();
            Receiver receivers[][] =
                (Receiver[][])port.getRemoteReceivers();
            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if ( time > ((DDEReceiver)receivers[i][j])._lastTime ) {
                        ((DDEReceiver)receivers[i][j]).put(
                                new NullToken(), time );
                    }
                }
            }
        }
    }

    /** Set the current time of this TimeKeeper. If the specified
     *  time is less than the previous value for current time, then
     *  throw an IllegalArgumentException. Do not throw an
     *  IllegalArgumentException if the current time is set to
     *  PrioritizedTimedQueue.INACTIVE to indicate termination.
     * @param time The new value for current time.
     * @exception IllegalArgumentException If there is an attempt to
     *  decrease the value of current time to a nonnegative number.
     */
    public synchronized void setCurrentTime(double time) {
        if ( time < _currentTime
                && time != PrioritizedTimedQueue.INACTIVE
                && time != PrioritizedTimedQueue.IGNORE ) {
            throw new IllegalArgumentException(
                    ((NamedObj)_actor).getName() + " - Attempt to "
                    + "set current time in the past."
                    + " time = " + time
                    + "; current time = " + _currentTime );
        }

        if ( time != PrioritizedTimedQueue.IGNORE ) {
            _currentTime = time;
        }
    }

    /** Update the list of receivers by adding a receiver to the
     *  receiver list if not already present and then sorting the
     *  list. The receiver list is sorted according to a ReceiverComparator.
     * @param prioritizedTimedQueue The PrioritizedTimedQueue whose
     *  position is being updated.
     * @see ptolemy.domains.dde.kernel.ReceiverComparator
     */
    public synchronized void updateReceiverList(
            PrioritizedTimedQueue prioritizedTimedQueue) {
        if (_receiverList == null) {
            _receiverList = new LinkedList();
        }

        double time = prioritizedTimedQueue.getReceiverTime();
        if (time == PrioritizedTimedQueue.IGNORE) {
            _ignoredReceivers = true;
        }

        if ( !_receiverList.contains(prioritizedTimedQueue) ) {
            // Add receiver to list with a touch of
            // optimization before actually sorting.
            if ( time > 0 ) {
                _receiverList.addFirst(prioritizedTimedQueue);
            } else {
                _receiverList.addLast(prioritizedTimedQueue);
            }
        }

        Collections.sort( _receiverList, _receiverComparator );
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly variables              ////

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods                   ////

    /** Print the contents of the receiver list contained by
     *  this actor.
     *  Use for testing purposes only.
     synchronized void printReceiverList() {
     String name = ((NamedObj)_actor).getName();
     System.out.println("\n###Print "+name+"'s ReceiverList.");
     System.out.println("   Number of Receivers in ReceiverList = "
     + _receiverList.size() );
     if ( _receiverList.size() == 0 ) {
     System.out.println("\tList is empty");
     System.out.println("###End of printReceiverList()\n");
     return;
     }
     for ( int i = 0; i < _receiverList.size(); i++ ) {
     PrioritizedTimedQueue testReceiver = (PrioritizedTimedQueue)_receiverList.get(i);
     double time = testReceiver.getReceiverTime();
     Token token = null;
     if ( testReceiver._queue.size() > 0 ) {
     token = ((PrioritizedTimedQueue.Event)testReceiver._queue.get(0)).getToken();
     }
     String msg = "\t"+name+"'s Receiver "+i+
     " has a time of " +time+" and ";
     if ( token instanceof NullToken ) {
     msg += "contains a NullToken";
     } else if ( token != null ) {
     msg += "contains a RealToken";
     } else {
     msg += "contains no token";
     }
     System.out.println(msg);
     }
     System.out.println("###End of printReceiverList()\n");
     }
    */

    /** Set the output time associated with this time keeper.
     * @param outputTime The output time of this time keeper.
     * @exception IllegalActionException If the output time is
     *  less than the current time.
     */
    synchronized void _setOutputTime(double outputTime)
            throws IllegalActionException {

        if ( outputTime < _currentTime ) {
            throw new IllegalActionException("Illegal attempt "
                    + "to set the time keeper's output time "
                    + "in the past");
        }
        if ( outputTime != PrioritizedTimedQueue.IGNORE ) {
            _outputTime = outputTime;
        }
    }

    /** Set the priorities of the receivers contained in the input
     *  ports of the actor managed by this time keeper. Order the
     *  receiver priorities relative to one another according to the
     *  inverse order in which they were connected to the model
     *  topology. I.e., if two input receivers (receiver A and
     *  receiver B) are added to an actor such that receiver A is
     *  connected in the model topology before receiver B, then
     *  receiver B will have a higher priority than receiver A.
     * @exception IllegalActionException If an error occurs during
     *  receiver access.
     */
    synchronized void _setReceiverPriorities()
            throws IllegalActionException {

        LinkedList listOfPorts = new LinkedList();
        Iterator inputPorts = _actor.inputPortList().iterator();
        if ( !inputPorts.hasNext() ) {
            return;
        }

        //
        // First Order The Ports
        //
        while ( inputPorts.hasNext() ) {
            listOfPorts.addLast( (IOPort)inputPorts.next() );
        }

        //
        // Now set the priorities of each port's receiver, set
        // the receiving time keeper and initialize the receiverList.
        //
        int cnt = 0;
        int currentPriority = 0;
        while ( cnt < listOfPorts.size() ) {
            IOPort port = (IOPort)listOfPorts.get(cnt);
            Receiver[][] receivers = port.getReceivers();
            for ( int i = 0; i < receivers.length; i++ ) {
                for ( int j = 0; j < receivers[i].length; j++ ) {
                    ((DDEReceiver)receivers[i][j])._priority =
                        currentPriority;
                    //
                    // Is the following necessary??
                    //
                    updateReceiverList( (DDEReceiver)receivers[i][j] );

                    currentPriority++;
                }
            }
            cnt++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor that is managed by this time keeper.
    private Actor _actor;

    // The currentTime of the actor that is controlled by this
    // time keeper is equivalent to the minimum positive receiverTime
    // of each input receiver.
    private double _currentTime = 0.0;

    // The output time associated with this time keeper.
    private double _outputTime = 0.0;

    // This flag is set to true if any of the receivers have
    // a time stamp of PrioritizedTimedQueue.IGNORE
    boolean _ignoredReceivers = false;

    // The comparator that sorts the receivers
    // controlled by this time keeper.
    private ReceiverComparator _receiverComparator;

    // The _receiverList stores the receivers controlled by
    // this time keeper. The receivers are ordered
    // according to the receiver comparator.
    private LinkedList _receiverList;

}
