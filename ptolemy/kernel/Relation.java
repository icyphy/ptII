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
import pt.exceptions.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Relation
/** 
A Relation is an arc in a hierarchical graph. Relations serve as a general 
notion of connection between Entities and should be thought of as nets 
that can be specialized to point-to-point connections. 
@author John S. Davis, II
@version $Id$
*/
public class Relation extends Node {
    /** 
     */	
    public Relation() {
	 super();
	 _shorts = null;
	 _shortAdditionCount = 0;
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	 super(name);
	 _shorts = null;
	 _shortAdditionCount = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Add a Port to this Relation. 
     * @param newPort The Port being added to this Relation.
     * @exception NameDuplicationException This exception is thrown if an
     * attempt is made to store two objects with identical names in the
     * same container.
     */
    public void addPort(Port newPort) throws NameDuplicationException {
	if( isPortAMember( newPort.getName() ) ) {
	     return;
	}
	createNewDanglingShort( newPort );
        return;
    }

    /** Connect two ports to each other through this Relation.
     * @param port1 This Port will be connected to port2. 
     * @param port2 This Port will be connected to port1. 
     * @exception NameDuplicationException This exception is thrown if an
     * attempt is made to store two objects with identical names in the
     * same container.
     */
    public void connectPorts(Port port1, Port port2) 
	throws NameDuplicationException {

	boolean port1Membership;
	boolean port2Membership;

	port1Membership = isPortAMember( port1.getName() );
	port2Membership = isPortAMember( port2.getName() );

	if( port1Membership == false && port2Membership == false ) {
	     Short aShort;
	     aShort = createNewDanglingShort( port1 );
	     connectShortToPort( aShort, port2 );

	} else if( port1Membership == true && port2Membership == false ) {
	     Short port1Short;
	     port1Short = getDanglingShort( port1.getName() );
	     if( port1Short == null ) {
		  port1Short = createNewDanglingShort( port1 );
	     }
	     connectShortToPort( port1Short, port2 );

	} else if( port1Membership == false && port2Membership == true ) {
	     Short port2Short;
	     port2Short = getDanglingShort( port2.getName() );
	     if( port2Short == null ) {
		  port2Short = createNewDanglingShort( port2 );
	     }
	     connectShortToPort( port2Short, port1 );

	} else { 
	     Short port1Short, port2Short;

	     port1Short = getDanglingShort( port1.getName() );
	     if( port1Short != null ) {
	          connectShortToPort( port1Short, port2 );
		  return;
	     } 

	     port2Short = getDanglingShort( port2.getName() );
	     if( port2Short != null ) {
	          connectShortToPort( port2Short, port1 );
		  return;
	     }
	}

	// Both ports have connections, neither of which are dangling.
	Short port1Short;
	port1Short = createNewDanglingShort( port1 ); 
	connectShortToPort( port1Short, port2 );
        return;
    }

    /** Determine if a Port is a member of any of the Shorts.
     * @param portName The name of the Port for which membership is in question.
     * @return Returns true if the Port is a member; returns false otherwise.
     */
    public boolean isPortAMember(String portName) {
	if( _shorts == null ) {
	     return false;
	}

	Short aShort; 

	Enumeration enum = _shorts.elements();
	while( enum.hasMoreElements() ) {
	     aShort = (Short)enum.nextElement();
	     if( aShort.isPortConnected( portName ) ) {
		  return true;
	     }
	}
        return false;
    }

    /** Check all Shorts within this Relation and consolidate if necessary. 
     *  // NOTE: Is it okay to remove elements as we iterate through the
     *  // corresponding Enumeration?
     * @return Return the number of consolidations that occurred.
     */
    public int consolidateShorts() {
	Enumeration short1Enum, short2Enum;
	Short short1, short2;
	int numRemovedShorts = 0;

	short1Enum = _shorts.elements();
	short2Enum = _shorts.elements();

	while( short1Enum.hasMoreElements() ) {
	     short1 = (Short)short1Enum.nextElement();
	     while( short2Enum.hasMoreElements() ) {
		  short2 = (Short)short2Enum.nextElement();
		  if( consolidateShorts( short1, short2 ) ) {
		       numRemovedShorts++;
		  }
	     }
	}

        return numRemovedShorts;
    }

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
    public int APublicMethod() {
        return 1;
    }
     */

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /* Connect a new Port to a Short. 
     */
    private void connectShortToPort(Short aShort, Port newPort ) 
	throws NameDuplicationException {
	aShort.connectPort( newPort );
        return;
    }

    /* Determine if two Shorts contain the same Ports. If so, 
     * remove one of the Shorts and consolidate one of them.
     * // FIXME: What if both Shorts are dangling? This shouldn't
     * // be a problem.
     * // NOTE: Doug Lea's Collections package includes a sameStructure()
     * // method which offers the functionality of this method.
     * @param short1 The first Short for which a match is sought. 
     * @param short2 The first Short for which a match is sought. 
     * @return Returns true if a consolidation occurred; returns false
     * otherwise.
     */
    private boolean consolidateShorts(Short short1, Short short2) {
	Enumeration short1PortEnum; 
	Enumeration short2PortEnum;
	Port short1Port, short2Port;
	int shortMatchCount = 0;
	int previousShortMatchCount = 0;

	if( short1.size() != short2.size() ) {
	     return false;
	}

	short1PortEnum = short1.getPorts();

	// NOTES: This algorithm relies on the fact that no duplications
	// of Ports connected to Shorts can occur.
	while( short1PortEnum.hasMoreElements() ) { 
	     short1Port = (Port)short1PortEnum.nextElement(); 
	     previousShortMatchCount++;
	     
	     short2PortEnum = short2.getPorts();
	     while( short2PortEnum.hasMoreElements() ) {
		  short2Port = (Port)short2PortEnum.nextElement();
		  if( short1Port.equals( short2Port ) ) {
		       shortMatchCount++;
		  }
	     }
	     if( previousShortMatchCount != shortMatchCount ) {
		  return false;
	     }
	     previousShortMatchCount++;
	}

	// The Shorts are identical. Remove short2. 
	short2PortEnum = short2.getPorts();
	while( short2PortEnum.hasMoreElements() ) {
	     short2Port = (Port)short2PortEnum.nextElement();
	     short2.disconnectPort( short2Port );
	}
	_shorts.remove( short2.getName() );
        return true;
    }

    /* Create a new dangling Short for a Port. A dangling Short is one 
     * which has only one Port connected to it. 
     */
    private Short createNewDanglingShort(Port port) 
	throws NameDuplicationException {
	if( _shorts == null ) {
	     _shorts = new Hashtable();
	}
	Short newShort = new Short( createNewName() );
	newShort.connectPort( port );
	_shorts.put( newShort.getName(), newShort );

        return newShort;
    }

    /* Create a unique name for a new Short. Names are of the form 
     * "short#num" where "num" is an enumeration of the order in which the
     * Short in question was added.
     */
    private String createNewName() {
	_shortAdditionCount++;
        String name;
        name = "short" + "#" + _shortAdditionCount;
        return name;
    }

    /* Get a Short that is connected to two specified Ports. 
     */
    private Short getShort(Port port1, Port port2) {
	if( _shorts == null ) {
	     return null;
	}

	Short aShort;

	Enumeration enum = _shorts.elements();
	while( enum.hasMoreElements() ) {
	     aShort = (Short)enum.nextElement();
	     if( aShort.isPortConnected( port1.getName() ) ) {
		  if( aShort.isPortConnected( port2.getName() ) ) {
		       return aShort;
		  }
	     }
	}
        return null;
    }

    /* Determine if a Port has a dangling short. 
     */
    private Short getDanglingShort(String portName) {
	Short aShort; 
	Enumeration enum = _shorts.elements();

	if( _shorts == null ) {
	     return null;
	}
	while( enum.hasMoreElements() ) {
	     aShort = (Short)enum.nextElement();
	     if( aShort.isPortConnected( portName ) ) {
		  aShort.isDangling();
		  return aShort;
	     }
	}
        return null;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The Shorts are the elemental connection units upon which a 
     * Relation is based. 
     */
    private Hashtable _shorts;

    /* This is a count of Ports that have been added to MultiPort. Note that
     * this only increments, even if Ports are removed. This variable is
     * used for creating unique Port names.
     */
    private int _shortAdditionCount;
}





