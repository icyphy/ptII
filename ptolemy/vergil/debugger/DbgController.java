/* A class that listens to execution events and allows or not a Director
   to continue executiong its actors.

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELEC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import java.util.*;

import javax.swing.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

////////////////////////////////////////////////////////////////////////
//// DbgController
/**
   DbgController is the class that analyses and modifies the ExecState
   of any Director that calls it test method. Its method waitUserCommand
   allows him to wait for an entry by the user, in collaboration with 
   DebuggerUI.

@author SUPELEC team B. Desoutter, P. Domecq & G. Vibert and Steve Neuendorffer
@version $Id$
*/
public class DbgController implements DebuggingListener {

    ////////////////////////////////////////////////////////
    //                Public variables                    //

    //Boolean that becomes false  when the Button END is pushed
    // (tested in XXXDbgDirector). 
    public boolean notFinished;
    
    //Boolean that becomes true when the Button Step In is pushed 
    //(tested in XXXDbgDirector).
    public boolean stepInPause;
    
    //List that contain the Watchers.
    public NamedList watcherList = new NamedList();

   /** 
     * Construct a new debug controller with a reference to the given
     * debugger.
     * @param pdb A reference to the debugger.
     */
    public DbgController(Pdb pdb) {
	_pdb = pdb;
	notFinished = true;
	stepInPause = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * Notify the DebuggingListener that actor is going to be prefired.
     * @param actor The actor that is going to be prefired.
     */
    public void prefireEvent(Actor actor) {
	if (notFinished) {
	    //Refresh the Value of the ExecState
	    _state.setnextMethod("prefire");
	    _pdb.getDebuggerUI().displayResult("Before prefiring " + 
		 ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint) 
		((NamedObj) actor).getAttribute("prefire");
	    if(breakpoint != null && breakpoint.evaluateCondition()) {
		_pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		waitUserCommand(_state);		
	    }

	    //Analyse the ExecState
	    test(_state);
	}
    }

    /** Notify the DebuggingListenr that actor is going to be fire
     * @param actor : the actor that is going to be fire
     */
    public void fireEvent(Actor actor) {
	if (notFinished) {
	    //Refresh the Value of ExecState
	    _state.setnextMethod("fire");
	    _pdb.getDebuggerUI().displayResult("Before firing " + 
		((Nameable) actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint) 
		((NamedObj) actor).getAttribute("fire");
	    if(breakpoint != null && breakpoint.evaluateCondition()) {
		_pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		waitUserCommand(_state);
	    }
	    
	    // Will hide the stepin button in the interface if the 
	    // actor is not an opaque composite atomic.
	    ComponentEntity entity = (ComponentEntity) actor;
	    if (!entity.isAtomic() && entity.isOpaque()) {
		_pdb.getDebuggerUI().enableButton(6);
	    } else {
		_pdb.getDebuggerUI().disableButton(6);
	    }

	    test(_state);

	    // stepInPause can be true only if isAtomic is 
	    // true because the button Step In
	    // cannot be used if actor is atomic 
	    if (stepInPause) {
		stepInPause = false;
		_state.setdbgCommand("pause");
	    }
	}			
    }

    /** 
     * Notify the DebuggingListener that actor is going to be postfired.
     * @param actor The actor that is going to be postfired.
     */
    public void postfireEvent(Actor actor) {
	if (notFinished) {
	    //Refresh the ExecState
	    _state.setnextMethod("postfire");
	    _pdb.getDebuggerUI().displayResult("Before postfiring " + 
		((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		waitUserCommand(_state);		    	       
	    }
	    test(_state);
	}
    }

    /** Notify the DebuggingListenr that actor is going to be postpostfire
     * @param actor The actor that is going to be postpostfired
     */
    public void postpostfireEvent(Actor actor) {
        TypedCompositeActor container = 
	    (TypedCompositeActor) ((NamedObj)actor).getContainer();

	if (notFinished) {
	    // Refresh ExecState
	    _state.setnextMethod("postpostfire");
	    _pdb.getDebuggerUI().displayResult("After postfiring " + 
		       ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postpostfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		waitUserCommand(_state);
	    }

	    test(_state);

	    // After a composite is postpostfired, then pause execution.
	    if (actor instanceof CompositeActor &&
		_state.getdbgCommand() == "stepout") {
		_state.setdbgCommand("pause");
	    }
	}
    }

    /** 
     * Main method that analyse and modify the execution state.
     * Depending on the method to be called and the command entered,
     * it chooses to pause the execution or not. If the command entered
     * is 'pause', then test will call waitUserCommand, receive a new
     * command from the user, and analyse it. There cannot be any
     * infinite loop, because the command will not be 'pause' after the
     * user choose one.  
     * @param state The execution state of the calling director.
     */
    public void test(ExecState state) {

	 method = state.getnextMethod();
	 command = state.getdbgCommand();
	
	if (method != "fire") {
	    /* signal to MMI that stepin forbidden */
	}

	if (command == "pause") {
	    waitUserCommand(state);
	    test(state);
	} else if (command == "step" && method == "postfire") {
	    state.setdbgCommand("pause");
	} else if (command == "microstep") {
	    state.setdbgCommand("pause");
	} else if (command == "stepin") {
	    if(method == "fire") {
		stepInPause = true;
		state.setdbgCommand("pause");
	    } else {
		_pdb.getDebuggerUI().displayResult("Error : Can't step in here");    
	    }
	}
    }

    /** 
     * Synchronize the DbgController with the DebuggerUI,
     * and wait for an entry by the user. See the actionListener of
     * the DebuggerUI for a more effective comprehension. 
     * @param state The execution state of the calling director.
     */
    public synchronized void waitUserCommand(ExecState directorState) {
	Iterator watchers = watcherList.elementList().iterator();
	while (watchers.hasNext()) {
	    ActorWatcher watcher = (ActorWatcher)watchers.next();
	    watcher.refresh();
	}

	_commandNotEntered = true;
	_pdb.getDebuggerUI().displayResult("Please, enter a command.");       
	while (_commandNotEntered) {
	    _pdb.getDebuggerUI().putCmd = true;
	    try {
		wait();
	    } catch (InterruptedException ex) {
	    }
	}
	_pdb.getDebuggerUI().putCmd = false;
	directorState.setdbgCommand(_pdb.getDebuggerUI().getuserCommand());
    }

    /**
     * Called by the UI when a command is entered, to allow the execution
     * thread to continue. 
     */
    public synchronized void commandEntered() {
	_commandNotEntered = false;
	notifyAll();
    }

    ////////////////////////////////////////////////////////
    //                Private variables                   //
    private Pdb _pdb;
    private String method;
    private String command;

    //Boolean that allows to synchronize DbgController and DebuggerUI.
    private boolean _commandNotEntered = true;

    // The current Execution state.
    private ExecState _state = new ExecState();
}

    
