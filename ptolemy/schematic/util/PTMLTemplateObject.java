/* An PTMLTemplateObject is a nameable object in a schematic.

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
//// PTMLTemplateObject
/**

A PTMLTemplateObject is the base class for
any object that can be pulled out of a Schematic file.  

@author Steve Neuendorffer
@version $Id$
*/
public class PTMLTemplateObject extends PTMLObject {

    /**
     * Create a new PTMLTemplateObject with the name 
     * "PTMLTemplateObject" and no template.
     */
    public PTMLTemplateObject () {
        this("PTMLTemplateObject", null);
    }

    /**
     * Create a new PTMLTemplateObject with the given name and no
     * template.
     */
    public PTMLTemplateObject (String name) {
        this(name, null);
    }

    /**
     * Create a new PTMLTemplateObject with given template and the 
     * name of the given template.
     */
    public PTMLTemplateObject (PTMLObject template) {
        this(template.getName(), template);
    }

    /**
     * Create a new PTMLTemplateObject with the given name and 
     * the given template
     */
    public PTMLTemplateObject (String name, PTMLObject template) {
        super(name);
        _template = template;
    }

    /*
     * Get the template object for this object.
     */
    public PTMLObject getTemplate() {
        return _template;
    }

    /*
     * Get the template object for this object.
     */
    public boolean hasTemplate() {
        return (_template != null);
    }

   /**
     * Set the template object of this object.
     */
    public void setTemplate(PTMLObject obj) {
        _template = obj;
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
    protected String _description(int indent) {
        String result = super._description(indent);
        result += _getIndentPrefix(indent) + "template\n";
        if(_template == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _template._description(indent + 1);
        return result;
    }

    private PTMLObject _template;
}

