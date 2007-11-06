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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.TwoWayHashMap;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
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

    public GraphTransformer(TransformationRule transformer,
            MatchResult matchResult) {
        super(null, "Apply graph transformation to model.");

        _pattern = transformer.getPattern();
        _replacement = transformer.getReplacement();
        _matchResult = matchResult;
    }

    public static void transform(TransformationRule transformer,
            MatchResult matchResult) {
        CompositeEntity hostGraph =
            (CompositeEntity) matchResult.get(transformer.getPattern());
        hostGraph.requestChange(new GraphTransformer(transformer, matchResult));
    }

    protected void _execute() throws TransformationException {
        CompositeEntity hostContainer =
            (CompositeEntity) _matchResult.get(_pattern);
        if (hostContainer == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not contain the pattern.");
        }

        _entityMap = new TwoWayHashMap<GTEntity, GTEntity>();
        _initEntityMap(_pattern, _replacement);

        _deletedObjects = new HashSet<NamedObj>();
        _replacementHostCorrespondence =
            new TwoWayHashMap<NamedObj, NamedObj>();

        _removeEntities(_pattern);
        _performOperations();
        _analyzeReplacementHostCorrespondence(_replacement);
        _addEntities(hostContainer, _replacement);
        _substituteGTEntities(_pattern, hostContainer);
        _addConnections(hostContainer, _replacement);
    }

    private void _addConnections(CompositeEntity hostContainer,
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
            for (Object connectedReplacementPortObject
                    : replacementPort.connectedPortList()) {
                Port connectedReplacementPort =
                    (Port) connectedReplacementPortObject;
                Port correspondingHostPort =
                    (Port) _replacementHostCorrespondence.get(
                            connectedReplacementPort);
                if (!hostPort.connectedPortList().contains(
                        correspondingHostPort)) {
                    // There is no connection between hostEntity and
                    // correspondingHostEntity, so create a new connection.
                    // FIXME: take into account existing relations.
                    Entity hostEntity1 = (Entity) hostPort.getContainer();
                    Entity hostEntity2 =
                        (Entity) correspondingHostPort.getContainer();
                    String hostPort1 = hostEntity1.getName() + "."
                            + hostPort.getName();
                    String hostPort2 = hostEntity2.getName() + "."
                            + correspondingHostPort.getName();
                    String relationName =
                        hostContainer.uniqueName("relation");
                    String moml = "<group>"
                            + "<relation name=\"" + relationName + "\"/>"
                            + "<link port=\"" + hostPort1 + "\" relation=\""
                            + relationName + "\"/>"
                            + "<link port=\"" + hostPort2 + "\" relation=\""
                            + relationName + "\"/>"
                            + "</group>";
                    new MoMLChangeRequest(this, hostContainer, moml)
                            .execute();
                }
            }
        }
    }

    private void _addEntities(CompositeEntity hostContainer,
            CompositeEntity replacementContainer) {
        for (Object replacementObject : replacementContainer.entityList()) {
            Entity replacementEntity = (ComponentEntity) replacementObject;
            Entity patternEntity =
                (Entity) _entityMap.getKey(replacementObject);
            if (patternEntity == null) {
                // replacementObject is newly added.
                String moml = replacementEntity.exportMoML();
                _addEntityMoMLRequest(hostContainer, moml);

                List<?> newHostEntityList = hostContainer.entityList();
                int lastIndex = newHostEntityList.size() - 1;
                _recordReplacementHostCorrespondence(replacementEntity,
                        (ComponentEntity) newHostEntityList.get(lastIndex));
            } else {
                NamedObj match = (NamedObj) _matchResult.get(patternEntity);
                if (replacementObject instanceof CompositeEntity) {
                    _addEntities((CompositeEntity) match,
                            (CompositeEntity) replacementEntity);
                }
                if (match.getContainer() != hostContainer) {
                    String moml = match.exportMoML();
                    _addEntityMoMLRequest(hostContainer, moml);

                    List<?> newHostEntityList = hostContainer.entityList();
                    int lastIndex = newHostEntityList.size() - 1;
                    _recordReplacementHostCorrespondence(replacementEntity,
                            (Entity) newHostEntityList.get(lastIndex));
                }
            }
        }
    }

    private void _addEntityMoMLRequest(NamedObj context, String moml) {
        moml = "<group name=\"auto\">" + moml + "</group>";
        new MoMLChangeRequest(this, context, moml).execute();
    }

    private void _analyzeReplacementHostCorrespondence(
            CompositeEntity replacementContainer) {
        for (Object replacementObject : replacementContainer.entityList()) {
            ComponentEntity replacementEntity =
                (ComponentEntity) replacementObject;
            ComponentEntity patternEntity =
                (ComponentEntity) _entityMap.getKey(replacementEntity);
            if (patternEntity != null) {
                ComponentEntity match =
                    (ComponentEntity) _matchResult.get(patternEntity);
                _replacementHostCorrespondence.put(replacementEntity, match);
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

            if (replacementEntity instanceof CompositeEntity) {
                _analyzeReplacementHostCorrespondence(
                        (CompositeEntity) replacementEntity);
            }
        }
    }

    private void _initEntityMap(Pattern pattern,
            CompositeEntity replacementContainer)
    throws TransformationException {
        for (Object replacementObject : replacementContainer.entityList()) {
            if (replacementObject instanceof CompositeEntity) {
                _initEntityMap(pattern, (CompositeEntity) replacementObject);
            }

            if (!(replacementObject instanceof GTEntity)) {
                continue;
            }

            GTEntity replacementEntity = (GTEntity) replacementObject;
            PatternEntityAttribute attribute =
                replacementEntity.getPatternEntityAttribute();
            if (attribute == null) {
                continue;
            }

            String patternEntityName = attribute.getExpression();
            GTEntity patternEntity =
                (GTEntity) pattern.getEntity(patternEntityName);
            if (patternEntity == null) {
                throw new TransformationException("Entity in the pattern with "
                        + "name \"" + patternEntityName + "\" not found.");
            }

            _entityMap.put(patternEntity, replacementEntity);
        }
    }

    private void _performOperations() throws TransformationException {
        for (GTEntity patternEntity : _entityMap.keySet()) {
            GTEntity replacementEntity = _entityMap.get(patternEntity);
            try {
                ComponentEntity match =
                    (ComponentEntity) _matchResult.get(patternEntity);
                GTIngredientList ingredientList = replacementEntity
                        .getOperationsAttribute().getIngredientList();
                for (GTIngredient ingredient : ingredientList) {
                    ChangeRequest request =
                        ((Operation) ingredient).getChangeRequest(
                                patternEntity, replacementEntity, match);
                    request.execute();
                }
            } catch (MalformedStringException e) {
                throw new TransformationException(
                        "Cannot parse operation list.");
            }
        }
    }

    private void _recordReplacementHostCorrespondence(Entity replacementEntity,
            Entity hostEntity) {
        _replacementHostCorrespondence.put(replacementEntity, hostEntity);
        List<?> replacementPortList = replacementEntity.portList();
        List<?> hostPortList = hostEntity.portList();
        for (int i = 0; i < replacementPortList.size(); i++) {
            _replacementHostCorrespondence.put(
                    (Port) replacementPortList.get(i),
                    (Port) hostPortList.get(i));
        }
    }

    private void _removeEntities(CompositeEntity patternContainer)
    throws TransformationException {
        for (Object patternObject : patternContainer.entityList()) {
            if (patternObject instanceof CompositeEntity) {
                _removeEntities((CompositeEntity) patternObject);
            }

            if (!_entityMap.containsKey(patternObject)) {
                // patternObject is removed in the replacement.
                ComponentEntity match =
                    (ComponentEntity) _matchResult.get(patternObject);
                NamedObj container = match.getContainer();

                _deletedObjects.add(match);
                String moml =
                    "<deleteEntity name=\"" + match.getName() + "\"/>";
                new MoMLChangeRequest(this, container, moml).execute();
            }
        }
    }

    private void _substituteGTEntities(Pattern pattern,
            CompositeEntity hostContainer) {
        for (Object hostObject : hostContainer.entityList()) {
            if (hostObject instanceof GTEntity) {
                GTEntity hostEntity = (GTEntity) hostObject;
                PatternEntityAttribute pattenrEntityAttribute =
                    hostEntity.getPatternEntityAttribute();
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
                _substituteGTEntities(pattern, (CompositeEntity) hostObject);
            }
        }
    }

    private Set<NamedObj> _deletedObjects;

    private TwoWayHashMap<GTEntity, GTEntity> _entityMap;

    private MatchResult _matchResult;

    private Pattern _pattern;

    private Replacement _replacement;

    private TwoWayHashMap<NamedObj, NamedObj> _replacementHostCorrespondence;
}
