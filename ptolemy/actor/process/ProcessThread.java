/* Thread class for process oriented domains.

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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow (mudit@eecs.berkeley.edu)
*/

package ptolemy.actor.process;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.PtolemyThread;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ProcessThread

/**
 Thread class acting as a process for process oriented domains.
 <P>
 In process oriented domains, each actor acts as a separate process and
 its execution is not centrally controlled by the director. Each process
 runs concurrently with other processes and is responsible for calling
 its execution methods.
 <P>
 This class provides the mechanism to implement the above.
 An instance of this class can be created by passing an actor as an
 argument to the constructor. This class runs as a separate thread on
 being started and calls the execution methods on the actor, repeatedly.
 In specific, it calls the prefire(), fire() and postfire() methods
 of the actor. Before termination, this calls the wrapup() method of
 the actor.
 <P>
 If an actor returns false in its postfire() methods, the
 actor is never fired again and the thread or process would terminate
 after calling wrapup() on the actor.
 <P>
 The initialize() method of the actor is not called from this class. It
 should be called before starting this thread.
 <P>
 In process oriented domains, the director needs to keep a count of the
 number of active processes in the system. This is used for detection of
 deadlocks, termination, and possibly some other reasons. For this two
 methods, _increaseActiveCount() and _decreaseActiveCount(), are defined
 in the ProcessDirector. _increaseActiveCount() is called on the director
 from the constructor of this class and the _decreaseActiveCount() method
 is called at the end of the run() method, i.e. before the thread terminates.
 <P>

 @author Mudit Goel, Neil Smyth, John S. Davis II
 @version $Id$
 @since Ptolemy II 0.2
 */
public class ProcessThread extends PtolemyThread {

