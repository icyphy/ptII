/* A Director governs the execution of a CompositeActor.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
Review changeRequest / changeListener code.
Review container relationship and new parent class.
Win added methods fireAtCurrentTime(Actor) and
Semantics of initialize(Actor) have changed.
Also, review stop() method.
*/

package ptolemy.actor;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Director
/**
A Director governs the execution within a CompositeActor.  A composite actor
that contains a director is said to be <i>opaque</i>, and the execution model
within the composite actor is determined by the contained director.   This
director is called the <i>local director</i> of a composite actor.
A composite actor is also aware of the director of its container,
which is referred to as its <i>executive director</i>.
A director may also be contained by a CompositeEntity that is not a
CompositeActor, in which case it acts like any other entity within
that composite.
<p>
A top-level composite actor is generally associated with a <i>manager</i>
as well as a local director.  The Manager has overall responsibility for
executing the application, and is often associated with a GUI.   Top-level
composite actors have no executive director and getExecutiveDirector() will
return null.
<p>
A local director is responsible for invoking the actors contained by the
composite.  If there is no local director, then the executive director
is given the responsibility.  The getDirector() method of CompositeActor,
therefore, returns the local director, if there is one, and otherwise
returns the executive director.  Thus, it returns whichever director
is responsible for executing the contained actors, or null if there is none.
Whatever it returns is called simply the <i>director</i> (vs. local
director or executive director).
<p>
A director implements the action methods (preinitialize(),
initialize(), prefire(), fire(), postfire(), iterate(),
and wrapup()).  In this base class, default implementations
are provided that may or may not be useful in specific domains.   In general,
these methods will perform domain-dependent actions, and then call the
respective methods in all contained actors.
<p>
The director also provides methods to optimize the iteration portion of an
execution. This is done by setting the workspace to be read-only during
an iteration. In this base class, the default implementation results in
a read/write workspace. Derived classes (e.g. domain specific
directors) should override the _writeAccessRequired() method to report
that write access is not required. If none of the directors in a simulation
require write access, then it is safe to set the workspace to be read-only,
which will result in faster execution.

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, John Reekie
@version $Id$
@since Ptolemy II 0.2
*/
public class Director extends Attribute implements Executable {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Director() {
        super();
        _addIcon();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public Director(Workspace workspace) {
        super(workspace);
        _addIcon();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public Director(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Transfer at most one data token from the given input port of
     *  the container to the ports it is connected to on the inside.
     *  This method delegates the operation to the IOPort, so that the
     *  subclass of IOPort, TypedIOPort, can override this method to
     *  perform run-time type conversion.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @see IOPort#transferInputs
     */
    public boolean domainPolymorphicTransferInputs(IOPort port) 
            throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque" +
                    "input port.");
        }
        boolean wasTransferred = false;
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);
                        if (_debugging) _debug(getName(),
                                "transferring input from "
                                + port.getName());
                        port.sendInside(i, t);
                        wasTransferred = true;
                    }
                } else {
                    // No inside connection to transfer tokens to.
                    // In this case, consume one input token if there is one.
                    if (_debugging) _debug(getName(),
                            "Dropping single input from "
                            + port.getName());
                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return wasTransferred;
    }

    /** Transfer at most one data token from the given output port of
     *  the container to the ports it is connected to on the outside.
     *  This method delegates the operation to the same method on
     *  IOPort.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @see IOPort#transferOutputs
     */
    public boolean domainPolymorphicTransferOutputs(IOPort port) 
            throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                    + "is not an opaque input port.");
        }
        boolean wasTransferred = false;
        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.hasTokenInside(i)) {
                    Token t = port.getInside(i);
                    if (_debugging) _debug(getName(),
                            "transferring output from "
                            + port.getName());
                    port.send(i, t);
                    wasTransferred = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return wasTransferred;
    }

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this director.  In general, this may be called more
     *  than once in the same iteration of the director's container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().  If stop() is called
     *  during this execution, the stop iterating actors immediately.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  <p>
     *  In this base class, an attempt is made to fire each actor exactly
     *  once, in the order they were created.  Prefire is called once, and
     *  if prefire returns true, then fire is called once, followed by
     *  postfire.  The return value from postfire is ignored. If the
     *  container is not an instance of CompositeActor, however, then
     *  this method does nothing.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            int iterationCount = 1;
            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor)actors.next();
                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE,
                            iterationCount));
                }
                if (actor.iterate(1) == Actor.STOP_ITERATING) {
                    if (_debugging) {
                        _debug("Actor requests halt: "
                                + ((Nameable)actor).getFullName());
                    }
                    break;
                }
                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_ITERATE,
                            iterationCount));
                }
            }
        }
    }

    /** Schedule a firing of the given actor at the given absolute
     *  time.  This method is only intended to be called from within
     *  main simulation thread.  Actors that create their own
     *  asynchronous threads should used the fireAtCurrentTime()
     *  method to schedule firings.
     *
     *  This method does nothing in this base class. Derived classes
     *  should override this method.  <p> Note that this method is not
     *  made abstract to facilitate the use of the test suite.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // do nothing in this base class.
        // Note that, alternatively, this method could have been abstract.
        // But we didn't do that, because otherwise we wouldn't be able
        // to run Tcl Blend test script on this class.

    }

    /** Schedule a firing of the given actor as soon as possible.  If
     *  this method is called from within the main simulation thread,
     *  then this will result in the actor being fired at the current
     *  time.  This method may also be invoked from a thread other
     *  than the main simulation thread, making it useful for actors
     *  that perform asynchronous processing, such as waiting for data
     *  in another thread. In these cases, calling fireAt(actor,
     *  getCurrentTime()) is not sufficient, since there is no way for
     *  an asynchronous thread to ensure that time does not advance in
     *  the model.  When invoked from an asynchronous thread, this
     *  method will attempt to fire the actor at the earliest time
     *  that this director is fired.
     *
     *  This base class checks to see if the director is embedded
     *  within a hierarchical model and, if so, calls the
     *  fireAtCurrentTime method of its executive director. Derived
     *  classes may should extend this behavior to ensure that the
     *  given actor is fired as soon as possible.  <p> Note that this
     *  method is not made abstract to facilitate the use of the test
     *  suite.
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If this method is called
     *  before the model is running.
     */
    public void fireAtCurrentTime(Actor actor)
            throws IllegalActionException {
        if (_isEmbedded()) {
            CompositeActor container = (CompositeActor)getContainer();
            container.getExecutiveDirector().fireAtCurrentTime(
                    container);
        }
    }

    /** Return the current time of the model being executed by this director.
     *  This time can be set with the setCurrentTime method. In this base
     *  class, time never passes, and there are no restrictions on valid
     *  times.
     *
     *  @return The current time.
     */
    public double getCurrentTime() {
        return _currentTime;
    }

    /** Return the next time of interest in the model being executed by
     *  this director. This method is useful for domains that perform
     *  speculative execution (such as CT).  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute.
     *  <p>
     *  In this base class, we return the current time.
     *  Derived classes should override this method to provide an appropriate
     *  value, if possible.
     *  <p>
     *  Note that this method is not made abstract to facilitate the use
     *  of the test suite.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        return _currentTime;
    }

    /** Initialize the model controlled by this director.  Set the
     *  current time to 0.0 or the time of the executive director, and
     *  then invoke the initialize() method of this director on each
     *  actor that is controlled by this director.  If the container
     *  is not an instance of CompositeActor, do nothing.  This method
     *  should typically be invoked once per execution, after the
     *  preinitialization phase, but before any iteration.  It may be
     *  invoked in the middle of an execution, if reinitialization is
     *  desired.  Since type resolution has been completed and the
     *  current time is set, the initialize() method of a contained
     *  actor may produce output or schedule events.  If stop() is
     *  called during this methods execution, then stop initializing
     *  actors immediately.  This method is
     *  <i>not</i> synchronized on the workspace, so the caller should
     *  be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Nameable containersContainer = container.getContainer();
            if (containersContainer instanceof CompositeActor) {
                double time = ((CompositeActor)containersContainer)
                    .getDirector().getCurrentTime();
                _currentTime = time;
            } else {
                _currentTime = 0.0;
            }
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor)actors.next();
                if (_debugging) _debug("Invoking initialize(): ",
                        ((NamedObj)actor).getFullName());
                initialize(actor);
            }
        }
    }

    /** Initialize the given actor.  This method is generally called
     *  by the initialize() method of the director, and by the manager
     *  whenever an actor is added to an executing model as a
     *  mutation.  This method will generally perform domain-specific
     *  initialization on the specified actor and call its
     *  initialize() method.  In this base class, only the actor's
     *  initialize() method of the actor is called and no
     *  domain-specific initialization is performed.  Typical actions
     *  a director might perform include starting threads to execute
     *  the actor or checking to see whether the actor can be managed
     *  by this director.  For example, a time-based domain (such as
     *  CT) might reject sequence based actors.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        actor.initialize();
    }

    /** Indicate that resolved types in the model may no longer be valid.
     *  This will force type resolution to be redone on the next iteration.
     *  This method simply defers to the manager, notifying it.  If there
     *  is no container, or the container is not an instance of
     *  CompositeActor, or if it has no manager, do nothing.
     */
    public void invalidateResolvedTypes() {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Manager manager = ((CompositeActor)container).getManager();
            if (manager != null) {
                manager.invalidateResolvedTypes();
            }
        }
    }

    /** Indicate that a schedule for the model may no longer be valid, if
     *  there is a schedule.  This method should be called when topology
     *  changes are made, or for that matter when any change that may
     *  invalidate the schedule is made.  In this base class, the method
     *  does nothing. In derived classes, it will cause any static
     *  schedule information to be recalculated in the prefire method
     *  of the director.
     */
    public void invalidateSchedule() {
    }

    /** Invoke a specified number of iterations of this director. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  Also, if the stop() is
     *  called during this execution, then immediately stop iterating
     *  and return STOP_ITERATING.
     *  <p>
     *  This base class method actually invokes prefire(), fire(),
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int n = 0;
        while (n++ < count && !_stopRequested) {
            if (prefire()) {
                fire();
                if (!postfire()) return Executable.STOP_ITERATING;
            } else {
                return Executable.NOT_READY;
            }
        }
        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
    }

    /** Return true if this director, or any of its contained directors
     *  requires write access on the workspace during execution.
     *  If this director requires write access during execution
     *  (i.e. _writeAccessRequired() returns true), then
     *  this method returns true.   Otherwise, needWriteAccess() is called
     *  recursively on all the local directors of all deeply
     *  contained entities that are opaque composite actors.
     *  If any of those lower level directors requires write access, then
     *  this method will return true.  Otherwise, this method returns false.
     *  <p>
     *  This method is called on the top level director by the manager
     *  at the start of an execution.
     *  If it returns false (indicating that none of the directors in
     *  the model need write access on the workspace), then the manager
     *  will set the workspace to be read only during each toplevel iteration
     *  of the model.  Note that mutations can still occur, but they can
     *  only be performed by the manager.
     *
     *  @return true If this director, or any of its contained directors,
     *  needs write access to the workspace.
     *  @exception InvalidStateException If the director does not have
     *  a container, or the container is not an instance of CompositeActor.
     */
    public final boolean needWriteAccess() {
        if (_writeAccessRequired()) {
            return true;
        }
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                // find out which of those actors has a local director.
                if (actor instanceof CompositeActor &&
                        ((CompositeActor)actor).isOpaque()) {
                    CompositeActor compositeActor = (CompositeActor) actor;
                    // compositeActor.getDirector() is guaranteed to return a
                    // local director, not the executive director.
                    if (compositeActor.getDirector().needWriteAccess()) {
                        // If any of the directors need a write access, then
                        // everyone has to respect it.
                        return true;
                    }
                }
            }
            // Up to this point, all lower level directors have been queried
            // and none of them returned true (or else we would have returned)
            // Therefore, return false.
            return false;
        } else {
            throw new InvalidStateException("Director is not " +
                    "associated with a composite actor!");
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration.  This method is called by the container of
     *  this director to see whether the director wishes to execute anymore.
     *  It should <i>not</i>, in general, call postfire() on the contained
     *  actors.
     *  <p>
     *  In this base class, return the false if stop() has been called
     *  since preinitialize(), and true otherwise. Derived classes that
     *  override this method need to respect this semantics. The
     *  protected variable _stopRequested indicates whether stop()
     *  has been called.
     *
     *  @return True to continue execution, and false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        return !_stopRequested;
    }

    /** Return true if the director is ready to fire. This method is
     *  called by the container of this director to determine whether the
     *  director is ready to execute. It does <i>not</i>
     *  call prefire() on the contained actors.
     *  If this director is not at the top level of the hierarchy,
     *  and the current time of the enclosing model is greater than the
     *  current time of this director, then this base class updates
     *  current time to match that of the enclosing model.
     *  <p>
     *  In this base class, assume that the director is always ready to
     *  be fired, and so return true. Domain directors should probably
     *  override this method to provide domain-specific behavior.
     *  However, they should call super.prefire() if they
     *  wish to propagate time as done here.
     *
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Director executiveDirector =
                ((Actor)container).getExecutiveDirector();
            if (executiveDirector != null) {
                double outTime = executiveDirector.getCurrentTime();
                if (getCurrentTime() < outTime) {
                    setCurrentTime(outTime);
                }
            }
        }
        return true;
    }

    /** Validate the attributes and then invoke the preinitialize()
     *  methods of all its deeply contained actors.
     *  This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  Time is not set during this stage. So preinitialize() method
     *  of actors should not make use of time. They should wait
     *  until the initialize phase of the execution.
     *  <p>This method also resets the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.
     *  <p>This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        Iterator attributes = attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable)attributes.next();
            attribute.validate();
        }
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                if (_debugging) _debug("Invoking preinitialize(): ",
                        ((NamedObj)actor).getFullName());
                actor.preinitialize();
            }
        }
        _stopRequested = false;
        if (_debugging) _debug("Finished preinitialize().");
    }

    /** Queue an initialization request with the manager.
     *  The specified actor will be initialized at an appropriate time,
     *  between iterations, by calling its preinitialize() and initialize()
     *  methods. This method is called by CompositeActor when an actor
     *  sets its container to that composite actor.  Typically, that
     *  will occur when a model is first constructed, and during the
     *  execute() method of a ChangeRequest.
     *  In this base class, the request is delegated
     *  to the manager. If there is no manager, or if the container
     *  is not an instance of CompositeActor, then do nothing.
     *  @param actor The actor to initialize.
     */
    public void requestInitialization(Actor actor) {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Manager manager = ((CompositeActor)container).getManager();
            if (manager != null) {
                manager.requestInitialization(actor);
            }
        }
    }

    /** Specify the container.  If the specified container is an instance
     *  of CompositeActor, then this becomes the active director for
     *  that composite.  Otherwise, this is an attribute like any other within
     *  the container. If the container is not in the same
     *  workspace as this director, throw an exception.
     *  If this director is already an attribute of the container,
     *  then this has the effect only of making it the active director.
     *  If this director already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then remove it from its container.
     *  This director is not added to the workspace directory, so calling
     *  this method with a null argument could result in
     *  this director being garbage collected.
     *  <p>
     *  If this method results in removing this director from a container
     *  that is a CompositeActor, then this director ceases to be the active
     *  director for that CompositeActor.  Moreover, if the composite actor
     *  contains any other directors, then the most recently added of those
     *  directors becomes the active director.
     *  <p>
     *  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this director and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this director
     *   collides with a name already in the container.  This will not
     *   be thrown if the container argument is an instance of
     *   CompositeActor.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            Nameable oldContainer = getContainer();
            if (oldContainer instanceof CompositeActor
                    && oldContainer != container) {
                // Need to remove this director as the active one of the
                // old container. Search for another director contained
                // by the composite.  If it contains more than one,
                // use the most recently added one.
                Director previous = null;
                CompositeActor castContainer = (CompositeActor)oldContainer;
                Iterator directors =
                    castContainer.attributeList(Director.class).iterator();
                while (directors.hasNext()) {
                    Director altDirector = (Director)directors.next();
                    // Since we haven't yet removed this director, we have
                    // to be sure to not just set it to the active
                    // director again.
                    if (altDirector != this) {
                        previous = altDirector;
                    }
                }
                castContainer._setDirector(previous);
            }

            super.setContainer(container);

            if (container instanceof CompositeActor) {
                // Set cached value in composite actor.
                ((CompositeActor)container)._setDirector(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the current time of the model under this director.
     *  Derived classes will likely override this method to ensure that
     *  the time is valid.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        if (newTime < getCurrentTime()) {
            throw new IllegalActionException(this, "Attempt to move current "
                    + "time backwards. (newTime = " + newTime
                    + ") < (getCurrentTime() = " + getCurrentTime() + ")");
        }
        _currentTime = newTime;
        if (_debugging) {
            _debug("--- Set current time to: " + newTime);
        }
    }

    /** Request that the director cease execution altogether.
     *  This causes a call to stop() on all actors contained by
     *  the container of this director, and sets a flag
     *  so that the next call to postfire() returns false.
     */
    public void stop() {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                actor.stop();
            }
        }
        _stopRequested = true;
    }

    /** Request that execution of the current iteration stop.
     *  In this base class, the request is simply passed on to all actors
     *  that are deeply contained by the container of this director.
     *  For most domains, an iteration is a finite computation, so nothing
     *  further needs to be done here.  However, for some process-oriented
     *  domains, the fire() method of the director is an unbounded computation.
     *  Those domains should override this method so that when it is called,
     *  it does whatever it needs to do to get the fire() method to return.
     *  Typically, it will set flags that will cause all executing threads
     *  to suspend.  These domains should suspend execution in such a way
     *  that if the fire() method is called again, execution will
     *  resume at the point where it was suspended.  However, they should
     *  not assume the fire() method will be called again.  It is possible
     *  that the wrapup() method will be called next.
     *  If the container is not an instance of CompositeActor, then this
     *  method does nothing.
     */
    public void stopFire() {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                actor.stopFire();
            }
        }
    }

    /** Terminate any currently executing model with extreme prejudice.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
     *  method instead. This method should be called only
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  There is no assurance that the topology will be in a consistent
     *  state after this method returns.  The
     *  topology should probably be recreated before attempting any
     *  further operations.
     *  <p>
     *  This base class recursively calls terminate() on all actors deeply
     *  contained by the container of this director. Derived classes should
     *  override this method to release all resources in use and kill
     *  any sub-threads.  Derived classes should not synchronize this
     *  method because it should execute as soon as possible.
     *  If the container is not an instance of CompositeActor, then
     *  this method does nothing.
     *  <p>
     */
    public void terminate() {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                actor.terminate();
            }
        }
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside.  The implementation in this
     *  base class defers to the domainPolymorphicTransferInputs
     *  method to transfer at most one token.  Derived classes may override
     *  this method to transfer a domain-specific number of tokens.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @see IOPort#transferInputs
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        return domainPolymorphicTransferInputs(port);
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The implementation
     *  in this base class defers to the
     *  domainPolymorphicTransferOutputs method to transfer at most
     *  one token.  Derived classes may override this method to
     *  transfer a domain-specific number of tokens.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @see IOPort#transferOutputs
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        return domainPolymorphicTransferOutputs(port);
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container.   In this base class wrapup() is called on the
     *  associated actors in the order of their creation.  If the container
     *  is not an instance of CompositeActor, then this method does nothing.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                actor.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            // FIXME: Add director-specific information here, like
            // what is the state of the director.
            // if ((detail & FIXME) != 0 ) {
            //  if (result.trim().length() > 0) {
            //      result += " ";
            //  }
            //  result += "FIXME {\n";
            //  result += _getIndentPrefix(indent) + "}";
            // }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor.
     */
    protected boolean _isEmbedded() {
        return (getContainer() != null &&
                getContainer().getContainer() != null);
    }

    /** Return true if this is a top-level director.
     *  Parts of this method is read synchronized on the workspace.
     * @return True if this director is at the top-level.
     */
    protected boolean _isTopLevel() {
        CompositeActor container = (CompositeActor)getContainer();
        if (container.getExecutiveDirector() == null) {
            return true;
        } else {
            return false;
        }
    }

    /** Return true if this director requires write access
     *  on the workspace during execution. Most director functions
     *  during execution do not need write access on the workspace.
     *  A director will generally only need write access on the workspace if
     *  it performs mutations locally, instead of queueing them with the
     *  manager.
     *  <p>
     *  In this base class, we assume
     *  that write access is required and always return true.  This method
     *  should probably be overridden by derived classes.
     *
     *  @return true
     */
    protected boolean _writeAccessRequired() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The current time of the model. */
    protected double _currentTime = 0.0;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add an XML graphic as a hint to UIs for rendering the director.
    private void _addIcon() {
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-15\" "
                + "width=\"100\" height=\"30\" "
                + "style=\"fill:green\"/>\n" +
                "</svg>\n");
    }
}
