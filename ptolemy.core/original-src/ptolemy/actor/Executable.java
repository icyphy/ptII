/* Interface for defining how an object can be invoked.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Executable

/**
 This interface defines the <i>action methods</i>, which determine
 how an object can be invoked. It should be implemented by actors
 and directors. In an execution of an application,
 the preinitialize() and initialize() methods should be
 invoked exactly once, followed by any number of iterations, followed
 by exactly one invocation of the wrapup() method. An <i>iteration</i>
 is defined to be any number of invocations of prefire() and fire(),
 where fire() is invoked only if prefire() returns true, followed by
 exactly one invocation of the postfire() method.
 The postfire() method returns false if no further iterations
 should occur. The initialize(), fire() and postfire() methods may produce
 output data.  The preinitialize() method runs
 before type resolution has been done, and is permitted to make
 changes in the topology of the model.

 @author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (davisj)
 */
public interface Executable extends Initializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actor.  This may be invoked several times between
     *  invocations of prefire() and postfire(). Output data may
     *  (and normally will) be produced.
     *  Typically, the fire() method performs the computation associated
     *  with an actor. This method is not required to have bounded
     *  execution.  However, after endFire() is called, this method should
     *  return in bounded time.
     *
     *  @exception IllegalActionException If firing is not permitted.
     */
    public void fire() throws IllegalActionException;

    /** Return true if this executable does not change state in either
     *  the prefire() or the fire() method. A class that returns true
     *  is said to obey the <i>actor abstract semantics</i>. In particular,
     *  such an actor can be used in domains that have a fixed point
     *  semantics and may repeatedly fire the actor before committing
     *  to state changes.
     *
     *  @return True if this executable only updates its states during
     *   an iteration in the postfire() method.
     */
    public boolean isFireFunctional();

    /** Return true if this executable is strict, meaning all inputs must
     *  be known before iteration. Normally, classes that implement this
     *  interface are strict, so this method will return true.
     *  However, some classes can perform an iteration even if some
     *  inputs are not known (i.e., these classes tolerate a return value
     *  of false from the isKnown() method of Receiver).
     *
     *  @return True if this executable is strict, meaning all inputs must
     *   be known before iteration.
     * @exception IllegalActionException Thrown by subclass.
     */
    public boolean isStrict() throws IllegalActionException;

    /** Invoke a specified number of iterations of the actor. An
     *  iteration here is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.
     *  <p>
     *  An implementation of this method is not required to
     *  actually invoke prefire(), fire(), and postfire(). An
     *  implementation of this method must, however,
     *  perform the equivalent operations.
     *  <p>
     *  Note that this method for iterating an actor should
     *  be used only in domains where a single invocation of
     *  prefire() and fire() is sufficient in an iteration.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException;

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration. The postfire() method should
     *  not produce output data on output ports of the actor.
     *  It returns true if the execution can proceed into the next iteration,
     *  false if the actor does not wish to be fired again.
     *  This method typically wraps up an iteration, which may involve
     *  updating local state or updating displays.
     *
     *  @return True if the execution can continue.
     *  @exception IllegalActionException If postfiring is not permitted.
     */
    public boolean postfire() throws IllegalActionException;

    /** This method should be invoked prior to each invocation of fire().
     *  It returns true if the fire() method can be invoked, given the
     *  current status of the inputs and parameters of the actor. Thus
     *  this method will typically check preconditions for a firing, if
     *  there are any. In an opaque, non-atomic entity,
     *  it may move data into an inner subsystem.
     *
     *  @return True if the iteration can proceed.
     *  @exception IllegalActionException If prefiring is not permitted.
     */
    public boolean prefire() throws IllegalActionException;

    /** Request that execution of this Executable stop as soon
     *  as possible.  An implementation of this method should
     *  pass on the request to any contained executable objects.
     *  An implementation should also return false from postfire()
     *  to indicate to the caller that no further execution of
     *  this Executable is appropriate.  After this method is
     *  called, the executable object should not be fired again.
     *  The stopFire() method, by contrast, requests that the
     *  current iteration be completed, but not that the entire
     *  execution be stopped.  I.e., the Executable may be fired
     *  again after stopFire() is called.
     */
    public void stop();

    /** Request that execution of the current iteration complete.  If
     *  an iteration is always a finite computation (the usual case),
     *  i.e.  the fire() method always returns in finite time, then
     *  nothing needs to be done in this method, except possibly to
     *  pass on the request to any contained executable objects.  This
     *  method is used to request that an unbounded computation
     *  suspend, returning control to the caller.  Thus, if the fire()
     *  method does not normally return in finite time, then this
     *  method is used to request that it return.  It should suspend
     *  execution in such a way that if the fire() method is called
     *  again, execution will resume at the point where it was
     *  suspended.  However, it should not assume the fire() method
     *  will be called again.  It is possible that the wrapup() method
     *  will be called next.
     */
    public void stopFire();

    /** Terminate any currently executing model with extreme prejudice.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
     *  method instead. This method should be called only
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  <p>
     *  After this method completes, all resources in use should be
     *  released and any sub-threads should be killed.
     *  However, a consistent state is not guaranteed.   The
     *  topology should probably be recreated before attempting any
     *  further operations.
     *  This method should not be synchronized because it must
     *  happen as soon as possible, no matter what.
     */
    public void terminate();

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** An indicator that the iterate() method completed successfully. */
    public static final int COMPLETED = 0;

    /** An indicator that the iterate() method did not complete because
     *  the actor was not ready (prefire() returned false).
     */
    public static final int NOT_READY = 1;

    /** An indicator that the actor does not wish to be fired again.
     */
    public static final int STOP_ITERATING = 2;
}
