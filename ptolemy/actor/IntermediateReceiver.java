/* This actor implements a receiver that adds functionality to another receiver.

@Copyright (c) 2011-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor;

import java.util.List;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/** A receiver that delegates to another receiver all method calls except
 *  {@link #put(Token)} (and its variants), for which it delegates to a
 *  communication aspect. The delegated receiver and the communication aspect are
 *  specified as constructor arguments.
 *  <p>
 *  This can be used, for example, when multiple communication links share
 *  resources. The communication aspect can, for example, delay the delivery
 *  of tokens to the delegated receiver to take into account resource
 *  availability. It could also be used to make a centralized record
 *  of various communications.
 *  <p>
 *  Subclasses of this receiver may also intervene on method calls other
 *  than put().
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class IntermediateReceiver extends AbstractReceiver {

    /** Construct an intermediate receiver with no container that wraps the
     *  specified receiver using the specified communication aspect.
     *  @param aspect The communication aspect that receives tokens received by this receiver.
     *  @param receiver The receiver wrapped by this intermediate receiver.
     */
    public IntermediateReceiver(CommunicationAspect aspect, Receiver receiver) {
        _receiver = receiver;
        communicationAspect = aspect;
    }

    /** Construct an intermediate receiver with no container that wraps the
     *  specified receiver using the specified communication aspect.
     *  @param aspect The communication aspect that receives tokens received by this receiver.
     *  @param receiver The receiver wrapped by this intermediate receiver.
     *  @param port The port wrapped by this intermediate receiver
     */
    public IntermediateReceiver(CommunicationAspect aspect, Receiver receiver,
            IOPort port) {
        _receiver = receiver;
        communicationAspect = aspect;
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** communication aspect that receives tokens from this receiver. */
    public CommunicationAspect communicationAspect;

    /** The source actor that sent a token to this receiver. */
    public Actor source;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the communication aspect and the receiver that we delegate to.
     */
    @Override
    public void clear() throws IllegalActionException {
        communicationAspect.reset();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException Always thrown in this base class.
     */
    @Override
    public List<Token> elementList() throws IllegalActionException {
        return _receiver.elementList();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @exception NoTokenException If the delegated receiver throws it.
     */
    @Override
    public Token get() throws NoTokenException {
        return _receiver.get();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     *  @return The port containing the internal receiver.
     *  @see #setContainer(IOPort)
     */
    @Override
    public IOPort getContainer() {
        return _receiver.getContainer();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    @Override
    public boolean hasRoom() {
        return _receiver.hasRoom();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return _receiver.hasRoom(numberOfTokens);
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    @Override
    public boolean hasToken() {
        return _receiver.hasToken();
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    @Override
    public boolean hasToken(int numberOfTokens) {
        return _receiver.hasToken(numberOfTokens);
    }

    /** Delegate to the internal receiver and return whatever it returns.
     */
    @Override
    public boolean isKnown() {
        return _receiver.isKnown();
    }

    /** Forward the specified token to communication aspect specified in
     *  the constructor.
     */
    @Override
    public void put(Token token) throws NoRoomException, IllegalActionException {
        communicationAspect.sendToken(this, _receiver, token);
        ((Actor) _receiver.getContainer().getContainer()).getDirector()
        .notifyTokenSentToCommunicationAspect();
    }

    /** Reset this receiver to its initial state, which in this base
     *  class is the same as calling clear().
     *  @exception IllegalActionException If reset() is not supported by
     *   the domain.
     */
    @Override
    public void reset() throws IllegalActionException {
        super.reset();
        _receiver.reset();
    }

    /** Set the container of the internal receiver.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort. Not thrown in this base class,
     *   but may be thrown in derived classes.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(IOPort port) throws IllegalActionException {
        _receiver.setContainer(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Target receiver that is wrapped by this intermediate receiver.  */
    public Receiver _receiver;

    /** The port. */
    protected IOPort _port;
}
