/* An  HDE domain director.

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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
Review transferOutputs().
*/

package ptolemy.domains.hde.kernel;
import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// DEDirector

/**
This director implements the discrete-event model of computation (MoC)
with memoried signals. It extends the  DE Director.
It should be used as the local director of a CompositeActor that is
to be executed according to this MoC. This director maintain a notion
of current time, and processes events chronologically in this time.
An <i>event</i> is a token with a time stamp.  Much of the sophistication
in this director is aimed at handling simultaneous events intelligently,
so that deterministic behavior can be achieved.
<p>
The bottleneck in a typical DE simulator is in the maintenance of the
global event queue. By default, a DE director uses the calendar queue
as the global event queue. This is an efficient algorithm
with O(1) time complexity in both enqueue and dequeue operations.
<p>
Sorting in the CalendarQueue class is done according to the order
defined by the DEEvent class, which implements the java.lang.Comparable
interface. A DE event has a time stamp, a microstep, and a depth.
The time stamp indicates the time when the event occurs.
The microstep represents the phase of execution
when processing simultaneous events in directed loops, or when an
actor schedules itself for firing later at the current time
(using fireAt()).
The depth is the index of the destination actor in a topological
sort.  A larger value of depth represents a lower priority when
processing events.  The depth is determined by topologically
sorting the actors according to data dependencies over which there
is no time delay. Note that the zero-delay data dependencies are
determined on a per port basis.
<p>
Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
should be used whenever an actor introduces time delays between the
inputs and the outputs. When an ordinary IOPort is used, the director
assumes, for the purpose of calculating priorities, that the delay
across the actor is zero. On the other hand, when DEIOPort is used,
the delay across the actor can be declared to be non-zero by calling
the delayTo() method on output ports.
<p>
Directed loops with no delay actors are not permitted; they would make it
impossible to assign priorities.  Such a loop can be broken by inserting
an instance of the Delay actor.  If zero delay around the loop is
truly required, then simply set the <i>delay</i> parameter of that
actor to zero.
<p>
Input ports in a DE model contain instances of DEReceiver.
When a token is put into a DEReceiver, that receiver enqueues the
event to the director  by calling the _enqueueEvent() method of
this director.
This director sorts all such events in a global event queue
(a priority queue).
<p>The HDEDirector employs a special HDE receiver which stores
the most recent values of the inputs of an actor and reapplies
them whenever an actor input changes.
<p>
An iteration, in the DE domain, is defined as processing all
the events whose time stamp equals to the current time of the director.
At the beginning of the fire() method, this director dequeues
a subset of the oldest events (the ones with smallest time
stamp, microstep, and depth) from the global event queue,
and puts those events into
their destination receivers. The actor(s) to which these
events are destined are the ones to be fired.  The depth of
an event is the depth of the actor to which it is destined.
The depth of an actor is its position in a topological sort of the graph.
The microstep is usually zero, but is incremented when a pure event
is queued with time stamp equal to the current time.
<p>
The actor that is fired must consume tokens from
its input port(s), and will usually produce new events on its output
port(s). These new events will be enqueued in the global event queue
until their time stamps equal the current time.  It is important that
the actor actually consume tokens from its inputs, even if the tokens are
solely used to trigger reactions. This is how polymorphic actors are
used in the DE domain. The actor will
be fired repeatedly until there are no more tokens in its input
ports with the current time stamp.  Alternatively, if the actor
returns false in prefire(), then it will not be invoked again
in the same iteration even if there are events in its receivers.
<p>
A model starts from the time specified by <i>startTime</i>, which
has default value 0.0
<P>
The stop time of the execution can be set using the
<i>stopTime</i> parameter. The parameter has default value
Double.MAX_VALUE, which means the execution stops
only when the model time reaches that (rather large) number.
<P>
Execution of a DE model ends when the time stamp of the oldest events
exceeds a preset stop time. This stopping condition is checked inside
the prefire() method of this director. By default, execution also ends
when the global event queue becomes empty. Sometimes, the desired
behaviour is for the director to wait on an empty queue until another
thread makes new events available.  For example, a DE actor may produce
events when a user hits a button on the screen. To prevent ending the
execution when there are no more events, set the
<i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
<p>
Parameters, <i>isCQAdaptive</i>, <i>minBinCount</i>, and
<i>binCountFactor</i>, are
used to configure the calendar queue. Changes to these parameters
are ignored when the model is running.
<p>
If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
then the director not process events until the real time elapsed
since the model started matches the time stamp of the event.
This ensures that the director does not get ahead of real time,
but, of course, it does not ensure that the director keeps up with
real time.
<p>
This director tolerates changes to the model during execution.
The change should be queued with a component in the hierarchy using
requestChange().  While invoking those changes, the method
invalidateSchedule() is expected to be called, notifying the director
that the topology it used to calculate the priorities of the actors
is no longer valid.  This will result in the priorities being
recalculated the next time prefire() is invoked.
<p>
However, there is one subtlety.  If an actor produces events in the
future via DEIOPort, then the destination actor will be fired even
if it has been removed from the topology by the time the execution
reaches that future time.  This may not always be the expected behavior.
The Delay actor in the DE library behaves this way.

@author Lukito Muliadi, Edward A. Lee, Jie Liu, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.domains.de.kernel.DEReceiver
@see ptolemy.actor.util.CalendarQueue
*/

