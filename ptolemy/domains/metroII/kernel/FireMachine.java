/* FireMachine is an abstract wrapper for Ptolemy actors to adapt to MetroII semantics.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

///////////////////////////////////////////////////////////////////
//// FireMachine

/**
 *
 * FireMachine is an abstract wrapper for Ptolemy actors to adapt to MetroII
 * semantics. FireMachine wraps an actor with a set of FSM interfaces so that
 * the actor can be be seen as a FSM from outside. We pre-define the following
 * states and each state represents a state of the wrapped actor:
 * <ol>
 * <li>START: the initial state;</li>
 * <li>BEGIN: before getfire() is called;</li>
 * <li>FIRING: getfire() 'yield returns' (@see net.jimblackler.Utils) some internal MetroII events;</li>
 * <li>END: after getfire() completes;</li>
 * <li>FINAL: the final state.</li>
 * </ol>
 * The wrapper explicitly records the current state of the FSM. The state
 * transition is triggered by a function call to startOrResume(events) with
 * MetroII events as the arguments.
 *
 * <p>
 * For any concrete subclass of ActMachine, the StartOrResumable() interface has
 * to be implemented, in which how the FSM react to MetroII events (or in other
 * words, the state transitions) should be implemented.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public abstract class FireMachine implements StartOrResumable {

    /**
     * Predefined states for the wrapped actor.
     */
    public enum State {
    /**
     * The initial state.
     */
    START,
    /**
     * The state before getfire() is called.
     */
    BEGIN,
    /**
     * The state when getfire() 'yield returns' some internal MetroII
     * events.
     */
    PROCESS,
    /**
     * The state after getfire() normally completes.
     */
    END,
    /**
     * The final state.
     */
    FINAL
    }

    /**
     * Constructs an FireMachine wrapper and initialize the MetroII events.
     *
     * @param actor
     *            the actor whose getfire() is to be wrapped.
     */
    public FireMachine(Actor actor) {
        _actor = actor;

        String actorName = _actor.getFullName();
        String actorNameWithoutModelName = _trimModelName(actorName);

        _BeginEvent = MetroIIEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "FIRE_BEGIN");
        _ProcessEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "." + "PROCESS");
        _EndEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "." + "FIRE_END");

        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Returns the MetroII event associated with the current state.
     *
     * @return the MetroII event associated with the current state.
     */
    public Builder getStateEvent() {
        switch (getState()) {
        case BEGIN:
            return _BeginEvent;
        case PROCESS:
            return _ProcessEvent;
        case END:
            return _EndEvent;
        default:
            assert false;
            return null;
        }
    }

    /**
     * Gets the state of the wrapped actor.
     *
     * @return the state.
     * @see #setState(State)
     */
    public State getState() {
        return _state;
    }

    /**
     * Resets the state to be PREFIRE_BEGIN.
     */
    @Override
    public void reset() {
        setState(State.START);
    }

    /**
     * Wraps up the firing by reseting.
     */
    public void wrapup() {
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Returns the wrapped actor.
     *
     * @return the wrapped actor.
     */
    protected Actor actor() {
        return _actor;
    }

    /**
     * Returns the MetroII event associated with the current state and set the
     * state of the event to be PROPOSED.
     *
     * @return the MetroII event associated with the current state
     */
    protected Builder proposeStateEvent() {
        Builder event = getStateEvent();
        assert event != null;
        event.setStatus(Status.PROPOSED);
        event.clearTime();

        return event;
    }

    /**
     * Sets the state of the wrapped actor.
     *
     * @param state
     *            the state to be set.
     * @see #getState()
     */
    protected void setState(State state) {
        _state = state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Trims the substring until (including) '.' from name.
     *
     * @param name
     *            the input string.
     * @return The trimmed string.
     */
    private String _trimModelName(String name) {
        assert name.length() > 1;
        int pos = name.indexOf(".", 1);
        return name.substring(pos);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * Event indicating state BEGIN.
     */
    final private Builder _BeginEvent;

    /**
     * Event indicating state PROCESS.
     */
    final private Builder _ProcessEvent;

    /**
     * Event indicating state END.
     */
    final private Builder _EndEvent;

    /**
     * The wrapped actor.
     */
    private Actor _actor;

    /**
     * The state of the fire function.
     */
    private State _state;
}
