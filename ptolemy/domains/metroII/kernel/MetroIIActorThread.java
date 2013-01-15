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

import net.jimblackler.Utils.YieldAdapterIterator;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

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
public class MetroIIActorThread {
    /** Actor type
     */
    public enum Type {
        Ptolemy, Metropolis
    }

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
    public MetroIIActorThread(Actor actor, Type type, State state,
            YieldAdapterIterator<Iterable<Event.Builder>> thread) {
        this.actor = actor;
        this.type = type;
        this.state = state;
        this.thread = thread;
    }

    /** Actor which is being fired 
     * 
     */
    public Actor actor;
    
    /**
     * Type of the actor
     */
    public Type type;
    
    /**
     * State of the thread
     */
    public State state;
    
    /**
     * Thread that is firing the actor
     */
    public YieldAdapterIterator<Iterable<Event.Builder>> thread;
}
