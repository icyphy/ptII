/* Relation supporting hierarchy.

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
*/

package pt.kernel;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ComponentRelation
/** 
Relation supporting hierarchy.  Specifically, a predicate isAlias()
is provided to differentiate alias relations from relations supporting
ordinary links.  In addition, methods are provided for deep accesses,
that is those that flatten the hierarchy.

@author Edward A. Lee
@version $Id$
*/
public class ComponentRelation extends Relation {

    /** Create an object with no name and no container */	
    public ComponentRelation() {
         super();
    }

    /** Create an object with a name and no container. 
     *  @param name
     *  @exception IllegalActionException Argument is null.
     */	
    public ComponentRelation(String name)
           throws IllegalActionException {
        super(name);
    }

    /** Create an object with a name and a container. 
     *  @param container
     *  @param name
     *  @exception IllegalActionException Name argument is null.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the container's contents list.
     */	
    public ComponentRelation(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
        super(name);
        // Inconsistency is impossible
        // at this stage, so we silence the compiler by catching.
        try {
            setContainer(container);
        } catch (InvalidStateException ex) {}
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Enumerate the entities linked to this relation.  If any of the ports
     *  that are directly linked to this relation are aliases, then follow those
     *  aliases down to the ports at the bottom of the alias chain, and include
     *  the container entities of those ports in the enumeration.  Note that
     *  if this is an alias relation, then only the bottom of the alias chain
     *  will be listed.  Thus, this method is not all that useful for alias
     *  relations.
     *  @return An enumeration of ComponentEntities.
     */	
    public Enumeration deepGetLinkedEntities() {
        Enumeration nearports = getLinkedPorts();
        LinkedList storedEntities = new LinkedList();
        
        while( nearports.hasMoreElements() ) {
            ComponentPort port = (ComponentPort)nearports.nextElement();
            ComponentPort bottom = port.deepGetDownAlias();
            Entity parent = (Entity) bottom.getContainer();
            // Ignore ports with no container.
            if (parent != null) {
                storedEntities.insertLast( parent );
            }
        }
        return storedEntities.elements();
    }

    /** Enumerate the ports linked to this relation.  If any of the ports
     *  that are directly linked to this relation are aliases, then follow those
     *  aliases down to the ports at the bottom of the alias chain, and include
     *  those ports in the enumeration.  Note that
     *  if this is an alias relation, then only the bottom of the alias chain
     *  will be listed.  Thus, this method is not all that useful for alias
     *  relations.
     *  @return An enumeration of ComponentPorts.
     */	
    public Enumeration deepGetLinkedPorts() {
        Enumeration nearports = getLinkedPorts();
        LinkedList storedEntities = new LinkedList();
        
        while( nearports.hasMoreElements() ) {
            ComponentPort port = (ComponentPort)nearports.nextElement();
            ComponentPort bottom = port.deepGetDownAlias();
            storedEntities.insertLast( bottom );
        }
        return storedEntities.elements();
    }

    /** Enumerate the ports linked to this relation, except the port given
     *  as an argument or any of its down aliases.  If any of the ports
     *  that are directly linked to this relation are aliases, then follow those
     *  aliases down to the ports at the bottom of the alias chain, and include
     *  those ports in the enumeration.  Note that
     *  if this is an alias relation, then only the bottom of the alias chain
     *  will be listed.  Thus, this method is not all that useful for alias
     *  relations.
     *  @param exceptport The port to exclude.
     *  @return An enumeration of ComponentPorts.
     */	
    public Enumeration deepGetLinkedPortsExcept(ComponentPort exceptport) {
        Enumeration nearports = getLinkedPorts();
        LinkedList storedEntities = new LinkedList();
        
        while( nearports.hasMoreElements() ) {
            ComponentPort port = (ComponentPort)nearports.nextElement();
            if (port != exceptport) {
                boolean include = true;
                ComponentPort bottom = port;
                AliasRelation downrelation = bottom.getDownAlias();
                while (downrelation != null) {
                    ComponentPort downport = downrelation.getDownAlias();
                    if (downport == exceptport) {
                        include = false;
                        break;
                    }
                    if (downport == null) break;
                    bottom = downport;
                    downrelation = downport.getDownAlias();
                }
                if (include) {
                    storedEntities.insertLast( bottom );
                }
            }
        }
        return storedEntities.elements();
    }

    /** Set the container.  Unless the argument
     *  is null, add the object to the relation list of the new container.
     *  If this object was previously contained, remove it from the relation
     *  list of the old container.
     *  @param container
     *  @exception InvalidStateException Inconsistent containment relationship.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the relation list of the container.
     */	
    public void setContainer(CompositeEntity container)
            throws InvalidStateException, NameDuplicationException {
        if (container != null) {
            // To ensure consistency, this is handled by the container.
            container.addRelation(this);
        } else {
            if (_container != null) {
                // To ensure consistency, this is handled by the container.
                // If this throws an IllegalActionException, then the container
                // does not contain me, which is an inconsistent state.
                try {
                    ((CompositeEntity)_container).removeRelation(this);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException(_container, this,
                          "Inconsistent containment relationship!");
                }
            } else {
                // Both the old and the new container are null.  Do nothing.
            }
        }
    }

    /** Return false, indicating that this relation is not an an alias
     *  relation.  Any derived class that returns true can be safely cast
     *  to AliasRelation().
     */	
    public boolean isAlias() {
        return false;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Return a reference to the local port list.  Throw an exception if
     *  there already is a link (only one link is supported), or if the
     *  specified port is not a ComponentPort.
     *  NOTE : This method has been made protected for the sole purpose
     *  of connecting Ports to Relations (see Port.link(Relation)). It
     *  should NOT be accessed by any other method.
     *  @param port The port to link to.
     *  @exception IllegalActionException AliasRelation cannot support more
     *   than one link.
     */
    protected CrossRefList _getPortList (Port port) 
            throws IllegalActionException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
                    "ComponentRelation can only link to a ComponentPort.");
        }
        return _portList;
    }

    /** Set the container.  This protected
     *  method should be called _only_ by CompositeEntity.addRelation() and
     *  CompositeEntity.removeRelation().
     *  This way, synchronization and consistency are ensured.
     *  Note that this method does nothing about removing this entity
     *  from the previous container's contents, nor does it check for
     *  recursive containment errors.
     *  @param container
     */	
    protected void _setContainer(CompositeEntity container) {
	_container = container;
    }
}
