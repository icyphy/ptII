/* Basic wrapper for Ptolemy actor to work with MetroIIDirector.

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

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

/** <p> MetroIIActorBasicWrapper is used for wrapping a Ptolemy actor
 * to work with a MetroIIDirector. It provides a basic implementation of 
 * MetroIIActorInterface.  
 * </p>
 * 
 * 
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */

public class MetroIIActorBasicWrapper implements MetroIIActorInterface {

    /** Construct a basic wrapper.
     * 
     * @param actor The actor
     * @param type The type of actor
     * @param state The initial thread state
     * @param thread The thread
     */
    public MetroIIActorBasicWrapper(Actor actor) {
        this.actor = actor;
        this.state = State.POSTFIRE_END_PREFIRE_BEGIN;
        currentStateEvent = createMetroIIEvent("PREFIRE_BEGIN");
    }

    /**
     * Dispose the current execution. 
     */
    public void reset() {
        this.state = State.POSTFIRE_END_PREFIRE_BEGIN;
        currentStateEvent = createMetroIIEvent("PREFIRE_BEGIN");
    }

    /**
    * The functions prefire(), fire() and postfire() 
    * are wrapped in startOrResume() as follows: 
    * <ol>
    * <li> Propose MetroII event POSTFIRE_END_PREFIRE_BEGIN and wait for 
    * the event being notified</li>
    * <li> prefire() </li>
    * <li> Propose MetroII event PREFIRE_END_FIRE_BEGIN and wait for the 
    * event being notified</li>
    * <li> fire() </li>
    * <li> Propose MetroII event FIRE_END_POSTFIRE_BEGIN and wait for the 
    * the event being notified</li>
    * <li> postfire() </li>
    * </ol>
    * where 'wait' means checking the status of MetroII event. If notified, 
    * continue execution, otherwise proposing the same event again.
    * 
    * @param metroIIEventList A list of MetroII events. 
    */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (state == State.POSTFIRE_END_PREFIRE_BEGIN) {
            assert currentStateEvent.getName().contains("PREFIRE_BEGIN");
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (actor.prefire()) {
                    state = State.PREFIRE_END_FIRE_BEGIN;
                    currentStateEvent = createMetroIIEvent("FIRE_BEGIN");
                }
            }
            metroIIEventList.add(currentStateEvent);
        } else if (state == State.PREFIRE_END_FIRE_BEGIN) {
            assert currentStateEvent.getName().contains("FIRE_BEGIN");
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                actor.fire();
                state = State.FIRE_END_POSTFIRE_BEGIN;
                currentStateEvent = createMetroIIEvent("POSTFIRE_BEGIN");
            }
            metroIIEventList.add(currentStateEvent);
        } else if (state == State.FIRE_END_POSTFIRE_BEGIN) {
            assert currentStateEvent.getName().contains("POSTFIRE_BEGIN");
            if (currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                if (actor.postfire()) {
                    state = State.POSTFIRE_END_PREFIRE_BEGIN;
                    currentStateEvent = createMetroIIEvent("PREFIRE_BEGIN");
                } else {
                    // FIXME: handle the request that the actor wants to halt
                    //                if (_debugging) {
                    //                    _debug("Actor requests halt: "
                    //                            + ((Nameable) actorThread.actor).getFullName());
                    //                }
                }
            }
            metroIIEventList.add(currentStateEvent);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected fields                       ////

    /** Create a MetroII event
     * 
     */
    protected Builder createMetroIIEvent(String name) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(actor.getFullName() + "." + name);
        builder.setOwner(actor.getFullName());
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.GENERIC);
        return builder;
    }

    /** Current state event
     * 
     */
    protected Builder currentStateEvent;

    /** Actor state
     * 
     */
    protected State state;

    /** Actor state 
     */
    protected enum State {
        POSTFIRE_END_PREFIRE_BEGIN, PREFIRE_END_FIRE_BEGIN, FIRING, FIRE_END_POSTFIRE_BEGIN
    }

    /** Actor which is being fired 
     * 
     */
    protected Actor actor;

}
