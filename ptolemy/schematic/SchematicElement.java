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
import collections.HashedMap;

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
     * Create a SchematicElement object with the specified element type.
     * The object will have no attributes by default.
     *
     * @param type the element type of the SchematicElement
     */
    SchematicElement(String type) {
        super(type);
        parameters = (HashedMap) new HashedMap();
    }

    /**
     * Create a SchematicElement object with the specified element type and 
     * attributes
     *
     * @param type the element type of the new object
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    SchematicElement(String type, HashedMap attributes) {
        super(type, attributes);
        parameters = (HashedMap) new HashedMap();
    }

    /**
     * Add a new parameter to this element. The name
     * of the parameter must be unique in this element.
     */
    public void addParameter (SchematicParameter parameter) {
        parameter.setParent(this);
        addChildElement(parameter);
        parameters.putAt(parameter.getName(), parameter);
    }

    /**
     * Add a new parameter to this element. The name
     * of the parameter must be unique in this element.
     */
    public void addParameter (String name, String type, 
    String value) {
        SchematicParameter parameter = 
            new SchematicParameter(name, type, value);
        parameter.setParent(this);
        addChildElement(parameter);
        parameters.putAt(name, parameter);
    }

   /**
     * Test if there is an parameter with the given name in this
     * element.
     */
    public boolean containsParameter (String name) {
        return parameters.includesKey(name);
    }

   /** Return the name of this element.
     */
    public String getName() {
        return getAttribute("name");
    }

   /**
     * Return the value of schematic parameter with the given name.
     * Throw an exception if there is no parameter with the
     * given name in this element.
     */
    public String getParameterValue (String name) {
        return ((SchematicParameter) parameters.at(name)).getValue();
    }

   /**
     * Return the type of the schematic parameter with the given name.
     * Throw an exception if there is no parameter with the
     * given name in this element.
     */
    public String getParameterType (String name) {
        return ((SchematicParameter) parameters.at(name)).getType();
    }

   /**
     * Return an enumeration over the (top-level) parameters in this
     * element.
     * @return an enumeration of SchematicParameters
     */
    public Enumeration parameters () {
        return parameters.elements();
    }

    /** 
     * Set the short name of this element
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    private HashedMap parameters;

}

