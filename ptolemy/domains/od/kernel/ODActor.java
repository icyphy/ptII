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
which is dependent on the time of events that flow through the input
receivers. An event is simply an object which contains a token, a
time stamp and a receiver (to which the event is destined).

To facilitate this process, the ODReceivers used by ODActors each have 
three important variables: rcvrTime, lastTime and priority. The rcvrTime 
of an ODReceiver is equal to the time of the oldest event that resides 
on the receiver. The lastTime is equal to the time of the newest event 
residing in the receiver. 

An ODActor consumes tokens from the input receiver which has the oldest
(smallest valued) rcvrTime. Such consumption is accomplished via blocking
reads from the corresponding input receiver. The priority variable is used
in cases where multiple input receivers share a common rcvrTime. Each
receiver has an integer-valued priority. The receiver with the highest 
priority is utilized if a common rcvrTime is shared. 

The receiver priorities are set using the method setPriorities(). 
First all of the input receivers are grouped by their container ports.
If the ODIOPorts which contain the receivers have been explicitly 
assigned priorities, then these groups are ordered accordingly. If not,
the groups are ordered in increasing order according to the call to
inputPorts(). Within each group, the priorities are assigned in increasing 
order based on the call to port.getReceivers(). 

Note that currently setPriorities() calls the method port.getPriority().
This requires the port to be of type ODIOPort and hence precludes 
polymorphic actors. A later version of this class will not have this 
constraint.

Synchronization Notes:
This domain observes a hierarchy of synchronization locks. When multiple
synchronization locks are required, they must be obtained in an order that
is consistent with this hierarchy. Adherence to this hierarchical ordering
ensures that deadlock can not occur due to circular lock dependencies.
 
The following synchronization hierarchy is utilized:
 
        1. read/write access on the workspace
        2. synchronization on the receiver
        3. synchronization on the director
        4. synchronization on the actor
        5. (other) synchronization on the workspace
 
We say that lock #1 is at the highest level in the hierarchy and lock #5
is at the lowest level.

As an example, a method that synchronizes on a receiver can not contain
read/write access on the workspace; such accesses must occur outside of
the receiver synchronization. Similarly, a method which synchronizes on a
director must not synchronize on the receiver or contain read/write
accesses on the workspace; it can contain synchronizations on actors or
the workspace.
 
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



