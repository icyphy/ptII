/* Base class for integrators in the CT domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (yuhong@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.FunctionDependency;
import ptolemy.actor.FunctionDependencyOfAtomicActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TimedActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTBaseIntegrator
/**
Base class for integrators in the continuous time (CT) domain.
An integrator has one input port and one output port. Conceptually,
the input is the derivative of the output w.r.t. time. So an ordinary
differential equation dx/dt = f(x, t) can be built by:
<P>
<pre>
<pre>               +---------------+
<pre>        dx/dt  |               |   x
<pre>    +--------->|   Integrator  |---------+----->
<pre>    |          |               |         |
<pre>    |          +---------------+         |
<pre>    |                                    |
<pre>    |             |---------|            |
<pre>    +-------------| f(x, t) |<-----------+
<pre>                  |---------|
</pre></pre></pre></pre></pre></pre></pre></pre></pre></pre>

<P>
An integrator
is a dynamic actor that can emit a token (the state) without knowing the
input. An integrator is a step size control actor that can control
the accuracy of the ODE solution by adjusting step sizes.
An integrator has memory, which is its state.
<P>
To help solving the ODE, a set of variables are used:<BR>
<I>state</I>: This is the value of the state variable at a time point,
which has beed confirmed by all the step size control actors.
<I>tentative state</I>: This is the value of the state variable
which has not been confirmed. It is a starting point for other actors
to estimate the accuracy of this integration step.
<I>history</I>: The previous states and their derivatives. History may
be used by multistep methods.
<P>
For different ODE solving methods, the functionality
of an integrator may be different. The delegation and strategy design
patterns are used in this class, ODESolver class, and the concrete
ODE solver classes. Some solver-dependent methods of integrators are
delegated to the ODE solvers.
<P>
An integrator has one parameter: the <i>initialState</i>. At the
initialization stage of the simulation, the state of the integrator is
set to the initial state. Changes of the <i>initialState</i> parameter
are ignored after the execution starts, unless the initialize() method
is called again. The default value of the parameter is 0.0 of type
DoubleToken.
<P>
An integrator can possibly have several auxiliary variables for the
the ODE solvers to use. The number of the auxiliary variables is checked
before each iteration. The ODE solver class provides the number of
variables needed for that particular solver.
The auxiliary variables can be set and get by setAuxVariables()
and getAuxVariables() methods.

@author Jie Liu
@version $Id$
@since Ptolemy II 0.2
@see ODESolver
@see CTDirector
*/
public class CTBaseIntegrator extends TypedAtomicActor
    implements TimedActor, CTStepSizeControlActor,
               CTDynamicActor, CTStatefulActor {

    /** Construct an integrator, with a name  and a container.
     *  The integrator is in the same workspace as the container.
     *
     * @param container The container.
     * @param name The name
     * @exception NameDuplicationException If the name is used by another
     *            actor in the container.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public CTBaseIntegrator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.DOUBLE);
        initialState = new Parameter(this, "initialState",
                new DoubleToken(0.0));
        initialState.setTypeEquals(BaseType.DOUBLE);
        _history = new History(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. This is a single port of type double.
     */
    public TypedIOPort input;

    /** The output port. This is a single port of type double.
     */
    public TypedIOPort output;

    /** The initial state of type DoubleToken. The default value is 0.0
     */
    public Parameter initialState;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the history information.
     */
    public void clearHistory() {
        _history.clear();
    }

    /** Emit the tentative output, which is the tentative state of the
     *  integrator.
     *  @exception IllegalActionException If the data transfer is not
     *  completed.
     */
    public void emitTentativeOutputs() throws IllegalActionException {
        output.send(0, new DoubleToken(_tentativeState));
    }

    /** Delegate to the integratorFire() of the
     *  current ODE solver. The existence of a director and an ODE solver
     *  is not checked here. If there's no director and ODE solver, a
     *  NullPointerException will be thrown.
     *
     *  @exception IllegalActionException If thrown by integratorFire()
     *       of the solver.
     */
    public void fire() throws  IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        ODESolver solver = (ODESolver)dir.getCurrentODESolver();
        if (_debugging) _debug(getName() +
                "fire using solver: ", solver.getName());
        solver.integratorFire(this);
    }

    /** Return the auxiliary variables in a double array.
     *  The auxiliary variables are created
     *  in the prefire() method and may be set during each firing
     *  of the actor. Return null if the
     *  auxiliary variables have never been created.
     *
     *  @return The auxiliary variables in a double array.
     */
    public double[] getAuxVariables() {
        return _auxVariables;
    }

    /** Return the latest updated derivative of the state variable.
     *  @return The derivative of the most recently resolved state.
     */
    public final double getDerivative() {
        return _derivative;
    }

    /** Return the history information of the last index-th step.
     *  The index starts from 0. If the current time is t(n),
     *  then getHistory(0) gives the
     *  state and derivative of time t(n-1), and getHistory(1)
     *  corresponds to time t(n-2).
     *  The returned history array has length 2, where the first
     *  element is the history state, and the second element is
     *  the corresponding derivative.
     *  The history information is equidistant in time, and the
     *  distance is the current step size.
     *  If the step sizes are changed during the execution,
     *  the history information will be self-adjusted.
     *  If the index is less than 0 or grater than
     *  the history capacity, a runtime IndexOutOfBoundsException
     *  will be thrown.
     *
     *  @param index The index.
     *  @return The history array at the index point.
     */
    public double[] getHistory(int index) {
        return _history.getEntry(index);
    }

    /** Return the history capacity.
     *  @return The maximum capacity of the history information.
     */
    public final int getHistoryCapacity() {
        return _history.getCapacity();
    }

    /** Return the state of the integrator. The returned state is the
     *  latest confirmed state. This is the same value as getHistory(0)[0],
     *  but more efficient.
     *
     *  @return A double number as the state of the integrator.
     */
    public final double getState() {
        return _state;
    }

    /** Return the tentative state.
     *  @return the tentative state.
     */
    public double getTentativeState() {
        return _tentativeState;
    }

    /** Return the number of valid history entries. This number is
     *  always less than or equal to the history capacity.
     *  @return The number of valid history entries.
     */
    public final int getValidHistoryCount() {
        return _history.getValidEntryCount();
    }

    /** Go to the marked state. After calling the markState() method,
     *  calling this method will bring the integrator back to the
     *  marked state. This method is used
     *  for rollbacking the execution to a previous time point.
     */
    public void goToMarkedState() {
        _state = _storedState;
        setTentativeState(_storedState);
    }

    /** Initialize the integrator. Check for director and ODE
     *  solver.
     *  Update initial state parameter. Set the initial state to
     *  the tentative state and the state. Set tentative
     *  derivative to 0.0.
     *
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the
     *       integratorInitialize() of the solver.
     */
    public void initialize() throws IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " no director available");
        }
        ODESolver solver = (ODESolver)dir.getCurrentODESolver();
        if (solver == null) {
            throw new IllegalActionException( this,
                    " no ODE solver available");
        }
        super.initialize();
        _tentativeState = ((DoubleToken)initialState.getToken()).doubleValue();
        _tentativeDerivative = 0.0;
        _state = _tentativeState;
        _derivative = _tentativeDerivative;
        if (_debugging) _debug(getName(), " initialize: initial state = "
                + _tentativeState);
        _history.clear();
    }

    /** Return true if this integration step is accurate from this
     *  integrator's point of view.
     *  This method delegates to the integratorIsAccurate() method of
     *  the current ODE solver.
     *  Throw a NullPointerException, if there's no ODE solver.
     *  If the input is not available, or the input is a result of
     *  divide by zero, an InternalErrorException is thrown.
     *  @return True if the last integration step is accurate.
     */
    public boolean isThisStepAccurate() {
        try {
            // We check the validity of the input
            // If it is NaN, or Infinity, an exception is thrown.
            double f_dot = ((DoubleToken)input.get(0)).doubleValue();
            if (Double.isNaN(f_dot) || Double.isInfinite(f_dot)) {
                throw new InternalErrorException("The input of " +
                    getName() + " is not valid because" +
                    " it is a result of divide-by-zero.");
            }
        } catch (IllegalActionException e) {        
            throw new InternalErrorException(getName() +
                    " can't read input." + e.getMessage());
        }
        ODESolver solver = ((CTDirector)getDirector()).getCurrentODESolver();
        _successful = solver.integratorIsAccurate(this);
        return _successful;
    }

    /** Mark and remember the current state. This remembered state can be
     *  retrieved by the goToMarkedState() method. The marked state
     *  may be used for rolling back the execution to a previous time point.
     */
    public void markState() {
        _storedState = getState();
    }

    /** Update the state and its derivative, and push them
     *  into history.
     *  @return True always.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        _state = _tentativeState;
        _derivative = _tentativeDerivative;
        if (_debugging) _debug(getName(), " state: " + _state +
                " derivative: " + _derivative);
        if (getHistoryCapacity() > 0) {
            _history.pushEntry(_tentativeState, _tentativeDerivative);
        }
        return true;
    }

    /** Return the predicted next step size. This method delegates to
     *  the integratorPredictedStepSize() method of the current ODESolver.
     *  Throw a NullPointerException, if there's no ODE solver.
     *  @return The predicteded next step size.
     */
    public double predictedStepSize() {
        ODESolver solver = ((CTDirector)getDirector()).getCurrentODESolver();
        return solver.integratorPredictedStepSize(this);
    }

    /** Setup the integrator to operate with the current ODE solver.
     *  This method checks whether
     *  there are enough auxiliary variables in the integrator for the
     *  current ODE solver. If not, create more auxiliary variables.
     *  This method also adjusts the history information w.r.t. the
     *  current ODE solver and the current step size.
     *  The existence of director and ODE solver is not checked,
     *  since they are checked in the initialize() method.
     *  @return True always.
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver.
     */
    public boolean prefire() throws IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        if (dir == null) {
            throw new IllegalActionException( this,
                    " does not have a director.");
        }
        ODESolver solver = (ODESolver)dir.getCurrentODESolver();
        if (solver == null) {
            throw new IllegalActionException( this,
                    " does not have an ODE solver.");
        }
        int n = solver.getIntegratorAuxVariableCount();
        if ((_auxVariables == null) || (_auxVariables.length < n)) {
            _auxVariables = new double[n];
        }
        if (getHistoryCapacity() != solver.getHistoryCapacityRequirement()) {
            setHistoryCapacity(solver.getHistoryCapacityRequirement());
        }
        if (getValidHistoryCount() >= 2) {
            _history.rebalance(dir.getCurrentStepSize());
        }
        return true;
    }

    /** Return the estimation of the refined next step size.
     *  If this integrator considers the current step to be accurate,
     *  then return the current step size, otherwise return half of the
     *  current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        double step = ((CTDirector)getDirector()).getCurrentStepSize();
        if (_successful) {
            return step;
        }else {
            return (double)0.5*step;
        }
    }

    /** Set the value of an auxiliary variable.  If the index is out of
     *  the bound of the auxiliary variable
     *  array, an InvalidStateException is thrown to indicate an
     *  inconsistency in the ODE solver.
     *
     *  @param index The index in the auxVariables array.
     *  @param value The value to be set.
     *  @exception InvalidStateException If the index is out of the range
     *       of the auxiliary variable array.
     */
    public void setAuxVariables(int index, double value)
            throws InvalidStateException {
        try {
            _auxVariables[index] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidStateException(this,
                    "index out of the range of the auxVariables.");
        }
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(input, output);
    }

    /** Set history capacity. This will typically be set by the ODE solvers
     *  that uses the history. If the argument is less than 0,
     *  the capacity is set to 0.
     *  @param cap The capacity.
     */
    public final void setHistoryCapacity(int cap) {
        _history.setCapacity(cap);
    }

    /** Set the tentative derivative, dx/dt. Tentative derivative
     *  is the derivative of the state that
     *  the ODE solver resolved in one step. This may not
     *  be the final derivative due to error control or event detection.
     *  @param value The value to be set.
     */
    public final void setTentativeDerivative(double value) {
        _tentativeDerivative = value;
    }

    /** Set the tentative state. Tentative state is the state that
     *  the ODE solver resolved in one step.
     *  It may not
     *  be the final state due to error control or event detection.
     *  @param value The value to be set.
     */
    public final void setTentativeState(double value) {
        _tentativeState = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The history states and their derivative.
     *  This variable is needed by Linear Multistep (LMS) methods,
     *  like Trapezoidal rule and backward differential formula.
     *  This variable is protected so that derived classes may
     *  access it directly.
     */
    protected History _history;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicate whether the latest step is successful from this
    // integrator's point of view.
    private boolean _successful = false;

    // Auxiliary variable array.
    private double[] _auxVariables;

    // State.
    private double _state;

    // Derivative.
    private double _derivative;

    // Tentative state;
    private double _tentativeState;

    // Tentative derivative;
    private double _tentativeDerivative;

    // The state stored, may be used for rollback execution.
    private double _storedState;

    ///////////////////////////////////////////////////////////////////
    ////                           Inner Class                     ////

    /** The history information, state and derivatives, at equidistance
     *  past time points. This inner class is protect so that it can be
     *  tested.
     */
    protected class History {

        /** Construct the history.
         */
        public History(CTBaseIntegrator container) {
            _container = container;
            _entries = new LinkedList();
            _capacity = 0;
            _stepsize = 0.0;
        }

        ///////////////////////////////////////////////////////////////
        ////                         public methods                ////

        /** Remove all history information.
         */
        public void clear() {
            _entries.clear();
        }

        /** Return the maximum capacity.
         *  @return The capacity.
         */
        public int getCapacity() {
            return _capacity;
        }

        /** Return the number of valid entries.
         *  @return The number of valid history entries.
         */
        public int getValidEntryCount() {
            return _entries.size();
        }

        /** Return the index-th entry in the history.
         *  The index starts from 0. If the current time index is n,
         *  then calling this method with argument 0 will return
         *  the history of time index n-1.
         *  @param index The index of the entry.
         *  @return The double matrix storing the index-th state and
         *         its derivative in history.
         */
        public double[] getEntry(int index) {
            return ((DoubleDouble)_entries.get(index)).toArray();
        }

        /** Push the new state-derivative pair into the history.
         *  If the number of entries exceeds the capacity
         *  after the pushing , then the oldest entry will be lost.
         *  @param state The state.
         *  @param derivative THe derivative of the state.
         *  @exception IllegalActionException If the capacity
         *  of history is less than or equal to zero.
         */
        public void pushEntry(double state, double derivative)
                throws IllegalActionException {
            if (_capacity >0) {
                DoubleDouble entry = new DoubleDouble(state, derivative);
                if (_entries.size() >= _capacity) {
                    // the history list has achieved its capacity,
                    // so remove the oldest entry.
                    _entries.removeLast();
                }
                _entries.addFirst(entry);
                _stepsize =
                    ((CTDirector)_container.getDirector()).getCurrentStepSize();
            } else {
                throw new IllegalActionException(getContainer(),
                        "The history capacity is less than or equal to 0.");
            }
        }


        /** Rebalance the history information
         *  with respect to the current step size, such that the information
         *  in the history list are equally distanced, and the
         *  distance is the current step size.
         *  If the current step size is less than the history step size
         *  used in the history list, then a 4-th order Hermite
         *  interpolation is used for every two consecutive points.
         *  If the current step size is larger than the history step size,
         *  then a linear extrapolation is used.
         *  @param currentStepSize The current step size.
         */
        public void rebalance(double currentStepSize) {
            double timeResolution =
                ((CTDirector) _container.getDirector()).getTimeResolution();
            if (Math.abs(currentStepSize - _stepsize)>timeResolution) {
                double[][] history = toDoubleArray();
                int size = _entries.size();
                for (int i = 0; i < size-1; i++) {
                    _entries.removeLast();
                }
                double ratio = currentStepSize/_stepsize;
                for (int i = 1; i < size; i++) {
                    double[] newEntry;
                    int bin = (int)Math.floor(i*ratio);
                    if (bin < size) {
                        // Interpolation as much as possible.
                        double remainder = i*ratio - (double)bin;
                        newEntry = _Hermite(history[bin+1], history[bin],
                                1-remainder);
                    } else {
                        // Extrapolation
                        newEntry = _extrapolation(history[size-2],
                                history[size-1], i*ratio-size+1);
                    }
                    _entries.addLast(new DoubleDouble
                        (newEntry[0], newEntry[1]));
                }
                _stepsize = currentStepSize;
            }
        }

        /** Set the history capacity. The entries exceed the capacity
         *  will be lost. If the argument is less than 0, it is set
         *  to 0.
         *  @param capacity The new capacity.
         */
        public void setCapacity(int capacity) {
            _capacity = (capacity>0) ? capacity : 0;
            while (_entries.size() > capacity) {
                _entries.removeLast();
            }
        }

        /** Return the history information in an array format. The
         *  entries are ordered in their backward chronological order.
         *  @return The content of the history information in a double
         *      array format.
         */
        public double[][] toDoubleArray() {
            double[][] array = new double[_entries.size()][2];
            Iterator objs = _entries.iterator();
            int i = 0;
            while (objs.hasNext()) {
                DoubleDouble entry = (DoubleDouble) objs.next();
                array[i++] = entry.toArray();
            }
            return array;
        }

        ///////////////////////////////////////////////////////////////
        ////                        private methods                ////

        // Hermite interpolation.
        // @param p1 Point 1, state and derivative.
        // @param p2 Point 2, state and derivative.
        // @param s The interpolation point.
        // @return The Hermite interpolation of the arguments.
        private double[] _Hermite(double[] p1, double[] p2, double s) {
            double s3 = s*s*s;
            double s2 = s*s;
            double h1 = 2*s3 - 3*s2 + 1;
            double h2 = -2*s3 + 3*s2;
            double h3 = s3- 2*s2 + s;
            double h4 = s3 - s2;
            double g1 = 6*s2 - 6*s;
            double g2 = -6*s2 + 6*s;
            double g3 = 3*s2 -4*s +1;
            double g4 = 3*s2 -2*s;
            double[] result = new double[2];
            result[0] = h1*p1[0] + h2*p2[0] + h3*p1[1] + h4*p2[1];
            result[1] = g1*p1[0] + g2*p2[0] + g3*p1[1] + g4*p2[1];
            return result;
        }

        // Linear extrapolation.
        // @param p1 Point1, state and derivative.
        // @param p2 Point2, state and derivative.
        // @param s The extrapolation ration.
        // @return The extrapolation of the arguments.
        private double[] _extrapolation(double[] p1, double[] p2, double s) {

            double[] result = new double[2];
            result[0] = p2[0] - (p1[0] - p2[0])*s;
            result[1] = p2[1] - (p1[1] - p2[1])*s;
            return result;
        }

        ///////////////////////////////////////////////////////////////
        ////                        private variables               ////

        // The container.
        CTBaseIntegrator _container;

        // The linked list storing the entries
        LinkedList _entries;

        // The capacity.
        int _capacity;

        // The step size that the entries are based on.
        double _stepsize;
    }

    ///////////////////////////////////////////////////////////////////
    ////                           Inner Class                     ////

    /** A data structure for storing two double numbers
     */
    private class DoubleDouble {

        /** construct the Double pair.
         */
        public DoubleDouble(double first, double second) {
            _data[0] = first;
            _data[1] = second;
        }

        ///////////////////////////////////////////////////////////////
        ////                         public methods                ////

        /** Return the data as a double array.
         */
        public double[] toArray() {
            return _data;
        }

        ///////////////////////////////////////////////////////////////
        ////                        private variables               ////

        // The data as a form of a double array of two elements
        private double[] _data = new double[2];
    }
}
