/* A base class for threaded DE domain actors.

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
package ptolemy.domains.de.kernel;

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.PtolemyThread;

///////////////////////////////////////////////////////////////////
//// DEThreadActor

/**
 A base class for threaded DE domain actors.
 <P>
 NOTE: This actor is very preliminary. It is not developed and maintained
 for a long time. We do not recommend using it. To try multiple threads under
 DE semantics, use DDE domain, which is another experimental domain.
 <P>
 This actor, upon its initialization, will start another thread.
 The thread communicate with the DEDirector thread by placing
 events into the DEEventQueue asynchronously.
 <P>
 Subclass of this class should implement the run() method.
 The subclass is executed in an event driven way. More precisely,
 the implementation of the run() method should call
 waitForNewInputs() after processing all current events. The
 calls are blocked until the next time fire() is called.
 Recall that the Director (after putting events into the
 receiver of the input ports) will call fire() on the actor.
 NOTE: The synchronization mechanism is implemented in DECQEventQueue
 to ensure the correct multi-threading behaviour.
 <P>
 This implementation does not change the semantics of DEReceiver,
 but still supports an asynchronous message passing type of
 concurrency.

 @author Lukito Muliadi
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (lmuliadi)
 @Pt.AcceptedRating Red (cxh)
 @see DEActor
 */
public abstract class DEThreadActor extends DEActor implements Runnable {
    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEThreadActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Awake the thread running this actor.
     */
    @Override
    public void fire() {

        synchronized (_monitor) {
            // Set the flag to false, to make sure only this actor wakes up.
            _isWaiting = false;

            _monitor.notifyAll();

            // then wait until this actor go to wait.
            while (!_isWaiting) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    System.err.println(KernelException.stackTraceToString(e));
                }
            }
        }
    }

    /** Create a thread for the actor and start the thread.
     */
    @Override
    public void initialize() {
        // start a thread.
        _thread = new PtolemyThread(this);
        _isWaiting = true;
        _thread.start();
    }

    /** Implement this method to define the job of the threaded actor.
     */
    @Override
    public abstract void run();

    /** Clear input ports then wait until
     *  input events arrive.
     */
    public void waitForNewInputs() {
        _emptyPorts();

        synchronized (_monitor) {
            // Set the flag to true, so the director can wake up.
            _isWaiting = true;

            _monitor.notifyAll();

            while (_isWaiting) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    System.err.println(KernelException.stackTraceToString(e));
                }
            }
        }
    }

    /** Wait for new inputs on the specified array of ports.
     *  @param ports The array of ports whose inputs we're interested in.
     *  @exception IllegalActionException If the specified array of ports
     *  is not all input ports.
     */
    public void waitForNewInputs(IOPort[] ports) throws IllegalActionException {
        _emptyPorts();

        while (true) {
            waitForNewInputs();

            // check for availability of tokens in the list of ports.
            // If any of the listed ports has at least a token, then return
            // Otherwise, wait for more new inputs.
            for (IOPort port : ports) {
                for (int j = 0; j < port.getWidth(); j++) {
                    if (port.hasToken(j)) {
                        return;
                    }
                }
            }
        } // while (true)
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Empty all receivers of all input ports.
    // FIXME: Shouldn't this be guaranteed by the run() of the actor?
    private void _emptyPorts() {
        Iterator<?> ports = inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            try {
                for (int channel = 0; channel < port.getWidth(); channel++) {
                    try {
                        while (port.hasNewToken(channel)) {
                            port.get(channel);
                        }
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(this, ex,
                                "Failed to empty ports?");
                    }
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(
                        this,
                        ex,
                        "At this time IllegalActionExceptions are not allowed to happen.\n"
                                + "Width inference should already have been done.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _isWaiting = true;

    private static Object _monitor = new Object();

    private PtolemyThread _thread;
}
