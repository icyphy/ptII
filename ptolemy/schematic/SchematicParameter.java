/* A SchematicParameter encapsulates a parameter of a SchematicElement

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
//// SchematicParser
/**

@author Steve Neuendorffer, John Reekie
@version $W$ $G$
*/
public class SchematicParameter extends XMLElement{

    /**
     * Create a SchematicParameter with empty name, value and type.
     */
    SchematicParameter() {
        super("parameter");
    }

    /** 
     * Create a SchematicParamter object with the given name.
     * The value and type are empty strings.
     */
    SchematicParameter(String name) {
        super("parameter");
        setAttribute("name", name);
        setAttribute("value", "");
        setAttribute("type", "");
    }

    /**
     * Create a SchematicParameter object with the given name and type.
     * The value is set to an empty string
     */
    SchematicParameter(String name, String type) {
        super("parameter");
        setAttribute("name", name);
        setAttribute("type", type);
        setAttribute("value", "");
    }

    /**
     * Create a SchematicParameter object with the given name, type and value.
     */
    SchematicParameter(String name,  
            String type, String value) {
        super("parameter");
        setAttribute("name", name);
        setAttribute("type", type);
        setAttribute("value", value);
    }

    /** 
     * Return the name of this parameter.
     */
    public String getName() {
        return getAttribute("name");
    }

    /**
     * Return the type of this parameter
     */
    public String getType() {
        return getAttribute("type");
    }
       
    /**
     * Return the Value of this parameter
     */     
    public String getValue() {
        return getAttribute("value");
    }
      
    /** 
     * Set the name of this parameter
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    /**
     * Set the type of this parameter
     */      
    public void setType(String type) {
        setAttribute("type", type);
    }

    /**
     * set the value of this parameter
     */
    public void setValue(String value) {
        setAttribute("value", value);
    }
}






