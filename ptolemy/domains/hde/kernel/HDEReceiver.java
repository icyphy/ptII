/* Hardware Discrete Event (HDE) domain receiver.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hde.kernel;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// HDEReceiver

/** An implementation of the ptolemy.actor.Receiver interface for the
DE domain.  Tokens that are put into this receiver logically have time
stamps. If the time stamp is not explicitly given using the setDelay()
method, then it is assumed to be the current time (which is maintained
by the director).  The put() method sends the specified token to the
director, which returns it to this receiver (via the protected method
_triggerEvent()) when current time matches the time stamp of the
token. The get() method returns only tokens that the director has so
returned. Thus, when a token is put into the receiver using put(), it
does not become immediately available to the get() method.

<p>By default, the time stamp of a token is the current time of the
director when put() is called. To specify a time stamp in the future,
call setDelay() prior to calling put(). This should be done in a
synchronized manner, since there could be multiple thread running in
this domain.

<p>Before firing an actor, the director is expected to put at least one
token into at least one of the receivers contained by the actor.

@author Steve Neuendorffer, Jim Armstrong
@version $Id$
@since Ptolemy II 2.0
*/
public class HDEReceiver extends DEReceiver {

    /** Construct an empty DEReceiver with no container.
     */
    public HDEReceiver() {
        super();
    }

    /** Construct an empty HDEReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public HDEReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from the receiver.  The token returned is one that
     *  was put in the receiver with a time stamp equal to or earlier than
     *  the current time.  Note that there might be multiple such
     *  tokens in the receiver. In that case, FIFO behaviour is used with
     *  respect to the put() method. If there is no such token, throw an
     *  exception. This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @return A token.
     *  @exception NoTokenException If there are no more tokens. This is
     *   a runtime exception, so it need not be declared explicitly.
     */

    public synchronized Token get() {
        return _token;
    }

    /** Return true if there are tokens available to the get() method.
     *  @return True if there are more tokens.
     */
    public final boolean hasToken() {
        return _token != null;
    }

    /** Return true if there are <i>numberOfTokens</i>
     *  tokens tokens available to the get() method.
     *  @return True if there are <i>numberOfTokens</i> tokens available.
     */
    public final boolean hasToken(int numberOfTokens) {
        return _token != null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make a token available to the get() method.
     *  Normally, only a director will call this method. It calls it
     *  when current time matches the time stamp of the token, i.e.
     *  when the delay specified by setDelay() has elapsed.
     *  @param token The token to make available to get().
     */
    protected void _triggerEvent(Token token) {
        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //This is  stored token value.
    private Token  _token;
}
