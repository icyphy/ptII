/* A Relation links ports, and therefore the entities that contain them.

 Copyright (c) 1997 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package pt.kernel;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Relation
/**
A Relation links ports, and therefore the entities that contain them.
It can be used to represent arcs in non-hierarchical graphs.
Ports may link themselves to Relations, but the other direction is
not supported.

@author Neil Smyth, Edward A. Lee
@version @(#)Relation.java	1.58  01/20/98
@see Port
@see Entity
*/
public class Relation extends NamedObj {

    /** Construct a relation in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public Relation() {
	super();
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a relation in the default workspace with the given name.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public Relation(String name)
            throws IllegalActionException {
	super(name);
        // Ignore exception because "this" cannot be null.
        try {
            _portList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a relation in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  If the name argument
     *  is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param workspace Object for synchronization and version tracking
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

    /** Enumerate the linked entities.  If any of the linked ports have no
     *  container, then those ports are ignored.  Thus, the enumeration may
     *  have fewer entries in it than what is reported by numLinks().
     *  Note that a particular entity may be listed more than once if it is
     *  linked more than once.
     *  This method is synchronized on the workspace.
     *  @return An Enumeration of Entity objects.
     */	
    public Enumeration getLinkedEntities() {
        synchronized(workspace()) {
            Enumeration ports = _portList.getLinks();
            LinkedList storedEntities = new LinkedList();

            while( ports.hasMoreElements() ) {
                Port port = (Port)ports.nextElement();
                Entity parent = (Entity) port.getContainer();
                // Ignore ports with no container.
                if (parent != null) {
                    storedEntities.insertLast( parent );
                }
            }

            return storedEntities.elements();
        }
    }

    /** Enumerate the linked ports.
     *  This method is synchronized on the workspace.
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration getLinkedPorts() {
        synchronized(workspace()) {
            return _portList.getLinks();
        }
    }

    /** Enumerate the linked ports except the specified port.
     *  This method is synchronized on the workspace.
     *  @param except Do not return this Port in the Enumeration 
     *  @return An Enumeration of Port objects.
     */	
    public Enumeration getLinkedPortsExcept(Port except) {
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

    /** Unlink the specified port. If the port
     *  is not linked to this relation, do nothing.
     *  This method is synchronized on the workspace and increments
     *  its version number.
     *  @param port
     */
    public void unlink(Relation port) {
        synchronized(workspace()) {
            _portList.unlink(port);
            workspace().incrVersion();
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

    /** Return a reference to the local port list.
     *  NOTE : This method has been made protected for the purpose
     *  of connecting Ports to Relations (see Port.link(Relation)), and so
     *  that it can be overridden in derived classes. If in a derived class
     *  no more links
     *  are supported for some reason, or the specified port is
     *  incompatible with this relation, throw an exception.  This base
     *  class does not throw this exception, but derived classes might
     *  in order to restrict the number or type of links that a relation has.
     *  @param port The port to link to.
     *  @exception IllegalActionException Relation cannot support any
     *   more links, or cannot support a link to the given port.
     */
    protected CrossRefList _getPortList (Port port) 
            throws IllegalActionException {
        return _portList;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         prive variables                          ////

    /** The CrossRefList of Ports which are connected to this Relation.
     */
    private CrossRefList _portList;
}
