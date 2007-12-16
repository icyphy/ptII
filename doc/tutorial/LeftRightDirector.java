package doc.tutorial;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;

public class LeftRightDirector extends StaticSchedulingDirector {

    public LeftRightDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setScheduler(new LeftRightScheduler(this, "LeftRightScheduler"));
    }

    public static class LeftRightScheduler extends Scheduler {

        public LeftRightScheduler(LeftRightDirector director, String name)
                throws IllegalActionException, NameDuplicationException {
            super(director, name);
        }

        protected Schedule _getSchedule() throws IllegalActionException,
                NotSchedulableException {
            StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();
            CompositeActor compositeActor = (CompositeActor) (director
                    .getContainer());
            List actors = compositeActor.deepEntityList();
            Iterator actorIterator = actors.iterator();
            TreeSet sortedActors = new TreeSet(new LeftRightComparator());
            while (actorIterator.hasNext()) {
                Actor actor = (Actor) actorIterator.next();
                sortedActors.add(actor);
            }
            Schedule schedule = new Schedule();
            Iterator sortedActorsIterator = sortedActors.iterator();
            while (sortedActorsIterator.hasNext()) {
                Actor actor = (Actor) sortedActorsIterator.next();
                Firing firing = new Firing();
                firing.setActor(actor);
                schedule.add(firing);
            }

            return schedule;
        }

        public /*static*/ class LeftRightComparator implements Comparator {
            // FindBugs suggests making this class static so as to decrease
            // the size of instances and avoid dangling references.
            // However, hashCode() calls attributeList() on this,
            // so this class cannot be static.

            public int compare(Object o1, Object o2) {
                double[] location1 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                double[] location2 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };

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
                    return 0;
                }
            }

            public boolean equals(Object o) {
                if (compare(this, o) == 0) {
                    return true;
                } else {
                    return false;
                }
            }

            public int hashCode() {
                // Findbugs says if we have an equals(), we need a hashCode().
                double[] location1 = { Double.NEGATIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
                List locations = attributeList(Locatable.class);
                if (locations.size() > 0) {
                    location1 = ((Locatable) locations.get(0)).getLocation();
                }
                // Return the bitwise xor
                return (int) location1[0]  ^ (int) location1[1];
            }
        }
    }
}
