/* A Relation is an arc in a hierarchical graph.

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

import java.util.Hashtable;
import java.util.Enumeration;
import collections.UpdatableBag;

//////////////////////////////////////////////////////////////////////////
//// Relation
/** 
A Relation is an arc in a hierarchical graph. Relations serve as a general 
notion of connection Entities and should be thought of as nets that can be 
specialized to point-to-point connections. 
<A HREF="pt.kernel.Particles.html#_top_">Particles</A> reside in in Relations 
during transit. Unlike Entities, Relations can not contain ports but they 
do have port references. Relations come in two types as follows: 
<UL>
<LI><EM>Source Relations</EM>: Facilitate a one-to-many topology.</LI>
<LI><EM>Destination Relations</EM>: Facilitate a many-to-one topology.</LI>
</UL>
@author John S. Davis, II
@version $Id$
@see pt.kernel.Particle
*/
public class Relation extends Node {
    /** 
     */	
    public Relation() {
	 super();
	 _buffer = null;
	 _sourcePorts = null;
	 _destinationPorts = null;
         _isSourceOrDestination = 0;
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	 super(name);
	 _buffer = null;
	 _sourcePorts = null;
	 _destinationPorts = null;
         _isSourceOrDestination = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Add a source port. If this is a source Relation, first verify
     *  that no other source port has already been set. 
     * @param port The port which will be added as a source port.
     * @return Return true if the port was successfully added. Return
     * false if this is a source Relation and the source was already
     * set (in which case changeSourcePort() should be used). 
     */	
    public boolean addSourcePort(Port port) {
	Port genPortWithDuplicateName;
	if( isSource() && _sourcePorts == null ) {
	     genPortWithDuplicateName = (Port)
		  _sourcePorts.put( port.getName(), port.newConnection() ); 
	     if( genPortWithDuplicateName != null ) {
		  //FIXME: Throw exception.
	     }
	     return true;
	} else if( isDestination() ) {
	     genPortWithDuplicateName = (Port)
		  _destinationPorts.put( port.getName(), port.newConnection() );
	     if( genPortWithDuplicateName != null ) {
		  //FIXME: Throw exception.
	     }
	     return true;
	}
	return false;
    }

    /** If this is a destination Relation, make the Port argument the
     *  new destination port. 
     * @param port The port which will become the new destinatino port.
     * @return Return true if successful. Return false if this is 
     * not a destination Relation. 
     */	
    public boolean changeDestinationPort(Port port) {
	if( isDestination() ) {
	     _destinationPorts.clear();
	     // _destinationPorts.insertFirst( port.newConnection() ); 
	     _destinationPorts.put( port.getName(), port.newConnection() ); 
	     return true;
	}
        return false;
    }

    /** If this is a source Relation, make the Port argument the
     *  new source port. 
     * @param port The port which will become the new source port.
     * @return Return true if successful. Return false if this is 
     * not a source Relation. 
     */	
    public boolean changeSourcePort(Port port) {
	Port genPortWithDuplicateName;
	if( isSource() ) {
	     _sourcePorts.clear();
	     genPortWithDuplicateName = (Port)
		  _sourcePorts.put( port.getName(), port.newConnection() ); 
	     if( genPortWithDuplicateName != null ) {
		  //FIXME: Throw exception.
	     }
	     return true;
	}
        return false;
    }

    /** Clear all port references.
     */	
    public void clearAllConnections() {
        _sourcePorts.clear();
        _destinationPorts.clear();
        return;
    }

    /** Get all particles in the Relation's buffer.
     * @return Return an Enumeration of the particles contained within
     * this Relation. Return null if empty.
     */	
    public Enumeration get() {
	if( _buffer == null ) {
	     return null;
	}
	return _buffer.elements();
    }

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation is a Destination. Return false 
     * otherwise.
     */	
    public boolean isDestination() {
	if( _isSourceOrDestination == 2 ) {
	     return true;
	}
        return false;
    }

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation has been set as a Source or
     * Destination relation. Return false otherwise.
     */	
    public boolean isRelationTypeSet() {
	if( _isSourceOrDestination <= 0 || _isSourceOrDestination >= 3 ) {
	     return true;
	}
        return false;
    }

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation is a Source. Return false otherwise.
     */	
    public boolean isSource() {
	if( _isSourceOrDestination == 1 ) {
	     return true;
	}
        return false;
    }

    /** Make this Relation a Destination if it has not already been set to a
     *  Destination
     * @return Return true if successful. Return false if this Relation
     * was previously set as a Source.
     */	
    public boolean makeDestinationRelation() {
	if( _isSourceOrDestination != 1 ) {
	     _isSourceOrDestination = 2;
	     return true;
	}
        return false;
    }

    /** Make this Relation a Source if it has not already been set to a
     *  Destination
     * @return Return true if successful. Return false if this Relation
     * was previously set as a Destination.
     */	
    public boolean makeSourceRelation() {
	if( _isSourceOrDestination != 2 ) {
	     _isSourceOrDestination = 1;
	     return true;
	}
        return false;
    }

    /** Put a particle in the Relation's buffer.
     * @param particle The Particle to be placed in the Relation.
     */	
    public void put(Particle particle) {
	_buffer.add( particle );
        return;
    }

    /** Remove a destination port from this relation.
     * @param port The Port to be removed.
     * @return Return a Port reference if the remove is successful. Return
     * null if the Port is not found.
     */	
    public Port removeDestinationPort(Port port) {
	return (Port) _destinationPorts.remove( port.getName() );
    }

    /** Remove a source port from this relation.
     * @param port The Port to be removed.
     * @return Return a Port reference if the remove is successful. Return
     * null if the Port is not found.
     */	
    public Port removeSourcePort(Port port) {
	return (Port) _sourcePorts.remove( port.getName() );
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The buffer which holds particles as they travel through the
     * the Relation. Note that this buffer has no delay properties
     * associated with it at this level. Derived versions of this
     * class might.
     */
    private UpdatableBag _buffer;

    /* The list of destination ports connected via this Relation
     */
    private Hashtable _destinationPorts;

    /* Set to 1 if this is an source relation. Set to 2 if this is a 
     * destination relation. Set to 0 by the constructor to indicate null. 
     */
    private int _isSourceOrDestination;

    /* The list of source ports connected via this Relation
     */
    private Hashtable _sourcePorts;
}





