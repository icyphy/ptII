/* A Relation links ports, and therefore the entities that contain them.

 Copyright (c) 1997- The Regents of the University of California.
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
*/

package pt.kernel;

import pt.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

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
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a relation in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     *  @param name Name of this object.
     */
    public Relation(String name) {
	super(name);
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a relation in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace Workspace for synchronization and version tracking
     *  @param name Name of this object.
     */
    public Relation(Workspace workspace, String name) {
	super(workspace, name);
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Clone the object into the specified workspace and add the clone
     *  to the directory of that workspace.
     *  The result is a new relation with no links and no container.
     *  @param ws The workspace in which to place the cloned object. 
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     *  @return A new Relation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // NOTE: It is not actually necessary to override the base class
        // method, but we do it anyway so that the exact behavior of this
        // method is documented with the class.
        return super.clone(ws);
    }

    /** Enumerate the linked ports.  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration linkedPorts() {
        try {
            workspace().read();
            return _portList.getLinks();
        } finally {
            workspace().doneReading();
        }
    }

    /** Enumerate the linked ports except the specified port.
     *  Note that a port may appear more than
     *  once if more than on link to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @param except Port to exclude from the enumeration.
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration linkedPorts(Port except) {
        // This works by constructing a linked list and then enumerating it.
        try {
            workspace().read();
            LinkedList storedPorts = new LinkedList();
            Enumeration ports = _portList.getLinks();
        
            while(ports.hasMoreElements()) {
                Port p = (Port)ports.nextElement();
                if(p != except)
                    storedPorts.insertLast(p); 
            }
            return storedPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the number of links to ports.
     *  This method is read-synchronized on the workspace.
     *  @return The number of links.
     */	
    public int numLinks() {
        try {
            workspace().read();
            return _portList.size();
        } finally {
            workspace().doneReading();
        }
    }

    /** Unlink all ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     */	
    public void unlinkAll() {
        try {
            workspace().write();
            _portList.unlinkAll();
        } finally {
            workspace().doneWriting();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

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

    /** Clear references that are not valid in a cloned object.  The clone()
     *  method makes a field-by-field copy, which results
     *  in invalid references to objects. 
     *  In this class, this method reinitializes the private member
     *  _portList.
     *  @param ws The workspace the cloned object is to be placed in.
     */
    protected void _clearAndSetWorkspace(Workspace ws) {
        super._clearAndSetWorkspace(ws);
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.  Lines are indented according to
     *  to the level argument using the protected method _indent().
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent){
        try {
            workspace().read();
            String result = super._description(detail, indent);
            if ((detail & LINKS) != 0) {
                if (result.length() > 0) {
                    result += " ";
                }
                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result += "links {\n";
                Enumeration enum = linkedPorts();
                while (enum.hasMoreElements()) {
                    Port port = (Port)enum.nextElement();
                    result = result +
                            port._description(detail, indent+1) + "\n";
                }
                result = result + _indent(indent) + "}";
            }
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a reference to the local port list.
     *  NOTE : This method has been made protected only for the purpose
     *  of connecting Ports to Relations (see Port.link(Relation)).
     *  @see Port
     *  @return The link list.
     */
    protected CrossRefList _getPortList () {
        return _portList;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /** The CrossRefList of Ports which are connected to this Relation.
     */
    private CrossRefList _portList;
}
