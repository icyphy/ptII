/* A server with a fixed or variable service time.

 Copyright (c) 1998-2011 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.FIFOQueue;
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
 This actor models a server with a fixed or variable service time.
 A server is either busy (serving a customer) or not busy at any given time.
 If an input arrives when the server is not busy, then the input token is
 produced on the output with a delay given by the <i>serviceTime</i>
 parameter.
 If an input arrives while the server is busy, then that input is
 queued until the server becomes free, at which point it is produced
 on the output with a delay given by the <i>serviceTime</i> parameter
 value at the time that the input arrived.
 If several inputs arrive while the server is busy, then they are
 served on a first-come, first-served basis.
 On every firing, produce an output indicating the final queue size.

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
        StringAttribute cardinality = new StringAttribute(
                serviceTime.getPort(), "_cardinal");
        cardinality.setExpression("SOUTH");

        _queue = new FIFOQueue();

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
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTime) {
            double value = ((DoubleToken) serviceTime.getToken()).doubleValue();

            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
        } else if (attribute == capacity) {
            int newCapacity = ((IntToken) capacity.getToken()).intValue();
            if (newCapacity <= 0) {
                if (_queue.getCapacity() != FIFOQueue.INFINITE_CAPACITY) {
                    _queue.setCapacity(FIFOQueue.INFINITE_CAPACITY);
                }
            } else {
                if (newCapacity < _queue.size()) {
                    throw new IllegalActionException(this, "Queue size ("
                            + _queue.size() + ") exceed requested capacity "
                            + newCapacity + ").");
                }
                _queue.setCapacity(newCapacity);
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Server newObject = (Server) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        newObject._queue = new FIFOQueue();
        return newObject;
    }

    /** Declare that the <i>output</i>
     *  does not depend on the <i>input</i> and <i>serviceTime</i> in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(input, output, 0.0);
        _declareDelayDependency(serviceTime.getPort(), output, 0.0);
    }

    /** If there is input, read it and put it in the queue.
     *  If the service time has expired for a token currently
     *  in the queue, then send that token on the output.
     *  Produce an output indicating the current queue size.
     *  @exception IllegalActionException If the serviceTime is invalid,
     *   or if an error occurs sending the output token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();

        // Consume the input.
        if (input.hasToken(0)) {
            serviceTime.update();
            double serviceTimeValue = ((DoubleToken) serviceTime.getToken())
                    .doubleValue();
            Token token = input.get(0);
            _queue.put(new Job(token, serviceTimeValue));
            if (_debugging) {
                _debug("Read input with value " + token
                        + ", and put into queue, which now has size"
                        + _queue.size()
                        + " at time "
                        + currentTime
                        + ". Event will be processes with service time "
                        + serviceTimeValue);
            }
        }

        // If appropriate, produce output.
        if (_queue.size() > 0 && currentTime.compareTo(_nextTimeFree) == 0) {
            Job job = (Job) _queue.take();
            Token outputToken = job.payload;
            output.send(0, outputToken);
            // Indicate that the server is free.
            _nextTimeFree = Time.NEGATIVE_INFINITY;
            if (_debugging) {
                _debug("Produced output " + outputToken
                        + ", so queue now has size "
                        + _queue.size()
                        + " at time "
                        + currentTime);
            }
        }
        size.send(0, new IntToken(_queue.size()));
    }

    /** Reset the states of the server to indicate that the server is ready
     *  to serve.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = Time.NEGATIVE_INFINITY;
        _queue.clear();
    }

    /** If the server is free and there is at least one token in the queue,
     *  request a firing at the current time plus the service time.
     *  @exception IllegalActionException If there is no director.
     *  @return Whatever the superclass returns.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        if (_nextTimeFree.equals(Time.NEGATIVE_INFINITY) && _queue.size() > 0) {
            Job job = (Job)_queue.get(0);
            _nextTimeFree = currentTime.add(job.serviceTime);
            if (_debugging) {
                _debug("In postfire, requesting a refiring at time " + _nextTimeFree);
            }
            _fireAt(_nextTimeFree);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Next time the server becomes free. */
    protected Time _nextTimeFree;

    /** The FIFOQueue. */
    protected FIFOQueue _queue;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A data structure containing a token and a service time. */
    private class Job {
        public Job (Token payload, double serviceTime) {
            this.payload = payload;
            this.serviceTime = serviceTime;
        }
        public Token payload;
        public double serviceTime;
    }
}
