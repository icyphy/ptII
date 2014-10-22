/* Debug event indicating a state change.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// StateEvent

/**
 An event indicating a state change.  This event can be used for debugging.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (nobody)
 */
public class StateEvent implements DebugEvent {
    /** Construct an event with the specified source and destination
     *  state.
     *  @param source The source of this state event.
     *  @param state The state of this event refers to.
     */
    public StateEvent(FSMActor source, State state) {
        _source = source;
        _state = state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the source of the event, which is an instance of FSMActor.
     *  @return The ptolemy object that published this event.
     */
    @Override
    public NamedObj getSource() {
        return _source;
    }

    /** Return the state to which this event refers.
     *  @return The state to which this event refers.
     */
    public State getState() {
        return _state;
    }

    /** Return a string representation of this event, which is the
     *  string "New state: <i>name of state</i>".
     *  @return A string describing the event.
     */
    @Override
    public String toString() {
        return "New state: " + _state.getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The source.
    private FSMActor _source;

    // The new state.
    private State _state;
}
