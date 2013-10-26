/* ResumableActor is used to wrap any MetroII compatible actor with MetroIIActorInterface.

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

import java.util.LinkedList;

import net.jimblackler.Utils.YieldAdapterIterable;
import net.jimblackler.Utils.YieldAdapterIterator;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MetroIIActorGeneralWrapper

/**
 * <p>
 * ResumableActor is used to wrap any MetroII compatible actor with
 * MetroIIActorInterface.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class ResumableActor extends ActMachine {
    /**
     * Construct a Actor-Thread pair.
     *
     * @param actor
     *            The actor
     */
    public ResumableActor(Actor actor) {
        super(actor);
        this._eventIterator = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Start or resume the execution of an actor. The execution means calling
     * prefire(), getfire(), and postfire(). The execution of getfire() is
     * encapsulated in a thread (implemented in YieldAdapter @see
     * net.jimblackler.Utils.YieldAdapter). The thread pauses when getfire()
     * yield-returns a list of MetroII events.
     *
     * @param metroIIEventList
     *            the list of MetroII events getfire() returns.
     *
     * @throws IllegalActionException
     *             if the associated action (e.g. firing) is not permitted.
     */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        /**
         * Start executing the wrapped actor in the thread.
         */
        if (getState() == State.PREFIRE_BEGIN) {
            assert getStateEvent().getName().contains("PREFIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                if (actor().prefire()) {
                    setState(State.PREFIRE_END_FIRE_BEGIN);
                }
            }
            metroIIEventList.add(proposeStateEvent());
        } else if (getState() == State.PREFIRE_END_FIRE_BEGIN) {
            assert getStateEvent().getName().contains("FIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                /* The getfire() of each Metropolis actor is invoked by a separate thread.
                 * Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                 * the events proposed by the thread.
                 */
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((GetFirable) actor())
                        .adapter();
                _eventIterator = results.iterator();
                setState(State.FIRING);
            }
            metroIIEventList.add(proposeStateEvent());
        }
        /**
         * Resume executing the wrapped actor with states saved in the thread.
         */
        else if (getState() == State.FIRING) {
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
                setState(State.FIRE_END_POSTFIRE_BEGIN);
                metroIIEventList.add(proposeStateEvent());
            }
        } else if (getState() == State.FIRE_END_POSTFIRE_BEGIN) {
            assert getStateEvent().getName().contains("POSTFIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                if (actor().postfire()) {
                    setState(State.POSTFIRE_END);
                } else {
                    // FIXME: handle the request that the actor wants to halt
                }
            }
            metroIIEventList.add(proposeStateEvent());
        } else if (getState() == State.POSTFIRE_END) {
            assert getStateEvent().getName().contains("POSTFIRE_END");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.PREFIRE_BEGIN);
            }
            metroIIEventList.add(proposeStateEvent());
        }
    }

    /**
     * Stop and dispose any associated thread.
     */
    @Override
    public void reset() {
        if (getState() == State.FIRING) {
            _eventIterator.dispose();
            actor().stop();
        }
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
