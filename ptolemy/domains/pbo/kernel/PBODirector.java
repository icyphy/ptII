/* A PBODirector governs the execution of a PBO composite actor.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.pbo.kernel;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PBODirector
/**
This class implements the Port-based object model of computation.
This model of computation is not data driven, like most of the other
ptolemy domains.  Instead, it relies on analysis and user tuning of the
scheduling to ensure that no important data is lost (or that lost data is
not critical to the execution of the system.  This gives good real-time
performance, and this model of computation is often used in embedded control
systems.

Communication between port-based objects is *NOT* based on FIFO queues!
Instead, the processes have a concept of a shared memory.   The shared
memory is synchronized across processes that may be executing concurrently,
so that an object is always guaraunteed to have the most recent consistent
available state of its inputs when it fires.   Note that in this communication
model, data may be lost!

Port-based objects are normally associated with independant processes.
However,
unlike Process Networks, the threads are time-driven, instead of data-driven.
Each actor has an associated firing period, which determines how often
it will execute.  When a process is scheduled, it reads the current state
of its input ports, performs calculation, and then sets the state of its
output ports.   Because of this model, the calculated output during any
firing is dependant on the arrival of events, which results in
non-deterministic output.  Some effort must be expended at some point in the
design of a system using this domain in properly choosing the firing
periods of each actor.

Instead of associating a process with each actor, this director manually
schedules the actors.  This can be useful for simulating execution on
a target architecture that is different from the development machine.  In the
future, a director based on actor/process could be developed that would allow
the fastest execution on a given architecture.

The scheduling of actors is purposefully handled in an informal way.  Each
actor specifies its firing period through the "firingPeriod" parameter.  The
scheduler attempts to fire the actor every time that period expires.
This director implements a very simple scheduler, and just fires the actor.
This is sufficient, since only zero-delay actors are currently supported.
If delays are allowed, then more complex schedulers are preferable, since
some actors may require a very low latency when they are fired.  In such a case
priority scheduling of various kinds is probably necessary.

This director maintains a calendar queue of all the actors in the
simulation, and fires them in the order given by the queue.  This director
creates receivers of class PBOReceiver, which implements the shared memory
communication.

Only zero-delay actors are currently supported.  (This is bogus.  The
scheduling should be frequent enough that actors have a significant latency.
Also, when scheduling on a single processor, the processor is a resource that
must be distributed among all the actors.)

@author Steve Neuendorffer
@version $Id$
*/
public class PBODirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public PBODirector() {
        super();
	_init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public PBODirector(Workspace workspace) {
        super(workspace);
	_init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public PBODirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        container.setDirector(this);
	_init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        PBODirector newobj = (PBODirector)super.clone(ws);
	newobj.stopTime = (Parameter) newobj.getAttribute("stopTime");
        return newobj;
    }

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this director.  In general, this may be called more
     *  than once in the same iteration of the director's container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  <p>
     *  In this base class, an attempt is made to fire each actor exactly
     *  once, in the order they were created.  Prefire is called once, and
     *  if prefire returns true, then fire is called once, followed by
     *  postfire.  The return value from postfire is ignored.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    public void fire() throws IllegalActionException {
	// advance the current time, if necessary.
	if(getNextIterationTime() > getCurrentTime())
	    setCurrentTime(getNextIterationTime());

	_debug("Starting iteration at " + getCurrentTime());
	double firingTime = getCurrentTime();
	double desiredFiringTime =
	    ((Double)_startQueue.getNextKey()).doubleValue();
	boolean postfireReturns;
	// get the next actor to fire.
	Actor actor = (Actor)_startQueue.take();
	if(actor.prefire()) {
	    _debug("Firing actor " + ((Entity) actor).getFullName() +
		   " at " + firingTime);
	    actor.fire();
	    // This is the time that the actor next wants to get fired.
	    Double refireTime = new Double(desiredFiringTime +
					   _getFiringPeriod(actor));

	    setCurrentTime(firingTime + _getDelay(actor));
	    _debug("Postfiring actor at " + getCurrentTime());
	    postfireReturns = actor.postfire();
	    _debug("done firing");

	    if(postfireReturns) {
		_debug("Rescheduling actor at " + refireTime);
		// reschedule the actor's next firing.
		_startQueue.put(refireTime, actor);
	    }
	}

	// reschedule this composite to handle the next process starting.
	CompositeActor container = (CompositeActor)getContainer();
	Director executive = container.getExecutiveDirector();
	if(executive != null) {
	    _debug("Rescheduling composite");
	    executive.fireAt(container, getNextIterationTime());
	}
    }

