/* ActMachine is an abstract wrapper for actors to adapt to MetroII semantics.

 Copyright (c) 2012-2013 The Regents of the University of California.
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
//// ActMachine

/**
 * ActMachine is an abstract wrapper for actors to adapt to MetroII semantics.
 * ActMachine wraps an actor with a set of FSM interfaces so that the actor can
 * be be seen as a FSM from outside. We pre-define the following states and each
 * state represents a state of the wrapped actor:
 * <ol>
 * <li>PREFIRE_BEGIN: before prefire() is called;</li>
 * <li>PREFIRE_END_FIRE_BEGIN: after prefire() is called, returns true and
 * before getfire() is called;</li>
 * <li>FIRING: getfire() is being called but is interrupted by some internal
 * MetroII events;</li>
 * <li>FIRE_END_POSTFIRE_BEGIN: after getfire() completes and before postfire()
 * is called;</li>
 * <li>POSTFIRE_END: after postfire() is called.</li>
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
 * <p>
 * With a concrete implementation of StartOrResumable(), a director (usually a
 * MetroII director) is able to manipulate the wrapped actor by calling
 * StartOrResumable() with MetroII events.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public abstract class ActMachine implements StartOrResumable {

    /**
     * Predefined states to indicate the internal state of the wrapped actor.
     */
    public enum State {
        /**
         * The state before prefire() is called.
         */
        PREFIRE_BEGIN,
        /**
         * The state after prefire() is called, returns true and before
         * getfire() is called.
         */
        PREFIRE_END_FIRE_BEGIN,
        /**
         * The state when getfire() is being called but is interrupted by some
         * internal MetroII events.
         */
        FIRING,
        /**
         * The state after getfire() completes and before postfire() is called.
         */
        FIRE_END_POSTFIRE_BEGIN,
        /**
         * The state after postfire() is called.
         */
        POSTFIRE_END
    }

    /**
     * Construct an ActMachine wrapper and initialize a set of the MetroII
     * events that are associated with the states. Reset the current state.
     *
     * @param actor
     *            the actor to be wrapped.
     */
    public ActMachine(Actor actor) {
        _actor = actor;

        String actorName = _actor.getFullName();
        String actorNameWithoutModelName = _trimModelName(actorName);

        _PrefireBeginEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "."
                        + "PREFIRE_BEGIN");
        _FireBeginEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "."
                        + "FIRE_BEGIN");
        _FiringEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "." + "FIRING");
        _PostfireBeginEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "."
                        + "POSTFIRE_BEGIN");
        _PostfireEndEvent = MetroIIEventBuilder
                .newProposedEvent(actorNameWithoutModelName + "."
                        + "POSTFIRE_END");

        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the MetroII event associated with the current state.
     *
     * @return the MetroII event associated with the current state.
     */
    public Builder getStateEvent() {
        switch (getState()) {
        case PREFIRE_BEGIN:
            return _PrefireBeginEvent;
        case PREFIRE_END_FIRE_BEGIN:
            return _FireBeginEvent;
        case FIRING:
            return _FiringEvent;
        case FIRE_END_POSTFIRE_BEGIN:
            return _PostfireBeginEvent;
        case POSTFIRE_END:
            return _PostfireEndEvent;
        default:
            assert false;
            return null;
        }
    }

    /**
     * Get the state of the wrapped actor.
     *
     * @see #setState
     * @return The state.
     */
    public State getState() {
        return _state;
    }

    /**
     * Reset the state to be PREFIRE_BEGIN.
     */
    @Override
    public void reset() {
        setState(State.PREFIRE_BEGIN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the wrapped actor.
     *
     * @return the wrapped actor.
     */
    protected Actor actor() {
        return _actor;
    }

    /**
     * Get the MetroII event associated with the current state and set the state
     * of the event to be PROPOSED.
     *
     * @return the MetroII event associated with the current state
     */
    protected Builder proposeStateEvent() {
        Builder event = getStateEvent();
        event.setStatus(Status.PROPOSED);
        event.clearTime();
        return event;
    }

    /**
     * Set the state of the wrapped actor.
     *
     * @see #getState
     * @param state
     *            The state to be set
     */
    protected void setState(State state) {
        _state = state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Trim the model name from a full name of an actor or an event.
     *
     * @param name
     *            A full name of an actor or an event.
     * @return The trimmed name.
     */
    private String _trimModelName(String name) {
        assert name.length() > 1;
        int pos = name.indexOf(".", 1);
        return name.substring(pos);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * Prefire begin event.
     */
    final private Builder _PrefireBeginEvent;

    /**
     * Fire begin event
     */
    final private Builder _FireBeginEvent;

    /**
     * Firing event
     */
    final private Builder _FiringEvent;

    /**
     * Postfire begin event
     */
    final private Builder _PostfireBeginEvent;

    /**
     * Postfire end event
     */
    final private Builder _PostfireEndEvent;

    /**
     * Actor state
     */
    private State _state;

    /**
     * Actor which is being fired
     */
    private Actor _actor;
}
