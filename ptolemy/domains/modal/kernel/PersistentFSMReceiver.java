/* A Token holder that always contains exactly one token.

 Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PersistentFSMReceiver

/**
 A Token holder that always contains exactly one token. An
 initial token is provided.

 @author Patricia Derler
 @version $Id: FSMReceiver.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class PersistentFSMReceiver extends AbstractReceiver {

    /** Construct an empty receiver with no container.
     *  The initial status is unknown.
     */
    public PersistentFSMReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     *  The initial status is unknown.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public PersistentFSMReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void clear() { 
    	// do nothing
    }

    /** Return a list with the token currently in the receiver, or
     *  an empty list if there is no such token.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException If the status is unknown.
     */
    public List<Token> elementList() throws IllegalActionException {
        List<Token> result = new LinkedList<Token>();
        result.add(_token);
        return result;
    }

    /** Get the contained Token.  If there is none, throw an exception.
     *  The token is not removed. It can be repeatedly read.
     *  @return The token contained by this receiver.
     *  @exception NoTokenException If this receiver is empty or unknown
     */
    public Token get() throws NoTokenException {
    	System.out.println("get " + this.getContainer() + " " + this + " " + _token);
        return _token;
    }

    /** If the argument is 1, there is a token, and the status is known,
     *  then return an array containing the one token. Otherwise, throw
     *  an exception.
     *  @exception NoTokenException If the status is unknown, if there is
     *   no token, or if the argument is not 1.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        if (numberOfTokens <= 0) {
            throw new IllegalArgumentException(
                    "Illegal argument to getArray():" + numberOfTokens);
        }
        if (numberOfTokens > 1) {
            throw new NoTokenException(getContainer(),
                    "Receiver can only contain one token, but request is for "
                            + numberOfTokens);
        }
        if (_tokenCache == null) {
            _tokenCache = new Token[1];
        }
        _tokenCache[0] = _token;
        return _tokenCache;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true if the argument is 1, and otherwise return false.
     *  @param numberOfTokens The number of tokens to put into the receiver.
     *  @return True if the argument is 1, and otherwise return false.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasRoom() requires a positive argument.");
        }
        if (numberOfTokens > 1) {
            return false;
        }
        return true;
    }

    /** Return true.
     *  @return True.
     */
    public boolean hasToken() {
        return true;
    }

    /** Return true if the argument is 1 and this receiver is not empty,
     *  and otherwise return false.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and this receiver is not empty.
     *  @exception InternalErrorException If the status is not known.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     */
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "hasToken() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return true;
        }

        return false;
    }

    /** The state of this receiver is always known. This is ensured
     *  by the initial token.
     *  @return True, the state of the receiver is always known.
     */
    public boolean isKnown() {
        return true;
    }

    /** Put a token into this receiver.  If the argument is null, then the
     *  receiver will not contain a token after this returns, getting the
     *  same effect as calling clear(). If there was previously a token
     *  in the receiver, this overwrites that token.
     *  Set the known status of the receiver to true.
     *  @param token The token to be put into the mailbox.
     *  @exception NoRoomException If this mailbox is not empty.
     */
    public void put(Token token) throws NoRoomException {
    	_token = token;
    }

    /** If the argument has one token, then put that token in
     *  the receiver. Otherwise, throw an exception.
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        if (numberOfTokens != 1 || tokenArray.length < 1) {
            throw new IllegalActionException(getContainer(),
                    "Receiver cannot accept more than one token.");
        }
        put(tokenArray[0]);
    }

    /** Set the receiver to the initial token. */
    public void reset() throws IllegalActionException {
    	ModalModel model = null;
    	if (this.getContainer().getContainer() instanceof ModalModel) {
	    	model = (ModalModel)getContainer().getContainer();
    	} else if (((CompositeEntity)getContainer().getContainer()).getContainer() instanceof ModalModel) { 
	    	model = (ModalModel) ((CompositeEntity)this.getContainer().getContainer()).getContainer();
    	}
        if (model == null) {
            throw new InternalErrorException(getContainer(), null, "Could not get the model?");
        } else {
            IOPort port = (IOPort) model.getPort(this.getContainer().getName());
            Parameter parameter = (Parameter) model.getAttribute("init_" + port.getName());
            _token = parameter.getToken();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The token held. */
    private Token _token = null;

    /** The cache used by the getArray() method to avoid reallocating. */
    private Token[] _tokenCache;
}