    /** Schedule a firing of the given actor at the given time. It does
     *  nothing in this base class. Derived classes
     *  should override this method.
     *  <p>
     *  Note that this method is not made abstract to facilitate the use
     *  of the test suite.
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
        // to run Tcl Blend testscript on this class.

    }

    /** Return the next time of interest in the model being executed by
     *  this director. This method is useful for domains that perform
     *  speculative execution (such as CT).  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute.
     *  <p>
     *  In this class, we return the time that the next actor will fire.  This
     *  time should always be greater than or equal to the current time.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
	try {
	    return ((Double) _startQueue.getNextKey()).doubleValue();
	}
	catch (IllegalActionException e) {
	    // This should never happen, because there should always be stuff
	    // in the event queue, but it is always safe to return current
	    // time, so just do that.
	    _debug("PBODirector.getNextIterationTime: " +
		   "case should never happen");
	    return getCurrentTime();
	}
    }

    /** Create receivers and then invoke the initialize()
     *  methods of all its deeply contained actors.
     *  Set the current time to be 0.0.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();

	/** Initialize the queue of next firings to contain all the actors.
	 */
	CompositeActor container = (CompositeActor) getContainer();
	if(container != null) {
	    Enumeration allActors = container.deepGetEntities();
	    while(allActors.hasMoreElements()) {
		Actor actor = (Actor) allActors.nextElement();
		_startQueue.put(new Double(0.0), actor);
	    }
	} else {
	    throw new IllegalActionException("Cannot fire this director " +
					     "without a container");
	}
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new PBOReceiver();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration.  This method is called by the container of
     *  this director to see if the director wishes to execute anymore, and
     *  should <i>not</i>, in general, just take the logical AND of calling
     *  postfire on all the contained actors.
     *  <p>
     *  Return true if the current time has exceeded the time given in the
     *  stoptime parameter.
     *
     *  @exception IllegalActionException If the postfire()
     *  method of one of the associated actors throws it.
     */
    public boolean postfire() throws IllegalActionException {
	_debug("postfiring");
        double stoptime = ((DoubleToken) stopTime.getToken()).doubleValue();
	double curtime = getCurrentTime();
	_debug("CurrentTime = " + curtime);
	if(curtime > stoptime)
	    return false;
	else
	    return true;
    }

    /** Return true if the director is ready to fire. This method is
     *  called but the container of this director to determine if the
     *  director is ready to execute, and
     *  should <i>not</i>, in general, just take the logical AND of calling
     *  prefire on all the contained actors.
     *  <p>
     *  In this base class, assume that the director is always ready to
     *  be fired, so return true. Domain directors should probably
     *  override this method.
     *
     *  @return true
     *  @exception IllegalActionException If the postfire()
     *  method of one of the associated actors throws it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Queue a change request with the manager.
     *  The indicated change will be executed at the next opportunity,
     *  typically between top-level iterations of the model.
     *  If there is no container, or if it has no manager, do nothing.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Manager manager = container.getManager();
            if (manager != null) {
                manager.requestChange(change);
            }
        }
    }

    public Parameter stopTime;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this director requires write access
     *  on the workspace during execution. Most director functions
     *  during execution do not need write access on the workpace.
     *  A director will generally only need write access on the workspace if
     *  it performs mutations locally, instead of queueing them with the
     *  manager.
     *  <p>
     *  Since PBO does not do mutations locally, return true.
     *
     *  @return true
     */
    protected boolean _writeAccessRequired() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    /** Initialize the local data members of this actor.
     *  This is called from the constructors for this object.
     */
    private void _init() {
	_startQueue = new CalendarQueue(new DoubleCQComparator());
	try {
	    stopTime = new Parameter(this, "stopTime", new DoubleToken(10.0));
	}
	catch (Exception e) {
	    // this should never happen
	    throw new InternalErrorException(e.getMessage());
	}
    }

    /** Get the period that the given actor will fire, as supplied by
     *  by the port's "firingPeriod" Parameter.
     *
     *  @exception IllegalActionException If the Actor does not implement
     *  the nameable interface, if the firingPeriod
     *  parameter does not exist, or it has an invalid expression.
     */
    private double _getFiringPeriod(Actor a)
            throws IllegalActionException {
	if(!(a instanceof Nameable))
	    throw new IllegalActionException(
		"Cannot get the firing period for an actor that is not " +
		"an entity");
        Parameter param =
	    (Parameter)((ComponentEntity)a).getAttribute("firingPeriod");
	if(param == null) {
	    throw new IllegalActionException("Actor does not have a " +
		"firingPeriod parameter");
	}
	return ((DoubleToken)param.getToken()).doubleValue();
    }

    /** Get the delay between the inputs and the outputs for a given actor.
     *  When the actor is fired, it is considered to be "active" for this
     *  amount of time, after which it will create new output values and
     *  pause execution.
     *
     *  @exception IllegalActionException If the Actor does not implement
     *  the nameable interface, if the delay
     *  parameter does not exist, or it has an invalid expression.
     */
    private double _getDelay(Actor a)
            throws IllegalActionException {
	if(!(a instanceof Nameable))
	    throw new IllegalActionException(
		"Cannot get the delay for an actor that is not " +
		"an entity.");
        Parameter param =
	    (Parameter)((ComponentEntity)a).getAttribute("delay");
	if(param == null) {
	    throw new IllegalActionException("Actor does not have a " +
		"delay parameter.");
	}
	return ((DoubleToken)param.getToken()).doubleValue();
    }

    private CalendarQueue _startQueue;
    private CalendarQueue _stopQueue;
}
