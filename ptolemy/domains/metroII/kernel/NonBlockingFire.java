/* NonBlockingFire is a wrapper for Ptolemy actor.

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
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

/**
 * NonBlockingFire is a wrapper for Ptolemy actor. It provides an implementation of FireMachine. 
 * More specifically, the wrapper implements a startOrResume() function 
 * that associates the state of FireMachine with the state of fire() of the wrapped actor as follows: 
 * <ol>
 * <li> START: initial state </li>
 * <li> FINAL: final state </li>
 * </ol>
 * When startOrResume() is called, if the current state is START, the wrapper calls fire() of the wrapped actor, 
 * and transition to FINAL
 *   
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class NonBlockingFire extends FireMachine {

    /**
     * Construct a NonBlocingfire by wrapping the actor.
     * 
     * @param actor actor to be wrapped.
     */
    public NonBlockingFire(Actor actor) {
        super(actor);
    }

    /**
     * if the current state is START, call fire() of the wrapped actor, 
     * and transition to FINAL.
     * 
     * @param metroIIEventList the list of MetroII events that trigger 
     * startOrResume().
     * @throws IllegalActionException if the associated action (e.g. firing) 
     * is not permitted.
     */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (getState() == State.START) {
            actor().fire();
            setState(State.FINAL);
        } 
        else {
            assert false; 
        }
    }
    
}
