/* The scheduler for the Giotto domain.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.giotto.kernel;

import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

////GiottoScheduler

/**
 This class generates schedules for the actors in a CompositeActor
 according to the Giotto semantics.
 <p>
 A schedule is represented by a list. Consider the following CompositeActor:
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
 The list representing the schedule for this CompositeActor looks as follows:
 <pre>
 +-------+                         +-------+
 | | | --------------------------->| | |nil|
 +-|-----+                         +-|-----+
 |                                 |
 V                                 V
 +-------+  +-------+  +-------+   +-------+
 | | | ---->| | | ---->| | |nil|   | | |nil|
 +-|-----+  +-|-----+  +-|-----+   +-|-----+
 |          |          |           |
 V          V          V           V
 +---+      +---+      +---+       +---+
 | A |      | B |      | c |       | c |
 +---+      +---+      +---+       +---+

 </pre>

 @author Christoph Kirsch, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cm)
 @Pt.AcceptedRating Red (eal)
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

    /**
     * Returns the LCM value.
     * @return an int representing the LCM value
     */

    public int getLCM() {
        return _lcm;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The default Giotto frequency. Actors without a <I>frequency</I>
     *  parameter will execute with this frequency.
     */
    protected static final int _DEFAULT_GIOTTO_FREQUENCY = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the unit of time increment for director.
     *  This method will be called by the director. It should not be called
     *  until the director call getSchedule() and the returned schedule is invalid. It is not
     *  synchronized on the workspace.
     *  @param period Giotto model period given in 'period' parameter of director
     *  @return unit of time increment for director.
     */
    protected double _getMinTimeStep(double period) {
        return period / _lcm;
    }

    /** Return the scheduling sequence.
     *  This method should not be called directly; rather the getSchedule()
     *  method will call it when the schedule is invalid. It is not
     *  synchronized on the workspace.
     *  @return A schedule.
     *  @exception NotSchedulableException If the model is not
     *   schedulable.
     */
    @Override
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) director
                .getContainer();
        List actorList = compositeActor.deepEntityList();
        int actorCount = actorList.size();

        if (actorCount < 1) {
            throw new NotSchedulableException(
                    this,
                    "Could not get schedule, "
                            + "the number of deeply contained entities for '"
                            + compositeActor.getFullName()
                            + "' is "
                            + actorCount
                            + ", which is less than 1."
                            + "If you have empty composite actors, try adding an  actor"
                            + "to the inside of one of the empty composite actors.");
        }

        int[] frequencyArray = new int[actorCount];
        int[] intervalArray = new int[actorCount];

        ListIterator actorListIterator = actorList.listIterator();

        int i = 0;

        while (actorListIterator.hasNext()) {
            Actor actor = (Actor) actorListIterator.next();
            int frequency = 1;
            try {
                frequency = GiottoDirector.getActorFrequency((NamedObj) actor,
                        (GiottoDirector) getContainer());
            } catch (IllegalActionException e) {
                throw new NotSchedulableException(actor, "Invalid frequency: "
                        + e.getMessage());
            }

            // if (Arrays.binarySearch(_candidateFrequencies, frequency) >= 0) {
            // this frequency is a good candidate to calculate accurate
            // _unitTimeIncrement for the director.
            frequencyArray[i] = frequency;
            i++;
            /*} else if (frequency > biggestFrequency) {
                throw new NotSchedulableException(
                        this,
                        "The specified frequency "
                                + frequency
                                + " is bigger than the allowed biggest "
                                + "frequency "
                                + biggestFrequency
                                + ". \n Try "
                                + "introducing hierarchies or reducing the period "
                                + "parameter of the director to achieve shorter "
                                + "execution time.");
            } else {
                throw new NotSchedulableException(
                        this,
                        "Cannot assign a frequency "
                                + frequency
                                + " to "
                                + actor.getName()
                                + ", because time cannot be calculated accurately. \n"
                                + " A good frequency will be of 2^m*5^n, where m and n"
                                + " are non-negative integers.");
            }*/
        }

        _lcm = _lcm(frequencyArray);

        if (_debugging) {
            _debug("LCM of frequencies is " + _lcm);
        }

        for (i = 0; i < actorCount; i++) {
            intervalArray[i] = _lcm / frequencyArray[i];
        }

        // Compute schedule
        // based on the frequencyArray and the actorList
        Schedule schedule = new Schedule();

        for (_giottoSchedulerTime = 0; _giottoSchedulerTime < _lcm;) {
            Schedule fireAtSameTimeSchedule = new Schedule();
            actorListIterator = actorList.listIterator();

            for (i = 0; i < actorCount; i++) {
                Actor actor = (Actor) actorListIterator.next();

                if (_giottoSchedulerTime % intervalArray[i] == 0) {
                    fireAtSameTimeSchedule.add(new Firing(actor));
                }
            }

            _giottoSchedulerTime += 1;

            // there may be several null schedule in schedule...
            // and the time step is period / _lcm
            schedule.add(fireAtSameTimeSchedule);
        }

        return schedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private int _lcm(int[] array) {
        int count = array.length;

        if (count < 1) {
            throw new RuntimeException(
                    "Length array passed to _lcm() is less than 1?");
        }

        int X = array[0];
        int i = 0;

        while (true) {
            if (X % array[i] == 0) {
                if (i >= count - 1) {
                    break;
                }

                i++;
            } else {
                X = X + 1;
                i = 0;
            }
        }

        return X;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // This is a list of frequencies that can be used to calculate
    // _unitTimeIncrement accurately.
    //    private static int[] _candidateFrequencies = new int[] { 1, 2, 4, 5, 8, 10,
    //        16, 20, 25, 32, 40, 50, 64, 80, 100, 125, 128, 160, 200, 250, 256,
    //        320, 400, 500, 512, 625, 640, 800, 1000, 1024, 1250, 1280, 1600,
    //        2000, 2048, 2500, 2560, 3125, 3200, 4000, 4096, 5000, 5120, 6250,
    //        6400, 8000, 8192, 10000, 10240, 12500, 12800, 15625, 16000, 16384,
    //        20000, 20480, 25000, 25600, 31250, 32000, 32768, 40000, 40960,
    //        50000, 51200, 62500, 64000, 65536, 78125, 80000, 81920, 100000 };

    private int _giottoSchedulerTime = 0;

    private int _lcm = 1;
}
