/* An Entity is a vertex in a flat graph.

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
//// Entity
/** 
An Entity is a vertex in a flat graph. It incorporates the notion of graph 
connections and disconnections. Relation is the equivalent of a graph edge.
Entities and Relations are linked together via Ports.
@author John S. Davis II
@version $Id$
@see Port
@see Relation
*/
public abstract class Entity extends NamedObj { 
    /** 
     */	
    public Entity() {
	super();
    }

    /** 
     * @param name The name of the Entity.
     */	
    public Entity(String name) {
	super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return an enumeration of the Entities that this
     *  Entity is connected to. The enumeration does not
     *  contain this Entity.
     */	
    public Enumeration enumEntities() {
	 LinkedList storedEntities = new LinkedList();

	 if( _portList == null ) { 
	     return storedEntities.elements();
	 }
	 Enumeration thisEntitiesPorts = _portList.enumPorts(); 

	 while( thisEntitiesPorts.hasMoreElements() ) {
	     Port thisPort = (Port)thisEntitiesPorts.nextElement();
	     Enumeration relations = thisPort.enumRelations();

	     while( relations.hasMoreElements() ) {
		 Relation relation = (Relation)relations.nextElement();
		 Enumeration otherPorts = relation.enumPortsExcept( thisPort );

		 while( otherPorts.hasMoreElements() ) {
		     Port otherPort = (Port)otherPorts.nextElement();
		     storedEntities.insertLast( otherPort.getEntity() );
		 }
	     }
	 }
	 return storedEntities.elements();
    }


    /** Return an enumeration of the Entities that are connected
     * to this Entity via the specified Port. The enumeration does 
     * not contain this particular Entity. 
     * @param portName The name of the specified Port.
     * QFIXME: Should we verify that the specified port name is valid.
     * Suppose no such Port is owned.
     */	
    public Enumeration enumEntities(String portName) {
	 LinkedList storedEntities = new LinkedList();

	 if( _portList == null ) { 
	     return storedEntities.elements();
	 }
	 Enumeration thisEntitiesPorts = _portList.enumPorts(); 

	 while( thisEntitiesPorts.hasMoreElements() ) {
	     Port thisPort = (Port)thisEntitiesPorts.nextElement();

	     if( thisPort.getName().equals(portName) ) {
	         Enumeration relations = thisPort.enumRelations(); 
		 
		 while( relations.hasMoreElements() ) { 
		     Relation relation = (Relation)relations.nextElement(); 
		     Enumeration otherPorts 
			     = relation.enumPortsExcept( thisPort );

		     while( otherPorts.hasMoreElements() ) {
		         Port otherPort = (Port)otherPorts.nextElement(); 
			 storedEntities.insertLast( otherPort.getEntity() );
	             }
		 }
	     }
	 }
	 return storedEntities.elements();
    }


    /** Return an enumeration of Relations that this
     * Entity is connected through.
     */	
    public Enumeration enumRelations() {
	 LinkedList storedRelations = new LinkedList();

	 if( _portList == null ) { 
	     return storedRelations.elements();
	 }
	 Enumeration ports = _portList.enumPorts(); 

	 while( ports.hasMoreElements() ) {
	     Port newPort = (Port)ports.nextElement(); 
	     Enumeration relations = newPort.enumRelations(); 
	     storedRelations.prependElements( relations );
	 }
	 return storedRelations.elements();
    }


    /** Return an enumeration of Relations that are connected to this 
     * Entity via the specified Port.
     * QFIXME: Should we verify that portName is valid?
     * Suppose no such Port is owned.
     */	
    public Enumeration enumRelations(String portName) {
	 LinkedList storedRelations = new LinkedList();

	 if( _portList == null ) { 
	     return storedRelations.elements();
	 }
	 Enumeration ports = _portList.enumPorts(); 

	 while( ports.hasMoreElements() ) {
	     Port newPort = (Port)ports.nextElement();
	     if( newPort.getName().equals(portName) ) { 
		 Enumeration relations = newPort.enumRelations(); 
	         storedRelations.prependElements( relations );
	     }
	 }
	 return storedRelations.elements();
    }


    /** Return this Entity's PortList. */	
    public PortList getPortList() {
	 if( _portList == null ) {
	     _portList = new PortList();
	 }
	 return _portList;
    }


    /** Return the number of Entities connected to this. */	
    public int numberOfConnectedEntities() {
	 int count = 0;
	 Enumeration enum = enumEntities();
	 while( enum.hasMoreElements() ) {
	     count++;
	 }
	 return count;
    }


    /** Return the number of Entities connected to this Entity
     * through the specified Port.
     * @param portName The name of the specified Port.
     *  FIXME: Throw an exception if portName does not exist.
     */	
    public int numberOfConnectedEntities(String portName) {
	 int count = 0;
	 Enumeration enum = enumEntities(portName);
	 while( enum.hasMoreElements() ) {
	     count++;
	 }
	 return count;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // A list of Ports owned by this Entity.
    private PortList _portList;
}




