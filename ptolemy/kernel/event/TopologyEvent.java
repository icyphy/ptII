/* One or more TopologyEvents are generated whenever a TopologyChangeRequest
   is processed.

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


//////////////////////////////////////////////////////////////////////////
//// TopologyEvent
/**
A TopologyEvent is an event object that is broadcast to TopologyListener
objects by a topology change request. TopologyEvents are queued in
a TopologyChangeRequest object until a client object (such as a
Director) decides that it is safe to perform the topology change.
At the time, the event has its action performed by the request
object that contains. After performing the request, the client
will cause the events to be broadcast to any interested
TopologyListener objects.

<p> The <i>source</i> is the object that created the topology change
request and the events is contains, and is typically an object such as
an Actor or a Director.  In addition, each event object contains
several fields that indicate the objects involved in the mutation,
such as <b>compositeEntity</b>, <b>entity</b>, <b>componentEntity</b>.
<b>port</b>, <b>relation</b>. and <b>componentRelation</b>.

<p> The <i>id</id> field in the event signifies the type of this
event. For example, if its value is the value of the field
ENTITY_ADDED, then the doTopologyChange() method will cause
<i>componentEntity</i> to be added to <i>compositeEntity</i>. In
addition, when the request object notifies the listeners, the
entityAdded() method of the TopologyListener interface will be called.

@author John Reekie
@version $Id$
@see TopologyListener
*/
public class TopologyEvent extends java.util.EventObject {

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                    ////

  /** The composite entity that had something added or removed.
   */
  public CompositeEntity compositeEntity;

  /** The component entity that was added to or removed from
   * a composite entity
   */
  public ComponentEntity componentEntity;

  /** The entity that had a port added or removed
   */
  public Entity entity;

  /** The port that was added, removed, linked, or unlinked
   */
  public Port port;

  /** The relation that was linked or unlinked
   */
  public Relation relation;

  /** The component relation that was added to or removed
   * from a composite entity
   */
  public ComponentRelation componentRelation;

    ///////////////////////////////////////////////////////////////////
    ////                     public static fields                  ////

  /** An entity has been added to a composite entity. The fields
   * <b>entity</b> and <b>compositeEntity</b> are valid.
   */
  public static final int ENTITY_ADDED = 7531;

  /** An entity has been removed from a composite entity. The fields
   * <b>entity</b> and <b>compositeEntity</b> are valid.
   */
  public static final int ENTITY_REMOVED = ENTITY_ADDED + 1;

  /** A port has been added to an entity. The fields
   * <b>entity</b> and <b>port</b> are valid.
   */
  public static final int PORT_ADDED = ENTITY_REMOVED + 1;

  /** A port has been removed from an entity. The fields
   * <b>entity</b> and <b>port</b> are valid.
   */
  public static final int PORT_REMOVED = PORT_ADDED + 1;;

  /** A port has been linked to a relation. The fields
   * <b>relation</b> and <b>port</b> are valid.
   */
  public static final int PORT_LINKED = PORT_REMOVED + 1;

  /** A port has been unlinked from a relation. The fields
   * <b>relation</b> and <b>port</b> are valid.
   */
  public static final int PORT_UNLINKED = PORT_LINKED + 1;

  /** A relation has been added to a composite entity. The fields
   * <b>relation</b> and <b>compositeEntity</b> are valid.
   */
  public static final int RELATION_ADDED = PORT_UNLINKED + 1;

  /** A relation has been removed from a composite entity. The fields
   * <b>relation</b> and <b>compositeEntity</b> are valid.
   */
  public static final int RELATION_REMOVED = RELATION_ADDED + 1;


    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

  /** Create a new mutation event, with the specified ID and with
   * the given source (i.e. director or similar), composite entity,
   * and component entity.
   */
  public TopologyEvent (int id, Object source, CompositeEntity compositeEntity,
			ComponentEntity componentEntity) {
    super(source);
    this.id = id;
    this.compositeEntity = compositeEntity;
    this.componentEntity = componentEntity;
  }

