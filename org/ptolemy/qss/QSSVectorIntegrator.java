/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package org.ptolemy.qss;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QSSIntegratorVector

/**
A quantized-state vector integrator.

@author David Broman, Edward A. Lee, Thierry Nouidui, Michael Wetter
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class QSSVectorIntegrator extends TypedAtomicActor {

    public QSSVectorIntegrator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        quantum = new Parameter(this, "quantum");
        quantum.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        initialState = new Parameter(this, "initialState");
        initialState.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public Parameter initialState, quantum;

    /** If it is time to produce a quantized output, produce it.
     *  Otherwise, indicate that the output is absent.
     *  @exception IllegalActionException If sending an output fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        if (currentTime.equals(t_min) || t_min == null) {
            for (int i = 0; i < index_min.size(); i++) {
                System.out.println("This is the current index: "
                        + String.valueOf(i) + LS + "This is the minimum time: "
                        + String.valueOf(t_min) + LS
                        + "This is the value to be sent: "
                        + String.valueOf(nextOutputValue[index_min.get(i)])
                        + LS + "This is the port where the value is sent: "
                        + q.get(index_min.get(i)).getName());
                q.get(index_min.get(i)).send(0,
                        new DoubleToken(nextOutputValue[index_min.get(i)]));
            }
        } else {
            // For the continuous director, assert that the output is absent.
            for (int i = 0; i < index_min.size(); i++) {
                q.get(index_min.get(i)).sendClear(0);
            }
        }
    }

    /** Initialize this actor to indicate that no input has yet been provided.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Get the input port lists
        _getInputPorts();

        // Get the output port lists
        _getOutputPorts();

        if (dx.size() != q.size()) {
            final String em = "Actor " + this.getFullName() + LS
                    + ": The number of input ports " + dx.size() + LS
                    + " should match the number of the output ports "
                    + q.size();
            throw new IllegalActionException(this, em);
        }

        // Get the length of the vector of quantum.
        int dqSize = ((ArrayToken) quantum.getToken()).length();
        // Get the length of the vector of initial state.
        int xIniSize = ((ArrayToken) initialState.getToken()).length();
        if (dx.size() != dqSize) {
            final String em = "Actor " + this.getFullName() + LS
                    + ": The number of input ports " + dx.size() + LS
                    + " should match the length of the quantum " + dqSize;
            throw new IllegalActionException(this, em);
        }
        if (dx.size() != xIniSize) {
            final String em = "Actor " + this.getFullName() + LS
                    + ": The number of input ports " + dx.size() + LS
                    + " should match the length of the quantum " + xIniSize;
            throw new IllegalActionException(this, em);
        }

        // Get the vector of quantum.
        dq = _getDoubleArray(quantum, dqSize);

        // Get the vector of initial values for states
        xIni = _getDoubleArray(initialState, xIniSize);

        // Initialize containers used for calculation.
        t_min = null;
        previousStateUpdateTime = null;
        u = new Token[dx.size()];
        x = new double[dx.size()];
        slope = new double[dx.size()];
        previousSlope = new double[dx.size()];
        nextOutputValue = new double[dx.size()];
        previousOutputValue = new double[dx.size()];
        previousInput = new Token[dx.size()];
        inputReceived = new boolean[dx.size()];
        _firstFiring = new boolean[dx.size()];
        nextOutputTime = new Time[dx.size()];
        index_min = new ArrayList<Integer>();

        for (int i = 0; i < dx.size(); i++) {
            nextOutputTime[i] = null;
            x[i] = xIni[i];
            nextOutputValue[i] = _quantize(x[i], dq[i]);
            previousOutputValue[i] = nextOutputValue[i];
            previousInput[i] = null;
            inputReceived[i] = false;
            _firstFiring[i] = true;
        }
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
    }

    /** Return false, indicating that this actor can fire even if its
     *  input is unknown.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Update the calculation of the next output time and request
     *  a refiring at that time.
     *  If there is a new input, read it and update the slope.
     *  @return True if the base class returns true.
     *  @exception IllegalActionException If reading inputs or parameters fails.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        for (int i = 0; i < dx.size(); i++) {
            // If an input is provided, we should override the nextOutputTime
            // and value calculated above, or if none was calculated above,
            // then modify the values set when the previous input arrived.
            if (dx.get(i).hasToken(0)) {
                u[i] = dx.get(i).get(0);
                if (!u[i].equals(previousInput[i])) {
                    // Initialize previousInput
                    if (_firstFiring[i]) {
                        previousInput[i] = u[i];
                        _firstFiring[i] = false;
                    }
                    // Save the previous slope
                    previousSlope[i] = ((DoubleToken) previousInput[i])
                            .doubleValue();
                    // Compute the new slope.
                    slope[i] = ((DoubleToken) u[i]).doubleValue();
                    // Save the previous input.
                    previousInput[i] = u[i];
                    // Set the inputReceived flag to true
                    inputReceived[i] = true;
                }
            }
            if (currentTime.equals(nextOutputTime[i])
                    || nextOutputTime[i] == null) {
                if (nextOutputTime[i] != null) {
                    x[i] = nextOutputValue[i];
                }
                previousOutputValue[i] = nextOutputValue[i];

                // Calculate the time of the next output, which is the time
                // it will take at the current slope to rise or fall by the quantum.
                // The following will make sure that output
                // does not get sent again at the current time if there is
                // another firing. Note that DE needs this because
                // of the way it handles feedback loops. It may invoke fire and
                // postfire more than once in each iteration.
                nextOutputTime[i] = _nextCrossingTime(slope[i], 0.0, 0.0,
                        dq[i], currentTime);
                // Calculate the next output value
                nextOutputValue[i] = _nextOutputValue(slope[i],
                        previousOutputValue[i], dq[i]);
            } else {
                // The fire method did not send an output.
                // If we did not receive a new input, there is nothing to do.
                // But if we did, then we have a new slope, so we need to recompute
                // the time it will take to get to the next quantum.
                if (inputReceived[i]) {
                    x[i] += previousSlope[i]
                            * (currentTime.subtract(previousStateUpdateTime))
                            .getDoubleValue();
                    System.out
                            .println("This is the index in received "
                                    + String.valueOf(i) + ": "
                                    + String.valueOf((x[i])));
                    // Update the time of the next output, which is the time it will take to
                    // get from the current state to previous output value plus or minus the quantum
                    // at the updated slope.
                    nextOutputTime[i] = _nextCrossingTime(previousSlope[i],
                            x[i], nextOutputValue[i], dq[i], currentTime);
                    inputReceived[i] = false;
                }
                // Calculate the next output value
                nextOutputValue[i] = _nextOutputValue(previousSlope[i],
                        previousOutputValue[i], dq[i]);
            }

        }
        // Record time at which state is updated.
        previousStateUpdateTime = currentTime;
        // Determine the time of the state which will fire the next event.
        _minNextCrossingTime();
        if (t_min != Time.POSITIVE_INFINITY) {
            getDirector().fireAt(this, t_min);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the next time at which a line with the given slope
     *  will rise or fall from the specified starting point to the
     *  specified reference plus or minus the specified quantum.
     *  If the specified slope is smaller than the value of
     *  {@link #_SMALL}, then return Time.POSITIVE_INFINITY.
     *  If the starting point has already hit or crossed
     *  the specified reference plus or minus the specified
     *  quantum, then return the current time.
     *  @param slope The derivative.
     *  @param start The starting point.
     *  @param reference The reference point.
     *  @param quantum The quantum.
     *  @param currentTime The current time.
     *  @return The next event time.
     */
    protected Time _nextCrossingTime(double slope, double start,
            double reference, double quantum, Time currentTime) {
        if (slope > _SMALL) {
            // Slope is positive.
            double threshold = reference + quantum;
            if (start >= threshold) {
                return currentTime;
            } else {
                return currentTime.add((threshold - start) / slope);
            }
        } else if (slope < -_SMALL) {
            // Slope is negative.
            double threshold = reference - quantum;
            if (start <= threshold) {
                return currentTime;
            } else {
                return currentTime.add((threshold - start) / slope);
            }
        } else {
            // Slope is small.
            return Time.POSITIVE_INFINITY;
        }
    }

    /** Return the next output value, which is the reference plus the quantum
     *  if the slope is positive, and the reference minus the quantum otherwise.
     *  @param slope The slope.
     *  @param reference The reference.
     *  @param quantum The quantum.
     *  @return The reference plus or minus the quantum.
     */
    protected double _nextOutputValue(double slope, double reference,
            double quantum) {
        if (slope > 0.0) {
            return reference + quantum;
        } else {
            return reference - quantum;
        }
    }

    /** Return the argument quantized to a multiple of quantum given by
     *  the {@link #quantum} parameter.
     *  @param x The value to quantize.
     *  @param dq The quantum.
     *  @return A quantized value.
     *  @exception IllegalActionException If the quantum parameter cannot
     *   be evaluated.
     */
    protected double _quantize(double x, double dq)
            throws IllegalActionException {
        return (Math.floor(x / dq)) * dq;
    }

    /** Compute from the vector of nextOutputTime
     * the minimal time to produce the next output.
     * Save in a list indexes which need to produce outputs at this time.
     */
    protected void _minNextCrossingTime() {
        index_min.clear();
        t_min = nextOutputTime[0];
        index_min.add(0);
        t_min = nextOutputTime[0];
        for (int i = 1; i < nextOutputTime.length; i++) {
            if (t_min.compareTo(nextOutputTime[i]) > 0) {
                t_min = nextOutputTime[i];
                index_min.clear();
                index_min.add(i);
            } else if (t_min.compareTo(nextOutputTime[i]) == 0) {
                index_min.add(i);
            }
        }
    }

    /** Retrieve a list of input ports.
     *  @exception IllegalActionException if the base class throws it
     */
    private void _getInputPorts() throws IllegalActionException {
        dx = this.inputPortList();
    }

    /** Retrieve a list of output ports.
     *  @exception IllegalActionException if the base class throws it
     */
    private void _getOutputPorts() throws IllegalActionException {
        q = this.outputPortList();
    }

    /** Get a double array from the Parameter.
     * @param t the parameter which must be a type that can be converted to an ArrayToken
     * @param n the size of the parameter
     * @return the double[] array with the elements of the Token
     * @exception IllegalActionException if the base class throws it.
     */
    protected double[] _getDoubleArray(Parameter t, int n)
            throws IllegalActionException {
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = ((DoubleToken) (((ArrayToken) t.getToken()).getElement(i)))
                    .doubleValue();
            if (Double.isNaN(ret[i])) {
                final String em = "Actor " + this.getFullName() + ": " + LS
                        + "Token number " + i + " is NaN at time "
                        + getDirector().getModelTime().getDoubleValue();
                throw new IllegalActionException(this, em);
            }
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // FIXME: Is "SMALL" adequate to check whether slope is zero?
    /** A small number, below which the slope is considered to be zero. */
    protected static final double _SMALL = 10E-9;
    /** System dependent line separator. */
    protected final static String LS = System.getProperty("line.separator");
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Inputs (derivative). */
    private List<TypedIOPort> dx;

    /** Outputs (states). */
    private List<TypedIOPort> q;

    /** Output(the quantized state). */
    // private Hashtable<Integer, TypedIOPort> q;

    /** Index of ports which produce outputs at the same time .*/
    private List<Integer> index_min;

    /** The time at which to produce the next output. */
    private Time nextOutputTime[];

    /** The time at which the state was last updated. */
    private Time previousStateUpdateTime;

    /** The next output produced. */
    private double nextOutputValue[];

    /** The last output produced. */
    private double previousOutputValue[];

    /** The slope at the time of the most recent input. */
    private double slope[];

    /** The slope at the time of the last recent input. */
    private double previousSlope[];

    /** The current state. */
    private double x[];

    /** The initial states. */
    private double xIni[];

    /** The quantum. */
    private double dq[];

    /** The most recently seen input. */
    private Token previousInput[];

    /** The current input. */
    private Token u[];

    /** The time for next output. */
    private Time t_min;

    /** The flag to indicate input received. */
    boolean inputReceived[];

    /** The flag to indicate first firing. */
    boolean _firstFiring[];

}
