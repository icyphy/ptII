/* A class that estimates a sequential schedule.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ListSchedulingSequenceEstimator

/**
 * A class that estimates a sequential schedule. Order given by already present
 * sequence numbers are preserved.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
public class ListSchedulingSequenceEstimator extends SequenceEstimator {

    /** Construct an estimator for the given director.
     *
     *  @param director The director that needs to guess a schedule.
     */
    public ListSchedulingSequenceEstimator(Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Estimate a sequenced schedule. Take into account ordering constraints
     * given by sequence numbers and data flow.
     *
     * @param independentList The already present SequenceAttributes for the
     * Actors controlled by this scheduler.
     *
     * @return A vector with the ordered actors. Note that the sequence numbers
     * are not changed. This has to be done somewhere else.
     *
     * @exception NotSchedulableException If the underlying graph of the
     * actors is not acyclic.
     */
    @Override
    public Vector<Actor> estimateSequencedSchedule(
            List<SequenceAttribute> independentList)
            throws NotSchedulableException {
        Vector<Actor> result = null;

        _createGraph(independentList);
        if (!_graph.isAcyclic()) {
            throw new NotSchedulableException(_director,
                    "Cannot estimate sequence for cyclic graphs.");
        }

        result = _schedule();

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a graph with dependencies caused by sequence numbers and data
     * flow constraints.
     *
     * @param independentList All sequence attributes to be considered when
     * building the graph.
     */
    private void _createGraph(List<SequenceAttribute> independentList) {
        _initSequenceEstimation();

        CompositeEntity composite = (CompositeEntity) _director.getContainer();
        Iterator entities = composite.deepEntityList().iterator();
        while (entities.hasNext()) {
            Actor actor = (Actor) entities.next();
            Node actorNode = (Node) _actorNodeMap.get(actor);

            // get predecessors of the actor
            HashSet preds = _getPredecessors(independentList, actor);
            int numPreds = preds.size();

            // add edges from predecessors to actor in the graph
            Iterator predsIterator = preds.iterator();
            while (predsIterator.hasNext()) {
                Actor pred = (Actor) predsIterator.next();
                if (pred == composite) {
                    numPreds--;
                } else {
                    Node predNode = (Node) _actorNodeMap.get(pred);
                    _graph.addEdge(predNode, actorNode);
                }
            }

            // initialize the number of unscheduled predecessors with
            // the number of predecessors
            _unscheduledPredecessors.put(actorNode, Integer.valueOf(numPreds));

            // put actor in ready list, if it has no predecessors
            if (numPreds == 0) {
                _ready.add(actorNode);
            }
        }
    }

    /** Get all predecessors of an actor based on dataflow and sequence numbers.
     *
     * @param independentList The list of sequence attributes present in the graph.
     * @param actor The actor for which the predecessors are determined.
     * @return All predecessors of the actor based on dataflow and sequence numbers.
     */
    private HashSet _getPredecessors(List<SequenceAttribute> independentList,
            Actor actor) {
        HashSet preds = new HashSet<Actor>();

        // FIXME: In case of "weak" data flow constraints, not all actors
        // that produce data for this actor are predecessors.
        Iterator inputs = actor.inputPortList().iterator();
        while (inputs.hasNext()) {
            IOPort input = (IOPort) inputs.next();
            Iterator connected = input.deepConnectedOutPortList().iterator();
            while (connected.hasNext()) {
                Actor pred = (Actor) ((IOPort) connected.next()).getContainer();
                preds.add(pred);
            }
        }

        int actorSequenceNumber = _getSequenceNumber(actor);

        if (actorSequenceNumber != -1) {
            Iterator independents = independentList.iterator();
            while (independents.hasNext()) {
                SequenceAttribute attribute = (SequenceAttribute) independents
                        .next();
                try {
                    int sequenceNumber = attribute.getSequenceNumber();
                    if (sequenceNumber < actorSequenceNumber) {
                        preds.add(attribute.getContainer());
                    }
                } catch (IllegalActionException e) {
                }
            }
        }
        return preds;
    }

    /** Get the sequence number of an actor.
     *
     * @param actor The actor that has a sequence number.
     * @return The sequence number of <i>actor</i>, if there is one. If not, -1
     * is returned.
     */
    private int _getSequenceNumber(Actor actor) {
        int actorSequenceNumber = -1;
        List sequenceAttributes = ((Entity) actor)
                .attributeList(SequenceAttribute.class);
        if (sequenceAttributes.size() > 0) {
            SequenceAttribute attribute = (SequenceAttribute) sequenceAttributes
                    .get(0);
            try {
                actorSequenceNumber = attribute.getSequenceNumber();
            } catch (IllegalActionException e) {
            }
        }
        return actorSequenceNumber;
    }

    private void _initSequenceEstimation() {
        _graph = new DirectedGraph();
        _actorNodeMap = new HashMap<Actor, Node>();
        _unscheduledPredecessors = new HashMap<Node, Integer>();
        _ready = new Vector<Node>();

        CompositeEntity composite = (CompositeEntity) _director.getContainer();
        Iterator entities = composite.deepEntityList().iterator();
        while (entities.hasNext()) {
            Actor actor = (Actor) entities.next();
            Node node = new Node(actor);
            _graph.addNode(node);
            _actorNodeMap.put(actor, node);
        }

    }

    /** Produce a static order schedule for the graph based on sequence
     * and data flow constraints.
     *
     *  @return A static order schedule.
     */
    private Vector<Actor> _schedule() {
        Vector<Actor> result = new Vector<Actor>();
        while (_ready.size() > 0) {
            Node current = (Node) _ready.remove(0);
            result.add((Actor) current.getWeight());
            Iterator successors = _graph.successors(current).iterator();
            while (successors.hasNext()) {
                Node successor = (Node) successors.next();
                int numPreds = ((Integer) _unscheduledPredecessors
                        .get(successor)).intValue();
                numPreds--;
                if (numPreds == 0) {
                    _ready.add(successor);
                }
                _unscheduledPredecessors.put(successor, numPreds);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap _actorNodeMap;

    private DirectedGraph _graph;

    private List _ready;

    private HashMap _unscheduledPredecessors;
}
