/* ResumableFire is used to wrap any MetroII compatible actor with MetroIIActorInterface.

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

import net.jimblackler.Utils.YieldAdapterIterable;
import net.jimblackler.Utils.YieldAdapterIterator;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ResumableFire

/**
 * ResumableFire is a wrapper for Ptolemy actor. It provides an implementation
 * of FireMachine. More specifically, the wrapper implements a startOrResume()
 * function that associates the state of FireMachine with the state of fire() of
 * the wrapped actor as follows:
 * <ol>
 * <li>START: initial state</li>
 * <li>BEGIN: prefire() is called and returns true. getfire() will be called.</li>
 * <li>PROCESS: getfire() is being called, may be suspended but not terminated
 * yet</li>
 * <li>END: getfire() is called and returns properly.</li>
 * <li>FINAL: final state</li>
 * </ol>
 * When startOrResume() is called, the wrapper checks if the Metro event
 * associated with the current state is notified. If the event is notified, call
 * related function of the wrapped actor, transition to the next state, and
 * propose the Metro event associated with the next state. For example,
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
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class ResumableFire extends FireMachine {

    /**
     * Constructs a ResumableFire by wrapping the actor.
     *
     * @param actor
     *            Actor to be wrapped
     */
    public ResumableFire(Actor actor) {
        super(actor);
    }

    /**
     * Fires the wrapped actor in a resumable style. The function getfire()
     * of the wrapped actor is called in startOrResume() as follows:
     * <ol>
     * <li>Propose MetroII event POSTFIRE_END_PREFIRE_BEGIN and wait for the
     * event being notified</li>
     * <li>prefire()</li>
     * <li>Propose MetroII event PREFIRE_END_FIRE_BEGIN and wait for the event
     * being notified</li>
     * <li>Repeated calling getfire(eventList) and proposing events in the
     * returned eventList until getfire(eventList) terminates properly</li>
     * <li>Propose MetroII event FIRE_END_POSTFIRE_BEGIN and wait for the the
     * event being notified</li>
     * <li>postfire()</li>
     * </ol>
     * where 'wait' means checking the status of MetroII event. If notified,
     * continue execution, otherwise proposing the same event again.
     *
     * @param metroIIEventList
     *            A list of MetroII events.
     * @exception IllegalActionException
     *             if the associated action (e.g. firing) is not permitted.
     */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (getState() == State.START) {
            setState(State.BEGIN);
            metroIIEventList.add(proposeStateEvent());
            /**
             * Start executing the wrapped actor in the thread.
             */
        } else if (getState() == State.BEGIN) {
            assert getStateEvent().getName().contains("FIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                /* The getfire() of each Metropolis actor is invoked by a separate thread.
                 * Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                 * the events proposed by the thread.
                 */
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((GetFirable) actor())
                        .adapter();
                _eventIterator = results.iterator();
                setState(State.PROCESS);
                metroIIEventList.add(proposeStateEvent());
            } else {
                metroIIEventList.add(proposeStateEvent());
            }
        }
        /**
         * Resume executing the wrapped actor with states saved in the thread.
         */
        else if (getState() == State.PROCESS) {
            /* Every time hasNext() is called, the thread runs until the next event
             * is proposed. If any event is proposed, hasNext() returns true.
             * The proposed event is returned by next().
             * If the getfire() terminates without proposing event, hasNext()
             * returns false.
             */
            if (_eventIterator.hasNext()) {
                Iterable<Event.Builder> result = _eventIterator.next();
                for (Builder eventBuilder : result) {
                    // Event.Builder eventBuilder = builder;
                    eventBuilder.setStatus(Event.Status.PROPOSED);
                    metroIIEventList.add(eventBuilder);
                }
                if (metroIIEventList.isEmpty()) {
                    metroIIEventList.add(proposeStateEvent());
                }
            } else {
                if (_eventIterator.getMessageIllegalAction() != null) {
                    // reset();
                    throw _eventIterator.getMessageIllegalAction();
                }
                setState(State.END);
                metroIIEventList.add(proposeStateEvent());
            }
        } else if (getState() == State.END) {
            assert getStateEvent().getName().contains("FIRE_END");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.FINAL);
            } else {
                metroIIEventList.add(proposeStateEvent());
            }
        } else if (getState() == State.FINAL) {
            // do nothing
        } else {
            // unknown state;
            assert false;
        }
    }

    /**
     * Stops and dispose any associated thread.
     */
    @Override
    public void reset() {
        if (getState() == State.PROCESS) {
            _eventIterator.dispose();
            actor().stop();
        }
        // System.out.println(actor().getFullName()+" stops!");
        super.reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected fields                        ////

    /**
     * YieldAdapterIterator that is used to trigger actions of the wrapped
     * actor.
     */
    protected YieldAdapterIterator<Iterable<Event.Builder>> _eventIterator;

}
