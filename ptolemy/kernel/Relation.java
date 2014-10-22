/* A Relation links ports, and therefore the entities that contain them.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.kernel;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.util.CrossRefList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Relation

/**
 A Relation links ports, and therefore the entities that contain them.
 To link a port to a relation, use the link() method
 in the Port class.  To remove a link, use the unlink() method in the
 Port class.
 <p>
 Relations can also be linked to other Relations.
 To create such a link, use the link() method of this class.
 To remove such a link, use the unlink() method. A group of linked
 relations behaves exactly as if it were one relation directly linked
 to all the ports linked to by each relation. In particular, the
 connectedPortList() method of the Port class returns the same list
 whether a single relation is used or a relation group. The order
 in which the ports are listed is the order in which links were
 made between the port and relations in the relation group.
 It is not relevant which relation in a relation group the port
 links to.
 <p>
 Derived classes may wish to disallow links under certain circumstances,
 for example if the proposed port is not an instance of an appropriate
 subclass of Port, or if the relation cannot support any more links.
 Such derived classes should override the protected method _checkPort()
 or _checkRelation() to throw an exception.

 @author Edward A. Lee, Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 @see Port
 @see Entity
 */
public class Relation extends NamedObj {
    /** Construct a relation in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public Relation() {
        super();
        _elementName = "relation";
    }

    /** Construct a relation in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public Relation(String name) throws IllegalActionException {
        super(name);
        _elementName = "relation";
    }

    /** Construct a relation in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version tracking.
     */
    public Relation(Workspace workspace) {
        super(workspace);
        _elementName = "relation";
    }

