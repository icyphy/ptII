/* Relations serve as a general notion of connection between Entities in a
hierarchical graph. 

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
import pt.exceptions.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Relation
/** 
Relations serve as a general notion of connection between Entities in a
hierarchical graph. Relations can be specialized to point-to-point
connections. FIXME: What should happen to a disconnected Net (zero 
connections)?
@author John S. Davis, II
@version $Id$
*/
public class Relation extends Node {
    /** 
     */	
    public Relation() {
	 super();
	 _nets = null;
	 _netAdditionCount = 0;
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	 super(name);
	 _nets = null;
	 _netAdditionCount = 0;
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
	createNewDanglingNet( newPort );
        return;
    }

    /** Connect three ports to each other through this Relation.
     * @param port1 The first port being connected to the triad.
     * @param port2 The second port being connected to the triad.
     * @param port3 The third port being connected to the triad.
     * @exception NameDuplicationException This exception is thrown if an
     * attempt is made to store two objects with identical names in the
     * same container.
     */
    public void connectThreePorts(Port port1, Port port2, Port port3) 
	throws NameDuplicationException {

	Net net23;
	net23 = getNet( port2, port3 ); 
	if( net23 != null ) {
	     connectNetToPort( net23, port1 );
	     consolidateNets();
	     return;
	} 

	Net net12;
	net12 = getNet( port1, port2 );
	if( net12 != null ) {
	     connectNetToPort( net12, port3 );
	     consolidateNets();
	     return;
	} 

	Net net13;
	net13 = getNet( port1, port3 );
	if( net13 != null ) {
	     connectNetToPort( net13, port2 );
	     consolidateNets();
	     return;
	} 

	return;
    }

    /** Connect two ports to each other through this Relation.
     * @param port1 The first port being connected.
     * @param port2 The second port being connected.
     * @exception NameDuplicationException This exception is thrown if an
     * attempt is made to store two objects with identical names in the
     * same container.
     */
    public void connectTwoPorts(Port port1, Port port2) 
	throws NameDuplicationException {

	boolean port1Membership;
	boolean port2Membership;

	port1Membership = isPortAMember( port1.getName() );
	port2Membership = isPortAMember( port2.getName() );

	if( port1Membership == false && port2Membership == false ) {
	     Net net;
	     net = createNewDanglingNet( port1 );
	     connectNetToPort( net, port2 );

	} else if( port1Membership == true && port2Membership == false ) {
	     Net port1Net;
	     port1Net = getDanglingNet( port1.getName() );
	     if( port1Net == null ) {
		  port1Net = createNewDanglingNet( port1 );
	     }
	     connectNetToPort( port1Net, port2 );

	} else if( port1Membership == false && port2Membership == true ) {
	     Net port2Net;
	     port2Net = getDanglingNet( port2.getName() );
	     if( port2Net == null ) {
		  port2Net = createNewDanglingNet( port2 );
	     }
	     connectNetToPort( port2Net, port1 );

	} else { 
	     Net port1Net, port2Net;

	     port1Net = getDanglingNet( port1.getName() );
	     if( port1Net != null ) {
	          connectNetToPort( port1Net, port2 );
		  return;
	     } 

	     port2Net = getDanglingNet( port2.getName() );
	     if( port2Net != null ) {
	          connectNetToPort( port2Net, port1 );
		  return;
	     }
	}

	// Both ports have connections, neither of which are dangling.
	Net port1Net;
	port1Net = createNewDanglingNet( port1 ); 
	connectNetToPort( port1Net, port2 );

	// Consolidate any duplicate Nets
	consolidateNets();
        return;
    }

    /** Check all Nets within this Relation and consolidate if necessary. 
     *  NOTE: Is it okay to remove elements as we iterate through the
     *  corresponding Enumeration?
     * @return Return the number of consolidations that occurred.
     */
    public int consolidateNets() {
	Enumeration net1Enum, net2Enum;
	Net net1, net2;
	int numRemovedNets = 0;

	net1Enum = _nets.elements();
	net2Enum = _nets.elements();

	while( net1Enum.hasMoreElements() ) {
	     net1 = (Net)net1Enum.nextElement();
	     while( net2Enum.hasMoreElements() ) {
		  net2 = (Net)net2Enum.nextElement();
		  if( consolidateNets( net1, net2 ) ) {
		       numRemovedNets++;
		  }
	     }
	}

        return numRemovedNets;
    }

    /** Disconnect two Ports from each other.
     * @param port1 The first Port being disconnected.
     * @param port2 The first Port being disconnected.
     * @returns Returns true if a disconnect occurs; returns false otherwise.
     */
    public boolean disconnectPorts(Port port1, Port port2) {
	if( _nets == null ) {
	     return false;
	}

	// Make sure that no Nets are being duplicated.
	consolidateNets();

	// Find the Net that these two Ports have in common.
	Net commonNet;
	commonNet = getNet(port1, port2);

	// Ports do not share a common Net and thus do not 
	// need to be disconnected.
	if( commonNet == null ) {
	     return false;
	}

	// Ports share a common Net.
	commonNet.disconnectPort( port1 );
	commonNet.disconnectPort( port2 );

        return true;
    }

