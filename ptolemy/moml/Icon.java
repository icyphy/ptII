/* An attribute that represents a renderable form of the container.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// Icon
/**
An attribute that represents a renderable form of the container.
It represents a visual depiction that can be placed on the screen.

@author  Edward A. Lee, Steve Neuendorffer
@version $Id$
*/
public class Icon extends Attribute implements Locatable {

    /** Construct an attribute with the specified container and name.
     *  The location contained by the attribute is initially null,
     *  but can be set using the setLocation() method.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Icon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	setLocation(null);
	setMoMLElementName("rendition");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the location in some cartesian coordinate system.
     *  @return The location.
     */
    public double[] getLocation() {
        return _location;
    }

    /** Set the location in some cartesian coordinate system.
     *  @param location The location.
     */
    public void setLocation(double[] location) {
        _location = location;
    }

    /** Get a description of the class, which is the class name and
     *  the location in parentheses.
     *  @return A string describing the object.
     */
    public String toString() {
        String className = getClass().getName();
        if (_location == null) {
            return "(" + className + ", null)";
        }
        StringBuffer location = new StringBuffer();
        for (int i = 0; i < _location.length; i++) {
            if (i > 0) location.append(", ");
            location.append("" +_location[i]);
        }

        return "(" + className + ", Location = (" + location.toString() + "))";
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
     *  @see NamedObj#_exportMoMLContents
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
	super._exportMoMLContents(output, depth);
	if(_location != null && _location.length > 0) {
            output.write(_getIndentPrefix(depth));
            output.write("<location value=\"" + _location[0]);
            if (_location.length > 1) {
                output.write(", " + _location[1]);
                if (_location.length > 2) {
                    output.write(", " + _location[1]);
                }
            }
            output.write("\"/>\n");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The location.
    private double[] _location;
}
