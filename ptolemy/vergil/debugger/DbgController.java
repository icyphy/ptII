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
import ptolemy.vergil.debugger.MMI.*;

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
public class DbgController implements DebugListener {

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
    public DbgController() {
	notFinished = true;
	stepInPause = false;
    	_mmi = new DebuggerUI(this);
    	_mmi.setTitle("Pdb");
    	_mmi.setSize(450, 260);
    	_mmi.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a reference to the UI
     * @see ptolemy.vergil.debugger.Pdb#getDebuggerUI()
     * @return a reference to the UI
     */
    public DebuggerUI getDebuggerUI() {
	return _mmi;
    }

    /** Ignore string messages.
     */
    public void message(String string) {
    }

    /**
     * Ignore debug events that aren't firing events
     */   
    public void event(DebugEvent debugEvent) {
	System.out.println("event = " + debugEvent);
	if(debugEvent instanceof FiringEvent) {
	    FiringEvent event = (FiringEvent) debugEvent;
	    if(event.getType() == FiringEvent.PREFIRE) {
		prefireEvent(event.getActor());
	    } else if(event.getType() == FiringEvent.FIRE) {
		fireEvent(event.getActor());
	    } else if(event.getType() == FiringEvent.POSTFIRE) {
		postfireEvent(event.getActor());
	    } else if(event.getType() == FiringEvent.POSTPOSTFIRE) {
		postpostfireEvent(event.getActor());
	    }
	}
    }

    /** 
     * Notify the DebuggingListener that actor is going to be prefired.
     * @param actor The actor that is going to be prefired.
     */
    public void prefireEvent(Actor actor) {
	if (notFinished) {
	    //Refresh the Value of the ExecState
	    _method = "prefire";
	    _mmi.displayResult("Before prefiring " + 
		 ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint) 
		((NamedObj) actor).getAttribute("prefire");
	    if(breakpoint != null && breakpoint.evaluateCondition()) {
		_mmi.displayResult("Breakpoint encountered!");
		waitUserCommand();		
	    }

	    //Analyse the ExecState
	    test();
	}
    }

    /** Notify the DebuggingListenr that actor is going to be fire
     * @param actor : the actor that is going to be fire
     */
    public void fireEvent(Actor actor) {
	if (notFinished) {
	    //Refresh the Value of ExecState
	    _method = "fire";
	    _mmi.displayResult("Before firing " + 
		((Nameable) actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint) 
		((NamedObj) actor).getAttribute("fire");
	    if(breakpoint != null && breakpoint.evaluateCondition()) {
		_mmi.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }
	    
	    // Will hide the stepin button in the interface if the 
	    // actor is not an opaque composite atomic.
	    ComponentEntity entity = (ComponentEntity) actor;
	    if (!entity.isAtomic() && entity.isOpaque()) {
		_mmi.enableButton(6);
	    } else {
		_mmi.disableButton(6);
	    }

	    test();

	    // stepInPause can be true only if isAtomic is 
	    // true because the button Step In
	    // cannot be used if actor is atomic 
	    if (stepInPause) {
		stepInPause = false;
		_command = "pause";
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
	    _method = "postfire";
	    _mmi.displayResult("Before postfiring " + 
		((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_mmi.displayResult("Breakpoint encountered!");
		waitUserCommand();		    	       
	    }
	    test();
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
	    _method = "postpostfire";
	    _mmi.displayResult("After postfiring " + 
		       ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postpostfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_mmi.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }

	    test();

	    // After a composite is postpostfired, then pause execution.
	    if (actor instanceof CompositeActor &&
		_command == "stepout") {
		_command = "pause";
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
    public void test() {

	if (_method != "fire") {
	    /* signal to MMI that stepin forbidden */
	}

	if (_command == "pause") {
	    waitUserCommand();
	    test();
	} else if (_command == "step" && _method == "postfire") {
	    _command = "pause";
	} else if (_command == "microstep") {
	    _command = "pause";
	} else if (_command == "stepin") {
	    if(_method == "fire") {
		stepInPause = true;
		_command = "pause";
	    } else {
		_mmi.displayResult("Error : Can't step in here");    
	    }
	}
    }

    /** 
     * Synchronize the DbgController with the DebuggerUI,
     * and wait for an entry by the user. See the actionListener of
     * the DebuggerUI for a more effective comprehension. 
     * @param state The execution state of the calling director.
     */
    public synchronized void waitUserCommand() {
	Iterator watchers = watcherList.elementList().iterator();
	while (watchers.hasNext()) {
	    ActorWatcher watcher = (ActorWatcher)watchers.next();
	    watcher.refresh();
	}

	_commandNotEntered = true;
	_mmi.displayResult("Please, enter a command.");       
	while (_commandNotEntered) {
	    _mmi.putCmd = true;
	    try {
		wait();
	    } catch (InterruptedException ex) {
	    }
	}
	_mmi.putCmd = false;
	_command = _mmi.getuserCommand();
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
    private String _method = "";
    private String _command = "go";

    //Boolean that allows to synchronize DbgController and DebuggerUI.
    private boolean _commandNotEntered = true;

    // a link to DebuggerUI 
    private DebuggerUI _mmi;
}

    
