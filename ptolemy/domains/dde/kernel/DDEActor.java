/* An optional base class for DDE actors.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// DDEActor
/**
An optional base class for DDE actors. DDEActors are intended to
execute as autonomous processes that maintain a distributed notion
of time. In an DDE model, each actor is controlled by a unique
DDEThread. Each DDEThread maintains its actor's local notion of
time. Local time information is dependent on the time stamps
associated with tokens that are consumed by an actor. More
precisely, an actor's local notion of time is equivalent to the
maximum time stamp of all tokens that the actor has consumed.
Constraints on the consumption of tokens are described in the
documentation for DDEThread. Note that consumed tokens may include
NullTokens. A NullToken is a subclass of Token that is communicated
solely for the purpose of advancing the local notion of time of the
actor that consumes the NullToken.
<P>
The DDE model of computation supports typed, polymorphic actors and
does not require this base class for implementation; this class is
purely optional. This class provides convenient syntactic shortcuts
for developing actors that operate according to DDE semantics.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEThread
@see ptolemy.domains.dde.kernel.NullToken
*/
public class DDEActor extends TypedAtomicActor {

    /** Construct a DDEActor with no container and a name that
     *  is an empty string.
     */
    public DDEActor() {
        super();
    }

    /** Construct a DDEActor with the specified workspace and a
     *  name that is an empty string.
     * @param workspace The workspace for this DDEActor.
     */
    public DDEActor(Workspace workspace) {
	super(workspace);
    }

    /** Construct a DDEActor with the specified container and name.
     *  The name must be unique with respect to the container or an
     *  exception is thrown.
     * @param container The container of this DDEActor.
     * @param name The name of this DDEActor.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public DDEActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current time of this actor. The current time
     *  is equal to the time stamp associated with the token most
     *  recently consumed by any of the receivers contained by
     *  this DDEActor. If the current thread accessing this method
     *  is not an instance of DDEThread, then return the cached
     *  current time value.
     * @return The current time of this DDEActor.
     */
    public double getCurrentTime() {
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	    _currentTime = timeKeeper.getCurrentTime();
	}

	return _currentTime;
    }

    /** Return the last port through which a token was consumed by
     *  this actor. If no tokens have ever been consumed through any
     *  ports of this actr, then return null.
     * @return TypedIOPort The last port through which a token was
     *  consumed by this actor.
     */
    public TypedIOPort getLastPort() {
	return _lastPort;
    }

    /** Return a non-NullToken from the receiver that has the minimum,
     *  non-negative receiver time of all receivers contained by this
     *  actor. If there exists a set of multiple receivers that share
     *  a common minimum receiver time, then return the token contained
     *  by the highest priority receiver within this set. If this actor
     *  contains no receivers then return null. This method may block
     *  as it calls several blocking methods.
     * @return Return a non-NullToken that has the minimum, nonnegative
     *  receiver time of all receivers contained by this actor.
     */
    public Token getNextToken() throws IllegalActionException {
        Token token = _getNextInput();
        if( token instanceof NullToken ) {
            return getNextToken();
	}
        return token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   package friendly methods		   ////

    /** Return a token from the receiver that has the minimum
     *  receiver time of all receivers contained by this actor.
     *  The returned token will have the lowest time stamp of
     *  all pending tokens for this actor. If there exists a
     *  set of multiple receivers that share a common minimum
     *  receiver time, then return the token contained by the
     *  highest priority receiver within this set. If this actor
     *  contains no receivers then return null.
     *  <P>
     *  The primary difference between this method and getNextToken()
     *  is that this method returns all types of tokens including
     *  NullTokens while getNextToken only returns real tokens.
     * @return The token with the smallest time stamp of all tokens
     *  contained by this actor. If multiple tokens share the smallest
     *  time stamp this token will come from the highest priority
     *  receiver that has the minimum receiver time. If all receivers
     *  have expired then throw a TerminateProcessException.
     * @see ptolemy.domains.dde.kernel.DDEReceiver
     * @see ptolemy.domains.dde.kernel.TimeKeeper
     * @see ptolemy.domains.dde.kernel.DDEThread
     */
    Token _getNextInput() throws IllegalActionException {
	String calleeName = getName();
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
            DDEReceiver lowestRcvr = (DDEReceiver)timeKeeper.getFirstRcvr();


	    // System.out.println("_getNextInput() called on "+calleeName);


            if( lowestRcvr.hasToken() ) {
                _lastPort = (TypedIOPort)lowestRcvr.getContainer();
                return lowestRcvr.get();
            } else {
                return _getNextInput();
            }
	} else {
	    throw new IllegalActionException(this, "Illegal attempt "
	            + "to execute a DDEActor by a non-DDEThread.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _currentTime = 0.0;
    private TypedIOPort _lastPort = null;

}
