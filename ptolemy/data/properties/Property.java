/**
 * The base class for a property.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 * 
 */
package ptolemy.data.properties;

//////////////////////////////////////////////////////////////////////////
//// Property

/**
 * The base class for a property.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
// FIXME: may want to make this into an interface
public class Property {

    /**
     * Construct a new Property.
     */
    public Property() {
    }

    /**
     * Construct a new Property with the specified name.
     * @param name The specified name.
     */
    public Property(String name) {
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if this is an acceptable solution.
     * @return true if this is an acceptable solution; otherwise, false;
     */
    public boolean isAcceptableSolution() {
        return true;
    }

    /**
     * Return the color previously set by {@link #setColor(String)}.
     * @return The color.
     */
    public String getColor() {
        return _color;
    }

    /**
     * Set the color value associated with this property. The specified color
     * string is assumed to be an array of 4 doubles (e.g. {1.0, 1.0, 1.0,
     * 1.0}).
     * @param color The specified color value.
     */
    public void setColor(String color) {
        _color = color;
    }

    /**
     * Return the string representation of the Property. If the Property is
     * constructed with a specified non-empty name, that name is returned.
     * Otherwise, return the default string representation of the object.
     * @return The string.
     */
    public String toString() {
        if (_name.length() > 0) {
            return _name;
        }
        return super.toString();
    }

    /**
     * Determine if this Property corresponds to an instantiable token class.
     * @return True if this property corresponds to an instantiable token class.
     */
    public boolean isInstantiable() {
        throw new AssertionError("Not supported in Base class.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /**
     * The name of this Property.
     */
    protected String _name = "";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The color of this Property.
     */
    private String _color = "";

}
