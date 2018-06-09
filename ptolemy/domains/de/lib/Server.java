/* A server with a fixed or variable service time.

 Copyright (c) 1998-2018 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Server

/**
 This actor models a preemptive server with a fixed or variable service time.
 A server is either busy (serving a customer) or not busy at any given time.
 If an input arrives when the server is not busy, then the input token is
 produced on the output with a delay given by the <i>serviceTime</i>
 parameter.
 If an input arrives while the server is busy, then that input is
 queued until the server becomes free, at which point it is produced
 on the output with a delay given by the <i>serviceTime</i> parameter
 value at the time that the input arrived.
 If several inputs arrive while the server is busy, then they are
 served in an order determined by the value of the <i>priority</i>
 parameter at the time of arrival of the input, and for inputs with
 identical priorities, on a first-come, first-served basis.
 On every firing, produce an output indicating the final queue size.
 <p>
 The service time and priority used for a job are the most recently arrived
 values prior to or simultaneous with the <i>arrival</i> of the job
 (not with the time at which service begins). Thus, if you want
 each job to have an independent service time or priority, you should
 provide each parameter as an input synchronized with each new job arrival.

 @see ptolemy.actor.lib.TimeDelay

 @author Lukito Muliadi, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class Server extends DETransformer {
    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Server(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeSameAs(input);

        // FIXME: Put in a name change from newServiceTime to serviceTime in MoML filters.
        serviceTime = new PortParameter(this, "serviceTime");
        serviceTime.setExpression("1.0");
        serviceTime.setTypeEquals(BaseType.DOUBLE);
        // Put the delay port at the bottom of the icon by default.
        StringAttribute cardinality = new StringAttribute(serviceTime.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        priority = new PortParameter(this, "priority");
        priority.setExpression("0.0");
        priority.setTypeEquals(BaseType.DOUBLE);
        // Put the priority port at the bottom of the icon by default.
        cardinality = new StringAttribute(priority.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");

        _queues = new TreeMap<Double, PriorityQueue<Job>>(
                new PriorityComparator());

        _queueCounter = 0;
        _queueSize = 0;

        size = new TypedIOPort(this, "size", false, true);
        size.setTypeEquals(BaseType.INT);
        // Put it at the bottom of the icon by default.
        cardinality = new StringAttribute(size, "_cardinal");
        cardinality.setExpression("SOUTH");

        capacity = new Parameter(this, "capacity");
        capacity.setTypeEquals(BaseType.INT);
        capacity.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The capacity of the queue. If the value is positive, then
     *  it specifies the capacity of the queue. If it is negative
     *  or 0, then it specifies that the capacity is infinite.
     *  This is an integer with default 0.
     */
    public Parameter capacity;

    /** The current size of the queue. This port produces an output
     *  whenever the size changes. It has type int.
     */
    public TypedIOPort size;

    /** The service time. This is a double with default 1.0.
     *  It is required to be non-negative.
     */
    public PortParameter serviceTime;

    /** The priority. This is a double with default 0.0.
     *  A higher priority implies the task has precedence over tasks
     *  with lower priority values.
     */
    public PortParameter priority;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative, and if the attribute is
     *  <i>capacity</i>, then change the capacity of the queue.
     *  If the size of the queue currently exceeds the specified
     *  capacity, then throw an exception.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            double value = ((DoubleToken) serviceTime.getToken()).doubleValue();

            if (value < 0.0 || Double.isNaN(value)) {
                throw new IllegalActionException(this,
                        "Cannot have negative or NaN serviceTime: " + value);
            }
        } else if (attribute == priority) {
            double value = ((DoubleToken) priority.getToken()).doubleValue();

            if (Double.isNaN(value)) {
                throw new IllegalActionException(this,
                        "Cannot have NaN priority: " + value);
            }
        } else if (attribute == capacity) {
            int newCapacity = ((IntToken) capacity.getToken()).intValue();
            if (newCapacity > 0 && queueSize() > newCapacity) {
                throw new IllegalActionException(this,
                        "Queue size (" + queueSize()
                                + ") exceed requested capacity " + newCapacity
                                + ").");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Server newObject = (Server) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        newObject._queues = new TreeMap<Double, PriorityQueue<Job>>(
                new PriorityComparator());
        return newObject;
    }

    /** Declare that the <i>output</i> does not depend on
     *  the <i>input</i>, <i>serviceTime</i>, and <i>priority</i>, in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(input, output, 0.0);
        _declareDelayDependency(serviceTime.getPort(), output, 0.0);
        _declareDelayDependency(priority.getPort(), output, 0.0);
    }

    /** If there is input, read it and put it in the queue.
     *  If the service time has expired for a token currently
     *  in the queue, then send that token on the output.
     *  Produce an output indicating the current queue size.
     *  @exception IllegalActionException If the serviceTime
     *  or priority is invalid, or if an error occurs sending
     *  the output token.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();

        serviceTime.update();
        priority.update();

        Job currentJob = this.peekQueue();
        if (currentJob != null) {
            currentJob.serviceTimeRemaining = _nextTimeFree
                    .subtractToDouble(currentTime);
        }

        long nextQueueCounter = _queueCounter;
        // Consume the input.
        if (input.hasToken(0)) {
            double serviceTimeValue = ((DoubleToken) serviceTime.getToken())
                    .doubleValue();
            double priorityValue = ((DoubleToken) priority.getToken())
                    .doubleValue();
            Token token = input.get(0);
            this.enqueue(new Job(token, serviceTimeValue, priorityValue,
                    currentTime));
            if (_debugging) {
                _debug("Read input with value " + token
                        + ", and put into queue, which now has size"
                        + queueSize() + " at time " + currentTime
                        + ". Event will be processes with service time "
                        + serviceTimeValue);
            }
        }

        // If appropriate, produce output.
        Job job = this.peekQueue();
        if (job != null && currentTime.compareTo(_nextTimeFree) == 0 &&
        /* We check the queueCounter to ensure at least a microstep delay */
                job.queueCounter < nextQueueCounter) {
            job = this.dequeue();
            Token outputToken = job.payload;
            output.send(0, outputToken);
            // Indicate that the server is free.
            if (_debugging) {
                _debug("Produced output " + outputToken
                        + ", so queue now has size " + queueSize() + " at time "
                        + currentTime);
            }
        }
        size.send(0, new IntToken(queueSize()));
    }

    protected int queueSize() {
        return _queueSize;
    }

    private Map.Entry<Double, PriorityQueue<Job>> getHighestPriorityQueue() {
        return _queues.firstEntry();
    }

    private Job peekQueue() {
        Map.Entry<Double, PriorityQueue<Job>> entry = getHighestPriorityQueue();
        Job result = entry != null ? entry.getValue().peek() : null;
        return result;
    }

    private void enqueue(Job newJob) throws IllegalActionException {
        int currentCapacity = ((IntToken) capacity.getToken()).intValue();
        if (currentCapacity > 0 && queueSize() >= currentCapacity) {
            throw new IllegalActionException(this,
                    "Queue size (" + queueSize()
                            + ") is already at maximum capacity "
                            + currentCapacity + ").");
        }
        newJob.queueCounter = _queueCounter;
        PriorityQueue<Job> queue = _queues.get(newJob.priority);
        if (queue == null) {
            queue = new PriorityQueue<Job>();
            _queues.put(newJob.priority, queue);
        }
        queue.add(newJob);
        ++_queueSize;
        ++_queueCounter;
        updateNextTimeFree();
    }

    private void updateNextTimeFree() {
        Job job = this.peekQueue();
        if (job != null) {
            Time now = getDirector().getModelTime();
            _nextTimeFree = now.add(job.serviceTimeRemaining);
        }
    }

    private Job dequeue() throws IllegalActionException {
        Job job = null;
        while (job == null && !_queues.isEmpty()) {
            Map.Entry<Double, PriorityQueue<Job>> entry = getHighestPriorityQueue();
            PriorityQueue<Job> queue = entry.getValue();
            if (!queue.isEmpty()) {
                job = queue.poll();
            }
            if (queue.isEmpty()) {
                _queues.remove(entry.getKey());
            }
        }
        assert job != null : "No job found (was queue not empty?)";
        if (job != null) {
            --_queueSize;
        }
        updateNextTimeFree();
        return job;
    }

    /** Reset the states of the server to indicate that the server is ready
     *  to serve.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = Time.NEGATIVE_INFINITY;
        _queues.clear();
        _queueCounter = 0;
        _queueSize = 0;
    }

    /** If the server is free and there is at least one token in the queue,
     *  request a firing at the current time plus the service time.
     *  @exception IllegalActionException If there is no director.
     *  @return Whatever the superclass returns.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        updateNextTimeFree();
        if (!_nextTimeFree.equals(Time.NEGATIVE_INFINITY) && queueSize() > 0) {
            if (_debugging) {
                _debug("In postfire, requesting a refiring at time "
                        + _nextTimeFree);
            }
            _fireAt(_nextTimeFree);
        }
        return super.postfire();
    }

    /** Clear the queue so that the capacity can be changed.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _queues.clear();
        _queueCounter = 0;
        _queueSize = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Next time the server becomes free. */
    protected Time _nextTimeFree;

    protected TreeMap<Double /* the dynamic (possibly modified) priority of the queue */, PriorityQueue<Job>> _queues;

    protected int _queueSize;

    /** The counter for tie-breaking the queue by insertion order. */
    protected long _queueCounter;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private static class PriorityComparator implements Comparator<Double> {
        public static int compare(double a, double b) {
            return Double.compare(b, a); // Note the swapped order (higher priorities come first)
        }

        @Override
        public int compare(Double a, Double b) {
            return compare((double) a, (double) b);
        }
    }

    /** A data structure containing a token and a service time. */
    private static class Job implements Comparable<Job> {
        public Job(Token payload, double serviceTime, double priority,
                Time creationTime) {
            this.payload = payload;
            this.serviceTimeRemaining = serviceTime;
            this.priority = priority;
            this.creationTime = creationTime;
        }

        public Token payload;
        public double serviceTimeRemaining;
        public double priority; //     priority=1 >     priority=0
        public Time creationTime; // creationTime=1 < creationTime=0
        public long queueCounter; // queueCounter=1 < queueCounter=0

        @Override
        public int compareTo(Job other) {
            int result = 0;
            if (result == 0) {
                result = PriorityComparator.compare(this.priority,
                        other.priority);
            }
            if (result == 0) {
                result = this.creationTime.compareTo(other.creationTime);
            }
            if (result == 0) {
                result = Long.compare(this.queueCounter, other.queueCounter);
            }
            return result;
        }
    }
}
