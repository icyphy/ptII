/* An GraphicElement is an atomic piece of a graphical representation.

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
//// GraphicElement
/**
An GraphicElement is an atomic piece of a graphical representation.
i.e. a line, box, textbox, etc.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class GraphicElement extends Object {

    /**
     * Create a new GraphicElement with the given type.
     * By default, the GraphicElement contains no graphic
     * representations.
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public GraphicElement (String type) {
        _attributes = (LLMap) new LLMap();
        _type = type;
    }

    /**
     * Return an enumeration of all the attributeNames of this graphic element
     */
    public Enumeration attributeNames() {
        return _attributes.keys();
    }
        
    /** 
     * Return the type of this graphic element.  
     * The type is immutably set when the element is created.
     */
    public String getType() {
        return _type;
    }

    /**
     * Return the value of the attribute with the given name.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public String getAttribute (String name) {
        return (String) _attributes.at(name);
    }

    /**
     * Test if this schematic has the attribute wuth the given name.
     */
    public boolean hasAttribute (String name) {
        return _attributes.includesKey(name);
    }

    /**
     * Remove an attribute from this element
     */
    public void removeAttribute(String name) {
        _attributes.removeAt(name);
    }

   /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        _attributes.putAt(name, value);
    }

    private LLMap _attributes;
    private String _type;
}

    
