/* A parser for MoML (model markup language)

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

package ptolemy.moml;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.Variable;

// Java imports.
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.io.InputStream;
import java.io.FileInputStream;

// XML imports.
import com.microstar.xml.*;


//////////////////////////////////////////////////////////////////////////
//// MoMLParser
/**
This class constructs Ptolemy II models from specifications
in MoML (model markup language), which is based on XML.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to call its parse() method.
The returned value is top-level composite entity of the model.

@author Edward A. Lee, Steve Neuendorffer, John Reekie
@version $Id$
*/
public class MoMLParser extends HandlerBase {

    /**
     * Create a MoMLParser
     */
    public MoMLParser() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
            throws XmlException {
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
        _attributes.put(name, value);
    }

    /** FIXME: Currently, character data is ignored.
    public void charData(char c[],int offset, int length)
    throws Exception {
        String s = new String(c,offset,length);
        current.appendPCData(s);
    }
     */

    /**
     * Implement com.microstar.xml.XMLHandler.endDocument
     * If we've finished the parse and didn't get back to the root of the
     * parse tree, then something is wrong, and throw an exception.
       FIXME
     */
    public void endDocument() throws Exception {
System.out.println("----------- top level: " + _toplevel.description());
    }

    /**
     * Implement com.microstar.xml.XMLHandler.endElement
     * Move up one level in the parse tree and apply any semantic meaning
     * that the element that is ending might have within its parent.  For
     * example, if the element is an Icon contained within an IconLibrary,
     * then the icon should be added to the library's list of icons.
     */
    public void endElement(String elementName) throws Exception {
        if(DEBUG) System.out.println("Ending Element:" + elementName);
        if (elementName.equals("director")
                || elementName.equals("actor")) {
            _current = (NamedObj)_containers.pop();
        } else if (elementName.equals("connection")) {
            _currentConnection = null;
        }
    }

    /**
     * Implement com.microstr.xml.XMLHandler.endExternalEntity
     * move up one leve in the entity tree.
     */
    public void endExternalEntity(String URI) throws Exception {
        String current = _currentExternalEntity();
        if(DEBUG)
            System.out.println("endExternalEntity: URI=\"" + URI + "\"\n");
/* FIXME
        if(!current.equals(URI))
            throw new XmlException("Entities out of order",
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        sysids.removeFirst();
*/
    }

    /**
     * Implement com.microstar.xml.XMLHandler.error
     * @throws XmlException if called.
     */
    public void error(String message, String sysid,
            int line, int column) throws XmlException {
        if (DEBUG) {
            System.out.println("XML error at line " + line + ", column "
            + column + " of " + sysid);
        }
        throw new XmlException(message, sysid, line, column);
    }

    /** Parse the MoML file with the given URL.
     *  @param url The URL for an a MoML file.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception if the parser fails.  Regrettably, the Microstar
     *   &AElig;lfred parser is not more specific about what exceptions
     *   it might throw.
     */
// FIXME-- handle exceptions better...
    public CompositeActor parse(String url) throws Exception {
        _parser.setHandler(this);
        _parser.parse(url, null, (String)null);
        return _toplevel;
    }

