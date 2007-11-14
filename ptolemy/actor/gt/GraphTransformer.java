/*

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.TwoWayHashMap;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphTransformer extends ChangeRequest {

    public GraphTransformer(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        super(null, "Apply graph transformation to model.");

        _pattern = transformationRule.getPattern();
        _replacement = transformationRule.getReplacement();
        _matchResult = matchResult;
        _host = (CompositeEntity) _matchResult.get(_pattern);

        if (_host == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not include the pattern.");
        }
    }

    public MatchResult getMatchResult() {
        return _matchResult;
    }

    public Pattern getPattern() {
        return _pattern;
    }

    public Replacement getReplacement() {
        return _replacement;
    }

    public static void transform(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        GraphTransformer transformer =
            new GraphTransformer(transformationRule, matchResult);
        transformer._host.requestChange(transformer);
    }

    protected void _addConnections() throws TransformationException {
        for (NamedObj replacement : _replacementToHost.keySet()) {
            if (!(replacement instanceof Port)) {
                continue;
            }

            Port replacementPort = (Port) replacement;
            Port hostPort = (Port) _replacementToHost.get(replacement);
            for (Object replacementRelationObject
                    : replacementPort.linkedRelationList()) {
                Relation replacementRelation =
                    (Relation) replacementRelationObject;
                Relation hostRelation =
                    (Relation) _replacementToHost.get(replacementRelation);
                // FIXME: hostRelation shouldn't be null, but it seems if a
                // Publisher appears in the host model, then an extra relation
                // created by it remains after it is deleted, so there is a
                // relation that has no match in the match result.
                // Needs to fix this in ptolemy.actor.lib.Publisher.
                List<?> hostRelations = hostPort.linkedRelationList();
                if (hostRelation != null
                        && !hostRelations.contains(hostRelation)) {
                    // There is no link between hostPort and hostRelation, so
                    // create a new link.
                    Entity hostPortContainer = (Entity) hostPort.getContainer();
                    NamedObj hostContainer = hostRelation.getContainer();
                    String moml = "<link port=\"" + hostPortContainer.getName()
                            + "." + hostPort.getName() + "\" relation=\""
                            + hostRelation.getName() + "\"/>";
                    MoMLChangeRequest request =
                        new MoMLChangeRequest(this, hostContainer, moml);
                    request.execute();
                }
            }

            if (replacementPort instanceof ComponentPort
                    && hostPort instanceof ComponentPort) {
                ComponentPort replacementComponentPort =
                    (ComponentPort) replacementPort;
                ComponentPort hostComponentPort = (ComponentPort) hostPort;
                for (Object replacementRelationObject
                        : replacementComponentPort.insideRelationList()) {
                    Relation replacementRelation =
                        (Relation) replacementRelationObject;
                    Relation hostRelation =
                        (Relation) _replacementToHost.get(replacementRelation);
                    if (!hostComponentPort.insideRelationList().contains(
                            hostRelation)) {
                        // There is no link between hostPort and hostRelation,
                        // so create a new link.
                        NamedObj hostContainer = hostRelation.getContainer();
                        String moml = "<link port=\"" + hostPort.getName()
                                + "\" relation=\"" + hostRelation.getName()
                                + "\"/>";
                        MoMLChangeRequest request =
                            new MoMLChangeRequest(this, hostContainer, moml);
                        request.execute();
                    }
                }
            }
        }
    }

    protected void _addObjects() throws TransformationException {
        _addObjects(_replacement, _host);
    }

    protected void _execute() throws TransformationException {
        _init();
        _performOperations();
        _recordMoML();
        _removeObjects();
        _addObjects();
        _addConnections();
        _wrapup();
    }

    protected void _init() throws TransformationException {
        _patternToReplacement = new TwoWayHashMap<NamedObj, NamedObj>();
        _initPatternToReplacement(_replacement);
        _initReplacementToHost();
        _initReplacementObjectAttributes(_replacement);
    }

    protected void _performOperations() throws TransformationException {
        for (Map.Entry<NamedObj, NamedObj> entry
                : _replacementToHost.entrySet()) {
            NamedObj replacement = entry.getKey();
            NamedObj host = entry.getValue();
            NamedObj pattern = _patternToReplacement.getKey(replacement);

            if (!(pattern instanceof GTEntity
                    && replacement instanceof GTEntity
                    && host instanceof ComponentEntity)) {
                continue;
            }

            try {
                GTEntity patternEntity = (GTEntity) pattern;
                GTEntity replacementEntity = (GTEntity) replacement;
                ComponentEntity hostEntity = (ComponentEntity) host;
                GTIngredientList ingredientList = replacementEntity
                        .getOperationsAttribute().getIngredientList();
                for (GTIngredient ingredient : ingredientList) {
                    ChangeRequest request =
                        ((Operation) ingredient).getChangeRequest(patternEntity,
                                replacementEntity, hostEntity);
                    request.execute();
                }
            } catch (MalformedStringException e) {
                throw new TransformationException(
                        "Cannot parse operation list.", e);
            }
        }
    }

    protected void _recordMoML() throws TransformationException {
        _moml = new HashMap<NamedObj, String>();
        for (Map.Entry<NamedObj, NamedObj> entry
                : _replacementToHost.entrySet()) {
            NamedObj replacement = entry.getKey();
            NamedObj host = entry.getValue();
            String moml = _getMoML(host);
            _moml.put(replacement, moml);
        }
    }

    protected void _removeObjects() throws TransformationException {
        _removeObjects(_replacement, _host);
    }

    protected void _wrapup() {
        _removeReplacementObjectAttributes(_host);
        _removeReplacementObjectAttributes(_replacement);
    }

    private void _addObjects(NamedObj replacement, NamedObj host)
    throws TransformationException {
        // FIXME: Consider attributes.
        Collection<?> children =
            _getChildren(replacement, false, false, true, true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            NamedObj hostChild = _replacementToHost.get(child);
            String moml = null;
            if (hostChild == null) {
                moml = _getMoML(child);
            } else {
                if (hostChild.getContainer() != host) {
                    moml = _moml.get(child);
                }
            }
            if (moml != null && moml != "") {
                moml = "<group name=\"auto\">\n" + moml + "</group>";
                MoMLChangeRequest request =
                    new MoMLChangeRequest(this, host, moml);
                request.execute();
                hostChild = _getNewlyAddedObject(host, child.getClass());
                _addReplacementToHostEntries(hostChild);
            }
            if (hostChild != null) {
                _addObjects(child, hostChild);
            }
        }
    }

    private void _addReplacementToHostEntries(NamedObj host) {
        ReplacementObjectAttribute attribute =
            _getReplacementObjectAttribute(host);
        if (attribute != null) {
            String replacementCode = attribute.getExpression();
            NamedObj replacement =
                _getObjectFromCode(replacementCode, _replacement);
            _replacementToHost.put(replacement, host);
        }

        Collection<?> children = _getChildren(host, false, true, true, true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            _addReplacementToHostEntries(child);
        }
    }

    private Collection<?> _getChildren(NamedObj object,
            boolean includeAttributes, boolean includePorts,
            boolean includeEntities, boolean includeRelations) {
        Collection<Object> collection = new CombinedCollection<Object>();
        if (includeAttributes) {
            collection.addAll((Collection<?>) object.attributeList());
        }
        if (includePorts && object instanceof Entity) {
            Entity entity = (Entity) object;
            collection.addAll((Collection<?>) entity.portList());
        }
        if (object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            if (includeEntities) {
                collection.addAll((Collection<?>) entity.entityList());
            }
            if (includeRelations) {
                collection.addAll((Collection<?>) entity.relationList());
            }
        }
        return collection;
    }

    private static String _getCodeFromObject(NamedObj object,
            NamedObj topContainer) {
        String replacementAbbrev = _getTypeAbbreviation(object);
        String name = topContainer == null ?
                object.getName() : object.getName(topContainer);
        return replacementAbbrev + name;
    }

    private String _getMoML(NamedObj host) throws TransformationException {
        if (host instanceof CompositeEntity) {
            try {
                CompositeEntity clone = (CompositeEntity) host.clone();
                clone.removeAllEntities();
                clone.removeAllRelations();
                return clone.exportMoMLPlain();
            } catch (CloneNotSupportedException e) {
                throw new TransformationException(
                        "Cannot clone composite entity " + host.getFullName()
                        + ".", e);
            }
        } else {
            return host.exportMoMLPlain();
        }
    }

    private NamedObj _getNewlyAddedObject(NamedObj container,
            Class<? extends NamedObj> objectClass) {
        List<?> objectList;
        if (Attribute.class.isAssignableFrom(objectClass)) {
            objectList = container.attributeList();
        } else if (Entity.class.isAssignableFrom(objectClass)) {
            objectList = ((CompositeEntity) container).entityList();
        } else if (Port.class.isAssignableFrom(objectClass)) {
            objectList = ((Entity) container).portList();
        } else if (Relation.class.isAssignableFrom(objectClass)) {
            objectList = ((CompositeEntity) container).relationList();
        } else {
            return null;
        }

        int lastIndex = objectList.size() - 1;
        return (NamedObj) objectList.get(lastIndex);
    }

    private static NamedObj _getObjectFromCode(String code,
            NamedObj topContainer) {
        String abbreviation = code.substring(0, 2);
        String name = code.substring(2);
        if (abbreviation.equals("A:")) {
            return topContainer.getAttribute(name);
        } else if (abbreviation.equals("E:")) {
            return ((CompositeEntity) topContainer).getEntity(name);
        } else if (abbreviation.equals("P:")) {
            return ((Entity) topContainer).getPort(name);
        } else if (abbreviation.equals("R:")) {
            return ((CompositeEntity) topContainer).getRelation(name);
        } else {
            return null;
        }
    }

    private ReplacementObjectAttribute _getReplacementObjectAttribute(
            NamedObj object) {
        Attribute attribute = object.getAttribute("replacementObject");
        if (attribute instanceof ReplacementObjectAttribute) {
            return (ReplacementObjectAttribute) attribute;
        } else {
            return null;
        }
    }

    private static String _getTypeAbbreviation(NamedObj object) {
        if (object instanceof Attribute) {
            return "A:";
        } else if (object instanceof Entity) {
            return "E:";
        } else if (object instanceof Port) {
            return "P:";
        } else if (object instanceof Relation) {
            return "R:";
        } else {
            return null;
        }
    }

    private void _initPatternToReplacement(NamedObj replacement) {
        NamedObj pattern = GTTools.getCorrespondingPatternObject(replacement);
        if (pattern != null) {
            _patternToReplacement.put(pattern, replacement);
            if (pattern instanceof Entity && replacement instanceof Entity) {
                Entity patternEntity = (Entity) pattern;
                Entity replacementEntity = (Entity) replacement;
                List<?> patternPortList = patternEntity.portList();
                List<?> replacementPortList = replacementEntity.portList();
                for (int i = 0; i < patternPortList.size(); i++) {
                    Port patternPort = (Port) patternPortList.get(i);
                    Port replacementPort = (Port) replacementPortList.get(i);
                    _patternToReplacement.put(patternPort, replacementPort);
                }
            }
        }

        Collection<?> children =
            _getChildren(replacement, false, false, true, true);
        for (Object child : children) {
            _initPatternToReplacement((NamedObj) child);
        }
    }

    private void _initReplacementObjectAttributes(NamedObj replacement)
    throws TransformationException {
        _setReplacementObjectAttribute(replacement,
                _getCodeFromObject(replacement, _replacement));

        Collection<?> children =
            _getChildren(replacement, false, true, true, true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            _initReplacementObjectAttributes(child);
        }
    }

    private void _initReplacementToHost() throws TransformationException {
        _replacementToHost = new TwoWayHashMap<NamedObj, NamedObj>();
        for (NamedObj pattern : _patternToReplacement.keySet()) {
            NamedObj replacement = _patternToReplacement.get(pattern);
            NamedObj host = (NamedObj) _matchResult.get(pattern);
            if (host != null) {
                _replacementToHost.put(replacement, host);
                _setReplacementObjectAttribute(host,
                        _getCodeFromObject(replacement, _replacement));
            }
        }
    }

    private void _removeObject(NamedObj object) throws TransformationException {
        if (object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            CompositeEntity container = (CompositeEntity) entity.getContainer();
            Map<NamedObj, NamedObj> entityMap =
                new HashMap<NamedObj, NamedObj>();

            // Record the object codes in a temporary map.
            Collection<?> children =
                _getChildren(entity, false, false, true, true);
            List<Object> childrenRemoved = new LinkedList<Object>();
            childrenRemoved.addAll(children);

            // Remove the composite entity here because when the objects are
            // added back, their names will not conflict with the name of this
            // composite entity.
            MoMLChangeRequest request =
                GTTools.getDeletionChangeRequest(this, object);
            request.execute();
            _removeReplacementToHostEntries(object);

            // Move the entities and relations inside to the outside.
            for (Object childObject : childrenRemoved) {
                NamedObj child = (NamedObj) childObject;
                String moml = "<group name=\"auto\">\n"
                        + child.exportMoMLPlain() + "</group>";
                request = new MoMLChangeRequest(this, container, moml);
                request.execute();
                NamedObj newlyAddedObject =
                    _getNewlyAddedObject(container, child.getClass());
                _addReplacementToHostEntries(newlyAddedObject);
                entityMap.put(child, newlyAddedObject);
            }

            // Fix the connections between the moved entities and relations.
            for (NamedObj originalObject : entityMap.keySet()) {
                if (originalObject instanceof Relation) {
                    Relation originalRelation = (Relation) originalObject;
                    Relation relation1 =
                        (Relation) entityMap.get(originalRelation);
                    for (Object linkedObject
                            : originalRelation.linkedObjectsList()) {
                        if (linkedObject instanceof Relation) {
                            Relation relation2 =
                                (Relation) entityMap.get(linkedObject);
                            String moml = "<link relation1=\""
                                + relation1.getName() + "\" relation2=\""
                                + relation2.getName() + "\"/>";
                            request =
                                new MoMLChangeRequest(this, container, moml);
                            request.execute();
                        } else if (linkedObject instanceof Port) {
                            Port originalPort = (Port) linkedObject;
                            Entity linkedEntity = (Entity) entityMap.get(
                                    originalPort.getContainer());
                            if (linkedEntity != null) {
                                Port port2 = linkedEntity.getPort(
                                        originalPort.getName());
                                String moml = "<link port=\""
                                    + linkedEntity.getName() + "."
                                    + port2.getName() + "\" relation=\""
                                    + relation1.getName() + "\"/>";
                                request = new MoMLChangeRequest(this, container,
                                        moml);
                                request.execute();
                            }
                        }
                    }
                }
            }
        } else {
            MoMLChangeRequest request =
                GTTools.getDeletionChangeRequest(this, object);
            request.execute();
        }
    }

    private void _removeObjects(CompositeEntity replacement,
            CompositeEntity host) throws TransformationException {
        List<NamedObj> childrenToRemove = new LinkedList<NamedObj>();
        boolean boxingRemoved = true;
        while (boxingRemoved) {
            // FIXME: Consider attributes.
            Collection<?> children =
                _getChildren(host, false, false, true, true);
            childrenToRemove.clear();
            for (Object childObject : children) {
                NamedObj child = (NamedObj) childObject;
                NamedObj replacementChild =
                    (NamedObj) _replacementToHost.getKey(child);

                if (childObject instanceof CompositeEntity) {
                    if (replacementChild == null
                            && _matchResult.containsValue(child)
                            || replacementChild != null
                            && replacementChild.getContainer() != replacement) {
                        childrenToRemove.add(child);
                    }
                }
            }
            for (NamedObj child : childrenToRemove) {
                _removeObject(child);
            }
            boxingRemoved = childrenToRemove.size() > 0;
        }

        Collection<?> children = _getChildren(host, false, false, true, true);
        childrenToRemove.clear();
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            NamedObj replacementChild =
                (NamedObj) _replacementToHost.getKey(child);

            if (!(childObject instanceof CompositeEntity)) {
                if (replacementChild == null
                        && _matchResult.containsValue(child)
                        || replacementChild != null
                        && replacementChild.getContainer() != replacement) {
                    childrenToRemove.add(child);
                }
            }
        }
        for (NamedObj child : childrenToRemove) {
            _removeObject(child);
        }
    }

    private void _removeReplacementObjectAttributes(NamedObj object) {
        ReplacementObjectAttribute attribute =
            _getReplacementObjectAttribute(object);
        if (attribute != null) {
            try {
                attribute.setContainer(null);
            } catch (IllegalActionException e) {
                // Impossible.
            } catch (NameDuplicationException e) {
                // Impossible.
            }
        }

        Collection<?> children = _getChildren(object, false, true, true, true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            _removeReplacementObjectAttributes(child);
        }
    }

    private void _removeReplacementToHostEntries(NamedObj host) {
        ReplacementObjectAttribute attribute =
            _getReplacementObjectAttribute(host);
        if (attribute != null) {
            String replacementCode = attribute.getExpression();
            NamedObj replacement =
                _getObjectFromCode(replacementCode, _replacement);
            _replacementToHost.remove(replacement);
        }

        Collection<?> children = _getChildren(host, false, true, true, true);
        for (Object childObject : children) {
            NamedObj child = (NamedObj) childObject;
            _removeReplacementToHostEntries(child);
        }
    }

    private void _setReplacementObjectAttribute(NamedObj object,
            String replacementObjectCode)
    throws TransformationException {
        try {
            ReplacementObjectAttribute attribute =
                _getReplacementObjectAttribute(object);
            if (attribute == null) {
                attribute =
                    new ReplacementObjectAttribute(object,
                            "replacementObject");
            }
            attribute.setExpression(replacementObjectCode);
        } catch (KernelException e) {
            throw new TransformationException("Cannot set replacementObject "
                    + "attributes", e);
        }
    }

    private CompositeEntity _host;

    private MatchResult _matchResult;

    private Map<NamedObj, String> _moml;

    private Pattern _pattern;

    private TwoWayHashMap<NamedObj, NamedObj> _patternToReplacement;

    private Replacement _replacement;

    private TwoWayHashMap<NamedObj, NamedObj> _replacementToHost;

}
