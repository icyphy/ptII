/* A Port is the interface of an Entity to any number of Relations.

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

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
A Port is the interface of an Entity to any number of Relations.
Normally, a Port is contained (owned) by an Entity, although a port
may exist with no container.  The role of a port is to aggregate
some set of links to relations.  Thus, for example, to represent
a directed graph, entities can be created with two ports, one for
incoming arcs and one for outgoing arcs.  More generally, the arcs
to an entity may be divided into any number of subsets, with one port
representing each subset.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see Entity
@see Relation
*/
public class Port extends NamedObj {

    /** Create a port with no containing Entity. */	
    public Port() {
	super();
        // Ignore exception because "this" cannot be null.
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    /** Create a port contained by the specified Entity and having
     *  the specified name.  The port is appended to the port list
     *  of the entity, so its name must not already exist on the port
     *  list of the container.
     *  @param container The parent Entity.
     *  @param name The name of the Port.
     *  @exception IllegalActionException Name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *  an element already on the port list of the parent.
     */	
    public Port(Entity container, String name) 
             throws IllegalActionException, NameDuplicationException {
	super(name);
        // Ignore this exception because it can't occur with a new port.
        try {
            setContainer(container);
        } catch (InvalidStateException ex) {};
        // Ignore exception because "this" cannot be null.
        try {
            _relationsList = new CrossRefList(this);
        } catch (IllegalActionException ex) {}
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Enumerate the linked relations.
     *  @return An enumeration of Relation objects. 
     */
    public Enumeration getLinkedRelations() {
        if (_relationsList == null) {
            // Ignore exception because "this" cannot be null.
            try {
                _relationsList = new CrossRefList(this);
            } catch (IllegalActionException ex) {}
        }
	return _relationsList.getLinks();
    }

    /** Link this port with a relation.  If the argument is null,
     *  do nothing. The relation and the entity that contains this
     *  port are required to be at the same level of the hierarchy.
     *  I.e., if the container of this port has a container,
     *  then that container is required to also contain the relation.
     *  @param relation
     *  @exception IllegalActionException Link crosses levels of
     *   the hierarchy, or attempts to link with an incompatible relation,
     *   or the port has no container.
     */	
    public void link(Relation relation) 
            throws IllegalActionException {
        if (relation != null) {
            if (_container != null) {
                if (_container.getContainer() != relation.getContainer()) {
                    throw new IllegalActionException(this, relation,
                            "Link crosses levels of the hierarchy");
                }
            } else {
                throw new IllegalActionException(this, relation,
                       "Port must have a container to establish a link.");
            }
            _relationsList.link( relation._getPortList(this) );
            return;
        }
    }

    /** Return the number of linked relations.
     * @return A non-negative integer
     */
    public int numLinks() {
        if (_relationsList == null) return 0;
	else return _relationsList.size();
    }

    /** Specify the container entity, adding the port to the list of ports
     *  in the container.  If the container already contains
     *  a port with the same name, then throw an exception and do not make
     *  any changes.  If a container was previously specified, remove
     *  this port from its port list.  If the argument is null, the
     *  unlink the port from any relations.  Note that a port can be moved
     *  from one entity to another without unlinking the relations.
     *  @exception IllegalActionException Port is not of the expected class
     *   for container.
     *  @exception InvalidStateException Inconsistent port-container
     *   relationship, or this port has a null name.
     *  @exception NameDuplicationException Duplicate name in the container.
     */
    public void setContainer(Entity entity)
            throws IllegalActionException, InvalidStateException,
            NameDuplicationException {
        // NOTE: This code is fairly tricky, and is designed to ensure
        // consistency.  It works in concert with Entity.addPort()
        // and with Entity.removePort(), so do not modify it without
        // considering those.
        Entity prevcontainer = (Entity)_container;
        if (prevcontainer == entity) return;
        // Must set _container before calling addPort or removePort
        // to avoid infinite loop.
	_container = entity;
        if (prevcontainer != null) {
            try {
                prevcontainer.removePort(this);
            } catch (IllegalActionException ex) {
                // Restore state.
                _container = prevcontainer;
                throw new InvalidStateException(prevcontainer, this,
                       "Inconsistent port-container relationship!");
            }
        }
        if (entity != null) {
            try {
                entity.addPort(this);
            } catch (InvalidStateException ex) {
                // Restore state.
                _container = prevcontainer;
                // Rethrow same exception
                throw ex;
            } catch (NameDuplicationException ex) {
                // Restore state.
                _container = prevcontainer;
                // Rethrow same exception
                throw ex;
            }
        }
        if (_container == null) {
            unlinkAll();
        }
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing.
     *  @param relation
     */
    public void unlink(Relation relation) {
        if ( _relationsList != null ) _relationsList.unlink(relation);
        return;
    } 

    /** Unlink all relations.
     */	
    public void unlinkAll() {
	if( _relationsList == null ) return;
	_relationsList.unlinkAll();
	_relationsList = null;
	return;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The list of relations for this port.
    private CrossRefList _relationsList;
}
