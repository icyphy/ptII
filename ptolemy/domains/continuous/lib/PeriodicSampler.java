/* Generate discrete events by periodically sampling a CT signal.

 Copyright (c) 1998-2009 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// PeriodicSampler

/**
 This actor generates discrete events by periodically sampling the input signal.
 The sampling rate is given by parameter "samplePeriod", which has default value
 0.1.  Specifically, if the actor is initialized at time <i>t</i> and the sample
 period is <i>T</i>, then the output will have the value of the input
 at times <i>t</i> + <i>nT</i>, for all natural numbers <i>n</i>.
 This sampler will send to the output <i>all</i> input events that occur
 at a sample time.
 <p>
 If the enclosing director implements the SuperdenseTimeDirector
 interface, then an output is produced only if the current index
 of the director is 0. Thus, this sampler produces on its output
 the <i>initial value</i> of the input at times <i>t</i> + <i>nT</i>.
 If the input is absent at those times, then the output will be
 absent.
 <p>
 If this actor is used inside a ModalModel, then its behavior may
 seem a bit subtle, but it exactly matches the explanation above.
 If, for example, you enter a mode that contains this actor at
 time <i>t</i> + <i>nT</i>, and this actor is inside the
 refinement of that mode, then it will not normally produce
 an output at that time because the mode refinement of the
 destination state does not get fired until the next larger
 index, which will be greater than zero. It will produce
 its first output at time <i>t</i> + (<i>n</i>+1)<i>T</i>.
 FIXME: What if the enclosing director does not support superdense time?
 <p>
 This actor has multiport inputs and outputs. Signals in
 each input channel are sampled and produced to corresponding output
 channel.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class PeriodicSampler extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *  @param container The container of this actor.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public PeriodicSampler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setWidthEquals(input, true);

        samplePeriod = new Parameter(this, "samplePeriod");
        samplePeriod.setExpression("0.1");
        samplePeriod.setTypeEquals(BaseType.DOUBLE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,0 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The parameter for the sampling period. This is a double
     *  whose default value is 0.1.
     */
    public Parameter samplePeriod;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate an output if the current time is one of the sampling
     *  times. The value of the event is the value of the input signal at the
     *  current time.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (hasCurrentEvent()) {
            for (int i = 0; i < Math.min(input.getWidth(), output.getWidth()); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    output.send(i, token);

                    if (_debugging) {
                        _debug("Output: " + token
                                + " to channel " + i + ", at: "
                                + getDirector().getModelTime());
                    }
                }
            }
        }
    }
    
    /** Notify this actor that a {@link Director#fireAt(Actor,Time)}
     *  request was skipped, and that current time has passed the
     *  requested time. A director calls this method when in a modal
     *  model it was inactive at the time of the request, and it
     *  became active again after the time of the request had
     *  expired. This base class identifies the next time at which
     *  a firing should occur, and calls fireAt() with that time
     *  argument.
     *  @param time The time of the request that was skipped.
     *  @exception IllegalActionException If skipping the request
     *   is not acceptable to the actor.
     */
    public void fireAtSkipped(Time time) throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        double samplePeriodValue = ((DoubleToken)samplePeriod.getToken()).doubleValue();
        while (_nextSamplingTime.compareTo(currentTime) < 0) {
            _nextSamplingTime = _nextSamplingTime.add(samplePeriodValue);
        }
        if (_debugging) {
            _debug("Request refiring at " + _nextSamplingTime);
        }
        _fireAt(_nextSamplingTime);
    }

    /** Return true if there is a current event. In other words, the current
     *  time is one of the sampling times.
     *  @return If there is a discrete event to emit.
     */
    public boolean hasCurrentEvent() {
        Director director = getDirector();

        if (director.getModelTime().compareTo(_nextSamplingTime) == 0) {
            _hasCurrentEvent = true;

            if (_debugging && _verbose) {
                _debug(getFullName(), " has an event at: "
                        + director.getModelTime() + ".");
            }
        } else {
            _hasCurrentEvent = false;
        }

        return _hasCurrentEvent;
    }

    /** Set the next sampling time as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextSamplingTime = getDirector().getModelTime();
    }

    /** Set the next sampling time and return true.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (hasCurrentEvent()) {
            double samplePeriodValue = ((DoubleToken)samplePeriod.getToken()).doubleValue();
            _nextSamplingTime = _nextSamplingTime.add(samplePeriodValue);
            if (_debugging) {
                _debug("Request refiring at " + _nextSamplingTime);
            }
            _fireAt(_nextSamplingTime);
        }
        return super.postfire();
    }


    /** Make sure the actor runs inside a Continuous domain.
     *  @exception IllegalActionException If the director is not
     *  a ContinuousDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof ContinuousDirector)) {
            throw new IllegalActionException("PeriodicSampler can only"
                    + " be used inside Continuous domain.");
        }
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // flag indicating if there is a current event.
    // NOTE: this variable should be only used inside the hasCurrentEvent
    // method. Other methods can only access the status of this variable
    // via the hasCurrentEvent method.
    private boolean _hasCurrentEvent = false;

    // the next sampling time.
    private Time _nextSamplingTime;
}
