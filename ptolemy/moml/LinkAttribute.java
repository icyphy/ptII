/* An attribute that represents a link between a port and a vertex.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// LinkAttribute
/** 
An attribute that represents a connection between a port and a vertex. 
The attribute contains a reference to the port and to the vertex within the
same relation that it graphically connects to.

@author Steve Neuendorffer
@version $Id$
*/
public class LinkAttribute extends Attribute {

    /** Construct an attribute with the given name and the specified
     *  contents for the the specified container.  If the container already
     *  contains an attribute by that name, replace it.  If the container
     *  rejects the attribute with an IllegalActionException, then
     *  the attribute is not forced upon the container, but rather is
     *  created with no container.
     *  @param container The container.
     *  @param name The name of this link.
     */	
    public LinkAttribute(NamedObj container, String name) 
	throws IllegalActionException, NameDuplicationException {
        super(container, name);
	if(!(container instanceof Relation)) {
	    throw new IllegalActionException(container, this, 
					     "Link can only be contained " + 
					     "in a relation");
	}
	_vertex = null;
	_port = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the vertex that this link is connected to, or 
     *  null if this link has not yet been connected to a vertex.
     */
    public VertexAttribute getVertex() {
	return _vertex;
    }

    /** Return the port that this link is connected to, or 
     *  null if this link has not yet been connected to a port.
     */
    public Port getPort() {
	return _port;
    }
 
    /** Set the container of this link.  
     *  @exception IllegalActionException If the container is not a Relation.
     */
    public void setContainer(NamedObj container) 
	throws IllegalActionException, NameDuplicationException {
	if(!(container instanceof Relation)) {
	    throw new IllegalActionException(container, this, 
					     "Vertex can only be contained " + 
					     "in a relation");
	} else {
	    super.setContainer(container);
	}
    }

    /** Set the vertex that this link is connected to.  If the given 
     *  vertex is null, then disconnect the link from the vertex.
     */
    public void setVertex(VertexAttribute vertex) {
	_vertex = vertex;
    }

    /** Set the port that this link is connected to.  If the given 
     *  port is null, then disconnect the link from the port.
     */
     public void setPort(Port value) {
	_port = value;
    }

    /** Get the vertex as a string.
     */	
    public String toString() {
	String portString, vertexString;
	if(_port == null) {
	    portString = "null";
	} else {
	    portString = _port.toString();
	}
	if(_vertex == null) {
	    vertexString = "null";
	} else {
	    vertexString = _vertex.toString();
	}

        return "Link[" + getName() + ", port=" + portString + 
	    ", vertex=" + vertexString + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The vertex that this attribute is connected to.
    private VertexAttribute _vertex;

    // The port that this vertex is connected to.
    private Port _port;
}
