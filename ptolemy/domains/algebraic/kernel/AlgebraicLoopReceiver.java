/* The receiver for use with AlgebraicLoopDirector or any of its subclasses.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

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
 @version $Id$
 @since Ptolemy II 10.0
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
    @Override
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
     *  @param numberOfTokens Ignored in this base class.
     *  @return true.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return true;
    }

    /** Return true if the status is present.
     *  @return True if the receiver has a token.
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
    public boolean hasToken(int numberOfTokens)
            throws IllegalArgumentException {
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
     *  set the status of this receiver to present, and to contain the
     *  specified token. If the specified token is null, then set the status to
     *  absent.
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalActionException If a token
     *   is present and cannot be compared to the specified token.
     */
    @Override
    public void put(Token token) throws IllegalActionException {
        if (_isBreakVariable) {
            _updatedValue = token;
        } else {
            _token = token;
        }
    }

    /** Clear stored tokens.
     */
    @Override
    public void reset() {
        _updatedValue = null;
        _token = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If this receiver is a break variable, then return the stored
     *  updated value. Otherwise, return the current value.
     *  @return The token stored by
     */
    protected Token _getUpdatedValue() {
        if (_isBreakVariable) {
            return _updatedValue;
        }
        return _token;
    }

    /** Indicate to this receiver that it is a break variable and
     *  set its initial value.
     *  If this receiver is a break variable, then put() does not
     *  update the value of the receiver. Instead, the value provided
     *  to put() is stored to be retrieved by _getUpdatedValue().
     *  @param initialValue The initial value.
     */
    protected void _setInitialValue(Token initialValue) {
        _isBreakVariable = true;
        _token = initialValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The director governing this receiver. */
    private AlgebraicLoopDirector _director;

    /** Indicator that this receiver stores a break variable. */
    private boolean _isBreakVariable = false;

    /** Updated token. */
    private Token _updatedValue;

    /** The token held. */
    private Token _token = null;
}
