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
import pt.exceptions.NameDuplicationException;
import pt.exceptions.NullReferenceException;

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
A Port is the interface between Entities and Relations.
@author John S. Davis, II
@version $Id$
*/
public class Port extends GenericPort {
    /** 
     * @param name The name of the Port.
     */	
    public Port(String name) {
	 super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the MultiPort which contains this Port. Return null if
     *  this Port is not part of a MultiPort.
     * @param relation The Relation to which this Port will be connected.
     * @exception NullReferenceException Attempt to pass null object 
     * references as arguments.
     * @exception NameDuplicationException Attempt to store two instances of
     * the same class with identical names in the same container.
     */	
    public void connectToRelation(Relation relation) 
	throws NullReferenceException, NameDuplicationException {
	relation_ = relation;
	if( relation_ == null ) {
	     throw new NullReferenceException( 
	     "Null Relation passed to Port.connectToRelation()" );
	}
	relation_.connectPort( this );
        return;
    }

    /** Disconnect this Port from its Relation.
     * @return Return the name of the Relation from which the Port
     * was connected. Return null if the connection was non-existent.
     */	
    public String disconnect() {
	if( relation_ == null ) {
	     return null;
	}
	relation_.disconnectPort(this);
	return relation_.getName();
    }

    /** Return the Ports that are connected to this Port through its Relation.
     *  Return null if no connections exist.
     */	
    public Enumeration getConnectedPorts() {
	return relation_.getPorts();
    }

    /** Return the MultiPort which contains this Port.
     */	
    public MultiPort getMultiPortContainer() {
        return _multiPortContainer;
    }

    /** Return the name of this Port's Relation. Return null if
     *  the Relation is null.
     */	
    public String getRelation() {
	if( relation_ == null ) {
	     return null;
	}
        return relation_.getName();
    }

    /** Insert this Port in the alias chain one level above
     *  the position of the argument.
     * @param lowerPort The Port above which this Port will be placed.
     */
    public void insertAliasAbove(Port lowerPort) {
	 super.insertAliasAbove_( lowerPort );
	 return;
    }

    /** Return false since this is a Port.
     */
    public final boolean isMulti() {
        return false;
    }

    /** Return true if this Port is connected to another Port. Return false
     *  otherwise.
     */	
    public boolean isConnected() {
	if( relation_ != null ) {
	     if( !relation_.isDangling() ) {
		  return true;
	     }
	}
        return false;
    }

    /** Set the MuliPort which contains this Port.
     * @param multiPort The MultiPort which will be the container of this Port.
     * @exception NullReferenceException Attempt to pass null object 
     * references as arguments.
     */	
    public void setMultiPortContainer(MultiPort multiPort) 
	throws NullReferenceException {
	if( multiPort == null ) {
	     throw new NullReferenceException( 
	     "Null Multiport passed to Port.setMultiPortContainer()" );
	}
	_multiPortContainer = multiPort;
        return; 
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** This is the Relation through which the Port connects to other
     *  Ports. The Relation is non-null only for Ports at the lowest level
     *  of the hierarchy.
     */
    protected Relation relation_;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////


    /* The MultiPort which contains this Port.
     */
    private MultiPort _multiPortContainer;
}
