/* The base class for OD actors.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODActor
/** 
The base class for OD actors. ODActors are intended to run as threaded
processes that maintain a distributed notion of time. Each ODActor in
a system of connected actors, must maintain a local notion of time 
which is dependent on the time of events that flow through its input
receivers. An event is simply an object which contains a token, a
time stamp and a receiver (to which the event is destined).
<P>
To facilitate this process, the ODReceivers contained by ODActors each have 
three important variables: rcvrTime, lastTime and priority. The rcvrTime 
of an ODReceiver is equal to the time of the oldest event that resides 
on the receiver. The lastTime is equal to the time of the newest event 
residing in the receiver. 
<P>
An ODActor consumes tokens from the input receiver which has the oldest
(smallest valued) rcvrTime. Such consumption is accomplished via blocking
reads from the corresponding input receiver. The priority variable is used
in cases where multiple input receivers share a common rcvrTime. Each
receiver has an integer-valued priority. The receiver with the highest 
priority is utilized if a common rcvrTime is shared by multiple receivers. 
<P>
The receiver priorities are set using the method setPriorities() in the
following manner. All of the input receivers for a given ODActor are 
grouped by their respective container input ports. If the ODIOPorts which 
contain the receivers have been explicitly assigned priorities, then the 
groups are ordered accordingly. If port priorities have not been explicitly
assigned, then the groups are ordered according to the inverse order in 
which their corresponding ODIOPorts were added to the containing ODActor. 
I.e., if two input ports (pA and pB) are added to an ODActor without explicit
priorities such that port pA is added before port pB, then all of the 
receivers of port pB will have a higher priority than the receivers of port 
pA.
<P>
Within a group the receiver priorities are further refined so that receivers
of the same group can be ordered relative to one another. Receiver priorities 
within a group are ordered according to the inverse order in which they were 
added to the containing ODIOPort. I.e., if two input receivers (rA and rB) 
are added to an ODActor such that receiver rA is added before receiver rB, 
then rB will have a higher priority than rA.
<P>
The above approach provides each receiver contained by a given ODActor with 
a unique priority, such that the set of receiver priorities for the  
containing ODActor is totally ordered. Note that currently setPriorities() 
calls the method port.getPriority(). This requires the port to be of type 
ODIOPort and hence precludes polymorphic actors. A later version of this 
class will not have this constraint.
<P>
RcvrTimeTriple objects are used to facilitate the ordering of receivers 
contained by an ODActor according to rcvrTime/lastTime and priority. A
RcvrTimeTriple is an object containing an ODReceiver, the _rcvrTime of
the receiver and the priority of the receiver. Each actor contains a list 
consisting of one RcvrTimeTriple per receiver contained by the actor. As 
tokens are placed in and taken out of the receivers of an actor, the list 
of RcvrTimeTriples is updated.
<P>
***
Synchronization Notes:
***
<P>
This domain observes a hierarchy of synchronization locks. When multiple
synchronization locks are required, they must be obtained in an order that
is consistent with this hierarchy. Adherence to this hierarchical ordering
ensures that deadlock can not occur due to circular lock dependencies.
<P>
The following synchronization hierarchy is utilized:
<P>
1. read/write access on the workspace <BR>
2. synchronization on the receiver <BR>
3. synchronization on the director <BR>
4. synchronization on the actor <BR>
5. (other) synchronization on the workspace <BR>
<P>
We say that lock #1 is at the highest level in the hierarchy and lock #5
is at the lowest level.
<P>
As an example, a method that synchronizes on a receiver can not contain
read/write access on the workspace; such accesses must occur outside of
the receiver synchronization. Similarly, a method which synchronizes on a
director must not synchronize on the receiver or contain read/write
accesses on the workspace; it can contain synchronizations on actors or
the workspace.
<P>
The justification of the chosen ordering of this hierarchy is based on
the access a method has to the fields of its object versus the fields of
other objects. The more (less) a method focuses on the internal state of
its object and non-synchronized methods of external objects, the lower
(higher) the method is placed in the synchronization hierarchy. In the
case of read/write access on the workspace, the corresponding methods,
i.e, getReadAccess() and getWriteAccess(), access the current thread
running in the JVM. This external access deems these methods as being at
the top of the hierarchy. All other synchronizations on the workspace only
focus on the internal state of the workspace and hence are at the bottom
of the synchronization hierarchy.
<P>

@author John S. Davis II
@version @(#)ODActor.java	1.18	11/19/98
*/
public class ODActor extends AtomicActor {

