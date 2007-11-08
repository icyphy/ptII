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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.TwoWayHashMap;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
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
            MatchResult matchResult) {
        super(null, "Apply graph transformation to model.");

        _pattern = transformationRule.getPattern();
        _replacement = transformationRule.getReplacement();
        _matchResult = matchResult;
    }

    public static void transform(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        CompositeEntity hostGraph =
            (CompositeEntity) matchResult.get(transformationRule.getPattern());
        if (hostGraph == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not include the pattern.");
        }
        hostGraph.requestChange(
                new GraphTransformer(transformationRule, matchResult));
    }

    protected void _execute() throws TransformationException {
        CompositeEntity hostGraph =
            (CompositeEntity) _matchResult.get(_pattern);
        if (hostGraph == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not include the pattern.");
        }

        _entityMap = new TwoWayHashMap<NamedObj, NamedObj>();
        _initObjectMap(_pattern, _replacement);

        _deletedObjects = new HashSet<NamedObj>();
        _replacementHostCorrespondence =
            new TwoWayHashMap<NamedObj, NamedObj>();

        _removeObjects(_pattern);
        _performOperations();
        _analyzeReplacementHostCorrespondence(_replacement);
        _addObjects(hostGraph, _replacement);
        _substituteGTEntities(hostGraph, _pattern);
        _addConnections(hostGraph, _replacement);
    }

    private void _addConnections(CompositeEntity hostGraph,
            CompositeEntity replacementContainer)
    throws TransformationException {
        for (NamedObj replacementObject
                : _replacementHostCorrespondence.keySet()) {
            if (!(replacementObject instanceof Port)) {
                continue;
            }

            Port replacementPort = (Port) replacementObject;
            Port hostPort =
                (Port) _replacementHostCorrespondence.get(replacementPort);
            for (Object connectedReplacementRelationObject
                    : replacementPort.linkedRelationList()) {
                Relation connectedReplacementRelation =
                    (Relation) connectedReplacementRelationObject;
                Relation correspondingHostRelation =
                    (Relation) _replacementHostCorrespondence.get(
                            connectedReplacementRelation);
                if (!hostPort.linkedRelationList().contains(
                        correspondingHostRelation)) {
                    // There is no connection between hostEntity and
                    // correspondingHostEntity, so create a new connection.
                    // FIXME: take into account existing relations.
                    Entity hostPortContainer = (Entity) hostPort.getContainer();
                    NamedObj relationContainer =
                        correspondingHostRelation.getContainer();
                    String moml = "<link port=\"" + hostPortContainer.getName()
                            + "." + hostPort.getName() + "\" relation=\""
                            + correspondingHostRelation.getName() + "\"/>";
                    new MoMLChangeRequest(this, relationContainer, moml)
                            .execute();
                }
            }
        }
    }

    private void _addEntityMoMLRequest(NamedObj context, String moml) {
        moml = "<group name=\"auto\">" + moml + "</group>";
        new MoMLChangeRequest(this, context, moml).execute();
    }

    private void _addObjects(CompositeEntity hostContainer,
            CompositeEntity replacementContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        (Collection<?>) replacementContainer.entityList(),
                        (Collection<?>) replacementContainer.relationList()
                });
        for (Object replacementObjectObject : objectCollection) {
            NamedObj replacementObject = (NamedObj) replacementObjectObject;
            NamedObj patternObject =
                (NamedObj) _entityMap.getKey(replacementObject);
            if (patternObject == null) {
                // replacementObject is newly added.
                String moml = replacementObject.exportMoML();
                _addEntityMoMLRequest(hostContainer, moml);

                List<?> objectList;
                if (replacementObject instanceof Relation) {
                    objectList = hostContainer.relationList();
                } else {
                    objectList = hostContainer.entityList();
                }
                int lastIndex = objectList.size() - 1;
                _recordReplacementHostCorrespondence(replacementObject,
                        (NamedObj) objectList.get(lastIndex));
            } else {
                NamedObj match = (NamedObj) _matchResult.get(patternObject);
                if (replacementObject instanceof CompositeEntity) {
                    _addObjects((CompositeEntity) match,
                            (CompositeEntity) replacementObject);
                }
                if (match.getContainer() != hostContainer) {
                    String moml = match.exportMoML();
                    _addEntityMoMLRequest(hostContainer, moml);

                    List<?> objectList;
                    if (replacementObject instanceof Relation) {
                        objectList = hostContainer.relationList();
                    } else {
                        objectList = hostContainer.entityList();
                    }
                    int lastIndex = objectList.size() - 1;
                    _recordReplacementHostCorrespondence(replacementObject,
                            (NamedObj) objectList.get(lastIndex));
                }
            }
        }
    }

    private void _analyzeReplacementHostCorrespondence(
            CompositeEntity replacementContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        replacementContainer.entityList(),
                        replacementContainer.relationList()
                });
        for (Object replacementObjectObject : objectCollection) {
            NamedObj replacementObject = (NamedObj) replacementObjectObject;
            NamedObj patternObject =
                (NamedObj) _entityMap.getKey(replacementObject);
            if (patternObject != null) {
                NamedObj match = (NamedObj) _matchResult.get(patternObject);
                if (match == null) {
                    continue;
                }

                _replacementHostCorrespondence.put(replacementObject, match);
                if (!(replacementObject instanceof ComponentEntity)) {
                    continue;
                }

                ComponentEntity replacementEntity =
                    (ComponentEntity) replacementObject;
                ComponentEntity patternEntity = (ComponentEntity) patternObject;
                List<?> patternPortList = patternEntity.portList();
                List<?> replacementPortList = replacementEntity.portList();
                for (int i = 0; i < patternPortList.size(); i++) {
                    Port patternPort = (Port) patternPortList.get(i);
                    Port replacementPort = (Port) replacementPortList.get(i);
                    Port matchPort = (Port) _matchResult.get(patternPort);
                    _replacementHostCorrespondence.put(replacementPort,
                            matchPort);
                }
            }

            if (replacementObject instanceof CompositeEntity) {
                _analyzeReplacementHostCorrespondence(
                        (CompositeEntity) replacementObject);
            }
        }
    }

    private void _initObjectMap(Pattern pattern,
            CompositeEntity replacementContainer)
    throws TransformationException {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        replacementContainer.entityList(),
                        replacementContainer.relationList()
                });
        for (Object replacementObjectObject : objectCollection) {
            NamedObj replacementObject = (NamedObj) replacementObjectObject;

            if (replacementObject instanceof CompositeEntity) {
                _initObjectMap(pattern, (CompositeEntity) replacementObject);
            }

            PatternObjectAttribute attribute =
                GTTools.getPatternObjectAttribute(replacementObject,
                        false);
            if (attribute == null) {
                continue;
            }

            String patternObjectName = attribute.getExpression();
            NamedObj patternObject = pattern.getEntity(patternObjectName);
            if (patternObject == null) {
                throw new TransformationException("Object in the pattern with "
                        + "name \"" + patternObjectName + "\" not found.");
            }

            _entityMap.put(patternObject, replacementObject);
        }
    }

    private void _performOperations() throws TransformationException {
        for (NamedObj patternObject : _entityMap.keySet()) {
            NamedObj replacementObject = _entityMap.get(patternObject);
            NamedObj match =
                (NamedObj) _matchResult.get(replacementObject);
            if (patternObject instanceof GTEntity
                    && replacementObject instanceof GTEntity) {
                try {
                    GTEntity patternEntity = (GTEntity) patternObject;
                    GTEntity replacementEntity = (GTEntity) replacementObject;
                    GTIngredientList ingredientList = replacementEntity
                            .getOperationsAttribute().getIngredientList();
                    for (GTIngredient ingredient : ingredientList) {
                        ChangeRequest request =
                            ((Operation) ingredient).getChangeRequest(
                                    patternEntity, replacementEntity,
                                    (ComponentEntity) match);
                        request.execute();
                    }
                } catch (MalformedStringException e) {
                    throw new TransformationException(
                            "Cannot parse operation list.");
                }
            }
        }
    }

    private void _recordReplacementHostCorrespondence(
            NamedObj replacementObject, NamedObj hostObject) {
        _replacementHostCorrespondence.put(replacementObject, hostObject);

        if (replacementObject instanceof ComponentEntity) {
            ComponentEntity replacementEntity =
                (ComponentEntity) replacementObject;
            ComponentEntity hostEntity = (ComponentEntity) hostObject;
            List<?> replacementPortList = replacementEntity.portList();
            List<?> hostPortList = hostEntity.portList();
            for (int i = 0; i < replacementPortList.size(); i++) {
                _replacementHostCorrespondence.put(
                        (Port) replacementPortList.get(i),
                        (Port) hostPortList.get(i));
            }
        }
    }

    private void _removeObjects(CompositeEntity patternContainer)
    throws TransformationException {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        patternContainer.entityList(),
                        patternContainer.relationList()
                });
        for (Object patternObject : objectCollection) {
            if (patternObject instanceof CompositeEntity) {
                _removeObjects((CompositeEntity) patternObject);
            }

            if (!_entityMap.containsKey(patternObject)) {
                // patternObject is removed in the replacement.
                NamedObj match = (NamedObj) _matchResult.get(patternObject);
                if (match != null) {
                    NamedObj container = match.getContainer();
                    _deletedObjects.add(match);
                    String moml =
                        "<deleteEntity name=\"" + match.getName() + "\"/>";
                    new MoMLChangeRequest(this, container, moml).execute();
                }
            }
        }
    }

    private void _substituteGTEntities(CompositeEntity hostContainer,
            Pattern pattern) {
        for (Object hostObject : hostContainer.entityList()) {
            if (hostObject instanceof GTEntity) {
                GTEntity hostEntity = (GTEntity) hostObject;
                PatternObjectAttribute pattenrEntityAttribute =
                    GTTools.getPatternObjectAttribute(
                            (NamedObj) hostEntity, false);
                ComponentEntity patternEntity = null;
                if (pattenrEntityAttribute != null) {
                    String patternEntityName =
                        pattenrEntityAttribute.getExpression();
                    patternEntity = pattern.getEntity(patternEntityName);
                }

                if (patternEntity != null) {
                    NamedObj match = (NamedObj) _matchResult.get(patternEntity);
                    NamedObj container = match.getContainer();
                    String moml;

                    if (match != hostEntity) {
                        moml = "<deleteEntity name=\"" + hostEntity.getName()
                                + "\"/>";
                        new MoMLChangeRequest(GraphTransformer.this,
                                hostContainer, moml).execute();

                        if (container != hostContainer) {
                            if (container != null) {
                                moml = "<deleteEntity name=\"" + match.getName()
                                        + "\"/>";
                                new MoMLChangeRequest(GraphTransformer.this,
                                        container, moml).execute();
                            }
                            moml = match.exportMoML();
                            new MoMLChangeRequest(GraphTransformer.this,
                                    hostContainer, moml).execute();
                        }
                    }
                }
            }

            if (hostObject instanceof CompositeEntity) {
                _substituteGTEntities((CompositeEntity) hostObject, pattern);
            }
        }
    }

    private Set<NamedObj> _deletedObjects;

    private TwoWayHashMap<NamedObj, NamedObj> _entityMap;

    private MatchResult _matchResult;

    private Pattern _pattern;

    private Replacement _replacement;

    private TwoWayHashMap<NamedObj, NamedObj> _replacementHostCorrespondence;
}
