/* The base class for ODF actors.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODFActor
/** 
The base class for ODF actors. ODFActors are intended to run as threaded
processes that maintain a distributed notion of time. Each ODFActor in
a system of connected actors, must maintain a local notion of time 
which is dependent on the time of events that flow through its input
receivers. An event is simply an object which contains a token, a
time stamp and a receiver (to which the event is destined).
<P>
To facilitate this process, the ODFReceivers contained by ODFActors each have 
three important variables: rcvrTime, lastTime and priority. The rcvrTime 
of an ODFReceiver is equal to the time of the oldest event that resides 
on the receiver. The lastTime is equal to the time of the newest event 
residing in the receiver. 
<P>
An ODFActor consumes tokens from the input receiver which has the oldest
(smallest valued) rcvrTime. Such consumption is accomplished via blocking
reads from the corresponding input receiver. The priority variable is used
in cases where multiple input receivers share a common rcvrTime. Each
receiver has an integer-valued priority. The receiver with the highest 
priority is utilized if a common rcvrTime is shared by multiple receivers. 
<P>
The receiver priorities are set using the method setPriorities() in the
following manner. All of the input receivers for a given ODFActor are 
grouped by their respective container input ports. If the ODFIOPorts which 
contain the receivers have been explicitly assigned priorities, then the 
groups are ordered accordingly. If port priorities have not been explicitly
assigned, then the groups are ordered according to the inverse order in 
which their corresponding ODFIOPorts were added to the containing ODFActor. 
I.e., if two input ports (pA and pB) are added to an ODFActor without explicit
priorities such that port pA is added before port pB, then all of the 
receivers of port pB will have a higher priority than the receivers of port 
pA.
<P>
Within a group the receiver priorities are further refined so that receivers
of the same group can be ordered relative to one another. Receiver priorities 
within a group are ordered according to the inverse order in which they were 
added to the containing ODFIOPort. I.e., if two input receivers (rA and rB) 
are added to an ODFActor such that receiver rA is added before receiver rB, 
then rB will have a higher priority than rA.
<P>
The above approach provides each receiver contained by a given ODFActor with 
a unique priority, such that the set of receiver priorities for the  
containing ODFActor is totally ordered. Note that currently setPriorities() 
calls the method port.getPriority(). This requires the port to be of type 
ODFIOPort and hence precludes polymorphic actors. A later version of this 
class will not have this constraint.
<P>
RcvrTimeTriple objects are used to facilitate the ordering of receivers 
contained by an ODFActor according to rcvrTime/lastTime and priority. A
RcvrTimeTriple is an object containing an ODFReceiver, the _rcvrTime of
the receiver and the priority of the receiver. Each actor contains a list 
consisting of one RcvrTimeTriple per receiver contained by the actor. As 
tokens are placed in and taken out of the receivers of an actor, the list 
of RcvrTimeTriples is updated.

@author John S. Davis II
@version $Id$
*/
public class ODFActor extends AtomicActor {

    /** Construct an ODFActor with no container and no name.
     */
    public ODFActor() {
        super();
        _rcvrTimeList = new LinkedList();
    }
    
    /** Construct an ODFActor with the specified workspace and no name.
     * @param workspace The workspace for this ODFActor.
     */
    public ODFActor(Workspace workspace) {
	super(workspace);
        _rcvrTimeList = new LinkedList();
    }

    /** Construct an ODFActor with the specified container and name.
     * @param container The container of this ODFActor.
     * @param name The name of this ODFActor.
     */
    public ODFActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _rcvrTimeList = new LinkedList();
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor. The current time is
     *  equal the time stamp associated with the token most recently
     *  taken from one of the receivers contained by this ODFActor.
     * @return The current time of this ODFActor.
     */
    public double getCurrentTime() {
	Thread thread = Thread.currentThread();
	if( thread instanceof ODFThread ) {
	    _currentTime = ((ODFThread)thread).getCurrentTime();
            return _currentTime;
	}
	return _currentTime;
    }
    
    /** Return the RcvrTimeTriple consisting of the receiver with the 
     *  highest priority given that it has the lowest nonnegative rcvrTime. 
     *  Return null if this actor's list of RcvrTimeTriples is empty.
     * @return The RcvrTimeTriple consisting of the receiver with the 
     *  highest priority and lowest nonnegative rcvrTime. If no triples 
     *  exist, return null.
    public RcvrTimeTriple getHighestPriorityTriple() {
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
   
    /** Return a non-NullToken from the receiver which has the minimum
     *  rcvrTime.
     *  FIXME: Add thorough comments.
     */
    public Token getNextToken() {
	/*
	System.out.println(getName()+": Inside getNextToken()");
        sendOutNullTokens();
	*/
        Token token = getNextInput(); 
        if( token instanceof NullToken ) {
	    System.out.println(getName()+": got a NullToken from getNextInput()");
            return getNextToken();
        } else if( token == null ) {
	    System.out.println(getName()+": got a null token from getNextInput()");
            return getNextToken();
	}
        return token;
    }
    
