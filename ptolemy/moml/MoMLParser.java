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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Configurable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.expr.Variable;

// Java imports.
import java.awt.Container;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Stack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.io.InputStream;
import java.io.Reader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringReader;
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

    /** Construct a parser that creates a new workspace into which to
     *  put the entities created by the parse() method.
     */
    public MoMLParser() {
        this(null);
    }

    /** Construct a parser that creates entities
     *  in the specified workspace.  If the argument is null,
     *  create a new workspace with an empty name.
     *  @param workspace The workspace into which to place entities.
     */
    public MoMLParser(Workspace workspace) {
        this(workspace, null);
    }

    /** Construct a parser that creates entities
     *  in the specified workspace, and if any of these entities implements
     *  the Placeable interface, then places the entity in the specified
     *  panel.  If the workspace argument is null, then
     *  create a new workspace with an empty name.
     *  If the panel argument is null, then entities implenting Placeable
     *  are not placed.
     *  @param workspace The workspace into which to place entities.
     *  @param panel The panel into which to place Placeable entities.
     */
    public MoMLParser(Workspace workspace, Container panel) {
        super();
        if (workspace == null) {
            // NOTE: Workspace has no name, to ensure that full names
            // of enties conform to MoML standard of starting with a
            // leading period.
            workspace = new Workspace();
        }
        _workspace = workspace;
        _panel = panel;
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
        // If we haven't initialized _currentCharData, then we don't
        // care about character data, so we ignore it.
        if (_currentCharData != null) {
            _currentCharData.append(chars, offset, length);
        }
    }

    /** End the document.  In this implementation, do nothing.
     *  &AElig;lfred will call this method once, when it has
     *  finished parsing the XML document.
     *  It is guaranteed that this will be the last method called.
     */
    public void endDocument() throws Exception {
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized properties
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("configure")) {
            // NOTE: There doesn't appear to be any more direct way to
            // do this in Java (!).
            byte[] bytes = _currentCharData.toString().getBytes();
            InputStream stream = new ByteArrayInputStream(bytes);
            ((Configurable)_current).configure(_base, stream);

        } else if (elementName.equals("doc")) {
            // Use a special property to contain the text.
            // Note that an object may contain several documentation
            // properties.
            _checkClass(_current, NamedObj.class,
                    "Element \"doc\" found inside an element that "
                    + "is not a NamedObj. It is: "
                    + _current);
            NamedObj current = (NamedObj)_current;
            String name = current.uniqueName("_doc_");
            Documentation doc = new Documentation(current, name);
            doc.setValue(_currentCharData.toString());
        } else if (
                elementName.equals("property")
                || elementName.equals("class")
                || elementName.equals("director")
                || elementName.equals("entity")
                || elementName.equals("port")
                || elementName.equals("relation")
                || elementName.equals("rendition")
                || elementName.equals("vertex")) {
            try {
                _current = _containers.pop();
            } catch (EmptyStackException ex) {
                // We are back at the top level.  Ignore.
            }
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

    /** Parse the MoML file at the given URL, which may be a file
     *  on the local file system, using the specified base
     *  to expand any relative references within the MoML file.
     *  That is, relative URLs are interpreted relative to the
     *  first argument. For example, the base might be the document
     *  base of an applet.  An applet might use
     *  this method as follows:
     *  <pre>
     *     MoMLParser parser = new MoMLParser();
     *     URL docBase = getDocumentBase();
     *     URL xmlFile = new URL(docBase, modelURL);
     *     NamedObj toplevel = parser.parse(docBase, xmlFile);
     *  </pre>
     *  If the first argument to parse() is null, then it is assumed that
     *  all URLs in the MoML file are absolute.
     *  <p>
     *  This version of the parse() method records the URL from which
     *  the file is read in the top-level entity that is created using
     *  the Origin attribute.  FIXME: More information here.
     *  <p>
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid MoML file.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public NamedObj parse(URL base, URL input)
            throws Exception {
        return parse(base, input.openStream());
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
     *     NamedObj toplevel = parser.parse(docBase, xmlFile.openStream());
     *  </pre>
     *  If the first argument to parse() is null, then it is assumed that
     *  all URLs in the MoML file are absolute.
     *  <p>
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid MoML file.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public NamedObj parse(URL base, InputStream input)
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

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  That is, relative URLs are interpreted relative to the
     *  first argument. For example, it might be the document
     *  base of an applet.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent a valid MoML file.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param reader The reader from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public NamedObj parse(URL base, Reader reader) throws Exception {
        _parser.setHandler(this);
        _base = base;
        if (base == null) {
            _parser.parse(null, null, reader);
        } else {
            _parser.parse(base.toExternalForm(), null, reader);
        }
        return _toplevel;
    }

    /** Parse the given string, which contains MoML
     *  (it does not need to include the "&lt;?xml ... &gt;"
     *  element nor the DOCTYPE specification).
     *  The top-level element must be "model" or "class".
     *  If there are external references in the MoML, they are interpreted
     *  relative to the current working directory.
     *  A variety of exceptions might be thrown if the parsed
     *  data does not represent valid MoML data.
     *  @param input The string from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception If the parser fails.
     */
    public NamedObj parse(String input) throws Exception {
        URL base = null;
        // Use the current working directory as a base.
        String cwd = System.getProperty("user.dir");
        if (cwd != null) {
            base = new URL("file", null, cwd);
        }
        return parse(base, new StringReader(input));
    }

    /** Resolve an external entity.  If the first argument is the
     *  name of the MoML PUBLIC DTD ("-//UC Berkeley//DTD MoML 1//EN"),
     *  then return a StringReader
     *  that will read the locally cached version of this DTD
     *  (the public variable MoML_DTD_1). Otherwise, return null,
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
        if (publicID != null &&
                publicID.equals("-//UC Berkeley//DTD MoML 1//EN")) {
            // This is the generic MoML DTD.
            return new StringReader(MoML_DTD_1);
        } else {
            return null;
        }
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
        _currentElement = elementName;
        try {
            // NOTE: The elements are alphabetical below...
            // NOTE: I considered using reflection to invoke a set of
            // methods with names that match the element names.  However,
            // since we can't count on the XML parser to enforce the DTD,
            // this seems dangerous.  It could result in being able to write
            // an XML that would call methods of this class that are not
            // intended to be called, simply by putting in an element
            // whose name matches the method name.  So instead, we do
            // a dumb if...then...elseif... chain with string comparisons.
            if (elementName.equals("property")) {
                String propertyName = (String)_attributes.get("name");
                _checkForNull(propertyName,
                        "No name for element \"property\"");
                String value = (String)_attributes.get("value");
                _checkClass(_current, NamedObj.class,
                        "Element \"property\" found inside an element that "
                        + "is not a NamedObj. It is: "
                        + _current);
                Object property = (Attribute)
                        ((NamedObj)_current).getAttribute(propertyName);

                String className = (String)_attributes.get("class");
                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className);
                }

                if (property == null) {
                    // No previously existing attribute with this name.
                    _checkForNull(newClass,
                            "Cannot create property without a class name.");

                    // Invoke the constructor.
                    Object[] arguments = new Object[2];
                    arguments[0] = _current;
                    arguments[1] = propertyName;
                    property = _createInstance(newClass, arguments);

                    if (value != null) {
                        if (!(property instanceof Variable)) {
                            throw new XmlException("Property is not an "
                            + "instance of Variable, so can't set value.",
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                        }
                        ((Variable)property).setExpression(value);
                    }
                } else {
                    // Previously existing property with this name.
                    if (newClass != null) {
                        // Check that it has the right class.
                        _checkClass(property, newClass,
                                "property named \"" + propertyName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                    // If value is null and the property already
                    // exists, then there is nothing to do.
                    if (value != null) {
                        _checkClass(property, Variable.class,
                               "property named \"" + propertyName
                               + "\" is not an instance of Variable,"
                               + " so its value cannot be set.");
                        ((Variable)property).setExpression(value);
                    }
                 }
                 _containers.push(_current);
                 _current = property;

             } else if (elementName.equals("class")) {
                String className = (String)_attributes.get("extends");
                _checkForNull(className,
                        "Missing extends attribute for element \"class\"");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"class\"");
                NamedObj newEntity;
                if (_current != null) {
                    _checkClass(_current, CompositeEntity.class,
                           "Element \"class\" found inside an element that "
                           + "is not a CompositeEntity. It is: "
                           + _current);
                    _containers.push(_current);
                    newEntity = _createEntity(className, entityName);
                } else {
                    newEntity = _createEntity(className, entityName);
                    _toplevel = newEntity;
                }
                newEntity.setMoMLElementName("class");
                _current = newEntity;

            } else if (elementName.equals("configure")) {
                String source = (String)_attributes.get("source");
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable. It is: "
                        + _current);
                if (source != null) {
                    URL xmlFile = new URL(_base, source);
                    InputStream stream = xmlFile.openStream();
                    ((Configurable)_current).configure(_base, stream);
                }
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("director")) {
                // NOTE: We do not check for a previously existing director.
                // There is presumably no harm in just creating a new one.
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                        + "is not a CompositeActor. It is: "
                        + _current);
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = dirName;
                _containers.push(_current);
                Class newClass = Class.forName(className);
                _current = _createInstance(newClass, arguments);

            } else if (elementName.equals("doc")) {
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("entity")) {
                String className = (String)_attributes.get("class");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"entity\"");
                _checkClass(_current, CompositeEntity.class,
                        "Element \"entity\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                NamedObj newEntity = _createEntity(className, entityName);
                if (_panel != null && newEntity instanceof Placeable) {
                    ((Placeable)newEntity).place(_panel);
                }
                _containers.push(_current);
                _current = newEntity;

            } else if (elementName.equals("import")) {
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"import\"");

                // If the base is specified, use that.
                // Otherwise, use this document's base.
                String baseSpec = (String)_attributes.get("base");
                URL base = _base;
                if (baseSpec != null) {
                    base = new URL(_base, baseSpec);
                }

                // Read external model definition.
                MoMLParser newParser = new MoMLParser(_workspace);
                URL xmlFile = new URL(base, source);
                NamedObj reference =
                        newParser.parse(base, xmlFile.openStream());
                if (_imports == null) {
                    _imports = new LinkedList();
                }
                // Put the entity at the top of the list so that if there
                // are name duplications in the imports, the most recent
                // import prevails.
                _imports.add(0, reference);

                if (_current instanceof NamedObj) {
                    NamedObj container = (NamedObj)_current;
                    Import attr = new Import(container,
                           container.uniqueName("_import")); 
                    attr.setSource(source);
                    attr.setBase(_base);
                }

            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                String relationName = (String)_attributes.get("relation");
                _checkForNull(relationName,
                        "No relation for element \"link\"");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Get relation
                Relation tmpRelation = context.getRelation(relationName);
                _checkForNull(tmpRelation, "No relation named \"" +
                        relationName + "\" in " + context.getFullName());
                ComponentRelation relation = (ComponentRelation)tmpRelation;

                port.link(relation);

             } else if (elementName.equals("location")) {
                String xSpec = (String)_attributes.get("x");
                String ySpec = (String)_attributes.get("y");
                String zSpec = (String)_attributes.get("z");
                _checkForNull(xSpec, "No x attribute for element \"location\"");
                _checkClass(_current, Locatable.class,
                       "Element \"location\" found inside an element that "
                       + "is not Locatable. It is: "
                       + _current);

                int x = Integer.parseInt(xSpec);
                if (ySpec != null) {
                    int y = Integer.parseInt(ySpec);
                    if (zSpec != null) {
                        // Have three dimensions.
                        int z = Integer.parseInt(zSpec);
                        int[] location = {x, y, z};
                        ((Locatable)_current).setLocation(location);
                    } else {
                        // Have two dimensions.
                        int[] location = {x, y};
                        ((Locatable)_current).setLocation(location);
                    }
                } else {
                    // Have one dimension.
                    int[] location = {x};
                    ((Locatable)_current).setLocation(location);
                }

            } else if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"model\"");
                String modelName = (String)_attributes.get("name");
                _checkForNull(modelName, "No name for element \"model\"");

                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                Class newClass = Class.forName(className);
                _toplevel = (NamedObj)_createInstance(newClass, arguments);
                _toplevel.setName(modelName);
                _toplevel.setMoMLElementName("model");
                _current = _toplevel;

            } else if (elementName.equals("port")) {
                String className = (String)_attributes.get("class");
                String portName = (String)_attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                _checkClass(_current, Entity.class,
                        "Element \"port\" found inside an element that "
                        + "is not an Entity. It is: "
                        + _current);
                Entity container = (Entity)_current;

                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className);
                }
                Port port = container.getPort(portName);
                if (port != null) {
                    if (newClass != null) {
                        // Previously existing port with the specified name.
                        _checkClass(port, newClass,
                                "port named \"" + portName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                } else {
                    // No previously existing port with this name.
                    _checkForNull(className, "No class for element \"port\"");
                    Object[] arguments = new Object[2];
                    arguments[0] = container;
                    arguments[1] = portName;
                    port = (Port)_createInstance(newClass, arguments);
                }
                _containers.push(_current);
                _current = port;

                if (port instanceof IOPort) {
                    String direction = (String)_attributes.get("direction");
                    if (direction != null) {
                        IOPort ioport = (IOPort)port;
                        ioport.setOutput(direction.equals("output")
                                || direction.equals("both"));
                        ioport.setInput(direction.equals("input")
                                || direction.equals("both"));
                    }
                }

            } else if (elementName.equals("relation")) {
                String className = (String)_attributes.get("class");
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName, "No name for element \"relation\"");
                _checkClass(_current, CompositeEntity.class,
                        "Element \"relation\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                CompositeEntity container = (CompositeEntity)_current;
                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className);
                }
                Relation relation = container.getRelation(relationName);
                if (relation == null) {
                    // No previous relation with this name.
                    _checkForNull(newClass,
                            "No class for element \"relation\"");
                    Object[] arguments = new Object[2];
                    arguments[0] = (CompositeEntity)_current;
                    arguments[1] = relationName;
                    _containers.push(_current);
                    _current = _createInstance(newClass, arguments);
                } else {
                    // Previously existing relation with the specified name.
                    if (newClass != null) {
                        _checkClass(relation, newClass,
                                "relation named \"" + relationName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                    _containers.push(_current);
                    _current = relation;
                }

            } else if (elementName.equals("rendition")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"rendition\"");

                _checkClass(_current, NamedObj.class,
                        "Element \"rendition\" found inside an element that "
                        + "is not a NamedObj. It is: "
                        + _current);

                Object[] arguments = new Object[2];
                arguments[0] = (NamedObj)_current;
                arguments[1] = "_icon";

                _containers.push(_current);
                Class newClass = Class.forName(className);
                _current = _createInstance(newClass, arguments);

            } else if (elementName.equals("vertex")) {
                String vertexName = (String)_attributes.get("name");
                _checkForNull(vertexName, "No name for element \"vertex\"");

                _checkClass(_current, Relation.class,
                        "Element \"vertex\" found inside an element that "
                        + "is not a Relation. It is: "
                        + _current);

                // Create an instance of Vertex and attach it to the Relation.
                Vertex vertex = new Vertex((Relation)_current, vertexName);

                _containers.push(_current);
                _current = vertex;

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

                // FIXME: While debugging, we print a stack trace here.
                // This is because XmlException loses 
                System.err.println("******** original error:");
                ex.printStackTrace();

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
    ////                         public members                    ////

    /** The standard MoML DTD, represented as a string.  This is used
     *  to parse MoML data when a compatible PUBLIC DTD is specified.
     */
    public static String MoML_DTD_1 = "<!ELEMENT model (property | class | configure | doc | director | entity | import | link | relation)*><!ATTLIST model name CDATA #REQUIRED class CDATA #REQUIRED><!ELEMENT class (property | configure | director | doc | entity | link)*><!ATTLIST class name CDATA #REQUIRED extends CDATA #REQUIRED><!ELEMENT configure (#PCDATA)><!ATTLIST configure source CDATA #IMPLIED><!ELEMENT director (property | configure)*><!ATTLIST director name CDATA \"director\" class CDATA #REQUIRED><!ELEMENT doc (#PCDATA)><!ELEMENT entity (property | class | configure | doc | director | entity | rendition | relation)*><!ATTLIST entity name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT import EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT link EMPTY><!ATTLIST link port CDATA #REQUIRED relation CDATA #REQUIRED vertex CDATA #IMPLIED><!ELEMENT location EMPTY><!ATTLIST location x CDATA #REQUIRED y CDATA #IMPLIED z CDATA #IMPLIED><!ELEMENT port (doc | configure)*><!ATTLIST port name CDATA #REQUIRED class CDATA #IMPLIED direction (input | output | both) #IMPLIED><!ELEMENT property (doc | configure)*><!ATTLIST property class CDATA #IMPLIED name CDATA #REQUIRED value CDATA #IMPLIED><!ELEMENT relation (vertex*)><!ATTLIST relation name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT rendition (configure | location)*><!ATTLIST rendition class CDATA #REQUIRED><!ELEMENT vertex (location?)><!ATTLIST vertex name CDATA #REQUIRED pathTo CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/moml/moml.dtd.  If modified, it needs to be also
    // updated at ptweb/archive/moml.dtd.

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

    // Create a new entity from the specified class name, give
    // it the specified entity name, and specify that its container
    // is the current container object.  If the current container
    // already contains an entity with the specified name and class,
    // then return that entity.  If the class name matches
    // an entity that has been previously created (by an absolute
    // or relative name), then that entity is cloned.  Otherwise,
    // the class name is interpreted as a Java class name and we
    // attempt to construct the entity.  This method assumes that
    // _current is an instance of CompositeEntity, or a class cast
    // exception is thrown.  It also assumes that the class is
    // a subclass of NamedObj, or a ClassCastException will be thrown.
    private NamedObj _createEntity(String className, String entityName)
                 throws Exception {
        CompositeEntity container = (CompositeEntity)_current;
        ComponentEntity previous = null;
        if (container != null) {
            previous = container.getEntity(entityName);
        }
        Class newClass = null;
        ComponentEntity reference = null;
        if (className != null) {
            reference = _searchForEntity(className);
            if (reference == null) {
                newClass = Class.forName(className);
            }
        }
        if (previous != null) {
            if (newClass != null) {
                _checkClass(previous, newClass,
                        "entity named \"" + entityName
                        + "\" exists and is not an instance of "
                        + className);
            }
            return previous;
        }

        // No previous entity.  Class name is required.
        _checkForNull(className, "Cannot create entity without a class name.");

        // Next check to see if the class extends a named entity.
        if (reference == null) {
            // Not a named entity. Invoke the class loader.
            if (_current != null) {
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = entityName;
                return (NamedObj)_createInstance(newClass, arguments);
            } else {
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                NamedObj result =
                        (NamedObj)_createInstance(newClass, arguments);
                result.setName(entityName);
                return result;
            }
        } else {
            // Extending a previously defined entity.  Clone it.
            ComponentEntity newEntity = (ComponentEntity)reference.clone();
            // The master may have an entity name "class" (or "model"?), and
            // that name will be cloned, so we need to change this.
            newEntity.setMoMLElementName("entity");
            
            // Set the name of the clone.
            // NOTE: The container is null, so there will be no
            // name conflict here.  If we were to set the name after
            // setting the container, we could get a spurious name conflict.
            newEntity.setName(entityName);

            // Set the container of the clone.
            newEntity.setContainer(container);
            
            return newEntity;
        }
    }

    // Create an instance of the specified class name by finding a
    // constructor that matches the specified arguments.
    // @param newClass The class.
    // @param arguments The constructor arguments.
    // @throws Exception If no matching constructor is found, or if
    //  invoking the constructor triggers an exception.
    private Object _createInstance(Class newClass, Object[] arguments)
            throws Exception {
        Constructor[] constructors = newClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != arguments.length) continue;
            boolean match = true;
            for (int j = 0; j < parameterTypes.length; j++) {
                if (!(parameterTypes[j].isInstance(arguments[j]))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return constructor.newInstance(arguments);
            }
        }
        // If we get here, then there is no matching constructor.
        throw new XmlException("Failed to construct " + newClass.getName(),
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
    }

    // Return the port corresponding to the specified port name in the
    // specified composite entity.  If the port belongs directly to the
    // composite entity, then the argument is a simple name.  If the
    // port belongs to a component entity, then the name is the entity
    // name, a period, and the port name.
    // Throw an exception if there is no such port.
    // The returned value is never null.
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        ComponentPort port = (ComponentPort)context.getPort(portspec);
        _checkForNull(port, "No port named \"" + portspec
                + "\" in " + context.getFullName());
        return (ComponentPort)port;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a component entity with that name.
    // Return null if it is not found.
    private ComponentEntity _searchForEntity(String name) {
        // If the name is absolute, we first have to find a
        // name from the imports that matches.
        if (name.startsWith(".")) {
            // Name is absolute.
            String topLevelName;
            int nextPeriod = name.indexOf(".", 1);
            if (nextPeriod < 1) {
                topLevelName = name.substring(1);
            } else {
                topLevelName = name.substring(1, nextPeriod);
            }
            Iterator entries = _imports.iterator();
            while (entries.hasNext()) {
                Object possibleCandidate = entries.next();
                if (possibleCandidate instanceof ComponentEntity) {
                    ComponentEntity candidate =
                            (ComponentEntity)possibleCandidate;
                    if (candidate.getName().equals(topLevelName)) {
                        if (nextPeriod < 1) {
                            // Found a match.
                            return candidate;
                        } else {
                            if (candidate instanceof CompositeEntity) {
                                ComponentEntity result =
                                ((CompositeEntity)candidate).getEntity(
                                        name.substring(nextPeriod + 1));
                                if (result != null) {
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } else {
            // Name is relative.
            if (_current instanceof CompositeEntity) {
                return ((CompositeEntity)_current).getEntity(name);
            }
            return null;
        }
    }
           
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Attributes associated with an entity.
    private Map _attributes;

    // Base for relative URLs.
    private URL _base;

    // The stack of objects that contain the current one.
    private Stack _containers = new Stack();

    // The current object in the hierarchy.
    private Object _current;

    // The current character data for the current element.
    private StringBuffer _currentCharData;

    // The relation for the currently active connection.
    private ComponentRelation _currentConnection;

    // The latest element seen by startElement.
    private String _currentElement;

    // List of top-level entities imported via import element.
    private List _imports;

    // The manager for this model.
    private Manager _manager;

    // The graphical container into which to place Placeable entities.
    private Container _panel;

    // The parser.
    private XmlParser _parser = new XmlParser();

    // Top-level entity.
    private NamedObj _toplevel = null;

    // The workspace for this model.
    private Workspace _workspace;

    // The external entities being parsed.
    private Stack _externalEntities = new Stack();
}
