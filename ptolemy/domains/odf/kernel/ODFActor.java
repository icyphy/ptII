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
The base class for ODF actors. ODFActors are intended to execute as 
autonomous processes that maintain a distributed notion of time. In
an ODF model, each actor is controlled by a unique ODFThread. Each
ODFThread maintains its actor's local notion of time. Local time
information is dependent on the time stamps associated with tokens
that are consumed by an actor. More precisely, an actor's local
notion of time is equivalent to the maximum time stamp of all tokens 
that the actor has consumed. Constraints on the consumption of tokens 
are described in the documentation for ODFThread. Note that consumed 
tokens may include NullTokens. A NullToken is a subclass of Token
that is communicated solely for the purpose of advancing the local
notion of time of the actor that receives the NullToken.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.ODFThread
@see ptolemy.domains.odf.kernel.NullToken
*/
public class ODFActor extends AtomicActor {

    /** Construct an ODFActor with no container and no name.
     */
    public ODFActor() {
        super();
    }
    
    /** Construct an ODFActor with the specified workspace and no name.
     * @param workspace The workspace for this ODFActor.
     */
    public ODFActor(Workspace workspace) {
	super(workspace);
    }

    /** Construct an ODFActor with the specified container and name.
     * @param container The container of this ODFActor.
     * @param name The name of this ODFActor.
     */
    public ODFActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor. The current time is
     *  equal to the time stamp associated with the token most recently
     *  consumed by one of the receivers contained by this ODFActor. 
     *  If the current thread accessing this method is not an instance
     *  of ODFThread, then return the cached current time valu.
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
    
    /** Return a non-NullToken from the receiver that has the minimum,
     *  nonegative rcvrTime of all receivers contained by this actor.
     * @return Return a non-NullToken that has the minimum, nonnegative 
     *  rcvrTime of all receivers contained by this actor.
     */
    public Token getNextToken() {
        Token token = getNextInput(); 
        if( token instanceof NullToken ) {
	    System.out.println(getName()+": got a NullToken "
                    + "from getNextInput()");
            return getNextToken();
	}
        return token;
    }
    
    /** Return a token from the receiver that has the minimum rcvrTime
     *  of all receivers contained by this actor. The returned token will 
     *  have the lowest time stamp of all pending tokens for this actor. 
     *  If there exists a set of multiple receivers that share a common 
     *  minimum rcvrTime, then return the token contained by the highest 
     *  priority receiver within this set. If this actor contains no
     *  receivers then return null.
     * @return The token with the smallest time stamp of all tokens
     *  contained by this actor. If multiple tokens share the smallest 
     *  time stamp this token will come from the highest priority 
     *  receiver that has the minimum rcvrTime. If all receivers have 
     *  expired then throw a TerminateProcessException. 
     * @see TimedQueueReceiver
     * @see ODFReceiver
     * @see ODFThread
     */
    public Token getNextInput() {
	Thread thread = Thread.currentThread();
	ODFThread odfthread = null;
        ODFReceiver lowestRcvr = null;
	if( thread instanceof ODFThread ) {
	    odfthread = (ODFThread)thread;
            lowestRcvr = odfthread.getFirstRcvr();
	} else {
	    System.err.println("Error: Non-ODFThread controlling "
                    +getName());
	}

	if( lowestRcvr.hasToken() ) {
	    return lowestRcvr.get();
	} else {
	    return getNextInput();
	}
    }

    /** Prepare to cease iterations of this actor. Notify actors which
     *  are connected downstream of this actor's cessation. Return false
     *  to indicate that future execution can not occur.
     * @return False to indicate that future execution can not occur.
     * @exception IllegalActionException Not thrown in this class. May be
     *  thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Print the contents of the RcvrTimeTriple list contained by 
     *  this actor. Use this method for testing purposes only.
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
	    RcvrTimeTriple testTriple = 
                    (RcvrTimeTriple)_rcvrTimeList.at(i);
	    Receiver testRcvr = testTriple.getReceiver(); 
            double time = testTriple.getTime();
            String testPort = testRcvr.getContainer().getName();
            String testString = "null";
            String testString2 = "null";
            if( getName().equals("printer") ) {
		System.out.println("   Printer -> size() = "
                        +((ODFReceiver)testRcvr)._queue.size());
		if( ((ODFReceiver)testRcvr)._queue.size() > 1 ) {
                    Event testEvent2 = 
		            ((Event)((ODFReceiver)testRcvr)._queue.get(1));
                    StringToken testToken2 = 
		            (StringToken)testEvent2.getToken();
		    testString2 = testToken2.stringValue();
                    System.out.println("\t"+getName()+"'s Receiver "+i+ 
		            " has a 2nd time of "+testEvent2.getTime()+
			    " and string: ");
		}
		if( ((ODFReceiver)testRcvr)._queue.size() > 0 ) {
                    Event testEvent = 
                            ((Event)((ODFReceiver)testRcvr)._queue.get(0));
                    StringToken testToken = 
                            (StringToken)testEvent.getToken();
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
     */

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
    
    /** Remove the specified RcvrTimeTriple that contains the same
     *  Receiver as that contained by the RcvrTimeTriple passed as
     *  a parameter.
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

    // The currentTime of this actor is equivalent to the minimum
    // positive rcvrTime of each input receiver. 
    private double _currentTime = 0.0;

}




