@author John S. Davis II
@version @(#)ODActor.java	1.18	11/19/98
*/
public class ODActor extends AtomicActor {

    /** 
     */
    public ODActor() {
        super();
        _rcvrTimeTable = new LinkedList();
    }
    
    /** 
     */
    public ODActor(Workspace workspace) {
	super(workspace);
        _rcvrTimeTable = new LinkedList();
    }

    /** 
     */
    public ODActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _rcvrTimeTable = new LinkedList();
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor.
     */
    public double getCurrentTime() {
        if( _rcvrTimeTable.size() == 0 ) {
            return _currentTime;
        }
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeTable.first();
        _currentTime = triple.getTime();
        return _currentTime;
    }
    
    /** Return true if the minimum receiver time is unique to a single
     *  receiver.
     *  FIXME: This is probably not the best name. Makes sense 
     *         mathematically but not colloquially.
     *         How about: hasUniqueMinTime()
     */
    public boolean hasMinRcvrTime() {
        if( _rcvrTimeTable.size() < 2 ) {
            return true;
        }

        RcvrTimeTriple firstTriple = (RcvrTimeTriple)_rcvrTimeTable.first(); 
	RcvrTimeTriple secondTriple = (RcvrTimeTriple)_rcvrTimeTable.at(1);

	if( firstTriple.getTime() == secondTriple.getTime() ) {
	    return false;
	}

	return true;
    }
    
    /** Initialize this actor
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // System.out.println("ODActor.initialize is beginning.");
	setPriorities();
        // System.out.println("Priorities have been set");
    }

    /** Return the receiver with the lowest time associated with it;
     *  return null if no receivers exist. The returned receiver may
     *  not necessarily have a unique time stamp associated with it.
     */
    public ODReceiver getLowestTimeRcvr() {
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeTable.first();
	return (ODReceiver)triple.getReceiver();
    }
    
    /** Return the receiver with the highest priority given that it has
     *  the lowest time stamp.
     */
    public RcvrTimeTriple getHighestPriorityTriple() {
        double time = -1.0;
	double firstTime = -1.0;
        int maxPriority = 0;
	int cnt = 0;
	boolean rcvrNotFound = true;
        RcvrTimeTriple highPriorityTriple = null;

	while( rcvrNotFound ) {
	    if( cnt == _rcvrTimeTable.size() ) {
	        return highPriorityTriple;
	    }
	    RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeTable.at(cnt);
	    if( time == -1.0 ) {
	        time = triple.getTime();
	        firstTime = time;
		maxPriority = triple.getPriority();
		highPriorityTriple = triple;
	    } else {
	        time = triple.getTime();
	    }
	    if( time > firstTime ) {
	        rcvrNotFound = false;
	    } else {
		if( maxPriority < triple.getPriority() ) {
		    maxPriority = triple.getPriority();
		    highPriorityTriple = triple;
		}
	    }
	    cnt++;
	}
	return highPriorityTriple;
    }
   
    /** Get the next token which has the minimum rcvrTime and highest
     *  priority. The returned token will have the lowest time stamp of 
     *  all pending tokens for this actor. If there are multiple tokens 
     *  with the lowest time stamp, then the returned token will also 
     *  have the highest priority.
     */
    public Token getNextToken() {
        
        Workspace workSpc = workspace();
        ODDirector director = (ODDirector)getDirector();
        
        String name = getName();
        
        /*
        if( name.equals("printer") ) {
            System.out.println("\n"+name+": Entered getNextToken()");
        }
        */
        //printRcvrTable();
        
        if( _rcvrTimeTable.size() == 0 ) {
            // System.out.println("No receivers. Return from getNextToken()");
            return null;
        }
        
        RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeTable.first();
        ODReceiver lowestRcvr = (ODReceiver)triple.getReceiver();
        _currentTime = triple.getTime();
        _lastPort = (ODIOPort)triple.getReceiver().getContainer();
        
        if( name.equals("printer") ) {
            System.out.println("preparing to call get");
            printRcvrTable();
        }
        Token token = lowestRcvr.get();
        if( name.equals("printer") ) {
            System.out.println("just finished calling get");
        }
        
        if( token != null ) {
            /*
            if( name.equals("printer") ) {
                System.out.println(name+" returned a token: 1st non-null");
            }
            */
            // updateRcvrTable( triple );
            return token;
        } else {
            if( this.hasMinRcvrTime() ) {
                if( name.equals("printer") ) {
                    System.out.println(name+" has minimum receiver time.");
                }
                /*
                */
                // This means that there must be a different
                // receiver which has the minimum arc time
                // after the token was actually received -
                // recall that the receiver may have been
                // blocking and then received an arc time
                // that was not the minimum.
                
                return getNextToken();
                
            } else {
                if( name.equals("printer") ) {
                    System.out.println(name+" has no minimum receiver time.");
                }
                /*
                */
                // This means that multiple arcs have the 
                // the same minimum arc time. Find the arc
                // with the lowest time and priority.
                
                RcvrTimeTriple priorityTriple = getHighestPriorityTriple();
                lowestRcvr = (ODReceiver)priorityTriple.getReceiver();
                lowestRcvr.setSimultaneousIgnore(true);
                token = lowestRcvr.get();
                
                _lastPort = (ODIOPort)lowestRcvr.getContainer();
                
                if( token != null ) {
                    // updateRcvrTable( priorityTriple );
                    if( name.equals("printer") ) {
                        System.out.println(name+
                                " returned a token: 2nd non-null.");
                    }
                    /*
                    */
                    return token;
                } else {
                    if( name.equals("printer") ) {
                        System.out.println(name+ 
                                " minimum rcvrTime must have changed.");
                    }
                    /*
                    */
                    // This means that although originally there was 
                    // a receiver with highest priority, it must have
                    // blocked and upon receiving a token no longer
                    // had the minimum time. The result is that there 
                    // is another minimum.
                        
                    return getNextToken();
                }
            }
        }
        // System.out.println("Reached end of getNextToken()");
    }

    /** Return the port from which the last token was consumed.
     */
    public synchronized ODIOPort lastTokenFrom() {
        return _lastPort;
    }

    /** FIXME
     */
    public boolean postfire() {
        System.out.println(getName()+" is calling postfire()");
        return false;
    }
    
    /** FIXME: This is only for testing purposes.
     *  Remove this eventually
     */
    public void printRcvrTable() {
        System.out.println("Print "+getName()+"'s RcvrTable.");
        System.out.println("\tRcvrTable size = " + _rcvrTimeTable.size() );
        if( _rcvrTimeTable.size() == 0 ) {
            System.out.println("\tTable is empty");
        }
        for( int i = 0; i < _rcvrTimeTable.size(); i++ ) {
	    RcvrTimeTriple testTriple = (RcvrTimeTriple)_rcvrTimeTable.at(i);
	    Receiver testRcvr = testTriple.getReceiver(); 
            double time = testTriple.getTime();
            String testPort = testRcvr.getContainer().getName();
            String testString = "null";
            try {
            if( testRcvr.hasToken() && getName().equals("printer") ) {
                System.out.println("Printer -> hasToken() = "+testRcvr.hasToken() );
                Event testEvent = ((Event)((ODReceiver)testRcvr)._queue.get(0));
                StringToken testToken = (StringToken)testEvent.getToken();
                testString = testToken.stringValue();
            }
            } catch( IllegalActionException e ) {
                System.out.println("*****ERROR*****");
            }
            System.out.println("\t"+getName()+"'s Receiver " 
                    + i + " has a time of " +time+" and string: "+testString);
            // System.out.println("\tReceiver " + testPort + " is in the rcvrTimeTable.");
        }
    }

    /** Set the priorities of the receivers contained in the input 
     *  ports of this actor. This method does not require the 
     *  priorities of the ports to be set. 
     *  FIXME: If the port priorities have not been set, then this
     *         method deterministically sets up the receiver
     *         priorities given that Java's enumeration creation
     *         mechanism is deterministic.
     */
    public void setPriorities() throws IllegalActionException {
        LinkedList listOfPorts = new LinkedList();
        Enumeration enum = inputPorts();
	if( !enum.hasMoreElements() ) {
            // FIXME: Eventually remove the print statement 
            // System.out.println("No ports in enumeration");
            return;
	}
        
        //
        // First Order Input Ports According To Priority
        //
        while( enum.hasMoreElements() ) {
            // FIXME: Are enumerations created deterministically?
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
        
        // System.out.println("Number of Ports = " + listOfPorts.size());
        
        //
        // Now Set The Priorities Of Each Port's Receiver
        // And Initialize RcvrTable
        //
        int cnt = 0;
        int currentPriority = 0;
        while( cnt < listOfPorts.size() ) {
            ODIOPort port = (ODIOPort)listOfPorts.at(cnt);
            Receiver[][] rcvrs = port.getReceivers();
            for( int i = 0; i < rcvrs.length; i++ ) {
                for( int j = 0; j < rcvrs[i].length; j++ ) {
                    // System.out.println("Port "+port.getName()+": " + cnt +
                    //         "  rcvr["+i+"]["+j+"] priority = "+currentPriority);
                    ((ODReceiver)rcvrs[i][j]).setPriority(currentPriority); 
                    RcvrTimeTriple triple = new RcvrTimeTriple( 
                            (ODReceiver)rcvrs[i][j], _currentTime, 
                            currentPriority );
                    updateRcvrTable( triple );
                    currentPriority++;
                }
            }
            cnt++;
        }
    }

    /** 
     */
    public synchronized void updateRcvrTable(RcvrTimeTriple triple) {
        // System.out.println("Update ODActor RcvrTimeTable");
	_removeRcvrTable( triple );
	_addRcvrTriple( triple );
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods			   ////

    /** Add the specified RcvrTimeTriple based on the _Receiver_.
     *  This method must be called after _removeRcvrTable() if 
     *  the RcvrTimeTriple table already conatains the triple
     *  specified in the argument.
     */
    private void _addRcvrTriple(RcvrTimeTriple newTriple) {
        // System.out.println("Entered _addRcvrTriple");
        // printRcvrTable();
	int cnt = 0;
        boolean notAddedYet = true;

        if( _rcvrTimeTable.size() == 0 ) {
            /* These checks are just for debugging but they
               violate the synchronization lock hierarchy.
            String portName = newTriple.getReceiver().getContainer().getName();
            // System.out.println(portName + " inserted at position " + cnt);
            */
            _rcvrTimeTable.insertAt( 0, newTriple );
            // printRcvrTable();
            return;
        }
        
        // System.out.println("Preparing to add triple to table of size "
        //        + _rcvrTimeTable.size() );
	while( cnt < _rcvrTimeTable.size() ) {
	    RcvrTimeTriple triple = (RcvrTimeTriple)_rcvrTimeTable.at(cnt);
            
            /* These checks are just for debugging but they
               violate the synchronization lock hierarchy.
            String portName1 = triple.getReceiver().getContainer().getName();
            // System.out.println(portName1 + " being checked.");
            String portName2 = newTriple.getReceiver().getContainer().getName();
            // System.out.println(portName2 + " to be added.");
            
            //System.out.println(portName1+" has time of "+triple.getTime());
            //System.out.println(portName2+" has time of "+newTriple.getTime());
            */
            
	    if( newTriple.getTime() < triple.getTime() ) {
                /* These checks are just for debugging but they
                   violate the synchronization lock hierarchy.
                String portName = 
                        newTriple.getReceiver().getContainer().getName();
                // System.out.println(portName + " inserted at position " + cnt);
                */
	        _rcvrTimeTable.insertAt( cnt, newTriple );
		cnt = _rcvrTimeTable.size();
                // return;
                notAddedYet = false;
	    }
	    cnt++;
	}
        
        if( notAddedYet ) {
            _rcvrTimeTable.insertLast( newTriple );
        }
        
        // System.out.println("Additions are complete. Now check elements");
        // printRcvrTable();
    }
    
    /** Remove the specified RcvrTimeTriple based on the _Receiver_. 
     */
    private void _removeRcvrTable(RcvrTimeTriple triple) {
        // System.out.println("Entered _removeRcvrTriple");
        
        // printRcvrTable();
        
        Receiver rcvrToBeRemoved = triple.getReceiver();
        
	for( int cnt = 0; cnt < _rcvrTimeTable.size(); cnt++ ) {
	    RcvrTimeTriple nextTriple = (RcvrTimeTriple)_rcvrTimeTable.at(cnt);
	    Receiver nextRcvr = nextTriple.getReceiver(); 
            
            /* These checks are just for debugging but they
               violate the synchronization lock hierarchy.
            String portName1 = nextRcvr.getContainer().getName();
            // System.out.println(portName1 + " being checked.");
            String portName2 = rcvrToBeRemoved.getContainer().getName();
            // System.out.println(portName2 + " to be removed.");
            */
            
	    if( rcvrToBeRemoved == nextRcvr ) {
                /* These checks are just for debugging but they
                   violate the synchronization lock hierarchy.
                String portName = nextRcvr.getContainer().getName();
                // System.out.println(portName+" removed from position "+cnt);
                */
	        _rcvrTimeTable.removeAt( cnt );
		cnt = _rcvrTimeTable.size();
                // return;
	    }
	}
        
        // System.out.println("Removal is complete. Now check remaining elements.");
        
        // printRcvrTable();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The last port from which a token was consumed.
    private ODIOPort _lastPort;
    
    // The _rcvrTimeTable stores RcvrTimeTriples and is used to
    // order the receivers according to time and priority.
    private LinkedList _rcvrTimeTable;
    
    // The currentTime of this actor is equivalent to the minimum
    // rcvrTime of each input receiver. This value is updated in 
    // getNextToken().
    private double _currentTime = 0.0;


}




















