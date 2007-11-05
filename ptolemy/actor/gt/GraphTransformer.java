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
import java.util.Set;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.TwoWayHashMap;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
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
public class GraphTransformer {

    public void transform(TransformationRule transformer,
            MatchResult matchResult, CompositeEntity hostGraph)
    throws TransformationException {
        Pattern pattern = transformer.getPattern();
        Replacement replacement = transformer.getReplacement();
        CompositeEntity hostContainer =
            (CompositeEntity) matchResult.get(pattern);
        if (hostContainer == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not contain the pattern.");
        }

        _matchResult = matchResult;

        _entityMap = new TwoWayHashMap<GTEntity, GTEntity>();
        _initEntityMap(pattern, replacement);

        _deletedObjects = new HashSet<NamedObj>();

        _removeEntities(pattern);
        _performOperations();
        _addEntities(hostContainer, replacement);
        hostContainer.requestChange(new SubstituteEntitiesChangeRequest(
                pattern, hostContainer));
    }

    private void _addEntities(CompositeEntity hostContainer,
            CompositeEntity replacementContainer) {
        for (Object replacementObject : replacementContainer.entityList()) {
            ComponentEntity patternEntity =
                (ComponentEntity) _entityMap.getKey(replacementObject);
            if (patternEntity == null) {
                // replacementObject is newly added.
                ComponentEntity replacementEntity =
                    (ComponentEntity) replacementObject;
                String moml = replacementEntity.exportMoML();
                _addEntityMoMLRequest(hostContainer, moml);
            } else {
                NamedObj match = (NamedObj) _matchResult.get(patternEntity);
                if (replacementObject instanceof CompositeEntity) {
                    _addEntities((CompositeEntity) match,
                            (CompositeEntity) replacementObject);
                }
                if (match.getContainer() != hostContainer) {
                    String moml = match.exportMoML();
                    _addEntityMoMLRequest(hostContainer, moml);
                }
            }
        }
    }

    private void _addEntityMoMLRequest(NamedObj context, String moml) {
        moml = "<group name=\"auto\">" + moml + "</group>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, context, moml);
        context.requestChange(request);
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
                    match.requestChange(request);
                }
            } catch (MalformedStringException e) {
                throw new TransformationException(
                        "Cannot parse operation list.");
            }
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
                MoMLChangeRequest request =
                    new MoMLChangeRequest(this, container, moml);
                container.requestChange(request);
            }
        }
    }

    private Set<NamedObj> _deletedObjects;

    private TwoWayHashMap<GTEntity, GTEntity> _entityMap;

    private MatchResult _matchResult;

    private class SubstituteEntitiesChangeRequest extends ChangeRequest {

        public SubstituteEntitiesChangeRequest(Pattern pattern,
                CompositeEntity hostContainer) {
            super(GraphTransformer.this, "Substitute replace entities.");

            _hostContainer = hostContainer;
            _pattern = pattern;
        }

        protected void _execute() throws Exception {
            _substitute(_pattern, _hostContainer);
        }

        private void _substitute(Pattern pattern,
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
                        NamedObj match =
                            (NamedObj) _matchResult.get(patternEntity);
                        NamedObj container = match.getContainer();
                        String moml;
                        MoMLChangeRequest request;

                        if (match != hostEntity) {
                            moml = "<deleteEntity name=\""
                                + hostEntity.getName() + "\"/>";
                            request = new MoMLChangeRequest(
                                    GraphTransformer.this, hostContainer, moml);
                            hostContainer.requestChange(request);

                            if (container != hostContainer) {
                                if (container != null) {
                                    moml = "<deleteEntity name=\""
                                        + match.getName() + "\"/>";
                                    request = new MoMLChangeRequest(
                                            GraphTransformer.this, container,
                                            moml);
                                    container.requestChange(request);
                                }
                                moml = match.exportMoML();
                                request = new MoMLChangeRequest(
                                        GraphTransformer.this, hostContainer,
                                        moml);
                                hostContainer.requestChange(request);
                            }
                        }
                    }
                }

                if (hostObject instanceof CompositeEntity) {
                    _substitute(pattern, (CompositeEntity) hostObject);
                }
            }
        }

        private CompositeEntity _hostContainer;

        private Pattern _pattern;
    }
}
