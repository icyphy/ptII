/* A port supporting hierarchy.

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
//// ComponentPort
/** 
A port supporting hierarchy. A component port can have "inside"
links as well as the usual "outside" links supported by the base
class. An inside link is a link to a relation that is within the
container of the port. Any ComponentPort object may be transparent
or not. It is transparent if it has one or
more inside links.

As with all classes that support hierarchical graphs,
methods that read the graph structure come in two versions,
shallow and deep.  The deep version flattens the hierarchy.
Deep traversals pass through transparent ports. This is
done with a simple rule. If a transparent port is encountered from
inside, then the traversal continues with its outside links. If it
is encountered from outside, then the traversal continues with its
inside links. 

For ComponentPort to support both inside links and outside links, it
has to override the link() and unlink() methods. Given a relation as
an argument, these methods can determine whether a link is an inside
link or an outside link by checking the container of the relation.
If that container is also the container of the port, then the link
is an inside link.

For a few applications, such as Statecharts, level-crossing
connections are needed. The links in these connections are created
using the liberalLink() method of ComponentPort. The link() method
prohibits such links, throwing an exception if they are attempted
(most applications will prohibit level-crossing connections by using
only the link() method).

@author Edward A. Lee
@version $Id$
*/
public class ComponentPort extends Port {

