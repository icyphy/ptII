/* A SchematicElement is an XML element that can appear on Ptoley schematics

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
//// SchematicElement
/**

A SchematicElement is the abstract superclass of classes that can
appear in a Ptolemy II schematic.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public abstract class SchematicElement extends XMLElement {

    /**
     * Add a new parameter to this element. The name
     * of the parameter must be unique in this element.
     */
    public void addParameter (String name, String value, Class type) {
        ;
    }

   /**
     * Test if there is an parameter with the given name in this
     * element.
     */
    public boolean containsParameter (String name) {
        return false;
    }

   /**
     * Return the value of schematic parameter with the given name.
     * Throw an exception if there is no parameter with the
     * given name in this element.
     */
    public String getParameter (String name) {
        return null;
    }

   /**
     * Return the type of the schematic parameter with the given name.
     * Throw an exception if there is no parameter with the
     * given name in this element.
     */
    public Class getParameterType (String name) {
        return null;
    }

   /**
     * Return an enumeration over the (top-level) parameters in this
     * element.
     */
    public Enumeration parameters () {
        return null;
    }

}