    /** Construct a thread to be used for the execution of the
     *  iteration methods of the actor. This increases the count of active
     *  actors in the director.
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this
     *  actor.
     */
    public ProcessThread(Actor actor, ProcessDirector director) {
        super();
        _actor = actor;
        _director = director;
        _manager = ((CompositeActor)
                ((NamedObj) actor).getContainer()).getManager();

        // This method is called here and not in the run() method as the
        // count should be incremented before any thread is started
        // or made active. This is because the second started thread might
        // block on a read or write to this process and increment the block
        // count even before this thread has incremented the active count.
        // This results in false deadlocks.
        _director._increaseActiveCount();

        if (_actor instanceof NamedObj) {
            _name = ((NamedObj) _actor).getFullName();
            addDebugListener((NamedObj) _actor);
        } else {
            _name = "Unnamed";
        }
        // Set the name of the thread to the full name of the actor.
        setName(_name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Request that execution of the actor controlled by this
     *  thread continue.  This is normally called by the ProcessDirector
     *  in the prefire method.
     */
    public void cancelStopThread() {
        if (_threadStopRequested) {
            _debug("-- Thread request to stop has been canceled.");
        }
        _threadStopRequested = false;
    }

    /** Return the actor being executed by this thread
     *  @return The actor being executed by this thread.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Initialize the actor, iterate it through the execution cycle
     *  until it terminates. At the end of the termination, calls wrapup
     *  on the actor.
     */
    public void run() {
        _debug("-- Starting thread.");
        Workspace workspace = _director.workspace();
        boolean iterate = true;
        Throwable thrownWhenIterate = null;
        Throwable thrownWhenWrapup = null;
        try {
            // Initialize the actor.
            _actor.initialize();

            // While postfire() returns true and stop() is not called.
            while (iterate) {

                // NOTE: Possible race condition... actor.stop()
                // might be called before we get to this.
                // This will cause postfire() on the actor
                // to return false, which will stop its execution.
                if (_threadStopRequested) {
                    // And wait until the flag has been cleared.
                    _debug("-- Thread pause requested. Get lock on director.");
                    synchronized (_director) {
                        // Tell the director we're stopped (necessary
                        // for deadlock detection).
                        _director._actorHasStopped();
                        while (_threadStopRequested
                                && !_director.isStopRequested()) {
                            _debug("-- Thread waiting for canceled pause request.");
                            try {
                                workspace.wait(_director);
                            } catch (InterruptedException ex) {
                                _debug("-- Thread interrupted, so cancel iteration.");
                                break;
                            }
                        }
                        // NOTE: Do we need to indicate that actor has
                        // restarted, with something like
                        // _director._actorHasRestarted();
                    }
                    // If a stop has been requested, then either
                    // pause execution (if the director does not report
                    // that a stop has been requested) or stop
                    // execution altogether and run wrapup().
                    if (_director.isStopRequested()) {
                        _debug("-- Thread stop requested, so cancel iteration.");
                        break;
                    }
                    _debug("-- Thread resuming.");
                }

                // container is checked for null to detect the
                // deletion of the actor from the topology.
                if (((Entity) _actor).getContainer() != null) {
                    if (_actor.prefire()) {
                        _actor.fire();
                        iterate = _actor.postfire();
                    }
                }
            }
        } catch (Throwable t) {
            thrownWhenIterate = t;
        } finally {
            try {
                wrapup();
            } catch (IllegalActionException e) {
                thrownWhenWrapup = e;
            } finally {
                // let the director know that this thread stopped
                _director._decreaseActiveCount();
                _debug("-- Thread stopped.");
                boolean rethrow = false;
                if (thrownWhenIterate instanceof TerminateProcessException) {
                    // Process was terminated.
                    _debug("-- Blocked Receiver call threw TerminateProcessException.");
                } else if (thrownWhenIterate instanceof IllegalActionException) {
                    _debug("-- Exception: " + thrownWhenIterate);
                    _manager.notifyListenersOfException(
                            (IllegalActionException)thrownWhenIterate);
                } else if (thrownWhenIterate != null) {
                    rethrow = true;
                }
                if (thrownWhenWrapup instanceof IllegalActionException) {
                    _debug("-- Exception: " + thrownWhenWrapup);
                    _manager.notifyListenersOfException(
                            (IllegalActionException)thrownWhenWrapup);
                } else if (thrownWhenWrapup != null) {
                    // must be a runtime exception
                    throw new RuntimeException(thrownWhenWrapup);
                } else if (rethrow) {
                    throw new RuntimeException(thrownWhenIterate);
                }
            }
        }
        /*
          catch (TerminateProcessException t) {
          // Process was terminated.
          _debug("-- Blocked Receiver call threw TerminateProcessException.");
          } catch (IllegalActionException e) {
          _debug("-- Exception: " + e);
          System.out.println(_name + " encountered exception");
          _manager.notifyListenersOfException(e);
          System.out.println(_name + " notified exception");
          } catch (Throwable t) {
          System.out.println("caught throwable " + t);
          t.printStackTrace();
          } finally {
          try {
          wrapup();
          } catch (IllegalActionException e) {
          _debug("-- Exception: " + e);
          _manager.notifyListenersOfException(e);
          } finally {
          System.out.println(_name + " going to stop");
          _director._decreaseActiveCount();
          _debug("-- Thread stopped.");
          }
          }*/
    }

    /** Request that execution of the actor controlled by this
     *  thread stop. Call stopFire() on all composite actors
     *  that are contained by the composite actor that contains
     *  this director.
     */
    public void stopThread() {
        _threadStopRequested = true;
        _debug("-- Thread requested to stop.");
    }

    /** End the execution of the actor under the control of this
     *  thread. Subclasses are encouraged to override this method
     *  as necessary for domain specific functionality.
     *  @exception IllegalActionException If an error occurs while
     *   ending execution of the actor under the control of this
     *   thread.
     */
    public void wrapup() throws IllegalActionException {
        _debug("-- Thread wrapup() called.");
        _actor.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Actor _actor;
    private ProcessDirector _director;
    private Manager _manager;
    private boolean _threadStopRequested = false;
    private String _name;
}
