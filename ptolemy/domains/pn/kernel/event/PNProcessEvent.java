/* An event representing the various changes in the state of a process in PN.

Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.pn.kernel.event;
import ptolemy.actor.Actor;
import ptolemy.kernel.Entity;

//////////////////////////////////////////////////////////////////////////
//// PNProcessEvent
/**
An event passed from a process executing under the PN semantics to a
PNProcessListener. This is used to
represent an event that happened during the execution of a topology.
This event contains two pieces of information:  the actor under the control
of the process and an exception that might be thrown.
The exception might not be a valid reference.

@author Mudit Goel
@version $Id$
*/

public class PNProcessEvent {

    /** Create a new event
     *  @param The actor
     */
    public PNProcessEvent(Actor actor, int state) {
        _actor = actor;
        _state = state;
    }

    /** Create a new event that corresponds to an exception
     *  caught by the process.
     */
    public PNProcessEvent(Actor actor, int state, int cause) {
        _actor = actor;
        _state = state;
        _cause = cause;
    }

    public PNProcessEvent(Actor actor, Exception exception) {
        _actor = actor;
        _state = PROCESS_FINISHED;
        _cause = FINISHED_WITH_EXCEPTION;
        _exception = exception;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the actor corresponding to the process that generated the event.
     */
    public Actor getActor() {
        return _actor;
    }

    public int getBlockingCause() {
        return _cause;
    }

    public int getCurrentState() {
        return _state;
    }

    /** Return the exception associated with the event.
     */
    public Exception getException() {
        return _exception;
    }

    public int getFinishingCause() {
        return _cause;
    }

    public String toString() {
        String result;
        String st = null;
        String ca = null;
        if (_state == PROCESS_BLOCKED) {
            st = "PROCESS_BLOCKED";
            if (_cause == BLOCKED_ON_DELAY) {
                ca = "BLOCKED_ON_DELAY";
            } else if (_cause == BLOCKED_ON_MUTATION) {
                ca = "BLOCKED_ON_MUTATION";
            } else if (_cause == BLOCKED_ON_READ) {
                ca = "BLOCKED_ON_READ";
            } else if (_cause == BLOCKED_ON_WRITE) {
                ca = "BLOCKED_ON_WRITE";
            } else ca = "BLOCKING_CAUSE_UNKNOWN";
            result = "State of "+((Entity)_actor).getFullName()+" is "+st+
                " and the cause = "+ca;
        } else if (_state == PROCESS_FINISHED) {
            st = "PROCESS_FINISHED";
            if (_cause == FINISHED_ABRUPTLY) {
                ca = "FINISHED_ABRUPTLY";
            } else if (_cause == FINISHED_PROPERLY) {
                ca = "FINISHED_PROPERLY";
            } else if (_cause == FINISHED_WITH_EXCEPTION) {
                ca = "FINISHED_WITH_EXCEPTION with exception "+
                    _exception.toString();
            } else ca = "FINISHED_CAUSE_UNKNOWN";
            result = "State of "+((Entity)_actor).getFullName()+" is "+st+
                " and the cause = "+ca;
        } else if (_state == PROCESS_PAUSED) {
            st = "PROCESS_PAUSED";
            result = "State of "+((Entity)_actor).getFullName()+" is "+st;
        } else if (_state == PROCESS_RUNNING) {
            st = "PROCESS_RUNNING";
            result = "State of "+((Entity)_actor).getFullName()+" is "+st;
        } else {
            st = "UNKNOWN_PROCESS_STATE";
            result = "State of "+((Entity)_actor).getFullName()+" is "+st;
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   public variables                  /////
    public static final int BLOCKED_ON_DELAY = 111;
    public static final int BLOCKED_ON_MUTATION = 112;
    public static final int BLOCKED_ON_READ = 113;
    public static final int BLOCKED_ON_WRITE = 114;

    public static final int FINISHED_ABRUPTLY = 734;
    public static final int FINISHED_PROPERLY = 735;
    public static final int FINISHED_WITH_EXCEPTION = 736;

    public static final int PROCESS_BLOCKED = 367;
    public static final int PROCESS_FINISHED = 368;
    public static final int PROCESS_PAUSED = 369;
    public static final int PROCESS_RUNNING = 370;


    ///////////////////////////////////////////////////////////////////
    ////                   private variables                 /////

    private Actor _actor = null;
    private int _cause = 0;
    private Exception _exception = null;
    private int _state = 0;
}
