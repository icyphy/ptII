/* Instantiate a Functional Mock-up Unit (FMU).

   Copyright (c) 2011-2013 The Regents of the University of California.
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIEventInfo;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.NativeSizeT;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import org.ptolemy.fmi.type.FMIType;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.continuous.Advanceable;
import ptolemy.actor.continuous.ContinuousStatefulComponent;
import ptolemy.actor.continuous.ContinuousStatefulDirector;
import ptolemy.actor.continuous.ContinuousStepSizeController;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.PeriodicDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Co-Simulation
 * Functional Mock-up Unit (FMU).
 *
 * <p>Read in a <code>.fmu</code> file named by the
 * <i>fmuFile</i> parameter.  The <code>.fmu</code> file is a zipped
 * file that contains a file named <code>modelDescription.xml</code>
 * that describes the ports and parameters that are created.
 * At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code>
 * file.</p>
 *
 * <p>To use this actor from within Vergil, use File -&gt; Import -&gt; Import
 * FMU, which will prompt for a .fmu file. This actor is <b>not</b>
 * available from the actor pane via drag and drop. The problem is
 * that dragging and dropping this actor ends up trying to read
 * fmuImport.fmu, which does not exist.  If we added such a file, then
 * dragging and dropping the actor would create an arbitrary actor
 * with arbitrary ports.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * <p>
 * Under the Continuous director, this actor invokes fmiDoStep() at the
 * beginning of the fire() method whenever time has advanced since the
 * last invocation of the fire() method. If at this point the FMU
 * discards the step, then the fire method will nonetheless produce
 * outputs, but they may not be valid for the current time.
 * This should not matter because the Continuous director will call
 * {@link #isStepSizeAccurate()}, which will return false, and it will
 * revert to the last committed and proceed with a smaller step size.
 * Under the Continuous director, when time is advanced, this actor
 * will be fired several times. The first firing will be at the
 * beginning of the time interval, which matches the last commit time of
 * the last postfire() invocation. The remaining firings will be at
 * times greater than the last commit time. These firings are all
 * speculative, in that any actor may reject the step size when they
 * occur. In the event that a step is rejected, the Continuous director
 * will call {@link #rollBackToCommittedState()}. If the FMU supports
 * it, then this method will use fmiSetFMUstate() to restore the state
 * to the state of the FMU at the time of the last postfire() (or
 * initialize(), if postfire() has not yet been invoked).
 * </p><p>
 * <b>If the FMU does not support rolling back (indicated by the
 * canGetAndSetFMUstate element in the XML file), then this actor
 * assumes that the FMU is stateless and hence can be rolled back
 * without any particular action.</b>  This may not be a good
 * assumption, so it issues a warning.
 * </p>
 *
 * @author Christopher Brooks, Michael Wetter, Edward A. Lee,
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends TypedAtomicActor implements Advanceable,
        ContinuousStepSizeController, ContinuousStatefulComponent {
    // FIXME: For FMI Co-simulation, we want to extend TypedAtomicActor.
    // For model exchange, we want to extend TypedCompositeActor.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUImport(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        fmuFile = new FileParameter(this, "fmuFile");
        fmuFile.setExpression("fmuImport.fmu");
        // The value of this parameter cannot be edited once the FMU has been imported.
        fmuFile.setVisibility(Settable.NOT_EDITABLE);

        fmiVersion = new StringParameter(this, "fmiVersion");
        fmiVersion.setExpression("1.0");
        fmiVersion.setVisibility(Settable.NOT_EDITABLE);

        suppressWarnings = new Parameter(this, "suppressWarnings");
        suppressWarnings.setTypeEquals(BaseType.BOOLEAN);
        suppressWarnings.setExpression("false");

        visible = new Parameter(this, "visible");
        visible.setTypeEquals(BaseType.BOOLEAN);
        visible.setExpression("false");
        
        modelExchange = new Parameter(this, "modelExchange");
        modelExchange.setTypeEquals(BaseType.BOOLEAN);
        modelExchange.setExpression("false");
        modelExchange.setVisibility(Settable.EXPERT);
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-25\" y=\"7\" " + "style=\"font-size:24\">\n"
                + "FMU" + "</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The FMI version of the FMU. This is a string that defaults to "1.0"
     *  if the FMU file does not specify a version, and otherwise is the
     *  version specified in the file.
     */
    public StringParameter fmiVersion;

    /** The Functional Mock-up Unit (FMU) file.  The FMU file is a zip
     *  file that contains a file named "modelDescription.xml" and any
     *  necessary shared libraries.  The file is read when this actor
     *  is instantiated or when the file name changes.  The initial
     *  default value is "fmuImport.fmu".
     */
    public FileParameter fmuFile;
    
    /** If true, then this FMU is for model exchange rather than co-simulation.
     *  This is a boolean that defaults to false.  The value of this parameter
     *  gets determined when FMU is imported. This parameter is set to expert
     *  mode because normally a user should not be allowed to change it.
     */
    public Parameter modelExchange;

    /** If true, suppress warnings about the FMU not being able to roll
     *  back. It is reasonable to set this to true if you know that the
     *  FMU can be executed at an earlier time than it was previously
     *  executed. This is safe, for example, if the FMU is stateless.
     *  This is a boolean that defaults to false.
     */
    public Parameter suppressWarnings;

    /** If true, indicate to the FMU (if it supports it) that it is
     *  allowed to create displays and otherwise interact with the user.
     *  This is a boolean that defaults to false.
     */
    public Parameter visible;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Advance to the specified time.
     *  @param time The time to advance.
     *  @param microstep The microstep to advance.
     *  @return True if advancement to the specified time succeeds.
     *  @exception IllegalActionException If an error occurs advancing time.
     */
    public boolean advance(Time time, int microstep)
            throws IllegalActionException {
        double refinedStepSize = _fmiDoStep(time, microstep);
        if (refinedStepSize >= 0.0) {
            _stepSizeRejected = true;
            if (_refinedStepSize < 0.0 || refinedStepSize < _refinedStepSize) {
                _refinedStepSize = refinedStepSize;
            }
            return false;
        }
        return true;
    }

    /** If the specified attribute is <i>fmuFile</i>, then unzip
     *  the file and load in the .xml file, creating and deleting parameters
     *  as necessary.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *  is <i>fmuFile</i> and the file cannot be opened or there
     *  is a problem creating or destroying the parameters
     *  listed in thile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fmuFile) {
            try {
                _updateParameters();
            } catch (NameDuplicationException e) {
                // This should not occur because the file is not editable.
                throw new IllegalActionException(this, e, "Name duplication");
            }
        } else if (attribute == fmiVersion) {
            try {
                _fmiVersion = Double.parseDouble(fmiVersion.stringValue());
            } catch (NumberFormatException ex) {
                throw new IllegalActionException(
                        this,
                        "Invalid fmiVersion. Required to be of the form n.m, where n and m are natural numbers.");
            }
        } else if (attribute == modelExchange) {
            // If the _fmiModelDescription is null, then this field will be set
            // when _fmiModelDescription is set, which will happen when the fmuFile
            // parameter is changed.
            if (_fmiModelDescription != null) {
                _fmiModelDescription.modelExchange = ((BooleanToken)modelExchange.getToken()).booleanValue();
            }
        }
        super.attributeChanged(attribute);
    }

    /** Set the dependency between all output ports and all input
     *  ports of this actor. By default, each
     *  output port is assumed to have a dependency on all input
     *  ports. If the FMU explicitly declares input dependencies
     *  for a particular output, then then it only depends on those
     *  inputs that it declares.
     *  @exception IllegalActionException Not thrown in this base
     *  class, derived classes should throw this exception if the
     *  delay dependency cannot be computed.
     *  @see #getCausalityInterface()
     *  @see #_declareDelayDependency(IOPort, IOPort, double)
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Iterate through the outputs, and for any output that declares
        // dependencies, indicate a delay dependency for any inputs that
        // it does not mention.
        for (Output output : _getOutputs()) {
            if (output.dependencies == null) {
                // There are no dependencies declared for this output,
                // so the output depends on all inputs.
                continue;
            }
            List<TypedIOPort> inputs = inputPortList();
            for (TypedIOPort input : inputs) {
                if (!output.dependencies.contains(input)) {
                    _declareDelayDependency(input, output.port, 0.0);
                }
            }
        }
    }

    /** Invoke fmiDoStep() of the slave FMU, if necessary to catch
     *  up to current time, and then set the (known) inputs of the
     *  FMU and retrieve and send out any outputs for which all
     *  inputs on which the output depends are known.
     *  @exception IllegalActionException If FMU indicates a failure.
     */
    @SuppressWarnings("deprecation")
    public void fire() throws IllegalActionException {

        /* Martin Arnold <martin.arnold@mathematik.uni-halle.de> explains rollback as follows:

        FMI v2.0 beta 4 supports this re-set mechanism by the fmiGetFMUState()/fmiSetFMUState()
        functions that are designed to be called by the co-simulation master (this might be
        the "orchestrator" in your terminology). Slaves indicate by the corresponding
        capability flag canGetAndSetFMUstate if they do support this functionality or not.

        Slaves that DO NOT support fmiGetFMUState()/fmiSetFMUState() should never be called
        with non-monotone sequences of communication points since there is no FMI-compatible
        way to re-set them to a previous state. This is obviously a strong restriction from
        the classical ODE/DAE time integration viewpoint since step rejections are quite normal
        in this field. On the other hand, most co-simulation slaves in industrial application
        are today not designed "to go back in time", i.e., to be re-set to some previous state.
        These two facts motivate the definition of a capability flag canGetAndSetFMUstate.

        Slaves that DO support fmiGetFMUState()/fmiSetFMUState() may in principle be called
        with non-monotone sequences of communication points if the co-simulation master takes
        care of getting and re-setting the slave FMU state in a reasonable way. I.e., the slave
        FMU is not expected to save and to re-set its state autonomously but only via the calls
        of fmiGetFMUState()/fmiSetFMUState() by the co-simulation master. This strategy is
        obviously independent of the number of slave FMUs in a co-simulation environment
        and supports nested co-simulation environments as well.

        Slave FMUs that support fmiGetFMUState()/fmiSetFMUState() may generate a large amount
        of simulation data that would be written to file during simulation in a classical
        off-line simulation. In co-simulation with non-monotone sequences of communication
        points these data can not be written to file as long as there is a "risk" that the
        slave FMU is re-set to a very early state in time history (e.g., to the initial FMU
        state). Parameter noSetFMUStatePriorToCurrentPoint was added to fmiDoStep() to provide
        the information that the slave FMU will never be re-set to an FMU state prior to the
        current communication point (this information correponds to an "accepted time step"
        in a classical ODE/DAE integrator). Typically, this information could be used to
        flush some result buffers and write simulation data to file etc. Alternatively,
        the slave FMU may simply ignore this parameter.
         */

        super.fire();

        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }

        if (_debugging) {
            _debugToStdOut("FMUImport.fire() at time " + currentTime
                    + " and microstep " + currentMicrostep);
        }

        double derivatives[] = null;
        if (_fmiModelDescription.modelExchange) {
            /////////////////////////////////////////
            // Model exchange version.
            
            // Need to retrieve the derivatives before advancing
            // the time of the FMU.  However, this needs to not be
            // done on the first firing, since the actual integration
            // step will be performed only in subsequent firings.
            if (!_firstFire) {
                derivatives = _fmiGetDerivatives();
            }
                          
            // Set the time.
            // FIXME: Is it OK to do this even if time has not advanced?
            _fmiSetTime(currentTime);

            // NOTE: The FMI standard says that all variable start values
            // (of "ScalarVariable / <type> / start") can be set at this time,
            // but we would only want to do that if we want to influence the
            // convergence of some algebraic solver. Since no Ptolemy director
            // currently supports algebraic solvers, we have no reason to want
            // to set the start value, so we ignore this.
            
        } else {
            /////////////////////////////////////////
            // Co-simulation version.
            
            // If time has changed since the last call to fire(), invoke
            // fmiDoStep() with the current data before updating the inputs
            // of the FMU. The current value of the inputs will be the
            // values set on the last call to fire().
            double refinedStepSize = _fmiDoStep(currentTime, currentMicrostep);
            if (refinedStepSize >= 0.0) {
                _stepSizeRejected = true;
                if (_refinedStepSize < 0.0 || refinedStepSize < _refinedStepSize) {
                    _refinedStepSize = refinedStepSize;
                }
            }
        }

        ////////////////
        // Set the inputs.
        // Iterate through the scalarVariables and set all the inputs
        // that are known.
        // FIXME: Here, we are iterating over a potentially very large number
        // of scalar variables to find a few that are relevant (the input
        // ports with some connection). It would be better to construct _once_
        // a list of such input ports, and then use that list here.
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // If this variable has an alias, then we operate
            // only on the real version, not the alias.
            // In bouncingBall.fmu, g has an alias, so it is skipped.
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                continue;
            }
            // NOTE: Page 27 of the FMI-1.0 CS spec says that for
            // variability==parameter and causality==input, we can
            // only call fmiSet* between fmiInstantiateSlave() and
            // fmiInitializeSlave(). Same for inputs that have
            // variability==constant.
            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter
                    && scalarVariable.variability != FMIScalarVariable.Variability.constant
                    && scalarVariable.causality == Causality.input) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);
                if (port != null) {
                    if (port.isKnown(0)) {
                        if (port.hasToken(0)) {
                            Token token = port.get(0);
                            _setFMUScalarVariable(scalarVariable, token);
                            if (_debugging) {
                                _debugToStdOut("FMUImport.fire(): set input variable "
                                        + scalarVariable.name + " to " + token);
                            }
                        } else {
                            // Port is known to be absent, but FMI
                            // does not support absent values.
                            throw new IllegalActionException(this, "Input "
                                    + scalarVariable.name
                                    + " has value 'absent', but FMI does not "
                                    + "support a notion of absent inputs.");
                        }
                    }
                } else {
                    throw new IllegalActionException(this,
                            "Expected an input port named "
                                    + scalarVariable.name
                                    + ", but there is no such port.");
                }
            }
        }
        
        //////////////////////
        // For model exchange.
        if (_fmiModelDescription.modelExchange) {

            //////////////////////
            // Initialize the FMU if necessary.  Model exchange only.

            if (_firstFire) {
                if (_fmiVersion < 2.0) {
                    _fmiInitialize();

                    // Record the state.
                    _recordFMUState();
                    
                    _lastCommitTime = currentTime;
                } else {
                    // FIXME: Support 2.0
                    throw new IllegalActionException(this,
                            "FMUImport does not yet support model exchange in version 2.0.");
                }
            }
            // If time has advanced since the last
            // firing, perform the forward Euler advance.
            double currentTimeValue = currentTime.getDoubleValue();
            
            double states[] = _states.array();
            if (_newStates == null || _newStates.length != states.length){ 
                _newStates = new double[states.length];
            }
            // Make sure the states of the FMU match the last commit.
            // FIXME: This is only needed if backtracking might occur.
            // Even then, it's incomplete. Probably need to record and reset _all_ variables,
            // not just the continuous states.
            // _fmiSetContinuousStates(states);

            if (currentTimeValue > _lastCommitTime.getDoubleValue()) {
                double step = currentTimeValue - _lastCommitTime.getDoubleValue();
                
                for (int i = 0; i < states.length; i++) {
                    _newStates[i] = states[i] + derivatives[i] * step;
                }
                _fmiSetContinuousStates(_newStates);
                
                // Check event indicators.
                boolean stateEventOccurred = _checkEventIndicators();
                
                // FIXME: Check also for time events.
                boolean timeEventOccurred = false;
                
                // Complete the integrator step.
                _fmiCompletedIntegratorStep(stateEventOccurred || timeEventOccurred);
            } else {
                // FIXME: Need to do an event update. Zero step size.
            }
        }
        
        /* The following commented out code was a first attempt to get this FMU
         * to work with the ContinuousDirector and its solver. This seems rather tricky, however,
         * so for now, we just provide a forward-Euler solver.
        ////////////////////////////////
        // For model exchange only, set the continuous states, which are the
        // values on the channels of the continuousStates input port.
        if (_fmiModelDescription.modelExchange) {
            TypedIOPort continuousStates = (TypedIOPort)getPort("continuousStates");
            // If there is no such input port, then there must be no continuous states.
            if (continuousStates != null) {
                int numberOfStates = _fmiModelDescription.numberOfContinuousStates;
                if (continuousStates.getWidth() != numberOfStates) {
                    throw new IllegalActionException(this,
                            "Model exchange requires that the width of the continuousStates port, which is "
                            + continuousStates.getWidth()
                            + " match the number of continuous states in the modelDescription.xml file of the FMU, which is "
                            + _fmiModelDescription.numberOfContinuousStates);
                }
                DoubleBuffer states = DoubleBuffer.allocate(numberOfStates);
                for (int i = 0; i < numberOfStates; i++) {
                    if (continuousStates.isKnown(i)) {
                        if (continuousStates.hasToken(i)) {
                            double stateValue = ((DoubleToken)continuousStates.get(i)).doubleValue();
                            states.put(stateValue);
                        } else {
                            // Port is known to be absent, but FMI
                            // does not support absent values.
                            throw new IllegalActionException(this, "Input channel "
                                    + i
                                    + " of continuousStates has value 'absent', but FMI does not "
                                    + "support a notion of absent inputs.");
                        }
                    } else {
                        // Port is known to be absent, but FMI
                        // does not support absent values.
                        throw new IllegalActionException(this, "Input channel "
                                + i
                                + " of continuousStates has unknown value."
                                + " This port should be connected to the output of an Integrator.");
                    }
                }
                int fmiFlag = ((Integer) _fmiSetContinuousStates.invoke(Integer.class, new Object[] {
                    _fmiComponent, states, numberOfStates })).intValue();
            
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new IllegalActionException(this, "Failed to set continuous states at time " + currentTime);
                }
            }
        }
         */
        
        ////////////////
        // Get the outputs from the FMU and produce them..
        for (Output output : _getOutputs()) {

            TypedIOPort port = output.port;

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
                    if (!inputPort.isKnown(0)) {
                        // Skip this output port. It depends on
                        // unknown inputs.
                        if (_debugging) {
                            _debugToStdOut("FMUImport.fire(): "
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
                    if (inputPort.getWidth() < 0 || !inputPort.isKnown(0)) {
                        // Input port value is not known.
                        foundUnknownInputOnWhichOutputDepends = true;
                        break;
                    }
                }
            }
            if (!foundUnknownInputOnWhichOutputDepends) {
                // Ok to get the output. All the inputs on which
                // it depends are known.
                Token token = null;
                FMIScalarVariable scalarVariable = output.scalarVariable;

                if (scalarVariable.type instanceof FMIBooleanType) {
                    boolean result = scalarVariable.getBoolean(_fmiComponent);
                    token = new BooleanToken(result);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    int result = scalarVariable.getInt(_fmiComponent);
                    token = new IntToken(result);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    double result = scalarVariable.getDouble(_fmiComponent);
                    token = new DoubleToken(result);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    String result = scalarVariable.getString(_fmiComponent);
                    token = new StringToken(result);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }

                if (_debugging) {
                    _debugToStdOut("FMUImport.fire(): Output "
                            + scalarVariable.name + " sends value " + token
                            + " at time " + currentTime + " and microstep "
                            + currentMicrostep);
                }
                port.send(0, token);
            }
        }
        
        /* The following was a part of a first attempt to get this FMU to work
         * with the ContinuousDirector's solvers.
        ////////////////////////////////
        // For model exchange only, get the derivatives from the FMU and send
        // them to the derivatives output ports.
        if (_fmiModelDescription.modelExchange) {
            TypedIOPort derivatives = (TypedIOPort)getPort("derivatives");
            // If there is no such input port, then there must be no continuous states.
            if (derivatives != null) {
                int numberOfStates = _fmiModelDescription.numberOfContinuousStates;
                if (derivatives.getWidth() != numberOfStates) {
                    throw new IllegalActionException(this,
                            "Model exchange requires that the width of the derivatives port, which is "
                            + derivatives.getWidth()
                            + " match the number of continuous states in the modelDescription.xml file of the FMU, which is "
                            + _fmiModelDescription.numberOfContinuousStates);
                }
                DoubleBuffer derivativesValues = DoubleBuffer.allocate(numberOfStates);
                
                int fmiFlag = ((Integer) _fmiGetDerivatives.invoke(Integer.class, new Object[] {
                    _fmiComponent, derivativesValues, numberOfStates })).intValue();
            
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new IllegalActionException(this, "Failed to set continuous states at time " + currentTime);
                }

                for (int i = 0; i < numberOfStates; i++) {
                    DoubleToken token = new DoubleToken(derivativesValues.get(i));
                    if (_debugging) {
                        _debugToStdOut("FMUImport.fire(): Sending derivative output on channel "
                                + i + " with value " + token
                                + " at time " + currentTime + " and microstep "
                                + currentMicrostep);
                    }
                    derivatives.send(i, token);
                }
            }
        }
        */

        _firstFireInIteration = false;
        _firstFire = false;
    }

    /** Initialize this FMU wrapper.
     *  For co-simulation, this initializes the FMU.
     *  For model exchange, it does not, because for model exchange, the
     *  inputs at the start time need to be provided prior to initialization.
     *  Initialization will therefore occur in the first invocation of fire().
     *  @exception IllegalActionException If the slave FMU cannot be
     *  initialized.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_debugging) {
            _debugToStdOut("FMIImport.initialize() method called.");
        }
        // Set a flag so the first call to fire() can do appropriate initialization.
        _firstFire = true;
        _firstFireInIteration = true;
        _newStates = null;

        // Set the parameters of the FMU.
        // Loop through the scalar variables and find a scalar
        // variable that has variability == "parameter" and is not an
        // input or output.  We can't do this in attributeChanged()
        // because setting a scalar variable requires that
        // _fmiComponent be non-null, which happens in
        // preinitialize();
        // FIXME: This should probably also be done in attributeChanged(),
        // with checks that _fmiComponent is non-null, so that FMU parameters
        // can be changed during run time.
        for (FMIScalarVariable scalar : _fmiModelDescription.modelVariables) {
            if (scalar.variability == FMIScalarVariable.Variability.parameter
                    && scalar.causality != Causality.input
                    && scalar.causality != Causality.output) {
                String sanitizedName = StringUtilities
                        .sanitizeName(scalar.name);
                Parameter parameter = (Parameter) getAttribute(sanitizedName,
                        Parameter.class);
                if (parameter != null) {
                    _setFMUScalarVariable(scalar, parameter.getToken());
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
            _relativeTolerance = ((ContinuousStatefulDirector) director)
                    .getErrorTolerance();
            _toleranceControlled = (byte) 1; // fmiBoolean
        }

        //////////////////////////////////////////////
        //// model exchange version
        if (_fmiModelDescription.modelExchange) {
            // Indicate that the start time is the last commit time to
            // prevent an integration step in the first firing.
            _lastCommitTime = startTime;
            
            // In case we are running in the DE domain, ensure a firing at
            // the start time. Initialization will be done then.
            // FIXME: Should this be done for co-simulation also?
            director.fireAt(this, startTime);
            
            return;
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
            _debugToStdOut("FMIImport.initialize() call completed.");
        }
    }
    
    /** Import a FMUFile.
     *  @param originator The originator of the change request.
     *  @param fmuFileParameter The .fmuFile
     *  @param context The context in which the FMU actor is created.
     *  @param x The x-axis value of the actor to be created.
     *  @param y The y-axis value of the actor to be created.
     *  @param modelExchange If true, import for model exchange (vs. co-simulation).
     *  @exception IllegalActionException If there is a problem instantiating the actor.
     *  @exception IOException If there is a problem parsing the fmu file.
     */
    public static void importFMU(Object originator, FileParameter fmuFileParameter,
            NamedObj context, double x, double y, boolean modelExchange)
            throws IllegalActionException, IOException {

        String fmuFileName = fmuFileParameter.asFile().getCanonicalPath();
        System.out.println("FMUImport.importFMU(): " + fmuFileName);
        // This method is called by the gui to import a fmu file and create the
        // actor.
        // The primary issue is that we need to define the ports early on and
        // handle
        // changes to the ports.

        // Calling parseFMUFile does not load the shared library.
        // Those are loaded upon the first attempt to use them.
        // This is important because we want to be able to view
        // a model that references an FMU even if the FMU does not
        // support the current platform.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);
        
        // FIXME: Use URLs, not files so that we can work from JarZip files.

        // If a location is given as a URL, construct MoML to
        // specify a "source".
        String source = "";
        // FIXME: not sure about this.
        if (fmuFileName.startsWith("http://")) {
            source = " source=\"" + fmuFileName.trim() + "\"";
        }

        String rootName = new File(fmuFileName).getName();
        int index = rootName.lastIndexOf('.');
        if (index != -1) {
            rootName = rootName.substring(0, index);
        }

        // Instantiate ports and parameters.
        int maximumNumberOfPortsToDisplay = 20;
        int modelVariablesLength = fmiModelDescription.modelVariables.size();
        String hide = "  <property name=\"_hide\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\"/>\n";
        String showName = "    <property name=\"_showName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\"/>\n";
        if (modelVariablesLength > maximumNumberOfPortsToDisplay) {
            MessageHandler.message("Importing \"" + fmuFileName
                    + "\" resulted in an actor with " + modelVariablesLength
                    + "variables.  To show as ports, right click and "
                    + "select Customize -> Ports.");
        }

        int portCount = 0;
        StringBuffer parameterMoML = new StringBuffer();
        StringBuffer portMoML = new StringBuffer();
        for (FMIScalarVariable scalar : fmiModelDescription.modelVariables) {
            if (scalar.variability == FMIScalarVariable.Variability.parameter) {
                // Parameters
                // Parameter parameter = new Parameter(this, scalar.name);
                // parameter.setExpression(Double.toString(((FMIRealType)scalar.type).start));
                // // Prevent exporting this to MoML unless it has
                // // been overridden.
                // parameter.setDerivedLevel(1);

                // FIXME: Need to sanitize the value.
                parameterMoML.append("  <property name=\""
                        + StringUtilities.sanitizeName(scalar.name)
                        + "\" class=\"ptolemy.data.expr.Parameter\" value =\""
                        + scalar.type + "\"/>\n");
            } else {
                // Ports

                // // FIXME: All output ports?
                // TypedIOPort port = new TypedIOPort(this, scalar.name, false,
                // true);
                // port.setDerivedLevel(1);
                // // FIXME: set the type
                // port.setTypeEquals(BaseType.DOUBLE);

                String causality = "";
                switch (scalar.causality) {
                case input:
                    portCount++;
                    causality = "input";
                    break;
                case none:
                    // FIXME: Not sure what to do with causality == none.
                    continue;
                case output:
                    portCount++;
                    // Drop through to internal
                case internal:
                    // Internal ports get hidden.
                    causality = "output";
                    break;
                }

                portMoML.append("  <port name=\""
                        + StringUtilities.sanitizeName(scalar.name)
                        + "\" class=\"ptolemy.actor.TypedIOPort\">\n"
                        + "    <property name=\""
                        + causality
                        + "\"/>\n"
                        + "    <property name=\"_type\" "
                        + "class=\"ptolemy.actor.TypeAttribute\" value=\""
                        + _fmiType2PtolemyType(scalar.type)
                        + "\"/>\n"
                        + showName
                        // Hide the port if we have lots of ports or it is
                        // internal.
                        + (portCount > maximumNumberOfPortsToDisplay
                                || scalar.causality == Causality.internal ? hide
                                : "") + "  </port>\n");
            }
        }
        
        // If importing for model exchange, we also provide input ports
        // for the continuous states and output ports for their derivatives.
        // The width of these ports is determined by the numberOfContinuousStates
        // attribute in the model description.
        if (modelExchange) {
            fmiModelDescription.modelExchange = true;
            
            // Provide a parameter that indicates that this is a model exchange FMU.
            parameterMoML.append("  <property name=\"modelExchange\" class=\"ptolemy.data.expr.Parameter\" value =\"true\"/>\n");

            // Provide a parameter for a forward Euler maximum step size.
            parameterMoML.append("  <property name=\"maximumStepSize\" class=\"ptolemy.data.expr.Parameter\" value =\"0.01\"/>\n");

            /* If we want to use model exchange FMUs with the Continuous director, then
             * we need to provide the derivatives as output ports and the continuousStates as
             * input ports, as follows. This is very tricky, however. The FMU needs to set
             * the states of these integrators after initialization and after event updates.
             * It seems more straightforward to just include a solver here, so we do that for now.
            if (fmiModelDescription.numberOfContinuousStates > 0) {
                // FIXME: What if the names "continuousStates" and "derivatives" collide with a pre-defined port?
                // FIXME: Set the width for the port
                portMoML.append(
                        "   <port name=\"continuousStates\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                + "    <property name=\"input\"/>\n"
                                + "    <property name=\"multiport\"/>\n"
                                + "    <property name=\"_type\" class=\"ptolemy.actor.TypeAttribute\" value=\"double\"/>\n"
                                + showName
                                + "   </port>\n");
                portMoML.append(
                        "   <port name=\"derivatives\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                + "    <property name=\"output\"/>\n"
                                + "    <property name=\"multiport\"/>\n"
                                + "    <property name=\"_type\" class=\"ptolemy.actor.TypeAttribute\" value=\"double\"/>\n"
                                + showName
                                + "   </port>\n");
            }
            if (fmiModelDescription.numberOfEventIndicators > 0) {
                // FIXME: What if the names "eventIndicator" and "eventUpdate" collide with a pre-defined port?
                // FIXME: Set the width for the port
                portMoML.append(
                        "   <port name=\"eventUpdate\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                + "    <property name=\"input\"/>\n"
                                + "    <property name=\"multiport\"/>\n"
                                + "    <property name=\"_type\" class=\"ptolemy.actor.TypeAttribute\" value=\"double\"/>\n"
                                + showName
                                + "   </port>\n");
                portMoML.append(
                        "   <port name=\"eventIndicator\" class=\"ptolemy.actor.TypedIOPort\">\n"
                                + "    <property name=\"output\"/>\n"
                                + "    <property name=\"multiport\"/>\n"
                                + "    <property name=\"_type\" class=\"ptolemy.actor.TypeAttribute\" value=\"double\"/>\n"
                                + showName
                                + "   </port>\n");
            }
            */
        }

        // FIXME: Get Undo/Redo working.

        // Use the "auto" namespace group so that name collisions
        // are automatically avoided by appending a suffix to the name.
        String moml = "<group name=\"auto\">\n" + " <entity name=\"" + rootName
                + "\" class=\"ptolemy.actor.lib.fmi.FMUImport\"" + source
                + ">\n" + "  <property name=\"_location\" "
                + "class=\"ptolemy.kernel.util.Location\" value=\"" + x + ", "
                + y + "\">\n" + "  </property>\n"
                + "  <property name=\"fmuFile\""
                + "class=\"ptolemy.data.expr.FileParameter\"" + "value=\""
                + fmuFileParameter.getExpression() + "\">\n"
                + "  </property>\n" + parameterMoML
                + portMoML + " </entity>\n</group>\n";
        MoMLChangeRequest request = new MoMLChangeRequest(originator, context,
                moml);
        context.requestChange(request);
    }

    /** Return whether the most recent call to fmiDoStep()
     *  succeeded. This will return false if fmiDoStep() discarded
     *  the step. This method assumes that if a director calls
     *  this method, then it will fully handle the discarded step.
     *  If this method is not called, then the next call to postfire()
     *  will throw an exception.
     *  @return True if the current step is accurate.
     */
    public boolean isStepSizeAccurate() {
        boolean result = !_stepSizeRejected;
        if (_stepSizeRejected) {
            _suggestZeroStepSize = true;
        }
        _stepSizeRejected = false;
        return result;
    }

    /** Override the base class to record the current time as the last
     *  commit time.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
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
        _lastCommitTime = director.getModelTime();
        if (_debugging) {
            _debug("Committing to time " + _lastCommitTime);
        }
        _refinedStepSize = -1.0;
        _firstFireInIteration = true;
        
        // If the FMU can provide a maximum step size, query for the maximum
        // step size and call fireAt() and ensure that the FMU is invoked
        // at the specified time.
        _requestRefiringIfNecessary();
                
        // In case we have to backtrack, if the FMU supports backtracking,
        // record its state.
        // FIXME: Not supporting backtracking.
        // _recordFMUState();

        return super.postfire();
    }

    /** Instantiate the slave FMU component.
     *  @exception IllegalActionException if it cannot be instantiated.
     */
    public void preinitialize() throws IllegalActionException {
        // _fmiModelDescription should not be null, as an exception would
        // have been caught by attributeChanged(), but nevertheless, we check
        // here.
        if (_fmiModelDescription == null) {
            throw new IllegalActionException(
                    this,
                    "Reading of FMU file failed, but for some reason wasn't reported when the model was opened.");
        }
        super.preinitialize();
        if (_debugging) {
            _debugToStdOut("FMUImport.preinitialize()");
        }

        try {
            _nativeLibrary = _fmiModelDescription.getNativeLibrary();
        } catch (IOException e1) {
            // Be sure to throw the cause here because if the
            // shared library refers to other libraries that are
            // not found, then the exception should reflect the
            // fact that the library was found but the
            // load failed.  Under Windows, we may get
            // "The specified module could not be found."
            throw new IllegalActionException(this, e1,
                    "Current platform not supported by this FMU");
        }
        if (_nativeLibrary == null) {
            throw new IllegalActionException(this, "No native library found.");
        }
        _checkFmiCommon();

        ////////////////////////////////////////////
        //// model exchange version
        if (_fmiModelDescription.modelExchange){
            _fmiCompletedIntegratorStepFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiCompletedIntegratorStep");
            _fmiFreeModelInstanceFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiFreeModelInstance");
            _fmiGetContinuousStatesFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiGetContinuousStates");
            _fmiSetContinuousStates = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiSetContinuousStates");
            _fmiGetDerivativesFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiGetDerivatives");
            // Optional function? The standard is not clear
            try {
                _fmiGetEventIndicatorsFunction = _nativeLibrary
                        .getFunction(_fmiModelDescription.modelIdentifier
                                + "_fmiGetEventIndicators");
            } catch (UnsatisfiedLinkError ex) {
                // The FMU has not provided the function.
                _fmiGetEventIndicatorsFunction = null;
            }
            _fmiInitializeFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInitialize");
            _fmiInstantiateModelFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInstantiateModel");
            _fmiSetTimeFunction = _nativeLibrary.getFunction(
                    _fmiModelDescription.modelIdentifier + "_fmiSetTime");
            _fmiTerminateFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiTerminate");
            _checkFmiModelExchange();
        } else {
            ////////////////////////////////////////////
            //// co-simulation version
            _fmiDoStepFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiDoStep");
            _fmiFreeSlaveInstanceFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiFreeSlaveInstance");
            _fmiInitializeSlaveFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInitializeSlave");
            _fmiInstantiateSlaveFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiInstantiateSlave");
            _fmiTerminateSlaveFunction = _nativeLibrary
                    .getFunction(_fmiModelDescription.modelIdentifier
                            + "_fmiTerminateSlave");

            // Optional function.
            try {
                _fmiGetRealStatusFunction = _nativeLibrary
                        .getFunction(_fmiModelDescription.modelIdentifier
                                + "_fmiGetRealStatus");
            } catch (UnsatisfiedLinkError ex) {
                // The FMU has not provided the function.
                _fmiGetRealStatusFunction = null;
            }
            if (_fmiModelDescription.canGetAndSetFMUstate) {
                // Retrieve the following FMI 2.0 functions for
                // getting and setting state.
                _fmiFreeFMUstateFunction = _nativeLibrary
                        .getFunction(_fmiModelDescription.modelIdentifier
                                + "_fmiFreeFMUstate");
                _fmiGetFMUstateFunction = _nativeLibrary
                        .getFunction(_fmiModelDescription.modelIdentifier
                                + "_fmiGetFMUstate");
                _fmiSetFMUstate = _nativeLibrary
                        .getFunction(_fmiModelDescription.modelIdentifier
                                + "_fmiSetFMUstate");
            } else {
                _fmiFreeFMUstateFunction = null;
                _fmiGetFMUstateFunction = null;
                _fmiSetFMUstate = null;
            }
            _checkFmiCoSimulation();
        }

        // The modelName may have spaces in it.
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        String fmuLocation = null;
        try {
            // The URL of the fmu file.
            String fmuFileName = fmuFile.asFile().getCanonicalPath();

            fmuLocation = new File(fmuFileName).toURI().toURL().toString();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get the value of \"" + fmuFile + "\"");
        }
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 1000;
        // There is no simulator UI.
        byte toBeVisible = 0;
        if (((BooleanToken) visible.getToken()).booleanValue()) {
            toBeVisible = 1;
        }
        // Run the simulator without user interaction.
        byte interactive = 0;

        // FIXME: We should send logging messages to the debug listener.
        byte loggingOn = _debugging ? (byte) 1 : (byte) 0;
        System.out.println("FMUImport: FIXME: Disabling logging because of errors");
        loggingOn = 0;

        if (_fmiVersion < 2.0) {
            _callbacks = new FMICallbackFunctions.ByValue(
                    new FMULibrary.FMULogger(),
                    new FMULibrary.FMUAllocateMemory(),
                    new FMULibrary.FMUFreeMemory(),
                    new FMULibrary.FMUStepFinished());
            if (_fmiModelDescription.modelExchange){
                if (_debugging) {
                    _debugToStdOut("FMU for model exchange: about to call " + modelIdentifier
                            + "_fmiInstantiateModel");
                }
                _fmiComponent = (Pointer) _fmiInstantiateModelFunction.invoke(
                        Pointer.class, new Object[] {
                            getFullName(),
                            _fmiModelDescription.guid,
                            _callbacks,
                            loggingOn });
            } else {
                if (_debugging) {
                    _debugToStdOut("FMUCoSimulation: about to call " + modelIdentifier
                            + "_fmiInstantiateSlave");
                }
                _fmiComponent = (Pointer) _fmiInstantiateSlaveFunction.invoke(
                        Pointer.class, new Object[] { getFullName(),
                            _fmiModelDescription.guid, fmuLocation, mimeType,
                            timeout, toBeVisible, interactive, _callbacks,
                            loggingOn });
            }
        } else {
            // In FMI 2.0, this is a pointer to the structure, which is by
            // default how a subclass of Structure is handled, so there is no
            // need for the inner class ByValue, as above.
            _callbacks = new FMICallbackFunctions(new FMULibrary.FMULogger(),
                    new FMULibrary.FMUAllocateMemory(),
                    new FMULibrary.FMUFreeMemory(),
                    new FMULibrary.FMUStepFinished());
            // The FMI standard is silent about whether an FMU needs to copy
            // the struct pointed to by the callbacks argument, so we have to
            // assume that the FMU will not. This means that we need to allocate
            // memory here, and deallocate it in wrapup().
                        
            if (_fmiModelDescription.modelExchange){
                if (_debugging) {
                    _debugToStdOut("FMU for model exchange: about to call " + modelIdentifier
                            + "_fmiInstantiateModel");
                }
                // FIXME: Implement this.
                throw new IllegalActionException(this, "Model exchange not yet implemented for FMI 2.0");
            } else {
                if (_debugging) {
                    _debugToStdOut("FMUCoSimulation: about to call " + modelIdentifier
                            + "_fmiInstantiateSlave");
                }

                // FIXME: Check canBeInstantiatedOnlyOncePerProcess capability flag.
                // Do not instantiate if true and previously instantiated.
                _fmiComponent = (Pointer) _fmiInstantiateSlaveFunction.invoke(
                        Pointer.class, new Object[] { getFullName(),
                            _fmiModelDescription.guid, fmuLocation, _callbacks,
                            toBeVisible, loggingOn });
            }
        }

        if (_fmiComponent == null || _fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException(
                    "Could not instantiate Functional Mockup Unit (FMU).");
        }
    }
    
    /** Return suggested refined step size, if the FMU has provided one.
     *  @return The suggested refined step size.
     *  @exception IllegalActionException If the step size cannot be further refined.
     */
    public double refinedStepSize() throws IllegalActionException {
        // Assume director has handled the failed step.
        _stepSizeRejected = false;
        // If a refined step size has been suggested by the FMU,
        // report that. Otherwise, divide the current step size in
        // half, if the director is a ContinuousDirector.
        // Otherwise, make no suggestion.
        if (_refinedStepSize >= 0.0) {
            if (_debugging) {
                _debugToStdOut("===> Suggesting a refined step size of "
                        + _refinedStepSize);
            }
            return _refinedStepSize;
        }
        Director director = getDirector();
        if (director instanceof ContinuousStatefulDirector) {
            double half = ((ContinuousStatefulDirector) director)
                    .getCurrentStepSize() * 0.5;
            if (_debugging) {
                _debugToStdOut("===> Suggesting a refined step size of half the current step size, or "
                        + half);
            }
            return half;
        }
        return Double.MAX_VALUE;
    }

    /** Roll back to committed state, if the FMU has asserted
     *  canGetAndSetFMUstate in the XML file and has provided the methods
     *  to set and restore state. Otherwise, issue a warning.
     *  @exception IllegalActionException If the rollback attempts to go
     *   back further than the last committed time.
     */
    public void rollBackToCommittedState() throws IllegalActionException {
        if (_fmiModelDescription.canGetAndSetFMUstate) {
            // Restore the state to state set in initialize() or postfire()
            // using fmiSetFMUState. The FMU supports this if the XML file
            // asserts that canGetAndSetFMUstate is true.
            // NOTE: During initialize, the FMUstate argument should be the
            // address of a null-valued pointer. During wrapup, the memory
            // should be freed and the FMUstate pointer set to null. If it
            // is already null, the call should be ignored.
            _restoreFMUState();
        } else {
            if (!((BooleanToken) suppressWarnings.getToken()).booleanValue()) {
                try {
                    boolean response = MessageHandler
                            .yesNoCancelQuestion(
                                    "FMU does not support rolling back to a previous state in time, "
                                            + "but it is being asked to roll back from time "
                                            + _lastFireTime + " to time "
                                            + _lastCommitTime, "Proceed",
                                    "Proceed and do not warn me again",
                                    "Cancel");
                    if (!response) {
                        // User has asked to not be warned again.
                        // NOTE: This does not mark the model modified, so
                        // the user does not save, then the warning will reappear next time.
                        suppressWarnings.setToken(BooleanToken.TRUE);
                    }
                } catch (CancelException e) {
                    // User cancelled, so we throw an exception to stop the execution.
                    throw new IllegalActionException(this, e,
                            "Execution cancelled.");
                }
            }
        }
    }

    /** Return the suggested next step size. This method returns 0.0 if
     *  the previous invocation of fmiDoStep() was discarded, in order to
     *  force a zero-time integration step after an event is detected by
     *  the FMU. Otherwise, return Double.MAC_VALUE.
     *  return java.lang.Double.MAX_VALUE.
     *  @return The suggested next step size.
     *  @exception IllegalActionException If an actor suggests an illegal step size.
     */
    public double suggestedStepSize() throws IllegalActionException {
        // If the previous step had a rejected step size, then
        // suggest a zero step size for the next step. This ensures that
        // discrete events are allowed to be produced before the FMU is fired
        // at the start of the next non-zero interval.
        if (_suggestZeroStepSize) {
            _suggestZeroStepSize = false;
            return 0.0;
        }
        // FIXME: Possibly the event mechanism (intended for model exchange,
        // not for cosimulation) could be used to provide some useful value
        // here.
        return java.lang.Double.MAX_VALUE;
    }

    /** Terminate and free the slave fmu.
     *  @exception IllegalActionException If the slave fmu cannot be
     *  terminated or freed.
     */
    public void wrapup() throws IllegalActionException {
        _checkFmiCommon();
        
        _fmiTerminate();
        _fmiFreeInstance();
        _freeFMUState();
        
        // Allow the callback functions structure to be garbage collected.
        _callbacks = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if we are not in the first firing and the
     *  sign of some event indicator has changed.
     *  @return True if a state event has occurred.
     *  @exception IllegalActionException If the fmiGetEventIndicators
     *   function is missing, or if calling it does not return fmiOK.
     */
    protected boolean _checkEventIndicators() throws IllegalActionException {
        int number = _fmiModelDescription.numberOfEventIndicators;
        if (number == 0) {
            // No event indicators.
            return false;
        }
        if (_eventIndicators == null || _eventIndicators.length != number) {
            _eventIndicators = new double[number];
        }
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

        int fmiFlag = ((Integer) _fmiGetEventIndicatorsFunction.invoke(Integer.class, new Object[] {
            _fmiComponent, _eventIndicators, number })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Failed to get event indicators.");
        }

        if (_firstFire) {
            _eventIndicatorsPrevious = _eventIndicators;
            return false;
        }
        // Check for polarity change.
        for (int i = 0; i < number; i++) {
            if (_eventIndicatorsPrevious[i]*_eventIndicators[i] < 0.0) {
                return true;
            }
        }
        _eventIndicatorsPrevious = _eventIndicators;
        return false;
    }
    
    /** Print the debug message to stdout and flush stdout.
     *  This is useful for tracking down segfault problems.
     *  To use this, right click on the FMUImport actor
     *  and select "Listen to Actor". The logging messages
     *  will appear on stdout and in the listener window.
     *  @param message The message to be displayed.
     */
    protected void _debugToStdOut(String message) {
        System.out.println(message);
        System.out.flush();
        _debug(message);
    }

    /** For model exchange, complete the integrator step, and if the FMU
     *  indicates that fmiEventUpdate() should be called, call it.
     *  @param eventOccurred True if a time event or state event has occurred and
     *   fmiEventUpdate should be called.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    protected void _fmiCompletedIntegratorStep(boolean eventOccurred) throws IllegalActionException {
        ByteBuffer callEventUpdate = ByteBuffer.allocate(1);
        int fmiFlag = ((Integer) _fmiCompletedIntegratorStepFunction
                .invoke(Integer.class, new Object[] {
                    _fmiComponent, callEventUpdate })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Failed to complete integrator step.");
        }

        if (callEventUpdate.get(0) != (byte) 0 || eventOccurred) {
            throw new IllegalActionException(this, "FIXME: Not supported yet. Call eventUpdate");
        }
    }
    
    /** Advance from the last firing time or last commit time to the specified time and
     *  microstep by calling fmiDoStep(), if necessary. This method is for
     *  co-simulation only. Such an advance
     *  is necessary if the newTime is not equal to the last firing time,
     *  or if it is equal and the newMicrostep is greater than the last firing microstep.
     *  If the step size is rejected, then return a new suggested step size, which
     *  is either half the specified step size, or, if the FMU supports it, the last
     *  successful time reported by the FMU. As a side effect, if the time advance
     *  is successful, then the _lastFireTime and _lastFireMicrostep are updated
     *  to match the arguments, indicating that the FMU has advanced to that time.
     *  @param newTime The time to advance to.
     *  @param newMicrostep The microstep to advance to.
     *  @return A revised suggested step size, or -1.0 if the step size was accepted
     *   by the FMU.
     *  @exception IllegalActionException If fmiDoStep() returns anything other than
     *   fmiDiscard or fmiOK.
     */
    protected double _fmiDoStep(Time newTime, int newMicrostep)
            throws IllegalActionException {
        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // By default, the FMU does not suggest a refined step size,
        // something we indicate with a -1.0.
        double result = -1.0;

        int timeAdvance = newTime.compareTo(_lastFireTime);
        if (timeAdvance != 0 || timeAdvance == 0
                && newMicrostep > _lastFireMicrostep) {
            // Time or microstep has advanced or time has declined
            // since the last invocation of fire() or initialize().
            // Even if only the microstep has advanced, we should still call
            // fmiDoStep() because the FMU may require it for zero-step-size iterations
            // (the standard is not clear about this).
            //
            // When calling fmiDoStep(), the time argument is the _start_
            // of the integration interval, which is not the current time, in general.
            // We are calling fmiDoStep() to advance to current time, which is therefore
            // the _end_ of the integration interval.
            double time = _lastFireTime.getDoubleValue();

            // Compute the step size.
            // Subtlety here: The step size computed below may not match the current
            // step size of the enclosing continuous director. In particular, at
            // the start of the next integration interval, this FMU is being asked
            // for its output at the _start_ of the interval. That time matches
            // the _end_ of the previous interval. Thus, it will appear here
            // that the step size is 0.0, but it is actually not.
            // As a consequence, an FMU that wants to produce an output
            // at a microstep other than 0 needs to actually insist on _three_
            // (or more) firings at that time. One to produce the output at the end
            // of the previous interval, one to produce the discrete value at
            // microstep 1, and one to produce the output at the _start_ of
            // of the next interval. Ugh. This makes it really hard to write
            // FMUs that control step sizes.
            double stepSize = newTime.subtract(_lastFireTime).getDoubleValue();

            /* It would be nice to do the following sanity check, but
             * unfortunately the ContinuousDirector completes the rounds
             * before checking to see whether any component was happy with the
             * step size, so it is like to re-invoke this FMU with a step
             * size that will trigger this exception.
            if (_refinedStepSize >= 0.0 && stepSize > _refinedStepSize) {
                throw new IllegalActionException(this,
                        "Previously rejected time advance and suggested a step size of "
                        + _refinedStepSize
                        + ", but am being offered a step size of "
                        + stepSize
                        + " at time "
                        + time);
            }
             */

            // The last argument to fmiDoStep is either newStep (for FMI 1.0),
            // which is true if we are not redoing a step, or
            // noSetFMUStatePriorToCurrentPoint (for FMI 2.0), which s true
            // if the start of the interval given to fmiDoStep coincides with
            // the last commit time (the time at which initialize() or postfire()
            // was last invoked).

            // For FMI 1.0, there is not really a good solution. There are two
            // flawed possibilities, described below.  We implement the first
            // flawed possibility.
            /*
            Suppose an RK 2-3 solver chooses a step size
            of, say, 1.0 seconds at communication point (current time)
            0.0. It will want to obtain outputs from the FMU at times
            0.0, 0.5, 0.75, and 1.0.  Since there are three time intervals
            here, there need to be three calls to fmiDoStep().  E.g.,

              fmiDoStep(component, 0.0, 0.5, true);  // newStep == true
              fmiDoStep(component, 0.5, 0.25, true);  // newStep == true
              fmiDoStep(component, 0.75, 0.25, true);  // newStep == true

            The last two calls have to specify newStep == true because
            otherwise the slave will interpret this as restarting the
            previous computation, which has an earlier communication point.
            Moreover, when we get fired at time 0.5, the provided inputs
            correspond to those at time 0.5. So we can provide those inputs
            to the FMU and proceed.

            But now, if the last fmiDoStep is rejected, then _all three_
            have to be redone. The slave, however, will not have a
            record of its state at time 0.0 unless it is recording
            the state at _all_ communication points, which is clearly
            not a good solution.

            So the above sequence won't work.  There is an alternative.
            The orchestrator could do this:

              fmiDoStep(component, 0.0, 0.5, true);  // newStep == true
              fmiDoStep(component, 0.0, 0.75, false);  // newStep == false
              fmiDoStep(component, 0.0, 1.0, false);  // newStep == false

            The slave has to redo time intervals even if the step size
            is ultimately accepted. This would also require that at times
            0.5 and 0.75, we do not provide the new inputs to the FMU.
            It would need to run with the inputs from time 0.0.
            Also, this will be less accurate,
            because with the first execution sequence, input values are
            provided for times 0.5 and 0.75, but for the second, they
            are not.

            Appendix B of the 1.0 standard has an extensive discussion
            of the limitations of newStep.
             */
            byte lastArg = 1;

            // If we have moved backwards in time, then we are redoing an integration
            // step since the last postfire() or initialize(). Therefore, the start
            // of the integration interval is the last commit time, not the last fire
            // time.
            if (timeAdvance < 0) {
                // Correct the above values to indicate that we are redoing a step.
                rollBackToCommittedState();
                time = _lastCommitTime.getDoubleValue();
                stepSize = newTime.subtract(_lastCommitTime).getDoubleValue();
                lastArg = 0;
            }

            if (_fmiVersion >= 2.0) {
                if (_firstFireInIteration) {
                    lastArg = 1;
                } else {
                    lastArg = 0;
                }
            }

            if (_debugging) {
                String lastArgDescription = ", /* newStep */";
                if (_fmiVersion >= 2.0) {
                    lastArgDescription = ", /* noSetFMUStatePriorToCurrentPoint */";
                }
                _debugToStdOut("FMIImport.fire(): about to call "
                        + modelIdentifier + "_fmiDoStep(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize
                        + lastArgDescription + lastArg + ")");
            }

            // Invoke fmiDoStep.
            // NOTE: As of FMI 2.0, there is a proposal on the table for "Exact event
            // handling" that will add two additional arguments to fmiDoStep,
            // fmiBoolean *upcomingTimeEvent, fmiReal *nextEventTime.  The first
            // of these will inform this actor that it should call fireAt(), and the
            // second will provide the time of the fireAt().
            int fmiFlag = ((Integer) _fmiDoStepFunction.invokeInt(new Object[] {
                    _fmiComponent, time, stepSize, lastArg })).intValue();
            
            // If the FMU discarded the step, handle this.
            if (fmiFlag == FMILibrary.FMIStatus.fmiDiscard) {
                if (_debugging) {
                    _debugToStdOut("Rejected step size of " + stepSize
                            + " at time " + time);
                }
                // By default, if the FMU does not provide better information,
                // we suggest a refined step size of half the current step size.
                result = stepSize * 0.5;

                if (_fmiGetRealStatusFunction != null) {
                    // The FMU has provided a function to query for
                    // a suggested step size.
                    // This function returns fmiDiscard if not supported.
                    DoubleBuffer valueBuffer = DoubleBuffer.allocate(1);
                    fmiFlag = ((Integer) _fmiGetRealStatusFunction
                            .invokeInt(new Object[] {
                                    _fmiComponent,
                                    FMILibrary.FMIStatusKind.fmiLastSuccessfulTime,
                                    valueBuffer })).intValue();
                    if (fmiFlag == FMILibrary.FMIStatus.fmiOK) {
                        double lastSuccessfulTime = valueBuffer.get(0);
                        if (_debugging) {
                            _debug("FMU reports last successful time of "
                                    + lastSuccessfulTime);
                        }
                        // Sanity check the time to make sure it makes sense.
                        // Since FMI uses double for time, we have to guard against
                        // quantization errors and allow approximation here.
                        if (lastSuccessfulTime < _lastCommitTime
                                .getDoubleValue() - _relativeTolerance) {
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
                        // We want lastSuccessfulTime - lastCommitTime, which
                        // is not necessarily equal to time. In particular,
                        // if using an RK solver, we may be rejecting a doStep
                        // beyond the first iteration, and the step size right
                        // now is actually a substep.
                        result = lastSuccessfulTime
                                - _lastCommitTime.getDoubleValue();
                        // Note that the above subtraction could yield a small negative
                        // number. Correct for that.
                        if (result < 0.0 && result >= -_relativeTolerance) {
                            result = 0.0;
                        }
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
                // NOTE: Even though doStep() has been rejected,
                // we nonetheless continue to set inputs and outputs.
                // Is this the right thing to do? It seems like we should
                // at least be producing outputs.
            } else if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                // FIXME: Handle fmiPending and fmiWarning.
                throw new IllegalActionException(this, "Could not simulate, "
                        + modelIdentifier + "_fmiDoStep(Component, /* time */ "
                        + time + ", /* stepSize */" + stepSize
                        + ", /* newStep */ 1) returned "
                        + _fmiStatusDescription(fmiFlag));
            } else {
                // Time advance succeeded.
                _lastFireTime = newTime;
                _lastFireMicrostep = newMicrostep;
            }
            if (_debugging) {
                _debugToStdOut("FMUImport done calling " + modelIdentifier
                        + "_fmiDoStep()");
            }
        }
        return result;
    }

    /** Free the instance of the FMU.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    protected void _fmiFreeInstance() throws IllegalActionException {
        if (_debugging) {
            _debugToStdOut("Freeing the FMU instance.");
        }
        int fmiFlag = 0;
        if (_fmiModelDescription.modelExchange) {
            fmiFlag = ((Integer) _fmiFreeModelInstanceFunction
                    .invokeInt(new Object[] { _fmiComponent })).intValue();
        } else {
            fmiFlag = ((Integer) _fmiFreeSlaveInstanceFunction
                    .invokeInt(new Object[] { _fmiComponent })).intValue();
        }
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Could not free the FMU instance: "
                            + _fmiStatusDescription(fmiFlag));
        }
    }

    /** Return the derivatives of the continuous stateus provided by the FMU.
     *  @return The derivatives of the FMU.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    protected double[] _fmiGetDerivatives() throws IllegalActionException {
        int numberOfStates = _fmiModelDescription.numberOfContinuousStates;
        if (_derivatives == null || _derivatives.length != numberOfStates) {
            // FIXME: All our other JNA code uses DoubleBuffer.allocat().  Why?
            // I'm getting the same (nonsensical) results from both.
            _derivatives = new double[numberOfStates];
        }
        
        int fmiFlag = ((Integer) _fmiGetDerivativesFunction.invoke(Integer.class, new Object[] {
            _fmiComponent, _derivatives, new NativeSizeT(numberOfStates) })).intValue();
    
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Failed to set continuous states at time:"
                    + _fmiStatusDescription(fmiFlag));
        }
        return _derivatives;
    }
    
    /** Invoke _fmiInitialize() (for model exchange) or
     *  _fmiInitializeSlave() (for co-simulation) on the FMU.
     *  In the case of model exchange, this method also checks for a returned
     *  next event time and calls fireAt() on the director if a next event time
     *  is returned. In the case of co-simulation, if the FMU provides a maximum
     *  step size, get that from the FMU and call fireAt() as well.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    @SuppressWarnings("deprecation")
    protected void _fmiInitialize() throws IllegalActionException {
        if (_fmiModelDescription.modelExchange) {
            FMIEventInfo eventInfo = new FMIEventInfo.ByValue();
            int fmiFlag = ((Integer) _fmiInitializeFunction.invoke(Integer.class, new Object[] {
                _fmiComponent, _toleranceControlled, _relativeTolerance,
                eventInfo.getPointer() })).intValue();

            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this, "Failed to initialize FMU.");
            }

            if (eventInfo.terminateSimulation != (byte) 0) {
                throw new IllegalActionException(this, "FMU terminates simulation in fmiInitialize()");
            }
            if (eventInfo.upcomingTimeEvent != (byte) 0) {
                // FIXME: Record the event time to make sure to call fmiEventUpdate when fired.
                // FIXME: Does this make sense to do here? Shouldn't this happen in initialize?
                getDirector().fireAt(this, eventInfo.nextEventTime);
            }
        } else {
            String modelIdentifier = _fmiModelDescription.modelIdentifier;

            Director director = getDirector();
            Time startTime = director.getModelStartTime();
            Time stopTime = director.getModelStopTime();

            int fmiFlag;
            if (_fmiVersion < 2.0) {
                fmiFlag = ((Integer) _fmiInitializeSlaveFunction.invoke(Integer.class, new Object[] {
                        _fmiComponent, startTime.getDoubleValue(), (byte) 1,
                        stopTime.getDoubleValue() })).intValue();
            } else {
                fmiFlag = ((Integer) _fmiInitializeSlaveFunction.invoke(
                        Integer.class,
                        new Object[] { _fmiComponent, _relativeTolerance,
                                startTime.getDoubleValue(), (byte) 1, // fmiBoolean stopTimeDefined
                                stopTime.getDoubleValue() })).intValue();
                
                // If the FMU can provide a maximum step size, query for the initial maximum
                // step size and call fireAt() and ensure that the FMU is invoked
                // at the specified time.
                _requestRefiringIfNecessary();
                
                // In case we have to backtrack, if the FMU supports backtracking,
                // record its state.
                _recordFMUState();
            }

            if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                throw new IllegalActionException(this, "Could not simulate, "
                        + modelIdentifier
                        + "_fmiInitializeSlave(Component, /* startTime */ "
                        + startTime.getDoubleValue() + ", 1, /* stopTime */"
                        + stopTime.getDoubleValue() + ") returned "
                        + _fmiStatusDescription(fmiFlag));
            }
        }
        if (_debugging) {
            _debugToStdOut("Initialized FMU.");
        }
    }

    /** For model exchange, set the continuous states of the FMU to the specified array.
     *  @param values The values to assign to the states.
     *  @throws IllegalActionException If the length of the array does not match
     *   the number of continuous states, or if the FMU does not return fmiOK.
     */
    protected void _fmiSetContinuousStates(double values[]) throws IllegalActionException {
        if (values.length != _fmiModelDescription.numberOfContinuousStates) {
            throw new IllegalActionException(this, "Number of values " + values.length
                    + " does not match the number of continuous states " + _fmiModelDescription.numberOfContinuousStates);
        }
        DoubleBuffer states = DoubleBuffer.allocate(values.length);
        for (int i = 0; i < values.length; i++) {
            states.put(values[i]);
        }
        int fmiFlag = ((Integer) _fmiSetContinuousStates.invoke(Integer.class, new Object[] {
            _fmiComponent, states, new NativeSizeT(values.length) })).intValue();
    
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            Time currentTime = getDirector().getModelTime();
            throw new IllegalActionException(this, "Failed to set continuous states at time " + currentTime
                    + ": " + _fmiStatusDescription(fmiFlag));
        }
        if (_debugging) {
            _debug("Setting FMU states to " + values);
        }
    }
    
    /** Set the time of the FMU to the specified time.
     *  @param time The time.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    protected void _fmiSetTime(Time time) throws IllegalActionException {
        if (_debugging) {
            _debugToStdOut("Setting FMU time to " + time);
        }
        int fmiFlag = ((Integer) _fmiSetTimeFunction.invoke(Integer.class, new Object[] {
            _fmiComponent, time.getDoubleValue() })).intValue();
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Failed to set FMU time at time " + time
                    + ": " + _fmiStatusDescription(fmiFlag));
        }
    }

    /** Return a string describing the specified fmiStatus.
     *  @param fmiStatus The status returned by an FMI procedure.
     *  @return a String describing the status.
     */
    protected static String _fmiStatusDescription(int fmiStatus) {
        // FIXME: FMI 2.0 has apparently lost fmiWarning and fmiFatal.
        // What is the new encoding? Need the header file.
        switch (fmiStatus) {
        case 0:
            return "fmiOK";
        case 1:
            return "fmiWarning";
        case 2:
            return "fmiDiscard";
        case 3:
            return "fmiError";
        case 4:
            return "fmiFatal";
        default:
            return "fmiPending";
        }
    }
    
    /** Terminate the FMU.
     *  @throws IllegalActionException If the FMU does not return fmiOK.
     */
    protected void _fmiTerminate() throws IllegalActionException {
        if (_debugging) {
            _debugToStdOut("Terminating the FMU.");
        }
        int fmiFlag = 0;
        if (_fmiModelDescription.modelExchange) {
            fmiFlag = ((Integer) _fmiTerminateFunction
                    .invokeInt(new Object[] { _fmiComponent })).intValue();
        } else {
            fmiFlag = ((Integer) _fmiTerminateSlaveFunction
                    .invokeInt(new Object[] { _fmiComponent })).intValue();
        }
        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this,
                    "Could not terminate FMU: "
                            + _fmiStatusDescription(fmiFlag));
        }
    }
    
    /** Free the memory recording FMU state, if the canGetAndSetFMUstate capability flag
     *  for the FMU is true. Otherwise, do nothing.
     *  @throws IllegalActionException If freeing the memory the state fails.
     */
    protected void _freeFMUState() throws IllegalActionException {
        if (_fmiFreeFMUstateFunction != null && _recordedState != null) {
            int freeStateSucceeded = ((Integer) _fmiFreeFMUstateFunction.invoke(
                    Integer.class,
                    new Object[] { _fmiComponent, _recordedState })).intValue();
            if (freeStateSucceeded != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this, "Failed to free memory recording FMU state: "
                        + _fmiStatusDescription(freeStateSucceeded));
            }
        }
        _recordedState = null;
    }

    /** Return the current step size.
     *  If the director implements ContinuousStatefulDirector,
     *  then the value returned by currentStepSize() is returned.
     *  If the director is an instance of PeriodicDirector, then the value
     *  returned by periodValue() is returned.
     *  @return the current step size.
     *  @exception IllegalActionException If there is a problem getting
     *  the currentStepSize.
     */
    protected double _getStepSize() throws IllegalActionException {
        double stepSize = 0.0;
        Director director = getDirector();
        if (director instanceof ContinuousStatefulDirector) {
            // FIXME: depending on ContinuousDirector here.
            stepSize = ((ContinuousStatefulDirector) getDirector())
                    .getCurrentStepSize();

        } else if (director instanceof PeriodicDirector) {
            stepSize = ((PeriodicDirector) getDirector()).periodValue();
        } else {
            throw new IllegalActionException(this,
                    "Don't know how to get the step size for "
                            + director.getClass().getName() + ".");
        }
        return stepSize;
    }

    /** Record the current FMU state. For model exchange, this copies the
     *  FMU state into the variable _state using fmiGetContinuousStates().
     *  For co-simulation, if the canGetAndSetFMUstate capability flag
     *  for the FMU is true, then it record the state by invoking
     *  fmiGetFMUstate(). If the capability flag is false, do nothing.
     *  @throws IllegalActionException If recording the state fails.
     */
    protected void _recordFMUState() throws IllegalActionException {
        if (_fmiModelDescription.modelExchange) {
            // Model exchange version.
            int numberOfStates = _fmiModelDescription.numberOfContinuousStates;
            if (_states == null || _states.array().length != numberOfStates) {
                _states = DoubleBuffer.allocate(numberOfStates);
            }

            int fmiFlag = ((Integer) _fmiGetContinuousStatesFunction.invoke(Integer.class, new Object[] {
                _fmiComponent, _states, new NativeSizeT(numberOfStates) })).intValue();
        
            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                Time currentTime = getDirector().getModelTime();
                throw new IllegalActionException(this, "Failed to get continuous states at time " + currentTime);
            }

        } else {
            // Co-Simulation version.
            if (_fmiGetFMUstateFunction != null) {
                // During initialize, the FMUstate argument should be the
                // address of a null-valued pointer.
                if (_recordedState == null) {
                    _recordedState = new PointerByReference();
                    _recordedState.setValue(Pointer.NULL);
                }
                int getStateSucceeded = ((Integer) _fmiGetFMUstateFunction.invoke(
                        Integer.class,
                        new Object[] { _fmiComponent, _recordedState })).intValue();
                if (getStateSucceeded != FMILibrary.FMIStatus.fmiOK) {
                    Time currentTime = getDirector().getModelTime();
                    throw new IllegalActionException(this, "Failed to record FMU state at time " + currentTime);
                }
            } else {
                _recordedState = null;
            }
        }
    }

    /** If the FMU can provide a maximum step size, query for that maximum
     *  step size and call fireAt() to ensure that the FMU is invoked
     *  at the specified time.
     *  @throws IllegalActionException If the call to fireAt() throws it.
     */
    protected void _requestRefiringIfNecessary() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (_fmiModelDescription.modelExchange) {
            // Provide a conservative default.
            double stepSize = 0.01;
            Parameter maximumStepSize = (Parameter)getAttribute("maximumStepSize", Parameter.class);
            if (maximumStepSize != null) {
                Token stepSizeValue = maximumStepSize.getToken();
                if (stepSizeValue instanceof DoubleToken) {
                    stepSize = ((DoubleToken) stepSizeValue).doubleValue();
                }
            }
            director.fireAt(this, currentTime.add(stepSize));
        } else {
            // If the FMU can provide a maximum step size, query for the initial maximum
            // step size so that we can call fireAt() and ensure that the FMU is invoked
            // at the specified time.
            if (_fmiModelDescription.canProvideMaxStepSize) {
                Function maxStepSizeFunction = _nativeLibrary.getFunction(
                        _fmiModelDescription.modelIdentifier
                        + "_fmiGetMaxStepSize");
                DoubleBuffer maxStepSize = DoubleBuffer.allocate(1);
                int providesMaxStepSize = ((Integer) maxStepSizeFunction.invoke(
                        Integer.class,
                        new Object[] { _fmiComponent, maxStepSize })).intValue();
                if (providesMaxStepSize == FMILibrary.FMIStatus.fmiOK){
                    // FMU provides an initial maximum step size.
                    double stepSize = maxStepSize.get(0);
                    director.fireAt(this, currentTime.add(stepSize));
                }
            }
        }
    }

    /** Restore the current FMU state to match that most recently
     *  recorded, if the canGetAndSetFMUstate capability flag
     *  for the FMU is true. Otherwise, do nothing.
     *  @throws IllegalActionException If there is no recorded state,
     *   or if restoring the state fails.
     */
    protected void _restoreFMUState() throws IllegalActionException {
        if (_fmiSetFMUstate != null) {
            if (_recordedState == null) {
                throw new IllegalActionException(this, "No recorded FMU state.");
            }
            int setStateSucceeded = ((Integer) _fmiSetFMUstate.invoke(
                    Integer.class,
                    new Object[] { _fmiComponent, _recordedState.getValue() })).intValue();
            if (setStateSucceeded != FMILibrary.FMIStatus.fmiOK) {
                throw new IllegalActionException(this, "Failed to set FMU state.");
            }
        } else {
            _recordedState = null;
        }
    }
    
    /** Set a Ptolemy II Parameter to the value of a FMI
     *  ScalarVariable.
     *  @param parameter The Ptolemy parameter to be set.
     *  @param scalar The FMI scalar variable that contains the value
     *  to be set
     *  @exception IllegalActionException If the scalar is of a type
     *  that is not handled.
     */
    protected void _setParameter(Parameter parameter, FMIScalarVariable scalar)
            throws IllegalActionException {
        // FIXME: What about arrays?
        if (scalar.type instanceof FMIBooleanType) {
            parameter.setToken(new BooleanToken(scalar
                    .getBoolean(_fmiComponent)));
        } else if (scalar.type instanceof FMIIntegerType) {
            // FIXME: handle Enumerations?
            parameter.setToken(new IntToken(scalar.getInt(_fmiComponent)));
        } else if (scalar.type instanceof FMIRealType) {
            parameter
                    .setToken(new DoubleToken(scalar.getDouble(_fmiComponent)));
        } else if (scalar.type instanceof FMIStringType) {
            parameter
                    .setToken(new StringToken(scalar.getString(_fmiComponent)));
        } else {
            throw new IllegalActionException("Type " + scalar.type
                    + " not supported.");
        }
    }

    /** Set a scalar variable of the FMU to the value of a Ptolemy token.
     *  @param scalar the FMI scalar to be set.
     *  @param token the Ptolemy token that contains the value to be set.
     *  @exception IllegalActionException If the scalar is of a type
     *  that is not handled or if the type of the token does not match
     *  the type of the scalar.
     */
    protected void _setFMUScalarVariable(FMIScalarVariable scalar, Token token)
            throws IllegalActionException {
        try {
            // FIXME: What about arrays?
            if (scalar.type instanceof FMIBooleanType) {
                scalar.setBoolean(_fmiComponent,
                        ((BooleanToken) token).booleanValue());
            } else if (scalar.type instanceof FMIIntegerType) {
                // FIXME: handle Enumerations?
                scalar.setInt(_fmiComponent, ((IntToken) token).intValue());
            } else if (scalar.type instanceof FMIRealType) {
                scalar.setDouble(_fmiComponent,
                        ((DoubleToken) token).doubleValue());
            } else if (scalar.type instanceof FMIStringType) {
                scalar.setString(_fmiComponent,
                        ((StringToken) token).stringValue());
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
    
    /** Return true if outputs are skipped if known.
     *  A true value means that only a single output token will be produced
     *  in each iteration of this actor. A false value means that a sequence
     *  of output tokens may be produced.  Generally, for domains that
     *  implement fixed-point semantics, such as Continuous and SR,
     *  the return value should be true. Otherwise it should be false.
     *  If the director is a ContinuousDirector, then return true.
     *  If the director is SDFDirector, then return false.
     *  @return the true if outputs that have been set are skipped.
     *  @exception IllegalActionException If there is a problem getting
     *  the currentStepSize.
     */
    protected boolean _skipIfKnown() throws IllegalActionException {
        Director director = getDirector();
        if (director instanceof FixedPointDirector) {
            return true;
        } else {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                      ////

    /** The FMI component created by the
     * modelIdentifier_fmiInstantiateSlave() method.
     */
    protected Pointer _fmiComponent = null;

    /** A representation of the fmiModelDescription element of a
     *  Functional Mock-up Unit (FMU) file.
     */
    protected FMIModelDescription _fmiModelDescription;

    /** The version of FMI that the FMU declares it is compatible with,
     *  converted to a double for easy comparison.
     */
    protected double _fmiVersion;
    
    /** For model exchange, the FMU state variables. */
    protected DoubleBuffer _states;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If _fmiModelDescription is null or does not have a non-null
     *  modelIdentifier, then thrown an exception
     *  with an informative message.
     *  @exception IllegalActionException If critical information is missing.
     */
    private void _checkFmiCommon() throws IllegalActionException {
        if (_fmiModelDescription == null) {
            throw new IllegalActionException(this,
                    "Could not get the FMU model description? " + "Perhaps \""
                            + fmuFile.asFile()
                            + "\" does not exist or is not readable?");
        }
        if (_fmiModelDescription.modelIdentifier == null) {
            throw new IllegalActionException(this,
                    "Could not get the modelIdentifier, perhaps the .fmu file \""
                            + fmuFile.asFile()
                            + "\" did not contain a modelDescription.xml file?");
        }
    }
    
    /** If functions needed for co-simulation are absent, then thrown an exception
     *  with an informative message.  The .fmu file may
     *  not have a shared library for the current platform.
     *  @exception IllegalActionException If functions needed for co-simulation
     *   are missing.
     */
    private void _checkFmiCoSimulation() throws IllegalActionException {
        String missingFunction = null;
        if (_fmiDoStepFunction == null) {
            missingFunction = "_fmiDoStep";
        }
        if (_fmiInstantiateSlaveFunction == null) {
            missingFunction = "_fmiInstantiateSlave";
        }
        if (_fmiTerminateSlaveFunction == null) {
            missingFunction = "_fmiTerminateSlave";
        }
        if (_fmiFreeSlaveInstanceFunction == null) {
            missingFunction = "_fmiFreeSlaveInstance";
        }
        if (missingFunction != null) {
            String sharedLibrary = "";
            try {
                sharedLibrary = "the shared library \""
                        + FMUFile.fmuSharedLibrary(_fmiModelDescription)
                        + "\" was probably not found after the .fmu file was unzipped?";

            } catch (IOException ex) {
                sharedLibrary = "the shared library could not be obtained from the fmu: "
                        + ex;
            }
            List<String> binariesFiles = new LinkedList<String>();
            // Get the list pathnames that contain the string "binaries"
            for (File file : _fmiModelDescription.files) {
                if (file.toString().indexOf("binaries") != -1) {
                    binariesFiles.add(file.toString() + "\n");
                }
            }
            throw new IllegalActionException(
                    this,
                    "Could not get the "
                            + _fmiModelDescription.modelIdentifier
                            + missingFunction
                            + "() C function?  Perhaps the .fmu file \""
                            + fmuFile.asFile()
                            + "\" does not contain a shared library for the current "
                            + "platform?  "
                            + "When the .fmu file was loaded, "
                            + sharedLibrary
                            + "  The .fmu file contained the following files with 'binaries'"
                            + " in the path:\n" + binariesFiles);
        }
    }

    /** If functions needed for model exchange are absent, then thrown an exception
     *  with an informative message.  The .fmu file may
     *  not have a shared library for the current platform.
     *  @exception IllegalActionException If functions needed for co-simulation
     *   are missing.
     */
    private void _checkFmiModelExchange() throws IllegalActionException {
        String missingFunction = null;
        if (_fmiSetTimeFunction == null) {
            missingFunction = "_fmiSetTime";
        }
        if (_fmiInstantiateModelFunction == null) {
            missingFunction = "_fmiInstantiateModel";
        }
        if (_fmiModelDescription.numberOfContinuousStates > 0) {
            if (_fmiGetContinuousStatesFunction == null) {
                missingFunction = "_fmiGetContinuousStates";
            }
            if (_fmiSetContinuousStates == null) {
                missingFunction = "_fmiSetContinuousStates";
            }
            if (_fmiGetDerivativesFunction == null) {
                missingFunction = "_fmiGetDerivatives";
            }
            if (_fmiCompletedIntegratorStepFunction == null) {
                missingFunction = "_fmiCompletedIntegratorStep";
            }
        }
        if (missingFunction != null) {
            String sharedLibrary = "";
            try {
                sharedLibrary = "the shared library \""
                        + FMUFile.fmuSharedLibrary(_fmiModelDescription)
                        + "\" was probably not found after the .fmu file was unzipped?";
            } catch (IOException ex) {
                sharedLibrary = "the shared library could not be obtained from the fmu: "
                        + ex;
            }
            List<String> binariesFiles = new LinkedList<String>();
            // Get the list pathnames that contain the string "binaries"
            for (File file : _fmiModelDescription.files) {
                if (file.toString().indexOf("binaries") != -1) {
                    binariesFiles.add(file.toString() + "\n");
                }
            }
            throw new IllegalActionException(
                    this,
                    "Could not get the "
                            + _fmiModelDescription.modelIdentifier
                            + missingFunction
                            + "() C function?  Perhaps the .fmu file \""
                            + fmuFile.asFile()
                            + "\" does not contain a shared library for the current "
                            + "platform?  "
                            + "When the .fmu file was loaded, "
                            + sharedLibrary
                            + "  The .fmu file contained the following files with 'binaries'"
                            + " in the path:\n" + binariesFiles);
        }
    }

    /** Given a FMIType object, return a string suitable for setting
     *  the TypeAttribute.
     *  @param type The FMIType object.
     *  @return a string suitable for ptolemy.actor.TypeAttribute.
     *  @exception IllegalActionException If the type is not supported.
     */
    private static String _fmiType2PtolemyType(FMIType type)
            throws IllegalActionException {
        if (type instanceof FMIBooleanType) {
            return "boolean";
        } else if (type instanceof FMIIntegerType) {
            // FIXME: handle Enumerations?
            return "int";
        } else if (type instanceof FMIRealType) {
            return "double";
        } else if (type instanceof FMIStringType) {
            return "string";
        } else {
            throw new IllegalActionException("Type " + type + " not supported.");
        }
    }

    /** Return a collection of scalar variables for which there is
     *  a connected output port, and for each such variable,
     *  a set of input ports on which it declares it depends,
     *  if any.
     *  @return A map, where the keys consist of all the scalar
     *   variables of the FMU corresponding to a connected output
     *   port, and the value of the map is the collection of input
     *   ports that the output declares it depends on (if any).
     *  @exception IllegalActionException If an expected output is not
     *   found, or if the width of the output cannot be determined.
     */
    private List<Output> _getOutputs() throws IllegalActionException {
        if (workspace().getVersion() == _outputsVersion) {
            return _outputs;
        }

        // The _outputs variable is out of date. Reconstruct it.
        _outputs = new LinkedList<Output>();
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // If this variable has an alias, then we operate
            // only on the real version, not the alias.
            // In bouncingBall.fmu, g has an alias, so it is skipped.
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                continue;
            }

            // An "output" may be an internal variable in the FMU.
            // FMI seems to have the odd notion of being able to "observe"
            // an internal variable but not call it an output.
            if (scalarVariable.causality == FMIScalarVariable.Causality.output
                    || scalarVariable.causality == FMIScalarVariable.Causality.internal) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                if (port == null || port.getWidth() <= 0) {
                    // Either it is not a port or not connected.
                    // Check to see if we should update the parameter.
                    // FIXME: Huh? What parameter? Does it make sense for
                    // the output of an FMU to go to a parameter?
                    /*
                    String sanitizedName = StringUtilities.sanitizeName(scalarVariable.name);
                    Parameter parameter = (Parameter)getAttribute(sanitizedName, Parameter.class);
                    if (parameter != null) {
                        _setParameter(parameter, scalarVariable);
                    } else {
                        throw new IllegalActionException(this,
                                "Expected an output named "
                                + scalarVariable.name
                                + " or a parameter named "
                                + sanitizedName
                                + ", but this actor has neither.");
                    }
                    */
                    continue;
                }
                Output output = new Output();
                output.scalarVariable = scalarVariable;
                output.port = port;

                // Next, we need to find the dependencies that the
                // FMU XML file says the port depends on. By default, an
                // output depends on _all_ inputs, but if there is
                // a DirectDependency element in the XML file, then
                // the output may depend on only _some_ inputs.
                if (scalarVariable.directDependency != null) {
                    // The output has declared direct dependence on some
                    // inputs. Make sure those are known before retrieving
                    // output values.
                    Set<TypedIOPort> dependencies = new HashSet<TypedIOPort>();
                    for (String inputName : scalarVariable.directDependency) {
                        TypedIOPort inputPort = (TypedIOPort) getPort(inputName);
                        if (inputPort == null) {
                            throw new IllegalActionException(
                                    this,
                                    "FMU declares that output port "
                                            + port.getName()
                                            + " depends directly on input port "
                                            + inputName
                                            + ", but there is no such input port.");
                        }
                        dependencies.add(inputPort);
                    }
                }
                _outputs.add(output);
            }
        }
        return _outputs;
    }

    /** Update the parameters listed in the modelDescription.xml file
     *  contained in the zipped file named by the <i>fmuFile</i>
     *  parameter
     *  @exception IllegalActionException If the file named by the
     *  <i>fmuFile<i> parameter cannot be unzipped or if there
     *  is a problem deleting any pre=existing parameters or
     *  creating new parameters.
     *  @exception NameDuplicationException If a paramater to be created
     *  has the same name as a pre-existing parameter.
     */
    private void _updateParameters() throws IllegalActionException,
            NameDuplicationException {

        if (_debugging) {
            _debugToStdOut("FMUImport.updateParameters() START");
        }
        // Unzip the fmuFile. We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        try {
            // FIXME: Use URLs, not files so that we can work from JarZip files.

            // Only read the file if the name has changed from the last time we
            // read the file or if the modification time has changed.
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            if (fmuFileName.equals(_fmuFileName)) {
                return;
            }
            _fmuFileName = fmuFileName;
            long modificationTime = new File(fmuFileName).lastModified();
            if (_fmuFileModificationTime == modificationTime) {
                return;
            }
            _fmuFileModificationTime = modificationTime;

            // Calling parseFMUFile does not load the shared library.
            // Those are loaded upon the first attempt to use them.
            // This is important because we want to be able to view
            // a model that references an FMU even if the FMU does not
            // support the current platform.
            _fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);
            
            // Specify whether the FMU is for model exchange or co-simulation.
            // This gets determined when the FMU is initially imported.
            _fmiModelDescription.modelExchange = ((BooleanToken)modelExchange.getToken()).booleanValue();

            if (_fmiModelDescription.fmiVersion != null) {
                fmiVersion.setExpression(_fmiModelDescription.fmiVersion);
                // Mysteriously, nondeterministically, the above doesn't always
                // result in attributeChanged() being called after this setExprssion
                // occurs. Sometimes it gets called before, weirdly. Why?
                // Anyway, force it here, because if we have the wrong version,
                // we will get seg faults.
                attributeChanged(fmiVersion);
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip, read in or process \"" + fmuFileName
                            + "\".");
        }
        if (_debugging) {
            _debugToStdOut("FMUImport.updateParameters() END");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Callback functions provided to the C code as a struct.
     *  This reference is non-null between creation of the struct in preinitialize()
     *  and invocation of wrapup() so that the callback structure does not get
     *  garbage collected. JNA documentation is silent about whether there is any
     *  assurance a C pointer to this struct is valid until garbage collection,
     *  but we assume it is.
     */
    private FMICallbackFunctions _callbacks;
    
    /** Buffer for the derivatives returned by the FMU. */
    private double[] _derivatives;
    
    /** Buffer for event indicators. */
    private double[] _eventIndicators;

    /** Buffer for previous event indicators. */
    private double[] _eventIndicatorsPrevious;

    /** Flag identifying the first invocation of fire() after initialize
     */
    private boolean _firstFire;

    /** Flag identifying the first invocation of fire() after each
     *  invocation of initialize() or postfire().
     */
    private boolean _firstFireInIteration;

    /** The fmiCompletedIntegratorStep() function. */
    private Function _fmiCompletedIntegratorStepFunction;
    
    /** The fmiDoStep() function. */
    private Function _fmiDoStepFunction;

    /** The _fmiFreeModelInstance function. */
    private Function _fmiFreeModelInstanceFunction;

    /** The _fmiFreeSlaveInstance function. */
    private Function _fmiFreeSlaveInstanceFunction;

    /** Function to free memory allocated to store the state of the FMU. */
    private Function _fmiFreeFMUstateFunction;
    
    /** Function to get the continuous states of the FMU for model exchange. */
    private Function _fmiGetContinuousStatesFunction;

    /** Function to get the derivatives of a model-exchange FMU. */
    private Function _fmiGetDerivativesFunction;

    /** Function to get the event indicators of the FMU for model exchange. */
    private Function _fmiGetEventIndicatorsFunction;

    /** Function to retrieve the current state of the FMU. */
    private Function _fmiGetFMUstateFunction;

    /** The fmiGetRealStatus() function. */
    private Function _fmiGetRealStatusFunction;
    
    /** The _fmiInitializeSlave function. */
    private Function _fmiInitializeFunction;

    /** The _fmiInitializeSlave function. */
    private Function _fmiInitializeSlaveFunction;

    /** The _fmiInstantiateModel function. */
    private Function _fmiInstantiateModelFunction;

    /** The _fmiInstantiateSlave function. */
    private Function _fmiInstantiateSlaveFunction;

    /** Function to set the continuous states of the FMU for model exchange. */
    private Function _fmiSetContinuousStates;

    /** Function to restore the current state of the FMU to a
     *  previously retrieved version.
     */
    private Function _fmiSetFMUstate;
    
    /** Function to set the time of the FMU for model exchange. */
    private Function _fmiSetTimeFunction;

    /** The _fmiTerminateFunction function. */
    private Function _fmiTerminateFunction;

    /** The _fmiTerminateSlaveFunction function. */
    private Function _fmiTerminateSlaveFunction;

    /** The name of the fmuFile.
     *  The _fmuFileName field is set the first time we read
     *  the file named by the <i>fmuFile</i> parameter.  The
     *  file named by the <i>fmuFile</i> parameter is only read
     *  if the name has changed or if the modification time of
     *  the file is later than the time the file was last read.
     */
    private String _fmuFileName = null;

    /** The modification time of the file named by the
     *  <i>fmuFile</i> parameter the last time the file was read.
     */
    private long _fmuFileModificationTime = -1;
    
    /** The time at which the last commit occurred (initialize or postfire). */
    private Time _lastCommitTime;

    /** The time at which the last fire occurred. */
    private Time _lastFireTime;

    /** The microstep at which the last fire occurred. */
    private int _lastFireMicrostep;
    
    /** The library of native binaries for the FMU C functions. */
    private NativeLibrary _nativeLibrary;
    
    /** The new states computed in fire(), to be committed in postfire. */
    private double[] _newStates;

    /** A collection of scalar variables for which there is
     *  a connected output port, and for each such variable,
     *  a set of input ports on which it declares it depends,
     *  if any.
     */
    private List<Output> _outputs;

    /** The workspace version at which the _outputs variable was last updated. */
    private long _outputsVersion = -1;
        
    /** The latest recorded state of the FMU. */
    private PointerByReference _recordedState = null;
    
    /** The relative tolerance for errors in double values. */
    private double _relativeTolerance;

    /** Refined step size suggested by the FMU if doStep failed,
     *  or -1.0 if there is no suggestion.
     */
    private double _refinedStepSize = -1.0;
    
    /** Indicator that the proposed step size provided to the fire method
     *  has been rejected by the FMU.
     */
    private boolean _stepSizeRejected;

    /** Indicator that we have had iteration with a rejected step size,
     *  so the next suggested step size should be zero.
     */
    private boolean _suggestZeroStepSize;
    
    /** Boolean indicating whether the director uses an error tolerance. */
    private byte _toleranceControlled;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A data structure representing an output from the FMU. */
    private static class Output {
        // FindBugs indicates that this should be a static class.

        /** The FMI scalar variable for this output. */
        public FMIScalarVariable scalarVariable;

        /** The Ptolemy output port for this output. */
        public TypedIOPort port;

        /** The set of input ports on which the output declares it depends. */
        public Set<TypedIOPort> dependencies;
    }
}
