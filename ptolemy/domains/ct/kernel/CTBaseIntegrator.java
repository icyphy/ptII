/* Base class for integrators in the CT domain.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTBaseIntegrator
/**
Base class for integrators for continuous time simulation. 
An integrator has one input port and one output port. Conceptually,
the input is the differential of the output. So an ordinary
differential equation dx/dt = f(x,t) can be represented by:
<P>
<pre>
<pre>               +---------------+
<pre>        dx/dt  |               |   x
<pre>    +--------->|   Integrator  |---------+----->
<pre>    |          |               |         |
<pre>    |          +---------------+         |
<pre>    |                                    |
<pre>    |             |---------|            |
<pre>    +-------------| f(x,t)  |<-----------+
<pre>                  |---------|
</pre></pre></pre></pre></pre></pre></pre></pre></pre></pre>

<P>
An integrator
is a dynamic actor that emit a token (internal state) at the beginning
of the simulation. An integrator is an error control actor that can control
the accuracy of the ODE solution by adjusting step sizes. An integrator has
one memory, which is its state. 
<P>
For different ODE solving methods, the functionality
of a integrator could be different. This class provide a basic
implementation of the integrator, in which some solver-dependent method
are hooked to the current ODE solver.
<P>
An integrator has one parameter: <code>initialState</code>. At the 
initialization stage of the simulation, the state of the integrator is 
set to the initial state. The initialState will not impact the simulation
after the simulation starts. The default value of the parameter is 0.
An integrator can possibly have several auxiliary variables--
<code>_auxVariables</code>. The number of <code>_auxVariabless</code> is get
from the ODE solver. 
<P>
The integrator has a history array, which remembered the states and 
their derivative for the past several steps. The history is used for
multistep methods.

@author Jie Liu
@version $Id$
@see ODESolver
@see CTDiretor
*/
public class CTBaseIntegrator extends CTActor
        implements CTErrorControlActor, CTDynamicActor, CTMemarisActor {
    /** Construct an integrator in the default workspace with an
     *  empty string name.
     *  A integrator has one single input port and one single
     *  output port.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     *  @exception NameDuplicationException Never thrown.
     *  @exception IllegalActionException Never thrown.
     */
    public CTBaseIntegrator()
            throws NameDuplicationException, IllegalActionException {
	super();
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _initState = 0.0;
        _paramInitState = new Parameter(this, "InitialState",
            new DoubleToken(_initState));
    }

    /** Construct an integrator in the specified workspace with an empty
     *  string as the name. You can then change the name with setName().
     *  A integrator has one single input port and one single
     *  output port.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace that will list the entity.
     *  @exception NameDuplicationException Never thrown.
     *  @exception IllegalActionException Never thrown.
     */
    public CTBaseIntegrator(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _paramInitState = new Parameter(this, "InitialState",
            new DoubleToken(_initState));
    }

    /** Construct an integrator, with a name, a input port, a output port
     *  and a CTSubSystem as the container.
     *  A integrator has one single input port and one single
     *  output port.
     *
     * @param container The CTSubSystem that contains this integrator.
     * @param name The name
     * @return The CTBaseIntegrator
     * @exception NameDuplicationException If the name is used by another
     *            actor.
     * @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container. FIXME: never happens?
     */
    public CTBaseIntegrator(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);
        _paramInitState = new Parameter(this, "InitialState",
            new DoubleToken(_initState));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire() method in the execution sequence. It in turn calls
     *  the integratorFire() of the current ODE solver.
     *
     *  @exception IllegalActionException If there's no director or the
     *       director has no ODE solver, or thrown by integratorFire()
     *       of the solver.
     */
    public void fire() throws  IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        if(dir == null) {
            throw new IllegalActionException( this,
                    " has no director.");
        }
        ODESolver solver = (ODESolver)dir.getCurrentODESolver();
        if(solver == null) {
            throw new IllegalActionException( dir,
                    " has no ODE solver.");
        }
        solver.integratorFire(this);
    }

    /** Return the _auxVariables in a double array. This method get the
     *  _auxVariables stored in the integrator. Return null if the
     *  _auxVariables has never been created.
     *
     *  @return The _auxVariabless in a double array.
     */
    public double[] getAuxVariables() {
        return _auxVariables;
    }

    /** Return the history information of the last index-th step.
     *  The history array is a two dimensional array, in which 
     *  the first dimension is the capacity of the history,
     *  and the second dimension has length 2.<BR> 
     *  history[*][0] is the state at the last index-th step;<Br>
     *  history[*][1] is its derivative.<Br>
     *  @param index The index
     *  @return The history array at that index point.
     *  @exception IllegalActionException If the index is out of bound.
     */
    public double[] getHistory(int index) throws IllegalActionException {
        if( index != 0) {
            throw new IllegalActionException(this,
                " history request out of range.");
        }
        return _history;
    }

    /** Return the history capacity.
     *  FIXME: In this implementation, it always returns 1.
     */
    public final int getHistoryCapacity() {
        return 1;
    }

    /** Returns the initial state.
     *
     *  @return the initial state.
     */
    public double getInitialState() {
        return _initState;
    }

    /** Returns the potential state.
     *
     *  @return the potential state.
     */
    public double getPotentialState() {
        return _potentialState;
    }


    /** Return the state of the integrator.
     *
     *  @return A double number as the state of the integrator.
     */
    public double getState() {
        return _state;
    }

    /** Update initial state parameter. Set the initial state to
     *  the potential state.
     *
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the
     *       integratorInitialize() of the solver.
     */
    public void initialize() throws IllegalActionException {
        _initState = ((DoubleToken)_paramInitState.getToken()).doubleValue();
        _potentialState = _initState;
    }

    /** Emit the tentative output, which is the tentative state of the
     *  integrator.
     *  @exception IllegalActionException If the data transfer is not 
     *  completed.
     */
    public void emitTentativeOutputs() throws IllegalActionException {
        output.broadcast(new DoubleToken(_potentialState));
    }

    /** Return true if last integration step is successful.
     *  It in turn calls the integratorIsSuccess() method of the current
     *  ODE solver.
     *  @return True if last integration step is successful.
     */
    public boolean isSuccessful() {
        ODESolver solver = ((CTDirector)getDirector()).getCurrentODESolver();
        return solver.integratorIsSuccess(this);
    }

    /** Postfire method in the execution sequence. It updates the 
     *  the states and push the current state and its derivative
     *  into history.
     *  @return True always.
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the
     *       integratorInitialize() of the solver.
     */
    public boolean postfire() throws IllegalActionException {
        _state = _potentialState;
        _pushHistory(_potentialState, _potentialDerivative);
        return true;
    }

    /** Prefire method in the execution sequence. This method checks
     *  if there enough auxVariables in the integrator given the
     *  current ODE solver. If not, create more auxVariables.
     *  @return True always.
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the
     *       integratorInitialize() of the solver.
     */
    public boolean prefire() throws IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        if(dir == null) {
            throw new IllegalActionException( this,
                    " has no director avalable");
        }
        ODESolver solver = (ODESolver)dir.getCurrentODESolver();
        if(solver == null) {
            throw new IllegalActionException( this,
                    " has no ODE solver avalable");
        }

        int n = solver.integratorAuxVariableNumber();
        if((_auxVariables == null) || (_auxVariables.length < n)) {
            _auxVariables = new double[n];
        }
        return true;
    }

    /** Restore the saved state to current state. This method may be used
     *  for rollback the simulation from a previous time point.
     */
    public void restoreStates() {
        _setState(_storedState);
        setPotentialState(_storedState);
    }

    /** Remember the current state. This remembered state can be
     *  retrieved by the restoreState() method. The remembered state
     *  may be used for back up simulation from past.
     */
    public void saveStates() {
        _storedState = getState();
    }

    /** Set the value of an auxVariable.  If the index is out of
     *  the bound of the auxVariables
     *  array, an IllegalActionException is thrown to indicate a error
     *  in the integration method.
     *
     *  @param index The index in the auxVariables array.
     *  @param value The value to be set.
     *  @exception IllegalActionException If the index is out of the range
     *       of the temporary states array.
     */
    public void setAuxVariables(int index, double value)
            throws IllegalActionException {
        try {
            _auxVariables[index] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalActionException(this,
                    "index out of the range of auxVariables.");
        }
    }

    /** Set history capacity. If the argument is less than 0,
     *  the capacity is set to 0.
     *  @param cap The capacity.
     */
    // FIXME: In the current implementation, it does nothing.
    public final void setHistoryCapacity(int cap) {
    }

    /** Set the potential state. Potential state is the state that
     *  the ODE solver think to be the new state for the integrator.
     *  It may not
     *  be the final state due to the event detection.
     *  @param value The value to be set. 
     */
     public final void setPotentialState(double value) {
         _potentialState = value;
     }

    /** Set the potential derivative dx/dt. Potential derivative
     *  is the derivative of the state that
     *  the ODE solver think to be at the fixed point. This may not
     *  be the final derivative due to the event detection.
     *  @param value The value to be set. 
     */
    public final void setPotentialDerivative(double value) {
         _potentialDerivative = value;
     }

    /** Return the suggested next step size. It in turn calls the 
     *  integratorSuggestedNextStepSize() in the current ODE solver.
     *  @return The suggested next step size.
     */
    public double suggestedNextStepSize() {
        ODESolver solver = ((CTDirector)getDirector()).getCurrentODESolver();
        return solver.integratorSuggestedNextStepSize(this);
    }

    /** Wrapup method in the execution sequence. It cleans the 
     *  unconsumed tokens at the input port.
     *
     *  @exception IllegalActionException Never thrown.
     */
    public void wrapup() throws IllegalActionException {
        try {
            input.get(0) ;
        } catch(NoTokenException e) {
            //ignore
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////
    /** Input port. Finals means they are not changeable once created.
     */
    public final TypedIOPort input;

    /** Input port. Finals means they are not changeable once created.
     */
    public final TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /** Push the state and derivative into the history storage.
     *  If the history capacity is full, then the oldest history
     *  will be lost. The contents of the history storage can be
     *  retrieved by getHistory() method.
     */
    /*  FIXME: In this implementaion, the history storage has
     *        capacity 1.
     */
    protected void _pushHistory(double state, double derivative) {
        _history[0] = state;
        _history[1] = derivative;
    }

    /** Set the state of the integrator.
     *
     *  @param value The value to be set to the state.
     */
    protected final void _setState(double value) {
        _state = value;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Parameter initial state.
    private Parameter _paramInitState;
    private double _initState;

    // Temporary states array.
    private double[] _auxVariables;
    // State.
    private double _state;
    // potential state;
    private double _potentialState;
    // Potential derivative;
    private double _potentialDerivative;

    // The state stored, may be used for back up simulation
    private double _storedState;

    // The history states and its derivative.
    // This variable is needed by Linear Multistep (LMS) methods,
    // like Tropezoidal rule.
    // FIXME: In the current implememtaion, the highest LMS method is of
    // order 2. So only the information of the last step is needed. But
    // the interface is provided to have multiple histories and retrive
    // them by index.
    private double[] _history = new double[2];
}

