/* The receiver for the CT domain.

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
package ptolemy.domains.ct.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

import java.io.Serializable;


//////////////////////////////////////////////////////////////////////////
//// CTReceiver

/**
   The receiver for the continuous-time and mixed-signal domain. The receiver
   can be of one of the two types: CONTINUOUS and DISCRETE. Conceptually,
   a CONTINUOUS CTReceiver contains a sample of a continuous signal at a
   particular time (defined by the CTDirector). Thus, there is one and
   only one token at all time in a CONTINUOUS CTReceiver. A DISCRETE
   CTReceiver contains a discrete event. Thus a DISCRETE CTReceiver may
   be empty if an event is not present.
   <P>
   The receiver is implemented as a Mailbox of capacity one. Any token put
   in the receiver overwrites any token previously present in the receiver.
   As a consequence, hasRoom() method always returns true.
   <P>
   The behavior of the get() method depends on the type of the receiver.
   If it is CONTINUOUS, then get() only reads the value. Consecutive calls on
   the get method will return the same token if the put method has not been
   called. For a CONTINUOUS CTReceiver, hasToken() will always return true
   after the first put() has been called. For a DISCRETE
   CTReceiver, get() will return and destroy the token, thus the token
   can only be retrived once. Therefore after the consumption, the hasToken()
   method will return false, until a token is put into this receiver.

   @author  Jie Liu
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (yuhong)
*/
public class CTReceiver extends Mailbox {
    /** Construct an empty CTReceiver with no container.
     */
    public CTReceiver() {
        super();
        _type = UNKNOWN;
    }

    /** Construct an empty CTReceiver with the specified container.
     *  @param container The port that contains the receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public CTReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _type = UNKNOWN;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Signal type: CONTINUOUS. */
    public final static SignalType CONTINUOUS = new SignalType() {
            public String toString() {
                return "CONTINUOUS";
            }
        };

    /** Signal type: DISCRETE. */
    public final static SignalType DISCRETE = new SignalType() {
            public String toString() {
                return "DISCRETE";
            }
        };

    /** Signal type: UNKNOWN. */
    public final static SignalType UNKNOWN = new SignalType() {
            public String toString() {
                return "UNKNOWN";
            }
        };

    ///////////////////////////////////////////////////////////////////
    ////                    public inner classes                   ////

    /** Inner class used for the static enumeration of indicators of
     *  signal types. Instances of this class cannot be constructed outside
     *  the enclosing interface because its constructor is protected.
     */
    public static class SignalType implements Serializable {
        // Protected constructor prevents construction outside.
        // This constructor should not be called!
        // it is protected to work around a compiler bug in JDK1.2.2
        protected SignalType() {
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the contained token if it is not null. If the receiver
     *  is CONTINUOUS, then the token is still available for the next
     *  get, i.e. the token is not set to null. If the receiver is
     *  DISCRETE, then the token is removed from the receiver, and
     *  the receiver contains null, which means that is another get()
     *  is called before a calling of put, then an exception will be
     *  thrown. If the receiver contains null, then thrown a
     *  NoTokenException.
     *  @exception NoTokenException If the receiver contains null.
     *  @exception InvalidStateException If this method is called and
     *  the signal type of this receiver is UNKNOWN.
     */
    public Token get() throws NoTokenException {
        if (_token != null) {
            if (_type == CONTINUOUS) {
                return _token;
            } else if (_type == DISCRETE) {
                return super.get();
            } else {
                throw new InvalidStateException(getContainer(),
                        "get() is called before the signal type of this port"
                        + " has been set. Bug in CTScheduler?");
            }
        } else {
            throw new NoTokenException(getContainer(),
                    "Attempt to get data from an empty CTReceiver.\n"
                    + "Are you trying to use a discrete signal "
                    + "to drive a continuous port?");
        }
    }

    /** Return the signal type of this receiver.
     *  @return The signal type of the receiver.
     *  @see #setSignalType
     */
    public SignalType getSignalType() {
        return _type;
    }

    /** Return true, since the new token will overwrite the old one.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Put a token into this receiver. If the argument is null,
     *  then this receiver will not contain any token after this method
     *  returns. If the receiver already has a token, then the new token
     *  will overwrite the old token, and the old token will be lost.
     *
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this base class.
     */
    public void put(Token token) throws NoRoomException {
        _token = token;
    }

    /** Set the signal type of this receiver. This method must be called
     *  by the CTScheduler before any get() method are called.
     *  @param type The SignalType to set to the receiver.
     *  @see #getSignalType
     */
    public void setSignalType(SignalType type) {
        _type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private SignalType _type;
}
