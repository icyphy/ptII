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
Normally links are only contained within a relation, but in the course of 
manipulation, it is sometimes convenient for them to be contained within
a composite entity, hence this is allowed.

@author Steve Neuendorffer
@version $Id$
*/
public class LinkAttribute extends Attribute {

    /** Construct an attribute with the given name and the specified
     *  contents for the the specified container. 
     *  @param container The container.
     *  @param name The name of this link.
     */	
    public LinkAttribute(NamedObj container, String name) 
	throws IllegalActionException, NameDuplicationException {
        super(container, name);
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
 
    /** Set the vertex that this link is connected to.  If the given 
     *  vertex is null, then disconnect the link from the vertex.  Update
     *  the links associated with the 
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

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if (result.trim().length() > 0) {
                result += " ";
            }
            result += "vertex {\n";
            if(_vertex == null) {
                result += _getIndentPrefix(indent+1) + "null";
            } else {
                result += _getIndentPrefix(indent+1) + _vertex.getFullName();
            }
            result += "\n" + _getIndentPrefix(indent) + "} ";

            result += "port {\n";
            if(_port == null) {
                result += _getIndentPrefix(indent+1) + "null";
            } else {
                result += _getIndentPrefix(indent+1) + _port.getFullName();
            }
            result += "\n" + _getIndentPrefix(indent) + "} ";

            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The vertex that this attribute is connected to.
    private VertexAttribute _vertex;

    // The port that this vertex is connected to.
    private Port _port;
}
