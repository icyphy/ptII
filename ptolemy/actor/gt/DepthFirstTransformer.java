package ptolemy.actor.gt;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.Pair;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;

public class DepthFirstTransformer {

    public void match(CompositeActorMatcher lhsGraph, NamedObj hostGraph)
    throws SubgraphMatchingException {
        _match = new HashMap<NamedObj, NamedObj>();
        _lhsFrontier = new FastLinkedList<NamedObj>();
        _hostFrontier = new FastLinkedList<NamedObj>();

        _match.put(hostGraph, lhsGraph);
        _lhsFrontier.add(lhsGraph);
        _hostFrontier.add(hostGraph);

        _match(_lhsFrontier.getHead(), _hostFrontier.getHead());
    }

    public NamedObj transform(NamedObj from, SingleRuleTransformer transformer)
    throws GraphTransformationException {
        CompositeActorMatcher leftHandSide = transformer.getLeftHandSide();
        match(leftHandSide, from);
        return null;
    }

    private AtomicActor _findFirstAtomicActor(CompositeEntity top,
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
                CompositeEntity entity = (CompositeEntity) entityObject;
                if (!excludedEntities.contains(entity)) {
                    markedList.add(new MarkedEntityList(entities, i));
                    AtomicActor actor =
                        _findFirstAtomicActor(entity, markedList,
                                excludedEntities);
                    if (actor != null) {
                        return actor;
                    } else {
                        markedList.removeAllAfter(tail);
                    }
                }
                i++;
            }
        }
        return null;
    }

    private AtomicActor _findNextAtomicActor(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<NamedObj> excludedEntities) {
        if (markedList.isEmpty()) {
            return _findFirstAtomicActor(top, markedList, excludedEntities);
        } else {
            FastLinkedList<MarkedEntityList>.Entry entry = markedList.getTail();
            MarkedEntityList markedEntityList = entry.getValue();
            List<?> atomicEntityList = markedEntityList.getFirst();
            for (int index = markedEntityList.getSecond() + 1;
                   index < atomicEntityList.size(); index++) {
                AtomicActor atomicEntity =
                    (AtomicActor) atomicEntityList.get(index);
                if (!excludedEntities.contains(atomicEntity)) {
                    markedEntityList.setSecond(index);
                    return (AtomicActor) atomicEntity;
                }
            }

            entry = entry.getPrevious();
            while (entry != null) {
                markedEntityList = entry.getValue();
                List<?> compositeEntityList = markedEntityList.getFirst();
                for (int index = markedEntityList.getSecond() + 1;
                        index < compositeEntityList.size(); index++) {
                    CompositeEntity compositeEntity =
                        (CompositeEntity) compositeEntityList.get(index);
                    if (!excludedEntities.contains(compositeEntity)) {
                        markedList.removeAllAfter(entry);
                        AtomicActor atomicEntity =
                            _findFirstAtomicActor(compositeEntity, markedList,
                                    excludedEntities);
                        if (atomicEntity != null) {
                            return atomicEntity;
                        }
                    }
                }
                entry = entry.getPrevious();
            }
            return null;
        }
    }

    private boolean _match(FastLinkedList<NamedObj>.Entry lhsEntry,
            FastLinkedList<NamedObj>.Entry hostEntry) {
        if (lhsEntry == null) {
            return true;
        } else {
            // Arbitrarily pick an object in _lhsFrontier to match.
            NamedObj lhsObject = lhsEntry.getValue();
            while (hostEntry != null) {
                if (_tryToMatch(lhsObject, hostEntry.getValue())) {
                    return true;
                } else {
                    hostEntry = hostEntry.getNext();
                }
            }
            return false;
        }
    }

    private boolean _tryToMatch(NamedObj lhsObject, NamedObj hostObject) {
        if (lhsObject instanceof CompositeEntity
                && hostObject instanceof CompositeEntity) {
            return _tryToMatchCompositeEntity((CompositeEntity) lhsObject,
                    (CompositeEntity) hostObject);
        } else if (lhsObject instanceof Relation
                && hostObject instanceof Relation) {
            return _tryToMatchRelation((Relation) lhsObject,
                    (Relation) hostObject);
        } else if (lhsObject instanceof AtomicActor
                && hostObject instanceof AtomicActor) {
            return _tryToMatchAtomicActor((AtomicActor) lhsObject,
                    (AtomicActor) hostObject);
        } else {
            return false;
        }
    }

    private boolean _tryToMatchAtomicActor(AtomicActor lhsActor,
            AtomicActor hostActor) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsActor, hostActor);

        Set<NamedObj> matchKeys = _match.keySet();
        Collection<NamedObj> matchValues = _match.values();

        for (Object portObject : lhsActor.portList()) {
            Port port = (Port) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                if (!matchKeys.contains(relation)) {
                    _lhsFrontier.add(relation);
                }
            }
        }

        for (Object portObject : hostActor.portList()) {
            Port port = (Port) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                if (!matchValues.contains(relation)) {
                    _hostFrontier.add(relation);
                }
            }
        }

        if (_match(lhsTail.getNext(), hostTail.getNext())) {
            return true;
        } else {
            _match.remove(lhsActor);
            _lhsFrontier.removeAllAfter(lhsTail);
            _hostFrontier.removeAllAfter(hostTail);
            return false;
        }
    }

    private boolean _tryToMatchCompositeEntity(CompositeEntity lhsEntity,
            CompositeEntity hostEntity) {

        FastLinkedList<MarkedEntityList> lhsMarkedList =
            new FastLinkedList<MarkedEntityList>();
        AtomicActor lhsNextActor =
            _findFirstAtomicActor(lhsEntity, lhsMarkedList, _match.keySet());

        if (lhsNextActor == null) {
            return true;
        } else {
            FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
            FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

            FastLinkedList<MarkedEntityList> hostMarkedList =
                new FastLinkedList<MarkedEntityList>();
            AtomicActor hostNextActor = _findFirstAtomicActor(hostEntity,
                    hostMarkedList, _match.values());

            while (hostNextActor != null) {
                _match.put(lhsNextActor, hostNextActor);
                _lhsFrontier.add(lhsNextActor);
                _hostFrontier.add(hostNextActor);

                if (_match(lhsTail.getNext(), hostTail.getNext())) {
                    return true;
                } else {
                    _hostFrontier.removeAllAfter(hostTail);
                    _lhsFrontier.removeAllAfter(lhsTail);
                    hostNextActor = _findNextAtomicActor(hostEntity,
                            hostMarkedList, _match.values());
                }
            }
            _match.remove(lhsNextActor);
            return false;
        }
    }

    private boolean _tryToMatchRelation(Relation lhsRelation,
            Relation hostRelation) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsRelation, hostRelation);

        Set<NamedObj> matchKeys = _match.keySet();
        Collection<NamedObj> matchValues = _match.values();

        for (Object portObject : lhsRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!matchKeys.contains(container)) {
                _lhsFrontier.add(container);
            }
        }

        for (Object portObject : hostRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!matchValues.contains(container)) {
                _hostFrontier.add(container);
            }
        }

        if (_match(lhsTail.getNext(), hostTail.getNext())) {
            return true;
        } else {
            _match.remove(lhsRelation);
            _hostFrontier.removeAllAfter(hostTail);
            _lhsFrontier.removeAllAfter(lhsTail);
            return false;
        }
    }

    private FastLinkedList<NamedObj> _hostFrontier;

    private FastLinkedList<NamedObj> _lhsFrontier;

    private Map<NamedObj, NamedObj> _match;

    private static class MarkedEntityList extends Pair<List<?>, Integer> {

        MarkedEntityList(List<?> list, Integer mark) {
            super(list, mark);
        }

        private static final long serialVersionUID = -8862333308144377821L;

    }
}
