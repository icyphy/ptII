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
@author John S. Davis, II
@version $Id$
*/
public class Relation extends Node {
    /** 
     */	
    public Relation() {
	 super();
	 _buffer = null;
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	 super(name);
	 _buffer = null;
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
    /*
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
    */

    /** If this is a destination Relation, make the Port argument the
     *  new destination port. 
     * @param port The port which will become the new destinatino port.
     * @return Return true if successful. Return false if this is 
     * not a destination Relation. 
     */	
    /*
    public boolean changeDestinationPort(Port port) {
	if( isDestination() ) {
	     _destinationPorts.clear();
	     // _destinationPorts.insertFirst( port.newConnection() ); 
	     _destinationPorts.put( port.getName(), port.newConnection() ); 
	     return true;
	}
        return false;
    }
    */

    /** If this is a source Relation, make the Port argument the
     *  new source port. 
     * @param port The port which will become the new source port.
     * @return Return true if successful. Return false if this is 
     * not a source Relation. 
     */	
    /*
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
    */

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
}





