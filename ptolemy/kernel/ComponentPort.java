/* A port supporting clustered graphs.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bart@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.CrossRefList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ComponentPort
/**
A port supporting hierarchy. A component port can have "inside"
links as well as the usual "outside" links supported by the base
class. That is, while the basic port has only links to relations
that are on the exterior of its containing entity, this port can have
links to relations on the interior.
An inside link is a link to a relation that is contained by the
container of the port.
<p>
A ComponentPort may be transparent or opaque.  If it is transparent,
then "deep" accesses of the topology see through the port.
Methods that read the topology come in two versions, shallow and deep.
The deep versions pass through transparent ports. This is
done with a simple rule. If a transparent port is encountered from
inside, then the traversal continues with its outside links. If it
is encountered from outside, then the traversal continues with its
inside links.  A ComponentPort is opaque if its container is opaque.
(its isOpaque() method returns true).  Derived classes may use other
strategies to specify whether a port is opaque.
<p>
Normally, links to a transparent port from the outside are to
relations contained by the container of the container of the port.
Links from the inside are to relations contained by the container
of the port.  That is, levels of the hierarchy are not crossed.
For a few applications, links that cross levels of the hierarchy
are needed. The links in these connections are created
using the liberalLink() method. The link() method
prohibits such links, throwing an exception if they are attempted
(most applications will prohibit level-crossing connections by using
only the link() method).
<p>
A ComponentPort can link to any instance of ComponentRelation.
An attempt to link to an instance of Relation will trigger an exception.
Derived classes may wish to further constrain links to a subclass
of ComponentRelation.  To do this, subclasses should override the
protected methods _checkLink() and _checkLiberalLink() to throw an exception
if their arguments are relations that are not of the appropriate
subclass.  Similarly, a ComponentPort can only be contained by a
ComponentEntity, and an attempt to set the container to an instance
of Entity will trigger an exception.  If a subclass wishes to
constrain the containers of the port to be of a subclass of
ComponentEntity, they should override _checkContainer().

@author Edward A. Lee, Xiaojun Liu
@version $Id$
@since Ptolemy II 0.2
*/
public class ComponentPort extends Port {

