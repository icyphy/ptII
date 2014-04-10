package org.ptolemy.qss;

import java.util.ArrayList;
import java.util.List;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.ArrayToken;
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
@version $Id: When.java 68298 2014-02-05 17:00:30Z eal $
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
     *  @throws IllegalActionException If sending an output fails.
     */
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
        // Create the output ports.
        //_createOutputPorts();

        // Get the vector of quantum.
        dq = _getDoubleArray(quantum, dqSize);

        // Get the vector of initial values for states
        xIni = _getDoubleArray(initialState, xIniSize);

        // Initialize containers used for calculation.
        t_min = null;
        u = new Token[dx.size()];
        x = new double[dx.size()];
        slope = new double[dx.size()];
        nextOutputValue = new double[dx.size()];
        previousOutputValue = new double[dx.size()];
        previousInput = new Token[dx.size()];
        inputReceived = new boolean[dx.size()];
        nextOutputTime = new Time[dx.size()];
        previousStateUpdateTime = new Time[dx.size()];
        index_min = new ArrayList<Integer>();

        for (int i = 0; i < dx.size(); i++) {
            nextOutputTime[i] = previousStateUpdateTime[i] = null;
            x[i] = xIni[i];
            nextOutputValue[i] = _quantize(x[i], dq[i]);
            previousOutputValue = nextOutputValue;
            previousInput[i] = null;
            inputReceived[i] = false;
        }
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
    }

    /** Return false, indicating that this actor can fire even if its
     *  input is unknown.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Update the calculation of the next output time and request
     *  a refiring at that time.
     *  If there is a new input, read it and update the slope.
     *  @return True if the base class returns true.
     *  @throws IllegalActionException If reading inputs or parameters fails.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        for (int i = 0; i < dx.size(); i++) {
            // If an input is provided, we should override the nextOutputTime
            // and value calculated above, or if none was calculated above,
            // then modify the values set when the previous input arrived.
            if (dx.get(i).hasToken(0)) {
                u[i] = dx.get(i).get(0);
                if (!u[i].equals(previousInput[i])) {
                    // We have a new input, different from the previous input.
                    previousInput[i] = u[i];
                    // Update the slope.
                    slope[i] = ((DoubleToken) u[i]).doubleValue();
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

            } else {
                // The fire method did not send an output.
                // If we did not receive a new input, there is nothing to do.
                // But if we did, then we have a new slope, so we need to recompute
                // the time it will take to get to the next quantum.
                if (inputReceived[i]) {
                    x[i] += slope[i]
                            * (currentTime.subtract(previousStateUpdateTime[i]))
                                    .getDoubleValue();
                    System.out.println("This is the index in received " + String.valueOf(i) + ": " + String.valueOf((x[i])));
                    // Update the time of the next output, which is the time it will take to
                    // get from the current state to previous output value plus or minus the quantum
                    // at the updated slope.
                    nextOutputTime[i] = _nextCrossingTime(slope[i], x[i],
                            nextOutputValue[i], dq[i], currentTime);
                    inputReceived[i] = false;
                }
            }
            nextOutputValue[i] = _nextOutputValue(slope[i],
                    previousOutputValue[i], dq[i]);
            // Save the time when the state has been updated.
            previousStateUpdateTime[i] = currentTime;
        }
        // Determine the time of the state which will fire the next event.
        _minNextCrossingTime();
        if (t_min != Time.POSITIVE_INFINITY) {
            getDirector().fireAt(this, t_min);
            for (int j = 0; j < dx.size(); j++) {
                for (int i = 0; i < index_min.size(); i++) {
                    if (j == index_min.get(i)) {
                        // Save the time of the states updated.
                        previousStateUpdateTime[j] = t_min;
                    }
                }
            }
        }
        return super.postfire();
    }

    /////////////////////////////////////////////////////////////////////
    ////                    protected methods                        ////

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
     *  @throws IllegalActionException If the quantum parameter cannot
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
        for (int i = 0; i < nextOutputTime.length; i++) {
            if (t_min.compareTo(nextOutputTime[i]) > 0) {
                t_min = nextOutputTime[i];
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

    /*    *//** Create a Hashtable which has indexes as keys and output ports as 
             *  values by checking whether the output port already first 
             *  and if not making an output port whose name is derived from a 
             *  corresponding input port.
             *  The name of the output port is the name of the corresponding input port + "_q".
             *  @exception IllegalActionException if the base class throws it.
             *  @throws NameDuplicationException if the base class throws it.
             */
    /*
    private void _createOutputPorts() throws IllegalActionException {
     q = new Hashtable<Integer, TypedIOPort>();
     for (int i = 0; i < dx.size(); i++) {
         TypedIOPort port;
         try {
             port = (TypedIOPort) _getOutputPortByNameOrDisplayName(dx
                     .get(i).getName() + "_q");
             if (port == null) {
                 port = new TypedIOPort(this, dx.get(i).getName() + "_q",
                         false, true);
                 port.setTypeEquals(BaseType.DOUBLE);
             }
             q.put(i, port);
         } catch (NameDuplicationException e) {
         }
     }
    }*/

    /*    *//** Get the port by display name or, if the display name
             *  is not set, then by name.  This is used to handle
             *  variable names that have periods (".") in them.
             *  @param portName The name of the port to find.  The name
             *  might have a period in it, for example "foo.bar".
             *  @return The port or null;
             */
    /*
    private Port _getOutputPortByNameOrDisplayName(String portName) {
     // RecordAssembler and RecordDisassembler use a similar design.
     Port returnValue = null;
     Iterator ports = this.outputPortList().iterator();
     while (ports.hasNext()) {
         Port port = (Port) ports.next();
         if (port.getDisplayName().equals(portName)
                 || port.getName().equals(portName)) {
             return port;
         }
     }
     return returnValue;
    }*/

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

    /////////////////////////////////////////////////////////////////////
    ////                  protected variables                        ////

    // FIXME: Is "SMALL" adequate to check whether slope is zero?
    /** A small number, below which the slope is considered to be zero. */
    protected static final double _SMALL = 10E-9;
    /** System dependent line separator. */
    protected final static String LS = System.getProperty("line.separator");
    /////////////////////////////////////////////////////////////////////
    ////                    private variables                        ////

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
    private Time previousStateUpdateTime[];

    /** The next output produced. */
    private double nextOutputValue[];

    /** The last output produced. */
    private double previousOutputValue[];

    /** The slope at the time of the most recent input. */
    private double slope[];

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

    boolean inputReceived[];

}
