/* An actor that implements a queue of events.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.data.BooleanToken;
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
//// Queue

/**
 This actor implements a queue.  When a token is received on the
 <i>input</i> port, it is stored in the queue.
 When the <i>trigger</i> port receives a token, the oldest element in the
 queue is produced on the output.  If there is no element in the queue when a
 token is received on the <i>trigger</i> port, then no output is
 produced. In this circumstance, if <i>persistentTrigger</i> is true
 then the next time an input is received, it is sent immediately to
 the output.
 <p>
 The inputs can be of any token type, and the output
 is constrained to be of a type at least that of the input. If
 the <i>capacity</i> parameter is negative or zero (the default),
 then the capacity is infinite. Otherwise, the capacity is
 given by that parameter, and inputs received when the queue
 is full are discarded. Whenever the size of the queue changes,
 the new size is produced on the <i>size</i> output port.
 If an input arrives at the same time that an output is
 produced, then the <i>size</i> port gets two events at
 the same time.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (eal)
 */
public class Queue extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Queue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeAtLeast(input);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
        // Leave trigger type undeclared.
        // Put it at the bottom of the icon by default.
        StringAttribute cardinality = new StringAttribute(trigger, "_cardinal");
        cardinality.setExpression("SOUTH");

        size = new TypedIOPort(this, "size", false, true);
        size.setTypeEquals(BaseType.INT);
        // Put it at the bottom of the icon by default.
        cardinality = new StringAttribute(size, "_cardinal");
        cardinality.setExpression("SOUTH");

        _queue = new FIFOQueue();

        capacity = new Parameter(this, "capacity");
        capacity.setTypeEquals(BaseType.INT);
        capacity.setExpression("0");

        persistentTrigger = new Parameter(this, "persistentTrigger");
        persistentTrigger.setTypeEquals(BaseType.BOOLEAN);
        persistentTrigger.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The capacity of the queue. If the value is positive, then
     *  it specifies the capacity of the queue. If it is negative
     *  or 0, then it specifies that the capacity is infinite.
     *  This is an integer with default 0.
     */
    public Parameter capacity;

    /** If set to true, then if a <i>trigger</i> arrives when the
     *  queue is empty, it is remembered, and the next time an
     *  <i>input</i> arrives, it is sent immediately to the output.
     *  This is a boolean with default false.
     */
    public Parameter persistentTrigger;

    /** The current size of the queue. This port produces an output
     *  whenever the size changes. It has type int.
     */
    public TypedIOPort size;

    /** The trigger port, which has undeclared type. If this port
     *  receives a token, then the oldest token in the queue
     *  will be emitted on the <i>output</i> port.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  If the attribute is
     *  <i>capacity</i>, then change the capacity of the queue.
     *  If the size of the queue currently exceeds the specified
     *  capacity, then throw an exception.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the current size
     *   of the queue exceeds the specified capacity.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == capacity) {
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

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Queue newObject = (Queue) super.clone(workspace);
        newObject._queue = new FIFOQueue();
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(input, output, 0.0);
    }

    /** Put a new input token on the queue and/or produce output
     *  data from the queue.  Specifically, if there is a new token
     *  on the <i>input</i> port, then put it on the input queue.
     *  Then, if there is a token in the <i>trigger</i> port
     *  and the queue is not empty, then
     *  send the oldest token on the queue to the <i>output</i> port.
     *  Send the resulting queue size to the output <i>size</i> output port.
     *  @exception IllegalActionException If getting tokens from input and
     *   trigger ports or sending token to output throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int sizeOutput = _queue.size();
        boolean gotTrigger = false;
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                // Consume the trigger token.
                trigger.get(i);
                gotTrigger = true;
            }
        }
        // Increment the size only either the queue has infinite capacity,
        // the capacity is greater than the current size, or a trigger
        // input was received (which will reduce the queue size by one,
        // making room for a new token).
        if (input.hasToken(0)) {
            _token = input.get(0);
            if (_queue.getCapacity() == FIFOQueue.INFINITE_CAPACITY
                    || _queue.getCapacity() > _queue.size() || gotTrigger) {
                sizeOutput++;
            }
        } else {
            _token = null;
        }
        if (gotTrigger) {
            if (sizeOutput > 0) {
                // If there is no token on the queue,
                // then send out the currently read token.
                if (_queue.size() == 0) {
                    output.send(0, _token);
                    _token = null;
                } else {
                    output.send(0, (Token) _queue.get(0));
                    _removeTokens = 1;
                }
                sizeOutput--;
                _persistentTrigger = false;
            } else {
                if (((BooleanToken) persistentTrigger.getToken())
                        .booleanValue()) {
                    _persistentTrigger = true;
                }
            }
        } else {
            // If the queue was previously empty and
            // persistent trigger is set, and there is an
            // input, then produce the current input as output.
            if (_persistentTrigger && _token != null) {
                output.send(0, _token);
                sizeOutput--;
                _token = null;
                _persistentTrigger = false;
            }
        }
        size.send(0, new IntToken(sizeOutput));
    }

    /** Clear the cached input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _queue.clear();
        _persistentTrigger = false;
        _token = null;
        _removeTokens = 0;
        super.initialize();
    }

    /** Commit additions or removals from the queue.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_token != null) {
            _queue.put(_token);
        }
        for (int i = 0; i < _removeTokens; i++) {
            _queue.take();
        }
        _token = null;
        _removeTokens = 0;

        return super.postfire();
    }

    /** If there is no input on the <i>trigger</i> port, return
     *  false, indicating that this actor does not want to fire.
     *  This has the effect of leaving input values in the input
     *  ports, if there are any.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // If the trigger input is not connected, never fire.
        boolean hasInput = false;
        boolean hasTrigger = false;

        if (input.isOutsideConnected()) {
            hasInput = input.hasToken(0);
        }

        if (trigger.isOutsideConnected()) {
            hasTrigger = trigger.hasToken(0);
        }

        return hasInput || hasTrigger;
    }

    /** Clear the queue tokens.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // If we don't clear the queue, then you can't set the capacity
        // to smaller than the final size on the last run.  So we
        // need to either clear the queue or somehow allow that change
        // in capacity.
        _queue.clear();
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The FIFOQueue. */
    protected FIFOQueue _queue;

    /** The number of tokens that should be removed from the queue in
     *  postfire().
     */
    protected int _removeTokens;

    /** Token received in the fire() method for inclusion in
     *  the queue in the postfire() method.
     */
    protected Token _token;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An indicator of whether a trigger token was received in
     *  the last fire() method invocation.
     */
    private boolean _persistentTrigger;
}