    /** Parse the given stream, using the specified url as the context
     *  to expand any external references within the MoML file.
     *  For example, the context might be the document base of an
     *  applet.
     *  @param url The context URL.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception if the parser fails.  Regrettably, the Microstar
     *   &AElig;lfred parser is not more specific about what exceptions
     *   it might throw.
     */
// FIXME-- handle exceptions better...
    public CompositeActor parse(String url, InputStream input)
            throws Exception {
        _parser.setHandler(this);
        _parser.parse(url, null, input, null);
        return _toplevel;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.resolveEntity
     * If no public ID is given, then return the system ID.
     * Otherwise, construct a local absolute URL by appending the
     * public ID to the location of the XML files.
     */
    public Object resolveEntity(String pubID, String sysID)
            throws Exception {
        if (DEBUG) {
            System.out.println("resolveEntity: " + pubID + " : " + sysID);
        }
        String result;
        StringBuffer dtdPath = new StringBuffer();
        // Use System ID if the public one is unknown.
        if(pubID == null) {
            result = sysID;
        } else {

            // Construct the path to the DTD file. The PTII root MUST be
            // defined as a system property (this can be done by using
            // the -D option to java.
            dtdPath = new StringBuffer(System.getProperty("PTII"));
            System.out.println("dtdPath = " + dtdPath);
            
            //// FIXME FIXME
            //// StringBuffer dtdPath = new StringBuffer(DomainLibrary.getPTIIRoot());
            // StringBuffer dtdPath = new StringBuffer("/users/ptII");
            
            // Use System ID if there's no PTII environment variable
            if(dtdPath.toString().equals("UNKNOWN")) {
                result = sysID;
            } else {

                // Always use slashes as file separator, since this is a URL
                //String fileSep = java.lang.System.getProperty("file.separator");
                String fileSep = "/";
                
                // Construct the URL
                int last = dtdPath.length()-1;
                if (dtdPath.charAt(last) != fileSep.charAt(0)) {
                    dtdPath.append(fileSep);
                }
                dtdPath.append("ptolemy" + fileSep + "schematic" + fileSep);
                dtdPath.append("lib" + fileSep + pubID);
                
                // Windows is special. Very special.
                if (System.getProperty("os.name").equals("Windows NT")) {
                    result = "file:/" + dtdPath;
                } else {
                    result = "file:" + dtdPath;
                }
            }
        }
        if (DEBUG) System.out.println("resolveEntity result: " + dtdPath);
        return result;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        if(DEBUG) System.out.println("-- Starting Document.");
        _attributes = new HashMap();
        _toplevel = null;
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     */
    public void startElement(String elementName) {
        if(DEBUG) System.out.println("Starting Element:" + elementName);
        try {
            if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                String modelName = (String)_attributes.get("name");
                // FIXME: Check for null.
                // FIXME: Need a different name... applet name?
                _workspace = new Workspace(modelName);
                Class toplevelClass = Class.forName(className);
                Class[] argTypes = new Class[1];
                argTypes[0] = Workspace.class;
                Constructor toplevelConstructor
                        = toplevelClass.getConstructor(argTypes);
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                _toplevel = (CompositeActor)
                        toplevelConstructor.newInstance(arguments);
                _manager = new Manager(_workspace, "manager");
                // FIXME: Need a different name.
                _toplevel.setName("top");
                _toplevel.setManager(_manager);
                _current = _toplevel;
            } else if (elementName.equals("director")) {
                String className = (String)_attributes.get("class");
                String dirName = (String)_attributes.get("name");
                // FIXME: Check for null.
                Class dirClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = CompositeActor.class;
                argTypes[1] = String.class;
                Constructor dirConstructor
                        = dirClass.getConstructor(argTypes);
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = dirName;
                _containers.push(_current);
                _current = (NamedObj)dirConstructor.newInstance(arguments);
            } else if (elementName.equals("actor")) {
                String className = (String)_attributes.get("class");
                String actorName = (String)_attributes.get("name");
                // FIXME: Check for null.
                Class actorClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                // FIXME: Is this cast OK? Catch errors.
                argTypes[0] = TypedCompositeActor.class;
                argTypes[1] = String.class;
                Constructor actorConstructor
                        = actorClass.getConstructor(argTypes);
                Object[] arguments = new Object[2];
                arguments[0] = (TypedCompositeActor)_current;
                arguments[1] = actorName;
                _containers.push(_current);
                _current = (NamedObj)actorConstructor.newInstance(arguments);
            } else if (elementName.equals("parameter")) {
                String paramName = (String)_attributes.get("name");
                String paramValue = (String)_attributes.get("value");
                // FIXME: Check for null.
                Variable param = (Variable)_current.getAttribute(paramName);
                // FIXME: Check for null. Cast problem?
if (param == null) {
    System.out.println("No such parameter: " + paramName + " in class "
    + _current.getClass().toString());
}
                param.setExpression(paramValue);
            } else if (elementName.equals("connection")) {
                // FIXME: Check cast below...
                TypedCompositeActor container = (TypedCompositeActor)_current;
                String source = (String)_attributes.get("source");
                String destination = (String)_attributes.get("destination");
                String name = (String)_attributes.get("name");
                // FIXME: check for null.

                // FIXME: Check cast below.
                CompositeEntity context = (CompositeEntity)_current;

                // Parse the source
                int point = source.lastIndexOf(".");
                // FIXME: Make sure the following is in bounds
                String portname = source.substring(point+1);
                String actorname = source.substring(0, point);
                ComponentEntity actor = context.getEntity(actorname);
                // FIXME: Check that above not null.
                // FIXME: Check cast below.
                ComponentPort sourcePort = (ComponentPort)
                        (actor.getPort(portname));

                // Parse the destination
                point = destination.lastIndexOf(".");
                // FIXME: Make sure the following is in bounds
                portname = destination.substring(point+1);
                actorname = destination.substring(0, point);
                actor = context.getEntity(actorname);
                // FIXME: Check that above not null.
                ComponentPort destPort = (ComponentPort)
                        (actor.getPort(portname));

                if (name == null) {
                    _currentConnection = ((CompositeEntity)_current)
                            .connect(sourcePort, destPort);
                } else {
                    _currentConnection = ((CompositeEntity)_current)
                            .connect(sourcePort, destPort, name);
                }
            }
        } catch (ClassNotFoundException ex) {
            // FIXME -- thrown by Class.forName().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            // FIXME -- thrown by getConstructor().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            // FIXME -- thrown by newInstance().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            // FIXME -- thrown by newInstance().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            // FIXME -- thrown by newInstance().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            // FIXME -- thrown if class is not a CompositeActor, or Variable
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (NameDuplicationException ex) {
            // FIXME -- thrown by setName().
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (IllegalActionException ex) {
            // FIXME -- thrown by setManager()
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        _attributes.clear();
    }

    /**
     * implement com.microstar.xml.XMLHandler.startExternalEntity
     * move down one level in the entity tree.
     */
    public void startExternalEntity(String URI) throws Exception {
        if(DEBUG)
            System.out.println("startExternalEntity: URI=\"" + URI + "\"\n");
        sysids.addFirst(URI);
    }

    protected String _currentExternalEntity() {
        if(DEBUG)
            System.out.println("currentExternalEntity: URI=\"" +
                    (String)sysids.getFirst() + "\"\n");
        return (String)sysids.getFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Attributes associated with an entity.
    private Map _attributes;

    // Top-level entity.
    private CompositeActor _toplevel = null;

    // The workspace for this model.
    Workspace _workspace;

    // The manager for this model.
    Manager _manager;

    // The current object in the hierarchy.
    NamedObj _current;

    // The stack of objects that contain the current one.
    Stack _containers = new Stack();

    // The relation for the currently active connection.
    ComponentRelation _currentConnection;

// FIXME...

    /* this linkedlist contains the current path in the tree of
     * entities being parsed.  The leaf is first in the list.
     */
    private LinkedList sysids = new LinkedList();

    private XmlParser _parser = new XmlParser();
    private static final boolean DEBUG = false;
    private String _dtdlocation = null;
}
