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
A port supporting hierarchy.
Specifically, it supports a mechanism whereby the port of a composite
entity can represent the port of one of its contained entities.
The port of the composite alias is called an "alias" or "up alias"
of the port of the contained entity.  The port of the contained entity
is called a "down alias" of the port of the composite entity.
Aliases can be chained to arbitrary depth.
As with all classes that support hierarchical graphs,
methods that read the graph structure come in two versions,
shallow and deep.  The deep version flattens the hierarchy.
For example, deepGetDownAlias() returns the port at the bottom
of an alias chain, whereas getDownAlias() returns the port one
level down in the alias chain.

@author Edward A. Lee
@version $Id$
*/
public class ComponentPort extends Port {

    /** Create a port with no containing Entity or name. */	
    public ComponentPort() {
	super();
    }

    /** Create a port contained by the specified entity and having
     *  the specified name.  The port is appended to the port list
     *  of the entity, so its name must not already exist on the port
     *  list of the container.
     *  @param container
     *  @param name
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *   an element already on the port list of the parent.
     */	
    public ComponentPort(ComponentEntity container, String name) 
             throws IllegalActionException, NameDuplicationException {
	super(container,name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the port at the bottom of the alias chain containing this
     *  port, or this port if there is no such alias chain.
     */	
    public ComponentPort deepGetDownAlias() {
        ComponentPort bottom = this;
        AliasRelation downrelation = _downAlias;
        while (downrelation != null) {
            ComponentPort downport = downrelation.getDownAlias();
            if (downport == null) break;
            bottom = downport;
            downrelation = downport.getDownAlias();
        }
        return bottom;
    }

    /** Enumerate the relations linked to this port and all of its up aliases.
     *  @return An enumeration of ComponentRelation objects.
     */	
    public Enumeration deepGetLinkedRelations() {
        Enumeration nearrelations = getLinkedRelations();
        LinkedList result = new LinkedList();
            
        while( nearrelations.hasMoreElements() ) {
            ComponentRelation relation =
                   (ComponentRelation)nearrelations.nextElement();
            if (relation.isAlias()) {
                ComponentPort upport = ((AliasRelation)relation).getUpAlias();
                if (upport != null) {
                    result.appendElements(upport.deepGetLinkedRelations());
                }
            } else {
                result.insertLast(relation);
            }
        }
        return result.elements();
    }

    /** Enumerate the deep entities linked to this port and all of its
     *  up aliases.  This is simply the containers of the ports returned
     *  by deepGetLinkedPorts().  Note that a particular entity may be
     *  listed more than once if there is more than one link to it.
     *  If any port has no container then nothing is included for that
     *  that port.
     *  @return An enumeration of ComponentEntity objects.
     */	
    public Enumeration deepGetLinkedEntities() {
        Enumeration ports = deepGetLinkedPorts();
        LinkedList result = new LinkedList();
        
        while (ports.hasMoreElements()) {
            ComponentPort port = (ComponentPort)ports.nextElement();
            Nameable container = port.getContainer();
            if (container != null) {
                result.insertLast(container);
            }
        }
        return result.elements();
    }

    /** Enumerate the ports linked to this port and all of its up aliases.
     *  If any ports are found that have down aliases, then the deep down
     *  alias of that port is the one listed.
     *  @return An enumeration of ComponentPort objects.
     */	
    public Enumeration deepGetLinkedPorts() {

        Enumeration nearrelations = getLinkedRelations();
        LinkedList result = new LinkedList();
            
        while( nearrelations.hasMoreElements() ) {
            ComponentRelation relation =
                 (ComponentRelation)nearrelations.nextElement();
            if (relation.isAlias()) {
                ComponentPort upport = ((AliasRelation)relation).getUpAlias();
                if (upport != null) {
                    result.appendElements(upport.deepGetLinkedPorts());
                }
            } else {
                result.appendElements(relation.deepGetLinkedPortsExcept(this));
            }
        }
        return result.elements();
    }

    /** Return the port immediately below this one in the alias chain, or
     *  null if there is no such port.
     */	
    public AliasRelation getDownAlias() {
        return _downAlias;
    }

    /** Set the down alias of this port, and the corresponding up alias
     *  of the relation argument.  The relation argument must belong to an
     *  entity that is contained by the entity that contains this port.
     *  Otherwise, throw an exception.  If there was previously a down alias,
     *  then reset (to null) the corresponding up alias first.
     *  Similarly, if the argument port previously had an up alias,
     *  set to null the down alias of the port it referred to.
     *  @param aliasrelation
     *  @exception IllegalActionException Alias relationship is required to
     *   span exactly one level of the hierarchy.
     */	
    public void setDownAlias(AliasRelation aliasrelation) 
            throws IllegalActionException {
        // First check validity (null is always valid).
        if (aliasrelation != null) {
            if (aliasrelation.getContainer() != getContainer()) {
                throw new IllegalActionException(this, aliasrelation,
                       "Alias relationship is required to " +
                       "span exactly one level of the hierarchy.");
            }
        }

        if (_downAlias != null) {
            _downAlias._setUpAlias(null);
        }
        _downAlias = aliasrelation;
        if (aliasrelation != null) {
            ComponentPort prevupport = aliasrelation.getUpAlias();
            if (prevupport != null) {
                prevupport.setDownAlias(null);
            }
            aliasrelation._setUpAlias(this);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Set the down alias of this port without ensuring consistency.
     *  This method should be used only by the public method setUpAlias().
     */	
    protected void _setDownAlias(AliasRelation relation) {
        _downAlias = relation;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The port of a contained entity.
    private AliasRelation _downAlias = null;
}
