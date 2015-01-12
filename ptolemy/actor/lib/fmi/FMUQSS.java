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
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ptolemy.fmi.FMI20ContinuousStateDerivative;
import org.ptolemy.fmi.FMI20EventInfo;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIModelDescription.ContinuousState;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.NativeSizeT;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
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
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.qss.kernel.QSSDirector;
import ptolemy.domains.qss.kernel.QSSToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
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
     * Evaluate the derivative function.
     *
     * @param time The simulation time.
     * @param stateVariables The vector of state variables at <code>time</code>.
     * @param inputVariables The vector of input variables at <code>time</code>.
     * @param stateVariableDerivatives The (output) vector of time rates of change of the state variables 
     * at <code>time</code>.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException
     */
    public final int evaluateDerivatives(final Time time, final double[] stateVariables,
            final double[] inputVariables, final double[] stateVariableDerivatives)
            throws IllegalActionException {

        // Check assumptions.
        assert (getStateCount() > 0);
        assert (stateVariables.length == getStateCount());
        assert (stateVariableDerivatives.length == getStateCount());

        // Give {time} to the FMU.
        final double timeValue = time.getDoubleValue();

        // long startTime = System.nanoTime();
        _fmiSetTime(timeValue);

        // Give {stateVariable} to the FMU.
        _fmiSetContinuousStates(stateVariables);

        // Handle any time, state or step event.
        _handleEvents(timeValue);

        // Give {inputVariable} to the FMU.
        for (int ii = 0; ii < _inputs.size(); ++ii) {
            final FMIScalarVariable scalar = _inputs.get(ii).scalarVariable;
            assert (scalar.type instanceof FMIRealType);
            final Input input = _inputs.get(ii);
            if (!input.hasChanged && !_firstRound) {
                continue;
            }
            scalar.setDouble(_fmiComponent, inputVariables[ii]);
            _inputs.get(ii).hasChanged = false;
        }

        // Give {stateVariableDerivative} to FMU and evaluate the derivative function.
        _fmiGetDerivatives(getStateCount(), stateVariableDerivatives);

        return 0;
    }

    /**
     * Evaluate directional derivative function.
     *
     * @param stateIndex The state index.
     * @param stateVariableDerivatives The vector of state derivatives.
     * @param inputVariableDerivatives The vector of input derivatives.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException If thrown while getting the input directional derivative.
     */
    public final double evaluateDirectionalDerivatives(final int stateIndex,
            final double[] stateVariableDerivatives, final double[] inputVariableDerivatives)
            throws IllegalActionException {
        // Get second derivative of current input
        return (_evaluateInputDirectionalDerivatives(stateIndex, inputVariableDerivatives)
                + _evaluateStateDirectionalDerivatives(
                        stateIndex, stateVariableDerivatives));
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
            int currentMicrostep =  _director.getIndex();
            _debugToStdOut(String.format(
                    "FMUQSS.fire() on id{%d} at time %s, microstep %d",
                    System.identityHashCode(this), currentTime.toString(),
                    currentMicrostep));
        }

        // Assume do not need a quantization-event.
        assert (_qssSolver.needQuantizationEventIndex() == -1);

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
            _triggerQuantizationEvents(currentTime, false);
        }

    }

    /**
     * Return the count of input variables.
     *
     * @return The number of input variables.
     */
    public final int getInputVariableCount() {
        return _inputs.size();
    }

    /**
     * Return the count of state variables.
     * 
     * @return The number of state variables.
     */
    public final int getStateCount() {
        return _fmiModelDescription.numberOfContinuousStates;
    }

    /**
     * Indicate existence of directional derivatives.
     *
     * @return True if directional derivatives are provided.
     */
    public final boolean getProvidesDirectionalDerivatives() {
        return _fmiModelDescription.providesDirectionalDerivative;
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
        // Note that this method is declared to not have a modelExchange
        // parameter because all QSS fmus are model exchange fmus.  However,
        // _importFMU does take a modelExchange parameter.
        FMUImport._importFMU(originator, fmuFileParameter, context, x, y,
                true /* modelExchange */,
                false /*addMaximumStepSize*/,
                "ptolemy.actor.lib.fmi.FMUQSS");
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
    public void initialize() throws IllegalActionException {
        // By design, this method does not call super.initialize(),
        // because this class uses a different algorithm that is
        // specific to QSS.  However, we need to do what
        // AtomicActor.initialize() does:

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

        // Set a flag so the first call to fire() can do appropriate
        // initialization.
        _firstRound = true;

        // Initialize FMU parameters.
        if (((BooleanToken) initFMUParameters.getToken()).booleanValue()) {
            _initializeFMUParameters();
        }

        // Enter and exit the initialization mode.
        _fmiInitialize();

        // To initialize the event indicators, call this.
        _checkStateEvents();

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
        double nextEventTime = 0;

        // In FMI-2.0, this is a pointer to the structure, which is by
        // default how a subclass of Structure is handled, so there is no
        // need for having FMI20EventInfo.ByValue().
        _eventInfo = new FMI20EventInfo(newDiscreteStatesNeeded,
                terminateSimulation, nominalsOfContinuousStatesChanged,
                valuesOfContinuousStatesChanged, nextEventTimeDefined,
                nextEventTime);

        // FIXME: We assume no event iteration.
        fmiFlag = ((Integer) _fmiNewDiscreteStatesFunction.invoke(
                Integer.class, new Object[] { _fmiComponent, _eventInfo }))
                .intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to enter discrete state FMU: "
                            + _fmiStatusDescription(fmiFlag));
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

        // FIXME: Switch to the continuous mode.
        fmiFlag = ((Integer) _fmiEnterContinuousTimeModeFunction
                .invokeInt(new Object[] { _fmiComponent })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Could not enter continuous mode for FMU: "
                            + _fmiStatusDescription(fmiFlag));
        }

        // Get and configure the QSS integrator.
        _createQSSSolver();

        return;

    }

    /**
     * Return false if any output has been found that not depend
     * directly on an input.
     * 
     * @return False if this actor can be fired without all inputs
     * being known.
     */
    @Override
    public boolean isStrict() {
        return _isStrict;
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
        // FIXME: Moved _initializeQSSIntegratorInputVariables from postfire to fire. 
        // _initializeQSSIntegratorInputVariables invokes _triggerQuantizationEvents, 
        // which produces outputs. Outputs should not be produced in postfire
        if (_firstRound) {
            _initializeQSSIntegratorInputVariables(currentTime);
            _firstRound = false;
        }

        // Update the internal, continuous state models if necessary.
        _triggerRateEvent(currentTime, false);

        // Find the next firing time, assuming nothing else in simulation
        // changes.
        final Time possibleFireAtTime = _qssSolver
                .predictQuantizationEventTimeEarliest();
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

        // As we are not calling super.postfire(), we need
        // to do what AtomicActor.postfire() does and check
        // to see if stop was requested:
        return !_stopRequested;
    }

    /**
     * Rdeclare input/output dependencies.
     * 
     * @exception IllegalActionException if an exception occurs.
     */
    public void preinitialize() throws IllegalActionException {
        // Initialize the continuous states.
        _initializeContinuousStates();

        // Initialize the input list.
        _getInputs();

        // Initialize the output list.
        _getOutputs();

        // FIXME: Declare dependencies.
        _declareDelayDependency();

        // Get the indexes of dependent variables
        _getStateDerivativesDependenciesIndexes();

        // Call superclass to instantiate the FMU.
        super.preinitialize();
    }

    /**
     * Terminate and free the slave fmu.
     * 
     * @exception IllegalActionException If the slave fmu cannot be
     * terminated or freed.
     */
    public void wrapup() throws IllegalActionException {
        // Allow eventInfo to be garbaged collected.
        _eventInfo = null;
        super.wrapup();
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
                    + "which is not supported.  "
                    + "QSS currently only supports FMI version 2.0.");
        }

        // Check if the FMU is for model exchange.
        if (fmiModelDescription.modelExchangeCapabilities == null) {
            throw new IOException("There is no ModelExchange attribute in the model description"
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

        // Check if the FMU has at least one state variable.
        if (fmiModelDescription.continuousStates.size() < 1) {
            throw new IOException("The number of continuous states of this FMU is: "
                    + fmiModelDescription.numberOfContinuousStates
                    + ".  The FMU does not have any state variables.  "
                    + "The FMU needs to have at least one state variable. Please check the FMU.");
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return true if we are not in the first firing and the sign of
     * some event indicator has changed.
     *
     * @return True if a state event has occurred.
     * @exception IllegalActionException If the fmiGetEventIndicators
     * function is missing, or if calling it does not return fmiOK.
     */
    private boolean _checkStateEvents() throws IllegalActionException {
        int number = _fmiModelDescription.numberOfEventIndicators;
        if (number == 0) {
            // No event indicators.
            return false;
        }
        if (_eventIndicators == null || _eventIndicators.length != number) {
            _eventIndicators = new double[number];
        }
        if (_fmiGetEventIndicatorsFunction == null) {
            throw new IllegalActionException(this, "Could not get the "
                    + _fmiModelDescription.modelIdentifier
                    + "_fmiGetEventIndicators"
                    + "() C function?  Perhaps the .fmu file \""
                    + fmuFile.asFile()
                    + "\" does not contain a shared library for the current "
                    + "platform?  ");
        }

        int fmiFlag = ((Integer) _fmiGetEventIndicatorsFunction.invoke(
                Integer.class, new Object[] { _fmiComponent, _eventIndicators,
                        new NativeSizeT(number) })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to get event indicators" + ", return result was "
                            + _fmiStatusDescription(fmiFlag));
        }

        if (_firstRound) {
            _eventIndicatorsPrevious = _eventIndicators;
            _eventIndicators = null;
            return false;
        }
        // Check for polarity change.
        for (int i = 0; i < number; i++) {
            if (_eventIndicatorsPrevious[i] * _eventIndicators[i] < 0.0) {
                return true;
            }
        }
        _eventIndicatorsPrevious = _eventIndicators;
        return false;
    }

    /**
     * Set the dependency between all output ports and all input ports
     * of this actor. By default, each output port is assumed to have
     * a dependency on all input ports. If the FMU explicitly declares
     * input dependencies for a particular output, then then it only
     * depends on those inputs that it declares.
     * 
     * @exception IllegalActionException Not thrown in this base
     * class, derived classes should throw this exception if the delay
     * dependency cannot be computed.
     * @see #getCausalityInterface()
     * @see #_declareDelayDependency(IOPort, IOPort, double)
     */
    private void _declareDelayDependency() throws IllegalActionException {
        // Iterate through the outputs and the state ports, and for any output
        // or state that declares
        // dependencies, indicate a delay dependency for any inputs that
        // it does not mention.
        // By default, if all outputs depend on all inputs, then the actor
        // is strict.
        _isStrict = true;
        for (Output output : _outputs) {
            if (output.dependencies == null) {
                // There are no dependencies declared for this output,
                // so the output depends on all inputs.
                continue;
            }
            for (Input input : _inputs) {
                // Coverity Scan warned "GC: Suspicious calls to
                // generic collection methods" because we were calling
                // contains(input), where input is an instance of the
                // input inner class, yet dependencies is a Set of
                // TypedIOPorts.
                if (!output.dependencies.contains(input.port)) {
                    _declareDelayDependency(input.port, output.port, 0.0);
                    _isStrict = false;
                    if (_debugging) {
                        _debug("Declare that output " + output.port.getName()
                                + " does not depend on input "
                                + input.port.getName());
                    }
                }
            }
        }

        // Declare dependencies of state ports.
        for (int i = 0; i < _fmiModelDescription.numberOfContinuousStates; i++) {
            for (Input input : _inputs) {
                // Remove the dependence of the state output on the actor inputs
                _declareDelayDependency(input.port,
                        _fmiModelDescription.continuousStates.get(i).port, 0.0);
            }
        }
    }

    /**
     * Evaluate input directional derivative function.
     *
     * @param idx The input index.
     * @param uu_dot The input derivative.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException If an error when getting directional derivatives.
     */
    private double _evaluateInputDirectionalDerivatives(final int idx,
            final double[] uu_dot) throws IllegalActionException {
        final FMI20ContinuousStateDerivative stateDeriv = _fmiModelDescription.continousStateDerivatives
                .get(idx);
        final int numDepInputs = stateDeriv.dependentInputIndexes.size();
        final IntBuffer vRefStateDeriv = IntBuffer.allocate(1).put(0,
                (int) stateDeriv.scalarVariable.valueReference);
        final DoubleBuffer vInputs = DoubleBuffer.allocate(1).put(0, (1.0));
        final DoubleBuffer vStateDeriv = DoubleBuffer.allocate(1);
        double jac_uu_dot = 0;

        for (int ii = 0; ii < numDepInputs; ++ii) {
            final int currIdx = stateDeriv.dependentInputIndexes.get(ii);
            final Input currInput = _inputs.get(currIdx);
            final IntBuffer vRefInput = IntBuffer.allocate(1).put(0,
                    (int) currInput.scalarVariable.valueReference);
            int fmiFlag = ((Integer) _fmiGetDirectionalDerivativeFunction
                    .invoke(Integer.class, new Object[] { _fmiComponent,
                            vRefInput, new NativeSizeT(1), vRefStateDeriv,
                            new NativeSizeT(1), vInputs, vStateDeriv }))
                    .intValue();
            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this,
                        "Failed to get directional derivatives. fmiFlag = "
                                + _fmiStatusDescription(fmiFlag));
            }
            jac_uu_dot = jac_uu_dot + vStateDeriv.get(0) * uu_dot[currIdx];
        }
        return jac_uu_dot;
    }

    /**
     * Evaluate state directional derivative function.
     *
     * 
     * @param idx The state index.
     * @param xx_dot The state derivative.
     * @return Success (0 for success, else user-defined error code).
     * @exception IllegalActionException If an error when getting directional derivatives.
     */
    private double _evaluateStateDirectionalDerivatives(final int idx,
            final double[] xx_dot) throws IllegalActionException {

        final FMI20ContinuousStateDerivative stateDeriv = _fmiModelDescription.continousStateDerivatives
                .get(idx);
        final int numDepStates = stateDeriv.dependentStateIndexes.size();
        final IntBuffer vRefStateDeriv = IntBuffer.allocate(1).put(0,
                (int) stateDeriv.scalarVariable.valueReference);
        final DoubleBuffer vState = DoubleBuffer.allocate(1).put(0, (1.0));
        final DoubleBuffer vStateDeriv = DoubleBuffer.allocate(1);
        double jac_xx_dot = 0;

        for (int ii = 0; ii < numDepStates; ++ii) {
            final int currIdx = stateDeriv.dependentStateIndexes.get(ii);
            final ContinuousState currState = _fmiModelDescription.continuousStates
                    .get(currIdx);
            final IntBuffer vRefState = IntBuffer.allocate(1).put(0,
                    (int) currState.scalarVariable.valueReference);
            int fmiFlag = ((Integer) _fmiGetDirectionalDerivativeFunction
                    .invoke(Integer.class, new Object[] { _fmiComponent,
                            vRefState, new NativeSizeT(1), vRefStateDeriv,
                            new NativeSizeT(1), vState, vStateDeriv }))
                    .intValue();
            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this,
                        "Failed to get directional derivatives. fmiFlag = "
                                + _fmiStatusDescription(fmiFlag));
            }
            jac_xx_dot = jac_xx_dot + vStateDeriv.get(0) * xx_dot[currIdx];
        }
        return jac_xx_dot;
    }

    /**
     * Set the time of the FMU to the specified time.
     * 
     * @param time The current simulation time.
     * @exception IllegalActionException If the FMU does not return fmiOK.
     */
    private void _fmiSetTime(double time) throws IllegalActionException {
        // Set the time in the FMU.
        if (_debugging) {
            _debugToStdOut("Setting FMU time to " + time);
        }
        final int fmiFlag = ((Integer) _fmiSetTimeFunction.invoke(
                Integer.class, new Object[] { _fmiComponent, time }))
                .intValue();
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to set FMU time at time " + time + ": "
                            + _fmiStatusDescription(fmiFlag));
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
     * Return a list of inputs of the FMU. This function differs from the base
     * class in the sense that it does not add the continuousStates as input
     * ports. An input has both a declared ScalarVariable in the model
     * description file with causality declared to be "input" and a port with
     * the same name contained by this actor. Each returned input contains a
     * reference to the port and a reference to the {@link FMIScalarVariable}.
     * 
     * @return A list of inputs of the FMU.
     * @exception IllegalActionException If no port matching the name of a variable declared as an
     * input is found.
     */
    private List<Input> _getInputs() throws IllegalActionException {
        if (workspace().getVersion() == _inputsVersion) {
            return _inputs;
        }

        // The _inputs variable is out of date. Reconstruct it.
        _inputs = new LinkedList<Input>();
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // If this variable has an alias, then we operate
            // only on the real version, not the alias.
            // In bouncingBall.fmu, g has an alias, so it is skipped.
            if (scalarVariable.alias != null
                    && !scalarVariable.alias.equals(Alias.noAlias)) {
                continue;
            }
            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter
                    && scalarVariable.variability != FMIScalarVariable.Variability.constant
                    && scalarVariable.variability != FMIScalarVariable.Variability.fixed
                    && scalarVariable.variability != FMIScalarVariable.Variability.tunable // FMI-2.0rc1
                    && (scalarVariable.causality == Causality.input)) {
                // FIXME: This only works with double variables.
                if (!(scalarVariable.type instanceof FMIRealType)) {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }
                TypedIOPort port = (TypedIOPort) _getPortByNameOrDisplayName(scalarVariable.name);
                if (port == null) {
                    throw new IllegalActionException(this,
                            "FMU has an input named " + scalarVariable.name
                                    + ", but the actor has no such input port");
                }
                Input input = new Input();
                input.scalarVariable = scalarVariable;
                input.port = port;
                input.hasChanged = false;
                if (scalarVariable.type instanceof FMIRealType) {
                    input.start = ((FMIRealType) scalarVariable.type).start;
                } else {
                    input.start = null;
                }

                _inputs.add(input);
            }
        }
        _inputsVersion = workspace().getVersion();
        return _inputs;
    }

    /** Populate the specified model with data from the specified token.
     *  If the token is a QSSToken, then insert in the model any derivatives
     *  that might be given in the token, and set any remaining derivatives
     *  required by the model to zero. Otherwise, set any derivatives
     *  required by the model to zero.
     *  @param ivMdl The input model to parameterize.
     *  @param token The token values for parameterization.
     *  @throws IllegalActionException If the specified token cannot be converted
     *   to a double.
     */
    private void _getModelFromToken(ModelPolynomial ivMdl, Token token)
	    throws IllegalActionException {
        
        // Convert to a DoubleToken. If token is a QSSToken or DoubleToken,
        // then the convert method does nothing and just returns the token.
        // Otherwise, it attempts to convert it to a DoubleToken, and throws
        // an exception if such conversion is not possible.
        DoubleToken doubleToken = DoubleToken.convert(token);
        
        // In all cases, the first coefficient is simply the current value of the token.
        ivMdl.coeffs[0] = doubleToken.doubleValue();
        
        double[] derivatives = null;
        if (doubleToken instanceof QSSToken) {
            derivatives = ((QSSToken)doubleToken).derivativeValues();
        }
	final int ncoeffs = ivMdl.coeffs.length;
	for (int i = 1; i < ncoeffs; i++) {
	    if (derivatives == null || derivatives.length < i) {
		ivMdl.coeffs[i] = 0.0;
	    } else {
		ivMdl.coeffs[i] = derivatives[i-1];
	    }
	}
    }

    /**
     * Return a list of connected outputs of the FMU. This function differs from
     * the base class in the sense that it gets the outputs from values
     * retrieved out of the model structure. An output has both a declared
     * ScalarVariable in the model description file with causality declared to
     * be "output" and a port with the same name contained by this actor. If the
     * port exists but is not connected to anything (its width is zero), then it
     * this output is not included in the returned list. Each returned output
     * contains a reference to the port, a reference to the
     * {@link FMIScalarVariable}, and a set of input port on which the output
     * declares that it depends (or a null if it makes no such dependency
     * declaration).
     * 
     * @return A list of outputs of the FMU.
     * @exception IllegalActionException If an expected output is not found, or if the width of the
     * output cannot be determined.
     */
    private List<Output> _getOutputs() throws IllegalActionException {
        if (workspace().getVersion() == _outputsVersion) {
            return _outputs;
        }

        // The _outputs variable is out of date. Reconstruct it.
        _outputs = new LinkedList<Output>();
        Set<Output> stateOutputs = new HashSet<Output>();
        for (int i = 0; i < _fmiModelDescription.outputs.size(); i++) {
            FMIScalarVariable scalarVariable = _fmiModelDescription.outputs
                    .get(i).scalarVariable;
            TypedIOPort port = (TypedIOPort) _getPortByNameOrDisplayName(scalarVariable.name);
            if (port == null || port.getWidth() <= 0) {
                continue;
            }
            // FIXME: This only works for double variables.
            if (!(scalarVariable.type instanceof FMIRealType)) {
                throw new IllegalActionException("Type " + scalarVariable.type
                        + " not supported.");
            }
            Output output = new Output();
            output.scalarVariable = scalarVariable;
            output.port = port;
            // Get the outputs retrieved from the model structure.
            Set<TypedIOPort> dependencies = null;
            for (int j = 0; j < _fmiModelDescription.outputs.get(i).dependentScalarVariables
                    .size(); j++) {
                String inputName = _fmiModelDescription.outputs.get(i).dependentScalarVariables
                        .get(j).name;
                TypedIOPort inputPort = (TypedIOPort) _getPortByNameOrDisplayName(inputName);
                if (inputPort == null) {
                    continue;
                    /*
                     * throw new IllegalActionException(this,
                     * "FMU declares that output port " + port.getName() +
                     * " depends directly on input port " + inputName +
                     * ", but there is no such input port.");
                     */
                }
                if (dependencies == null) {
                    dependencies = new HashSet<TypedIOPort>();
                }
                dependencies.add(inputPort);
            }

            output.dependencies = dependencies;
            // Coverty Scan states that dependencies could be null, so we check here.
            if (dependencies != null) {
                // Get the outputs which depend on the states.
                for (int j = 0; j < _fmiModelDescription.continuousStates.size(); j++) {
                    TypedIOPort inputPort = _fmiModelDescription.continuousStates
                        .get(j).port;
                    if (dependencies.contains(inputPort)) {
                        output.isKnown = true;
                        stateOutputs.add(output);
                    }
                }
            }
            _outputs.add(output);
        }
        // Convert the set into a list.
        _stateDependentOutputs = new ArrayList<Output>(stateOutputs);
        // Create the list if outputs that do not depend on states.
        _nonStateDependentOutputs = new LinkedList<Output>();
        // Get outputs which do not depend on the states.
        for (int j = 0; j < _outputs.size(); j++) {
            if (_outputs.get(j).isKnown) {
                // Reset the isKnown flag.
                _outputs.get(j).isKnown = false;
                continue;
            }
            _nonStateDependentOutputs.add(_outputs.get(j));
            // Reset the isKnown flag
            _outputs.get(j).isKnown = false;
        }

        // Create an array of input value references
        _outputValueReferences = new long[_outputs.size()];

        for (int ii = 0; ii < _outputs.size(); ++ii) {
            final FMIScalarVariable scalar = _outputs.get(ii).scalarVariable;
            // FIXME: Retrieve these values once and save them.
            _outputValueReferences[ii] = scalar.valueReference;
        }

        _outputsVersion = workspace().getVersion();
        return _outputs;
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
            _fmiModelDescription.continousStateDerivatives.get(i).dependentInputIndexes = new LinkedList<Integer>();
            _fmiModelDescription.continousStateDerivatives.get(i).dependentStateIndexes = new LinkedList<Integer>();
            final FMI20ContinuousStateDerivative stateDeriv = _fmiModelDescription.continousStateDerivatives
                    .get(i);
            // Get the indexes of the dependent input variables.
            for (int j = 0; j < _inputs.size(); j++) {
                final Input curIpt = _inputs.get(j);
                if (stateDeriv.dependentScalarVariables
                        .contains(curIpt.scalarVariable)) {
                    final int index = _inputs.indexOf(curIpt);
                    _fmiModelDescription.continousStateDerivatives.get(i).dependentInputIndexes
                            .add(index);
                }
            }
            // Get the indexes of dependent continuous state variables.
            for (int j = 0; j < numContStates; j++) {
                final ContinuousState curState = _fmiModelDescription.continuousStates
                        .get(j);
                if (stateDeriv.dependentScalarVariables
                        .contains(curState.scalarVariable)) {
                    final int index = _fmiModelDescription.continuousStates
                            .indexOf(curState);
                    _fmiModelDescription.continousStateDerivatives.get(i).dependentStateIndexes
                            .add(index);
                }
            }

        }
    }

    /**
     * Handle time, state and step events.
     *
     * @param timeValue The current time.
     * @exception IllegalActionException If an error occurs when handling events.
     */
    private void _handleEvents(double timeValue) throws IllegalActionException {
        // Complete the integrator step.
        // True if fmi2SetFMUState() will not be called for times
        // before the current time in this simulation.
        // Check event indicators.
        boolean stateEvent = _checkStateEvents();
        boolean noSetFMUStatePriorToCurrentPoint = true;
        boolean stepEvent = _fmiCompletedIntegratorStep(noSetFMUStatePriorToCurrentPoint);
        boolean timeEvent = ((_eventInfo.nextEventTimeDefined == 1) && (_eventInfo.nextEventTime < timeValue));

        if (timeEvent || stateEvent || stepEvent) {
            _enterEventMode();
            if (timeEvent) {
                // nTimeEvents++;
                if (_debugging) {
                    _debug("time event at t=" + timeValue);
                }
                if (stateEvent) {
                    if (_debugging) {
                        _debug("state event at t=" + timeValue);
                    }
                }
                if (stepEvent) {
                    // nStepEvents++;
                    // if (loggingOn) printf("step event at t=%.16g\n",
                    // time);
                    if (_debugging) {
                        _debug("step event at t=" + timeValue);
                    }
                }
                // "event iteration in one step, ignoring intermediate results"
                _eventInfo.newDiscreteStatesNeeded = (byte) 1;
                _eventInfo.terminateSimulation = (byte) 0;

                // FIXME: We assume no event iteration.
                final int fmiFlag = ((Integer) _fmiNewDiscreteStatesFunction
                        .invoke(Integer.class, new Object[] { _fmiComponent,
                                _eventInfo })).intValue();

                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new IllegalActionException(this,
                            "Failed to enter discrete state FMU: "
                                    + _fmiStatusDescription(fmiFlag));
                }
                if (_eventInfo.terminateSimulation == (byte) 1) {
                    System.out.println("model requested termination at t="
                            + timeValue);
                    getDirector().finish();
                }
                // Ingore event iteration and enter continuous mode.
                _enterContinuousTimeMode();

                // "check for change of value of states"
                if (_debugging) {
                    if (_eventInfo.valuesOfContinuousStatesChanged == (byte) 1) {
                        _debug("continuous state values changed at t="
                                + timeValue);
                    }
                    if (_eventInfo.nominalsOfContinuousStatesChanged == (byte) 1) {
                        _debug("nominals of continuous state changed  at t="
                                + timeValue);
                    }
                }
            }
        }
    }

    /**
     * Initialize the values of the continuous state ports with their
     * dependencies.
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
            // Initialize vector of value references of state variables
            _stateValueReferences[i] = _fmiModelDescription.continuousStates.get(i).scalarVariable.valueReference;
            // Initialize vector of value references of derivatives of state
            // variables.
            _stateDerivativeValueReferences[i] = _fmiModelDescription.continousStateDerivatives
                    .get(i).scalarVariable.valueReference;

            // Get the output retrieved from the model structure.
            Set<TypedIOPort> dependencies = null;
            for (int j = 0; j < _fmiModelDescription.continuousStates.get(i).dependentScalarVariables
                    .size(); j++) {
                final String inputName = _fmiModelDescription.continuousStates
                        .get(i).dependentScalarVariables.get(j).name;
                final TypedIOPort inputPort = (TypedIOPort) _getPortByNameOrDisplayName(inputName);
                if (inputPort == null) {
                    continue;
                    /*
                     * throw new IllegalActionException(this,
                     * "FMU declares that port " +
                     * _fmiModelDescription.continuousStates
                     * .get(i).port.getName() +
                     * " depends directly on input port " + inputName +
                     * ", but there is no such input port.");
                     */
                }
                if (dependencies == null) {
                    dependencies = new HashSet<TypedIOPort>();
                }
                dependencies.add(inputPort);
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
                    || scalar.variability == FMIScalarVariable.Variability.fixed || scalar.variability == FMIScalarVariable.Variability.tunable) // FMI-2.0rc1
                    && scalar.causality != Causality.local // FMI-2.0rc1
                    && scalar.causality != Causality.input
                    && scalar.causality != Causality.output) {
                String sanitizedName = StringUtilities
                        .sanitizeName(scalar.name);
                Parameter parameter = (Parameter) getAttribute(sanitizedName,
                        Parameter.class);
                if (parameter != null) {
                    try {
                        _setFMUScalarVariable(scalar, parameter.getToken());
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

    /** Create a new QSS solver and initialize it for use by this actor.
     *  @exception IllegalActionException If the solver cannot be created or initialized.
     */
    private final void _createQSSSolver() throws IllegalActionException {

        // Get director and check its type.
        Director director = getDirector();
        if (!(director instanceof QSSDirector)) {
            throw new IllegalActionException(
                    this,
                    String.format(
                            "Director %s cannot be used for QSS, which requires a QSSDirector.",
                            director.getName()));
        }
        _director = (QSSDirector) director;
        final Time currentTime = _director.getModelTime();

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._initializeQssIntegrator() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Create a new QSS solver and initialize it.
        _qssSolver = _director.newQSSSolver();
        _qssSolver.initializeDerivativeFunction(this);
        _qssSolver.initializeSimulationTime(currentTime);
        _qssSolver.setQuantizationEventTimeMaximum(_director.getModelStopTime());

        // Get initial state values from FMU.
        // TODO: Does the FMUQSS need to carry {_states}? Shouldn't all state
        // information be delegated to the integrator {_qssIgr}?
        final int stateCt = _qssSolver.getStateCount();
        _states = new double[stateCt];
        final int fmiFlag = ((Integer) _fmiGetContinuousStatesFunction.invoke(
                Integer.class, new Object[] { _fmiComponent, _states,
                        new NativeSizeT(stateCt) })).intValue();
        if (FMILibrary.FMIStatus.fmiOK != fmiFlag) {
            throw new IllegalActionException(
                    this,
                    String.format(
                            "Failed to get continuous states at time %s. Return value of fmiGetContinuousStates() was %s",
                            currentTime.toString(), _fmiStatusDescription(fmiFlag)));
        }

        // Set initial state values.
        for (int ii = 0; ii < stateCt; ++ii) {
            _qssSolver.setStateValue(ii, _states[ii]);
        }

        // Set quantization tolerances.
        final double absTolMin = 1e-20;
        final double relTol = _director.getErrorTolerance();
        for (int ii = 0; ii < stateCt; ++ii) {
            // Choose absolute tolerance for state based on its nominal value.
            // Thus a state with nominal value 1000 will have an absolute
            // tolerance
            // 1000 times greater than a state with nominal value 1.
            final double nominalValue = _fmiModelDescription.continuousStates
                    .get(ii).nominal.doubleValue();
            double absTol = Math.abs(nominalValue) * relTol;
            if (absTol < absTolMin) {
                absTol = absTolMin;
            }
            _qssSolver.setQuantizationTolerance(ii, absTol, relTol);
        }

        // Tell integrator to quantize.
        // Expect this to fail-- integrator should want to do a rate-event also,
        // but don't yet have input variable models.
        _triggerQuantizationEvents(currentTime, false); // TODO: Currently set
        // forceAll==false, to test code,
        // but should be true, to indicate
        // intent.

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
                            System.identityHashCode(this), currentTime.toString()));
        }

        // Create array for input models.
        final int ivCt = _qssSolver.getInputVariableCount();
        if (ivCt > 0) {
            _inputVariableModels = new ModelPolynomial[ivCt];
        }

        // Create input variable models.
        // Note this code block may have to change location at some point.
        // Reason-- when input variable models get variable order, will want to
        // read the model order from the model. Can't do that until have tokens.
        // But don't have tokens yet at this point in the initialization.
        final int ivMdlOrder = _qssSolver.getStateModelOrder();
        for (int ii = 0; ii < ivCt; ++ii) {
            // Get the input corresponding to input variable {ii}.
            final Input input = _inputs.get(ii);
            // Require port has known state.
            if (!input.port.isKnown(0)) {
                throw new IllegalActionException(this, String.format(
                        "Input port %s is unknown at initialization.",
                        input.port.getName()));
            }
            // Create the model.
            // Note the port may not have a token on it yet. Therefore,
            // while can create the model, can't initialize its time or value.
            final ModelPolynomial ivMdl = new ModelPolynomial(ivMdlOrder);
            _inputVariableModels[ii] = ivMdl;
            // ivMdl.tMdl = currentTime; // TODO: Confirm where time set.
            ivMdl.claimWriteAccess();
            // Give model to the integrator.
            _qssSolver.addInputVariableModel(ii, ivMdl);
        }

        // Load input variable models with data.
        for (int ii = 0; ii < ivCt; ++ii) {
            // Get the input corresponding to input variable {ii}.
            final Input input = _inputs.get(ii);
            double initialValue;
            // Look for a token.
            if (input.port.hasToken(0)) {
                final Token token = input.port.get(0);
                if (token instanceof QSSToken) {
                    final QSSToken qssTok = (QSSToken) token;
                    initialValue = qssTok.doubleValue();
                } else if (token instanceof DoubleToken) {
                    initialValue = ((DoubleToken) token).doubleValue();
                } else {
                    throw new IllegalActionException(this, String.format(
                            "Input port %s is connected to a port which has"
                                    + " a type which is not an instance "
                                    + "of QSSToken or DoubleToken.",
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
            // Get the model.
            // We only initialize the first coefficient
            // since we don't have other coefficients yet.
            final ModelPolynomial ivMdl = _inputVariableModels[ii];
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
        try {
            // TODO: This may not be allowed-- rate events change internal
            // state,
            // which is meant to happen in postfire(). ??? Check Ptolemy user
            // guide.
            _qssSolver.triggerRateEvent();
        } catch (Exception ee) {
            // Rethrow as an IllegalActionException.
            throw new IllegalActionException(this, ee, "Triggering rate event failed.");
        }

    }

    /**
     * Broadcast FMU outputs that are not quantized states and do not depend on
     * quantized states.
     *
     * <p>FMU can produce outputs that don't correspond to states.</p>
     * 
     * @param currentTime The current simulation time.
     * @param order The state model order.
     */
    private final void _produceOutputs(final Time currentTime, final int order)
            throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._produceOutputs() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }
        // FIXME: This code will only work with doubles for now.
        for (int ii = 0; ii < _nonStateDependentOutputs.size(); ii++) {
            final Output output = _nonStateDependentOutputs.get(ii);

            final TypedIOPort port = output.port;
            Token token = null;
            FMIScalarVariable scalarVariable = output.scalarVariable;

            if (scalarVariable.type instanceof FMIBooleanType) {
                throw new IllegalActionException("Type " + scalarVariable.type
                        + " not supported.");
                // boolean result = scalarVariable.getBoolean(_fmiComponent);
                // token = new BooleanToken(result);
            } else if (scalarVariable.type instanceof FMIIntegerType) {
                // FIXME: handle Enumerations?
                throw new IllegalActionException("Type " + scalarVariable.type
                        + " not supported.");
                // int result = scalarVariable.getInt(_fmiComponent);
                // token = new IntToken(result);
            } else if (scalarVariable.type instanceof FMIRealType) {
                double result = scalarVariable.getDouble(_fmiComponent);
                final double[] ooArr = { result, 0.0, 0.0 };
                _sendModelToPort(order, ooArr, port);
                // token = new DoubleToken(result);
            } else if (scalarVariable.type instanceof FMIStringType) {
                throw new IllegalActionException("Type " + scalarVariable.type
                        + " not supported.");
                // String result = scalarVariable.getString(_fmiComponent);
                // token = new StringToken(result);
            } else {
                throw new IllegalActionException("Type " + scalarVariable.type
                        + " not supported.");
            }

            if (_debugging) {
                _debugToStdOut(String.format(
                        "-- Id{%d} send output %s value %s",
                        System.identityHashCode(this), scalarVariable.name,
                        token));
            }
            // port.send(0, token);
        }

    }

    /**
     * Broadcast FMU outputs that are not quantized states and do
     * depend on quantized states.
     *
     * <p>FMU can produce outputs that don't correspond to states.</p>
     *
     * @param currentTime The current simulation time.
     * @param order The state model order.
     */
    private final void _produceOutputs(final Time currentTime,
            final TypedIOPort outPort, final int order)
            throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._produceOutputs() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }
        // FIXME: This code will only work with doubles for now.
        for (int ii = 0; ii < _stateDependentOutputs.size(); ii++) {
            final Output output = _stateDependentOutputs.get(ii);

            final TypedIOPort port = output.port;
            if (output.dependencies.contains(outPort) && !output.isKnown) {
                Token token = null;
                FMIScalarVariable scalarVariable = output.scalarVariable;

                if (scalarVariable.type instanceof FMIBooleanType) {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                    // boolean result =
                    // scalarVariable.getBoolean(_fmiComponent);
                    // token = new BooleanToken(result);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                    // int result = scalarVariable.getInt(_fmiComponent);
                    // token = new IntToken(result);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    double result = scalarVariable.getDouble(_fmiComponent);
                    // token = new DoubleToken(result);
                    final double[] ooArr = { result, 0.0, 0.0 };
                    _sendModelToPort(order, ooArr, port);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                    // String result = scalarVariable.getString(_fmiComponent);
                    // token = new StringToken(result);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }

                if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} send output %s value %s",
                            System.identityHashCode(this), scalarVariable.name,
                            token));
                }
                // port.send(0, token);
                _stateDependentOutputs.get(ii).isKnown = true;
            }
        }

    }

    /**
     * Send state and output models to port.
     *
     * @param ord The state model order.
     * @param val The values to be sent.
     * @param prt The port where values will be sent.
     */
    private void _sendModelToPort(final int ord, final double[] val,
            final TypedIOPort prt) throws NoRoomException,
            IllegalActionException {
        // Send values (Qss1)
        if (ord == 0) {
            prt.send(0, new QSSToken(val[0]));
        }
        // Send values and higher order derivatives (Qss2)
        else if (ord == 1) {
            final double[] der = { val[1] };
            prt.send(0, new QSSToken(val[0], der));
        }
        // Send values and higher order derivatives (Qss3)
        else if (ord == 2) {
            final double[] der = { val[1], val[2] };
            prt.send(0, new QSSToken(val[0], der));
        } else {
            throw new IllegalActionException(
                    "The order of the Qss integration method used: "
                            + ord
                            + " is  not supported. Current implementation supports "
                            + "Qss1, Qss2, and Qss3");
        }
    }

    /**
     * Trigger quantization-events if necessary.
     *
     * <p>Update the external, quantized state models.</p>
     *
     * @param currentTime The current simulation time.
     * @param forceAll If true, requantize all state models.
     */
    private final void _triggerQuantizationEvents(final Time currentTime,
            final boolean forceAll) throws IllegalActionException {

        // Initialize.
        final int stateCt = _qssSolver.getStateCount();

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._triggerQuantEvts() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Reset the isKnown flag.
        for (int ii = 0; ii < _stateDependentOutputs.size(); ii++) {
            _stateDependentOutputs.get(ii).isKnown = false;
        }
        // Loop over states that need to be requantized.
        int qIdx = -1;
        final int order = _qssSolver.getStateModelOrder();
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

            // Requantize the state.
            _qssSolver.triggerQuantizationEvent(qIdx);

            // Export to rest of simulation.
            // TODO: Convert this to export models, not just values.
            final TypedIOPort outPort = _fmiModelDescription.continuousStates
                    .get(qIdx).port;

            // Added a check to produce outputs to port which are connected.
            if (outPort.getWidth() > 0) {
                _sendModelToPort(order, _qssSolver.getStateModel(qIdx).coeffs,
                        outPort);
            }
            // Only produce outputs that depend on the states.
            _produceOutputs(currentTime, outPort, order);

            // Diagnostic output.
            if (_debugging) {
                _debugToStdOut(String.format(
                        "-- Id{%d} set quantized state model %d to %s", System
                                .identityHashCode(this), qIdx, _qssSolver
                                .getStateModel(qIdx).toString()));
            }

        }

        // A quantization-event implies other FMU outputs change.
        // Produce outputs to all outputs that do not depend on the states.
        _produceOutputs(currentTime, order);

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
            final boolean force) throws IllegalActionException {

        if (_debugging) {
            _debugToStdOut(String.format(
                    "FMUQSS._triggerRateEvt() on id{%d} at time %s",
                    System.identityHashCode(this), currentTime.toString()));
        }

        // Update the input variable models if necessary.
        int currIdx = 0;
        boolean updatedInputVarMdl = false;
        for (Input input : _inputs) {
            // TODO: Check whether there is a guarantee that _getInputs() here
            // returns the same count of inputs as it did during
            // _initializeQssIntegrator_inputVars().
            // If not, then index {currIdx} can be wrong.
            assert (input.port.isKnown(0)); // Checked in
                                            // _initializeQssIntegrator_inputVars();
                                            // assume it doesn't change.
            if (input.port.hasToken(0)) {
                // Here, have a new value on the input port.
                final Token token = input.port.get(0);
                // The check below has been disabled. The assumption is that
                // any input is a new input. This is guaranteed if we use
                // the QSSQuantizer since other outputs will react to
                // quantization events.
                // if (!token.equals(input.lastToken)) {
                // assert (token instanceof DoubleMatrixToken);
                // Update the model.
                final ModelPolynomial ivMdl = _inputVariableModels[currIdx];

                _getModelFromToken(ivMdl, token);
                // TODO: Here, just assuming that if have a new input, means
                // the
                // model
                // has changed. This may not be true; not sure. See field
                // persistentInputs
                // on class FMUImport. Consider putting in code that checks
                // if
                // value
                // changed. But it might be better to put a listener on the
                // integrator (see notes in QssBase).
                // TODO: Line below assumes the model time is {currentTime},
                // but
                // not
                // sure this is true. Need a way to inspect/get model time
                // from
                // the
                // information on the port. Either do it by hand, or
                // bootstrap
                // Ptolemy
                // existing timestamp conventions, if possible.
                ivMdl.tMdl = currentTime;
                updatedInputVarMdl = true;
                // Save the last token seen at this port
                // _inputs.get(currIdx).lastToken = token;
                // Set the hasChanged flag to true.
                _inputs.get(currIdx).hasChanged = true;
                if (_debugging) {
                    _debugToStdOut(String.format(
                            "-- Id{%d} set input variable model %d to %s",
                            System.identityHashCode(this), currIdx,
                            ivMdl.toString()));
                }
                // }
            }
            currIdx++;
        }
        assert (_qssSolver.getInputVariableCount() == currIdx);

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
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee, "Triggering rate event failed.");
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                           private fields                  ////

    /** QSS director. */
    private QSSDirector _director;

    /** Buffer for event indicators. */
    private double[] _eventIndicators;

    /** Buffer for previous event indicators. */
    private double[] _eventIndicatorsPrevious;

    /** The eventInfo. */
    private FMI20EventInfo _eventInfo;

    /**
     * Flag identifying the first round of iterations of fire() and postfire()
     * after initialize().
     */
    private boolean _firstRound = true;

    /** The new states computed in fire, to be committed in postfire. */
    private double[] _states;

    /** The inputs of this FMU. */
    private List<Input> _inputs;

    /** The workspace version at which the _inputs variable was last updated. */
    private long _inputsVersion = -1;

    /** Models for communicating with the integrator. */
    private ModelPolynomial[] _inputVariableModels;

    /** Track requests for firing. */
    private Time _lastFireAtTime;

    /** The outputs of this FMU. */
    private List<Output> _nonStateDependentOutputs;

    /** Vector of output value references.  */
    private long[] _outputValueReferences;

    /** The outputs of this FMU. */
    private List<Output> _outputs;

    /** The workspace version at which the _outputs variable was last updated. */
    private long _outputsVersion = -1;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _qssSolver = null;

    /** The non state dependent outputs of this FMU. */
    private List<Output> _stateDependentOutputs;

    /**
     * Indicator of whether the actor is strict, meaning that all inputs must be
     * known to fire it.
     */
    private boolean _isStrict = true;

    /** Vector of state value references. */
    private long[] _stateValueReferences;

    /** Vector of state derivative value references. */
    private long[] _stateDerivativeValueReferences;


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A data structure representing an input to the FMU. */
    private static class Input {
        // FindBugs indicates that this should be a static class.

        /** The FMI scalar variable for this output. */
        public FMIScalarVariable scalarVariable;

        /** The Ptolemy output port for this output. */
        public TypedIOPort port;

        /** The start value for this variable, or null if it is not given. */
        public Double start;

        /** The flag which indicates that input changed. */
        public boolean hasChanged;
    }

    /** A data structure representing an output from the FMU. */
    private static class Output {
        // FindBugs indicates that this should be a static class.

        /** The FMI scalar variable for this output. */
        public FMIScalarVariable scalarVariable;

        /** The Ptolemy output port for this output. */
        public TypedIOPort port;

        /** The set of input ports on which the output declares it depends. */
        public Set<TypedIOPort> dependencies;

        /** The flag which indicates that an output is set. */
        public boolean isKnown;
    }

}
