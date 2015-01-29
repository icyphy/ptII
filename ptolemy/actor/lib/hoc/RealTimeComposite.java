/* An actor that iterates a contained actor over input arrays.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// RealTimeComposite

/**
 This is a container for another actor that fires that other actor
 at real times corresponding to the input time stamps. Its
 ports are those of the contained actor. Given one or more events
 with time stamp <i>t</i> at the input ports, it queues the events
 to provide to a firing of the contained actor that is deferred to
 occur when real time (since start of execution, in seconds) exceeds
 or matches <i>t</i>.  If real time already exceeds <i>t</i>, then the firing
 may occur immediately.
 <p>
 In addition to the parameters of the contained actor, this actor
 has a <i>delay</i> parameter. The value of this parameter is
 the minimum delay (in model time) between an input event and
 an output event that results from that input event.
 If the enclosed actor produces no output, or if the time
 of the outputs can be arbitrarily whatever current time
 is in the model when they are produced, then <i>delay</i>
 should be set to <i>UNDEFINED</i>. This is the default value.
 With this value, the enclosed actor is
 executed in a separate thread.
 If the firing produces output events, then those are given time
 stamps equal to the greater of the current model time of the
 enclosing model and the current real time at which the outputs
 are produced (in seconds since the start of execution). In
 this case, the enclosed actor
 does not regulate in any way the passage of time of the
 enclosing model, so the time stamps of the enclosing model
 could get arbitrarily far ahead of real time.
 <p>
 If the value of <i>delay</i> is 0.0 (zero), then the inside
 model is run in the same thread as the enclosing model.
 When this RealTimeComposite fires, the fire() method stalls
 until real time matches the current time of the model, and
 then invokes the enclosed model. If the enclosed model produces
 any outputs, then those outputs have time stamps equal to the
 time stamps of the input. Hence, from the perspective of DE
 semantics, this actor has zero delay, even though it can
 introduce real-time delay (which is indistinguishable from
 just taking a long time to evaluate the fire() method).
 Note that with <i>delay</i> = 0.0, this actor affects the
 model in way similar to the <i>synchronizeToRealTime</i>
 parameter of the director, except that only the events
 provided to this actor are synchronized to real time, rather
 than all events.
 <p>
 If the value of <i>delay</i> is positive, then the inside
 model is run in a separate thread, just as if the value
 were UNDEFINED, but in this case, this actor does
 regulate the passage of time of the enclosing model.
 In particular, given an event with time stamp <i>t</i>
 it prevents model time from advancing past <i>t</i>
 + <i>delay</i> until the firing triggered by the event
 has completed (which will be at some real time greater
 than <i>t</i>). Any outputs produced by that firing are
 assigned time stamps equal to the greater of <i>t</i>
 + <i>delay</i> and the current real time at which the
 output is produced.
 <p>
 For various reasons, this actor is tricky to use. The most natural
 domain to use it in is DE, providing it with input events with time
 stamps that specify when to perform some action, such as an actuator
 or display action. However, if the DE system is an open-loop system,
 then model time of the DE system can get very far ahead of the
 RealTimeComposite. It is helpful to use a feedback loop including
 this RealTimeComposite to keep the DE model from getting ahead,
 and to use the <i>delay</i> parameter judiciously as explained
 above.
 <p>
 This actor may also be used in SDF and SR if the <i>period</i> parameter
 of the director is set to something greater than zero.
 This actor consumes its inputs and schedules execution in
 its postfire() method, and hence in SR will behave as a strict
 actor (all inputs must be known for anything to happen).
 <p>
 FIXME: For actors that are triggered by internal calls to fireAt(),
 it seems that the delay needs to be no larger than the smallest
 increment between calls to fireAt(). Is this correct?  Why?
 <p>
 FIXME: If there is a PortParameter, the parameter gets updated when the
 fire() method of this composite is invoked, which creates a nondeterminate
 interaction with the deferred execution. See CompositeActor.fire().

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @deprecated Use {@link ptolemy.actor.lib.hoc.ThreadedComposite} instead
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 @deprecated Use ThreadedComposite instead.
 */
