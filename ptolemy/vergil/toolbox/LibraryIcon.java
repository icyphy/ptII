/* An Icon whose pattern is in an icon library.

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
//// LibraryIcon
/**

An icon is the graphical representation of a schematic entity.
Some generic icons are stored in icon libraries to serve as generic templates.
This icon references such an icon as its pattern.
If the icon is never configured or the name of the icon in the library
that it represents is not set, then it will use a default figure for its
pattern.

@author Steve Neuendorffer
@version $Id$
*/
public class LibraryIcon extends PatternIcon implements Configurable {

    /**
     * Create a new icon with the given name in the given container.
     */
    public LibraryIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	_iconName = null;
    }

    /** Configure the icon by giving a name of an icon in the icon
     *  library.  The name is given in the <i>text</i> argument.
     *  Although the configure tag allows configuration information
     *  to be given in a URL, this hardly makes sense in this case,
     *  so we disallow it.
     *  If the string is not the name of an icon in the icon library,
     *  or if the icon library has not been set, then do not change
     *  the pattern.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception IllegalActionException If the source argument is given.
     */
    public void configure(URL base, String source, String text)
           throws IllegalActionException {
        if (source != null && !source.equals("")) {
            throw new IllegalActionException(this,
                   "LibraryIcon cannot be configured via a URL.");
        }
        if (text != null && !text.equals("")) {
            setIconName(text);
        }
    }

    /** 
     * Return the name of the pattern in the icon library.
     */
    public String getIconName() {
	return _iconName;
    }

    /** 
     * Set the pattern of this icon to the icon with the given name. 
     * If the string is the name of an icon in the icon library, then set the
     * icon name to the given string, and set the pattern of this icon to that
     * icon.
     * If the library is null or if the icon was not found 
     * in the library, then do nothing.  
     */
    public void setIconName(String name) {   
        CompositeEntity library = LibraryIcon.getIconLibrary();
        if(library == null) return;
	EditorIcon icon = (EditorIcon)library.getAttribute(name);
	// if it is found
	if(icon != null) {
	    setPattern(icon);
	    _iconName = name;
	}
    }        
 
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

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
        if (bracket == 2) result += "}";

        return result;
    }

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see NamedObj#_exportMoMLContents
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
	super._exportMoMLContents(output, depth);
	if(_iconName != null) {
	    output.write(_getIndentPrefix(depth));
	    output.write("<configure>" + _iconName + "</configure>\n");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private String _iconName;

    ///////////////////////////////////////////////////////////////////
    ////                         static methods                    ////

    /** 
     * Return the root icon library from which to search for icons.
     */
    public static CompositeEntity getIconLibrary() {
	return _iconLibrary;
    }

    /** 
     * Set the root icon library from which to search for icons.
     */
    public static void setIconLibrary(CompositeEntity library) {
	_iconLibrary = library;
    }

    private static CompositeEntity _iconLibrary;
}
