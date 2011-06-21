/*
 Parent actor that contains logic common to both sink and source
 remote actors.  This actor is responsible for removing a target actor
 and putting itself as a proxy.

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
package ptserver.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
////RemoteActor
/**
 * An abstract parent actor that contains logic common to both sink
 * and source remote actors.  This actor is responsible for either
 * removing the target actor and putting itself as a proxy or removing
 * all actors connected to the target actor and putting itself instead
 * of all of them.  The intent is to allow sinks or sources to run
 * remotely by putting instance of RemoteSink or RemoteSource instead.
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 * @see RemoteSink
 * @see RemoteSource
 */
public abstract class RemoteActor extends TypedAtomicActor {

    /**
     * Create a new instance of the RemoteActor without doing any
     * actor replacement.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If this actor cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public RemoteActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _targetEntityName = new StringAttribute(this, "targetEntityName");
    }

    /**
     * Parent constructor that replaces either targetEntity if
     * replaceTargetEntity is true or otherwise all entities connected
     * to it with a proxy instance (RemoteSink or RemoteSource).  The
     * proxy actor is named the same as the original with addition of
     * "_remote" suffix.  All links of the targetEntity are
     * removed. The proxy actor dynamically adds ports that were
     * present in the targetEntity (with the same port name) or and
     * connects them to the targetEntity's relations.
     * @param container The container
     * @param targetEntity the targetEntity to be replaced by a proxy
     * @param replaceTargetEntity true to replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes Map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    public RemoteActor(CompositeEntity container, ComponentEntity targetEntity,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        this(container, targetEntity.getName() + "_remote");
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
    ////                         protected methods                 ////

    /**
     * Check if the connecting port is valid and could be used
     * for cloning for the RemoteActor.
     * @param connectingPort The connecting port to check
     * @return true if connectingPort is valid, false otherwise
     */
    protected abstract boolean isValidConnectingPort(IOPort connectingPort);

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Replace all entities connected to the targetEntity with one
     * RemoteSource or RemoteSink.  Essentially instead of all
     * entities connected to it, RemoteSink or RemoteSource would be
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

        for (Object portObject : targetEntity.portList()) {
            if (!(portObject instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                List<Port> linkedPortList = relation.linkedPortList(port);
                IOPort remotePort = null;
                for (Port connectingPort : linkedPortList) {
                    if (connectingPort instanceof IOPort
                            && isValidConnectingPort((IOPort) connectingPort)) {
                        remotePort = (IOPort) connectingPort.clone(port
                                .workspace());
                        remotePort.setPersistent(true);
                        remotePort.setName(port.getName());
                        remotePort.setContainer(this);
                        if (remotePort instanceof TypedIOPort) {
                            Type type = BaseType.forName(portTypes
                                    .get(connectingPort.getFullName()));
                            ((TypedIOPort) remotePort).setTypeEquals(type);
                            StringAttribute targetPortName = new StringAttribute(
                                    remotePort, "targetPortName");
                            targetPortName.setExpression(connectingPort
                                    .getFullName());
                        }
                        break;
                    }
                }
                relation.unlinkAll();
                if (remotePort != null) {
                    port.link(relation);
                    remotePort.link(relation);
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
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(
                targetEntity.attributeList());
        for (Attribute attribute : attributes) {
            attribute.setContainer(this);
        }
        for (Object portObject : targetEntity.portList()) {
            if (!(portObject instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) portObject;
            IOPort remotePort = (IOPort) port.clone(port.workspace());
            remotePort.setName(port.getName());
            remotePort.setContainer(this);
            remotePort.setPersistent(true);
            if (remotePort instanceof TypedIOPort) {
                Type type = BaseType.forName(portTypes.get(port.getFullName()));
                ((TypedIOPort) remotePort).setTypeEquals(type);
                StringAttribute targetPortName = new StringAttribute(
                        remotePort, "targetPortName");
                targetPortName.setExpression(port.getFullName());
            }
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                port.unlink(relation);
                remotePort.link(relation);
            }
            port.unlinkAll();
        }
        targetEntity.setContainer(null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Full name of the targetEntity. */
    private final StringAttribute _targetEntityName;
}
