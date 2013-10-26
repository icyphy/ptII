/* Constraint Solver for Modified MetroII semantics.

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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

///////////////////////////////////////////////////////////////////
//// MappingConstraintSolver

/**
 * The constraint solver is used to enforce the user defined constraints on the
 * scheduling via updating the event status. The mapping constraint solver is
 * used to update the event status based on mapping constraints. The mapping
 * constraint is a type of rendezvous constraint. Each mapping constraint is a
 * event pair, which requires the events are scheduled at the same time. More
 * precisely, the mapping constraint is satisfied when both events are in
 * presence. An event status is updated to NOTIFIED when it satisfies all the
 * constraints. Otherwise the event status is updated to WAITING. The mapping
 * constraint resolution has three steps:
 * <ol>
 * <li>Step 1: reset() is called to initialize the solver.</li>
 * <li>Step 2: presentMetroIIEvent(event id) is called for each PROPOSED or
 * WAITING event.</li>
 * <li>Step 3: isSatisfied(event id) is called for each event. It returns true
 * if the event satisfies all the mapping constraints.</li>
 * </ol>
 *
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MappingConstraintSolver implements ConstraintSolver, Cloneable {

    /**
     * Construct a mapping constraint solver.
     */
    public MappingConstraintSolver() {
    }

    /**
     * Clone MappingConstraintSolver.
     *
     * @exception CloneNotSupportedException
     *             the object's class does not implement the Cloneable
     *             interface.
     */
    @Override
    public MappingConstraintSolver clone() throws CloneNotSupportedException {
        MappingConstraintSolver newObject = (MappingConstraintSolver) super
                .clone();
        // FIXME: I'm not sure if we want to call clone like this.  Typically, we
        // would just instantiate new versions of the fields.
        if (_counter == null) {
            newObject._counter = null;
        } else {
            newObject._counter = (ConstraintCounter) _counter.clone();
        }
        newObject._mapping = (Graph) _mapping.clone();
        newObject._eventIDDictionary = (EventDictionary) _eventIDDictionary
                .clone();
        return newObject;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the adjacency matrix of mapping constraints as a string.
     *
     * @return the adjacency matrix.
     */
    public String toString() {
        return _counter.toString();
    }

    /**
     * Check if the debugging option is checked.
     *
     * @return the state of debugging option
     */
    public boolean debugging() {
        return _debugging;
    }

    /**
     * Turn on debugging option.
     */
    public void turnOnDebugging() {
        _debugging = true;
    }

    /**
     * Turn off debugging option.
     */
    public void turnOffDebugging() {
        _debugging = false;
    }

    /**
     * Resolve the MetroII event list, updating the event status based on the
     * mapping constraints. The mapping constraint is a type of rendezvous
     * constraint. Each mapping constraint is a event pair, which requires the
     * events are scheduled at the same time. More precisely, the mapping
     * constraint is satisfied when both events are in presence. An event status
     * is updated to NOTIFIED when it satisfies all the constraints. Otherwise
     * the event status is updated to WAITING.
     */
    @Override
    public void resolve(Iterable<Event.Builder> metroIIEventList) {
        // The constraints are resolved in three steps.
        // STEP 1: reset the constraint solver.
        reset();

        Hashtable<Integer, Event.Builder> id2event = new Hashtable<Integer, Event.Builder>();
        // Step 2: present all the proposed events to the event solver.
        for (Event.Builder event : metroIIEventList) {
            String eventName = event.getName();
            int nodeId = _eventIDDictionary.getID(eventName);
            if (nodeId < 0) {
                if (event.getType() == Event.Type.DEFAULT_NOTIFIED) {
                    event.setStatus(Event.Status.PROPOSED);
                } else if (event.getType() == Event.Type.DEFAULT_WAITING) {
                    event.setStatus(Event.Status.WAITING);
                } else {
                    assert false;
                }
            } else {
                id2event.put(nodeId, event);
                Iterable<Integer> edges = _mapping.getEdges(nodeId);
                _counter.increaseCount(edges);
                int firstConstraintId = _counter.firstGreaterThanOne(edges);
                if (firstConstraintId < 0) {
                    event.setStatus(Event.Status.WAITING);
                } else {
                    Pair<Integer, Integer> idPair = _mapping
                            .getEdge(firstConstraintId);
                    int eventId1 = idPair.getFirst();
                    int eventId2 = idPair.getSecond();
                    Event.Builder e1 = id2event.get(eventId1);
                    Event.Builder e2 = id2event.get(eventId2);
                    e1.setStatus(Event.Status.PROPOSED);
                    e2.setStatus(Event.Status.PROPOSED);

                    assert !e1.hasTime() && !e2.hasTime();

                    //                    if (e1.hasTime() && !e2.hasTime()) {
                    //                        assert false;  //e2.setTime(e1.getTime());
                    //                    }
                    //                    else if (!e1.hasTime() && e2.hasTime()) {
                    //                        assert false;  //e1.setTime(e2.getTime());
                    //                    }
                    //                    else if (e1.hasTime() && e2.hasTime() && e1.getTime() != e2.getTime()) {
                    //                        assert false;
                    //                    }

//                    System.out.println("Notifying " + e1.getName() + " "
//                            + e2.getName());

                    Iterable<Integer> edges1 = _mapping.getEdges(eventId1);
                    Iterable<Integer> edges2 = _mapping.getEdges(eventId2);
                    _counter.decreaseCount(edges1);
                    _counter.decreaseCount(edges2);
                }
            }
            // System.out.println(_counter);
        }
    }

    /**
     * Return the number of mapping constraints.
     *
     * @return the number of mapping constraints.
     */
    public int numConstraints() {
        return _mapping.edgeSize();
    }

    /**
     * Initialize the constraint solver.
     **/
    public void reset() {
        _counter = new ConstraintCounter(numConstraints());
    }

    /**
     * Read mapping constraints from a file.
     *
     * @param filename
     *            Filename of the mapping constraint file.
     * @exception IOException
     *                a failed or interrupted I/O operations has occurred.
     */
    public void readMapping(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] actorNames = line.split(",");
                assert actorNames.length == 2;
                addMapping(actorNames[0], actorNames[1]);
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Add a mapping constraint.
     *
     * @param eventName1
     *            first event in the mapping.
     * @param eventName2
     *            second event in the mapping.
     */
    public void addMapping(String eventName1, String eventName2) {
        _eventIDDictionary.add(eventName1);
        _eventIDDictionary.add(eventName2);
        int id1 = _eventIDDictionary.getID(eventName1);
        int id2 = _eventIDDictionary.getID(eventName2);
        // System.out.println(id1+" "+id2);
        _mapping.add(id1, id2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * ConstraintCounter maintains a counter for each constraint. When
     * increaseCount(Iterable<Integer> ids) is called, the counter of the
     * constraint whose id is in ids is increased by the number of appearances
     * in ids.
     *
     * @author glp
     *
     */
    private static class ConstraintCounter implements Cloneable {

        /**
         * Construct and initialize the counter for each constraint.
         *
         * @param size
         *            the largest possible id of the constraints + 1.
         */
        public ConstraintCounter(int size) {
            _size = size;
            _count = new int[_size];
            reset();
        }

        /**
         * Clone the ConstraintCounter
         */
        public ConstraintCounter clone() throws CloneNotSupportedException {
            ConstraintCounter newObject = (ConstraintCounter) super.clone();
            newObject._count = (int[]) _count.clone();
            return newObject;
        }

        /**
         * Convert the ConstraintCounter to string
         */
        public String toString() {
            return Arrays.toString(_count);
        }

        /**
         * return the first id in ids whose counter is greater than 1.
         *
         * @param ids
         *            a vector of ids
         * @return the first id in ids whose counter is greater than 1.
         */
        public int firstGreaterThanOne(Iterable<Integer> ids) {
            for (Integer id : ids) {
                if (_count[id] > 1) {
                    return id;
                }
            }
            return -1;
        }

        /**
         * Reset the counters
         */
        public void reset() {
            for (int i = 0; i < _size; i++) {
                _count[i] = 0;
            }
        }

        /**
         * The counter of the constraint whose id is in ids is increased by the
         * number of appearances in ids.
         *
         * @param ids
         *            the vector of ids
         */
        public void increaseCount(Iterable<Integer> ids) {
            for (Integer id : ids) {
                _count[id]++;
            }
        }

        /**
         * The counter of the constraint whose id is in ids is decreased by the
         * number of appearances in ids.
         *
         * @param ids
         *            the vector of ids
         */
        public void decreaseCount(Iterable<Integer> ids) {
            for (Integer id : ids) {
                _count[id]--;
            }
        }

        /**
         * The largest possible id + 1
         */
        private int _size;

        /**
         * the counters
         */
        private int[] _count;
    }

    /**
     * Whether in debugging mode or not
     */
    private boolean _debugging = false;

    /**
     * A constraint counter
     */
    private ConstraintCounter _counter;

    /**
     * Mapping constraints (stored in a graph)
     */
    private Graph _mapping = new Graph();

    /**
     * A dictionary that maps an event to its id.
     */
    private EventDictionary _eventIDDictionary = new EventDictionary();
}