    /** Construct a port in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public ComponentPort() {
        super();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public ComponentPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.  Increment the version of the workspace.
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ComponentPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new ComponentPort.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ComponentPort newObject = (ComponentPort)super.clone(workspace);
        newObject._insideLinks = new CrossRefList(newObject);
        return newObject;
    }

    /** Deeply list the ports connected to this port on the outside.
     *  Begin by listing the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then list all the ports deeply connected
     *  on the outside to that transparent port.  If any are transparent
     *  ports that we are connected to from the outside, then list
     *  opaque ports deeply inside that port. Note that a port may
     *  be listed more than once. This method is read synchronized on the
     *  workspace.
     *  @return An unmodifiable list of ComponentPort objects.
     */
    public List deepConnectedPortList() {
        try {
            _workspace.getReadAccess();
            return _deepConnectedPortList(null);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Deeply enumerate the ports connected to this port on the outside.
     *  Begin by enumerating the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then enumerate all the ports deeply connected
     *  on the outside to that transparent port.  Note that a port may
     *  be listed more than once. This method read synchronized on the
     *  workspace.
     *  @return An enumeration of ComponentPort objects.
     *  @deprecated Use deepConnectedPortList() instead.
     */
    public Enumeration deepConnectedPorts() {
        return Collections.enumeration(deepConnectedPortList());
    }

    /** Deeply list the ports connected on the inside.
     *  All ports listed are opaque. Note that
     *  the returned list could conceivably be empty, for
     *  example if this port has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  @return An unmodifiable list of ComponentPort objects.
     */
    public List deepInsidePortList() {
        try {
            _workspace.getReadAccess();
            return _deepInsidePortList(null);
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this port is transparent, then deeply enumerate the ports
     *  connected on the inside.  Otherwise, enumerate
     *  just this port. All ports enumerated are opaque. Note that
     *  the returned enumeration could conceivably be empty, for
     *  example if this port is transparent but has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  @return An enumeration of ComponentPort objects.
     *  @deprecated Use deepInsidePortList() instead.
     */
    public Enumeration deepInsidePorts() {
        return Collections.enumeration(deepInsidePortList());
    }

    /** Insert a link to the specified relation at the specified index,
     *  and notify the container by calling its connectionsChanged() method.
     *  This method defaults to adding an inside  null link at the given index
     *  if the relation argument is null. Otherwise it simply invokes the
     *  insertLink) method.
     *  <p>
     *  The specified index can be any non-negative integer.
     *  Any links with indices larger than or equal to the one specified
     *  here will henceforth have indices that are larger by one.
     *  If the index is larger than the number of existing
     *  links (as returned by numLinks()), then empty links
     *  are inserted (these will be null elements in the list returned
     *  by linkedRelationsList() or in the enumeration returned by
     *  linkedRelations()). If the specified relation is null, then
     *  an empty inside link is inserted at the specified index.
     *  <p>
     *  Note that a port may be linked to the same relation more than
     *  once, in which case the link will be reported more than once
     *  by the linkedRelations() method.
     *  <p>
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _checkLink() protected method).
     *  <p>
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param index The index at which to insert the link.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation.
     */
    public void insertInsideLink(int index, Relation relation)
            throws IllegalActionException {
        if (relation != null) {
            insertLink(index, relation);
            return;
        }
        try {
            _workspace.getWriteAccess();
            // Assume an inside link
            _insideLinks.insertLink(index, null);
            // NOTE: _checkLink() ensures that the container is
            // not null, and the class ensures that it is an Entity.
            ((Entity)getContainer()).connectionsChanged(this);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Insert a link to the specified relation at the specified index,
     *  and notify the container by calling its connectionsChanged() method.
     *  This overrides the base class to allow inside links as well as links
     *  at the same level of the hierarchy.
     *  <p>
     *  The specified index can be any non-negative integer.
     *  Any links with indices larger than or equal to the one specified
     *  here will henceforth have indices that are larger by one.
     *  If the index is larger than the number of existing
     *  links (as returned by numLinks()), then empty links
     *  are inserted (these will be null elements in the list returned
     *  by linkedRelationsList() or in the enumeration returned by
     *  linkedRelations()). If the specified relation is null, then
     *  an empty outside link is inserted at the specified index.
     *  <p>
     *  Note that a port may be linked to the same relation more than
     *  once, in which case the link will be reported more than once
     *  by the linkedRelations() method.
     *  <p>
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _checkLink() protected method).
     *  <p>
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param index The index at which to insert the link.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation.
     */
    public void insertLink(int index, Relation relation)
            throws IllegalActionException {
        if (relation != null && _workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            if (relation == null) {
                // Assume outside link
                _relationsList.insertLink(index, null);
            } else {
                _checkLink(relation);
                if (_isInsideLinkable(relation.getContainer())) {
                    // An inside link
                    _insideLinks.insertLink(index, relation._getPortList());
                } else {
                    // An outside link
                    _relationsList.insertLink(index, relation._getPortList());
                }
            }
            // NOTE: _checkLink() ensures that the container is
            // not null, and the class ensures that it is an Entity.
            ((Entity)getContainer()).connectionsChanged(this);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** List the ports connected on the inside to this port. Note that
     *  a port may be listed more than once if more than one inside connection
     *  has been established to it.
     *  This method is read-synchronized on the workspace.
     *  @return A list of ComponentPort objects.
     */
    public List insidePortList() {
        try {
            _workspace.getReadAccess();
            LinkedList result = new LinkedList();
            Iterator relations = insideRelationList().iterator();
            while (relations.hasNext()) {
                Relation relation = (Relation)relations.next();
                // A null link might yield a null relation here.
                if (relation != null) {
                    result.addAll(relation.linkedPortList(this));
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the ports connected on the inside to this port. Note that
     *  a port may be listed more than once if more than one inside connection
     *  has been established to it.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentPort objects.
     *  @deprecated Use insidePortList() instead.
     */
    public Enumeration insidePorts() {
        return Collections.enumeration(insidePortList());
    }

    /** List the relations linked on the inside to this port.
     *  Note that a relation may be listed more than once if more than link
     *  to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return A list of ComponentRelation objects.
     */
    public List insideRelationList() {
        try {
            _workspace.getReadAccess();
            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            LinkedList result = new LinkedList();
            Enumeration relations = _insideLinks.getContainers();
            while (relations.hasMoreElements()) {
                result.add(relations.nextElement());
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the relations linked on the inside to this port.
     *  Note that a relation may be listed more than once if more than link
     *  to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */
    public Enumeration insideRelations() {
        // NOTE: There is no reason to deprecate this because it does
        // depend on Doug Lea's collections, and it is more efficient than
        // the list version.
        try {
            _workspace.getReadAccess();
            return _insideLinks.getContainers();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true the the given port is deeply connected with this port.
     *  This method is read-synchronized on the workspace.
     *  @return True if the given port is deeply connected.
     */
    public boolean isDeeplyConnected(ComponentPort port) {
        if (port == null) return false;
        try {
            _workspace.getReadAccess();
            return deepConnectedPortList().contains(port);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the given relation is linked from inside.
     *  @return True if the given relation is linked from inside.
     */
    public boolean isInsideLinked(Relation relation) {
        return _insideLinks.isLinked(relation);
    }

    /** Return true if the container entity is opaque.
     *  @return True if the container entity is opaque.
     */
    public boolean isOpaque() {
        ComponentEntity entity = (ComponentEntity)getContainer();
        if (entity == null) return true;
        return entity.isOpaque();
    }

    /** Link this port with the specified relation.  The only constraints are
     *  that the port and the relation share the same workspace, and
     *  that the relation be of a compatible type (ComponentRelation).
     *  They are not required to be at the same level of the hierarchy.
     *  To prohibit links across levels of the hierarchy, use link().
     *  Note that generally it is a bad idea to allow level-crossing
     *  links, since it breaks modularity.  This loss of modularity
     *  means, among other things, that the composite within which this
     *  port exists cannot be cloned.
     *  Nonetheless, this capability is provided for the benefit of users
     *  that feel they just must have it, and who are willing to sacrifice
     *  clonability and modularity.
     *  <p>
     *  Both inside and outside links are supported.  Note that a port may
     *  be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method. If the <i>relation</i> argument is null, then create a
     *  null link (on the outside).
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the relation does not share
     *   the same workspace, or the port has no container.
     */
    public void liberalLink(ComponentRelation relation)
            throws IllegalActionException {
        if (relation != null) _checkLiberalLink(relation);
        _doLink(relation);
    }

    /** Link this port with the specified relation. Note that a port may
     *  be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method.  If the argument is null, then create a null link (on
     *  the outside).
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the link crosses levels of
     *   the hierarchy, or the port has no container, or the relation
     *   is not a ComponentRelation, or if the port is contained
     *   by a class definition.
     */
    public void link(Relation relation) throws IllegalActionException {
        if (relation != null) {
            _checkLink(relation);
        } 
        _doLink(relation);
    }

    /** Return the number of inside links.
     *  This method is read-synchronized on the workspace.
     *  @return The number of inside links.
     */
    public int numInsideLinks() {
        try {
            _workspace.getReadAccess();
            return _insideLinks.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Specify the container entity, adding the port to the list of ports
     *  in the container.  This class overrides the base class to remove
     *  all inside links if the given container is null.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param entity The container.
     *  @exception IllegalActionException If this port is not of the
     *   expected class for the container, or it has no name,
     *   or the port and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, NameDuplicationException {
        if (entity != null && _workspace != entity.workspace()) {
            throw new IllegalActionException(this, entity,
                    "Cannot set container because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            super.setContainer(entity);
            if (entity == null) {
                unlinkAllInside();
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This overrides the base class to check to see whether the link
     *  is an inside link, based on the container of the relation, and
     *  to call unlinkInside() if it is.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        if (relation != null
                && _isInsideLinkable(relation.getContainer())) {
            // An inside link
            unlinkInside(relation);
        } else {
            super.unlink(relation);
        }
    }

    /** Unlink all outside links.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     */
    public void unlinkAll() {
        // NOTE: This overrides the base class only to update the docs
        // to refer to _outside_ links.
        super.unlinkAll();
    }

    /** Unlink all inside links.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     */
    public void unlinkAllInside() {
        try {
            _workspace.getWriteAccess();
            _insideLinks.unlinkAll();
            Entity container = (Entity)getContainer();
            if (container != null) {
                container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink whatever relation is currently linked on the inside
     *  with the specified index number. If the relation
     *  is not linked to this port on the inside, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param index The index number of the link to remove.
     */
    public void unlinkInside(int index) {
        try {
            _workspace.getWriteAccess();
            _insideLinks.unlink(index);
            Entity container = (Entity)getContainer();
            if (container != null) {
                container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified relation on the inside. If the relation
     *  is not linked to this port on the inside, do nothing.
     *  If the relation is linked more than once on the inside,
     *  remove all occurrences of the link.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlinkInside(Relation relation) {
        try {
            _workspace.getWriteAccess();
            _insideLinks.unlink(relation);
            Entity container = (Entity)getContainer();
            if (container != null) {
                container.connectionsChanged(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container is a
     *  ComponentEntity.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   ComponentEntity.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ComponentEntity) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "ComponentPort can only be contained by ComponentEntity");
        }
    }

    /** This method is identical to _checkLink(), except that it does
     *  not throw an exception if the link crosses levels of the
     *  hierarchy.  It is used in a "strategy pattern," where the link
     *  methods call it to check the validity of a link, and derived
     *  classes perform more elaborate checks.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not a ComponentRelation, or the relation has
     *   no container, or the link crosses levels of the hierarchy.
     */
    protected void _checkLiberalLink(Relation relation)
            throws IllegalActionException {
        if (relation != null) {
            if (!(relation instanceof ComponentRelation)) {
                throw new IllegalActionException(this, relation,
                        "Attempt to link to an incompatible relation "
                        + "(expected ComponentRelation).");
            }
            Entity container = (Entity)getContainer();
            if (container == null) {
                throw new IllegalActionException(this, relation,
                        "Port must have a container to establish a link.");
            }
            // Check that the container is not a class or that
            // if it is, that this is an inside link.
            if (container.isClassDefinition() 
                    && container != relation.getContainer()) {
                throw new IllegalActionException(this, relation,
                        "Cannot establish a link to a port contained " +
                        "by a class definition");
            }
            // Throw an exception if this port is not of an acceptable
            // class for the relation.
            relation._checkPort(this);
        }
    }

    /** Override the base class to throw an exception if the relation is
     *  not a ComponentRelation, or if the container of the port or
     *  relation is null, or if the link crosses levels of the hierarchy,
     *  or if the container of this port is a class definition and the
     *  link is not an inside link.
     *  This method is used in a "strategy pattern," where the link
     *  methods call it to check the validity of a link, and derived
     *  classes perform more elaborate checks.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *  If the relation argument is null, do nothing.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container, or
     *   the relation is not a ComponentRelation, or the relation has
     *   no container, or the link crosses levels of the hierarchy, or
     *   this port is not an acceptable port for the specified relation,
     *   or if the container of this port is a class definition and the
     *   link is not an inside link.
     */
    protected void _checkLink(Relation relation)
            throws IllegalActionException {
        super._checkLink(relation);
        if (relation != null) {
            if (!(relation instanceof ComponentRelation)) {
                throw new IllegalActionException(this, relation,
                        "Attempt to link to an incompatible relation "
                        + "(expected ComponentRelation).");
            }
            Entity container = (Entity)getContainer();
            // Superclass assures that the container is not null.
            Nameable relationContainer = relation.getContainer();
            if (container != relationContainer &&
                    container.getContainer() != relationContainer) {
                throw new IllegalActionException(this, relation,
                        "Link crosses levels of the hierarchy");
            }
            // Check that the container is not a class or that
            // if it is, that this is an inside link.
            if (container.isClassDefinition() && container != relationContainer) {
                throw new IllegalActionException(this, relation,
                        "Cannot establish a link to a port contained " +
                        "by a class definition");
            }
            // Throw an exception if this port is not of an acceptable
            // class for the relation.
            relation._checkPort(this);
        }
    }

    /** Deeply list the ports connected to this port on the outside.
     *  Begin by listing the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then list all the ports deeply connected
     *  on the outside to that transparent port.  If any are transparent
     *  ports that we are connected to from the outside, then list
     *  opaque ports deeply inside that port. Note that a port may
     *  be listed more than once. The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method not synchronized on the workspace, so the
     *  caller should.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @return An unmodifiable list of ComponentPort objects.
     */
    protected List _deepConnectedPortList(LinkedList path) {
        if (_deepLinkedPortsVersion == _workspace.getVersion()) {
            // Cache is valid.  Use it.
            return _deepLinkedPorts;
        }
        if (path == null) {
            path = new LinkedList();
        } else {
            if (path.indexOf(this) >= 0) {
                throw new InvalidStateException(path, "Loop in topology!");
            }
        }
        path.add(0, this);
        Iterator nearRelations = linkedRelationList().iterator();
        LinkedList result = new LinkedList();

        while (nearRelations.hasNext()) {
            ComponentRelation relation =
                (ComponentRelation)nearRelations.next();

            // A null link (supported since indexed links) might
            // yield a null relation here. EAL 7/19/00.
            if (relation != null) {
                Iterator connectedPorts
                    = relation.linkedPortList(this).iterator();
                while (connectedPorts.hasNext()) {
                    ComponentPort port
                        = (ComponentPort)connectedPorts.next();
                    // NOTE: If level-crossing transitions are not allowed,
                    // then a simpler test than that of the following
                    // would work.
                    if (port._isInsideLinkable(relation.getContainer())) {
                        // We are coming at the port from the inside.
                        if (port.isOpaque()) {
                            result.add(port);
                        } else {
                            // Port is transparent
                            result.addAll(port._deepConnectedPortList(path));
                        }
                    } else {
                        // We are coming at the port from the outside.
                        if (port.isOpaque()) {
                            result.add(port);
                        } else {
                            // It is transparent.
                            result.addAll(port._deepInsidePortList(path));
                        }
                    }
                }
            }
        }
        _deepLinkedPorts = Collections.unmodifiableList(result);
        _deepLinkedPortsVersion = _workspace.getVersion();
        path.remove(0);
        return _deepLinkedPorts;
    }

    /** Deeply enumerate the ports connected to this port on the outside.
     *  Begin by enumerating the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then list all the ports deeply connected
     *  on the outside to that transparent port.  Note that a port may
     *  be enumerated more than once. The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method not synchronized on the workspace, so the
     *  caller should.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @deprecated Use _deepConnectedPortList() instead.
     *  @return An enumeration of ComponentPort objects.
     */
    protected Enumeration _deepConnectedPorts(LinkedList path) {
        return Collections.enumeration(_deepConnectedPortList(path));
    }

    /** If this port is transparent, then deeply list the ports
     *  connected on the inside.  Otherwise, list
     *  just this port. All ports listed are opaque. Note that
     *  the returned list could conceivably be empty, for
     *  example if this port is transparent but has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method is read-synchronized on the workspace.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @return An unmodifiable list of ComponentPort objects.
     */
    protected List _deepInsidePortList(LinkedList path) {
        if (_deepLinkedInPortsVersion == _workspace.getVersion()) {
            // Cache is valid.  Use it.
            return _deepLinkedInPorts;
        }
        if (path == null) {
            path = new LinkedList();
        } else {
            if (path.indexOf(this) >= 0) {
                throw new InvalidStateException(path, "Loop in topology!");
            }
        }
        path.add(0, this);
        LinkedList result = new LinkedList();
        // Port is transparent.
        Iterator relations = insideRelationList().iterator();
        while (relations.hasNext()) {
            Relation relation = (Relation)relations.next();
            // A null link might yield a null relation here.
            if (relation != null) {
                Iterator insidePorts =
                    relation.linkedPortList(this).iterator();
                while (insidePorts.hasNext()) {
                    ComponentPort port =
                        (ComponentPort)insidePorts.next();
                    // The inside port may not be actually inside,
                    // in which case we want to look through it
                    // from the inside (this supports transparent
                    // entities).
                    if (port._isInsideLinkable(relation.getContainer())) {
                        // The inside port is not truly inside.
                        // Check to see whether it is transparent.
                        if (port.isOpaque()) {
                            result.add(port);
                        } else {
                            result.addAll(
                                    port._deepConnectedPortList(path));
                        }
                    } else {
                        // We are coming at the port from the outside.
                        if (port.isOpaque()) {
                            // The inside port is truly inside.
                            result.add(port);
                        } else {
                            result.addAll(
                                    port._deepInsidePortList(path));
                        }
                    }
                }
            }
        }
        _deepLinkedInPorts = Collections.unmodifiableList(result);
        _deepLinkedInPortsVersion = _workspace.getVersion();
        path.remove(0);
        return _deepLinkedInPorts;
    }

    /** If this port is transparent, then deeply enumerate the ports
     *  connected on the inside.  Otherwise, enumerate
     *  just this port. All ports enumerated are opaque. Note that
     *  the returned enumeration could conceivably be empty, for
     *  example if this port is transparent but has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method is read-synchronized on the workspace.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @return An enumeration of ComponentPort objects.
     *  @deprecated Use _deepInsidePortList() instead.
     */
    protected Enumeration _deepInsidePorts(LinkedList path) {
        return Collections.enumeration(_deepInsidePortList(path));
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & LINKS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result += "insidelinks {\n";
                Iterator insideRelations = insideRelationList().iterator();
                while (insideRelations.hasNext()) {
                    Relation relation = (Relation)insideRelations.next();
                    if (relation != null) {
                        result += relation._description(detail,
                                indent + 1, 2) + "\n";
                    } else {
                        result += _getIndentPrefix(indent + 1) + "null\n";
                    }
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if this port is either a port of the specified entity,
     *  or a port of an entity that (deeply) contains the specified entity.
     *  This method is read-synchronized on the workspace.
     *  @param entity A possible container.
     *  @return True if this port is outside the entity.
     */
    protected boolean _isInsideLinkable(Nameable entity) {
        try {
            _workspace.getReadAccess();
            Nameable portContainer = getContainer();
            while (entity != null) {
                if (portContainer == entity) return true;
                entity = entity.getContainer();
            }
            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the link.  This method does not do validity checks.
     *  @param relation The relation to link to.
     */
    private void _doLink(Relation relation) throws IllegalActionException {
        if (relation != null && _workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            if (relation == null) {
                // Create a null link.
                _relationsList.link( null );
            } else {
                if (_isInsideLinkable(relation.getContainer())) {
                    // An inside link
                    _insideLinks.link( relation._getPortList() );
                } else {
                    // An outside link
                    _relationsList.link( relation._getPortList() );
                }
            }
            // NOTE: _checkLink() and _checkLiberalLink()
            // ensure that the container is
            // not null, and the class ensures that it is an Entity.
            ((Entity)getContainer()).connectionsChanged(this);
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    // 'transient' means that the variable will not be serialized.
    private transient List _deepLinkedPorts;
    private transient long _deepLinkedPortsVersion = -1;
    private transient List _deepLinkedInPorts;
    private transient long _deepLinkedInPortsVersion = -1;
}
