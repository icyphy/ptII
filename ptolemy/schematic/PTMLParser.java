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
This class interfaces to the Microstar Aelfred XML parser in order to
parse PTML files.  Calling one of the parse methods will read the XML
input and create a parse tree of XMLElements, returning the root of the
tree.   Some of the nodes in the tree may actually be subclasses of
XMLElement.  The subclass that is created depends solely on the type
of the element.   These subclasses encapsulate the semantic meaning
of the contained parse tree.  For Example an element in the XML document
of type "iconlibrary" will be placed in an instance of the IconLibrary class.
an IconLibrary represents a collection of Icons, and contains methods
to directly access its child elements that represent icons.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLParser extends HandlerBase{

    /**
     * Create a PTMLParser
     */
    public PTMLParser() {
        super();
    }

    /**
     * Implement com.microstar.xml.XMLHandler.attribute
     * Accumulate all the attributes until the next startElement.
     *
     * @throws XmlException if the attribute is not valid.
     */
    public void attribute(String name, String value, boolean specified)
    throws Exception {
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                parser.getLineNumber(),
                parser.getColumnNumber());
        if(value == null) throw new XmlException("Attribute has no value",
                _currentExternalEntity(),
                parser.getLineNumber(),
                parser.getColumnNumber());
        attributes.putAt(name, value);
    }

    /**
     * Implement com.microstar.xml.XMLHandler.charData
     */
    public void charData(char c[],int offset, int length)
    throws Exception {
        String s = new String(c,offset,length);
        current.appendPCData(s);
    }

    /**
     * Implement com.microstar.xml.XMLHandler.endDocument
     * If we've finished the parse and didn't get back to the root of the
     * parse tree, then something is wrong, and throw an exception.
     */
    public void endDocument() throws Exception {
        if(current!=root)
            throw new XmlException(
                    "internal error in PTMLParser",
                    _currentExternalEntity(),
                    parser.getLineNumber(),
                    parser.getColumnNumber());
    }

    /**
     * Implement com.microstar.xml.XMLHandler.endElement
     * Move up one level in the parse tree and apply any semantic meaning
     * that the element that is ending might have within its parent.  For
     * example, if the element is an Icon contained within an IconLibrary,
     * then the icon should be added to the library's list of icons.
     */
    public void endElement(String name) throws Exception {
        XMLElement parent= current.getParent();
        if(parent!=null)
            parent.applySemanticsToChild(current);
        current = parent;
    }

    /**
     * Implement com.microstr.xml.XMLHandler.endExternalEntity
     * move up one leve in the entity tree.
     */
    public void endExternalEntity(String URI) throws Exception {
        if(!_currentExternalEntity().equals(URI))
            throw new XmlException("Entities out of order",
                    _currentExternalEntity(),
                    parser.getLineNumber(),
                    parser.getColumnNumber());
        if(DEBUG)
            System.out.println("endExternalEntity: URI=\"" + URI + "\"\n");
        sysids.removeFirst();
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
     * Parse the URL associated with this library.  The URL should specify
     * an XML file that is valid with IconLibrary.dtd.
     *
     * @return the XMLElement that contains the root of the parse tree.
     * this element will have element type of "document".
     * @throws IllegalActionException if the URL is not valid or the file
     * could not be retrieved.
     * @throws IllegalActionException if the parser fails.
     */
    public XMLElement parse(String url) throws Exception {
        try {
        parser.setHandler(this);
        parser.parse(url, null, (String)null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
    public XMLElement parse(String url, InputStream is)
    throws Exception {
        try {
            parser.setHandler(this);
            parser.parse(url, null, is, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.resolveEntity
     * If a public specifier is given, then append XMLLocation to it.
     */
    public Object resolveEntity(String pubID, String sysID)
            throws Exception {
        if(pubID == null) return sysID;

        // Construct the path to the DTD file
        StringBuffer dtdPath = new StringBuffer(DomainLibrary.getPTIIRoot());
        String fileSep = java.lang.System.getProperty("file.separator");

        int last = dtdPath.length()-1;
        if (dtdPath.charAt(last) != fileSep.charAt(0)) {
            dtdPath.append(fileSep);
        }
        dtdPath.append("ptolemy" + fileSep + "schematic" + fileSep);
        dtdPath.append("lib" + fileSep + pubID);

        return "file:" + dtdPath;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.startDocument
     * Initialize the parse tree to contain no elements.
     */
    public void startDocument() {
        attributes = (HashedMap) new HashedMap();
        root = null;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.startElement
     * Create a new XMLElement, or derived class of XMLElement, based on
     * the element type.   Set the attributes of the new XMLElement equal
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

        if(name.equals("domainlibrary")) {
            e=new DomainLibrary(attributes);
        }
        else if(name.equals("entitytype")) {
            e=new SchematicEntity(attributes);
        }
        else if(name.equals("entitytype")) {
            e=new EntityType(attributes);
        }
        else if(name.equals("icon")) {
            e=new Icon(attributes);
        }
        else if(name.equals("iconlibrary")) {
            e=new IconLibrary(attributes);
        }
        else if(name.equals("parameter")) {
            e=new SchematicParameter(attributes);
        }
        else if(name.equals("port")) {
            e=new SchematicPort(attributes);
        }
        else if(name.equals("relation")) {
            e=new SchematicRelation(attributes);
        }
        else if(name.equals("schematic")) {
            e=new Schematic(attributes);
        }
         else {
            e=new XMLElement(name,attributes);
        }
        e.setParent(current);
        if(current == null)
            root = e;
        else
            current.addChildElement(e);
        current = e;
        attributes = (HashedMap) new HashedMap();
    }

    /**
     * implement com.microstar.xml.XMLHandler.startExternalEntity
     * move down one level in the entity tree.
     */
    public void startExternalEntity(String URI) throws Exception {
        if(DEBUG)
            System.out.println("startExternalEntity: URI=\"" + URI + "\"\n");
        sysids.insertFirst(URI);
    }

    protected String _currentExternalEntity() {
        if(DEBUG)
            System.out.println("currentExternalEntity: URI=\"" +
                    (String)sysids.first() + "\"\n");
        return (String)sysids.first();
    }

    /* this linkedlist contains the current path in the tree of
     * entities being parsed.  The leaf is first in the list.
     */
    private LinkedList sysids = new LinkedList();
    private HashedMap attributes;
    private XMLElement current;
    private XMLElement root;
    private XmlParser parser = new XmlParser();
    private static final boolean DEBUG = false;
    private static final String XMLLOCATION = "file:/users/neuendor/";

}

