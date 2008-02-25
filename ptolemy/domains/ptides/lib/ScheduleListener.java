package ptolemy.domains.ptides.lib;

import java.util.Hashtable;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ScheduleListener

/**
 Interface for listeners that receive schedule messages.

 @author  Johan Eker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (johane)
 @Pt.AcceptedRating Red (johane)
 @see ptolemy.kernel.util.NamedObj
 */
public interface ScheduleListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the given scheduling event.
     *  @param actorName The name of the actor involved in the event.
     *  @param time The time of the event.
     *  @param scheduleEvent One of {@link #RESET_DISPLAY},
     *  {@link #TASK_SLEEPING}, {@link #TASK_BLOCKED} or {@link #TASK_RUNNING}.
     */
    public void event(Actor node, Actor actor, double time, int scheduleEvent);
    
    public void initialize(Hashtable nodesActors);

    /** Reset display message. */
    static final int START = 0;

    /** Task sleeping message. */
    static final int STOP = 1;
    
    static final int TRANSFEROUTPUT = 2;
    
    static final int TRANSFERINPUT = 3;
    
    static final int MISSEDEXECUTION = 4;

}
