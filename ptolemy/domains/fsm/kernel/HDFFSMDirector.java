/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

 Copyright (c) 1999 The Regents of the University of California.
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
import collections.LinkedList;
import java.util.Enumeration;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector
/**
A HDFFSMDirector governs the execution of the finite state
machine in heterochronous dataflow model.
Note:
There is currently the following constraint on port names: All ports
that are linked to an input port of this director's container must
have have the same name (the name of the input port). The same goes
for output ports.
@author Brian K. Vogel
@version: $Id$
*/

// ************ FIXME ****************
/* There is currently the following constraint on port names: All ports
 * that are linked to an input port of this director's container must
 * have have the same name (the name of the input port).
 */
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
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
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

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this Director.  In general, this may be called more
     *  than once in the same iteration of the Directors container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().
     *  Notice that we ignore the return value of postfire() in this base
     *  class.   In general, derived classes will want to do something
     *  intelligent with the returned value.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of the
     *   container or one of the deeply contained actors throws it.
     */
    // FIXME!!
    // The controller may delegate firing its current refinement to this
    // director.
    // Note that the fire sequence of FSMController is: 1. evaluate preemptive
    // transitions; 2. invoke refinement; 3. evaluate non-preemptive
    // transitions.
    public void fire() throws IllegalActionException {
        // _controller must not be null
	System.out.println("HDFFSMDirector: fire()");
        _controller.fire();
    }

    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        if (execDir == null) {
            // PANIC!!
            throw new InvalidStateException(this,
                    "HDFFSMDirector must have an executive director!");
        }
        execDir.fireAt(cont, time);
    }

    /** Return the current time of the simulation. In this base class,
     *  it returns 0. The derived class should override this method
     *  and return the current time.
     */
    // FIXME: complete this.
    // Should ask its executive director for current time.
    public double getCurrentTime() {
        double ctime = 0.0;
        CompositeActor cont = (CompositeActor)getContainer();
        if (cont == null) {
            // In fact this should not happen, this director must have
            // a container.
            ctime = 0.0;
        } else {
            Director execDir = (Director)cont.getExecutiveDirector();
            if (execDir == null) {
                // PANIC!!
                throw new InvalidStateException(this,
                        "HDFFSMDirector must have an executive director!");
            }
            ctime = execDir.getCurrentTime();
        }
        return ctime;
    }

    /** Get the next iteration time.
     */
    // FIXME: complete this.
    // Note we should make clear which entities call this method: the
    // executive director, or the directors of the composite actors
    // governed by this director.
    // Should only be called by executive director.
    public double getNextIterationTime() {
        double nextTime = 0.0;
        CompositeActor cont = (CompositeActor)getContainer();
        if (cont == null) {
            // In fact this should not happen, this director must have
            // a container.
            nextTime = 0.0;
        } else {
            Director execDir = (Director)cont.getExecutiveDirector();
            if (execDir == null) {
                // PANIC!!
                throw new InvalidStateException(this,
                        "HDFFSMDirector must have an executive director!");
            }
            nextTime = execDir.getNextIterationTime();
        }
        return nextTime;
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
    // This now initializes the controller and all refinements.
    // Possible to initialize only the controller.
    // FIXME!!
    public void initialize() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            Enumeration allActors = container.deepGetEntities();
            while (allActors.hasMoreElements()) {
                Actor actor = (Actor)allActors.nextElement();
                if (actor == _controller) {
                    continue;
                } else {
                    actor.initialize();
                }
            }
            _controller.initialize();
        }



        /* REMOVE! */
        System.out.println("Initializing HDFFSMDirector " + this.getFullName());

    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    // Use QueueReceiver?
    // NOTE! There is a problem of token accumulating in the receivers.
    // FIXME!!
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return false. The default director will only get fired once, and will
     *  terminate execution afterwards.   Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director wishes to execute anymore, and
     *  should *NOT*, in general, just take the logical AND of calling
     *  postfire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it.
     */
    // NOTE! There is the problem of how to deal with refinements' return
    // value.
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

    /** return True, indicating that the Director is ready to fire.
     *  Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director is ready to execute, and
     *  should *NOT*, in general, just take the logical AND of calling
     *  prefire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // elaborate
        Actor refine = _controller.currentRefinement();

        // REMOVE
        //System.out.println("HDFFSMDirector: get controller's current refinement.");

        boolean result = true;
        if (refine != null) {
            result = refine.prefire();

            /* REMOVE! */
            //System.out.println("Result of prefire " + ((ComponentEntity)refine).getFullName()
            //+ " is " + result);
        }

        // result = result & _controller.prefire();
        return result;
    }


    /** Initialize the guard variables, which are used in the
     *  state transition expressions. Then Create receivers and then
     *  invoke the preinitialize()
     *  methods of all its deeply contained actors.
     *  Set the current time to be 0.0.
     *  This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
	// Initialize guard variables.
	/*
	if (guardVar == null) {
	    try {
		if (_debugging) _debug("Setting guard variables.");
		guardVar = new Variable(this, "TheGuard");
	    } catch (NameDuplicationException ex) {
		System.out.println("HDFFSMDirector " +ex.getMessage());
	    }
	}
	*/
	//guardLength
	// Code from base Direcotr class:
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
	    // Initialize guard variables.

	    if (inputPortNameToArrayFIFOQueue == null) {
		// Initialize the map from a port name to
		// its assiciated ArrayFIFOQueue of most
		// recently transfered tokens.
		inputPortNameToArrayFIFOQueue = new HashMap();
	    }

	    if (inputPortNameToVariableArray == null) {
		// Initialize the map from a port name to
		// its assiciated variable array of most
		// recently transfered tokens.
		inputPortNameToVariableArray = new HashMap();
	    }

	    Enumeration inPorts = container.inputPorts();
	    while (inPorts.hasMoreElements()) {
		TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		if (_debugging) _debug("guard: port name:" + inport.getName());
		// Array to store queue of tokens to be used in evaluating
		// the state transition guard expression.

		ArrayFIFOQueue guardTokenArray = new ArrayFIFOQueue(guardLength);

		
		// Fill up guardTokenArray. The queue should always
		// be full so that its size does not need to be checked
		// on each call to fire().
		while (!guardTokenArray.isFull()) {
		    Token tempToken = new Token();
		    guardTokenArray.put(tempToken);
		    if (_debugging) _debug("guard: puting temparary token in guardTokenArray");
		}

		// Create a mapping from the current port's name to
		// a queue to be used to store the guardLength most
		// recently read in tokens.
		if (!inputPortNameToArrayFIFOQueue.containsKey(inport.getName())) {
		    inputPortNameToArrayFIFOQueue.put(inport.getName(), guardTokenArray);
		}
		
		

		Variable[] guardVarArray = new Variable[guardLength];
				
		try {
		    for(int i = 0; i < guardLength; i++) {

			Integer iInt = new Integer(i);
			String guardName = inport.getName() + "$" + iInt.toString();

			if (_debugging) _debug("guard: with guard name:" + guardName);

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
		    System.out.println("HDFFSMDirector " +ex.getMessage());
		}

		// Create a mapping from the current port's name to
		// an array of variables. The tokens in the 
		// ArrayFIFOQueue associated with the port will
		// be copied into the variables in the array on the
		// last firing of an iteration (Type B firing in the
		// reference paper).
		if (!inputPortNameToVariableArray.containsKey(inport.getName())) {
		    inputPortNameToVariableArray.put(inport.getName(), guardVarArray);
		}


	    }

	    // End of Initialize guard variables.
	    CompositeActor containersContainer =
                (CompositeActor)container.getContainer();
	    if( containersContainer == null ) {
                _currentTime = 0.0;
	    } else {
		double time =
                    containersContainer.getDirector().getCurrentTime();
                _currentTime = time;
	    }
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                if (_debugging) _debug("Invoking preinitialize(): ",
                ((NamedObj)actor).getFullName());
                actor.preinitialize();
            }
        }
	// End of code from base Direcotr class.



        if (_debugging) _debug("Finished preinitialize().");
    }


    /** Set the controller associated with this director. This method
     *  must be called in the model code.
     */
    public void setController(HDFFSMController ctrl)
            throws IllegalActionException {
	System.out.println("HDFFSMDirector: setController()");
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
     */
    public void  setGuardTokenHistory(int histSize) {
	guardLength = histSize;
    }
    

    /** Return true if it
     *  transfers data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @return True if data are transfered.
     */
    // ******************************************************
    // ************ FIXME ************* THIS IS REALLY STUPID! ****
    /* This is stupid. This assumes that the name of the
     * refining state's port must have the same name 
     * as the input port (of this director's container) to
     * which it is connected. It the names don't match,
     * then things silently fail! :(
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
	// Reset the gard token to null.
	t = null;

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        // do not handle multiple tokens, multiple channels now
        boolean trans = false;
        Entity refine = (Entity)_controller.currentRefinement();
	if (refine == null) {
		    System.out.println("HDFFSMDirector: transferInputs():Current refinement is null!");
	} else {
	     System.out.println("HDFFSMDirector: transferInputs():Current refinement is not null, full name is: " + refine.getFullName());
	}

        IOPort p;
        Receiver rec;
        if (port.hasToken(0)) {
            try {
		
		
		// This  port.get(0) removes the token from the port, so
		// that subsequent port.hasToken(0) will return false.
		//Token t = port.get(0);
		t = port.get(0);
		
		// FIXME: currently only allow 1 input, 1 token for guard.
		//guardVar.setToken(t);

		

		// Get token queue associated with "port".
		ArrayFIFOQueue guardTokenArray = (ArrayFIFOQueue)inputPortNameToArrayFIFOQueue.get(port.getName());
		
		// Remove the oldest token from the queu and throw it away
		// to make room for a new token. 
		// FIXME: Make this work for non-homogeneous case.
		guardTokenArray.take();

		// Put the most recently read in token in the queue.
		guardTokenArray.put(t);

		// Copy the newest token into the Variable array.
		// FIXME: For the non-homogeneous case, only do this
		// on the last firing of an iteration.
		Token tempToken2 = (Token)guardTokenArray.get(0);

		// Get the array of variables associated with "port".
		Variable[] guardVarArray = (Variable[])inputPortNameToVariableArray.get(port.getName());

		// Copy the token(s) into the array of variables.
		(guardVarArray[0]).setToken(tempToken2);
		

		System.out.println("HDFFSMDirector: transferInputs(): Port " + port.getFullName() + " has token.");
		System.out.println("HDFFSMDirector: transferInputs(): input port's token: " + t.toString());
	     
		
		
		// end DEBUG
                //p = (IOPort)_controller.getPort(port.getName());

                //System.out.println("HDFFSMDirector: transferInputs(): Try get a port from " + ((ComponentEntity)_controller).getFullName());

                //if (p != null) {
		//  rec = (p.getReceivers())[0][0];
		//  if (rec.hasToken()) {
		//      rec.get();
		//  }
		//  rec.put(t);
		// }
		// Debug stuff:
		System.out.println("HDFFSMDirector: transferInputs(): caled on port: " + port.getName());
		if (_controller == null) {
		    System.out.println("HDFFSMDirector: transferInputs():_controller is null!!!");
		} else if (refine == null) {
		    System.out.println("HDFFSMDirector: transferInputs():Current refinement is null!");
		} else {
		    for (Enumeration e = refine.getPorts() ; e.hasMoreElements() ;) {
			System.out.println("HDFFSMDirector: transferInputs(): Next port contained by current refinement: " + e.nextElement());
		    }
		}
		// End of debug stuff.
		

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
                    if (rec.hasToken()) {
                        rec.get();
                    }
		    System.out.println("HDFFSMDirector: transferInputs(): Put a token in the current refining state");
                    rec.put(t);
                } else {
		    System.out.println("HDFFSMDirector: transferInputs(): Oh darn. FAILED to put a token in the current refining state");
		    throw new IllegalActionException(this, port, "Director.transferInputs: Can't access input port the current refining state. Note that the name of a refining states port is constrained to be the same as the name of the input port (of the TypedComposite actor that represents an FSM) to which it is connected.");
		}
                trans = true;
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " +
                        ex.getMessage());
            }
        }
        return trans;
    }

    /** Return true if it
     *  transfers data from an output port of the container to the
     *  ports it is connected to on the outside.  The port argument must
     *  be an opaque output port.  If any channel of the output port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @return True if data are transfered.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {

	System.out.println("HDFFSMDirector: transferOutputs(): caled on port: " + port.getName());
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not an opaque output port.");
        }
        boolean trans = false;
        // do not handle multiple tokens, multiple channels now
        Receiver insideReceiver = (port.getInsideReceivers())[0][0];

        CompositeActor cont = (CompositeActor)getContainer();

	// ******************************************************
	// ************ FIXME ************* THIS IS REALLY STUPID! ****
	/* This is stupid. This assumes that the name of the
	 * refining state's port must have the same name 
	 * as the output port (of this director's container) to
	 * which it is connected. It the names don't match,
	 * then things silently fail! :(
	 */
        IOPort p = (IOPort)cont.getPort(port.getName());
	// ********************************************************

        if (insideReceiver.hasToken()) {
            try {
                Token t = insideReceiver.get();

                //System.out.println("Transfer output from " +
                //port.getFullName() + " " +
                //((DoubleToken)t).doubleValue());

                _controller.currentState().setLocalInputVar(port.getName(), t);
                if (p != null) {
                    Receiver rec = (p.getInsideReceivers())[0][0];
                    rec.put(t);
		    System.out.println("HDFFSMDirector: transferOutputs(): Put a token in the compisite actor's (containing this director) output port");
                } else {
		    System.out.println("HDFFSMDirector: transferInputs(): Oh darn. FAILED to put a token in the current refining state");
		    throw new IllegalActionException(this, port, "Director.transferOutputs: Can't access an output port (of the container of the current refining state) connected to the currect refining state's output port. Note that the name of a refining states port is constrained to be the same as the name of the output port (of the TypedComposite actor that represents an FSM) to which it is connected.");
		}
                trans = true;
            } catch (NoTokenException ex) {
                throw new InternalErrorException(
                        "Director.transferOutputs: " +
                        "Internal error: " +
                        ex.getMessage());
            }
        }
        return trans;
    }

    ///////////////////////////////////////////////////////////////
    //////////        protected methods           /////////////////

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
    ////                         public variables                  ////

    // This is the token used to evaluate the guard. It is curently
    // constained to consist of the most recent token read from the
    // input port of the CompositeActor containing this director.
    public Token t;
    
    public Variable guardVar;

    public int guardLength = 1;
    

    // Map a port name to its associated ArrayFIFOQueue of most
    // recently transfered tokens.
    public HashMap inputPortNameToArrayFIFOQueue;

    // Map a port name to its associated array of Variables. The
    // array of Variables stores the guardLength most recently
    // transfered tokens by the input port. The variables in the
    // array are updated by copying the tokens contained in the
    // ArrayFIFOQueue of the same port (obtained from 
    // inputPortNameToArrayFIFOQueue). Note that the array
    // of variables is only updated on the last firing of an
    // iteration.
    public HashMap inputPortNameToVariableArray;


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    /** @serial Controller of this director. */
    protected HDFFSMController _controller = null;

    /* List of all of the variables that can be part
     * of a transistion's guard expression.
     */
    protected ArrayFIFOQueue _allGuardVars = null;

}
