/* Import a Hybrid Co-Simulation FMU.

   Copyright (c) 2015 The Regents of the University of California.
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
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;

import com.sun.jna.Function;

import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TimeRegulator;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.continuous.ContinuousStatefulDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.AbsentToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/**
 * Import a Hybrid Co-Simulation FMU.
 *
 * @author Fabio Cremona
 * @version $Id: FMUImportHybrid.java$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */

public class FMUImportHybrid extends FMUImport implements
        TimeRegulator {

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
    public FMUImportHybrid(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {        
        super(container, name);
        _stepSize = 0;
    }

    /**
     * 
     * @param originator
     * @param fmuFileParameter
     * @param context
     * @param x
     * @param y
     * @param modelExchange
     * @throws IllegalActionException
     * @throws IOException
     */
    
    public static void importFMU(Object originator,
            FileParameter fmuFileParameter, NamedObj context, double x,
            double y, boolean modelExchange) throws IllegalActionException,
            IOException {

        System.out.println("FMUImportHybrid importFMU()");
        Method acceptFMUMethod = null;
        try {
            Class clazz = Class.forName("ptolemy.actor.lib.fmi.FMUImport");
            acceptFMUMethod = clazz.getDeclaredMethod("_acceptFMU",
                    new Class[] { FMIModelDescription.class });
        } catch (Throwable throwable) {
            throw new IllegalActionException(
                    context,
                    throwable,
                    "Failed to get the static _acceptFMU(FMIModelDescription) method for FMUImportHybrid.");
        }
        // We use a protected method so that we can change
        // the name of the entity that is instantiated.
        FMUImportHybrid._importFMU(originator, fmuFileParameter, context, x, y,
                modelExchange, true /* addMaximumStepSize */,
                true /* stateVariablesAsInputPorts */,
                "ptolemy.actor.lib.fmi.FMUImportHybrid", acceptFMUMethod);
    }
    
    /**
     * Initialize this FMU wrapper. For co-simulation, this
     * initializes the FMU.  For model exchange, it does not, because
     * for model exchange, the inputs at the start time need to be
     * provided prior to initialization.  Initialization will
     * therefore occur in the first invocation of fire().
     *
     * @exception IllegalActionException If the slave FMU cannot be
     * initialized.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid.initialize() method called.");
        }
        
        // I don't invoke super.initialize(), therefore I call initialize()
        // for all the initializable.
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
        
        // Set a flag so the first call to fire() can do appropriate
        // initialization.
        _firstFire = true;
        _firstFireInIteration = true;
        _newStates = null;

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
            if ((scalar.variability == FMIScalarVariable.Variability.parameter || scalar.variability == FMIScalarVariable.Variability.fixed) // FMI-2.0rc1
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
        
        Director director = getDirector();
        Time startTime = director.getModelStartTime();

        // Determine the error tolerance of the director, if specified.
        // The FMI 2.0 standard does not offer any suggestion for a default
        // relative tolerance, so we just pick one, in case the director does
        // not provide one.
        // The 2.0beta4 spec says:
        // "Argument "relativeTolerance" suggests a relative
        // (local) tolerance in case the slave utilizes a
        // numerical integrator with variable step size and
        // error estimation.
        _relativeTolerance = 1e-4;
        _toleranceControlled = (byte) 0; // fmiBoolean
        if (director instanceof ContinuousStatefulDirector) {
            _relativeTolerance = ((ContinuousStatefulDirector) director).getErrorTolerance();
            _toleranceControlled = (byte) 1; // fmiBoolean
        }

        //////////////////////////////////////////////
        //// co-simulation version

        _fmiInitialize();
        _lastCommitTime = startTime;
        _lastFireTime = startTime;
        if (director instanceof SuperdenseTimeDirector) {
            _lastFireMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        } else {
            // Director must be discrete, so we assume microstep == 1.
            _lastFireMicrostep = 1;
        }
        _stepSizeRejected = false;
        _refinedStepSize = -1.0;
        _suggestZeroStepSize = false;
        
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid.initialize() call completed.");
        }
        
        boolean allInputsKnown = _allInputsKnown();
        if (allInputsKnown) {
            Time currentTime = director.getModelTime();
            Time proposedTime = proposeTime(currentTime);
            director.fireAt(this, proposedTime);
        }
        
        return;
    }
    
    @Override
    public void fire() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }

        if (_debugging) {
            _debugToStdOut("FMUImportHybrid.fire() at time "
                    + currentTime 
                    + " and microstep "
                    + currentMicrostep);
        }
        
        // Set Inputs value to the FMU
        _setFmuInputs();
        
        // Get Outputs value from the FMU and produce them.
        _getFmuOutputs();
        
        boolean allInputsKnown = _allInputsKnown();
        if (allInputsKnown) {
            Time proposedTime = proposeTime(currentTime.add(1.0));
            director.fireAt(this, proposedTime);
        }
        
        _firstFireInIteration = false;
    }
    
    /**
     * Override the base class to record the current time as the last commit
     * time.
     *
     * @return True if execution can continue into the next iteration.
     * @exception IllegalActionException If the step size was
     * rejected, if thrown while requesting refiring if necessary, if
     * thrown while the FMU state is being recorded or if thrown by
     * the superclass.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("FMUImportHybrid.postfire()");
        }
        if (_stepSizeRejected) {
            // The director is unaware that this FMU
            // has discarded a step. The discarded step
            // has not been handled. The director must not
            // be capable of supporting components that reject
            // step sizes.
            throw new IllegalActionException(
                    this,
                    getDirector(),
                    "FMU discarded a step, rejecting the director's step size."
                            + " But the director has not handled it."
                            + " Hence, this director is incompatible with this FMU.");
        }
        
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        Time futureTime = director.getModelNextIterationTime();
        int currentMicrostep = 1;
        
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        Time refinedStepSize = _fmiDoStepHybrid(futureTime, currentMicrostep);
//        System.out.println("->FMU " + getFullName() + ", current time: " + currentTime.getLongValue() + ", refinedStepSize: " + refinedStepSize.getLongValue() + ", futureTime: " + futureTime);
        if (refinedStepSize.getLongValue() > _stepSize/*refinedStepSize.isPositive()*/) {
            throw new IllegalActionException(
                    this,
                    getDirector(),
                    "FMU discarded a step, rejecting the director's step size." 
                    + " In postfire() the stepsize is supposed to be accepted.");
        }
        _lastCommitTime = currentTime;
        _refinedStepSize = -1.0;
        _firstFireInIteration = true;
        
        // Advance Time
//        if (refinedStepSize.getLongValue() > 0) {
//            System.out.println("->FMU " + getFullName() + ", calling fireAt(): " + refinedStepSize.getLongValue() + ", at time: " + currentTime.getLongValue());
//            director.fireAt(this, refinedStepSize, currentMicrostep);
//        }
        
        return !_stopRequested;
    }

    /**
     * 
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid.preinitialize()");
        }
        try {
            _fmiGetMaxStepSize = _fmiModelDescription
                    .getFmiFunction("fmiHybridGetMaxStepSize");
        } catch (Throwable throwable) {
            throw new IllegalActionException(
                    this,
                    throwable,
                    "The Co-Simulation fmiGetMaxStepSize() function was not found? ");
        }
        _stepSize = 0;
    }
   
    /**
     * 
     */    
    @Override
    public Time proposeTime(Time proposedTime) throws IllegalActionException {
        int currentMicrostep = 1;
        Director director = getDirector();
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        LongBuffer stepSize = LongBuffer.allocate(1);
        if (_fmiGetMaxStepSize != null) {
            int fmiFlag = ((Integer) _fmiGetMaxStepSize.invokeInt(new Object[] {
                    _fmiComponent, stepSize})).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiDiscard) {
                if (_debugging) {
                    _debugToStdOut("Error while getMaxStepSize()"
                            + " at time " + director.getModelTime());
                }
            }
            _stepSize = stepSize.get(0) + director.getModelTime().getLongValue();
            return new Time(getDirector(), _stepSize);
        } else {
            Time refinedStepSize = _fmiDoStepHybrid(proposedTime, currentMicrostep);
            _stepSize = refinedStepSize.getLongValue();
            return refinedStepSize;
        }        
    }
    
    /**
     * 
     */
    @Override
    public boolean noNewActors() {
        // TODO Auto-generated method stub
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /**
     * Returns true if all inputs are known. Return false if at least
     * one of the inputs is unknown. If an FMU has no inputs it also
     * returns true.
     * @return
     * @throws IllegalActionException
     */
    protected boolean _allInputsKnown() throws IllegalActionException {
        boolean allInputsKnown = true;
        List<TypedIOPort> inputPorts = inputPortList();
        for (TypedIOPort inputPort : inputPorts) {
            if (inputPort.getWidth() < 0 || !inputPort.isKnown(0)) {
                // Input port value is not known.
                allInputsKnown = false;
                break;
            }
        }
        return allInputsKnown;
    }
    
    /**
     * 
     */
    protected void _defaultStepSize() {
        
    }
    /**
     * Initialize the FMU.
     */
    protected void _fmiInitialize() throws IllegalActionException {
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize()");
        }
        
        // This _fmiInitialize() is specific for FMI 2.1 Hybrid Co-Simulation
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        Director director = getDirector();
        Time startTime = director.getModelStartTime();
        Time stopTime = director.getModelStopTime();
//        System.out.println("-> Time Initilized: " + startTime.getLongValue());
        int fmiFlag;

        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize(): about to invoke the fmi setup experiment function");
        }
        fmiFlag = ((Integer) _fmiSetupExperimentFunction.invoke(
                Integer.class,
                new Object[] { _fmiComponent, _toleranceControlled,
                        (long)_relativeTolerance, startTime.getLongValue(),
                        1, stopTime.getLongValue() })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to setup the experiment of the FMU: "
                            + _fmiStatusDescription(fmiFlag));
        }
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize(): about to invoke the fmi enter initialization function");
        }
        fmiFlag = ((Integer) _fmiEnterInitializationModeFunction
                .invoke(Integer.class, new Object[] { _fmiComponent }))
                .intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to enter the initialization mode of the FMU: "
                            + _fmiStatusDescription(fmiFlag));
        }
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize(): about to invoke the fmi exit initialization function");
        }
        fmiFlag = ((Integer) _fmiExitInitializationModeFunction.invoke(
                Integer.class, new Object[] { _fmiComponent }))
                .intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Failed to exit the initialization mode of the FMU: "
                            + _fmiStatusDescription(fmiFlag));
        }
        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize(): about to request refiring if necessary.");
        }

        // If the FMU can provide a maximum step size, query for the
        // initial maximum
        // step size and call fireAt() and ensure that the FMU is
        // invoked
        // at the specified time.
        _requestRefiringIfNecessary();

        if (_debugging) {
            _debugToStdOut("FMUImportHybrid._fmiInitialize(): about to record FMU state.");
        }
        // In case we have to backtrack, if the FMU supports
        // backtracking,
        // record its state.
        _recordFMUState();

        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new IllegalActionException(this, "Could not simulate, "
                    + modelIdentifier
                    + "_fmiInitializeSlave(Component, /* startTime */ "
                    + startTime.getLongValue() + ", 1, /* stopTime */"
                    + stopTime.getLongValue() + ") returned "
                    + _fmiStatusDescription(fmiFlag));
        }

        if (_debugging) {
            _debugToStdOut("Initialized FMU.");
        }
        _modelInitialized = true;
    }
    
    /**
     * Advance from the last firing time or last commit time to the specified
     * time and microstep by calling fmiDoStep(), if necessary. This method is
     * for co-simulation only. Such an advance is necessary if the newTime is
     * not equal to the last firing time, or if it is equal and the newMicrostep
     * is greater than the last firing microstep. If the step size is rejected,
     * then return a new suggested step size.
     *
     * @param newTime The time to advance to.
     * @param newMicrostep The microstep to advance to.
     * @return A revised suggested step size, or -1.0 if the step size was
     * accepted by the FMU.
     * @exception IllegalActionException If fmiDoStep() returns
     * anything other than fmiDiscard or fmiOK.
     */
    protected Time _fmiDoStepHybrid(Time newTime, int newMicrostep)
            throws IllegalActionException {
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // By default, the FMU does not suggest a refined step size,
        // something we indicate with a -1.0.
        double result = -1;

        int timeAdvance = newTime.compareTo(_lastFireTime);

//        System.out.println("timeAdvance: " + timeAdvance);
//        System.out.println("newTime: " + newTime + ", _lastFireTime: " + _lastFireTime + ", _lastCommitTime: " + _lastCommitTime);
        // FIXME: Should perhaps check to see whether we are at phase 0 of a
        // non-zero-step-size
        // invocation by the ContinuousDirector. This invocation yields a
        // spurious zero-step-size
        // fmiDoStep for the FMU.
        if (timeAdvance != 0 || (newMicrostep > _lastFireMicrostep)) {
            
            // Time or microstep has advanced or time has declined
            // since the last invocation of fire() or initialize().
            // Even if only the microstep has advanced, we should still call
            // fmiDoStep() because the FMU may require it for zero-step-size
            // iterations
            // (the standard is not clear about this).
            //
            // When calling fmiDoStep(), the time argument is the _start_
            // of the integration interval, which is not the current time, in
            // general.
            // We are calling fmiDoStep() to advance to current time, which is
            // therefore
            // the _end_ of the integration interval.
            long time = _lastFireTime.getLongValue();

            // Compute the step size.
            long stepSize = newTime.subtract(_lastFireTime).getLongValue();

            byte lastArg = 1;

            // If we have moved backwards in time, then we are redoing an
            // integration step since the last doStep().
            // This imply a roll-back.            
            if (timeAdvance < 0) {
                // Correct the above values to indicate that we are redoing a
                // step.
                rollBackToCommittedState();
                time = _lastCommitTime.getLongValue();
                stepSize = newTime.subtract(_lastCommitTime).getLongValue();
                lastArg = 0;
            }

            if (_firstFireInIteration) {
                lastArg = 1;
            } else {
                lastArg = 0;
            }

            if (_debugging) {
                String lastArgDescription = ", /* newStep */";
                    lastArgDescription = ", /* noSetFMUStatePriorToCurrentPoint */";
                _debugToStdOut("FMIImport.fire(): about to call "
                        + modelIdentifier + "_fmiDoStepHybrid(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize
                        + lastArgDescription + lastArg + ")");
            }
            
            int fmiFlag = ((Integer) _fmiDoStepFunction.invokeInt(new Object[] {
                    _fmiComponent, time, stepSize, lastArg })).intValue();

            // If the FMU discarded the step, handle this.
            if (fmiFlag == FMILibrary.FMIStatus.fmiDiscard) {
//                System.out.println("-> Step Discarded!");
                if (_debugging) {
                    _debugToStdOut("Rejected step size of " + stepSize
                            + " at time " + time);
                }
                // By default, if the FMU does not provide better information,
                // we suggest a refined step size of half the current step size.
                result = stepSize * 0.5;

                if (_fmiGetRealStatusFunction != null) {
//                    System.out.println("-> _fmiGetRealStatusFunction!");
                    // The FMU has provided a function to query for
                    // a suggested step size.
                    // This function returns fmiDiscard if not supported.
                    IntBuffer valueBuffer = IntBuffer.allocate(1);
                    fmiFlag = ((Integer) _fmiGetIntegerStatusFunction
                            .invokeInt(new Object[] {
                                    _fmiComponent,
                                    FMILibrary.FMIStatusKind.fmiLastSuccessfulTime,
                                    valueBuffer })).intValue();
                    if (fmiFlag == FMILibrary.FMIStatus.fmiOK) {
                        long lastSuccessfulTime = valueBuffer.get(0);
                        if (_debugging) {
                            _debug("FMU reports last successful time of "
                                    + lastSuccessfulTime);
                        }
                        // Sanity check the time to make sure it makes sense.
                        if (lastSuccessfulTime < _lastCommitTime.getLongValue()) {
                            throw new IllegalActionException(
                                    this,
                                    "FMU Rejected step size of "
                                            + stepSize
                                            + " at time "
                                            + time
                                            + ", and returns a last successful time of "
                                            + lastSuccessfulTime
                                            + ", which is less than the last commit time of "
                                            + _lastCommitTime);
                        }
                        // Adjust the return result with a suggested time.
                        result = lastSuccessfulTime - _lastCommitTime.getLongValue();
                    } else {
                        if (_debugging) {
                            _debug("FMU does not report a last successful time.");
                        }
                    }
                } else {
                    if (_debugging) {
                        _debug("FMU does not provide a procedure fmiGetRealStatus.");
                    }
                }
            } else if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                // FIXME: Handle fmiPending and fmiWarning.
                throw new IllegalActionException(this, "Could not simulate, "
                        + modelIdentifier + "_fmiDoStepHybrid(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize
                        + ", /* newStep */ 1) returned "
                        + _fmiStatusDescription(fmiFlag));
            } else {
//                System.out.println("-> Step Accepted!");
                _lastFireTime = newTime;
                _lastFireMicrostep = newMicrostep;
                result = newTime.getLongValue();
            }
            if (_debugging) {
                _debugToStdOut("FMUImport done calling " + modelIdentifier
                        + "_fmiDoStepHybrid()");
            }
        }
        result = result * getDirector().getTimeResolution();
        return new Time(getDirector(), result);
    }

    /**
     * Set a scalar variable of the FMU to the value of a Ptolemy token.
     * This method works with the new proposal of FMI for Hybrid Co-Simulation.
     * We suppose FMUs able to handle an explicit notion of "absent" signal.
     * 
     * @param scalar the FMI scalar to be set.
     * @param token the Ptolemy token that contains the value to be set.
     * @exception IllegalActionException If the scalar is of a type
     * that is not handled or if the type of the token does not match
     * the type of the scalar.
     */
    protected void _setFMUScalarVariable(FMIScalarVariable scalar, Token token)
            throws IllegalActionException {
        boolean isAbsent = false;
        try {
            // Check if the input token is absent
            if (token instanceof AbsentToken) {
                isAbsent = true;
            }

            // FIXME: What about arrays?
            if (scalar.type instanceof FMIBooleanType) {
                if (!isAbsent) {
                    scalar.setBooleanHybrid(_fmiComponent,
                            ((BooleanToken) token).booleanValue(),
                            isAbsent);
                } else {
                    scalar.setBooleanHybrid(_fmiComponent,
                            false, isAbsent);
                }                
            } else if (scalar.type instanceof FMIIntegerType) {
                // FIXME: handle Enumerations?
                if (!isAbsent) {
                    scalar.setIntHybrid(_fmiComponent,
                            ((IntToken) token).intValue(),
                            isAbsent);
                } else {
                    scalar.setIntHybrid(_fmiComponent,
                            0,
                            isAbsent);
                }
            } else if (scalar.type instanceof FMIRealType) {
                if (!isAbsent) {
                    scalar.setDoubleHybrid(_fmiComponent,
                            ((DoubleToken) token).doubleValue(),
                            isAbsent);
                } else {
                    scalar.setDoubleHybrid(_fmiComponent,
                            0.0,
                            isAbsent);
                }
            } else if (scalar.type instanceof FMIStringType) {
                if (!isAbsent) {
                    scalar.setStringHybrid(_fmiComponent,
                            ((StringToken) token).stringValue(),
                            isAbsent);
                } else {
                    scalar.setStringHybrid(_fmiComponent,
                            "",
                            isAbsent);
                }  
            } else {
                throw new IllegalActionException("Type " + scalar.type
                        + " not supported.");
            }
        } catch (ClassCastException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not cast a token \"" + token + "\" of type "
                            + token.getType()
                            + " to an FMI scalar variable of type "
                            + scalar.type);
        }
    }
    
    /**
     * @throws IllegalActionException 
     * @throws NoRoomException 
     * 
     */
    protected void _getFmuOutputs() throws NoRoomException, IllegalActionException {
        
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        
        for (Output output : _getOutputs()) {
            TypedIOPort port = output.port;
            if (_debugging) {
                _debugToStdOut("FMUImportHybrid._getFmuOutputs(): port "
                        + port.getName());
            }

            // If the output port has already been set, then
            // skip it.
            // FIXME: This will not work with SDF because the port
            // will likely be known but not have a token in it.
            // We have to also check to make sure that the destination
            // ports have a token.
            // Even better, we should keep track locally of whether
            // we've produced an output in this iteration.
            if (_skipIfKnown() && port.isKnown(0)) {
                continue;
            }

            // Next, we need to check whether the input ports that
            // the output depends on are known. By default, an
            // output depends on _all_ inputs, but if there is
            // a DirectDependency element in the XML file, then
            // the output may depend on only _some_ inputs.
            boolean foundUnknownInputOnWhichOutputDepends = false;
            if (output.dependencies != null) {
                // The output port has some declared dependencies.
                // Check only those ports.
                for (TypedIOPort inputPort : output.dependencies) {
                    if (_debugging) {
                        _debugToStdOut("FMUImportHybrid._getFmuOutputs(): port "
                                + port.getName()
                                + " depends on " 
                                + inputPort.getName());
                    }
                    // The snipped below is important! I commented it out to use IntegratorWithReset without reset                    
                    if (!inputPort.isKnown(0)) {
                        // Skip this output port. It depends on
                        // unknown inputs.
                        if (_debugging) {
                            _debugToStdOut("FMUImportHybrid._getFmuOutputs(): "
                                    + "FMU declares that output port "
                                    + port.getName()
                                    + " depends directly on input port "
                                    + inputPort.getName()
                                    + ", but the input is not yet known.");
                        }
                        foundUnknownInputOnWhichOutputDepends = true;
                        break;
                    }
                }
            } else {
                // No directDependency is given.
                // This means that the output depends on all
                // inputs, so all inputs must be known.
                List<TypedIOPort> inputPorts = inputPortList();
                for (TypedIOPort inputPort : inputPorts) {
                    if (_debugging) {
                        _debugToStdOut("FMUImportHybrid._getFmuOutputs(): port "
                                + port.getName()
                                + " looking for unknown input"
                                + inputPort.getName());
                    }
                    if (inputPort.getWidth() < 0 || !inputPort.isKnown(0)) {
                        // Input port value is not known.
                        foundUnknownInputOnWhichOutputDepends = true;
                        if (_debugging) {
                            _debugToStdOut("FMUImportHybrid._getFmuOutputs(): "
                                    + "FMU does not declare input dependencies, "
                                    + "which means that output port "
                                    + port.getName() + " depends directly on all "
                                    + "input ports, including "
                                    + inputPort.getName()
                                    + ", but this input is not yet known.");
                        }
                        break;
                    }
                }
            }
            if (_debugging) {
                _debugToStdOut("FMUImportHybrid._getFmuOutputs(): port "
                        + port.getName()
                        + " foundUnknownInputOnWhichOutputDepends: "
                        + foundUnknownInputOnWhichOutputDepends);
            }
            if (!foundUnknownInputOnWhichOutputDepends) {
                // Ok to get the output. All the inputs on which
                // it depends are known.
                Token token = null;
                Token result = null;
                FMIScalarVariable scalarVariable = output.scalarVariable;

                if (scalarVariable.type instanceof FMIBooleanType) {
                    result = scalarVariable.getBooleanHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    result = scalarVariable.getIntHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    result = scalarVariable.getDoubleHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    result = scalarVariable.getStringHybrid(_fmiComponent);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }
                if (!(result instanceof AbsentToken)) {
                    token = result;
                    port.send(0, token);
                } else {
                    port.sendClear(0);
                }
                if (_debugging) {
                    _debugToStdOut("FMUImportHybrid._getFmuOutputs(): Output "
                            + scalarVariable.name + " sends value " + token
                            + " at time " + currentTime + " and microstep "
                            + currentMicrostep);
                }
                
            }
        }
    }
    
    /**
     * Iterate through the scalarVariables and set all the inputs
     * that are known.
     * 
     * @throws IllegalActionException 
     * @throws NoTokenException 
     * 
     */
    protected void _setFmuInputs() throws NoTokenException, IllegalActionException {
        for (Input input : _getInputs()) {
            if (input.port.getWidth() > 0) {
                if (input.port.hasToken(0)) {
                    Token token = input.port.get(0);
                    _setFMUScalarVariable(input.scalarVariable, token);                    
                    if (_debugging) {
                        _debugToStdOut("FMUImportHybrid._setFmuInputs(): set input variable " 
                                        + input.scalarVariable.name 
                                        + " to "
                                        + token);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param time
     * @return
     */
    protected long _toGlobalTime(long time) {
        return 0;
    }
    /**
     * 
     * @param time
     * @return
     */
    protected long _toLocalTime(long time) {
        return 0;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private Function _fmiGetMaxStepSize;
    
    private long _stepSize;
}
