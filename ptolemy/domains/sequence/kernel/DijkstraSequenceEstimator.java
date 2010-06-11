/* A class that estimates a sequential schedule. 

 Copyright (c) 2010 The Regents of the University of California.
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
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DijkstraSequenceEstimator

/**
* A class that estimates a sequential schedule based on a modified version 
* of Dijkstra's algorithm to compute longest paths.
*
* @author Bastian Ristau
* @version $Id$
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ristau)
* @Pt.AcceptedRating Red (ristau)
*/
public class DijkstraSequenceEstimator {

    /** Construct an estimator for the given director.
     * 
     *  @param director The director that needs to guess a schedule.
     */
    public DijkstraSequenceEstimator(Director director) {
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* Estimate a sequenced schedule. This uses a basic Dijkstra algorithm
     * for getting the maximal distances.
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
     * @exception NotSchedulableException If the schedule is acyclic.
     */
    public Vector<Actor> estimateSequencedSchedule(
            List<SequenceAttribute> independentList) {
        
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
            Iterator it = _seqInfo.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Actor, SeqInfo> elem = (Entry<Actor, SeqInfo>) it.next();
                Actor actor = elem.getKey();
                SeqInfo info = elem.getValue();
                if (info.distance == i) {
                    result.add(actor);
                }
            }
            ++i;
        }
        return result;

    }

    private void _init() {
        _seqInfo = new HashMap<Actor, SeqInfo>();
        _unsettled = new HashSet<Actor>();
        _maxDistance = 0;
    }

    private void _initSequencedActors(List<SequenceAttribute> independentList) {
        Iterator seqAttribute = independentList.iterator();
        while (seqAttribute.hasNext()) {
            SequenceAttribute seq = (SequenceAttribute) seqAttribute.next();
            int seqNum = seq.getSequenceNumber();
            SeqInfo si = new SeqInfo(seqNum, true, false);
            si.original = seqNum;
            Actor actor = (Actor) seq.getContainer();
            _seqInfo.put(actor, si);
            _unsettled.add(actor);
        }

    }

    private void _initSources() {
        Iterator actors = ((CompositeEntity) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedObj container = actor.getContainer();
            boolean isSource = true;
            Iterator inputs = actor.inputPortList().iterator();
            while (inputs.hasNext() && isSource) {
                IOPort input = (IOPort) inputs.next();
                Iterator conList = input.deepConnectedOutPortList().iterator();
                while (conList.hasNext() && isSource) {
                    IOPort connected = (IOPort) conList.next();
                    if (container != ((NamedObj) connected).getContainer()) {
                        isSource = false;
                    }
                }
            }
            if (isSource) {
                //if (sources.contains(_actorGraphNodeList.get(actor))) {
                SeqInfo si = new SeqInfo(0, false, false);
                _seqInfo.put(actor, si);
                _unsettled.add(actor);
            }
        }

        return;
    }

    private Actor _selectActor() {
        Iterator it = _unsettled.iterator();
        int max = 0;
        Actor result = null;
        while (it.hasNext()) {
            Actor actor = (Actor) it.next();
            SeqInfo info = _seqInfo.get(actor);
            int dist = info.distance;
            if (actor == null || dist > max) {
                result = actor;
                max = dist;
            }
        }
        return result;
    }

    private void _update(Actor actor) throws NotSchedulableException {

        SeqInfo actorSeqInfo = _seqInfo.get(actor);
        int actorDistance = actorSeqInfo.distance;
        Iterator ports = actor.outputPortList().iterator();

        // update _maxDistance
        if (ports.hasNext() && _maxDistance <= actorDistance) {
            _maxDistance = actorDistance + 1;
        }

        // update distances and unsettled
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            Iterator deepConnectedInPortList = port.deepConnectedInPortList()
                    .iterator();

            while (deepConnectedInPortList.hasNext()) {
                Port deepConnectedPort = (Port) deepConnectedInPortList.next();
                Actor connectedActor = (Actor) deepConnectedPort.getContainer();
                SeqInfo seqInfo = _seqInfo.get(connectedActor);

                if (seqInfo == null) {
                    // put actor in the map, if not visited
                    _seqInfo.put(connectedActor, new SeqInfo(actorDistance + 1,
                            false, true));
                    _unsettled.add(connectedActor);
                } else if (seqInfo.distance < actorDistance + 1) {
                    // update distance, if a longer way is found.
                    _unsettled.add(connectedActor);
                    seqInfo.distance = actorDistance + 1;
                    if(seqInfo.isFixed) {
                        seqInfo.changed = true;
                    }
                }
            }
        }

        // remove current actor from list of actors to be inspected
        _unsettled.remove(actor);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private classes                   ////

    private class SeqInfo {

        public SeqInfo(int d, boolean f, boolean c) {
            distance = d;
            isFixed = f;
            changed = c;
            original = -1;
        }

        public boolean changed;
        public int distance;
        public boolean isFixed;
        public int original;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Director _director;
    private int _maxDistance;
    private HashMap<Actor, SeqInfo> _seqInfo;
    private Set<Actor> _unsettled;

}
