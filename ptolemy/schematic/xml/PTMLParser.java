/* A PTMLParser can read PTML files.

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

package ptolemy.schematic.xml;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import collections.*;
import java.io.InputStream;
import java.io.FileInputStream;
import com.microstar.xml.*;
import ptolemy.schematic.*;


//////////////////////////////////////////////////////////////////////////
//// PTMLParser
/**
This class interfaces to the Microstar Aelfred XML parser in order to
parse XML files.  Calling one of the parse methods will read the XML
input and create a parse tree of XMLElements, returning the root of the
tree.   

This parser is capable of resolving external entities using either public IDs
or system IDs.  System IDs are usually given as a complete URL to the given 
file.  Public IDs are given as partial pathnames to which the parser prepends 
a locally known location for libraries of XML files, such as local Document
Type Descriptors (DTDs).  Where both IDs are given, this parser prefers to 
use public IDs for resolving external entities.

This parser assumes that public IDs are relative to the Ptolemy II classpath.
To expand public IDs, this parser determines the location of the classpath
from the $PTII environment variable and appends the public ID to that value.

This parser is relatively general, and implements little specific
functionality.  To implement some function using this class, traverse the 
returned parse tree. 

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLParser extends HandlerBase{

    /**
     * Create a PTMLParser with the default public ID path.
     */
    public PTMLParser() {
        super();
    }

    /** Handle an attribute assignment that is part of an XML element.
     *  This method is called prior to the corresponding startElement()
     *  call, so it simply accumulates attributes in a hashtable for
     *  use by startElement().
     *  @param name The name of the attribute.
     *  @param value The value of the attribute, or null if the attribute
     *   is <code>#IMPLIED</code> and not specified.
     *  @param specified True if the value is specified, false if the
     *   value comes from the default value in the DTD rather than from
     *   the XML file.
     *  @exception XmlException If the name or value is null.
     */
    public void attribute(String name, String value, boolean specified)
            throws Exception {
        if(DEBUG) {
            System.out.println(
                    "Attribute name = " + name + 
                    ", value = " + value + 
                    ", specified = " + specified);
        }
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        if(value == null) throw new XmlException("Attribute with name " + 
                name + " has no value",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        _attributes.putAt(name, value);
    }

    /**
     * Append the given character bytes to the character data of the current
     * XML element.
     */
    public void charData(char c[], int offset, int length)
            throws Exception {
        String s = new String(c, offset, length);
        _currentElement.appendPCData(s);
    }

    /** 
     * Get the current public ID path. 
     * @see #resolveEntity
     */ 
    public String getPublicIDPath() {
        return _publicIDPath;
    }

    /**
     * End the document
     * If we've finished the parse and didn't get back to the root of the
     * parse tree, then throw an exception.
     */
    public void endDocument() throws Exception {
        //FIXME: actually do what the documentation says.
    }

    /**
     * Move up one level in the parse tree.
     */
    public void endElement(String name) throws Exception {
        if(DEBUG)
            System.out.println("Ending Element:" + name);
        XMLElement parent = _currentElement.getParent();
        _currentElement = parent;
    }

    /**
     * Move up one level in the external entity tree.
     * @exception XmlException If given URI was not the URI that was expected,
     * based on the external entity tree. 
     */
    public void endExternalEntity(String URI) throws Exception {
        String _currentElement = _currentExternalEntity();
        if(DEBUG)
            System.out.println("endExternalEntity: URI=\"" + URI + "\"\n");
        if(!_currentElement.equals(URI))
            throw new XmlException("Entities out of order",
                    _currentElement,
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        _externalEntities.removeFirst();
    }

    /**
     * Throw an exception.
     * @exception XmlException If called.
     */
    public void error(String message, String sysid,
            int line, int column) throws Exception {
                throw new XmlException(message, sysid, line, column);
    }

    /**
     * Parse the file located at the given url.
     *
     * @return the XMLElement that contains the root of the parse tree.
     * @exception Exception If the parser fails.  Regrettably, the Microstar
     * &AElig;lfred parser is not more specific about what exceptions
     * it might throw.
     */
    public XMLElement parse(String url) throws Exception {
        try {
            _parser.setHandler(this);
            _parser.parse(url, null, (String)null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        _rootElement.setXMLFileLocation(url);
        return _rootElement;
    }

    /**
     * Parse the given stream, using the url associated with this library
     * to expand any external references within the XML.  
     *
     * @param url The context URL.
     * @param input The stream from which to read XML.
     * @return The XMLElement that contains the root of the parse tree.
     * @exception Exception If the parser fails.  Regrettably, the Microstar
     * &AElig;lfred parser is not more specific about what exceptions
     * it might throw.
     */
    public XMLElement parse(String url, InputStream is)
            throws Exception {
        try {
            _parser.setHandler(this);
            _parser.parse(url, null, is, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        _rootElement.setXMLFileLocation(url);
        return _rootElement;
    }

    /**
     * Attempt resolve the public ID representing an XML external entity
     * into a valid string url.  All public external entities are located
     * relative to the Ptolemy II classpath in the public ID path directory.
     * This path can be set using the setPublicIDPath method.
     *  
     * @return The "PTII" environment variable appended by the public ID path 
     * and the public ID path, or the system ID if the environment variable 
     * is not set, or the public ID is null.
     */
    public Object resolveEntity(String pubID, String sysID)
            throws Exception {
        if (DEBUG) {
            System.out.println("resolveEntity: " + pubID + " : " + sysID);
        }

        String result;
        // Use System ID if the public one is unknown
        if(pubID == null) {
            result = sysID;
        } else {

            // Construct the path to the DTD file. The PTII root MUST be
            // defined as a system property (this can be done by using
            // the -D option to java).
            String ptII = System.getProperty("PTII");
            if(ptII == null) {
                ptII = "UNKNOWN";
            }

            StringBuffer dtdPath = 
                new StringBuffer(ptII);

            if (DEBUG) {
                System.out.println("dtdPath = " + dtdPath);
            }

            // Use System ID if there's no PTII environment variable
            if(dtdPath.toString().equals("UNKNOWN")) {
                result = sysID;
            } else {
            
                // Always use slashes as file separator, since this is a URL
                //String fileSep = 
                // java.lang.System.getProperty("file.separator");
                String fileSep = "/";
                
                // Construct the URL
                int last = dtdPath.length() - 1;
                if (dtdPath.charAt(last) != fileSep.charAt(0)) {
                    dtdPath.append(fileSep);
                }
                //FIXME this seems like a bad Idea to hardwire in.
                dtdPath.append(getPublicIDPath() + pubID);
                
                // Windows is special. Very special.
                if (System.getProperty("os.name").equals("Windows NT")) {
                    result = "file:/" + dtdPath;
                } else {
                    result = "file:" + dtdPath;
                }
            }
        }
        if (DEBUG) System.out.println("resolveEntity result: " + result);
        return result;
    }

    /** 
     * Set the public ID path.
     * @see #resolveEntity
     */
    public void setPublicIDPath(String path) {
        _publicIDPath = path;
    }
        
    /**
     * Start a document.  This method is called just before the parser
     * attempts to read the first entity (the root of the document).
     * It is guaranteed that this will be the first method called.
     * Initialize the parse tree to contain no elements.
     */
    public void startDocument() {
        if(DEBUG) System.out.println("-- Starting Document.");
        _attributes = (LLMap) new LLMap();
        _rootElement = null;
    }

    /** 
     * Start an element.
     * This is called at the beginning of each XML
     * element.  By the time it is called, all of the attributes
     * for the element will already have been reported using the
     * attribute() method.  
     * Create a new XMLElement to represent the element. 
     * Set the attributes of the new XMLElement equal
     * to the attributes that have been accumulated since the last
     * call to this method.  If this is the first element encountered
     * during this parse, set the root of the parse tree equal to the
     * newly created element.
     * Descend the parse tree into the new element
     *
     * @param name the element type of the element that is beginning.
     */
    public void startElement(String name) {
        XMLElement e;
        if(DEBUG)
            System.out.println("Starting Element:"+name);

        e = new XMLElement(name, _attributes);
        e.setParent(_currentElement);
        if(_currentElement == null)
            _rootElement = e;
        else
            _currentElement.addChildElement(e);
        _currentElement = e;
        _attributes = (LLMap) new LLMap();
    }

    /**
     * Move down one level in the entity tree.
     */
    public void startExternalEntity(String URI) throws Exception {
        if(DEBUG)
            System.out.println("startExternalEntity: URI=\"" + URI + "\"\n");
        _externalEntities.insertFirst(URI);
    }

    /**
     * The default path that is appended to the classpath to resolve public 
     * IDs
     */ 
    public static final String DEFAULTPUBLICIDPATH = "ptolemy/schematic/lib/";

    protected String _currentExternalEntity() {
        if(DEBUG)
            System.out.println("currentExternalEntity: URI=\"" +
                    (String)_externalEntities.first() + "\"\n");
        return (String)_externalEntities.first();
    }

    /* A map for accumulating the XML attributes before the start of the next
     * entity.  This is a map from a String representing the attribute's name
     * to a string representing the attribute's value.
     */
    private LLMap _attributes;

    /* The current element being created in the parse tree.
     */
    private XMLElement _currentElement;

    /* The contained parser object.
     */
    private XmlParser _parser = new XmlParser();

    /* The current public ID path.
     * 
     */
    private String _publicIDPath = DEFAULTPUBLICIDPATH;

    /* The root of the parse tree.
     */
    private XMLElement _rootElement;

    /* This linkedlist contains the current path in the tree of
     * XML external entities being parsed.  The current external entity
     * is first in the list.
     */
    private LinkedList _externalEntities = new LinkedList();

    /* If true, then print debugging messages for each tag and attribute 
     * parsed.
     */
    private static final boolean DEBUG = false;
}
