/* Static schedule for CT domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.kernel.util.NamedObj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CTSchedule
/**
A schedule for CT models. This class overrides the base class and uses
an ArrayList implementation for the first level of the schedule, so
that accessing subschedules takes constant time. A CT schedule is an
array of LinkedList. The array always has length X, consisting of the
following entries in that order (in a type-safe enumeration),
<pre>
DISCRETE_ACTORS
DYNAMIC_ACTORS
EVENT_GENERATORS
OUTPUT_ACTORS
OUTPUT_STEP_SIZE_CONTROL_ACTORS
STATE_TRANSITION_ACTORS
STATEFUL_ACTORS
STATE_STEP_SIZE_CONTROL_ACTORS
WAVEFORM_GENERATORS
</pre>
Each entry is a Schedule. Actors
in the schedule are ordered according to the order they should be
executed.
<P>
A typical use of the schedule is to get one of the subschedules.
For example, to schedule the CT execution and get the iterator for
dynamic actor schedule,
(assume we have a CTScheduler called scheduler) do:
<pre>
CTSchedule schedule = scheduler.getSchedule();
Iterator dynamicActorIterator =
        schedule.get(CTSchedule.DYNAMIC_ACTOR_SCHEDULE).actorIterator();
</pre>
@author  Jie Liu
@version $Id$
@since Ptolemy II 2.0
*/
public class CTSchedule extends Schedule {

    /** Construct a CTSchedule.
     */
    public CTSchedule() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Index for actors in the discrete part of the system,
     *  topologically ordered.
     */
    public final static int DISCRETE_ACTORS = 0;

    /** Index for actors in the continuous part of the system, unordered.
     */
    public final static int CONTINUOUS_ACTORS = 1;

    /** Index for dynamic actor schedule.
     */
    public final static int DYNAMIC_ACTORS = 2;

    /** Index for actors implement the CTEventGenerator interface.
     */
    public final static int EVENT_GENERATORS = 3;

    /** Index for output schedule.
     */
    public final static int OUTPUT_ACTORS = 4;

    /** Index for actors that are in the output map and implement the
     *  CTStepSizeControlActor.
     */
    public final static int OUTPUT_STEP_SIZE_CONTROL_ACTORS = 5;

    /** Index for state transition schedule.
     */
    public final static int STATE_TRANSITION_ACTORS = 6;

    /** Index for actors that implement the CTStatefulActor interface.
     */
    public final static int STATEFUL_ACTORS = 7;

    /** Index for actors that are in the state transition map and
     *  implement the CTStepSizeControlActor interface.
     */
    public final static int STATE_STEP_SIZE_CONTROL_ACTORS = 8;

    /** Index for actors that implement the CTWaveformGenerator interface.
     */
    public final static int WAVEFORM_GENERATORS = 9;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return all the scheduling information in a Sting.
     *  @return All the schedules.
     */
    public String toString() {
        //_schedule.ensureCapacitance();
        String result = new String();
        Iterator iterator;
        result += "CTSchedule {\n";
        result += "    continuousActors {\n";
        Schedule continuous = (Schedule)get(CONTINUOUS_ACTORS);
        iterator = continuous.actorIterator();
        while (iterator.hasNext()) {
            result = result +
                "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    discreteActors {\n";
        Schedule discrete = (Schedule)get(DISCRETE_ACTORS);
        iterator = discrete.actorIterator();
        while (iterator.hasNext()) {
            result = result +
                "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    dynamicActors {\n";
        iterator = get(DYNAMIC_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    stateStepSizeControlActors {\n";
        iterator = get(STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    outputStepSizeControlActors {\n";
        iterator = get(OUTPUT_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    eventGenerators {\n";
        iterator = get(EVENT_GENERATORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    waveformGenerators {\n";
        iterator = get(WAVEFORM_GENERATORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    statefulActors {\n";
        iterator = get(STATEFUL_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    stateTransitionActors {\n";
        iterator = get(STATE_TRANSITION_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "    outputActors {\n";
        iterator = get(OUTPUT_ACTORS).actorIterator();
        while (iterator.hasNext()) {
            result += "\t" + ((NamedObj)iterator.next()).getFullName() + "\n";
        }
        result += "    }\n";
        result += "}\n";
        return result;
    }
}