    /** Disconnect a Port from this Relation.
     * @param port The Port being disconnected. 
     * @return Returns true if the Port is found and is disconnected;
     * returns false otherwise.
     */
    public boolean disconnectPort(Port port) {
	boolean portWasConnected = false;
	if( _nets == null ) {
	     return false;
	}

	Net net; 
	Enumeration enum = _nets.elements();
	while( enum.hasMoreElements() ) {
	     net = (Net)enum.nextElement();
	     if( net.isPortConnected( port.getName() ) ) {
		  net.disconnectPort( port );
		  portWasConnected = true;
	     }
	}

        return portWasConnected;
    }

    /** Determine if a Port is a member of any of the Nets.
     * @param portName The name of the Port for which membership is in question.
     * @return Returns true if the Port is a member; returns false otherwise.
     */
    public boolean isPortAMember(String portName) {
	if( _nets == null ) {
	     return false;
	}

	Net net; 

	Enumeration enum = _nets.elements();
	while( enum.hasMoreElements() ) {
	     net = (Net)enum.nextElement();
	     if( net.isPortConnected( portName ) ) {
		  return true;
	     }
	}
        return false;
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

    /* Connect a new Port to a Net. 
     */
    private void connectNetToPort(Net net, Port newPort ) 
	throws NameDuplicationException {
	net.connectPort( newPort );
        return;
    }

    /* Determine if two Nets contain the same Ports. If so, 
     * remove one of the Nets and consolidate one of them.
     * FIXME: What if both Nets are dangling? This shouldn't
     * be a problem.
     * NOTE: Doug Lea's Collections package includes a sameStructure()
     * method which offers the functionality of this method.
     * @param net1 The first Net for which a match is sought. 
     * @param net2 The first Net for which a match is sought. 
     * @return Returns true if a consolidation occurred; returns false
     * otherwise.
     */
    private boolean consolidateNets(Net net1, Net net2) {
	Enumeration net1PortEnum; 
	Enumeration net2PortEnum;
	Port net1Port, net2Port;
	int netMatchCount = 0;
	int previousNetMatchCount = 0;

	if( net1.size() != net2.size() ) {
	     return false;
	}

	net1PortEnum = net1.getPorts();

	// NOTES: This algorithm relies on the fact that no duplications
	// of Ports connected to Nets can occur.
	while( net1PortEnum.hasMoreElements() ) { 
	     net1Port = (Port)net1PortEnum.nextElement(); 
	     previousNetMatchCount++;
	     
	     net2PortEnum = net2.getPorts();
	     while( net2PortEnum.hasMoreElements() ) {
		  net2Port = (Port)net2PortEnum.nextElement();
		  if( net1Port.equals( net2Port ) ) {
		       netMatchCount++;
		  }
	     }
	     if( previousNetMatchCount != netMatchCount ) {
		  return false;
	     }
	     previousNetMatchCount++;
	}

	// The Nets are identical. Remove net2. 
	net2PortEnum = net2.getPorts();
	while( net2PortEnum.hasMoreElements() ) {
	     net2Port = (Port)net2PortEnum.nextElement();
	     net2.disconnectPort( net2Port );
	}
	_nets.remove( net2.getName() );
        return true;
    }

    /* Create a new dangling Net for a Port. A dangling Net is one 
     * which has only one Port connected to it. 
     */
    private Net createNewDanglingNet(Port port) 
	throws NameDuplicationException {
	if( _nets == null ) {
	     _nets = new Hashtable();
	}
	Net newNet = new Net( createNewName() );
	newNet.connectPort( port );
	_nets.put( newNet.getName(), newNet );

        return newNet;
    }

    /* Create a unique name for a new Net. Names are of the form 
     * "net#num" where "num" is an enumeration of the order in which the
     * Net in question was added.
     */
    private String createNewName() {
	_netAdditionCount++;
        String name;
        name = "net" + "#" + _netAdditionCount;
        return name;
    }

    /* Determine if a Port has a dangling net. 
     */
    private Net getDanglingNet(String portName) {
	Net net; 
	Enumeration enum = _nets.elements();

	if( _nets == null ) {
	     return null;
	}
	while( enum.hasMoreElements() ) {
	     net = (Net)enum.nextElement();
	     if( net.isPortConnected( portName ) ) {
		  net.isDangling();
		  return net;
	     }
	}
        return null;
    }

    /* Get a Net that is connected to two specified Ports. 
     */
    private Net getNet(Port port1, Port port2) {
	if( _nets == null ) {
	     return null;
	}

	Net net;

	Enumeration enum = _nets.elements();
	while( enum.hasMoreElements() ) {
	     net = (Net)enum.nextElement();
	     if( net.isPortConnected( port1.getName() ) ) {
		  if( net.isPortConnected( port2.getName() ) ) {
		       return net;
		  }
	     }
	}
        return null;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* This is a count of Ports that have been added to MultiPort. Note that
     * this only increments, even if Ports are removed. This variable is
     * used for creating unique Port names.
     */
    private int _netAdditionCount;

    /* The Nets are the elemental connection units upon which a 
     * Relation is based. 
     */
    private Hashtable _nets;
}





