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
<P>
The ODF model of computation supports typed, polymorphic actors and 
does not require this base class for implementation. Nevertheless, 
this class provides useful syntactic conveniences for developing ODF 
models. 

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
     *  If there exists a set of multiple receivers that share a common 
     *  minimum rcvrTime, then return the token contained by the highest 
     *  priority receiver within this set. If this actor contains no
     *  receivers then return null.
     * @return Return a non-NullToken that has the minimum, nonnegative 
     *  rcvrTime of all receivers contained by this actor.
     */
    public Token getNextToken() {
        Token token = _getNextInput(); 
        if( token instanceof NullToken ) {
	    System.out.println(getName()+": got a NullToken "
                    + "from _getNextInput()");
            return getNextToken();
	}
        return token;
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
    ////                        private methods                    ////
    
    /** Return a token from the receiver that has the minimum rcvrTime
     *  of all receivers contained by this actor. The returned token 
     *  will have the lowest time stamp of all pending tokens for this 
     *  actor. If there exists a set of multiple receivers that share 
     *  a common minimum rcvrTime, then return the token contained by 
     *  the highest priority receiver within this set. If this actor 
     *  contains no receivers then return null.
     * @return The token with the smallest time stamp of all tokens
     *  contained by this actor. If multiple tokens share the smallest 
     *  time stamp this token will come from the highest priority 
     *  receiver that has the minimum rcvrTime. If all receivers have 
     *  expired then throw a TerminateProcessException. 
     * @see ptolemy.domains.odf.kernel.TimedQueueReceiver
     * @see ptolemy.domains.odf.kernel.ODFReceiver
     * @see ptolemy.domains.odf.kernel.ODFThread
     */
    private Token _getNextInput() {
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
	    return _getNextInput();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The currentTime of this actor is equivalent to the minimum
    // positive rcvrTime of each input receiver. 
    private double _currentTime = 0.0;

}




