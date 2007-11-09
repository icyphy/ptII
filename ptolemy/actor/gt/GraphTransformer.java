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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import ptolemy.kernel.util.Attribute;
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
            MatchResult matchResult) throws TransformationException {
        super(null, "Apply graph transformation to model.");

        _pattern = transformationRule.getPattern();
        _replacement = transformationRule.getReplacement();
        _matchResult = matchResult;
        _hostGraph = (CompositeEntity) _matchResult.get(_pattern);

        if (_hostGraph == null) {
            throw new TransformationException("Match result is invalid because "
                    + "it does not include the pattern.");
        }
    }

    public static void transform(TransformationRule transformationRule,
            MatchResult matchResult) throws TransformationException {
        GraphTransformer transformer =
            new GraphTransformer(transformationRule, matchResult);
        transformer._hostGraph.requestChange(transformer);
    }

    protected void _execute() throws TransformationException {
        _deletedObjects = new HashSet<NamedObj>();
        _patternToReplacement = new TwoWayHashMap<String, String>();
        _replacementToHost = new TwoWayHashMap<String, String>();

        _initPatternReplacementCorrespondence(_replacement);
        _initReplacementHostCorrespondence();

        _removeObjects(_pattern);
        _performOperations();
        _addObjects(_hostGraph, _replacement);
        _substituteGTEntities(_hostGraph);
        _addConnections();
    }

    private void _addConnections() throws TransformationException {
        for (String replacementObjectCode : _replacementToHost.keySet()) {
            if (!replacementObjectCode.startsWith("P:")) {
                continue;
            }

            Port replacementPort =
                (Port) _getObjectFromCode(replacementObjectCode, _replacement);
            String hostPortCode = _replacementToHost.get(replacementObjectCode);
            Port hostPort = (Port) _getObjectFromCode(hostPortCode, _hostGraph);
            for (Object replacementRelationObject
                    : replacementPort.linkedRelationList()) {
                Relation replacementRelation =
                    (Relation) replacementRelationObject;
                String replacementRelationCode =
                    _getCodeFromObject(replacementRelation, _replacement);
                String hostRelationCode =
                    _replacementToHost.get(replacementRelationCode);
                Relation hostRelation =
                    (Relation) _getObjectFromCode(hostRelationCode, _hostGraph);
                if (!hostPort.linkedRelationList().contains(hostRelation)) {
                    // There is no link between hostPort and
                    // correspondingHostRelation, so create a new link.
                    Entity hostPortContainer = (Entity) hostPort.getContainer();
                    NamedObj hostContainer = hostRelation.getContainer();
                    String moml = "<link port=\"" + hostPortContainer.getName()
                            + "." + hostPort.getName() + "\" relation=\""
                            + hostRelation.getName() + "\"/>";
                    new MoMLChangeRequest(this, hostContainer, moml)
                            .execute();
                }
            }
        }
    }

    private void _addObjectMoMLRequest(NamedObj context, String moml) {
        moml = "<group name=\"auto\">" + moml + "</group>";
        new MoMLChangeRequest(this, context, moml).execute();
    }

    private void _addObjects(CompositeEntity hostContainer,
            CompositeEntity replacementContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        replacementContainer.attributeList(),
                        replacementContainer.entityList(),
                        replacementContainer.portList(),
                        replacementContainer.relationList()
                });
        for (Object replacementObjectRaw : objectCollection) {
            NamedObj replacementObject = (NamedObj) replacementObjectRaw;
            String replacementObjectCode =
                _getCodeFromObject(replacementObject, _replacement);
            String patternObjectCode =
                _patternToReplacement.getKey(replacementObjectCode);
            if (patternObjectCode == null) {
                // replacementObject is newly added.
                String moml = replacementObject.exportMoML();
                _addObjectMoMLRequest(hostContainer, moml);

                _recordAddedObject(hostContainer, replacementObject,
                        replacementObject);
            } else {
                NamedObj patternObject =
                    _getObjectFromCode(patternObjectCode, _pattern);
                NamedObj match = (NamedObj) _matchResult.get(patternObject);
                if (match == null) {
                    continue;
                }

                if (replacementObject instanceof CompositeEntity) {
                    _addObjects((CompositeEntity) match,
                            (CompositeEntity) replacementObject);
                }
                if (match.getContainer() != hostContainer) {
                    String moml = match.exportMoML();
                    _addObjectMoMLRequest(hostContainer, moml);

                    _recordAddedObject(hostContainer, match, replacementObject);
                }
            }
        }
    }

    private String _getCodeFromObject(NamedObj object,
            CompositeEntity topContainer) {
        String replacementAbbrev = _getTypeAbbreviation(object);
        return replacementAbbrev + object.getName(topContainer);
    }

    private NamedObj _getObjectFromCode(String code, NamedObj topContainer) {
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

    private String _getTypeAbbreviation(NamedObj object) {
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

    private void _initPatternReplacementCorrespondence(
            CompositeEntity replacementContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        replacementContainer.attributeList(),
                        replacementContainer.entityList(),
                        replacementContainer.portList(),
                        replacementContainer.relationList()
                });
        for (Object replacementObjectRaw : objectCollection) {
            NamedObj replacementObject = (NamedObj) replacementObjectRaw;

            if (replacementObject instanceof CompositeEntity) {
                _initPatternReplacementCorrespondence(
                        (CompositeEntity) replacementObject);
            }

            NamedObj patternObject =
                GTTools.getCorrespondingPatternObject(replacementObject);

            if (patternObject != null) {
                _putPatternReplacementCorrespondence(patternObject,
                        replacementObject);
                
                if (!(patternObject instanceof ComponentEntity
                        && replacementObject instanceof ComponentEntity)) {
                    continue;
                }

                ComponentEntity patternEntity = (ComponentEntity) patternObject;
                ComponentEntity replacementEntity =
                    (ComponentEntity) replacementObject;
                List<?> patternPortList = patternEntity.portList();
                List<?> replacementPortList = replacementEntity.portList();
                for (int i = 0; i < patternPortList.size(); i++) {
                    Port patternPort = (Port) patternPortList.get(i);
                    Port replacementPort = (Port) replacementPortList.get(i);
                    _putPatternReplacementCorrespondence(patternPort,
                            replacementPort);
                }
            }
        }
    }

    private void _initReplacementHostCorrespondence() {
        Map<NamedObj, NamedObj> newCorrespondence =
            new HashMap<NamedObj, NamedObj>();
        for (String patternObjectCode : _patternToReplacement.keySet()) {
            NamedObj patternObject =
                _getObjectFromCode(patternObjectCode, _pattern);
            String replacementObjectCode =
                _patternToReplacement.get(patternObjectCode);
            NamedObj replacementObject =
                _getObjectFromCode(replacementObjectCode, _replacement);
            NamedObj hostObject = (NamedObj) _matchResult.get(patternObject);
            if (hostObject != null) {
                newCorrespondence.put(replacementObject, hostObject);
            }
        }
        for (NamedObj replacementObject : newCorrespondence.keySet()) {
            _putReplacementHostCorrespondence(replacementObject,
                    newCorrespondence.get(replacementObject));
        }
    }

    private boolean _isContainedIn(String objectCode, String containerName) {
        int length1 = objectCode.length();
        int length2 = containerName.length();
        if (length1 == length2 + 2) {
            return objectCode.endsWith(containerName);
        } else if (length1 > length2 + 2) {
            return objectCode.substring(2, length2 + 3).equals(
                    containerName + ".");
        } else {
            return false;
        }
    }

    private void _performOperations() throws TransformationException {
        for (String patternObjectCode : _patternToReplacement.keySet()) {
            NamedObj patternObject =
                _getObjectFromCode(patternObjectCode, _pattern);
            String replacementObjectCode =
                _patternToReplacement.get(patternObjectCode);
            NamedObj replacementObject =
                _getObjectFromCode(replacementObjectCode, _replacement);
            NamedObj match = (NamedObj) _matchResult.get(patternObject);
            if (match == null) {
                continue;
            }

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

    private void _putPatternReplacementCorrespondence(NamedObj patternObject,
            NamedObj replacementObject) {
        String patternAbbrev = _getTypeAbbreviation(patternObject);
        String replacementAbbrev = _getTypeAbbreviation(replacementObject);
        if (patternAbbrev != null && patternAbbrev.equals(replacementAbbrev)) {
            String patternObjectName = patternObject.getName(_pattern);
            String replacementObjectName =
                replacementObject.getName(_replacement);
            _patternToReplacement.put(patternAbbrev + patternObjectName,
                    replacementAbbrev + replacementObjectName);
        }
    }

    private void _putReplacementHostCorrespondence(NamedObj replacementObject,
            NamedObj hostObject) {
        String replacementAbbrev = _getTypeAbbreviation(replacementObject);
        String hostAbbrev = _getTypeAbbreviation(hostObject);
        if (replacementAbbrev != null && replacementAbbrev.equals(hostAbbrev)) {
            String replacementObjectName =
                replacementObject.getName(_replacement);
            String hostObjectName = hostObject.getName(_hostGraph);
            _replacementToHost.put(replacementAbbrev + replacementObjectName,
                    hostAbbrev + hostObjectName);
        }
    }

    private void _recordAddedObject(CompositeEntity container,
            NamedObj originalObject, NamedObj replacementObject) {
        // Get the newly added object.
        List<?> objectList;
        if (originalObject instanceof Relation) {
            objectList = container.relationList();
        } else if (originalObject instanceof Entity) {
            objectList = container.entityList();
        } else if (originalObject instanceof Port) {
            objectList = container.portList();
        } else {
            return;
        }

        int lastIndex = objectList.size() - 1;
        NamedObj newlyAdded = (NamedObj) objectList.get(lastIndex);

        _recordReplacementHostCorrespondence(replacementObject,
                newlyAdded);
    }

    private void _recordDeletedObject(NamedObj hostObject) {
        _deletedObjects.add(hostObject);
        String hostObjectName = hostObject.getName(_hostGraph);
        List<String> deletedReplacementCodes = new LinkedList<String>();
        for (String hostObjectCode : _replacementToHost.values()) {
            if (_isContainedIn(hostObjectCode, hostObjectName)) {
                deletedReplacementCodes.add(
                        _replacementToHost.getKey(hostObjectCode));
            }
        }
        for (String replacementCode : deletedReplacementCodes) {
            _replacementToHost.remove(replacementCode);
        }
    }

    private void _recordReplacementHostCorrespondence(
            NamedObj replacementObject, NamedObj hostObject) {
        String replacementObjectCode =
            _getCodeFromObject(replacementObject, _replacement);
        String hostObjectCode = _getCodeFromObject(hostObject, _hostGraph);
        _replacementToHost.put(replacementObjectCode, hostObjectCode);

        if (replacementObject instanceof ComponentEntity) {
            ComponentEntity replacementEntity =
                (ComponentEntity) replacementObject;
            ComponentEntity hostEntity = (ComponentEntity) hostObject;

            String patternEntityCode =
                _patternToReplacement.getKey(replacementObjectCode);

            if (patternEntityCode == null) {
                // The replacement entity is newly added.
                List<?> replacementPortList = replacementEntity.portList();
                List<?> hostPortList = hostEntity.portList();
                for (int i = 0; i < replacementPortList.size(); i++) {
                    Port replacementPort = (Port) replacementPortList.get(i);
                    Port hostPort = (Port) hostPortList.get(i);
                    String replacementPortCode =
                        _getCodeFromObject(replacementPort, _replacement);
                    String hostPortCode =
                        _getCodeFromObject(hostPort, _hostGraph);
                    _replacementToHost.put(replacementPortCode, hostPortCode);
                }
            } else {
                ComponentEntity patternEntity = (ComponentEntity)
                        _getObjectFromCode(patternEntityCode, _pattern);
                List<?> patternPortList = patternEntity.portList();
                List<?> replacementPortList = replacementEntity.portList();
                for (int i = 0; i < patternPortList.size(); i++) {
                    Port patternPort = (Port) patternPortList.get(i);
                    Port replacementPort = (Port) replacementPortList.get(i);
                    Port originalHostPort =
                        (Port) _matchResult.get(patternPort);
                    String hostPortName = originalHostPort.getName();
                    Port hostPort = hostEntity.getPort(hostPortName);
                    String replacementPortCode =
                        _getCodeFromObject(replacementPort, _replacement);
                    String hostPortCode =
                        _getCodeFromObject(hostPort, _hostGraph);
                    _replacementToHost.put(replacementPortCode, hostPortCode);
                }
            }
        }

        if (replacementObject instanceof CompositeEntity) {
            CompositeEntity replacementEntity =
                (CompositeEntity) replacementObject;
            CompositeEntity hostEntity = (CompositeEntity) hostObject;

            List<?> replacementRelationList = replacementEntity.relationList();
            List<?> hostRelationList = hostEntity.relationList();
            for (int i = 0; i < replacementRelationList.size(); i++) {
                Relation replacementRelation =
                    (Relation) replacementRelationList.get(i);
                Relation hostRelation = (Relation) hostRelationList.get(i);
                String replacementRelationCode =
                    _getCodeFromObject(replacementRelation, _replacement);
                String hostRelationCode =
                    _getCodeFromObject(hostRelation, _hostGraph);
                _replacementToHost.put(replacementRelationCode,
                        hostRelationCode);
            }

            List<?> replacementEntityList = replacementEntity.entityList();
            List<?> hostEntityList = hostEntity.entityList();
            for (int i = 0; i < replacementEntityList.size(); i++) {
                _recordReplacementHostCorrespondence(
                        (NamedObj) replacementEntityList.get(i),
                        (NamedObj) hostEntityList.get(i));
            }
        }
    }

    private void _removeObjects(CompositeEntity patternContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        patternContainer.attributeList(),
                        patternContainer.entityList(),
                        patternContainer.portList(),
                        patternContainer.relationList()
                });
        for (Object patternObjectRaw : objectCollection) {
            NamedObj patternObject = (NamedObj) patternObjectRaw;

            if (patternObject instanceof CompositeEntity) {
                _removeObjects((CompositeEntity) patternObject);
            }

            String patternObjectCode =
                _getCodeFromObject(patternObject, _pattern);
            if (!_patternToReplacement.containsKey(patternObjectCode)) {
                // patternObject is removed in the replacement.
                NamedObj match = (NamedObj) _matchResult.get(patternObject);
                if (match == null) {
                    continue;
                }

                GTTools.getDeletionChangeRequest(this, match).execute();
                _recordDeletedObject(match);
            }
        }
    }

    private void _substituteGTEntities(CompositeEntity hostContainer) {
        Collection<?> objectCollection = new CombinedCollection<Object>(
                new Collection<?>[] {
                        hostContainer.attributeList(),
                        hostContainer.entityList(),
                        hostContainer.portList(),
                        hostContainer.relationList()
                });
        for (Object hostObjectRaw : objectCollection) {
            NamedObj hostObject = (NamedObj) hostObjectRaw;
            String hostObjectCode = _getCodeFromObject(hostObject, _hostGraph);
            String replacementObjectCode =
                _replacementToHost.getKey(hostObjectCode);
            PatternObjectAttribute patternObjectAttribute =
                GTTools.getPatternObjectAttribute(hostObject);

            if (patternObjectAttribute != null) {
                String patternObjectCode = _getTypeAbbreviation(hostObject) +
                    patternObjectAttribute.getExpression();
                NamedObj patternObject = _getObjectFromCode(patternObjectCode,
                        _pattern);

                if (patternObject != null) {
                    NamedObj match = (NamedObj) _matchResult.get(patternObject);
                    if (match == null) {
                        continue;
                    }
        
                    NamedObj container = match.getContainer();
                    if (match != hostObject) {
                        _recordDeletedObject(hostObject);
                        GTTools.getDeletionChangeRequest(GraphTransformer.this,
                                hostObject).execute();
        
                        if (container != hostContainer) {
                            if (container != null) {
                                _recordDeletedObject(match);
                                GTTools.getDeletionChangeRequest(
                                        GraphTransformer.this, match).execute();
                            }
                            String moml = match.exportMoML();
                            new MoMLChangeRequest(GraphTransformer.this,
                                    hostContainer, moml).execute();
                            if (replacementObjectCode != null) {
                                NamedObj replacementObject = _getObjectFromCode(
                                        replacementObjectCode, _replacement);
                                _recordAddedObject(hostContainer, match,
                                        replacementObject);
                            }
                        }
                    }
                }
            }

            if (hostObject instanceof CompositeEntity) {
                _substituteGTEntities((CompositeEntity) hostObject);
            }
        }
    }

    private Set<NamedObj> _deletedObjects;

    private CompositeEntity _hostGraph;

    private MatchResult _matchResult;

    private Pattern _pattern;

    private TwoWayHashMap<String, String> _patternToReplacement;

    private Replacement _replacement;

    private TwoWayHashMap<String, String> _replacementToHost;
}
