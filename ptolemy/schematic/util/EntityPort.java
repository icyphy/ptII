/* A EntityPort represents a PtII design

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

import java.util.Enumeration;

////////////////////////////////////////////////////////////////////////////// EntityPort
/**

An entity port represents a port of an entity in an entity template. 

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class EntityPort extends PTMLObject {

    /**
     * Create a new EntityPort object, with no attributes.
     */
    public EntityPort () {
        this("port");
     }

    /**
     * Create a new EntityPort object, with the specified attributes.
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public EntityPort (String name) {
        super(name);
	setInput(false);
        setOutput(false);
        setMultiport(false);
        setType("undeclared");
    }

    /**
     * Return a string that represents the type of this port.  If the type
     * is not specified and should be determined at runtime, then
     * the type returned will be "undeclared".
     */
    public String getType() {
        return _type;
    }

    /**
     * Return true if and only if the port is an input port.
     */
    public boolean isInput() {
        return _input;
    }

    /**
     * Return true if and only if the port is a multiport.
     */
    public boolean isMultiport() {
        return _multi;
    }

    /**
     * Return true if and only if the port is an output port.
     */
    public boolean isOutput() {
        return _output;
    }

    /**
     * Set whether or not this port is an input port.
     */
    public void setInput(boolean flag) {
	_input = flag;
    }

    /**
     * Set whether or not this port is a multiport.
     */
    public void setMultiport(boolean flag) {
	_multi = flag;
    }

    /**
     * Set whether or not this port is an output port.
     */
    public void setOutput(boolean flag) {
	_output = flag;
    }

    /**
     * Set the type of this port.   If the type is to be dynamically
     * determined, then set the type to "undeclared".
     *
     * @param a String representing the type of this port.
     */
    public void setType(String type) {
	_type = type;
    }

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(indent, 0);
        else 
            result += super._description(indent, 1);
	result += " type {\n";
        result += _getIndentPrefix(indent + 1) + _type + "\n";
        result += _getIndentPrefix(indent) + "}";
	result += " input {" + _input + "}";
        result += " output {" + _output + "}";
        result += " multi {" + _multi + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    private boolean _input;
    private boolean _multi;
    private boolean _output;
    private String _type;
}

