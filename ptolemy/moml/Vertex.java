/* An attribute that represents a waypoint in a relation.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Vertex
/**
This attribute represents a waypoint in a relation. It extends
Location, meaning that can be associated with a physical location
in a visual rendition.  It can optionally be associated with another
instance of Vertex to indicate that there is a path from this
one to the other one. Cyclic paths are not permitted, although
currently that is not enforced by this class.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
*/
public class Vertex extends Location {

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Vertex(Workspace workspace) {
        super(workspace);
        getMoMLInfo().elementName = "vertex";
    }

    /** Construct an attribute with the given name and position.
     *  @param container The container.
     *  @param name The name of the vertex.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Vertex(Relation container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().elementName = "vertex";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified port to the list of ports linked to this vertex.
     *  @param port The port to link.
     */
    public void addLinkedPort(Port port) {
        if (_ports == null) {
            _ports = new LinkedList();
        }
        _ports.add(port);
    }

    /** Write a MoML description of this object.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name", "class", and "value" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline. If the object is non-persistent, then
     *  write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_suppressMoML()) {
            return;
        }
        // This method is very similar to the superclass
        // Location.exportMoML() except that this method does not
        // include the 'class='.

        String value = getExpression();
        String valueTerm = "";
        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" +
                StringUtilities.escapeForXML(value) + "\"";
        }

        output.write(_getIndentPrefix(depth)
                + "<"
                + getMoMLInfo().elementName
                + " name=\""
                + name
                // + "\" class=\""
                // + getMoMLInfo().className
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }


    /** Return the other vertex to which there is a path from this vertex, or
     *  null if there is none.  Note that the paths are one directional,
     *  so this vertex might return null even though there is another vertex
     *  with a path to it.
     */
    public Vertex getLinkedVertex() {
        return _linked;
    }

    /** Get the list of ports linked to this vertex.
     *  @return A list of ports connected to this vertex.
     */
    public List linkedPorts() {
        // FIXME: Perhaps this should return an unmodifiable version?
        return _ports;
    }

    /** Remove the specified port from the list of ports linked to this vertex.
     *  If the port is not linked, do nothing.
     *  @param port The port to remove.
     */
    public void removeLinkedPort(Port port) {
        if (_ports != null) {
            _ports.remove(port);
        }
    }

    /** Set the other vertex to which there is a path from this vertex.
     *  If the argument is null, remove the path.
     *  @param vertex The vertex to link to this one.
     */
    public void setLinkedVertex(Vertex vertex) {
        _linked = vertex;
    }

    /** Get a description of the class, which is the class name and
     *  the location in parentheses.
     *  @return A string describing the object.
     */
    public String toString() {
        // FIXME add linked ports.
        return super.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see ptolemy.kernel.util.NamedObj#_exportMoMLContents
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        if (_linked != null) {
            output.write(_getIndentPrefix(depth));
            output.write("<pathTo=\"" + _linked.getName() + "\"/>\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The vertex that this attribute is connected to.
    private Vertex _linked;

    // The list of linked ports.
    private LinkedList _ports;
}
