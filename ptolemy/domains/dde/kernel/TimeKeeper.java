/* A TimeKeeper manages an actor's local value of time in the DDE domain.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
A TimeKeeper is instantiated by a DDEThread and is used by the thread
to manage time for the thread's actor. A TimeKeeper has a list of
DDEReceivers that are contained by the actor that the thread controls.
As tokens flow through the DDEReceivers, the TimeKeeper keeps track
of the advancement of time.
<P>
DDEReceivers each have three important variables: <I>receiver time</I>,
<I>last time</I> and <I>priority</I>. The receiver time of a DDEReceiver
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

    /** Construct a time keeper to manage the local time of an actor
     *  in the DDE domain. Set the receiver priorities of all receivers
     *  contained by the actor of this time keeper.
     * @param actor The DDEActor for which time will be managed.
     * @exception IllegalActionException if there is an error
     *  while setting the receiver priorities.
     */
    public TimeKeeper(Actor anActor) throws IllegalActionException {
	_actor = anActor;
        _rcvrTimeList = new LinkedList();

        String name = ((Nameable)_actor).getName();
        setRcvrPriorities();
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
    public synchronized TimedQueueReceiver getHighestPriorityNull() {
        double time = -10.0;
	double firstTime = -10.0;
        int maxPriority = -1;
	int cnt = 0;
	boolean rcvrNotFound = true;
        RcvrTimeTriple highPriorityTriple = null;

	if( _rcvrTimeList.size() == 0 ) {
	    return null;
	}
	while( cnt < _rcvrTimeList.size() ) {
	    RcvrTimeTriple triple =
                (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    if( time == -10.0 ) {
	        time = triple.getTime();
	        firstTime = time;
		TimedQueueReceiver tmpRcvr =
                    (TimedQueueReceiver)triple.getReceiver();
		if( tmpRcvr.hasNullToken() ) {
		    maxPriority = triple.getPriority();
		    highPriorityTriple = triple;
		}
	    } else {
	        time = triple.getTime();
	    }

	    if( time > firstTime ||
		    time == TimedQueueReceiver.INACTIVE ||
                    time == TimedQueueReceiver.IGNORE ) {
		if( highPriorityTriple != null ) {
		    return highPriorityTriple.getReceiver();
		} else {
		    return null;
		}
	    } else if( maxPriority < triple.getPriority() ) {
		TimedQueueReceiver tmpRcvr =
                    (TimedQueueReceiver)triple.getReceiver();
		if( tmpRcvr.hasNullToken() ) {
		    maxPriority = triple.getPriority();
		    highPriorityTriple = triple;
		}
	    }
	    cnt++;
	}
	if( highPriorityTriple != null ) {
	    return highPriorityTriple.getReceiver();
	} else {
	    return null;
	}
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
    public synchronized TimedQueueReceiver getHighestPriorityReal() {
        double time = -10.0;
	double firstTime = -10.0;
        int maxPriority = -1;
	int cnt = 0;
	boolean rcvrNotFound = true;
        RcvrTimeTriple highPriorityTriple = null;

	if( _rcvrTimeList.size() == 0 ) {
	    return null;
	}
	while( cnt < _rcvrTimeList.size() ) {
	    RcvrTimeTriple triple =
                (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    if( time == -10.0 ) {
	        time = triple.getTime();
	        firstTime = time;
		TimedQueueReceiver tmpRcvr =
                    (TimedQueueReceiver)triple.getReceiver();
		if( !tmpRcvr.hasNullToken() ) {
		    if( tmpRcvr.getRcvrTime() != TimedQueueReceiver.IGNORE ) {
			maxPriority = triple.getPriority();
			highPriorityTriple = triple;
		    }
		}
	    } else {
	        time = triple.getTime();
	    }

	    if( time > firstTime ||
		    time == TimedQueueReceiver.INACTIVE ||
                    time == TimedQueueReceiver.IGNORE ) {
		if( highPriorityTriple != null ) {
		    return highPriorityTriple.getReceiver();
		} else {
		    return null;
		}
	    } else if( maxPriority < triple.getPriority() ) {
		TimedQueueReceiver tmpRcvr =
                    (TimedQueueReceiver)triple.getReceiver();
		if( !tmpRcvr.hasNullToken() ) {
		    if( tmpRcvr.getRcvrTime() != TimedQueueReceiver.IGNORE ) {
			maxPriority = triple.getPriority();
			highPriorityTriple = triple;
		    }
		}
	    }
	    cnt++;
	}
	if( highPriorityTriple != null ) {
	    return highPriorityTriple.getReceiver();
	} else {
	    return null;
	}
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

	if( _rcvrTimeList.size() == 0 ) {
	    return null;
	}
	while( cnt < _rcvrTimeList.size() ) {
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
		    time == TimedQueueReceiver.INACTIVE ||
                    time == TimedQueueReceiver.IGNORE ) {
		return highPriorityTriple.getReceiver();
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

    /** Return the number of active channels associated with
     *  this time keeper. 
    public int getNumberOfActiveChannels() {
    	_rcvrTimeList.size();
    }
     */
    
    /** Return the current value of the output time associated with
     *  this time keeper and, after so doing, set the output time to
     *  a new value that is equivalent to this time keeper's current time.
     * @return double The output time of this time keeper.
     */
    public synchronized double getOutputTime() {
	Thread thread = Thread.currentThread();
        
        if( !((ComponentEntity)_actor).isAtomic() ) {
            return _outputTime;
        }
        
        
        if( _outputTime < _currentTime ) {
            _outputTime = _currentTime;
        }
        return _outputTime;
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

        RcvrTimeTriple firstTriple =
            (RcvrTimeTriple)_rcvrTimeList.first();
	RcvrTimeTriple secondTriple =
            (RcvrTimeTriple)_rcvrTimeList.at(1);

	if( firstTriple.getTime() == secondTriple.getTime() ) {
	    return false;
	}
	return true;
    }

    /** Resort the receivers that are controlled by this TimeKeeper.
     *  Use this method in cases where the receiver times may have
     *  been modified without being reordered with respect to the
     *  other receivers.
     */
    public synchronized void resortRcvrList() {
	String calleeName = ((Nameable)_actor).getName();
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

    /** If the actor managed by this time keeper is atomic, then send a 
     *  NullToken to all output channels that have a receiver time less 
     *  than or equal to the current time of this time keeper. In this
     *  case, set the time stamp of the NullTokens to be equal to the
     *  current time of this TimeKeeper.
     *  <P>
     *  If the receiver argument of this method is contained on the 
     *  inside of a boundary port and the outer model of computation is 
     *  DDE, then send a NullToken to each receiver linked to the receiver 
     *  argument. In this case, set the time stamp of the NullTokens to be
     *  equal to the output time of this TimeKeeper.
     *  <P>
     *  If the receiver argument of this method is contained on the
     *  outside of a boundary port and the inner model of computation is
     *  DDE, then send a NullToken to each receiver linked to the receiver
     *  argument. In this case, set the time stamp of the NullTokens to be
     *  equal to the current time of this TimeKeeper.
     *  <P>
     *  This method is not synchronized so the calling method should be. 
     * @params rcvr The receiver that is causing this method to be invoked.
     */
    public void sendOutNullTokens(DDEReceiver rcvr) {
        String actorName = ((Nameable)_actor).getName();
        /*
        System.out.println("Actor: " + actorName + " called "
        	+ "sendOutNullTokens.");
        */
        if( rcvr.isInsideBoundary() ) {
            if( _actor.getExecutiveDirector() instanceof DDEDirector ){
                IOPort port = (IOPort)rcvr.getContainer();
                Receiver[][] rcvrs = null;
                rcvrs = port.getRemoteReceivers();
                double time = getOutputTime();
                for (int i = 0; i < rcvrs.length; i++) {
            	    for (int j = 0; j < rcvrs[i].length; j++ ) {
                        if( time > ((DDEReceiver)
                                rcvrs[i][j]).getLastTime() ) {
                            ((DDEReceiver)rcvrs[i][j]).put(
                                    new NullToken(), time );
                        }
                    }
                }
            }
            return;
        }
        
        else if( rcvr.isOutsideBoundary() ) {
            if( _actor.getDirector() instanceof DDEDirector ){
                IOPort port = (IOPort)rcvr.getContainer();
                Receiver[][] rcvrs = null;
                try {
                    rcvrs = port.deepGetReceivers();
                } catch( IllegalActionException e ) {
                    // FIXME: Do Something
                }
                double time = getCurrentTime();
                for (int i = 0; i < rcvrs.length; i++) {
            	    for (int j = 0; j < rcvrs[i].length; j++ ) {
                        if( time > ((DDEReceiver)
                                rcvrs[i][j]).getLastTime() ) {
                            ((DDEReceiver)rcvrs[i][j]).put(
                                    new NullToken(), time );
                        }
                    }
                }
            }
            return;
        }
        
        else {
	    Enumeration ports = _actor.outputPorts(); 
            double time = getCurrentTime(); 
            while( ports.hasMoreElements() ) {
            	IOPort port = (IOPort)ports.nextElement(); 
                Receiver rcvrs[][] = 
                        (Receiver[][])port.getRemoteReceivers();
            	for (int i = 0; i < rcvrs.length; i++) {
                    for (int j = 0; j < rcvrs[i].length; j++) {
                        if( time >
			        ((DDEReceiver)rcvrs[i][j]).getLastTime() ) {
                            ((DDEReceiver)rcvrs[i][j]).put(
                                    new NullToken(), time );
                        }
		    }
		}
            }
            /*
            System.out.println("Actor: " + actorName + " called "
        	    + "sendOutNullTokens.");
            */
            return;
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
    public synchronized void setCurrentTime(double time) {
	if( time < _currentTime
		&& time != TimedQueueReceiver.INACTIVE
		&& time != TimedQueueReceiver.IGNORE ) {
	    throw new IllegalArgumentException(
		    ((NamedObj)_actor).getName() + " - Attempt to "
		    + "set current time in the past.");
	}
            
	String name = ((Nameable)_actor).getName();
	if( name.equals("fBack") ) {
	    /*
	    if( time == 9.0 ) {
		try {
		    throw new IllegalActionException("fBack.setCurrentTime() at time = 9.0");
		} catch(IllegalActionException e) {
		    e.printStackTrace();
		}
	    }
	    */
	}

	if( time != TimedQueueReceiver.IGNORE ) {
            _currentTime = time;
	} else {
	    return;
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
	String name = ((Nameable)_actor).getName();
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

    /** Set the output time associated with this time keeper.
     *  Throw an IllegalActionException if the output time is
     *  less than the current time.
     * @param outputTime The output time of this time keeper.
     */
    synchronized void setOutputTime(double outputTime)
            throws IllegalActionException {
        if( _outputTime > _currentTime ) {
            if( _outputTime > outputTime ) {
                return;
            }
        }
	if( outputTime != TimedQueueReceiver.IGNORE ) {
            _outputTime = outputTime;
	}
    }

    /** Return true if a search for receivers with a receiver
     *  time of TimedQueueReceiver.IGNORE is taking place.
     *  Return false otherwise.
     * @return True if a search for ignored receivers is taking
     *  place; otherwise return false.
    public boolean searchingForIgnoredTokens() {
	return _searchingForIgnoredTokens;
    }
     */

    /** Specify according to the parameter whether or not this
     *  TimeKeeper currently has receivers that contain IGNORE
     *  tokens at the head of their queues.
     * @param ignore Set to true if IGNORE tokens exist at the
     *  head of a receiver queue; set to false otherwise.
     */
    void setIgnoredTokens(boolean ignore) {
	_ignoredReceivers = ignore;
    }

    /** Update receivers controlled by this time keeper that have
     *  a receiver time equal to TimedQueueReceiver.IGNORE. For
     *  each such receiver, call DDEReceiver.clearIgnoredTokens().
     */
    public synchronized void updateIgnoredReceivers() {
	if( _rcvrTimeList == null ) {
	    return;
	}
	if( _ignoredReceivers ) {
	    RcvrTimeTriple triple;
	    DDEReceiver rcvr;
	    for( int i = 0; i < _rcvrTimeList.size(); i++ ) {
	        triple = (RcvrTimeTriple)_rcvrTimeList.at(i);
		rcvr = (DDEReceiver)triple.getReceiver();
		if( rcvr.getRcvrTime() ==
			TimedQueueReceiver.IGNORE ) {
		    rcvr.removeIgnoredToken();
                    rcvr._ignoreNotSeen = true;
		}
	    }
	    _ignoredReceivers = false;
	}
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
	String calleeName = ((Nameable)_actor).getName();
	double time = tqr.getRcvrTime();

	/*
	if( time == 15.0 && calleeName.equals("wormhole") ) {
	    try {
		throw new IllegalActionException(calleeName+": updateRcvrList() at time = "+time);
	    } catch(IllegalActionException e) {
		e.printStackTrace();
	    }
	}
	*/

	int priority = tqr.getPriority();
	RcvrTimeTriple triple =
            new RcvrTimeTriple(tqr, time, priority);
	_removeRcvrTriple( triple );
	_addRcvrTriple( triple );
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly variables              ////

    // A flag indicating whether a token has been consumed. This
    // flag is only used by composite actors.
    boolean _tokenConsumed = false;

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the RcvrTimeTriple list contained by
     *  this actor. Use this method for testing purposes only.
     * @deprecated
    synchronized void printRcvrList() {
	String name = ((NamedObj)_actor).getName();
        System.out.println("\n###Print "+name+"'s RcvrList.");
        System.out.println("   Number of Receivers in RcvrList = "
                + _rcvrTimeList.size() );
        if( _rcvrTimeList.size() == 0 ) {
            System.out.println("\tList is empty");
            System.out.println("###End of printRcvrList()\n");
	    return;
        }
        for( int i = 0; i < _rcvrTimeList.size(); i++ ) {
	    RcvrTimeTriple testTriple = (RcvrTimeTriple)_rcvrTimeList.at(i);
	    TimedQueueReceiver testRcvr = testTriple.getReceiver();
            double time = testTriple.getTime();
	    Token token = null;
	    if( testRcvr._queue.size() > 0 ) {
		token = ((TimedQueueReceiver.Event)testRcvr._queue.get(0)).getToken();
	    }
	    String msg = "\t"+name+"'s Receiver "+i+
                " has a time of " +time+" and ";
	    if( token instanceof NullToken ) {
		msg += "contains a NullToken";
	    } else if( token != null ) {
		msg += "contains a RealToken";
	    } else {
		msg += "contains no token";
	    }
	    System.out.println(msg);
        }
        System.out.println("###End of printRcvrList()\n");
    }
     */

    /** Set a flag indicating whether a search for ignored
     *  tokens is taking place as per the specified parameter.
     * @param search The search flag.
    synchronized void setSearchForIgnoredTokens(boolean search) {
	_searchingForIgnoredTokens = search;
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods		   ////

    /** Add the specified RcvrTimeTriple to the list of triples.
     *  If the time stamp of the specified triple is
     *  TimedQueueReceiver.INACTIVE, then insert the triple into
     *  the last position of the RcvrTimeTriple list. If the time
     *  stamp of the specified triple is TimedQueueReceiver.IGNORE
     *  then insert the triple before the first INACTIVE receiver.
     *  In all other cases insert the triple immediately after all
     *  other triples with time stamps less than or equal to the
     *  time stamp of the specified triple. ALWAYS call
     *  _removeRcvrTriple immediately before calling this method if
     *  the RcvrTimeTriple list already contains the triple specified
     *  in the argument.
     */
    private void _addRcvrTriple(RcvrTimeTriple newTriple) {
	String calleeName = ((Nameable)_actor).getName();
        if( _rcvrTimeList.size() == 0 ) {
            _rcvrTimeList.insertAt( 0, newTriple );
            return;
        }

        boolean notAddedYet = true;
	// Add INACTIVE receivers
	if( newTriple.getTime() == TimedQueueReceiver.INACTIVE ) {
	    _rcvrTimeList.insertLast(newTriple);
	    return;
	}

	// Add IGNORE receivers
	else if( newTriple.getTime() == TimedQueueReceiver.IGNORE ) {
	    int cnt = 0;
	    while( cnt < _rcvrTimeList.size() && notAddedYet ) {
		RcvrTimeTriple triple =
                    (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	        if( triple.getTime() == TimedQueueReceiver.INACTIVE ) {
		    _rcvrTimeList.insertAt( cnt, newTriple );
		    cnt = _rcvrTimeList.size();
		    notAddedYet = false;
		} else if( triple.getTime() == TimedQueueReceiver.IGNORE ) {
		    _rcvrTimeList.insertAt( cnt, newTriple );
		    cnt = _rcvrTimeList.size();
		    notAddedYet = false;
	        }
	        cnt++;
	    }
	    _ignoredReceivers = true;
	}

	// Add regular receivers
	else {
	    int cnt = 0;
	    while( cnt < _rcvrTimeList.size() && notAddedYet ) {
		RcvrTimeTriple triple =
                    (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	        if( triple.getTime() == TimedQueueReceiver.INACTIVE ) {
		    _rcvrTimeList.insertAt( cnt, newTriple );
		    cnt = _rcvrTimeList.size();
		    notAddedYet = false;
		} else if( triple.getTime() == TimedQueueReceiver.IGNORE ) {
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
	}

        if( notAddedYet ) {
            _rcvrTimeList.insertLast( newTriple );
        }
    }

    /** Remove the specified RcvrTimeTriple from the list of triples.
     */
    private void _removeRcvrTriple(RcvrTimeTriple triple) {
	String calleeName = ((Nameable)_actor).getName();

        TimedQueueReceiver rcvrToBeRemoved = triple.getReceiver();

	for( int cnt = 0; cnt < _rcvrTimeList.size(); cnt++ ) {
	    RcvrTimeTriple nextTriple =
                (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    TimedQueueReceiver nextRcvr = nextTriple.getReceiver();

	    if( rcvrToBeRemoved == nextRcvr ) {
	        _rcvrTimeList.removeAt( cnt );
		return;
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor that is managed by this time keeper.
    private Actor _actor;

    // The _rcvrTimeList stores RcvrTimeTriples and is used to
    // order the receivers according to time and priority.
    private LinkedList _rcvrTimeList;

    // The currentTime of the actor that is controlled by this
    // thread is equivalent to the minimum positive rcvrTime of
    // each input receiver.
    private double _currentTime = 0.0;

    // The output time associated with this actor.
    private double _outputTime = 0.0;

    // Flag set to true if any of the receivers have time of
    // TimedQueueReceiver.IGNORE
    private boolean _ignoredReceivers = false;

    // A flag to prevent infinite cycles while searching for
    // receivers with receiver time = TimedQueueReceiver.IGNORE
    // private boolean _searchingForIgnoredTokens = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // A RcvrTimeTriple is a data structure for storing a receiver
    // along with its rcvrTime and priority. RcvrTimeTriples are
    // used by DDEActors to order incoming events according to time
    // stamps. Each DDEActor has a RcvrTimeTriple associated with
    // each receiver it owns. In situations where multiple receivers
    // of a DDEActor have simultaneous events, the priority of the
    // RcvrTimeTriples are used to determine order.

    public class RcvrTimeTriple {

        // Construct a RcvrTimeTriple with a TimeQueueReceiver,
	// a rcvr time and a priority. The rcvr time must be
	// greater than or equal to any previous rcvr times
	// associated with the TimedQueueReceiver.
        public RcvrTimeTriple(TimedQueueReceiver rcvr,
		double rcvrTime, int priority ) {
            _rcvr = rcvr;
	    _priority = priority;
	    _rcvrTime = rcvrTime;
	    if( !(rcvr instanceof TimedQueueReceiver) ) {
	        throw new IllegalArgumentException(
			rcvr.getContainer().getName() +
			" is not a TimedQueueReceiver.");
	    }
	    if( rcvrTime < _rcvrTime &&
		    rcvrTime != TimedQueueReceiver.INACTIVE &&
		    rcvrTime != TimedQueueReceiver.IGNORE ) {
	        throw new IllegalArgumentException(
			"Rcvr times must be monotonically "
			+ "non-decreasing.");
	    }
	}

        ///////////////////////////////////////////////////////////
	////                     public inner methods          ////

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
        ////                     private inner variables       ////

        private TimedQueueReceiver _rcvr;
	private double _rcvrTime = 0.0;
	private int _priority;
    }

}
