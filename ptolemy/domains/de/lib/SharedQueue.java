/* A queue that outputs the next token to an output channel that is ready to
 receive it.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import java.util.Arrays;

import ptolemy.actor.util.FIFOQueue;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// SharedQueue

/**
 A queue that outputs the next token to an output channel that is ready to
 receive it. It can have multiple output channels, and the same number of
 trigger channels. Initially all output channels are assumed to be ready. When a
 token is available, it is sent to the first channel that is ready, and that
 channel is marked not ready. When all channels are not ready, the token would
 be queued. If the number of tokens already in the queue is equal to the queue
 capacity (0 means infinity), then new tokens are lost.
 <p>
 To make an output channel ready after a token is sent to it, a triggering
 signal needs to be received in the corresponding channel of the trigger port.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SharedQueue extends Queue {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SharedQueue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setMultiport(true);
        persistentTrigger.setVisibility(Settable.NONE);
    }

    /** If there is an input at the input port, consume it. If there are tokens
     *  in the queue and there are ready output channels, the tokens are sent to
     *  those output channels. If triggers are available at the trigger port,
     *  consume them and make the corresponding output channels ready. Also send
     *  tokens to those channels if there are tokens available.
     *  @exception IllegalActionException If getting tokens from input and
     *   trigger ports or sending token to output throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        int sizeOutput = _queue.size();
        _removeTokens = 0;
        for (int i = 0, j = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
                if (sizeOutput > 0) {
                    _outputReady[i] = false;
                    output.send(i, (Token) _queue.get(j++));
                    sizeOutput--;
                    _removeTokens++;
                } else {
                    _outputReady[i] = true;
                }
            }
        }
        if (input.hasToken(0)) {
            _token = input.get(0);
            for (int i = 0; i < _outputReady.length; i++) {
                if (_outputReady[i]) {
                    _outputReady[i] = false;
                    output.send(i, _token);
                    _token = null;
                    break;
                }
            }
            if (_token != null
                    && (_queue.getCapacity() == FIFOQueue.INFINITE_CAPACITY || _queue
                    .getCapacity() > _queue.size() - _removeTokens)) {
                sizeOutput++;
            } else {
                _token = null;
            }
        }
        size.send(0, new IntToken(sizeOutput));
    }

    /** Return true if either there is input at the input port, or there are
     *  triggering signals at the trigger port. Return false otherwise.
     *
     *  @return True if this actor is ready to fire.
     *  @exception IllegalActionException If the ports cannot be tested for
     *   availability of tokens.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (input.isOutsideConnected() && input.hasToken(0)) {
            return true;
        }

        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                return true;
            }
        }

        return false;
    }

    /** Make all output channels ready.
     *
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _outputReady = new boolean[output.getWidth()];
        Arrays.fill(_outputReady, true);
    }

    /** Clear the buffer.
     *
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _outputReady = null;
    }

    // Each entry represents the readiness of an output channel.
    private boolean[] _outputReady;
}
