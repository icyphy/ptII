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
import java.io.InputStream;
import java.io.FileInputStream;
import com.microstar.xml.*;



//////////////////////////////////////////////////////////////////////////
//// PTMLParser
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLParser extends HandlerBase{

    /** 
     * Create a PTMLParser that will operate relative to the url in the given 
     * string.
     */
    public PTMLParser(String s) {
        super();
        url=s;
    } 

    /** 
     * Implement com.microstar.xml.XMLHandler.attribute
     * Accumulate all the attributes until the next startElement.
     */
    public void attribute(String name, String value, boolean specified) 
    throws Exception {
        attributes.putAt(name, value);
    }
        
    /** 
     * Implement com.microstar.xml.XMLHandler.endDocument
     * If we've finished the parse and didn't get back to the root of the
     * parse tree, then something is wrong, and throw an exception.
     */
    public void endDocument() throws Exception {
        if(current!=root) 
            throw new IllegalActionException("internal error in PTMLParser");
    }

    /** 
     * Implement com.microstar.xml.XMLHandler.endElement
     * Move up one level in the parse tree and apply any semantic meaning 
     * that the element that is ending might have within its parent.  For
     * example, if the element is an Icon contained within an IconLibrary, 
     * then the icon should be added to the library's list of icons.
     */
    public void endElement() throws Exception {
        XMLElement parent= current.getParent();
        if(parent instanceof IconLibrary) {
            if(current instanceof Icon) {
                ((IconLibrary) parent).icons.putAt(
                        ((Icon) current).getEntityType(),
                        current);
            } else if(current.getElementType().equals("sublibrary")) {
                String file = current.getAttribute("file");
                ((IconLibrary) parent).sublibraries.putAt(file,current);
            }
        }
        current = parent;
    }

    /** 
     * Implement com.microstar.xml.XMLHandler.error
     * @throws XmlException if called.
     */
    public void error(String message, String sysid,
            int line, int column) throws Exception {
                throw new XmlException(message, sysid, line, column);
    }

    /** 
     * Get the URL associated with this Parser.  
     */
    public String getURL() {
        return url;
    }

    /** 
     * Parse the URL associated with this library.  The URL should specify 
     * an XML file that is valid with IconLibrary.dtd.
     *
     * @return the XMLElement that contains the root of the parse tree.  
     * this element will have element type of "document". 
     * @throws IllegalActionException if the URL is not valid or the file
     * could not be retrieved.  
     * @throws IllegalActionException if the parser fails.
     */
    public XMLElement parse() throws Exception {
        XmlParser parser = new XmlParser();
        parser.setHandler(this);
        parser.parse(url, null, (String)null); 
        return root;      
    }

    /** 
     * Parse the given stream, using the url associated with this library
     * to expand any external references within the XML.  The stream should
     * be valid with IconLibrary.dtd.  
     *
     * @return the XMLElement that contains the root of the parse tree.  
     * this element will have element type of "document". 
     * @throws IllegalActionException if the parser fails.
     */
    public XMLElement parse(InputStream is) 
    throws Exception {
        XmlParser parser = new XmlParser();
        parser.setHandler(this);
        parser.parse(url, null, is, null);
        return root;
    }
 
    /** 
     * Implement com.microstar.xml.XMLHandler.startDocument
     * Initialize the parse tree to contain a single element of type 
     * "document".
     */
    public void startDocument() {
        attributes = (HashedMap) new HashedMap();
        current = new XMLElement("Document");
        root = current;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.startElement
     * Create a new XMLElement, or derived class of XMLElement, based on 
     * the element type.   Set the attributes of the new XMLElement equal 
     * to the attributes that have been accumulated since the last 
     * call to this method.  Descend the parse tree into the new XMLElement
     *
     * @param name the element type of the element that is beginning.
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
            //            e=new SchematicParameter(attributes);
        }
        if(name.equals("description")) {
            e=new XMLElement(name,attributes);
        }
        if(name.equals("port")) {
            //            e=new SchematicPort(attributes);
        }
        if(name.equals("graphic")) {
            e=new XMLElement(name,attributes);
        }
        else {
            e=new XMLElement(name,attributes);
        }
        e.setParent(current);
        current.addChildElement(e);
        current=e;
        attributes = (HashedMap) new HashedMap();
    }

    private String url;
    private HashedMap attributes;
    private XMLElement current;
    private XMLElement root;

}

