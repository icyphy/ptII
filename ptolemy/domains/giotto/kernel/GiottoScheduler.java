/* The scheduler for the Giotto domain.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Yellow (cm@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
//import ptolemy.actor.sched.Scheduler;
//import ptolemy.actor.sched.NotSchedulableException;
//import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Comparator;

//////////////////////////////////////////////////////////////////////////
//// GiottoScheduler
/**
This class generates schedules for the actors in a CompositeActor
according to the Giotto semantics.
<p>
A schedule is represented by a tree. Consider the following CompositeActor:
<pre>
              +-----------------------+
              |           A           |
              +-----------------------+
              +-----------------------+
              |           B           |
              +-----------------------+
              +---------+   +---------+
              |    C    |   |    C    |
              +---------+   +---------+
</pre>
There are three actors A, B, and C, where C runs twice as often as A and B.
The tree representing the schedule for this CompositeActor looks as follows:
<pre>
+-------+               +-------+
| | | ----------------->| | |nil|
+-|-----+               +-|-----+
  |                       |
  V                       V
+-------+  +-------+    +-------+  +-------+  +-------+  +-------+
| | | ---->| | |nil|    | | | ---->|nil| ---->| | | ---->|nil|nil|
+-|-----+  +-|-----+    +-|-----+  +-------+  +-|-----+  +-------+
  |          |            |                     |
  V          V            V                     V
+---+      +---+        +-------+             +-------+
| A |      | B |        | | |nil|             | | |nil|
+---+      +---+        +-|-----+             +-|-----+
                          |                     |
                          V                     V
                        +---+                 +---+
                        | C |                 | C |
                        +---+                 +---+
</pre>
Note that repeated parts of the tree are shared, no deep cloning!

@author Christoph Meyer Kirsch
@version $Id$
*/
public class GiottoScheduler extends Scheduler {

    /** Construct a Giotto scheduler with no container (director)
     *  in the default workspace.
     */
    public GiottoScheduler() {
        super();
    }

