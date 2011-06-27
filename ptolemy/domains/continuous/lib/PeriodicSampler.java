/* Generate discrete events by periodically sampling a CT signal.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PeriodicSampler

/**
 This actor generates discrete events by periodically sampling the input signal.
 The sampling rate is given by parameter "samplePeriod", which has default value
 0.1.  Specifically, if the actor is initialized at time <i>t</i> and the sample
 period is <i>T</i>, then the output will have the value of the input
 at times <i>t</i> + <i>nT</i>, for all natural numbers <i>n</i>.
 This sampler will send to the output the initial value of
 the input (at microstep 0), but will send it one microstep later (at 1).
 This ensures that the output at microstep 0 is always absent, thus
 ensuring continuity from the left. That is, the input is absent prior
 to the sample time, so continuity requires that it be absent at
 microstep 0 at the sample time.
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PeriodicSampler newObject = (PeriodicSampler) super.clone(workspace);
        newObject.output.setWidthEquals(newObject.input, true);

        return newObject;
    }

    /** Generate an output if the current time is one of the sampling
     *  times and the current microstep is 1.
     *  The value of the event is the value of the input signal at the
     *  current time at microstep 0.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ContinuousDirector director = (ContinuousDirector)getDirector();
        Time currentTime = director.getModelTime();
        int microstep = director.getIndex();
        if (_debugging) {
            _debug("Current time is " + currentTime
                    + " with microstep " + microstep);
        }
        if (currentTime.compareTo(_nextSamplingTime) == 0 && microstep == 1) {
            int width = Math.min(input.getWidth(), output.getWidth());
            for (int i = 0; i < width; i++) {
                if (_pendingOutputs[i] != null) {
                    // There is a deferred output for this input channel.
                    output.send(i, _pendingOutputs[i]);
                    if (_debugging) {
                        _debug("Sending output value " + _pendingOutputs[i] + " on channel " + i);
                    }
                }
            }
        }
    }

    /** Set the next sampling time for each
     *  input as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        int width = Math.min(input.getWidth(), output.getWidth());
        if (_pendingOutputs == null || _pendingOutputs.length != width) {
            _pendingOutputs = new Token[width];
        }
        Time currentTime = getDirector().getModelTime();
        _nextSamplingTime = currentTime;
        for (int i = 0; i < width; i++) {
            _pendingOutputs[i] = null;
        }
    }

    /** If the current microstep is zero, sample the inputs and request
     *  a refiring at the current time. If it is one, then request a refiring
     *  at the next sample time.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        ContinuousDirector director = (ContinuousDirector)getDirector();
        Time currentTime = director.getModelTime();
        int microstep = director.getIndex();
        if (currentTime.compareTo(_nextSamplingTime) == 0) {
            if (microstep == 0) {
                int width = Math.min(input.getWidth(), output.getWidth());
                for (int i = 0; i < width; i++) {
                    if (input.hasToken(i)) {
                        _pendingOutputs[i] = input.get(i);
                    } else {
                        _pendingOutputs[i] = null;
                    }
                    if (_debugging) {
                        _debug("Read input: " + _pendingOutputs[i] + " on channel " + i);
                    }
                }
                director.fireAt(this, currentTime, 1);
            } else if (microstep == 1) {
                double samplePeriodValue = ((DoubleToken) samplePeriod.getToken()).doubleValue();
                _nextSamplingTime = currentTime.add(samplePeriodValue);
                director.fireAt(this, _nextSamplingTime, 0);
            }
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
    
    /** The next sampling time. */
    private Time _nextSamplingTime;
        
    /** Record of pending output tokens (those that have been
     *  delayed because they appeared at the input when the
     *  the microstep was zero).
     */
    private Token[] _pendingOutputs;
}
