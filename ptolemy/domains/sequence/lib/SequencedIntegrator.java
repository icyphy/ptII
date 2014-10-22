/* An integrator actor that can be sequenced and share state across multiple
 * instances.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.domains.sequence.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// SequencedIntegrator

/** An integrator actor that can be sequenced and share state across multiple
 *  instances.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class SequencedIntegrator extends BaseMultipleMethodsActor {

    /** Create a new instance of the SequencedIntegrator actor with the given
     *  name and container.
     *
     *  @param container The model in which the new actor will be contained.
     *  @param name The name of the new actor
     *  @exception IllegalActionException If the new actor cannot be created.
     *  @exception NameDuplicationException If there is already a NamedObj with
     *   the same name in the container model.
     */
    public SequencedIntegrator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create inports
        input = new TypedIOPort(this, "input", true, false);
        sampleTime = new TypedIOPort(this, "sampleTime", true, false);
        setStateValue = new TypedIOPort(this, "resetStateValue", true, false);

        // input for parameter structure containing THighLow and TLowHigh values
        sampleFactor = new TypedIOPort(this, "sampleFactor", true, false);

        // input port to trigger the setStateMethod
        callSetStateMethod = new TypedIOPort(this, "callSetStateMethod", true,
                false);
        StringAttribute callSetStateTrigger = new StringAttribute(
                callSetStateMethod, "methodName");
        callSetStateTrigger.setExpression(_setStateMethodName);
        callSetStateTrigger.setVisibility(Settable.NOT_EDITABLE);

        // create outports
        output = new TypedIOPort(this, "output", false, true);
        currentState = new TypedIOPort(this, "currentState", false, true);

        // set direction of ports
        StringAttribute sampleFactorCardinal = new StringAttribute(
                sampleFactor, "_cardinal");
        sampleFactorCardinal.setExpression("NORTH");
        StringAttribute sampleTimeCardinal = new StringAttribute(sampleTime,
                "_cardinal");
        sampleTimeCardinal.setExpression("SOUTH");
        StringAttribute setStateValueCardinal = new StringAttribute(
                setStateValue, "_cardinal");
        setStateValueCardinal.setExpression("SOUTH");
        StringAttribute callSetStateCardinal = new StringAttribute(
                callSetStateMethod, "_cardinal");
        callSetStateCardinal.setExpression("NORTH");

        // set type constraints for output port
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(sampleFactor);
        output.setTypeAtLeast(sampleTime);
        output.setTypeAtLeast(setStateValue);

        // Set the input ports for each fire method.
        List setStateInputs = new LinkedList<IOPort>();
        setStateInputs.add(setStateValue);

        List integrateInputs = new LinkedList<IOPort>();
        integrateInputs.add(input);
        integrateInputs.add(sampleFactor);
        integrateInputs.add(sampleTime);

        _addFireMethod(_setStateMethodName, null, setStateInputs);
        _addFireMethod(_integrateMethodName, output, integrateInputs);
        _addFireMethod(_currentStateMethodName, currentState, null);

        _defaultFireMethodName = _setStateMethodName;

        // Set the fire method to the default fire method.
        _fireMethodName = getDefaultFireMethodName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The trigger port for the setState() method. */
    public TypedIOPort callSetStateMethod;

    /** The current state of the integrator output port. */
    public TypedIOPort currentState;

    /** The sample factor input port. */
    public TypedIOPort sampleFactor;

    /** The sample time input port. */
    public TypedIOPort sampleTime;

    /** The integrator initialization input port.
     *  Used for the setState() fire method.
     */
    public TypedIOPort setStateValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the SequencedIntegrator actor. call either the setState method,
     *  the integrate method, or the currentState method depending on which
     *  current fire method is set.
     *  @exception IllegalActionException If the fire method name is invalid.
     */
    @Override
    public void fire() throws IllegalActionException {
        Variable stateVar = getVariable();

        if (_fireMethodName != null
                && _fireMethodName.equals(_setStateMethodName)) {
            _setStateMethod(stateVar);
        } else if (_fireMethodName != null
                && _fireMethodName.equals(_integrateMethodName)) {
            _integrateMethod(stateVar);
        } else if (_fireMethodName != null
                && _fireMethodName.equals(_currentStateMethodName)) {
            _currentStateMethod(stateVar);
        } else {
            throw new IllegalActionException(this,
                    "Unrecognized fire method name: " + _fireMethodName);
        }
    }

    /** Perform preinitialization checks on the actor.
     *  Also call the superclass ASCETSharedMemoryActor's preinitialize() method.
     *  In ASCETSharedMemoryActor, preinitialize() sets up a type constraint between the input port
     *  and the variable (setTypeAtLeast) and the variable and the output port.
     *  Also, preinitialize() in ASCETSharedMemoryActor will create a new variable if none exists.
     *
     *  @exception IllegalActionException If the SequencedIntegrator actor's state variable is not a scalar type.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Variable var = getVariable();

        if (!(var.getToken() instanceof ScalarToken)) {
            throw new IllegalActionException(this,
                    "The state parameter variable for SequencedIntegrator actor "
                            + getName() + " must contain a ScalarToken.");
        }

        // Set type constraints for the Srv_Debouce actor's internal state.
        var.setTypeEquals(BaseType.SCALAR);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Supplies a default value for the variable, in the case that there
     *  is no initial value.
     *
     *  @return  A token containing the default value
     *  @exception IllegalActionException  Subclasses should throw an exception if
     *   an explicit initial value is required.
     */
    @Override
    protected Token _getDefaultValue() throws IllegalActionException {
        return new DoubleToken(0.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Execute the currentState method for the SequencedIntegrator actor.
     *  Output the current value of the integrator state on the currentState
     *  output port.
     *
     * @param stateVar The shared state variable for the SequencedIntegrator actor.
     * @exception IllegalActionException If there is a problem using the state variable value.
     */
    private void _currentStateMethod(Variable stateVar)
            throws IllegalActionException {
        currentState.send(0, stateVar.getToken());
    }

    /** Execute the integrate fire method for the SequencedIntegrator actor.
     *  @param stateVar The shared state variable for the SequencedIntegrator actor.
     *  @exception IllegalActionException If there is a problem using the state variable value.
     */
    private void _integrateMethod(Variable stateVar)
            throws IllegalActionException {
        ScalarToken integratorState = (ScalarToken) stateVar.getToken();

        if (sampleFactor.hasToken(0) && sampleTime.hasToken(0)
                && input.hasToken(0)) {
            ScalarToken sampleFactorToken = (ScalarToken) sampleFactor.get(0);
            ScalarToken sampleTimeToken = (ScalarToken) sampleTime.get(0);
            ScalarToken inputToken = (ScalarToken) input.get(0);

            integratorState = (ScalarToken) integratorState.add(inputToken
                    .multiply(sampleFactorToken.multiply(sampleTimeToken)));

            stateVar.setToken(integratorState);
            output.send(0, integratorState);
        } else {
            throw new IllegalActionException(
                    this,
                    "Attempt to call the integrate method on a SequencedIntegrator, "
                            + "but there is not an input token available on each of the input, "
                            + "sampleFactor, and sampleTime input ports.");
        }
    }

    /** Execute the setState fire method for the SequencedIntegrator actor. Initialize
     *  the internal state of the integrator with the value from the
     *  setStateValue input port.
     *  @param stateVar The shared state variable for the SequencedIntegrator actor.
     *  @exception IllegalActionException If there is a problem using the state variable value.
     */
    private void _setStateMethod(Variable stateVar)
            throws IllegalActionException {
        ScalarToken integratorState = null;
        if (setStateValue.hasToken(0)) {
            integratorState = (ScalarToken) setStateValue.get(0);
        } else {
            throw new IllegalActionException(
                    this,
                    "Attempt to call the setState method on a SequencedIntegrator, "
                            + "but there is no input token available on the setStateValue input.");
        }

        stateVar.setToken(integratorState);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The outState method name string. */
    private static final String _currentStateMethodName = "currentState";

    /** The out method name string. */
    private static final String _integrateMethodName = "integrate";

    /** The init method name string. */
    private static final String _setStateMethodName = "setState";
}