    /** Return a token from the receiver which has the minimum rcvrTime
     *  of all receivers contained by this actor. The returned token will 
     *  have the lowest time stamp of all pending tokens for this actor. 
     *  If there exists a set of multiple receivers that share a common 
     *  minimum rcvrTime, then return the token contained by the highest 
     *  priority receiver within this set. If this actor contains no
     *  receivers then return null.
     *  <P>
     *  The following describes the algorithm that this method implements.
     *  <P>
     *  Prior to returning the appropriate token, set the current time of 
     *  this actor to equal the minimum rcvrTime of all receivers contained
     *  by this actor. If the current time is equal to -1, then this means
     *  that all receivers contained by this actor have expired. In this
     *  case, call noticeOfTermination() so that actors connected downstream
     *  are made aware that this actor will no longer be producing tokens.
     *  Then call requestFinish() on the most recently expired receiver, 
     *  followed by get() on this receiver. This will lead to the calling of 
     *  a TerminateProcessException which will cease iteration of this actor.
     *  <P>
     *  If the current time is not equal to -1, then call get() on the
     *  receiver which has the minimum rcvrTime and is listed first in 
     *  the RcvrTimeTriple list. If get() returns null, then check to
     *  see if there is a single receiver which has a minimum rcvrTime. 
     *  If so, call getNextInput(). Note that a new receiver may now have 
     *  the minimum rcvrTime. If multiple receivers share a common minimum 
     *  rcvrTime, then determine the receiver which has the minimum rcvrTime 
     *  and highest priority. Call simultaneousIgnore() on this receiver 
     *  followed by get(). If get() returns a non-null token then return this 
     *  token. Otherwise this indicates that the receiver which previously 
     *  had the minimum rcvrTime and highest priority no longer has the 
     *  minimum rcvrTime. In this case call getNextInput().
     * @return The token with the smallest time stamp of all tokens
     *  contained by this actor. If multiple tokens share the smallest time
     *  stamp this token will come from the highest priority receiver that
     *  has the minimum rcvrTime. If all receivers have expired then a 
     *  TerminateProcessException will be thrown. 
     * @see TimedQueueReceiver
     * @see ODFReceiver
     */
    public Token getNextInput() {
	Thread thread = Thread.currentThread();
	ODFThread odfthread = null;
	if( thread instanceof ODFThread ) {
	    // System.err.println(getName()+": Controlled by ODFThread");
	    odfthread = (ODFThread)thread;
	    if( odfthread.getActor() != this ) {
		System.err.println("ODFThread does not own this actor!!!");
	    }
	} else {
	    System.err.println("Non-ODFThread controlling "+getName());
	}
	/*
        if( _rcvrTimeList.size() == 0 ) {
            return null;
        }
        
        Workspace workSpc = workspace();
        ODFDirector director = (ODFDirector)getDirector();
        String name = getName();
	*/
        
	/*
        RcvrTimeTriple bestTriple = null;
        ODFReceiver lowestRcvr = null;
	synchronized(this) {
        bestTriple = 
	        (RcvrTimeTriple)odfthread._rcvrTimeList.first();
        lowestRcvr = 
                (ODFReceiver)bestTriple.getReceiver();
	}
	*/
        // _currentTime = bestTriple.getTime();

        ODFReceiver lowestRcvr = odfthread.getFirstRcvr();

	/*
	if( bestTriple.getTime() == -1.0 ) {
	    // noticeOfTermination();
	    lowestRcvr.requestFinish();
	    lowestRcvr.get();
	    System.out.println("Didn't throw TerminateProcessException "
			       + "in ODFActor.get()");
	}
	*/

        // Token token = null;

	if( lowestRcvr.hasToken() ) {
	    /*
	    if( getName().equals("printer") ) {
	        System.out.println(getName()+": Lowest receiver has a token");
	    } else {
	        System.out.println(getName()+": Lowest receiver has a token");
	    }
	    */
	    return lowestRcvr.get();
	} else {
	    /*
	    if( getName().equals("printer") ) {
	        System.out.println(getName()+": Lowest receiver does not have a token");
	    }
	    */
	    return getNextInput();
	}
        
	    /*
            if( this.hasMinRcvrTime() ) {
                // This means that there must be a different receiver 
	        // which has the minimum rcvrTime after the token was 
	        // actually received - recall that the receiver may have 
	        // been blocking and then received an receiver time that 
	        // was not the minimum.

                return getNextInput();
            } else {
                // This means that multiple receivers have the the same 
	        // minimum rcvrTime. Find the receiver with the lowest 
	        // time and priority.
                
                bestTriple = getHighestPriorityTriple();
                lowestRcvr = (ODFReceiver)bestTriple.getReceiver();
                lowestRcvr.setSimultaneousIgnore(true);
                token = lowestRcvr.get();
                
                if( token != null ) {
                    return token;
                } else {
                    // This means that although originally there was 
                    // a receiver with highest priority, it must have
                    // blocked and upon receiving a token no longer
                    // had the minimum time. The result is that there 
                    // is another minimum.
                    
                    return getNextInput();
                }
            }
        }
	*/
    }

