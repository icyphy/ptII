/* FBDelay actors are used to prevent Zeno conditions caused by
cycles of null tokens in feedback topologies in DDE models.

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
@AcceptedRating Red (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// FBDelay
/**
FBDelay actors are used to prevent Zeno conditions caused by
cycles of null tokens in feedback topologies in DDE models. If a
FBDelay actor consumes a real token, it will pass the token
through without altering the time stamp associated with the token.
If a FBDelay actor encounters a Null token it will pass through
the null token with an incremented time stamp value.
<P>
FBDelay actors are effective for preventing Zeno conditions
involving cycles of null tokens. If a DDE model has a feedback topology,
a FBDelay actor should be added into the feedback loop. The
addition of such an actor will not alter any real tokens but will alter
null tokens so that livelock cycles of null tokens with identical time
stamps can be avoided.
<P>
The delay value of a FBDelay actor must be wisely chosen. The
delay value should be smaller than any other successive time stamp
increment found in the DDE model. This means that if a particular model
might have any two time stamps with time difference delta, then the
delay value should be smaller than delta.
<P>
FBDelay actors do not prevent Zeno conditions involving real
tokens.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.NullToken
*/
public class FBDelay extends DDEActor {

    /** Construct a FBDelay with no container and a name that
     *  is an empty.
     */
    public FBDelay()
            throws IllegalActionException, NameDuplicationException {
        super();
	input = new TypedIOPort(this, "input", true, false);
	output = new DDEIOPort(this, "output", false, true);
	input.setTypeEquals(Token.class);
	output.setTypeEquals(Token.class);
    }

    /** Construct a FBDelay with the specified workspace and
     *  no name.
     * @param workspace The workspace for this FBDelay.
     */
    public FBDelay(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
	super(workspace);
	input = new TypedIOPort(this, "input", true, false);
	output = new DDEIOPort(this, "output", false, true);
	input.setTypeEquals(Token.class);
	output.setTypeEquals(Token.class);
    }

    /** Construct a FBDelay with the specified container and
     *  name.
     * @param container The container of this FBDelay.
     * @param name The name of this FBDelay.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public FBDelay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	input = new TypedIOPort(this, "input", true, false);
	output = new DDEIOPort(this, "output", false, true);
	input.setTypeEquals(Token.class);
	output.setTypeEquals(Token.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    public TypedIOPort input = null;
    public DDEIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public double getDelay() {
	return _delay;
    }

    /**
     */
    public void setDelay(double delay) {
	_delay = delay;
    }

    /** Consume a single input token and produce an identical output
     *  token. If the input token is a NullToken, then advance the
     *  current time of this actor by DELAY amount. Otherwise, do
     *  not advance the current time of this actor.
     * @throws IllegalActionException If there is an error when
     *  sending the output token or setting the current time.
     */
    public void fire() throws IllegalActionException {
	// System.out.println("fire() of FBDelay");
	Token token = _getNextInput();
	/*
	System.out.println("fire() of FBDelay - _getNextInput()."
		+ "  current time = " + getCurrentTime());
	*/
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper keeper = ((DDEThread)thread).getTimeKeeper();
	    output.send( 0, token, getCurrentTime() + getDelay() );
	}
	// System.out.println("end of fire() of FBDelay");
    }

    /**
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
	System.out.println("Beginning initialize()");
	output.send( 0, new Token(), TimedQueueReceiver.IGNORE );
	System.out.println("Finished initialize()");

	Receiver[][] rcvrs = input.getReceivers();
	for( int i = 0; i < rcvrs.length; i++ ) {
	    for( int j = 0; j < rcvrs[i].length; j++ ) {
		DDEReceiver rcvr = (DDEReceiver)rcvrs[i][j];
		rcvr.hideNullTokens(false);
	    }
	}
    }

    /** Continue execution of this actor by informing the DDEThread
     *  which controls it to continue iterations.
     * @return True to indicate that future execution can occur.
     * @exception IllegalActionException Not thrown in this class.
     *  May be thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // FIXME: I need to use parameters here.
    protected double _delay = 4.0;
    protected int _cntr = 0;

}
