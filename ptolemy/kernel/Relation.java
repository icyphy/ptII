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

@author Neil Smyth, Edward A. Lee
@version $Id$
@see Port
@see Entity
*/
public class Relation extends NamedObj {

    /** Construct a relation in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
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

    /** Clone the object and register the clone in the workspace.
     *  The result is a relation with no links and no container that
     *  is registered with the workspace.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     */
    public Object clone() throws CloneNotSupportedException {
        // NOTE: It is not actually necessary to override the base class
        // method, but we do it anyway so that the exact behavior of this
        // method is documented with the class.
        return super.clone();
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the Nameable interface.
     *  This method is synchronized on the workspace.
     *  @param verbosity The level of detail.
     *  @return A description of the object.
     */
    public String description(int verbosity){
        synchronized(workspace()) {
            String result = super.description(verbosity);
            if ((verbosity & LINKS) != 0) {
                if (result.length() > 0) {
                    result += " ";
                }
                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                verbosity &= ~LINKS;
                result += "links {";
                Enumeration enum = linkedPorts();
                while (enum.hasMoreElements()) {
                    Port port = (Port)enum.nextElement();
                    result = result + "\n" + port.description(verbosity);
                }
                result += "\n}";
            }
            return result;
        }
    }

    /** Enumerate the linked ports.
     *  This method is synchronized on the workspace.
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration linkedPorts() {
        synchronized(workspace()) {
            return _portList.getLinks();
        }
    }

    /** Enumerate the linked ports except the specified port.
     *  This method is synchronized on the workspace.
     *  @param except Port to exclude from the enumeration.
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration linkedPorts(Port except) {
        // This works by constructing a linked list and then enumerating it.
        synchronized(workspace()) {
            LinkedList storedPorts = new LinkedList();
            Enumeration ports = _portList.getLinks();
        
            while(ports.hasMoreElements()) {
                Port p = (Port)ports.nextElement();
                if(p != except)
                    storedPorts.insertLast(p); 
            }
            return storedPorts.elements();
        }
    }

    /** Return the number of links to ports.
     *  This method is synchronized on the workspace.
     */	
    public int numLinks() {
        synchronized(workspace()) {
            return _portList.size();
        }
    }

    /** Unlink all ports.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     */	
    public void unlinkAll() {
        synchronized(workspace()) {
            _portList.unlinkAll();
            workspace().incrVersion();
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
     */
    protected void _clear() {
        super._clear();
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Return a reference to the local port list.
     *  NOTE : This method has been made protected only for the purpose
     *  of connecting Ports to Relations (see Port.link(Relation)).
     *  @see Port
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
