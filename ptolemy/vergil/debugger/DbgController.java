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
   DebuggerUI
@author SUPELEC team B. Desoutter, P. Domecq & G. Vibert
@version $Id$
@see DbgController
@see ptolemy.vergil.debugger.DbgController
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
    
    //Boolean that allows to synchronize DbgController and DebuggerUI.
    private boolean _commandNotEntered = true;

    //List that contain the Watchers.
    public NamedList actorWatcher = new NamedList();


    /** Constructor
     * @see ptolemy.vergil.debugger.DbgController#DbgController()
     * @param pdb : a reference on the debugger
     */
    public DbgController(Pdb pdb) {
	_pdb = pdb;
	notFinished = true;
	stepInPause = false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the DebuggingListenr that actor is going to be prefire
     * @see ptolemy.vergil.debugger.DbgController#prefireEvent(Actor actor)
     * @param actor : the actor that is going to be prefire.
     */
    public void prefireEvent(Actor actor) {
	ExecState state = ((DbgDirector) actor.getExecutiveDirector()).getState();

	if (notFinished) {
	    //Refresh the Value of the ExecState
	    state.setnextMethod("prefire");
	    _pdb.getDebuggerUI().displayResult("Before prefiring " + ((Nameable)actor).getFullName());
	    if (((NamedObj) actor).getAttribute("prefire") != null) {
		if (((Breakpoint) ((NamedObj) actor).getAttribute("prefire")).evaluateCondition()) {
		    _pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		    waitUserCommand(state);
		}
	    }
	    //Analyse the ExecState
	    test(state);
	}
    }

    /** Notify the DebuggingListenr that actor is going to be fire
     * @see ptolemy.vergil.debugger.DbgController#fireEvent(Actor actor)
     * @param actor : the actor that is going to be fire
     */
    public void fireEvent(Actor actor) {
	ExecState state = ((DbgDirector) actor.getExecutiveDirector()).getState();

	if (notFinished) {
	    //Refresh the Value of ExecState
	    state.setnextMethod("fire");
	    _pdb.getDebuggerUI().displayResult("Before firing " + ((Nameable)actor).getFullName());		    
	    if (((NamedObj) actor).getAttribute("fire") != null) {
		if (((Breakpoint) ((NamedObj) actor).getAttribute("fire")).evaluateCondition()) {
		    _pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		    waitUserCommand(state);
		}
	    }
	    
	    if ( !((ComponentEntity)actor).isAtomic() && ((ComponentEntity)actor).isOpaque()) {
		/* will hide stepin button in the MMI if actor is atomic
		 * or a transparent composite actor has no local director
		 */
		_pdb.getDebuggerUI().enableButton(6);
	    }
	    else {_pdb.getDebuggerUI().disableButton(6);}
	    test(state);

	    /* stepInPause can be true only if isAtomic is true because the button Step In
	     * cannot be used if actor is atomic 
	     */
	    if (stepInPause) {
		stepInPause = false;
		/* get the local director of the CompositeActor actor and put its state at Pause*/
		(((DbgDirector)(actor.getDirector())).getState()).setdbgCommand("pause");
	    }
	}			
    }

    /** Notify the DebuggingListenr that actor is going to be postfire
     * @see ptolemy.vergil.debugger.DbgController#postfireEvent(Actor actor)
     * @param actor : the actor that is going to be postfire
     */
    public void postfireEvent(Actor actor) {
	ExecState state = ((DbgDirector) actor.getExecutiveDirector()).getState();

	if (notFinished) {
	    //Refresh the ExecState
	    state.setnextMethod("postfire");
	    _pdb.getDebuggerUI().displayResult("Before postfiring " + ((Nameable)actor).getFullName());
	    if (((NamedObj) actor).getAttribute("postfire") != null) {
		if (((Breakpoint) ((NamedObj) actor).getAttribute("postfire")).evaluateCondition()) {
		    _pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		    waitUserCommand(state);
		    
		}
	    }
	    test(state);
	}
    }

    /** Notify the DebuggingListenr that actor is going to be postpostfire
     * @see ptolemy.vergil.debugger.DbgController#postpostfireEvent(Actor actor)
     * @param actor : the actor that is going to be postpostfire
     */
    public void postpostfireEvent(Actor actor) {
	ExecState state = ((DbgDirector) actor.getExecutiveDirector()).getState();
        TypedCompositeActor container = (TypedCompositeActor) actor.getExecutiveDirector().getContainer();

	if (notFinished) {
	    //Refresh ExecState
	    state.setnextMethod("postpostfire");
	    _pdb.getDebuggerUI().displayResult("After postfiring " + ((Nameable)actor).getFullName());
	    if (((NamedObj) actor).getAttribute("postpostfire") != null) {
		if (((Breakpoint) ((NamedObj) actor).getAttribute("postpostfire")).evaluateCondition()) {
		    _pdb.getDebuggerUI().displayResult("Breakpoint encountered!");
		    waitUserCommand(state);
		}
	    }
	    test(state);
	    
	    //If the Command is Resume, than the Director above must be set at resume.
	    //more over, but this is juste temporary, if the command is Step Out, the Director Above must be set at pause.
	    if (state.getdbgCommand() == "resume" || state.getdbgCommand() == "stepout") {
		DbgDirector dbgUp;
		dbgUp = (DbgDirector)(container.getExecutiveDirector());
		if (dbgUp != null) {
		    if (state.getdbgCommand() == "resume") 
			(dbgUp.getState()).setdbgCommand("resume");
		    if (state.getdbgCommand() == "stepout") 
			(dbgUp.getState()).setdbgCommand("pause");
		}
	    }
	}
    }


    /** Main method that analyse and modify the ExecState it receive :
     *  depending on the method to be called and the command entered,
     *  it choose to pause the execution or not. If the command entered
     *  is 'pause', then test will call waitUserCommand, receive a new
     *  command from the user, and analyse it. There cannot be any
     *  infinite loop, because the command will not be 'pause' after the
     *  user choose one.  
     * @see ptolemy.vergil.debugger.DbgController#test(ExecState state)
     * @param state : the execution state of the calling director
     */
    public void test(ExecState state) {

	 method = state.getnextMethod();
	 command = state.getdbgCommand();
	
	if (method != "fire") {
	    /* signal to MMI that stepin forbidden */
	}

	if (command == "pause") {
	    //Debug code
	    System.out.println("\t Before waitusercommand de test\n"
			       + "\t" + method + "\t" + command);
	    waitUserCommand(state);
	    test(state);
	} 
	else if (command == "step") {
	    if (method == "postfire") {
		state.setdbgCommand("pause");
	    } 
	} 
	else if (command == "microstep") {
	    state.setdbgCommand("pause");
	}
	else if (command =="stepin") {
	    if (method != "fire") {
		/* send error message : stepin forbidden */
		_pdb.getDebuggerUI().displayResult("Error : Can't step in here");    
	    }
	    else {
		stepInPause = true;
		state.setdbgCommand("pause");
	    }
	}
	
    }

    /** 
     * Synchronize the DbgController with the DebuggerUI,
     * and wait for an entry by the user. See the actionListener of
     * the DebuggerUI for a more effective comprehension. 
     * @see ptolemy.vergil.debugger.DbgController#waitUserCommand()
     * @param state : the execution state of the calling director
     */
    public synchronized void waitUserCommand(ExecState directorState) {
	Enumeration e = actorWatcher.elements();
	while (e.hasMoreElements()) {
	    ((ActorWatcher)e.nextElement()).refresh();
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
	System.out.println("\t waitusercommand \n"
			    + "\t" + _pdb.getDebuggerUI().getuserCommand());
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
}

    
