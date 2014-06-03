/* The receiver for use with AlgebraicLoopDirector or any of its subclasses.

 Copyright (c) 2006-2013 The Regents of the University of California.
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

 */
package ptolemy.domains.algebraic.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.sched.FixedPointReceiver;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// AlgebraicLoopReceiver

/**
 The receiver for use with AlgebraicLoopDirector or any of its subclasses.
 This receiver has capacity 1 and honors the defaultValue parameter.
 The status of this receiver can be either <i>known</i> or <i>unknown</i>.
 If it is known, then it can be either <i>present</i> or <i>absent</i>.
 If it is present, then it has a token, which provides a value.
 <p>
 At first, an instance of this class has status unknown, unless it has
 a defaultValue parameter.
 The clear() method makes the status known and absent.
 The put() method makes the status known and present, and provides a value.
 The reset() method reverts the status to unknown, or to the default value
 if there is one.
 Unlike the base class, the value in this port can change during an
 iteration.
 <p>
 FIXME: What about changing from present to absent
 or vice versa? Currently, this receiver simply allows these changes.
 <p>
 The isKnown() method returns true if the receiver has status known.
 The hasRoom() method always returns true.
 If the receiver has a known status, the hasToken() method returns true
 if the receiver contains a token. If the receiver has an unknown status,
 the hasToken() method will throw an InvalidStateException.
 <p>

 @author Edward A. Lee
 @version $Id: AlgebraicLoopReceiver.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class AlgebraicLoopReceiver extends FixedPointReceiver {

    /** Construct an AlgebraicLoopReceiver with unknown status.
     *  This constructor does not need a director.
     */
    public AlgebraicLoopReceiver() {
        this(null);
    }

    /** Construct an AlgebraicLoopReceiver with unknown status.
     *  @param director The director of this receiver.
     */
    public AlgebraicLoopReceiver(AlgebraicLoopDirector director) {
        super();
        reset();
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the status of this receiver to be known and absent.
     */
    public void clear() throws IllegalActionException {
    	_token = null;
    	_known = true;
    	if (_director != null) {
    		((AlgebraicLoopDirector)_director)._receiverChanged();
    	}
    }

    /** Return true.
     *  @return true.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true.
     *  @return true.
     */
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** If the specified token is non-null, then
     *  set the status of this receiver to known and present, and to contain the
     *  specified token. If the specified token is null, then set the status to
     *  be known and absent (by calling {@link #clear()}).
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalActionException If a token
     *   is present and cannot be compared to the specified token.
     */
    public void put(Token token) throws IllegalActionException {
        _previousToken = _token;
        if (token == null) {
            clear();
            return;
        }
        _token = token;
        if (_director != null && !_known) {
        	// Notify the base class that this receiver is now known.
        	((AlgebraicLoopDirector)_director)._receiverChanged();
        }
        _known = true;
    }

    /** Override the base class to set the token to value of
     *  containing port's defaultValue parameter, if there is one.
     */
    public void reset() {
    	_previousToken = null;
    	IOPort container = getContainer();
    	if (container == null) {
    		super.reset();
    		return;
    	}
    	Parameter defaultValue = container.defaultValue;
    	if (defaultValue == null) {
    		super.reset();
    		return;
    	}
        try {
			_token = defaultValue.getToken();
		} catch (IllegalActionException e) {
			// Unfortunately, to make this a compile-time exception, we have
			// to unravel all the way to Director. Don't do this now.
			// Too big a change. So we throw a runtime exception.
			throw new InternalErrorException(getContainer(), e, "Failed to reset receiver.");
		}
        _known = (_token != null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return the current token without doing an error checks.
     *  @return The current token.
     */
    protected Token _getCurrentToken() {
    	return _token;
    }

    /** Return the previous token.
     *  @return The token before the most recent call to put(), or null
     *   if there is none since the last reset().
     */
    protected Token _getPreviousToken() {
    	return _previousToken;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Previously recorded token. */
    private Token _previousToken;
}