    /** Construct a relation in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace Workspace for synchronization and version tracking
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public Relation(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
        _elementName = "relation";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes cannot
     *   be cloned.
     *  @return A new Relation.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Relation newObject = (Relation) super.clone(workspace);
        newObject._linkList = new CrossRefList(newObject);
        return newObject;
    }

    /** Link this relation with another relation.  The relation is required
     *  to be at the same level of the hierarchy as this relation.
     *  That is, level-crossing links are not allowed.
     *  If the specified relation is already linked to this one,
     *  do nothing.
     *  In derived classes, the relation may be required to be an
     *  instance of a particular subclass of Relation (this is checked
     *  by the _checkRelation() protected method).
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param relation The relation to link to this relation.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or this relation has no container, or this relation is not in the
     *   same workspace as the relation.
     */
    public void link(Relation relation) throws IllegalActionException {
        if (relation != null && _workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }

        try {
            _workspace.getWriteAccess();

            if (relation != null) {
                _checkRelation(relation, true);

                if (!_linkList.isLinked(relation)) {
                    _linkList.link(relation._linkList);
                }
            } else {
                _linkList.link(null);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a list of the objects directly linked to this
     *  relation (ports and relations).
     *  Note that a port may appear more than
     *  once if more than one link to it has been established,
     *  but a relation will appear only once. There is no significance
     *  to multiple links between the same relations.
     *  This method is read-synchronized on the workspace.
     *  @return A list of Port and Relation objects.
     */
    public List linkedObjectsList() {
        try {
            _workspace.getReadAccess();

            // NOTE: This should probably be cached.
            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            LinkedList result = new LinkedList();
            Enumeration links = _linkList.getContainers();

            while (links.hasMoreElements()) {
                Object next = links.nextElement();
                result.add(next);
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the ports linked to any relation in this relation's
     *  group.  Note that a port may appear more than
     *  once if more than one link to it has been established.
     *  The order in which ports are listed has no significance.
     *  This method is read-synchronized on the workspace.
     *  @return A list of Port objects.
     */
    public List linkedPortList() {
        try {
            _workspace.getReadAccess();

            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            LinkedList result = new LinkedList();
            Enumeration links = _linkList.getContainers();

            Set<Relation> exceptRelations = new HashSet<Relation>();
            exceptRelations.add(this);

            while (links.hasMoreElements()) {
                Object next = links.nextElement();

                if (next instanceof Port) {
                    result.add(next);
                } else {
                    // Must be another relation.
                    result.addAll(((Relation) next)._linkedPortList(null,
                            exceptRelations));
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the ports linked to any relation in this relation's
     *  group except the specified port.
     *  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  The order in which ports are listed has no significance.
     *  This method is read-synchronized on the workspace.
     *  @param except Port to exclude from the list.
     *  @return A list of Port objects.
     */
    public List linkedPortList(Port except) {
        // This works by constructing a linked list and then returning it.
        try {
            _workspace.getReadAccess();

            LinkedList result = new LinkedList();
            Enumeration links = _linkList.getContainers();

            Set<Relation> exceptRelations = new HashSet<Relation>();
            exceptRelations.add(this);

            while (links.hasMoreElements()) {
                Object link = links.nextElement();

                if (link instanceof Port) {
                    if (link != except) {
                        result.add(link);
                    }
                } else {
                    // Must be another relation.
                    result.addAll(((Relation) link)._linkedPortList(except,
                            exceptRelations));
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the linked ports.  Note that a port may appear more than
     *  once if more than one link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use linkedPortList() instead.
     *  @return An Enumeration of Port objects.
     */
    @Deprecated
    public Enumeration linkedPorts() {
        return Collections.enumeration(linkedPortList());
    }

    /** Enumerate the linked ports except the specified port.
     *  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @param except Port to exclude from the enumeration.
     *  @return An Enumeration of Port objects.
     *  @deprecated Use linkedPortList(Port) instead.
     */
    @Deprecated
    public Enumeration linkedPorts(Port except) {
        return Collections.enumeration(linkedPortList(except));
    }

    /** Return the number of links to ports, either directly
     *  or indirectly via other relations in the relation
     *  group. This is the size
     *  of the list returned by linkedPortList().
     *  This method is read-synchronized on the workspace.
     *  @return The number of links.
     *  @see #linkedPortList()
     */
    public int numLinks() {
        return linkedPortList().size();
    }

    /** Return the list of relations in the relation group containing
     *  this relation.
     *  The relation group includes this relation, all relations
     *  directly linked to it, all relations directly linked to
     *  those, etc. That is, it is a maximal set of linked
     *  relations. There is no significance to the order of the
     *  returned list, but the returned value is a List rather than
     *  a Set to facilitate testing.
     *  @return The relation group.
     */
    public List relationGroupList() {
        LinkedList result = new LinkedList();
        _relationGroup(result);
        return result;
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this relation, do nothing.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            _linkList.unlink(relation);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all ports and relations that are directly linked
     *  to this relation.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();

            // NOTE: Do not just use _linkList.unlinkAll() because then the
            // containers of ports are not notified of the change.
            // Also, have to first copy the link references, then remove
            // them, to avoid a corrupted enumeration exception.
            int size = _linkList.size();
            Object[] linkedObjectsArray = new Object[size];
            int i = 0;
            Enumeration links = _linkList.getContainers();

            while (links.hasMoreElements()) {
                Object linkedObject = links.nextElement();
                linkedObjectsArray[i++] = linkedObject;
            }

            // NOTE: It would be better if there were a
            // Linkable interface so that these instanceof
            // tests would not be needed.
            for (i = 0; i < size; i++) {
                if (linkedObjectsArray[i] instanceof Port) {
                    ((Port) linkedObjectsArray[i]).unlink(this);
                } else {
                    ((Relation) linkedObjectsArray[i]).unlink(this);
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation.  In this base class, the exception is not thrown, but
     *  in derived classes, it might be, for example if the relation cannot
     *  support any more links, or the port is not an instance of an
     *  appropriate subclass of Port.
     *  @param port The port to link to.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
    }

    /** Check that this relation is compatible with the specified relation.
     *  In this base class, this method calls the corresponding check method
     *  of the specified relation, and if that returns, then it returns.
     *  Derived classes should override this method to check validity
     *  of the specified relation, and then call super._checkRelation().
     *  @param relation The relation to link to.
     *  @param symmetric If true, the call _checkRelation on the specified
     *   relation with this as an argument.
     *  @exception IllegalActionException If this relation has no container,
     *   or if this relation is not an acceptable relation for the specified
     *   relation.
     */
    protected void _checkRelation(Relation relation, boolean symmetric)
            throws IllegalActionException {
        if (relation != null && symmetric) {
            // Throw an exception if this relation is not of an acceptable
            // class for the specified relation.
            relation._checkRelation(this, false);
        }
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    @Override
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            StringBuffer result = new StringBuffer();

            if (bracket == 1 || bracket == 2) {
                result.append(super._description(detail, indent, 1));
            } else {
                result.append(super._description(detail, indent, 0));
            }

            if ((detail & LINKS) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result.append("links {\n");

                Enumeration links = _linkList.getContainers();

                while (links.hasMoreElements()) {
                    Object object = links.nextElement();

                    if (object instanceof Port) {
                        result.append(((Port) object)._description(detail,
                                indent + 1, 2) + "\n");
                    } else {
                        result.append(((Relation) object)._description(detail,
                                indent + 1, 2) + "\n");
                    }
                }

                result.append(_getIndentPrefix(indent) + "}");
            }

            if (bracket == 2) {
                result.append("}");
            }

            return result.toString();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get a relation with the specified name in the specified container.
     *  The returned object is assured of being an
     *  instance of the same class as this object.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object, which
     *   must be an instance of CompositeEntity.
     *  @return An object of the same class as this object, or null if
     *   there is none.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class, or if the specified container is not
     *   an instance of CompositeEntity.
     */
    @Override
    protected NamedObj _getContainedObject(NamedObj container,
            String relativeName) throws IllegalActionException {
        if (!(container instanceof CompositeEntity)) {
            throw new InternalErrorException("Expected "
                    + container.getFullName()
                    + " to be an instance of ptolemy.kernel.CompositeEntity, "
                    + "but it is " + container.getClass().getName());
        }

        Relation candidate = ((CompositeEntity) container)
                .getRelation(relativeName);

        if (candidate != null && !getClass().isInstance(candidate)) {
            throw new IllegalActionException(this, "Expected "
                    + candidate.getFullName() + " to be an instance of "
                    + getClass().getName() + ", but it is "
                    + candidate.getClass().getName());
        }

        return candidate;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of Ports and Relations that are linked to this Relation. */
    protected CrossRefList _linkList = new CrossRefList(this);

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** List the linked ports except the <i>exceptPort</i> and
     *  those linked via the relations in the <i>exceptRelations</i>
     *  list. This relation is
     *  added to the <i>exceptRelations</i> list before returning.
     *  Note that a port may appear more than
     *  once if more than one link to it has been established.
     *  This method is not read-synchronized on the workspace,
     *  so the caller is expected to be.
     *  @param exceptPort A port to exclude, or null to not
     *   specify one.
     *  @param exceptRelations Set of relations to exclude.
     *  @return A list of Port objects.
     */
    private List _linkedPortList(Port exceptPort, Set exceptRelations) {
        // This works by constructing a linked list and then returning it.
        LinkedList result = new LinkedList();

        if (exceptRelations.contains(this)) {
            return result;
        }

        // Prevent listing the ports connected to this relation again.
        exceptRelations.add(this);

        Enumeration links = _linkList.getContainers();

        while (links.hasMoreElements()) {
            Object link = links.nextElement();

            if (link instanceof Port) {
                if (link != exceptPort) {
                    result.add(link);
                }
            } else {
                // Link must be to a relation.
                Relation relation = (Relation) link;

                if (!exceptRelations.contains(relation)) {
                    result.addAll(relation._linkedPortList(exceptPort,
                            exceptRelations));
                }
            }
        }

        return result;
    }

    /** Append to the specified list all relations in the relation
     *  group with this one that are not already on the list.
     *  The relation group includes this relation, all relations
     *  directly linked to it, all relations directly linked to
     *  those, etc.
     *  @param list The list to append to.
     */
    private void _relationGroup(List list) {
        if (!list.contains(this)) {
            list.add(this);

            Enumeration links = _linkList.getContainers();

            while (links.hasMoreElements()) {
                Object link = links.nextElement();

                if (link instanceof Relation) {
                    ((Relation) link)._relationGroup(list);
                }
            }
        }
    }
}
