
package ptolemy.domains.ptides.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.domains.dde.kernel.ReceiverComparator;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TimeKeeper


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
        //_receiverComparator = new ReceiverComparator(this);

        _setReceiverPriorities();
        _currentTime = new Time(actor.getDirector());
        _outputTime = new Time(actor.getDirector());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this TimeKeeper. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers managed by this TimeKeeper.
     *  @return The current time of this TimeKeeper.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelTime()}
     *  @see #setCurrentTime(Time)
     */
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
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
        if (_receiverList.size() == 0) {
            return null;
        }

        return (PrioritizedTimedQueue) _receiverList.getFirst();
    }

    /** Return the current time of this TimeKeeper. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers managed by this TimeKeeper.
     * @return The current time of this TimeKeeper.
     */
    public Time getModelTime() {
        return _currentTime;
    }

    /** Return the earliest possible time stamp of the next token to be
     *  consumed by the actor managed by this time keeper. Consider
     *  this returned time value as a greatest lower bound. The next
     *  time is equal to the oldest (smallest valued) receiver time of
     *  all receivers managed by this thread.
     * @return The next earliest possible time stamp to be produced by
     *  this actor.
     */
    public Time getNextTime() {
        if (_receiverList.size() == 0) {
            return _currentTime;
        }

        return ((PrioritizedTimedQueue) _receiverList.getFirst())
                .getReceiverTime();
    }

    /** Return the current value of the output time associated with
     *  this time keeper and, after so doing, set the output time to
     *  a new value that is equivalent to this time keeper's current time.
     * @return double The output time of this time keeper.
     */
    public synchronized Time getOutputTime() {
        if (!((ComponentEntity) _actor).isAtomic()) {
            return _outputTime;
        }

        if (_outputTime.compareTo(_currentTime) < 0) {
            _outputTime = _currentTime;
        }

        return _outputTime;
    }

    /** Update receivers controlled by this time keeper that have
     *  a receiver time equal to PrioritizedTimedQueue.IGNORE. For
     *  each such receiver, call DDEReceiver.removeIgnoredToken().
     */
    public synchronized void removeAllIgnoreTokens() {
        if (_receiverList == null) {
            return;
        }

        if (_ignoredReceivers) {
            PrioritizedTimedQueue receiver;

            for (int i = 0; i < _receiverList.size(); i++) {
                receiver = (PrioritizedTimedQueue) _receiverList.get(i);
            }

            _ignoredReceivers = false;
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
     * @see #getCurrentTime()
     * @see #getModelTime()
     */
    public synchronized void setCurrentTime(Time time) {
        if ((time.compareTo(_currentTime) < 0)
                && (time.getDoubleValue() != PrioritizedTimedQueue.INACTIVE)
                && (time.getDoubleValue() != PrioritizedTimedQueue.IGNORE)) {
            throw new IllegalArgumentException(((NamedObj) _actor).getName()
                    + " - Attempt to " + "set current time in the past."
                    + " time = " + time + "; current time = " + _currentTime);
        }

        if (time.getDoubleValue() != PrioritizedTimedQueue.IGNORE) {
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

        Time time = prioritizedTimedQueue.getReceiverTime();

        if (time.getDoubleValue() == PrioritizedTimedQueue.IGNORE) {
            _ignoredReceivers = true;
        }

        if (!_receiverList.contains(prioritizedTimedQueue)) {
            // Add receiver to list with a touch of
            // optimization before actually sorting.
            if (time.getDoubleValue() > 0) {
                _receiverList.addFirst(prioritizedTimedQueue);
            } else {
                _receiverList.addLast(prioritizedTimedQueue);
            }
        }

        //Collections.sort(_receiverList, _receiverComparator);
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
     PrioritizedTimedQueue testReceiver =
     (PrioritizedTimedQueue)_receiverList.get(i);
     double time = testReceiver.getReceiverTime();
     Token token = null;
     if ( testReceiver._queue.size() > 0 ) {
     token =
     ((PrioritizedTimedQueue.Event)testReceiver._queue.get(0)).getToken();
     }
     String message = "\t"+name+"'s Receiver "+i+
     " has a time of " +time+" and ";
     if ( token instanceof NullToken ) {
     message += "contains a NullToken";
     } else if ( token != null ) {
     message += "contains a RealToken";
     } else {
     message += "contains no token";
     }
     System.out.println(message);
     }
     System.out.println("###End of printReceiverList()\n");
     }
     */
    /** Set the output time associated with this time keeper.
     * @param outputTime The output time of this time keeper.
     * @exception IllegalActionException If the output time is
     *  less than the current time.
     */
    synchronized void _setOutputTime(Time outputTime)
            throws IllegalActionException {
        if (outputTime.compareTo(_currentTime) < 0) {
            throw new IllegalActionException("Illegal attempt "
                    + "to set the time keeper's output time " + "in the past");
        }

        if (outputTime.getDoubleValue() != PrioritizedTimedQueue.IGNORE) {
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
    synchronized void _setReceiverPriorities() throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
        Iterator inputPorts = _actor.inputPortList().iterator();

        if (!inputPorts.hasNext()) {
            return;
        }

        //
        // First Order The Ports
        //
        while (inputPorts.hasNext()) {
            listOfPorts.addLast(inputPorts.next());
        }

        //
        // Now set the priorities of each port's receiver, set
        // the receiving time keeper and initialize the receiverList.
        //
        int cnt = 0;
        int currentPriority = 0;

        while (cnt < listOfPorts.size()) {
            IOPort port = (IOPort) listOfPorts.get(cnt);
            Receiver[][] receivers = port.getReceivers();

            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    ((DDEReceiver4Ptides) receivers[i][j])._priority = currentPriority;

                    //
                    // Is the following necessary??
                    //
                    updateReceiverList((DDEReceiver4Ptides) receivers[i][j]);

                    currentPriority++;
                }
            }

            cnt++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The actor that is managed by this time keeper.
    protected Actor _actor;

    // The currentTime of the actor that is controlled by this
    // time keeper is equivalent to the minimum positive receiverTime
    // of each input receiver.
    private Time _currentTime;

    // The output time associated with this time keeper.
    private Time _outputTime;

    // This flag is set to true if any of the receivers have
    // a time stamp of PrioritizedTimedQueue.IGNORE
    boolean _ignoredReceivers = false;

    // The comparator that sorts the receivers
    // controlled by this time keeper.
    //private ReceiverComparator _receiverComparator;

    // The _receiverList stores the receivers controlled by
    // this time keeper. The receivers are ordered
    // according to the receiver comparator.
    private LinkedList _receiverList;
}
