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

/** <p> The constraint solver is used to enforce the user defined
 *  constraints on the scheduling via updating the event status. The
 *  mapping constraint solver is used to update the event status based
 *  on mapping constraints. The mapping constraint is a type of
 *  rendezvous constraint. Each mapping constraint is a event pair,
 *  which requires the events are scheduled at the same time. More
 *  precisely, the mapping constraint is satisfied when both events
 *  are in presence. An event status is updated to
 *  NOTIFIED when it satisfies all the constraints. Otherwise the
 *  event status is updated to WAITING. </p>
 *
 *  <p>
 *  The mapping constraint resolution has three steps:</p>
 *  <ol>
 *  <li> Step 1: reset() is called to initialize the solver. </li>
 *  <li> Step 2: presentMetroIIEvent(event id) is called for each
 *  PROPOSED or WAITING event. </li>
 *  <li> Step 3: isSatisfied(event id) is called for each event. It
 *  returns true if the event satisfies all the mapping
 *  constraints. </li>
 *  </ol>
 *  </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MappingConstraintSolver implements ConstraintSolver {

    /** Construct a mapping constraint solver.
     */
    public MappingConstraintSolver() {
    }

    /** Return the adjacency matrix of mapping constraints as a 
     *  string.
     *  @return the adjacency matrix.
     */
    public String toString() {
        return _counter.toString();
    }

    public boolean debugging() {
        return _debugging;
    }

    public void turnOnDebugging() {
        _debugging = true;
    }

    public void turnOffDebugging() {
        _debugging = false;
    }

    /**
     * Resolve the MetroII event list, updating the event status based
     * on the mapping constraints. The mapping constraint is a type of
     * rendezvous constraint. Each mapping constraint is a event pair,
     * which requires the events are scheduled at the same time. More
     * precisely, the mapping constraint is satisfied when both events
     * are in presence. An event status is updated to
     * NOTIFIED when it satisfies all the constraints. Otherwise the
     * event status is updated to WAITING.
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
                } else if (event.getType() == Event.Type.DEFAULT_WAITING){
                    event.setStatus(Event.Status.WAITING);
                }
                else {
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

                    System.out.println("Notifying " + e1.getName() + " "
                            + e2.getName());

                    Iterable<Integer> edges1 = _mapping.getEdges(eventId1);
                    Iterable<Integer> edges2 = _mapping.getEdges(eventId2);
                    _counter.decreaseCount(edges1);
                    _counter.decreaseCount(edges2);
                }
            }
            // System.out.println(_counter); 
        }
    }

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
     * Read mapping constraints from a file
     * @param filename Filename of the mapping constraint file.
     * @exception IOException
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

    private class ConstraintCounter {

        public ConstraintCounter(int size) {
            _size = size;
            _count = new int[_size];
            initialize();
        }

        public String toString() {
            return Arrays.toString(_count);
        }

        public int firstGreaterThanOne(Iterable<Integer> ids) {
            for (Integer id : ids) {
                if (_count[id] > 1) {
                    return id;
                }
            }
            return -1;
        }

        public void initialize() {
            for (int i = 0; i < _size; i++) {
                _count[i] = 0;
            }
        }

        public void increaseCount(Iterable<Integer> ids) {
            for (Integer id : ids) {
                _count[id]++;
            }
        }

        public void decreaseCount(Iterable<Integer> ids) {
            for (Integer id : ids) {
                _count[id]--;
            }
        }

        private int _size;

        private int[] _count;
    }

    private boolean _debugging = false;

    private ConstraintCounter _counter;

    private Graph _mapping = new Graph();

    private EventDictionary _eventIDDictionary = new EventDictionary();
}