  /** Create a new mutation event, with the specified ID and with
   * the given source (i.e. director or similar), entity,
   * and port.
   */
  public TopologyEvent (int id, Object source, Entity entity, Port port) {
    super(source);
    this.id = id;
    this.entity = entity;
    this.port = port;
  }

    /** Create a new mutation event, with the specified ID and with
     * the given source (i.e. director or similar), composite entity,
     * and component relation.
     */
    public TopologyEvent (int id, Object source,
            CompositeEntity compositeEntity,
            ComponentRelation componentRelation) {
        super(source);
        this.id = id;
        this.compositeEntity = compositeEntity;
        this.componentRelation = componentRelation;
    }

  /** Create a new mutation event, with the specified ID and with
   * the given source (i.e. director or similar), relation,
   * and port.
   */
  public TopologyEvent (int id, Object source,
			 Relation relation, Port port) {
    super(source);
    this.id = id;
    this.relation = relation;
    this.port = port;
  }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

  /** Get the composite entity involved in this event.
   *
   * @return The composite entity, or null if this is not the right event
   * type.
   */
  public CompositeEntity getCompositeEntity() {
    return compositeEntity;
  }

  /** Get the component entity involved in this event.
   *
   * @return The component entity, or null if this is not the right event
   * type.
   */
  public ComponentEntity getComponentEntity() {
    return componentEntity;
  }

  /** Get the entity involved in this event.
   *
   * @return The entity, or null if this is not the right event
   * type.
   */
  public Entity getEntity() {
    return entity;
  }

  /** Get the ID of this event. The ID indicates what type
   * of event this, and is one of the constants ENTIYTY_ADDED,
   * ENTITY_REMOVED, and so on.
   *
   * @return The integer ID.
   */
  public int getID() {
    return id;
  }

  /** Get the relation involved in this event.
   *
   * @return The relation, or null if this is not the right event
   * type.
   */
  public Relation getRelation() {
    return relation;
  }

  /** Get the component relation involved in this event.
   *
   * @return The component relation, or null if this is not the right event
   * type.
   */
  public ComponentRelation getComponentRelation() {
    return componentRelation;
  }

  /** Get the port involved in this event.
   *
   * @return The port, or null if this is not the right event
   * type.
   */
  public Port getPort() {
    return port;
  }

  /** Make the topology change represented by this event.
   */
  public void doTopologyChange() throws Exception {
	switch (getID()) {
	case ENTITY_ADDED:
	  componentEntity.setContainer(compositeEntity);
	  break;

	case ENTITY_REMOVED:
	  componentEntity.setContainer(null);
	  break;

	case PORT_ADDED:
	  port.setContainer(entity);
	  break;

	case PORT_REMOVED:
	  port.setContainer(null);
	  break;

	case PORT_LINKED:
	  port.link(relation);
	  break;

	case PORT_UNLINKED:
	  port.unlink(relation);
	  break;

	case RELATION_ADDED:
	  componentRelation.setContainer(compositeEntity);
	  break;

	case RELATION_REMOVED:
	  componentRelation.setContainer(null);
	  break;
	}
  }

  /** Undo the topology change represented by this event.
   */
  public void undoTopologyChange() throws Exception {
	switch (getID()) {
	case ENTITY_ADDED:
	  componentEntity.setContainer(null);
	  break;

	case ENTITY_REMOVED:
	  componentEntity.setContainer(compositeEntity);
	  break;

	case PORT_ADDED:
	  port.setContainer(null);
	  break;

	case PORT_REMOVED:
	  port.setContainer(entity);
	  break;

	case PORT_LINKED:
	  port.unlink(relation);
	  break;

	case PORT_UNLINKED:
	  port.link(relation);
	  break;

	case RELATION_ADDED:
	  componentRelation.setContainer(null);
	  break;

	case RELATION_REMOVED:
	  componentRelation.setContainer(compositeEntity);
	  break;
	}
  }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                    ////

    /** The id of the event. Note: this field name does not have
     * a preceding underscore for consistency with AWT.
     */
    private int id;
}