    /** Construct a Giotto scheduler in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public GiottoScheduler(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the frequency of the given actor. If the actor has a
     *  <I>frequency</I> parameter with a valid integer value, return
     *  that value. For actors without a <I>frequency</I> parameter,
     *  their frequency is DEFAULT_GIOTTO_FREQUENCY.
     *  @param actor An actor.
     *  @return The frequency of the actor.
     */
    public static int getFrequency(Actor actor) {
        try {
            Parameter parameter = (Parameter)
                ((NamedObj) actor).getAttribute("frequency");

            if (parameter != null) {
                IntToken intToken = (IntToken) parameter.getToken();

                return intToken.intValue();
            } else
                return DEFAULT_GIOTTO_FREQUENCY;
        } catch (ClassCastException ex) {
            return DEFAULT_GIOTTO_FREQUENCY;
        } catch (IllegalActionException ex) {
            return DEFAULT_GIOTTO_FREQUENCY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The default Giotto frequency. Actors without a <I>frequency</I>
     *  parameter will execute with this frequency.
     */
    public static int DEFAULT_GIOTTO_FREQUENCY = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.
     *  Overrides _schedule() method in the base class.
     *
     *  This method should not be called directly, rather the schedule()
     *  method will call it when the schedule is invalid. So it is not
     *  synchronized on the workspace.
     *
     *  @return An enumeration of the scheduling sequence.
     *  @exception NotSchedulableException If the fiCompositeActor is not
     *   schedulable.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector director =
            (StaticSchedulingDirector) getContainer();

        CompositeActor compositeActor =
	    (CompositeActor) (director.getContainer());

        List actorList = compositeActor.deepEntityList();

	/* Sort all actors according to their frequency.
	   Small frequency value means earlier in the list.
	   Sort keeps order of actors with same frequency value. */
	Collections.sort(actorList, new GiottoActorComparator());

	// Compute schedule

	if (actorList.isEmpty())
	    return null;
	else {
	    // Get first actor's frequency.
	    // Assumption: It's the lowest frequency in actorList.

	    Actor actor = (Actor) actorList.get(0);

	    int frequency = getFrequency(actor);

            // Compute schedule represented by a tree.
	    Schedule schedule = _treeSchedule(actorList.listIterator(),
					      DEFAULT_GIOTTO_FREQUENCY,
					      frequency);

            /* instead of return a Enumeration, we return a Schedule

	    // Return a shallow enumeration over the top list.
	    return Collections.enumeration(scheduleList);
            */

            return schedule;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a schedule for a CompositeActor represented by a tree,
     *  see comment at top of file.
     *
     *  @param iterator over all actors of CompositeActor.
     *  @param lastFrequency of the previous call to this method.
     *  @param frequency of the first actor in iterator.
     *  @return a shallow Enumeration of the scheduling tree.
     *  @exception NotSchedulableException If the CompositeActor is not
     *   schedulable.
     */


    private Schedule _treeSchedule(ListIterator iterator,
			       int lastFrequency, int frequency)
	throws NotSchedulableException {
	// This schedule contains all firings made of actor with frequency 'frequency'.
	Schedule sameFrequencySchedule = new Schedule();

	// This schedule is actually a tree.
	// It contains all 'sameFrequencyList' lists with strictly
	// higher frequencies than 'frequency'.

	Schedule higherFrequencySchedule = new Schedule();

	while (iterator.hasNext()) {
	    Actor actor = (Actor) (iterator.next());

	    int actorFrequency = getFrequency(actor);

	    if (actorFrequency == frequency) {
                Firing firing = new Firing();
                firing.setActor(actor);
                sameFrequencySchedule.add(firing);
            }
	    else if (actorFrequency > frequency) {
		// We reached the first actor with a strictly higher frequency
		// than all actors before. Prepare for recursive call.

		// Makes sure that current actor will be read again.
		Actor dummy = (Actor) (iterator.previous());

		// Recursive call where 'lastFrequency' becomes current
                // 'frequency'
		// and 'frequency' becomes the frequency of the current actor.
		higherFrequencySchedule =
                    _treeSchedule(iterator, frequency, actorFrequency);

		// Redundant break because recursive call
		// completely iterates iterator.

		break;
	    } else
		throw new NotSchedulableException(
						  "Sorting frequencies failed!");
	}

	// This is actually a tree.
	// It will be the result of this method.
	Schedule scheduleSchedule = new Schedule();

	// Assumption: frequency >= lastFrequency
	if ((frequency%lastFrequency) == 0) {
	    int currentFrequency = frequency / lastFrequency;

	    // Length of scheduleList will be even!

	    for (int i = 1; i <= currentFrequency; i++) {
		scheduleSchedule.add(sameFrequencySchedule);
                scheduleSchedule.add(higherFrequencySchedule);
	    }
	} else
	    throw new NotSchedulableException("Frequencies not harmonic!");

	return scheduleSchedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// GiottoActorComparator
    /* This class implements the Comparator interface for actors
       based on the <I>frequency</I> parameter of the actors.
       The frequency of an actor which does not have a <I>frequency</I>
       parameter is DEFAULT_GIOTTO_FREQUENCY.
       Given two actors A1 and A2, compare(A1, A2) is -1 (A1 < A2) if A1's
       frequency is strictly less than A2's frequency, or compare(A1, A2) is 0
       (A1 == A2) if A1's frequency is equal to A2's frequency, or
       compare(A1, A2) is 1 (A1 > A2) if A1's frequency is strictly greater
       than A2's frequency.
    */

    private class GiottoActorComparator implements Comparator {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/** Compare two actors based on their <I>frequency</I> parameter.
	 *  The frequency of an actor that does not have a <I>frequency</I>
         *  parameter is DEFAULT_GIOTTO_FREQUENCY.
	 *
	 *  @param actor1 The first actor to be compared.
	 *  @param actor2 The second actor to be compared.
	 *  @return -1 if the frequency of the first actor is strictly less
	 *   than that of the second actor, 0 if the frequencies are equal,
	 *   1 otherwise.
	 *  @exception ClassCastException If an argument is null or not an
	 *   instance of Actor.
	 */
	public int compare(Object actor1, Object actor2) {
	    if (actor1 != null && actor1 instanceof Actor &&
                actor2 != null && actor2 instanceof Actor) {

		if (getFrequency((Actor)actor1)
		        < getFrequency((Actor)actor2))
		    return -1;
		else if (getFrequency((Actor)actor1)
			== getFrequency((Actor)actor2))
		    return 0;
		else
		    return 1;
	    } else
		throw new ClassCastException();
	}
    }

}
