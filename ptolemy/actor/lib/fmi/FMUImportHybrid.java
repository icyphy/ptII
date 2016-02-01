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
import ptolemy.actor.SuperdenseTimeDirector;
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
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fmima.kernel.FMIMADirector;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/**
 * Import a Hybrid Co-Simulation FMU.
 * This actor is strongly based on FMUImport.java developed
 * by Christopher Brooks, Michael Wetter, Edward A. Lee.
 *
 * @author Fabio Cremona
 * @version $Id: FMUImportHybrid.java$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */

public class FMUImportHybrid extends FMUImport {

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
    }

    /**
     * Invoke set() and get() on the FMU for the currently known inputs.
     * The inputs of this actor are by default unknown. Every time we
     * fire() this actor, we supply new known inputs that can be set
     * on the FMU. This allows to compute new outputs on the FMU that
     * are captured and sent to the output port of this actor.
     * When all the inputs are known, the FMU compute the default step
     * size by invoking fireAt().
     *
     * @exception IllegalActionException If the FMU indicates a failure.
     */
    @Override
    public void fire() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 0;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }

        if (_debugging) {
            _debug("Fire() at time "
                    + currentTime 
                    + " and microstep "
                    + currentMicrostep);
        }
        
        // Set Inputs value to the FMU
        _setFmuInputs();
        
        // Get Outputs value from the FMU and produce them.
        _getFmuOutputs();
        
        // If all inputs are known, the FMU can request a time advancement.
        // The argument to getNextStepSize() is used as default step size for
        // FMUs that does not implement getMaxStepSize().
        // If the FMU returns a step size < 0, getNextStepSize() returns null.
        // This return value must be interpreted as an unbounded step size:
        // the FMU does not constrain the advancement of time. Therefore, in
        // this case we do not propose a time advancement to the Director
        // with fireAt().
        boolean allInputsKnown = _allInputsAreKnown();
        if (allInputsKnown) {
            Time proposedTime = getNextStepSize(currentTime.add(1.0));
            if (proposedTime != null) {                                

                // If the FMU request superdense time iteration increment
                // currentMicrostep. Otherwise set currentMicrostep to 0/
                int secondCompare = proposedTime.compareTo(currentTime);
                int proposedMicrostep = currentMicrostep;
                if (secondCompare > 0) {
                    proposedMicrostep = 0;
                }
                // Now we can request a new firing.
                Time actualTime = currentTime;
                actualTime = director.fireAt(this, proposedTime, proposedMicrostep);          
                _proposedTime = proposedTime;
                _proposedMicrostep = proposedMicrostep;
                if (_debugging) {
                    _debug("* Invoked fireAt(/*proposed time*/ "
                            + proposedTime
                            + ", /*proposed microstep*/ "
                            + _proposedMicrostep
                            + " ) and the director returned: "
                            + actualTime);
                }
            }   
        } else {
            // If fire() has been invoked and I am here, means that the actor
            // received an event but not all the inputs are known
            if (_debugging) {
                _debug("* Invoked fire() but not all the inputs are known");
            }
        }
        _firstFireInIteration = false;
    }
    
    /**
     * Import a FMUFile.
     *
     * @param originator The originator of the change request.
     * @param fmuFileParameter The .fmuFile
     * @param context The context in which the FMU actor is created.
     * @param x The x-axis value of the actor to be created.
     * @param y The y-axis value of the actor to be created.
     * @param modelExchange True if the FMU should be imported as
     * a model exchange FMU.
     * @exception IllegalActionException If there is a problem
     * instantiating the actor.
     * @exception IOException If there is a problem parsing the fmu file.
     */
    public static void importFMU(Object originator,
            FileParameter fmuFileParameter, NamedObj context, double x,
            double y, boolean modelExchange) throws IllegalActionException,
            IOException {

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
            _debug("FMUImportHybrid.initialize() method called.");
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
        _proposedMicrostep = 0;

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
                        || scalar.variability == FMIScalarVariable.Variability.fixed) // FMI-2.0rc1
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
            _debug("FMUImportHybrid.initialize() call completed.");
        }
        
        if (director instanceof DEDirector) {
            director.fireAt(this, new Time(director, 0.0), _proposedMicrostep);
        }
        
        _proposedTime = startTime;
        
        return;
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
            _debug("Postfire()");
            _debug("* Time = (" + getDirector().getModelTime() + ", " + ((SuperdenseTimeDirector) getDirector()).getIndex() + "), _lastCommitTime = " + _lastCommitTime);
        }

        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 0;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        
        Time expectedNewTime = null;
        int expectedNewMicrostep = currentMicrostep;
        
        if (director instanceof ContinuousDirector) {
            expectedNewTime = new Time(director, ((ContinuousDirector) director).suggestedStepSize());
            expectedNewTime = expectedNewTime.add(currentTime);
        } else if (director instanceof DEDirector) {
            expectedNewTime = ((DEDirector) director).getModelNextIterationTime();
        } else if (director instanceof FMIMADirector) {
            expectedNewTime = ((FMIMADirector) director).getModelNextIterationTime();
        } else {
            throw new IllegalActionException(
                    this,
                    getDirector(),
                    "This actor works only with CT and DE Directors.");
        }
        
        if (expectedNewTime.compareTo(currentTime) == 0) {
            expectedNewMicrostep++;
        } else if (expectedNewTime.compareTo(currentTime) > 0) {
            expectedNewMicrostep = 0;
        }
        
        if (_debugging) {
            _debug("* Invoking _fmiDoStepHybrid at time: "
                    + currentTime
                    + ", the expected future time is: "
                    + expectedNewTime
                    + ", the expected future microstep is: "
                    + expectedNewMicrostep);
        }
        
        if (expectedNewTime.compareTo(currentTime) < 0) {
            throw new IllegalActionException(this, "Expected negative time advancement for this FMU");
        }        

        // Advance the state of the FMU
        Time computedTime = _fmiDoStepHybrid(expectedNewTime, expectedNewMicrostep);
        
        if (_debugging) {
            _debug("* _fmiDoStepHybrid computed a time advancement at time: "
                    + computedTime);
        }
        
        // This is a sanity check.
        // It cannot happen that an FMU rejects a time step smaller or equal to the
        // time stamp it proposed. 
        if ((computedTime.compareTo(expectedNewTime) < 0) 
                && !(getDirector() instanceof DEDirector)
                && !(getDirector() instanceof SRDirector)) {
            throw new IllegalActionException(
                    this,
                    getDirector(),
                    "FMU discarded a step, rejecting the director's step size." 
                    + " In postfire() the stepsize is supposed to be accepted.");
        }
        

        // In case the performed step size is smaller than the step size proposed
        // from the FMU with fireAt(), we have to cancel fireAt().
        if (((_proposedTime.compareTo(computedTime) > 0) ||
                (_proposedTime.compareTo(computedTime) == 0 &&
                 _proposedMicrostep > currentMicrostep)) &&
                (_allInputsAreKnown() && (inputPortList().size() > 0))) {
            // This can happen only with the DE Director
            if (director instanceof DEDirector) {
                if (_debugging) { 
                    _debug("* Cancelling fireAt(/*proposed time*/ "
                            + _proposedTime
                            + ", /*proposed microstep*/ "
                            + _proposedMicrostep
                            + " )");
                }
                // Cancel the step size
                ((DEDirector) director).cancelFireAt(this, _proposedTime, _proposedMicrostep);
                // Schedule a new event
                // TODO: check if the fireAt should be actually at
                // currentTime and currentMicrostep
                director.fireAt(this, expectedNewTime, expectedNewMicrostep);
            }
        }
        _lastCommitTime = computedTime;
        _refinedStepSize = -1.0;
        _firstFireInIteration = true;
        _proposedMicrostep = 0;

        return !_stopRequested;
    }

    /**
     * Instantiate the slave FMU component.
     *
     * @exception IllegalActionException If it cannot be instantiated.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debug("FMUImportHybrid.preinitialize()");
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
    }
   
    /**
     * Compute the step size for the FMU. This can happen in two ways.
     * If the FMU implements getMaxStepSize(), we simply use it.
     * Otherwise, we propose a default step size to the FMU. 
     * The FMU in this case can partially accept the step size.
     * We can check this looking at the internal state of the FMU.
     * _fmiDoStepHybrid() returns the step size actually accepted from the FMU.
     * @param proposedTime The proposed time
     * @return the new time
     * @exception IllegalActionException If thrown by while doing the step.
     */    
    public Time getNextStepSize(Time proposedTime) throws IllegalActionException {
        int currentMicrostep = 0;
        Director director = getDirector();
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        
        if (_debugging) {
            _debug("* getNextStepSize("
                    + proposedTime
                    + ", "
                    + currentMicrostep
                    + ")");
        }
        
        LongBuffer stepSizeBuffer = LongBuffer.allocate(1);
        Time newTime = null;
        
        if (_fmiGetMaxStepSize != null) {
            int fmiFlag = ((Integer) _fmiGetMaxStepSize.invokeInt(new Object[] {
                    _fmiComponent, stepSizeBuffer})).intValue();
            if (fmiFlag >= FMILibrary.FMIStatus.fmiDiscard) {
                if (_debugging) {
                    _debug("** Error while getMaxStepSize()"
                            + " at time " + director.getModelTime());
                }
            }
            long stepSize = stepSizeBuffer.get(0);   
            if (stepSize >= 0) {
                newTime = new Time(director, stepSizeBuffer.get(0)
                        + _lastCommitTime.getLongValue());
            }
            if (_debugging) {
                _debug("** getMaxStepSize returned step size = "
                            + stepSize +" and newTime = " + newTime);
            }
            
        } else {
            newTime = _fmiDoStepHybrid(proposedTime, currentMicrostep);
            if (_debugging) {
                _debug("** _fmiDoStepHybrid returned a refined step size = " + newTime);
            }
        }     
        return newTime;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /**
     * Returns true if all inputs are known. Return false if at least
     * one of the inputs is unknown. If an FMU has no inputs it also
     * returns true.
     * @return true if all the inputs are known.
     * @throws IllegalActionException If throw while getting the width
     * of an input port or checking if an input port is known.
     */
    protected boolean _allInputsAreKnown() throws IllegalActionException {
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
     * Initialize the FMU.
     * This _fmiInitialize() is specific for FMI 2.1 Hybrid Co-Simulation
     */
    protected void _fmiInitialize() throws IllegalActionException {
        if (_debugging) {
            _debug("FMUImportHybrid._fmiInitialize()");
        }
        
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        Director director = getDirector();
        Time startTime = director.getModelStartTime();
        Time stopTime = director.getModelStopTime();
        int fmiFlag;

        if (_debugging) {
            _debug("FMUImportHybrid._fmiInitialize(): about to "
                    + "invoke the fmi setup experiment function");
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
            _debug("FMUImportHybrid._fmiInitialize(): about to invoke "
                    + "the fmi enter initialization function");
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
            _debug("FMUImportHybrid._fmiInitialize(): about to invoke "
                    + "the fmi exit initialization function");
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
            _debug("FMUImportHybrid._fmiInitialize(): about to request "
                    + "refiring if necessary.");
        }

        // If the FMU can provide a maximum step size, query for the
        // initial maximum
        // step size and call fireAt() and ensure that the FMU is
        // invoked
        // at the specified time.
        _requestRefiringIfNecessary();

        if (_debugging) {
            _debug("FMUImportHybrid._fmiInitialize(): about to record "
                    + "FMU state.");
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
            _debug("Initialized FMU.");
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

        if (_debugging) {
            _debug("* _fmiDoStepHybrid at time "
                    + getDirector().getModelTime());
            _debug("** newTime: " + newTime
                    + ", newMicrostep: " + newMicrostep
                    + ", _lastFireTime: " + _lastFireTime
                    + ", _lastFireMicrostep: " + _lastFireMicrostep);
        }
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
                if (_debugging) {
                    _debug("** Rolling back the FMU! "
                            + "The new current time is previous than the "
                            + "last committed time.");
                }
                rollBackToCommittedState();
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
                _debug("** About to call "
                        + modelIdentifier + "_fmiDoStepHybrid(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize
                        + lastArgDescription + lastArg + ")");
            }
            
            int fmiFlag = ((Integer) _fmiDoStepFunction.invokeInt(new Object[] {
                    _fmiComponent, time, stepSize, lastArg })).intValue();

            // If the FMU discarded the step, handle this.
            if (fmiFlag == FMILibrary.FMIStatus.fmiDiscard) {
                if (_debugging) {
                    _debug("** FMU rejected step size of " + stepSize
                            + " at time " + time);
                }

                if (_fmiGetIntegerStatusFunction != null) {
                    // The FMU has provided a function to query for
                    // a suggested step size.
                    // This function returns fmiDiscard if not supported.
                    LongBuffer valueBuffer = LongBuffer.allocate(1);
                    fmiFlag = ((Integer) _fmiGetIntegerStatusFunction
                            .invokeInt(new Object[] {
                                    _fmiComponent,
                                    FMILibrary.FMIStatusKind.fmiLastSuccessfulTime,
                                    valueBuffer })).intValue();
                    if (fmiFlag == FMILibrary.FMIStatus.fmiOK) {
                        long lastSuccessfulTime = valueBuffer.get(0);
                        if (_debugging) {
                            _debug("** FMU reports last successful time of "
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
                        result = lastSuccessfulTime;
                    } else {
                        if (_debugging) {
                            _debug("** FMU does not report a last successful time.");
                        }
                    }
                } else {
                    if (_debugging) {
                        _debug("** FMU does not provide a procedure fmiGetIntegerStatus.");
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
                _lastFireTime = newTime;
                _lastFireMicrostep = newMicrostep;
                result = newTime.getLongValue();
                if (_debugging) {
                    _debug("** FMU accepted the step size. "
                            + "New FMU time is: " + result);
                }
            }
            if (_debugging) {
                _debug("** FMUImportHybrid done calling " + modelIdentifier
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
     * Retrieve the outputs from the FMU. Only outputs with known inputs are
     * retrieved. In an output cannot be retrieved, an AbsentToken is sent
     * on the correspondent actor port. In case the output can be retrieved
     * (all the dependent inputs are known) than we get this value, we
     * create a Token and we send the token on the correspondent output port.
     * 
     * @throws IllegalActionException If thrown while getting the inputs, determining
     * if the inputs are known or have a token
     */
    protected void _getFmuOutputs() throws IllegalActionException {
        
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        
        for (Output output : _getOutputs()) {
            TypedIOPort port = output.port;
            if (_debugging) {
                _debug("* FMUImportHybrid._getFmuOutputs(): port "
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
                        _debug("** port "
                                + port.getName()
                                + " depends on " 
                                + inputPort.getName());
                    }
                    // The snippet below is important! I commented it out to use
                    // IntegratorWithReset without reset                    
                    if (!inputPort.isKnown(0)) {
                        // Skip this output port. It depends on
                        // unknown inputs.
                        if (_debugging) {
                            _debug("** port "
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
                        _debug("** port "
                                + port.getName()
                                + " looking for unknown input"
                                + inputPort.getName());
                    }
                    if (inputPort.getWidth() < 0 || !inputPort.isKnown(0)) {
                        // Input port value is not known.
                        foundUnknownInputOnWhichOutputDepends = true;
                        if (_debugging) {
                            _debug("** FMU does not declare input dependencies, "
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

            if (!foundUnknownInputOnWhichOutputDepends) {
                // Ok to get the output. All the inputs on which
                // it depends are known.
                Token token = null;
                FMIScalarVariable scalarVariable = output.scalarVariable;
                if (_debugging) {
                    _debugToStdOut("** All inputs known at time: "
                            + getDirector().getModelTime());
                }

                if (scalarVariable.type instanceof FMIBooleanType) {
                    token = scalarVariable.getBooleanHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    token = scalarVariable.getIntHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    token = scalarVariable.getDoubleHybrid(_fmiComponent);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    token = scalarVariable.getStringHybrid(_fmiComponent);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }
                if (!(token instanceof AbsentToken)) {
                    port.send(0, token);
                    if (_debugging) {
                        _debug("** Output "
                                + scalarVariable.name + " sends value " + token
                                + " at time " + currentTime + " and microstep "
                                + currentMicrostep);
                    }
                } else {
                    port.sendClear(0);
                    if (_debugging) {
                        _debug("** Output "
                                + scalarVariable.name + " is Absent"
                                + " at time " + currentTime + " and microstep "
                                + currentMicrostep);
                    }
                }
            } else {
                if (_debugging) {
                    _debugToStdOut("** Not all inputs known at time: "
                            + getDirector().getModelTime());
                }
            }
        }
    }
    
    /**
     * Iterate through the scalarVariables and set all the inputs
     * that are known.
     * 
     * @throws IllegalActionException If thrown while getting the inputs, determining
     * if the inputs are known or have a token
     */
    protected void _setFmuInputs() throws IllegalActionException {
        for (Input input : _getInputs()) {
            if (input.port.getWidth() > 0 && input.port.isKnown(0)) {
                if (input.port.hasToken(0)) {
                    Token token = input.port.get(0);
                    _setFMUScalarVariable(input.scalarVariable, token);                    
                    if (_debugging) {
                        _debug("* FMUImportHybrid._setFmuInputs(): set input variable " 
                                        + input.scalarVariable.name 
                                        + " to "
                                        + token);
                    }
                }
                else {
                    AbsentToken token = new AbsentToken();
                    _setFMUScalarVariable(input.scalarVariable, token);   
                    if (_debugging) {
                        _debug("FMUImportHybrid._setFmuInputs(): set input variable " 
                                        + input.scalarVariable.name 
                                        + " to Absent");
                    }
                }
            } else {
                AbsentToken token = new AbsentToken();
                _setFMUScalarVariable(input.scalarVariable, token);   
                if (_debugging) {
                    _debug("FMUImportHybrid._setFmuInputs(): set input variable " 
                                    + input.scalarVariable.name 
                                    + " to Absent");
                }
            }           
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * An experimental function that returns the maximum step size 
     * that an FMU can compute. 
     */
    private Function _fmiGetMaxStepSize;

    /**
     * This is the proposed microstep. We save it because probably we need
     * to cancel the fireAt.
     */
    int _proposedMicrostep;
    
    /**
     * This is the time proposed in fire() from the FMU.
     * It can happen that the FMU will receive an input event
     * and we need to cancel that event.
     */
    Time _proposedTime;
}
