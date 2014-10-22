/*
 Parent actor that contains logic common to both proxy sink and source
 actors.  This actor is responsible for either removing the target actor
 and putting itself as a proxy or removing all entities connected to it and putting itself instead.

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
package ptserver.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.Type;
import ptolemy.homer.kernel.HomerConstants;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptserver.util.ServerUtility;
import ptserver.util.TypeParser;

///////////////////////////////////////////////////////////////////
////ProxyActor
/**
 * An abstract parent actor that contains logic common to both sink
 * and source proxy actors.  This actor is responsible for either
 * removing the target actor and putting itself as a proxy or removing
 * all actors connected to the target actor and putting itself instead
 * of all of connected actor connected to the target actor.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 * @see ProxySink
 * @see ProxySource
 */
public abstract class ProxyActor extends TypedAtomicActor {

    /**
     * Replace target entity with the ProxySink and ProxySource.
     */
    public static final boolean REPLACE_TARGET_ENTITY = true;
    /**
     * Replace entities connected to the target entity with proxy actor.
     */
    public static final boolean REPLACE_CONNECTING_ENTITIES = false;

    /**
     * Create a new instance of the ProxyActor without doing any
     * actor replacement.
     * @param container The container of the actor.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If this actor cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ProxyActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _targetEntityName = new StringAttribute(this, "targetEntityName");
    }

    /**
     * Parent constructor that replaces either targetEntity if
     * replaceTargetEntity is true or otherwise all entities connected
     * to it with a proxy instance (ProxySink or ProxySource).  The
     * proxy actor is named the same as the original with addition of
     * "_remote" suffix.  All links of the targetEntity are
     * removed. The proxy actor dynamically adds ports that were
     * present in the targetEntity (with the same port name) or and
     * connects them to the targetEntity's relations.
     * @param container The container of the actor.
     * @param targetEntity the targetEntity to be replaced by a proxy
     * @param replaceTargetEntity if true replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes Map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    public ProxyActor(CompositeEntity container, ComponentEntity targetEntity,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        this(container, targetEntity.getName()
                + ServerUtility.REMOTE_OBJECT_TAG);
        System.out.println("ProxyActor() ctor: " + targetEntity.getName()
                + ServerUtility.REMOTE_OBJECT_TAG);
        setTargetEntityName(targetEntity.getFullName());
        _targetEntityName.setExpression(getTargetEntityName());
        if (replaceTargetEntity) {
            _replaceTargetEntity(targetEntity, portTypes);
        } else {
            _replaceConnectingEntities(targetEntity, portTypes);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the full name of the target entity.
     * @return the targetEntityName
     * @see #setTargetEntityName(String)
     */
    public String getTargetEntityName() {
        return _targetEntityName.getExpression();
    }

