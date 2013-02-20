/* GMetroIIActorGeneralWrapper is used to wrap any MetroII compatible actor with MetroIIActorInterface. 

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

/** <p> MetroIIActorGeneralWrapper is used to wrap any MetroII 
 * compatible actor with MetroIIActorInterface. </p> 
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIActorGeneralWrapper extends MetroIIActorBasicWrapper {
    /** Construct a Actor-Thread pair.
     * 
     * @param actor The actor
     */
    public MetroIIActorGeneralWrapper(Actor actor) {
        super(actor);
        this._eventIterator = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Start or resume the execution of an actor. The execution means 
     * calling prefire(), getfire(), and postfire(). The 
     * execution of getfire() is encapsulated in a thread (implemented 
     * in YieldAdapter @see net.jimblackler.Utils.YieldAdapter). 
     * The thread pauses when getfire() yield-returns a list of MetroII 
     * events. 
     * 
     * @param metroIIEventList the list of MetroII events getfire() 
     * returns. 
     */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        /**
         * Start executing the wrapped actor in the thread. 
         */
        if (_state == State.POSTFIRE_END_PREFIRE_BEGIN) {
            assert _currentStateEvent.getName().contains("PREFIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (_actor.prefire()) {
                    _state = State.PREFIRE_END_FIRE_BEGIN;
                    _currentStateEvent = _createMetroIIEvent("FIRE_BEGIN");
                }
            }
            metroIIEventList.add(_currentStateEvent);
        } else if (_state == State.PREFIRE_END_FIRE_BEGIN) {
            assert _currentStateEvent.getName().contains("FIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                /* The getfire() of each Metropolis actor is invoked by a separate thread.
                 * Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                 * the events proposed by the thread.
                 */
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) _actor)
                        .adapter();
                _eventIterator = results.iterator();
                _state = State.FIRING;
                _currentStateEvent = _createMetroIIEvent("FIRING");
            }
            metroIIEventList.add(_currentStateEvent);
        }
        /**
         * Resume executing the wrapped actor with states saved in the thread.
         */
        else if (_state == State.FIRING) {
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
            } else {
                _state = State.FIRE_END_POSTFIRE_BEGIN;
                _currentStateEvent = _createMetroIIEvent("POSTFIRE_BEGIN");
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (_state == State.FIRE_END_POSTFIRE_BEGIN) {
            assert _currentStateEvent.getName().contains("POSTFIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (_actor.postfire()) {
                    _state = State.POSTFIRE_END_PREFIRE_BEGIN;
                    _currentStateEvent = _createMetroIIEvent("PREFIRE_BEGIN");
                } else {
                    // FIXME: handle the request that the actor wants to halt
                }
            }
            metroIIEventList.add(_currentStateEvent);
        }
    }

    /**
     * Stop and dispose any associated thread. 
     */
    @Override
    public void reset() {
        if (_state == State.FIRING) {
            _eventIterator.dispose();
            _actor.stop();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * Thread that is firing the actor
     */
    private YieldAdapterIterator<Iterable<Event.Builder>> _eventIterator;

}
