/* Model transformer based on graph isomorphism.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.FastHashMap;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.Pair;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;

/** A recursive algorithm to match a subgraph to the given pattern.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RecursiveGraphMatcher {

    /** Get the last matching result as an unmodifiable map.
     *
     *  @return The last matching result.
     */
    public Map<NamedObj, NamedObj> getMatch() {
        return Collections.unmodifiableMap(_match);
    }

    public boolean match(CompositeActorMatcher lhsGraph, NamedObj hostGraph)
    throws SubgraphMatchingException {

        // Matching result.
        _match = new FastHashMap<NamedObj, NamedObj>();

        // Temporary data structures.
        _lhsFrontier = new FastLinkedList<NamedObj>();
        _hostFrontier = new FastLinkedList<NamedObj>();
        _visitedLHSCompositeEntities = new FastLinkedList<CompositeEntity>();

        _lhsFrontier.add(lhsGraph);
        _hostFrontier.add(hostGraph);

        _success = _matchEntryList(_lhsFrontier.getHead(), _hostFrontier.getHead());
        if (_success) {
            for (NamedObj lhsObject : _match.keySet()) {
                System.out.println(lhsObject.getName() + " : " +
                        _match.get(lhsObject).getName());
            }
        }

        // Clear temporary data structures to free memory.
        _lhsFrontier = null;
        _hostFrontier = null;
        _visitedLHSCompositeEntities = null;
        return _success;
    }

    /** Generate a string that describes the last matching result. The matching
     *  result is stored in a <tt>Map&lt;{@link NamedObj}, {@link
     *  NamedObj}&gt;</tt> object. To generate the returned string,
     *  <tt>toString</tt> of the keys and values in the map are invoked.
     *
     *  @return A string that describes the last matching result.
     */
    public String stringifyMatchResult() {
        if (_success) {
            return Utils.toString(_match);
        } else {
            return "{}";
        }
    }

    public NamedObj transform(NamedObj from, SingleRuleTransformer transformer)
    throws GraphTransformationException {
        CompositeActorMatcher leftHandSide = transformer.getLeftHandSide();
        match(leftHandSide, from);
        return null;
    }

    private ComponentEntity _findFirstChild(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<NamedObj> excludedEntities) {
        List<?> entities = top.entityList(AtomicActor.class);
        if (!entities.isEmpty()) {
            int i = 0;
            for (Object entityObject : entities) {
                AtomicActor atomicEntity = (AtomicActor) entityObject;
                if (!excludedEntities.contains(atomicEntity)) {
                    markedList.add(new MarkedEntityList(entities, i));
                    return atomicEntity;
                }
                i++;
            }
        }

        entities = top.entityList(CompositeEntity.class);
        if (!entities.isEmpty()) {
            FastLinkedList<MarkedEntityList>.Entry tail = markedList.getTail();
            int i = 0;
            for (Object entityObject : entities) {
                CompositeEntity container = (CompositeEntity) entityObject;
                if (!excludedEntities.contains(container)) {
                    markedList.add(new MarkedEntityList(entities, i));
                    if (_isNewLevel(container)) {
                        return container;
                    } else {
                        ComponentEntity actor = _findFirstChild(container,
                                markedList, excludedEntities);
                        if (actor != null) {
                            return actor;
                        } else {
                            markedList.removeAllAfter(tail);
                        }
                    }
                }
                i++;
            }
        }
        return null;
    }

    private ComponentEntity _findNextChild(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<NamedObj> excludedEntities) {
        if (markedList.isEmpty()) {
            return _findFirstChild(top, markedList, excludedEntities);
        } else {
            FastLinkedList<MarkedEntityList>.Entry entry = markedList.getTail();
            while (entry != null) {
                MarkedEntityList markedEntityList = entry.getValue();
                List<?> entityList = markedEntityList.getFirst();
                for (int index = markedEntityList.getSecond() + 1;
                       index < entityList.size(); index++) {
                    markedEntityList.setSecond(index);
                    ComponentEntity entity =
                        (ComponentEntity) entityList.get(index);
                    if (!excludedEntities.contains(entity)) {
                        markedList.removeAllAfter(entry);
                        if (entity instanceof AtomicActor
                                || entity instanceof CompositeEntity
                                && _isNewLevel((CompositeEntity) entity)) {
                            return entity;
                        } else {
                            CompositeEntity compositeEntity =
                                (CompositeEntity) entity;
                            ComponentEntity child = _findFirstChild(
                                    compositeEntity, markedList,
                                    excludedEntities);
                            if (child != null) {
                                return child;
                            }
                        }
                    }
                }
                entry = entry.getPrevious();
            }
            return null;
        }
    }

    private boolean _isNewLevel(CompositeEntity container) {
        return container instanceof CompositeActor
                && ((CompositeActor) container).getDirector() != null;
    }

    private boolean _matchAtomicActor(AtomicActor lhsActor,
            AtomicActor hostActor) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsActor, hostActor);

        for (Object portObject : lhsActor.portList()) {
            Port port = (Port) portObject;
            if (!_match.containsKey(port)) {
                _lhsFrontier.add(port);
            }
        }

        for (Object portObject : hostActor.portList()) {
            Port port = (Port) portObject;
            if (!_match.containsValue(port)) {
                _hostFrontier.add(port);
            }
        }

        if (_matchLoop(lhsTail, hostTail)) {
            return true;
        } else {
            _match.remove(lhsActor);
            _lhsFrontier.removeAllAfter(lhsTail);
            _hostFrontier.removeAllAfter(hostTail);
            return false;
        }
    }

    private boolean _matchCompositeEntity(CompositeEntity lhsEntity,
            CompositeEntity hostEntity) {

        FastLinkedList<MarkedEntityList> lhsMarkedList =
            new FastLinkedList<MarkedEntityList>();
        ComponentEntity lhsNextActor =
            _findFirstChild(lhsEntity, lhsMarkedList, _match.keySet());

        boolean firstEntrance = !_match.containsKey(lhsEntity);
        if (firstEntrance) {
            _match.put(lhsEntity, hostEntity);
        }

        if (lhsNextActor == null) {
            return true;
        } else {
            FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
            FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

            FastLinkedList<CompositeEntity>.Entry compositeTail = null;
            if (firstEntrance) {
                _visitedLHSCompositeEntities.add(lhsEntity);
                compositeTail = _visitedLHSCompositeEntities.getTail();
            }

            FastLinkedList<MarkedEntityList> hostMarkedList =
                new FastLinkedList<MarkedEntityList>();
            ComponentEntity hostNextActor = _findFirstChild(hostEntity,
                    hostMarkedList, _match.values());

            while (hostNextActor != null) {
                _lhsFrontier.add(lhsNextActor);
                _hostFrontier.add(hostNextActor);

                if (_matchEntryList(lhsTail.getNext(), hostTail.getNext())) {
                    return true;
                } else {
                    _hostFrontier.removeAllAfter(hostTail);
                    _lhsFrontier.removeAllAfter(lhsTail);
                    if (firstEntrance) {
                        _visitedLHSCompositeEntities.removeAllAfter(
                                compositeTail);
                    }
                    hostNextActor = _findNextChild(hostEntity,
                            hostMarkedList, _match.values());
                }
            }
            if (firstEntrance) {
                compositeTail.remove();
                _match.remove(lhsEntity);
            }
            return false;
        }
    }

    private boolean _matchDisconnectedComponents() {
        FastLinkedList<CompositeEntity>.Entry lhsEntry =
            _visitedLHSCompositeEntities.getTail();
        while (lhsEntry != null) {
            CompositeEntity lhsEntity = lhsEntry.getValue();
            if (!_matchCompositeEntity(lhsEntity,
                    (CompositeEntity) _match.get(lhsEntity))) {
                return false;
            }
            lhsEntry = lhsEntry.getPrevious();
        }
        return true;
    }

    /** Match a list of LHS entries with a list of host entries. All LHS entries
     *  must be matched with some or all of the host entries.
     *
     *  @param lhsEntry The start of the LHS entries.
     *  @param hostEntry The start of the host entries.
     *  @return <tt>true</tt> is the match is successful; <tt>false</tt>
     *   otherwise.
     */
    private boolean _matchEntryList(FastLinkedList<NamedObj>.Entry lhsEntry,
            FastLinkedList<NamedObj>.Entry hostEntry) {
        if (lhsEntry == null) {
            return true;
        } else {
            // Arbitrarily pick an object in _lhsFrontier to match.
            NamedObj lhsObject = lhsEntry.getValue();
            while (hostEntry != null) {
                if (_matchNamedObj(lhsObject, hostEntry.getValue())) {
                    return true;
                } else {
                    hostEntry = hostEntry.getNext();
                }
            }
            return false;
        }
    }

    private boolean _matchLoop(FastLinkedList<NamedObj>.Entry lhsStart,
            FastLinkedList<NamedObj>.Entry hostStart) {

        // The real start of the two frontiers.
        // For the 1st check for disconnected components, the parameters have to
        // be non-null, and the following variables are the actual parameters to
        // the loop.
        FastLinkedList<NamedObj>.Entry lhsChildStart = lhsStart.getNext();
        FastLinkedList<NamedObj>.Entry hostChildStart = hostStart.getNext();

        if (lhsChildStart == null) {
            return _matchDisconnectedComponents();
        } else {
            FastLinkedList<NamedObj>.Entry lhsEntry = lhsChildStart;
            boolean nestedMatch = false;
            while (lhsEntry != null) {
                nestedMatch = true;
                if (!_matchEntryList(lhsEntry, hostChildStart)) {
                    return false;
                }
                lhsEntry = lhsEntry.getNext();
            }
            if (nestedMatch) {
                return true;
            } else {
                return _matchDisconnectedComponents();
            }
        }
    }

    private boolean _matchNamedObj(NamedObj lhsObject, NamedObj hostObject) {
        if (_match.containsKey(lhsObject)) {
            return _match.get(lhsObject) == hostObject
                    && _matchDisconnectedComponents();
        } else if (_match.containsValue(hostObject)) {
            return false;
        } else if (lhsObject instanceof AtomicActor
                && hostObject instanceof AtomicActor) {
            return _matchAtomicActor((AtomicActor) lhsObject,
                    (AtomicActor) hostObject);
        } else if (lhsObject instanceof CompositeEntity
                && hostObject instanceof CompositeEntity) {
            return _matchCompositeEntity((CompositeEntity) lhsObject,
                    (CompositeEntity) hostObject);
        } else if (lhsObject instanceof Port
                && hostObject instanceof Port) {
            return _matchPort((Port) lhsObject, (Port) hostObject);
        } else if (lhsObject instanceof Relation
                && hostObject instanceof Relation) {
            return _matchRelation((Relation) lhsObject,
                    (Relation) hostObject);
        } else {
            return false;
        }
    }

    private boolean _matchPort(Port lhsPort, Port hostPort) {

        if (!_shallowMatchPort(lhsPort, hostPort)) {
            return false;
        }

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsPort, hostPort);

        for (Object relationObject : lhsPort.linkedRelationList()) {
            Relation relation = (Relation) relationObject;
            if (!_match.containsKey(relation)) {
                _lhsFrontier.add(relation);
            }
        }

        for (Object relationObject : hostPort.linkedRelationList()) {
            Relation relation = (Relation) relationObject;
            if (!_match.containsValue(relation)) {
                _hostFrontier.add(relation);
            }
        }

        if (_matchLoop(lhsTail, hostTail)) {
            return true;
        } else {
            _match.remove(lhsPort);
            _lhsFrontier.removeAllAfter(lhsTail);
            _hostFrontier.removeAllAfter(hostTail);
            return false;
        }
    }

    private boolean _matchRelation(Relation lhsRelation,
            Relation hostRelation) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsRelation, hostRelation);

        for (Object portObject : lhsRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!_match.containsKey(container)) {
                _lhsFrontier.add(container);
            }
        }

        for (Object portObject : hostRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!_match.containsValue(container)) {
                _hostFrontier.add(container);
            }
        }

        if (_matchLoop(lhsTail, hostTail)) {
            return true;
        } else {
            _match.remove(lhsRelation);
            _hostFrontier.removeAllAfter(hostTail);
            _lhsFrontier.removeAllAfter(lhsTail);
            return false;
        }
    }

    private boolean _shallowMatchPort(Port lhsPort, Port hostPort) {
        if (lhsPort instanceof TypedIOPort) {
            if (hostPort instanceof TypedIOPort) {
                TypedIOPort lhsTypedPort = (TypedIOPort) lhsPort;
                TypedIOPort hostTypedPort = (TypedIOPort) hostPort;
                if (lhsTypedPort.isInput() && !hostTypedPort.isInput()) {
                    return false;
                } else if (lhsTypedPort.isOutput()
                        && !hostTypedPort.isOutput()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private FastLinkedList<NamedObj> _hostFrontier;

    private FastLinkedList<NamedObj> _lhsFrontier;

    private Map<NamedObj, NamedObj> _match;

    private boolean _success = false;

    private FastLinkedList<CompositeEntity> _visitedLHSCompositeEntities;

    private static class MarkedEntityList extends Pair<List<?>, Integer> {

        MarkedEntityList(List<?> list, Integer mark) {
            super(list, mark);
        }

        private static final long serialVersionUID = -8862333308144377821L;

    }
}
