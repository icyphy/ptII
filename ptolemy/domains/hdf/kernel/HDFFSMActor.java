/* An actor containing a finite state machine (FSM) which is used in
   conjunction with an HDFFSMDirector.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMActor
/**
An HDFFSMActor is used in a modal model to represent the mode control
logic. An HDFFSMActor contains a set of states and transitions. Each
state must have a TypedCompositeActor refinement. The refinement
actor is set by the <i>refinementName</i> parameter of State. A
state transition may only occur immediately following a
"Type B firing" [1], which is the last firing of an HDF actor in
the current iteration of the HDF schedule. A transition is
enabled when its guard expression is true. When a type B firing
occurs, the outgoing transitions of the current state
are examined. An exception is thrown if there is more than one
enabled transition. If there is exactly one enabled transition then
the current state of the actor is set to the destination state
of the enabled transition.
<p>
An HDFFSMActor enters its initial state during initialization.
The name of the initial state is specified by the
<i>initialStateName</i> parameter.
<p>
An HDFFSMActor contains a set of variables for the input ports that
can be referenced in the guard expression of a transition. If an
input port is a single port, one variable and one variable array are
created: an input value variable with the name <i>portName</i> and
an array with the name <i>portName</i>. The input value
variable always
contains the latest token received from the port. The input variable
array contains all tokens read during the current iteration of the
 HDF schedule. If the HDF actor that this FSM refines has a port
rate of M, and a firing count of N, then the length of the token
array is M*N.
<p>
If the given port is a multiport, a value variable and a value
array are created for each channel. The value variable is named
<i>portName</i>_<i>channelIndex</i> and the value array is named
<i>portName</i>_<i>channelIndex</i>.
<p>
<b>References</b>
<p>
<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee,
``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>
</ol>

@author Brian K. Vogel
@version $Id$
@see HDFFSMDirector
@see HDFDirector
*/
public class HDFFSMActor extends FSMActor implements TypedActor {

    /** Construct an HDFFSMActor in the default workspace with an
     *  empty string as its name. Add the actor to the workspace
     *  directory. Increment the version number of the workspace.
     */
    public HDFFSMActor() {
        super();
        _init();
    }

    /** Construct an HDFFSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public HDFFSMActor(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Create an HDFFSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HDFFSMActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** A parameter representing the number of most recently read tokens
     *  that are available for use in a state transition guard expression.
     *  The default value for this parameter is 1.
     */
    public Parameter tokenHistorySize;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the enabled transition among the given list of transitions.
     *  Throw an exception if there is more than one transition enabled.
     *  This method is called by HDFFSMDirector.
     *
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or if thrown by any choice action contained
     *   by the enabled transition.
     */
    public Transition chooseTransition(List transitionList)
            throws IllegalActionException {
        return _chooseTransition(transitionList);
    }

    /** Return the current state of this actor.
     *
     *  @return The current state of this actor.
     */
    public State getCurrentState() {
        return _currentState;
    }

    /** Set the current state of this actor.
     *
     *  @param state The state to set.
     */
    public void setCurrentState(State state) {
        _currentState = state;
    }

