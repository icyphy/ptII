/* Abstract wrapper for getfire function.

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
//// FireMachine

/**
* 
* Abstract wrapper for getfire function. FireMachine is a FSM. The states of the FSM represent
* the state of the getfire function. Each state may associate with a MetroII event and 
* these events are supposed to be used as the interface interacting with outside. 
* The StartOrResumable interface has to be implemented by subclass as the triggering 
* function of the FSM, in which the transitions are going to be defined.
* 
* @author Liangpeng Guo
* @version $Id$
* @since Ptolemy II 9.1
* @Pt.ProposedRating Red (glp)
* @Pt.AcceptedRating Red (glp)
*
*/
public abstract class FireMachine implements StartOrResumable {

    /** Fire state
     */
    public enum State {
        START, BEGIN, PROCESS, END, FINAL
    }
    
    /**
     * Construct an FireMachine wrapper and initialize the MetroII events
     * 
     * @param actor the actor whose getfire is to be wrapped.
     */
    public FireMachine(Actor actor) {
        _actor = actor;

        String actorName = _actor.getFullName();
        String actorNameWithoutModelName = _trimModelName(actorName);

        _BeginEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "FIRE_BEGIN", actorName);
        _ProcessEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "PROCESS", actorName);
        _EndEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "FIRE_END", actorName);
        
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the MetroII event associated with the current state. 
     * 
     * @return the MetroII event associated with the current state.
     */
    public Builder getStateEvent() {
        switch (getState()) {
        case BEGIN: 
            return _BeginEvent; 
        case PROCESS:
            return _ProcessEvent;
        case END:
            return _EndEvent;
        default:
            assert false; 
            return null; 
        }
    }

    /**
     * Get the state of the wrapped actor.
     * 
     * @return the state.
     * @see #setState(State)
     */
    public State getState() {
        return _state; 
    }

    
    /**
     * Reset the state to be PREFIRE_BEGIN.
     */
    @Override
    public void reset() {
        setState(State.START);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /**
     * Return the wrapped actor.
     * 
     * @return the wrapped actor.
     */
    protected Actor actor() {
        return _actor;
    }
    
    /**
     * Return the MetroII event associated with the current state and 
     * set the state of the event to be PROPOSED.
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
     * @param state
     * @see #getState()
     */
    protected void setState(State state) {
        _state = state; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private String _trimModelName(String name) {
        assert name.length()>1; 
        int pos = name.indexOf(".", 1); 
        return name.substring(pos); 
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////


    /**
     * Fire begin event
     */
    final private Builder _BeginEvent;
    
    /**
     * Processing fire event
     */
    final private Builder _ProcessEvent;
    
    /**
     * Fire end event
     */
    final private Builder _EndEvent;

    /**
     * The wrapped actor
     */
    private Actor _actor;

    /**
     * The state of the fire function
     */
    private State _state; 
}
