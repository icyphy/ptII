/* An IconLibrary stores icons in XML

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
public class IconLibrary {

    /** Create an IconLibrary object.  The Library should then be associated
     * with a URL and parsed before expecting valid results from the other 
     * methods
     */
    IconLibrary() {
        sublibraries = (HashedSet) new HashedSet();
        icons = (HashedMap) new HashedMap();
    }

    /** Set the URL that is associated with this IconLibrary.   The URL is
     *  assumed to be the location of an XML file that this object can 
     *  parse to get information about a set of associated icons.
     *  According to the URL spec this string contains forward slashes.
     */
    public void setURL(String newurl) {
        url = newurl;
    }
    
    /** Get the URL associated with thi IconLibrary.  If the URL has not
     *  been set, return the Empty String.
     */
    public String getURL() {
        return url;
    }

    /** Return the icons that have been parsed from a file.   If parse() 
     *  has not been called, return null.  If parse is called more than 
     *  once, then the Enumeration will only contain those icons defined in 
     *  the most recent URL or stream that was parsed. 
     *  @return an enumeration of Icon
     */
    public Enumeration icons() {
        if(icons==null) return null;
        return icons.elements();
    }

    /** Return the names of subLibraries of this IconLibrary.   These names 
     *  are URL Strings which can be passed to other IconLibrary objects 
     *  for parsing.
     *  @return an Enumeration of Strings
     */
    public Enumeration subLibraries() {
        return sublibraries.elements();
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
        return description;
    }

    /** Set the string that contains the long description of this library.
     */
    public void setDescription(String s) {
        description = s;
    }
    
    /** Return the version of this library.
     */
    public String getVersion() {
        return version;
    }
    
    /** Set the string that represents the version of this library.
     */
    public void setVersion(String s) {
        version = s;   
    }
    

    /** Return the name of this library.
     */
    public String getName() {
        return name;
    }

    /** Set the short name of this library
     */
    public void setName(String s) {
        name = s;
    }

    /** Parse the URL associated with this library.  The URL should specify 
     *  an XML file that is valid with IconLibrary.dtd.
     *
     *  @throws IllegalActionException if the URL is not valid or the file
     *  could not be retrieved.  
     *  @throws IllegalActionException if the parser fails.
     */
    public void parse() 
            throws IllegalActionException {
    }

    /** Parse the given stream for an IconLibrary element.  The stream should
     *  be valid with IconLibrary.dtd.
     *  @throws IllegalActionException if the parser fails.
     */
    public void parse(InputStream is) 
        throws IllegalActionException {
    }
  
    /** Dump the current contents of the IconLibrary to the URL associated 
     *  with the library.   The contents will be readable with the parse()
     *  method
     */
    public void print() throws IllegalActionException {
    }
    
    public void print(OutputStream os) {
    }

    private String name;
    private String url;
    private String description;
    private String version;
    private HashedSet sublibraries;
    private HashedMap icons;

}

