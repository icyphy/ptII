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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DijkstraSequenceEstimator

/** A class that estimates a sequential schedule based on a modified version
 * of Dijkstra's algorithm to compute longest paths.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
public class DijkstraSequenceEstimator extends SequenceEstimator {

    /** Construct an estimator for the given director.
     *
     *  @param director The director that needs to guess a schedule.
     */
    public DijkstraSequenceEstimator(Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Estimate a sequenced schedule. Do not care much about ordering
     * constraints given by sequence numbers.
     *
     * FIXME: If the graph is cyclic, this method runs forever! Currently this
     * has to (or at least should) be checked before.
     *
     * @param independentList The already present SequenceAttributes for the
     * Actors controlled by this scheduler.
     *
     * @return A vector with the ordered actors. Note that the sequence numbers
     * are not changed. This has to be done somewhere else.
     *
     * @exception NotSchedulableException If the underlying graph of the actors
     * is not acyclic.
     */
    @Override
    public Vector<Actor> estimateSequencedSchedule(
            List<SequenceAttribute> independentList)
            throws NotSchedulableException {

        _init();

        // initialize Dijkstra with sources and the actors that already
        // have sequence numbers
        _initSources();
        _initSequencedActors(independentList);

        // compute longest distances
        while (!_unsettled.isEmpty()) {
            Actor actor = _selectActor();
            _update(actor);
        }

        // assemble result
        Vector<Actor> result = _getResult();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Vector<Actor> _getResult() {
        Vector<Actor> result = new Vector<Actor>();
        int i = 0;
        while (i <= _maxDistance) {
            Iterator sequenceEntries = _sequenceInfos.entrySet().iterator();
            while (sequenceEntries.hasNext()) {
                Entry<Actor, _SequenceInfo> sequenceEntry = (Entry<Actor, _SequenceInfo>) sequenceEntries
                        .next();
                Actor actor = sequenceEntry.getKey();
                _SequenceInfo info = sequenceEntry.getValue();
                if (info.distance == i) {
                    result.add(actor);
                }
            }
            ++i;
        }
        return result;

    }

    private void _init() {
        _sequenceInfos = new HashMap<Actor, _SequenceInfo>();
        _unsettled = new HashSet<Actor>();
        _maxDistance = 0;
    }

    private void _initSequencedActors(List<SequenceAttribute> independentList)
            throws NotSchedulableException {
        if (independentList != null) {
            Iterator sequenceAttributes = independentList.iterator();
            while (sequenceAttributes.hasNext()) {
                SequenceAttribute attribute = (SequenceAttribute) sequenceAttributes
                        .next();
                try {
                    int sequenceNumber = attribute.getSequenceNumber();
                    _SequenceInfo info = new _SequenceInfo(sequenceNumber,
                            true, false);
                    //info.original = sequenceNumber;
                    Actor actor = (Actor) attribute.getContainer();
                    _sequenceInfos.put(actor, info);
                    _unsettled.add(actor);
                } catch (IllegalActionException e) {
                    List unschedulableActorList = new ArrayList(1);
                    unschedulableActorList.add(attribute.getContainer());
                    throw new NotSchedulableException(
                            unschedulableActorList,
                            e,
                            "The actor "
                                    + attribute.getContainer().getName()
                                    + " cannot be scheduled because "
                                    + "its SequenceAttribute "
                                    + attribute
                                    + " does not contain a valid sequence number.");
                }
            }
        }

    }

    private void _initSources() {
        NamedObj container = _director.getContainer();
        Iterator actors = ((CompositeEntity) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            //NamedObj container = actor.getContainer();
            boolean isSource = true;
            Iterator inputs = actor.inputPortList().iterator();
            while (inputs.hasNext() && isSource) {
                IOPort input = (IOPort) inputs.next();
                Iterator connectedPorts = input.deepConnectedOutPortList()
                        .iterator();
                while (connectedPorts.hasNext() && isSource) {
                    IOPort connected = (IOPort) connectedPorts.next();
                    if (container != ((NamedObj) connected).getContainer()) {
                        isSource = false;
                    }
                }
            }
            if (isSource) {
                //if (sources.contains(_actorGraphNodeList.get(actor))) {
                _SequenceInfo si = new _SequenceInfo(0, false, false);
                _sequenceInfos.put(actor, si);
                _unsettled.add(actor);
            }
        }

        return;
    }

    private Actor _selectActor() {
        Iterator it = _unsettled.iterator();
        int max = -1;
        Actor result = null;
        while (it.hasNext()) {
            Actor actor = (Actor) it.next();
            _SequenceInfo info = _sequenceInfos.get(actor);
            int dist = info.distance;
            if (actor == null || dist > max) {
                result = actor;
                max = dist;
            }
        }
        return result;
    }

    private void _update(Actor actor) throws NotSchedulableException {

        _SequenceInfo actorSeqInfo = _sequenceInfos.get(actor);
        int actorDistance = actorSeqInfo.distance;
        List portList = actor.outputPortList();
        if (portList != null) {
            Iterator ports = portList.iterator();

            // update _maxDistance
            if (ports.hasNext() && _maxDistance <= actorDistance) {
                _maxDistance = actorDistance + 1;
            }

            // update distances and unsettled
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                Iterator deepConnectedInPortList = port
                        .deepConnectedInPortList().iterator();

                while (deepConnectedInPortList.hasNext()) {
                    Port deepConnectedPort = (Port) deepConnectedInPortList
                            .next();
                    Actor connectedActor = (Actor) deepConnectedPort
                            .getContainer();

                    // do nothing, if container of actor is reached
                    // --> actor is sink
                    if (actor.getContainer() == connectedActor) {
                        continue;
                    }
                    _SequenceInfo _SequenceInfo = _sequenceInfos
                            .get(connectedActor);

                    if (_SequenceInfo == null) {
                        // put actor in the map, if not visited
                        _sequenceInfos.put(connectedActor, new _SequenceInfo(
                                actorDistance + 1, false, true));
                        _unsettled.add(connectedActor);
                    } else if (_SequenceInfo.distance < actorDistance + 1) {
                        // update distance, if a longer way is found.
                        _unsettled.add(connectedActor);
                        _SequenceInfo.distance = actorDistance + 1;
                        //                        if (_SequenceInfo.isFixed) {
                        //                            _SequenceInfo.changed = true;
                        //                        }
                    }
                }
            }
        }

        // remove current actor from list of actors to be inspected
        _unsettled.remove(actor);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private classes                   ////

    private static class _SequenceInfo {

        public _SequenceInfo(int d, boolean f, boolean c) {
            distance = d;
            //isFixed = f;
            //changed = c;
            //original = -1;
        }

        //public boolean changed;
        public int distance;
        //public boolean isFixed;
        //public int original;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _maxDistance;
    private HashMap<Actor, _SequenceInfo> _sequenceInfos;
    private Set<Actor> _unsettled;

}
