/* A TimeKeeper manages an actor's local value of time in the DDE domain.

 Copyright (c) 1997-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTelelIAL DAMAGES
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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TimeKeeper
/**
A TimeKeeper manages an actor's local value of time in the DDE domain.
A TimeKeeper is instantiated by an DDEThread and is used by the thread
to manage time for the thread's actor. A TimeKeeper has a list of 
DDEReceivers that are contained by the actor that the thread controls.
As tokens flow through the DDEReceivers, the TimeKeeper keeps track 
of the advancement of time. 
<P>
DDEReceivers each have three important variables: <I>receiver time</I>, 
<I>last time</I> and <I>priority</I>. The receiver time of an DDEReceiver 
is equal to the time of the oldest event that resides on the receiver. 
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
The receiver priorities are set using the method setRcvrPriorities() in 
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
the TimeKeeper's list is updated. This same information allows the 
TimeKeeper to determine what the current time is.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class TimeKeeper {

    /** Construct a time keeper to manage local time of an actor in
     *  the DDE domain. Set the receiver priorities of all receivers
     *  contained by the actor of this time keeper. 
     * @param actor The DDEActor for which time will be managed.
     * @exception IllegalActionException if there is an error
     *  while setting the receiver priorities.
     */
    public TimeKeeper(Actor anActor) throws IllegalActionException {
	_actor = anActor;
        _rcvrTimeList = new LinkedList();

        setRcvrPriorities(); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this TimeKeeper. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers managed by this TimeKeeper.
     * @return The current time of this TimeKeeper.
     */
    public synchronized double getCurrentTime() {
        return _currentTime;
    }

    /** Return the delay time associated with this time keeper.
     *  The delay time value is expected to be updated possibly
     * @return double The delay time of this time keeper.
     *  once per every token consumption of a given actor.
     */
    public synchronized double getDelayTime() { 
	return _delayTime;
    }

    /** Return the active TimedQueueReceiver with the oldest receiver time 
     *  of all receivers contained in the actor that this TimeKeeper 
     *  controls. A TimedQueueReceiver is considered active if its receiver 
     *  time is nonnegative. If all receivers of the managed actor are no 
     *  longer active, then return the first receiver to become inactive. 
     * @return TimedQueueReceiver The oldest active TimedQueueReceiver 
     *  managed by this TimeKeeper.
     */
    public synchronized TimedQueueReceiver getFirstRcvr() {
	if( _rcvrTimeList.size() == 0 ) {
	    return null;
	}
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
	return triple.getReceiver();
    }

    /** Return the TimedQueueReceiver of this time keeper's list such that
     *  the returned receiver has the highest priority given that it has 
     *  the lowest nonnegative receiver time of all receivers managed by 
     *  this TimeKeeper. Return null if this time keeper's list of receivers 
     *  is empty.
     * @return The TimedQueueReceiver with the highest priority 
     *  and lowest nonnegative rcvrTime. If no receivers exist, 
     *  return null.
     */
    public synchronized TimedQueueReceiver getHighestPriorityReceiver() {
        double time = -10.0;
	double firstTime = -10.0;
        int maxPriority = 0;
	int cnt = 0;
	boolean rcvrNotFound = true;
        RcvrTimeTriple highPriorityTriple = null;

	while( rcvrNotFound ) {
	    if( cnt == _rcvrTimeList.size() ) {
	        return null;
	    }

	    RcvrTimeTriple triple = 
		    (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    if( time == -10.0 ) {
	        time = triple.getTime();
	        firstTime = time;
		maxPriority = triple.getPriority();
		highPriorityTriple = triple;
	    } else {
	        time = triple.getTime();
	    }

	    if( time > firstTime || 
		    time == TimedQueueReceiver.INACTIVE ) {
	        rcvrNotFound = false;
	    } else if( maxPriority < triple.getPriority() ) {
		maxPriority = triple.getPriority();
		highPriorityTriple = triple;
	    }
	    cnt++;
	}
	return highPriorityTriple.getReceiver();
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
        if( _rcvrTimeList.size() == 0 ) {
            return _currentTime;
        }
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
        return triple.getTime();
    }

    /** Return true if the minimum receiver time is unique to a single
     *  receiver. Return true if there are no input receivers. Return
     *  false if two or more receivers share the same receiver time and 
     *  this receiver time is less than that of any other receivers 
     *  contained by the same actor.
     * @return True if the minimum receiver time is unique to a single 
     *  receiver or if there are no receivers; otherwise return false.
     */
    public synchronized boolean hasMinRcvrTime() {
        if( _rcvrTimeList.size() < 2 ) {
            return true;
        }

        RcvrTimeTriple firstTriple = (RcvrTimeTriple)_rcvrTimeList.first();
	RcvrTimeTriple secondTriple = (RcvrTimeTriple)_rcvrTimeList.at(1);

	if( firstTriple.getTime() == secondTriple.getTime() ) {
	    return false;
	}
	return true;
    }

    /** Resort the receivers that are controlled by this TimeKeeper. Use
     *  this method in cases where the receiver times may have been 
     *  modified without being reordered with respect to the other 
     *  receivers.
     */
    public synchronized void resortRcvrList() {
	int listSize = _rcvrTimeList.size();
	LinkedList oldRcvrTimeList = _rcvrTimeList;
	_rcvrTimeList = new LinkedList();
	RcvrTimeTriple triple;
	TimedQueueReceiver rcvr;
	for( int i = 0; i < listSize; i++ ) {
	    triple = (RcvrTimeTriple)oldRcvrTimeList.at(i);
	    rcvr = triple.getReceiver();
	    updateRcvrList(rcvr);
	}
    }

    /** Cause the actor managed by this time keeper to send a NullToken
     *  to all output channels that have a receiver time less than or
     *  equal to the current time of this time keeper. Associate a time i
     *  stamp with each NullToken that is equal to the current time of 
     *  this thread.
     */
    public synchronized void sendOutNullTokens() {
	Enumeration ports = _actor.outputPorts();
        double time = getCurrentTime();
        while( ports.hasMoreElements() ) {
            IOPort port = (IOPort)ports.nextElement();
	    Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
            for (int i = 0; i < rcvrs.length; i++) {
                for (int j = 0; j < rcvrs[i].length; j++) {
                    if( time >=
			    ((DDEReceiver)rcvrs[i][j]).getRcvrTime() ) {
                        ((DDEReceiver)rcvrs[i][j]).put(
                                new NullToken(), time );
                    }
		}
            }
        }
    }

    /** Set the current time of this TimeKeeper. If the specified
     *  time is less than the previous value for current time, then
     *  throw an IllegalArgumentException. Do not throw an
     *  IllegalActionException if the current time is set to 
     *  TimedQueueReceiver.INACTIVE to indicate termination.
     * @param time The new value for current time.
     * @exception IllegalArgumentException If there is an attempt to
     *  decrease the value of current time to a nonnegative number.
     */
    synchronized void setCurrentTime(double time) {
	if( time < _currentTime && 
		time != TimedQueueReceiver.INACTIVE ) {
	    throw new IllegalArgumentException(
		    ((NamedObj)_actor).getName() + " - Attempt to "
		    + "set current time in the past.");
	}
        _currentTime = time;
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
    public synchronized void setRcvrPriorities() 
            throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
	Enumeration enum = _actor.inputPorts();
	if( !enum.hasMoreElements() ) {
            return;
	}

        //
        // First Order The Ports 
        //
        while( enum.hasMoreElements() ) {
	    listOfPorts.insertLast( (IOPort)enum.nextElement() ); 
        }

        //
        // Now set the priorities of each port's receiver, set 
	// the receiving time keeper and initialize the rcvrList.
        //
        int cnt = 0;
        int currentPriority = 0;
        while( cnt < listOfPorts.size() ) {
            IOPort port = (IOPort)listOfPorts.at(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((DDEReceiver)rcvrs[i][j]).setPriority(
			    currentPriority);
		    //
		    // Is the following necessary?? 
		    //
		    updateRcvrList( (DDEReceiver)rcvrs[i][j] );

                    currentPriority++;
                }
            }
            cnt++;
        }
    }

    /** Set the delay time associated with this time keeper.
     *  Throw an IllegalActionException if the delay time is
     *  negative. 
     * @param delay The delay time of this time keeper.
     */
    public synchronized void setDelayTime(double delay) 
	     throws IllegalActionException { 
	if( delay < 0.0 ) {
	    throw new IllegalActionException(
		    ((NamedObj)_actor).getName() + " - Attempt to "
		    + "set negative delay time.");
	}
	_delayTime = delay;
    }

    /** Update the list of TimedQueueReceivers. 
     *  RcvrTimeTriples by positioning the
     *  specified triple. If the specified triple is already
     *  contained in the list, then the triple is removed and
     *  then added back to the list. The position of the triple
     *  is based on the triple's time value. 
     * @param tqr The TimedQueueReceiver whose position is being 
     *  updated.
     * @param time The time of the repositioned TimedQueueReceiver.
     * @param priority The priority of the repositioned 
     *  TimedQueueReceiver.
     */
    public synchronized void updateRcvrList(TimedQueueReceiver tqr) {
	double time = tqr.getRcvrTime(); 
	int priority = tqr.getPriority(); 
	RcvrTimeTriple triple = 
	        new RcvrTimeTriple(tqr, time, priority);
	_removeRcvrTriple( triple );
	_addRcvrTriple( triple );
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the RcvrTimeTriple list contained by
     *  this actor. Use this method for testing purposes only.
     * @deprecated
     */
    synchronized void printRcvrList() {
	String name = ((NamedObj)_actor).getName();
        System.out.println("\n***Print "+name+"'s RcvrList.");
        System.out.println("   Number of Receivers in RcvrList = "
                + _rcvrTimeList.size() );
        if( _rcvrTimeList.size() == 0 ) {
            System.out.println("\tList is empty");
            System.out.println("***End of printRcvrList()\n");
	    return;
        }
        for( int i = 0; i < _rcvrTimeList.size(); i++ ) {
	    RcvrTimeTriple testTriple = (RcvrTimeTriple)_rcvrTimeList.at(i);
	    TimedQueueReceiver testRcvr = testTriple.getReceiver();
            double time = testTriple.getTime();
            String testPort = testRcvr.getContainer().getName();
            String testString = "null";
            System.out.println("\t"+name+"'s Receiver "+i+
	            " has a time of " +time+" and string: "+testString);
        }
        System.out.println("***End of printRcvrList()\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         methods			   ////

    /** Add the specified RcvrTimeTriple to the list of triples.
     *  If the time stamp of the specified triple is 
     *  TimedQueueReceiver.INACTIVE, then insert the triple into 
     *  the last position of the RcvrTimeTriple list. Otherwise, 
     *  insert the triple immediately after all other triples with 
     *  time stamps less than or equal to the time stamp of the 
     *  specified triple. ALWAYS call _removeRcvrTriple immediately 
     *  before calling this method if the RcvrTimeTriple list already 
     *  contains the triple specified in the argument.
     */
    private void _addRcvrTriple(RcvrTimeTriple newTriple) {
        if( _rcvrTimeList.size() == 0 ) {
            _rcvrTimeList.insertAt( 0, newTriple );
            return;
        }

	if( newTriple.getTime() == TimedQueueReceiver.INACTIVE ) {
	    _rcvrTimeList.insertLast(newTriple);
	    return;
	}

	int cnt = 0;
        boolean notAddedYet = true;
	while( cnt < _rcvrTimeList.size() ) {
	    RcvrTimeTriple triple = 
		    (RcvrTimeTriple)_rcvrTimeList.at(cnt);

	    if( triple.getTime() == TimedQueueReceiver.INACTIVE ) {
	        _rcvrTimeList.insertAt( cnt, newTriple );
		cnt = _rcvrTimeList.size();
                notAddedYet = false;
	    } else if( newTriple.getTime() < triple.getTime() ) {
	        _rcvrTimeList.insertAt( cnt, newTriple );
		cnt = _rcvrTimeList.size();
                notAddedYet = false;
	    }
	    cnt++;
	}

        if( notAddedYet ) {
            _rcvrTimeList.insertLast( newTriple );
        }
    }

    /** Remove the specified RcvrTimeTriple from the list of triples.
     */
    private void _removeRcvrTriple(RcvrTimeTriple triple) {

        TimedQueueReceiver rcvrToBeRemoved = triple.getReceiver();

	for( int cnt = 0; cnt < _rcvrTimeList.size(); cnt++ ) {
	    RcvrTimeTriple nextTriple = 
		    (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    TimedQueueReceiver nextRcvr = nextTriple.getReceiver();

	    if( rcvrToBeRemoved == nextRcvr ) {
	        _rcvrTimeList.removeAt( cnt );
		cnt = _rcvrTimeList.size();
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The actor that is managed by this time keeper.
    private Actor _actor;

    // The _rcvrTimeList stores RcvrTimeTriples and is used to
    // order the receivers according to time and priority.
    private LinkedList _rcvrTimeList;

    // The currentTime of the actor that is controlled by this
    // thread is equivalent to the minimum positive rcvrTime of
    // each input receiver.
    private double _currentTime = 0.0;

    // The delay time associated with this actor. 
    private double _delayTime = 0.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // A RcvrTimeTriple is a data structure for storing a receiver 
    // along with its rcvrTime and priority. RcvrTimeTriples are 
    // used by DDEActors to order incoming events according to time 
    // stamps. Each DDEActor has a RcvrTimeTriple associated with 
    // each receiver it owns. In situations where multiple receivers 
    // of an DDEActor have simultaneous events, the priority of the 
    // RcvrTimeTriples are used to determine order.

    public class RcvrTimeTriple extends NamedObj {

        // Construct a RcvrTimeTriple with a TimeQueueReceiver, 
	// a rcvr time and a priority. The rcvr time must be 
	// greater than or equal to any previous rcvr times 
	// associated with the TimedQueueReceiver. 
        public RcvrTimeTriple(TimedQueueReceiver rcvr, 
		double rcvrTime, int priority ) {
            super();
            _rcvr = rcvr; 
	    _priority = priority; 
	    _rcvrTime = rcvrTime;
	    if( !(rcvr instanceof TimedQueueReceiver) ) {
	        throw new IllegalArgumentException(
			rcvr.getContainer().getName() + 
			" is not a TimedQueueReceiver.");
	    } 
	    if( rcvrTime < _rcvrTime && 
		    rcvrTime != TimedQueueReceiver.INACTIVE ) {
	        throw new IllegalArgumentException(
			"Rcvr times must be monotonically " 
			+ "non-decreasing.");
	    }
	}

        ///////////////////////////////////////////////////////////
	////                     public methods                ////

        // Return the TimedQueueReceiver of this RcvrTimeTriple.
        public TimedQueueReceiver getReceiver() {
            return _rcvr;
        }

        // Return the priority of this RcvrTimeTriple.
        public int getPriority() {
            return _priority;
        }

        // Return the time of this RcvrTimeTriple.
        public double getTime() {
            return _rcvrTime;
        }

        ///////////////////////////////////////////////////////////
        ////                     private variables             ////

        private TimedQueueReceiver _rcvr; 
	private double _rcvrTime = 0.0; 
	private int _priority;
    }

}








