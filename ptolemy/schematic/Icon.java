/* An Icon is the graphical representation of a schematic entity.

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
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Icon 
/**

An icon is the graphical representation of a schematic entity.
Icons are created by an IconLibrary in response to a request
for an icon. Each icon is represented in an icon library
XML files by the <icon> element.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Icon extends XMLElement {

    /**
     * Create a new Icon. By default, the icon contains no graphic
     * representation.
     */
    public Icon () {
        ;
    }

    /**
     * Get an enumeration over the names of the graphics formats
     * supported by this icon. Each element of the enumeration
     * is a string.
     */
    public Enumeration graphicsFormats() {
        return null;
    }

    /** Add a new graphics element to the icon. The format
     * specifies the graphics format. Throw an exception if
     * a graphics element in this format already exists.
     */
    public void addGraphicsElement (String format, XMLElement element) {
        ;
    }

     /** Test if this icon contains a graphics element in the
     * given graphics format.
     */
    public boolean containsGraphicsElement (String format) {
        return false;
    }

   /** Given the graphics format attribute, return the graphics
     * element that has that format. If there isn't one, throw
     * an exception.
     */
    public XMLElement getGraphicsElement (String format) {
        return null;
    }

    /** Remove a graphics element from the icon. Throw an exception if
     * a graphics element in this format does not exists.
     */
    public void removeGraphicsElement (String format) {
        ;
    }

}

