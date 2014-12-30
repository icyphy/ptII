/* BlockingFire is a wrapper for Ptolemy actors to adapt to MetroII semantics.

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

import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

/**
 * BlockingFire is a wrapper for Ptolemy actors to adapt to MetroII semantics. It
 * provides an implementation of the state transitions for the abstract wrapper
 * FireMachine (@see FireMachine), which wraps an actor with a set of FSM
 * interfaces so that the actor can be seen as a FSM from outside. In
 * particular, startOrResume(event_list) is the function that reacts to the
 * MetroII events that trigger the FSM.
 *
 * <p>
 * The FireMachine has the following states. Each represents a state of the
 * wrapped actor:</p>
 * <ol>
 * <li>START: initial state.</li>
 * <li>BEGIN: the actor is not fired.</li>
 * <li>END: the actor is fired.</li>
 * <li>FINAL: final state.</li>
 * </ol>
 * <p>And each of the states BEGIN and END is associated with a 'state event',
 * which is the full name of the actor without model name plus one of the
 * following suffixes:</p>
 * <ol>
 * <li>FIRE_BEGIN</li>
 * <li>FIRE_END</li>
 * </ol>
 * <p>For example, 'Ramp' is the name of a top level actor in a model 'Test'. The
 * full actor name is 'Test.Ramp'. The MetroII state event associated with the
 * state BEGIN of the actor is 'Ramp.FIRE_BEGIN'.
 * </p>
 *
 * <p>
 * Neither START nor FINAL is associated with any state event.
 * </p>
 *
 * <p> To understand the transition table of the FSM, 
 * {@link #startOrResume(LinkedList)}.</p>
 *
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class BlockingFire extends FireMachine {

    /**
     * Construct a basic wrapper and wrap the input actor.
     *
     * @param actor
     *            the actor to be wrapped.
     */
    public BlockingFire(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *
     * When startOrResume(eventList) is called, the wrapper checks if the MetroII
     * event associated with the current state is changed to NOTIFIED. If the
     * event is notified, call related function of the wrapped actor, transition
     * to the next state, clear eventList and add the MetroII event associated
     * with the state to eventList (referred to as propose events). If the state
     * is associated with no state event, eventList is an empty list. The 'next'
     * state is defined as follows: STAR -&gt; BEGIN -&gt; END -&gt; FINAL. For example,
     *
     * <pre>
     *       action: propose FIRE_BEGIN
     * START ---------------------------------------&gt; BEGIN
     *
     *       guard: FIRE_BEGIN is notified
     *       action: call fire(), propose FIRE_END
     * BEGIN ---------------------------------------&gt; FIRE_END
     *
     *       guard: FIRE_BEGIN is not notified
     *       action: propose FIRE_BEGIN
     * BEGIN ---------------------------------------&gt; BEGIN
     * </pre>
     *
     * @param metroIIEventList
     *            a list of MetroII events that are proposed. It is set by
     *            startOrResume() not the caller.
     * @exception IllegalActionException
     *                If the wrapped actor is in an illegal state or any called
     *                method throws it.
     */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        assert metroIIEventList != null;
        if (getState() == State.START) {
            setState(State.BEGIN);
            metroIIEventList.add(proposeStateEvent());
        } else if (getState() == State.BEGIN) {
            assert getStateEvent().getName().contains("FIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.END);
                metroIIEventList.clear();
                metroIIEventList.add(proposeStateEvent());
            } else {
                metroIIEventList.clear();
                metroIIEventList.add(getStateEvent());

            }
        } else if (getState() == State.END) {
            assert getStateEvent().getName().contains("FIRE_END");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                actor().fire();
                setState(State.FINAL);
                metroIIEventList.clear();
            } else {
                metroIIEventList.clear();
                metroIIEventList.add(getStateEvent());

            }
        } else if (getState() == State.FINAL) {
            // do nothing
        } else {
            // unknown state;
            throw new IllegalActionException(this.actor(),
                    " is in an unknown state.");
        }
    }
}
