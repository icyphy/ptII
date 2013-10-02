/* BlockingFire is a wrapper for Ptolemy actors to adapt to Metro semantics.

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
 * BlockingFire is a wrapper for Ptolemy actors to adapt to Metro semantics. 
 * It provides an implementation of the state transitions for the abstract 
 * wrapper FireMachine (@see FireMachine), which wraps an actor with a set of FSM 
 * interfaces so that the actor can be seen as a FSM from outside.
 * More specifically, the wrapper implements startOrResume(event_list) function 
 * that gives how the FSM reacts to the Metro events. 
 * 
 * The FireMachine has the following states. Each represents a state of the wrapped actor:
 * <ol>
 * <li> START: initial state </li>
 * <li> BEGIN: prefire() is called and returns true. getfire() will be called. </li>
 * <li> END: getfire() is called and returns properly. </li>
 * <li> FINAL: final state </li>
 * </ol>
 * And each of the states BEGIN and END is associated with a 'state event', which is 
 * the full name of the actor without model name plus one of the following suffixes: 
 * <ol>
 * <li> FIRE_BEGIN </li>
 * <li> FIRE_END </li>
 * </ol>
 * For example, 'Ramp' is the name of a top level actor in a model 'Test'. The full actor 
 * name should be 'Test.Ramp'. The Metro event associated with the state BEGIN 
 * of the actor is 'Ramp.FIRE_BEGIN'. 
 * 
 * Neither START nor FINAL is associated with any state event.
 *
 * When startOrResume() is called, the wrapper checks if the Metro event associated 
 * with the current state is notified. If the event is notified, call related function 
 * of the wrapped actor, transition to the next state, and propose the 
 * Metro event associated with the next state. If the state is associated with no state 
 * event, simply transition to the next state, and propose the Metro event associated with 
 * the next state. The 'next' state is defined as follows: STAR -> BEGIN -> END -> FINAL. 
 * For example, 
 * 
 *       action: propose FIRE_BEGIN
 * START ---------------------------------------> BEGIN
 * 
 *       guard: FIRE_BEGIN is notified   
 *       action: call fire(), propose FIRE_END
 * BEGIN ---------------------------------------> FIRE_END
 *  
 *       guard: FIRE_BEGIN is not notified   
 *       action: propose FIRE_BEGIN
 * BEGIN ---------------------------------------> BEGIN
 *  
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */

public class BlockingFire extends FireMachine {

    /** Construct a basic wrapper and wrap the input actor.
    *
    * @param actor the actor to be wrapped.
    */
    public BlockingFire(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
    * When startOrResume() is called, the wrapper checks if the Metro event associated 
    * with the current state is notified. If the event is notified, call related function 
    * of the wrapped actor, transition to the next state, and propose the 
    * Metro event associated with the next state. If the state is associated with no state 
    * event, simply transition to the next state, and propose the Metro event associated with 
    * the next state. The 'next' state is defined as follows: STAR -> BEGIN -> END -> FINAL. 
    * For example, 
    * 
    *       action: propose FIRE_BEGIN
    * START ---------------------------------------> BEGIN
    * 
    *       guard: FIRE_BEGIN is notified   
    *       action: call fire(), propose FIRE_END
    * BEGIN ---------------------------------------> FIRE_END
    *  
    *       guard: FIRE_BEGIN is not notified   
    *       action: propose FIRE_BEGIN
    * BEGIN ---------------------------------------> BEGIN
    *  
    *
    * @param metroIIEventList a list of MetroII events that are proposed. It is set by startOrResume()
    * not the caller.
    * @throws IllegalActionException If firing is not permitted.
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
