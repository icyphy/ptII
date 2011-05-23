/*
 Parent actor that contains logic common to both sink and source remote actors.
 This actor is responsible for removing a target actor and putting itself as a proxy.
 
 Copyright (c) 2002-2010 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Parent actor that contains logic common to both sink and source remote actors.
 * This actor is responsible for removing a target actor and putting itsefl as a proxy.
 * @author ahuseyno
 * @version $Id$ 
 *  
 */
public class RemoteActor extends TypedAtomicActor {

    /**
     * Parent constructor that replaces targetEntity with a proxy instance (RemoteSink or Remote Source).
     * The proxy actor is named the same as the original with addition of "_remote" suffix.
     * All links of targetEntity are removed. The proxy actor dynamically adds ports that were present
     * in the targetEntity (with the same port name) and connects them to the targetEntity's relations.
     * @param container The container
     * @param targetEntity the targetEntity to be replaced by a proxy
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     */
    public RemoteActor(CompositeEntity container, ComponentEntity targetEntity)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, targetEntity.getName() + "_remote");
        for (Object portObj : targetEntity.portList()) {
            if (!(portObj instanceof IOPort)) {
                break;
            }
            IOPort port = (IOPort) portObj;
            IOPort remotePort = (IOPort) port.clone(this.workspace());
            remotePort.setName(port.getName());
            remotePort.setContainer(this);
            for (Object relationObj : port.linkedRelationList()) {
                Relation relation = (Relation) relationObj;
                port.unlink(relation);
                remotePort.link(relation);
            }
            port.unlinkAll();
        }
        targetEntity.setContainer(null);
        setTargetEntityName(targetEntity.getName());
    }

    /**
     * Sets the name of the entity this proxy actor replaces
     * @param targetEntityName the target Entity name
     * @see #getOriginalActorName()
     */
    public void setTargetEntityName(String targetEntityName) {
        this.targetEntityName = targetEntityName;
    }

    /**
     * Returns the name of the entity this proxy actor is replacing
     * @return the targetEntityName 
     * @see #setOriginalActorName(String)
     * 
     */
    public String getTargetEntityName() {
        return targetEntityName;
    }

    /**
     * Name of the targetEntity
     */
    private String targetEntityName;
}
