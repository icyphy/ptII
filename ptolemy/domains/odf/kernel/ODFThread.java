/* A thread that controls an actors according to ODF semantics.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODFThread
/**
A thread that controls an actors according to ODF semantics. The
primary purpose of an ODFThread is to control the execution of an
actor and to maintain the actor's local notion of time. To facilitate
this purpose, an ODFThread has a list of ODFReceivers that are
contained by the actor that the ODFThread controls.
<P>
ODFReceivers each have three important variables: rcvrTime, lastTime
and priority. The rcvrTime of an ODFReceiver is equal to the time of
the oldest event that resides on the receiver. The lastTime is equal
to the time of the newest event residing on the receiver.
<P>
An ODFThread manages the ODFReceivers of its actor by keeping track of
the receiver with the minimum rcvrTime. The actor is allowed to consume
a token from a receiver if that receiver has the unique, minimum
rcvrTime of all receivers managed by the ODFThread. The ODFThread
keeps track of its receiver's priorities as well. The receiver with the
highest priority is enabled for having its token consumed if the receiver
shares a common minimum rcvrTime with one or more additional receivers.
<P>
The receiver priorities are set using the method setRcvrPriorities() in the
following manner. All of the input receivers for a given ODFThread are
grouped by their respective container input ports. The port groups are
ordered according to the inverse order in which their corresponding ports
were connected in the model topology. I.e., if two input ports (pA and pB)
of an actor are connected such that port pA is connected before port pB,
then all of the receivers of port pB will have a higher priority than the
receivers of port pA.
<P>
Within a group the receiver priorities are further refined so that receivers
of the same group can be ordered relative to one another. Receiver priorities
within a group are ordered according to the inverse order in which they were
connected in the model topology. I.e., if two input receivers (rA and rB)
of an actor are connected such that receiver rA is connected before receiver
rB, then rB will have a higher priority than rA.
<P>
The above approach provides each receiver contained by a given ODFThread with
a unique priority, such that the set of receiver priorities for the
containing ODFThread is totally ordered.
<P>
RcvrTimeTriple objects are used to facilitate the ordering of receivers
contained by an ODFThread according to rcvrTime/lastTime and priority. A
RcvrTimeTriple is an object containing an ODFReceiver, the _rcvrTime of
the receiver and the priority of the receiver. Each ODFThread contains a
list consisting of one RcvrTimeTriple per receiver contained by the actor.
As tokens are placed in and taken out of the receivers of an actor, the
list of RcvrTimeTriples is updated.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.RcvrTimeTriple
*/
public class ODFThread extends ProcessThread {

