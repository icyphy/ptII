/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2007-2014 The Regents of the University of California.
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
 */
package doc.tutorial.domains;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** Fire actors in left-to-right order. This director is a simple
 *  illustration of how to construct schedules for firing. It examines
 *  the location of the actor in a Vergil window, and on each
 *  invocation of the fire() method, fires each actor once,
 *  starting with the leftmost one and ending with the rightmost one.
 *  If two actors have the same horizontal position, then the order
 *  of their firings is arbitrary.
 *  <p>
 *  Note that this director will fire the actors forever. It may
 *  be difficult to stop the model executing.
 *  @author Edward A. Lee
 */
public class LeftRightDirector extends StaticSchedulingDirector {

    /** Constructor. A director is an Attribute.
     *  @param container The container for the director.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the container cannot contain
     *   this director.
     *  @exception NameDuplicationException If the container already contains an
     *   Attribute with this name.
     */
    public LeftRightDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Set the scheduler.
        setScheduler(new LeftRightScheduler(this, "LeftRightScheduler"));

        iterations = new Parameter(this, "iterations");
        iterations.setExpression("1");
    }

    /** Parameter specifying the number of iterations.
     *  If the value is 0 or less, then the model does not stop
     *  executing on its own.
     *  This is an int that defaults to 1.
     */
    public Parameter iterations;

    /** Override to initialize the iteration count. */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    /** Override to check the number of iterations. */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _iterationCount++;
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        if (iterationsValue > 0 && _iterationCount >= iterationsValue) {
            return false;
        }
        return result;
    }

    /** Count of the number of iterations. */
    private int _iterationCount;

    /** Inner class defining the scheduler.
     */
    public static class LeftRightScheduler extends Scheduler {

        /** Constructor. A Scheduler is an Attribute,
         *  normally contained by a director.
         *  @param director The director that will use this scheduler.
         *  @param name The name of the scheduler.
         *  @exception IllegalActionException If the director cannot use
         *   this scheduler.
         *  @exception NameDuplicationException If the director already
         *   contains an Attribute with this name.
         */
        public LeftRightScheduler(LeftRightDirector director, String name)
                throws IllegalActionException, NameDuplicationException {
            super(director, name);
        }

        /** Return a left-to-right schedule. */
        @Override
        protected Schedule _getSchedule() throws IllegalActionException,
        NotSchedulableException {
            // Get the director.
            NamedObj director = getContainer();
            // Get the container of the director.
            CompositeActor compositeActor = (CompositeActor) director
                    .getContainer();
            // Get the actors to be fired by the director.
            List<Actor> actors = compositeActor.deepEntityList();
            // Create a sorted list of actors, sorted by
            // a specialized comparator.
            TreeSet<Actor> sortedActors = new TreeSet(new LeftRightComparator());
            sortedActors.addAll(actors);
            // Construct a Schedule from the sorted list.
            Schedule schedule = new Schedule();
            for (Actor actor : sortedActors) {
                Firing firing = new Firing(actor);
                schedule.add(firing);
            }
            return schedule;
        }

        /** Inner class that implements a specialized comparator
         *  that compares the horizontal positions of the two
         *  arguments, which are assumed to actors.
         */
        public static class LeftRightComparator implements Comparator {
            /** Compare  the horizontal positions of the two
             *  arguments, which are assumed to actors.
             *  @param o1 The first Object, which is assumed to be an
             *  instance of Locatable
             *  @param o2 The first Object, which is assumed to be an
             *  instance of Locatable
             *  @return -1 if the x value of the location of the o1 object
             *  is less than x value of the location of the o2 object,
             *  0 if the x values are the same, 1 if the x value of the
             *  location of th o1 object is greater than the x value
             *  of the o2 object.
             */
            @Override
            public int compare(Object o1, Object o2) {
                // In case there is no location for an actor,
                // provide a default.
                double[] location1 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                double[] location2 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                // The location of the actor in Vergil is stored in an
                // Attribute that implements the Locatable interface.
                // Get a list of all such attributes, and use the first one
                // (normally there will be only one).
                List locations = ((Entity) o1).attributeList(Locatable.class);
                if (locations.size() > 0) {
                    location1 = ((Locatable) locations.get(0)).getLocation();
                }
                locations = ((Entity) o2).attributeList(Locatable.class);
                if (locations.size() > 0) {
                    location2 = ((Locatable) locations.get(0)).getLocation();
                }
                if (location1[0] < location2[0]) {
                    return -1;
                } else if (location1[0] > location2[0]) {
                    return 1;
                } else {
                    // NOTE: It is not correct to return 0 if the x
                    // locations are the same because the actors may
                    // not be the same actor. A comparator has to be
                    // consistent with equals. We arbitrarily return -1,
                    // unless they are equal.
                    if (o1.equals(o2)) {
                        return 0;
                    }
                    return -1;
                }
            }
        }
    }
}
