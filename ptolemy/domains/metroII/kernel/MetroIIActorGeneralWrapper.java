/* Actor-Thread data structure for MetroII Semantics.

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
//// MetroIIActorThread

/** <p> MetroIIActorThread is the data structure to maintain the actor 
 * and thread pair for MetroIIDirector.  
 * 
 * @author glp
 * @version $ld$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIActorGeneralWrapper implements MetroIIActorInterface {

    /** Status of thread 
     */
    public enum State {
        ACTIVE, READY, WAITING
    }

    /** Construct a Actor-Thread pair.
     * 
     * @param actor The actor
     * @param type The type of actor
     * @param state The initial thread state
     * @param thread The thread
     */
    public MetroIIActorGeneralWrapper(Actor actor) {
        this.actor = actor;
        this.state = State.WAITING;
        this.eventIterator = null;
    }

    public void close() {
        if (state == MetroIIActorGeneralWrapper.State.ACTIVE) {
            eventIterator.dispose();
        }
    }

    /** Actor which is being fired 
     * 
     */
    public Actor actor;

    /**
     * State of the thread
     */
    public State state;

    /**
     * Thread that is firing the actor
     */
    public YieldAdapterIterator<Iterable<Event.Builder>> eventIterator;

    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (state == MetroIIActorGeneralWrapper.State.WAITING) {
            if (actor.prefire()) {
                // The getfire() of each Metropolis actor is invoked by a separate thread.
                // Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                // the events proposed by the thread. 
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) actor)
                        .adapter();
                eventIterator = results.iterator();
                state = MetroIIActorGeneralWrapper.State.ACTIVE;
            }
        } else if (state == MetroIIActorGeneralWrapper.State.ACTIVE) {

            // Every time hasNext() is called, the thread runs until the next event 
            // is proposed. If any event is proposed, hasNext() returns true. 
            // The proposed event is returned by next().  
            // If the getfire() terminates without proposing event, hasNext()
            // returns false. 
            if (eventIterator.hasNext()) {
                Iterable<Event.Builder> result = eventIterator.next();
                for (Builder eventBuilder : result) {
                    // Event.Builder eventBuilder = builder;
                    eventBuilder.setStatus(Event.Status.WAITING);
                    metroIIEventList.add(eventBuilder);
                }
            } else {
                state = MetroIIActorGeneralWrapper.State.WAITING;
                if (!actor.postfire()) {
                    // FIXME: handle the request that the actor wants to halt

                    //                    if (_debugging) {
                    //                        _debug("Actor requests halt: "
                    //                                + ((Nameable) actor).getFullName());
                    //                    }
                }
                //                if (_stopRequested) {
                //                    if (_debugging) {
                //                        _debug("Actor requests halt: "
                //                                + ((Nameable) actor).getFullName());
                //                    }
                //                }
            }
        }
    }

}
