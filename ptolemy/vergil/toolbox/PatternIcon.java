/* An Icon that has another Icon as a pattern

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.Configurable;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.net.URL;
import java.io.*;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// PatternIcon
/**
An icon represnts the visual representation of a schematic entity.
This icon represents an icon with a similar look to another icon (its
'pattern').  This is useful for using an icon in more than one place, while
keeping a reference to it's original, in case the original is changed.
This class is minimally useful by itself, since it doesn't understand MoML.
Subclasses should implement different ways of setting the
pattern (such as referring to a library of icons).

@author Steve Neuendorffer
@version $Id$
*/
public class PatternIcon extends EditorIcon {

    /**
     * Create a new icon with the given name in the given container.
     * @param container The container.
     * @param name The name of the attribute.
     * @exception IllegalActionException If the attribute is not of an
     *  acceptable class for the container.
     * @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public PatternIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Create a new background figure based on this icon.  
     * In this class, defer to the pattern, if a 
     * pattern has been set.  If the pattern has not been set, then 
     * return a new instance of the default background figure specified
     * in the base class.
     */
    public Figure createBackgroundFigure() {
	if(_pattern == null)
	    return _createDefaultBackgroundFigure();
	else
	    return _pattern.createBackgroundFigure();
    }


    /** 
     * Create a new Swing icon that visually represents this icon.
     * In this class, defer to the pattern if one is present.
     */
    public javax.swing.Icon createIcon() {
        if(_pattern != null) {
            return _pattern.createIcon();
        } else {
            return super.createIcon();
        }
    }

    /**
     * Return the current pattern icon, or null if no pattern has been set.
     */
    public Icon getPattern() {
	return _pattern;
    }

    /**
     * Set the pattern to the given icon.
     */
    public void setPattern(EditorIcon icon) {
	_pattern = icon;
    }

    /**
     * Return a string representing this Icon.
     */
    public String toString() {
        String str = super.toString() + "(";
	str += _pattern;
        return str + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Return a description of the object.  Lines are indented according
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        String result = "";
        if(bracket == 0)
            result += super._description(detail, indent, 0);
        else
            result += super._description(detail, indent, 1);
	result += " pattern {\n";
	if(_pattern == null)
	    result += _getIndentPrefix(indent + 1) + "null\n";
	else
	    result += _getIndentPrefix(indent + 1) + _pattern.toString() + "\n";
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The pattern
    private EditorIcon _pattern;
}
