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
import java.net.URL;

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
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        // NOTE: value may be null if attribute default is #IMPLIED.
        _attributes.put(name, value);
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the
     *  end element.  Character data appears only in doc elements.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        _currentCharData.append(chars, offset, length);
    }

    /** End the document.  In this implementation, do nothing.
     *  &AElig;lfred will call this method once, when it has
     *  finished parsing the XML document.
     *  It is guaranteed that this will be the last method called.
     */
    public void endDocument() throws Exception {
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized attributes
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("doc")) {
            // Use the special attribute name "_doc" to contain the text.
            // If the attribute already exists, remove it firts.
            _checkClass(_current, NamedObj.class,
                    "Element \"doc\" found inside an element that "
                    + "is not a NamedObj.");
            DocAttribute doc
                    = new DocAttribute((NamedObj)_current,
                    _currentCharData.toString());
        // FIXME: icon should be included.
        } else if (elementName.equals("director")
                || elementName.equals("actor")
                || elementName.equals("parameter")
                || elementName.equals("port")
                || elementName.equals("clone")) {
            _current = _containers.pop();
        } else if (elementName.equals("connection")) {
            _currentConnection = null;
        }
    }

    /** Handle the end of an external entity.  This pops the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void endExternalEntity (String systemId) {
        _externalEntities.pop();
    }

    /** Indicate a fatal XML parsing error.
     *  &AElig;lfred will call this method whenever it encounters
     *  a serious error.  This method simply throws an XmlException.
     *  @param message The error message.
     *  @param systemId The URI of the tntity that caused the error.
     *  @param line The approximate line number of the error.
     *  @param column The approximate column number of the error.
     *  @exception XmlException If called.
     */
    public void error(String message, String sysid,
            int line, int column) throws XmlException {
        throw new XmlException(message, _currentExternalEntity(), line, column);
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  That is, relative URLs are interpreted relative to the
     *  first argument. For example, it might be the document
     *  base of an applet.  For example, an applet might use
     *  this method as follows:
     *  <pre>
     *     MoMLParser parser = new MoMLParser();
     *     URL docBase = getDocumentBase();
     *     URL xmlFile = new URL(docBase, modelURL);
     *     TypedCompositeActor toplevel =
     *             parser.parse(docBase, xmlFile.openStream());
     *  </pre>
     *  If the first argument is null, then it is assumed that
     *  all URLs are absolute.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid MoML file.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public TypedCompositeActor parse(URL base, InputStream input)
            throws Exception {
        _parser.setHandler(this);
        _base = base;
        if (base == null) {
            _parser.parse(null, null, input, null);
        } else {
            _parser.parse(base.toExternalForm(), null, input, null);
        }
        return _toplevel;
    }

    /** Resolve an external entity.  This method returns null,
     *  which has the effect of defering to &AElig;lfred for
     *  resolution of the URI.  Derived classes may return a
     *  a modified URI (a string), an InputStream, or a Reader.
     *  In the latter two cases, the input character stream is
     *  provided.
     *  @param publicId The public identifier, or null if none was supplied.
     *  @param systemId The system identifier.
     *  @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        return null;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        _attributes = new HashMap();
        _toplevel = null;
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        try {
            // NOTE: The elements are alphabetical below...
            if (elementName.equals("actor")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"actor\"");
                String actorName = (String)_attributes.get("name");
                _checkForNull(actorName, "No name for element \"actor\"");

                // Get a constructor for the actor.
                Class actorClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = TypedCompositeActor.class;
                argTypes[1] = String.class;
                Constructor actorConstructor
                        = actorClass.getConstructor(argTypes);

                // Invoke the constructor.
                Object[] arguments = new Object[2];
                _checkClass(_current, TypedCompositeActor.class,
                        "Element \"actor\" found inside an element that "
                        + "is not a TypedCompositeActor.");
                arguments[0] = (TypedCompositeActor)_current;
                arguments[1] = actorName;
                _containers.push(_current);
                _current = actorConstructor.newInstance(arguments);

            } else if (elementName.equals("clone")) {
                String cloneName = (String)_attributes.get("name");
                _checkForNull(cloneName, "No name for element \"clone\"");
                String masterName = (String)_attributes.get("master");
                _checkForNull(masterName, "No master for element \"clone\"");
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"clone\"");

                // Read external model definition and then clone it.
                MoMLParser newParser = new MoMLParser();
                URL xmlFile = new URL(_base, source);
                TypedCompositeActor reference =
                        newParser.parse(_base, xmlFile.openStream());
                // Save the reference in case it is cloned again.
                // FIXME: do this...

                // Clone the master into the current workspace.
                TypedCompositeActor newActor =
                        (TypedCompositeActor)reference.clone(_workspace);

                // Set the container of the clone.
                _checkClass(_current, TypedCompositeActor.class,
                        "Element \"clone\" found inside an element that "
                        + "is not a TypedCompositeActor.");
                newActor.setContainer((TypedCompositeActor)_current);

                // Set the name of the clone.
                newActor.setName(cloneName);

                // Make the clone the current context.
                _containers.push(_current);
                _current = newActor;

            } else if (elementName.equals("configure")) {
                String src = (String)_attributes.get("src");
                _checkForNull(src, "No src for element \"configure\"");
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable.");
                URL xmlFile = new URL(_base, src);
                InputStream stream = xmlFile.openStream();
                ((Configurable)_current).configure(_base, stream);

            } else if (elementName.equals("connection")) {
                String port1Name = (String)_attributes.get("port1");
                _checkForNull(port1Name, "No port1 for element \"connection\"");
                String port2Name = (String)_attributes.get("port2");
                _checkForNull(port2Name, "No port2 for element \"connection\"");
                String name = (String)_attributes.get("name");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"connection\" found inside a container that"
                        + " is not an instance of CompositeEntity.");
                CompositeEntity context = (CompositeEntity)_current;

                // Parse ports
                ComponentPort port1 = _getPort(port1Name, context);
                ComponentPort port2 = _getPort(port2Name, context);

                if (name == null) {
                    _currentConnection = context.connect(port1, port2);
                } else {
                    _currentConnection = context.connect(port1, port2, name);
                }

            } else if (elementName.equals("director")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                Class dirClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = CompositeActor.class;
                argTypes[1] = String.class;
                Constructor dirConstructor
                        = dirClass.getConstructor(argTypes);
                Object[] arguments = new Object[2];
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                        + "is not a CompositeActor.");
                arguments[0] = _current;
                arguments[1] = dirName;
                _containers.push(_current);
                _current = dirConstructor.newInstance(arguments);

            } else if (elementName.equals("doc")) {
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No name for element \"link\"");
                String connectionName = (String)_attributes.get("connection");
                _checkForNull(connectionName,
                        "No connection for element \"link\"");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity.");
                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Get relation
                Relation tmpRelation = context.getRelation(connectionName);
                _checkForNull(tmpRelation, "No relation named \"" +
                        connectionName + "\" in " + context.getFullName());
                ComponentRelation relation = (ComponentRelation)tmpRelation;

                port.link(relation);

            } else if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"model\"");
                String modelName = (String)_attributes.get("name");
                _checkForNull(modelName, "No name for element \"model\"");
                // NOTE: Workspace has no name.
                _workspace = new Workspace();
                Class toplevelClass = Class.forName(className);
                Class[] argTypes = new Class[1];
                argTypes[0] = Workspace.class;
                Constructor toplevelConstructor
                        = toplevelClass.getConstructor(argTypes);
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                _toplevel = (TypedCompositeActor)
                        toplevelConstructor.newInstance(arguments);
                _manager = new Manager(_workspace, "manager");
                _toplevel.setName(modelName);
                _toplevel.setManager(_manager);
                _current = _toplevel;

            } else if (elementName.equals("parameter")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"parameter\"");
                String paramName = (String)_attributes.get("name");
                _checkForNull(paramName, "No name for element \"parameter\"");

                // Get a constructor for the port.
                Class paramClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = NamedObj.class;
                argTypes[1] = String.class;
                Constructor paramConstructor
                        = paramClass.getConstructor(argTypes);

                // Invoke the constructor.
                Object[] arguments = new Object[2];
                _checkClass(_current, NamedObj.class,
                        "Element \"parameter\" found inside an element that "
                        + "is not a NamedObj.");
                arguments[0] = _current;
                arguments[1] = paramName;
                _containers.push(_current);
                // FIXME: How to enforce that the specified class is
                // Variable or a subclass?
                Variable param =
                        (Variable)paramConstructor.newInstance(arguments);
                _current = param;

                String value = (String)_attributes.get("value");
                _checkForNull(value, "No value for element \"parameter\"");
                param.setExpression(value);

            } else if (elementName.equals("port")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"port\"");
                String portName = (String)_attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                // Get a constructor for the port.
                Class portClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = ComponentEntity.class;
                argTypes[1] = String.class;
                Constructor portConstructor
                        = portClass.getConstructor(argTypes);

                // Invoke the constructor.
                Object[] arguments = new Object[2];
                _checkClass(_current, ComponentEntity.class,
                        "Element \"port\" found inside an element that "
                        + "is not a ComponentEntity.");
                arguments[0] = (ComponentEntity)_current;
                arguments[1] = portName;
                _containers.push(_current);
                IOPort port = (IOPort)portConstructor.newInstance(arguments);
                _current = port;

                // FIXME: this should be an enumeration in the dtd.
                String direction = (String)_attributes.get("direction");
                _checkForNull(direction, "No direction for element \"port\"");
                port.setOutput(direction.equals("output")
                        || direction.equals("both"));
                port.setInput(direction.equals("input")
                        || direction.equals("both"));                    

            } else if (elementName.equals("setParameter")) {
                String paramName = (String)_attributes.get("name");
                _checkForNull(paramName,
                        "No name for element \"setParameter\"");
                String paramValue = (String)_attributes.get("value");
                _checkForNull(paramValue,
                        "No value for element \"setParameter\"");
                _checkClass(_current, NamedObj.class,
                        "Element \"setParameter\" found inside an element "
                        + "that is not a NamedObj.");
                Attribute attribute = (Attribute)
                        ((NamedObj)_current).getAttribute(paramName);
                _checkForNull(attribute, "No such parameter: \"" + paramName
                        + "\" in class: " + _current.getClass().toString());
                _checkClass(attribute, Variable.class,
                        "setParameter argument named \"" + paramName
                        + "\" is not an instance of Variable.");
                Variable param = (Variable)attribute;
                param.setExpression(paramValue);
            }
        } catch (InvocationTargetException ex) {
            // A constructor or method invoked via reflection has
            // triggered an exception.
            String msg = "XML element \"" + elementName
                    + "\" triggers exception:\n  "
                    + ex.getTargetException().toString();
            throw new XmlException(msg,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException)ex;
            } else {
                String msg = "XML element \"" + elementName
                        + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg,
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
        }
        _attributes.clear();
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void startExternalEntity (String systemId) {
        _externalEntities.push(systemId);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the the URI for the current external entity.
     *  @return A string giving the URI of the external entity being read,
     *   or null if none.
     */
    protected String _currentExternalEntity() {
        return (String)_externalEntities.peek();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if(!correctClass.isInstance(object)) {
            throw new XmlException(msg,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if(object == null) {
            throw new XmlException(message,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // Return the port corresponding to the specified port name in the
    // specified composite actor.  If the port belongs directly to the
    // composite actor, then the argument is a simple name.  If the
    // port belongs to a component actor, then the name is the actor
    // name, a period, and the port name.
    // Throw an exception if there is no such port.
    // The returned value is never null.
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        int position = portspec.lastIndexOf(".");
        // NOTE: Disallow null strings for names.
        if ((position == 0) || (position == portspec.length() - 1)) {
            throw new XmlException("Invalid port name: \"" + portspec + "\"",
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
        Port port;
        if (position < 0) {
            // No period in the name.
            port = context.getPort(portspec);
            _checkForNull(port, "No port named \"" + portspec
                    + "\" in " + context.getFullName());
        } else {
            // Period in name... port belongs to a contained actor.
            String portname = portspec.substring(position+1);
            String actorname = portspec.substring(0, position);
            ComponentEntity actor = context.getEntity(actorname);
            _checkForNull(actor, "No actor named \"" + actorname
                    + "\" in " + context.getFullName());
            port = actor.getPort(portname);
            _checkForNull(port, "No port named \"" + portname
                    + "\" in " + actor.getFullName());
        }
        return (ComponentPort)port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Attributes associated with an entity.
    private Map _attributes;

    // Base for relative URLs.
    private URL _base;

    // The stack of objects that contain the current one.
    Stack _containers = new Stack();

    // The current object in the hierarchy.
    Object _current;

    // The current character data for the current element.
    StringBuffer _currentCharData;

    // The relation for the currently active connection.
    ComponentRelation _currentConnection;

    // The manager for this model.
    Manager _manager;

    // The parser.
    private XmlParser _parser = new XmlParser();

    // Top-level entity.
    private TypedCompositeActor _toplevel = null;

    // The workspace for this model.
    private Workspace _workspace;

    // The external entities being parsed.
    private Stack _externalEntities = new Stack();
}