    /** Construct an ODActor with no container and no name.
     */
    public ODActor() {
        super();
        _rcvrTimeList = new LinkedList();
    }
    
    /** Construct an ODActor with the specified workspace and no name.
     * @param workspace The workspace for this ODActor.
     */
    public ODActor(Workspace workspace) {
	super(workspace);
        _rcvrTimeList = new LinkedList();
    }

    /** Construct an ODActor with the specified container and name.
     * @param container The container of this ODActor.
     * @param name The name of this ODActor.
     */
    public ODActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _rcvrTimeList = new LinkedList();
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor. The current time is
     *  equal the time stamp associated with the token most recently
     *  taken from one of the receivers contained by this ODActor.
     * @return The current time of this ODActor.
     */
    public double getCurrentTime() {
        return _currentTime;
    }
    
    /** Return the RcvrTimeTriple consisting of the receiver with the 
     *  highest priority given that it has the lowest nonnegative rcvrTime. 
     *  Return null if this actor's list of RcvrTimeTriples is empty.
     * @return The RcvrTimeTriple consisting of the receiver with the 
     *  highest priority and lowest nonnegative rcvrTime. If no triples 
     *  exist, return null.
     */
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
   
    /** Return the earliest possible time stamp of the next token to be
     *  processed or produced by this actor. The next time is equal to the 
     *  oldest (smallest valued) rcvrTime of all receivers contained by 
     *  this actor. 
     * @return The next earliest possible time stamp to be produced by 
     *  this actor.
     * @see TimedQueueReceiver
     */
    public double getNextTime() {
        if( _rcvrTimeList.size() == 0 ) {
            return _currentTime;
        }
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
        return triple.getTime();
    }
    
