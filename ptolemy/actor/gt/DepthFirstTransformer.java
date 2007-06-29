package ptolemy.actor.gt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;

public class DepthFirstTransformer {

    public NamedObj transform(NamedObj from, SingleRuleTransformer transformer)
    throws GraphTransformationException {
        CompositeActorMatcher leftHandSide = transformer.getLeftHandSide();
        match(from, leftHandSide);
        return null;
    }
    
    public void match(NamedObj hostGraph, CompositeActorMatcher lhs)
    throws SubgraphMatchingException {
        _match = new HashMap<NamedObj, NamedObj>();
        _hostFrontier = new LinkedList<NamedObj>();
        _lhsFrontier = new LinkedList<NamedObj>();
        
        _match.put(hostGraph, lhs);
        _hostFrontier.add(hostGraph);
        _lhsFrontier.add(lhs);
        
        _match();
    }
    
    private boolean _match() {
        if (_lhsFrontier.isEmpty()) {
            return true;
        } else {
            int hostFrontierSize = _hostFrontier.size();
            int lhsFrontierSize = _lhsFrontier.size();

            // Arbitrarily pick an object in _lhsFrontier to match.
            NamedObj lhsObj = _lhsFrontier.get(0);
            for (NamedObj hostObj : _hostFrontier) {
                return _tryToMatch(hostObj, lhsObj);
            }
            return false;
        }
    }
    
    private boolean _tryToMatch(NamedObj hostObj, NamedObj lhsObject) {
        if (hostObj instanceof CompositeEntity
                && lhsObject instanceof CompositeEntity) {
            CompositeEntity lhsActor = (CompositeEntity) lhsObject;
            ComponentEntity lhsNextChild = _findNextChild(lhsActor);
            return true;
        } else {
            return false;
        }
    }
    
    private ComponentEntity _findNextChild(CompositeEntity parent) {
        List<?> lhsEntities = parent.entityList();
        if (!lhsEntities.isEmpty()) {
            for (Object lhsObject : lhsEntities) {
                ComponentEntity lhsEntity = (ComponentEntity) lhsObject;
                if (lhsEntity instanceof CompositeEntity) {
                    ComponentEntity lhsSubentity =
                        _findNextChild((CompositeEntity) lhsEntity);
                    if (lhsSubentity != null) {
                        return lhsSubentity;
                    }
                } else {
                    if (!_match.containsKey(lhsEntity)) {
                        return lhsEntity;
                    }
                }
            }
        }
        return null;
    }
    
    private Map<NamedObj, NamedObj> _match;
    
    private List<NamedObj> _hostFrontier;
    
    private List<NamedObj> _lhsFrontier;
}