    /** Return the earliest possible time stamp of the next token to be
     *  processed or produced by this actor. The next time is equal to the 
     *  oldest (smallest valued) rcvrTime of all receivers contained by 
     *  this actor. 
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
    
    /** Initialize this actor by setting the receiver priorities.
     *  This method will also initialize the RcvrTimeTriple list
     *  via setPriorities().
     * @exception IllegalActionException If there is an error when
     *  setting the receiver priorities.
    public void initialize() throws IllegalActionException {
        super.initialize();
	setPriorities();
    }
     */

    /** Notify actors connected to this actor via its output ports, that 
     *  this actor is being terminated. Send events with time stamps of
     *  -1.0 to these "downstream" actors. 
    public void noticeOfTermination() { 
	Enumeration outputPorts = outputPorts();
	if( outputPorts == null ) {
	    return;
	}
	while( outputPorts.hasMoreElements() ) {
	    IOPort port = (IOPort)outputPorts.nextElement();
	    Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
	    if( rcvrs == null ) {
	        return;
	    }
            for (int j = 0; j < rcvrs.length; j++) {
                for (int i = 0; i < rcvrs[j].length; i++) {
	            ((ODFReceiver) rcvrs[j][i]).put(null, -1.0);
		}
            }
	}
    }
     */

    /** Prepare to cease iterations of this actor. Notify actors which
     *  are connected downstream of this actor's cessation. Return false
     *  to indicate that future execution can not occur.
     * @return False to indicate that future execution can not occur.
     * @exception IllegalActionException Not thrown in this class. May be
     *  thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        // noticeOfTermination();
        return false;
    }
    
    /** Send a NullToken to all output channels that have a rcvrTime 
     *  less than the current time of this actor. Associate a time
     *  stamp with each NullToken that is equal to the current time
     *  of this actor.
    public void sendOutNullTokens() {
        Enumeration ports = outputPorts(); 
        while( ports.hasMoreElements() ) {
            IOPort port = (IOPort)ports.nextElement();
	    Receiver rcvrs[][] = (Receiver[][])port.getRemoteReceivers();
	    if( rcvrs == null ) {
	        return;
	    }
            for (int i = 0; i < rcvrs.length; i++) {
                for (int j = 0; j < rcvrs[i].length; j++) {
                    double time = getCurrentTime();
                    if( time > ( (ODFReceiver)rcvrs[i][j]
                            ).getRcvrTime() ) {
                        ((ODFReceiver)rcvrs[i][j]).put( 
                                new NullToken(), time );
                    }
		}
            }
        }
    }
     */
    
