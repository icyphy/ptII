/* Class representing one atomic change to the topology.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.event;

import ptolemy.kernel.Entity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Relation;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// TopologyEvent
/**
A TopologyEvent represents an atomic change to a topology, such as the
addition of an entity or link.  A sequence of instances of this class
are aggregated in an instance of TopologyChangeRequest to be performed
together by a server that is capable of safely performing mutations to
the topology. A server may also maintain a list of listeners (instances
of TopologyListener), in which case instances of this class are used
to inform the listeners what mutations have been performed.
Thus, instances of this class represent both topology mutations to be
performed, and notifications of topology events that have been performed.
<p>
The <i>client</i> or <i>source</i>
is the object that creates a topology change
request, which is an aggregation of topology events (instances of this
class). The <i>server</i> is the object that implements the topology
changes (by invoking doTopologyChange()).
<p>
This class contains
several private fields that indicate the objects involved in the mutation,
such as <b>_compositeEntity</b>, <b>_entity</b>, <b>_componentEntity</b>.
<b>_port</b>, <b>_relation</b>. and <b>_componentRelation</b>.
In a typical usage, a client constructs an instance of this classes,
using a constructor that sets the appropriate fields.
<p>
The <i>_id</id> field in the event signifies the type of this
event. It should be one of the public static integer constants
provided in the class.
For example, if the <i>_id</i> is
ENTITY_ADDED, then the doTopologyChange() method will cause
<i>_componentEntity</i> to be added to <i>_compositeEntity</i>. In
addition, when notifyListener() is called, the
entityAdded() method of the TopologyListener interface will be called.

@author John Reekie, Edward A. Lee
@version $Id$
@see TopologyListener
@see TopologyChangeRequest
*/
public class TopologyEvent extends java.util.EventObject {

    ///////////////////////////////////////////////////////////////////
    ////                     public static fields                  ////

    /** An entity has been added to a composite entity. The fields
     * <b>_entity</b> and <b>_compositeEntity</b> are valid.
     */
    public static final int ENTITY_ADDED = 7531;

    /** An entity has been removed from a composite entity. The fields
     * <b>_entity</b> and <b>_compositeEntity</b> are valid.
     */
    public static final int ENTITY_REMOVED = ENTITY_ADDED + 1;

    /** A port has been added to an entity. The fields
     * <b>_entity</b> and <b>_port</b> are valid.
     */
    public static final int PORT_ADDED = ENTITY_REMOVED + 1;

    /** A port has been removed from an entity. The fields
     * <b>_entity</b> and <b>_port</b> are valid.
     */
    public static final int PORT_REMOVED = PORT_ADDED + 1;;

    /** A port has been linked to a relation. The fields
     * <b>_relation</b> and <b>_port</b> are valid.
     */
    public static final int PORT_LINKED = PORT_REMOVED + 1;

    /** A port has been unlinked from a relation. The fields
     * <b>_relation</b> and <b>_port</b> are valid.
     */
    public static final int PORT_UNLINKED = PORT_LINKED + 1;

    /** A relation has been added to a composite entity. The fields
     * <b>_relation</b> and <b>_compositeEntity</b> are valid.
     */
    public static final int RELATION_ADDED = PORT_UNLINKED + 1;

    /** A relation has been removed from a composite entity. The fields
     * <b>_relation</b> and <b>_compositeEntity</b> are valid.
     */
    public static final int RELATION_REMOVED = RELATION_ADDED + 1;


    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a new mutation event, with the specified ID and with
     * the given client (i.e. actor or similar), composite entity,
     * and component entity.
     */
    public TopologyEvent (int id, Object client,
            CompositeEntity compositeEntity,
            ComponentEntity componentEntity) {
        super(client);
        this._id = id;
        this._compositeEntity = compositeEntity;
        this._componentEntity = componentEntity;
    }

    /** Create a new mutation event, with the specified ID and with
     * the given client (i.e. actor or similar), entity,
     * and port.
     */
    public TopologyEvent (int id, Object client,
            Entity entity,
            Port port) {
        super(client);
        this._id = id;
        this._entity = entity;
        this._port = port;
    }

    /** Create a new mutation event, with the specified ID and with
     * the given client (i.e. actor or similar), composite entity,
     * and component relation.
     */
    public TopologyEvent (int id, Object client,
            CompositeEntity compositeEntity,
            ComponentRelation componentRelation) {
        super(client);
        this._id = id;
        this._compositeEntity = compositeEntity;
        this._componentRelation = componentRelation;
    }

