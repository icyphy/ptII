/*
 TODO
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypeConflictException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptserver.actor.ProxyActor;
import ptserver.actor.ProxySink;
import ptserver.actor.ProxySource;
import ptserver.communication.ProxySourceData;

///////////////////////////////////////////////////////////////////
//// ProxyModelGenerator

/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxyModelBuilder {

    /**
     * An enumerations that specifies the remote model type: client or server.
     *
     */
    public enum ProxyModelType {
        /**
         * Client remote model type.
         */
        CLIENT,
        /**
         * Server remote model type.
         */
        SERVER;
    }

    public static final String PROXY_SINK_ATTRIBUTE = "sink";
    public static final String PROXY_REMOTE_TAG = "_remote";
    public static final String PROXY_SOURCE_ATTRIBUTE = "source";

    public ProxyModelBuilder(ProxyModelType modelType,
            CompositeActor topLevelActor) {
        _modelType = modelType;
        _topLevelActor = topLevelActor;
    }

    /**
     * @throws IllegalActionException
     * @throws TypeConflictException
     * @throws NameDuplicationException
     * @throws CloneNotSupportedException
     */
    public void build() throws IllegalActionException, TypeConflictException,
            NameDuplicationException, CloneNotSupportedException {
        _proxySinkMap.clear();
        _proxySourceMap.clear();
        _remoteAttributesMap.clear();
        getModelTypes().clear();
        HashSet<ComponentEntity> unneededActors = new HashSet<ComponentEntity>();
        HashSet<ComponentEntity> sinks = new HashSet<ComponentEntity>();
        HashSet<ComponentEntity> sources = new HashSet<ComponentEntity>();
        for (Object obj : _topLevelActor.deepEntityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            // find actors that have a remote tag
            Attribute remoteAttribute = actor.getAttribute(PROXY_REMOTE_TAG);
            if (isTargetProxySource(remoteAttribute)) {
                sources.add(actor);
            } else if (isTargetProxySink(remoteAttribute)) {
                sinks.add(actor);
            } else if (_modelType == ProxyModelType.CLIENT) {
                // If the model is being created for the client, 
                // keep track of actors that are unneeded for the
                // model execution
                // They are not removed immediately, because their links
                // are needed to create proxy actors.
                unneededActors.add(actor);
            }
            // Locate remote attributes of the actor.
            findRemoteAttributes(deepAttributeList(actor, null),
                    getRemoteAttributesMap());
        }
        // Locate remote attributes of the top level actor.
        findRemoteAttributes(deepAttributeList(_topLevelActor, null),
                getRemoteAttributesMap());

        // The manager must be created because type resolution for some models requires it
        _topLevelActor.setManager(new Manager(_topLevelActor.workspace(),
                "manager"));
        // Resolve types.
        if (_topLevelActor instanceof TypedCompositeActor) {
            TypedCompositeActor typedActor = (TypedCompositeActor) _topLevelActor;
            TypedCompositeActor.resolveTypes(typedActor);
        }
        // Capture types of all target sinks and sources.
        _captureModelTypes(sources);
        _captureModelTypes(sinks);

        switch (_modelType) {
        case SERVER:
            // Replace sinks and sources with proxies.
            for (ComponentEntity entity : sources) {
                _createSource(entity, ProxyActor.REPLACE_TARGET_ENTITY);
            }
            for (ComponentEntity entity : sinks) {
                _createSink(entity, ProxyActor.REPLACE_TARGET_ENTITY);
            }
            break;
        case CLIENT:
            // Replace actors connected to sinks and sources with proxies.
            for (ComponentEntity entity : sources) {
                _createSink(entity, ProxyActor.REPLACE_CONNECTING_ENTITIES);
            }
            for (ComponentEntity entity : sinks) {
                _createSource(entity, ProxyActor.REPLACE_CONNECTING_ENTITIES);
            }
            // Create dummy parent attribute within toplevel actor for attributes that 
            // need to be synchronized with the model in the server.
            HashMap<NamedObj, StringAttribute> containerToDummyAttributeMap = new HashMap<NamedObj, StringAttribute>();
            for (Settable settable : getRemoteAttributesMap().values()) {
                Attribute attribute = (Attribute) settable;
                NamedObj container = attribute.getContainer();
                Attribute lastAttribute = attribute;
                // Create dummy parent attribute for each remote attribute and
                // continue doing that until you reach the top level actor.
                while (container != _topLevelActor) {
                    StringAttribute dummyAttribute = containerToDummyAttributeMap
                            .get(container);
                    if (dummyAttribute == null) {
                        dummyAttribute = new StringAttribute(
                                container.getContainer(), container
                                        .getContainer().uniqueName("dummy"));
                        dummyAttribute.setPersistent(true);
                        containerToDummyAttributeMap.put(container,
                                dummyAttribute);
                    }
                    container = container.getContainer();
                    lastAttribute.setContainer(dummyAttribute);
                    lastAttribute = dummyAttribute;
                }
            }
            // Remove unneeded actors.
            for (ComponentEntity componentEntity : unneededActors) {
                componentEntity.setContainer(null);
            }
            // Connect dummy attributes to the top level actor.  This must be done
            // after unneeded actors are removed to avoid name collision.
            for (Entry<NamedObj, StringAttribute> entry : containerToDummyAttributeMap
                    .entrySet()) {
                entry.getValue().setName(entry.getKey().getName());
            }
            break;
        default:
            // This is not possible but just in case if new enum value is added in the future.
            throw new IllegalStateException("Unhandled enum value");
        }
    }

    /**
     * Create a new instance of the RemoteSink either by replacing the targetEntity
     * or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace
     * the target entity with the proxy, otherwise replace all
     * entities connecting to it with one proxy
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see ptserver.actor.ProxySink
     */
    private void _createSink(ComponentEntity targetEntity,
            boolean replaceTargetEntity) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        ProxySink remoteSink = new ProxySink(
                (CompositeEntity) targetEntity.getContainer(), targetEntity,
                replaceTargetEntity, getModelTypes());
        _proxySinkMap.put(remoteSink.getTargetEntityName(), remoteSink);
    }

    /**
     * Create a new instance of the RemoteSource either by replacing
     * the targetEntity or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace
     * the target entity with the proxy, otherwise replace all
     * entities connecting to it with one proxy
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see ptserver.actor.ProxySource
     */
    private void _createSource(ComponentEntity targetEntity,
            boolean replaceTargetEntity) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        ProxySource remoteSource = new ProxySource(
                (CompositeEntity) targetEntity.getContainer(), targetEntity,
                replaceTargetEntity, getModelTypes());
        ProxySourceData data = new ProxySourceData(remoteSource);
        _proxySourceMap.put(remoteSource.getTargetEntityName(), data);
    }

    /**
     * TODO
     * @param targetEntityAttribute
     * @return
     */
    public static boolean isTargetProxySource(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(PROXY_SOURCE_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO
     * @param targetEntityAttribute
     * @return
     */
    public static boolean isTargetProxySink(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(PROXY_SINK_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Capture inferred types of the entities.
     * @param entities The entities whose inferred types are captured
     * @param portTypes The mapping that stores the types
     * @exception IllegalActionException If there is a problem
     * inferring type of Typeable.
     */
    private void _captureModelTypes(HashSet<ComponentEntity> entities)
            throws IllegalActionException {
        for (ComponentEntity entity : entities) {
            for (Object portObject : entity.portList()) {
                Port port = (Port) portObject;
                if (port instanceof IOPort) {
                    // If it's TypedIOPort, capture its types.
                    if (port instanceof TypedIOPort) {
                        // Note: using toString on Type is not elegant
                        // and could break but this is the only way to serialize port information
                        // for all types.
                        // TypeParser handles string parsing and returns original type name.
                        getModelTypes().put(port.getFullName(),
                                ((TypedIOPort) port).getType().toString());
                    }
                }

            }
            // Also capture type information of all Typeable attributes.
            for (Attribute attribute : deepAttributeList(entity, null)) {
                if (attribute instanceof Typeable) {
                    getModelTypes().put(((Nameable) attribute).getFullName(),
                            ((Typeable) attribute).getType().toString());
                }
            }
        }
    }

    public static List<Attribute> deepAttributeList(NamedObj container) {
        return deepAttributeList(container, null);
    }

    /**
     * TODO
     * @param container
     * @param attributeList
     * @return
     */
    private static List<Attribute> deepAttributeList(NamedObj container,
            List<Attribute> attributeList) {
        if (attributeList == null) {
            attributeList = new ArrayList<Attribute>();
        }
        for (Object attributeObject : container.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            attributeList.add(attribute);
            deepAttributeList(attribute, attributeList);
        }
        return attributeList;
    }

    /**
     * Find all remote attributes of the model and add them to the
     * _settableAttributesMap.
     * @param attributeList TODO
     * @return TODO
     */
    public static void findRemoteAttributes(List<Attribute> attributeList,
            HashMap<String, Settable> remoteAttributeMap) {
        for (Attribute attribute : attributeList) {
            if (isRemoteAttribute(attribute)) {
                remoteAttributeMap.put(attribute.getFullName(),
                        (Settable) attribute);
            }
        }
    }

    /**
     * Return true if the attribute is marked as remote attribute, false otherwise.
     * @param attribute the attribute to check.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isRemoteAttribute(Attribute attribute) {
        if (attribute instanceof Settable) {
            Attribute isRemoteAttribute = attribute.getAttribute("_remote");
            if (isRemoteAttribute instanceof Parameter) {
                if (((Parameter) isRemoteAttribute).getExpression().equals(
                        "attribute")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create and initialize a new MoMLParser.
     * The parser would have BackwardCompatiblity and RemoveGraphicalClasses filters.
     * The RemoteGraphicalClasses would filter out only classes that are known not to be
     * portable to be portable to Android.
     * @return new MoMLParser with BackwardCompatibility and RemoveGraphicalClasses filters.
     */
    public static MoMLParser createMoMLParser() {
        MoMLParser parser = new MoMLParser(new Workspace());
        parser.resetAll();
        // TODO: is this thread safe?
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        // TODO either fork RemoveGraphicalClasses or make its hashmap non-static (?)
        RemoveGraphicalClasses filter = new RemoveGraphicalClasses();
        filter.remove("ptolemy.actor.lib.gui.ArrayPlotter");
        filter.remove("ptolemy.actor.lib.gui.SequencePlotter");
        filter.remove("ptolemy.actor.lib.gui.Display");
        filter.remove("ptolemy.actor.gui.style.CheckBoxStyle");
        filter.remove("ptolemy.actor.gui.style.ChoiceStyle");
        MoMLParser.addMoMLFilter(filter);
        return parser;
    }

    /**
     * @return the _remoteSourceMap
     */
    public HashMap<String, ProxySourceData> getProxySourceMap() {
        return _proxySourceMap;
    }

    /**
     * @return the _remoteSinkMap
     */
    public HashMap<String, ProxySink> getProxySinkMap() {
        return _proxySinkMap;
    }

    /**
     * @return the _remoteAttributesMap
     */
    public HashMap<String, Settable> getRemoteAttributesMap() {
        return _remoteAttributesMap;
    }

    /**
     * @return the _modelTypes
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    /**
     * The type of the remote model.
     */
    private final ProxyModelType _modelType;
    /**
     * The map from the Typeable's full name to its type
     */
    private final HashMap<String, String> _modelTypes = new HashMap<String, String>();

    /**
     * The mapping from the original source actor name to its remote
     * source actor and queue.
     */
    private final HashMap<String, ProxySourceData> _proxySourceMap = new HashMap<String, ProxySourceData>();

    /**
     * The mapping from the original sink actor name to its remote sink.
     */
    private final HashMap<String, ProxySink> _proxySinkMap = new HashMap<String, ProxySink>();

    /**
     * The mapping from the original settable object name to the
     * remote representation.
     */
    private final HashMap<String, Settable> _remoteAttributesMap = new HashMap<String, Settable>();

    /**
     * The top level actor of the loaded model.
     */
    private CompositeActor _topLevelActor;

}