    /** Construct a port in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public ComponentPort() {
	super();
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public ComponentPort(Workspace workspace) {
	super(workspace);
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the port.
     *  @exception NameDuplicationException Name coincides with
     *   a port already in the container.
     */	
    public ComponentPort(ComponentEntity container, String name) 
            throws NameDuplicationException {
	super(container,name);
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Deeply enumerate the ports connected to this port on the outside.
     *  Begin by enumerating the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then enumerate all the ports deeply connected
     *  on the outside to that transparent port.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentPort objects.
     */	
    public Enumeration deepConnectedPorts() {
        synchronized(workspace()) {
            if (_deeplinkedportsversion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deeplinkedports.elements();
            }
            Enumeration nearrelations = linkedRelations();
            LinkedList result = new LinkedList();
            
            while( nearrelations.hasMoreElements() ) {
                ComponentRelation relation =
                    (ComponentRelation)nearrelations.nextElement();

                Enumeration connectedports =
                    relation.linkedPortsExcept(this);
                while (connectedports.hasMoreElements()) {
                    ComponentPort port =
                        (ComponentPort)connectedports.nextElement();
                    // NOTE: If level-crossing transitions are not allowed, then
                    // a simpler test than that of the following would work.
                    if (port._outside(relation.getContainer())) {
                        // Port is transparent, and we are coming at it from
                        // the inside.
                        result.appendElements(port.deepConnectedPorts());
                    } else {
                        // We are coming at the port from the outside.
                        // Is it transparent?
                        if (port.numInsideLinks() > 0) {
                            // It is transparent.
                            result.appendElements(port.deepInsidePorts());
                        } else {
                            result.insertLast(port);
                        }
                    }
                }
            }
            _deeplinkedports = result;
            _deeplinkedportsversion = workspace().getVersion();
            return _deeplinkedports.elements();
        }
    }

    /** Deeply enumerate the ports linked on the inside to this port.
     *  If there are no such ports, then enumerate just this port.
     *  All ports enumerated are opaque, in that they have no
     *  inside links.  Note that the returned enumeration could
     *  conceivably be empty, for example if this port has an inside
     *  link, but no other ports are linked to the inside relation.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentPort objects.
     */	
    public Enumeration deepInsidePorts() {
        synchronized(workspace()) {
            if (_deeplinkedinportsversion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deeplinkedinports.elements();
            }
            LinkedList result = new LinkedList();
            if (numInsideLinks() > 0) {
                // Port is transparent.
                Enumeration relations = insideRelations();
                while (relations.hasMoreElements()) {
                    Relation relation = (Relation)relations.nextElement();
                    Enumeration insideports =
                        relation.linkedPortsExcept(this);
                    while (insideports.hasMoreElements()) {
                        ComponentPort downport =
                            (ComponentPort)insideports.nextElement();
                        if (downport._outside(relation.getContainer())) {
                            result.appendElements(
                                    downport.deepConnectedPorts());
                        } else {
                            result.appendElements(
                                    downport.deepInsidePorts());
                        }
                    }
                }
            } else {
                // Port is opaque.
                result.insertLast(this);
            }
            _deeplinkedinports = result;
            _deeplinkedinportsversion = workspace().getVersion();
            return _deeplinkedinports.elements();
        }
    }

    /** Return a description of the object
     *  @param verbosity The level of verbosity.
     */
    public String description(int verbosity){
        String results = new String();
        switch (verbosity) {
        case pt.kernel.Nameable.LIST_CONTENTS:
            return toString();
        case pt.kernel.Nameable.CONTENTS:
            return toString() + "\n";
        case pt.kernel.Nameable.LIST_CONNECTIONS:
        case pt.kernel.Nameable.CONNECTIONS:
            Enumeration enum = insideRelations();
            while (enum.hasMoreElements()) {
                Relation relation = (Relation)enum.nextElement();
                results = results.concat(toString() + " link "
                        + relation.toString() + "\n");
            }
            enum = linkedRelations();
            while (enum.hasMoreElements()) {
                Relation relation = (Relation)enum.nextElement();
                results = results.concat(toString() + " link "
                        + relation.toString() + "\n");
            }
            return results;
        case pt.kernel.Nameable.PRETTYPRINT:
            return description(CONTENTS) + description(CONNECTIONS);
        case pt.kernel.Nameable.LIST_PRETTYPRINT:
            return description(LIST_CONTENTS) + description(LIST_CONNECTIONS);
        case pt.kernel.Nameable.QUIET:
        default:
            return toString();
        }
    }

    /** Enumerate the ports connected on the inside to this port.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentPort objects.
     */	
    public Enumeration insidePorts() {
        synchronized(workspace()) {
            LinkedList result = new LinkedList();
            Enumeration relations = insideRelations();
            while (relations.hasMoreElements()) {
                Relation relation = (Relation)relations.nextElement();
                result.appendElements(relation.linkedPortsExcept(this));
            }
            return result.elements();
        }
    }

    /** Enumerate the relations linked on the inside to this port.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */	
    public Enumeration insideRelations() {
        synchronized(workspace()) {
            return _insideLinks.getLinks();
        }
    }

    /**
     */
    public boolean isDeeplyConnected(ComponentPort port) {
        if(port == null) return false;
        synchronized(workspace()){
            // Call deepConnectedPort to refresh the cache.
            Enumeration dummy = deepConnectedPorts();
            return _deeplinkedports.includes(port);
        }
    }
            
    /** Link this port with a relation.  The only constraints are
     *  that the port and the relation share the same workspace, and
     *  that the relation be of a compatible type (ComponentRelation).
     *  They are not required to be at the same level of the hierarchy.
     *  To prohibit links across levels of the hierarchy, use link().
     *  Both inside and outside links are supported.
     *  If the relation argument is null, do nothing.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException Relation does not share the same
     *   workspace, or attempt to link with an incompatible relation,
     *   of the port has no container.
     */	
    public void liberalLink(Relation relation) 
            throws IllegalActionException {
        if (relation == null) return;
        if (workspace() != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        synchronized(workspace()) {
            if (_outside(relation.getContainer())) {
                // An inside link
                _checkRelation(relation);
                if (getContainer() == null) {
                    throw new IllegalActionException(this, relation,
                            "Port must have a container to establish a link.");
                }
                _insideLinks.link(relation._getPortList(this));
            } else {
                // An outside link
                _link(relation);
            }
        }
    }

    /** Link this port with a relation.  This method calls liberalLink()
     *  if the proposed link does not cross levels of the hierarchy, and
     *  otherwise throws an exception.
     *  If the argument is null, do nothing.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param relation
     *  @exception IllegalActionException Link crosses levels of
     *   the hierarchy, or attempts to link with an incompatible relation,
     *   or the port has no container.
     */	
    public void link(Relation relation) 
            throws IllegalActionException {
        if (relation == null) return;
        if (workspace() != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        synchronized(workspace()) {
            _checkRelation(relation);
            Nameable container = getContainer();
            if (container != null) {
                Nameable relcont = relation.getContainer();
                if (container != relcont &&
                        container.getContainer() != relcont) {
                    throw new IllegalActionException(this, relation,
                            "Link crosses levels of the hierarchy");
                }
            }
            liberalLink(relation);
        }
    }

    /** Return the number of inside links.
     *  This method is synchronized on the workspace.
     *  @return A non-negative integer
     */
    public int numInsideLinks() {
        synchronized(workspace()) {
            return _insideLinks.size();
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     *  @param relation
     */
    public void unlink(Relation relation) {
        synchronized(workspace()) {
            // Not sure whether it's an inside link, so unlink both.
            super.unlink(relation);
            _insideLinks.unlink(relation);
            workspace().incrVersion();
        }
    } 

    /** Unlink all relations, inside and out.
     *  This method is synchronized on the workspace
     *  and increments its version number.
     */	
    public void unlinkAll() {
        synchronized(workspace()) {
            super.unlinkAll();
            _insideLinks.unlinkAll();
            workspace().incrVersion();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Do nothing if the specified relation is compatible with this port.
     *  Otherwise, throw an exception.
     *  @param relation
     *  @exception IllegalActionException Incompatible relation.
     */	
    public void _checkRelation(Relation relation) 
            throws IllegalActionException {
        if (!(relation instanceof ComponentRelation)) {
            throw new IllegalActionException(this,
                    "Attempt to link to an incompatible relation.");
        }
    }

    /** Return true if the port is either a port of the specified entity,
     *  or a port of an entity that contains the specified entity.
     *  This method is synchronized on the workspace.
     *  @param entity A container.
     */	
    public boolean _outside(Nameable entity) {
        synchronized(workspace()) {
            Nameable portcontainer = getContainer();
            while (entity != null) {
                if (portcontainer == entity) return true;
                entity = entity.getContainer();
            }
            return false;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // The list of inside relations for this port.
    private CrossRefList _insideLinks;

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    // 'transient' means that the variable will not be serialized.
    private transient LinkedList _deeplinkedports;
    private long _deeplinkedportsversion = -1;
    private transient LinkedList _deeplinkedinports;
    private long _deeplinkedinportsversion = -1;
}
