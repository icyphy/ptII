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

import ptolemy.kernel.util.*;
import java.util.Enumeration;
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
public class IconLibrary extends NamedObj{

    /** 
     * Create an IconLibrary object with the name "iconlibrary".
     * The library will have an empty string for the description and version.
     */
    public IconLibrary() {
        super("iconlibrary");
        _sublibraries = (LLMap) new LLMap();
        _icons = (LLMap) new LLMap();
        _description = new String("");
        _version = new String("");
    }

    /** 
     * Create an IconLibrary object with the given name.
     * The library will have an empty string for the description and version.
     */
    public IconLibrary(String name) {
        super(name);
        _sublibraries = (LLMap) new LLMap();
        _icons = (LLMap) new LLMap();
        _description = new String("");
        _version = new String("");
   }

    /**
     * Add an Icon to this library
     */
    public void addIcon(Icon i) {
        _icons.putAt(i.getName(),i);
    }

    /**
     * Add a sublibrary to this library.
     */
    public void addSubLibrary(IconLibrary library) {
        _sublibraries.putAt(library.getName(), library);
    }

    /**
     * Test if the library contains an Icon with the given name
     */
    public boolean containsIcon(String name) {
        return _icons.includesKey(name);
    }

    /**
     * Test if the library contains the sublibrary
     */
    public boolean containsSubLibrary(String name) {
        return _sublibraries.includesKey(name);
    }

    /**
     * Return a long description string of the the Icons in thie Library.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Get the Icon that is stored in this IconLibrary with the specified
     * type signature
     */
    // public Icon getIcon(EntityType e) {
    //    return (Icon) _icons.at(e);
    //}
    public Icon getIcon(String s) {
        return (Icon) _icons.at(s);
    }

    /** 
     * return the URL of the given sublibrary.
     */
    public IconLibrary getSubLibrary(String name) {
        return (IconLibrary) _sublibraries.at(name);
    }

    /** Return the version of this library.
     */
    public String getVersion() {
        return _version;
    }

    /**
     * Return the icons that are contained in this icon library.
     *
     * @return an enumeration of Icon
     */
    public Enumeration iconNames() {
        if(_icons == null) return null;
        return _icons.keys();
    }

   /**
     * Remove an icon from this IconLibrary
     */
    public void removeIcon(String name) {
        _sublibraries.removeAt(name);
    }

    /**
     * Remove a sublibrary from this IconLibrary
     */
    public void removeSubLibrary(String name) {
        _sublibraries.removeAt(name);
    }

    /**
     * Set the string that contains the long description of this library.
     */
    public void setDescription(String s) {
        _description = s;;
    }

    /** Set the string that represents the version of this library.
     */
    public void setVersion(String s) {
        _version = s;
    }

    /**
     * Return the names of subLibraries of this IconLibrary.   These names
     * are URL Strings which can be passed to other IconLibrary objects
     * for parsing.
     * @return an Enumeration of Strings
     */
    public Enumeration subLibraryNames() {
        return _sublibraries.keys();
    }

    private String _description;
    private String _version;
    private LLMap _sublibraries;
    private LLMap _icons;
}

