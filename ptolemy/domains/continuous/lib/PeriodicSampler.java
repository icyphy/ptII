/* Generate discrete events by periodically sampling a CT signal.

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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.lib.Sampler;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.CausalityInterface;
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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PeriodicSampler

/**
 This actor generates discrete events by periodically sampling the input signal.
 The sampling rate is given by parameter <i>samplePeriod</i>, which has default value
 0.1.  Specifically, if the actor is initialized at time <i>t</i> and the sample
 period is <i>T</i>, then the output will have the value of the input
 at times <i>t</i> + <i>nT</i>, for all natural numbers <i>n</i>.
 By default, this sampler will send to the output the initial value of
 the input (the input value at microstep 0), but will send it one
 microstep later (at microstep 1).
 This ensures that the output at microstep 0 is always absent, thus
 ensuring continuity from the left. That is, the input is absent prior
 to the sample time, so continuity requires that it be absent at
 microstep 0 at the sample time.
 <p>
 To get this sampler to record values other than the initial value,
 set the <i>microstep</i> parameter to a value greater than 0.
 Setting it to 1, for example, will record the input value after
 a discontinuity rather than before the discontinuity. Note
 that {@link Sampler} normally records its inputs at microstep 1
 (because it is triggered by a discrete signal, which has events
 at microstep 1), and therefore if you want this PeriodicSampler
 to behave the same as Sampler, you should set <i>microstep</i>
 to 1.
 <p>
 This actor has multiport inputs and outputs. Signals in
 each input channel are sampled and produced to corresponding output
 channel.
 <p>
 Note that this actor does not tolerate changing input or output
 connections during execution.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
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

        microstep = new Parameter(this, "microstep");
        microstep.setExpression("0");
        microstep.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,0 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The microstep at which to read the input. This is an
     *  whose default value is 0.
     */
    public Parameter microstep;

    /** The parameter for the sampling period. This is a double
     *  whose default value is 0.1.
     */
    public Parameter samplePeriod;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is microstep, adjust the causality interface.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == microstep) {
            int microstepValue = ((IntToken) microstep.getToken()).intValue();
            if (microstepValue != _microstep) {
                CausalityInterface causalityInterface = getCausalityInterface();
                if (microstepValue == 0) {
                    // Output depends on the input after a microstep delay.
                    causalityInterface.declareDelayDependency(input, output,
                            0.0, 1);
                } else {
                    // Output depends immediately on the input.
                    causalityInterface.declareDelayDependency(input, output,
                            0.0, 0);
                }
                _microstep = microstepValue;
                Director director = getDirector();
                if (director != null) {
                    director.invalidateSchedule();
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PeriodicSampler newObject = (PeriodicSampler) super.clone(workspace);
        newObject.output.setWidthEquals(newObject.input, true);
        return newObject;
    }

    /** Generate an output if the current time is one of the sampling
     *  times and the microstep matches.
     *  In addition, if the microstep parameter has value 0,
     *  produce the output only if the current microstep is 1.
     *  The value of the event is the value of the input signal at the
     *  current time at the microstep specified by the microstep parameter.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        SuperdenseTimeDirector director = (SuperdenseTimeDirector) getDirector();
        Time currentTime = getDirector().getModelTime();
        int microstep = director.getIndex();
        if (_debugging) {
            _debug("Current time is " + currentTime + " with microstep "
                    + microstep);
        }
        int inputWidth = input.getWidth();
        int outputWidth = output.getWidth();
        if (currentTime.compareTo(_nextSamplingTime) == 0) {
            // Time is right to produce an output. Check the microstep.
            if (_microstep == 0 && microstep == 1) {
                // In delay mode. Input should have been read in microstep 0.
                for (int i = 0; i < outputWidth; i++) {
                    if (_pendingOutputs[i] != null) {
                        // There is a deferred output for this input channel.
                        output.send(i, _pendingOutputs[i]);
                        if (_debugging) {
                            _debug("Sending output value " + _pendingOutputs[i]
                                    + " on channel " + i);
                        }
                    } else {
                        output.sendClear(i);
                    }
                }
            } else if (_microstep != 0 && _microstep == microstep) {
                // Microstep matches.
                // Read the input and produce an output.
                for (int i = 0; i < inputWidth; i++) {
                    if (input.isKnown(i) && input.hasToken(i)) {
                        Token token = input.get(i);
                        if (i < outputWidth) {
                            output.send(i, token);
                            if (_debugging) {
                                _debug("Read input and sent to output: "
                                        + token + " on channel " + i);
                            }
                        }
                    }
                }
                // If the output is wider than the input, send clear.
                for (int i = inputWidth; i < outputWidth; i++) {
                    output.sendClear(i);
                }
            } else {
                // Microstep does not match.
                for (int i = 0; i < outputWidth; i++) {
                    output.sendClear(i);
                }
            }
        } else {
            // Not time to send an output.
            for (int i = 0; i < outputWidth; i++) {
                output.sendClear(i);
            }
        }
    }

    /** Set the next sampling time for each
     *  input as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        int width = output.getWidth();
        if (_pendingOutputs == null || _pendingOutputs.length != width) {
            _pendingOutputs = new Token[width];
        }
        Time currentTime = getDirector().getModelTime();
        _nextSamplingTime = currentTime;
        for (int i = 0; i < width; i++) {
            _pendingOutputs[i] = null;
        }
        getDirector().fireAt(this, _nextSamplingTime, _microstep);
    }

    /** Return false if the microstep value
     *  is zero. In that case, this actor can produce some outputs even the
     *  inputs are unknown. This actor is usable for breaking feedback
     *  loops. It does not read inputs in the fire() method.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        if (_microstep == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** If the current microstep is zero, sample the inputs and request
     *  a refiring at the current time. If it is one, then request a refiring
     *  at the next sample time.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int microstep = ((SuperdenseTimeDirector) director).getIndex();
        int inputWidth = input.getWidth();
        if (currentTime.compareTo(_nextSamplingTime) == 0) {
            // Current time matches. Check microstep.
            if (_microstep == 0) {
                // In delay mode. Read the input if the microstep is 0.
                if (microstep == 0) {
                    int outputWidth = output.getWidth();
                    for (int i = 0; i < outputWidth; i++) {
                        if (i < inputWidth && input.hasToken(i)) {
                            _pendingOutputs[i] = input.get(i);
                        } else {
                            _pendingOutputs[i] = null;
                        }
                        if (_debugging) {
                            _debug("Read input: " + _pendingOutputs[i]
                                    + " on channel " + i);
                        }
                    }
                    for (int i = outputWidth; i < inputWidth; i++) {
                        // Read and discard the input.
                        input.get(i);
                    }
                    director.fireAt(this, currentTime, 1);
                } else {
                    if (microstep == 1) {
                        double samplePeriodValue = ((DoubleToken) samplePeriod
                                .getToken()).doubleValue();
                        _nextSamplingTime = currentTime.add(samplePeriodValue);
                        director.fireAt(this, _nextSamplingTime, 0);
                    }
                    // Consume the inputs.
                    for (int i = 0; i < inputWidth; i++) {
                        if (input.hasToken(i)) {
                            input.get(i);
                        }
                    }
                }
            } else {
                // Not in delay mode. If the microstep matches,
                // request refiring.
                if (_microstep == microstep) {
                    double samplePeriodValue = ((DoubleToken) samplePeriod
                            .getToken()).doubleValue();
                    _nextSamplingTime = currentTime.add(samplePeriodValue);
                    director.fireAt(this, _nextSamplingTime, _microstep);
                } else {
                    // Consume the inputs.
                    for (int i = 0; i < inputWidth; i++) {
                        if (input.hasToken(i)) {
                            input.get(i);
                        }
                    }
                }
            }
        } else {
            // Time does not match.
            // Consume the inputs.
            for (int i = 0; i < inputWidth; i++) {
                if (input.hasToken(i)) {
                    input.get(i);
                }
            }
        }
        return super.postfire();
    }

    /** Make sure the actor runs inside a domain that understands
     *  superdense time.
     *  @exception IllegalActionException If the director is not
     *  a SuperdenseTimeDirector or the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof SuperdenseTimeDirector)) {
            throw new IllegalActionException("PeriodicSampler can only"
                    + " be used inside a superdense time domain.");
        }
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The next sampling time. */
    private Time _nextSamplingTime;

    /** Record of pending output tokens (those that have been
     *  delayed because they appeared at the input when the
     *  the microstep was zero).
     */
    private Token[] _pendingOutputs;

    /** Value of the microstep. */
    private int _microstep = 1;
}
