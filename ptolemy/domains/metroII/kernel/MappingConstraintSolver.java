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
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MappingConstraintSolver implements ConstraintSolver {

    /** Construct a mapping constraint solver of given size.
     *
     *  @param size The maximum number of allowed events.
     */
    public MappingConstraintSolver(int size) {
        _mapping = new int[size][size];
        _size = size;
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                _mapping[i][j] = 0;
            }
        }
        _currentMAXID = 0;
    }

    /** Return the adjacency matrix of mapping constraints as a
     *  string.
     *  @return the adjacency matrix.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i <= _currentMAXID; i++) {
            for (int j = 0; j <= _currentMAXID; j++) {
                result.append(" " + _mapping[i][j]);
            }
            result.append("\n");
        }
        return result.toString();
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

        // Step 2: present all the proposed events to the event solver.
        for (Event.Builder eventBuilder : metroIIEventList) {
            String eventName = eventBuilder.getName();
            if (!_eventName2ID.containsKey(eventName)) {
                _eventName2ID.put(eventName, _nextAvailableID);
                _nextAvailableID++;
            }
            eventBuilder.setStatus(Event.Status.WAITING);
            presentMetroIIEvent(_eventName2ID.get(eventName));
        }

        // Step 3: update the statuses of all events. 
        for (Event.Builder eventBuilder : metroIIEventList) {
            String eventName = eventBuilder.getName();
            if (isSatisfied(_eventName2ID.get(eventName))) {
                eventBuilder.setStatus(Event.Status.NOTIFIED);
            }
        }

    }

    /** Mark each event on the adjacency matrix of mapping
     * constraints.
     *
     * @param id Event ID that is PROPOSED or WAITING
     */
    public void presentMetroIIEvent(int id) {
        // System.out.print("present M2Event: ");
        // System.out.println(id);
        assert id < _size;
        assert id > 0;
        if (id > _size) {
            return;
        }
        for (int i = 0; i < _size; i++) {
            if (_mapping[id][i] > 0) {
                _mapping[id][i]++;
            }
        }
    }

    /** Check if the input event satisfies all the mapping constraints
     *
     * @param id Event ID
     */
    public boolean isSatisfied(int id) {
        // System.out.print("check M2Event: ");
        // System.out.println(id);
        assert id > 0;
        if (id > _size) {
            return true;
        }
        for (int i = 0; i < _size; i++) {
            if (_mapping[id][i] == 1 || _mapping[i][id] == 1) {
                return false;
            }
        }
        return true;
    }

    /** Initialize the constraint solver. */
    public void reset() {
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                if (_mapping[i][j] > 0) {
                    _mapping[i][j] = 1;
                }
            }
        }
    }

    /** Add a mapping constraint (A, B).
     *
     * @param id1 Event A in the constraint
     * @param id2 Event B in the constraint
     */
    public void add(int id1, int id2) {
        _mapping[id1][id2] = 1;
        _mapping[id2][id1] = 1;

        if (id1 > _currentMAXID) {
            _currentMAXID = id1;
        }
        if (id2 > _currentMAXID) {
            _currentMAXID = id2;
        }
    }

    /**
     * Read mapping constraints from a file
     * @param filename Filename of the mapping constraint file. 
     * @throws IOException
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
                if (!_eventName2ID.containsKey(actorNames[0])) {
                    _eventName2ID.put(actorNames[0], _nextAvailableID);
                    _nextAvailableID++;
                }
                if (!_eventName2ID.containsKey(actorNames[1])) {
                    _eventName2ID.put(actorNames[1], _nextAvailableID);
                    _nextAvailableID++;
                }
                add(_eventName2ID.get(actorNames[0]),
                        _eventName2ID.get(actorNames[1]));
            }
        } finally {
            reader.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /** The adjacency matrix that represents the mapping constraints
     *  (event pairs).
     */
    private int _mapping[][];

    /** The maximum number of allowed events. */
    private int _size;

    /** The largest event ID.  */
    private int _currentMAXID;

    /** The next available event ID. If an new event is proposed, the
     *  _nextAvailableID is assigned to the new event and
     *  _nextAvailableID is increased by one.
     */
    private int _nextAvailableID = 0;

    /** The dictionary of event name and ID pair. 
     * 
     */
    private Hashtable<String, Integer> _eventName2ID = new Hashtable<String, Integer>();
}
