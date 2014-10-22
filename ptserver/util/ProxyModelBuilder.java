/*
 ProxyModelGenerator splices up CompositeActor to support distribution execution.
 The splicing would be done slightly differently depending where the model runs, client or server.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.TypeConflictException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptserver.actor.ProxyActor;
import ptserver.actor.ProxySink;
import ptserver.actor.ProxySource;
import ptserver.communication.ProxySourceData;

///////////////////////////////////////////////////////////////////
//// ProxyModelGenerator

/**
 * ProxyModelGenerator splices up CompositeActor to support distribution execution.
 * The splicing would be done slightly differently depending where the model runs, client or server.
 * The builder would modify original model by injecting proxy actors that facilitate distributed communication
 * with another instance of proxy model running remotely.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxyModelBuilder {

    /**
     * An enumerations that specifies the proxy model type: client or server.
     *
     */
    public enum ProxyModelType {
        /**
         * Client proxy model type.
         */
        CLIENT,
        /**
         * Server proxy model type.
         */
        SERVER;
    }

    /**
     * Create a new instance of the builder for the provided model.
     * @param modelType The type of the model the builder should build.
     * @param topLevelActor The target model on which builder would perform its operations.
     */
    public ProxyModelBuilder(ProxyModelType modelType,
            CompositeActor topLevelActor) {
        _modelType = modelType;
        _topLevelActor = topLevelActor;
    }

    /**
     * Build the ProxyModel that supports distributed execution.
     * In the case of the server model type, sinks and sources marked to run on the client
     * would be replaced with a proxy sinks and sources that facilitate communication with
     * a remote instance of the proxy model.
     * In the case of the client model type, all actors directly connected to sinks and sources
     * marked with a "_remote" attribute would replaced with proxy sources and sinks respectively.
     * All others actor would be removed.
     * @exception IllegalActionException if there is a problem setting a manager or
     * capturing certain type information.
     * @exception TypeConflictException if there is a problem resolving types on the model.
     * @exception NameDuplicationException if there is a problem creating proxy sinks or sources.
     * @exception CloneNotSupportedException if there is a problem cloning ports or attributes.
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
            System.out.println("ProxyModelBuilder.build: "
                    + actor.getFullName());
            // find actors that have a remote tag
            Attribute remoteAttribute = actor
                    .getAttribute(ServerUtility.REMOTE_OBJECT_TAG);
            if (ServerUtility.isTargetProxySource(remoteAttribute)) {
                System.out.println("ProxyModelBuilder.build: source: "
                        + actor.getFullName());
                sources.add(actor);
            } else if (ServerUtility.isTargetProxySink(remoteAttribute)) {
                System.out.println("ProxyModelBuilder.build: sink: "
                        + actor.getFullName());
                sinks.add(actor);
            } else if (_modelType == ProxyModelType.CLIENT) {
                System.out.println("ProxyModelBuilder.build: client: "
                        + actor.getFullName());
                // If the model is being created for the client,
                // keep track of actors that are unneeded for the
                // model execution
                // They are not removed immediately, because their links
                // are needed to create proxy actors.
                unneededActors.add(actor);
            }
            // Locate remote attributes of the actor.
            ServerUtility.findRemoteAttributes(
                    ServerUtility.deepAttributeList(actor),
                    getRemoteAttributesMap());
        }
        // Locate remote attributes of the top level actor.
        ServerUtility.findRemoteAttributes(
                ServerUtility.deepAttributeList(_topLevelActor),
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
                // First check if the attribute belongs to the remote sink or source.
                // If it's not, create a dummy parent for it since its parent would be removed.
                if (!isParentRemote(attribute)) {
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
                entry.getValue().setContainer(_topLevelActor);
            }
            List<Relation> relationList = _topLevelActor.relationList();
            for (Relation relation : relationList) {
                if (relation instanceof IORelation) {
                    IORelation ioRelation = (IORelation) relation;
                    if (ioRelation.linkedPortList().isEmpty()) {
                        ioRelation.setContainer(null);
                    }
                }
            }
            break;
        default:
            // This is not possible but just in case if new enum value is added in the future.
            throw new IllegalStateException("Unhandled enum value");
        }
    }

    /**
     * Return the mapping from the typeable named object's full name to its type string.
     * The type string is obtained by calling .toString() method on the type instance.
     * @return Return the mapping from the typeable named object's full name to its type string.
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    /**
     * Return the mapping from the target actor's full name to the ProxySink associated with it.
     * @return Return the mapping from the target actor's full name to the ProxySink associated with it.
     */
    public HashMap<String, ProxySink> getProxySinkMap() {
        return _proxySinkMap;
    }

    /**
     * Return the mapping from the target actor's full name to the ProxySource associated with it.
     * @return Return the mapping from the target actor's full name to the ProxySource associated with it.
     */
    public HashMap<String, ProxySourceData> getProxySourceMap() {
        return _proxySourceMap;
    }

    /**
     * Return the mapping from the attribute full name's to the attribute instance.
     * The mapping only contains attributes that have {@link ServerUtility#REMOTE_OBJECT_TAG} attribute within them.
     * @return the _remoteAttributesMap
     */
    public HashMap<String, Settable> getRemoteAttributesMap() {
        return _remoteAttributesMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Create a new instance of the ProxySink either by replacing the targetEntity
     * or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity if replaceTargetEntity is true, replace
     * the target entity with the proxy, otherwise replace all
     * entities connecting to it with one proxy.
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
     * Create a new instance of the ProxySource either by replacing
     * the targetEntity or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity if replaceTargetEntity is true, replace
     * the target entity with the proxy, otherwise replace all
     * entities connecting to it with one proxy.
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
     * Capture inferred types of the entities.
     * @param entities The entities whose inferred types are captured
     * @exception IllegalActionException If there is a problem
     * inferring type of Typeable.
     */
    private void _captureModelTypes(HashSet<ComponentEntity> entities)
            throws IllegalActionException {
        System.out
        .println("ProxyModelBuilder._captureModelTypes() start. # of entities: "
                + entities.size());
        for (ComponentEntity entity : entities) {
            System.out.println("ProxyModelBuilder._captureModelTypes() entity "
                    + entity.getFullName());
            for (Object portObject : entity.portList()) {
                Port port = (Port) portObject;
                System.out
                .println("ProxyModelBuilder._captureModelTypes() port "
                        + port.getFullName());
                if (port instanceof IOPort) {
                    // If it's TypedIOPort, capture its types.
                    if (port instanceof TypedIOPort) {
                        System.out
                        .println("ProxyModelBuilder._captureModelTypes() port "
                                + port.getFullName()
                                + " type: "
                                + ((TypedIOPort) port).getType());
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
            for (Attribute attribute : ServerUtility.deepAttributeList(entity)) {
                if (attribute instanceof Typeable) {
                    getModelTypes().put(((Nameable) attribute).getFullName(),
                            ((Typeable) attribute).getType().toString());
                }
            }
        }
        System.out.println("ProxyModelBuilder._captureModelTypes() end");
    }

    private boolean isParentRemote(Attribute attribute) {
        NamedObj container = attribute.getContainer();
        while (container != null) {
            if (_proxySourceMap.containsKey(container.getFullName())
                    || _proxySinkMap.containsKey(container.getFullName())
                    || _remoteAttributesMap
                            .containsKey(container.getFullName())) {
                return true;
            }
            container = container.getContainer();
        }
        return false;
    }

    /**
     * The type of the proxy model.
     */
    private final ProxyModelType _modelType;
    /**
     * The map from the Typeable's full name to its type.
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
