/** An actor that slices the input bits and output a consecutive subset
 of the input bits.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.vhdl;

import java.util.LinkedList;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QueuedIOPort

/**
 Delays each fixed point token sent on the port by the specified latency
 parameter and then outputs it via the parent TypedIOPort. Initially will
 output NIL until one input can be sent.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class QueuedTypedIOPort extends TypedIOPort {

    /** Construct a QueuedTypedIOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the TypedActor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public QueuedTypedIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);

        myQueue = new LinkedList<Token>();
        _oldToken = null;
        latency = 0;
        initialToken = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the size of the queue.  This operation will clear whatever
     *  is currently enqueued and create a queue of the new size.
     *  @param size The size of the queue.
     *  @param initialValue The initial value of the queue.
     */
    public void setSize(int size, Token initialValue) {
        latency = size;
        initialToken = initialValue;
        _createQueue();
    }

    /** Set the size of the queue.  This operation will clear whatever
     *  is currently enqueued and create a queue of the new size.
     *  @param size The size of the queue.
     */
    public void resize(int size) {
        latency = size;
        _createQueue();

    }

    /** Clear the queue.
     */
    public void clear() {
        myQueue.clear();
    }

    /**
     * Set the initial token value.
     * @param initialValue The initial value of the token.
     */
    public void setInitToken(Token initialValue) {

        // FIXME: rename this to setInitialToken() so as to follow
        // naming convention.

        initialToken = initialValue;
        _createQueue();
    }

    /**
     * Create a token queue for the port.
     */
    private void _createQueue() {
        myQueue.clear();
        _oldToken = initialToken;
        for (int i = 1; i < latency; i++) {
            myQueue.add(initialToken);
        }
    }

    /** Enqueue the token that is being sent and send to the parent whatever
     *  is at the end of the queue.
     *  @param channelIndex The channel on which to send the token.
     *  @param token The token to be sent.
     *  @exception IllegalActionException If thrown while sending to the
     *  channel.
     *  @exception NoRoomException If thrown while sending to the channel.
     */
    @Override
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        if (latency == 0) {
            super.send(channelIndex, token);
        } else {
            if (token != null) {
                myQueue.add(token);
                super.send(channelIndex, _oldToken);
                _oldToken = myQueue.removeFirst();
            }
        }
    }

    /**
     * Resend the token from the given channel index.
     * @param channelIndex The given channel index.
     * @exception IllegalActionException If super class throws it.
     * @exception NoRoomException If super class throws it.
     */
    public void resend(int channelIndex) throws IllegalActionException,
            NoRoomException {
        if (latency != 0) {
            super.send(channelIndex, _oldToken);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The token queue.
     */
    private LinkedList<Token> myQueue;

    /**
     * The previous token.
     */
    private Token _oldToken;

    /**
     * The latency of the port.
     */
    private int latency;

    /**
     * The initial output value of the port.
     */
    private Token initialToken;

}