@Deprecated
public class RealTimeComposite extends MirrorComposite {
    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public RealTimeComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("ptolemy.actor.lib.hoc.RealTimeComposite");
        new RealTimeDirector(this, "RealTimeDirector");

        // Hidden parameter defining "UNDEFINED".
        Parameter UNDEFINED = new Parameter(this, "UNDEFINED");
        UNDEFINED.setVisibility(Settable.EXPERT);
        UNDEFINED.setPersistent(false);
        UNDEFINED.setExpression("-1.0");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("UNDEFINED");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The maximum model-time delay between the input events and the
     *  output events. This is a double that defaults to <i>UNDEFINED</i>.
     */
    public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay) {
            _delayValue = ((DoubleToken) delay.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. This overrides
     *  the base class to instantiate a new RealTimeDirector.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(java.io.Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        RealTimeComposite result = (RealTimeComposite) super.clone(workspace);
        try {
            // Remove the old inner RealTimeDirector(s) that is(are) in the wrong workspace.
            String realTimeDirectorName = null;
            Iterator realTimeDirectors = result.attributeList(
                    RealTimeDirector.class).iterator();
            while (realTimeDirectors.hasNext()) {
                RealTimeDirector oldRealTimeDirector = (RealTimeDirector) realTimeDirectors
                        .next();
                if (realTimeDirectorName == null) {
                    realTimeDirectorName = oldRealTimeDirector.getName();
                }
                oldRealTimeDirector.setContainer(null);
            }

            // Create a new RealTimeDirector that is in the right workspace.
            RealTimeDirector realTimeDirector = result.new RealTimeDirector(
                    workspace);
            realTimeDirector.setContainer(result);
            realTimeDirector.setName(realTimeDirectorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        return result;
    }

    /** Invoke iterations on the contained actor of the
     *  container of this director repeatedly until either it runs out
     *  of input data or prefire() returns false. If postfire() of any
     *  actor returns false, then return false. Otherwise, return true.
     *  @return True to allow the thread to continue executing.
     *  @exception IllegalActionException If any called method of
     *   of the contained actor throws it, or if the contained
     *   actor is not opaque.
     */
    public boolean fireContainedActors() throws IllegalActionException {
        // Don't call "super.fire();" here, this actor contains its
        // own director.
        Iterator actors = entityList().iterator();
        boolean postfireReturns = true;

        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) actors.next();

            if (!((ComponentEntity) actor).isOpaque()) {
                throw new IllegalActionException(this,
                        "Inside actor is not opaque "
                                + "(perhaps it needs a director).");
            }

            int result = Executable.COMPLETED;

            while (result != Executable.NOT_READY) {
                if (_debugging) {
                    _debug("Iterating actor: " + actor.getFullName());
                }
                if (_debugging) {
                    _debug("---- Iterating actor in associated thread: "
                            + actor.getFullName());
                }
                result = actor.iterate(1);

                // Should return if there are no more input data,
                // irrespective of return value of prefire() of
                // the actor, which is not reliable.
                boolean outOfData = true;
                Iterator inPorts = actor.inputPortList().iterator();

                while (inPorts.hasNext()) {
                    IOPort port = (IOPort) inPorts.next();

                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            outOfData = false;
                            break;
                        }
                    }
                }

                if (outOfData) {
                    break;
                }

                if (result == Executable.STOP_ITERATING) {
                    if (_debugging) {
                        _debug("---- Actor requests halt: "
                                + actor.getFullName());
                    }
                    postfireReturns = false;
                    break;
                }
            }
        }
        return postfireReturns;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The cached value of the <i>delay</i> parameter. */
    private double _delayValue = 0.0;

    /** Queue of times at which inside actors have requested firings.
     *  This queue is accessed from multiple threads, so it must be
     *  thread safe.
     */
    private List<Time> _fireAtTimes = Collections
            .synchronizedList(new LinkedList<Time>());

    /** Queue of unprocessed input events.
     */
    private DelayQueue<InputFrame> _inputFrames = new DelayQueue<InputFrame>();

    /** Queue of unprocessed output events.
     *  This queue is accessed from multiple threads, so it must be
     *  thread safe.
     */
    private List<OutputFrame> _outputFrames = Collections
            .synchronizedList(new LinkedList<OutputFrame>());

    /** The real time at which the model begins executing, in milliseconds. */
    private long _realStartTime = 0;

    /** Queue of times at which responses to firings are expected.
     *  This is accessed only from the Director action methods, which run
     *  in a single thread, so it need not by thread safe.
     */
    private Queue<Time> _responseTimes = new LinkedList<Time>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// InputFrame

    /** Bundle of a token and the input port at which it arrived.
     *  Use null for <i>theTokens</i> specifies this frame as a "stop frame" to
     *  flag that no more inputs will be delivered.
     */
    private class InputFrame implements Delayed {

        /** Construct an input frame.
         *  @param theTime The model time of the input events.
         *  @param theTokens The tokens in the input events.
         */
        public InputFrame(Time theTime, List<QueuedToken> theTokens) {
            tokens = theTokens;
            time = theTime;
        }

        public final Time time;

        public final List<QueuedToken> tokens;

        @Override
        public long getDelay(TimeUnit unit) {
            // Calculate time to wait.
            long elapsedTime = System.currentTimeMillis() - _realStartTime;
            // NOTE: We assume that the elapsed time can be
            // safely cast to a double.  This means that
            // the DE domain has an upper limit on running
            // time of Double.MAX_VALUE milliseconds.
            double elapsedTimeInSeconds = elapsedTime / 1000.0;
            long timeToWait = (long) (time.subtract(elapsedTimeInSeconds)
                    .getDoubleValue() * 1000.0);
            return unit.convert(timeToWait, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed frame) {
            // NOTE: We assume that only comparisons against instances
            // of Frame will be done.  Is this safe?
            return time.compareTo(((InputFrame) frame).time);
        }

        /** Return true if this InputFrame object has the same
         *  time as the given InputFrame object.
         *  @param inputFrame The InputFrame object that this
         *  InputFrame object is compared to.
         *  @return True if the two InputFrame objects have the same time.
         */
        @Override
        public boolean equals(Object inputFrame) {
            // See http://www.technofundo.com/tech/java/equalhash.html

            /* FindBugs says that InputFrame "defined
             * compareTo(Object) and uses Object.equals()"
             * http://findbugs.sourceforge.net/bugDescriptions.html#EQ_COMPARETO_USE_OBJECT_EQUALS
             * says: "This class defines a compareTo(...) method but
             * inherits its equals() method from
             * java.lang.Object. Generally, the value of compareTo should
             * return zero if and only if equals returns true. If this is
             * violated, weird and unpredictable failures will occur in
             * classes such as PriorityQueue. In Java 5 the
             * PriorityQueue.remove method uses the compareTo method,
             * while in Java 6 it uses the equals method.
             *
             *  From the JavaDoc for the compareTo method in the
             *  Comparable interface:
             *
             * It is strongly recommended, but not strictly required that
             * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking,
             * any class that implements the Comparable interface and
             * violates this condition should clearly indicate this
             * fact. The recommended language is "Note: this class has a
             * natural ordering that is inconsistent with equals." "
             */
            if (inputFrame == this) {
                return true;
            }
            if (inputFrame == null || inputFrame.getClass() != getClass()) {
                return false;
            } else {
                InputFrame frame = (InputFrame) inputFrame;
                if (compareTo(frame) == 0
                        && frame.tokens.size() == tokens.size()) {
                    return frame.tokens.equals(tokens);
                }
            }
            return false;
        }

        /** Return the hash code for the InputFrame object.
         *  @return The hash code for this InputFrame object;
         */
        @Override
        public int hashCode() {
            // See http://www.technofundo.com/tech/java/equalhash.html
            int hashCode = 7;
            if (time != null) {
                hashCode = 31 * hashCode + time.hashCode();
            }
            if (tokens != null) {
                hashCode = 31 * hashCode + tokens.hashCode();
            }
            return hashCode;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// QueuedToken

    /** Bundle of a token and the input port and channel
     *  at which it arrived.
     */
    private static class QueuedToken {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public QueuedToken(IOPort thePort, int theChannel, Token theToken) {
            token = theToken;
            channel = theChannel;
            port = thePort;
        }

        public final int channel;

        public final Token token;

        public final IOPort port;

        @Override
        public String toString() {
            return "token " + token + " for port " + port.getFullName() + "("
                    + channel + ")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OutputFrame

    /** Bundle of a token and the output port at which it arrived.
     */
    private static class OutputFrame {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct an output frame.
         *  @param theTime The model time of the output events.
         *  @param theTokens The tokens in the output events.
         */
        public OutputFrame(Time theTime, List<QueuedToken> theTokens) {
            tokens = theTokens;
            time = theTime;
        }

        public final Time time;

        public final List<QueuedToken> tokens;
    }

    ///////////////////////////////////////////////////////////////////
    //// RealTimeDirector

    /** This is a specialized director that defers firing of the
     *  contained actors until real-time matches the time stamp of
     *  provided inputs. It does this in a separate thread that
     *  blocks until the times match, then transfers the input tokens
     *  that arrived with that time stamp and fires the contained actors
     *  in the order in which they appear in the actor list repeatedly
     *  until either there is no more input data for the actor or
     *  the prefire() method of the actor returns false. If postfire()
     *  of any actor returns false, then postfire() of this director
     *  will return false, requesting a halt to execution of the model.
     */
    private class RealTimeDirector extends Director {
        /** Create a new instance of the director for RealTimeComposite.
         *  @param container The container for the director.
         *  @param name The name of the director.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @exception NameDuplicationException Not thrown in this base class.
         */
        public RealTimeDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            setPersistent(false);
        }

        /** Construct a RealTimeDirector in the specified workspace with
         *  no container and an empty string as a name. You can then change
         *  the name with setName(). If the workspace argument is null, then
         *  use the default workspace.  You should set the local director or
         *  executive director before attempting to send data to the actor
         *  or to execute it. Add the actor to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public RealTimeDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** If current model time of the environment matches the time at which outputs
         *  that have been queued should be produced, then produce them.
         *  Yield to other threads.
         *  @exception IllegalActionException If production of an output
         *   fails (e.g. type error).
         */
        @Override
        public void fire() throws IllegalActionException {
            if (_realStartTime < 0L) {
                _realStartTime = System.currentTimeMillis();
            }
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            if (_delayValue == 0.0) {
                // Delay is zero, so wait until current time matches
                // model time, and then treat this as an ordinary composite actor.
                long realTimeMillis = System.currentTimeMillis()
                        - _realStartTime;
                long modelTimeMillis = Math.round(environmentTime
                        .getDoubleValue() * 1000.0);
                if (realTimeMillis < modelTimeMillis) {
                    try {
                        Thread.sleep(modelTimeMillis - realTimeMillis);
                    } catch (InterruptedException e) {
                        // Ignore and continue.
                    }
                }
                // FIXME: This isn't quite right, since this will postfire()
                // contained actors.
                super.fire();
            } else {
                // Delay is either UNDEFINED or positive,
                // so we are running in separate thread.

                // If the delay value is positive, then we may need
                // to stall to prevent model time from getting too
                // far ahead of real time.
                if (_delayValue > 0.0) {
                    // Delay value is positive. If current time matches
                    // the time at the head of the _responseTime queue,
                    // then stall until real time matches that time.
                    // Note that there is no harm in consuming the
                    // head of the queue since the side effect here
                    // is the passage of real time.
                    Time responseTime = _responseTimes.peek();
                    if (responseTime != null
                            && responseTime.equals(environmentTime)) {

                        // FIXME: Findbugs says that the next line:
                        // "ignores return value of java.util.Queue.poll()"
                        _responseTimes.poll();
                        // Time matches.  Compare to real time.
                        long realTimeMillis = System.currentTimeMillis()
                                - _realStartTime;
                        long modelTimeMillis = Math.round(environmentTime
                                .getDoubleValue() * 1000.0);
                        if (realTimeMillis < modelTimeMillis) {
                            try {
                                Thread.sleep(modelTimeMillis - realTimeMillis);
                            } catch (InterruptedException e) {
                                // Ignore and continue.
                            }
                        }
                    }
                }

                // Next check for outputs to produce.
                if (_outputFrames.size() > 0) {
                    OutputFrame frame = _outputFrames.get(0);
                    if (frame.time.equals(environmentTime)) {
                        // Current time matches the time of the first frame on
                        // the output queue.
                        // Produce the outputs on the frame.
                        for (QueuedToken token : frame.tokens) {
                            if (token.channel < token.port.getWidth()) {
                                token.port.send(token.channel, token.token);
                            }
                        }
                    }
                }
                Thread.yield();
            }
        }

        /** Delegate by calling fireAt() on the director of the container's
         *  container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         *  @param microstep The microstep.
         *  @return The time at which the actor passed as an argument
         *   will be fired.
         */
        @Override
        public Time fireAt(Actor actor, Time time, int microstep)
                throws IllegalActionException {
            Time result = time;
            Director director = RealTimeComposite.this.getExecutiveDirector();
            if (director != null) {
                if (RealTimeComposite.this._debugging) {
                    RealTimeComposite.this
                    ._debug("---- Actor requests firing at time "
                            + time + ": " + actor.getFullName());
                }
                result = director.fireAt(RealTimeComposite.this, time,
                        microstep);
            }
            if (actor != RealTimeComposite.this) {
                // The fireAt() request is coming from the inside, so
                // when the firing occurs, we want to post an input
                // frame (even if there are no input events) for
                // the associated thread.
                _fireAtTimes.add(time);
            }
            return result;
        }

        /** Fire the specified actor at the first opportunity
         *  and then pass the request up to the executive director.
         *  When passing it up, request a firing at the greater of
         *  the current time of that director or the elapsed real
         *  time since the start of the model.
         *  This is useful for actors that spontaneously produce output,
         *  e.g. from sensor data or from completion of some previously
         *  started task. The firing of the actor will produce the output,
         *  sending it to the inside of the output ports of this composite,
         *  and then the firing of the composite will transfer those tokens
         *  to the outside model.
         *  @param actor The actor requesting firing (ignored).
         */
        @Override
        public Time fireAtCurrentTime(Actor actor)
                throws IllegalActionException {
            // Coverity Scan reports that this method does not call
            // super.fireAtCurrentTime(), which is ok because we call
            // it on the executive director.
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            _inputFrames.put(new InputFrame(environmentTime,
                    new LinkedList<QueuedToken>()));
            Director director = RealTimeComposite.this.getExecutiveDirector();
            if (director != null) {
                // We assume that the contained actors mean "real time" by
                // "current time". Hopefully, this will be in the future w.r.t. model time.
                // Use fireAt() hoping that the director will not increment time
                // too soon.
                // FIXME: This is not right!
                Time time = new Time(this,
                        (System.currentTimeMillis() - _realStartTime) / 1000.0);
                if (RealTimeComposite.this._debugging) {
                    RealTimeComposite.this
                    ._debug("----- fireAtCurrentTime() request by actor "
                            + actor.getFullName()
                            + ". Model time is "
                            + environmentTime
                            + ", and real time is "
                            + time);
                }
                director.fireAt(RealTimeComposite.this, time);
                return time;
            }
            return environmentTime;
        }

        /** Return the current time of the enclosing actor if the delay
         *  is zero. Otherwise, get the local notion of current time.
         *  @return The current time.
         */
        @Override
        public Time getModelTime() {
            if (_delayValue == 0.0) {
                return ((Actor) getContainer()).getExecutiveDirector()
                        .getModelTime();
            } else {
                return super.getModelTime();
            }
        }

        /** Start the associated thread.
         *  @exception IllegalActionException If the initialize() method of
         *   one of the associated actors throws it.
         */
        @Override
        public void initialize() throws IllegalActionException {
            // The following must be done before the initialize() methods
            // of the actors is called because those methods may call fireAt().
            _fireAtTimes.clear();

            // The superclass will initialize all the actors.
            super.initialize();
            // Set a flag indicating that the first firing should
            // initialize the _realStartTime variable. This is done
            // in the first firing to be as late as possible, so
            // that startup transients are minimized.
            // FIXME: This will impede synchronization with other
            // actors, since there won't be a common time base.
            _realStartTime = -1L;
            if (_delayValue != 0) {
                // We will be executing in a new thread.
                // Create and start that thread.
                _inputFrames.clear();
                _outputFrames.clear();
                _responseTimes.clear();
                _thread = new RealTimeThread();
                _thread.setPriority(Thread.MAX_PRIORITY);
                _thread.start();
            }
        }

        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        @Override
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Clear the list of input events for this iteration and return true
         *  if the associated thread is alive, if <i>delay</i> is not 0.0.
         *  Otherwise, return true.
         *  @return True if the associated thread is still alive, or true
         *   if delay == 0.0.
         */
        @Override
        public boolean prefire() throws IllegalActionException {
            // Do not call super.prefire()!
            // Superclass aligns current time to that of the container.
            // The notion of current time presented to these actors
            // should match that of the frame.
            // super.prefire();
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            if (RealTimeComposite.this._debugging) {
                RealTimeComposite.this
                ._debug("----- Current environment time is: "
                        + environmentTime);
            }

            if (_delayValue != 0) {
                // Have to create a new list because the previous list may
                // not have been consumed yet.
                _inputTokens = new LinkedList<QueuedToken>();
                return _thread.isAlive();
            } else {
                return true;
            }
        }

        /** Send all the collected tokens to the queue for consumption
         *  by the associated thread, if there is an associated thread.
         *  Otherwise, just invoke the superclass postfire().
         *  @return True if the associated thread is still alive.
         */
        @Override
        public boolean postfire() throws IllegalActionException {
            boolean result = super.postfire();
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            if (_delayValue != 0) {
                // Delay is either UNDEFINED or positive.
                // Post the inputs for consumption in the
                // associated thread.
                if (_inputTokens.size() > 0) {
                    if (RealTimeComposite.this._debugging) {
                        RealTimeComposite.this
                        ._debug("Queueing input tokens for the associated thread: "
                                + _inputTokens.toString()
                                + " to be processed at time "
                                + environmentTime);
                    }
                    _inputFrames.put(new InputFrame(environmentTime,
                            _inputTokens));
                    if (_delayValue > 0.0) {
                        // Delay value is positive. Schedule a firing
                        // at current time plus the delay.
                        Time responseTime = environmentTime.add(_delayValue);
                        fireAt(RealTimeComposite.this, responseTime);

                        // Queue an indicator to stall when that firing occurs.
                        _responseTimes.add(responseTime);
                    }
                }
                // Even if _inputTokens is null, we still want to post an
                // event if the firing is due to a call to fireAt() from the inside.
                // Check to see whether that is the case.
                if (_fireAtTimes.size() > 0) {
                    Time fireAtTime = _fireAtTimes.get(0);
                    if (fireAtTime.equals(environmentTime)) {
                        // Remove the time from the queue.
                        _fireAtTimes.remove(0);
                        // Queue an iteration even if there are no inputs.
                        if (_inputTokens.size() == 0) {
                            if (RealTimeComposite.this._debugging) {
                                RealTimeComposite.this
                                ._debug("Queueing pure event for the associated thread, "
                                        + " to be processed at time "
                                        + environmentTime);
                            }
                            _inputFrames.put(new InputFrame(environmentTime,
                                    _inputTokens));
                            if (_delayValue > 0.0) {
                                // Delay value is positive. Schedule a firing
                                // at current time plus the delay.
                                Time responseTime = environmentTime
                                        .add(_delayValue);
                                fireAt(RealTimeComposite.this, responseTime);

                                // Queue an indicator to stall when that firing occurs.
                                _responseTimes.add(responseTime);
                            }
                        }
                    }
                }

                // If current time matches the time at the head of
                // of the queue for outputs, then consume the data on the
                // head of the queue. Those data were sent to the output
                // in the fire() method.
                if (_outputFrames.size() > 0) {
                    OutputFrame frame = _outputFrames.get(0);
                    if (frame.time.equals(environmentTime)) {
                        // Consume the outputs on the frame, which will have
                        // been sent in the fire() method.
                        _outputFrames.remove(0);
                    }
                }
                result = _thread.isAlive();
            }
            return result;
        }

        /** Override the base class to post a "stop frame" on the queue
         *  if there is an associated thread.
         */
        @Override
        public void stop() {
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            if (_delayValue != 0) {
                if (RealTimeComposite.this._debugging) {
                    RealTimeComposite.this
                    ._debug("Queueing a stop-frame token for the associated thread with time: "
                            + environmentTime);
                }
                // A "stop frame" has a null token list.
                _inputFrames.put(new InputFrame(environmentTime, null));
            } else {
                super.stop();
            }
        }

        /** Record data from the specified input port
         *  for transfer to the queue used to communicate these data to the
         *  associated thread.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException If reading the inputs fails.
         */
        @Override
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {
            if (_delayValue == 0) {
                return super.transferInputs(port);
            }
            boolean result = false;

            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token token = port.get(i);
                            _inputTokens.add(new QueuedToken(port, i, token));
                            if (RealTimeComposite.this._debugging) {
                                RealTimeComposite.this._debug(
                                        getName(),
                                        "transferring input from "
                                                + port.getName());
                            }
                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
            return result;
        }

        /** If real time is less than or equal to the current model time
         *  of the environment, then produce the outputs immediately at the
         *  current model time. Otherwise, collect them and queue them to
         *  be produced by the fire method when model time matches the
         *  current real time, and call fireAt() to request a firing
         *  at that time.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is produced now.
         *  @exception IllegalActionException If reading the inputs fails.
         */
        @Override
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            if (_delayValue == 0) {
                return super.transferOutputs(port);
            }
            // Compare against the environment time.
            Time environmentTime = RealTimeComposite.this
                    .getExecutiveDirector().getModelTime();
            double realTimeInSeconds = (System.currentTimeMillis() - _realStartTime) / 1000.0;
            if (environmentTime.getDoubleValue() >= realTimeInSeconds) {
                return super.transferOutputs(port);
            } else {
                // The current real time is greater than the current
                // model time of the environment. Schedule the production
                // of outputs at the real time.
                environmentTime = new Time(this, realTimeInSeconds);
                LinkedList<QueuedToken> outputTokens = new LinkedList<QueuedToken>();
                for (int i = 0; i < port.getWidth(); i++) {
                    try {
                        if (port.isKnownInside(i)) {
                            if (port.hasTokenInside(i)) {
                                Token token = port.getInside(i);
                                outputTokens
                                .add(new QueuedToken(port, i, token));
                                if (RealTimeComposite.this._debugging) {
                                    RealTimeComposite.this._debug(
                                            getName(),
                                            "transferring output from "
                                                    + port.getName()
                                                    + " with value " + token);
                                }
                            }
                        }
                    } catch (NoTokenException ex) {
                        // this shouldn't happen.
                        throw new InternalErrorException(this, ex, null);
                    }
                }
                if (outputTokens.size() > 0) {
                    OutputFrame frame = new OutputFrame(environmentTime,
                            outputTokens);
                    _outputFrames.add(frame);
                    // Request a firing to actually transfer the outputs to
                    // the outside.
                    fireAt(RealTimeComposite.this, environmentTime);
                }
                return false;
            }
        }

        /** Override the base class to wait until the associated thread
         *  terminates and then call super.wrapup().
         *  @exception IllegalActionException If the wrapup() method of
         *   one of the associated actors throws it.
         */
        @Override
        public void wrapup() throws IllegalActionException {
            if (_delayValue != 0) {
                // First, post a "stop frame" in case one has not been posted.
                // In the case of a finite run, one will likely have not been posted.
                Time environmentTime = RealTimeComposite.this
                        .getExecutiveDirector().getModelTime();
                if (RealTimeComposite.this._debugging) {
                    RealTimeComposite.this
                    ._debug("Queueing a stop-frame token for the associated thread with time: "
                            + environmentTime);
                }
                // A "stop frame" has a null token list.
                _inputFrames.put(new InputFrame(environmentTime, null));
                try {
                    if (RealTimeComposite.this._debugging) {
                        RealTimeComposite.this
                        ._debug("Waiting for associated thread to stop.");
                    }
                    _thread.join();
                    if (RealTimeComposite.this._debugging) {
                        RealTimeComposite.this
                        ._debug("Associated thread has stopped.");
                    }
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
            super.wrapup();
        }

        //////////////////////////////////////////////////////////////
        ////                   private variables                  ////

        /** List of input events in the current iteration. */
        private List<QueuedToken> _inputTokens;

        /** The thread that executes the contained actors. */
        private Thread _thread;

        //////////////////////////////////////////////////////////////
        ////                   inner inner classes                ////

        ///////////////////////////////////////////////////////////////////
        //// RealTimeThread

        /** This the thread that executed the actors.
         */
        private class RealTimeThread extends Thread {
            public RealTimeThread() {
                super("RealTimeThread");
            }

            @Override
            public void run() {
                while (!_stopRequested) {
                    try {
                        if (RealTimeComposite.this._debugging) {
                            RealTimeComposite.this
                            ._debug("---- Waiting for inputs in the associated thread.");
                        }
                        InputFrame frame = _inputFrames.take();
                        if (frame.tokens == null) {
                            // Recognize a "stop frame" and exit the thread.
                            if (RealTimeComposite.this._debugging) {
                                RealTimeComposite.this
                                ._debug("---- Read a stop frame in associated thread.");
                            }
                            break;
                        }
                        if (RealTimeComposite.this._debugging) {
                            RealTimeComposite.this
                            ._debug("---- Reading input tokens in associated thread with time "
                                    + frame.time
                                    + " and value "
                                    + frame.tokens);
                        }
                        // Current time of the director should match the frame time.
                        // This is the view of time that should be presented to any inside actors.
                        localClock.setLocalTime(frame.time);

                        // Note that there may not be any tokens here, since there
                        // may not be any inputs. We still want to iterate the
                        // enclosed model at the specified time because the firing
                        // is due to the model itself having previously called
                        // fireAt().
                        for (QueuedToken token : frame.tokens) {
                            if (token.channel < token.port.getWidthInside()) {
                                token.port.sendInside(token.channel,
                                        token.token);
                            }
                        }
                        boolean postfireReturnsTrue = fireContainedActors();
                        // If outputs are produced by the firing, then
                        // we need to trigger a transferOutputs() call.
                        // Note that this does not have to be done if the delay
                        // is 0.0, since it will be done by the superclass.
                        if (_delayValue != 0.0) {
                            Iterator ports = outputPortList().iterator();
                            while (ports.hasNext()) {
                                IOPort port = (IOPort) ports.next();
                                boolean hasOutputs = false;
                                for (int i = 0; i < port.getWidth(); i++) {
                                    if (port.isKnownInside(i)
                                            && port.hasTokenInside(i)) {
                                        hasOutputs = true;
                                    }
                                }
                                if (hasOutputs) {
                                    transferOutputs(port);
                                }
                            }
                        }
                        if (!postfireReturnsTrue) {
                            // postfire() of the contained actors returns false.
                            break;
                        }
                    } catch (InterruptedException e) {
                        // Exit the thread.
                        break;
                    } catch (IllegalActionException ex) {
                        MessageHandler.error("Error in real-time thread.", ex);
                    }
                }
            }
        }
    }
}
