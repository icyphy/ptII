/*  Invoke a Functional Mock-up Interface (FMI) 2.0 Model Exchange Functional
    Mock-up Unit (FMU) which will be integrated using QSS.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ptolemy.fmi.FMI20ContinuousStateDerivative;
import org.ptolemy.fmi.FMI20EventInfo;
import org.ptolemy.fmi.FMI20ModelInstance;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIModelDescription.ContinuousState;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.NativeSizeT;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.qss.solver.QSSBase;
import org.ptolemy.qss.util.DerivativeFunction;
import org.ptolemy.qss.util.ModelPolynomial;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.type.BaseType;
import ptolemy.domains.qss.kernel.QSSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

/**
 * Invoke a Functional Mock-up Interface (FMI) 2.0 Model Exchange Functional
 * Mock-up Unit (FMU) which will be integrated using QSS.
 *
 * <p>
 * Read in a <code>.fmu</code> file named by the <i>fmuFile</i> parameter. The
 * <code>.fmu</code> file is a zipped file that contains a file named
 * <code>modelDescription.xml</code> that describes the ports and parameters
 * that are created. At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code> file.
 * </p>
 *
 * <p>
 * To use this actor from within Vergil, use File -&gt; Import -&gt; Import FMU
 * for QSS integration, which will prompt for a .fmu file. This actor is
 * <b>not</b> available from the actor pane via drag and drop. The problem is
 * that dragging and dropping this actor ends up trying to read fmuImport.fmu,
 * which does not exist. If we added such a file, then dragging and dropping the
 * actor would create an arbitrary actor with arbitrary ports.
 * </p>
 * This actor uses any QSS solver to integrate the FMUs for model exchange. A
 * package of QSS solvers can be found in org.ptolemy.qss. This actor extends
 * from FMUImport.java and should be used along with the QSSDirector.
 *
 * @author Thierry S. Nouidui, David M. Lorenzetti, Michael Wetter, Christopher Brooks.
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUQSS extends FMUImport implements DerivativeFunction {

    /**
     * Construct an actor with the given container and name.
     *
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be
     * contained by the proposed container.
     * @exception NameDuplicationException If the container already
     * has an actor with this name.
     */
    public FMUQSS(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        initFMUParameters = new Parameter(this, "initFMUParameters");
        initFMUParameters.setTypeEquals(BaseType.BOOLEAN);
        initFMUParameters.setExpression("true");

        // The modelExchange parameter in the parent class is marked
        // as expert, which means it is not usually visible to the
        // user. QSS FMUs are always model exchange, so we change the
        // visibility to none just to be sure no one tries to edit it.
        // We can use the parent attributeChanged method.
        modelExchange.setExpression("true");
        modelExchange.setVisibility(Settable.NONE);

        // The persistenInputs parameter in the parent class is marked
        // as false. QSS FMUs do not support "absent" values, so we change the
        // parameter to true so a user does not need to set this manually.
        persistentInputs.setExpression("true");

        // stateVariablesAsInputPorts is true in the parent class and
        // it is used by FMUImport._getInputs().
        stateVariablesAsInputPorts.setExpression("false");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-25\" y=\"7\" "
                + "style=\"font-size:12\">\n" + "FMUQSS" + "</text>\n"
                + "</svg>\n");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * If true, indicate the FMU to initialize parameters variables
     * parameters.  The default value is true.
     */
    public Parameter initFMUParameters;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Evaluate the derivative function for state event detection.
     *
     * @param time The current simulation time.
     * @param dtSample The delta time used to approximate the first derivative.
     * @param eventIndicatorDerivatives The (output) vector of 
     * time rates of change of the event indicators variables
     * at <code>time</code>.
     * @param eventIndicatorDerivatives2 The (output) vector of 
     * time rates of change of the event indicators variables.
     * @param eventIndicatorDerivatives3 The (output) vector of 
     * time rates of change of the event indicators variables.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException If an error occurred.
     */
    @Override
    public final int evaluateDerivatives(final Time time,
            final double[] dtSample,
            final double[] eventIndicatorVariableDerivatives,
            final double[] eventIndicatorVariableDerivatives2,
            final double[] eventIndicatorVariableDerivatives3, final int order)
            throws IllegalActionException {

        // Check assumptions.
        assert (eventIndicatorVariableDerivatives.length == getEventIndicatorCount());
        // Return the values computed when calling evaluateDerivatives()
        for (int ii = 0; ii < 3; ++ii) {
            dtSample[ii] = _deltaSample[ii];
        }
        if (order >= 0) {
            for (int ii = 0; ii < getEventIndicatorCount(); ++ii) {
                eventIndicatorVariableDerivatives[ii] = _eventIndicatorsDerivatives[ii];
            }
        }
        if (order >= 1) {
            for (int ii = 0; ii < getEventIndicatorCount(); ++ii) {
                eventIndicatorVariableDerivatives2[ii] = _eventIndicatorsDerivatives2[ii];
            }
        }
        if (order >= 2) {
            for (int ii = 0; ii < getEventIndicatorCount(); ++ii) {
                eventIndicatorVariableDerivatives3[ii] = _eventIndicatorsDerivatives3[ii];
            }
        }
        return 0;
    }

    /**
     * Evaluate the derivative function.
     *
     * @param time The current simulation time.
     * @param stateVariables The vector of state variables at <code>time</code>.
     * @param inputVariables The vector of input variables at <code>time</code>.
     * @param stateVariableDerivatives The (output) vector of time rates of change of the state variables
     * at <code>time</code>.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException If using raw JNI throws it,
     * if the time cannot be set, if the continuous states cannot be
     * set or if the state count cannot be read.
     */
    @Override
    public final int evaluateDerivatives(final Time time,
            final double[] stateVariables, final double[] inputVariables,
            final double[] stateVariableDerivatives)
            throws IllegalActionException {

        // Check assumptions.
        assert (getStateCount() > 0);
        assert (stateVariables.length == getStateCount());
        assert (stateVariableDerivatives.length == getStateCount());

        // Set the time.
        _fmiSetTime(time);

        // Enter event mode.
        _enterEventMode();

        // Give {stateVariable} to the FMU.
        _fmiSetContinuousStates(stateVariables);

        // Update the FMUs inputs if needed.
        _setFMUInputsAtCurrentTime(time, inputVariables, false);

        // Give {stateVariableDerivative} to FMU and evaluate the derivative function.
        if (!_useRawJNI()) {
            _fmiGetDerivatives(getStateCount(), stateVariableDerivatives);
        } else {
            _fmiGetDerivativesJNI(stateVariableDerivatives);
        }

        // Exit event mode.
        _enterDiscreteStateMode();

        // Enter continuous time mode.
        _enterContinuousTimeMode();

        return 0;
    }

    /**
     * Evaluate directional derivative and compute second derivative.
     *
     * @param stateDerivIndex The state derivative index.
     * @param stateVariableDerivatives The state derivatives.
     * @param inputVariableDerivatives The input derivatives.
     * @return The directional derivative (see fmi2GetDirectionalDerivatives in FMI specification).
     * @exception IllegalActionException If thrown while computing the directional derivative.
     */
    @Override
    public final double evaluateDirectionalDerivatives(
            final int stateDerivIndex, final double[] stateVariableDerivatives,
            final double[] inputVariableDerivatives)
            throws IllegalActionException {
        // FIXME: Evaluating directional derivatives only works with FMUs generated by Dymola 2015 FD01 on Windows.
        // We will need to see whether newer versions of Dymola will work on any OS.
        if (!_useRawJNI()) {
            // Get second derivative of current input.
            return (_evaluateInputDirectionalDerivatives(stateDerivIndex,
                    inputVariableDerivatives) + _evaluateStateDirectionalDerivatives(
                    stateDerivIndex, stateVariableDerivatives));
        } else {
            // Get the current continuous state.
            final FMI20ContinuousStateDerivative stateDerivative = _fmiModelDescription.continuousStateDerivatives
                    .get(stateDerivIndex);
            final long stateDerivRef = stateDerivative.scalarVariable.valueReference;
            double[] stateSecondDerivative = new double[1];

            // Get the dependent inputs and match the indexes to the input list.
            final int numDepInputs = stateDerivative.dependentInputIndexes
                    .size();
            final double[] tmpInputs = new double[numDepInputs];
            final long[] tmpInputsRefs = new long[numDepInputs];

            for (int ii = 0; ii < numDepInputs; ++ii) {
                final int curIdx = stateDerivative.dependentInputIndexes
                        .get(ii);
                final Input input = _inputs.get(curIdx);
                tmpInputs[ii] = inputVariableDerivatives[curIdx];
                tmpInputsRefs[ii] = input.scalarVariable.valueReference;
            }

            // Get the dependent states and match the indexes to the continuous
            // state list.
            final int numDepStates = stateDerivative.dependentStateIndexes
                    .size();
            final double[] tmpStates = new double[numDepStates];
            final long[] tmpStatesRefs = new long[numDepStates];

            for (int ii = 0; ii < numDepStates; ++ii) {
                final int curIdx = stateDerivative.dependentStateIndexes
                        .get(ii);
                final ContinuousState state = _fmiModelDescription.continuousStates
                        .get(curIdx);
                tmpStates[ii] = stateVariableDerivatives[curIdx];
                tmpStatesRefs[ii] = state.scalarVariable.valueReference;
            }

            // Give {stateVariableDerivatives} and {inputVariableDerivatives}
            // and retrieve {stateSecondDerivative}.
            // FIXME: This can only be used for QSS2. The assumption being here
            // that the time and the continuous states have been set previously.
            runNativeFMU(_fmiJNIComponent, -10, null, null, null, 0.0, 0.0,
                    0.0, 0, 0.0, 0, 0, null, null, null, null, null, null,
                    null, null, stateDerivRef, tmpStates, tmpStatesRefs,
                    tmpInputs, tmpInputsRefs, stateSecondDerivative, null, null);

            return stateSecondDerivative[0];
        }
    }

    /**
    * Provide inputs to evaluate the derivative 
    * function for event indicators using finite difference methods. 
    *
    * @param time Simulation time.
    * @param stateVariables The vector of state variables at <code>time</code>.
    * @param inputVariables The vector of input variables at <code>time</code>.
    * @param timeSample Simulation time.
    * @param stateVariablesSample The vector of state variables at <code>timeSample</code>.
    * @param inputVariablesSample The vector of input variables at <code>timeSample</code>.
    * @param dtSample The delta between <timeSample> and  <code>time</code>.
    * @param timeSample2 Simulation time.
    * @param stateVariablesSample2 The vector of state variables at <code>timeSample2</code>.
    * @param inputVariablesSample2 The vector of input variables at <code>timeSample2</code>.
    * @param dtSample2 The delta between <timeSample2> and  <code>time</code>.
    * @param timeSample3 Simulation time.
    * @param stateVariablesSample3 The vector of state variables at <code>timeSample3</code>.
    * @param inputVariablesSample3 The vector of input variables at <code>timeSample3</code>.
    * @param dtSample3 The delta between <timeSample3> and  <code>time</code>.
    * @param timeSample4 Simulation time.
    * @param stateVariablesSample4 The vector of state variables at <code>timeSample4</code>.
    * @param inputVariablesSample4 The vector of input variables at <code>timeSample4</code>.
    * @param dtSample4 The delta between <timeSample4> and  <code>time</code>.
    * @param timeSample5 Simulation time.
    * @param stateVariablesSample5 The vector of state variables at <code>timeSample5</code>.
    * @param inputVariablesSample5 The vector of input variables at <code>timeSample5</code>.
    * @param dtSample5 The delta between <timeSample4> and  <code>time</code>.
    * @return Success (0 for success, else user-defined error code).
    * @exception IllegalActionException If an error occurred.
    */
    @Override
    public int eventIndicatorDerivativeInputs(Time time,
            double[] stateVariables, double[] inputVariables, Time timeSample,
            double[] stateVariablesSample, double[] inputVariablesSample,
            double dtSample, Time timeSample2, double[] stateVariablesSample2,
            double[] inputVariablesSample2, double dtSample2, Time timeSample3,
            double[] stateVariablesSample3, double[] inputVariablesSample3,
            double dtSample3, Time timeSample4, double[] stateVariablesSample4,
            double[] inputVariablesSample4, double dtSample4, Time timeSample5,
            double[] stateVariablesSample5, double[] inputVariablesSample5,
            double dtSample5, int stateModelOrder)
            throws IllegalActionException {
        // Check assumptions.
        assert (getStateCount() > 0);
        assert (stateVariables.length == getStateCount());

        final int numEvtInd = getEventIndicatorCount();
        // get current event indicators
        if (numEvtInd > 0) {

            if (stateModelOrder >= 0) {
                _deltaSample[0] = dtSample;
                _deltaSample[1] = 0.0;
                _deltaSample[2] = 0.0;
                // Approximate first derivative using two sample times
                double[] eventIndicator = new double[numEvtInd];
                double[] eventIndicatorSample = new double[numEvtInd];
                _getEventIndicators(eventIndicator, numEvtInd, time,
                        stateVariables, inputVariables);
                _getEventIndicators(eventIndicatorSample, numEvtInd,
                        timeSample, stateVariablesSample, inputVariablesSample);
                for (int i = 0; i < numEvtInd; ++i) {
                    _eventIndicatorsDerivatives[i] = (eventIndicatorSample[i] - eventIndicator[i])
                            / dtSample;
                }
            }
            if (stateModelOrder >= 1) {
                _deltaSample[1] = dtSample2;
                // Approximate first derivative using two sample times.
                double[] eventIndicatorSample2 = new double[numEvtInd];
                double[] eventIndicatorSample3 = new double[numEvtInd];
                _getEventIndicators(eventIndicatorSample2, numEvtInd,
                        timeSample2, stateVariablesSample2,
                        inputVariablesSample2);
                _getEventIndicators(eventIndicatorSample3, numEvtInd,
                        timeSample3, stateVariablesSample3,
                        inputVariablesSample3);
                for (int i = 0; i < numEvtInd; ++i) {
                    _eventIndicatorsDerivatives2[i] = (eventIndicatorSample3[i] - eventIndicatorSample2[i])
                            / (dtSample3 - dtSample2);
                }
            }

            if (stateModelOrder >= 2) {
                _deltaSample[2] = dtSample4;
                // Approximate first derivative using two sample times.
                double[] eventIndicatorSample4 = new double[numEvtInd];
                double[] eventIndicatorSample5 = new double[numEvtInd];
                _getEventIndicators(eventIndicatorSample4, numEvtInd,
                        timeSample4, stateVariablesSample4,
                        inputVariablesSample4);
                _getEventIndicators(eventIndicatorSample5, numEvtInd,
                        timeSample5, stateVariablesSample5,
                        inputVariablesSample5);
                for (int i = 0; i < numEvtInd; ++i) {
                    _eventIndicatorsDerivatives3[i] = (eventIndicatorSample5[i] - eventIndicatorSample4[i])
                            / (dtSample5 - dtSample4);
                }
            }
        }
        return 0;
    }

    /**
     * Override the FMUImport base class to produce outputs.
     *
     * <p> According to "System Design, Modeling, and Simulation Using Ptolemy II",
     * version 1.02, section 12.3.1 "Execution Control": "The main computation
     * of the actor is typically performed during the fire action, when it reads
     * input data, performs computation, and produces output data."</p>
     *
     * If it is time to produce a quantized output, produce it. If necessary to
     * catch up to current time, and then set the (known) inputs of the FMU and
     * retrieve and send out any outputs for which all inputs on which the
     * output depends are known.
     *
     * @exception IllegalActionException If FMU indicates a failure.
     */
    @Override
    public void fire() throws IllegalActionException {
        // By design, this method does not call super.fire(), because
        // this class uses a different algorithm that is specific
        // to QSS.  However, we need to do what AtomicActor.fire()
        // does:
        if (_debugging) {
            _debug("Called fire()");
        }

        // Get current simulation time.
        final Time currentTime = _director.getModelTime();
        if (_debugging) {
            int currentMicrostep = _director.getIndex();
            _debugToStdOut(String.format(
                    "FMUQSS.fire() on id{%d} at time %s, microstep %d",
                    System.identityHashCode(this), currentTime.toString(),
                    currentMicrostep));
        }

        // Initialize the input variable models.
        if (_firstRound) {
            _initializeQSSIntegratorInputVariables(currentTime);
        }

        // Assume do not need a quantization-event.
        assert (_qssSolver.needQuantizationEventIndex() == -1);
        if (getEventIndicatorCount() > 0) {
            assert (_zcSolver.needQuantizationEventIndex() == -1);
        }

        // Get a new time event
        if (!_firstRound) {
            // Signalize that the time event is reached and get a new one. 
            if (_lastNextEventTime != null
                    && currentTime.compareTo(_lastNextEventTime) == 0) {
                _forceQuantizationTimeEvents = true;
                _numberOfTimeEvents++;
                //if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} has a time event at time %s",
                            System.identityHashCode(this),
                            currentTime.toString()));
                //}
            }
            // Handle step events
            if (_checkStepEvents(currentTime)) {
                _numberOfStepEvents++;
                _forceQuantizationStepEvents = true;
                if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} has a step event at time %s",
                            System.identityHashCode(this),
                            currentTime.toString()));
                }
            }
        }

        // Step.
        // Only step if it will advance the integrator.
        // TODO: Consider instead relaxing the integrator to allow steps that
        // do nothing.
        // TODO: Still need to figure out whether/how to commit integrator to a
        // step, in cases where Ptolemy calls the fire() and postfire() methods
        // multiple times at a single step.
        if (_qssSolver.getCurrentSimulationTime().compareTo(currentTime) < 0) {
            try {
                _qssSolver.stepToTime(currentTime);
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee.getMessage());
            }
            // Requantize if necessary.
            // Check if we have a valid state event. if true then requantize
            // all states as we do not know the state which had a state event.
            if (_forceQuantizationStateEvents || _forceQuantizationTimeEvents
                    || _forceQuantizationStepEvents) {
                _triggerQuantizationEvents(currentTime, true);
                if (_forceQuantizationStateEvents)
                    _forceQuantizationStateEvents = false;
                if (_forceQuantizationTimeEvents)
                    _forceQuantizationTimeEvents = false;
                if (_forceQuantizationStepEvents)
                    _forceQuantizationStepEvents = false;
            } else {
                _triggerQuantizationEvents(currentTime, false);
            }
        }
        // FIXME: Maybe we can merge with the above?
        if (getEventIndicatorCount() > 0) {
            if (_zcSolver.getCurrentSimulationTime().compareTo(currentTime) < 0) {
                try {
                    _zcSolver.stepToTime(currentTime, getEventIndicatorCount());
                } catch (Exception ee) {
                    throw new IllegalActionException(this, ee.getMessage());
                }
                _triggerQuantizationEventsEventIndicator(currentTime, false);
            }
        }
        _numberOfSteps++;
    }

    /**
     * Return the count of event indicator variables.
     *
     * @return The number of event  indicator.
     */
    @Override
    public final int getEventIndicatorCount() {
        return _fmiModelDescription.numberOfEventIndicators;
        // return 0;
    }

    /**
     * Return the count of input variables.
     *
     * @return The number of input variables.
     */
    @Override
    public final int getInputVariableCount() {
        return _inputs.size();
    }

    /**
     * Indicate existence of directional derivatives.
     *
     * @return True if directional derivatives are provided.
     */
    @Override
    public final boolean getProvidesDirectionalDerivatives() {
        return _fmiModelDescription.providesDirectionalDerivative;
    }

    /**
     * Return the count of state variables.
     *
     * @return The number of state variables.
     */
    @Override
    public final int getStateCount() {
        return _fmiModelDescription.numberOfContinuousStates;
    }

    /**
     * Override the importFMU in FMUImport base class.
     *
     * @param originator The originator of the change request.
     * @param fmuFileParameter The .fmuFile
     * @param context The context in which the FMU actor is created.
     * @param x The x-axis value of the actor to be created.
     * @param y The y-axis value of the actor to be created.
     * @exception IllegalActionException If there is a problem instantiating the actor.
     * @exception IOException If there is a problem parsing the fmu file.
     */
    public static void importFMU(Object originator,
            FileParameter fmuFileParameter, NamedObj context, double x, double y)
            throws IllegalActionException, IOException {
        // FIXME: we should use a factory here.  The issue is that
        // when we import a fmu for QSS, we want to call
        // FMUQSS._acceptFMU().  However, we are using static methods
        // because we use a MoMLChangeRequest to instantiate the actor
        // so we can't use object-oriented design to have the subclass
        // provide an implementation of _acceptFMU(). ick.
        Method acceptFMUMethod = null;
        try {
            Class clazz = Class.forName("ptolemy.actor.lib.fmi.FMUQSS");
            acceptFMUMethod = clazz.getDeclaredMethod("_acceptFMU",
                    new Class[] { FMIModelDescription.class });
        } catch (Throwable throwable) {
            throw new IllegalActionException(
                    context,
                    throwable,
                    "Failed to get the static _acceptFMU(FMIModelDescription) method for FMUImport.");
        }

        // Note that this method is declared to not have a modelExchange
        // parameter because all QSS FMUs are model exchange FMUs.  However,
        // _importFMU does take a modelExchange parameter.
        FMUImport._importFMU(originator, fmuFileParameter, context, x, y,
                true /* modelExchange */, false /*addMaximumStepSize*/,
                false /* stateVariablesAsInputPorts */,
                "ptolemy.actor.lib.fmi.FMUQSS", acceptFMUMethod);
    }

    /**
     * Initialize this FMU wrapper.
     *
     * <p>According to "System Design, Modeling, and Simulation Using Ptolemy II",
     * version 1.02, section 12.3.1 "Execution Control": "The initialize action
     * of the setup phase initializes parameters, resets local state, and sends
     * out any initial messages."</p>
     *
     * @exception IllegalActionException If the slave FMU cannot be initialized.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // By design, this method does not call super.initialize(),
        // because this class uses a different algorithm that is
        // specific to QSS.  However, we need to do what
        // AtomicActor.initialize() does:

        // Initialize report variables
        _numberOfStateEvents = 0;
        _numberOfTimeEvents = 0;
        _numberOfStepEvents = 0;
        _numberOfSteps = 0;
        _deltaSample = new double[3];
        _hasStateEventAtStart = false;

        _lastNextEventTime = null;
        _forceQuantizationStepEvents = false;

        if (_debugging) {
            _debug("Called initialize()");
        }
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
        // end of AtomicActor.java

        if (_debugging) {
            _debugToStdOut(String.format("FMUQSS.initialize() on id{%d}",
                    System.identityHashCode(this)));
        }

        int fmiFlag;

        // Set a flag so the first call to fire() and postfire() can do appropriate
        // initialization.
        _firstRound = true;
        // Initialize the quantum scale factor.
        _quantumScaleFactor = _director.getQuantumScaleFactor();
        // Initialize the internal relative quantum
        _internalRelativeQuantum = _director.getRelativeQuantum()
                * _quantumScaleFactor;
        if (!_useRawJNI()) {
            // Initialize FMU parameters.
            if (((BooleanToken) initFMUParameters.getToken()).booleanValue()
                    && !_useRawJNI()) {
                _initializeFMUParameters();
            }
            // Enter and exit the initialization mode.
            _fmiInitialize();

            // Initialize the model instance
            if (_firstRound) {
                _fmi20ModelInstance = new FMI20ModelInstance(_fmiComponent);
                if (_fmi20ModelInstance.eventInfo == null) {
                    _fmi20ModelInstance.eventInfo = new FMI20EventInfo();
                }
            }

            // To initialize the event indicators, call this.
            // _checkStateEvents();

            // The specification says on page 75 that after calling
            // fmiExitInitializationMode, the FMU is implicitly in Event Mode
            // and all discrete-time and continuous time variables at the
            // initial time can be calculated, if needed also iteratively due
            // to an algebraic loop. Once finalized, fmiNewDiscreteStates must be
            // called,
            // and depending on the value of the return argument, the FMU either
            // continues
            // the event iteration at the initial time or switches to Continuous
            // mode.
            int newDiscreteStatesNeeded = 1;
            int terminateSimulation = 0;
            int nominalsOfContinuousStatesChanged = 0;
            int valuesOfContinuousStatesChanged = 0;
            int nextEventTimeDefined = 0;
            double nextEventTime = -1.0;

            // In FMI-2.0, this is a pointer to the structure, which is by
            // default how a subclass of Structure is handled, so there is no
            // need for having FMI20EventInfo.ByValue().
            _eventInfo = new FMI20EventInfo(newDiscreteStatesNeeded,
                    terminateSimulation, nominalsOfContinuousStatesChanged,
                    valuesOfContinuousStatesChanged, nextEventTimeDefined,
                    nextEventTime);

            if (_fmiNewDiscreteStatesFunction == null) {
                throw new InternalErrorException(
                        this,
                        null,
                        "_fmiNewDiscreteStatesFunction was null, indicating that the fmiNewDiscreteStates() function was not found? "
                                + "This can happen if for some reason the FMU file was imported into Ptolemy II as a Co-Simulation FMU instead of as a Model Exchange FMU."
                                + "The actor's modelExchange parameter is: "
                                + modelExchange
                                + ", the value of the modelExchange field in the _fmiModelDescription is: "
                                + _fmiModelDescription.modelExchange);
            }

            // FIXME: We assume no event iteration.
            fmiFlag = ((Integer) _fmiNewDiscreteStatesFunction.invoke(
                    Integer.class, new Object[] { _fmiComponent, _eventInfo }))
                    .intValue();

            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this,
                        "Failed to enter discrete state FMU: "
                                + _fmiStatusDescription(fmiFlag));
            }

            final boolean timeEventOccurred = (_eventInfo.nextEventTimeDefined == 1);
            if (timeEventOccurred) {
                _lastNextEventTime = new Time(_director,
                        _eventInfo.nextEventTime);
            }

            // FIXME: we check to see whether we should stay in the event mode but do
            // not do anything if we have to stay in event mode.
            // We assume that there is no event and that we can switch to the
            // continuous mode.
            // We nonetheless warn the user with an exception which does not stop
            // the simulation.
            if (_eventInfo.newDiscreteStatesNeeded != 0) {
                new Exception(
                        "Warning: FIXME: Need to stay in event mode and do an event update.")
                        .printStackTrace();
            }

            // We check whether we should terminate the simulation.
            // If that is the case we call wrapup() and terminate the simulation.
            // If not we enter the continuous mode and do the time integration.
            if (_eventInfo.terminateSimulation != 0) {
                getDirector().finish();
            }
        } else {
            // Get the model identifier from the model description.
            final String modelIdentifier = _fmiModelDescription.modelIdentifier;

            // There is no simulator UI.
            byte toBeVisible = 0;
            if (((BooleanToken) visible.getToken()).booleanValue()) {
                toBeVisible = 1;
            }

            // FIXME: We should send logging messages to the debug listener.
            byte loggingOn = _debugging ? (byte) 1 : (byte) 0;

            // Get the path to the native FMU library.
            String fmuLibPath = null;
            try {
                fmuLibPath = _fmiModelDescription.getNativeLibraryPath();
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Could not find path to the native library.");
            }

            // Instantiate FMU
            final Time currentTime = _director.getModelTime();
            final Time startTime = _director.getModelStartTime();
            final Time stopTime = _director.getModelStopTime();
            final double timeValue = currentTime.getDoubleValue();

            // Initialize number of continuous states so the size is defined once.
            double[] derivatives = new double[_fmiModelDescription.numberOfContinuousStates];

            // Initialize number of continuous states so the size is defined once.
            double[] eventIndicators = new double[getEventIndicatorCount()];

            // Instantiate FMU.
            _fmiJNIComponent = _fmiInstantiateJNI(modelIdentifier, fmuLibPath,
                    _fmiModelDescription.fmuResourceLocation,
                    startTime.getDoubleValue(), stopTime.getDoubleValue(),
                    timeValue, 0, _internalRelativeQuantum, toBeVisible,
                    loggingOn, _fmiModelDescription.guid, derivatives,
                    eventIndicators);

            // Initialize FMU parameters.
            if (((BooleanToken) initFMUParameters.getToken()).booleanValue()
                    && !_useRawJNI()) {
                _initializeFMUParameters();
            }

            // Initialize FMU.
            _fmiInitializeJNI();

            // Check event indicator for time event
            _fmiNewDiscreteStatesJNI();

            final boolean timeEventOccurred = (_eventInfoJNI.nextEventTimeDefined == 1);
            if (timeEventOccurred) {
                _lastNextEventTime = new Time(_director,
                        _eventInfoJNI.nextEventTime);
            }

        }

        // Get and configure the QSS integrator.
        _createQSSSolver();

        // To make sure this actor fires at the start time, request a firing.
        _director.fireAtCurrentTime(this);

        return;

    }

    /**
     * Update the calculation of the next output time and request a refiring at
     * that time.
     *
     * <p>According to "System Design, Modeling, and Simulation Using Ptolemy II",
     * version 1.02, section 12.3.1 "Execution Control": "An actor may have
     * persistent state that evolves during execution; the postfire action
     * updates that state in response to any inputs. The fact that the state of
     * an actor is updated only in postfire is an important part of the actor
     * abstract semantics..."</p>
     *
     * If there is a new input, read it and update the slope.
     *
     * @return True if the base class returns true.
     * @exception IllegalActionException If reading inputs or parameters fails.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // By design, this method does not call super.fire(), because
        // this class uses a different algorithm that is specific
        // to QSS.  However, we need to do what AtomicActor.postfire()
        // does:
        if (_debugging) {
            _debug("Called postfire()");
        }

        // Get current simulation time.
        final Time currentTime = _director.getModelTime();
        if (_debugging) {
            int currentMicrostep = _director.getIndex();
            _debugToStdOut(String.format(
                    "FMUQSS.postfire() on id{%d} at time %s, microstep %d",
                    System.identityHashCode(this), currentTime.toString(),
                    currentMicrostep));
        }

        // To Discuss for optimization
        // In the current approach, we have a  model
        // which is similar to the state model for the
        // event indicator. This model is used to predict
        // state events. When a state event is predicted
        // we extrapolate time to that point and check 
        // whether we had a real state event. If yes, then
        // we quantize the states. This approach is a bit expensive
        // as we have event indicator models that we carry throughout the
        // simulation, however since we do not export the models
        // to the ports, it might not be that expensive. There might
        // be better way to do?

        // Update the internal, continuous state models if necessary.
        boolean triggerRateEvent[] = { false };
        _triggerRateEvent(currentTime, false, triggerRateEvent);

        if (getEventIndicatorCount() > 0) {
            if (triggerRateEvent[0]) {
                try {
                    _zcSolver.triggerRateEvent(getEventIndicatorCount());
                } catch (Exception ee) {
                    throw new IllegalActionException(this, ee,
                            "Triggering rate event failed.");
                }
            }
        }

        // Find the next firing time, assuming nothing else in simulation
        // changed.
        Time possibleFireAtTime = _qssSolver
                .predictQuantizationEventTimeEarliest();

        // Check if the next firing is over the next event time
        if ((_lastNextEventTime != null)
                && _lastNextEventTime.compareTo(possibleFireAtTime) <= 0) {
            possibleFireAtTime = _lastNextEventTime;
        }

        if (getEventIndicatorCount() > 0) {
            // Handle state events
            // If we have a state event at simulation start,
            // we do not try to predict the next state event
            // as this will lead to time which is very closed
            // to start time. 
            if (_firstRound && _hasStateEventAtStart) {
                // We increase the number of state events but do
                // not re-quantize the models as we know that the
                // states are exported at simulation start.
                _numberOfStateEvents++;
                if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} has a state event at start time %s",
                            System.identityHashCode(this),
                            currentTime.toString()));
                    _hasStateEventAtStart = false;
                }
            } else {
                // We check if we have a state event at the predicted time.
                // If we do, then we use the event indicator model to 
                // determine the time. Another approach will be to check
                // the event indicator model only but this will increase 
                // the number of steps. Here we just have to select a quantum
                // which is small enough so we can still catch state events.	
                if (_checkStateEvents(possibleFireAtTime)) {
                    final Time possibleNextStateEventTime = _zcSolver
                            .predictQuantizationEventTimeEarliest();
                    if (possibleFireAtTime
                            .compareTo(possibleNextStateEventTime) > 0) {
                        possibleFireAtTime = possibleNextStateEventTime;
                        // Check if we have a real state event
                        if (_checkStateEvents(possibleFireAtTime)) {
                            _forceQuantizationStateEvents = true;
                            _numberOfStateEvents++;
                            //if (_debugging) {
                                _debugToStdOut(String
                                        .format("-- Id{%d} predicts a state event at time %s",
                                                System.identityHashCode(this),
                                                possibleFireAtTime.toString()));
                            //}
                        }
                    }
                }
            }
        }

        if (_debugging) {
            _debugToStdOut(String.format("-- Id{%d} want to fire by time %s",
                    System.identityHashCode(this),
                    possibleFireAtTime.toString()));
        }

        // Cancel firing time if necessary.
        final boolean possibleDiffersFromLast = (null == _lastFireAtTime || possibleFireAtTime
                .compareTo(_lastFireAtTime) != 0);
        if (null != _lastFireAtTime // Made request before.
                && _lastFireAtTime.compareTo(currentTime) > 0 // Last request was
                // not used.
                // _lastFireAtTime >
                // currentTime
                && possibleDiffersFromLast // Last request is no longer valid.
        ) {
            if (_debugging) {
                _debugToStdOut(String.format(
                        "-- Id{%d} cancel last fire request (time %s)",
                        System.identityHashCode(this),
                        _lastFireAtTime.toString()));
            }
            _director.cancelFireAt(this, _lastFireAtTime);
        }

        // Request firing time if necessary.
        if (possibleDiffersFromLast && !possibleFireAtTime.isPositiveInfinite()) {
            if (_debugging) {
                _debugToStdOut(String.format(
                        "-- Id{%d} request fire by time %s",
                        System.identityHashCode(this),
                        possibleFireAtTime.toString()));
            }
            getDirector().fireAt(this, possibleFireAtTime);
            _lastFireAtTime = possibleFireAtTime;
        }
        _firstRound = false;
        // As we are not calling super.postfire(), we need
        // to do what AtomicActor.postfire() does and check
        // to see if stop was requested:
        return !_stopRequested;
    }

    /**
     * Initialize the continuous states.
     *
     * @exception IllegalActionException if an exception occurs.
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        // Reninitialize the director
        _director = null;
        
        // Initialize the input list.
        _inputs = new LinkedList<Input>();

        // Initialize the output list.
        _outputs = new LinkedList<Output>();

        // Initialize map for input and continuous state indexes.
        //_modelVariableIndexesOfInputsAndContinuousStates = new HashMap<String, Integer>();

        // Get the inputs from super class.
        _inputs = _getInputs();

        // Create an array of input value references
        _inputValueReferences = new long[_inputs.size()];

        for (int ii = 0; ii < _inputs.size(); ++ii) {
            final FMIScalarVariable scalar = _inputs.get(ii).scalarVariable;
            _inputValueReferences[ii] = scalar.valueReference;
        }

        // Get the outputs from super class
        _outputs = _getOutputs();

        // Initialize the continuous states.
        _initializeContinuousStates();

        // Initialize list of indexes of updated variables
        // _indexesOfUpdatedModelVariables = new LinkedList<Integer>();

        // Get the indexes of dependent variables
        // _getStateDerivativesDependenciesIndexes();

        // Get the indexes of input and state variables.
        // _getIndexOfInputsAndContinuousStates();

        // Initialize hasChanged field of model variables.
        // for (int i = 0; i <  _fmiModelDescription.modelVariables.size(); i++) {
        //    _fmiModelDescription.modelVariables.get(i).hasChanged = true;
        //}

        // Call superclass to instantiate the FMU.
        if (!_useRawJNI()) {
            super.preinitialize();
        } else {
            // By design, this method does not call super.preinitialize() when useRawJNI() is true.
            // However we need to do what AtomicActor.preinitialize()
            // does:
            if (_debugging) {
                _debug("Called preinitialize()");
            }

            _stopRequested = false;

            // For backward compatibility, in case there are actors
            // that override pruneDependencies() to alter their
            // causality interface, call it here.
            pruneDependencies();

            // Declare dependency for this actor. For actors such as
            // TimeDelay, the delay dependency between input and output
            // ports are declared.
            declareDelayDependency();

            // First invoke initializable methods.
            if (_initializables != null) {
                for (Initializable initializable : _initializables) {
                    initializable.preinitialize();
                }
            }

            // end of AtomicActor.java

            // This need to be called to initialize the _outputQuantum
            // since the initialization of the parameter is done in
            // super.preinitialize which is not called when using JNI.
            _hasQSSDirector();

            // Indicate that only parameters with FMIRealType will be updated.
            if (((BooleanToken) initFMUParameters.getToken()).booleanValue()) {
                new Exception(
                        "Warning: initFMUParameters has been set to true for actor "
                                + this.getDisplayName()
                                + " . This is not supported when using raw JNI yet.")
                        .printStackTrace();
            }
            // Load the "JNIFMU" native interface. Use a classpath-relative
            // pathname without the shared library suffix (which is selected
            // and appended by {@link UtilityFunctions#loadLibrary}) for
            // portability.

            String buildLibrary = "ptolemy/actor/lib/fmi/jni/libJNIFMU";
            String installLibrary = "lib/libJNIFMU";
            try {
                if (_debugging) {
                    _debugToStdOut("About to load " + buildLibrary);
                }
                UtilityFunctions.loadLibrary(buildLibrary);
            } catch (Throwable buildThrowable) {
                try {
                    if (_debugging) {
                        _debugToStdOut("Loading " + buildLibrary
                                + " failed with " + buildThrowable
                                + " About to load " + installLibrary);
                    }
                    // This will load the library from $PTII/lib/libFMUQSS.jnilib or .so.
                    UtilityFunctions.loadLibrary(installLibrary);
                } catch (Throwable installThrowable) {
                    throw new IllegalActionException(this, installThrowable,
                            "Failed to load " + installLibrary
                                    + " Also tried to load " + buildLibrary
                                    + ", which threw: " + buildThrowable);
                }
            }
        }
        // Get director and check its type.
        Director director = getDirector();
        if (!(director instanceof QSSDirector)) {
            throw new IllegalActionException(
                    this,
                    String.format(
                            "Director %s cannot be used for QSS, which requires a QSSDirector.",
                            director.getName()));
        }
        // Initialize the QSSDirector
        _director = (QSSDirector) director;
    }

    /**
     * Interface to the FMU.
     *
     * <p>
     * This methods calls the FMU using JNI with different flags.
     * 0: Instantiate.
     * 1: Initialize.
     * 2: Enter event mode.
     * 3: Enter continuous mode.
     * 4: Get continuous states.
     * 5: Get state derivatives.
     * 6: Set continuous states.
     * 7: Set single inputs.
     * 8: Get single outputs.
     * 9: Complete integrator.
     * -1: Terminate simulation.
     * -10: Get directional derivatives."
     * </p>
     * @param idx The index of the FMU instance.
     * @param fla The flag to determine the FMI functions to call.
     * @param instance The FMU instance name.
     * @param jniResourceLocation The path to the FMU native library.
     * @param fmuResourceLocation The path to the FMU resource location.
     * @param tStart The FMU simulation start time.
     * @param tEnd the FMU simulation end time.
     * @param time The current simulation time.
     * @param toleranceDefined the flag is true if sover tolerance is defined.
     * @param tolerance The FMU solver tolerance.
     * @param visible The FMU visible flag.
     * @param loggingOn The FMU logginOn flag.
     * @param guid The FMU GUID.
     * @param continuousStateDerivatives The FMU state derivatives to get.
     * @param continuousStatesFromFMU The FMU continuous states to get.
     * @param continuousStatesToFMU The FMU continuous states to set.
     * @param inputValues The FMU input values.
     * @param inputValueReferences The FMU input value references.
     * @param outputValues The FMU output values.
     * @param outputValueReferences The FMU output value references.
     * @param continuousStateDerivativesValueReference The FMU state derivative value reference.
     * @param directionalStateDerivatives The FMU directional state derivatives.
     * @param directionalStateDerivativesValueReferences The FMU directional state derivatives value references.
     * @param directionalInputDerivatives The FMU directional input derivatives.
     * @param directionalInputDerivativesValueReferences The FMU directional input derivatives value references.
     * @param continuousSecondDerivatives The FMU second derivatives.
     * @param eventIndicators The FMU event indicators.
     * @param nextTimeEvent The next FMU time event.
     * @return FMU instance The index when fla is zero.
     */
    public static native int runNativeFMU(int idx, int fla, String instance,
            String jniResourceLocation, String fmuResourceLocation,
            double tStart, double tEnd, double time, int toleranceDefined,
            double tolerance, int visible, int loggingOn, String guid,
            double[] continuousStateDerivatives,
            double[] continuousStatesFromFMU, double[] continuousStatesToFMU,
            double[] inputValues, long[] inputValueReferences,
            double[] outputValues, long[] outputValueReferences,
            long continuousStateDerivativesValueReference,
            double[] directionalStateDerivatives,
            long[] directionalStateDerivativesValueReferences,
            double[] directionalInputDerivatives,
            long[] directionalInputDerivativesValueReferences,
            double[] continuousSecondDerivatives, double[] eventIndicators,
            double[] nextTimeEvent);

    /**
     * Terminate and free the slave fmu.
     *
     * @exception IllegalActionException If the slave fmu cannot be
     * terminated or freed.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        //if (_debugging) {
            _debugToStdOut(" Number of steps: " + _numberOfSteps
                    + "\n  stateEvents: " + _numberOfStateEvents
                    + "\n  stepEvents: " + _numberOfStepEvents
                    + "\n  timeEvents: " + _numberOfTimeEvents);
        //}
        if (!_useRawJNI()) {
            // Allow eventInfo to be garbaged collected.
            _eventInfo = null;
            _fmi20EventInfo = null;
            super.wrapup();
        } else {
            _eventInfoJNI = null;
            // By design, this method does not call super.wrapup()
            // when JNI is used, because this class uses a different
            // algorithm that is specific to QSS.  However, we need
            // to do what AtomicActor.wrapup()
            // does:
            if (_debugging) {
                _debug("Called wrapup()");
            }
            // Invoke initializable methods.
            if (_initializables != null) {
                for (Initializable initializable : _initializables) {
                    initializable.wrapup();
                }
            }

            // end of AtomicActor.java
            runNativeFMU(_fmiJNIComponent, -1, null, null, null, 0.0, 0.0, 0.0,
                    0, 0.0, 0, 0, null, null, null, null, null, null, null,
                    null, 0, null, null, null, null, null, null, null);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Determine if the model description is acceptable.
     *  FMUQSS only works with FMI-2.0 (and later) model exchange fmus.
     *  @param fmiModelDescription The description of the model to be checked.
     *  @return true if the model description is acceptable.
     *  @exception IOException If the model description is not acceptable
     */
    protected static boolean _acceptFMU(FMIModelDescription fmiModelDescription)
            throws IOException {
        // Check if the version of the FMU is higher than 2.0
        if (fmiModelDescription.fmiVersion.compareTo("2.0") < 0) {
            throw new IOException("The FMI version of this FMU is: "
                    + fmiModelDescription.fmiVersion
                    + " which is not supported.  "
                    + "QSS currently only supports FMI version 2.0.");
        }

        // Check if the FMU is for model exchange.
        if (fmiModelDescription.modelExchangeCapabilities == null) {
            throw new IOException(
                    "There is no ModelExchange attribute in the model description"
                            + " file of This FMU to indicate whether it is for model exchange or not.  "
                            + "QSS currently only supports FMU for model exchange.");
        }

        // Check to see if this FMU is for model exchange.
        // FMUFile checks modelDescription.xml for a ModelExchange element
        // and sets FMIModelDescription.modelExchange accordingly
        if (!fmiModelDescription.modelExchange) {
            throw new IOException("The FMU is not a model exchange FMU.  "
                    + "Perhaps the modelDescription.xml file does not have a "
                    + "\"ModelExchange\" element?  "
                    + "QSS requires a model exchange FMU.");
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return true if we need to update the outputs
     * @param index The variable index.
     * @return True if outputs should be updated.
     */
    private boolean _cancelSendModelToPort(int index) {

        // Check if any of the dependent has changed
        // if not then do not commit any changes.
        boolean doNotUpdate = true;
        for (int i = 0; i < _outputs.get(index).inputStateDependentScalarVariables
                .size(); i++) {
            FMIScalarVariable scalar = _outputs.get(index).inputStateDependentScalarVariables
                    .get(i);
            // Check whether the dependent variable which is an input or a state has changed.
            // FIXME: Note that inputs are not observable thus we assume that when there is
            // a quantization event then the input must have changed too.
            final int modVarIdx = _modelVariableIndexesOfInputsAndContinuousStates
                    .get(scalar.name);
            // Record the changes in this variable.
            if ((modVarIdx >= 0)
                    && _fmiModelDescription.modelVariables.get(modVarIdx).hasChanged) {
                doNotUpdate = false;
            }
        }
        if (_outputs.get(index).inputStateDependentScalarVariables == null
                || _outputs.get(index).inputStateDependentScalarVariables
                        .size() == 0) {
            doNotUpdate = false;
        }
        return doNotUpdate;
    }

    /**
     * Return true if a state event occurred.
     *
     * @param nextStateEventTime The next state event time.
     * @return True if a state event has occurred.
     * @exception IllegalActionException If the fmiGetEventIndicators
     * function is missing, or if calling it does not return fmiOK.
     */
    private boolean _checkStateEvents(Time nextStateEventTime)
            throws IllegalActionException {
        int number = getEventIndicatorCount();
        if (number > 0) {
            double[] currentEventIndicators = new double[number];
            double[] nextEventIndicators = new double[number];
            double stateVariables[] = new double[getStateCount()];

            // Set the states and the inputs and re-evaluate the event indicators.
            for (int i = 0; i < getStateCount(); i++) {
                stateVariables[i] = _qssSolver.evaluateStateModelContinuous(i,
                        getDirector().getModelTime());
            }

            // Get event indicators at current time.
            _getEventIndicators(currentEventIndicators, number, getDirector()
                    .getModelTime(), stateVariables, null);

            // Set the states and the inputs and re-evaluate the event indicators.
            for (int i = 0; i < getStateCount(); i++) {
                stateVariables[i] = _qssSolver.evaluateStateModelContinuous(i,
                        nextStateEventTime);
            }

            // Get the next event indicators.
            _getEventIndicators(nextEventIndicators, number,
                    nextStateEventTime, stateVariables, null);

            // Check for polarity change.
            for (int i = 0; i < number; i++) {
                if (currentEventIndicators[i] * nextEventIndicators[i] < 0.0) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Return true if a step event occurred.
     *
     * @param nextFiringTime The next firing event time.
     * @return True if a step event has occurred.
     * @exception IllegalActionException If the fmiGetEventIndicators
     * function is missing, or if calling it does not return fmiOK.
     */
    private boolean _checkStepEvents(Time nextFiringTime)
            throws IllegalActionException {
        if (!_completedIntegratorStepNotNeeded()) {
            double stateVariables[] = new double[getStateCount()];

            // Set the states and the inputs and re-evaluate the event indicators.
            for (int i = 0; i < getStateCount(); i++) {
                stateVariables[i] = _qssSolver.evaluateStateModelContinuous(i,
                        nextFiringTime);
            }

            // Set the time in the FMU.
            _fmiSetTime(nextFiringTime);

            // Enter event mode.
            _enterEventMode();

            // Give {stateVariable} to the FMU.
            _fmiSetContinuousStates(stateVariables);

            // Update the FMUs inputs if needed.
            _setFMUInputsAtCurrentTime(nextFiringTime, null, true);

            // Exit event mode.
            _enterDiscreteStateMode();

            // Enter continuous time mode.
            _enterContinuousTimeMode();

            // Get step event indicator
            boolean noSetFMUStatePriorToCurrentPoint = true;
            boolean stepEvent = _fmiCompletedIntegratorStep(noSetFMUStatePriorToCurrentPoint);

            if (stepEvent) {
                return true;
            }
            return false;
        }
        return false;
    }

    /** Create a new QSS solver and initialize it for use by this actor.
     *  @exception IllegalActionException If the solver cannot be created or initialized.
     */
    private final void _createQSSSolver() throws IllegalActionException {
        final Time currentTime = _director.getModelTime();

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._initializeQssIntegrator() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Get the number of event indicators.
        final int evtInCt = getEventIndicatorCount();
        // Create a new QSS solver and initialize it.
        _qssSolver = _director.newQSSSolver();
        // Setting the number of event indicators must happen before 
        // we initialize the derivative function.
        _qssSolver.setNumberOfEventIndicators(evtInCt);
        _qssSolver.initializeDerivativeFunction(this);
        _qssSolver.initializeSimulationTime(currentTime);
        _qssSolver
                .setQuantizationEventTimeMaximum(_director.getModelStopTime());

        if (evtInCt > 0) {
            // Create the zero crossing detection solver
            _zcSolver = _director.newQSSSolver();
            // Create the event indicators
            double[] eventIndicators = new double[evtInCt];
            _eventIndicatorsDerivatives = new double[evtInCt];
            _eventIndicatorsDerivatives2 = new double[evtInCt];
            _eventIndicatorsDerivatives3 = new double[evtInCt];
            _zcSolver.initializeDerivativeFunction(this, evtInCt);
            _zcSolver.setNumberOfEventIndicators(evtInCt);
            _zcSolver.initializeSimulationTime(currentTime);
            _zcSolver.setQuantizationEventTimeMaximum(_director
                    .getModelStopTime());

            // Get initial event indicators
            _getEventIndicators(eventIndicators, evtInCt);

            // Set quantization tolerances.
            // Set initial state values and quantization tolerances.
            final double absoluteQuantumMinimumEventDetection = 1e-20;
            double modifiedInternalAbsoluteQuantumEventDetection = _internalRelativeQuantum / 1000;
            double relativeQuantumEventDetection = _internalRelativeQuantum / 1000;
            // double relativeQuantumEventDetection = 1;
            if (modifiedInternalAbsoluteQuantumEventDetection < absoluteQuantumMinimumEventDetection) {
                modifiedInternalAbsoluteQuantumEventDetection = absoluteQuantumMinimumEventDetection;
            }
            for (int ii = 0; ii < evtInCt; ++ii) {
                // Currently a value of 1e-7 indicates a zero crossing at
                // simulation start.
                if (Math.abs(eventIndicators[ii]) < 1e-7) {
                    // Check if we have a state event right
                    // at the beginning of the simulation.
                    // If true, set _hasStateEventAtStart to true.
                    if (_firstRound) {
                        _hasStateEventAtStart = true;
                    }
                }
                _zcSolver.setStateValue(ii, eventIndicators[ii]);
                _zcSolver.setQuantizationTolerance(ii,
                        modifiedInternalAbsoluteQuantumEventDetection,
                        relativeQuantumEventDetection);
            }
        }
        // Get initial state values from FMU.
        final int stateCt = _qssSolver.getStateCount();
        _states = new double[stateCt];
        _stateVariables = new double[stateCt];

        // Enter continuous time mode
        _enterContinuousTimeMode();

        // Get the continuous states.
        _fmiGetContinuousStates(_states);

        // Set initial state values.
        for (int ii = 0; ii < stateCt; ++ii) {
            _qssSolver.setStateValue(ii, _states[ii]);
        }

        // Set quantization tolerances.
        final double absoluteQuantumMinimum = 1e-20;
        for (int ii = 0; ii < stateCt; ++ii) {
            // If the relativeQuantum is greater than 0.0, then use the
            // nominal value for the state, given by the FMU, to
            // calculate the absolute quantum.
            // Thus a state with nominal value 1000 will have an absolute
            // quantum 1000 times greater than a state with nominal value 1.
            final double nominalValue = _fmiModelDescription.continuousStates
                    .get(ii).nominal.doubleValue();
            double modifiedInternalAbsoluteQuantum = Math.abs(nominalValue)
                    * _internalRelativeQuantum;
            if (modifiedInternalAbsoluteQuantum < absoluteQuantumMinimum) {
                modifiedInternalAbsoluteQuantum = absoluteQuantumMinimum;
            }
            _qssSolver.setQuantizationTolerance(ii,
                    modifiedInternalAbsoluteQuantum, _internalRelativeQuantum);
        }
        // Diagnostic output.
        if (_debugging) {

            final int hashCode = System.identityHashCode(this);
            _debugToStdOut(String
                    .format("-- Id{%d} has input ports:", hashCode));
            int ii = 0;
            if (_qssSolver.getInputVariableCount() > 0) {
                for (Input input : _inputs) {
                    _debugToStdOut(String.format("-- %4d: %s", ii,
                            input.scalarVariable.name));
                    ++ii;
                }
            } else {
                _debugToStdOut("-- (None)");
            }

            _debugToStdOut(String.format(
                    "-- Id{%d} has output ports for quantized states:",
                    hashCode));
            for (ii = 0; ii < _qssSolver.getStateCount(); ++ii) {
                final TypedIOPort outPort = _fmiModelDescription.continuousStates
                        .get(ii).port;
                _debugToStdOut(String.format("-- %4d: %s", ii,
                        outPort.getName()));
            }

            _debugToStdOut(String.format(
                    "-- Id{%d} has output ports for FMU outputs:", hashCode));
            final List<Output> outputs = _outputs;
            if (outputs.size() > 0) {
                for (Output output : _outputs) {
                    // If the output port is the port of a quantized state
                    // variable,
                    // then skip it as this has already been set.
                    if (output.scalarVariable.isState) {
                        continue;
                    }
                    _debugToStdOut(String.format("-- %4d: %s", ii,
                            output.scalarVariable.name));
                    ++ii;
                }
            } else {
                _debugToStdOut("-- (None)");
            }

        }

    }

    /**
     * Enter new discrete state mode.
     *
     * @exception IllegalActionException If an error occurs when handling events.
     */
    private void _enterDiscreteStateMode() throws IllegalActionException {
        if (!_useRawJNI()) {
            // Time event indicator
            _fmi20EventInfo = new FMI20EventInfo.ByReference(
                    _fmi20ModelInstance.eventInfo);
            _fmi20EventInfo.newDiscreteStatesNeeded = 1;
            _fmi20EventInfo.terminateSimulation = 0;
            _fmi20EventInfo.nominalsOfContinuousStatesChanged = 0;
            _fmi20EventInfo.nextEventTimeDefined = 0;
            _fmi20EventInfo.nextEventTime = -1.0;
        }

        // Check event indicator for time event
        _newDiscreteStatesNeeded(_fmi20EventInfo);
    }

    /**
     * Evaluate directional derivative with respect to inputs.
     *
     * @param idx The input index.
     * @param inputDerivatives The input derivatives.
     * @return The directional derivative with respect to inputs.
     * @exception IllegalActionException If an error when getting directional derivatives.
     */
    private double _evaluateInputDirectionalDerivatives(final int idx,
            final double[] inputDerivatives) throws IllegalActionException {
        if (!_useRawJNI()) {
            final FMI20ContinuousStateDerivative stateDerivative = _fmiModelDescription.continuousStateDerivatives
                    .get(idx);
            final int numDepInputs = stateDerivative.dependentInputIndexes
                    .size();
            final IntBuffer valueReferenceStateDerivative = IntBuffer.allocate(
                    1).put(0,
                    (int) stateDerivative.scalarVariable.valueReference);
            final DoubleBuffer valueInput = DoubleBuffer.allocate(1).put(0,
                    (double) (1.0));
            final DoubleBuffer valueStateDerivative = DoubleBuffer.allocate(1);
            double jacobianInputs = 0;

            for (int ii = 0; ii < numDepInputs; ++ii) {
                final int curIdx = stateDerivative.dependentInputIndexes
                        .get(ii);
                final Input input = _inputs.get(curIdx);
                final IntBuffer valueReferenceInput = IntBuffer.allocate(1)
                        .put(0, (int) input.scalarVariable.valueReference);
                // FIXME: The calling order of _fmiGetDirectionalDerivative is not according to the
                // Standard. This was modified to accommodate Dymola 2015 FD01's FMUs which have a wrong calling
                // order. Dassault Systems was informed and might fix this in Dymola 2016.
                // The current calling order is:
                //_fmiGetDirectionalDerivative(_fmiComponent, vKnownRef, 1, vUnKnownRef, 1, dvKnown, dvUnknown);
                // The correct calling order should be:
                //_fmiGetDirectionalDerivative(_fmiComponent, vUnknownRef, 1, vKnownRef, 1, dvKnown, dvUnknown);
                int fmiFlag = ((Integer) _fmiGetDirectionalDerivativeFunction
                        .invoke(Integer.class, new Object[] { _fmiComponent,
                                valueReferenceInput, new NativeSizeT(1),
                                valueReferenceStateDerivative,
                                new NativeSizeT(1), valueInput,
                                valueStateDerivative })).intValue();
                if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                    throw new IllegalActionException(
                            this,
                            "Failed to get directional derivatives in "
                                    + "_evaluateInputDirectionalDerivatives. fmiFlag = "
                                    + _fmiStatusDescription(fmiFlag));
                }
                jacobianInputs = jacobianInputs + valueStateDerivative.get(0)
                        * inputDerivatives[curIdx];
            }
            return jacobianInputs;
        } else {
            return 0.0;
        }
    }

    /**
     * Evaluate directional derivative with respect to states.
     *
     * @param idx The state derivative index.
     * @param stateDerivatives The state derivatives.
     * @return The directional derivative with respect to states.
     * @exception IllegalActionException If an error when getting directional derivatives.
     */
    private double _evaluateStateDirectionalDerivatives(final int idx,
            final double[] stateDerivatives) throws IllegalActionException {
        if (!_useRawJNI()) {
            final FMI20ContinuousStateDerivative stateDerivative = _fmiModelDescription.continuousStateDerivatives
                    .get(idx);
            final int numDepStates = stateDerivative.dependentStateIndexes
                    .size();
            final IntBuffer valueReferenceStateDerivative = IntBuffer.allocate(
                    1).put(0,
                    (int) stateDerivative.scalarVariable.valueReference);
            final DoubleBuffer valueState = DoubleBuffer.allocate(1).put(0,
                    (double) (1.0));
            final DoubleBuffer valueStateDerivative = DoubleBuffer.allocate(1);
            double jacobianStates = 0.0;

            for (int ii = 0; ii < numDepStates; ++ii) {
                final int curIdx = stateDerivative.dependentStateIndexes
                        .get(ii);
                final ContinuousState state = _fmiModelDescription.continuousStates
                        .get(curIdx);
                final IntBuffer valueReferenceState = IntBuffer.allocate(1)
                        .put(0, (int) state.scalarVariable.valueReference);
                // FIXME: The calling order of _fmiGetDirectionalDerivative is not according to the
                // Standard. This was modified to accommodate Dymola 2015 FD01's FMUs which have a wrong calling
                // order. Dassault Systems was informed and might fix this in Dymola 2016.
                // The current calling order is:
                //_fmiGetDirectionalDerivative(_fmiComponent, vKnownRef, 1, vUnKnownRef, 1, dvKnown, dvUnknown);
                // The correct calling order should be:
                //_fmiGetDirectionalDerivative(_fmiComponent, vUnknownRef, 1, vKnownRef, 1, dvKnown, dvUnknown);
                int fmiFlag = ((Integer) _fmiGetDirectionalDerivativeFunction
                        .invoke(Integer.class, new Object[] { _fmiComponent,
                                valueReferenceState, new NativeSizeT(1),
                                valueReferenceStateDerivative,
                                new NativeSizeT(1), valueState,
                                valueStateDerivative })).intValue();
                if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                    throw new IllegalActionException(
                            this,
                            "Failed to get directional derivatives in "
                                    + "_evaluateStateDirectionalDerivatives. fmiFlag = "
                                    + _fmiStatusDescription(fmiFlag));
                }
                jacobianStates = jacobianStates + valueStateDerivative.get(0)
                        * stateDerivatives[curIdx];
            }
            return jacobianStates;
        } else {
            return 0.0;
        }
    }

    /**
     * Return the derivatives of the continuous states provided by the FMU.
     *
     * @param numberOfStates The number of continuous states.
     * @param derivatives The state derivatives.
     * @return The state derivatives.
     * @exception IllegalActionException If the FMU does not return fmiOK.
     */
    private void _fmiGetDerivatives(int numberOfStates, double[] derivatives)
            throws IllegalActionException {
        // Evaluate the derivative function.
        if (_debugging) {
            _debugToStdOut("Evaluate the derivatives to "
                    + getDirector().getModelTime());
        }
        final int fmiFlag = ((Integer) _fmiGetDerivativesFunction.invoke(
                Integer.class, new Object[] { _fmiComponent, derivatives,
                        new NativeSizeT(numberOfStates) })).intValue();
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to get derivatives. fmiFlag = "
                            + _fmiStatusDescription(fmiFlag));
        }
    }

    /**
     * Get the event indicator at the model time
     *
     * @param eventIndicator The event indicator.
     * @param number The number of event indicators.
     * @param time The time to evaluate the event indicators.
     * @param stateVariables The state variables.
     * @param inputVariables The input variables.
     * @exception IllegalActionException If the fmiGetEventIndicators
     * function is missing, or if calling it does not return fmiOK.
     */
    private void _getEventIndicators(double[] eventIndicator, int number,
            Time time, double[] stateVariables, double[] inputVariables)
            throws IllegalActionException {

        // Only run this code if event indicator is greater than 0.
        if (number > 0) {

            // Set the time in the FMU.
            _fmiSetTime(time);

            // Enter event mode.
            _enterEventMode();

            // Give {stateVariable} to the FMU.
            _fmiSetContinuousStates(stateVariables);

            // Update the FMUs inputs if needed.
            if (inputVariables != null) {
                _setFMUInputsAtCurrentTime(time, inputVariables, false);
            } else {
                _setFMUInputsAtCurrentTime(time, inputVariables, true);
            }

            // Get the event indicator.
            _getEventIndicators(eventIndicator, number);

            // Exit event mode.
            _enterDiscreteStateMode();

            // Enter continuous time mode.
            _enterContinuousTimeMode();
        }
    }

    /**
     * Get the event indicator at the model time
     *
     * @param eventIndicator The event indicator.
     * @param number The number of event indicators.
    
     * @exception IllegalActionException If the fmiGetEventIndicators
     * function is missing, or if calling it does not return fmiOK.
     */
    private void _getEventIndicators(double[] eventIndicator, int number)
            throws IllegalActionException {

        int fmiFlag;
        // Only run this code if event indicator is greater than 0.
        if (number > 0) {
            // This code will not work when using JNI
            // as the fmiGetIndicator() is not implemented yet.
            if (!_useRawJNI()) {
                if (_fmiGetEventIndicatorsFunction == null) {
                    throw new IllegalActionException(
                            this,
                            "Could not get the "
                                    + _fmiModelDescription.modelIdentifier
                                    + "_fmiGetEventIndicators"
                                    + "() C function?  Perhaps the .fmu file \""
                                    + fmuFile.asFile()
                                    + "\" does not contain a shared library for the current "
                                    + "platform?  ");
                }

                fmiFlag = ((Integer) _fmiGetEventIndicatorsFunction.invoke(
                        Integer.class, new Object[] { _fmiComponent,
                                eventIndicator, new NativeSizeT(number) }))
                        .intValue();

                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new IllegalActionException(this,
                            "Failed to get event indicators"
                                    + ", return result was "
                                    + _fmiStatusDescription(fmiFlag));
                }
            } else {
                _fmiGetEventIndicatorsJNI(eventIndicator);
            }
        }
    }

    /**
     * Get the index of input and state variables from model variables.
     */
    private void _getIndexOfInputsAndContinuousStates() {
        final int numContStates = _fmiModelDescription.numberOfContinuousStates;
        for (int i = 0; i < numContStates; i++) {
            final ContinuousState contState = _fmiModelDescription.continuousStates
                    .get(i);
            // Record the index of the continuous state variable in the model variable list.
            _modelVariableIndexesOfInputsAndContinuousStates.put(
                    contState.name, _fmiModelDescription.modelVariablesNames
                            .indexOf(contState.name));
        }
        for (int i = 0; i < _inputs.size(); i++) {
            // Record the index of the input variable in the model variable list.
            final Input input = _inputs.get(i);
            _modelVariableIndexesOfInputsAndContinuousStates.put(
                    input.scalarVariable.name,
                    _fmiModelDescription.modelVariablesNames
                            .indexOf(input.scalarVariable.name));
        }
    }

    /**
     * Get the next time event if defined.
     *
     * @param currentTime The current time.
     * @exception IllegalActionException If an error occurs when handling events.
     */
    private Time _getNextEventTime() throws IllegalActionException {
        if (!_useRawJNI()) {
            // Save the last retrieved event time
            if (_fmi20EventInfo.nextEventTimeDefined == 1) {
                final Time newEventTime = new Time(_director,
                        _fmi20EventInfo.nextEventTime);
                // Check if we got a distinct new event time.
                if (newEventTime.compareTo(_lastNextEventTime) != 0) {
                    return newEventTime;
                } else {
                    // no new event time
                    return _lastNextEventTime;
                }
            } else {
                // no event time defined
                return new Time(_director, Double.POSITIVE_INFINITY);
            }
        } else {
            // Save the last retrieved event time
            if (_eventInfoJNI.nextEventTimeDefined == 1) {
                final Time newEventTime = new Time(_director,
                        _eventInfoJNI.nextEventTime);
                // Check if we got a distinct new event time.
                if (newEventTime.compareTo(_lastNextEventTime) != 0) {
                    return newEventTime;
                } else {
                    // no new event time
                    return _lastNextEventTime;
                }
            }
            // no event time defined
            return new Time(_director, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * Get the indexes of the dependent inputs and continuous state variables.
     */
    private void _getStateDerivativesDependenciesIndexes() {
        // Get the number of continuous states.
        final int numContStates = _fmiModelDescription.numberOfContinuousStates;
        // Initialize arrays
        for (int i = 0; i < numContStates; i++) {
            // Initialize the lists.
            _fmiModelDescription.continuousStateDerivatives.get(i).dependentInputIndexes = new LinkedList<Integer>();
            _fmiModelDescription.continuousStateDerivatives.get(i).dependentStateIndexes = new LinkedList<Integer>();
            final FMI20ContinuousStateDerivative stateDerivative = _fmiModelDescription.continuousStateDerivatives
                    .get(i);
            // Get the indexes of the dependent input variables.
            for (int j = 0; j < _inputs.size(); j++) {
                final Input input = _inputs.get(j);
                if (stateDerivative.dependentScalarVariables
                        .contains(input.scalarVariable)) {
                    final int index = _inputs.indexOf(input);
                    _fmiModelDescription.continuousStateDerivatives.get(i).dependentInputIndexes
                            .add(index);
                }
            }
            // Get the indexes of dependent continuous state variables.
            for (int j = 0; j < numContStates; j++) {
                final ContinuousState state = _fmiModelDescription.continuousStates
                        .get(j);
                if (stateDerivative.dependentScalarVariables
                        .contains(state.scalarVariable)) {
                    final int index = _fmiModelDescription.continuousStates
                            .indexOf(state);
                    _fmiModelDescription.continuousStateDerivatives.get(i).dependentStateIndexes
                            .add(index);
                }
            }

        }
    }

    /** If an input is present, read it. If its value and time do not
     *  match that of the most recently seen input, then set the input
     *  variable model of the solver using the provided input data.
     *  @param input The input to be set.
     *  @param currentTime The current simulation time.
     *  @param token The token read from the port
     *  @param curIdx The input index.
     *  @exception IllegalActionException If the input cannot be set.
     */
    private boolean _handleInput(Input input, Time currentTime, Token token,
            int curIdx) throws IllegalActionException {
        // Here we have gotten a SmoothToken which has derivatives information. We assume
        // this token to be a quantized state since it does contain value and non-zero
        // derivatives.
        // Handle cases where we have a smooth token with non-zero derivatives.
        if (token instanceof SmoothToken
                && (((SmoothToken) token).derivativeValues() != null)) {
            // Update the model.
            _updateInputModel(input, currentTime, token, curIdx);
            return true;
        }
        // Handle cases where we have a smooth token with zero derivatives.
        else if (token instanceof SmoothToken
                && (((SmoothToken) token).derivativeValues() == null)) {
            final double inputDoubleValue = ((SmoothToken) token).doubleValue();
            if (inputDoubleValue != input.lastInputPortValue) {
                // Update the model.
                _updateInputModel(input, currentTime, token, curIdx);
                // Save the last double seen at this port
                _inputs.get(curIdx).lastInputPortValue = inputDoubleValue;
                return true;
            }
        }
        // Handle cases where we have a double token.
        else if (token instanceof DoubleToken) {
            final double inputDoubleValue = ((DoubleToken) token).doubleValue();
            if (inputDoubleValue != input.lastInputPortValue) {
                // Update the model.
                _updateInputModel(input, currentTime, token, curIdx);
                // Save the last double seen at this port.
                _inputs.get(curIdx).lastInputPortValue = inputDoubleValue;
                return true;
            }
        } else if (!(token instanceof SmoothToken)
                && !(token instanceof DoubleToken)) {
            {
                throw new IllegalActionException(this, String.format(
                        "Input port %s is connected to a port which has"
                                + " a type which is not an instance "
                                + "of SmoothToken or DoubleToken.",
                        input.port.getName()));
            }
        }
        return false;
    }

    /**
     * Initialize the values of the continuous state ports with their
     * dependencies and remove the direct dependency of states on inputs.
     *
     * @exception IllegalActionException If no port matching the name of
     * a variable declared as an input is found.
     */
    private void _initializeContinuousStates() throws IllegalActionException {
        // Get the number of continuous states.
        final int numContStates = _fmiModelDescription.numberOfContinuousStates;
        // Initialize arrays
        _stateValueReferences = new long[numContStates];
        _stateDerivativeValueReferences = new long[numContStates];
        for (int i = 0; i < numContStates; i++) {
            _fmiModelDescription.continuousStates.get(i).port = (TypedIOPort) _getPortByNameOrDisplayName(_fmiModelDescription.continuousStates
                    .get(i).scalarVariable.name);
            final ContinuousState contState = _fmiModelDescription.continuousStates
                    .get(i);
            final FMI20ContinuousStateDerivative contStateDeriv = _fmiModelDescription.continuousStateDerivatives
                    .get(i);
            // Initialize vector of value references of state variables
            _stateValueReferences[i] = contState.scalarVariable.valueReference;
            // Initialize vector of value references of derivatives of state variables.
            _stateDerivativeValueReferences[i] = contStateDeriv.scalarVariable.valueReference;

            // Get the output retrieved from the model structure.
            Set<TypedIOPort> dependencies = null;
            for (int j = 0; j < contState.dependentScalarVariables.size(); j++) {
                final String inputName = contState.dependentScalarVariables
                        .get(j).name;
                final TypedIOPort inputPort = (TypedIOPort) _getPortByNameOrDisplayName(inputName);
                if (inputPort == null) {
                    continue;
                }
                if (dependencies == null) {
                    dependencies = new HashSet<TypedIOPort>();
                }
                dependencies.add(inputPort);
            }
        }

        // Iterate through the continuous state ports and remove their
        // dependencies with respect to inputs. There are no direct dependencies
        // between the continuous state ports and the input ports by default.
        for (int i = 0; i < _fmiModelDescription.numberOfContinuousStates; i++) {
            IOPort port = _fmiModelDescription.continuousStates.get(i).port;
            for (Input input : _getInputs()) {
                // Remove the dependency of the state output on the actor inputs
                _declareDelayDependency(input.port, port, 0.0);
            }
        }
    }

    /**
     * Initialize variable from type parameters.
     *
     * @exception IllegalActionException If the FMU cannot be initialized.
     */
    private void _initializeFMUParameters() throws IllegalActionException {

        // Set the parameters of the FMU.
        // Loop through the scalar variables and find a scalar
        // variable that has variability == "parameter" and is not an
        // input or output. We can't do this in attributeChanged()
        // because setting a scalar variable requires that
        // _fmiComponent be non-null, which happens in
        // preinitialize();
        // FIXME: This should probably also be done in attributeChanged(),
        // with checks that _fmiComponent is non-null, so that FMU parameters
        // can be changed during run time.
        for (FMIScalarVariable scalar : _fmiModelDescription.modelVariables) {
            if ((scalar.variability == FMIScalarVariable.Variability.parameter
                    || scalar.variability == FMIScalarVariable.Variability.fixed
                    || scalar.variability == FMIScalarVariable.Variability.tunable) // FMI-2.0rc1
                    && scalar.causality != Causality.local // FMI-2.0rc1
                    && scalar.causality != Causality.input
                    && scalar.causality != Causality.output) {
                String sanitizedName = StringUtilities
                        .sanitizeName(scalar.name);
                Parameter parameter = (Parameter) getAttribute(sanitizedName,
                        Parameter.class);
                if (parameter != null) {
                    try {
                        if (_useRawJNI()) {
                            if (scalar.type instanceof FMIRealType) {
                                final double tmp_uu[] = { ((DoubleToken) parameter
                                        .getToken()).doubleValue() };
                                final long tmp_uuRef[] = { scalar.valueReference };
                                // Set the inputs which have changed.
                                _fmiSetRealJNI(tmp_uu, tmp_uuRef);

                            } else {
                                throw new IllegalActionException(
                                        this,
                                        "useRawJNI is true, "
                                                + "but but only doubles are suported. "
                                                + "the type was " + scalar.type);
                            }

                        } else {
                            _setFMUScalarVariable(scalar, parameter.getToken());
                        }
                    } catch (IllegalActionException ex) {
                        throw new IllegalActionException(this, "Failed to set "
                                + scalar.name + " to " + parameter.getToken());
                    } catch (RuntimeException runtimeException) {
                        // FIXME: we are reusing supressWarnings here
                        // because the AMS model throws an exception
                        // while trying to set hx.hc.
                        if (!((BooleanToken) suppressWarnings.getToken())
                                .booleanValue()) {
                            throw new IllegalActionException(
                                    this,
                                    runtimeException,
                                    "Failed to set "
                                            + scalar.name
                                            + " to "
                                            + parameter.getToken()
                                            + ".  To ignore this exception, set the supressWarnings parameter.");
                        }
                    }
                }
            }
        }
    }

    /**
    * Configure the QSS integrator's input variable models.
    *
    * <p>This cannot be done during initialization stage, because did not yet have
    * all the information needed.</p>
    *
    * @param currentTime The current simulation time.
    */
    private final void _initializeQSSIntegratorInputVariables(
            final Time currentTime) throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String
                    .format("FMUQSS._initializeQssIntegrator_inputVars() on id{%d} at time %s",
                            System.identityHashCode(this),
                            currentTime.toString()));
        }

        // Create array for input models.
        final int ivCt = _qssSolver.getInputVariableCount();
        if (ivCt > 0) {
            _inputVariableModels = new ModelPolynomial[ivCt];
            _inputVariableValues = new double[ivCt];
        }

        // Create input variable models.
        // Note this code block may have to change location at some point.
        // Reason-- when input variable models get variable order, will want to
        // read the model order from the model. Can't do that until have tokens.
        // But don't have tokens yet at this point in the initialization.
        final int ivMdlOrder = _qssSolver.getStateModelOrder();
        for (int ii = 0; ii < ivCt; ++ii) {
            // Create the model.
            // Note the port may not have a token on it yet. Therefore,
            // while can create the model, can't initialize its time or value.
            final ModelPolynomial ivMdl = new ModelPolynomial(ivMdlOrder);
            _inputVariableModels[ii] = ivMdl;
            // ivMdl.tMdl = currentTime; // TODO: Confirm where time set.
            ivMdl.claimWriteAccess();
            // Give model to the integrator.
            _qssSolver.setInputVariableModel(ii, ivMdl);
        }

        // Load input variable models with data.
        for (int ii = 0; ii < ivCt; ++ii) {
            // Get the input corresponding to input variable {ii}.
            final Input input = _inputs.get(ii);

            // Get the model.
            // We only initialize the first coefficient
            // since we don't have other coefficients yet.
            final ModelPolynomial ivMdl = _inputVariableModels[ii];
            double initialValue;
            // Look for a token.
            if (input.port.hasNewToken(0)) {
                final Token token = input.port.get(0);
                if (token instanceof SmoothToken) {
                    initialValue = ((SmoothToken) token).doubleValue();
                    // Initialize model with input derivatives
                    double[] derivatives = ((SmoothToken) token)
                            .derivativeValues();
                    if (derivatives != null) {
                        int factorial = 1;
                        for (int i = 1; i <= derivatives.length; i++) {
                            ivMdl.coeffs[i] = derivatives[i - 1] / factorial;
                            factorial = factorial * i;
                        }
                    }
                } else if (token instanceof DoubleToken) {
                    initialValue = ((DoubleToken) token).doubleValue();
                } else {
                    throw new IllegalActionException(this, String.format(
                            "Input port %s is connected to a port which has"
                                    + " a type which is not an instance "
                                    + "of SmoothToken or DoubleToken.",
                            input.port.getName()));
                }
            } else {
                // Here, missing a token.
                // TODO: This means there may be an algebraic loop. Need
                // to handle that as a real possibility.
                // Fill in a default value, taken from the FMU.
                // initialValue = input.scalarVariable.getDouble(_fmiComponent);
                initialValue = input.start;
                if (_debugging) {
                    _debugToStdOut(String
                            .format("-- Id{%d} set initial value of input %d to default value of %g",
                                    System.identityHashCode(this), ii,
                                    initialValue));
                }
            }
            // Save the last double token seen at this port.
            _inputs.get(ii).lastInputPortValue = initialValue;
            ivMdl.coeffs[0] = initialValue;
            ivMdl.tMdl = currentTime;
        }

        // Validate the integrator.
        final String failMsg = _qssSolver.validate();
        if (null != failMsg) {
            throw new IllegalActionException(this, failMsg);
        }

        // Set up integrator's state models.
        // TODO: Not sure this is necessary.
        _triggerQuantizationEvents(currentTime, false);
        _triggerQuantizationEventsEventIndicator(currentTime, false);
        try {
            // TODO: This may not be allowed-- rate events change internal
            // state,
            // which is meant to happen in postfire(). ??? Check Ptolemy user
            // guide.
            _qssSolver.triggerRateEvent();
            // This needs to be done for event detection too
            if (getEventIndicatorCount() > 0) {
                _zcSolver.triggerRateEvent(getEventIndicatorCount());
            }

        } catch (Exception ee) {
            // Rethrow as an IllegalActionException.
            throw new IllegalActionException(this, ee,
                    "Triggering rate event failed.");
        }

    }

    /**
     * Produce FMU outputs that are not quantized states.
     *
     * <p>FMU can produce outputs that don't correspond to states.</p>
     *
     * @param currentTime The current simulation time.
    
     */
    private final void _produceOutputs(final Time currentTime)
            throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._produceOutputs() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }
        // FIXME: This code will only work with doubles for now.
        for (int ii = 0; ii < _outputs.size(); ii++) {
            final Output output = _outputs.get(ii);

            final TypedIOPort port = output.port;
            // Skip known outputs and outputs which are states.
            if (_skipIfKnown() || output.scalarVariable.isState) {
                continue;
            }
            FMIScalarVariable scalarVariable = output.scalarVariable;
            if (!(scalarVariable.type instanceof FMIRealType)) {
                new Exception(
                        "Warning: The output variable "
                                + scalarVariable.name
                                + " is of type "
                                + scalarVariable.type
                                + " .But only variables from type FMIRealType are updated.")
                        .printStackTrace();
                continue;
            } else {
                final double[] oo = new double[1];
                if (!_useRawJNI()) {
                    double result = scalarVariable.getDouble(_fmiComponent);
                    oo[0] = result;
                } else {
                    final long[] ooRef = { scalarVariable.valueReference };
                    _fmiGetRealJNI(oo, ooRef);
                }
                // Send the initial output values to the port.
                _sendModelToPort(oo, port, currentTime, ii, false);
            }
        }
    }

    /**
     * Send model to a port.
     *
     * @param val The values to be sent.
     * @param prt The port where values will be sent.
     * @param time The time when value is sent.
     * @param isState The parameter to identify state variable
     */
    private void _sendModel(final double[] val, final TypedIOPort prt,
            Time time, boolean isState) throws NoRoomException,
            IllegalActionException {
        // Send model to a port which is a state.
        if (isState) {
            prt.send(0, new SmoothToken(val, time));
        }
        // Send model to a port which is a regular output.
        else {
            prt.send(0, new DoubleToken(val[0]));
        }
    }

    /**
     * Send output and state model to the port.
     *
     * @param val The values to be sent.
     * @param prt The port where values will be sent.
     * @param index The index of an output or continuous state.
     * @param isState The flag to indicate if continuous state.
     */
    private void _sendModelToPort(final double[] val, final TypedIOPort prt,
            Time time, int index, boolean isState) throws NoRoomException,
            IllegalActionException {

        // Handle outputs which are states.
        // The code below has been commented out. The problem is that 
        // when we check whether the quantum has been crossed for the isState case, and we see that this is not 
        // the case, the code should not do send any new data, instead the last seen value should be used.
        // Unfortunately, the values that are sent are smooth token which can be extrapolated.
        // This causes the code to use wrong values for computation leading to different results.
        // The drawback of current code is that it will send values if we have finer discretization,
        // which is done by using a quantumScaleFactor with  a value less than 1.0.
        // One way to solve this problem, might be to see whether we should send a different type of
        // token when the quantum is not crossed. A token which is persistent but is not extrapolated.
        if (isState) {
            //final int modVarIdx = _modelVariableIndexesOfInputsAndContinuousStates
            //        .get(_fmiModelDescription.continuousStates.get(index).name);
            // We should only use the first coefficient of the quantized state model
            // since since higher order terms are zero at quantization time.
            _sendModel(val, prt, time, isState);
            /*if (_firstRound) {
            	_sendModel(val, prt, time, isState );
                _fmiModelDescription.continuousStates.get(index).lastDoubleOutput = val[0];
                _fmiModelDescription.continuousStates.get(index).quantum = 
                		_qssSolver.findQuantum(index) /_quantumScaleFactor;
                // Update model variable hasChanged field.
                //_updateModelVariableAttribute(modVarIdx, true);
            } else {
            	 double lastqSta = _fmiModelDescription.continuousStates.get(index).lastDoubleOutput;
                if (Math.abs(val[0] - lastqSta) <= Math.abs(_fmiModelDescription.continuousStates.get(index).quantum)) {
                    // Update model variable hasChanged field.
                    //_updateModelVariableAttribute(modVarIdx, false);
                    return;
                } else {
                	_sendModel(val, prt, time, isState );
                    // Update model variable hasChanged field.
                    //_updateModelVariableAttribute(modVarIdx, true);
                    _fmiModelDescription.continuousStates.get(index).lastDoubleOutput = val[0];
                    _fmiModelDescription.continuousStates.get(index).quantum = 
                    		_qssSolver.findQuantum(index) /_quantumScaleFactor;
                }
            }*/
        }
        // Handle outputs which are not states.
        else {
            if (_firstRound
                    || Math.abs(val[0]
                            - _outputs.get(index).lastOutputPortValue) > _threshold) {
                _sendModel(val, prt, time, isState);
                _outputs.get(index).lastOutputPortValue = val[0];
            }
            /*	        
              if (_firstRound) {
                	_sendModel(val, prt, time, isState);
                    _outputs.get(index).lastOutputPortValue = val[0];
                    _outputs.get(index).quantum = Math.abs(_threshold 
                    		* _outputs.get(index).lastOutputPortValue);
                } else {
                	double lastDblOut = _outputs.get(index).lastOutputPortValue;
                    if (Math.abs(val[0] - lastDblOut) > Math.abs(_outputs.get(index).quantum)) {
                        // Check if any of output dependents has changed.
                        // If not then do not commit any changes. This is particularly
                        // important for outputs which are function of states.
                        // For instance, suppose y = x_w and x_w is scheduled to have a
                        // quantization event at the end of the simulation. However, in the current
                        // code every time we called trigger quantization events, we also updated the
                        // continuous states using the continuous state model. this in turn will
                        // change x_w causing it to produce outputs at a rate which is different
                        // from the quantized model. therefore, by using following method,
                        // which checks whether dependents have changed, we can capture that.
                        //if (_cancelSendModelToPort(index))
                        //    return;
                        _sendModel(val, prt, time, isState);
                        _outputs.get(index).lastOutputPortValue = val[0];
                        _outputs.get(index).quantum = Math.abs(_threshold 
                        		* _outputs.get(index).lastOutputPortValue);
                    }
                }
                */
        }

    }

    /** Set the FMU inputs at current time.
     *  @param currentTime The current model time.
     *  @param inpuVariables The input variable values
     *  @param evaluate A flag to re-evaluate the input at current time.
     *  @exception IllegalActionException If the specified token cannot be converted
     *   to a double.
     */
    private void _setFMUInputsAtCurrentTime(Time currentTime,
            double[] inputVariables, boolean evaluate)
            throws IllegalActionException {
        // Convert to a DoubleToken. If token is a SmoothToken or DoubleToken,
        // then the convert method does nothing and just returns the token.
        // Otherwise, it attempts to convert it to a DoubleToken, and throws
        // an exception if such conversion is not possible.
        int curIdx = -1;
        double timeValue = currentTime.getDoubleValue();
        for (Input input : _inputs) {
            curIdx++;
            // Note: In higher order QSS, higher order derivatives are approximated.
            // This happens by evaluating the FMU at two time instants.
            // The consequence of this approach is that inputs of the FMU are computed
            // at a time that might be different from the time when the FMUs should
            // produce outputs. To ensure that inputs of the FMUs are in sync with outputs
            // we use this function to evaluate the inputs at the time when outputs are produced.
            // This happens when this function is called with evalaute set to true.
            final FMIScalarVariable scalar = input.scalarVariable;
            if (!(scalar.type instanceof FMIRealType)) {
                new Exception(
                        "Warning: The input variable "
                                + scalar.name
                                + " is of type "
                                + scalar.type
                                + " .But only variables from type FMIRealType are updated.")
                        .printStackTrace();
                continue;
            }
            if (evaluate) {
                ModelPolynomial ivMdl = _qssSolver
                        .getInputVariableModel(curIdx);
                // Check if the input is a smooth token. This can be done by
                // checking whether whether higher order coefficients are zero.
                int sum = 0;
                for (int i = 1; i < ivMdl.coeffs.length; i++) {
                    sum += ivMdl.coeffs[i];
                }

                // Reevaluate inputs which are smooth token.
                if (sum != 0) {
                    // Reevaluate the input model at the current time.
                    double smoothTokenInputVariable = _qssSolver
                            .getInputVariableModel(curIdx)
                            .evaluate(currentTime);
                    // Set the inputs of the FMUs which are smooth token.
                    _setRealInputs(timeValue, scalar, smoothTokenInputVariable,
                            _inputValueReferences[curIdx], curIdx);
                }
            } else {
                // Set the inputs of the FMUs.
                _setRealInputs(timeValue, scalar, inputVariables[curIdx],
                        _inputValueReferences[curIdx], curIdx);
            }
        }
    }

    /** Populate the specified model with data from the specified token.
    *  If the token is a SmoothToken, then insert in the model any derivatives
    *  that might be given in the token, and set any remaining derivatives
    *  required by the model to zero. Otherwise, set any derivatives
    *  required by the model to zero.
    *  @param ivMdl The input model to parameterize.
    *  @param token The token values for parameterization.
    *  @exception IllegalActionException If the specified token cannot be converted
    *   to a double.
    */
    private void _setModelFromToken(ModelPolynomial ivMdl, Token token)
            throws IllegalActionException {

        // Convert to a DoubleToken. If token is a SmoothToken or DoubleToken,
        // then the convert method does nothing and just returns the token.
        // Otherwise, it attempts to convert it to a DoubleToken, and throws
        // an exception if such conversion is not possible.
        DoubleToken doubleToken = DoubleToken.convert(token);

        // In all cases, the first coefficient is simply the current value of
        // the token.
        ivMdl.coeffs[0] = doubleToken.doubleValue();

        double[] derivatives = null;
        if (doubleToken instanceof SmoothToken) {
            derivatives = ((SmoothToken) token).derivativeValues();
            final int ncoeffs = ivMdl.coeffs.length;
            int factorial = 1;
            for (int i = 1; i < ncoeffs; i++) {
                if (derivatives == null || derivatives.length < i) {
                    ivMdl.coeffs[i] = 0.0;
                } else {
                    ivMdl.coeffs[i] = derivatives[i - 1] / factorial;
                    factorial = factorial * i;
                }
            }
        }
    }

    /**
     * Set the FMU inputs at current time.
     * 
     * @param timeValue The current time in double.
     * @param scalar The scalar variable.
     * @param value The scalar variable value.
     * @param valueReference The scalar variable value reference.
     * @param curIdx The scalar input index.
     * @exception IllegalActionException If the specified token cannot be converted to a double.
     */
    private void _setRealInputs(double timeValue, FMIScalarVariable scalar,
            double value, long valueReference, int curIdx)
            throws IllegalActionException {
        if (_firstRound
                || Math.abs((_inputs.get(curIdx).lastInputModelValue - value)) > _threshold) {
            if (!_useRawJNI()) {
                scalar.setDouble(_fmiComponent, value);
            } else {
                final double uu[] = { value };
                final long uuRef[] = { valueReference };
                _fmiSetRealJNI(uu, uuRef);
            }
            _inputs.get(curIdx).lastInputModelValue = value;

        }
    }

    /**
     * Trigger quantization-events if necessary.
     *
     * <p>Update the external, quantized state models.</p>
     *
     * @param currentTime The current simulation time.
     * @param forceAll If true, re-quantize all state models.
     */
    private final void _triggerQuantizationEvents(final Time currentTime,
            final boolean forceAll) throws IllegalActionException {

        // Initialize.
        final int stateCt = _qssSolver.getStateCount();
        boolean needQuantizationEvents = false;

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._triggerQuantEvts() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Loop over states that need to be re-quantized.
        int qIdx = -1;
        while (true) {

            // Get next state.
            if (forceAll) {
                qIdx++;
                if (qIdx >= stateCt) {
                    break;
                }
            } else {
                qIdx = _qssSolver.needQuantizationEventIndex();
                if (qIdx < 0) {
                    break;
                }
            }

            // Re-quantize the state.
            _qssSolver.triggerQuantizationEvent(qIdx);

            // Export to rest of simulation.
            // TODO: Convert this to export models, not just values.
            final TypedIOPort outPort = _fmiModelDescription.continuousStates
                    .get(qIdx).port;

            // Signalize a quantization event.
            needQuantizationEvents = true;
            // Only update the variables which have changed
            _stateVariables[qIdx] = _qssSolver.getStateModel(qIdx).coeffs[0];

            // Send model to the port
            if (outPort.getWidth() > 0) {
                _sendModelToPort(_qssSolver.getStateModel(qIdx).coeffs,
                        outPort, currentTime, qIdx, true);
            }

            // Diagnostic output.
            if (_debugging) {
                _debugToStdOut(String.format(
                        "-- Id{%d} set quantized state model %d to %s", System
                                .identityHashCode(this), qIdx, _qssSolver
                                .getStateModel(qIdx).toString()));
            }
        }

        // Check if any state variable had a quantization event. If true
        // then update the continuous states in the FMU before updating the outputs.
        // This is needed particularly if an output depends on a state.

        // Set the time in the FMU
        _fmiSetTime(currentTime);

        // A quantization-event implies other FMU outputs change.
        // Produce outputs to all outputs that do not depend on the states.

        // Enter event mode.
        _enterEventMode();

        if (_firstRound || needQuantizationEvents) {
            for (int i = 0; i < stateCt; i++) {
                _stateVariables[i] = _qssSolver.evaluateStateModelContinuous(i,
                        currentTime);
            }
            _fmiSetContinuousStates(_stateVariables);
        }

        // Update the inputs at the current time so the outputs get the correct values
        // This is particularly important if an output depends on an input.
        _setFMUInputsAtCurrentTime(currentTime, null, true);

        // Exit event mode.
        _enterDiscreteStateMode();

        // Get an updated time event if defined
        _lastNextEventTime = _getNextEventTime();

        // Produce outputs
        _produceOutputs(currentTime);

        // Enter continuous time mode.
        _enterContinuousTimeMode();

        // Reset hasChanged to false for next quantization events.
        // for (int i = 0; i < _indexesOfUpdatedModelVariables.size(); i++) {
        //    _fmiModelDescription.modelVariables
        //            .get(_indexesOfUpdatedModelVariables.get(i)).hasChanged = false;
        // }

        // Clear the list of model variables indexes so
        // they can be used in the next quantization.
        // _indexesOfUpdatedModelVariables.clear();
    }

    /**
     * Trigger quantization-events for event indicator if necessary.
     *
     * <p>Update the external, quantized state models.</p>
     *
     * @param currentTime The current simulation time.
     * @param forceAll If true, requantize all state models.
     */
    private final void _triggerQuantizationEventsEventIndicator(
            final Time currentTime, final boolean forceAll)
            throws IllegalActionException {

        if (getEventIndicatorCount() > 0) {
            // Initialize.
            final int stateCt = _zcSolver.getStateCount();

            if (_debugging) {
                _debugToStdOut(String.format(
                        "FMUQSS._triggerQuantEvts() on id{%d} at time %s",
                        System.identityHashCode(this), currentTime.toString()));
            }

            // Loop over states that need to be re-quantized.
            int qIdx = -1;
            while (true) {

                // Get next state.
                if (forceAll) {
                    qIdx++;
                    if (qIdx >= stateCt) {
                        break;
                    }
                } else {
                    qIdx = _zcSolver.needQuantizationEventIndex();
                    if (qIdx < 0) {
                        break;
                    }
                }

                // Re-quantize the state.
                _zcSolver.triggerQuantizationEvent(qIdx);

                // Diagnostic output.
                if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} set quantized state model %d to %s",
                            System.identityHashCode(this), qIdx, _zcSolver
                                    .getStateModel(qIdx).toString()));
                }
            }
        }
    }

    /**
     * Trigger a rate-event if necessary.
     *
     * <p>Update the internal, continuous state models.</p>
     *
     * @param currentTime The current simulation time.
     * @param force If true, always trigger a rate-event. Otherwise,
     * only trigger a rate-event if an input changed or if the
     * integrator signals it needs one to satisfy internal logic.
     */
    private final void _triggerRateEvent(final Time currentTime,
            final boolean force, boolean[] trigger)
            throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._triggerRateEvt() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Update the input variable models if necessary.
        int curIdx = -1;
        boolean updatedInputVarMdl = false;
        for (Input input : _inputs) {
            curIdx++;
            // Guarantee that _inputs() has the same count of inputs
            // as it did during _initializeQssIntegrator_inputVars().
            // If not, then index {curIdx} can be wrong.
            if (input.port.hasNewToken(0)) {
                // Here, have a new value on the input port.
                final Token token = input.port.get(0);
                _updateInputModel(input, currentTime, token, curIdx);
                //if (!_handleInput(input, currentTime, token, curIdx) && !_firstRound) continue;
                // Specify input which has changed.
                // final int modVarIdx = _modelVariableIndexesOfInputsAndContinuousStates.get(input.scalarVariable.name);
                // _updateModelVariableAttribute (modVarIdx, true);
                updatedInputVarMdl = true;
            }
        }
        // Make sure that the index matches the number of input variables.
        assert (_qssSolver.getInputVariableCount() == curIdx);

        // Trigger rate-event if necessary.
        if (force || updatedInputVarMdl || _qssSolver.needRateEvent()) {
            if (_debugging) {
                _debugToStdOut(String
                        .format("-- Id{%d} trigger rate-event because force=%b, updatedInputVarMdl=%b, _qssIgr.needRateEvt()=%b",
                                System.identityHashCode(this), force,
                                updatedInputVarMdl, _qssSolver.needRateEvent()));
            }
            try {
                _qssSolver.triggerRateEvent();
                trigger[0] = true;
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee,
                        "Triggering rate event failed.");
            }
        }

    }

    /** Update the input model.
     *  @param input The input to be set.
     *  @param currentTime The current simulation time.
     *  @param token The token read from the port
     *  @param curIdx The input index.
     *  @exception IllegalActionException If the input cannot be set.
     */
    private void _updateInputModel(Input input, Time currentTime, Token token,
            int curIdx) throws IllegalActionException {
        // Update the model.
        final ModelPolynomial ivMdl = _inputVariableModels[curIdx];
        // Set model from token.
        _setModelFromToken(ivMdl, token);
        // Update time.
        ivMdl.tMdl = currentTime;
        if (_debugging) {
            _debugToStdOut(String.format(
                    "-- Id{%d} set input variable model %d to %s",
                    System.identityHashCode(this), curIdx, ivMdl.toString()));
        }
    }

    /**
     * Update the model variable field hasChanged
     * @param index The variable index.
     * @param value The variable value.
     */
    private void _updateModelVariableAttribute(int index, boolean value) {

        // Check if index is positive
        // if yes, then set the hasChanged to be true.
        if (index >= 0) {
            _fmiModelDescription.modelVariables.get(index).hasChanged = value;
        }

        // Save the indexes of the model variables which have changed
        if (index >= 0 && value) {
            _indexesOfUpdatedModelVariables.add(index);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                           private fields                  ////

    /** QSS director. */
    private QSSDirector _director;

    /** Enter event mode for step event. */
    private boolean _forceQuantizationStepEvents;

    /** Buffer for event indicators. */
    private double[] _eventIndicators;

    /** Buffer for event indicators. */
    private double[] _eventIndicatorsDerivatives;

    /** Buffer for event indicators. */
    private double[] _eventIndicatorsDerivatives2;

    /** Buffer for event indicators. */
    private double[] _eventIndicatorsDerivatives3;

    /** Buffer for previous event indicators. */
    private double[] _eventIndicatorsPrevious;

    /** The eventInfo. */
    private FMI20EventInfo _eventInfo;

    /** The eventInfo by reference. */
    private FMI20EventInfo.ByReference _fmi20EventInfo;

    /**
     * Flag identifying the first round of iterations of fire() and postfire()
     * after initialize().
     */
    private boolean _firstRound;

    /** The model instance. */
    private FMI20ModelInstance _fmi20ModelInstance;

    /** Flag to indicate a state event. */
    private boolean _forceQuantizationStateEvents = false;

    /** Flag to indicate a time event. */
    private boolean _forceQuantizationTimeEvents = false;

    /** The outputs of this FMU. */
    private List<Integer> _indexesOfUpdatedModelVariables;

    /** The inputs of this FMU. */
    private List<Input> _inputs;

    /** Models for communicating with the integrator. */
    private ModelPolynomial[] _inputVariableModels;

    /** The new updated states. */
    private double[] _inputVariableValues;

    /** Vector of input value references. */
    private long[] _inputValueReferences;

    /** Internal relative quantum. */
    private double _internalRelativeQuantum;

    /** The delta between sample and current time. */
    private double[] _deltaSample;

    /** Indicate a state event at start. */
    private boolean _hasStateEventAtStart;

    /** Track requests for firing. */
    private Time _lastFireAtTime;

    /** Track last time event time. */
    private Time _lastNextEventTime;

    /** Record of model variable index of continuous states. */
    private Map<String, Integer> _modelVariableIndexesOfInputsAndContinuousStates;

    /** The number of state events in this FMU. */
    private int _numberOfStateEvents;

    /** The number of time events in this FMU. */
    private int _numberOfTimeEvents;

    /** The number of step evens. */
    private int _numberOfStepEvents;

    /** The number of steps. */
    private int _numberOfSteps;

    /** The outputs of this FMU. */
    private List<Output> _outputs;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _qssSolver = null;

    /** Quantum scale factor. */
    private double _quantumScaleFactor;

    /** Vector of state derivative value references. */
    private long[] _stateDerivativeValueReferences;

    /** The new states computed in fire, to be committed in postfire. */
    private double[] _states;

    /** The new updated states. */
    private double[] _stateVariables;

    /** Vector of state value references. */
    private long[] _stateValueReferences;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _zcSolver = null;

}
