/* An Icon is the graphical representation of a schematic entity.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;

//////////////////////////////////////////////////////////////////////////
//// Icon
/**

An icon is the graphical representation of a schematic entity.
Icons are stored hierarchically in icon libraries.   Every icon has a 
name, along with a graphical representation.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Icon extends NamedObj {

    /**
     * Create a new Icon with the name "icon". 
     * By default, the icon contains no graphic
     * representations.
     */
    public Icon () {
        super("icon");
        _graphics = (CircularList) new CircularList();
        _description = new String("");
    }

    /**
     * Create a new Icon with the given name.
     * By default, the icon contains no graphic
     * representations.
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public Icon (String name) {
        super(name);
        _graphics = (CircularList) new CircularList();
        _description = new String("");
    }

    /**
     * Add a new graphic to the icon. The format
     * is specified in the "format" attribute of the XMLElement.
     * The XMLElement must be of element type "graphic".
     *
     * @throw IllegalActionException if the element is not of element type
     * "graphic"
     * @throw IllegalActionException if a graphic with the same type as
     * the element is already associated with this Icon.
     */
    public void addGraphicElement (GraphicElement g) 
            throws IllegalActionException {
        _graphics.insertLast(g);
    }

    /**
     * Test if this icon contains a graphic in the
     * given format.
     */
    public boolean containsGraphicElement (GraphicElement g) {
        return _graphics.includes(g);
    }

    /**
     * Return a long description string of the the Icons in thie Library.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Return an enumeration over the names of the graphics formats
     * supported by this icon.
     *
     * @return Enumeration of String.
     */
    public Enumeration graphicElements() {
        return _graphics.elements();
    }

    /**
     * Remove a graphic element from the icon. Throw an exception if
     * the graphic element is not contained in this icon
     */
    public void removeGraphicElement (GraphicElement g)
            throws IllegalActionException {
        try {
            _graphics.removeOneOf(g);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("removeGraphicElement:" +
                    "GraphicElement not found in icon.");
        }
    }

   /**
     * Set the string that contains the long description of this icon.
     */
    public void setDescription(String s) {
        _description = s;
    }

    String _description;
    CircularList _graphics;

}