    /** Return a token from the receiver which has the minimum rcvrTime
     *  of all receivers contained by this actor. The returned token will 
     *  have the lowest time stamp of all pending tokens for this actor. 
     *  If there exists a set of multiple receivers that share a common 
     *  minimum rcvrTime, then return the token contained by the highest 
     *  priority receiver within this set. 
     *  <P>
     *  The following describes the algorithm that this method implements.
     *  <P>
     *  Prior to returning the appropriate token, set the current time of 
     *  this actor. If the current time is equal to -1, then this means
     *  that all receivers contained by this actor have expired. In this
     *  case, call noticeOfTermination() so that actors connected downstream
     *  are made aware that this actor will no longer be producing tokens.
     *  Then call setFinish() on the most recently expired receiver, followed
     *  by get() on this receiver. This will lead to the calling of a 
     *  TerminateProcessException which will cease iteration of this actor.
     *  <P>
     *  If the current time is not equal to -1, then call get() on the
     *  receiver which has the minimum rcvrTime and is listed first in 
     *  the RcvrTimeTriple list. If get() returns null, then check to
     *  see if there is a single receiver which has a minimum rcvrTime. 
     *  If so, call getNextToken() - note that a new receiver may now have 
     *  the minimum rcvrTime. If multiple receivers share a common minimum 
     *  rcvrTime, then determine the receiver which has the minimum rcvrTime 
     *  and highest priority. Call simultaneousIgnore() on this receiver 
     *  followed by get(). If get() returns a non-null token then return this 
     *  token. Otherwise this indicates that the receiver which previously 
     *  had the minimum rcvrTime and highest priority no longer has the 
     *  minimum rcvrTime. In this case call getNextToken().
     * @return The token with the smallest time stamp of all tokens
     *  contained by this actor. If multiple tokens share the smallest time
     *  stamp this token will come from the highest priority receiver that
     *  has the minimum rcvrTime. If all receivers have expired then a 
     *  TerminateProcessException will be thrown. 
     * @see TimedQueueReceiver
     * @see ODReceiver
     */
    public Token getNextToken() {
        
        // Increment the getNextToken counter
        _cntr++;

        Workspace workSpc = workspace();
        ODDirector director = (ODDirector)getDirector();
        
        String name = getName();
        
        /*
        if( name.equals("printer") ) {
            System.out.println("\n\n\n"+name+": Entered getNextToken()");
	    // printRcvrList();
        }
        */
        
        if( _rcvrTimeList.size() == 0 ) {
            // System.out.println("No receivers. Return from getNextToken()");
            return null;
        }
        
        /* BEGINNING ORIGINAL STUFF
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
        ODReceiver lowestRcvr = (ODReceiver)triple.getReceiver();
        _currentTime = triple.getTime();
	END OF ORIGINAL STUFF */

	// BEGINNING OF NEW STUFF
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeList.first();
        ODReceiver lowestRcvr = (ODReceiver)triple.getReceiver();
        _currentTime = triple.getTime();

        /*
	if( _currentTime > 9.0 && name.equals("printer") ) {
	    System.out.println("getNextToken() time = " + _currentTime);
	    System.out.println("getNextToken() counter = "+_cntr);
	}
        */

	if( triple.getTime() == -1.0 ) {
	    // All receivers have completed. 
	    // Prepare to terminate.
	    // System.out.println("All receivers have completed."); 
	    // printRcvrList();
	    noticeOfTermination();
	    lowestRcvr.setFinish();
	    lowestRcvr.get();
	    // Should never get to this point
	    System.out.println("Didn't throw TerminateProcessException "
			       + "in ODActor.get()");
	}
	// END OF NEW STUFF

        
        if( _currentTime > 2.0 && name.equals("printer") ) {
	    // System.out.println("preparing to call get");
	    // System.out.println("getNextToken() time = " + _currentTime);
	    // System.out.println("getNextToken() counter = "+_cntr);
            // printRcvrList();
        }
        /*
        */
        Token token = lowestRcvr.get();
        /*
        if( _currentTime > 9.0 && name.equals("printer") ) {
        // if( name.equals("printer") ) {
            System.out.println("just finished calling get");
	    System.out.println("getNextToken() time = " + _currentTime);
        }
        */
        
        if( token != null ) {
            /*
            if( name.equals("printer") ) {
                System.out.println(name+" returned a token: 1st non-null");
            }
            */
	    // updateRcvrList( triple );
            return token;
        } else {
            if( this.hasMinRcvrTime() ) {
                // This means that there must be a different
                // receiver which has the minimum arc time
                // after the token was actually received -
                // recall that the receiver may have been
                // blocking and then received an arc time
                // that was not the minimum.
                
                // if( name.equals("printer") ) {
	        if( _currentTime > 9.0 && name.equals("printer") ) {
                    System.out.println(name+" has minimum receiver time.");
                }
                /*
                */
                /*
	        if( _currentTime > 9.0 && name.equals("printer") ) {
		    System.out.println("getNextToken() time = "+_currentTime);
		}
                */
                return getNextToken();
                
            } else {
                // This means that multiple arcs have the 
                // the same minimum arc time. Find the arc
                // with the lowest time and priority.
                
                // if( name.equals("printer") ) {
	        if( _currentTime > 9.0 && name.equals("printer") ) {
                    System.out.println(name+" has no minimum receiver time.");
                }
                /*
                */
                
                RcvrTimeTriple priorityTriple = getHighestPriorityTriple();
                lowestRcvr = (ODReceiver)priorityTriple.getReceiver();
                lowestRcvr.setSimultaneousIgnore(true);
                token = lowestRcvr.get();
                
                if( token != null ) {
                    // updateRcvrList( priorityTriple );
                    /*
                    if( name.equals("printer") ) {
                        System.out.println(name+
                                " returned a token: 2nd non-null.");
                    }
                    */
                    return token;
                } else {
                    // This means that although originally there was 
                    // a receiver with highest priority, it must have
                    // blocked and upon receiving a token no longer
                    // had the minimum time. The result is that there 
                    // is another minimum.
                    
                    // if( name.equals("printer") ) {
	            if( _currentTime > 9.0 && name.equals("printer") ) {
                        System.out.println(name+ 
                                " minimum rcvrTime must have changed.");
                    }
                    /*
                    */
                        
                    return getNextToken();
                }
            }
        }
        // System.out.println("Reached end of getNextToken()");
    }

