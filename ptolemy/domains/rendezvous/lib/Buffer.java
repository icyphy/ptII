/* An actor representing a resource pool with a specified number of resources.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.rendezvous.kernel.RendezvousDirector;
import ptolemy.domains.rendezvous.kernel.RendezvousReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////

/**
 This actor buffers data provided at the input, sending it to the
 output when needed. It uses two threads. The main actor thread
 is willing to rendezvous with the output as long as the buffer
 is not empty. A second thread is created on the first invocation
 of fire(). This second thread is willing to rendezvous with the
 input as long as the buffer is not full. Thus, this actor
 acts as a FIFO (first-in, first-out) buffer that will accept
 input tokens as long as it is not full, and will produce output
 tokens as long as it is not empty.
 <p>
 If the capacity changes during execution, and the buffer already
 contains more tokens than the new capacity, then no tokens are lost,
 but no new tokens are accepted at the input until the number of
 buffered tokens drops below the capacity.
 <p>
 This actor is similar to the ResourcePool actor except that
 the input and output ports are not multiports and there are
 no initial tokens in this buffer.

 @author Edward A. Lee
 @version $Id$
 @see ResourcePool
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class Buffer extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Buffer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        capacity = new Parameter(this, "capacity");
        capacity.setTypeEquals(BaseType.INT);
        capacity.setExpression("1");

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The capacity of the buffer. To provide "infinite" capacity,
     *  set this to a negative number.
     */
    public Parameter capacity;

    /** The input port.
     */
    public TypedIOPort input;

    /** The output port. The type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Buffer newObject = (Buffer) super.clone(workspace);
        newObject._buffer = new LinkedList();

        return newObject;
    }

    /** If it has not already been done, start a thread to read tokens from the
     *  <i>input</i> port and store them in the buffer.
     *  Then, in the calling thread, if there is at least one
     *  token in the buffer, write the first token to the <i>output</i> port.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        final Thread writeThread = Thread.currentThread();

        if (!(getDirector() instanceof RendezvousDirector)) {
            throw new IllegalActionException(this,
                    "Buffer actor can only be used with RendezvousDirector.");
        }

        final RendezvousDirector director = (RendezvousDirector) getDirector();

        _postfireReturns = true;
        if (_readThread == null) {
            _readThread = new Thread(getFullName() + "_readThread") {
                @Override
                public void run() {
                    try {
                        if (_debugging) {
                            _debug("** Starting read thread.");
                        }
                        _exception = null;
                        while (!_stopRequested) {
                            // Synchronize on the director since all read/write
                            // operations do.
                            synchronized (director) {
                                // If the buffer is full, then wait until it is no
                                // longer full.
                                int capacityValue = ((IntToken) capacity
                                        .getToken()).intValue();
                                while (_buffer.size() >= capacityValue
                                        && !_stopRequested) {
                                    if (_debugging) {
                                        _debug("** Waiting because buffer is full.");
                                    }
                                    try {
                                        director.threadBlocked(_readThread,
                                                null);
                                        RendezvousReceiver
                                                .waitForChange(director);
                                    } finally {
                                        director.threadUnblocked(_readThread,
                                                null);
                                    }
                                }
                                if (_stopRequested) {
                                    break;
                                }
                                if (_debugging) {
                                    _debug("** Waiting for input.");
                                }
                                Token token = input.get(0);
                                _buffer.add(token);
                                if (_debugging) {
                                    _debug("** Received input. Buffer contents: "
                                            + _buffer);
                                }
                                director.threadUnblocked(writeThread, null);
                                director.notifyAll();
                            }
                        }
                    } catch (TerminateProcessException ex) {
                        // OK, just exit.
                        _postfireReturns = false;
                    } catch (IllegalActionException ex) {
                        _exception = ex;
                    } finally {
                        director.removeThread(_readThread);
                        if (_debugging) {
                            _debug("** Ending read thread.");
                        }
                    }
                }
            };
            director.addThread(_readThread);
            _readThread.start();
        }
        // Synchronize on the director since all read/write
        // operations do.
        synchronized (director) {
            if (_exception != null) {
                throw _exception;
            }
            while (_buffer.size() == 0) {
                if (_stopRequested || !_postfireReturns) {
                    _postfireReturns = false;
                    return;
                }
                if (_debugging) {
                    _debug("Buffer is empty. Waiting for it to fill.");
                }
                try {
                    director.threadBlocked(writeThread, null);
                    RendezvousReceiver.waitForChange(director);
                } catch (TerminateProcessException ex) {
                    _postfireReturns = false;
                    return;
                } finally {
                    director.threadUnblocked(writeThread, null);
                }
                if (_exception != null) {
                    throw _exception;
                }
            }
            // There is a token.
            Token token = (Token) _buffer.get(0);
            if (_debugging) {
                _debug("Sending token to output: " + token);
            }
            if (_exception != null) {
                throw _exception;
            }
            // If this put blocks for any reason, it will block on
            // a director.wait(), so the lock will not be held.
            try {
                output.send(0, token);
            } catch (TerminateProcessException e) {
                _postfireReturns = false;
                return;
            }
            if (_exception != null) {
                throw _exception;
            }
            _buffer.remove(0);
            if (_debugging) {
                _debug("Buffer contents: " + _buffer);
            }

            int capacityValue = ((IntToken) capacity.getToken()).intValue();
            if (_buffer.size() == capacityValue - 1 && !_stopRequested) {
                director.threadUnblocked(_readThread, null);
                director.notifyAll();
            }
        }
    }

    /** Clear the buffer.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buffer.clear();
        _exception = null;
        _readThread = null;
        _postfireReturns = true;
    }

    /** Return false if it is time to stop the process.
     *  @return False a TerminateProcessException was thrown during
     *  I/O or if the superclass returns false.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        return _postfireReturns;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current buffer. */
    private List _buffer = new LinkedList();

    /** Exception that might be thrown by the spawned thread. */
    private IllegalActionException _exception;

    /** Flag indicating what postfire should return. */
    private boolean _postfireReturns = true;

    /** The read thread, if it exists. */
    private Thread _readThread = null;
}
