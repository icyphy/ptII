/* An IconLibrary stores icons in PTML files.

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

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import collections.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// IconLibrary
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class IconLibrary extends XMLElement{

    /** Create an IconLibrary object with no attributes.  
     * The Library should then be associated
     * with a URL and parsed before expecting valid results from the other 
     * methods
     */
    public IconLibrary() {
        super("iconlibrary");
        sublibraries = (HashedSet) new HashedSet();
        icons = (HashedMap) new HashedMap();
        description = new XMLElement("description");
    }

    /** Create an IconLibrary object with the attributes given in the 
     *  HashedMap.
     * The Library should then be associated
     * with a URL and parsed before expecting valid results from the other 
     * methods
     */
    public IconLibrary(HashedMap attributes) {
        super("iconlibrary", attributes);        
        sublibraries = (HashedSet) new HashedSet();
        icons = (HashedMap) new HashedMap();
    }
        
    /** Get the Icon that is stored in this IconLibrary with the specified 
     *  type signature
     */
    public Icon getIcon(EntityType e) {
        return (Icon) icons.at(e);
    }

    /** Return a long description string of the the Icons in thie Library.
     */
    public String getDescription() {
        return description.getPCData();
    }

    /** Return the name of this library.
     */
    public String getName() {
        return getAttribute("name");
    }

    /** Return the version of this library.
     */
    public String getVersion() {
        return getAttribute("version");
    }

    /** 
     * Return the icons that are contained in this icon library.
     * 
     * @return an enumeration of Icon
     */
    public Enumeration icons() {
        if(icons==null) return null;
        return icons.elements();
    }
    
    /** 
     * Set the string that contains the long description of this library.
     */
    public void setDescription(String s) {
        description.setPCData(s);
    }
    
    /** 
     * Set the short name of this library
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    /** Set the string that represents the version of this library.
     */
    public void setVersion(String s) {
        setAttribute("version",s);
    }

    /** 
     * Return the names of subLibraries of this IconLibrary.   These names 
     * are URL Strings which can be passed to other IconLibrary objects 
     * for parsing.
     * @return an Enumeration of Strings
     */
    public Enumeration subLibraries() {
        return sublibraries.elements();
    }

    // These are package-private because the parser will set them
    XMLElement description;
    HashedSet sublibraries;
    HashedMap icons;
}