    /** Construct a thread to be used to execute the iteration 
     *  methods of an ODFActor. This increases the count of 
     *  active actors in the director.
     * @param actor The ODFActor that needs to be executed.
     * @param director The director responsible for the execution 
     *  of this actor.
     */
    public ODFThread(Actor actor, ProcessDirector director) 
            throws IllegalActionException {
        super(actor, director);
        // _director = director;
        _manager = ((CompositeActor)
                ((NamedObj)actor).getContainer()).getManager();
        // _rcvrTimeList = new LinkedList();
	_timeKeeper = new TimeKeeper(actor);
	/*
        _timeKeeper.setRcvrPriorities(); 
	_timeKeeper.initializeRcvrList();
	*/
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this ODFThread. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers managed by this ODFThread.
     * @return The current time of this ODFThread.
    public double getCurrentTime() {
        return _currentTime;
    }
     */

    /** Return the time keeper that keeps time for the actor that this
     *  thread controls.
     * @return The TimeKeeper of the actor that this thread controls.
     */
    public TimeKeeper getTimeKeeper() {
	return _timeKeeper;
    }

    /** Return the RcvrTimeTriple consisting of the receiver with the
     *  highest priority given that it has the lowest nonnegative rcvrTime
     *  of all receivers managed by this ODFThread. Return null if this
     *  thread's list of RcvrTimeTriples is empty.
     * @return The RcvrTimeTriple consisting of the receiver with the
     *  highest priority and lowest nonnegative rcvrTime. If no triples
     *  exist, return null.
    public synchronized  RcvrTimeTriple getHighestPriorityTriple() {
        double time = -10.0;
	double firstTime = -10.0;
        int maxPriority = 0;
	int cnt = 0;
	boolean rcvrNotFound = true;
        RcvrTimeTriple highPriorityTriple = null;

	while( rcvrNotFound ) {
	    if( cnt == _rcvrTimeList.size() ) {
	        return highPriorityTriple;
	    }

	    RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    if( time == -10.0 ) {
	        time = triple.getTime();
	        firstTime = time;
		maxPriority = triple.getPriority();
		highPriorityTriple = triple;
	    } else {
	        time = triple.getTime();
	    }

	    if( time > firstTime || time == -1.0 ) {
	        rcvrNotFound = false;
	    } else if( maxPriority < triple.getPriority() ) {
		maxPriority = triple.getPriority();
		highPriorityTriple = triple;
	    }
	    cnt++;
	}
	return highPriorityTriple;
    }
     */

    /** Return the earliest possible time stamp of the next token to be
     *  processed or produced by the actor controlled by this thread.
     *  The next time is equal to the oldest (smallest valued) rcvrTime
     *  of all receivers managed by this thread.
     * @return The next earliest possible time stamp to be produced by
     *  this actor.
     * @see TimedQueueReceiver
    public double getNextTime() {
        if( _rcvrTimeList.size() == 0 ) {
            return _currentTime;
        }
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
        return triple.getTime();
    }
     */

    /** Return the active ODFReceiver with the oldest rcvrTime of all 
     *  ODFReceivers contained in the actor that this ODFThread controls.
     * @return ODFReceiver Return the oldest active ODFReceiver managed by
     *  this ODFThread.
    public synchronized ODFReceiver getFirstRcvr() {
	if( _rcvrTimeList.size() == 0 ) {
	    return null;
	}
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
	return (ODFReceiver)triple.getReceiver();
    }
     */

    /** Return true if the minimum receiver time is unique to a single
     *  receiver. Return true if there are no input receivers. Return
     *  false if two or more receivers share the same rcvrTime and this
     *  rcvrTime is less than that of any other receivers contained by
     *  the same actor.
     * @return True if the minimum rcvrTime is unique to a single receiver
     *  or if there are no receivers; otherwise return false.
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
     */

    /** Set the current time of this ODFThread. If the new specified
     *  time is less than the previous value for current time, then
     *  throw an IllegalArgumentException. Do not throw an
     *  IllegalActionException if the current time is set to -1.0 to
     *  indicate termination.
     * @param time The new value for current time.
     * @exception IllegalActionException If there is an attempt to
     *  decrease the value of current time.
    void setCurrentTime(double time) {
	if( time < _currentTime && time != -1.0 ) {
	    throw new IllegalArgumentException(
		    ((NamedObj)getActor()).getName() + ": Attempt to "
		    + "set current time in the past.");
	}
        _currentTime = time;
    }
     */

    /** FIXME
    public synchronized void setDelayTime(double delay) { 
	_delayTime = delay;
    }
     */

    /** Notify actors connected via output ports of the actor that
     *  this thread controls, that this thread's actor will no
     *  longer be producing tokens. Send events with time stamps of
     *  -1.0 to these "downstream" actors.
     */
    public synchronized void noticeOfTermination() { 
        Actor actor = (Actor)getActor();
	/*
        ODFActor actor = (ODFActor)getActor();
	System.out.println(((NamedObj)actor).getName()+
                ": calling noticeOfTermination()");
	*/
	Enumeration outputPorts = actor.outputPorts();
	if( outputPorts != null ) {
	    while( outputPorts.hasMoreElements() ) {
	        IOPort port = (IOPort)outputPorts.nextElement();
                Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
                if( rcvrs == null ) {
	            break;
	        }
                for (int i = 0; i < rcvrs.length; i++) {
                    for (int j = 0; j < rcvrs[i].length; j++) {
			try {
                            ((ODFReceiver) rcvrs[i][j]).put(null, -1.0);
			} catch( TerminateProcessException e ) {
			    // Do nothing since we are ending
			}
		    }
                }
            }
	}
    }

    /** Cause the actor controlled by this thread to send a NullToken
     *  to all output channels that have a rcvrTime less than the
     *  current time of this thread. Associate a time stamp with each
     *  NullToken that is equal to the current time of this thread.
     */
    public void sendOutNullTokens() {
        /*
        ODFActor actor = (ODFActor)getActor();
	Enumeration ports = actor.outputPorts();
        while( ports.hasMoreElements() ) {
            IOPort port = (IOPort)ports.nextElement();
	    Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
	    if( rcvrs == null ) {
	        return;
	    }
            for (int i = 0; i < rcvrs.length; i++) {
                for (int j = 0; j < rcvrs[i].length; j++) {
                    double time = getCurrentTime();
                    if( time >
			    ((ODFReceiver)rcvrs[i][j]).getRcvrTime() ) {
                        ((ODFReceiver)rcvrs[i][j]).put(
                                new NullToken(), time );
                    }
		}
            }
        }
        */
    }

    /** Set the priorities of the receivers contained in the input
     *  ports of the actor controlled by this thread. Group the input
     *  receivers for the controlled actor according to their respective
     *  container input ports. Order the port groups according to the
     *  inverse order in which their corresponding ports were connected
     *  in the model topology. I.e., if two input ports (port A and port
     *  B) of an actor are such that port A is connected in the topology
     *  before port B, then all of the receivers of port B will have a
     *  higher priority than the receivers of port A.
     *  <P>
     *  Within a group, order the receiver priorities relative to one
     *  another according to the inverse order in which they were
     *  connected to the model topology. I.e., if two input receivers
     *  (receiver A and receiver B) are added to an actor such that
     *  receiver A is connected in the model topology before receiver
     *  B, then receiver B will have a higher priority than receiver A.
     *  <P>
     *  Set this thread as the controlling thread for each receiver
     *  whose priority is set.
     * @exception IllegalActionException If receiver access leads to
     *  an error.
    public synchronized void setRcvrPriorities() throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
        Actor actor = (Actor)getActor();
	Enumeration enum = actor.inputPorts();
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
        // Now set the priorities of each port's receiver, set the receiving
	// time keeper and initialize the rcvrList
        //
        int cnt = 0;
        int currentPriority = 0;
        while( cnt < listOfPorts.size() ) {
            IOPort port = (IOPort)listOfPorts.at(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((ODFReceiver)rcvrs[i][j]).setPriority(
			    currentPriority);
                    ((ODFReceiver)rcvrs[i][j]).setThread(this);
                    ((ODFReceiver)rcvrs[i][j]).setReceivingTimeKeeper(_timeKeeper);

		    // Is the following necessary?? 
                    //////
                    RcvrTimeTriple triple = new RcvrTimeTriple(
                            (ODFReceiver)rcvrs[i][j],
			    _currentTime, currentPriority );
                    updateRcvrList( triple );
                    //////

                    currentPriority++;
                }
            }
            cnt++;
        }

	//
	// Now set the sending time keeper for all connected receivers.
	//
    }
     */

    /** Initialize the RcvrTimeList of this ODFThread by properly
     *  ordering all triples.
     * @exception IllegalActionException If there is an error
     *  while accessing the receivers contained by the actor
     *  that this ODFThread controls.
    public void initializeRcvrList() throws IllegalActionException {
        Actor actor = (Actor)getActor();
	Enumeration enum = actor.inputPorts();

	//
	// Set the receiving time keeper and update the RcvrTimeTable
	//
        while( enum.hasMoreElements() ) {
	    IOPort inport = (IOPort)enum.nextElement();
            Receiver[][] rcvrs = inport.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
		    ODFReceiver rcvr = (ODFReceiver)rcvrs[i][j];
                    rcvr.setReceivingTimeKeeper(_timeKeeper);
                    RcvrTimeTriple triple = new RcvrTimeTriple(
			    rcvr, rcvr.getRcvrTime(),
			    rcvr.getPriority() );
		    updateRcvrList(triple);
		}
	    }
	}

	//
	// Now set the sending time keeper for all connected receivers.
	//
	enum = actor.outputPorts();
	while( enum.hasMoreElements() ) {
	    IOPort outport = (IOPort)enum.nextElement();
            Receiver[][] rcvrs = outport.getRemoteReceivers();
	    if( rcvrs == null ) {
		// FIXME: Not necessary anymore
		System.out.println("Null Rcvrs!!!");
	    } else {
	    }
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
		    ODFReceiver rcvr = (ODFReceiver)rcvrs[i][j];
                    rcvr.setSendingTimeKeeper(_timeKeeper);
		}
	    }
	}
    }
     */

    /** Update the list of RcvrTimeTriples by positioning the
     *  specified triple. If the specified triple is already
     *  contained in the list, then the triple is removed and
     *  then added back to the list. The position of the triple
     *  is based on the triple's time value. If all receivers
     *  contained in the RcvrTimeTriple list have rcvrTimes of
     *  -1.0, then notify all actors connected via the output
     *  ports of the actor that this thread controls, that this
     *  actor is ceasing execution.
     * @param triple The RcvrTimeTriple to be positioned in the list.
    public synchronized void updateRcvrList(RcvrTimeTriple triple) {
	_removeRcvrTriple( triple );
	_addRcvrTriple( triple );
    }
     */

    /** End the execution of the actor under the control of this
     *  thread. Notify all actors connected to this actor that
     *  this actor is preparing to cease execution.
     *  @exception IllegalActionException if an error occurs while
     *  ending execution of the actor under the control of this
     *  thread.
     */
    public void wrapup() throws IllegalActionException {
	noticeOfTermination();
	super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the RcvrTimeTriple list contained by
     *  this actor. Use this method for testing purposes only.
     * @deprecated
    synchronized void printRcvrList() {
	String name = ((NamedObj)getActor()).getName();
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
	    Receiver testRcvr = testTriple.getReceiver();
            double time = testTriple.getTime();
            String testPort = testRcvr.getContainer().getName();
            String testString = "null";
            System.out.println("\t"+name+"'s Receiver "+i+
	            " has a time of " +time+" and string: "+testString);
        }
        System.out.println("***End of printRcvrList()\n");
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         methods			   ////

    /** Add the specified RcvrTimeTriple to the list of triples.
     *  If the time stamp of the specified triple is -1.0, then
     *  insert the triple into the last position of the RcvrTimeTriple
     *  list. Otherwise, insert the triple immediately after all
     *  other triples with time stamps less than or equal to the
     *  time stamp of the specified triple. ALWAYS call
     *  _removeRcvrTriple immediately before calling this method if
     *  the RcvrTimeTriple list already contains the triple specified
     *  in the argument.
    private void _addRcvrTriple(RcvrTimeTriple newTriple) {
        if( _rcvrTimeList.size() == 0 ) {
            _rcvrTimeList.insertAt( 0, newTriple );
            return;
        }

	if( newTriple.getTime() == -1.0 ) {
	    _rcvrTimeList.insertLast(newTriple);
	    return;
	}

	int cnt = 0;
        boolean notAddedYet = true;
	while( cnt < _rcvrTimeList.size() ) {
	    RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.at(cnt);

	    if( triple.getTime() == -1.0 ) {
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
     */

    /** Remove the specified RcvrTimeTriple from the list of triples.
    private void _removeRcvrTriple(RcvrTimeTriple triple) {

        Receiver rcvrToBeRemoved = triple.getReceiver();

	for( int cnt = 0; cnt < _rcvrTimeList.size(); cnt++ ) {
	    RcvrTimeTriple nextTriple = (RcvrTimeTriple)_rcvrTimeList.at(cnt);
	    Receiver nextRcvr = nextTriple.getReceiver();

	    if( rcvrToBeRemoved == nextRcvr ) {
	        _rcvrTimeList.removeAt( cnt );
		cnt = _rcvrTimeList.size();
	    }
	}
    }
     */

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private Manager _manager;
    // private ProcessDirector _director;

    // The _rcvrTimeList stores RcvrTimeTriples and is used to
    // order the receivers according to time and priority.
    // private LinkedList _rcvrTimeList;

    // The currentTime of the actor that is controlled by this
    // thread is equivalent to the minimum positive rcvrTime of
    // each input receiver.
    // private double _currentTime = 0.0;

    // The delay time associated with this actor. 
    // private double _delayTime = 0.0;

    private TimeKeeper _timeKeeper = null;

}








