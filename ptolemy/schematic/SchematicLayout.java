/* A SchematicLayout encapsulates a layout element of a SchematicElement

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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic;

import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// SchematicLayout
/**
A SchematicLayout element contains a non-semantic element that
is intepreted by the GUI. Layout alements typically have a name,
a type, and a set of coordinates.

<!-- layout elements will be parsed into class SchematicLayout -->
<!ELEMENT layout EMPTY>
<!ATTLIST layout
name ID #REQUIRED
coords CDATA ""
type CDATA #REQUIRED>

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicLayout extends XMLElement{

    /**
     * Create a SchematicLayout object with empty name,
     * type and value.
     */
    public SchematicLayout() {
        super("layout");
        setName("");
        setType("");
        setCoords("");
    }

    /**
     * Create a SchematicLayout object with the given attributes
     */
    public SchematicLayout(HashedMap attributes) {
        super("layout", attributes);
        if(!hasAttribute("name")) setName("");
        if(!hasAttribute("type")) setType("");
        if(!hasAttribute("coords")) setCoords("");
    }

    /**
     * Create a SchematicLayout object with the given name, type and
     * cordinates.
     */
    public SchematicLayout(
            String name,
            String type,
            String coords) {
        super("layout");
        setAttribute("name", name);
        setAttribute("type", type);
        setAttribute("coords", coords);
    }


    /**
     * Return the name of this layout element.
     */
    public String getName() {
        return getAttribute("name");
    }

    /**
     * Return the type of this layout element
     */
    public String getType() {
        return getAttribute("type");
    }

    /**
     * Return the coordinates of this layout element
     */
    public String getCoords() {
        return getAttribute("coords");
    }

    /**
     * Set the name of this layout element
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    /**
     * Set the type of this layout element
     */
    public void setType(String type) {
        setAttribute("type", type);
    }

    /**
     * set the coordinates of this layout element
     */
    public void setCoords(String coords) {
        setAttribute("coords", coords);
    }
}