    /** Set the priorities of the receivers contained in the input 
     *  ports of this actor. Group the input receivers for this actor
     *  according to their respective container input ports. If the
     *  ODFIOPorts which contain the receivers have been explicitly
     *  assigned priorities, then the groups are ordered in a fashion
     *  consistent with this ordering. If port priorities have not been 
     *  explicitly assigned, then the groups are ordered according to 
     *  the inverse order in which their corresponding ODFIOPorts were 
     *  added to the containing ODFActor. I.e., if two input ports (pA 
     *  and pB) are added to an ODFActor without explicit priorities such 
     *  that port pA is added before port pB, then all of the receivers of 
     *  port pB will have a higher priority than the receivers of port pA.
     *  <P> 
     *  Within a group, order the receiver priorities relative to one 
     *  another according to the inverse order in which they were added to 
     *  the containing ODFIOPort. I.e., if two input receivers (rA and rB) 
     *  are added to an ODFActor such that receiver rA is added before 
     *  receiver rB, then rB will have a higher priority than rA. 
     *  <P> 
     *  This above approach provides each receiver contained by a given 
     *  ODFActor with a unique priority, such that the set of receiver 
     *  priorities for the containing ODFActor is totally ordered. Note 
     *  that currently setPriorities() calls the method port.getPriority(). 
     *  This requires the port to be of type ODFIOPort and hence precludes 
     *  polymorphic actors. A later version of this class will not have 
     *  this constraint.
     * @exception IllegalActionException If receiver access leads to an error.
    public void setPriorities() throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
        Enumeration enum = inputPorts();
	if( !enum.hasMoreElements() ) {
            return;
	}
        
        //
        // First Order Input Ports According To Priority
        //
        while( enum.hasMoreElements() ) {
            ODFIOPort port = (ODFIOPort)enum.nextElement();
            int priority = port.getPriority();
            boolean portNotInserted = true;
            if( listOfPorts.size() == 0 ) {
                listOfPorts.insertAt( 0, port ); 
                portNotInserted = false;
            } else {
                for( int cnt = 0; cnt < listOfPorts.size(); cnt++ ) {
                    ODFIOPort nextPort = (ODFIOPort)listOfPorts.at(cnt);
                    if( port.getPriority() < nextPort.getPriority() ) {
                        if( port != nextPort ) {
                            listOfPorts.insertAt( cnt, port ); 
                            cnt = listOfPorts.size(); 
                        } 
                        portNotInserted = false;
                    }
                }
            }
            if( portNotInserted ) {
                listOfPorts.insertLast(port);
                portNotInserted = false;
            }
        } 
        
        //
        // Now Set The Priorities Of Each Port's Receiver
        // And Initialize RcvrList
        //
        int cnt = 0;
        int currentPriority = 0;
        while( cnt < listOfPorts.size() ) {
            ODFIOPort port = (ODFIOPort)listOfPorts.at(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((ODFReceiver)rcvrs[i][j]).setPriority(currentPriority); 
                    RcvrTimeTriple triple = new RcvrTimeTriple( 
                            (ODFReceiver)rcvrs[i][j], _currentTime, 
                            currentPriority );
                    updateRcvrList( triple );
                    currentPriority++;
                }
            }
            cnt++;
        }
    }
     */

    /** Update the list of RcvrTimeTriples by positioning the 
     *  specified triple. If the specified triple is already
     *  contained in the list, then the triple is removed and
     *  then added back to the list. The position of the triple
     *  is based on the triple's time value.
     * @param triple The RcvrTimeTriple to be positioned in the list.
    public synchronized void updateRcvrList(RcvrTimeTriple triple) {
	_removeRcvrTriple( triple );
	_addRcvrTriple( triple );
    }
     */
    
    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the RcvrTimeTriple list contained by 
     *  this actor. Use this method for testing purposes only.
     */
    void printRcvrList() {
        System.out.println("\n***Print "+getName()+"'s RcvrList.");
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
            String testString2 = "null";
            if( getName().equals("printer") ) {
		System.out.println("   Printer -> size() = "
                        +((ODFReceiver)testRcvr)._queue.size());
		if( ((ODFReceiver)testRcvr)._queue.size() > 1 ) {
		    /*
                    Event testEvent2 = 
		            ((Event)((ODFReceiver)testRcvr)._queue.get(1));
                    StringToken testToken2 = 
		            (StringToken)testEvent2.getToken();
		    testString2 = testToken2.stringValue();
                    System.out.println("\t"+getName()+"'s Receiver "+i+ 
		            " has a 2nd time of "+testEvent2.getTime()+
			    " and string: ");
		    */
		}
                /*
		if( ((ODFReceiver)testRcvr)._queue.size() > 0 ) {
                    Event testEvent = 
                            ((Event)((ODFReceiver)testRcvr)._queue.get(0));
                    StringToken testToken = (StringToken)testEvent.getToken();
		    if( testToken != null ) {
                        testString = testToken.stringValue();
		    }
		} else {
                    testString = "null";
		}
                */
            }
            System.out.println("\t"+getName()+"'s Receiver "+i+
	            " has a time of " +time+" and string: "+testString);
        }
        System.out.println("***End of printRcvrList()\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods			   ////

    /** Add the specified RcvrTimeTriple to the list of triples.
     *  If the time stamp of the specified triple is -1.0, then
     *  insert the triple into the last position of the RcvrTimeTriple
     *  list. Otherwise, insert the triple immediately after all
     *  other triples with time stamps less than or equal to the
     *  time stamp of the specified triple. ALWAYS call _removeRcvrTriple 
     *  immediately before calling this method if the RcvrTimeTriple list 
     *  already contains the triple specified in the argument.
     */
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
    
    /** Remove the specified RcvrTimeTriple from the list of triples.
     */
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

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The _rcvrTimeList stores RcvrTimeTriples and is used to
    // order the receivers according to time and priority.
    private LinkedList _rcvrTimeList;
    
    // The currentTime of this actor is equivalent to the minimum
    // positive rcvrTime of each input receiver. This value is 
    // updated in getNextInput().
    private double _currentTime = 0.0;

}




















