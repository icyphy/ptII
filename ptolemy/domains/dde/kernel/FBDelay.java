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
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// FBDelay
/**
FBDelay actors are used to prevent Zeno conditions caused by
cycles of null tokens in feedback topologies in DDE models. If a
FBDelay actor consumes a token (real or Null), it has the option
of producing an equivalent token on the output with an incremented 
time stamp value. Alternatively, the FBDelay actor will simply 
produce the token without altering the time stamp.
<P>
Two parameters - <I>nullDelay</I> and <I>realDelay</I> - are available
for determining whether an FBDelay actor increments the time stamp of 
produced output tokens. The default value of nullDelay (realDelay) is
true (false). If the nullDelay (realDelay) parameter is set to true, 
then the time stamps of NullTokens (real tokens) will be incremented as 
they pass through this actor. 
<P>
The delay value that is applied (given that one of the above parameters 
is true) is determined by the setDelay() and getDelay() methods. 
Conditional delay values can be made available by overriding the 
getDelay() method in derived classes.
<P>
FBDelay actors are effective for preventing Zeno conditions involving 
cycles of null tokens. If a DDE model has a feedback topology, a FBDelay 
actor should be added into the feedback loop. 
<P>
The delay value of a FBDelay actor must be wisely chosen. The delay 
value should be smaller than any other successive time stamp increment 
found in the DDE model. This means that if a particular model might 
have any two time stamps with time difference delta, then the delay 
value should be smaller than delta.
<P>

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
        _setVariables();
    }

    /** Construct a FBDelay with the specified workspace and
     *  no name.
     * @param workspace The workspace for this FBDelay.
     */
    public FBDelay(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
	super(workspace);
        _setVariables();
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
        _setVariables();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    public TypedIOPort input = null;
    public DDEIOPort output = null;
    
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
     * @returns The delay value of this actor.
     */
    public double getDelay() {
	return _delay;
    }

    /** Consume a single input token and produce an identical output
     *  token. If the input token is a NullToken and the nullDelay
     *  parameter is set to true, then produce an output NullToken to
     *  have a time stamp with a delay specified by getDelay(). Otherwise
     *  produce a NullToken that does not have a delayed time stamp value.
     *  If the *  input token is a real token and the realDelay parameter 
     *  is set to true, then produce an output real token to have a time 
     *  stamp with a delay specified by getDelay(). Otherwise produce a
     *  real token that does not have a delayed time stamp value.
     * @throws IllegalActionException If there is an error when
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
            if( token instanceof NullToken ) {
                if( delayNullVal ) {
	            output.send( 0, token, getCurrentTime() + getDelay() );
                } else {
	            output.send( 0, token, getCurrentTime() );
                }
            } else {
                if( delayRealVal ) {
	            output.send( 0, token, getCurrentTime() + getDelay() );
                } else {
	            output.send( 0, token, getCurrentTime() );
                }
            }
	}
    }

    /** Initialize this actor by setting all receivers so that
     *  they do not hide NullTokens.
     * @throws IllegalActionException If there is an error when
     *  when attempting to access the receivers of this actor.
     * @see ptolemy.domains.dde.kernel.NullToken
     * @see ptolemy.domains.dde.kernel.DDEReceiver
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
	output.send( 0, new Token(), TimedQueueReceiver.IGNORE );

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
     * @exception IllegalActionException Is not thrown in this class
     *  but may be thrown in derived classes.
     */
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    /** Set the delay value of this actor.
     * @params delay The delay value of this actor.
     */
    public void setDelay(double delay) {
	_delay = delay;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    protected double _delay = 4.0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _setVariables() throws IllegalActionException, 
    	    NameDuplicationException {
	input = new TypedIOPort(this, "input", true, false);
	output = new DDEIOPort(this, "output", false, true);
	input.setTypeEquals(Token.class);
	output.setTypeEquals(Token.class);
        
        nullDelay = 
                new Parameter(this, "nullDelay", new BooleanToken(true));
        realDelay = 
                new Parameter(this, "realDelay", new BooleanToken(false));
    }

}
