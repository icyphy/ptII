/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.expr.Variable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector
/**
<h1>HDF overview</h1>
Read [1].
<h1>Class comments</h1>
An HDFFSMDirector governs the execution of the finite state
machine in heterochronous dataflow model. This class should be used
as the director of a finite state machine when the finite state
 machine refines a heterochronous dataflow (HDF) actor. Note that
currently, there is no HDF director. Instead, an SDFDirector should
be used as the director for a HDF model.
<p>
<h1>Usage</h1>
The toplevel graph must be HDF. The toplevel HDF diagram should be
constructed using the SDFDirector as the toplevel's local director.
The toplevel model is constructed just like an SDF model, except that
it may contain HDF actors as well as SDF actors. All HDF actors must
refine to an FSM with this class as the FSM's local director. All
states in the FSM must refine to either another FSM, an HDF diagram
or a SDF diagram. There is no constraint on the number of levels in
the hierarchy.
<p>
Currently, constructing the FSM is somewhat akward. To construct an FSM,
first create a TypedCompositeActor in the HDF diagram to contain
the FSM. This TypedCompositeActor will henceforth be refered to as
"the HDF actor." Create an HDFFSMDirector with the HDF actor as its
container. Create an HDFFSMController actor with the HDF actor as
its container. Create a TypedComposite actor (one for each state
in the FSM) with the HDF actor as its container. This
TypedComposite actor is henceforth refered to as "the refining state
actor." Create the necessary ports on each refining state actor
such that each refining state actor contains the same number and
type of ports (typically input or output TypedIOPort) with the
same name as the corresponding ports of the HDF actor. Create a
relation for each port of the HDF actor. For each relation, link
all ports with the same name (1 + number of states) to the relation.
<p>
The FSM diagram itself is constructed inside the HDFFSMController.
To construct the FSM diagram, create an HDFFSMState actor (one
for each state in the FSM) with the HDFFSMController as its
container. Call the setRefinement() mehtod of HDFFSMState to
set its refining state actor.
Use the setInitialState() method of HDFFSMController to
set the initial state. Create a HDFFSMTransition (one for each transition)
with the HDFFSMController as its container. The guard expression
of a transition is set by using the setTriggerCondition() method
of HDFFSMTransition, with a guard expression as the parameter.
The guard expression is evaluated only after a "Type B firing" [1],
which is the last firing of the HDF actor in the current iteration.
A state transition (possibly back to the current state) will occurr
if the guard expression evaluates to true.
<p>
<h1>Guard expression syntax</h1>
The guard expressions use the Ptolemy II expression language. Currently,
the only variables allowed in the guard expressions are variables
containing tokens transfered through the input and output ports of
the HDF actor. Following the syntax of [1], if the HDF actor contains
an input port called dataIn, then use dataIn$0 in the guard
expression to reference the token most recently transfered through
port dataIn. Use dataIn$1 to reference the next most recent token,
dataIn$2 to reference the next most recent token, and so on. By
default, only the most recently transfered token is allowed.
In order to be able to reference up to the m'th most recently
transfered token (dataIn$m), call the setGuardTokenHistory()
method with m as the parameter.

<H1>
References</H1>

<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee, "<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>," April 13,
1998.</LI>

@author Brian K. Vogel
@version: $Id$
*/
// FIXME:
//There is currently the following constraint on port names: All ports
//that are linked to an input port of this director's container must
//have have the same name (the name of the input port). The same goes
//for output ports.
public class HDFFSMDirector extends FSMDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HDFFSMDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public HDFFSMDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public HDFFSMDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
	// FIXME: Remove this when get something better.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        HDFFSMDirector newobj = (HDFFSMDirector)super.clone(ws);
        newobj._controller = null;
        return newobj;
    }

    /** Return the current state of the controller.
     */
    public FSMState currentState() {
        return _controller.currentState();
    }

    public Actor currentRefinement() {
        return _controller.currentRefinement();
    }



    /** Invoke an iteration on the current state's refinement. Update
     *  the count of the number of times the current state's refinement
     *  has been fired in the current iteration of the HDF diagram in
     *  which this FSM is contained.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        // _controller must not be null
	//if (_debugging) _debug("HDFFSMDirector: fire()");
	if (_controller != null) {
	    _controller.fire();
	} else {
	    throw new IllegalActionException(this,
                    "HDFFSMDirector must have a contoller. "
                    + "Use setController() to set the contoller"
                    + " and try again.");
	}
    }

    /** Return the number of tokens in the guard token history.
     *  This returns the value set by setGuardTokenHistory().
     *  The default is one.
     *
     *  @return The number of tokens in the history.
     */
    public int getGuardTokenHistory() {
	return _guardLength;
    }


    /** Create receivers and then invoke the initialize()
     *  methods of all its deeply contained actors.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
	//if (_debugging) _debug("HDFFSMDirector: initialize()");
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            Enumeration allActors =
                Collections.enumeration(container.deepEntityList());
            while (allActors.hasMoreElements()) {
                Actor actor = (Actor)allActors.nextElement();
                if (actor == _controller) {
                    continue;
                } else {
		    //if (_debugging) _debug("HDFFSMDirector: initialize(): " +
		    //"initializing " +
		    //		   ((NamedObj)actor).getFullName());
                    actor.initialize();
                }
            }
	    //if (_debugging) _debug("HDFFSMDirector: initialize(): " +
	    //      "initializing " +
	    //		   ((NamedObj)_controller).getFullName());
            _controller.initialize();
        }



    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Call postfire() on the current refinement (an HDF/SDF diagram)
     *  and then call postfire() on the controller.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it.
     */
    // FIXME: move the controller's postfire() code here.
    public boolean postfire() throws IllegalActionException {
        // elaborate
        Actor refine = _controller.currentRefinement();
        if (refine != null) {
            refine.postfire();
            /*Enumeration outports = refine.outputPorts();
              while(outports.hasMoreElements()) {
              IOPort p = (IOPort)outports.nextElement();
              transferOutputs(p);
              }*/
        }
        return _controller.postfire();
    }

    /** If the prefire() method of the current refinement returns
     *  false, then return false. Otherwise return true.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it. Or if the current refinement is null.
     */
    public boolean prefire() throws IllegalActionException {
        // elaborate
        Actor refine = _controller.currentRefinement();

        boolean result = true;
        if (refine != null) {
            result = refine.prefire();
        } else {
	    throw new IllegalActionException(this,
                    "Current refinement is null in prefire().");
	}

        return result;
    }


    /** Initialize the guard variables, which are used in the
     *  state transition expressions. Then Create receivers and then
     *  invoke the preinitialize()
     *  methods of all its deeply contained actors.
     *  This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {

        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
	    // Initialize input guard variables.

	    if (_portNameToArrayFIFOQueue == null) {
		// Initialize the map from a port name to
		// its assiciated ArrayFIFOQueue of most
		// recently transfered tokens.
		_portNameToArrayFIFOQueue = new HashMap();
	    }

	    Enumeration containPorts =
                Collections.enumeration(container.portList());
	    while (containPorts.hasMoreElements()) {
		TypedIOPort aPort = (TypedIOPort)containPorts.nextElement();
		//if (_debugging)
                //   _debug("guard: port name:" + aPort.getName());
		// Array to store queue of tokens to be used in evaluating
		// the state transition guard expression.

		ArrayFIFOQueue guardTokenArray =
		    new ArrayFIFOQueue(getGuardTokenHistory());


		// Fill up guardTokenArray. The queue should always
		// be full so that its size does not need to be checked
		// on each call to fire().
		while (!guardTokenArray.isFull()) {
		    //Token tempToken = new Token();
		    // FIXME: let user decide on initial token type
		    // and value?
		    Token tempToken = new IntToken(0);
		    guardTokenArray.put(tempToken);
		    //if (_debugging) _debug("guard: puting temparary " +
				//	   " token in guardTokenArray");
		}

		// Create a mapping from the current port's name to
		// a queue to be used to store the _guardLength most
		// recently read in tokens.
		if (!_portNameToArrayFIFOQueue.containsKey(
                        aPort.getFullName())) {
		    _portNameToArrayFIFOQueue.put(aPort.getFullName(),
                            guardTokenArray);
		}

		// Create guard variables for this port, for use
		// in state transition guard expressions.
		_createGuardVariables(aPort);

	    }

	    // End of Initialize guard variables.


            Enumeration allactors =
                Collections.enumeration(container.deepEntityList());
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                //if (_debugging) _debug("Invoking preinitialize(): ",
                //((NamedObj)actor).getFullName());
                actor.preinitialize();
            }
        }

        //if (_debugging) _debug("Finished preinitialize().");
    }


    /** Set the controller associated with this director. This method
     *  must be called in the model code.
     */
    public void setController(HDFFSMController ctrl)
            throws IllegalActionException {
	//if (_debugging) _debug("HDFFSMDirector: setController()");
	// Check that _controller is not already set.
	if (_controller == null) {
	    if (getContainer() == null) {
		throw new IllegalActionException(this, ctrl,
                        "HDFFSMDirector must have a container to set its "
                        + "controller.");
	    }
	    if (getContainer() != ctrl.getContainer()) {
		throw new IllegalActionException(this, ctrl,
                        "HDFFSMDirector must have the same container as its "
                        + "controller.");
	    }
	    _controller = ctrl;
	} else {
	    throw new IllegalActionException(this,
                    "setController() was already called."
                    + "HDFFSMDirector can only have one controller");
	}
    }

    /** Set the number of guard tokens allowed (largest m in dataIn$m)
     *  for each port. This number is common to all port. The default
     *  value is 1.
     *  <p>
     *  As an example, suppose that the the opaque composite actor
     *  containing the FSM model has an input port named "dataIn."
     *  Then, in order to reference tokens read in the "dataIn"
     *  port in the state transition guard expression, the guard
     *  variable(s) associated with port "dataIn"  must be used.
     *  These guard variables have the names dataIn$0, dataIn$1,
     *  dataIn$2, .... Here, dataIn$0 references the token most
     *  recently read in port "dataIn", dataIn$1 references the
     *  next most recently read token, and so on.
     *
     * @param history The number of tokens in the guard history.
     */
    // FIXME: what if user tries to call this multiple times.
    public void  setGuardTokenHistory(int histSize) {
	_guardLength = histSize;
    }



    /** Return true if it transfers data from an input port of the
     *  container to the port(s) of the current refinement (an
     *  opaque composite actor). This method will transfer all
     *  available tokens on channel 0 of the input port.
     *  Put the transfered data in the FIFO token queue
     *  associated with the input port. This token queue has
     *  a length set by setGuardTokenHistory(). The token queue is
     *  used when evaluating state transition expressions.
     *  <p>
     *  The port argument must be an opaque input port.  If
     *  channel 0 of the input port has no data, then that channel
     *  is ignored.
     *  <p>
     *  This assumes that the name of the
     *  refining state's port must have the same name
     *  as the input port and is connected to the input port.
     *  Therefore, it is necessary that all input ports of a
     *  heterochronous dataflow actor that refines to an FSM be
     *  connected to the corresponding ports (with the same name)
     *  of all of the refining states in the FSM.
     *
     *  @param port The input port to transfer data from.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port or if the port is not connected to the current
     *   refining state of the FSM.
     *  @return True if data are transfered.
     */
    // ************ FIXME *************
    /* This is stupid. This assumes that the name of the
     * refining state's port must have the same name
     * as the input port (of this director's container) to
     * which it is connected.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an " +
                    "opaque input port.");
        }
        // do not handle multiple channels now
        boolean trans = false;
        Entity refine = (Entity)_controller.currentRefinement();
	if (refine == null) {
            throw new IllegalActionException(this,
                    "transferInputs: current refinement is null.");
	} else {
            //if (_debugging)
            //       _debug("HDFFSMDirector: transferInputs():Current " +
            //              refinement is not null, full name is: " +
            //              refine.getFullName());
	}

        IOPort p;
        Receiver rec;
	// Get token queue associated with "port".
	ArrayFIFOQueue guardTokenArray =
            (ArrayFIFOQueue)_portNameToArrayFIFOQueue.get(port.getFullName());

	// Reset the token to null.
	Token t = null;
        while (port.hasToken(0)) {
            try {


		// This  port.get(0) removes the token from the port, so
		// that subsequent port.hasToken(0) will return false.
		//Token t = port.get(0);
		t = port.get(0);




		// Remove the oldest token from the queue and throw it away
		// to make room for a new token.
		// Note that "guardTokenArray" is filled to capacity when
		// initialized, so need to remove token to make room
		// for new token.
		guardTokenArray.take();

		// Put the most recently read in token in the queue.
		guardTokenArray.put(t);


		//if (_debugging) _debug("HDFFSMDirector: transferInputs():" +
                //      " Port " + port.getFullName() + " has token.");
		//if (_debugging) _debug("HDFFSMDirector: transferInputs():" +
                //      " input port's token: " + t.toString());


		if (_controller == null) {
		    throw new IllegalActionException(this,
                            "_controller is null.");
		} else if (refine == null) {
		    throw new IllegalActionException(this,
                            "Current refinement is null.");
		} else {
		    //for (Enumeration e = refine.getPorts();
                    // e.hasMoreElements() ;) {
		    //System.out.println("HDFFSMDirector: transferInputs():" +
                    //     "Next port contained by current refinement: " +
                    // e.nextElement());
		    //}
		}

		// ************ FIXME ***************
		/* This is stupid. This assumes that the name of the
		 * refining state's port must have the same name
		 * as the input port (of this director's container) to
		 * which it is connected. It the names don't match,
		 * then things silently fail! :(
		 */
                p = (IOPort)refine.getPort(port.getName());
		// ********************************************************


                if (p != null) {
                    rec = (p.getReceivers())[0][0];

		    //if (_debugging)
                    //    _debug("HDFFSMDirector: transferInputs(): " +
                    //           "Put a token in the current refining state");
                    rec.put(t);
                } else {

		    throw new IllegalActionException(this, port,
                            "Director.transferInputs: Can't access input " +
                            "port the current refining state. Note that the " +
                            "name of a refining states port is constrained " +
                            "to be the same as the name of the input port " +
                            "(of the TypedComposite actor that represents " +
                            "an FSM) to which it is connected.");

		}
                trans = true;
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " +
                        ex.getMessage());
            }
        }
	// Copy the token(s) into the array of variables.
	// FIXME: As a performance optimization, should only do this
	// on the last firing of an iteration.
	// Get the array of variables associated with "port".
	Variable[] guardVarArray =
            (Variable[])_inputPortNameToVariableArray.get(port.getFullName());

	if (guardVarArray == null) {
	    throw new InternalErrorException("Guard variable array is null " +
                    "in transferInputs().");
	}

	// Copy the newest token into the Variable array.
	//Token tempToken2 = (Token)guardTokenArray.get(0);

	// Copy the token(s) into the array of variables.
	//(guardVarArray[0]).setToken(tempToken2);

	int i; // loop var.
	for (i = 0; i < guardVarArray.length; i++) {
	    (guardVarArray[i]).setToken(
                    (Token)guardTokenArray.get(guardVarArray.length -1 - i));
	}
        return trans;
    }



   /** Return true if it transfers data from an output port of the
     *  current refinement (an opaque composite actor) to the ports it
     *  is connected to on the outside. This method will transfer all
     *  available tokens on channel 0 of the output port.
     *  Put the transfered data in the FIFO token queue
     *  associated with the output port. This token queue has
     *  a length set by setGuardTokenHistory(). The token queue is
     *  used when evaluating state transition expressions.
     *  <p>
     *  The port argument must be an opaque output port.  If
     *  channel 0 of the output port has no data, then that channel
     *  is ignored.
     *  <p>
     *  This assumes that the name of the
     *  refining state's port must have the same name
     *  as the output port and that is connected to the output port.
     *  Therefore, it is necessary that all output ports of a
     *  heterochronous dataflow actor that refines to an FSM be
     *  connected to the corresponding ports (with the same name)
     *  of all of the refining states in the FSM.
     *
     *  @param port The output port to transfer data from.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port or if the port is not connected to the current
     *   refining state of the FSM.
     *  @return True if data are transfered.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {

	//if (_debugging)
        //    _debug("HDFFSMDirector: transferOutputs(): called on port: " +
        //         port.getName());
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not an opaque " +
                    "output port.");
        }
        boolean trans = false;
        // do not handle multiple channels now
        Receiver insideReceiver = (port.getInsideReceivers())[0][0];

        CompositeActor cont = (CompositeActor)getContainer();

	// ************ FIXME *********
	/* This is stupid. This assumes that the name of the
	 * refining state's port must have the same name
	 * as the output port (of this director's container) to
	 * which it is connected.
	 */
	// Get the output port of this director's container.
        IOPort p = (IOPort)cont.getPort(port.getName());
	// ********************************************************

	// Get token queue associated with "port".
	ArrayFIFOQueue guardTokenArray =
            (ArrayFIFOQueue)_portNameToArrayFIFOQueue.get(p.getFullName());

        while (insideReceiver.hasToken()) {
            try {
                Token t = insideReceiver.get();


		// Remove the oldest token from the queue and throw it away
		// to make room for a new token.
		// Note that "guardTokenArray" is filled to capacity when
		// initialized, so need to remove token to make room
		// for new token.
		guardTokenArray.take();

		// Put the most recently read in token in the queue.
		guardTokenArray.put(t);


                _controller.currentState().setLocalInputVar(port.getName(), t);
                if (p != null) {
                    Receiver rec = (p.getInsideReceivers())[0][0];
                    rec.put(t);
		    //if (_debugging)
                    //    _debug("HDFFSMDirector: transferOutputs(): " +
                    //         "Put a token in the compisite actor's " +
                    //         "(containing this director) output port");
                } else {
                    throw new IllegalActionException(this, port,
                            "Director.transferOutputs: Can't access an " +
                            "output port (of the container of the current " +
                            "refining state) connected to the current " +
                            "refining state's output port. Note that the " +
                            "name of a refining states port is constrained " +
                            "to be the same as the name of the output port " +
                            "(of the TypedComposite actor that represents " +
                            "an FSM) to which it is connected.");
		}
                trans = true;
            } catch (NoTokenException ex) {
                throw new InternalErrorException(
                        "Director.transferOutputs: " +
                        "Internal error: " +
                        ex.getMessage());
            }
        }
	// Copy the token(s) into the array of variables.
	// FIXME: As a performance optimization, should only do this
	// on the last firing of an iteration.
	// Get the array of variables associated with "port".
	Variable[] guardVarArray =
            (Variable[])_outputPortNameToVariableArray.get(p.getFullName());

	if (guardVarArray == null) {
	    // This should not happen.
	    throw new InternalErrorException("Guard variable array is null " +
                    "in transferOutputs().");

	}

	// Copy the newest token into the Variable array.
	//Token tempToken2 = (Token)guardTokenArray.get(0);

	// Copy the token(s) into the array of variables.
	//(guardVarArray[0]).setToken(tempToken2);

	int i; // loop var.
	for (i = 0; i < guardVarArray.length; i++) {
	    (guardVarArray[i]).setToken(
                    (Token)guardTokenArray.get(guardVarArray.length -1 - i));
	}
        return trans;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /* Get an enumeration of all of the variables that can be part
     * of a transistion's guard expression. This method should
     * only be called by an instance of HDFFSMTransistion.
     */
    protected Enumeration _getTransitionGuardVars() {
	return _allGuardVars.elements();
    }

    /** Indicate whether this director would like to have write access
     *  during its iteration. By default, the return value is true, indicating
     *  the need for a write access.
     *
     *  @return True if this director need write access, false otherwise.
     */
    protected boolean _writeAccessRequired() {
        // should ask the refinements
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**  Controller of this director. */
    protected HDFFSMController _controller = null;

    /* List of all of the variables that can be part
     * of a transistion's guard expression.
     */
    protected ArrayFIFOQueue _allGuardVars = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the guard variables assiciated with <i>port</i> and
     *  add them to a queue containing all the guard
     *  variables associated with all of the ports of contained by
     *  the HDF composite actor with this director.
     *  <p>
     *  The number of guard variables created for <i>port</i> is
     *  the number returned by getGuardTokenHistory().
     *  This variables in the queue are returned by
     *  _getTransitionGuardVars().
     *
     *  @param port The port to create guard variables for.
     */
    private void _createGuardVariables(IOPort port)
            throws IllegalActionException {

	int history = getGuardTokenHistory();
	Variable[] guardVarArray = new Variable[history];


	if (port.isInput()) {
	    if (_inputPortNameToVariableArray == null) {
		// Initialize the map from a port name to
		// its assiciated variable array of most
		// recently transfered tokens.
		_inputPortNameToVariableArray = new HashMap();
	    }
	    // Create a mapping from the current port's name to
	    // an array of variables. The tokens in the
	    // ArrayFIFOQueue associated with the port will
	    // be copied into the variables in the array on the
	    // last firing of an iteration (Type B firing in the
	    // reference paper).
	    if (!_inputPortNameToVariableArray.containsKey(
                    port.getFullName())) {
		_inputPortNameToVariableArray.put(
                        port.getFullName(), guardVarArray);
		try {
		    for(int i = 0; i < history; i++) {
			Integer iInt = new Integer(i);
			String guardName =
                            port.getName() + "$" + iInt.toString();
			//if (_debugging)
                        //   _debug("guard: with guard name:" + guardName);

			guardVarArray[i] = new Variable(this, guardName);
		    }
		    // Put this variable in a list of variables and make
		    // this list available to the transition. The
		    // transition will then add the variables in the
		    // list to its scope of variables allowed in the
		    // transition guard expression.

		    // create new variable lists
		    if (_allGuardVars == null) {
			_allGuardVars = new ArrayFIFOQueue();
		    }
		    _allGuardVars.putArray(guardVarArray);
		} catch (NameDuplicationException ex) {
		    System.err.println("HDFFSMDirector " +ex.getMessage());
		}
	    }
	} else if (port.isOutput()) {
	    if (_outputPortNameToVariableArray == null) {
		// Initialize the map from a port name to
		// its assiciated variable array of most
		// recently transfered tokens.
		_outputPortNameToVariableArray = new HashMap();
	    }
	    // Create a mapping from the current port's name to
	    // an array of variables. The tokens in the
	    // ArrayFIFOQueue associated with the port will
	    // be copied into the variables in the array on the
	    // last firing of an iteration (Type B firing in the
	    // reference paper).
	    if (!_outputPortNameToVariableArray.containsKey(
                    port.getFullName())) {
		_outputPortNameToVariableArray.put(
                        port.getFullName(), guardVarArray);
		try {
		    for(int i = 0; i < history; i++) {

			Integer iInt = new Integer(i);
			String guardName = port.getName() + "$" +
                            iInt.toString();

			//if (_debugging)
                        //   _debug("guard: with guard name:" + guardName);

			guardVarArray[i] = new Variable(this, guardName);
		    }
		    // Put this variable in a list of variables and make
		    // this list available to the transition. The
		    // transition will then add the variables in the
		    // list to its scope of variables allowed in the
		    // transition guard expression.

		    // create new variable lists
		    if (_allGuardVars == null) {
			_allGuardVars = new ArrayFIFOQueue();
		    }
		    _allGuardVars.putArray(guardVarArray);
		} catch (NameDuplicationException ex) {
		    System.err.println("HDFFSMDirector " +ex.getMessage());
		}
	    }
	} else {
	    throw new IllegalActionException(this,
                    "port parameter is not an inport or an " +
                    "output port");
	}


    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _guardLength = 1;

    // Map a port name to its associated ArrayFIFOQueue of most
    // recently transfered tokens.
    private HashMap _portNameToArrayFIFOQueue;

    // Map a port name to its associated array of Variables. The
    // array of Variables stores the _guardLength most recently
    // transfered tokens by the input port. The variables in the
    // array are updated by copying the tokens contained in the
    // ArrayFIFOQueue of the same port (obtained from
    // _portNameToArrayFIFOQueue). Note that the array
    // of variables is only updated on the last firing of an
    // iteration.
    private HashMap _inputPortNameToVariableArray;



    // Map a port name to its associated array of Variables. The
    // array of Variables stores the _guardLength most recently
    // transfered tokens by the output port. The variables in the
    // array are updated by copying the tokens contained in the
    // ArrayFIFOQueue of the same port (obtained from
    // outputPortNameToArrayFIFOQueue). Note that the array
    // of variables is only updated on the last firing of an
    // iteration.
    private HashMap _outputPortNameToVariableArray;
}
