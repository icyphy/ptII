/* FeedBackDelay actors are used to add delay to feedback topologies.

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
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// FeedBackDelay
/**
FeedBackDelay actors are used to add delay to feedback topologies.
If a FeedBackDelay actor consumes a token (real or Null), it has the
option of producing an equivalent token on the output with an
incremented time stamp value. Alternatively, the FeedBackDelay actor
will simply produce the token without altering the time stamp.
<P>
Two parameters - <I>nullDelay</I> and <I>realDelay</I> - are 
available for determining whether an FeedBackDelay actor 
increments the time stamp of produced output tokens. The default 
value of nullDelay (realDelay) is true (false). If the nullDelay 
(realDelay) parameter is set to true, then the time stamps of 
NullTokens (real tokens) will be incremented as they pass through 
this actor.
<P>
The delay value that is applied (given that one of the above
parameters is true) is determined by the setDelay() and getDelay()
methods. More elaborate delay values can be made available by
overriding the getDelay() method in derived classes.
<P>
FeedBackDelay actors are effective for preventing Zeno conditions 
involving cycles of null tokens. If a DDE model has a feedback 
topology, a FeedBackDelay actor should be added into the feedback loop.
<P>
The delay value of a FeedBackDelay actor must be wisely chosen. The 
delay value should be smaller than any other successive time stamp 
increment found in a given DDE model. This means that if a particular 
model might have any two time stamps with time difference delta, then 
the delay value should be smaller than delta.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.NullToken
*/
public class FeedBackDelay extends DDEActor {

    /** Construct a FeedBackDelay with no container and a name that
     *  is an empty string.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public FeedBackDelay()
            throws IllegalActionException, NameDuplicationException {
        super();
        _setVariables();
    }

    /** Construct a FeedBackDelay with the specified workspace and
     *  a name that is an empty string.
     * @param workspace The workspace for this FeedBackDelay.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public FeedBackDelay(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
	super(workspace);
        _setVariables();
    }

    /** Construct a FeedBackDelay with the specified container and
     *  name.
     * @param container The container of this FeedBackDelay.
     * @param name The name of this FeedBackDelay.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public FeedBackDelay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _setVariables();
        
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    public TypedIOPort input = null;
    public TypedIOPort output = null;

    /** The boolean parameter that indicates whether a delay value
     *  will be added to the time stamp of null tokens that are
     *  produced by this actor. This parameter defaults to true.
     */
    public Parameter nullDelay;

    /** The boolean parameter that indicates whether a delay value
     *  will be added to the time stamp of real tokens that are
     *  produced by this actor. This parameter defaults to false.
     */
    public Parameter realDelay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the delay value of this actor.
     * @return The delay value of this actor.
     */
    public double getDelay() {
	return _delay;
    }

    /** Consume a single input token and produce an identical output
     *  token. If the input token is a NullToken and the nullDelay
     *  parameter is set to true, then produce an output NullToken to
     *  have a time stamp with a delay specified by getDelay(). 
     *  Otherwise produce a NullToken that does not have a delayed 
     *  time stamp value. If the input token is a real token and the 
     *  realDelay parameter is set to true, then produce an output 
     *  real token to have a time stamp with a delay specified by 
     *  getDelay(). Otherwise produce a real token that does not have 
     *  a delayed time stamp value.
     * @exception IllegalActionException If there is an error when
     *  sending the output token or setting the current time.
     */
    public void fire() throws IllegalActionException {
	Token token = _getNextInput();
        boolean delayNullVal =
            ((BooleanToken)nullDelay.getToken()).booleanValue();
        boolean delayRealVal =
            ((BooleanToken)realDelay.getToken()).booleanValue();
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
            DDEThread ddeThread = (DDEThread)thread;
            if( token instanceof NullToken ) {
                if( delayNullVal ) {
                    _sendOutToken( token, getCurrentTime() + getDelay() );
                } else {
                    _sendOutToken( token, getCurrentTime() );
                }
            } else {
                if( delayRealVal ) {
                    _sendOutToken( token, getCurrentTime() + getDelay() );
                } else {
                    _sendOutToken( token, getCurrentTime() );
                }
            }
	}
    }

    /** Initialize this actor by setting all receivers so that
     *  they do not hide NullTokens.
     * @exception IllegalActionException If there is an error when
     *  when attempting to access the receivers of this actor.
     * @see ptolemy.domains.dde.kernel.NullToken
     * @see ptolemy.domains.dde.kernel.DDEReceiver
     */
    public void initialize() throws IllegalActionException {
	super.initialize();

        Receiver[][] rcvrs = output.getRemoteReceivers();
	for( int i = 0; i < rcvrs.length; i++ ) {
	    for( int j = 0; j < rcvrs[i].length; j++ ) {
            	DDEReceiver rcvr = (DDEReceiver)rcvrs[i][j];
                rcvr.put( new Token(), PrioritizedTimedQueue.IGNORE );
            }
        }


	rcvrs = input.getReceivers();
	for( int i = 0; i < rcvrs.length; i++ ) {
	    for( int j = 0; j < rcvrs[i].length; j++ ) {
		DDEReceiver rcvr = (DDEReceiver)rcvrs[i][j];
		rcvr.hideNullTokens(false);
	    }
	}
    }

    /** Set the delay value of this actor.
     * @params delay The delay value of this actor.
     */
    public void setDelay(double delay) {
	_delay = delay;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected double _delay = 4.0;
    
    private String _name;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Syntactic sugar for sending out tokens without
     *  depending on IOPort.send().
     */
    private void _sendOutToken(Token token, double time) {
        Receiver[][] rcvrs =
            (Receiver[][])output.getRemoteReceivers();
	for( int i = 0; i < rcvrs.length; i++ ) {
	    for( int j = 0; j < rcvrs[i].length; j++ ) {
            	DDEReceiver rcvr = (DDEReceiver)rcvrs[i][j];
                rcvr.put( token, time );
            }
        }
    }

    /** Syntactic sugar for initializing parameters.
     */
    private void _setVariables() throws IllegalActionException,
    	    NameDuplicationException {
	input = new TypedIOPort(this, "input", true, false);
	output = new TypedIOPort(this, "output", false, true);
	input.setTypeEquals(BaseType.GENERAL);
	output.setTypeEquals(BaseType.GENERAL);

        nullDelay = new
            Parameter(this, "nullDelay", new BooleanToken(true));
        realDelay = new
            Parameter(this, "realDelay", new BooleanToken(false));
    }

}
