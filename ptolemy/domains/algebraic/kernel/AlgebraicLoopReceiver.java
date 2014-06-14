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

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// AlgebraicLoopReceiver

/**
 The receiver for use with AlgebraicLoopDirector or any of its subclasses.
 This receiver can be either <i>present</i> or <i>absent</i>.
 If it is present, then it has a token, which provides a value.
 This receiver has capacity 1, honors the defaultValue parameter, and its
 value can be overwritten or cleared at any time (made absent).
 <p>
 At first, an instance of this class has status absent, unless it has
 a defaultValue parameter.
 The clear() method makes the status absent.
 The put() method makes the status present, and provides a value.
 The reset() method reverts the status to absent or to the default value
 if there is one.
 <p>
 The isKnown() method and hasRoom() methods always return true.
 <p>

 @author Edward A. Lee
 @version $Id: AlgebraicLoopReceiver.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class AlgebraicLoopReceiver extends AbstractReceiver {

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
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the status of this receiver to be absent.
     */
	@Override
    public void clear() throws IllegalActionException {
        _token = null;
    }
	
    /** Return the contained Token. If there is none, throw an exception.
     *  @return The token contained by this receiver.
     *  @exception NoTokenException If this receiver is absent.
     */
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(_director,
                    "AlgebraicLoopReceiver: Attempt to get data from an empty receiver.");
        }
        return _token;
    }

    /** Return true.
     *  @return true.
     */
	@Override
    public boolean hasRoom() {
        return true;
    }

    /** Return true.
     *  @return true.
     */
	@Override
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

	/** Return true if the status is present.
	 *  @return True if the recevier has a token.
	 */
	@Override
	public boolean hasToken() {
        return _token != null;
	}

    /** Return true if the argument is 1 and this mailbox is not empty,
     *  and otherwise return false.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and this mailbox is not empty.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
	@Override
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasToken() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return _token != null;
        }

        return false;
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
	@Override
    public void put(Token token) throws IllegalActionException {
        _previousToken = _token;
        _token = token;
    }

    /** Override the base class to set the token to value of
     *  containing port's defaultValue parameter, if there is one.
     */
	@Override
    public void reset() {
        _previousToken = null;
        IOPort container = getContainer();
        if (container == null) {
            _token = null;
        } else {
        	Parameter defaultValue = container.defaultValue;
        	if (defaultValue == null) {
                _token = null;
        	} else {
        		try {
        			_token = defaultValue.getToken();
        		} catch (IllegalActionException e) {
        			// Unfortunately, to make this a compile-time exception, we have
        			// to unravel all the way to Director. Don't do this now.
        			// Too big a change. So we throw a runtime exception.
        			throw new InternalErrorException(getContainer(), e, "Failed to evaluate defaultValue parameter.");
        		}
        	}
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return the previous token.
     *  @return The token before the most recent call to put(), or null
     *   if there is none since the last reset().
     */
    protected Token _getPreviousToken() {
        return _previousToken;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The director governing this receiver. */
    private AlgebraicLoopDirector _director;
    
    /** Previously recorded token. */
    private Token _previousToken;
    
    /** The token held. */
    private Token _token = null;
}
