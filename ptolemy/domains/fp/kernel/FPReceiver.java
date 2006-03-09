/* The receiver for the Fixed Point domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.fp.kernel;

import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FPReceiver

/**
 The receiver for the Fixed Point (FP) domain. This receiver has capacity 1.  
 <p> 
 The status of this receiver can be either <i>known</i> or <i>unknown</i>. 
 A receiver has a known status if the receiver is either known to contain a 
 token or known not to contain a token.  
 <p> 
 During the fire() method of an iteration, this receiver changes its status 
 from unknown to known when the put() or clear() methods are called. Once the 
 status of a receiver becomes known, the token contained by the receiver 
 cannot be changed. Otherwise, an IllegalOutputException will be thrown. The 
 status of receiver changes from known to unknown when the reset() method is 
 called. The reset() method deletes the token contained by the receiver.
 The FPDirector calls the reset() method at its initialize() and postfire() 
 methods. An actor cannot change the status of a receiver to unknown.
 <p>
 The isKnown() method returns true if the receiver has a known status. 
 The hasRoom() method returns true if the receiver has a unknown status. 
 If the receiver has a known status, the hasToken() method returns true 
 if the receiver contains a token. If the receiver has an unknown status, 
 the hasToken() method will throw an UnknownTokenException.
 
 @author Haiyang Zheng, Paul Whitaker
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (reviewModerator)
 */
public class FPReceiver extends AbstractReceiver {

    /** Construct an FPReceiver with unknown status.
     *  @param director The director of this receiver.
     */
    public FPReceiver(FPDirector director) {
        super();
        _reset();
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the status of this receiver to be known and to contain no token.
     *  @exception IllegalActionException If this receiver is known to 
     *  contain a token. 
     */
    public void clear() throws IllegalActionException {
        if (isKnown()) {
            if (hasToken()) {
                throw new IllegalActionException(
                        "FPReceiver: Cannot change its value from presence"
                        + " to absence.");
            }
        } else {
            _token = null;
            _known = true;
            _becomesKnown = true;
            _getDirector()._receiverChanged();
        }
    }

    /** Return the contained token without modifying it.  If there
     *  is no token or the status of this receiver is unknown, throw an 
     *  exception.
     *  @return The token contained in the receiver.
     *  @exception NoTokenException If there is no token or the status of 
     *  this receiver is unknown.
     */
    public Token get() throws NoTokenException {
        if (_token == null) {
            throw new NoTokenException(
                    "FPReceiver: Attempt to get data from an " +
                    "empty receiver.");
        }

        if (!isKnown()) {
            throw new UnknownTokenException(
                    "FPReceiver: get() called on an FPReceiver " +
                    "with an unknown status.");
        }

        return _token;
    }

    /** Return true if the status of the receiver is unknown. 
     *  @return True if the status of the receiver is unknown. 
     */
    public boolean hasRoom() {
        return !isKnown();
    }

    /** Return what the hasRoom() method returns if the argument is 1. 
     *  If the argument is less than 1, throw an exception. Otherwise return 
     *  false.
     *  @param numberOfTokens The number of tokens to put into the receiver.
     *  @return True if the receiver can accept a token.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it is not declared explicitly.
     *  @see #hasRoom()
     */
    public boolean hasRoom(int numberOfTokens) throws IllegalArgumentException {
        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "FPReceiver: hasRoom() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return hasRoom();
        }

        return false;
    }

    /** Return true if the receiver contains a token, or false otherwise.
     *  If the receiver has unknown status, this method will throw an
     *  exception.
     *  @return True if this receiver contains a token.
     *  @exception UnknownTokenException If the state is unknown. 
     *  This is a runtime exception, so it is not declared explicitly.
     */
    public boolean hasToken() {
        if (isKnown()) {
            return (_token != null);
        } else {
            throw new UnknownTokenException(getContainer(),
                    "hasToken() called on FPReceiver with unknown status.");
        }
    }