    /** Create a new mutation event, with the specified ID and with
     * the given client (i.e. actor or similar), relation,
     * and port.
     */
    public TopologyEvent (int id, Object client,
            Relation relation,
            Port port) {
        super(client);
        this._id = id;
        this._relation = relation;
        this._port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the composite entity involved in this event.
     *
     * @return The composite entity, or null if this is not the right event
     * type.
     */
    public CompositeEntity getCompositeEntity() {
        return _compositeEntity;
    }

    /** Get the component entity involved in this event.
     *
     * @return The component entity, or null if this is not the right event
     * type.
     */
    public ComponentEntity getComponentEntity() {
        return _componentEntity;
    }

    /** Get the entity involved in this event.
     *
     * @return The entity, or null if this is not the right event
     * type.
     */
    public Entity getEntity() {
        return _entity;
    }

    /** Get the ID of this event. The ID indicates what type
     * of event this, and is one of the constants ENTIYTY_ADDED,
     * ENTITY_REMOVED, and so on.
     *
     * @return The integer ID.
     */
    public int getID() {
        return _id;
    }

    /** Get the relation involved in this event.
     *
     * @return The relation, or null if this is not the right event
     * type.
     */
    public Relation getRelation() {
        return _relation;
    }

    /** Get the component relation involved in this event.
     *
     * @return The component relation, or null if this is not the right event
     * type.
     */
    public ComponentRelation getComponentRelation() {
        return _componentRelation;
    }

    /** Get the port involved in this event.
     *
     * @return The port, or null if this is not the right event
     * type.
     */
    public Port getPort() {
        return _port;
    }

    /** Make the topology change represented by this event.
     *
     *  @exception IllegalActionException If the change has already
     *   been implemented.
     *  @exception Exception If the topology change implementation
     *   throws it.
     */
    public void doTopologyChange() throws Exception {
        if(_mutationDone) {
            throw new IllegalActionException("TopologyEvent.doTopologyChange()"
                    + ": attempt to implement topology change that has already been"
                    + "implemented.");
        }
        _mutationDone = true;
	switch (getID()) {
	case ENTITY_ADDED:
            _componentEntity.setContainer(_compositeEntity);
            break;

	case ENTITY_REMOVED:
            _componentEntity.setContainer(null);
            break;

	case PORT_ADDED:
            _port.setContainer(_entity);
            break;

	case PORT_REMOVED:
            _port.setContainer(null);
            break;

	case PORT_LINKED:
            _port.link(_relation);
            break;

	case PORT_UNLINKED:
            _port.unlink(_relation);
            break;

	case RELATION_ADDED:
            _componentRelation.setContainer(_compositeEntity);
            break;

	case RELATION_REMOVED:
            _componentRelation.setContainer(null);
            break;
	}
    }

    /** Send this mutation event to one or more mutation listeners.
     *  This method must be called only after doTopologyChange()
     *  has successfully completed.  Typically is is called by
     *  the notifyListeners() method of a TopologyChangeRequest object.
     *  Also, typically the caller will
     *  pass an instance of TopologyMulticaster, so that a number
     *  mutation listeners will be notified with the events.
     *
     *  @param listener The mutation listener to pass the
     *   sequence of events to
     *  @exception IllegalActionException If the topology change has not
     *   yet been implemented.
     */
    public void notifyListeners (TopologyListener listener)
            throws IllegalActionException {
        if(!_mutationDone) {
            throw new IllegalActionException("TopologyEvent.notifyListeners()"
                    + ": attempt to notify listeners of a topology change that has not"
                    + " been implemented.");
        }

        switch (getID()) {
        case TopologyEvent.ENTITY_ADDED:
            listener.entityAdded(this);
            break;

        case TopologyEvent.ENTITY_REMOVED:
            listener.entityRemoved(this);
            break;

        case TopologyEvent.PORT_ADDED:
            listener.portAdded(this);
            break;

        case TopologyEvent.PORT_REMOVED:
            listener.portRemoved(this);
            break;

        case TopologyEvent.PORT_LINKED:
            listener.portLinked(this);
            break;

        case TopologyEvent.PORT_UNLINKED:
            listener.portUnlinked(this);
            break;

        case TopologyEvent.RELATION_ADDED:
            listener.relationAdded(this);
            break;

        case TopologyEvent.RELATION_REMOVED:
            listener.relationRemoved(this);
            break;
        }
    }

    /** Undo the topology change represented by this event.
     */
    public void undoTopologyChange() throws Exception {
	switch (getID()) {
	case ENTITY_ADDED:
            _componentEntity.setContainer(null);
            break;

	case ENTITY_REMOVED:
            _componentEntity.setContainer(_compositeEntity);
            break;

	case PORT_ADDED:
            _port.setContainer(null);
            break;

	case PORT_REMOVED:
            _port.setContainer(_entity);
            break;

	case PORT_LINKED:
            _port.unlink(_relation);
            break;

	case PORT_UNLINKED:
            _port.link(_relation);
            break;

	case RELATION_ADDED:
            _componentRelation.setContainer(null);
            break;

	case RELATION_REMOVED:
            _componentRelation.setContainer(_compositeEntity);
            break;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                    ////

    // Flag that the mutation has been performed.
    private boolean _mutationDone = false;

    // The id of the event. Note: this field name has a
    // preceding underscore even though it makes it inconsistent with AWT.
    private int _id;

    // The component entity that was added to or removed from
    //  a composite entity
    private ComponentEntity _componentEntity;

    // The composite entity that had something added or removed.
    private CompositeEntity _compositeEntity;

    // The entity that had a port added or removed
    private Entity _entity;

    // The port that was added, removed, linked, or unlinked
    private Port _port;

    // The relation that was linked or unlinked
    private Relation _relation;

    // The component relation that was added to or removed
    // from a composite entity
    private ComponentRelation _componentRelation;
}
