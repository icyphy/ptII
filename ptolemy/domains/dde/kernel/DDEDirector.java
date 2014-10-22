/* A DDEDirector governs the execution of actors operating according
 to the DDE model of computation.

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
package ptolemy.domains.dde.kernel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DDEDirector

/**
 * A DDEDirector governs the execution of actors operating according to the DDE
 * model of computation (MoC). The DDE MoC incorporates a distributed notion of
 * time into a dataflow style communication semantic. Blocking reads occur if
 * attempts to consume data are made when the corresponding receiver is empty
 * and blocking writes occur if attempts to produce data are made when the
 * corresponding receiver is full.
 * <P>
 * In conjunction with the blocking read/write facilities, the DDE domain uses a
 * distributed, local notion of time. In a network of actors governed by a
 * DDEDirector each actor has a local notion of time. Several features of the
 * DDEDirector are intended to facilitate these local notions of time.
 * <P>
 * All DDE models have a completion time. The completion time is a preset time
 * after which all execution ceases. The completion time for a DDEDirector is
 * specified via the <I>stopTime</I> parameter. The value of the stopTime
 * parameter is passed to the receivers of all actors that the DDEDirector
 * governs via newReceiver() during initialize(). After initialize() has been
 * called, the value of stopTime can not be changed.
 * <P>
 * The default value of the stopTime parameter is
 * PrioritizedTimedQueue.ETERNITY. Given this value, a DDE model will continue
 * executing without regard for a completion time.
 * <P>
 * Deadlock due to feedback loops is dealt with via NullTokens. When an actor in
 * a DDE model receives a NullToken, it may advance its local time value even
 * though no computation results directly from consumption of the NullToken. For
 * models with feedback topologies, the FeedBackDelay actor should be used in
 * the feedback loop.
 * <P>
 * The DDE model of computation assumes that valid time stamps have non-negative
 * values. Three special purpose negative time values are reserved with the
 * following meanings. The value of PrioritizedTimedQueue.INACTIVE is reserved
 * to indicate the termination of a receiver. The value of
 * PrioritizedTimedQueue.ETERNITY is reserved to indicate that a receiver has
 * not begun to participate in a model's execution. The value of
 * PrioritizedTimedQueue.IGNORE is reserved to indicate that the current token
 * at the head of a DDEReceiver should be ignored in favor of the tokens
 * contained in the other receivers of the actor in question. More details of
 * IGNORE can be found in FeedBackDelay.
 * <P>
 * NOTE: The current implementation of this director does not include an
 * infrastructure for mutations. Hence, ChangeRequest and other facilities for
 * changing the topology of a model are not included in this director.
 *
 *
 * @author John S. Davis II, Mudit Goel
 * @version $Id$
 * @since Ptolemy II 0.3
 * @Pt.ProposedRating Red (davisj)
 * @Pt.AcceptedRating Red (cxh)
 * @see ptolemy.domains.pn.kernel.PNDirector
 * @see ptolemy.domains.dde.kernel.FeedBackDelay
 * @see ptolemy.domains.dde.kernel.NullToken
 */
public class DDEDirector extends CompositeProcessDirector {
    /**
     * Construct a DDEDirector in the default workspace with an empty string as
     * its name. The director is added to the list of objects in the workspace.
     * Increment the version number of the workspace.
     * @exception IllegalActionException If the director is not
     * compatible with the specified container.
     * @exception NameDuplicationException If thrown while adding a
     * stopTime parameter.
     */
    public DDEDirector() throws IllegalActionException,
            NameDuplicationException {
        super();

        double value = PrioritizedTimedQueue.ETERNITY;
        stopTime.setToken(new DoubleToken(value));
    }

