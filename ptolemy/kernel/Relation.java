/* A Relation is an arc in a flat graph.

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
//// Relation
/** A Relation is an arc in a flat graph. A Relation connects n links such
that each link has access to the other n-1 links. Relations interface to 
Entities, the vertices of a graph, via Ports.  Ports may attach themselves 
to Relations, but the other direction does not hold.
@author Neil Smyth
@version $Id$
@see Port
@see Entity
*/
public class Relation extends NamedObj {
    /** 
     */	
    public Relation() {
	super();
        _portList = new CrossRefList(this);
    }


    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	super(name);
        _portList = new CrossRefList(this);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the Ports which are connected to this Relation.
     * @return Return an Enumeration of Ports; return null if the
     * collection of Ports is null.
     */	
    public Enumeration enumPorts() {
        return new PortEnumeration();
    }


    /** Return the Ports which are connected to this Relation, except for 
     *  the specified Port
     * @param exceptPort Do not return this Port in the Enumeration 
     * @return Return an Enumeration of Ports; returns null if the
     *  collection of Ports is null.
     */	
    public Enumeration enumPortsExcept(Port exceptPort) {
        return new PortEnumeration(exceptPort);
    }


    /** Return the Entities which are connected to this Relation.
     * @return Return an Enumeration of Entities; returns null if the
     *  collection of Entities is null.
     */	
    public Enumeration enumEntities() {

	Enumeration XRefs = _portList.elements();
        LinkedList storedEntities = new LinkedList();

        while( XRefs.hasMoreElements() ) {
            Port tmpPort = (Port)XRefs.nextElement();
	    Entity tmpEntity = tmpPort.getEntity();
	    storedEntities.insertLast( tmpEntity );
        }

        return storedEntities.elements();
    }


    /** Determine if a given Port is connected to this Relation.
     * @param portName The name of the Port for which we check connectivity.
     * @return Return true if the Port is connected to this Relation. Return 
     *  false otherwise.
     */	
    public boolean isPortConnected(String portName) {
	Enumeration XRefs = _portList.elements();
        while (XRefs.hasMoreElements()) {
            Port nextPort = (Port)XRefs.nextElement();
            if (nextPort.getName() == portName) return true;
	}
	return false;
    }


    /** Return the number of Ports connected to the relation.
     */	
    public int numberOfConnections() {
	if( _portList == null ) {
            return 0;
	}
        return _portList.size();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////
    /* A CrossRefList of Ports which are connected to this Relation.
     * Note : This member has been made protected for the sole purpose of
     * connecting Ports to Relations (see Port.connect(Port)). It should 
     * NOT be modified by any other method.
     */
    protected CrossRefList _portList;

    //////////////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////

    // Class PortEnumeration
    /** Wrapper class for returning an emumeration of Ports. It uses the 
     *  elements() method provided by CrossRefList
     *  @see CrossRefList
     */

    private class PortEnumeration implements Enumeration {

        public PortEnumeration() {
            _XRefEnum = _portList.elements();
        }

        /** @param exceptPort Do not return this port in the enumeration. */
        public PortEnumeration(Port exceptPort) {
            if(exceptPort != null) {	    // Remove exceptPort.
                CrossRefList censored = new CrossRefList(this);
                censored.duplicate(_portList);
                censored.dissociate(exceptPort);
                _XRefEnum = censored.elements();
            } else {
                _XRefEnum = _portList.elements();
            }
        }
      
        /** Check if there are remaining elements to enumerate. */
        public boolean hasMoreElements() {
            return _XRefEnum.hasMoreElements();
        }
      
        /** Return the next element in the enumeration. */
        public Object nextElement() {
            return (Port)_XRefEnum.nextElement();
        }
      
        private Enumeration _XRefEnum;
    }
}
