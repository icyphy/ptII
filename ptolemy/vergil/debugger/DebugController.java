/* An execution listener that suspends execution based on breakpoints.

 Copyright (c) 1999-2002 SUPELEC and The Regents of the University of
 California.  All rights reserved.  Permission is hereby granted,
 without written agreement and without license or royalty fees, to
 use, copy, modify, and distribute this software and its documentation
 for any purpose, provided that the above copyright notice and the
 following two paragraphs appear in all copies of this software.

 IN NO EVENT SHALL SUPELEC OR THE UNIVERSITY OF CALIFORNIA BE LIABLE
 TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 DOCUMENTATION, EVEN IF SUPELEC OR THE UNIVERSITY OF CALIFORNIA HAVE
 BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND SUPELEC SPECIFICALLY DISCLAIM ANY
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA AND SUPELEC HAVE NO OBLIGATION TO PROVIDE MAINTENANCE,
 SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (frederic.boulanger@supelec.fr)
@AcceptedRating Red
*/

package ptolemy.vergil.debugger;

import java.util.*;

import javax.swing.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

////////////////////////////////////////////////////////////////////////
//// DebugController
/**
An execution listener that suspends execution based on breakpoints.
This class is created by a debugger frame to handle the logic of breakpoints.

@author B. Desoutter, P. Domecq & G. Vibert and Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class DebugController implements DebugListener {

    ///////////////////////////////////////////////////////////////////
    //                Public variables                    //
    
    //Boolean that becomes false  when the Button END is pushed
    public boolean notFinished;

    //Boolean that becomes true when the Button Step In is pushed
    public boolean stepInPause;

    //List that contain the Watchers.
    public NamedList watcherList = new NamedList();

    /**
     * Construct a new debug controller with a reference to the given
     * debugger.
     * @param frame The debugger frame that created this controller.
     */
    public DebugController(DebuggerFrame frame) {
	notFinished = true;
	stepInPause = false;
    	_frame = frame;
    	_frame.setTitle("Pdb");
    	_frame.setSize(450, 260);
    	_frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a reference to the DebuggerFrame
     */
    public DebuggerFrame getDebuggerFrame() {
	return _frame;
    }

    /** Ignore string messages.
     */
    public void message(String string) {
    }

    /**
     * Ignore debug events that aren't firing events.  Firing events
     * are dispatched an appropriate method according to type.
     */
    public void event(DebugEvent debugEvent) {
	if (debugEvent instanceof FiringEvent) {
	    FiringEvent event = (FiringEvent) debugEvent;
	    if (event.getType() == FiringEvent.BEFORE_PREFIRE) {
		prefireEvent(event.getActor());
	    } else if (event.getType() == FiringEvent.BEFORE_FIRE) {
		fireEvent(event.getActor());
	    } else if (event.getType() == FiringEvent.BEFORE_POSTFIRE) {
		postfireEvent(event.getActor());
	    } else if (event.getType() == FiringEvent.AFTER_POSTFIRE) {
		postpostfireEvent(event.getActor());
	    }
	}
    }

    /**
     * This method is called just prior to an actor being prefired.  If
     * a breakpoint named "prefire" is contained within the given actor, then
     * pause execution and await user input from the DebuggerFrame.
     * @param actor The actor that is going to be prefired.
     */
    public void prefireEvent(Actor actor) {
	if (notFinished) {
	    _method = "prefire";
	    _frame.displayResult("Before prefiring " +
                    ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("prefire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_frame.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }

	    _test();
	}
    }

    /**
     * This method is called just prior to an actor being fired.  If
     * a breakpoint named "fire" is contained within the given actor, then
     * pause execution and await user input from the DebuggerFrame.
     * @param actor The actor that is going to be fired.
     */
    public void fireEvent(Actor actor) {
	if (notFinished) {
	    _method = "fire";
	    _frame.displayResult("Before firing " +
                    ((Nameable) actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("fire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_frame.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }

	    // Will hide the stepin button in the interface if the
	    // actor is not an opaque composite atomic.
	    ComponentEntity entity = (ComponentEntity) actor;
	    if (!entity.isAtomic() && entity.isOpaque()) {
		_frame.enableButton(6);
	    } else {
		_frame.disableButton(6);
	    }

	    _test();

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
     * This method is called just prior to an actor being postfired.  If
     * a breakpoint named "postfire" is contained within the given actor, then
     * pause execution and await user input from the DebuggerFrame.
     * @param actor The actor that is going to be postfired.
     */
    public void postfireEvent(Actor actor) {
	if (notFinished) {
	    _method = "postfire";
	    _frame.displayResult("Before postfiring " +
                    ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_frame.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }
	    _test();
	}
    }

    /**
     * This method is called just after an actor is postfired.  If
     * a breakpoint named "postpostfire" is contained within the given actor,
     * then pause execution and await user input from the DebuggerFrame.
     * @param actor The actor that was just postfired.
     */
    public void postpostfireEvent(Actor actor) {
        TypedCompositeActor container =
	    (TypedCompositeActor) ((NamedObj)actor).getContainer();

	if (notFinished) {
	    // Refresh ExecState
	    _method = "postpostfire";
	    _frame.displayResult("After postfiring " +
                    ((Nameable)actor).getFullName());
	    Breakpoint breakpoint = (Breakpoint)
		((NamedObj) actor).getAttribute("postpostfire");
	    if (breakpoint != null && breakpoint.evaluateCondition()) {
		_frame.displayResult("Breakpoint encountered!");
		waitUserCommand();
	    }

	    _test();

	    // After a composite is postpostfired, then pause execution.
	    if (actor instanceof CompositeActor &&
                    _command == "stepout") {
		_command = "pause";
	    }
	}
    }

    /**
     * This method is called after a firing event has been handled.
     * Depending on the event and the current command,
     * it chooses to pause the execution or not.
     * It may also change the current command to "pause" if the firing
     * event completed the previous command.
     */
    private void _test() {

	if (_method != "fire") {
	    /* signal to MMI that stepin forbidden */
	}

	if (_command == "pause") {
	    waitUserCommand();
	    _test();
	} else if (_command == "step" && _method == "postfire") {
	    _command = "pause";
	} else if (_command == "microstep") {
	    _command = "pause";
	} else if (_command == "stepin") {
	    if (_method == "fire") {
		stepInPause = true;
		_command = "pause";
	    } else {
		_frame.displayResult("Error : Can't step in here");
	    }
	}
    }

    /**
     * Synchronize the DebugController with the DebuggerFrame,
     * and wait for an entry by the user. See the actionListener of
     * the DebuggerFrame for a more effective comprehension.
     * @param state The execution state of the calling director.
     */
    public synchronized void waitUserCommand() {
	Iterator watchers = watcherList.elementList().iterator();
	while (watchers.hasNext()) {
	    ActorWatcher watcher = (ActorWatcher)watchers.next();
	    watcher.refresh();
	}

	_commandNotEntered = true;
	_frame.displayResult("Please, enter a command.");
	while (_commandNotEntered) {
	    _frame.putCmd = true;
	    try {
		wait();
	    } catch (InterruptedException ex) {
	    }
	}
	_frame.putCmd = false;
	_command = _frame.getUserCommand();
    }

    /**
     * Called by the Frame when a command is entered, to allow the execution
     * thread to continue.
     */
    public synchronized void commandEntered() {
	_commandNotEntered = false;
	notifyAll();
    }

    ///////////////////////////////////////////////////////////////////
    //                Private variables                   //
    private String _method = "";
    private String _command = "go";

    //boolean that allows to synchronize DebugController and DebuggerFrame.
    private boolean _commandNotEntered = true;

    // a link to DebuggerFrame
    private DebuggerFrame _frame;
}


