/* An Icon has another Icon as a pattern

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
This icon represents an icon with a similar look to another icon (its
'pattern').  This is useful for using an icon in more than one place, while
keeping a reference to it's original, in case the original is changed.

@author Steve Neuendorffer
@version $Id$
*/
public class PatternIcon extends EditorIcon {

    /**
     * Create a new icon with the name "_icon" in the given container.
     */
    public PatternIcon(NamedObj container)
            throws NameDuplicationException, IllegalActionException {
        this(container, "_icon");
    }

    /**
     * Create a new icon with the name "EditorIcon" in the given container.
     */
    public PatternIcon(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Create a background figure based on this icon.  The background figure
     * will be painted with each graphic element that this icon contains.
     * In this class, return the
     */
    public Figure createBackgroundFigure() {
	if(_pattern == null)
	    return _createDefaultBackgroundFigure();
	else
	    return _pattern.createBackgroundFigure();
    }

    public Icon getPattern() {
	return _pattern;
    }

    public void setPattern(EditorIcon icon) {
	_pattern = icon;
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        String str = super.toString() + "(";
	str += _pattern;
        return str + ")";
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

    private EditorIcon _pattern;
}

