/* A PBODirector governs the execution of a PBO composite actor.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.pbo.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.CQComparator;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// PBODirector
/**
This class implements the Port-based object model of computation.
This model of computation is not data driven, like most of the other
ptolemy domains.  Instead, it relies on analysis and user tuning of the
scheduling to ensure that no important data is lost (or that lost data is
not critical to the execution of the system).  This gives good real-time
performance, and this model of computation is often used in embedded control
systems.
<p>
Communication between port-based objects is *NOT* based on FIFO queues!
Instead, the processes have a concept of a shared memory.   The shared
memory is synchronized across processes that may be executing concurrently,
so that an object is always guaranteed to have the most recent consistent
available state of its inputs when it fires.   Note that in this communication
model, data may be lost!
<p>
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
<p>
Instead of associating a process with each actor, this director manually
schedules the actors.  This can be useful for simulating execution on
a target architecture that is different from the development machine.  In the
future, a director based on actor/process could be developed that would allow
the fastest execution on a given architecture.
<p>
The scheduling of actors is purposefully handled in an informal way.  Each
actor specifies the period between firings using its
<i>executionPeriod</i> parameter.  The
scheduler attempts to fire the actor every time that period expires.
The time spent during firing of the actor is given by its <i>executionTime</i>
parameter.
<p>
This director schedules each process using a non-preemptive
earliest deadline first strategy.
Note that this means the currentTime of the director will advance
by the <i>executionTime</i> parameter of an actor when the actor is fired.
This is not the optimal strategy in terms of processor utilization, but it
is deterministic and very much simpler than trying to deal with preeemptive
modeling.  In such a case priority scheduling of some kind is
probably necessary.
<p>
This director maintains a calendar queue of all the actors in the
simulation, and fires them in the order given by the queue.  This director
creates receivers of class PBOReceiver, which implements the shared memory
communication.

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
    public PBODirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        if (_deadlineQueue.isEmpty()) {
            // nothing is currently waiting, so update time to the
            // make the next actor ready to execute.
            PBOEvent event = (PBOEvent)_requestQueue.take();
            Actor actor = event.actor();
            double requestTime = event.time();
            // This is the time that the actor next wants to get fired.
            double nextRequestTime =
                requestTime + _getExecutionPeriod(actor);
            double deadlineTime = requestTime + _getExecutionTime(actor);
            _requestQueue.put(new PBOEvent(actor, nextRequestTime));
            _deadlineQueue.put(new PBOEvent(actor, deadlineTime));
            setCurrentTime(requestTime);
            return;
        }

        _debug("Starting iteration at " + getCurrentTime());
        PBOEvent deadlineEvent = (PBOEvent)_deadlineQueue.take();
        Actor executingActor = deadlineEvent.actor();
        // The time the actor expected to finish by.
        double executingTime = deadlineEvent.time();
        // The time the actor actually started executing.
        double firingTime = getCurrentTime();
        // The time the actor actually finished.
        double endFiringTime = getCurrentTime() +
            _getExecutionTime(executingActor);

        // first process any new activations that will
        // occur before endFiringTime.
        double requestTime = ((PBOEvent)_requestQueue.get()).time();
        while (requestTime < endFiringTime) {
            // make the given actor ready to execute.
            PBOEvent event = (PBOEvent)_requestQueue.take();
            Actor actor = (Actor)event.actor();
            // This is the time that the actor next wants to get fired.
            double nextRequestTime =
                requestTime + _getExecutionPeriod(actor);
            double deadlineTime = requestTime + _getExecutionTime(actor);
            _requestQueue.put(new PBOEvent(actor, nextRequestTime));
            _deadlineQueue.put(new PBOEvent(actor, deadlineTime));
            // get the next requested time
            requestTime = ((PBOEvent)_requestQueue.get()).time();
        }

        // now fire the currently executing actor and update
        // time to reflect the amount of time spent.
        boolean postfireReturns;
        if (executingActor.prefire()) {
            _debug("Firing actor " + ((Entity) executingActor).getFullName() +
                    " at " + firingTime);
            executingActor.fire();

            // This is the time when the actor finishes.
            setCurrentTime(endFiringTime);
            _debug("Postfiring actor at " + getCurrentTime());
            postfireReturns = executingActor.postfire();
            _debug("done firing");

        } else {
            _debug("Actor " + ((Entity) executingActor).getFullName() +
                    " is not ready to fire.");
        }

        // reschedule this composite to handle the next process starting.
        CompositeActor container = (CompositeActor)getContainer();
        Director executive = container.getExecutiveDirector();
        if (executive != null) {
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
        if (_deadlineQueue.isEmpty()) {
            // This should never be empty.
            PBOEvent requestEvent = (PBOEvent)_requestQueue.get();
            return requestEvent.time();
        } else {
            PBOEvent deadlineEvent = (PBOEvent)_deadlineQueue.get();
            PBOEvent requestEvent = (PBOEvent)_requestQueue.get();
            return Math.min(deadlineEvent.time(), requestEvent.time());
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

        _deadlineQueue.clear();
        _requestQueue.clear();
        //Initialize the queue of deadlines and next firings to
        // contain all the actors.
        CompositeActor container = (CompositeActor) getContainer();
        if (container != null) {
            Iterator allActors = container.deepEntityList().iterator();
            while (allActors.hasNext()) {
                Actor actor = (Actor) allActors.next();
                _deadlineQueue.put(new PBOEvent(actor,
                        _getExecutionTime(actor)));
                _requestQueue.put(new PBOEvent(actor,
                        _getExecutionPeriod(actor)));
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
        if (curtime > stoptime)
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
        _requestQueue = new CalendarQueue(new PBOCQComparator());
        _deadlineQueue = new CalendarQueue(new PBOCQComparator());
        try {
            stopTime = new Parameter(this, "stopTime", new DoubleToken(10.0));
        }
        catch (Exception e) {
            // this should never happen
            throw new InternalErrorException(e.getMessage());
        }

        addDebugListener(new StreamListener());
    }

    /** Get the period that the given actor will fire, as supplied by
     *  by the port's "firingPeriod" Parameter.
     *
     *  @exception IllegalActionException If the Actor does not implement
     *  the nameable interface, if the firingPeriod
     *  parameter does not exist, or it has an invalid expression.
     */
    private double _getExecutionPeriod(Actor a)
            throws IllegalActionException {
        if (!(a instanceof Nameable))
            throw new IllegalActionException(
                    "Cannot get the execution period for an actor that "
                    + "is not an entity");
        Parameter param =
            (Parameter)((ComponentEntity)a).getAttribute("executionPeriod");
        if (param == null) {
            throw new IllegalActionException("Actor does not have a " +
                    "executionPeriod parameter");
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
    private double _getExecutionTime(Actor a)
            throws IllegalActionException {
        if (!(a instanceof Nameable))
            throw new IllegalActionException(
                    "Cannot get the executionTime for an actor that is not " +
                    "an entity.");
        Parameter param =
            (Parameter)((ComponentEntity)a).getAttribute("executionTime");
        if (param == null) {
            throw new IllegalActionException("Actor does not have an " +
                    "executionTime parameter.");
        }
        return ((DoubleToken)param.getToken()).doubleValue();
    }

    // The queue of times when actors will start, ordered by deadline times.
    private CalendarQueue _deadlineQueue;
    // The queue of times when actors will next become ready, ordered
    // by request times.
    private CalendarQueue _requestQueue;

    // An implementation of the CQComparator interface for use with
    // calendar queue that compares two PBOEvents according to their
    // time stamps, microstep, and depth in that order.
    // One PBOEvent is said to be earlier than another, if it has
    // a smaller time stamp, or when the time stamps are identical,
    // it has a smaller microstep, or when both time stamps and
    // microsteps are identical, it has a smaller depth.
    //
    // The default binWidth is 1.0, and the default zeroReference is 0.0.
    //
    private class PBOCQComparator implements CQComparator {

        /** Compare the two argument for order. Return a negative integer,
         *  zero, or a positive integer if the first argument is less than,
         *  equal to, or greater than the second.
         *  Both arguments must be instances of PBOEvent or a
         *  ClassCastException will be thrown.  The compareTo() method
         *  of the first argument is used to do the comparison.
         *
         * @param object1 The first event.
         * @param object2 The second event.
         * @return A negative integer, zero, or a positive integer if the first
         *  argument is less than, equal to, or greater than the second.
         * @exception ClassCastException If one of the arguments is not
         *  an instance of PBOEvent.
         */
        public final int compare(Object object1, Object object2) {
            return((PBOEvent) object1).compareTo(object2);
        }

        /** Given an event, return the virtual index of
         *  the bin that should contain the event.
         *  If the argument is not an instance of PBOEvent, then a
         *  ClassCastException will be thrown.  Only the time stamp
         *  of the arguments is used.  The quantity returned is the
         *  quantized time stamp, i.e. the
         *  difference between the time stamp of the event and that of
         *  the zero reference, divided by the time stamp of the bin width.
         *  @param event The event.
         *  @return The index of the virtual bin containing the event.
         *  @exception ClassCastException If the argument is not
         *   an instance of PBOEvent.
         */
        public final long getVirtualBinNumber(Object event) {
            return (long)((((PBOEvent) event).time()
                    - _zeroReference.time())/_binWidth.time());
        }

        /** Given an array of PBOEvent objects, set an appropriate bin
         *  width. This method assumes that the
         *  entries provided are all different, and are in increasing order.
         *  Note, however, that the time stamps may not be increasing.
         *  It may instead be the receiver depth that is increasing,
         *  or the microsteps that are increasing.
         *  This method attempts to choose the bin width so that
         *  the average number of entries in a bin is one.
         *  If the argument is null or is an array with length less
         *  than two, set the bin width to the default, which is 1.0
         *  for this implementation.
         *
         *  @param entryArray An array of PBOEvent objects.
         *  @exception ClassCastException If an entry in the array is not
         *   an instance of PBOEvent.
         */
        public void setBinWidth(Object[] entryArray) {

            if ( entryArray == null || entryArray.length < 2) {
                _zeroReference = new PBOEvent(null, 0.0);
                return;
            }

            double[] diff = new double[entryArray.length - 1];

            double average =
                (((PBOEvent)entryArray[entryArray.length - 1]).time() -
                        ((PBOEvent)entryArray[0]).time()) /
                (entryArray.length-1);
            double effectiveAverage = 0.0;
            int effectiveSamples = 0;
            for (int i = 0; i < entryArray.length - 1; ++i) {
                diff[i] = ((PBOEvent)entryArray[i+1]).time() -
                    ((PBOEvent)entryArray[i]).time();
                if (diff[i] < 2.0 * average) {
                    effectiveSamples++;
                    effectiveAverage += diff[i];
                }
            }

            if (effectiveAverage == 0.0 || effectiveSamples == 0) {
                // To avoid setting NaN or 0.0
                // for the width, apparently due to simultaneous events,
                // we leave it unchanged instead.
                return;
            }
            effectiveAverage /= (double)effectiveSamples;
            _binWidth = new PBOEvent(null, 3.0 * effectiveAverage);
        }

        /** Set the zero reference, to be used in calculating the virtual
         *  bin number. The argument should be a PBOEvent, otherwise a
         *  ClassCastException will be thrown.
         *
         *  @exception ClassCastException If the argument is not an instance
         *   of PBOEvent.
         */
        public void setZeroReference(Object zeroReference) {
            _zeroReference = (PBOEvent) zeroReference;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////

        // The bin width.
        private PBOEvent _binWidth = new PBOEvent(null, 1.0);

        // The zero reference.
        private PBOEvent _zeroReference = new PBOEvent(null, 0.0);
    }


    public class PBOEvent implements Comparable {

        /** Construct an event with the specified entity and time.
         *  @param actor The actor.
         *  @param time The time associated with the actor.
         */
        public PBOEvent(Actor actor, double time) {
            _actor = actor;
            _time = time;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return the destination actor for this event.
         *  @return The destination actor.
         */
        public Actor actor() {
            return _actor;
        }

        /** Compare the tag of this event with the specified event for order.
         *  See compareTo(PBOEvent event) for the comparison rules.
         *  The argument has to be an instance of PBOEvent or a
         *  ClassCastException will be thrown.
         *
         * @param event The event to compare against.
         * @return -1, 0, or 1, depends on the order of the events.
         * @exception ClassCastException If the argument is not an instance
         *  of PBOEvent.
         */
        public final int compareTo(Object event) {
            return compareTo((PBOEvent)event);
        }

        /** Compare the tag of this event with the specified event for order.
         *  Return -1, zero, or +1 if this
         *  event is less than, equal to, or greater than the specified event.
         *  The time stamp is checked first.  If the two time stamps are
         *  identical, then the microstep is checked.  If those are identical,
         *  then the receiver depth is checked.
         *
         * @param event The event to compare against.
         * @return -1, 0, or 1, depends on the order of the events.
         */
        public final int compareTo(PBOEvent event) {

            if ( _time > event._time)  {
                return 1;
            } else if ( _time < event._time) {
                return -1;
            } else
                return 0;

        }

        /** Compare the tag of this event with the specified and return true
         *  if they are equal and false otherwise.  This is provided along
         *  with compareTo() because it is slightly faster when all you need
         *  to know is whether the events are simultaneous.
         *  @param event The event to compare against.
         */
        public boolean isSimultaneousWith(PBOEvent event) {
            return ( _time == event._time);
        }

        /** Return the time stamp.
         *  @return The time stamp.
         */
        public double time() {
            return _time;
        }

        /** Return a description of the event, including the contained token
         *  (or "null" if there is none) and the time stamp.
         *  @return The token as a string with the time stamp.
         */
        public String toString() {
            return "PBOEvent(time=" + _time + ", dest="
                + ((NamedObj)_actor).getFullName() + ")";
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        // The destination actor.
        private Actor _actor;

        // The time stamp of the event.
        private double _time;
    }
}
