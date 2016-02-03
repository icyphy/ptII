/* Embedding of a Vert.x core.

   Copyright (c) 2014-2015 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.jjs;

import java.util.TreeSet;

import javax.script.ScriptContext;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// HelperBase

/**
   A base class for helper classes. The main function of this class is to
   provide a reference to the JavaScript actor for which the helper is helping.
   This is available in a protected method so that it isn't directly available
   in JavaScript. This actor should be used for all synchronized actions
   (to avoid deadlocks and race conditions).

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class HelperBase {

    /** Construct a helper for the specified JavaScript object.
     *  The argument can be a JavaScript actor or an instance of a
     *  JavaScript class.
     *  @param helping The object that this is helping.
     */
    public HelperBase(Object helping) {
    	Object actorOrWrapper = helping;
    	if (helping instanceof ScriptObjectMirror) {
    		// Helping a JavaScript object.
            _currentObj = (ScriptObjectMirror) helping;
            
            // Find the actor associated with the object.
            actorOrWrapper = _currentObj.eval("actor");
            if (actorOrWrapper instanceof ScriptObjectMirror) {
                actorOrWrapper = ScriptObjectMirror.unwrap(actorOrWrapper,
                        ScriptContext.ENGINE_SCOPE);
            }
    	}
        if (actorOrWrapper instanceof RestrictedJavaScriptInterface) {
            _actor = ((RestrictedJavaScriptInterface) actorOrWrapper)
                    ._getActor();
        } else if (actorOrWrapper instanceof JavaScript) {
            _actor = ((JavaScript) actorOrWrapper);
        } else {
            throw new InternalErrorException("Invalid actor object: "
                    + actorOrWrapper.toString());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Handle an error by emitting an error event, or if there is no
     *  error event handler registered, by invoking the error() method
     *  of the associated actor. Note that this may not stop execution.
     *  @param message The error message.
     */
    protected void _error(String message) {
        try {
            _currentObj.callMember("emit", "error", message);
            // NOTE: The error handler may not stop execution.
        } catch (Throwable ex) {
            // There may be no error event handler registered.
            // Use the actor to report the error.
            _actor.error(message);
        }
    }

    /** Handle an error by emitting an error event, or if there is no
     *  error event handler registered, by invoking the error() method
     *  of the associated actor. Note that this may not stop execution.
     *  @param message The error message.
     *  @param throwable The throwable.
     */
    protected void _error(String message, Throwable throwable) {
        try {
            _currentObj.callMember("emit", "error",
                    message + ": " + throwable );
            // NOTE: The error handler may not stop execution.
        } catch (Throwable ex) {
            // There may be no error event handler registered.
            // Use the actor to report the error.
            _actor.error(message, throwable);
        }
    }

    /** Emit the error message in the director thread by emitting
     *  an "error" event on the specified JavaScript object.
     *  @param emitter The JavaScript object that should emit an "error" event.
     *  @param message The error message.
     */
    protected void _error(ScriptObjectMirror emitter, final String message) {
        _issueResponse(new Runnable() {
            @Override
            public void run() {
                try {
                    // FIXME: This should include the name of the actor.
                    emitter.callMember("emit", "error", message);
                    // NOTE: The error handler may not stop execution.
                } catch (Throwable ex) {
                    // There may be no error event handler registered.
                    // Use the actor to report the error.
                    _actor.error(message, new IllegalActionException(_actor, ex, message));
                }
            }
        });
    }

    /** Emit the error message in the director thread by emitting
     *  an "error" event on the specified JavaScript object.
     *  @param emitter The JavaScript object that should emit an "error" event.
     *  @param message The error message.
     *  @param throwable The exception that caused the error.
     */
    protected void _error(ScriptObjectMirror emitter, final String message, final Throwable throwable) {
        _issueResponse(new Runnable() {
            @Override
            public void run() {
                try {
                    emitter.callMember("emit", "error", message);
                    // NOTE: The error handler may not stop execution.
                } catch (Throwable ex) {
                    // There may be no error event handler registered.
                    // Use the actor to report the error.
                    _actor.error(message, new IllegalActionException(_actor, throwable, message));
                }
            }
        });
    }

    /** Execute the specified response in the director thread
     *  so that it is executed atomically with respect to the swarmlet
     *  execution. This ensures, for example, that if the response
     *  produces multiple output events or errors, that all those
     *  output events and errors are simultaneous. It also prevents
     *  threading issues from having the response execute concurrently
     *  with the swarmlet execution. Generally, this should not be used
     *  for callbacks that will register listeners in reaction to some
     *  event, because the listeners will be registered an arbitrary
     *  amount of time in the future, which can cause events to be missed.
     *  @param response The response to execute.
     */
    protected void _issueResponse(Runnable response) {
        try {
            _actor.invokeCallback(response);
        } catch (IllegalActionException e) {
            _actor.error(_actor.getName()
                    + ": Failed to schedule response handler: "
                    + e.getMessage());
        }
    }

    /** Given an array of strings, return an array where everything is
     *  converted to lower case, duplicates are removed, and the order
     *  is alphabetical.
     */
    protected static TreeSet<String> _removeDuplicates(String[] original) {
        TreeSet<String> result = new TreeSet<String>();
        for (String value : original) {
            result.add(value.toLowerCase());
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                      ////

    /** The JavaScript actor that this is helping. All synchronization
     *  done by the helper should synchronize using this object as the monitor.
     */
    protected JavaScript _actor;

    /** The JavaScript object that this is a helper for. */
    protected ScriptObjectMirror _currentObj;
}
