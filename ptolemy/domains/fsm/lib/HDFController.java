/* An FSM Controller to be used with HDF

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red
*/

package ptolemy.domains.fsm.lib;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.graph.*;
import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.domains.fsm.kernel.util.VariableList;

//////////////////////////////////////////////////////////////////////////
//// HDFController
/**
An HDFController should be used instead of an FSMController when the
FSM refines a heterochronous dataflow (HDF) graph. In this case,
it is necessary that each of the states (instance of FSMState) refine
to an SDF graph.


@version $Id$
@author Brian K. Vogel
*/

public class HDFController extends FSMController implements TypedActor {

    public HDFController(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    /*  public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        if (dir == null) {
        throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
        }

        public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
        }

        public double getStopTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStopTime();
        }

        public void refireAfterDelay(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.

        dir.fireAfterDelay(this, delay);
        }
    */

    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }


    /* Add all ports contained by this controller and all ports contained
     * by the container of this controller to the scope.
     */
    // FIXME: Why is this public?
    public void setupScope()
            throws NameDuplicationException, IllegalActionException {
	
        try {
            // remove old variable lists
            if (_inputStatusVars != null) {
                _inputStatusVars.setContainer(null);
                _inputValueVars.setContainer(null);
            }
            // create new variable lists
            _inputStatusVars = new VariableList(this, INPUT_STATUS_VAR_LIST);
            _inputValueVars = new VariableList(this, INPUT_VALUE_VAR_LIST);
	    // Add all the input ports of this Controller to the scope.
            _inputStatusVars.createVariables(inputPorts());
	    // Add all the input ports of this Controller to the scope.
            _inputValueVars.createVariables(inputPorts());
	    // Add all the input ports of the Controller's container
	    // to the scope.
	    _inputStatusVars.createVariables(((CompositeEntity)getContainer()).getPorts());
	    _inputValueVars.createVariables(((CompositeEntity)getContainer()).getPorts());
	    
        } catch (IllegalActionException ex) {
        } catch (NameDuplicationException ex) {
        }
        Enumeration states = getEntities();
        FSMState state;
	// Setup the scope associated with each state. The scope
	// associated with a state consists of all of its refining
	// actor's input and output ports.
        while (states.hasMoreElements()) {
            state = (FSMState)states.nextElement();
            state.setupScope();
        }
        Enumeration transitions = getRelations();
        FSMTransition trans;
	// Setup the scope associated with each transition. The scope
	// associated with a transition consists of the union of the
	// scope of its source state and the scope of its controller.
        while (transitions.hasMoreElements()) {
            trans = (FSMTransition)transitions.nextElement();
            trans.setupScope();
        }
    }


    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

	    LinkedList result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(), outport.getTypeTerm());
			    result.insertLast(ineq);
			}
		    }
		}
	    }
	    return result.elements();

	}finally {
	    workspace().doneReading();
	}
    }

}