    /** Return what hasToken() returns if the argument is 1. 
     *  If the argument is less than 1, throw an exception. 
     *  Otherwise return false.
     *  @param numberOfTokens The number of tokens to get from the receiver.
     *  @return True if the argument is 1 and the receiver has a token.
     *  @exception IllegalArgumentException If the argument is not positive.
     *   This is a runtime exception, so it does not need to be declared
     *   explicitly.
     *  @see #hasToken()
     */
    public boolean hasToken(int numberOfTokens) throws IllegalArgumentException {

        if (numberOfTokens < 1) {
            throw new IllegalArgumentException(
                    "FPReceiver: hasToken() requires a positive argument.");
        }

        if (numberOfTokens == 1) {
            return hasToken();
        }
        
        return false;
    }

    /** Return true if this receiver has a known status, that is, this receiver
     *  either is either known to have a token or known to not to have a token.
     *  @return True if this receiver has a known status.
     */
    public boolean isKnown() {
        return _known;
    }

    /** Set the status of this receiver to known and to contain the
     *  specified token.  If the receiver is already known and the value of
     *  the contained token is different from that of the new token, throw 
     *  an exception.
     *  @param token The token to be put into this receiver.
     *  @exception IllegalArgumentException If the argument is null.
     *  @exception IllegalOutputException If the state is known and absent,
     *   or a token is present and does not have the same value.
     */
    public void put(Token token) throws NoRoomException, 
        IllegalActionException {
        if (token == null) {
            throw new IllegalArgumentException(
                    "FPReceiver.put(null) is illegal. To set a receiver " +
                    "to contain an absence value, use the clear() method.");
        }

        if (!isKnown()) {
            _token = token;
            _known = true;
            _becomesKnown = true;
            _getDirector()._receiverChanged();
        } else {
            if (!hasToken()) {
                throw new IllegalOutputException(getContainer(),
                        "FPReceiver cannot change its absence value"
                        + " to a presence value.  Call reset() instead.");
            } else {
                try {
                    if (token.isEqualTo(_token).booleanValue()) {
                        // The token contains the same value. Do nothing.
                    } else {
                        throw new IllegalOutputException(getContainer(),
                                "FPReceiver cannot receive two tokens "
                                        + "that differ.");
                    }
                } catch (IllegalActionException ex) {
                    throw new IllegalActionException("FPReceiver cannot "
                            + "determine whether the two tokens received are "
                            + "equal. Make sure that the actor writes to " 
                            + "this receiver is deterministic (monotonic).");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this receiver changes from unknown to known status.
     *  @return True if this receiver changes from unknown to known status.
     */
    protected boolean _becomesKnown() {
        return _becomesKnown;
    }

    /** Reset the receiver by deleting any contained tokens and setting
     *  the status of this receiver to be unknown.  This is called
     *  by the FPDirector at is the postfire() method.
     */
    protected void _reset() {
        _token = null;
        _known = false;
        _becomesKnown = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the director that created this receiver.
     *  If this receiver is an inside receiver of
     *  an output port of an opaque composite actor,
     *  then the director will be the local director
     *  of the container of its port. Otherwise, it's the executive
     *  director of the container of its port.  Note that
     *  the director returned is guaranteed to be non-null.
     *  This method is read synchronized on the workspace.
     *  @return An instance of FPDirector.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of DEDirector.
     */
    private FPDirector _getDirector() throws IllegalActionException {
        IOPort port = (IOPort) getContainer();

        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }

            // Cache is invalid.  Reconstruct it.
            try {
                port.workspace().getReadAccess();

                Actor actor = (Actor) port.getContainer();

                if (actor != null) {
                    Director director;

                    if ((port.isOutput()) && (actor instanceof CompositeActor)
                            && ((CompositeActor) actor).isOpaque()) {
                        director = actor.getDirector();
                    } else {
                        director = actor.getExecutiveDirector();
                    }

                    if (director != null) {
                        if (director instanceof FPDirector) {
                            _director = (FPDirector) director;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a FPDirector.");
                        }
                    }
                }
            } finally {
                port.workspace().doneReading();
            }
        }

        throw new IllegalActionException(getContainer(),
                "This receiver doesn't have an IOPort as the container.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director of this receiver.
    private FPDirector _director;
    
    // version control for the director.
    private long _directorVersion = -1;
    
    // A flag indicating whether this receiver has a known status.  
    // A receiver has known state if it has a presence or absence value.
    private boolean _known;
    
    // A flag indicating whether the receiver status changes from unknown 
    // to known.  
    private boolean _becomesKnown = false;

    // The token held.
    private Token _token = null;
}
