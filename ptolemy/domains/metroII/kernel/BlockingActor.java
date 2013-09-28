/* BlockingActor is a basic wrapper for Ptolemy actors to adapt to Metro semantics.

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
 * BlockingActor is a basic wrapper for Ptolemy actors to adapt to MetroII semantics. 
 * BlockingActor provides an implementation of the state transitions for the abstract 
 * wrapper ActMachine (@see ActMachine), which wraps an actor with a set of FSM 
 * interfaces so that the actor can be seen as a FSM from outside. More specifically, 
 * BlockingActor implements the startOrResume(event_list) function that gives how the FSM 
 * reacts to the Metro events. 
 * 
 * The ActMachine has the following states. Each represents a state of the wrapped actor:
 * <ol>
 * <li> PREFIRE_BEGIN: the state before prefire() is called; </li>
 * <li> PREFIRE_END_FIRE_BEGIN: the state after prefire() is called, returns 
 * true and before getfire() is called; </li>
 * <li> FIRING: the state when getfire() is being called but is interrupted by some 
 * internal Metro events; </li>
 * <li> FIRE_END_POSTFIRE_BEGIN: the state after getfire() completes and before 
 * postfire() is called; </li>
 * <li> POSTFIRE_END: the state after postfire() is called. </li>
 * </ol>
 *
 * And each state is associated with a 'state event', which is the full name of the actor 
 * without model name plus one of the following suffixes: 
 * <ol>
 * <li> PREFIRE_BEGIN </li>
 * <li> FIRE_BEGIN </li>
 * <li> FIRING </li>
 * <li> POSTFIRE_BEGIN </li>
 * <li> POSTFIRE_END </li>
 * </ol>
 * For example, 'Ramp' is the name of a top level actor in a model 'Test'. The full actor 
 * name should be 'Test.Ramp'. The Metro event associated with the state PREFIRE_BEGIN 
 * of the actor is 'Ramp.PREFIRE_BEGIN'. 
 *
 * When startOrResume(event_list) is called, the wrapper checks if the Metro event associated with 
 * the current state is notified. If the event is notified, call related function of the 
 * wrapped actor, transition to the next state, and propose the Metro event associated with 
 * the next state. Otherwise stay in the current state. The 'next' state is defined as follows: 
 * PREFIRE_BEGIN -> PREFIRE_END_FIRE_BEGIN -> FIRING -> FIRE_END_POSTFIRE_BEGIN -> POSTFIRE_END
 * -> PREFIRE_BEGIN.
 * 
 * The outgoing transitions for PREFIRE_BEGIN is as follows:  
 * 
 *               guard: PREFIRE_BEGIN is notified   
 *               action: call prefire(), propose FIRE_BEGIN
 * PREFIRE_BEGIN ---------------------------------------> PREFIRE_END_FIRE_BEGIN
 *  
 *               guard: PREFIRE_BEGIN is not notified   
 *               action: propose PREFIRE_BEGIN
 * PREFIRE_BEGIN ---------------------------------------> PREFIRE_BEGIN
 *  
 * 'propose' means setting the state of the Metro event to be 'PROPOSED' and adding it into 
 * event_list (the parameter of startOrResume(event_list)). Thus the caller of startOrResume 
 * will get a reference to the proposed event. 
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class BlockingActor extends ActMachine {

    /** Construct a basic wrapper and wrap the input actor.
     *
     * @param actor The actor
     */
    public BlockingActor(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Dispose the current state and reset to PREFIRE_BEGIN.
     */
    public void reset() {
        setState(State.PREFIRE_BEGIN);
    }

    /**
    * When startOrResume(event_list) is called, the wrapper checks if the Metro event associated with 
    * the current state is notified. If the event is notified, call related function of the 
    * wrapped actor, transition to the next state, and propose the Metro event associated with 
    * the next state. Otherwise stay in the current state. The 'next' state is defined as follows: 
    * PREFIRE_BEGIN -> PREFIRE_END_FIRE_BEGIN -> FIRING -> FIRE_END_POSTFIRE_BEGIN -> POSTFIRE_END
    * -> PREFIRE_BEGIN.
    * 
    * The outgoing transitions for PREFIRE_BEGIN is as follows:  
    * 
    *               guard: PREFIRE_BEGIN is notified   
    *               action: call prefire(), propose FIRE_BEGIN
    * PREFIRE_BEGIN ---------------------------------------> PREFIRE_END_FIRE_BEGIN
    *  
    *               guard: PREFIRE_BEGIN is not notified   
    *               action: propose PREFIRE_BEGIN
    * PREFIRE_BEGIN ---------------------------------------> PREFIRE_BEGIN
    *  
    * 'propose' means setting the state of the Metro event to be 'PROPOSED' and adding it into 
    * event_list (the parameter of startOrResume(event_list)). Thus the caller of startOrResume 
    * will get a reference to the proposed event. 
    *
    * @param metroIIEventList a list of MetroII events that are proposed. It is set by startOrResume()
    * not the caller.
    */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (getState() == State.PREFIRE_BEGIN) {
            assert getStateEvent().getName().contains("PREFIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                if (actor().prefire()) {
                    setState(State.PREFIRE_END_FIRE_BEGIN);
                }
            }
            metroIIEventList.add(getStateEvent());
        } else if (getState() == State.PREFIRE_END_FIRE_BEGIN) {
            assert getStateEvent().getName().contains("FIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                actor().fire();
                setState(State.FIRE_END_POSTFIRE_BEGIN);
            }
            metroIIEventList.add(getStateEvent());
        } else if (getState() == State.FIRE_END_POSTFIRE_BEGIN) {
            assert getStateEvent().getName().contains("POSTFIRE_BEGIN");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                if (actor().postfire()) {
                    setState(State.POSTFIRE_END);
                } else {
                    // FIXME: handle the request that the actor wants to halt
                    //                if (_debugging) {
                    //                    _debug("Actor requests halt: "
                    //                            + ((Nameable) actorThread.actor).getFullName());
                    //                }
                }
            } else {
                metroIIEventList.add(proposeStateEvent());
            }
        } else if (getState() == State.POSTFIRE_END) {
            assert getStateEvent().getName().contains("POSTFIRE_END");
            if (getStateEvent().getStatus() == Event.Status.NOTIFIED) {
                setState(State.PREFIRE_BEGIN);
            }
            metroIIEventList.add(proposeStateEvent());
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected fields                       ////

}