    /**
     * Construct a director in the workspace with an empty string as a
     * name. The director is added to the list of objects in the
     * workspace. Increment the version number of the workspace.
     * @param workspace The workspace of this object.
     * @exception IllegalActionException If the director is not
     * compatible with the specified container.
     * @exception NameDuplicationException If thrown while adding a
     * stopTime parameter.
     */
    public DDEDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);

        double value = PrioritizedTimedQueue.ETERNITY;
        stopTime.setToken(new DoubleToken(value));
    }

    /**
     * Construct a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. The given name must be unique with respect to the container. If
     * the name argument is null, then the name is set to the empty string.
     * Increment the version number of the workspace.
     *
     * @param container The container of this director.
     * @param name Name of this director.
     * @exception IllegalActionException If the director is not
     * compatible with the specified container. May be thrown in a
     * derived class.
     * @exception NameDuplicationException If the container not a
     * CompositeActor and the name collides with an entity in the
     * container.
     */
    public DDEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        double value = PrioritizedTimedQueue.ETERNITY;
        stopTime.setToken(new DoubleToken(value));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DDEDirector newObject = (DDEDirector) super.clone(workspace);
        newObject._writeBlockedQueues = new HashMap();
        return newObject;
    }

    /**
     * Schedule an actor to be fired at the specified time. If the thread that
     * calls this method is an instance of DDEThread, then the specified actor
     * must be contained by this thread. If the thread that calls this method is
     * not an instance of DDEThread, then store the actor and refire time in the
     * initial time table of this director.
     * <P>
     * NOTE: The current implementation of this method is such that a
     * more appropriate name might be <I>continueAt()</I> rather than
     * <I>fireAt()</I>.
     *
     * @param actor The actor scheduled to fire.
     * @param time The scheduled time to fire.
     * @param microstep The microstep (ignored by this director).
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     * @exception IllegalActionException If the specified time is in
     * the past or if the thread calling this method is a DDEThread
     * but the specified actor is not contained by the DDEThread.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        double ETERNITY = PrioritizedTimedQueue.ETERNITY;
        DDEThread ddeThread;
        Thread thread = Thread.currentThread();

        if (thread instanceof DDEThread) {
            ddeThread = (DDEThread) thread;
        } else {
            // Add the start time of actor to initialize table
            if (_initialTimeTable == null) {
                _initialTimeTable = new Hashtable();
            }

            _initialTimeTable.put(actor, Double.valueOf(time.getDoubleValue()));
            return time;
        }

        if (_completionTime.getDoubleValue() != ETERNITY
                && time.compareTo(_completionTime) > 0) {
            return time;
        }

        Actor threadActor = ddeThread.getActor();

        if (threadActor != actor) {
            throw new IllegalActionException("Actor argument of "
                    + "DDEDirector.fireAt() must be contained "
                    + "by the DDEThread that calls fireAt()");
        }

        TimeKeeper timeKeeper = ddeThread.getTimeKeeper();

        try {
            if (_debugging) {
                _debug("fireAt " + actor + " time " + time);
                _debug("current time was "
                        + timeKeeper.getModelTime().getDoubleValue());
            }

            timeKeeper.setCurrentTime(time);
        } catch (IllegalArgumentException e) {
            throw new IllegalActionException(((NamedObj) actor).getName()
                    + " - Attempt to " + "set current time in the past.");
        }
        return time;
    }

    /**
     * Return the current time of the DDEThread that calls this method on behalf
     * of an actor. If this method is called by other than a DDEThread, then
     * return the current time as specified by the superclass of this method.
     *
     * @return The current time of the DDEThread that calls this method.
     *  @deprecated As of Ptolemy II 4.1, replaced by
     *  {@link #getModelTime()}
     */
    @Deprecated
    @Override
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
    }

    /**
     * Return the current time of the DDEThread that calls this method on behalf
     * of an actor. If this method is called by other than a DDEThread, then
     * return the current time as specified by the superclass of this method.
     *
     * @return The current time of the DDEThread that calls this method.
     */
    @Override
    public Time getModelTime() {
        Thread thread = Thread.currentThread();

        if (thread instanceof DDEThread) {
            TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
            return timeKeeper.getModelTime();
        } else {
            return super.getModelTime();
        }
    }

    /**
     * Initialize this director and the actors it contains and set variables to
     * their initial values. Create a DDEThread for each actor that this
     * director controls but do not start the thread.
     *
     * @exception IllegalActionException If there is an error during
     * the creation of the threads or initialization of the actors.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _completionTime = new Time(this, PrioritizedTimedQueue.ETERNITY);
        _writeBlockedQueues = new HashMap();
        super.initialize();
    }

    /**
     * Return a new receiver of a type compatible with this director. If the
     * completion time of this director has been explicitly set to a particular
     * value then set the completion time of the receiver to this same value;
     * otherwise set the completion time to PrioritizedTimedQueue.ETERNITY which
     * indicates that the receivers should ignore the completion time.
     *
     * @return A new DDEReceiver.
     */
    @Override
    public Receiver newReceiver() {
        DDEReceiver receiver = new DDEReceiver();
        double timeValue;

        try {
            timeValue = ((DoubleToken) stopTime.getToken()).doubleValue();
            receiver._setCompletionTime(new Time(this, timeValue));
            receiver._lastTime = new Time(this);
        } catch (IllegalActionException e) {
            // If the time resolution of the director or the stop
            // time is invalid, it should have been caught before this.
            throw new InternalErrorException(e);
        }

        return receiver;
    }

    /**
     * Return true if the actors governed by this director can continue
     * execution, and false otherwise. Continuation of execution is dependent
     * upon whether the system is deadlocked in a manner that can not be
     * resolved even if external communication occurs. If stop() has been
     * called, then return false.
     *
     * @return True if execution can continue; false otherwise.
     * @exception IllegalActionException Not thrown in this base class.
     * May be thrown in derived classes.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Thread thread = Thread.currentThread();

        if (thread instanceof DDEThread) {
            TimeKeeper timeKeeper = ((DDEThread) thread).getTimeKeeper();
            timeKeeper.removeAllIgnoreTokens();
        }

        return super.postfire();
    }

    /**
     * Notify the director that the specified thread is blocked on an I/O
     * operation.
     *
     * @param thread The thread.
     * @param receiver The receiver handling the I/O operation, or
     * null if it is not a specific receiver.
     * @param readOrWrite Either READ_BLOCKED or WRITE_BLOCKED to
     * indicate whether the thread is blocked on read or write.
     * @see CompositeProcessDirector#threadBlocked(Thread, ProcessReceiver)
     */
    public synchronized void threadBlocked(Thread thread,
            ProcessReceiver receiver, boolean readOrWrite) {
        if (readOrWrite == WRITE_BLOCKED) {
            _writeBlockedQueues.put(receiver, thread);
        }

        super.threadBlocked(thread, receiver);
    }

    /**
     * Notify the director that the specified thread is unblocked on an I/O
     * operation. If the thread has not been registered with threadBlocked(),
     * then this call is ignored.
     *
     * @param thread The thread.
     * @param receiver The receiver handling the I/O operation, or
     * null if it is not a specific receiver.
     * @param readOrWrite Either READ_BLOCKED or WRITE_BLOCKED to
     * indicate whether the thread is blocked on read or write.
     * @see CompositeProcessDirector#threadUnblocked(Thread, ProcessReceiver)
     */
    public synchronized void threadUnblocked(Thread thread,
            ProcessReceiver receiver, boolean readOrWrite) {
        if (readOrWrite == WRITE_BLOCKED) {
            _writeBlockedQueues.remove(receiver);
        }

        super.threadUnblocked(thread, receiver);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // Findbugs suggests making these final.

    /** Indicator that a thread is read blocked. */
    public static final boolean READ_BLOCKED = true;

    /** Indicator that a thread is write blocked. */
    public static final boolean WRITE_BLOCKED = false;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Increment the port capacity's according to Tom Parks' algorithm. Select
     * the port with the smallest capacity and double the capacity.
     *
     * @exception IllegalActionException If there is an error while
     * attempting to set the capacity of a DDE receiver.
     */
    protected void _incrementLowestCapacityPort() throws IllegalActionException {
        if (_writeBlockedQueues == null) {
            return;
        }

        PNQueueReceiver smallestCapacityQueue = null;
        int smallestCapacity = -1;
        Iterator receivers = _writeBlockedQueues.keySet().iterator();

        if (!receivers.hasNext()) {
            return;
        }

        while (receivers.hasNext()) {
            PNQueueReceiver queue = (PNQueueReceiver) receivers.next();

            if (smallestCapacity == -1) {
                smallestCapacityQueue = queue;
                smallestCapacity = queue.getCapacity();
            } else if (smallestCapacity > queue.getCapacity()) {
                smallestCapacityQueue = queue;
                smallestCapacity = queue.getCapacity();
            }
        }

        if (smallestCapacityQueue.getCapacity() <= 0) {
            smallestCapacityQueue.setCapacity(1);
        } else {
            int cap = smallestCapacityQueue.getCapacity();
            smallestCapacityQueue.setCapacity(cap * 2);
        }

        // Need to mark any thread that is blocked on
        // this receiver unblocked now, before the notification,
        // or we will detect deadlock all over again and
        // again increase the buffer sizes.
        threadUnblocked(
                (Thread) _writeBlockedQueues.get(smallestCapacityQueue),
                smallestCapacityQueue, WRITE_BLOCKED);

        notifyAll();

        synchronized (smallestCapacityQueue) {
            smallestCapacityQueue.notifyAll();
        }
    }

    /**
     * Return a new ProcessThread of a type compatible with this director.
     *
     * @param actor The actor that the new ProcessThread will control.
     * @param director The director that manages the new
     * ProcessThread.
     * @return A new DDEThread.
     * @exception IllegalActionException If an error occurs while
     * instantiating the new ProcessThread.
     */
    @Override
    protected ProcessThread _newProcessThread(Actor actor,
            ProcessDirector director) throws IllegalActionException {
        return new DDEThread(actor, director);
    }

    /**
     * Apply an algorithm to resolve an internal deadlock and return
     * true if the algorithm is successful. If the algorithm is
     * unsuccessful then return false. The algorithm applied was
     * created by Thomas Parks for resolving internal deadlocks in
     * which one or more actors are write blocked.
     *
     * @return True if an internal deadlock has been resolved;
     * otherwise return false.
     * @exception IllegalActionException If thrown while incrementing the
     * lowest capacity port.
     */
    @Override
    protected synchronized boolean _resolveInternalDeadlock()
            throws IllegalActionException {
        System.out.println("_writeBlockedQueues.size() = "
                + _writeBlockedQueues.size());

        if (_writeBlockedQueues.size() > 0) {
            _incrementLowestCapacityPort();
            return true;
        }

        return super._resolveInternalDeadlock();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     package friendly methods              ////

    /**
     * Return the initial time table of this director.
     *
     * @return The initial time table of this director.
     */
    Hashtable _getInitialTimeTable() {
        if (_initialTimeTable == null) {
            _initialTimeTable = new Hashtable();
        }

        return _initialTimeTable;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The completion time. Since the completionTime is a constant,
     *  we do not convert it to a time object.
     */
    private Time _completionTime;

    /** The set of receivers blocked on a write to a receiver. */
    private HashMap _writeBlockedQueues = new HashMap();

    private Hashtable _initialTimeTable;
}
