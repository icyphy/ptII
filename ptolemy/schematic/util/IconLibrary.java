/* An IconLibrary stores icons in PTML files.

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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import collections.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// IconLibrary
/**
An IconLibrary is the hierarchical object for organizing Icons.   An
IconLibrary contains a set of Icons, and a set of other IconLibraries
called suiblibraries.  

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class IconLibrary extends Entity {

    /** 
     * Create an IconLibrary object with the name "iconlibrary".
     * The library will have an empty string for the description and version.
     */
    public IconLibrary() {
        super();
        _sublibraries = (NamedList) new NamedList();
        _icons = (NamedList) new NamedList();
        _terminalstyles = (NamedList) new NamedList();
        try {
            setName("IconLibrary");
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** 
     * Create an IconLibrary object with the given name.
     * The library will have an empty string for the description and version.
     * @throws IllegalActionException If the name contains a period.
     */
    public IconLibrary(String name) throws IllegalActionException {
        super(name);
        _sublibraries = (NamedList) new NamedList();
        _icons = (NamedList) new NamedList();
        _terminalstyles = (NamedList) new NamedList();
   }

    /**
     * Add an Icon to this library
     *  @exception IllegalActionException If the icon has no name.
     *  @exception NameDuplicationException If the name of the icon
     *  coincides with the name of another icon contained in this library.
     */
    public void addIcon(Icon i)
        throws IllegalActionException, NameDuplicationException {
        _icons.append(i);
    }

    /**
     * Add a sublibrary to this library.
     *  @exception IllegalActionException If the sublibrary has no name.
     *  @exception NameDuplicationException If the name of the sublibrary
     *  coincides with the name of another sublibrary
     *  contained in this library.
     */
    public void addSubLibrary(IconLibrary library)
        throws IllegalActionException, NameDuplicationException {
        _sublibraries.append(library);
    }

    /**
     * Add a terminal style to this library.
     *  @exception IllegalActionException If the terminal style has no name.
     *  @exception NameDuplicationException If the name of the terminal style
     *  coincides with the name of another terminal style
     *  contained in this library.
      */
    // public void addTerminalStyle(TerminalStyle style)
    //    throws IllegalActionException, NameDuplicationException {
    //    _terminalstyles.append(style);
    // }

    /**
     * Test if the library contains the given icon.
     */
    public boolean containsIcon(Icon icon) {
        return _icons.includes(icon);
    }

    /**
     * Test if the library contains the sublibrary
     */
    public boolean containsSubLibrary(IconLibrary lib) {
        return _sublibraries.includes(lib);
    }

    /**
     * Test if the library contains the terminal style
     */
    //    public boolean containsTerminalStyle(TerminalStyle style) {
    //    return _terminalstyles.includes(style);
    // }

    /** 
     * Search for an icon with the given hierarchical name in 
     * this library and all its deeply contained sublibraries.
     * @return The found icon, or null if the icon was not found.
     */
    public Icon findIcon(String dottedName) {
        StringTokenizer tokens = new StringTokenizer(dottedName, ".");
        IconLibrary temp = this;
        int count = tokens.countTokens();
        
        int i;
        for(i = 0; i < (count - 1); i++) {
	    String name = (String) tokens.nextElement();
	    temp = temp.getSubLibrary(name);
	    if(temp == null) {
		return null;
	    }
	}

	String name = (String) tokens.nextElement();
	return temp.getIcon(name);
    }

    /** 
     * Search for an terminal style with the given hierarchical name in 
     * this library and all its deeply contained sublibraries.
     * @return The found terminal style, or null if the terminal style was
     * not found.
     */
    /*public TerminalStyle findTerminalStyle(String dottedName)
            throws IllegalActionException {
        StringTokenizer tokens = new StringTokenizer(dottedName, ".");
        IconLibrary temp = this;
        int count = tokens.countTokens();
        
        int i;
        for(i = 0; i < (count - 1); i++) {
	    String name = (String) tokens.nextElement();
	    temp = temp.getSubLibrary(name);
	    if(temp == null) {
		return null;
	    }
	}

	String name = (String) tokens.nextElement();
	return temp.getTerminalStyle(name);
	}*/

    /**
     * Get the icon that is stored in this icon library with the given name
     */
    public Icon getIcon(String name) {
	return (Icon) _icons.get(name);
    }

    /** 
     * Return the URL of the given sublibrary.
     */
    public IconLibrary getSubLibrary(String name) {
	return (IconLibrary) _sublibraries.get(name);
    }

    /**
     * Get the terminal style in this IconLibrary with the given name.
     */
    //    public TerminalStyle getTerminalStyle(String name) {
    //	return (TerminalStyle) _terminalstyles.get(name);
    //}

    /** Return the version of this library.
     */
    //public String getVersion() {
    //    return _version;
    //}

    /**
     * Return the icons that are contained in this icon library.
     *
     * @return an enumeration of Icon
     */
    public Enumeration icons() {
        return _icons.elements();
    }

   /**
     * Remove an icon from this IconLibrary
     */
    public void removeIcon(Icon icon) {
        _sublibraries.remove(icon);
    }

    /**
     * Remove a sublibrary from this IconLibrary
     */
    public void removeSubLibrary(IconLibrary lib) {
        _sublibraries.remove(lib);
    }

    /**
     * Remove a terminal style from this IconLibrary
     */
    //    public void removeTerminalStyle(TerminalStyle style) {
     //   _terminalstyles.remove(style);
    //}

    /** Set the string that represents the version of this library.
     */
    //public void setVersion(String s) {
    //    _version = s;
    //}

    /**
     * Return the names of subLibraries of this IconLibrary.   These names
     * are URL Strings which can be passed to other IconLibrary objects
     * for parsing.
     * @return an Enumeration of Strings
     */
    public Enumeration subLibraries() {
        return _sublibraries.elements();
    }

    /**
     * Return the names of subLibraries of this IconLibrary.   These names
     * are URL Strings which can be passed to other IconLibrary objects
     * for parsing.
     * @return an Enumeration of Strings
     */
    //    public Enumeration terminalStyles() {
    //   return _terminalstyles.elements();
    //}

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        Enumeration els = subLibraries();
        String str = getName() + "({";
        while(els.hasMoreElements()) {
            IconLibrary il = (IconLibrary) els.nextElement();
	    str += il.toString();
        }
        str += "}{";
        els = icons();
         while(els.hasMoreElements()) {
            Icon icon = (Icon) els.nextElement();
	    str += "\n" + icon.toString();
        }        
        return str + "})";
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
        /*
	result += _getIndentPrefix(indent) + " sublibraries {\n";
        result += _enumerationDescription(subLibraries(), indent);

	result += _getIndentPrefix(indent) + "} icons {\n";
        result += _enumerationDescription(icons(), indent);

	result += _getIndentPrefix(indent) + "} terminalstyles{\n";
        result += _enumerationDescription(terminalStyles(), indent);
	*/
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    private NamedList _icons;
    private NamedList _sublibraries;
    private NamedList _terminalstyles;
}
