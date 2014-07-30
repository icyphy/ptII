/* An actor that executes a contained actor in separate thread.

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
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

///////////////////////////////////////////////////////////////////
//// ThreadedComposite

/**
 A container for another actor that executes that other actor
 in a separate thread called the <i>inside thread</i>.
 This actor starts that thread in its initialize()
 method, which is invoked by its executive director (the director
 in charge of firing this actor). The thread that invokes the
 action methods of this actor
 (initialize(), prefire(), fire(), postfire(), and wrapup())
 is called the <i>director thread</i>.
 <p>
 A paper describing the use of this actor is found at
 <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2008/EECS-2008-151.html">
 http://www.eecs.berkeley.edu/Pubs/TechRpts/2008/EECS-2008-151.html</a>.
 <p>
 This actor automatically creates input and output ports to
 match those of the inside actor. Input events provided at those
 input ports are provided as input events to the contained actor.
 Outputs provided by the contained actor become output events
 of this actor. The time stamp of the input events is provided
 by the container of this actor. The time stamp of the output events
 depends on the <i>delay</i> parameter, as explained below.
 <p>
 The inside thread blocks waiting for inputs or pure events.
 Inputs are provided to that thread when the fire() method of
 this actor is invoked by the director thread.
 Pure events are provided after fireAt(),
 fireAtCurrentTime(), or fireAtFirstValidTimeAfter() are called
 by either the inside thread or the director thread.
 When the time of those firing requests becomes current time,
 the container will (presumably) fire this actor, and
 this actor will provide a pure event to the inside thread,
 causing it to fire the contained actor.
 <p>
 If the <i>synchronizeToRealTime</i> parameter is true, then
 when the inside thread encounters an input or pure event
 with time stamp <i>t</i>, it stalls until real time matches
 or exceeds <i>t</i> (measured in seconds since the start of
 execution of the inside thread). In contrast for example
 to the <i>synchronizeToRealTime</i> parameter of the DEDirector,
 this enables construction of models where only a portion of the
 model synchronizes to real time.
 <p>
 When the wrapup() method of this actor is called, the inside thread is
 provided with signal to terminate rather than to process additional
 inputs. The inside thread will also exit if stop() is called on this
 actor; however, in this case, which iterations are completed
 is nondeterminate (there may be inputs left unprocessed).
 <p>
 The parameters of this actor include all the parameters of the
 contained actor, and setting those parameters automatically
 sets the parameters of the contained actor.
 <p>
 In addition to the parameters of the contained actor, this actor
 has a <i>delay</i> parameter. This parameter is a double that
 be any nonnegative value or the special value <i>UNDEFINED</i>.
 If it is given a nonnegative value, then the value specifies
 the model-time delay between input events and the output
 events that result from reacting to those input events.
 That is, if this actor is given an input event with time
 stamp <i>t</i>, then if the contained actor produces any output
 events in reaction to that event, those output events will be
 produced by this actor with time stamp <i>t</i> + <i>delay</i>.
 <p>
 If <i>delay</i> has value <i>UNDEFINED</i>, then
 outputs are produced at the current model time of the executive
 director when the inside thread happens to produce those events,
 or if <i>synchronizeToRealTime</i>, at the greater of current
 model time and current real time (measured in seconds since
 the start of execution).
 This is accomplished by the inside thread calling
 fireAtFirstValidTimeAfter() of the enclosing director, and
 then producing the outputs when the requested firing occurs
 in the director thread. Note that with this value of the
 <i>delay</i>, it is possible for the inside thread to
 continue to execute and respond to input events after
 the wrapup phase of the director thread has been entered.
 The wrapup phase will stall until the inside thread has
 completed its processing of its inputs, but any outputs
 it produces after the wrapup phase has started will be
 discarded.
 <p>
 The most common use of this actor is in the DE domain,
 although it can also be used in CT, SR, SDF, and other domains,
 with some care. See the above referenced memo.
 Regardless of the value of <i>delay</i>, this actor is treated
 by DE as introducing a delay, much like the TimedDelay actor.
 In fact, if <i>delay</i> is 0.0, there will be a one tick delay
 in superdense time, just as with the TimedDelay actor.
 If the inside model also has a time delay (e.g. if you
 put a TimedDelay actor inside a ThreadedComposite), then
 the total delay is the sum of the two delays.
 <p>
 <b>Discussion:</b>
 <p>
 There are several useful things you can do with this model.
 We describe some use cases here:
 <p>
 <i>Background execution.</i> When <i>delay</i> is greater than
 or equal to 0.0,
 then when this actor is fired in response to input events
 with time stamp <i>t</i>, the actual
 processing of those events occurs later in a separate thread. The
 director thread is not blocked, and can continue to process events
 with time stamps less than or equal to <i>t</i> + <i>delay</i>.
 The director thread is blocked from processing events with larger
 time stamps than that because this is necessary to preserve DE
 semantics. To implement this, this actor uses fireAt() to
 request a firing at time  <i>t</i> + <i>delay</i>, and when that
 firing occurs, it blocks the director thread until the reaction
 is complete.
 <p>
 <i>Parallel firing.</i> Note that if <i>delay</i> is set to 0.0,
 it may seem that there is no point in using this actor, since
 model time will not be allowed to increase past <i>t</i> until
 the contained actor has reacted to events with time stamp <i>t</i>.
 However, there is actually exploitable concurrency if there
 are other actors in the model that also have pending input
 events with time stamp <i>t</i>. Those event can be processed
 concurrently with this actor reacting to its input event.
 A typical use case will broadcast an event to several instances
 of ThreadedComposite, in which case each of those several
 inside threads can execute concurrently in reaction to those
 input events.
 <p>
 <i>Real-time source.</i> If the contained actor (and hence this
 actor) has no inputs and <i>synchronizeToRealTime</i> is true, then
 the contained actor must call fireAt() or one of its variants so that
 the inside thread will be provided with pure events.
 The behavior depends on which variant of the fireAt() methods is used
 by the inside actor.  There are three cases:
 FIXME: Described these. In particular, delay needs to specify the
 minimum increment between these or fireAt() could result in an
 exception.  Do we want a parameter to relax that?
 <p>
 On subtlety of this actor is that it cannot expose instances of ParameterPort
 without introducing nondeterminacy in the execution. A ParameterPort
 is an input port that sets the value of a parameter with the same name. Upon receiving
 a token at such a port, if this actor were to set a parameter visible by the
 inside thread, there is no assurance that the inside thread is not still
 executing an earlier iteration. Thus, it could appear to be sending a message
 backward in time, which would be truly bizarre. To prevent this error,
 this actor does not mirror such ports, and hence they appear on the outside
 only as parameters.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ThreadedComposite extends MirrorComposite {
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
    public ThreadedComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        // The false argument specifies that instances of ParameterPort
        // should not be mirrored. This would make the behavior nondeterminate,
        // so we expose these only as parameters.
        super(container, name, false);
        setClassName("ptolemy.actor.lib.hoc.ThreadedComposite");

        // Create the ThreadedDirector in the proper workspace.
        ThreadedDirector threadedDirector = new ThreadedDirector(workspace());
        threadedDirector.setContainer(this);
        threadedDirector.setName(uniqueName("ThreadedDirector"));

        // Hidden parameter defining "UNDEFINED".
        Parameter UNDEFINED = new Parameter(this, "UNDEFINED");
        UNDEFINED.setVisibility(Settable.EXPERT);
        UNDEFINED.setPersistent(false);
        UNDEFINED.setExpression("-1.0");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("0.0");

        synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime");
        synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);
        synchronizeToRealTime.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The model-time delay between the input events and the
     *  output events. This is a double that defaults to 0.0,
     *  indicating that outputs should have the same time stamps
     *  as the inputs that trigger them. If it has a value greater
     *  than zero, then the outputs will have large time stamps
     *  by that amount. If it has the value <i>UNDEFINED</i>
     *  (or any negative number), then the output time stamp
     *  will be nondeterminate, and will depend on the current
     *  model time of the outside director when the output is
     *  produced or on current real time.
     */
    public Parameter delay;

    /** If set to true, the inside thread stalls until real time matches
     *  the time stamps of input events or pure events for each firing.
     *  In addition, if <i>delay</i> is set to undefined and this is set
     *  to true, then output events are assigned a time stamp that is the
     *  greater of current model time and real time.
     *  Time is measured since the start of the execution of the inside
     *  thread.  This is a boolean that defaults to false. Changing
     *  the value of this parameter has no effect until the next
     *  execution of the model.
     */
    public Parameter synchronizeToRealTime;

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

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ThreadedComposite newObject = (ThreadedComposite) super
                .clone(workspace);
        try {
            // Remove the old inner ThreadedDirector that is in the wrong workspace.
            List iterateDirectors = newObject
                    .attributeList(ThreadedDirector.class);
            ThreadedDirector oldThreadedDirector = (ThreadedDirector) iterateDirectors
                    .get(0);
            String threadedDirectorName = oldThreadedDirector.getName();
            oldThreadedDirector.setContainer(null);

            // Create a new ThreadedDirector that is in the right workspace.
            ThreadedDirector iterateDirector = newObject.new ThreadedDirector(
                    workspace);
            iterateDirector.setContainer(newObject);
            iterateDirector.setName(threadedDirectorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }

        newObject._causalityInterface = null;
        newObject._realStartTime = 0L;
        return newObject;
    }

    /** Override the base class to return a causality interface that
     *  indicates that the output does not depend (immediately) on
     *  the input. This method assumes that the director deals with BooleanDependencies
     *  and returns an instance of BreakCausalityInterface.
     *  @return A representation of the dependencies between input ports
     *  and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        // FIXME: This will not work property with Ptides because it will effectively
        // declare that the delay from input to output is infinite, which it is not.
        // What we want is for the delay from input to output to be a superdense time
        // delay of (0.0, 1).  This could be implemented by a class similar to
        // BreakCausalityInterface that does the right thing when the director
        // provides a Dependency that is a SuperdenseTimeIdentity.
        if (_causalityInterface == null) {
            _causalityInterface = new BreakCausalityInterface(this,
                    getExecutiveDirector().defaultDependency());
        }
        return _causalityInterface;
    }

    /** Iterate the contained actors of the
     *  container of this director.
     *  @return False if any contained actor returns false in postfire.
     *  @exception IllegalActionException If any called method of
     *   of the contained actor throws it, or if the contained
     *   actor is not opaque.
     */
    public boolean iterateContainedActors() throws IllegalActionException {
        // Don't call "super.fire();" here, this actor contains its
        // own director.
        boolean result = true;
        List<Actor> actors = entityList();
        for (Actor actor : actors) {
            if (_stopRequested) {
                break;
            }
            if (!((ComponentEntity) actor).isOpaque()) {
                throw new IllegalActionException(this,
                        "Inside actor is not opaque "
                                + "(perhaps it needs a director).");
            }
            if (_debugging) {
                _debug("---- Iterating actor in inside thread: "
                        + actor.getFullName());
            }
            if (actor.iterate(1) == Executable.STOP_ITERATING) {
                result = false;
                _debug("---- Prefire returned false: " + actor.getFullName());
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The cached value of the <i>delay</i> parameter. */
    private double _delayValue = 0.0;

    /** The real time at which the model begins executing, in milliseconds. */
    private long _realStartTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// TokenFrame

    /** Bundle data associated with ports and a time stamp.
     *  There are three types of frames:
     *  EVENT is a (possibly empty) bundle of data and a time
     *  stamp that is either provided to the inside thread from
     *  the inputs of a ThreadedComposite or provided by the
     *  inside thread to form the outputs of a ThreadedComposite.
     *  POSTFIRE is a frame indicating that the inside actor
     *  can be postfired. No tokens are provided (they are assumed
     *  to have been consumed in the firing). STOP is a frame
     *  provided to the inside thread to indicate that it should
     *  stop executing.
     */
    protected static class TokenFrame {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a  TokenFrame.
         *  @param theTime The time of this token frame.
         *  @param theTokens a list of QueueTokens.
         *  @param theType The FrameType.
         */
        public TokenFrame(Time theTime, List<QueuedToken> theTokens,
                FrameType theType) {
            tokens = theTokens;
            time = theTime;
            type = theType;
        }

        /** The time. */
        public final Time time;
        /** A list of tokens. */
        public final List<QueuedToken> tokens;
        /** The type of the frame. */
        public final FrameType type;

        // Final fields (FindBugs suggestion)
        /**  A (possibly empty) bundle of data and a time
         *  stamp that is either provided to the inside thread from
         *  the inputs of a ThreadedComposite or provided by the
         *  inside thread to form the outputs of a ThreadedComposite.
         */
        public final static FrameType EVENT = new FrameType();

        /**  POSTFIRE is a frame indicating that the inside actor
         *  can be postfired. No tokens are provided (they are assumed
         *  to have been consumed in the firing).
         */
        public final static FrameType POSTFIRE = new FrameType();

        /** STOP is a frame provided to the inside thread to indicate
         *  that it should stop executing.
         */
        public final static FrameType STOP = new FrameType();

        private static class FrameType {
            private FrameType() {
            };
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
    //// ThreadedDirector

    /** A specialized director that fires a contained actor
     *  in a separate thread. The prefire() method returns true
     *  if the inside thread is alive. The fire() method posts
     *  input events, if any, for the current firing on a queue for
     *  the inside thread to consume. If the firing is in response
     *  to a prior refiring request by this director, then the fire()
     *  method will also wait for the inside thread to complete
     *  its firing, and will then produce outputs from that firing.
     *  The postfire() method posts
     *  a request to postfire the contained actor and also requests
     *  a refiring of this director at the current time plus the delay
     *  value (unless the delay value is UNDEFINED). The wrapup() method
     *  requests termination of the inside thread. If postfire()
     *  of the contained actor returns false, then postfire() of this director
     *  will return false, requesting a halt to execution of the model.
     */
    private class ThreadedDirector extends Director {

        /** Construct a new instance of the director for ThreadedComposite.
         *  The director is created in the specified workspace with
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
        public ThreadedDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** Clone the director into the specified workspace.
         *  @param workspace The workspace for the new object.
         *  @return A new director.
         *  @exception CloneNotSupportedException If a derived class has
         *   has an attribute that cannot be cloned.
         */
        @Override
        public Object clone(Workspace workspace)
                throws CloneNotSupportedException {
            ThreadedDirector newObject = (ThreadedDirector) super
                    .clone(workspace);
            newObject._exception = null;
            newObject._inputTokens = null;
            newObject._thread = null;
            newObject._outputTimes = new LinkedList<Time>();
            newObject._fireAtTimes = Collections
                    .synchronizedSet(new HashSet<Time>());
            newObject._inputFrames = new LinkedList<TokenFrame>();
            newObject._outputFrames = new LinkedList<TokenFrame>();
            return newObject;
        }

        /** Produce outputs (if appropriate).
         *  @exception IllegalActionException If production of an output
         *   fails (e.g. type error), or if this thread is interrupted
         *   while we are waiting for output to produce.
         */
        @Override
        public synchronized void fire() throws IllegalActionException {
            // NOTE: This method is synchronized to ensure that when
            // delay is UNDEFINED and the inside thread calls
            // fireAtFirstTimeAfter(), that the firing does not
            // occur before _outputFrames and _outputTimes have
            // been updated.

            if (_exception != null) {
                throw new IllegalActionException(ThreadedComposite.this,
                        _exception, "Error in inside thread of actor.");
            }

            Time environmentTime = ThreadedComposite.this
                    .getExecutiveDirector().getModelTime();

            if (ThreadedComposite.this._debugging) {
                ThreadedComposite.this._debug("Firing at time "
                        + environmentTime);
            }

            // If there is an output to be produced at this
            // time, produce it.
            Time nextOutputTime = _outputTimes.peek();
            if (environmentTime.equals(nextOutputTime)) {
                // There is an output to be produced.
                // First, remove that time from the pending outputs queue.

                // FIXME: FindBugs "RV: Base use of return value from method,
                // Method ignores return value." java.util.Queue.poll() returns
                // the value, which is ignored.
                _outputTimes.poll();
                // First, wait (if necessary) for output
                // to be produced.
                // We already know that the environment time matches
                // the expected output time, so we can ignore the time
                // stamp of the output frame.
                try {
                    // NOTE: Cannot use LinkedBlockingQueue for _outputFrames
                    // because we have to release the lock on this director
                    // while we are waiting or we get a deadlock.
                    while (_outputFrames.isEmpty() && !_stopRequested) {
                        if (ThreadedComposite.this._debugging) {
                            ThreadedComposite.this
                            ._debug("Waiting for outputs from inside thread.");
                        }
                        // The timeout allows this to respond to stop()
                        // even if we have a deadlock for some reason.
                        wait(1000L);
                    }
                    if (_outputFrames.isEmpty()) {
                        // A stop has been requested and there is no data to produce.
                        return;
                    }
                    TokenFrame frame = _outputFrames.poll();

                    // There is now an output frame to be produced.
                    if (ThreadedComposite.this._debugging) {
                        ThreadedComposite.this._debug("Done waiting.");
                    }

                    // Produce the outputs on the frame, if there are any
                    // outputs. Note that frame.tokens can only be null
                    // if the inside thread was interrupted while executing.
                    // If an exception occurred in the inside thread, then
                    // the _exception variable tested above would have been
                    // set (within a synchronized block), so that cannot be
                    // the cause.
                    if (frame.tokens == null) {
                        throw new IllegalActionException(this,
                                "Inside thread was interrupted.");
                    }
                    for (QueuedToken token : frame.tokens) {
                        if (token.channel < token.port.getWidth()) {
                            // There is now an output frame to be produced.
                            if (ThreadedComposite.this._debugging) {
                                ThreadedComposite.this._debug(
                                        "Sending output token ",
                                        token + " to port "
                                                + token.port.getName());
                            }

                            token.port.send(token.channel, token.token);
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new IllegalActionException(ThreadedComposite.this,
                            ex, "Director thread interrupted.");
                }
            }
        }

        /** Delegate by calling fireAt() on the director of the container's
         *  container (the executive director), and make a local record that
         *  a refiring request has been made for the specified time. Note that the
         *  executive director may modify the requested time. If it does, the
         *  modified value is returned. It is up to the calling actor to
         *  throw an exception if the modified time is not acceptable.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         *  @param microstep The microstep.
         *  @return The time at which the actor passed as an argument
         *   will be fired.
         *  @exception IllegalActionException If the executive director throws it.
         */
        @Override
        public Time fireAt(Actor actor, Time time, int microstep)
                throws IllegalActionException {
            Time result = time;
            Director director = ThreadedComposite.this.getExecutiveDirector();
            if (director != null) {
                if (ThreadedComposite.this._debugging) {
                    ThreadedComposite.this
                    ._debug("---- Request refiring at time " + time
                            + " for actor: " + actor.getFullName());
                }
                try {
                    result = director.fireAt(ThreadedComposite.this, time,
                            microstep);
                } catch (IllegalActionException ex) {
                    throw new IllegalActionException(this, ex, "Actor "
                            + actor.getFullName()
                            + " requests refiring at time " + time
                            + ", which fails.\n"
                            + "Perhaps the delay parameter is too large?\n"
                            + "Try setting it to 0.");
                }
            }
            if (actor != ThreadedComposite.this) {
                // The fireAt() request is coming from the inside, so
                // when the firing occurs, we want to post an input
                // frame (even if there are no input events) for
                // the inside thread.
                _fireAtTimes.add(result);
            }
            return result;
        }

        /** Start the inside thread.
         *  @exception IllegalActionException If the initialize() method of
         *   one of the inside actors throws it.
         */
        @Override
        public synchronized void initialize() throws IllegalActionException {
            // The following must be done before the initialize() methods
            // of the actors is called because those methods may call fireAt().
            // Note that previous runs may have left residual data on these lists.
            _fireAtTimes.clear();
            _outputFrames.clear();
            _outputTimes.clear();
            _inputFrames.clear();

            _exception = null;

            // The superclass will initialize all the actors.
            super.initialize();

            // Set a flag indicating that the first firing should
            // initialize the _realStartTime variable. This is done
            // in the first firing to be as late as possible, so
            // that startup transients are minimized.
            // FIXME: This will impede synchronization with other
            // actors, since there won't be a common time base.
            _realStartTime = -1L;

            _inputFrames.clear();
            _outputFrames.clear();

            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();

            // Create and start the inside thread.
            _thread = new CompositeThread();
            _thread.setPriority(Thread.MAX_PRIORITY);
            _thread.start();
        }

        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        @Override
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Return true if the inside thread is alive.
         *  @return True if the inside thread is still alive.
         */
        @Override
        public boolean prefire() throws IllegalActionException {
            // Do not call super.prefire()!
            // Superclass sets current time of this
            // director to that of the container.
            // The notion of current time presented to the
            // inside actors (which may be currently executing
            // in another thread) must match that of the frame
            // that the inside thread is processing.

            // Have to create a new list because the previous list may
            // not have been consumed yet.
            _inputTokens = new LinkedList<QueuedToken>();

            boolean result = _thread.isAlive();

            if (ThreadedComposite.this._debugging) {
                ThreadedComposite.this._debug("Prefire returns " + result);
            }
            return result;
        }

        /** Consume inputs (if any) and post a frame on the queue
         *  for the inside thread to consume. A frame will be posted
         *  even if there are no inputs if a refiring request has
         *  been made for the current time.
         *  @return True if the inside thread is still alive.
         */
        @Override
        public boolean postfire() throws IllegalActionException {

            Time environmentTime = ThreadedComposite.this
                    .getExecutiveDirector().getModelTime();

            if (ThreadedComposite.this._debugging) {
                ThreadedComposite.this._debug("Postfiring at time "
                        + environmentTime);
            }

            // If there are inputs to be consumed, or if a refiring
            // request has been made for this time, then create an
            // input frame for the inside thread.
            // We can safely remove the refire request since we
            // are now responding to it. Note that semantically,
            // multiple refire requests for the same time are only
            // required to trigger a single refiring, so this is true
            // even if there were multiple refire requests.
            boolean refireRequested = _fireAtTimes.remove(environmentTime);

            // Put a frame on the _inputFrames for the inside thread
            // if either a refire was requested or if there are inputs.
            if (refireRequested || !_inputTokens.isEmpty()) {
                if (ThreadedComposite.this._debugging) {
                    ThreadedComposite.this
                    ._debug("Queueing input tokens for the inside thread: "
                            + _inputTokens.toString()
                            + " to be processed at time "
                            + environmentTime);
                }
                synchronized (this) {
                    _inputFrames.add(new TokenFrame(environmentTime,
                            _inputTokens, TokenFrame.EVENT));
                    notifyAll();
                    if (_delayValue >= 0.0) {
                        // Delay value is not UNDEFINED. Schedule a firing
                        // at current time plus the delay.
                        Time responseTime = environmentTime.add(_delayValue);
                        // Need to be sure to call the executive director's fireAt().
                        // Make sure to throw an exception if the executive
                        // director does not exactly respect this request.
                        Time response = ThreadedComposite.this
                                .getExecutiveDirector().fireAt(
                                        ThreadedComposite.this, responseTime);

                        if (!response.equals(responseTime)) {
                            throw new IllegalActionException(
                                    this,
                                    "Director is unable to fire the actor at the requested time: "
                                            + responseTime
                                            + ". It responds it will fire it at: "
                                            + response);
                        }

                        // Queue an indicator to produce outputs in response to that firing.
                        _outputTimes.add(responseTime);
                    }
                }
                // Give the inside thread a chance to react.
                Thread.yield();
            }
            return _thread.isAlive();
        }

        /** Override the base class to post a "stop frame" on the queue
         *  for the inside thread to stop.
         */
        @Override
        public void stop() {
            super.stop();
            Time environmentTime = ThreadedComposite.this
                    .getExecutiveDirector().getModelTime();
            if (ThreadedComposite.this._debugging) {
                ThreadedComposite.this
                ._debug("Queueing a stop-frame token for the inside thread with time: "
                        + environmentTime);
            }
            synchronized (this) {
                _inputFrames.add(new TokenFrame(environmentTime, null,
                        TokenFrame.STOP));
                notifyAll();
            }
        }

        /** Record data from the specified input port
         *  for transfer to the queue used to communicate these data to the
         *  inside thread. This is called in the fire() method of
         *  the enclosing composite actor after the prefire() method
         *  of this director has been called and before its fire() method
         *  is called.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @exception IllegalActionException If reading the inputs fails.
         */
        @Override
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;
            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token token = port.get(i);
                            _inputTokens.add(new QueuedToken(port, i, token));
                            if (ThreadedComposite.this._debugging) {
                                ThreadedComposite.this
                                ._debug("Transferring input from "
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

        /** Override the base class to do nothing since the fire() method of this
         *  director directly handles producing the outputs. In particular, we
         *  don't want to read from the inside of the output ports because the
         *  inside thread may be concurrently writing to them for the next
         *  iteration.
         *  @param port The port to transfer tokens from.
         *  @return False, indicating that no data token is produced now.
         *  @exception IllegalActionException If writing the outputs fails.
         */
        @Override
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            return false;
        }

        /** Override the base class to wait until the inside thread
         *  terminates and then call super.wrapup().
         *  @exception IllegalActionException If the wrapup() method of
         *   one of the associated actors throws it.
         */
        @Override
        public void wrapup() throws IllegalActionException {
            // First, post a "stop frame" in case one has not been posted.
            // In the case of a finite run, one will likely have not been posted.
            Time environmentTime = ThreadedComposite.this
                    .getExecutiveDirector().getModelTime();
            if (ThreadedComposite.this._debugging) {
                ThreadedComposite.this._debug("Called wrapup. ",
                        "Queueing a stop-frame token for the inside thread with time: "
                                + environmentTime);
            }
            synchronized (this) {
                // A "stop frame" has a null token list.
                _inputFrames.add(new TokenFrame(environmentTime, null,
                        TokenFrame.STOP));
                notifyAll();
            }

            if (_exception != null) {
                throw new IllegalActionException(ThreadedComposite.this,
                        _exception, "Error in inside thread of actor.");
            }
            if (_thread != null && _thread.isAlive()) {
                try {
                    if (ThreadedComposite.this._debugging) {
                        ThreadedComposite.this
                        ._debug("Waiting for inside thread to stop.");
                    }
                    _thread.join();
                    if (ThreadedComposite.this._debugging) {
                        ThreadedComposite.this
                        ._debug("Inside thread has stopped.");
                    }
                    if (_exception != null) {
                        throw new IllegalActionException(
                                ThreadedComposite.this, _exception,
                                "Error in inside thread of actor.");
                    }
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
            super.wrapup();
        }

        //////////////////////////////////////////////////////////////
        ////                   private variables                  ////

        /** If an exception occurs in the inside thread, the exception
         *  will be assigned to this member, which will cause the
         *  the next invocation of the fire() or wrapup() method
         *  to throw the exception.
         */
        private Throwable _exception;

        /** Record of the times which refire requests have been made
         *  and not yet processed by any of the fireAt() methods.
         *  This set is accessed from both the director
         *  thread and the inside thread so it has to
         *  be thread safe.
         */
        private Set<Time> _fireAtTimes = Collections
                .synchronizedSet(new HashSet<Time>());

        // NOTE: Cannot use LinkedBlockingQueue for _inputFrames
        // because we have to release the lock on this director
        // while we are waiting or we get a deadlock.

        /** Queue of unprocessed input events. This is a blocking
         *  queue, which blocks the calling thread if the queue is empty.
         *  This is accessed by both the director thread and the inside
         *  thread, so it has to be thread safe (LinkedBlockingQueue is a
         *  thread-safe container).
         */
        private LinkedList<TokenFrame> _inputFrames = new LinkedList<TokenFrame>();

        /** List of input events in the current iteration.
         *  This is accessed only in the director thread so it need
         *  not be thread safe.
         */
        private List<QueuedToken> _inputTokens;

        /** Queue of unprocessed output events.
         *  This queue is accessed from multiple threads, so it must be
         *  thread safe.
         */
        private LinkedList<TokenFrame> _outputFrames = new LinkedList<TokenFrame>();

        /** Record of the time stamps at which
         *  to produce outputs. These are enqueued and dequeued
         *  in time stamp order. If the delay value is UNDEFINED,
         *  then this is accessed from the inside thread as well
         *  as the director thread, so it needs to be thread safe.
         *  To ensure this, we always access it within a block
         *  synchronized on this director.
         */
        private Queue<Time> _outputTimes = new LinkedList<Time>();

        /** The value of the synchronizeToRealTime parameter when
         *  initialize() was invoked.
         */
        private boolean _synchronizeToRealTime;

        /** The thread that executes the contained actors. */
        private Thread _thread;

        //////////////////////////////////////////////////////////////
        ////                   inner inner classes                ////

        ///////////////////////////////////////////////////////////////////
        //// CompositeThread

        /** The inside thread, which executes the contained actor.
         */
        private class CompositeThread extends Thread {
            public CompositeThread() {
                super("CompositeThread_" + ThreadedComposite.this.getFullName());
            }

            @Override
            public void run() {
                while (!_stopRequested) {
                    try {
                        if (ThreadedComposite.this._debugging) {
                            ThreadedComposite.this
                            ._debug("---- Waiting for inputs in the inside thread.");
                        }
                        TokenFrame frame = null;
                        synchronized (ThreadedDirector.this) {
                            // The following blocks this thread if the queue is empty.
                            while (_inputFrames.isEmpty() && !_stopRequested) {
                                // The timeout allows this to respond to stop()
                                // even if we have a deadlock for some reason.
                                ThreadedDirector.this.wait(1000L);
                            }
                            if (_stopRequested) {
                                break;
                            }
                            frame = _inputFrames.poll();
                        }

                        // Check for a "stop frame" and exit the thread.
                        if (frame.type == TokenFrame.STOP) {
                            if (ThreadedComposite.this._debugging) {
                                ThreadedComposite.this
                                ._debug("---- Read a stop frame in inside thread.");
                            }
                            break;
                        }
                        if (ThreadedComposite.this._debugging) {
                            ThreadedComposite.this
                            ._debug("---- Reading input tokens in inside thread with time "
                                    + frame.time
                                    + " and value "
                                    + frame.tokens);
                        }
                        // Current time of the director should match the frame time.
                        // This is the view of time that should be presented to any inside actors.
                        setModelTime(frame.time);

                        if (_synchronizeToRealTime) {
                            long currentRealTime = System.currentTimeMillis();
                            // If this is the first firing, record the start time.
                            if (_realStartTime < 0L) {
                                _realStartTime = currentRealTime;
                            }
                            long realTimeMillis = currentRealTime
                                    - _realStartTime;
                            long modelTimeMillis = Math.round(getModelTime()
                                    .getDoubleValue() * 1000.0);
                            if (realTimeMillis < modelTimeMillis) {
                                try {
                                    Thread.sleep(modelTimeMillis
                                            - realTimeMillis);
                                } catch (InterruptedException e) {
                                    // Ignore and continue.
                                }
                            }
                        }

                        // Note that there may not be any tokens here, since there
                        // may not be any inputs (the firing is in response to
                        // a pure event). We still want to fire the
                        // enclosed model at the specified time because the firing
                        // is due to the model itself having previously called
                        // fireAt().
                        for (QueuedToken token : frame.tokens) {
                            if (token.channel < token.port.getWidthInside()) {
                                token.port.sendInside(token.channel,
                                        token.token);
                            }
                        }
                        // Iterate the contained actors.
                        if (!iterateContainedActors()) {
                            break;
                        }

                        // If outputs are produced by the iteration, then
                        // we need to record those in an output frame.
                        List<QueuedToken> outputTokens = new LinkedList<QueuedToken>();
                        Iterator ports = outputPortList().iterator();
                        while (ports.hasNext()) {
                            IOPort port = (IOPort) ports.next();
                            for (int i = 0; i < port.getWidth(); i++) {
                                if (port.isKnownInside(i)
                                        && port.hasTokenInside(i)) {
                                    Token token = port.getInside(i);
                                    QueuedToken tokenBundle = new QueuedToken(
                                            port, i, token);
                                    outputTokens.add(tokenBundle);
                                    if (ThreadedComposite.this._debugging) {
                                        ThreadedComposite.this
                                        ._debug("---- Inside actor produced token "
                                                + token
                                                + " for port "
                                                + port.getName());
                                    }
                                }
                            }
                        }
                        Time responseTime = getModelTime().add(_delayValue);

                        synchronized (ThreadedDirector.this) {
                            // If delay is UNDEFINED, then we have to now request a
                            // refiring at the first opportunity. This is because
                            // the postfire method won't do it.
                            if (_delayValue < 0.0) {
                                // If synchronizeToRealTime is true, then we want to use the
                                // greater of real-time or the current environment time.
                                // Otherwise, we just use the current environment time.
                                if (_synchronizeToRealTime) {
                                    long realTimeMillis = System
                                            .currentTimeMillis()
                                            - _realStartTime;
                                    Time realTime = new Time(
                                            ThreadedDirector.this,
                                            realTimeMillis * 0.001);
                                    responseTime = ThreadedDirector.this
                                            .fireAt(ThreadedComposite.this,
                                                    realTime);
                                } else {
                                    responseTime = ThreadedDirector.this
                                            .fireAt(ThreadedComposite.this,
                                                    getModelTime());
                                }
                                _outputTimes.add(responseTime);
                            }
                            TokenFrame outputFrame = new TokenFrame(
                                    responseTime, outputTokens,
                                    TokenFrame.EVENT);
                            _outputFrames.add(outputFrame);
                            if (ThreadedComposite.this._debugging) {
                                ThreadedComposite.this
                                ._debug("---- Inside thread posted output frame.");
                            }
                            ThreadedDirector.this.notifyAll();
                            // Give the director thread a chance to react.
                            Thread.yield();
                        }
                    } catch (InterruptedException e) {
                        // Post a stop frame.
                        TokenFrame stopFrame = new TokenFrame(getModelTime(),
                                null, TokenFrame.STOP);
                        synchronized (ThreadedDirector.this) {
                            _outputFrames.add(stopFrame);
                            ThreadedDirector.this.notifyAll();
                        }
                        // Exit the thread.
                        break;
                    } catch (IllegalActionException ex) {
                        synchronized (ThreadedDirector.this) {
                            // To stop the outside firing, set this variable.
                            // On the next invocation of fire() or wrapup(), the
                            // exception will be thrown.
                            _exception = ex;
                            // Post a stop frame.
                            TokenFrame stopFrame = new TokenFrame(
                                    getModelTime(), null, TokenFrame.STOP);
                            _outputFrames.add(stopFrame);
                            ThreadedDirector.this.notifyAll();
                        }
                        break;
                    }
                }
            }
        }
    }
}
