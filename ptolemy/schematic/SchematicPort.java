/* A Schematic represents a PtII design

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

package ptolemy.schematic;

import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// SchematicPort
/**

A schematic port represents a port of an entity in a PtolemyII
schematic. Currently, it is not clear exactly how much
information is in this object...
<!-- port elements will be parsed into class SchematicPort -->
<!ELEMENT port EMPTY>
<!ATTLIST port
name ID #REQUIRED
input (true|false) "false"
output (true|false) "false"
multiport (true|false) "false"
type (string|double|doubleArray) #REQUIRED>

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicPort extends SchematicElement {

    /**
     * Create a new SchematicPort object, with no attributes.
     */
    public SchematicPort () {
        super("port");
        setInput(false);
        setOutput(false);
        setMultiport(false);
        setType("undeclared");
    }

    /**
     * Create a new SchematicPort object, with the specified attributes.
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicPort (HashedMap attributes) {
        super("port", attributes);
        if(!hasAttribute("input")) setInput(false);
        if(!hasAttribute("output")) setOutput(false);
        if(!hasAttribute("multiport")) setMultiport(false);
        if(!hasAttribute("type")) setType("undeclared");
    }

    /**
     * Return a string that represents the type of this port.  If the type
     * is not specified and should be determined at runtime, then
     * the type returned will be "undeclared".
     */
    public String getType() {
        return getAttribute("type");
    }

    /**
     * Return true if and only if the port is an input port.
     */
    public boolean isInput() {
        return getAttribute("input").equals("true");
    }

    /**
     * Return true if and only if the port is a multiport.
     */
    public boolean isMultiport() {
        return getAttribute("multiport").equals("true");
    }

    /**
     * Return true if and only if the port is an output port.
     */
    public boolean isOutput() {
        return getAttribute("output").equals("true");
    }

    /**
     * Set whether or not this port is an input port.
     */
    public void setInput(boolean flag) {
        if(flag) setAttribute("input", "true");
        else setAttribute("input", "false");
    }

    /**
     * Set whether or not this port is a multiport.
     */
    public void setMultiport(boolean flag) {
        if(flag) setAttribute("multiport", "true");
        else setAttribute("multiport", "false");
    }

    /**
     * Set whether or not this port is an output port.
     */
    public void setOutput(boolean flag) {
        if(flag) setAttribute("output", "true");
        else setAttribute("output", "false");
    }

    /**
     * Set the type of this port.   If the type is to be dynamically
     * determined, then set the type to "undeclared".
     *
     * @param a String representing the type of this port.
     */
    public void setType(String type) {
        setAttribute("type", type);
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(e instanceof SchematicParameter) {
            // if a parameter, remove the old one and install the new one.
            parameters.putAt(
                    ((SchematicParameter) e).getName(), e);
        }
    }
}