    /**
     * Set the full name of the target entity.
     * @param targetEntityName the target entity name
     * @exception IllegalActionException If the change is not
     * acceptable to the container.
     * @see #getTargetEntityName()
     */
    public void setTargetEntityName(String targetEntityName)
            throws IllegalActionException {
        _targetEntityName.setExpression(targetEntityName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Replace all entities connected to the targetEntity with one
     * ProxySource or ProxySink.  Essentially instead of all
     * entities connected to it, ProxySource or ProxySink would be
     * used that would redirect all links from those entities to
     * itself and connect them to dynamically added ports derived from
     * the connected entities.
     *
     * This configuration would allow running of sources and sinks
     * disconnected from the actors in between remotely by passing
     * respective input and output via CommunicationToken.
     * @param targetEntity the entity to which actors that are
     * replaced are connected
     * @param portTypes The map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    private void _replaceConnectingEntities(ComponentEntity targetEntity,
            HashMap<String, String> portTypes)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        // Copy all attributes of the entity whose connected entities are removed.
        // This is needed in cases when a port references any of the attributes via an expression.
        for (Object attributeObject : targetEntity.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            if (attribute instanceof Settable
                    && !_BLACK_LISTED_NAMES.contains(attribute.getName())) {
                Attribute clonedAttribute = (Attribute) attribute
                        .clone(attribute.workspace());
                clonedAttribute.setContainer(this);
                clonedAttribute.setPersistent(true);
            }
        }

        // Copy all ports that are connected to any relationship.
        for (Object portObject : targetEntity.portList()) {
            // Skip non-IO ports.
            if (!(portObject instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                List<Port> linkedPortList = relation.linkedPortList(port);
                // The port is connected somewhere.
                if (!linkedPortList.isEmpty()) {
                    IOPort remotePort = null;
                    // If the port has a connection, clone it and flip its direction
                    // and token consumption or production rates.
                    remotePort = (IOPort) port.clone(port.workspace());

                    // FIXME: what if the port is both input and output?
                    remotePort.setInput(!port.isInput());
                    remotePort.setOutput(!port.isOutput());
                    remotePort.setPersistent(true);
                    remotePort.setContainer(this);
                    // Since we are linking the targetActor with just proxy actor
                    // ports don't need to support multi port.
                    remotePort.setMultiport(false);

                    // Flip token production rate.
                    Attribute productionRate = port
                            .getAttribute("tokenProductionRate");
                    if (port.isOutput() && productionRate != null) {
                        remotePort.removeAttribute(remotePort
                                .getAttribute("tokenConsumptionRate"));
                        Attribute cloned = (Attribute) productionRate
                                .clone(productionRate.workspace());
                        cloned.setPersistent(true);
                        cloned.setName("tokenConsumptionRate");
                        cloned.setContainer(remotePort);
                    }
                    // Flip token consumption rate.
                    Attribute consumptionRate = port
                            .getAttribute("tokenConsumptionRate");
                    if (port.isInput() && consumptionRate != null) {
                        remotePort.removeAttribute(remotePort
                                .getAttribute("tokenProductionRate"));
                        Attribute cloned = (Attribute) consumptionRate
                                .clone(consumptionRate.workspace());
                        cloned.setPersistent(true);
                        cloned.setName("tokenProductionRate");
                        cloned.setContainer(remotePort);
                    }
                    // If the port is typed, save full name of the original port
                    // within an attribute.  This is needed for setting port type information
                    // when the spliced-up model is recreated from the XML.
                    if (remotePort instanceof TypedIOPort) {
                        // Set the type information of the port.
                        Type type = TypeParser.parse(portTypes.get(port
                                .getFullName()));
                        if (type == null) {
                            throw new NullPointerException(
                                    "Could not get parse the type of \""
                                            + port.getFullName());
                        } else {
                            ((TypedIOPort) remotePort).setTypeEquals(type);
                            StringAttribute targetPortName = new StringAttribute(
                                    remotePort, "targetPortName");
                            targetPortName.setExpression(port.getFullName());
                        }
                    }
                    // Remove all links of the port and connect the remote and
                    // current port via the relation.

                    relation.unlinkAll();
                    port.link(relation);
                    remotePort.link(relation);
                    break;
                }
            }
        }
    }

    /**
     * Replace the targetEntity with the proxy.  This configuration
     * would allow execution of the model where sinks or sources run
     * remotely and proxies execute instead of them and pass
     * information to/from them.
     * @param targetEntity The target entity that is replaced with the proxy
     * @param portTypes The map of ports and their resolved types
     * @exception CloneNotSupportedException
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _replaceTargetEntity(ComponentEntity targetEntity,
            HashMap<String, String> portTypes)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        // Copy all attributes of the entity being replaced.
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(
                targetEntity.attributeList());
        for (Attribute attribute : attributes) {
            if (attribute instanceof Settable
                    && !_BLACK_LISTED_NAMES.contains(attribute.getName())) {
                attribute.setContainer(this);
                attribute.setPersistent(true);
            }
        }
        // Copy all IO ports of the target entity.
        for (Object portObject : targetEntity.portList()) {
            if (!(portObject instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) portObject;
            IOPort remotePort = (IOPort) port.clone(port.workspace());
            remotePort.setName(port.getName());
            remotePort.setContainer(this);
            remotePort.setPersistent(true);
            // If the port is typed, save full name of the original port
            // within an attribute.  This is needed for setting port type information
            // when the spliced-up model is recreated from the XML.
            if (remotePort instanceof TypedIOPort) {
                // Set the type information.
                Type type = TypeParser.parse(portTypes.get(port.getFullName()));
                if (type == null) {
                    throw new NullPointerException("Could not parse type for "
                            + port.getFullName());
                } else {
                    ((TypedIOPort) remotePort).setTypeEquals(type);
                    StringAttribute targetPortName = new StringAttribute(
                            remotePort, "targetPortName");
                    targetPortName.setExpression(port.getFullName());
                }
            }
            // Disconnect the port from all relationships and connect the remote copy instead.
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                port.unlink(relation);
                remotePort.link(relation);
            }
            // Disconnect the port.
            port.unlinkAll();
        }
        //Remove the entity from the container.
        targetEntity.setContainer(null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Full name of the targetEntity. */
    private final StringAttribute _targetEntityName;
    /**
     * List of attribute names that don't need to be copied over to the proxy actors.
     */
    private final static HashSet<String> _BLACK_LISTED_NAMES = new HashSet<String>();
    static {
        _BLACK_LISTED_NAMES.add(ServerUtility.REMOTE_OBJECT_TAG);
        _BLACK_LISTED_NAMES.add(HomerConstants.POSITION_NODE);
        _BLACK_LISTED_NAMES.add(HomerConstants.TAB_NODE);
    }
}
