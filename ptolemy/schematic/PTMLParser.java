/* A PTMLParser can read PTML files

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
import com.microstar.xml.*;

//////////////////////////////////////////////////////////////////////////
//// PTMLParser
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLParser extends HandlerBase{

    PTMLParser(String url) {
        super();
        url=newurl;
    } 

    /** Implement com.microstar.xml.XMLHandler.attribute
     */
    public void attribute(String name, String value, boolean specified) 
    throws Exception {
        attributes.putAt(name, value);
    }
        
    /** Implement com.microstar.xml.XMLHandler.endDocument
     */
    public void endDocument() throws Exception {
        if(current!=document) 
            throw new IllegalActionException("internal error in PTMLParser");
        parsing=false;
    }

    /** Implement com.microstar.xml.XMLHandler.endElement
     */
    public void endElement() throws Exception {
        XMLElement parent= current.getParent();
        if(parent instanceof iconlibrary) {
            if(current instanceof icon) {
                ((IconLibrary) parent).icons.putAt(current);
            } else if(current instanceof sublibrary) {
                String file = current.getAttribute("file");
                ((IconLibrary) parent).sublibraries.putAt(file);
            }
        }
        current = parent;
    }

    /** Implement com.microstar.xml.XMLHandler.error
     */
    public void error(String message, String sysid,
            int line, int column) throws Exception {
                throw new XMLException(message, sysid, line, column);
    }

   /** Get the URL associated with this Parser.  If the URL has not
     *  been set, return the Empty String.
     */
    public String getURL() {
        return url;
    }

    /** Parse the URL associated with this library.  The URL should specify 
     *  an XML file that is valid with IconLibrary.dtd.
     *
     *  @throws IllegalActionException if the URL is not valid or the file
     *  could not be retrieved.  
     *  @throws IllegalActionException if the parser fails.
     */
    public void synchronized parse() throws IllegalActionException {
        if(parsing == false) {
            parsing = true;
            XmlParser parser = new XmlParser();
            parser.setHandler(this);
            parser.parse(makeAbsoluteURL(url), null, null);       
        } 
    }

    /** Parse the given stream for an IconLibrary element.  The stream should
     *  be valid with IconLibrary.dtd.
     *  @throws IllegalActionException if the parser fails.
     */
    public void parse(InputStream is) throws IllegalActionException {
        if(parsing == false) {
            parsing = true;
            XmlParser parser = new XmlParser();
            parser.setHandler(this);
            parser.parse(makeAbsoluteURL(url), null, is, null);
        }
    }
 
    /** Implement com.microstar.xml.XMLHandler.startDocument
     */
    public void startDocument() {
        attributes = (HashedMap) new HashedMap();
        current = new XMLElement();
        document = current;
        first = true;
    }

     /** Implement com.microstar.xml.XMLHandler.startElement
     */
    public void startElement(String name) {
        XMLElement e;
 
        if(name.equals("icon")) {
            e=new Icon(attributes);
        }
        if(name.equals("sublibrary")) {
            e=new XMLElement(name,attributes);
        }
        if(name.equals("iconlibrary")) {
            e=new IconLibrary(attributes);
        }
        if(name.equals("parameter")) {
            e=new SchematicParameter(attributes);
        }
        if(name.equals("description")) {
            e=new XMLElement(name,attributes);
        }
        if(name.equals("port")) {
            e=new SchematicPort(attributes);
        }
        if(name.equals("graphic")) {
            e=new SchematicPort(attributes);
        }
        else {
            e=new XMLElement(name,attributes);
        }
        if(first) {
            first = false;
            root=e;
            current=e;
            e.setParent(null);
        } else {
            e.setParent(current);
            current.addChild(e);
            current=e;
        }
        attributes = (HashedMap) new HashedMap();
    }

    private boolean parsing=false;
    private String url;
    private HashedMap attributes;
    private XMLElement current;
    private XMLElement root;

}
