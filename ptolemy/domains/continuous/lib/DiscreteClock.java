/* Generate discrete events at prespecified time instants.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DiscreteClock

/**
 This actor produces a periodic signal, a sequence of discrete events at
 regularly spaced intervals. It can generate a finite sequence by specifying
 a finite <i>numberOfCycles</i>. The numberOfCycles has a default value
 of -1, indicating no bound on the number of events. If numberOfCycles is
 a positive number, once the specified number of cycles has been completed,
 then this actor returns false from the postfire method.
 <p>
 At the beginning of each time interval of length given by <i>period</i>,
 this actor initiates a sequence of output events with values given by
 <i>values</i> and offset into the period given by <i>offsets</i>.
 These parameters contain arrays, which are required to have the same length.
 The <i>offsets</i> array contains doubles, which
 must be nondecreasing and nonnegative,
 or an exception will be thrown when it is set.
 It can have repeated entries, in which case more than one event will
 be produced at the same time.
 Moreover, its largest entry must be smaller than <i>period</i>
 or an exception will be thrown.
 <p>
 The <i>values</i> parameter by default
 contains the array of {1, 0}.  The default
 <i>offsets</i> array is {0.0, 1.0}.  Thus, the default output will be
 alternating 1 and 0, uniformly spaced one time unit apart.  The default period
 is 2.0.
 <p>
 The actor uses the fireAt() method of the director to request
 firing at the beginning of each period and at each of the offsets.
 <p>
 The type of the output can be any token type. This type is inferred
 from the element type of the <i>values</i> parameter.
 <p>
 There is another kind of clock called ContinuousClock, which produces
 a square wave instead of a sequence of events. The same effect can be
 achieved by using this DiscreteClock actor with a ZeroOrderHold.

 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class DiscreteClock extends TypedAtomicActor {
    
    // This actor only generates predictable events and that is why it does not
    // implement the ContinuousStepSizeControlActor interface. This actor requests a
    // refiring at its initialize method to produce events. During its postfire 
    // method, it requests further firings to produce more events if necessary.
    // FIXME: However, it probably should. What if it's in a modal model
    // and its mode gets activated at a time close to when it should produce
    // an output?
    
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public DiscreteClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create port and parameters.
        output = new TypedIOPort(this, "output", false, true);

        period = new Parameter(this, "period");
        period.setExpression("2.0");
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // set the values parameter
        values = new Parameter(this, "values");
        values.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        values.setExpression("{1, 0}");

        // set type constraint
        ArrayType valuesArrayType = (ArrayType) values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);

        // Set the numberOfCycles parameter.
        numberOfCycles = new Parameter(this, "numberOfCycles");
        numberOfCycles.setTypeEquals(BaseType.INT);
        numberOfCycles.setExpression("-1");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                + "style=\"fill:white\"/>\n"
                + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\"/>\n"
                + "<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\"/>\n"
                + "<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\"/>\n"
                + "<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\"/>\n"
                + "<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\"/>\n"
                + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of cycles to produce, or -1 to specify no limit.
     *  This is an integer with default -1.
     */
    public Parameter numberOfCycles;

    /** The output port.  The type of this port is that of
     *  the elements of the <i>values</i> parameter.
     */
    public TypedIOPort output = null;

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0, 1.0}.
     */
    public Parameter offsets;

    /** The period of the output events.
     *  This parameter must contain a DoubleToken, and defaults to 2.0.
     */
    public Parameter period;

    /** The values that will be produced at the specified offsets.
     *  This parameter must contain an ArrayToken, and defaults to {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>offsets</i> parameter, check that the
     *  array is nondecreasing and has the right dimension; if the
     *  argument is <i>period</i>, check that it is positive. Other
     *  sanity checks with <i>period</i> and <i>values</i> are done in
     *  the fire() method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the offsets array is not
     *   nondecreasing and nonnegative, or it is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == offsets) {
            ArrayToken offsetsValue = (ArrayToken) offsets.getToken();
            _offsets = new double[offsetsValue.length()];

            double previous = 0.0;

            for (int i = 0; i < offsetsValue.length(); i++) {
                _offsets[i] = ((DoubleToken) offsetsValue.getElement(i))
                        .doubleValue();

                // Check nondecreasing property.
                if (_offsets[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of offsets is not nondecreasing "
                                    + "and nonnegative.");
                }

                previous = _offsets[i];
            }
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  "
                                + "Period given: " + periodValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DiscreteClock newObject = (DiscreteClock) super.clone(workspace);
        ArrayType valuesArrayType = (ArrayType) newObject.values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);

        return newObject;
    }

    /** Send to the output the value corresponding to the current
     *  phase of the clock. This relies on the fact that prefire()
     *  will return false if the current time is not a time at which
     *  we should emit an event.
     *  @exception IllegalActionException If the event cannot be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, ((ArrayToken) values.getToken()).getElement(_phase));
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it.
     */
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();

        FixedPointDirector director = (FixedPointDirector) getDirector();

        _cycleStartTime = director.getModelTime();
        _cycleCount = 0;
        _nextOutputIndex = 1;
        _phase = 0;

        // Schedule the first firing.
        _nextOutputTime = _cycleStartTime.add(_offsets[0]);
        director.fireAt(this, _nextOutputTime);
    }

    /** Update the state of the actor and schedule the next firing,
     *  unless the specified <i>numberOfCycles</i> has been reached.
     *  @return False if the specified number of cycles has been reached,
     *   and true otherwise.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing, or if an offset value exceeds the period.
     */
    public boolean postfire() throws IllegalActionException {
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        // Increment to the next phase.
        _phase++;

        if (_phase >= _offsets.length) {
            _phase = 0;
            _cycleCount++;
            // If we are beyond the number of cycles requested, then
            // return false.
            int cycleLimit = ((IntToken) numberOfCycles.getToken()).intValue();
            if (cycleLimit > 0 && _cycleCount >= cycleLimit) {
                return false;
            }
            _cycleStartTime = _cycleStartTime.add(periodValue);
        }

        if (_offsets[_phase] >= periodValue) {
            throw new IllegalActionException(this, "Offset number " + _phase
                    + " with value " + _offsets[_phase]
                    + " must be less than the " + "period, which is "
                    + periodValue);
        }

        Time nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        FixedPointDirector director = (FixedPointDirector) getDirector();
        director.fireAt(this, nextOutputTime);
        if (_nextOutputTime.equals(nextOutputTime)) {
            // Duplicate offsets allow production of multiple events
            // at one time.
            _nextOutputIndex++;
        } else {
            _nextOutputTime = nextOutputTime;
            _nextOutputIndex = 1;
        }
        return true;
    }

    /** Return true if this actor can produce an event at the current time.
     *  @return True if current time and index matches a specified
     *   period and offset.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean prefire() throws IllegalActionException {
        FixedPointDirector director = (FixedPointDirector) getDirector();
        boolean rightIndex = _nextOutputIndex == director.getIndex();
        int rightTime = director.getModelTime().compareTo(_nextOutputTime);
        // Check whether model time has passed us by, perhaps because we are
        // in a modal model and are in a refinement that has not
        // been active.
        while (rightTime > 0.0 && !_stopRequested) {
            // If model time has passed us by, then increment
            // our state until either we match model time or
            // we exceed it.
            // Increment to the next phase.
            _phase++;
            if (_phase >= _offsets.length) {
                _phase = 0;
                // Do not increment the cycle count, since we did
                // not actually complete these cycles.
                double periodValue = ((DoubleToken) period.getToken()).doubleValue();
                _cycleStartTime = _cycleStartTime.add(periodValue);
            }
            _nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
            _nextOutputIndex = 1;
            rightIndex = _nextOutputIndex == director.getIndex();
            rightTime = director.getModelTime().compareTo(_nextOutputTime);
            
            // If at this point we are caught up but can't fire now,
            // then request a refiring.
            if (rightTime < 0.0 || (rightTime == 0.0 && !rightIndex)) {
                director.fireAt(this, _nextOutputTime);
                // The return statement below will return false.
            }
        }
        return super.prefire() &&  rightIndex && (rightTime == 0.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.
    
    /** The count of iterations. */
    private transient int _cycleCount;
    
    /** The most recent cycle start time. */
    private transient Time _cycleStartTime;

    /** Cache of offsets array value. */
    private transient double[] _offsets;

    /** The phase of the next output. */
    private transient int _phase;

    /** The index of when the output should be emitted. */
    private transient int _nextOutputIndex;

    /** The next time point when the output should be emitted. */
    private transient Time _nextOutputTime;
}