public class HDEDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HDEDirector() {
        this(null);
    }

    /**  Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public HDEDirector(Workspace workspace) {
        super(workspace);

    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     * @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in
     *   the container.
     */
    public HDEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Advance current time to the next event in the event queue,
     *  and fire one or more actors that have events at that time.
     *  If <i>synchronizeToRealTime</i> is true, then before firing,
     *  wait until real time matches or exceeds the time stamp of the event.
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens at the current time, or
     *  its prefire() method returns false. If there are no events in the
     *  event queue, then the behavior depends on the
     *  <i>stopWhenQueueIsEmpty</i> parameter.  If it is false,
     *  then this thread will stall until events
     *  become available on the event queue.  Otherwise, time will advance
     *  to the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {
        _stopRequested = false;
        while (true) {

            Actor actorToFire = _dequeueEvents();
            if (actorToFire == null) {
                // There is nothing more to do.
                if (_debugging) _debug("No more events on the event queue.");
                _noMoreActorsToFire = true;
                return;
            }
            // It is possible that the next event to be processed is on
            // an inside receiver of an output port of an opaque composite
            // actor containing this director.  In this case, we simply
            // return, giving the outside domain a chance to react to
            // event.
            if (actorToFire == getContainer()) {
                return;
            }

            // Fire an actor exactly once, when it has a waiting event.
            // NOTE: There are enough tests here against the
            // _debugging variable that it makes sense to split
            // into two duplicate versions.
            if (_debugging) {
                // Debugging. Report everything.
                if (((Nameable)actorToFire).getContainer() == null) {
                    _debug("Actor has no container. Disabling actor.");
                    _disableActor(actorToFire);
                    break;
                }
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_PREFIRE));
                if (!actorToFire.prefire()) {
                    _debug("*** Prefire returned false.");
                    break;
                }
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_PREFIRE));
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_FIRE));
                actorToFire.fire();
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_FIRE));
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.BEFORE_POSTFIRE));
                if (!actorToFire.postfire()) {
                    _debug("*** Postfire returned false:",
                            ((Nameable)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                }
                _debug(new FiringEvent(this, actorToFire,
                        FiringEvent.AFTER_POSTFIRE));
            } else {
                // Not debugging.
                if (((Nameable)actorToFire).getContainer() == null) {
                    _disableActor(actorToFire);
                    break;
                }
                if (!actorToFire.prefire()) {
                    break;
                }
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                    //break;
                }
            }


            // Check whether the next time stamp is equal to current time.
            synchronized(_eventQueue) {
                if (!_eventQueue.isEmpty()) {
                    DEEvent next = _eventQueue.get();
                    // If the next event is in the future,
                    // proceed to postfire().

                    if (next.timeStamp() > getCurrentTime()) {
                        break;
                    } else if (next.timeStamp() < getCurrentTime()) {
                        throw new InternalErrorException(
                                "fire(): the time stamp of the next event "
                                + next.timeStamp() + " is smaller than the "
                                + "current time " + getCurrentTime() + " !");
                    }
                } else {
                    // The queue is empty, proceed to postfire().
                    break;
                }
            }
        }
    }

    /** Return a new receiver of a type HDEReceiver.
     *  @return A new HDEReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging) _debug("Creating new HDE receiver.");
        return new HDEReceiver();
    }
}