    /** Return true if the minimum receiver time is unique to a single
     *  receiver. Return true if there are no input receivers. Return
     *  false if two or more receivers share the same rcvrTime and this 
     *  rcvrTime is less than that of any other receivers contained by 
     *  the same actor.
     * @return True if the minimum rcvrTime is unique to a single receiver 
     *  or if there are no receivers; otherwise return false.
     */
    public boolean hasMinRcvrTime() {
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
    
    /** Initialize this actor by setting the receiver priorities.
     *  This method will also initialize the RcvrTimeTriple list
     *  via setPriorities().
     * @exception IllegalActionException If there is an error when
     *  setting the receiver priorities.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	setPriorities();
    }

    /** Notify actors connected to this actor via its output ports, that 
     *  this actor is being terminated. Send events with time stamps of
     *  -1.0 to these "downstream" actors. 
     */
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
	            ((ODReceiver) rcvrs[j][i]).put(null, -1.0);
		}
            }
	}
    }

    /** Prepare to cease iterations of this actor. Notify actors which
     *  are connected downstream of this actor's cessation. Return false
     *  to indicate that future execution can not occur.
     * @return False to indicate that future execution can not occur.
     */
    public boolean postfire() {
        // System.out.println(getName()+" is calling postfire()");
        noticeOfTermination();
        return false;
    }
    
    /** Set the priorities of the receivers contained in the input 
     *  ports of this actor. Group the input receivers for this actor
     *  according to their respective container input ports. If the
     *  ODIOPorts which contain the receivers have been explicitly
     *  assigned priorities, then the groups are ordered in a fashion
     *  consistent with this ordering. If port priorities have not been 
     *  explicitly assigned, then the groups are ordered according to 
     *  the inverse order in which their corresponding ODIOPorts were 
     *  added to the containing ODActor. I.e., if two input ports (pA 
     *  and pB) are added to an ODActor without explicit priorities such 
     *  that port pA is added before port pB, then all of the receivers of 
     *  port pB will have a higher priority than the receivers of port pA.
     *  <P> 
     *  Within a group, order the receiver priorities relative to one 
     *  another according to the inverse order in which they were added to 
     *  the containing ODIOPort. I.e., if two input receivers (rA and rB) 
     *  are added to an ODActor such that receiver rA is added before 
     *  receiver rB, then rB will have a higher priority than rA. 
     *  <P> 
     *  This above approach provides each receiver contained by a given 
     *  ODActor with a unique priority, such that the set of receiver 
     *  priorities for the containing ODActor is totally ordered. Note 
     *  that currently setPriorities() calls the method port.getPriority(). 
     *  This requires the port to be of type ODIOPort and hence precludes 
     *  polymorphic actors. A later version of this class will not have 
     *  this constraint.
     * @exception IllegalActionException If receiver access leads to an error.
     */
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
            ODIOPort port = (ODIOPort)enum.nextElement();
            int priority = port.getPriority();
            boolean portNotInserted = true;
            if( listOfPorts.size() == 0 ) {
                listOfPorts.insertAt( 0, port ); 
                portNotInserted = false;
            } else {
                for( int cnt = 0; cnt < listOfPorts.size(); cnt++ ) {
                    ODIOPort nextPort = (ODIOPort)listOfPorts.at(cnt);
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
            ODIOPort port = (ODIOPort)listOfPorts.at(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    ((ODReceiver)rcvrs[i][j]).setPriority(currentPriority); 
                    RcvrTimeTriple triple = new RcvrTimeTriple( 
                            (ODReceiver)rcvrs[i][j], _currentTime, 
                            currentPriority );
                    updateRcvrList( triple );
                    currentPriority++;
                }
            }
            cnt++;
        }
    }

    /** Update the list of RcvrTimeTriples by positioning the 
     *  specified triple. If the specified triple is already
     *  contained in the list, then the triple is removed and
     *  then added back to the list. The position of the triple
     *  is based on the triple's time value.
     * @param triple The RcvrTimeTriple to be positioned in the list.
     */
    public synchronized void updateRcvrList(RcvrTimeTriple triple) {
	_removeRcvrList( triple );
	_addRcvrTriple( triple );
    }
    
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
                        +((ODReceiver)testRcvr)._queue.size());
		if( ((ODReceiver)testRcvr)._queue.size() > 1 ) {
		    /*
                    Event testEvent2 = 
		            ((Event)((ODReceiver)testRcvr)._queue.get(1));
                    StringToken testToken2 = 
		            (StringToken)testEvent2.getToken();
		    testString2 = testToken2.stringValue();
                    System.out.println("\t"+getName()+"'s Receiver "+i+ 
		            " has a 2nd time of "+testEvent2.getTime()+
			    " and string: ");
		    */
		}
		if( ((ODReceiver)testRcvr)._queue.size() > 0 ) {
                    Event testEvent = 
                            ((Event)((ODReceiver)testRcvr)._queue.get(0));
                    StringToken testToken = (StringToken)testEvent.getToken();
		    if( testToken != null ) {
                        testString = testToken.stringValue();
		    }
		} else {
                    testString = "null";
		}
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
    private void _removeRcvrList(RcvrTimeTriple triple) {

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
    // updated in getNextToken().
    private double _currentTime = 0.0;

    private int _cntr = 0;


}




















