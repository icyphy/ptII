/* An Terminal represents a point that can be connected to.

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
//// SchematicTerminal
/**

An SchematicTerminal is the graphical representation of a schematic entity.
SchematicTerminals are connected by SchematicLinks.  SchematicTerminals
are contained within schematics, schematic relations, and schematic entities.
Usually terminals may be freely moved about.  However, some terminals (notably,
those that are contained in entities with a template) may have a template.  
In these cases, the terminal is fixed to the position of the template.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicTerminal extends PTMLTemplateObject {

    /**
     * Create a new SchematicTerminal with the name "SchematicTerminal",
     * and no template. 
     */
    public SchematicTerminal () {
        this("SchematicTerminal", null);
    }

    /** 
     * Create a new SchematicTerminal with the given name, and no template.
     */
    public SchematicTerminal (String name) {
        super(name, null);
    }

    /**
     * Create a new SchematicTerminal with the given template, and the
     * name of the template. 
     */
    public SchematicTerminal (Terminal template) {
        this(template.getName(), template);
    }

    /** 
     * Create a new SchematicTerminal with the given name and template.
     */
    public SchematicTerminal (String name, Terminal template) {
        super(name, template);
        if(template != null) {
            _x = template.getX();
            _y = template.getY();
        } else {
            _x = 0;
            _y = 0;
        }
    }

    /**
     * Return the X position of this SchematicTerminal
     */
    public double getX() {
        return _x;
    }

    /**
     * Return the Y position of this SchematicTerminal
     */
    public double getY() {
        return _y;
    }

    /**
     * Return true only if this SchematicTerminal can be moved.
     */
    public boolean isMoveable() {
        return !hasTemplate();
    }

   /**
     * Set the template object of this object.   If the template is not
     * null, then set the position of the terminal to the position of the
     * template terminal.
     */
    public void setTemplate(PTMLObject obj) {
        super.setTemplate(obj);
        if(hasTemplate()) {
            _x = ((Terminal)getTemplate()).getX();
            _y = ((Terminal)getTemplate()).getY();
        }
    }

   /**
     * Set the X location of this Terminal.
     */
    public void setX(double x) {
        if(!isMoveable())
            throw new InternalErrorException("Terminal " + getFullName() +
                    "has a template, and so can't be moved.");
        _x = x;
    }

   /**
     * Set the Y location of this Terminal.
     */
    public void setY(double y) {
        if(!isMoveable())
            throw new InternalErrorException("Terminal " + getFullName() +
                    "has a template, and so can't be moved.");
        _y = y;
    }

    /** 
     * Return a string representation of the terminal
     */
    protected String _description(int indent) {
        String result = super._description(indent);
        result += _getIndentPrefix(indent) + "X=" + _x + "\n";
        result += _getIndentPrefix(indent) + "Y=" + _y + "\n";
        return result;
    }

    private double _x, _y;
}

