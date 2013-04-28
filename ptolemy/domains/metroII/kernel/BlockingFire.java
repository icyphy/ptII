/* BlockingFire is a wrapper for Ptolemy actor.

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

/**
 * BlockingFire is a wrapper for Ptolemy actor. It provides a basic implementation of
 * MetroIIActorInterface (@see MetroIIActorInterface#startOrResume). 
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */

public class BlockingFire extends FireMachine {

    /** Construct a basic wrapper.
    *
    * @param actor The actor
    */
    public BlockingFire(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


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
        assert metroIIEventList != null;        
        if (getState() == State.START) {
            setState(State.BEGIN);
            metroIIEventList.add(proposeStateEvent());
        } else if (getState() == State.BEGIN) {
            assert getStateEvent().getName().contains("FIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                actor().fire();
                setState(State.END);
                metroIIEventList.add(proposeStateEvent());
            } else {
                metroIIEventList.add(getStateEvent());
            }
        } else if (getState() == State.END) {
            assert getStateEvent().getName().contains("FIRE_END");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.FINAL);
            } else {
                metroIIEventList.add(getStateEvent());
            }
        } else if (getState() == State.FINAL) {
            // do nothing
        } else {
            // unknown state; 
            assert false; 
        }
        
    }
        
}
