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

import ptolemy.actor.Director;
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
 This sampler will send to the output the first non-absent input event that occurs
 at a sample time. It produces it one microstep later than that input event.
 This ensures that the output at microstep 0 is always absent, thus
 ensuring continuity from the left. That is, the input is absent prior
 to the sample time, so continuity requires that it be absent at
 microstep 0 at the sample time. Moreover, this ensures that the
 sampler outputs the initial value of the input at the sample time.
 <p>
 This actor has multiport inputs and outputs. Signals in
 each input channel are sampled and produced to corresponding output
 channel. When there are multiple inputs, the first non-absent input
 from each channel is read, and the output is produced at the first
 microstep after the last of the inputs became non-absent.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
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
     *  times. The value of the event is the value of the input signal at the
     *  current time.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_inputIsComplete) {
            for (int i = 0; i < _recordedInputs.length; i++) {
                if (_debugging) {
                    _debug("Sending output value "
                            + _recordedInputs[i]
                            + " on channel "
                            + i);
                }
                if (_recordedInputs[i] != null) {
                    output.send(i, _recordedInputs[i]);
                } else {
                    output.send(i, null);
                }
            }
        }
    }

    /** Set the next sampling time as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextSamplingTime = getDirector().getModelTime();
        _inputIsComplete = false;
    }

    /** Set the next sampling time and return true.
     *  @return True.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        Director director = getDirector();
        if (director.getModelTime().compareTo(_nextSamplingTime) == 0) {
            // Read and record the input.
            int width = Math.min(input.getWidth(), output.getWidth());
            if (_recordedInputs == null || _recordedInputs.length != width) {
                _recordedInputs = new Token[width];
            }
            // Set a flag so the next fire() will produce the output.
            _inputIsComplete = true;
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    _recordedInputs[i] = input.get(i);
                    if (_debugging) {
                        _debug("Read input value " 
                                + _recordedInputs[i] 
                                + " at time "
                                + director.getModelTime());
                    }
                } else {
                    // Erase any previous value that may have been recorded.
                    _recordedInputs[i] = null;
                }
            }
            // To ensure we read only the initial value of the input signal,
            // increment the next sampling time now.
            double samplePeriodValue = ((DoubleToken) samplePeriod.getToken())
                    .doubleValue();
            _nextSamplingTime = _nextSamplingTime.add(samplePeriodValue);

            if (_debugging) {
                _debug("Request refiring at current time and at " + _nextSamplingTime);
            }
            _fireAt(director.getModelTime());
            _fireAt(_nextSamplingTime);
        } else {
            _inputIsComplete = false;
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

    /** Flag indicating that the record of inputs is complete. */
    private boolean _inputIsComplete;
    
    /** The next sampling time. */
    private Time _nextSamplingTime;
    
    /** The recorded input data. */
    private Token[] _recordedInputs;
}
