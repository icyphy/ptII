/* A Port is the interface between Entities and Relations.

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
import pt.exceptions.NullReferenceException;
import pt.exceptions.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
A Port is the interface between Entities and Relations.
@author John S. Davis II
@author Neil Smyth
@version $Id$
*/
public class Port extends GenericPort {
    /** 
     */	
    public Port() {
	super();
    }

    /** 
     * @param name The name of the Port.
     */	
    public Port(String name) {
	super(name);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Connect this Port to a Relation.
     * @param relation The Relation to which this Port will be connected.
     * @exception pt.exceptions.NullReferenceException Signals an attempt
     * to pass null object references as arguments.
     * @exception pt.exceptions.NameDuplicationException Attempt to store
     * two instances of the same class with identical names in the same
     * container.
     */	
    public void connectToRelation(Relation relation) 
	throws NullReferenceException, NameDuplicationException {

	_relation = relation;
	if( _relation == null ) {
	     throw new NullReferenceException( 
	     "Null Relation passed to Port.connectToRelation()" );
	} else if( !(_relation.isPortConnected(this.getName())) ) {
	     _relation.connectPort( this );
	} 
    }

    /** Disconnect this Port from its Relation.
     * @return Return the name of the Relation from which the Port
     * was connected. Return null if the connection was non-existent.
     */	
    public String disconnect() {
	if( _relation == null ) {
	     return null;
	}
	_relation.disconnectPort(this);
	_relation = null;
	return _relation.getName();
    }

    /** Return the Ports that are connected to this Port through its 
     *  Relation; return null if no connections exist.
     */	
    public Enumeration getConnectedPorts() {
	return _relation.getPorts();
    }

    /** Get the maximum particle count of this port.
     */
    public int getMaxParticleCount() {
	return __maxParticleCount;
    }

    /** Return the MultiPort which contains this Port.
     */	
    public MultiPort getMultiPortContainer() {
        return __multiPortContainer;
    }

    /** Return the name of this Port's Relation; return null if
     *  the Relation is null.
     */	
    public String getRelationName() {
	if( _relation == null ) {
	     return null;
	}
        return _relation.getName();
    }

    /** Return true if this Port is connected to another Port; return false
     *  otherwise.
     */	
    public boolean isConnected() {
	if( _relation != null ) {
	     if( !_relation.isDangling() ) {
		  return true;
	     }
	}
        return false;
    }

    /** Return true if this port is connected to a relation of a given
     *  name; return false otherwise.
     */
    public boolean isConnectedToRelation(String name) {
         if( _relation == null ) {
              return false;
         } else if( _relation.getName() == name ) {
              return true;
         }
         return false;
    }

    /** Return false since this is a Port.
     */
    public final boolean isMulti() {
        return false;
    }

    /** Prepare for a new connection by returning a port. 
     * @return Return the real port.
     * @exception pt.exceptions.NullReferenceException Signals an attempt
     * to pass null object references as arguments.
     */	
    public Port prepareForConnection() throws NullReferenceException {

	return (Port)realPort();
    }


    /** Set the maximum number of particles that can reside in this port.
     */
    public void setMaxParticleCount(int count) {
	__maxParticleCount = count;
    }

    /** Set the MuliPort which contains this Port.
     * @param multiPort The MultiPort which will be the container of this Port.
     * @exception pt.exceptions.NullReferenceException Signals an attempt
     * to pass null object references as arguments.
     */	
    public void setMultiPortContainer(MultiPort multiPort) 
	throws NullReferenceException {
	if( multiPort == null ) {
	     throw new NullReferenceException( 
	     "Null Multiport passed to Port.setMultiPortContainer()" );
	}
	__multiPortContainer = multiPort;
        return; 
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** The storage location for particles in this port.
     */
    protected LinkedList _buffer;

    /** This is the Relation through which the Port connects to other
     *  Ports. 
     */
    protected Relation _relation;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The maximum number of particles that can reside in this port.
     */
    private int __maxParticleCount = 1;

    /* The MultiPort which contains this Port.
     */
    private MultiPort __multiPortContainer;
}
