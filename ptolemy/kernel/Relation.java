/* A Relation links ports, and therefore the entities that contain them.

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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.CrossRefList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Relation
/**
A Relation links ports, and therefore the entities that contain them.
To link a port to a relation, use the link() method
in the Port class.  To remove a link, use the unlink() method in the
Port class.
<p>
Derived classes may wish to disallow links under certain circumstances,
for example if the proposed port is not an instance of an appropriate
subclass of Port, or if the relation cannot support any more links.
Such derived classes should override the protected method _checkPort()
to throw an exception.

@author Edward A. Lee, Neil Smyth
@version $Id$
@since Ptolemy II 0.2
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Relation newObject = (Relation)super.clone(workspace);
        newObject._portList = new CrossRefList(newObject);
        return newObject;
    }

    /** List the linked ports.  Note that a port may appear more than
     *  once if more than one link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return A list of Port objects.
     */
    public List linkedPortList() {
        try {
            _workspace.getReadAccess();
            // Unfortunately, CrossRefList returns an enumeration only.
            // Use it to construct a list.
            LinkedList result = new LinkedList();
            Enumeration ports = _portList.getContainers();
            while (ports.hasMoreElements()) {
                result.add(ports.nextElement());
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the linked ports except the specified port.
     *  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @param except Port to exclude from the enumeration.
     *  @return A list of Port objects.
     */
    public List linkedPortList(Port except) {
        // This works by constructing a linked list and then returning it.
        try {
            _workspace.getReadAccess();
            LinkedList storedPorts = new LinkedList();
            Enumeration ports = _portList.getContainers();

            while (ports.hasMoreElements()) {
                Port p = (Port)ports.nextElement();
                if (p != except) storedPorts.add(p);
            }
            return storedPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the linked ports.  Note that a port may appear more than
     *  once if more than one link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An Enumeration of Port objects.
     */
    public Enumeration linkedPorts() {
        // NOTE: There is no reason to deprecate this because it does not
        // depend on Doug Lea's collections, and it is more efficient than
        // the list version.
        try {
            _workspace.getReadAccess();
            return _portList.getContainers();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the linked ports except the specified port.
     *  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @param except Port to exclude from the enumeration.
     *  @return An Enumeration of Port objects.
     *  @deprecated Use linkedPortList() instead.
     */
    public Enumeration linkedPorts(Port except) {
        return Collections.enumeration(linkedPortList(except));
    }

    /** Return the number of links to ports.
     *  This method is read-synchronized on the workspace.
     *  @return The number of links.
     */
    public int numLinks() {
        try {
            _workspace.getReadAccess();
            return _portList.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Unlink all ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            // NOTE: Do not just use _portList.unlinkAll() because then the
            // containers of the ports are not notified of the change.
            // Also, have to first copy the ports references, then remove
            // them, to avoid a corrupted enumeration exception.
            int size = _portList.size();
            Port portArray[] = new Port[size];
            int i = 0;
            Enumeration ports = _portList.getContainers();
            while (ports.hasMoreElements()) {
                Port p = (Port)ports.nextElement();
                portArray[i++] = p;
            }
            for (i = 0; i < size; i++) {
                portArray[i].unlink(this);
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
    protected void _checkPort (Port port) throws IllegalActionException {
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
                result += "links {\n";
                Enumeration linkedPorts = linkedPorts();
                while (linkedPorts.hasMoreElements()) {
                    Port port = (Port)linkedPorts.nextElement();
                    result += port._description(detail, indent + 1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
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
     *  @return An object of the same class as this object.
     *  @exception InternalErrorException If the object does not exist
     *   or has the wrong class, or if the specified container is not
     *   an instance of CompositeEntity.
     */
    protected NamedObj _getHeritageObject(String relativeName, NamedObj container)
            throws InternalErrorException {
        if (!(container instanceof CompositeEntity)) {
            throw new InternalErrorException(
                    "Expected "
                    + container.getFullName()
                    + " to be an instance of ptolemy.kernel.CompositeEntity, but it is "
                    + container.getClass().getName());
        }
        Relation candidate = ((CompositeEntity)container).getRelation(relativeName);
        if (!getClass().isInstance(candidate)) {
            throw new InternalErrorException(
                    "Expected "
                    + container.getFullName()
                    + " to contain a port with name "
                    + relativeName
                    + " and class "
                    + getClass().getName());
        }
        return candidate;
    }

    /** Return a reference to the local port list.
     *  NOTE : This method has been made protected only for the purpose
     *  of connecting Ports to Relations (see Port.link(Relation)).
     *  @see Port
     *  @return The link list.
     */
    protected CrossRefList _getPortList() {
        return _portList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The CrossRefList of Ports which are connected
     *  to this Relation.
     */
    private CrossRefList _portList = new CrossRefList(this);
}
