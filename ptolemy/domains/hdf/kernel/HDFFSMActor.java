/* An actor containing a finite state machine (FSM) which is used in
   conjunction with an HDFFSMDirector.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

import ptolemy.domains.fsm.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.Actor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.domains.sdf.kernel.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

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
    }

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

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state. This method
     *  is called by HDFFSMDirector.
     *
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    public void setInputsFromRefinement()
            throws IllegalActionException {
	_setInputsFromRefinement();
    }

    /** Set the input variables for all ports of this actor. This
     *  method is called by HDFFSMDirector.
     *
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    public void setInputVariables() throws IllegalActionException {
	// FIXME: Only the most recently read token of each input
	// port is available as a variable. This should be extended
	// to construct arrays of variables corresponding to all tokens
	// that are read in an iteration.
	_setInputVariables();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the last chosen transition.
     *
     *  @return The last chosen transition.
     */
    protected Transition _getLastChosenTransition() {
	return _lastChosenTransition;
    }

    /** Set the input variables for the channel of the port.
     *
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor, or if the port is not an input port, or if the value
     *   variable cannot take the token read from the channel.
     */
    protected void _setInputVariables(TypedIOPort port, int channel)
            throws IllegalActionException {
	// FIXME: This should set the variable arrays as well.
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
        if (_debugging) {
            _debug(this.getFullName(), "set input variables for port",
                    port.getName());
        }
        int width = port.getWidth();
        Variable[][] pVars = (Variable[][])_inputVariableMap.get(port);
        if (pVars == null) {
            throw new InternalErrorException(getName() + ": "
                    + "Cannot find input variables for port "
                    + port.getName() + ".\n");
        }
	Token tok;
	boolean t;
        while (t = port.hasToken(channel)) {
	    if (_debugging) {
		_debug(port.getName(), "has token: " + t);
	    }
	    if (_debug_info) {
		System.out.println("FSMActor: _setInputVariables(): " 
                                + port.getName() + "has token: " + t);
	    }
	    tok = t ? BooleanToken.TRUE : BooleanToken.FALSE;
	    pVars[channel][0].setToken(tok);
	    // Update the value variable if there is a token in the channel.
	    
            tok = port.get(channel);
            if (_debugging) {
                _debug(port.getName(), "token value:", tok.toString());
            }
	    if (_debug_info) {
		System.out.println("FSMActor: _setInputVariables(): " 
                    + port.getName() + "token value:" + tok.toString());
	    }
            pVars[channel][1].setToken(tok);

	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Set to true to enable debugging.
    private boolean _debug_info = false;
    //private boolean _debug_info = true;

}