    /** Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state. This method is called by HDFFSMDirector.
     *
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    public void setCurrentConnectionMap() throws IllegalActionException {
        _setCurrentConnectionMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create input variables for the port. The variables are contained
     *  by this actor and can be referenced in the guard and trigger
     *  expressions of transitions.
     *  If the given port is a single port, two variables are created:
     *  one is input status variable with name "<i>portName</i>_isPresent";
     *  the other is input value variable with name "<i>portName</i>". The
     *  input status variable always contains a BooleanToken. When this
     *  actor is fired, the status variable is set to true if the port has
     *  a token, false otherwise. The input value variable always contains
     *  the latest token received from the port.
     *  If the given port is a multiport, a status variable and a value
     *  variable are created for each channel. The status variable is
     *  named "<i>portName</i>_<i>channelIndex</i>_isPresent".
     *  The value variable is named "<i>portName</i>_<i>channelIndex</i>".
     *  If a variable to be created has the same name as an attribute
     *  already contained by this actor, the attribute will be removed
     *  from this actor by setting its container to null.
     *  @param port A port.
     *  @exception IllegalActionException If the port is not contained
     *   by this FSMActor or is not an input port.
     */
    protected void _createInputVariables(TypedIOPort port)
            throws IllegalActionException {
        if (_debug_info) {
            System.out.println(this.getName() +
                    ": _createInputVariables " +
                    "invoked on port " + port.getName());
        }
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "that is not input.");
        }
        // If there are input variables already created for the port,
        // remove them.
        if (_inputVariableMap.get(port) != null) {
            _removeInputVariables(port);
        }
        int width = port.getWidth();
        if (width == 0) {
            return;
        }
        Variable[][] pVars = new Variable[width][2];

        int historySize =
            ((IntToken)(tokenHistorySize.getToken())).intValue();
        if (_debug_info) {
            System.out.println(this.getName() + ": tokenHistorySize = " +
                    historySize);

        }
        Variable[][] recentlyReadVariables = new Variable[width][historySize];
        boolean addChIndex = (width > 1);
        for (int chIndex = 0; chIndex < width; ++chIndex) {
            String vName = null;
            if (addChIndex) {
                vName = port.getName() + "_" + chIndex + "_isPresent";
            } else {
                vName = port.getName() + "_isPresent";
            }
            if (_debug_info) {
                System.out.println("chIndex = " + chIndex);
                System.out.println(this.getName() +
                        ": Added Vaiable = " + vName);
            }
            try {
                pVars[chIndex][0] = new Variable(this, vName);
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error creating input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            }
            if (addChIndex) {
                vName = port.getName() + "_" + chIndex;
            } else {
                vName = port.getName();
            }
            if (_debug_info) {
                System.out.println(this.getName() +
                        ": Added Vaiable = " + vName);
            }
            try {
                pVars[chIndex][1] = new Variable(this, vName);
            } catch (NameDuplicationException ex) {
                throw new InvalidStateException(this,
                        "Error creating input variables for port.\n"
                        + ex.getMessage());
            }
            // Now create the HDF-specific variables.

            for (int historyIndex = 0; historyIndex < historySize; historyIndex++) {
                if (_debug_info) {
                    System.out.println("historyIndex = " + historyIndex);
                }
                String variableName = null;
                if (addChIndex) {
                    variableName = port.getName() + "_" + chIndex +
                        "_$" + historyIndex;
                } else {
                    variableName = port.getName() + "_$" + historyIndex;
                }
                if (_debug_info) {
                    System.out.println(this.getFullName() +
                            ": Adding variable with name = " +
                            variableName);
                }
                try {
                    recentlyReadVariables[chIndex][historyIndex]
                        = new Variable(this, variableName);
                } catch (NameDuplicationException ex) {
                    throw new InvalidStateException(this,
                            "Error creating input variables for port.\n"
                            + ex);
                }

            }
        }
        // The map is for generic FSM variables.
        _inputVariableMap.put(port, pVars);
        // The map is for HDF-specifc variables.
        _hdfInputVariableMap.put(port, recentlyReadVariables);
    }


    /** Return the last chosen transition.
     *
     *  @return The last chosen transition.
     */
    protected Transition _getLastChosenTransition() {
        return _lastChosenTransition;
    }

    /** Remove the input variables created for the port.
     *  @see #_createInputVariables(TypedIOPort port)
     *  @param port A port.
     */
    protected void _removeInputVariables(TypedIOPort port) {
        super._removeInputVariables(port);
        // Now remove all HDF-specific variables.
        Variable[][] recentlyReadVariables = (Variable[][])_hdfInputVariableMap.get(port);
        if (recentlyReadVariables == null) {
            return;
        }
        for (int index = 0; index < recentlyReadVariables.length; ++index) {
            for (int index2 = 0; index2 < recentlyReadVariables[0].length; ++index2) {
                try {
                    Variable v = recentlyReadVariables[index][index2];
                    if (v != null) {
                        v.setContainer(null);
                    }
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(getName() + ": "
                            + "Error removing input variables for port "
                            + port.getName() + ".\n"
                            + ex.getMessage());
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(getName() + ": "
                            + "Error removing input variables for port "
                            + port.getName() + ".\n"
                            + ex.getMessage());
                }
            }
        }
    }

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    protected void _setInputsFromRefinement()
            throws IllegalActionException {
        super._setInputsFromRefinement();
    }
    /** Set the input variables for all ports of this actor.
     *
     *  @param firings The number of times this actor has been fired in
     *   the current iteration - 1. If the firing count of this actor is
     *   M, then the value of this parameter ranges from 0 to M-1.
     *   @param firingsPerSchedulIteration The firing count of this actor
     *   in the current iteration.
     *
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    protected void _setInputVariables(int firings, int firingsPerIteration)
            throws IllegalActionException {
        Iterator inports = inputPortList().iterator();

        while (inports.hasNext()) {
            TypedIOPort p = (TypedIOPort)inports.next();
            int width = p.getWidth();
            for (int channel = 0; channel < width; ++channel) {
                _setInputVariables(p, channel, firings,
                        firingsPerIteration);
            }
        }
    }

    /** Set the input variables for the channel of the port.
     *
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @param firings The number of times this actor has been fired in the
     *   current iteration - 1. If the firing count of this actor is
     *   M, then the value of this parameter ranges from 0 to M-1.
     *  @param firingsPerIteration The firing count of this actor
     *   in the current iteration.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor, or if the port is not an input port, or if the value
     *   variable cannot take the token read from the channel.
     */
    protected void _setInputVariables(TypedIOPort port, int channel,
            int firings, int firingsPerIteration)
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot set input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            throw new IllegalActionException(this, port,
                    "Cannot set input variables for port "
                    + "that is not input.");
        }
        if (_debug_info) {
            System.out.println(this.getFullName() +
                    ": setting input variables for port " +
                    port.getName());
        }
        int width = port.getWidth();
        Variable[][] pVars = (Variable[][])_inputVariableMap.get(port);
        if (pVars == null) {
            throw new InternalErrorException(getName() + ": "
                    + "Cannot find input variables for port "
                    + port.getName() + ".\n");
        }
        Variable[][] hdfVars  = (Variable[][])_hdfInputVariableMap.get(port);
        if (hdfVars == null) {
            throw new InternalErrorException(getName() + ": "
                    + "Cannot find input variables for port "
                    + port.getName() + ".\n");
        }
        Token tok;
        boolean t;
        int currentTokenIndex = 0;
        while (t = port.hasToken(channel)) {
            if (_debug_info) {
                System.out.println(port.getName() + "has token: " + t);
            }
            if (_debug_info) {
                System.out.println("FSMActor: _setInputVariables(): "
                        + port.getName() + "has token: " + t);
            }
            tok = t ? BooleanToken.TRUE : BooleanToken.FALSE;
            pVars[channel][0].setToken(tok);
            // Update the value variable if there is a token in the channel.

            tok = port.get(channel);
            if (_debug_info) {
                System.out.println(port.getName() + "token value:" + tok.toString());
            }
            if (_debug_info) {
                System.out.println("FSMActor: _setInputVariables(): "
                        + port.getName() + "token value:" + tok.toString());
            }
            pVars[channel][1].setToken(tok);
            // HDF specific:
            int portRate = SDFScheduler.getTokenConsumptionRate(port);
            int index = firingsPerIteration*portRate - 1 -
                (firings*portRate + currentTokenIndex);
            int historySize =
                ((IntToken)(tokenHistorySize.getToken())).intValue();
            if (index < historySize) {
                hdfVars[channel][index].setToken(tok);
            }
            currentTokenIndex++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the tokenHistorySize parameter and set the the default
     *  value of 100.
     */
    private void _init() {
        try {
            tokenHistorySize
                = new Parameter(this,"tokenHistorySize",new IntToken(1));
        } catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default tokenHistorySize parameter:\n" +
                    e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // A map from ports to corresponding hdf-specific input variables.
    protected Map _hdfInputVariableMap = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Set to true to enable debugging.
    private boolean _debug_info = false;
    //private boolean _debug_info = true;

}
