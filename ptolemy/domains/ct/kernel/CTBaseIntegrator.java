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
Base class for integrators for continuous time simulation. An integrator
is a dynamic actor that emit a token (internal state) at the beginning
of the simulation. For different ODE solving method, the functionality
of a integrator could be different. This class provide a basic 
implementation of the integrator, and the hook methods that point the
implementations of action method to the solver. 
<P>
An integrator has one input port and one output port. Conceptually,
the input is the differential of the output. So an ordinary
differential equation dx/dt = f(x,t) can be represented by:
                --------------- 
        dx/dt  |               |   x
     --------->|   Integrator  |----------------->
    |          |               |         |
    |           ---------------          |
    |                                    |
    |             |---------|            |
    |-------------| f(x,t)  |<-----------|
                  |---------|
<P>      
An integrator has one parameter: <code>initialState</code>, which
can be set use setParam(). The initialState will not impact the simulation
after the simulation starts. The default value of the parameter is 0.
An integrator has one state, and possibly several auxiliary variables--
<code>_auxVariabless</code>. The number of <code>_auxVariabless</code> is get 
from the ODE solver.

@author Jie Liu
@version $Id$
@see ODESolver
@see CTDiretor
*/
public class CTBaseIntegrator extends CTActor implements CTErrorControlActor {
    /** Construct an integrator in the default workspace with an
     *  empty string name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  A integrator is dynamic, with a single input port and a single
     *  output port.
     *
     *  @param isDynamic true if the actor is a dynamic actor.
     *  @exception NameDuplicationException Never thrown.
     *  @exception IllegalActionException Never thrown.
     */
    public CTBaseIntegrator()
            throws NameDuplicationException, IllegalActionException {
	super(true);
        input = new IOPort(this, "input");
        input.makeInput(true);
        output = new IOPort(this, "output");
        output.makeOutput(true);
        _initState = 0.0;
        _paramInitState = new Parameter(this, "InitialState", 
            new DoubleToken(_initState));
    }

    /** Construct an integrator in the specified workspace with an empty
     *  string as the name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  A integrator is dynamic, with a single input port and a single
     *  output port.
     *
     *  @param workspace The workspace that will list the entity.
     *  @exception NameDuplicationException Never thrown.
     *  @exception IllegalActionException Never thrown.
     */
    public CTBaseIntegrator(Workspace workspace) 
            throws NameDuplicationException, IllegalActionException {
        super(workspace, true);
        input = new IOPort(this, "input");
        input.makeInput(true);
        output = new IOPort(this, "output");
        output.makeOutput(true);
        _paramInitState = new Parameter(this, "InitialState", 
            new DoubleToken(_initState));
    }

    /** Construct an integrator, with a name, a input port, a output port
     *  and a CTSubSystem as the container.
     *  A integrator is dynamic, with a single input port and a single
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
    public CTBaseIntegrator(CompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException {
        super(container, name, true);
        input = new IOPort(this, "input");
        input.makeInput(true);
        output = new IOPort(this, "output");
        output.makeOutput(true);
        _paramInitState = new Parameter(this, "InitialState", 
            new DoubleToken(_initState));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire method in the execution sequence. It in turn calls
     *  the integratorFire() of the ODE solver of the director.
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

    /** Return the _auxVariabless in a double array. This method get the 
     *  _auxVariabless stored in the integrator. Return null if the 
     *  _auxVariables has never been created.
     *
     *  @return The _auxVariabless in a double array.
     */
    public double[] getAuxVariables() {
        return _auxVariables;
    }

    /** Update initial state parameter.
     *
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the 
     *       integratorInitialize() of the solver.
     */
    public void initialize() throws IllegalActionException {
        _initState = ((DoubleToken)_paramInitState.getToken()).doubleValue();
        _potentialState = _initState;
    }

    /** Excite output.
     */
    public void exciteOutputs() throws IllegalActionException {
        output.broadcast(new DoubleToken(_potentialState));
    }

    /** Postfire method in the execution sequence. It in turn calls
     *  the integratorPostfire() of ODE solver of the director.
     *
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the 
     *       integratorInitialize() of the solver.
     */
    public boolean postfire() throws IllegalActionException {
        _state = _potentialState;
        return true;
    }

    /** Prefire method in the execution sequence. It in turn calls
     *  the integratorPrefire() of ODE solver of the director.
     *
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
        
        int n = solver.auxVariableNumber();
        if((_auxVariables == null) || (_auxVariables.length < n)) {
            _auxVariables = new double[n];
        }
        return true;
    }

    /** Backup the saved state to current state. This method may be used
     *  for backup the simulation from a previous time point.
     */
    public void rollback(int count) {
        setState(_storedState);
    }

    /** Remember the current state. This remembered state can be
     *  retrieved by the restoreState() method. The remembered state
     *  may be used for back up simulation from past.
     */
    public void saveState() {
        _storedState = getState();
    }
    
    /** Set the potential state. Potential state is the state that
     *  the ODE solver think to be the fixed point. This may not
     *  be the final state due to the event detection.
     */
     public void setPotentialState(double value) {
         _potentialState = value;
     }


    /** Set the state of the integrator.
     *  NOTE: Should only be called by Directors.
     *
     *  @param value The value to be set to the state.
     */
    public void setState(double value) {
        _state = value;
    }   

    /** Set the value of a temporary state. The number of temporary states
     *  is set by newAuxVariables, which in turn calls the 
     *  integratorNewAuxVariables()
     *  of the solver. If the index is out of the bound of the auxVariables
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

    /** Wrapup method in the execution sequence. It in turn calls
     *  the integratorWrapup() of the ODE solver of the director.
     *
     *  @exception IllegalActionException If there's no director or
     *       the director has no ODE solver, or thrown by the 
     *       integratorInitialize() of the solver.  
     */
    public void wrapup() throws IllegalActionException {
        try {
            input.get(0) ;
        } catch(NoTokenException e) {
            //ignore
        }
    }

    /** Return true if current step is successful.
     */	
    public boolean isSuccessful() {
        return true;
    }

    /** Return the suggested next step size.
     */
    public double suggestedNextStepSize() {
        return 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////
    /** Input port. Finals means they are not changeable once created.
     */
    public final IOPort input;

    /** Input port. Finals means they are not changeable once created.
     */
    public final IOPort output;

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
    
    // The state stored, may be used for back up simulation
    private double _storedState;
}

