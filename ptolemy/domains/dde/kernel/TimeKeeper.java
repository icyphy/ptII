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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;

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
        _rcvrList = new LinkedList();
	_rcvrComparator = new RcvrComparator(this);

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
	if( _rcvrList.size() == 0 ) {
	    return null;
	}
        return (TimedQueueReceiver)_rcvrList.getFirst();
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
        if( _rcvrList.size() == 0 ) {
            return _currentTime;
        }
        return ((TimedQueueReceiver)_rcvrList.getFirst()).getRcvrTime();
    }

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

    /** Send a NullToken to all output channels that have a receiver 
     *  time less than or equal to the current time of this time keeper. 
     *  In this case, set the time stamp of the NullTokens to be equal 
     *  to the current time of this time keeper. This method assumes
     *  that the actor controlled by this time keeper is atomic.
     *  <P>
     *  This method is not synchronized so the calling method should be. 
     * @params rcvr The receiver that is causing this method to be invoked.
     */
    public void sendOutNullTokens(DDEReceiver rcvr) {
	String name = ((Nameable)_actor).getName();
	/*
	if( name.equals("fBack") ) {
	    try {
		throw new IllegalArgumentException();
	    } catch( IllegalArgumentException e ) {
		e.printStackTrace();
	    }
	}
	*/



	Iterator ports = _actor.outputPortList().iterator(); 
	double time = getCurrentTime(); 
	while( ports.hasNext() ) {
	    IOPort port = (IOPort)ports.next(); 
	    Receiver rcvrs[][] = 
		(Receiver[][])port.getRemoteReceivers();
	    for (int i = 0; i < rcvrs.length; i++) {
		for (int j = 0; j < rcvrs[i].length; j++) {
		    if( time > ((DDEReceiver)rcvrs[i][j])._lastTime ) {
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
    public synchronized void setCurrentTime(double time) {
	if( time < _currentTime
		&& time != TimedQueueReceiver.INACTIVE
		&& time != TimedQueueReceiver.IGNORE ) {
	    throw new IllegalArgumentException(
		    ((NamedObj)_actor).getName() + " - Attempt to "
		    + "set current time in the past." 
		    + " time = " + time
		    + "; current time = " + _currentTime );
	}
            
	if( time != TimedQueueReceiver.IGNORE ) {
            _currentTime = time;
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
		/*
	String name = ((Nameable)_actor).getName();
	System.out.println("##### TimeKeeper Set Priorities: "+name+" #####");
		*/



        LinkedList listOfPorts = new LinkedList();
	Iterator inputPorts = _actor.inputPortList().iterator();
	if( !inputPorts.hasNext() ) {
            return;
	}

        //
        // First Order The Ports
        //
        while( inputPorts.hasNext() ) {
	    listOfPorts.addLast( (IOPort)inputPorts.next() );
        }

        //
        // Now set the priorities of each port's receiver, set
	// the receiving time keeper and initialize the rcvrList.
        //
        int cnt = 0;
        int currentPriority = 0;
        while( cnt < listOfPorts.size() ) {
            IOPort port = (IOPort)listOfPorts.get(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((DDEReceiver)rcvrs[i][j])._priority = 
			    currentPriority;
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

    /** Update receivers controlled by this time keeper that have
     *  a receiver time equal to TimedQueueReceiver.IGNORE. For
     *  each such receiver, call DDEReceiver.removeIgnoredToken().
     */
    public synchronized void removeAllIgnoreTokens() {
	String name = ((Nameable) _actor).getName();
	/*
	if( name.equals("join") ) {
	    System.out.println("*****removeAllIgnoreTokens() called by "+name+" at time "+_currentTime);
	}
	*/



	if( _rcvrList == null ) {
	    return;
	}
	if( _ignoredRcvrs ) {
	    TimedQueueReceiver rcvr;
	    for( int i = 0; i < _rcvrList.size(); i++ ) {
	        rcvr = (TimedQueueReceiver)_rcvrList.get(i);
		if( rcvr.getRcvrTime() ==
			TimedQueueReceiver.IGNORE ) {
		    rcvr.removeIgnoredToken();
		}
	    }
	    _ignoredRcvrs = false;
	}
    }

    /** Update the list of receivers by adding and sorting a new 
     *  receiver or resorting a receiver that already exists. The
     *  receiver is sorted according to a RcvrComparator.
     * @param tqr The TimedQueueReceiver whose position is being
     *  updated.
     * @see ptolemy.domains.dde.kernel.RcvrComparator
     */
    public synchronized void updateRcvrList(TimedQueueReceiver tqr) {
	if( _rcvrList == null ) {
	    _rcvrList = new LinkedList();
	}

	/*
	double time = tqr.getRcvrTime();
	if( time == TimedQueueReceiver.IGNORE ) {
	    System.out.println("#### SET IGNORE #####");
	    _ignoredRcvrs = true;
	}

	if( !_rcvrList.contains( tqr ) ) {
	    // Add receiver to list
	    if( time > 0 ) {
		_rcvrList.addFirst( tqr );
	    } else {
		_rcvrList.addLast( tqr );
	    }
	}

	Collections.sort( _rcvrList, _rcvrComparator );
	*/




	double time = tqr.getRcvrTime();
	if( time == TimedQueueReceiver.IGNORE ) {
	    // System.out.println("#### SET IGNORE #####");
	    _ignoredRcvrs = true;
	}

	// If receiver is already on the list then sort and return
	if( _rcvrList.contains( tqr ) ) {
	    Collections.sort( _rcvrList, _rcvrComparator );
	    return;
	}

	// Add receiver to list and then sort
	if( time > 0.0 ) {
	    _rcvrList.addFirst( tqr );
	} else {
	    _rcvrList.addLast( tqr );
	}
	
	Collections.sort( _rcvrList, _rcvrComparator );
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly variables              ////

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the receiver list contained by
     *  this actor. 
     * @deprecated Use for testing purposes only.
    synchronized void printRcvrList() {
	String name = ((NamedObj)_actor).getName();
        System.out.println("\n###Print "+name+"'s RcvrList.");
        System.out.println("   Number of Receivers in RcvrList = "
                + _rcvrList.size() );
        if( _rcvrList.size() == 0 ) {
            System.out.println("\tList is empty");
            System.out.println("###End of printRcvrList()\n");
	    return;
        }
        for( int i = 0; i < _rcvrList.size(); i++ ) {
	    TimedQueueReceiver testRcvr = (TimedQueueReceiver)_rcvrList.get(i);
            double time = testRcvr.getRcvrTime();
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor that is managed by this time keeper.
    private Actor _actor;

    // The currentTime of the actor that is controlled by this
    // time keeper is equivalent to the minimum positive rcvrTime 
    // of each input receiver.
    private double _currentTime = 0.0;

    // The output time associated with this time keeper.
    private double _outputTime = 0.0;

    // This flag is set to true if any of the receivers have 
    // a time stamp of TimedQueueReceiver.IGNORE
    boolean _ignoredRcvrs = false;

    // The comparator that sorts the receivers 
    // controlled by this time keeper.
    private RcvrComparator _rcvrComparator;

    // The _rcvrList stores the receivers controlled by
    // this time keeper. The receivers are ordered 
    // according to the rcvr comparator.
    private LinkedList _rcvrList;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

}
